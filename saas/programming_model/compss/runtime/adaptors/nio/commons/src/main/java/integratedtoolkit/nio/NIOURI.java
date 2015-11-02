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
