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
package integratedtoolkit.util;

//import integratedtoolkit.types.resources.Resource;
import integratedtoolkit.types.ScheduleState;

import java.util.HashMap;
import java.util.List;
import java.util.LinkedList;
import integratedtoolkit.types.Task;

/**
 * The QueueManager class is an utility to manage the schedule of all the
 * dependency-free tasks. It controls if they are running, if they have been
 * scheduled in a resource slot queue, if they failed on its previous execution
 * and must be rescheduled or if they have no resource where to run.
 *
 * There are many queues: - tasks without resource where to run - tasks to be
 * rescheduled - one queue for each slot of all the resources
 */
public class TaskSets {

    // Pending tasks
    /**
     * Tasks with no resource where they can be run
     */
    private LinkedList<Task>[] noResourceTasks;

    /**
     * Task to be rescheduled
     */
    private LinkedList<Task>[] tasksToReschedule;
    /**
     * Tasks with priority
     */
    private LinkedList<Task>[] priorityTasks;

    /**
     * Regular tasks
     */
    private LinkedList<Task>[] regularTasks;

    /**
     * Amount of tasks per core that can't be run
     */
    private int noResourceCount;
    /**
     * Amount of tasks per core to be rescheduled
     */
    private int toRescheduleCount;
    /**
     * Amount of priority tasks per core
     */
    private int priorityCount;
    /**
     * Amount of regular tasks
     */
    private int regularCount;

    // Tasks running
    private HashMap<String, List<Task>> nodeToRunningTasks;
    //Task Stats
    /**
     * First task to be executed for that method
     */
    private Task[] firstMethodExecution;
    /**
     * Average execution time per core
     */
    private Long[] coreAverageExecutionTime;
    /**
     * Max execution time per core
     */
    private Long[] coreMaxExecutionTime;
    /**
     * Min execution time per core
     */
    private Long[] coreMinExecutionTime;
    /**
     * Executed Tasks per CoreManager
     */
    private int[] coreExecutedCount;

    /**
     * Constructs a new QueueManager
     *
     * @param TS TaskScheduler associated to the manager
     */
    public TaskSets() {
        if (noResourceTasks == null) {
            noResourceCount = 0;
            noResourceTasks = new LinkedList[CoreManager.coreCount];
            for (int i = 0; i < CoreManager.coreCount; i++) {
                noResourceTasks[i] = new LinkedList<Task>();
            }
        } else {
            for (int i = 0; i < CoreManager.coreCount; i++) {
                noResourceTasks[i].clear();
            }
        }

        if (tasksToReschedule == null) {
            toRescheduleCount = 0;
            tasksToReschedule = new LinkedList[CoreManager.coreCount];
            for (int i = 0; i < CoreManager.coreCount; i++) {
                tasksToReschedule[i] = new LinkedList<Task>();
            }
        } else {
            for (int i = 0; i < CoreManager.coreCount; i++) {
                tasksToReschedule[i].clear();
            }
        }

        if (priorityTasks == null) {
            priorityCount = 0;
            priorityTasks = new LinkedList[CoreManager.coreCount];
            for (int i = 0; i < CoreManager.coreCount; i++) {
                priorityTasks[i] = new LinkedList<Task>();
            }
        } else {
            for (int i = 0; i < CoreManager.coreCount; i++) {
                priorityTasks[i].clear();
            }
        }

        if (regularTasks == null) {
            regularCount = 0;
            regularTasks = new LinkedList[CoreManager.coreCount];
            for (int i = 0; i < CoreManager.coreCount; i++) {
                regularTasks[i] = new LinkedList<Task>();
            }
        } else {
            for (int i = 0; i < CoreManager.coreCount; i++) {
                regularTasks[i].clear();
            }
        }

        if (nodeToRunningTasks == null) {
            nodeToRunningTasks = new HashMap<String, List<Task>>();
        } else {
            nodeToRunningTasks.clear();
        }

        if (coreAverageExecutionTime == null) {
            coreAverageExecutionTime = new Long[CoreManager.coreCount];

            for (int i = 0; i < CoreManager.coreCount; i++) {
                coreAverageExecutionTime[i] = null;
            }
        }

        if (coreMinExecutionTime == null) {
            coreMinExecutionTime = new Long[CoreManager.coreCount];

            for (int i = 0; i < CoreManager.coreCount; i++) {
                coreMinExecutionTime[i] = Long.MAX_VALUE;
            }
        }

        if (coreMaxExecutionTime == null) {
            coreMaxExecutionTime = new Long[CoreManager.coreCount];

            for (int i = 0; i < CoreManager.coreCount; i++) {
                coreMaxExecutionTime[i] = 0l;
            }
        }
        coreExecutedCount = new int[CoreManager.coreCount];
        firstMethodExecution = new Task[CoreManager.coreCount];
    }

