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

import java.util.LinkedList;

/**
 * The NewWaitingTaskRequest represents a notification about a task that will be
 * dependency-free when some of the task already submitted to the TaskDispathcer
 * end.
 */
public class NewWaitingTaskRequest extends TDRequest {

    /** blocked task method id */
    private int coreId;
    /** List of obsolete versions */
    private LinkedList<String> obsoletes;

    /**
     * Contructs a NewWaitingTaskRequest
     * @param coreId core id of the blocked task
     * @param obsoletes list of obsolete files
     */
    public NewWaitingTaskRequest(int coreId, LinkedList<String> obsoletes) {
        super(TDRequestType.NEW_WAITING_TASK);
        this.coreId = coreId;
        this.obsoletes = obsoletes;
    }

    /** 
     * Returns the core Id of the blocked task
     * @return core Id of the blocked task
     */
    public int getMethodId() {
        return coreId;
    }

    /**
     * Sets the core Id of the blocked task
     * @param coreId core Id of the blocked task
     */
    public void setMethodId(int coreId) {
        this.coreId = coreId;
    }

    /**
     * Returns a list with the obsolete files name
     * @return list with the obsolete files name
     */
    public LinkedList<String> getObsoletes() {
        return obsoletes;
    }

    /**
     * Changes the obsolete files list
     * @param obsoletes list with the obsolete files name
     */
    public void setObsoletes(LinkedList<String> obsoletes) {
        this.obsoletes = obsoletes;
    }
}
