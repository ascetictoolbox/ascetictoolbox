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

package integratedtoolkit.components.impl.debug;

import integratedtoolkit.components.impl.TaskDispatcher;
import integratedtoolkit.types.request.td.TDRequest;
import integratedtoolkit.types.request.td.debug.TDDebugRequest;

public class TaskDispatcherDebug extends TaskDispatcher {

    AccessProcessorDebug accessProcessor;

    public void setTP(AccessProcessorDebug ap) {
        this.accessProcessor = ap;
        scheduler.setCoWorkers(jobManager);
        jobManager.setCoWorkers(ap, this);

    }

    @Override
    protected void dispatchRequest(TDRequest request) throws Exception {
        if (request.getRequestType() == TDRequest.TDRequestType.DEBUG) {
            TDDebugRequest drequest = (TDDebugRequest) request;
            switch (drequest.getDebugRequestType()) {
                default:
            }
        } else {
            super.dispatchRequest(request);
        }
    }

}
