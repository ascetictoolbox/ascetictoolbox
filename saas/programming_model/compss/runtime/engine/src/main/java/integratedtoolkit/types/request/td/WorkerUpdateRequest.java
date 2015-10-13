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
import integratedtoolkit.types.resources.Worker;

/**
 * The AddCloudNodeRequest represents a request to add a new resource ready to
 * execute to the resource pool
 */
public class WorkerUpdateRequest extends TDRequest {

    public final Worker worker;

    /**
     * Constructs a AddCloudNodeRequest with all its parameters
     *
     * @param worker Worker that has been added
     *
     */
    public WorkerUpdateRequest(Worker worker) {
        this.worker = worker;
    }

    public Worker getWorker() {
        return worker;
    }

    @Override
    public TDRequestType getRequestType() {
        return TDRequestType.UPDATED_WORKER_POOL;
    }

    @Override
    public void process(TaskScheduler ts, JobManager jm) {
        ts.resourcesCreated(worker);
    }

}
