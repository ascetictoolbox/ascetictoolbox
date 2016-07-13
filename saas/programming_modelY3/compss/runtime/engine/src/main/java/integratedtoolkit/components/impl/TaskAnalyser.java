package integratedtoolkit.components.impl;

import java.util.Hashtable;
import java.util.LinkedList;
import java.util.List;
import java.util.HashMap;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.Map.Entry;
import java.util.concurrent.Semaphore;

import org.apache.log4j.Logger;

import integratedtoolkit.api.COMPSsRuntime.DataType;
import integratedtoolkit.components.monitor.impl.GraphGenerator;
import integratedtoolkit.log.Loggers;
import integratedtoolkit.types.TaskParams;
import integratedtoolkit.types.Task;
import integratedtoolkit.types.Task.TaskState;
import integratedtoolkit.types.TaskParams.Type;
import integratedtoolkit.types.data.DataAccessId.RWAccessId;
import integratedtoolkit.types.data.DataInstanceId;
import integratedtoolkit.types.data.AccessParams.*;
import integratedtoolkit.types.data.DataAccessId;
import integratedtoolkit.types.data.DataAccessId.*;
import integratedtoolkit.types.parameter.Parameter;
import integratedtoolkit.types.parameter.DependencyParameter;
import integratedtoolkit.types.parameter.SCOParameter;
import integratedtoolkit.types.data.FileInfo;
import integratedtoolkit.types.data.operation.ResultListener;
import integratedtoolkit.types.parameter.FileParameter;
import integratedtoolkit.types.parameter.ObjectParameter;
import integratedtoolkit.types.request.ap.EndOfAppRequest;
import integratedtoolkit.types.request.ap.WaitForAllTasksRequest;
import integratedtoolkit.types.request.ap.WaitForTaskRequest;
import integratedtoolkit.util.ErrorManager;


public class TaskAnalyser {

    // Constants definition
    private static final String TASK_FAILED = "Task failed: ";

    // Components
    private DataInfoProvider DIP;
    private GraphGenerator GM;

    // <File id, Last writer task> table
    private TreeMap<Integer, Task> writers;
    // Method information
    private HashMap<Integer, Integer> currentTaskCount;
    // Map: app id -> task count
    private HashMap<Long, Integer> appIdToTotalTaskCount;
    // Map: app id -> task count
    private HashMap<Long, Integer> appIdToTaskCount;
    // Map: app id -> semaphore to notify end of app
    private HashMap<Long, Semaphore> appIdToSemaphore;
    // Map: app id -> set of written data ids (for result files)
    private HashMap<Long, TreeSet<Integer>> appIdToWrittenFiles;
    // Map: app id -> set of written data ids (for result SCOs)
    private HashMap<Long, TreeSet<Integer>> appIdToSCOWrittenIds;
    // Tasks being waited on: taskId -> list of semaphores where to notify end of task
    private Hashtable<Task, List<Semaphore>> waitedTasks;

    // Logger
    private static final Logger logger = Logger.getLogger(Loggers.TA_COMP);
    private static final boolean debug = logger.isDebugEnabled();
    // Graph drawing
    private static final boolean drawGraph = GraphGenerator.isEnabled();

    private static int synchronizationId;

    public TaskAnalyser() {
        currentTaskCount = new HashMap<Integer, Integer>();
        writers = new TreeMap<Integer, Task>();
        appIdToTaskCount = new HashMap<Long, Integer>();
        appIdToTotalTaskCount = new HashMap<Long, Integer>();
        appIdToSemaphore = new HashMap<Long, Semaphore>();
        appIdToWrittenFiles = new HashMap<Long, TreeSet<Integer>>();
        appIdToSCOWrittenIds = new HashMap<Long, TreeSet<Integer>>();
        waitedTasks = new Hashtable<Task, List<Semaphore>>();
        synchronizationId = 0;
        logger.info("Initialization finished");
    }

    public void setCoWorkers(DataInfoProvider DIP) {
        this.DIP = DIP;
    }

    public void setGM(GraphGenerator GM) {
        this.GM = GM;
    }