    /**
     * ** NO RESOURCE TASKs MANAGEMENT *****
     */
    /**
     * Adds a task to the queue of tasks with no resource
     *
     * @param t Task to be added
     */
    public void waitWithoutNode(Task t) {
        noResourceCount++;
        noResourceTasks[t.getTaskParams().getId()].add(t);
    }

    /**
     * Removes from the queue of tasks with no resources all the tasks of a
     * specific core
     *
     * @param coreId core identifier that can be executed
     */
    public void resourceFound(int coreId) {
        noResourceCount -= noResourceTasks[coreId].size();
        for (Task t : noResourceTasks[coreId]) {
            if (t.getTaskParams().hasPriority()) {
                priorityTasks[coreId].add(t);
                priorityCount++;
            } else {
                regularTasks[coreId].add(t);
                regularCount++;
            }
        }
        noResourceTasks[coreId].clear();
    }

    /**
     * Gets the amount of task without resource that execute a specific core
     *
     * @param coreId identifier of the core
     * @return amount of task without resource that execute a specific core
     */
    public int getNoResourceCount(int coreId) {
        return noResourceTasks[coreId].size();
    }

    /**
     * Gets the amount of task without resource
     *
     * @return amount of task without resource
     */
    public int getNoResourceCount() {
        return noResourceCount;
    }

    /**
     * Returns the whole list of tasks without resource
     *
     * @return The whole list of tasks without resource
     */
    public LinkedList<Task>[] getPendingTasksWithoutNode() {
        return noResourceTasks;
    }

    /**
     * ** TO RESCHEDULE TASKs MANAGEMENT *****
     */
    /**
     * Adds a task to the queue of tasks to be rescheduled
     *
     * @param t Task to be added
     */
    public void newTaskToReschedule(Task t) {
        toRescheduleCount++;
        tasksToReschedule[t.getTaskParams().getId()].add(t);
    }

    /**
     * Removes a task from the queue of tasks to reschedule
     *
     * @param t tasks to be removed
     */
    public void rescheduledTask(Task t) {
        toRescheduleCount--;
        tasksToReschedule[t.getTaskParams().getId()].remove(t);
    }

    /**
     * Checks if there is any tasks to reschedule
     *
     * @return true if there is some tasks on the queue of tasks to reschedule
     */
    public boolean areTasksToReschedule() {
        return toRescheduleCount != 0;
    }

    /**
     * Gets the amount of task to be rescheduled that execute a specific core
     *
     * @param coreId identifier of the core
     * @return amount of task to be rescheduled that execute a specific core
     */
    public int getToRescheduleCount(int coreId) {
        return tasksToReschedule[coreId].size();
    }

    /**
     * Returns the whole list of tasks to reschedule
     *
     * @return The whole list of tasks to reschedule
     */
    public LinkedList<Task>[] getTasksToReschedule() {
        return tasksToReschedule;
    }

    /**
     * ** Priority Tasks Management
     */
    /**
     * Adds a task to the queue of prioritary tasks
     *
     * @param t Task to be added
     */
    public void newPriorityTask(Task t) {
        priorityTasks[t.getTaskParams().getId()].add(t);
        priorityCount++;

    }

    /**
     * Removes a task from the queue of prioritary tasks
     *
     * @param t tasks to be removed
     */
    public void priorityTaskScheduled(Task t) {
        priorityCount--;
        priorityTasks[t.getTaskParams().getId()].remove(t);
    }

    /**
     * Checks if there is any prioritary task
     *
     * @return true if there is some tasks on the queue of prioritary tasks
     */
    public boolean arePriorityTasks() {
        return priorityCount != 0;
    }

