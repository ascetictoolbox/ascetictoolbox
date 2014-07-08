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

import java.util.HashMap;
import java.util.LinkedList;
import java.util.concurrent.Semaphore;

/**
 * This class represents a notification to end the execution
 */
public class ShutdownRequest extends TDRequest {

    /**
     * amount of tasks per core still pending on the graph
     */
    private HashMap<Integer, Integer> currentTaskCount;
    /**
     * set of obsolete data versions
     */
    private LinkedList<String> obsoletes;
    /**
     * Semaphore where to synchronize until the operation is done
     */
    private Semaphore semaphore;

    /**
     * Constructs a new ShutdownRequest
     *
     * @param currentTaskCount amount of tasks per core still pending on the
     * graph
     * @param obsoletes set of obsolete data versions
     */
    public ShutdownRequest(HashMap<Integer, Integer> currentTaskCount,
            LinkedList<String> obsoletes, Semaphore sem) {
        super(TDRequestType.SHUTDOWN);
        this.currentTaskCount = currentTaskCount;
        this.obsoletes = obsoletes;
        this.semaphore=sem;
    }

    /**
     * Returns the amount of tasks per core still pending on the graph
     *
     * @return amount of tasks per core still pending on the graph
     */
    public HashMap<Integer, Integer> getCurrentTaskCount() {
        return currentTaskCount;
    }

    /**
     * Sets the amount of tasks per core still pending on the graph
     *
     * @param currentTaskCount number of tasks per core still pending on the
     * graph
     */
    public void setCurrentTaskCount(HashMap<Integer, Integer> currentTaskCount) {
        this.currentTaskCount = currentTaskCount;
    }

    /**
     * Returns a set of obsolete data versions
     *
     * @return set of obsolete data versions
     */
    public LinkedList<String> getObsoletes() {
        return obsoletes;
    }

    /**
     * Sets the obsolete data versions
     *
     * @param obsoletes set of obsolete data versions
     */
    public void setObsoletes(LinkedList<String> obsoletes) {
        this.obsoletes = obsoletes;
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
}
