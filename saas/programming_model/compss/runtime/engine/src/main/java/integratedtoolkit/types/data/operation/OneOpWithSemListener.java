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

import integratedtoolkit.log.Loggers;
import java.util.concurrent.Semaphore;
import org.apache.log4j.Logger;

public class OneOpWithSemListener extends DataOperation.EventListener {

    private static final Logger logger = Logger.getLogger(Loggers.FTM_COMP);
    private static final boolean debug = logger.isDebugEnabled();

    private Semaphore sem;

    public OneOpWithSemListener(Semaphore sem) {
        this.sem = sem;
    }

    @Override
    public void notifyEnd(DataOperation fOp) {
        sem.release();
    }

    @Override
    public void notifyFailure(DataOperation fOp, Exception e) {
        if (debug) {
            logger.error("THREAD " + Thread.currentThread().getName() + " File Operation failed on " + fOp.getName()
                    + ", file role is OPEN_FILE"
                    + ", operation end state is FAILED",
                    e);
        } else {
            logger.error("THREAD " + Thread.currentThread().getName() + " File Operation failed on " + fOp.getName()
                    + ", file role is OPEN_FILE"
                    + ", operation end state is FAILED");
        }

        sem.release();

    }
}
