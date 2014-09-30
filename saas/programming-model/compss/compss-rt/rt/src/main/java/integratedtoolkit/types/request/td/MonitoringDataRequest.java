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
 * The MonitoringDataRequest class represents a request to obtain the current
 * resources and cores that can be run
 */
public class MonitoringDataRequest extends TDRequest {

    /** Semaphore where to synchronize until the operation is done*/
    private Semaphore sem;
    /** Applications progress description*/
    private String response;

    /**
     * Constructs a new TaskStateRequest
     * @param sem semaphore where to synchronize until the current state is 
     * described
     */
    public MonitoringDataRequest(Semaphore sem) {
        super(TDRequestType.MONITOR_DATA);
        this.sem = sem;
    }

    /**
     * Returns the semaphore where to synchronize until the current state is
     * described
     * @return the semaphore where to synchronize until the current state is 
     * described
     */
    public Semaphore getSemaphore() {
        return sem;
    }

    /**
     * Sets the semaphore where to synchronize until the current state is 
     * described
     * @param sem the semaphore where to synchronize until the current state is 
     * described
     */
    public void setSemaphore(Semaphore sem) {
        this.sem = sem;
    }

    /**
     * Returns the progress description in an xml format string
     * @return progress description in an xml format string
     */
    public String getResponse() {
        return response;
    }

    /**
     * Sets the current state description 
     * @param response current state description
     */
    public void setResponse(String response) {
        this.response = response;
    }
}
