/*
 *  Copyright 2002-2012 Barcelona Supercomputing Center (www.bsc.es)
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package integratedtoolkit.types;

import integratedtoolkit.util.CoreManager;
import java.util.HashMap;

public class ScheduleState {

    //App Information
    public boolean endRequested;
    
    //Task counters
    public int noResourceCount;
    public int[] noResourceCounts;
    public int ordinaryCount;
    public int[] ordinaryCounts;
    public int toRescheduleCount;
    public int[] toRescheduleCounts;

    //Core Information
    int coreCount;
    public long[] coreMinTime;
    public long[] coreMeanTime;
    public long[] coreMaxTime;

    //Resource Information
    private HashMap<String, HostInfo> nameToInfo;

    //Cloud Usage
    public boolean useCloud;
    public int[] cloudSlots;
    public int currentCloudVMCount;
    public long creationTime;

    public ScheduleState() {
        coreCount = CoreManager.coreCount;
        //TaskInfo
        noResourceCounts = new int[coreCount];
        ordinaryCounts = new int[coreCount];
        toRescheduleCounts = new int[coreCount];

        //Core Information
        coreMinTime = new long[coreCount];
        coreMeanTime = new long[coreCount];
        coreMaxTime = new long[coreCount];

        //Resource Information
        nameToInfo = new HashMap<String, HostInfo>();
        cloudSlots = new int[coreCount];
    }

    public void addHost(String hostName, int[] coreSimultaneousTasks) {
        HostInfo hi = new HostInfo(hostName);
        hi.coreSlots = coreSimultaneousTasks;
        hi.running = new int[0];
        hi.elapsedTime = new long[0];
        nameToInfo.put(hostName, hi);
    }

    public void updateHostInfo(String hostName, int[] runningCores, long[] elapsedTime, Object waitingTasks) {
        HostInfo hi = nameToInfo.get(hostName);
        hi.running = runningCores;
        hi.elapsedTime = elapsedTime;
        hi.waitingTasks = waitingTasks;
    }

    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("Load Info").append("\n");
        for (int coreId = 0; coreId < coreCount; coreId++) {
            sb.append("\t* Core").append(coreId).append(": ")
                    .append("noResource: ").append(noResourceCounts[coreId]).append(", ")
                    .append("toReschedule: ").append(toRescheduleCounts[coreId]).append(", ")
                    .append("ordinary: ").append(ordinaryCounts[coreId]).append("\t\t")
                    .append("Min: ").append(coreMinTime[coreId]).append(", ")
                    .append("Mean: ").append(coreMeanTime[coreId]).append(", ")
                    .append("Max: ").append(coreMaxTime[coreId]).append("\n");
        }

        sb.append("Resource Info").append("\n");
        for (java.util.Map.Entry<String, HostInfo> entry : nameToInfo.entrySet()) {
            String resourceName = entry.getKey();
            HostInfo hi = entry.getValue();
            sb.append("\t* ").append(resourceName).append("\n");
            for (int i = 0; i < hi.running.length; i++) {
                sb.append("\t\t ").append(hi.running[i]).append("(").append(hi.elapsedTime[i]).append(")\n");
            }

        }
        if (useCloud) {
            sb.append("Cloud Info").append("\n");
            sb.append("\t * Creating [ ");
            sb.append(cloudSlots[0]);
            for (int coreId = 1; coreId < coreCount; coreId++) {
                sb.append(", ").append(cloudSlots[coreId]);
            }
            sb.append("]\n");
        }
        return sb.toString();
    }

    public int getCoreCount() {
        return coreCount;
    }

    public int[] getTotalTaskCount() {
        int[] pendingCounts = new int[coreCount];
        for (int coreId = 0; coreId < coreCount; coreId++) {
            pendingCounts[coreId] = noResourceCounts[coreId];
            pendingCounts[coreId] += toRescheduleCounts[coreId];
            pendingCounts[coreId] += ordinaryCounts[coreId];
        }
        return pendingCounts;
    }

    public int[] getRealSlots() {
        int[] coreSlots = new int[coreCount];
        for (HostInfo hi : nameToInfo.values()) {
            for (int i = 0; i < coreCount; i++) {
                coreSlots[i] += hi.coreSlots[i];
            }
        }
        return coreSlots;
    }

    public int[] getTotalSlots() {
        int[] coreSlots = new int[coreCount];
        for (HostInfo hi : nameToInfo.values()) {
            for (int i = 0; i < coreCount; i++) {
                coreSlots[i] += hi.coreSlots[i];
            }
        }
        for (int i = 0; i < coreCount; i++) {
            coreSlots[i] += cloudSlots[i];
        }
        return coreSlots;
    }

    private static class HostInfo {

        private String hostName;
        private int[] coreSlots;
        private int[] running;
        private long[] elapsedTime;
        private Object waitingTasks;

        public HostInfo(String hostName) {
            this.hostName = hostName;
        }
    }
}
