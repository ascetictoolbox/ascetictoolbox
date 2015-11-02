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

import es.bsc.comm.Connection;
import integratedtoolkit.nio.NIOAgent;
import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;

public class CommandShutdownACK extends Command implements Externalizable {

    public CommandShutdownACK() {
        super();
    }

    public CommandShutdownACK(NIOAgent ng) {
        super(ng);
    }

    @Override
    public CommandType getType() {
        return CommandType.STOP_WORKER_ACK;
    }

    @Override
    public void handle(Connection c) {
        agent.shutdownNotification(c);
    }

    @Override
    public void writeExternal(ObjectOutput out) throws IOException {

    }

    @Override
    public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {

    }

}
