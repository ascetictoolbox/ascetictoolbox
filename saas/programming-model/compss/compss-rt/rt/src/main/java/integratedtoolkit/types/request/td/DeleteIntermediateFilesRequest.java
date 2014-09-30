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

package integratedtoolkit.types.request.td;

import java.util.concurrent.Semaphore;

/**
 * The DeleteIntermediateFilesRequest represents a request to delete the 
 * intermediate files of the execution from all the worker nodes of the resource
 * pool.
 */
public class DeleteIntermediateFilesRequest extends TDRequest {

    /** Semaphore to synchronize until the files have been deleted */
    private Semaphore sem;

    /** 
     * Constructs a DeleteIntermediateFilesRequest
     * 
     * @param sem Semaphore where to synchronize until the files have been
     * deleted
     * 
     */
    public DeleteIntermediateFilesRequest(Semaphore sem) {
        super(TDRequestType.DELETE_INTERMEDIATE_FILES);
        this.sem = sem;
    }

    /** 
     * Returns the semaphore where to synchronize until the files have been
     * deleted
     * 
     * @result Semaphore to synchronize until the files have been deleted
     * 
     */
    public Semaphore getSemaphore() {
        return sem;
    }

    /** 
     * Changes the semaphore where to synchronize until the files have been
     * deleted
     * 
     * @param sem New semaphore to synchronize until the files have been deleted
     * 
     */
    public void setSemaphore(Semaphore sem) {
        this.sem = sem;
    }
}