    public void processTask(Task currentTask) {
        TaskParams params = currentTask.getTaskParams();
        logger.info("New " + (params.getType() == Type.METHOD ? "method" : "service") + " task(" + params.getName() + "), ID = " + currentTask.getId());
        if (drawGraph) {
            this.GM.addTaskToGraph(currentTask);
            if (synchronizationId > 0) {
                this.GM.addEdgeToGraph("Synchro" + synchronizationId, String.valueOf(currentTask.getId()), "");
            }
        }

        // Update task count
        Integer methodId = params.getId();
        Integer actualCount = currentTaskCount.get(methodId);
        if (actualCount == null) {
            actualCount = 0;
        }
        currentTaskCount.put(methodId, actualCount + 1);

        // Update app id task count
        Long appId = currentTask.getAppId();
        Integer taskCount = appIdToTaskCount.get(appId);
        if (taskCount == null) {
            taskCount = 0;
        }
        taskCount++;
        appIdToTaskCount.put(appId, taskCount);
        Integer totalTaskCount = appIdToTotalTaskCount.get(appId);
        if (totalTaskCount == null) {
            totalTaskCount = 0;
        }
        totalTaskCount++;
        appIdToTotalTaskCount.put(appId, totalTaskCount);

        //Check scheduling enforcing data
        int constrainingParam = -1;
        if (params.getType() == TaskParams.Type.SERVICE && params.hasTargetObject()) {
            if (params.hasReturnValue()) {
                constrainingParam = params.getParameters().length - 2;
            } else {
                constrainingParam = params.getParameters().length - 1;
            }
        }

        Parameter[] parameters = params.getParameters();
        for (int paramIdx = 0; paramIdx < parameters.length; paramIdx++) {
            Parameter p = parameters[paramIdx];
            if (debug) {
                logger.debug("* Parameter : " + p);
            }

            // Conversion: direction -> access mode
            AccessMode am = null;
            switch (p.getDirection()) {
                case IN:
                    am = AccessMode.R;
                    break;
                case OUT:
                    am = AccessMode.W;
                    break;
                case INOUT:
                    am = AccessMode.RW;
                    break;
            }

            // Inform the Data Manager about the new accesses
            DataAccessId daId;
            switch (p.getType()) {
                case FILE_T:
                    FileParameter fp = (FileParameter) p;
                    daId = DIP.registerFileAccess(am, fp.getLocation(), methodId);
                    break;

                case SCO_T:
                case PSCO_T:
                    SCOParameter sco = (SCOParameter) p;
                    daId = DIP.registerObjectAccess(am, sco.getValue(), sco.getCode(), methodId);
                    break;

                case OBJECT_T:
                    ObjectParameter op = (ObjectParameter) p;
                    daId = DIP.registerObjectAccess(am, op.getValue(), op.getCode(), methodId);
                    break;

                default:
                    /* Basic types (including String).
                     * The only possible access mode is R (already checked by the API)
                     */
                    continue;
            }

            //Add dependencies to the graph and register output values for future dependencies
            DependencyParameter dp = (DependencyParameter) p;
            dp.setDataAccessId(daId);
            switch (am) {
                case R:
                    checkDependencyForRead(currentTask, dp);
                    if (paramIdx == constrainingParam) {
                        DataAccessId.RAccessId raId = (DataAccessId.RAccessId) dp.getDataAccessId();
                        DataInstanceId dependingDataId = raId.getReadDataInstance();
                        if (dependingDataId != null) {
                            if (dependingDataId.getVersionId() > 1) {
                                currentTask.setEnforcingTask(writers.get(dependingDataId.getDataId()));
                            }
                        }
                    }
                    break;

                case RW:
                    checkDependencyForRead(currentTask, dp);
                    if (paramIdx == constrainingParam) {
                        DataAccessId.RWAccessId raId = (DataAccessId.RWAccessId) dp.getDataAccessId();
                        DataInstanceId dependingDataId = raId.getReadDataInstance();
                        if (dependingDataId != null) {
                            if (dependingDataId.getVersionId() > 1) {
                                currentTask.setEnforcingTask(writers.get(dependingDataId.getDataId()));
                            }
                        }
                    }
                    registerOutputValues(currentTask, dp);
                    break;

                case W:
                    registerOutputValues(currentTask, dp);
                    break;
            }

        }

    }

