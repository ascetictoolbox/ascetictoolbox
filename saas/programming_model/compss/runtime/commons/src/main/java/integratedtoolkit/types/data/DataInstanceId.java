/**
 *
 *   Copyright 2014-2015 Barcelona Supercomputing Center (www.bsc.es) All rights reserved.
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

import java.io.Serializable;

// A File Instance is identified by its file and version identifiers 
public class DataInstanceId implements Serializable, Comparable<DataInstanceId> {

    // Time stamp
    private static String timeStamp = Long.toString(System.currentTimeMillis());

    // File instance identifier fields
    private int dataId;
    private int versionId;

    // Renaming for this file version
    private String renaming;

    public DataInstanceId() {
    }

    public DataInstanceId(int dataId, int versionId) {
        this.dataId = dataId;
        this.versionId = versionId;
        this.renaming = "d" + dataId + "v" + versionId + "_" + timeStamp + ".IT";
    }

    public int getDataId() {
        return dataId;
    }

    public int getVersionId() {
        return versionId;
    }

    public String getRenaming() {
        return renaming;
    }

    // Override the toString method
    public String toString() {
        return "d" + dataId + "v" + versionId;
    }

    // Comparable interface implementation
    public int compareTo(DataInstanceId dId) {
        if (dId == null) {
            throw new NullPointerException();
        }

        // First compare file identifiers
        if (dId.dataId != this.dataId) {
            return dId.dataId - this.dataId;
        } else {
            // If same file identifier, compare version identifiers
            return dId.versionId - this.versionId;
        }
    }

    @Override
    public boolean equals(Object o) {
        return (o instanceof DataInstanceId) && (this.compareTo((DataInstanceId) o) == 0);
    }

    @Override
    public int hashCode() {
        return super.hashCode();
    }

    public static String previousVersionRenaming(String renaming) {
        int dIdx = renaming.indexOf('d');
        int vIdx = renaming.indexOf('v');
        int tIndex = renaming.indexOf('_');
        if (vIdx == 1) {
            return null;
        }
        int dataId = Integer.parseInt(renaming.substring(dIdx + 1, vIdx));
        int previousVersion = Integer.parseInt(renaming.substring(vIdx + 1, tIndex)) - 1;
        return "d" + dataId + "v" + previousVersion + "_" + timeStamp + ".IT";
    }

}
