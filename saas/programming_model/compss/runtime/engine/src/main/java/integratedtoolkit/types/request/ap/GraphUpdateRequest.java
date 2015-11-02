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
import integratedtoolkit.types.Task;
import integratedtoolkit.types.resources.Worker;

public class GraphUpdateRequest extends APRequest {

    private Task task;
    private int implementationId;
    private Worker resource;

    public GraphUpdateRequest(Task task, int implementationId, Worker resource) {
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

    public Worker getResource() {
        return resource;
    }

    public void setResource(Worker resource) {
        this.resource = resource;
    }

    @Override
    public void process(TaskAnalyser ta, DataInfoProvider dip, TaskDispatcher td) {
        ta.updateGraph(task, implementationId, resource);
    }

    @Override
    public APRequestType getRequestType() {
        return APRequestType.UPDATE_GRAPH;
    }

}
