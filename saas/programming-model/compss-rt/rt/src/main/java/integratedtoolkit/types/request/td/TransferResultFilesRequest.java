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

import java.util.List;
import java.util.concurrent.Semaphore;

import integratedtoolkit.types.data.ResultFile;

/** 
 * The TransferResultFilesRequest class represents a request to send a set of 
 * files resultants from the execution located in the workers to the master
 */
public class TransferResultFilesRequest extends TDRequest {

    /** List of files to be transferred */
    private List<ResultFile> resFiles;
    /** Semaphore where to synchronize until the operation is done*/
    private Semaphore sem;

    /**
     * Constructs a new TransferResultFilesRequest
     * @param resFiles List of files to be transferred
     * @param sem Semaphore where to synchronize until the operation is done
     */
    public TransferResultFilesRequest(List<ResultFile> resFiles, Semaphore sem) {
        super(TDRequestType.TRANSFER_RESULT_FILES);
        this.resFiles = resFiles;
        this.sem = sem;
    }

    /**
     * Returns the list of files to be transferred back
     * @return The list of files to be transferred back
     */
    public List<ResultFile> getResFiles() {
        return resFiles;
    }

    /**
     * Sets the files to be transferred back to the master
     * @param resFiles list of files to be transferred back to the master
     */
    public void setResFiles(List<ResultFile> resFiles) {
        this.resFiles = resFiles;
    }

    /**
     * Returns the semaphore where to synchronize until the operation is done
     * @return Semaphore where to synchronize until the operation is done
     */
    public Semaphore getSemaphore() {
        return sem;
    }

    /**
     * Sets the semaphore where to synchronize until the operation is done
     * @param sem Semaphore where to synchronize until the operation is done
     */
    public void setSemaphore(Semaphore sem) {
        this.sem = sem;
    }
}
