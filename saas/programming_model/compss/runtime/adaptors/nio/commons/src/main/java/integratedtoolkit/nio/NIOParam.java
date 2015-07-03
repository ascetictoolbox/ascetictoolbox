package integratedtoolkit.nio;

import integratedtoolkit.api.ITExecution.ParamType;
import integratedtoolkit.nio.commands.Data;

import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;

public class NIOParam implements Externalizable {

    private ParamType type;
    private boolean mustWrite;

    private Data data;
    private Object value;

    public NIOParam() {

    }

    public NIOParam(ParamType type, Object value, boolean mustWrite, Data data) {
        this.type = type;
        this.value = value;
        this.mustWrite = mustWrite;
        this.data = data;
    }

    public ParamType getType() {
        return type;
    }

    public boolean isWrite() {
        return mustWrite;
    }

    public Object getValue() {
        return value;
    }

    public Data getData() {
        return data;
    }

    public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
        type = (ParamType) in.readObject();
        value = in.readObject();
        mustWrite = in.readBoolean();
        try {
            data = (Data) in.readObject();
        } catch (java.io.OptionalDataException e) {
            data = null;
        }
    }

    public void writeExternal(ObjectOutput out) throws IOException {
        out.writeObject(type);
        out.writeObject(value);
        out.writeBoolean(mustWrite);
        if (data != null) {
            out.writeObject(data);
        }
    }

    public String toString() {
        StringBuilder sb = new StringBuilder("[PARAM");
        sb.append("[TYPE = ").append(type).append("]");
        sb.append("[DATA ").append(data).append("]");
        sb.append("[VALUE = ").append(value).append("]");
        sb.append("[STORE AT END = ").append(mustWrite).append("]");
        sb.append("]");
        return sb.toString();
    }
}
