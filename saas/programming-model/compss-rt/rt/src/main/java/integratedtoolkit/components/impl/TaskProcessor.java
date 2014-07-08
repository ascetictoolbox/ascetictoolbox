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

import integratedtoolkit.components.DataAccess.AccessMode;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;
import java.util.TreeSet;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.Semaphore;

import org.apache.log4j.Logger;

import integratedtoolkit.components.TaskCreation;
import integratedtoolkit.components.TaskStatus;
import integratedtoolkit.log.Loggers;
import integratedtoolkit.types.Parameter;
import integratedtoolkit.types.Task;
import integratedtoolkit.types.data.AccessParams;
import integratedtoolkit.types.data.DataAccessId;
import integratedtoolkit.types.data.DataInstanceId;
import integratedtoolkit.types.data.FileInfo;
import integratedtoolkit.types.data.ResultFile;
import integratedtoolkit.types.request.tp.AlreadyAccessedRequest;
import integratedtoolkit.types.request.tp.BlockAndGetResultFilesRequest;
import integratedtoolkit.types.request.tp.DeleteFileRequest;
import integratedtoolkit.types.request.tp.EndOfAppRequest;
import integratedtoolkit.types.request.tp.GetLastDataAccessRequest;
import integratedtoolkit.types.request.tp.GetLastRenamingRequest;
import integratedtoolkit.types.request.tp.GraphDescriptionRequest;
import integratedtoolkit.types.request.tp.GraphUpdateRequest;
import integratedtoolkit.types.request.tp.IsObjectHereRequest;
import integratedtoolkit.types.request.tp.NewVersionSameValueRequest;
import integratedtoolkit.types.request.tp.RegisterDataAccessRequest;
import integratedtoolkit.types.request.tp.SetObjectVersionValueRequest;
import integratedtoolkit.types.request.tp.ShutdownRequest;
import integratedtoolkit.types.request.tp.TPRequest;
import integratedtoolkit.types.request.tp.TaskAnalysisRequest;
import integratedtoolkit.types.request.tp.TasksStateRequest;
import integratedtoolkit.types.request.tp.UnblockResultFilesRequest;
import integratedtoolkit.types.request.tp.WaitForTaskRequest;

public class TaskProcessor implements Runnable, TaskCreation, TaskStatus {

    // Other supercomponent
    protected TaskDispatcher TD;
    // Subcomponents
    protected TaskAnalyser TA;
    protected DataInfoProvider DIP;
    // Processor thread
    private static Thread processor;
    private static boolean keepGoing;
    // Tasks to be processed
    protected LinkedBlockingQueue<TPRequest> requestQueue;
    // Component logger
    private static final Logger logger = Logger.getLogger(Loggers.TP_COMP);
    private static final boolean debug = logger.isDebugEnabled();
    private static int CHANGES = 1;
    int changes = CHANGES;

    public TaskProcessor(String appHost, String tempDirPath) {
        TA = new TaskAnalyser();
        DIP = new DataInfoProvider(appHost, tempDirPath);

        requestQueue = new LinkedBlockingQueue<TPRequest>();

        keepGoing = true;
        processor = new Thread(this);
        processor.setName("Task Processor");
        processor.start();
    }

    public void setTD(TaskDispatcher TD) {
        this.TD = TD;
        TA.setCoWorkers(DIP, TD);
        DIP.setCoWorkers(TD);
    }

    public void cleanup() {
        keepGoing = false;
        processor.interrupt();
    }

    public void run() {
        while (keepGoing) {
            TPRequest request = null;
            try {
                request = requestQueue.take();
            } catch (InterruptedException e) {
                continue;
            }
            dispatchRequest(request);
        }
    }

