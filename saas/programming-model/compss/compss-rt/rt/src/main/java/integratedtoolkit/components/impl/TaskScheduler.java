/*
 *  Copyright 2002-2014 Barcelona Supercomputing Center (www.bsc.es)
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
package integratedtoolkit.components.impl;

import integratedtoolkit.components.scheduler.SchedulerPolicies;
import org.apache.log4j.Logger;

import integratedtoolkit.ITConstants;
import integratedtoolkit.ascetic.Ascetic;
import integratedtoolkit.log.Loggers;
import integratedtoolkit.types.Implementation;
import integratedtoolkit.types.Resource;
import integratedtoolkit.types.Task;
import integratedtoolkit.types.ScheduleDecisions;
import integratedtoolkit.types.ScheduleState;
import integratedtoolkit.types.ResourceDescription;
import integratedtoolkit.util.CoreManager;
import integratedtoolkit.util.ResourceManager;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

public abstract class TaskScheduler {

    // Components
    protected JobManager JM;
    protected FileTransferManager FTM;

    protected SchedulerPolicies schedulerPolicies;

    // Component logger - No need to configure, ProActive does
    protected static final Logger monitor = Logger.getLogger(Loggers.RESOURCES);
    protected static final boolean monitorDebug = monitor.isDebugEnabled();
    protected static final Logger logger = Logger.getLogger(Loggers.TS_COMP);
    protected static final boolean debug = logger.isDebugEnabled();

    // Preschedule
    protected static final boolean presched = System.getProperty(ITConstants.IT_PRESCHED) != null
            && System.getProperty(ITConstants.IT_PRESCHED).equals("true")
            ? true : false;

    // Tasks running
    private HashMap<String, List<Task>> nodeToRunningTasks;
    //Task Stats
    protected ExecutionProfile[][] profile;

    public TaskScheduler() {
        if (nodeToRunningTasks == null) {
            nodeToRunningTasks = new HashMap<String, List<Task>>();
        } else {
            nodeToRunningTasks.clear();
        }

        profile = new ExecutionProfile[CoreManager.coreCount][];
        for (int coreId = 0; coreId < CoreManager.coreCount; coreId++) {
            int implCount = CoreManager.getCoreImplementations(coreId).length;
            profile[coreId] = new ExecutionProfile[implCount];
            for (int implId = 0; implId < implCount; implId++) {
                profile[coreId][implId] = new ExecutionProfile();
            }
        }
    }

    public void setCoWorkers(JobManager JM, FileTransferManager FTM) {
        this.JM = JM;
        this.schedulerPolicies.JM = JM;
        this.FTM = FTM;
        this.schedulerPolicies.FTM = FTM;
    }

    /**
     * Resizes all the internal data structures to enable them to manage a
     * different number of TaskParams Elements.
     */
    public void resizeDataStructures() {

        ExecutionProfile[][] profileTmp = new ExecutionProfile[CoreManager.coreCount][];
        for (int coreId = 0; coreId < profile.length; coreId++) {
            int implCount = CoreManager.getCoreImplementations(coreId).length;
            profileTmp[coreId] = new ExecutionProfile[implCount];
            System.arraycopy(profile[coreId], 0, profileTmp[coreId], 0, profile[coreId].length);
            for (int implId = profile[coreId].length; implId < implCount; implId++) {
                profileTmp[coreId][implId] = new ExecutionProfile();
            }
        }
        for (int coreId = profile.length; coreId < CoreManager.coreCount; coreId++) {
            int implCount = CoreManager.getCoreImplementations(coreId).length;
            profileTmp[coreId] = new ExecutionProfile[implCount];
            for (int implId = profile[coreId].length; implId < implCount; implId++) {
                profileTmp[coreId][implId] = new ExecutionProfile();
            }
        }
        profile = profileTmp;

    }

    /**
     ********************************************
     *
     * Pending Work Query
     *
     ********************************************
     */
    /**
     * Checks if there is pending tasks to be executed for a given core
     *
     * @param coreId Identifier of the core whose tasks presence has to be
     * tested
     * @return {@literal true} - if there are pending tasks to be executed
     */
    public abstract boolean isPendingWork(Integer coreId);

    /**
     ********************************************
     *
     * Resource Management
     *
     ********************************************
     */
    /**
     * Notifies the availability of new resources to the Task Scheduler.
     *
     * Adds or increases the capacity of a resource if the scheduling system.
     * Once the resource capacity has been added/increased, it tries to schedule
     * and submit pending tasks on the resource.
     *
     * @param schedulerName name of the augmented/created node
     * @param granted description of increase of the features for the resources
     * @param limitOfTasks max number of simultaneous tasks that can run
     * parallelly in the resource
     */
    public void resourcesCreated(String schedulerName, ResourceDescription granted, Integer limitOfTasks) {
        nodeToRunningTasks.put(schedulerName, new LinkedList<Task>());
    }

    /**
     * Notifies the unavailability of some existing resources to the Task
     * Scheduler.
     *
     * Removes from the scheduling system a set of capabilities of one resource.
     * So the removed resource are no longer considered in the scheduling
     * algorithm; and therefore, no more tasks are scheduled to them.
     *
     * @param reduction Description on the resource reduction
     */
    public abstract void reduceResource(ResourceDescription reduction);

    /**
     * Removes a resource and all its slots from the scheduling system.
     *
     * The resource passed as a parameter and all bounded slots are completely
     * removed from the scheduling systes. Before calling this method, the user
     * must ensure that no tasks are running on the resource slots.
     *
     * @param hostName Name of the resource to be removed
     */
    public void removeNode(String hostName) {
        nodeToRunningTasks.remove(hostName);
    }

    /**
     * Tries to perform a pending modification on a resource.
     *
     * In the case that there exists a pending modification for the specified
     * resource, it checks if there are enough free slots to actually perform
     * the modification.
     *
     * When the amount of free slots is lower than the required by the
     * modification, it returns null and mantains all the modification request
     * in a pending state. Otherwise, if there are enough resources, the method
     * commits the modification
     *
     *
     * @param resource Name of the resource to be modified
     * @return {@literal true} if the pending modification has been commited
     */
    ///public abstract boolean performModification(String resource);
    /**
     * Checks the computing ability to compute tasks.
     *
     * @param resource Name of the resource whose computing ability has to be
     * tested.
     * @return {@literal true} - if the resource still has some slots to compute
     */
    //public abstract boolean canResourceCompute(String resource);
    /**
     ********************************************
     *
     * Task Scheduling
     *
     ********************************************
     */
    /**
     * Schedules a task execution on an available resource.
     *
     * Given a task passed as a parameter, it looks for an available resource
     * where to execute it. If there is no slot able to host the execution
     * (because of the task constraints or the slot occupation) the tasks is
     * stored to be executed later. Otherwise, if there is some resource that
     * fulfills the task constraints and one free slot, the task execution is
     * submitted to the resource via the Job Manager.
     *
     * @param task Task whose execution has to be scheduled
     */
    public abstract void scheduleTask(Task task);

    /**
     *
     * Reschedules a task execution on an available resource different from the
     * one where it already failed.
     *
     * Given a task passed as a parameter, it looks for an available resource
     * where to execute it. If there is no slot able to host the execution
     * (because of the task constraints or the slot occupation) the tasks is
     * stored to be executed later. Otherwise, if there is some resource that
     * fulfills the task constraints and one free slot, the task execution is
     * submitted to the resource via the Job Manager.
     *
     * The host where the task already run is ignored during the resource
     * selecting process.
     *
     * @param task Task whose execution has to be scheduled
     * @param failedresource Resource where the task execution failed
     */
    public abstract void rescheduleTask(Task task, String failedresource);

    /**
     * Tries to find a pending task to run in a given resource and submits its
     * execution.
     *
     * It looks for a pending tasks that can be submitted to the resource passed
     * as a parameter. If there's no pending task that can run in the resource,
     * the method does nothing and returns false. Otherwise, if a pending task
     * whose constraints match the resource features is found, its execution is
     * submitted via the Job Manager.
     *
     * The method does not check the number of available slots on the resource.
     * It's responsibility of the user to check it.
     *
     * @param hostName Resource where to run the chosen task.
     * @return {@literal true} if a pending task execution is submitted to the
     * resource
     */
    public abstract boolean scheduleToResource(String hostName);

    /**
     ********************************************
     *
     * Scheduling state operations
     *
     ********************************************
     */
    /**
     * Describes the current load of the scheduling system.
     *
     * @param state object describing the current state
     */
    public void getSchedulingState(ScheduleState state) {
        //Core Info
        for (int i = 0; i < CoreManager.coreCount; i++) {
            long[] stats = getCoreStats(i, 100l);
            state.coreMinTime[i] = stats[1];
            state.coreMeanTime[i] = stats[2];
            state.coreMaxTime[i] = stats[3];
        }
        //ResourceInfo
        for (java.util.Map.Entry<String, List<Task>> entry : nodeToRunningTasks.entrySet()) {
            String resource = entry.getKey();
            List<Task> running = entry.getValue();
            int[] tasks = new int[running.size()];
            long[] elapsed = new long[running.size()];
            int i = 0;
            long now = System.currentTimeMillis();
            for (Task t : running) {
                tasks[i] = t.getTaskParams().getId();
                elapsed[i] = now - t.getInitialTimeStamp();
                i++;
            }
            state.updateHostInfo(resource, tasks, elapsed, null);
        }
    }

    private long[] getCoreStats(int coreId, long defaultValue) {
        long[] result = new long[4];

        int counter = 0;
        long maxTime = Long.MIN_VALUE;
        long minTime = Long.MAX_VALUE;
        long avgTime = 0l;
        for (int implId = 0; implId < CoreManager.getCoreImplementations(coreId).length; implId++) {
            if (profile[coreId][implId].executionCount > 0) {//Implementation has been executed
                counter += profile[coreId][implId].executionCount;
                avgTime += profile[coreId][implId].executionCount * profile[coreId][implId].avgExecutionTime;
                if (profile[coreId][implId].maxExecutionTime > maxTime) {
                    maxTime = profile[coreId][implId].maxExecutionTime;
                }
                if (profile[coreId][implId].minExecutionTime < minTime) {
                    minTime = profile[coreId][implId].minExecutionTime;
                }
            }
        }
        if (counter > 0) {
            result[0] = counter;
            result[1] = minTime;
            result[2] = avgTime / counter;
            result[3] = maxTime;
        } else {
            Task earlier = null;

            for (int implId = 0; implId < CoreManager.getCoreImplementations(coreId).length; implId++) {
                if (profile[coreId][implId].firstExecution != null) {
                    if (earlier == null) {
                        earlier = profile[coreId][implId].firstExecution;
                    } else {
                        if (earlier.getInitialTimeStamp() > profile[coreId][implId].firstExecution.getInitialTimeStamp()) {
                            earlier = profile[coreId][implId].firstExecution;
                        }
                    }
                }
            }
            if (earlier == null) {
                result[0] = 0;
                result[1] = defaultValue;
                result[2] = defaultValue;
                result[3] = defaultValue;
            } else {
                result[0] = 0;
                long difference = System.currentTimeMillis() - earlier.getInitialTimeStamp();
                result[1] = difference;
                result[2] = difference;
                result[3] = difference;
            }
        }
        return result;
    }

    /**
     * Applies some improvents to the task scheduling
     *
     * @param newState Scheduling improvements recommendations
     */
    public void setSchedulingState(ScheduleDecisions newState) {
    }

    /**
     * Returns the current state that should be printed in the monitor
     *
     * @return current state in an XML format
     */
    public String getMonitoringState() {
        StringBuilder sb = new StringBuilder("\t<CoresInfo>\n");
        for (java.util.Map.Entry<String, Integer> entry : CoreManager.signatureToId.entrySet()) {
            int core = entry.getValue();
            String signature = entry.getKey();
            sb.append("\t\t<Core id=\"").append(core).append("\" signature=\"" + signature + "\">\n");
            long stats[] = getCoreStats(core, 0);
            sb.append("\t\t\t<MeanExecutionTime>").append(stats[2]).append("</MeanExecutionTime>\n");
            sb.append("\t\t\t<MinExecutionTime>").append(stats[1]).append("</MinExecutionTime>\n");
            sb.append("\t\t\t<MaxExecutionTime>").append(stats[3]).append("</MaxExecutionTime>\n");
            sb.append("\t\t\t<ExecutedCount>").append(stats[3]).append("</ExecutedCount>\n");
            sb.append("\t\t</Core>\n");
        }
        sb.append("\t</CoresInfo>\n");

        sb.append("\t<ResourceInfo>\n");
        for (java.util.Map.Entry<String, List<Task>> entry : nodeToRunningTasks.entrySet()) {
            String resourceName = entry.getKey();
            sb.append("\t\t<Resource id=\"").append(resourceName).append("\">\n");
            sb.append(ResourceManager.getResourceMonitoringData("\t\t\t", entry.getKey()));
            List<Task> tasks = entry.getValue();
            sb.append("\t\t\t<Tasks>");
            for (Task t : tasks) {
                sb.append(t.getId()).append(" ");
            }
            sb.append("</Tasks>\n");

            sb.append("\t\t\t<EnergyEstimation cores=\"").append(CoreManager.coreCount).append("\">\n");
            for (int coreId = 0; coreId < CoreManager.coreCount; coreId++) {
                sb.append("\t\t\t\t<Core id=\"").append(coreId).append("\" implementations=\"").append(CoreManager.getCoreImplementations(coreId).length).append("\">\n");
                double[] consumptions = Ascetic.getConsumptions(resourceName, coreId);
                for (int implId = 0; implId < CoreManager.getCoreImplementations(coreId).length; implId++) {
                    sb.append("\t\t\t\t\t<Implementation id=\"").append(implId).append("\">").append(consumptions[implId]).append("</Implementation>\n");
                }
                sb.append("\t\t\t\t</Core>\n");
            }
            sb.append("\t\t\t</EnergyEstimation>\n");
            sb.append("\t\t</Resource>\n");
        }
        sb.append("\t</ResourceInfo>\n");
        return sb.toString();
    }

    protected void sendJob(Task task, Resource resource, Implementation impl) {
        // Request the creation of the job       
        startsExecution(task, resource.getName(), impl.getImplementationId());
        resource.runTask(impl.getResource());
        JM.newJob(task, impl, resource);
    }

    protected void sendJobRescheduled(Task task, Resource resource, Implementation impl) {
        // Request the creation of the job
        resource.runTask(impl.getResource());
        JM.jobRescheduled(task, impl, resource);
    }

    /**
     * Releases the slot where a task was running.
     *
     * Releases the slot where the task was running so another task can be
     * assigned to it. In addition, if the task execution finished properly, the
     * slot is released and the Task Scheduler updates core monitoring data.
     *
     * @param task Task whose execution has ended
     * @param resource Name of the resource where the exeuction run
     * @param implementationId Id of the implementation executed
     */
    public void taskEnd(Task task, String resource, int implementationId) {
        // Obtain freed resource
        switch (task.getStatus()) {
            case FINISHED:
                endsExecution(task, resource, true, implementationId);
                break;
            case TO_RESCHEDULE:
                endsExecution(task, resource, false, implementationId);
                break;
            case FAILED:
                endsExecution(task, resource, false, implementationId);
                break;
            default: //This Task should not be here
                logger.fatal("INVALID KIND OF TASK ENDED: " + task.getStatus());
                System.exit(1);
                break;
        }
    }

    private void startsExecution(Task t, String resourceName, int implId) {
        List<Task> tasks = nodeToRunningTasks.get(resourceName);
        tasks.add(t);

        t.setInitialTimeStamp(System.currentTimeMillis());
        if (profile[t.getTaskParams().getId()][implId].firstExecution == null) {
            profile[t.getTaskParams().getId()][implId].firstExecution = t;
        }
    }

    private void endsExecution(Task task, String resourceName, boolean success, int implId) {
        nodeToRunningTasks.get(resourceName).remove(task);
        int core = task.getTaskParams().getId();
        if (success) {
            profile[core][implId].executionEnded(task);
        } else {
            if (profile[core][implId].firstExecution == task) {

                long firstTime = Long.MAX_VALUE;
                Task firstTask = null;
                for (List<Task> tasks : nodeToRunningTasks.values()) {
                    for (Task running : tasks) {
                        if (running.getTaskParams().getId() == core) {
                            if (firstTime > running.getInitialTimeStamp()) {
                                firstTask = running;
                            }
                        }
                    }
                }
                profile[core][implId].firstExecution = firstTask;
            }
        }
    }

    public class ExecutionProfile {

        private Task firstExecution;
        private int executionCount;

        private long avgExecutionTime;
        private long maxExecutionTime;
        private long minExecutionTime;

        public ExecutionProfile() {
            executionCount = 0;
            firstExecution = null;
            avgExecutionTime = 0l;
            maxExecutionTime = 0l;
            minExecutionTime = 0l;
        }

        public void executionEnded(Task task) {
            long initialTime = task.getInitialTimeStamp();
            long duration = System.currentTimeMillis() - initialTime;
            Long mean = avgExecutionTime;
            if (mean == null) {
                mean = 0l;
            }
            if (maxExecutionTime < duration) {
                maxExecutionTime = duration;
            }
            if (minExecutionTime > duration) {
                minExecutionTime = duration;
            }
            avgExecutionTime = ((mean * executionCount) + duration) / (executionCount + 1);
            executionCount++;
        }

        public int getExecutionCount() {
            return executionCount;
        }

        public Long getMinExecutionTime(Long defaultValue) {
            if (executionCount > 0) {
                return minExecutionTime;
            } else {
                if (firstExecution == null) {
                    return defaultValue;
                } else {
                    return System.currentTimeMillis() - firstExecution.getInitialTimeStamp();
                }
            }
        }

        public Long getMaxExecutionTime(Long defaultValue) {
            if (executionCount > 0) {
                return maxExecutionTime;
            } else {
                if (firstExecution == null) {
                    return defaultValue;
                } else {
                    return System.currentTimeMillis() - firstExecution.getInitialTimeStamp();
                }
            }
        }

        public Long getAverageExecutionTime(Long defaultValue) {
            if (executionCount > 0) {
                return avgExecutionTime;
            } else {
                if (firstExecution == null) {
                    return defaultValue;
                } else {
                    return System.currentTimeMillis() - firstExecution.getInitialTimeStamp();
                }
            }
        }

    }
}
