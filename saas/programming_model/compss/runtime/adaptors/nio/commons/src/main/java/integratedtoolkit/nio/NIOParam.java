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
