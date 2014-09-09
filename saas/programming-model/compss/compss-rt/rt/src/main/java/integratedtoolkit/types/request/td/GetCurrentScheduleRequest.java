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

package integratedtoolkit.types.request.td;

import integratedtoolkit.types.ScheduleState;
import java.util.concurrent.Semaphore;

/**
 * The DeleteIntermediateFilesRequest represents a request to delete the 
 * intermediate files of the execution from all the worker nodes of the resource
 * pool.
 */
public class GetCurrentScheduleRequest extends TDRequest {

    /** Current Schedule representation */
    private ScheduleState response;
    /** Semaphore to synchronize until the representation is constructed */
    private Semaphore sem;

    /** 
     * Constructs a GetCurrentScheduleRequest
     * 
     * @param sem Semaphore to synchronize until the representation is
     * constructed
     * 
     */
    public GetCurrentScheduleRequest(Semaphore sem) {
        super(TDRequestType.GET_STATE);
        this.sem = sem;
    }

    /** 
     * Stores the current schedule representation
     * 
     * @param current schedule representation
     * 
     */
    public void setResponse(ScheduleState response) {
        this.response = response;
    }

    /** 
     * Returns the current schedule representation
     * 
     * @result current schedule representation
     * 
     */
    public ScheduleState getResponse() {
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
}
