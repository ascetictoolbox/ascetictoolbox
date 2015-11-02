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

import java.io.Serializable;

import integratedtoolkit.types.data.location.DataLocation;

//Parameters of access to a file
public class AccessParams implements Serializable {

    public static enum AccessMode {

        R,// Read
        W, //Write
        RW//ReadWrite
    }

    private final AccessMode mode;

    public AccessParams(AccessMode mode) {
        this.mode = mode;
    }

    public AccessMode getMode() {
        return mode;
    }

    // File access
    public static class FileAccessParams extends AccessParams {

        DataLocation loc;

        public FileAccessParams(AccessMode mode, DataLocation loc) {
            super(mode);
            this.loc=loc;
        }

        public DataLocation getLocation() {
            return loc;
        }

    }

    // Object access
    public static class ObjectAccessParams extends AccessParams {

        private int hashCode;
        private Object value;

        public ObjectAccessParams(AccessMode mode, Object value, int hashCode) {
            super(mode);
            this.value = value;
            this.hashCode = hashCode;
        }

        public Object getValue() {
            return value;
        }

        public int getCode() {
            return hashCode;
        }
    }

}
