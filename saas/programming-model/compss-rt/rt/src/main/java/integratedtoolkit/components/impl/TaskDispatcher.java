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
package integratedtoolkit.components.impl;

import integratedtoolkit.types.request.td.UpdateLocalCEIRequest;
import integratedtoolkit.ITConstants;
import integratedtoolkit.components.scheduler.impl.DefaultTaskScheduler;
import integratedtoolkit.components.JobStatus;
import integratedtoolkit.components.Schedule;
import integratedtoolkit.connectors.utils.CreationThread;
import integratedtoolkit.connectors.utils.DeletionThread;
import integratedtoolkit.log.Loggers;
import integratedtoolkit.types.Resource;
import integratedtoolkit.types.ResourceCreationRequest;
import integratedtoolkit.types.ScheduleDecisions;
import integratedtoolkit.types.ScheduleState;
import integratedtoolkit.types.Task;
import integratedtoolkit.types.Task.TaskState;
import integratedtoolkit.types.data.DataAccessId;
import integratedtoolkit.types.data.DataInstanceId;
import integratedtoolkit.types.data.Location;
import integratedtoolkit.types.data.ResultFile;
import integratedtoolkit.types.request.td.*;
import integratedtoolkit.types.ResourceDestructionRequest;
import integratedtoolkit.types.WorkerNode;
import integratedtoolkit.util.CoreManager;
import integratedtoolkit.util.ProjectManager;
import integratedtoolkit.util.ResourceManager;
import java.lang.reflect.Constructor;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.Semaphore;
import org.apache.log4j.Logger;

public class TaskDispatcher implements Runnable, Schedule, JobStatus {

    // Other supercomponent
    protected TaskProcessor TP;
    // Subcomponents
    protected TaskScheduler TS;
    protected JobManager JM;
    protected FileTransferManager FTM;
    protected SchedulingOptimizer SO;
    // Queue that can contain ready, finished or to-reschedule tasks
    protected LinkedBlockingQueue<TDRequest> requestQueue;
    protected LinkedBlockingQueue<TDRequest> readQueue;
    protected LinkedBlockingQueue<TDRequest> prioritaryTaskQueue;
    // Scheduler thread
    protected Thread dispatcher;
    protected boolean keepGoing;

    //End of Execution
    private boolean endRequested;
    //Number of Tasks to execute
    private int[] taskCountToEnd;
    // Logging
    protected static final Logger logger = Logger.getLogger(Loggers.TD_COMP);
    protected static final boolean debug = logger.isDebugEnabled();
    // Component logger - No need to configure, ProActive does
    protected static final Logger monitor = Logger.getLogger(Loggers.RESOURCES);
    protected static final boolean monitorDebug = monitor.isDebugEnabled();

    private static final String PROJ_LOAD_ERR = "Error loading project information";
    private static final String RES_LOAD_ERR = "Error loading resource information";
    private static final String CREAT_INIT_VM_ERR = "Error creating initial VMs";
    private static final String DEL_VM_ERR = "Error deleting VMs";

    public TaskDispatcher() {
        endRequested = false;
        CoreManager.load();
        if (!ProjectManager.isInit()) {
            try {
                ProjectManager.init();
            } catch (Exception e) {
                logger.fatal(PROJ_LOAD_ERR, e);
                System.exit(1);
            }
        }
        try {
            ResourceManager.load();
        } catch (ClassNotFoundException e) {
            logger.fatal(CREAT_INIT_VM_ERR, e);
            System.exit(1);
        } catch (Throwable e) {
            logger.fatal(RES_LOAD_ERR, e);
            System.exit(1);
        }
        try {
            String schedulerPath = System.getProperty(ITConstants.IT_SCHEDULER);
            if (schedulerPath == null || schedulerPath.compareTo("default") == 0) {
                TS = new DefaultTaskScheduler();
            } else {
                Class<?> conClass = Class.forName(schedulerPath);
                Constructor<?> ctor = conClass.getDeclaredConstructors()[0];
                TS = (TaskScheduler) ctor.newInstance();
            }

        } catch (Exception e) {
            logger.fatal(CREAT_INIT_VM_ERR, e);
            System.exit(1);
        }
        for (Resource r : ResourceManager.getAllResources()) {
            if (r.getType() == Resource.Type.SERVICE) {
                TS.resourcesCreated(r.getName(), null, r.getMaxTaskCount());
            } else {
                TS.resourcesCreated(r.getName(), ((WorkerNode) r).getDescription(), r.getMaxTaskCount());
            }
        }
        JM = new JobManager();
        FTM = new FileTransferManager();
        SO = new SchedulingOptimizer();
        SO.setName("SchedulerOptimizer");

        taskCountToEnd = new int[CoreManager.coreCount];

        requestQueue = new LinkedBlockingQueue<TDRequest>();
        readQueue = requestQueue;
        prioritaryTaskQueue = new LinkedBlockingQueue<TDRequest>();

        keepGoing = true;
        dispatcher = new Thread(this);
        dispatcher.setName("Task Dispatcher");
        dispatcher.start();

        Runtime.getRuntime().addShutdownHook(new Ender());
        logger.info("Initialization finished");
    }

