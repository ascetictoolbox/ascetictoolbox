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

package integratedtoolkit.types.request.ap;

import integratedtoolkit.components.impl.DataInfoProvider;
import integratedtoolkit.components.impl.TaskAnalyser;
import integratedtoolkit.components.impl.TaskDispatcher;
import integratedtoolkit.types.data.FileInfo;
import integratedtoolkit.types.data.location.DataLocation;

public class DeleteFileRequest extends APRequest {

    private final DataLocation loc;

    public DeleteFileRequest(DataLocation loc) {
        this.loc = loc;
    }

    public DataLocation getLocation() {
        return loc;
    }

    @Override
    public void process(TaskAnalyser ta, DataInfoProvider dip, TaskDispatcher td) {
        FileInfo fileInfo = dip.deleteData(loc);

        if (fileInfo == null) { //file is not used by any task
            java.io.File f = new java.io.File(loc.getPath());
            f.delete();
        } else { // file is involved in some task execution
            if (!fileInfo.isToDelete()) {
                // There are no more readers, therefore it can 
                //be deleted (once it has been created)
                ta.deleteFile(fileInfo);
            } else {
                // Nothing has to be done yet. It is already marked as a
                //file to delete but it has to be read by some tasks
            }
        }
    }

    @Override
    public APRequestType getRequestType() {
        return APRequestType.DELETE_FILE;
    }

}