    /**
     * Gets the amount of priority task that execute a specific core
     *
     * @param coreId identifier of the core
     * @return amount of prioritary task that execute a specific core
     */
    public int getPriorityCount(int coreId) {
        return priorityTasks[coreId].size();
    }

    /**
     * Returns the whole list of prioritary tasks
     *
     * @return The whole list of prioritary tasks
     */
    public LinkedList<Task>[] getPriorityTasks() {
        return priorityTasks;
    }

    /**
     * ** Regular Tasks Management
     */
    /**
     * Adds a task to the queue of regular tasks
     *
     * @param t Task to be added
     */
    public void newRegularTask(Task t) {
        regularTasks[t.getTaskParams().getId()].add(t);
        regularCount++;
    }

    /**
     * Removes a task from the queue of regular tasks
     *
     * @param t tasks to be removed
     */
    public void regularTaskScheduled(Task t) {
        regularCount--;
        regularTasks[t.getTaskParams().getId()].remove(t);
    }

    /**
     * Checks if there is any regular task
     *
     * @return true if there is some tasks on the queue of regular tasks
     */
    public boolean areRegularTasks() {
        return regularCount != 0;
    }

    /**
     * Gets the amount of regular task that execute a specific core
     *
     * @param coreId identifier of the core
     * @return amount of regular task that execute a specific core
     */
    public int getRegularCount(int coreId) {
        return regularTasks[coreId].size();
    }

    /**
     * Returns the whole list of regular tasks
     *
     * @return The whole list of regular tasks
     */
    public LinkedList<Task>[] getRegularTasks() {
        return regularTasks;
    }

    /**
     * ** Resource management
     */
    /**
     * Adds a resource to be managed
     *
     * @param resourceName name of the resource
     */
    public void addNode(String resourceName) {
        List<Task> tasks = new LinkedList<Task>();
        nodeToRunningTasks.put(resourceName, tasks);
    }

    /**
     * Removes a resource to be managed
     *
     * @param resourceName name of the resource
     */
    public void removeNode(String resourceName) {
        nodeToRunningTasks.remove(resourceName);
    }

    /**
     * ** Timing
     */
    /**
     * The execution of a tasks starts at the specified resource
     *
     * @param t task which is running
     * @param resourceName resource where the task is being executed
     */
    public void startsExecution(Task t, String resourceName) {
        List<Task> tasks = nodeToRunningTasks.get(resourceName);
        tasks.add(t);

        t.setInitialTimeStamp(System.currentTimeMillis());
        if (firstMethodExecution[t.getTaskParams().getId()] == null) {
            firstMethodExecution[t.getTaskParams().getId()] = t;
        }
    }

    /**
     * The execution of a tasks ends at the specified resource
     *
     * @param task the tasks that has been run
     * @param resourceName resource where the task was being executed
     * @param success true if the task finished correctly
     */
    public void endsExecution(Task task, String resourceName, boolean success) {
        nodeToRunningTasks.get(resourceName).remove(task);
        int core = task.getTaskParams().getId();
        if (success) {
            long initialTime = task.getInitialTimeStamp();
            long duration = System.currentTimeMillis() - initialTime;
            Long mean = coreAverageExecutionTime[core];
            if (mean == null) {
                mean = 0l;
            }
            if (coreMaxExecutionTime[core] < duration) {
                coreMaxExecutionTime[core] = duration;
            }
            if (coreMinExecutionTime[core] > duration) {
                coreMinExecutionTime[core] = duration;
            }
            coreAverageExecutionTime[core] = ((mean * coreExecutedCount[core]) + duration) / (coreExecutedCount[core] + 1);
            coreExecutedCount[core]++;
        } else {
            if (firstMethodExecution[core] == task) {

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
                firstMethodExecution[core] = firstTask;
            }
        }

    }

