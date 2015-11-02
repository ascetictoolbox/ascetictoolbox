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
import integratedtoolkit.util.ResourceManager;
import java.util.concurrent.Semaphore;

/**
 * This class represents a notification to end the execution
 */
public class ShutdownRequest extends TDRequest {

    /**
     * Semaphore where to synchronize until the operation is done
     */
    private Semaphore semaphore;

    /**
     * Constructs a new ShutdownRequest
     *
     */
    public ShutdownRequest(Semaphore sem) {
        this.semaphore = sem;
    }

    /**
     * Returns the semaphore where to synchronize until the object can be read
     *
     * @return the semaphore where to synchronize until the object can be read
     */
    public Semaphore getSemaphore() {
        return semaphore;
    }

    /**
     * Sets the semaphore where to synchronize until the requested object can be
     * read
     *
     * @param sem the semaphore where to synchronize until the requested object
     * can be read
     */
    public void setSemaphore(Semaphore sem) {
        this.semaphore = sem;
    }

    @Override
    public TDRequestType getRequestType() {
        return TDRequestType.SHUTDOWN;
    }

    @Override
    public void process(TaskScheduler ts, JobManager jm) throws ShutdownException {
        //ts.shutdown();
        jm.shutdown();
        ResourceManager.stopNodes();
        semaphore.release();
        throw new ShutdownException();
    }

    public static class ShutdownException extends Exception {

    }
}