    private void checkDependencyForRead(Task currentTask, DependencyParameter dp) {
        int dataId = dp.getDataAccessId().getDataId();
        Task lastWriter = writers.get(dataId);
        if (lastWriter != null && lastWriter != currentTask) { // avoid self-dependencies
            if (debug) {
                logger.debug("Last writer for datum " + dp.getDataAccessId().getDataId() + " is task " + lastWriter.getId());
                logger.debug("Adding dependency between task " + lastWriter.getId() + " and task " + currentTask.getId());
            }

            if (drawGraph) {
                try {
                    boolean b = true;
                    for (Task t : lastWriter.getSuccessors()) {
                        if (t.getId() == currentTask.getId()) {
                            b = false;
                        }
                    }
                    if (b) {
                        this.GM.addEdgeToGraph(String.valueOf(lastWriter.getId()), String.valueOf(currentTask.getId()), String.valueOf(dp.getDataAccessId().getDataId()));
                    }
                } catch (Exception e) {
                    logger.error("Error drawing dependency in graph", e);
                }
            }
            currentTask.addDataDependency(lastWriter);
        } else if (drawGraph && lastWriter != null) {
            this.GM.addEdgeToGraph("Synchro" + (synchronizationId), String.valueOf(currentTask.getId()), "");
        }
    }

    public void registerOutputValues(Task currentTask, DependencyParameter dp) {
        int currentTaskId = currentTask.getId();
        int dataId = dp.getDataAccessId().getDataId();
        Long appId = currentTask.getAppId();
        writers.put(dataId, currentTask); // update global last writer
        if (dp.getType() == DataType.FILE_T) { // Objects are not checked, their version will be only get if the main access them
            TreeSet<Integer> idsWritten = appIdToWrittenFiles.get(appId);
            if (idsWritten == null) {
                idsWritten = new TreeSet<Integer>();
                appIdToWrittenFiles.put(appId, idsWritten);
            }
            idsWritten.add(dataId);
        }
        if (dp.getType() == DataType.PSCO_T) {
            TreeSet<Integer> idsWritten = appIdToSCOWrittenIds.get(appId);
            if (idsWritten == null) {
                idsWritten = new TreeSet<Integer>();
                appIdToSCOWrittenIds.put(appId, idsWritten);
            }
            idsWritten.add(dataId);
        }
        if (debug) {
            logger.debug("New writer for datum " + dp.getDataAccessId().getDataId() + " is task " + currentTaskId);
        }
    }

    public void endTask(Task task) {
        logger.info("Notification received for task " + task.getId() + " with end status " + task.getStatus());
        if (task.getStatus() == TaskState.FAILED) {
            ErrorManager.error(TASK_FAILED + task);
        }

        // Update app id task count
        Long appId = task.getAppId();
        Integer taskCount = appIdToTaskCount.get(appId) - 1;
        appIdToTaskCount.put(appId, taskCount);
        if (taskCount == 0) {
            Semaphore sem = appIdToSemaphore.remove(appId);
            if (sem != null) { // App has notified that no more tasks are coming
                appIdToTaskCount.remove(appId);
                sem.release();
            }
        }

        // Check if task is being waited
        List<Semaphore> sems = waitedTasks.remove(task);
        if (sems != null) {
            for (Semaphore sem : sems) {
                sem.release();
            }
        }

        for (Parameter param : task.getTaskParams().getParameters()) {
            DataType type = param.getType();
            if (type == DataType.FILE_T || type == DataType.OBJECT_T || type == DataType.SCO_T || type == DataType.PSCO_T) {
                DependencyParameter dPar = (DependencyParameter) param;
                DataAccessId dAccId = dPar.getDataAccessId();
                DIP.dataHasBeenAccessed(dAccId);
            }
        }

        // Add the task to the set of finished tasks
        //finishedTasks.add(task);
        // Check if the finished task was the last writer of a file, but only if task generation has finished
        if (appIdToSemaphore.get(appId) != null) {
            checkResultFileTransfer(task);
        }

        task.releaseDataDependents();
    }

    // Private method to check if a finished task is the last writer of its file parameters and eventually order the necessary transfers
    private void checkResultFileTransfer(Task t) {
        LinkedList<DataInstanceId> fileIds = new LinkedList<DataInstanceId>();
        for (Parameter p : t.getTaskParams().getParameters()) {
            switch (p.getType()) {
                case FILE_T:
                    FileParameter fp = (FileParameter) p;
                    switch (fp.getDirection()) {
                        case IN:
                            break;
                        case INOUT:
                            DataInstanceId dId = ((RWAccessId) fp.getDataAccessId()).getWrittenDataInstance();
                            if (writers.get(dId.getDataId()) == t) {
                                fileIds.add(dId);
                            }
                            break;
                        case OUT:
                            dId = ((WAccessId) fp.getDataAccessId()).getWrittenDataInstance();
                            if (writers.get(dId.getDataId()) == t) {
                                fileIds.add(dId);
                            }
                            break;
                    }
                    break;
                default:
                    break;
            }
        }
        // Order the transfer of the result files
        final int numFT = fileIds.size();
        if (numFT > 0) {
            //List<ResultFile> resFiles = new ArrayList<ResultFile>(numFT);
            for (DataInstanceId fileId : fileIds) {
                try {
                    int id = fileId.getDataId();
                    DIP.blockDataAndGetResultFile(id, new ResultListener(new Semaphore(0)));
                    DIP.unblockDataId(id);
                } catch (Exception e) {
                }
            }

        }
    }