    public void setTP(TaskProcessor TP) {
        this.TP = TP;
        CreationThread.setTaskDispatcher(this);
        DeletionThread.setTaskDispatcher(this);
        TS.setCoWorkers(JM, FTM);
        JM.setCoWorkers(TP, this, FTM);
        FTM.setCoWorkers(this, JM);
        SO.setCoWorkers(this);
        SO.start();
        if (ResourceManager.useCloud()) {
            try {
                //Creates the VM needed for execute the methods
                int alreadyCreated = ResourceManager.addBasicNodes();
                //Distributes the rest of the VM
                ResourceManager.addExtraNodes(alreadyCreated);
            } catch (Exception e) {
                e.printStackTrace();
            }

        }
    }

    public void cleanup() {
        if (ResourceManager.useCloud()) {
            // Stop all Cloud VM
            try {
                ResourceManager.stopVirtualNodes();
            } catch (Exception e) {
                logger.error(ITConstants.TS + ": " + DEL_VM_ERR, e);
            }
        }
        FTM.cleanup();
        JM.cleanup();
        SO.kill();

        keepGoing = false;
        dispatcher.interrupt();
    }

    // Dispatcher thread
    public void run() {
        while (keepGoing) {

            TDRequest request = null;
            try {
                request = readQueue.take();
            } catch (InterruptedException e) {
                continue;
            }
            dispatchRequest(request);
        }
    }

    private void addRequest(TDRequest request) {
        requestQueue.offer(request);
    }

    private void addPrioritaryRequest(TDRequest request) {
        prioritaryTaskQueue.offer(request);
        readQueue = prioritaryTaskQueue;
        dispatcher.interrupt();
        while (prioritaryTaskQueue.size() > 0) {
            try {
                Thread.sleep(50);
            } catch (InterruptedException ex) {
            }
        }
        readQueue = requestQueue;
        dispatcher.interrupt();
    }

