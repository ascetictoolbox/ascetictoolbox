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

package integratedtoolkit.types.request.td;

import integratedtoolkit.components.impl.JobManager;
import integratedtoolkit.components.impl.TaskScheduler;
import integratedtoolkit.types.Task;
import integratedtoolkit.types.resources.Worker;

/**
 * The RefuseCloudWorkerRequest class represents the notification of an error
 * during a task execution that must be rescheduled in another resource.
 */
public class RescheduleTaskRequest extends TDRequest {

    /**
     * Task that must be rescheduled
     */
    private Task task;
    /**
     * Implementation that has been executed
     */
    private int implementationId;
    /**
     * Resource where the task has been submitted
     */
    private Worker resource;

    /**
     * Constructs a new RescheduleTaskRequest for the task task
     *
     * @param task Task that must be rescheduled
     * @param implId Implementation that has been executed
     * @param resource Resource where the task has been submitted
     */
    public RescheduleTaskRequest(Task task, int implId, Worker resource) {
        this.task = task;
        this.implementationId = implId;
        this.resource = resource;
    }

    /**
     * Returns the task that must be rescheduled
     *
     * @return Task that must be rescheduled
     */
    public Task getTask() {
        return task;
    }

    /**
     * Sets the task that must be rescheduled
     *
     * @param task Task that must be rescheduled
     */
    public void setTask(Task task) {
        this.task = task;
    }

    /**
     * Returns the id of implementation that has been executed
     *
     * @return Id of the implementation that has been executed
     */
    public int getImplementationId() {
        return implementationId;
    }

    /**
     * Sets the implementation that has been executed
     *
     * @param implId Id of the implementation that has been executed
     */
    public void setImplementationId(int implId) {
        this.implementationId = implId;
    }

    /**
     * Gets the resource where the task has been submitted
     *
     * @return name of the resource where the task has been submitted
     */
    public Worker getResource() {
        return resource;
    }

    /**
     * Sets the resource where the task has been submitted
     *
     * @param resource Resource where the task has been submitted
     */
    public void setResource(Worker resource) {
        this.resource = resource;
    }

    @Override
    public TDRequestType getRequestType() {
        return TDRequestType.RESCHEDULE_TASK;
    }

    @Override
    public void process(TaskScheduler ts, JobManager jm) throws ShutdownRequest.ShutdownException {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

}
