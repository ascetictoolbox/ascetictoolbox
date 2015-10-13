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

package integratedtoolkit.connectors;

import integratedtoolkit.types.COMPSsWorker;
import integratedtoolkit.types.resources.CloudMethodWorker;
import integratedtoolkit.types.resources.description.CloudMethodResourceDescription;

public class VM implements Comparable<VM> {

    protected Object envId;
    protected CloudMethodResourceDescription rd;
    protected COMPSsWorker node;
    protected CloudMethodWorker worker;
    protected long requestTime;
    protected long startTime;
    protected long creationTime;
    protected boolean toDelete;

    public VM(Object envId, CloudMethodResourceDescription description) {
        this.envId = envId;
        this.rd = description;
        this.requestTime = System.currentTimeMillis();
        this.startTime = 0;
        this.creationTime = 0;
        this.toDelete = false;
    }

    public void computeCreationTime() {
        creationTime = this.startTime - this.requestTime;
    }

    public String toString() {
        return "VM " + envId
                + " (ip = " + rd.getName()
                + ", request time = " + requestTime
                + ", start time = " + startTime
                + ", creation time = " + creationTime
                + ", image = " + rd.getImage().getName()
                + ", procs = " + rd.getProcessorCoreCount()
                + ", memory = " + rd.getMemoryPhysicalSize()
                + ", disk = " + rd.getStorageElemSize()
                + ", to delete = " + toDelete + ")";
    }

    public Object getEnvId() {
        return envId;
    }

    public String getName() {
        return rd.getName();
    }

    public CloudMethodResourceDescription getDescription() {
        return this.rd;
    }

    public COMPSsWorker getNode() {
        return node;
    }

    public void setWorker(CloudMethodWorker worker) {
        this.worker = worker;
    }

    public CloudMethodWorker getWorker() {
        return worker;
    }

    public long getStartTime() {
        return startTime;
    }

    public String getImage() {
        return rd.getImage().getName();
    }

    public boolean isToDelete() {
        return toDelete;
    }

    public void setToDelete(boolean toDelete) {
        this.toDelete = toDelete;
    }

    // Comparable interface implementation
    public int compareTo(VM vm) throws NullPointerException {
        if (vm == null) {
            throw new NullPointerException();
        }

        if (vm.getName().equals(getName())) {
            return 0;
        }

        long now = System.currentTimeMillis();
        int mod1 = (int) (now - getStartTime()) % 3600000;  // 1 h in ms
        int mod2 = (int) (now - vm.getStartTime()) % 3600000;  // 1 h in ms

        return mod2 - mod1;
    }

}
