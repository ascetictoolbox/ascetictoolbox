/*
 *  Copyright 2002-2012 Barcelona Supercomputing Center (www.bsc.es)
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package integratedtoolkit.components.scheduler.impl;

import integratedtoolkit.components.impl.TaskScheduler;
import integratedtoolkit.components.scheduler.SchedulerPolicies;
import integratedtoolkit.types.Implementation;
import integratedtoolkit.types.Resource;
import integratedtoolkit.types.ScheduleDecisions;
import integratedtoolkit.types.ScheduleState;
import integratedtoolkit.types.Task;
import integratedtoolkit.types.ResourceDescription;
import integratedtoolkit.util.CoreManager;
import integratedtoolkit.util.TaskSets;
import integratedtoolkit.util.ResourceManager;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.PriorityQueue;
import java.util.TreeSet;

public class DefaultTaskScheduler extends TaskScheduler {

    // Max number of tasks to examine when scheduling
    private static final int MAX_TASK = 10;

    // Object that stores the information about the current project
    private final TaskSets taskSets;

    public DefaultTaskScheduler() {
        super();
        taskSets = new TaskSets();
        schedulerPolicies = new DefaultSchedulerPolicies();
        logger.info("Initialization finished");
    }

    public void resizeDataStructures() {
        super.resizeDataStructures();
        taskSets.resizeDataStructures();
    }

    /**
     ********************************************
     *
     * Pending Work Query
     *
     ********************************************
     */
    public boolean isPendingWork(Integer coreId) {
        return (taskSets.getToRescheduleCount(coreId) + taskSets.getNoResourceCount(coreId) + taskSets.getPriorityCount(coreId) + taskSets.getRegularCount(coreId)) != 0;
    }

    /**
     ********************************************
     *
     * Resource Management
     *
     ********************************************
     */
    @Override
    public void resourcesCreated(String schedulerName, ResourceDescription granted, Integer limitOfTasks) {
        super.resourcesCreated(schedulerName, granted, limitOfTasks);

        if (taskSets.getNoResourceCount() > 0) {
            int[] simTasks = ResourceManager.getResource(schedulerName).getSimultaneousTasks();
            for (int coreId = 0; coreId < CoreManager.coreCount; coreId++) {
                if (taskSets.getNoResourceCount(coreId) > 0 && simTasks[coreId] > 0) {
                    taskSets.resourceFound(coreId);
                }
            }
        }
        scheduleToResource(schedulerName);
    }

    @Override
    public void reduceResource(ResourceDescription reduction) {

    }

    public void removeNode(String hostName) {
        super.removeNode(hostName);
    }

    /**
     ********************************************
     *
     * Task Scheduling
     *
     ********************************************
     */
    public void scheduleTask(Task currentTask) {
        Resource chosenResource = null;
        int coreId = currentTask.getTaskParams().getId();
        if (currentTask.isSchedulingForced()) {
            //Task is forced to run in a given resource
            TreeSet<String> hosts = FTM.getHosts(currentTask.getEnforcingData());
            String chosenResourceName = hosts.first();
            chosenResource = ResourceManager.getResource(chosenResourceName);
            Implementation[] impls = CoreManager.getCoreImplementations(coreId);
            LinkedList<Implementation> runnable = ResourceManager.canRunNow(chosenResource, impls);
            if (runnable.isEmpty()) {
                taskSets.newRegularTask(currentTask);
                if (debug) {
                    logger.debug("Pending: Task(" + currentTask.getId() + ", "
                            + currentTask.getTaskParams().getName() + ") "
                            + "Resource(" + chosenResource + ")");
                }
            } else {
                LinkedList<Implementation> run = schedulerPolicies.sortImplementationsForResource(runnable, chosenResource, profile);
                if (debug) {
                    logger.debug("Match: Task(" + currentTask.getId() + ", "
                            + currentTask.getTaskParams().getName() + ") "
                            + "Resource(" + chosenResource + ")");
                }

                // Request the creation of a job for the task
                sendJob(currentTask, chosenResource, run.getFirst());
            }
        } else {
            // Schedule task
            List<Resource> validResources = ResourceManager.findCompatibleResources(coreId);
            if (validResources.isEmpty()) {
                //There's no point on getting scores, any existing machines can run this task <- score=0
                taskSets.waitWithoutNode(currentTask);
                if (debug) {
                    logger.debug("Blocked: Task(" + currentTask.getId() + ", "
                            + currentTask.getTaskParams().getName() + ") ");
                }
            } else {
                // Try to assign task to available resources
                Implementation[] impls = CoreManager.getCoreImplementations(coreId);
                HashMap<Resource, LinkedList<Implementation>> resourceToImpls = ResourceManager.findAvailableResources(impls, validResources);
                if (!resourceToImpls.keySet().isEmpty()) {
                    PriorityQueue<SchedulerPolicies.Object_Value<Resource>> orderedResources = schedulerPolicies.sortResourcesForTask(currentTask, resourceToImpls.keySet(), profile);
                    chosenResource = (Resource) orderedResources.peek().o;
                    if (debug) {
                        logger.debug("Match: Task(" + currentTask.getId() + ", "
                                + currentTask.getTaskParams().getName() + ") "
                                + "Resource(" + chosenResource + ")");
                    }
                    // Request the creation of a job for the task
                    LinkedList<Implementation> orderedImpls = schedulerPolicies.sortImplementationsForResource(resourceToImpls.get(chosenResource), chosenResource, profile);
                    sendJob(currentTask, chosenResource, orderedImpls.getFirst());
                } else {
                    if (currentTask.getTaskParams().hasPriority()) {
                        taskSets.newPriorityTask(currentTask);
                    } else {
                        taskSets.newRegularTask(currentTask);
                    }
                    if (debug) {
                        logger.debug("Pending: Task(" + currentTask.getId() + ", "
                                + currentTask.getTaskParams().getName() + ")");
                    }
                }
            }
        }
    }

    public void rescheduleTask(Task task, String failedResource) {
        //Rescheduling the failed Task
        // Find available resources that match user constraints for this task
        int coreId = task.getTaskParams().getId();
        Implementation[] impls = CoreManager.getCoreImplementations(coreId);
        List<Resource> validResources = ResourceManager.findCompatibleResources(coreId);
        if (!validResources.isEmpty()) {
            HashMap<Resource, LinkedList<Implementation>> resourceToImpls = ResourceManager.findAvailableResources(impls, validResources);
            resourceToImpls.remove(ResourceManager.getResource(failedResource));

            if (!resourceToImpls.keySet().isEmpty()) {
                PriorityQueue<SchedulerPolicies.Object_Value<Resource>> orderedResources = schedulerPolicies.sortResourcesForTask(task, resourceToImpls.keySet(), profile);
                Resource chosenResource = orderedResources.peek().o;
                if (debug) {
                    logger.debug("Match: Task(" + task.getId() + ", "
                            + task.getTaskParams().getName() + ") "
                            + "Resource(" + chosenResource + ")");
                }
                // Request the creation of a job for the task
                LinkedList<Implementation> orderedImpls = schedulerPolicies.sortImplementationsForResource(resourceToImpls.get(chosenResource), chosenResource, profile);
                sendJobRescheduled(task, chosenResource, orderedImpls.getFirst());
            } else {
                taskSets.newTaskToReschedule(task);
                task.setLastResource(failedResource);
                if (debug) {
                    logger.debug("To Reschedule: Task(" + task.getId() + ", "
                            + task.getTaskParams().getName() + ") ");
                }
            }
        } else {
            taskSets.newTaskToReschedule(task);
            task.setLastResource(failedResource);
            if (debug) {
                logger.debug("To Reschedule: Task(" + task.getId() + ", "
                        + task.getTaskParams().getName() + ") ");
            }
        }
    }

    public boolean scheduleToResource(String hostName) {
        boolean assigned = false;
        LinkedList<Integer> executableCores = new LinkedList<Integer>();
        Resource resource = ResourceManager.getResource(hostName);
        LinkedList<Implementation>[] fittingImplementations = new LinkedList[CoreManager.coreCount];

        for (Integer coreId : ResourceManager.getExecutableCores(hostName)) {
            fittingImplementations[coreId]
                    = schedulerPolicies.sortImplementationsForResource(
                            ResourceManager.canRunNow(resource, CoreManager.getCoreImplementations(coreId)),
                            resource, profile);
        }

        // First check if there is some task to reschedule
        if (taskSets.areTasksToReschedule()) {
            for (Integer coreId : ResourceManager.getExecutableCores(hostName)) {
                if (!fittingImplementations[coreId].isEmpty()) {
                    executableCores.add(coreId);
                }
            }
            LinkedList<Task>[] tasks = taskSets.getTasksToReschedule();

            assigned = assignTasks(tasks, executableCores, resource, fittingImplementations);
            executableCores.clear();
        }

        // Now assign, if possible, one of the pending tasks to the resource
        if (taskSets.arePriorityTasks()) {
            for (Integer coreId : ResourceManager.getExecutableCores(hostName)) {
                if (!fittingImplementations[coreId].isEmpty()) {
                    executableCores.add(coreId);
                }
            }
            LinkedList<Task>[] tasks = new LinkedList[CoreManager.coreCount];
            for (Integer coreId : executableCores) {
                tasks[coreId] = trimTaskList(taskSets.getPriorityTasks()[coreId]);
            }
            assigned = assigned || assignTasks(tasks, executableCores, resource, fittingImplementations);
            executableCores.clear();
        }

        if (taskSets.areRegularTasks()) {
            for (Integer coreId : ResourceManager.getExecutableCores(hostName)) {
                if (!fittingImplementations[coreId].isEmpty()) {
                    executableCores.add(coreId);
                }
            }
            LinkedList<Task>[] tasks = new LinkedList[CoreManager.coreCount];
            for (Integer coreId : executableCores) {
                tasks[coreId] = trimTaskList(taskSets.getRegularTasks()[coreId]);
            }
            assigned = assigned || assignTasks(tasks, executableCores, resource, fittingImplementations);
            executableCores.clear();
        }

        if (debug && !assigned) {
            logger.debug("Resource " + hostName + " FREE");
        }
        return assigned;
    }

    private LinkedList<Task> trimTaskList(LinkedList<Task> original) {
        LinkedList<Task> result = new LinkedList();
        Iterator<Task> it = original.iterator();
        while (it.hasNext() && result.size() < MAX_TASK) {
            result.add(it.next());
        }
        return result;
    }

    private boolean assignTasks(LinkedList<Task>[] tasks, LinkedList<Integer> executableCores, Resource resource, LinkedList<Implementation>[] fittingImplementations) {
        boolean assigned = false;
        PriorityQueue<SchedulerPolicies.Object_Value<Task>>[] sortedTasks = new PriorityQueue[CoreManager.coreCount];
        LinkedList<Integer> unloadedCores = new LinkedList<Integer>();
        for (Integer coreId : executableCores) {
            if (tasks[coreId] == null || tasks[coreId].size() == 0) {
                unloadedCores.add(coreId);
            } else {
                sortedTasks[coreId] = schedulerPolicies.sortTasksForResource(resource, tasks[coreId], profile);
                if (sortedTasks[coreId].isEmpty()) {
                    unloadedCores.add(coreId);
                }
            }
        }
        for (Integer coreId : unloadedCores) {
            executableCores.remove(coreId);
        }

        while (!executableCores.isEmpty()) {
            Integer coreId = null;
            int maxValue = Integer.MIN_VALUE;
            for (Integer i : executableCores) {
                if (sortedTasks[i].peek().value > maxValue) {
                    maxValue = sortedTasks[i].peek().value;
                    coreId = i;
                }
            }
            Task t = sortedTasks[coreId].poll().o;
            if (t.getLastResource() != null) {
                if (t.getLastResource().compareTo(resource.getName()) == 0) {
                    if (sortedTasks[coreId].isEmpty()) {
                        executableCores.remove(coreId);
                    }
                    continue;
                }
                taskSets.rescheduledTask(t);
            } else {
                if (t.getTaskParams().hasPriority()) {
                    taskSets.priorityTaskScheduled(t);
                } else {
                    taskSets.regularTaskScheduled(t);
                }
            }

            sendJob(t, resource, fittingImplementations[coreId].get(0));
            assigned = true;
            if (sortedTasks[coreId].isEmpty()) {
                executableCores.remove(coreId);
            }
            unloadedCores.clear();
            for (Integer i : executableCores) {
                fittingImplementations[i] = ResourceManager.canRunNow(resource, fittingImplementations[t.getTaskParams().getId()]);
                if (fittingImplementations[i].isEmpty()) {
                    unloadedCores.add(i);
                }
            }
            for (Integer i : unloadedCores) {
                executableCores.remove(i);
            }

        }

        return assigned;
    }

    /**
     ********************************************
     *
     * Scheduling state operations
     *
     ********************************************
     */
    @Override
    public void getSchedulingState(ScheduleState ss) {
        super.getSchedulingState(ss);
        try {
            taskSets.describeState(ss);
        } catch (Exception e) {
            logger.fatal("Can not get the current schedule", e);
            System.exit(1);
        }
    }

    @Override
    public void setSchedulingState(ScheduleDecisions newState) {
        super.setSchedulingState(newState);
    }

    public String getMonitoringState() {
        StringBuilder sb = new StringBuilder();
        sb.append(super.getMonitoringState());
        sb.append(taskSets.getMonitoringInfo());
        return sb.toString();
    }
}

