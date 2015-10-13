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

package integratedtoolkit.types.request.td;

import integratedtoolkit.components.impl.JobManager;
import integratedtoolkit.components.impl.TaskScheduler;

/**
 * The NewWaitingTaskRequest represents a notification about a task that will be
 * dependency-free when some of the task already submitted to the TaskDispathcer
 * end.
 */
public class NewWaitingTaskRequest extends TDRequest {

    /**
     * blocked task method id
     */
    private int coreId;

    /**
     * Contructs a NewWaitingTaskRequest
     *
     * @param coreId core id of the blocked task
     */
    public NewWaitingTaskRequest(int coreId) {
        this.coreId = coreId;
    }

    /**
     * Returns the core Id of the blocked task
     *
     * @return core Id of the blocked task
     */
    public int getMethodId() {
        return coreId;
    }

    /**
     * Sets the core Id of the blocked task
     *
     * @param coreId core Id of the blocked task
     */
    public void setMethodId(int coreId) {
        this.coreId = coreId;
    }

    @Override
    public TDRequestType getRequestType() {
        return TDRequestType.NEW_WAITING_TASK;
    }

    @Override
    public void process(TaskScheduler ts, JobManager jm) throws ShutdownRequest.ShutdownException {
        
    }
}