    protected void dispatchRequest(TDRequest request) {
        Task task;
        int coreId;
        int taskId;
        int implId;
        String resourceName;
        ResourceCreationRequest rcr;
        ResourceDestructionRequest rdr;
        switch (request.getRequestType()) {
            case SCHEDULE_TASKS:
                ScheduleTasksRequest stRequest = (ScheduleTasksRequest) request;
                List<Task> toSchedule = stRequest.getToSchedule();
                LinkedList<String> obsoletes = stRequest.getObsoletes();
                if (obsoletes != null) {
                    FTM.obsoleteVersions(obsoletes);
                }
                SO.updateWaitingCounts(toSchedule, stRequest.getWaiting(), stRequest.getWaitingCount());
                for (Task currentTask : toSchedule) {
                    taskCountToEnd[currentTask.getTaskParams().getId()]++;
                    currentTask.setStatus(TaskState.TO_SCHEDULE);
                    TS.scheduleTask(currentTask);
                }
                break;
            case FINISHED_TASK:
                NotifyTaskEndRequest nte = (NotifyTaskEndRequest) request;
                task = nte.getTask();
                implId = nte.getImplementationId();
                resourceName = nte.getResourceName();
                coreId = task.getTaskParams().getId();
                taskCountToEnd[coreId]--;
                TS.taskEnd(task, resourceName, implId);
                ResourceManager.freeResource(resourceName, CoreManager.getCoreImplementations(coreId)[implId]);
                rdr = ResourceManager.checkPendingModifications(resourceName);
                if (rdr != null && rdr.isTerminate()) {
                    resourceName = rdr.getRequested().getName();
                    TS.removeNode(resourceName);
                    FTM.transferStopFiles(rdr, ResourceManager.getBestSafeResourcePerCore());
                } else {
                    if (!TS.scheduleToResource(resourceName) && endRequested) {
                        mayTerminateEnvironment(resourceName);
                    }
                }

                break;
            case RESCHEDULE_TASK:
                // Get the corresponding task to reschedule
                RescheduleTaskRequest rqr = (RescheduleTaskRequest) request;
                task = rqr.getTask();
                coreId = task.getTaskParams().getId();
                taskId = task.getId();
                implId = rqr.getImplementationId();
                resourceName = rqr.getResource();
                logger.debug("Reschedule: Task " + taskId + " failed to run in " + resourceName);
                //register task execution end 
                TS.taskEnd(task, resourceName, implId);
                ResourceManager.freeResource(resourceName, CoreManager.getCoreImplementations(coreId)[implId]);
                rdr = ResourceManager.checkPendingModifications(resourceName);
                if (rdr != null && rdr.isTerminate()) {
                    resourceName = rdr.getRequested().getName();
                    TS.removeNode(resourceName);
                    FTM.transferStopFiles(rdr, ResourceManager.getBestSafeResourcePerCore());
                } else {
                    //schedule another task in the failed resource
                    if (!TS.scheduleToResource(resourceName) && endRequested) {
                        mayTerminateEnvironment(resourceName);
                    }
                }
                TS.rescheduleTask(task, resourceName);
                break;
            case NEW_WAITING_TASK:
                NewWaitingTaskRequest nwtRequest = (NewWaitingTaskRequest) request;
                obsoletes = nwtRequest.getObsoletes();
                if (obsoletes != null) {
                    FTM.obsoleteVersions(obsoletes);
                }
                SO.newWaitingTask(nwtRequest.getMethodId());
                break;
            case NEW_DATA_VERSION:
                NewDataVersionRequest ndvRequest = (NewDataVersionRequest) request;
                FTM.newDataVersion(ndvRequest.getLastDID().getRenaming(), ndvRequest.getFileName(), ndvRequest.getLocation());
                break;
            case TRANSFER_OPEN_FILE:
                TransferOpenFileRequest tofRequest = (TransferOpenFileRequest) request;
                FTM.transferFileForOpen(tofRequest.getFaId(), tofRequest.getLocation(), tofRequest.getSemaphore());
                break;
            case TRANSFER_RAW_FILE:
                TransferRawFileRequest trfRequest = (TransferRawFileRequest) request;
                FTM.transferFileRaw(trfRequest.getFaId(), trfRequest.getLocation(), trfRequest.getSemaphore());
                break;
            case TRANSFER_OBJECT:
                FTM.transferObjectValue((TransferObjectRequest) request);
                break;
            case TRANSFER_RESULT_FILES:
                TransferResultFilesRequest tresfRequest = (TransferResultFilesRequest) request;
                FTM.transferBackResultFiles(tresfRequest.getResFiles(), tresfRequest.getSemaphore());
                break;
            case TRANSFER_TRACE_FILES:
                TransferTraceFilesRequest ttracefRequest = (TransferTraceFilesRequest) request;
                FTM.transferTraceFiles(ttracefRequest.getLocation(), ttracefRequest.getSemaphore());
                break;
            case DELETE_INTERMEDIATE_FILES:
                FTM.deleteIntermediateFiles(((DeleteIntermediateFilesRequest) request).getSemaphore());
                break;
            case GET_STATE:
                ScheduleState state = new ScheduleState();
                state.endRequested = endRequested;
                ResourceManager.getResourcesState(state);
                TS.getSchedulingState(state);
                ((GetCurrentScheduleRequest) request).setResponse(state);
                ((GetCurrentScheduleRequest) request).getSemaphore().release();
                break;
            case SET_STATE:
                ScheduleDecisions decisions = ((SetNewScheduleRequest) request).getNewState();
                TS.setSchedulingState(decisions);
                applyResourceChanges(decisions);
                break;
            case ADD_CLOUD:
                AddCloudNodeRequest acnRequest = (AddCloudNodeRequest) request;
                rcr = acnRequest.getRequest();
                addCloudNode(acnRequest.getName(), acnRequest.getProvider(), rcr, acnRequest.getCheck(), acnRequest.getLimitOfTasks());
                SO.optimizeNow();
                break;
            case REMOVE_CLOUD:
                RemoveCloudNodeRequest rcnRequest = (RemoveCloudNodeRequest) request;
                rdr = rcnRequest.getRequest();
                ResourceManager.notifyShutdown(rdr);
                break;
            case REMOVE_OBSOLETES:
                RemoveObsoletesRequest ror = (RemoveObsoletesRequest) request;
                obsoletes = ror.getObsoletes();
                if (obsoletes != null) {
                    FTM.obsoleteVersions(obsoletes);
                }
                break;
            case REFUSE_CLOUD:
                RefuseCloudWorkerRequest rcwRequest = (RefuseCloudWorkerRequest) request;
                ResourceManager.errorCloudRequest(rcwRequest.getRequest(), rcwRequest.getProvider());
                break;
            case MONITOR_DATA:
                String monitorData = TS.getMonitoringState();
                ((MonitoringDataRequest) request).setResponse(monitorData);
                ((MonitoringDataRequest) request).getSemaphore().release();
                break;
            case SHUTDOWN:
                ShutdownRequest sRequest = (ShutdownRequest) request;
                obsoletes = sRequest.getObsoletes();
                if (obsoletes != null) {
                    FTM.obsoleteVersions(obsoletes);
                }
                for (java.util.Map.Entry<Integer, Integer> entry : sRequest.getCurrentTaskCount().entrySet()) {
                    taskCountToEnd[entry.getKey()] += entry.getValue();
                }
                SO.cleanUp();
                this.endRequested = true;
                LinkedList<ResourceDestructionRequest> rdrs = ResourceManager.terminateUnbounded(taskCountToEnd);
                for (ResourceDestructionRequest dest : rdrs) {
                    TS.reduceResource(dest.getRequested());
                    if (dest.isTerminate()) {
                        resourceName = dest.getRequested().getName();
                        TS.removeNode(resourceName);
                        FTM.transferStopFiles(dest, ResourceManager.getBestSafeResourcePerCore());
                    }
                }
                FTM.waitForTransfers(sRequest.getSemaphore());
                break;
            case UPDATE_LOCAL_CEI:
                UpdateLocalCEIRequest uCEIReq = (UpdateLocalCEIRequest) request;
                logger.info("Treating request to update core elements");
                LinkedList<Integer> newCores = CoreManager.loadJava(uCEIReq.getCeiClass());
                logger.debug("New methods: " + newCores);
                ResourceManager.newCoreElementsDetected(newCores);
                TS.resizeDataStructures();
                SO.resizeDataStructures();
                logger.debug("Data structures resized and CE-resources links updated");
                uCEIReq.getSemaphore().release();
                break;
        }
    }

