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

package integratedtoolkit.types.request.ap;

import integratedtoolkit.comm.Comm;
import integratedtoolkit.components.impl.DataInfoProvider;
import integratedtoolkit.components.impl.TaskAnalyser;
import integratedtoolkit.components.impl.TaskDispatcher;
import integratedtoolkit.types.data.location.DataLocation;
import java.util.concurrent.Semaphore;

import integratedtoolkit.types.data.DataAccessId.RAccessId;
import integratedtoolkit.types.data.LogicalData;
import integratedtoolkit.types.data.operation.FileTransferable;
import integratedtoolkit.types.data.operation.OneOpWithSemListener;

/**
 * The TransferRawFileRequest class represents a request to transfer a file
 * located in a worker to be transferred to another location without register
 * the transfer
 */
public class TransferRawFileRequest extends APRequest {

    /**
     * Data Id and version of the requested file
     */
    private RAccessId faId;
    /**
     * Location where to leave the requested file
     */
    private DataLocation location;
    /**
     * Semaphore where to synchronize until the operation is done
     */
    private Semaphore sem;

    /**
     * Constructs a new TransferOpenFileRequest
     *
     * @param faId Data Id and version of the requested file
     * @param location Location where to leave the requested file
     * @param sem Semaphore where to synchronize until the operation is done
     */
    public TransferRawFileRequest(RAccessId faId, DataLocation location, Semaphore sem) {
        this.faId = faId;
        this.location = location;
        this.sem = sem;
    }

    /**
     * Returns the semaphore where to synchronize until the operation is done
     *
     * @return Semaphore where to synchronize until the operation is done
     */
    public Semaphore getSemaphore() {
        return sem;
    }

    /**
     * Sets the semaphore where to synchronize until the operation is done
     *
     * @param sem Semaphore where to synchronize until the operation is done
     */
    public void setSemaphore(Semaphore sem) {
        this.sem = sem;
    }

    /**
     * Returns the data Id and version of the requested file
     *
     * @return Data Id and version of the requested file
     */
    public RAccessId getFaId() {
        return faId;
    }

    /**
     * Sets the data Id and version of the requested file
     *
     * @param faId Data Id and version of the requested file
     */
    public void setFaId(RAccessId faId) {
        this.faId = faId;
    }

    /**
     * Returns the location where to leave the requested file
     *
     * @return the location where to leave the requested file
     */
    public DataLocation getLocation() {
        return location;
    }

    /**
     * Sets the location where to leave the requested file
     *
     * @param location Location where to leave the requested file
     */
    public void setLocation(DataLocation location) {
        this.location = location;
    }

    @Override
    public void process(TaskAnalyser ta, DataInfoProvider dip, TaskDispatcher td) {
        // Make a copy of the original logical file, we don't want to leave track
        String sourceName = faId.getReadDataInstance().getRenaming();
        Comm.appHost.getData(sourceName, location, (LogicalData) null, new FileTransferable(), new OneOpWithSemListener(sem));
    }

    @Override
    public APRequestType getRequestType() {
        return APRequestType.TRANSFER_RAW_FILE;
    }

}
