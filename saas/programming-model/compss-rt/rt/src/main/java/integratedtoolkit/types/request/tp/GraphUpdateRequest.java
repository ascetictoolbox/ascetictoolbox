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
package integratedtoolkit.types.request.tp;

import integratedtoolkit.types.Task;

public class GraphUpdateRequest extends TPRequest {

    private Task task;
    private int implementationId;
    private String resource;

    public GraphUpdateRequest(Task task, int implementationId, String resource) {
        super(TPRequestType.UPDATE_GRAPH);
        this.task = task;
        this.implementationId = implementationId;
        this.resource = resource;
    }

    public Task getTask() {
        return task;
    }

    public void setTask(Task task) {
        this.task = task;
    }

    public int getImplementationId() {
        return implementationId;
    }

    public void setImplementationId(int implementationId) {
        this.implementationId = implementationId;
    }

    public String getResource() {
        return resource;
    }

    public void setResource(String resource) {
        this.resource = resource;
    }

}