    protected void dispatchRequest(TPRequest request) {
        switch (request.getRequestType()) {
            case ANALYSE_TASK:
                TA.processTask(((TaskAnalysisRequest) request).getTask());
                break;
            case UPDATE_GRAPH:
                GraphUpdateRequest gur = (GraphUpdateRequest) request;
                TA.updateGraph(gur.getTask(), gur.getImplementationId(), gur.getResource());
                break;
            case WAIT_FOR_TASK:
                TA.findWaitedTask((WaitForTaskRequest) request);
                break;
            case END_OF_APP:
                TA.noMoreTasks((EndOfAppRequest) request);
                break;
            case ALREADY_ACCESSED:
                AlreadyAccessedRequest aaRequest = (AlreadyAccessedRequest) request;
                boolean aa = DIP.alreadyAccessed(aaRequest.getFileName(), aaRequest.getPath(), aaRequest.getHost());
                aaRequest.setResponse(aa);
                aaRequest.getSemaphore().release();
                break;
            case REGISTER_DATA_ACCESS:
                RegisterDataAccessRequest rdaRequest = (RegisterDataAccessRequest) request;
                DataAccessId daId = DIP.registerDataAccess(rdaRequest.getAccess());
                rdaRequest.setResponse(daId);
                rdaRequest.getSemaphore().release();
                break;
            case NEW_VERSION_SAME_VALUE:
                NewVersionSameValueRequest nvsvRequest = (NewVersionSameValueRequest) request;
                DIP.newVersionSameValue(nvsvRequest.getrRenaming(), nvsvRequest.getwRenaming());
                break;
            case IS_OBJECT_HERE:
                IsObjectHereRequest iohRequest = (IsObjectHereRequest) request;
                boolean ih = DIP.isHere(iohRequest.getdId());
                iohRequest.setResponse(ih);
                iohRequest.getSemaphore().release();
                break;
            case SET_OBJECT_VERSION_VALUE:
                SetObjectVersionValueRequest sovvRequest = (SetObjectVersionValueRequest) request;
                DIP.setObjectVersionValue(sovvRequest.getRenaming(), sovvRequest.getValue());
                break;
            case GET_LAST_RENAMING:
                GetLastRenamingRequest glrRequest = (GetLastRenamingRequest) request;
                String renaming = DIP.getLastRenaming(glrRequest.getCode());
                glrRequest.setResponse(renaming);
                glrRequest.getSemaphore().release();
                break;
            case GET_LAST_DATA_ACCESS:
                GetLastDataAccessRequest gldaRequest = (GetLastDataAccessRequest) request;
                DataInstanceId dId = DIP.getLastDataAccess(gldaRequest.getCode());
                gldaRequest.setResponse(dId);
                gldaRequest.getSemaphore().release();
                break;
            case BLOCK_AND_GET_RESULT_FILES:
                BlockAndGetResultFilesRequest bagrfRequest = (BlockAndGetResultFilesRequest) request;
                TreeSet<Integer> writtenDataIds = TA.getAndRemoveWrittenFiles(bagrfRequest.getAppId());
                List<ResultFile> resFiles;
                if (writtenDataIds != null) {
                    List<DataInstanceId> versionIds = DIP.getLastVersions(writtenDataIds);
                    DIP.blockDataIds(writtenDataIds);
                    resFiles = new ArrayList<ResultFile>(writtenDataIds.size());
                    for (DataInstanceId dataId : versionIds) {
                        if (dataId != null) {
                            resFiles.add(new ResultFile(dataId,
                                    DIP.getOriginalName(dataId.getDataId()),
                                    DIP.getOriginalLocation(dataId.getDataId())));
                        }
                    }

                } else {
                    resFiles = new LinkedList<ResultFile>();
                }
                bagrfRequest.setResponse(resFiles);
                bagrfRequest.getSemaphore().release();
                break;
            case UNBLOCK_RESULT_FILES:
                resFiles = ((UnblockResultFilesRequest) request).getResultFiles();
                for (ResultFile resFile : resFiles) {
                    DIP.unblockDataId(resFile.getFileInstanceId().getDataId());
                }
                break;
            case SHUTDOWN:
                ShutdownRequest sdRequest = (ShutdownRequest) request;
                TA.shutdown(); // TP -> TD
                sdRequest.getSemaphore().release();
                break;
            case GRAPHSTATE:
                GraphDescriptionRequest gdrReq = (GraphDescriptionRequest) request;
                gdrReq.setResponse(TA.getGraphDOTFormat());
                gdrReq.getSemaphore().release();
                break;
            case TASKSTATE:
                TasksStateRequest ctsReq = (TasksStateRequest) request;
                ctsReq.setResponse(TA.getTaskStateRequest());
                ctsReq.getSemaphore().release();
                break;
            case DELETE_FILE:

                DeleteFileRequest dfReq = (DeleteFileRequest) request;
                FileInfo fileInfo = DIP.deleteData(dfReq.getHost(), dfReq.getPath(), dfReq.getFileName());

                if (fileInfo == null) { //file is not used by any task
                    java.io.File f = new java.io.File(dfReq.getPath() + dfReq.getFileName());
                    f.delete();
                } else { // file is involved in some task execution
                    if (!fileInfo.isToDelete()) {
                        // There are no more readers, therefore it can 
                        //be deleted (once it has been created)
                        TA.deleteFile(fileInfo);
                    } else {
                        // Nothing has to be done yet. It is already marked as a
                        //file to delete but it has to be read by some tasks
                    }
                }
                break;
            default:
                System.err.println("UNKONWN REQUEST TYPE " + request.getRequestType());
        }
    }

    // TaskCreation interface
    // App
    public int newTask(Long appId,
            String methodClass,
            String methodName,
            boolean priority,
            boolean hasTarget,
            Parameter[] parameters) {
        Task currentTask = new Task(appId, methodClass, methodName, priority, hasTarget, parameters);
        if (debug) {
            logger.debug("New method task(" + methodName + "), ID = " + currentTask.getId());
        }

        requestQueue.offer(new TaskAnalysisRequest(currentTask));
        return currentTask.getId();
    }

    // App
    public int newTask(Long appId,
            String namespace,
            String service,
            String port,
            String operation,
            boolean priority,
            boolean hasTarget,
            Parameter[] parameters) {
        Task currentTask = new Task(appId, namespace, service, port, operation, priority, hasTarget, parameters);

        if (debug) {
            logger.debug("New service task(" + operation + "), ID = " + currentTask.getId());
        }

        requestQueue.offer(new TaskAnalysisRequest(currentTask));
        return currentTask.getId();
    }

