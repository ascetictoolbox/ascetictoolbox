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
package integratedtoolkit.components.impl.debug;

import integratedtoolkit.components.impl.TaskProcessor;
import integratedtoolkit.types.Task;
import integratedtoolkit.types.data.DataInstanceId;
import integratedtoolkit.types.data.FileInfo;
import integratedtoolkit.types.request.tp.TPRequest;
import integratedtoolkit.types.request.tp.debug.GetInstancesRequest;
import integratedtoolkit.types.request.tp.debug.GetLocationsRequest;
import integratedtoolkit.types.request.tp.debug.TPDebugRequest;
import integratedtoolkit.types.request.tp.debug.TPDebugRequest.DebugRequestType;
import integratedtoolkit.types.request.tp.debug.TaskEndRequest;
import java.util.LinkedList;
import java.util.TreeSet;
import java.util.concurrent.Semaphore;

public class TaskProcessorDebug extends TaskProcessor {

    TaskDispatcherDebug TD;

    public TaskProcessorDebug(String appHost, String tempDirPath) {
        super(appHost, tempDirPath);
    }

    public void setTD(TaskDispatcherDebug TD) {
        this.TD = TD;
        TA.setCoWorkers(DIP, TD);
        DIP.setCoWorkers(TD);
    }

    protected void dispatchRequest(TPRequest request) {
        if (request.getRequestType() == TPRequest.TPRequestType.DEBUG) {
            TPDebugRequest drequest = (TPDebugRequest) request;

            switch (drequest.getDebugRequestType()) {
                case TASK_END:
                    TaskEndRequest ter = (TaskEndRequest) drequest;
                    Task t = TA.getTask(ter.getTaskId());
                    ter.setResponse(t == null);
                    ter.getSem().release();
                    break;
                case GET_INSTANCES:
                    GetInstancesRequest grr = (GetInstancesRequest) drequest;
                    LinkedList<DataInstanceId> renamings = DIP.getInstances(grr.getHost(), grr.getPath(), grr.getFileName());
                    grr.setResponse(renamings);
                    grr.getSem().release();
                    break;
                case GET_LOCATIONS:
                    TA.forceObsoleteRemoval();
                    GetLocationsRequest glr = (GetLocationsRequest) drequest;
                    glr.setResponse(TD.getLocations(glr.getDaId()));
                    glr.getSem().release();
                    break;
                default:
            }
        } else {
            super.dispatchRequest(request);
        }
    }

    public boolean isTaskEnded(int taskId) {
        Semaphore sem = new Semaphore(0);
        TaskEndRequest req = new TaskEndRequest(taskId, sem);
        requestQueue.offer(req);
        try {
            sem.acquire();
        } catch (InterruptedException e) {
        }
        return req.isResponse();
    }

    public LinkedList<DataInstanceId> getFileInstances(String host, String path, String fileName) {
        Semaphore sem = new Semaphore(0);
        GetInstancesRequest req = new GetInstancesRequest(host, path, fileName, sem);
        requestQueue.offer(req);
        try {
            sem.acquire();
        } catch (InterruptedException e) {
        }
        return req.isResponse();
    }

    public TreeSet<String> getFileLocations(DataInstanceId daId) {
        Semaphore sem = new Semaphore(0);
        GetLocationsRequest req = new GetLocationsRequest(daId, sem);
        requestQueue.offer(req);
        try {
            sem.acquire();
        } catch (InterruptedException e) {
        }
        return req.getResponse();
    }
}