    private void addCloudNode(String schedulerName, String provider, ResourceCreationRequest rcr, boolean check, Integer limitOfTasks) {

        if (check) {
            List<Integer> coreIds = CoreManager.findExecutableCores(rcr.getGranted());
            boolean moreJobs = false;
            for (Integer coreId : coreIds) {
                Integer stats = taskCountToEnd[coreId];
                boolean pendingGraph = (stats != null && stats != 0);
                moreJobs = pendingGraph || TS.isPendingWork(coreId);
                if (moreJobs) {
                    break;
                }
            }
            if (!moreJobs) {
                if (debug) {
                    logger.debug("There are no more Tasks for resource " + schedulerName);
                }
                ResourceManager.refuseCloudRequest(rcr, provider);
                return;
            }
        }
        ResourceManager.addCloudResource(schedulerName, rcr.getGranted(), limitOfTasks);
        TS.resourcesCreated(schedulerName, rcr.getGranted(), limitOfTasks);

    }

    private void applyResourceChanges(ScheduleDecisions newState) {
        ResourceCreationRequest rcr = null;
        ResourceDestructionRequest rdr = null;
        try {
            if (ResourceManager.useCloud()) {

                if (newState.mandatoryCreations) {
                    rcr = ResourceManager.increaseResources(newState.creationRecommendations, newState.requiredRecomendations, newState.mandatoryCreations);
                    if (rcr == null && newState.recommendsDestruction) {
                        rdr = ResourceManager.reduceResources(newState.destroyRecommendations, newState.mandatoryDestruction);
                        if (rdr != null) {
                            TS.reduceResource(rdr.getRequested());
                            if (rdr.isTerminate()) {
                                String resourceName = rdr.getRequested().getName();
                                TS.removeNode(resourceName);
                                FTM.transferStopFiles(rdr, ResourceManager.getBestSafeResourcePerCore());
                            }
                        }
                    } else {
                        monitor.debug(ResourceManager.getCurrentState(""));
                    }
                } else {
                    if (newState.recommendsDestruction) {
                        rdr = ResourceManager.reduceResources(newState.destroyRecommendations, newState.mandatoryDestruction);
                        if (rdr != null) {
                            TS.reduceResource(rdr.getRequested());
                            if (rdr.isTerminate()) {
                                String resourceName = rdr.getRequested().getName();
                                TS.removeNode(resourceName);
                                FTM.transferStopFiles(rdr, ResourceManager.getBestSafeResourcePerCore());
                            }
                        }
                    }
                    if (rdr == null && newState.recommendsCreation) {
                        rcr = ResourceManager.increaseResources(newState.creationRecommendations, newState.requiredRecomendations, newState.mandatoryCreations);
                        if (rcr != null) {
                            monitor.debug(ResourceManager.getCurrentState(""));
                        }
                    } else {
                        if (rdr != null) {
                            monitor.debug(ResourceManager.getCurrentState(""));
                        }
                    }
                }
            }
        } catch (Exception e) {
            logger.error("CAN NOT UPDATE THE CURRENT STATE", e);
        }
    }

