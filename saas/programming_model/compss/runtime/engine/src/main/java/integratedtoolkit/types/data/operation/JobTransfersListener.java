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

package integratedtoolkit.types.data.operation;

import integratedtoolkit.types.job.Job;
import integratedtoolkit.components.impl.JobManager;
import integratedtoolkit.log.Loggers;
import integratedtoolkit.types.resources.Worker;
import org.apache.log4j.Logger;

public class JobTransfersListener extends DataOperation.EventListener {

    private int operation = 0;
    private int errors = 0;
    private boolean enabled = false;

    private static final Logger logger = Logger.getLogger(Loggers.FTM_COMP);
    private static final boolean debug = logger.isDebugEnabled();

    private Job job;
    private Worker res;
    private JobManager jm;

    public JobTransfersListener(Job job, Worker res, JobManager jm) {
        this.job = job;
        this.res = res;
        this.jm = jm;
    }

    public void enable() {
        boolean finished;
        boolean failed;
        synchronized (this) {
            enabled = true;
            finished = operation == 0;
            failed = errors > 0;
        }

        if (finished) {
            if (failed) {
                doFailures();
            } else {
                doReady();
            }
        }
    }

    public synchronized void addOperation() {
        operation++;
    }

    @Override
    public void notifyEnd(DataOperation fOp) {
        boolean enabled;
        boolean finished;
        boolean failed;
        synchronized (this) {
            operation--;
            finished = operation == 0;
            failed = errors > 0;
            enabled = this.enabled;
        }
        if (finished && enabled) {
            if (failed) {
                doFailures();
            } else {
                doReady();
            }
        }
    }

    @Override
    public void notifyFailure(DataOperation fOp, Exception e) {
        e.printStackTrace();
        if (debug) {
            logger.error("THREAD " + Thread.currentThread().getName() + " File Operation failed on " + fOp.getName()
                    + ", file role is JOB_FILE"
                    + ", operation end state is FAILED",
                    e);
        } else {
            logger.error("THREAD " + Thread.currentThread().getName() + " File Operation failed on " + fOp.getName()
                    + ", file role is JOB_FILE"
                    + ", operation end state is FAILED");
        }
        boolean enabled;
        boolean finished;
        synchronized (this) {
            errors++;
            operation--;
            finished = operation == 0;
            enabled = this.enabled;
        }
        if (enabled && finished) {
            doFailures();
        }
    }

    private void doReady() {
        jm.submitJob(job, res);

    }

    private void doFailures() {
        jm.failedTransfers(job, errors, res);
    }
}
