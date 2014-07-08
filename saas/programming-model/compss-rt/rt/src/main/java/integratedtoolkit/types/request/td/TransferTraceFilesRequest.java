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

import java.util.concurrent.Semaphore;

import integratedtoolkit.types.data.Location;

/** 
 * The TransferResultFilesRequest class represents a request to send a set of 
 * files resultants from the execution located in the workers to the master
 */
public class TransferTraceFilesRequest extends TDRequest {

    /** Location where to transfer trace files */
    private Location loc;
    /** Semaphore where to synchronize until the operation is done*/
    private Semaphore sem;

    /**
     * Constructs a new TransferResultFilesRequest
     * @param loc Location where to transfer trace files 
     * @param sem Semaphore where to synchronize until the operation is done
     */
    public TransferTraceFilesRequest(Location loc, Semaphore sem) {
        super(TDRequestType.TRANSFER_TRACE_FILES);
        this.loc = loc;
        this.sem = sem;
    }

    /**
     * Returns the location where to transfer trace files 
     * @return The location where to transfer trace files 
     */
    public Location getLocation() {
        return loc;
    }

    /**
     * Sets the location where to transfer trace files 
     * @param loc Location where to transfer trace files 
     */
    public void setLocation(Location loc) {
        this.loc = loc;
    }

    /**
     * Returns the semaphore where to synchronize until the operation is done
     * @return Semaphore where to synchronize until the operation is done
     */
    public Semaphore getSemaphore() {
        return sem;
    }

    /**
     * Sets the semaphore where to synchronize until the operation is done
     * @param sem Semaphore where to synchronize until the operation is done
     */
    public void setSemaphore(Semaphore sem) {
        this.sem = sem;
    }
}
