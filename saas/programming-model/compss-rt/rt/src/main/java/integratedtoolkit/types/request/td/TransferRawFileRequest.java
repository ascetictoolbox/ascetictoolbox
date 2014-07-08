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

import integratedtoolkit.types.data.DataAccessId;
import integratedtoolkit.types.data.Location;
import integratedtoolkit.types.request.td.TDRequest.TDRequestType;

/**
 * The TransferRawFileRequest class represents a request to transfer a file 
 * located in a worker to be transferred to another location without register 
 * the transfer
 */
public class TransferRawFileRequest extends TDRequest {

    /** Data Id and version of the requested file */
    private DataAccessId faId;
    /** Location where to leave the requested file */
    private Location location;
    /** Semaphore where to synchronize until the operation is done*/
    private Semaphore sem;

    /**
     * Constructs a new TransferOpenFileRequest
     * @param faId Data Id and version of the requested file
     * @param location Location where to leave the requested file
     * @param sem Semaphore where to synchronize until the operation is done
     */
    public TransferRawFileRequest(DataAccessId faId, Location location, Semaphore sem) {
        super(TDRequestType.TRANSFER_RAW_FILE);
        this.faId = faId;
        this.location = location;
        this.sem = sem;
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

    /**
     * Returns the data Id and version of the requested file
     * @return Data Id and version of the requested file
     */
    public DataAccessId getFaId() {
        return faId;
    }

    /**
     * Sets the data Id and version of the requested file
     * @param faId Data Id and version of the requested file
     */
    public void setFaId(DataAccessId faId) {
        this.faId = faId;
    }

    /**
     * Returns the location where to leave the requested file 
     * @return the location where to leave the requested file 
     */
    public Location getLocation() {
        return location;
    }

    /**
     * Sets the location where to leave the requested file 
     * @param location Location where to leave the requested file 
     */
    public void setLocation(Location location) {
        this.location = location;
    }
}