    private void mayTerminateEnvironment(String hostName) {
        LinkedList<ResourceDestructionRequest> rdrs = ResourceManager.tryToTerminate(hostName, taskCountToEnd);
        for (ResourceDestructionRequest rdr : rdrs) {
            TS.reduceResource(rdr.getRequested());
            if (rdr.isTerminate()) {
                String resourceName = rdr.getRequested().getName();
                TS.removeNode(resourceName);
                FTM.transferStopFiles(rdr, ResourceManager.getBestSafeResourcePerCore());
            }
        }
    }

    public void safeResourceEnd(ResourceDestructionRequest rdr) {
        ResourceManager.terminate(rdr);
    }

    /**
     * ************************************************************
     *
     *
     ********* Public methods to enqueue requests *************
     *
     *
     **************************************************************
     */
    // TP (TA)
    public void scheduleTasks(List<Task> toSchedule, boolean waiting, int[] waitingCount, LinkedList<String> obsoletes) {
        if (debug) {
            StringBuilder sb = new StringBuilder("Schedule tasks: ");
            for (Task t : toSchedule) {
                sb.append(t.getTaskParams().getName()).append("(").append(t.getId()).append(") ");
            }
            logger.debug(sb);
        }
        addRequest(new ScheduleTasksRequest(toSchedule, waiting, waitingCount, obsoletes));
    }

    // Notification thread (JM)
    public void notifyJobEnd(Task task, int implId, String resource) {
        addRequest(new NotifyTaskEndRequest(task, implId, resource));
    }

    // Notification thread (JM) / Transfer threads (FTM)
    public void rescheduleJob(Task task, int implId, String hostName) {
        task.setStatus(TaskState.TO_RESCHEDULE);
        addRequest(new RescheduleTaskRequest(task, implId, hostName));
    }

    // TP (TA)
    public void newWaitingTask(int methodId, LinkedList<String> obsoletes) {
        addRequest(new NewWaitingTaskRequest(methodId, obsoletes));
    }

    // TP (DIP)
    public void newDataVersion(DataInstanceId lastDID, String fileName, Location location) {
        addRequest(new NewDataVersionRequest(lastDID, fileName, location));
    }

    // App
    public void transferFileForOpen(DataAccessId faId, Location location) {
        Semaphore sem = new Semaphore(0);
        addRequest(new TransferOpenFileRequest(faId, location, sem));

        try {
            sem.acquire();
        } catch (InterruptedException e) {
        }

        if (debug) {
            logger.debug("File for open transferred");
        }
    }

    // App
    public void transferFileRaw(DataAccessId faId, Location location) {
        Semaphore sem = new Semaphore(0);
        TransferRawFileRequest request = new TransferRawFileRequest(faId, location, sem);
        addRequest(request);

        try {
            sem.acquire();
        } catch (InterruptedException e) {
        }

        if (debug) {
            logger.debug("Raw file transferred");
        }
    }

    // App
    public Object transferObject(DataAccessId daId, String path, String host, String wRename) {
        Semaphore sem = new Semaphore(0);
        TransferObjectRequest request = new TransferObjectRequest(daId, path, host, sem);
        addRequest(request);

        try {
            sem.acquire();
        } catch (InterruptedException e) {
        }

        if (debug) {
            logger.debug("Object transferred");
        }

        return request.getResponse();
    }

