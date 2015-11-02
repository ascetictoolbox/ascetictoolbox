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

import integratedtoolkit.components.ResourceUser.WorkloadStatus;
import integratedtoolkit.components.impl.JobManager;
import integratedtoolkit.components.impl.TaskScheduler;
import integratedtoolkit.util.CoreManager;
import java.util.concurrent.Semaphore;

/**
 * The DeleteIntermediateFilesRequest represents a request to delete the
 * intermediate files of the execution from all the worker nodes of the resource
 * pool.
 */
public class GetCurrentScheduleRequest extends TDRequest {

    /**
     * Current Schedule representation
     */
    private WorkloadStatus response;
    /**
     * Semaphore to synchronize until the representation is constructed
     */
    private Semaphore sem;

    /**
     * Constructs a GetCurrentScheduleRequest
     *
     * @param sem Semaphore to synchronize until the representation is
     * constructed
     *
     */
    public GetCurrentScheduleRequest(Semaphore sem) {
        this.sem = sem;
    }

    /**
     * Returns the current schedule representation
     *
     * @result current schedule representation
     *
     */
    public WorkloadStatus getResponse() {
        return response;
    }

    /**
     * Returns the semaphore to synchronize until the representation is
     * constructed
     *
     * @result Semaphore to synchronize until the representation is constructed
     *
     */
    public Semaphore getSemaphore() {
        return sem;
    }

    /**
     * Changes the semaphore to synchronize until the representation is
     * constructed
     *
     * @param sem New semaphore to synchronize until the representation is
     * constructed
     *
     */
    public void setSemaphore(Semaphore sem) {
        this.sem = sem;
    }

    @Override
    public TDRequestType getRequestType() {
        return TDRequestType.GET_STATE;
    }

    @Override
    public void process(TaskScheduler ts, JobManager jm) throws ShutdownRequest.ShutdownException {
        response = new WorkloadStatus(CoreManager.getCoreCount());
        ts.getWorkloadState(response);
        sem.release();
    }
}
