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

package integratedtoolkit.components;

import java.util.List;

import integratedtoolkit.types.data.AccessParams;
import integratedtoolkit.types.data.DataAccessId;


/* To register a new data access (file/object), either from a task or from the main code of the application
 * To determine if a datum has already been accessed
 */
public interface DataAccess {
    // File access modes

    public enum AccessMode {

        R,
        W,
        RW;
    }
    /*
    // Returns an identifier for the file
    DataAccessId registerFileAccess(AccessMode mode,
    String fileName,
    String path,
    String host,
    int readerId);

    // Returns an identifier for the object
    DataAccessId registerObjectAccess(AccessMode mode,
    Object value,
    int code,
    int readerId);*/

    // Returns true if the file has been accessed by a task before
    boolean alreadyAccessed(String fileName,
            String path,
            String host);

    //Access by Main Program
    DataAccessId registerDataAccess(AccessParams accesses);

    //Accessed by task
    List<DataAccessId> registerDataAccesses(List<AccessParams> accesses, int readerMethodId);
}
