/**
 *
 *   Copyright 2015-2015 Barcelona Supercomputing Center (www.bsc.es) All rights reserved.
 *
 *   Licensed under the Apache License, Version 2.0 (the "License");
 *   you may not use this file except in compliance with the License.
 *   You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *   Unless required by applicable law or agreed to in writing, software
 *   distributed under the License is distributed on an "AS IS" BASIS,
 *   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *   See the License for the specific language governing permissions and
 *   limitations under the License.
 */

package integratedtoolkit.components.scheduler.impl;

import integratedtoolkit.ascetic.Ascetic;
import integratedtoolkit.components.ResourceUser;
import integratedtoolkit.components.impl.TaskScheduler;
import integratedtoolkit.components.scheduler.SchedulerPolicies;
import integratedtoolkit.types.Implementation;
import integratedtoolkit.types.Task;
import integratedtoolkit.types.resources.Worker;
import integratedtoolkit.util.CoreManager;
import integratedtoolkit.util.ResourceManager;
import integratedtoolkit.util.TaskSets;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.PriorityQueue;

public class AsceticScheduler extends TaskScheduler {

    // Max number of tasks to examine when scheduling
    private static final int MAX_TASK = 50;

    // Object that stores the information about the current project
    private final TaskSets taskSets;

    public AsceticScheduler() {
        super();
        logger.info("Loading Ascetic Scheduler");
        taskSets = new TaskSets();
        if (Ascetic.getSchedulerOptimization().equals("Energy")) {
            logger.info("Setting Energy optimization policy");
        	schedulerPolicies = new EnergyPolicies();
        } else if (Ascetic.getSchedulerOptimization().equals("Cost")) {
        	logger.info("Setting Cost optimization policy");
            schedulerPolicies = new CostPolicies();
        } else {
        	logger.info("Setting Performance optimization policy");
            schedulerPolicies = new PerformancePolicies();
        }
        logger.info("Initialization finished");
    }

    @Override
    public void resizeDataStructures() {
        super.resizeDataStructures();
        taskSets.resizeDataStructures();
    }

    /*
     ********************************************
     *
     * Pending Work Query
     *
     ********************************************
     *
     */
    @Override
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
    public void resourcesCreated(Worker res) {
        super.resourcesCreated(res);
        logger.info("Resource " + res.getName() + " created");
        if (taskSets.getNoResourceCount() > 0) {
            int[] simTasks = res.getSimultaneousTasks();
            for (int coreId = 0; coreId < CoreManager.getCoreCount(); coreId++) {
                if (taskSets.getNoResourceCount(coreId) > 0 && simTasks[coreId] > 0) {
                    taskSets.resourceFound(coreId);
                }
            }
        }
        scheduleToResource(res);
    }

    @Override
    public void reduceResource(Worker res) {

    }

    @Override
    public void removeNode(Worker res) {
        super.removeNode(res);
    }

    /**
     ********************************************
     *
     * Scheduling state operations
     *
     ********************************************
     */
    @Override
    public void getWorkloadState(ResourceUser.WorkloadStatus ss) {
        super.getWorkloadState(ss);
        try {
            taskSets.getWorkloadState(ss);
        } catch (Exception e) {
            logger.fatal("Cannot get the current schedule", e);
            System.exit(1);
        }
    }

    @Override
    public String getCoresMonitoringData(String prefix) {
        StringBuilder sb = new StringBuilder();
        sb.append(super.getCoresMonitoringData(prefix));
        sb.append(taskSets.getMonitoringInfo());
        return sb.toString();
    }

