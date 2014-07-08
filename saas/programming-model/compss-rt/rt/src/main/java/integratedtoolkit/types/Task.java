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
package integratedtoolkit.types;

import integratedtoolkit.types.data.DataInstanceId;
import java.util.concurrent.atomic.AtomicInteger;

public class Task implements Comparable<Task> {

    // Task states
    public enum TaskState {

        TO_ANALYSE,
        TO_SCHEDULE,
        TO_RESCHEDULE,
        TO_EXECUTE,
        FINISHED,
        FAILED
    }
    // Task fields
    private long appId;
    private int taskId;
    private TaskState status;
    private TaskParams taskParams;
    private String lastResource;
    
    // Scheduling info
    private boolean enforcedSceduling;
    private boolean strongEnforcedScheduling;
    private DataInstanceId enforcingData;
    // Execution info
    private long initialTimeStamp;
    // Task ID management
    private static final int FIRST_TASK_ID = 1;
    private static AtomicInteger nextTaskId = new AtomicInteger(FIRST_TASK_ID);

    public Task(Long appId, String methodClass, String methodName, boolean priority, boolean hasTarget, Parameter[] parameters) {
        this.appId = appId;
        this.taskId = nextTaskId.getAndIncrement();//nextTaskId++;
        this.status = TaskState.TO_ANALYSE;
        this.taskParams = new TaskParams(methodClass, methodName, priority, hasTarget, parameters);
    }

    public Task(Long appId, String namespace, String service, String port, String operation, boolean priority, boolean hasTarget, Parameter[] parameters) {
        this.appId = appId;
        this.taskId = nextTaskId.getAndIncrement();//nextTaskId++;
        this.status = TaskState.TO_ANALYSE;
        this.taskParams = new TaskParams(namespace, service, port, operation, priority, hasTarget, parameters);
    }

    public long getAppId() {
        return appId;
    }

    public int getId() {
        return taskId;
    }

    public TaskState getStatus() {
        return status;
    }

    public void setStatus(TaskState status) {
        this.status = status;
    }

    public void setInitialTimeStamp(long time) {
        this.initialTimeStamp = time;
    }

    public void forceScheduling() {
        this.enforcedSceduling = true;
        this.strongEnforcedScheduling = false;
    }

    public void forceStrongScheduling() {
        this.enforcedSceduling = true;
        this.strongEnforcedScheduling = true;
    }

    public void unforceScheduling() {
        this.enforcedSceduling = false;
        this.strongEnforcedScheduling = false;
    }

    public void setEnforcingData(DataInstanceId dataId) {
        this.enforcingData = dataId;
    }

    public TaskParams getTaskParams() {
        return taskParams;
    }

    public boolean isSchedulingForced() {
        return this.enforcedSceduling;
    }

    public boolean isSchedulingStrongForced() {
        return this.strongEnforcedScheduling;
    }

    public DataInstanceId getEnforcingData() {
        return this.enforcingData;
    }

    public long getInitialTimeStamp() {
        return initialTimeStamp;
    }

    public String toString() {
        StringBuilder buffer = new StringBuilder();

        buffer.append("[[Task id: ").append(getId()).append("]");
        buffer.append(", [Status: ").append(getStatus()).append("]");
        buffer.append(", ").append(getTaskParams().toString()).append("]");

        return buffer.toString();
    }

    // Comparable interface implementation
    public int compareTo(Task task) throws NullPointerException {
        if (task == null) {
            throw new NullPointerException();
        }

        return this.getId() - task.getId();
    }

    public String getLastResource() {
        return lastResource;
    }

    public void setLastResource(String lastResource) {
        this.lastResource = lastResource;
    }

    
}
