package es.bsc.comm.stage;

public abstract class Transfer extends Stage {

    public enum Direction {
        SEND, RECEIVE
    }

    public enum Type {
        COMMAND, DATA
    }

    public enum Destination {
        OBJECT, FILE, ARRAY
    }


    protected static final int SIZE_SIZE = 8;
    protected static final int TYPE_SIZE = 4;
    protected static final int DEST_SIZE = 4;
    protected static final int INIT_SIZE = TYPE_SIZE + DEST_SIZE;
    protected static final int HEADER_SIZE = SIZE_SIZE + INIT_SIZE;

    protected Type type;
    protected Destination destination;

    // Value
    protected String fileName;
    protected byte[] array;
    protected Object object;

    // To know whether the size has been initialized
    protected boolean sizeInit = false;

    // Total size to read/write
    protected long totalSize = -1L;

    // Remaining size to read/write
    protected long remainingSize;


    public Transfer(boolean notifyErrors) {
        super(notifyErrors);
    }

    protected void setSize(long size) {
        totalSize = size;
        remainingSize = size;
        sizeInit = true;
    }

    /**
     * Return size if it has been initialized
     *
     * @return the size or -1 if it's not initialized
     */
    public long getSize() {
        return totalSize;
    }

    public Type getType() {
        return this.type;
    }

    public boolean isData() {
        return this.type == Type.DATA;
    }

    public boolean isCommand() {
        return this.type == Type.COMMAND;
    }

    public abstract Direction getDirection();

    public Destination getDestination() {
        return destination;
    }

    // Return true if it is a file transfer
    public boolean isFile() {
        return destination == Destination.FILE;
    }

    public String getFileName() {
        return fileName;
    }

    // Return true if it is an object transfer
    public boolean isObject() {
        return destination == Destination.OBJECT;
    }

    public Object getObject() {
        return object;
    }

    // Return true if it is an object transfer
    public boolean isArray() {
        return destination == Destination.ARRAY;
    }

    public byte[] getArray() {
        return array;
    }

    @Override
    public String toString() {
        return super.toString() + "[" + this.getDirection() + " " + type + " " + destination + "]";
    }

}
