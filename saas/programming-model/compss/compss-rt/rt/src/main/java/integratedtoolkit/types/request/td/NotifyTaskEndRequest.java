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

import integratedtoolkit.types.Task;

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
    private String resourceName;

    /**
     * The executed Implementation
     */
    private int implementationId;

    /**
     * Constructs a new NotifyTaskEndRequest for the task
     *
     * @param task Task that has ended
     *
     */
    public NotifyTaskEndRequest(Task task, int implementationId, String resource) {
        super(TDRequestType.FINISHED_TASK);
        this.task = task;
        this.implementationId = implementationId;
        this.resourceName = resource;
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
    public String getResourceName() {
        return resourceName;
    }

    /**
     * Sets the resource where the task was submitted
     *
     * @param resourceName resource where the task was submitted
     */
    public void setResourceName(String resourceName) {
        this.resourceName = resourceName;
    }

}
