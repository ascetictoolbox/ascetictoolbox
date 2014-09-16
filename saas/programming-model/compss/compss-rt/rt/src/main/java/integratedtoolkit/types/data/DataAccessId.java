/*
 *  Copyright 2002-2014 Barcelona Supercomputing Center (www.bsc.es)
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
package integratedtoolkit.types.data;

import java.io.Serializable;

public abstract class DataAccessId implements Serializable {

    public abstract int getDataId();

    // Read access
    public static class RAccessId extends DataAccessId {

        // File version read
        private DataInstanceId readDataInstance;

        public RAccessId() {
        }

        public RAccessId(int dataId, int RVersionId) {
            this.readDataInstance = new DataInstanceId(dataId, RVersionId);
        }

        public RAccessId(DataInstanceId rdi) {
            this.readDataInstance = rdi;
        }

        public int getDataId() {
            return readDataInstance.getDataId();
        }

        public int getRVersionId() {
            return readDataInstance.getVersionId();
        }

        public DataInstanceId getReadDataInstance() {
            return readDataInstance;
        }

        public String toString() {
            return "Read data: " + readDataInstance;
        }

    }

    // Write access
    public static class WAccessId extends DataAccessId {

        // File version written
        private DataInstanceId writtenDataInstance;

        public WAccessId() {
        }

        public WAccessId(int dataId, int WVersionId) {
            this.writtenDataInstance = new DataInstanceId(dataId, WVersionId);
        }

        public WAccessId(DataInstanceId wdi) {
            this.writtenDataInstance = wdi;
        }

        public int getDataId() {
            return writtenDataInstance.getDataId();
        }

        public int getWVersionId() {
            return writtenDataInstance.getVersionId();
        }

        public DataInstanceId getWrittenDataInstance() {
            return writtenDataInstance;
        }

        public String toString() {
            return "Written data: " + writtenDataInstance;
        }

    }

    // Read-Write access
    public static class RWAccessId extends DataAccessId {

        // File version read
        private DataInstanceId readDataInstance;
        // File version written
        private DataInstanceId writtenDataInstance;

        public RWAccessId() {
        }

        public RWAccessId(int dataId, int RVersionId, int WVersionId) {
            this.readDataInstance = new DataInstanceId(dataId, RVersionId);
            this.writtenDataInstance = new DataInstanceId(dataId, WVersionId);
        }

        public RWAccessId(DataInstanceId rdi, DataInstanceId wdi) {
            this.readDataInstance = rdi;
            this.writtenDataInstance = wdi;
        }

        public int getDataId() {
            return readDataInstance.getDataId();
            // or return writtenFileInstance.getDataId();
        }

        public int getRVersionId() {
            return readDataInstance.getVersionId();
        }

        public int getWVersionId() {
            return writtenDataInstance.getVersionId();
        }

        public DataInstanceId getReadDataInstance() {
            return readDataInstance;
        }

        public DataInstanceId getWrittenDataInstance() {
            return writtenDataInstance;
        }

        public String toString() {
            return "Read data: " + readDataInstance + ", Written data: " + writtenDataInstance;
        }

    }

}
