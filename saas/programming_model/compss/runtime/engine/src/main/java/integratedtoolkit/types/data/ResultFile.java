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

package integratedtoolkit.types.data;

import integratedtoolkit.types.data.location.DataLocation;
import java.io.File;

public class ResultFile implements Comparable<ResultFile> {

    DataInstanceId fId;
    DataLocation originalLocation;

    public ResultFile(DataInstanceId fId, DataLocation location) {
        this.fId = fId;
        this.originalLocation = location;
    }

    public DataInstanceId getFileInstanceId() {
        return fId;
    }

    public DataLocation getOriginalLocation() {
        return originalLocation;
    }

    public String getOriginalName() {
        String[] splitPath = originalLocation.getPath().split(File.separator);
        return splitPath[splitPath.length - 1];
    }

    // Comparable interface implementation
    public int compareTo(ResultFile resFile) throws NullPointerException {
        if (resFile == null) {
            throw new NullPointerException();
        }

        // Compare file identifiers
        return this.getFileInstanceId().compareTo(resFile.getFileInstanceId());
    }

    public String toString() {
        return fId.getRenaming();
    }

}