    public void findWaitedTask(WaitForTaskRequest request) {
        int dataId = request.getDataId();
        AccessMode am = request.getAccessMode();
        Semaphore sem = request.getSemaphore();
        Task lastWriter = writers.get(dataId);
        if (drawGraph && lastWriter != null) {
            if (am == AccessMode.RW) {
                writers.put(dataId, null);
            }
            synchronizationId++;
            try {
                this.GM.addSynchroToGraph(synchronizationId);
                this.GM.addEdgeToGraph(String.valueOf(lastWriter.getId()), "Synchro" + synchronizationId, String.valueOf(dataId));

                if (synchronizationId > 0) {
                    this.GM.addEdgeToGraph("Synchro" + (synchronizationId - 1), "Synchro" + synchronizationId, String.valueOf(dataId));
                }
            } catch (Exception e) {
                logger.error("Error adding task to graph file", e);
            }
        }
        if (lastWriter == null || lastWriter.getStatus() == TaskState.FINISHED) {
            sem.release();
        } else {
            List<Semaphore> list = waitedTasks.get(lastWriter);
            if (list == null) {
                list = new LinkedList<Semaphore>();
                waitedTasks.put(lastWriter, list);
            }
            list.add(sem);
        }
    }
    
    public void waitForAllTasks(WaitForAllTasksRequest request) {
        Long appId = request.getAppId();
        Integer count = appIdToTaskCount.get(appId);
        
        // We can draw the graph on a barrier while we wait for tasks
        if (drawGraph) {
            this.GM.commitGraph();
        }
        
        // Release the sem only if all app tasks have finished
        if (count == null || count == 0) {
            request.getSemaphore().release();
        } else {
            appIdToSemaphore.put(appId, request.getSemaphore());
        }
    }

    public void noMoreTasks(EndOfAppRequest request) {
        Long appId = request.getAppId();
        Integer count = appIdToTaskCount.get(appId);
        if (drawGraph) {
            this.GM.commitGraph();
        }
        if (count == null || count == 0) {
            appIdToTaskCount.remove(appId);
            request.getSemaphore().release();
        } else {
            appIdToSemaphore.put(appId, request.getSemaphore());
        }
    }

    public TreeSet<Integer> getAndRemoveWrittenFiles(Long appId) {
        TreeSet<Integer> data = appIdToWrittenFiles.remove(appId);
        return data;
    }

    public void shutdown() {
        if (drawGraph) {
            GraphGenerator.removeTemporaryGraph();
        }
    }

    public String getTaskStateRequest() {
        StringBuilder sb = new StringBuilder("\t<TasksInfo>\n");
        for (Entry<Long, Integer> e : appIdToTotalTaskCount.entrySet()) {
            Long appId = e.getKey();
            Integer totalTaskCount = e.getValue();
            Integer taskCount = appIdToTaskCount.get(appId);
            if (taskCount == null) {
                taskCount = 0;
            }
            int completed = totalTaskCount - taskCount;
            sb.append("\t\t<Application id=\"").append(appId).append("\">\n");
            sb.append("\t\t\t<TotalCount>").append(totalTaskCount).append("</TotalCount>\n");
            sb.append("\t\t\t<InProgress>").append(taskCount).append("</InProgress>\n");
            sb.append("\t\t\t<Completed>").append(completed).append("</Completed>\n");
            sb.append("\t\t</Application>\n");
        }
        sb.append("\t</TasksInfo>\n");
        return sb.toString();
    }

    public void deleteFile(FileInfo fileInfo) {
        int dataId = fileInfo.getDataId();
        Task task = writers.get(dataId);
        if (task != null) {
            return;
        }
        for (TreeSet<Integer> files : appIdToWrittenFiles.values()) {
            files.remove(fileInfo.getDataId());
        }
    }
}
