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
package integratedtoolkit.types.request.tp.debug;

import java.util.concurrent.Semaphore;

public class TaskEndRequest extends TPDebugRequest {

    private int taskId;
    private Semaphore sem;
    private boolean response;

    public TaskEndRequest(int taskId, Semaphore sem) {
        super(DebugRequestType.TASK_END);
        this.taskId = taskId;
        this.sem = sem;
    }

    public void setTaskId(int taskId) {
        this.taskId = taskId;
    }

    public int getTaskId() {
        return taskId;
    }

    public void setSem(Semaphore sem) {
        this.sem = sem;
    }

    public Semaphore getSem() {
        return sem;
    }

    public void setResponse(boolean response) {
        this.response = response;
    }

    public boolean isResponse() {
        return response;
    }

}