    /**
     * ** Schedule state
     */
    /**
     * Constructs a new ScheduleState and adds the description of the current
     * scheduling.
     *
     * @param ss current schedule state to be complemented
     */
    public void describeState(ScheduleState ss) {
        for (int i = 0; i < CoreManager.coreCount; i++) {
            //Task Info
            ss.noResourceCount = noResourceCount;
            ss.noResourceCounts[i] = noResourceTasks[i].size();
            ss.toRescheduleCount = toRescheduleCount;
            ss.toRescheduleCounts[i] = tasksToReschedule[i].size();
            ss.ordinaryCount = priorityCount + regularCount;
            ss.ordinaryCounts[i] = priorityTasks[i].size() + regularTasks[i].size();
            //Core Info
            if (coreAverageExecutionTime[i] != null) {
                ss.coreMeanTime[i] = coreAverageExecutionTime[i];
                ss.coreMaxTime[i] = this.coreMaxExecutionTime[i];
                ss.coreMinTime[i] = this.coreMinExecutionTime[i];
            } else {
                if (firstMethodExecution[i] != null) {
                    //if any has started --> take the already spent time as the mean.
                    Long initTimeStamp = firstMethodExecution[i].getInitialTimeStamp();
                    if (initTimeStamp != null) {
                        //if the first task hasn't failed
                        long elapsedTime = System.currentTimeMillis() - initTimeStamp;
                        ss.coreMeanTime[i] = elapsedTime;
                        ss.coreMaxTime[i] = elapsedTime;
                        ss.coreMinTime[i] = elapsedTime;
                    } else {
                        ss.coreMeanTime[i] = 100l;
                        ss.coreMaxTime[i] = 100l;
                        ss.coreMinTime[i] = 100l;
                    }
                } else {
                    ss.coreMeanTime[i] = 100l;
                    ss.coreMaxTime[i] = 100l;
                    ss.coreMinTime[i] = 100l;
                }
            }
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
            ss.updateHostInfo(resource, tasks, elapsed, null);
        }
    }

    /**
     * Creates a description of the current schedule for all the resources. The
     * string pattern is described as follows: On execution: hostName1: taskId
     * taskId ... (all running tasks for hostName1) hostName2: taskId ... (all
     * running tasks for hostName2) ...
     *
     * Pending: taskId taskId taskId ... (all pending tasks in slots, to
     * reschedule or without resource)
     *
     * @return description of the current schedule state
     */
    public String describeCurrentState() {
        String pending = "\tPending:";
        for (int i = 0; i < CoreManager.coreCount; i++) {
            for (Task t : tasksToReschedule[i]) {
                pending += " " + t.getId() + "r";
            }
            for (Task t : priorityTasks[i]) {
                pending += " " + t.getId() + "p";
            }
            for (Task t : noResourceTasks[i]) {
                pending += " " + t.getId() + "b";
            }
            for (Task t : regularTasks[i]) {
                pending += " " + t.getId();
            }
        }
        String info = "\tOn execution:\n";
        for (java.util.Map.Entry<String, List<Task>> entry : nodeToRunningTasks.entrySet()) {
            String hostName = entry.getKey();
            info += "\t\t" + hostName + ":";
            List<Task> tasks = entry.getValue();
            if (tasks != null) {
                for (Task t : tasks) {
                    info += " " + t.getId();
                }
            }
            info += "\n";
        }
        return info + "\n" + pending;
    }

    /**
     * Obtains the data that must be shown on the monitor
     *
     * @return String with core Execution information in an XML format
     */
    public String getMonitoringInfo() {

        StringBuilder sb = new StringBuilder("\t<CoresInfo>\n");
        for (java.util.Map.Entry<String, Integer> entry : CoreManager.signatureToId.entrySet()) {
            int core = entry.getValue();
            String signature = entry.getKey();
            sb.append("\t\t<Core id=\"").append(core).append("\" signature=\"" + signature + "\">\n");
            if (coreAverageExecutionTime[core] != null) {
                sb.append("\t\t\t<MeanExecutionTime>").append((coreAverageExecutionTime[core] / 1000) + 1).append("</MeanExecutionTime>\n");
                sb.append("\t\t\t<MinExecutionTime>").append((coreAverageExecutionTime[core] / 1000) + 1).append("</MinExecutionTime>\n");
                sb.append("\t\t\t<MaxExecutionTime>").append((coreAverageExecutionTime[core] / 1000) + 1).append("</MaxExecutionTime>\n");
            } else {
                sb.append("\t\t\t<MeanExecutionTime>0</MeanExecutionTime>\n");
                sb.append("\t\t\t<MinExecutionTime>0</MinExecutionTime>\n");
                sb.append("\t\t\t<MaxExecutionTime>0</MaxExecutionTime>\n");
            }
            sb.append("\t\t\t<ExecutedCount>").append(coreExecutedCount[core]).append("</ExecutedCount>\n");
            sb.append("\t\t</Core>\n");
        }
        sb.append("\t</CoresInfo>\n");

        sb.append("\t<ResourceInfo>\n");
        for (java.util.Map.Entry<String, List<Task>> entry : nodeToRunningTasks.entrySet()) {
            sb.append("\t\t<Resource id=\"").append(entry.getKey()).append("\">\n");
            sb.append(ResourceManager.getResourceMonitoringData("\t\t\t", entry.getKey()));
            List<Task> tasks = entry.getValue();
            sb.append("\t\t\t<Tasks>");
            for (Task t : tasks) {
                sb.append(t.getId()).append(" ");
            }
            sb.append("</Tasks>\n");
            sb.append("\t\t</Resource>\n");
        }
        sb.append("\t</ResourceInfo>\n");
        return sb.toString();
    }

