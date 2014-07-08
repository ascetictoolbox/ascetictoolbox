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

/**
 * The TransferObjectRequest is a request for an object contained in a remote 
 * worker
 */
public class TransferObjectRequest extends TDRequest {

    /** Data Access Id*/
    private DataAccessId daId;
    /** Path of the file where to leave the object if its in a file*/
    private String path;
    /** Host which contains the object*/
    private String host;
    /** Semaphore where to synchronize until the operation is done*/
    private Semaphore sem;
    /** Object asked for*/
    private Object response;

    /**
     * Constructs a new TransferObjectRequest
     * @param daId Object required, data id + version
     * @param path Path of the file where the object will be left
     * @param host Host where to leave the object
     * @param sem Semaphore where to synchronize until the operation is done
     */
    public TransferObjectRequest(DataAccessId daId, String path, String host,
            Semaphore sem) {
        super(TDRequestType.TRANSFER_OBJECT);
        this.daId = daId;
        this.path = path;
        this.host = host;
        this.sem = sem;
    }

    /**
     * Returns the data id + version of the required object
     * @return data id + version of the required object
     */
    public DataAccessId getDaId() {
        return daId;
    }

    /**
     * Sets the requested data id and version 
     * @param daId data id + version of the required object
     */
    public void setDaId(DataAccessId daId) {
        this.daId = daId;
    }

    /**
     * Returns the path where to leave the required object
     * @return The path where to leave the required object
     */
    public String getPath() {
        return path;
    }

    /**
     * Sets the path where to leave the required object
     * @param path Path where to leave the required object
     */
    public void setPath(String path) {
        this.path = path;
    }

    /**
     * Returns the name of the host where the object will be left
     * @return the name of the host where the object will be left
     */
    public String getHost() {
        return host;
    }

    /**
     * Sets the name of the host where to leave the object
     * @param host Name of the host where to leave the object
     */
    public void setHost(String host) {
        this.host = host;
    }

    /**
     * Returns the semaphore where to synchronize until the object can be read
     * @return the semaphore where to synchronize until the object can be read
     */
    public Semaphore getSemaphore() {
        return sem;
    }

    /**
     * Sets the semaphore where to synchronize until the requested object can be
     * read
     * @param sem the semaphore where to synchronize until the requested object
     * can be read
     */
    public void setSemaphore(Semaphore sem) {
        this.sem = sem;
    }

    /**
     * Returns the requested object (Null if it was on a file).
     * @return the requested object (Null if it was on a file).
     */
    public Object getResponse() {
        return response;
    }

    /**
     * Sets the requested object.
     * @param response The requested object.
     */
    public void setResponse(Object response) {
        this.response = response;
    }
}
