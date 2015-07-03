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

        public RAccessId(int dataId, int rVersionId) {
            this.readDataInstance = new DataInstanceId(dataId, rVersionId);
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

        public WAccessId(int dataId, int wVersionId) {
            this.writtenDataInstance = new DataInstanceId(dataId, wVersionId);
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

        public RWAccessId(int dataId, int rVersionId, int wVersionId) {
            this.readDataInstance = new DataInstanceId(dataId, rVersionId);
            this.writtenDataInstance = new DataInstanceId(dataId, wVersionId);
        }

        public RWAccessId(DataInstanceId rdi, DataInstanceId wdi) {
            this.readDataInstance = rdi;
            this.writtenDataInstance = wdi;
        }

        public int getDataId() {
            return readDataInstance.getDataId();
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
