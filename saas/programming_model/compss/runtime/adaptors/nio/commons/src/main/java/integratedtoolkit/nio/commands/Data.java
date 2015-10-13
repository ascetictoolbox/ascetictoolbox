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

package integratedtoolkit.nio.commands;

import integratedtoolkit.nio.NIOAgent;
import integratedtoolkit.types.data.LogicalData;
import integratedtoolkit.types.data.location.URI;
import integratedtoolkit.nio.NIOURI;
import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.util.LinkedList;

public class Data implements Externalizable {

    // Name of the data to send
    private String name;

    // Sources list
    private LinkedList<NIOURI> sources;

    public Data() {
    }

    public Data(String name, NIOURI uri) {
        this.name = name;
        sources = new LinkedList<NIOURI>();
        sources.add(uri);
    }

    public Data(LogicalData ld) {
        this.name = ld.getName();
        sources = new LinkedList<NIOURI>();
        for (URI uri : ld.getURIs()) {
            Object o = uri.getInternalURI(NIOAgent.ID);
            if (o != null) {
                this.sources.add((NIOURI) o);
            }
        }
    }

    public Data(LogicalData ld, NIOURI source) {
        this.name = ld.getName();
        sources = new LinkedList<NIOURI>();
        sources.add(source);
        for (URI uri : ld.getURIs()) {
            Object o = uri.getInternalURI(NIOAgent.ID);
            if (o != null) {
                this.sources.add((NIOURI) o);
            }
        }
    }

    // Returns true if the name of the data is the same
    // Returns false otherwise
    public boolean compareTo(Data n) {
        if (n.name.compareTo(name) == 0) {
            return true;
        } else {
            return false;
        }
    }

    public String getName() {
        return name;
    }

    public LinkedList<NIOURI> getSources() {
        return sources;
    }

    public NIOURI getFirstURI() {
        if (sources != null && !sources.isEmpty()) {
            return sources.getFirst();
        }
        return null;
    }

    public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
        name = in.readUTF();
        sources = (LinkedList<NIOURI>) in.readObject();
    }

    public void writeExternal(ObjectOutput out) throws IOException {
        out.writeUTF(name);
        out.writeObject(sources);
    }

    public String toString() {
        return name + "@" + sources;
    }
}
