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
