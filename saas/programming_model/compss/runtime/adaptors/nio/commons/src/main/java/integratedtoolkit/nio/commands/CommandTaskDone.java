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

import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;

import integratedtoolkit.nio.NIOAgent;

import es.bsc.comm.Connection;

public class CommandTaskDone extends Command implements Externalizable {

    private int jobID;
    private boolean successful;

    public CommandTaskDone() {
    }

    public CommandTaskDone(NIOAgent ng, int jobID, boolean successful) {
        super(ng);
        this.jobID = jobID;
        this.successful = successful;
    }

    @Override
    public CommandType getType() {
        return CommandType.TASK_DONE;
    }

    @Override
    public void handle(Connection c) {
        NIOAgent nm = (NIOAgent) agent;
        nm.receivedTaskDone(c, jobID, successful);
    }

    public boolean isSuccessful() {
        return successful;
    }

    public int getJobID() {
        return jobID;
    }

    public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
        jobID = in.readInt();
        successful = in.readBoolean();
    }

    public void writeExternal(ObjectOutput out) throws IOException {
        out.writeInt(jobID);
        out.writeBoolean(successful);
    }

    public String toString() {
        return "Job" + jobID + " finishes " + (successful ? "properly" : "with some errors");
    }
}
