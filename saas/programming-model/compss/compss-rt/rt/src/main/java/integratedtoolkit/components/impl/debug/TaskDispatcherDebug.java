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
package integratedtoolkit.components.impl.debug;

import integratedtoolkit.components.impl.TaskDispatcher;
import integratedtoolkit.connectors.utils.CreationThread;
import integratedtoolkit.connectors.utils.DeletionThread;
import integratedtoolkit.types.data.DataInstanceId;
import integratedtoolkit.types.request.td.TDRequest;
import integratedtoolkit.types.request.td.debug.GetLocationsRequest;
import integratedtoolkit.types.request.td.debug.TDDebugRequest;
import integratedtoolkit.util.ResourceManager;
import java.util.TreeSet;
import java.util.concurrent.Semaphore;

public class TaskDispatcherDebug extends TaskDispatcher {

    TaskProcessorDebug TP;

    public void setTP(TaskProcessorDebug TP) {
        this.TP = TP;
        CreationThread.setTaskDispatcher(this);
        DeletionThread.setTaskDispatcher(this);
        TS.setCoWorkers(JM, FTM);
        JM.setCoWorkers(TP, this, FTM);
        FTM.setCoWorkers(this, JM);
        SO.setCoWorkers(this);
        SO.start();
        if (ResourceManager.useCloud()) {
            //Creates the VM needed for execute the methods
            int alreadyCreated = ResourceManager.addBasicNodes();
            monitor.info(alreadyCreated + " resources have been requested to the Cloud in order to fulfill all core constraints");
            //Distributes the rest of the VM
            ResourceManager.addExtraNodes(alreadyCreated);
        }
    }

    protected void dispatchRequest(TDRequest request) {
        if (request.getRequestType() == TDRequest.TDRequestType.DEBUG) {
            TDDebugRequest drequest = (TDDebugRequest) request;
            switch (drequest.getDebugRequestType()) {
                case GET_LOCATIONS:
                    GetLocationsRequest glr = (GetLocationsRequest) drequest;
                    glr.setResponse(FTM.getLocations(glr.getDaId()));
                    glr.getSem().release();
                    break;
                default:
            }
        } else {
            super.dispatchRequest(request);
        }
    }

    public TreeSet<String> getLocations(DataInstanceId daId) {
        Semaphore sem = new Semaphore(0);
        GetLocationsRequest req = new GetLocationsRequest(daId, sem);
        requestQueue.offer(req);
        try {
            sem.acquire();
        } catch (InterruptedException e) {
        }
        return req.isResponse();
    }
}
