/**
 *
 *   Copyright 2013-2015 Barcelona Supercomputing Center (www.bsc.es) All rights reserved.
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

package integratedtoolkit.types.job;

import org.apache.log4j.Logger;

import integratedtoolkit.ITConstants;
import integratedtoolkit.log.Loggers;
import integratedtoolkit.types.COMPSsNode;
import integratedtoolkit.types.Implementation;
import integratedtoolkit.types.Task;
import integratedtoolkit.types.TaskParams;
import integratedtoolkit.types.resources.Resource;

public abstract class Job<T extends COMPSsNode> {

    // Job identifier management
    protected static final int FIRST_JOB_ID = 1;
    protected static int nextJobId = FIRST_JOB_ID;
    // Language
    protected static final String lang = System.getProperty(ITConstants.IT_LANG);
    // Tracing
    protected static final boolean tracing = System.getProperty(ITConstants.IT_TRACING) != null
            && System.getProperty(ITConstants.IT_TRACING).equals("true")
            ? true : false;

    // Job history
    public enum JobHistory {

        NEW,
        RESUBMITTED_FILES,
        RESUBMITTED,
        RESCHEDULED;
    }

    // Job kind
    public enum JobKind {

        METHOD,
        SERVICE;
    }

    // Information of the job
    protected int jobId;

    protected final Task task;
    protected final Implementation impl;
    protected final Resource worker;
    protected final JobListener listener;

    protected String eventId;

    protected JobHistory history;
    protected int transferId;
    protected static final Boolean workerDebug = Logger.getLogger(Loggers.WORKER).isDebugEnabled();

    protected static final Logger logger = Logger.getLogger(Loggers.COMM);
    protected static final boolean debug = logger.isDebugEnabled();

    public Job(Task task, Implementation impl, Resource res, JobListener listener) {
        jobId = nextJobId++;
        this.history = JobHistory.NEW;
        this.task = task;
        this.impl = impl;
        this.worker = res;
        this.listener = listener;
    }

    public int getJobId() {
        return jobId;
    }

    public TaskParams getCore() {
        return task.getTaskParams();
    }

    public Task getTask() {
        return task;
    }

    public JobHistory getHistory() {
        return history;
    }

    public void setHistory(JobHistory newHistoryState) {
        this.history = newHistoryState;
    }

    public Resource getResource() {
        return this.worker;
    }

    public T getResourceNode() {
        return (T) this.worker.getNode();
    }

    public JobListener getListener() {
        return listener;
    }

    public Implementation getImplementation() {
        return this.impl;
    }

    public void setTransferGroupId(int transferId) {
        this.transferId = transferId;
    }

    public int getTransferGroupId() {
        return this.transferId;
    }

    public abstract String toString();

    public abstract void submit() throws Exception;

    public abstract void stop() throws Exception;

    public Object getReturnValue() {
        return null;
    }

    public abstract String getHostName();

    public abstract JobKind getKind();

    public static interface JobListener {

        enum JobEndStatus {

            OK,
            TO_RESCHEDULE,
            TRANSFERS_FAILED,
            SUBMISSION_FAILED,
            EXECUTION_FAILED;
        }

        void jobCompleted(Job job);

        void jobFailed(Job job, JobEndStatus endStatus);

    }

    public void setEventId(String eventId) {
        this.eventId = eventId;
    }

    public String getEventId() {
        return eventId;
    }

}