    /**
     ********************************************
     *
     * Task Scheduling
     *
     ********************************************
     */
    @Override
    public void scheduleTask(Task task) {
        Worker chosenResource = null;
        int coreId = task.getTaskParams().getId();
        if (task.isSchedulingForced()) {
            //Task is forced to run in a given resource
            chosenResource = JM.enfDataToService.get(task.getEnforcingData().getDataId());
        }
        if (chosenResource != null) {
            logger.info("Task " + task.getId() + " forced to run in " + chosenResource.getName());
            LinkedList<Implementation> runnable = checkBoundaries(chosenResource.getRunnableImplementations(coreId), chosenResource);
            if (runnable.isEmpty()) {
                if (task.getTaskParams().hasPriority()) {
                    taskSets.newPriorityTask(task);
                } else {
                    taskSets.newRegularTask(task);
                }
                logger.info("Pending: Task(" + task.getId() + ", "
                        + task.getTaskParams().getName() + ") "
                        + "Resource(" + chosenResource.getName() + ")");
            } else {
                LinkedList<Implementation> run = schedulerPolicies.sortImplementationsForResource(runnable, chosenResource, profile);
                logger.info("Match: Task(" + task.getId() + ", "
                        + task.getTaskParams().getName() + ") "
                        + "Resource(" + chosenResource.getName() + ")");

                // Request the creation of a job for the task
                logger.info("Sending job " + task + ", to res name " + chosenResource.getName() + ", resource " + chosenResource + ", with impl " + run.getFirst());
                Implementation impl = run.getFirst();
                if (sendJob(task, chosenResource, impl)){
                	logger.debug("Job sended. Registering event");
                	Ascetic.startEvent(chosenResource, task, impl);
                }
            }
        } else {
            // Schedule task
            LinkedList<Worker> validResources = ResourceManager.findCompatibleWorkers(coreId);
            if (validResources.isEmpty()) {
                //There's no point on getting scores, any existing machines can run this task <- score=0
                taskSets.waitWithoutNode(task);
                logger.info("Blocked: Task(" + task.getId() + ", " + task.getTaskParams().getName() + ") ");
            } else {
                // Try to assign task to available resources
                HashMap<Worker, LinkedList<Implementation>> resourceToImpls = ResourceManager.findAvailableWorkers(validResources, coreId);
                for (java.util.Map.Entry<Worker, LinkedList<Implementation>> entry : resourceToImpls.entrySet()) {
                    LinkedList<Implementation> runnable = checkBoundaries(entry.getValue(), entry.getKey());
                    entry.setValue(runnable);
                }
                PriorityQueue<SchedulerPolicies.ObjectValue<Worker>> orderedResources = schedulerPolicies.sortResourcesForTask(task, resourceToImpls.keySet(), profile);
                for (SchedulerPolicies.ObjectValue<Worker> entry : orderedResources) {
                    chosenResource = (Worker) entry.o;
                    LinkedList<Implementation> orderedImpls = schedulerPolicies.sortImplementationsForResource(resourceToImpls.get(chosenResource), chosenResource, profile);
                    logger.info("Match: Task(" + task.getId() + ", " + task.getTaskParams().getName() + ") " + "Resource(" + chosenResource.getName() + ")");
                    Implementation impl = orderedImpls.getFirst();
                    if (sendJob(task, chosenResource, impl)) {
                        System.out.println("Job sended. Registering event");
                    	Ascetic.startEvent(chosenResource, task, impl);
                    	
                        return;
                    }
                }
                if (task.getTaskParams().hasPriority()) {
                    taskSets.newPriorityTask(task);
                } else {
                    taskSets.newRegularTask(task);
                }
                logger.info("Pending: Task(" + task.getId() + ", " + task.getTaskParams().getName() + ")");
            }
        }
    }

    @Override
    public void taskEnd(Task task, Worker resource, int implId) {
        Ascetic.stopEvent(resource, task, CoreManager.getCoreImplementations(task.getTaskParams().getId())[implId]);
        super.taskEnd(task, resource, implId);
    }

    @Override
    public void rescheduleTask(Task task, Worker failedResource) {
        //Rescheduling the failed Task
        // Find available resources that match user constraints for this task
        int coreId = task.getTaskParams().getId();
        LinkedList<Worker> validResources = ResourceManager.findCompatibleWorkers(coreId);
        if (!validResources.isEmpty()) {
            HashMap<Worker, LinkedList<Implementation>> resourceToImpls = ResourceManager.findAvailableWorkers(validResources, coreId);
            for (java.util.Map.Entry<Worker, LinkedList<Implementation>> entry : resourceToImpls.entrySet()) {
                LinkedList<Implementation> runnable = checkBoundaries(entry.getValue(), entry.getKey());
                entry.setValue(runnable);
            }

            resourceToImpls.remove(failedResource);
            PriorityQueue<SchedulerPolicies.ObjectValue<Worker>> orderedResources = schedulerPolicies.sortResourcesForTask(task, resourceToImpls.keySet(), profile);
            for (SchedulerPolicies.ObjectValue<Worker> entry : orderedResources) {
                Worker chosenResource = entry.o;
                // Request the creation of a job for the task
                LinkedList<Implementation> orderedImpls = schedulerPolicies.sortImplementationsForResource(resourceToImpls.get(chosenResource), chosenResource, profile);
                logger.info("Match: Task(" + task.getId() + ", " + task.getTaskParams().getName() + ") " + "Resource(" + chosenResource.getName() + ")");
                Implementation impl = orderedImpls.getFirst();
                if (sendJobRescheduled(task, chosenResource, impl)) {
                	logger.debug("Job sended. Registering event");
                	Ascetic.startEvent(chosenResource, task, impl);
                	return;
                }
            }
            task.setLastResource(failedResource.getName());
            taskSets.newTaskToReschedule(task);
            logger.info("To Reschedule: Task(" + task.getId() + ", " + task.getTaskParams().getName() + ") ");

        } else {
            taskSets.newTaskToReschedule(task);
            task.setLastResource(failedResource.getName());
            logger.info("To Reschedule: Task(" + task.getId() + ", " + task.getTaskParams().getName() + ") ");
        }

    }

