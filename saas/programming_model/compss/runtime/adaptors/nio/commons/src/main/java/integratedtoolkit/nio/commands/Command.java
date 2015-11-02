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
import java.io.Externalizable;

import integratedtoolkit.nio.NIOAgent;

public abstract class Command implements Externalizable {

    // Type of command
    // NEW TASK: send a new task to a node with a list of the files and its locations
    // DATA DEMAND: ask a node for some data
    // DATA NEGATE: can not send the data now
    // DATA RECEIVED: notify the master that the worker has received the data
    // TASK DONE: notify the master that the task has been done
    // SHUTDOWN: tell the worker to shutdown
    // SHUTDOWN: lets the master know that the worker is stopping
    public enum CommandType {

        NEW_TASK, DATA_DEMAND, DATA_NEGATE, DATA_RECEIVED, TASK_DONE, START_WORKER, STOP_WORKER, STOP_WORKER_ACK
    }

    public NIOAgent agent;

    public Command() {
    }

    public Command(NIOAgent agent) {
        this.agent = agent;
    }

    public abstract CommandType getType();

    public abstract void handle(Connection c);

}
