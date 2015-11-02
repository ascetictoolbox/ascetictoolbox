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
import integratedtoolkit.types.data.DataInstanceId;

import java.util.concurrent.Semaphore;

public class IsObjectHereRequest extends APRequest {

    private int code;
    private Semaphore sem;

    private boolean response;

    public IsObjectHereRequest(int code, Semaphore sem) {
        this.code = code;
        this.sem = sem;
    }

    public int getdCode() {
        return code;
    }

    public void setCode(int code) {
        this.code = code;
    }

    public Semaphore getSemaphore() {
        return sem;
    }

    public void setSemaphore(Semaphore sem) {
        this.sem = sem;
    }

    public boolean getResponse() {
        return response;
    }

    public void setResponse(boolean response) {
        this.response = response;
    }

    @Override
    public void process(TaskAnalyser ta, DataInfoProvider dip, TaskDispatcher td) {
        DataInstanceId dId = dip.getLastDataAccess(code);
        response = dip.isHere(dId);
        sem.release();
    }

    @Override
    public APRequestType getRequestType() {
        return APRequestType.IS_OBJECT_HERE;
    }

}
