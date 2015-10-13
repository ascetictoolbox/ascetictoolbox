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

import integratedtoolkit.components.impl.DataInfoProvider;
import integratedtoolkit.components.impl.TaskAnalyser;
import integratedtoolkit.components.impl.TaskDispatcher;
import java.util.concurrent.Semaphore;

import integratedtoolkit.types.data.DataAccessId;

/**
 * The TransferObjectRequest is a request for an object contained in a remote
 * worker
 */
public class TransferObjectRequest extends APRequest {

    /**
     * Data Access Id
     */
    private DataAccessId daId;
    /**
     * Semaphore where to synchronize until the operation is done
     */
    private Semaphore sem;
    /**
     * Object asked for
     */
    private Object response;

    /**
     * Constructs a new TransferObjectRequest
     *
     * @param daId Object required, data id + version
     * @param sem Semaphore where to synchronize until the operation is done
     */
    public TransferObjectRequest(DataAccessId daId, Semaphore sem) {
        this.daId = daId;
        this.sem = sem;
    }

    /**
     * Returns the data id + version of the required object
     *
     * @return data id + version of the required object
     */
    public DataAccessId getDaId() {
        return daId;
    }

    /**
     * Sets the requested data id and version
     *
     * @param daId data id + version of the required object
     */
    public void setDaId(DataAccessId daId) {
        this.daId = daId;
    }

    /**
     * Returns the semaphore where to synchronize until the object can be read
     *
     * @return the semaphore where to synchronize until the object can be read
     */
    public Semaphore getSemaphore() {
        return sem;
    }

    /**
     * Sets the semaphore where to synchronize until the requested object can be
     * read
     *
     * @param sem the semaphore where to synchronize until the requested object
     * can be read
     */
    public void setSemaphore(Semaphore sem) {
        this.sem = sem;
    }

    /**
     * Returns the requested object (Null if it was on a file).
     *
     * @return the requested object (Null if it was on a file).
     */
    public Object getResponse() {
        return response;
    }

    /**
     * Sets the requested object.
     *
     * @param response The requested object.
     */
    public void setResponse(Object response) {
        this.response = response;
    }

    @Override
    public void process(TaskAnalyser ta, DataInfoProvider dip, TaskDispatcher td) {
        dip.transferObjectValue(this);
    }

    @Override
    public APRequestType getRequestType() {
        return APRequestType.TRANSFER_OBJECT;
    }
}
