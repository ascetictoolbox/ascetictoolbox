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
import integratedtoolkit.types.data.AccessParams;
import java.util.concurrent.Semaphore;

import integratedtoolkit.types.data.DataAccessId;

public class RegisterDataAccessRequest extends APRequest {

    private AccessParams access;
    private Semaphore sem;
    private DataAccessId response;

    public RegisterDataAccessRequest(AccessParams access, Semaphore sem) {
        this.access = access;
        this.sem = sem;
    }

    public AccessParams getAccess() {
        return access;
    }

    public void setAccess(AccessParams access) {
        this.access = access;
    }

    public Semaphore getSemaphore() {
        return sem;
    }

    public void setSemaphore(Semaphore sem) {
        this.sem = sem;
    }

    public DataAccessId getResponse() {
        return response;
    }

    public void setResponse(DataAccessId response) {
        this.response = response;
    }

    @Override
    public void process(TaskAnalyser ta, DataInfoProvider dip, TaskDispatcher td) {
        DataAccessId daId = dip.registerDataAccess(this.access);
        this.response = daId;
        sem.release();
    }

    @Override
    public APRequestType getRequestType() {
        return APRequestType.REGISTER_DATA_ACCESS;
    }
}
