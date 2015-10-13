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

package integratedtoolkit.types;

import integratedtoolkit.util.CoreManager;
import java.util.HashMap;

public class ResourcesState {

    //Resource Information
    private final HashMap<String, HostInfo> nameToInfo;

    //Cloud Usage
    private boolean useCloud;
    private final int[] cloudSlots;
    private int currentCloudVMCount;
    private long creationTime;

    public ResourcesState() {
        //Resource Information
        nameToInfo = new HashMap<String, HostInfo>();
        cloudSlots = new int[CoreManager.getCoreCount()];
    }

    public void setUseCloud(boolean useCloud) {
        this.useCloud = useCloud;
    }

    public boolean getUseCloud() {
        return useCloud;
    }

    public void increaseCloudSlots(int core, int resourceSlots) {
        this.cloudSlots[core] += resourceSlots;
    }

    public int getCloudSlots(int core) {
        return cloudSlots[core];
    }

    public long getCreationTime() {
        return creationTime;
    }

    public void setCreationTime(long creationTime) {
        this.creationTime = creationTime;
    }

    public int getCurrentCloudVMCount() {
        return currentCloudVMCount;
    }

    public void setCurrentCloudVMCount(int currentCloudVMCount) {
        this.currentCloudVMCount = currentCloudVMCount;
    }

    public void addHost(String hostName, int[] coreSimultaneousTasks) {
        HostInfo hi = new HostInfo(hostName);
        hi.setCoreSlots(coreSimultaneousTasks);
        hi.setRunning(new int[0]);
        hi.setElapsedTime(new long[0]);
        nameToInfo.put(hi.getHostName(), hi);
    }

    public void updateHostInfo(String hostName, int[] runningCores, long[] elapsedTime, Object waitingTasks) {
        HostInfo hi = nameToInfo.get(hostName);
        hi.setRunning(runningCores);
        hi.setElapsedTime(elapsedTime);
        hi.setWaitingTasks(waitingTasks);
    }

    public int[] getRealSlots() {
        int[] coreSlots = new int[cloudSlots.length];
        for (HostInfo hi : nameToInfo.values()) {
            int[] hostCoreSlots = hi.getCoreSlots();
            for (int i = 0; i < cloudSlots.length; i++) {
                coreSlots[i] += hostCoreSlots[i];
            }
        }
        return coreSlots;
    }

    public int[] getTotalSlots() {
        int[] coreSlots = new int[cloudSlots.length];
        for (HostInfo hi : nameToInfo.values()) {
            int[] hostCoreSlots = hi.getCoreSlots();
            for (int i = 0; i < cloudSlots.length; i++) {
                coreSlots[i] += hostCoreSlots[i];
            }
        }
        for (int i = 0; i < cloudSlots.length; i++) {
            coreSlots[i] += cloudSlots[i];
        }
        return coreSlots;
    }

    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("RESOURCES_INFO = [").append("\n");
        for (java.util.Map.Entry<String, HostInfo> entry : nameToInfo.entrySet()) {
            String resourceName = entry.getKey();
            HostInfo hi = entry.getValue();
            sb.append("\t").append("RESOURCE = [").append("\n");
            sb.append("\t").append("\t").append("NAME = ").append(resourceName).append("\n");
            sb.append("\t").append("\t").append("HOST_INFO = [").append("\n");
            for (int i = 0; i < hi.getRunning().length; i++) {
                sb.append("\t").append("\t").append("\t").append("CORE = [").append("\n");
                sb.append("\t").append("\t").append("\t").append("\t").append("COREID = ").append(i).append("\n");
                sb.append("\t").append("\t").append("\t").append("\t").append("RUNNING = ").append(hi.getRunning()[i]).append("\n");
                sb.append("\t").append("\t").append("\t").append("\t").append("ELAPSED_TIME = ").append(hi.getElapsedTime()[i]).append("\n");
                sb.append("\t").append("\t").append("\t").append("]").append("\n");
            }
            sb.append("\t").append("\t").append("]").append("\n");
            sb.append("\t").append("]").append("\n");
        }
        sb.append("]").append("\n");

        //Cloud Information
        sb.append("CLOUD_INFO = [").append("\n");
        if (useCloud) {
            for (int coreId = 1; coreId < CoreManager.getCoreCount(); coreId++) {
                sb.append("\t").append("CREATING_CLOUD_SLOT = [").append("\n");
                sb.append("\t").append("\t").append("COREID = ").append(coreId).append("\n");
                sb.append("\t").append("\t").append("CLOUD_SLOTS = ").append(cloudSlots[coreId]).append("\n");
                sb.append("\t").append("]").append("\n");
            }
        }
        sb.append("]");
        return sb.toString();
    }

    private static class HostInfo {

        private String hostName;
        private int[] coreSlots;
        private int[] running;
        private long[] elapsedTime;
        private Object waitingTasks;

        public HostInfo(String hostName) {
            this.hostName = hostName;
            this.coreSlots = null;
            this.running = null;
            this.elapsedTime = null;
            this.waitingTasks = null;
        }

        public String getHostName() {
            return this.hostName;
        }

        public int[] getCoreSlots() {
            return this.coreSlots;
        }

        public void setCoreSlots(int[] coreSlots) {
            this.coreSlots = coreSlots;
        }

        public int[] getRunning() {
            return this.running;
        }

        public void setRunning(int[] running) {
            this.running = running;
        }

        public long[] getElapsedTime() {
            return this.elapsedTime;
        }

        public void setElapsedTime(long[] elapsedTime) {
            this.elapsedTime = elapsedTime;
        }

        public Object getWaitingTasks() {
            return this.waitingTasks;
        }

        public void setWaitingTasks(Object waitingTasks) {
            this.waitingTasks = waitingTasks;
        }
    }
}
