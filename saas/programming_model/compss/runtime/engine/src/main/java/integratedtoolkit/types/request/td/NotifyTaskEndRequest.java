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
 * The NotifyTaskEndRequest class represents the notification of the end of a
 * task execution
 */
public class NotifyTaskEndRequest extends TDRequest {

    /**
     * The ended task
     */
    private Task task;

    /**
     * Resource where the task has been submitted
     */
    private Worker worker;

    /**
     * The executed Implementation
     */
    private int implementationId;

    /**
     * Constructs a new NotifyTaskEndRequest for the task
     *
     * @param task Task that has ended
     * @param implementationId id of the implementation that was ran
     * @param worker Worker where the task ran
     *
     */
    public NotifyTaskEndRequest(Task task, int implementationId, Worker worker) {
        this.task = task;
        this.implementationId = implementationId;
        this.worker = worker;
    }

    /**
     * Returns the task that has ended
     *
     * @return Task that has ended
     */
    public Task getTask() {
        return task;
    }

    /**
     * Set the task that has ended
     *
     * @param task Task that has ended
     */
    public void setTask(Task task) {
        this.task = task;
    }

    /**
     * Set the ran implementation
     *
     * @param impl Implementation that has executed
     */
    public void setImplementationId(int impl) {
        this.implementationId = impl;
    }

    /**
     * Gets the implementation that has been executed
     *
     * @return id of the implementation that has been executed
     */
    public int getImplementationId() {
        return implementationId;
    }

    /**
     * Gets the resource where the task was submmitted
     *
     * @return the name of the resource where the task was submitted
     */
    public Worker getWorker() {
        return worker;
    }

    /**
     * Sets the resource where the task was submitted
     *
     * @param worker resource where the task was submitted
     */
    public void setWorker(Worker worker) {
        this.worker = worker;
    }

    @Override
    public TDRequestType getRequestType() {
        return TDRequestType.FINISHED_TASK;
    }

    @Override
    public void process(TaskScheduler ts, JobManager jm) throws ShutdownRequest.ShutdownException {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

}