    @Override
    public boolean scheduleToResource(Worker resource) {
        boolean assigned = false;
        LinkedList<Integer> compatibleCores = resource.getExecutableCores();
        LinkedList<Integer> executableCores = new LinkedList();
        LinkedList<Implementation>[] fittingImplementations = new LinkedList[CoreManager.getCoreCount()];
        if (compatibleCores==null){
        	System.out.println("WARNING: Resource " + resource.getName() + " has no executable cores");
        	logger.warn("WARNING: Resource " + resource.getName() + " has no executable cores");
        	return false;
        }
        for (int coreId : compatibleCores) {
            LinkedList<Implementation> impls = checkBoundaries(resource.getRunnableImplementations(coreId), resource);
            fittingImplementations[coreId] = schedulerPolicies.sortImplementationsForResource(impls, resource, profile);
        }

        // First check if there is some task to reschedule
        if (taskSets.areTasksToReschedule()) {
            for (Integer coreId : compatibleCores) {
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
            for (Integer coreId : compatibleCores) {
                if (!fittingImplementations[coreId].isEmpty()) {
                    executableCores.add(coreId);
                }
            }
            LinkedList<Task>[] tasks = new LinkedList[CoreManager.getCoreCount()];
            for (Integer coreId : executableCores) {
                tasks[coreId] = trimTaskList(taskSets.getPriorityTasks()[coreId]);
            }
            assigned = assigned || assignTasks(tasks, executableCores, resource, fittingImplementations);
            executableCores.clear();
        }

        if (taskSets.areRegularTasks()) {
            for (Integer coreId : compatibleCores) {
                if (!fittingImplementations[coreId].isEmpty()) {
                    executableCores.add(coreId);
                }
            }
            LinkedList<Task>[] tasks = new LinkedList[CoreManager.getCoreCount()];
            for (Integer coreId : executableCores) {
                tasks[coreId] = trimTaskList(taskSets.getRegularTasks()[coreId]);
            }
            assigned = assigned || assignTasks(tasks, executableCores, resource, fittingImplementations);
            executableCores.clear();
        }

        if (debug && !assigned) {
            logger.debug("Resource " + resource.getName() + " FREE");
        }
        return assigned;
    }

    private LinkedList<Implementation> checkBoundaries(LinkedList<Implementation> candidates, Worker worker) {
        LinkedList<Implementation> runnables = new LinkedList<Implementation>();
        for (Implementation impl : candidates) {
        	if (Ascetic.executionWithinBoundaries(worker, impl)) {
                runnables.add(impl);
            }else{
            	logger.debug("Core "+ impl.getCoreId()+" Implementation " + impl.getImplementationId() +" not added because it is exceeding boundaries");
            }
        }
        return runnables;
    }

    private LinkedList<Task> trimTaskList(LinkedList<Task> original) {
        LinkedList<Task> result = new LinkedList<Task>();
        Iterator<Task> it = original.iterator();
        while (it.hasNext() && result.size() < MAX_TASK) {
            result.add(it.next());
        }
        return result;
    }

    private boolean assignTasks(LinkedList<Task>[] tasks, LinkedList<Integer> executableCores, Worker resource, LinkedList<Implementation>[] fittingImplementations) {
        boolean assigned = false;
        PriorityQueue<SchedulerPolicies.ObjectValue<Task>>[] sortedTasks = new PriorityQueue[CoreManager.getCoreCount()];
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
            Integer coreId = executableCores.getFirst();
            double maxValue = Double.MIN_VALUE;
            for (Integer i : executableCores) {
                if (sortedTasks[i].peek().value > maxValue) {
                    maxValue = sortedTasks[i].peek().value;
                    coreId = i;
                }
            }
            SchedulerPolicies.ObjectValue<Task> ov = sortedTasks[coreId].poll();
            Task t = ov.o;
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
            Implementation impl = fittingImplementations[coreId].get(0);
            if (sendJob(t, resource, impl)) {
            	logger.debug("Job sended. Registering event");
            	Ascetic.startEvent(resource, t, impl);
            	assigned = true;
                if (sortedTasks[coreId].isEmpty()) {
                    executableCores.remove(coreId);
                }
            } else {
                sortedTasks[coreId].offer(ov);
            }
            unloadedCores.clear();
            for (Integer i : executableCores) {
                fittingImplementations[i] = checkBoundaries(resource.canRunNow(fittingImplementations[t.getTaskParams().getId()]), resource);

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
}