    public void resizeDataStructures() {
        //TASK SET STATE
        LinkedList<Task>[] tmp = new LinkedList[CoreManager.coreCount];
        System.arraycopy(noResourceTasks, 0, tmp, 0, noResourceTasks.length);
        for (int i = noResourceTasks.length; i < CoreManager.coreCount; i++) {
            tmp[i] = new LinkedList<Task>();
        }
        noResourceTasks = tmp;
        
        tmp = new LinkedList[CoreManager.coreCount];
        System.arraycopy(tasksToReschedule, 0, tmp, 0, tasksToReschedule.length);
        for (int i = tasksToReschedule.length; i < CoreManager.coreCount; i++) {
            tmp[i] = new LinkedList<Task>();
        }
        tasksToReschedule = tmp;
        
        tmp = new LinkedList[CoreManager.coreCount];
        System.arraycopy(priorityTasks, 0, tmp, 0, priorityTasks.length);
        for (int i = priorityTasks.length; i < CoreManager.coreCount; i++) {
            tmp[i] = new LinkedList<Task>();
        }
        priorityTasks = tmp;
        
        tmp = new LinkedList[CoreManager.coreCount];
        System.arraycopy(regularTasks, 0, tmp, 0, regularTasks.length);
        for (int i = regularTasks.length; i < CoreManager.coreCount; i++) {
            tmp[i] = new LinkedList<Task>();
        }
        regularTasks = tmp;

        //METHOD INFORMATION
        Task[] firstMethodExecutionTmp = new Task[CoreManager.coreCount];
        System.arraycopy(firstMethodExecution, 0, firstMethodExecutionTmp, 0, firstMethodExecution.length);
        firstMethodExecution = firstMethodExecutionTmp;

        Long[] coreAverageExecutionTimeTmp = new Long[CoreManager.coreCount];
        System.arraycopy(coreAverageExecutionTime, 0, coreAverageExecutionTimeTmp, 0, coreAverageExecutionTime.length);
        for (int i = coreAverageExecutionTime.length; i < CoreManager.coreCount; i++) {
            coreAverageExecutionTimeTmp[i] = 0l;
        }
        coreAverageExecutionTime = coreAverageExecutionTimeTmp;

        Long[] coreMaxExecutionTimeTmp = new Long[CoreManager.coreCount];
        System.arraycopy(coreMaxExecutionTime, 0, coreMaxExecutionTimeTmp, 0, coreMaxExecutionTime.length);
        for (int i = coreMaxExecutionTime.length; i < CoreManager.coreCount; i++) {
            coreMaxExecutionTimeTmp[i] = 0l;
        }
        coreMaxExecutionTime = coreMaxExecutionTimeTmp;

        Long[] coreMinExecutionTimeTmp = new Long[CoreManager.coreCount];
        System.arraycopy(coreMinExecutionTime, 0, coreMinExecutionTimeTmp, 0, coreMinExecutionTime.length);
        for (int i = coreMinExecutionTime.length; i < CoreManager.coreCount; i++) {
            coreMinExecutionTimeTmp[i] = 0l;
        }
        coreMinExecutionTime = coreMinExecutionTimeTmp;

        int[] coreExecutedCountTmp = new int[CoreManager.coreCount];
        System.arraycopy(coreExecutedCount, 0, coreExecutedCountTmp, 0, coreExecutedCount.length);
        coreExecutedCount = coreExecutedCountTmp;
    }
}
