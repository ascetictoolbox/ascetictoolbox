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
package integratedtoolkit.types.request.td.debug;

import integratedtoolkit.types.data.DataInstanceId;
import java.util.TreeSet;
import java.util.concurrent.Semaphore;

public class GetLocationsRequest extends TDDebugRequest {

    private DataInstanceId daId;
    private Semaphore sem;
    private TreeSet<String> response;

    public GetLocationsRequest(DataInstanceId daId, Semaphore sem) {
        super(DebugRequestType.GET_LOCATIONS);
        this.daId = daId;
        this.sem = sem;
    }

    public void setDaId(DataInstanceId daId) {
        this.daId = daId;
    }

    public DataInstanceId getDaId() {
        return daId;
    }

    public void setSem(Semaphore sem) {
        this.sem = sem;
    }

    public Semaphore getSem() {
        return sem;
    }

    public void setResponse(TreeSet<String> response) {
        this.response = response;
    }

    public TreeSet<String> isResponse() {
        return response;
    }

}
