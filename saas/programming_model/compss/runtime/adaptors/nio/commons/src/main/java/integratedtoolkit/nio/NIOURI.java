package integratedtoolkit.nio;

import es.bsc.comm.nio.NIONode;

import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;

public class NIOURI implements Externalizable {

    private static final String SCHEME = "any://";
    NIONode host;
    String path;

    public NIOURI() {
    }

    public NIOURI(NIONode host, String path) {
        this.host = host;
        this.path = path;
    }

    public String getInternalURI() {
        return toString();
    }

    public NIONode getHost() {
        return host;
    }

    public String getPath() {
        return path;
    }

    public String getScheme() {
        return "any://";
    }

    public String toString() {
        return SCHEME + host + "/" + path;
    }


    @Override
    public void writeExternal(ObjectOutput out) throws IOException {
        out.writeObject(host);
        out.writeUTF(path);
    }

    @Override
    public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
        host = (NIONode) in.readObject();
        path = in.readUTF();
    }

}