    // App and TP (TA)
    public void transferBackResultFiles(List<ResultFile> resFiles, boolean wait) {
        Semaphore sem = new Semaphore(0);
        TransferResultFilesRequest request = new TransferResultFilesRequest(resFiles, sem);
        addRequest(request);

        if (wait) {
            try {
                sem.acquire();
            } catch (InterruptedException e) {
            }

            if (debug) {
                logger.debug("Result files transferred");
            }
        }
    }

    // App
    public void transferTraceFiles(Location loc) {
        Semaphore sem = new Semaphore(0);
        TransferTraceFilesRequest request = new TransferTraceFilesRequest(loc, sem);
        addRequest(request);

        try {
            sem.acquire();
        } catch (InterruptedException e) {
        }

        if (debug) {
            logger.debug("Trace files transferred");
        }
    }

    // App
    public void deleteIntermediateFiles() {
        Semaphore sem = new Semaphore(0);
        DeleteIntermediateFilesRequest request = new DeleteIntermediateFilesRequest(sem);
        addRequest(request);

        try {
            sem.acquire();
        } catch (InterruptedException e) {
        }

        if (debug) {
            logger.debug("Intermediate files deleted");
        }
    }

    // Scheduling optimizer thread
    public ScheduleState getCurrentSchedule() {
        Semaphore sem = new Semaphore(0);
        GetCurrentScheduleRequest request = new GetCurrentScheduleRequest(sem);
        addPrioritaryRequest(request);
        try {
            sem.acquire();
        } catch (InterruptedException e) {
        }

        return request.getResponse();
    }

    // Scheduling optimizer thread
    public void setNewSchedule(ScheduleDecisions newSchedule) {
        SetNewScheduleRequest request = new SetNewScheduleRequest(newSchedule);
        addPrioritaryRequest(request);
    }

    // Creation thread
    public void addCloudNode(String schedulerName, ResourceCreationRequest rcr, String provider, int limitOfTasks, boolean check) {
        AddCloudNodeRequest request = new AddCloudNodeRequest(schedulerName, provider, rcr, limitOfTasks, check);
        addPrioritaryRequest(request);
    }

    // Creation thread
    public void refuseCloudWorkerRequest(ResourceCreationRequest rcr, String provider) {
        RefuseCloudWorkerRequest request = new RefuseCloudWorkerRequest(rcr, provider);
        addPrioritaryRequest(request);
    }

    // TP (TA)
    public void shutdown(HashMap<Integer, Integer> currentTaskCount, LinkedList<String> obsoletes) {
        Semaphore sem = new Semaphore(0);
        ShutdownRequest request = new ShutdownRequest(currentTaskCount, obsoletes, sem);
        addRequest(request);
        try {
            sem.acquire();
        } catch (InterruptedException e) {
        }
    }

    public void removeObsoletes(LinkedList<String> obsoleteRenamings) {
        if (obsoleteRenamings != null) {
            addRequest(new RemoveObsoletesRequest(obsoleteRenamings));
        }
    }

    /**
     * Returs a string with the description of the tasks in the graph
     *
     * @return description of the current tasks in the graph
     */
    public String getCurrentMonitoringData() {
        Semaphore sem = new Semaphore(0);
        MonitoringDataRequest request = new MonitoringDataRequest(sem);
        requestQueue.offer(request);
        try {
            sem.acquire();
        } catch (InterruptedException e) {
        }
        return (String) request.getResponse();
    }

    public void notifyShutdown(ResourceDestructionRequest rdr) {
        RemoveCloudNodeRequest rcr = new RemoveCloudNodeRequest(rdr);
    }

    class Ender extends Thread {

        public void run() {
            if (ResourceManager.useCloud()) {
                // Stop all Cloud VM
                try {
                    ResourceManager.stopVirtualNodes();
                } catch (Exception e) {
                    logger.error(ITConstants.TS + ": " + DEL_VM_ERR, e);
                }
            }
            try {

                SO.kill();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public void addInterface(Class<?> forName) {
        if (debug) {
            logger.debug("Updating CEI " + forName.getName());
        }
        Semaphore sem = new Semaphore(0);
        addRequest(new UpdateLocalCEIRequest(forName, sem));

        try {
            sem.acquire();
        } catch (InterruptedException e) {
        }

        if (debug) {
            logger.debug("Updated CEI " + forName.getName());
        }
    }
}