    // App
    public void noMoreTasks(Long appId) {
        Semaphore sem = new Semaphore(0);
        requestQueue.offer(new EndOfAppRequest(appId, sem));
        try {
            sem.acquire();
        } catch (InterruptedException e) {
        }
        if (debug) {
            logger.debug("All tasks finished");
        }
    }

    // TaskStatus interface
    // Notification thread (JM)
    public void notifyTaskEnd(Task task, int implId, String resource) {
        if (debug) {
            logger.debug("Notification received for task " + task.getId() + " with end status " + task.getStatus());
        }

        requestQueue.offer(new GraphUpdateRequest(task, implId, resource));
    }

    // App
    public void waitForTask(int dataId, AccessMode mode) {
        Semaphore sem = new Semaphore(0);
        requestQueue.offer(new WaitForTaskRequest(dataId, mode, sem));
        try {
            sem.acquire();
        } catch (InterruptedException e) {
        }
        if (debug) {
            logger.debug("End of waited task for data " + dataId);
        }
    }

    // App 
    public boolean alreadyAccessed(String fileName, String path, String host) {
        Semaphore sem = new Semaphore(0);
        AlreadyAccessedRequest request = new AlreadyAccessedRequest(fileName, path, host, sem);
        requestQueue.offer(request);
        try {
            sem.acquire();
        } catch (InterruptedException e) {
        }
        return request.getResponse();
    }

    // App
    public DataAccessId registerDataAccess(AccessParams access) {
        Semaphore sem = new Semaphore(0);
        RegisterDataAccessRequest request = new RegisterDataAccessRequest(access, sem);
        requestQueue.offer(request);
        try {
            sem.acquire();
        } catch (InterruptedException e) {
        }
        return request.getResponse();
    }

    // App
    public void newVersionSameValue(String rRenaming, String wRenaming) {
        NewVersionSameValueRequest request = new NewVersionSameValueRequest(rRenaming, wRenaming);
        requestQueue.offer(request);
    }

    // App
    public boolean isHere(DataInstanceId dId) {
        Semaphore sem = new Semaphore(0);
        IsObjectHereRequest request = new IsObjectHereRequest(dId, sem);
        requestQueue.offer(request);
        try {
            sem.acquire();
        } catch (InterruptedException e) {
        }
        return request.getResponse();
    }

    // App
    public void setObjectVersionValue(String renaming, Object value) {
        SetObjectVersionValueRequest request = new SetObjectVersionValueRequest(renaming, value);
        requestQueue.offer(request);
    }

    // App
    public String getLastRenaming(int code) {
        Semaphore sem = new Semaphore(0);
        GetLastRenamingRequest request = new GetLastRenamingRequest(code, sem);
        requestQueue.offer(request);

        try {
            sem.acquire();
        } catch (InterruptedException e) {
        }
        return request.getResponse();
    }

    // App
    public DataInstanceId getLastDataAccess(int code) {
        Semaphore sem = new Semaphore(0);
        GetLastDataAccessRequest request = new GetLastDataAccessRequest(code, sem);
        requestQueue.offer(request);

        try {
            sem.acquire();
        } catch (InterruptedException e) {
        }
        return request.getResponse();
    }

    // App
    public List<ResultFile> blockAndGetResultFiles(Long appId) {
        Semaphore sem = new Semaphore(0);
        BlockAndGetResultFilesRequest request = new BlockAndGetResultFilesRequest(appId, sem);
        requestQueue.offer(request);
        try {
            sem.acquire();
        } catch (InterruptedException e) {
        }
        return request.getResponse();
    }

    // App
    public void unblockResultFiles(List<ResultFile> resFiles) {
        UnblockResultFilesRequest request = new UnblockResultFilesRequest(resFiles);
        requestQueue.offer(request);
    }

    // App / Shutdown thread
    public void shutdown() {
        Semaphore sem = new Semaphore(0);
        requestQueue.offer(new ShutdownRequest(sem));
        try {
            sem.acquire();
        } catch (InterruptedException e) {
        }
    }

    /**
     * Returs a string with the description of the tasks in the graph
     *
     * @return description of the current tasks in the graph
     */
    public String getCurrentGraphState() {
        Semaphore sem = new Semaphore(0);
        GraphDescriptionRequest request = new GraphDescriptionRequest(sem);
        requestQueue.offer(request);
        try {
            sem.acquire();
        } catch (InterruptedException e) {
        }
        return (String) request.getResponse();
    }

    /**
     * Returs a string with the description of the tasks in the graph
     *
     * @return description of the current tasks in the graph
     */
    public String getCurrentTaskState() {
        Semaphore sem = new Semaphore(0);
        TasksStateRequest request = new TasksStateRequest(sem);
        requestQueue.offer(request);
        try {
            sem.acquire();
        } catch (InterruptedException e) {
        }
        return (String) request.getResponse();
    }

    public void markForDeletion(String name, String path, String host) {
        requestQueue.offer(new DeleteFileRequest(name, path, host));
    }
}
