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
package integratedtoolkit.components.impl;

import integratedtoolkit.log.Loggers;
import integratedtoolkit.types.ScheduleDecisions;
import integratedtoolkit.types.ScheduleState;
import integratedtoolkit.types.Task;
import integratedtoolkit.util.CoreManager;
import integratedtoolkit.util.ProjectManager;
import java.util.LinkedList;
import java.util.List;
import org.apache.log4j.Logger;

public class SchedulingOptimizer extends Thread {

    private final Object alarmClock = new Object();
    private boolean running;
    private Integer maxNumberOfVMs;
    private Integer minNumberOfVMs;
    private TaskDispatcher TD;
    //Number of Graph second level tasks per method
    private static int[] secondLevelGraphCount;
    private static final Logger monitor = Logger.getLogger(Loggers.RESOURCES);
    private static final boolean monitorDebug = monitor.isDebugEnabled();
    private static final Logger logger = Logger.getLogger(Loggers.TS_COMP);
    private static final boolean debug = logger.isDebugEnabled();
    private static boolean cleanUp;
    private static boolean redo;

    SchedulingOptimizer() {
        secondLevelGraphCount = new int[CoreManager.coreCount];
        redo = false;
    }

    public void setCoWorkers(TaskDispatcher td) {
        TD = td;
    }

    public void kill() {
        synchronized (alarmClock) {
            running = false;
            alarmClock.notify();
            cleanUp = true;
        }
    }

    public void run() {
        running = true;
        ScheduleState oldSchedule;
        ScheduleDecisions newSchedule;

        while (running) {
            try {
                do {
                    redo = false;
                    oldSchedule = TD.getCurrentSchedule();
                    monitor.debug(oldSchedule.toString());
                    newSchedule = new ScheduleDecisions();
                    if (oldSchedule.useCloud) {
                        applyPolicies(oldSchedule, newSchedule);
                    }
                    monitor.debug(newSchedule.toString());
                    loadBalance(oldSchedule, newSchedule);
                } while (redo);
                TD.setNewSchedule(newSchedule);
                try {
                    synchronized (alarmClock) {
                        alarmClock.wait(20000);
                    }
                } catch (InterruptedException ex) {
                    ex.printStackTrace();
                }

            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public void optimizeNow() {
        synchronized (alarmClock) {
            alarmClock.notify();
            redo = true;
        }
    }

    public void updateWaitingCounts(List<Task> tasks, boolean waiting, int[] waitingSet) {
        for (int i = 0; i < CoreManager.coreCount; i++) {
            secondLevelGraphCount[i] += waitingSet[i];
        }
        for (Task currentTask : tasks) {
            if (waiting) {
                secondLevelGraphCount[currentTask.getTaskParams().getId()]--;
            }
        }
    }

    public void newWaitingTask(int methodId) {
        secondLevelGraphCount[methodId]++;
    }

    public void cleanUp() {
        cleanUp = true;
    }

    public void resizeDataStructures() {
        int[] secondLevelGraphCountTmp = new int[CoreManager.coreCount];
        System.arraycopy(secondLevelGraphCount, 0, secondLevelGraphCountTmp, 0, secondLevelGraphCount.length);
        secondLevelGraphCount = secondLevelGraphCountTmp;
    }

    private void applyPolicies(ScheduleState oldSchedule, ScheduleDecisions sd) {
        int currentCloudVMCount = oldSchedule.currentCloudVMCount;
        long creationTime = oldSchedule.creationTime;

        try {
            ProjectManager.refresh();
            String maxAmountString = ProjectManager.getCloudProperty("maxVMCount");
            if (maxAmountString != null) {
                maxNumberOfVMs = Integer.parseInt(maxAmountString);
            }
            String minAmountString = ProjectManager.getCloudProperty("minVMCount");
            if (minAmountString != null) {
                minNumberOfVMs = Integer.parseInt(minAmountString);
            }
            if (maxNumberOfVMs != null && minNumberOfVMs != null && minNumberOfVMs > maxNumberOfVMs) {
                minNumberOfVMs = maxNumberOfVMs;
            }
        } catch (Exception e) {
        }

        int coreCount = oldSchedule.getCoreCount();
        int noResourceCount = oldSchedule.noResourceCount;
        int[] noResourceCounts = oldSchedule.noResourceCounts;
        int toRescheduleCount = oldSchedule.toRescheduleCount;
        int ordinaryCount = oldSchedule.ordinaryCount;

        long[] minCoreTime = new long[coreCount];
        long[] meanCoreTime = new long[coreCount];
        long[] maxCoreTime = new long[coreCount];
        //TODO: limit to the creation time
        for (int coreId = 0; coreId < coreCount; coreId++) {
            minCoreTime[coreId] = Math.min(oldSchedule.coreMinTime[coreId], creationTime);
            meanCoreTime[coreId] = Math.min(oldSchedule.coreMeanTime[coreId], creationTime);
            maxCoreTime[coreId] = Math.min(oldSchedule.coreMaxTime[coreId], creationTime);
        }

        long[] aggregatedMinCoreTime = new long[coreCount];
        long[] aggregatedMeanCoreTime = new long[coreCount];
        long[] aggregatedMaxCoreTime = new long[coreCount];

        int[] realSlots = oldSchedule.getRealSlots();
        int[] totalSlots = oldSchedule.getTotalSlots();
        int[] pendingCounts = oldSchedule.getTotalTaskCount();

        for (int i = 0; i < coreCount; i++) {
            aggregatedMinCoreTime[i] = minCoreTime[i] * pendingCounts[i];
            aggregatedMeanCoreTime[i] = meanCoreTime[i] * pendingCounts[i];
            aggregatedMaxCoreTime[i] = maxCoreTime[i] * pendingCounts[i];
        }

        if (!cleanUp && (maxNumberOfVMs == null || maxNumberOfVMs > currentCloudVMCount)) {
            sd.creationRecommendations = dynamicCreationPolicy(coreCount, creationTime, aggregatedMinCoreTime, aggregatedMeanCoreTime, aggregatedMaxCoreTime, totalSlots, realSlots);
            sd.requiredRecomendations = checkNeededMachines(noResourceCount, noResourceCounts, totalSlots);
            for (Integer coreId : sd.requiredRecomendations) {
                //mirar que com a minim es demani un slot per aquell
                sd.creationRecommendations[coreId] = Math.max(sd.creationRecommendations[coreId], 1);
                sd.recommendsCreation = true;
            }
            for (int coreId = 0; coreId < coreCount && (!sd.recommendsCreation); coreId++) {
                sd.recommendsCreation = sd.recommendsCreation || (sd.creationRecommendations[coreId] > 0);
            }
        } else {
            sd.creationRecommendations = new float[coreCount];
            sd.requiredRecomendations = new LinkedList<Integer>();
        }
        if (minNumberOfVMs != null && minNumberOfVMs > currentCloudVMCount) {
            //marcar la creació com a obligatoria
            sd.mandatoryCreations = true;
            if (!sd.recommendsCreation) {
                sd.creationRecommendations = enforcedCreationPolicy(coreCount, creationTime, aggregatedMinCoreTime, aggregatedMeanCoreTime, aggregatedMaxCoreTime, totalSlots, realSlots);
            }
            sd.recommendsCreation = true;
        }

        //join second level tasks to the statistics
        for (int i = 0; i < coreCount; i++) {
            aggregatedMinCoreTime[i] = minCoreTime[i] * pendingCounts[i];
            aggregatedMeanCoreTime[i] = meanCoreTime[i] * pendingCounts[i];
            aggregatedMaxCoreTime[i] = maxCoreTime[i] * pendingCounts[i];
        }

        if (minNumberOfVMs == null || currentCloudVMCount > minNumberOfVMs) {
            //Tenim marge per controlar quines màquines s'han de destruir
            sd.destroyRecommendations = destructionPolicy(coreCount, creationTime, aggregatedMinCoreTime, aggregatedMeanCoreTime, aggregatedMaxCoreTime, totalSlots, realSlots);

            float maxValue = Float.MIN_VALUE;
            int maxIndex = 0;
            for (int coreId = 0; coreId < coreCount; coreId++) {
                if (sd.destroyRecommendations[coreId] <= 0) {
                    sd.destroyRecommendations[coreId] = 0;
                } else {
                    sd.recommendsDestruction = true;
                    if (maxValue < sd.destroyRecommendations[coreId]) {
                        maxIndex = coreId;
                        maxValue = sd.destroyRecommendations[coreId];
                    }
                }
            }

            if (maxNumberOfVMs != null && currentCloudVMCount > maxNumberOfVMs) {
                sd.mandatoryDestruction = true;
                if (!sd.recommendsDestruction) {
                    sd.destroyRecommendations[maxIndex] = 1;
                }
                sd.recommendsDestruction = true;
            }
        } else {
            sd.destroyRecommendations = new float[coreCount];
        }

    }

    private LinkedList<Integer> checkNeededMachines(int noResourceCount, int[] noResourceCountPerCore, int[] slotCountPerCore) {
        LinkedList<Integer> needed = new LinkedList<Integer>();
        if (noResourceCount == 0) {
            return needed;
        }
        for (int i = 0; i < CoreManager.coreCount; i++) {
            if (noResourceCountPerCore[i] > 0 && slotCountPerCore[i] == 0) {
                needed.add(i);
            }
        }
        return needed;
    }

    private float[] dynamicCreationPolicy(int coreCount, long creationTime, long[] aggregatedMinCoreTime, long[] aggregatedMeanCoreTime, long[] aggregatedMaxCoreTime, int[] totalSlots, int[] realSlots) {
        float[] creations = new float[coreCount];
        for (int coreId = 0; coreId < coreCount; coreId++) {
            long realTime = realSlots[coreId] * creationTime;
            long totalTime = totalSlots[coreId] * creationTime;
            long embraceableLoad = (realTime + totalTime) / 2;
            long remainingLoad = aggregatedMeanCoreTime[coreId] - embraceableLoad;
            if (remainingLoad > 0) {
                creations[coreId] = (int) (remainingLoad / creationTime);
            } else {
                creations[coreId] = 0;
            }
        }
        return creations;
    }

    private float[] enforcedCreationPolicy(int coreCount, long creationTime, long[] aggregatedMinCoreTime, long[] aggregatedMeanCoreTime, long[] aggregatedMaxCoreTime, int[] totalSlots, int[] realSlots) {
        float[] creations = new float[coreCount];
        int maxI = 0;
        float maxRatio = 0;
        for (int i = 0; i < CoreManager.coreCount; i++) {
            if (aggregatedMeanCoreTime[i] > 0) {
                float ratio = aggregatedMeanCoreTime[i] / totalSlots[i];
                if (ratio > maxRatio) {
                    maxI = i;
                    maxRatio = ratio;
                }
            }
        }
        creations[maxI] = 1;
        return creations;
    }

    private float[] destructionPolicy(int coreCount, long limitTime, long[] aggregatedMinCoreTime, long[] aggregatedMeanCoreTime, long[] aggregatedMaxCoreTime, int[] totalSlots, int[] realSlots) {
        float[] destructions = new float[coreCount];
        for (int coreId = 0; coreId < coreCount; coreId++) {
            long embraceableLoad = limitTime * (realSlots[coreId]);
            if (embraceableLoad == 0l) {
                destructions[coreId] = 0;
            } else {
                double unusedTime = (((double) 3 * embraceableLoad) / (double) 4) - (double) aggregatedMinCoreTime[coreId];
                destructions[coreId] = (float) (unusedTime / (double) limitTime);
            }
        }
        return destructions;
    }

    private void loadBalance(ScheduleState ss, ScheduleDecisions sd) {
        /*
         LinkedList<HostSlotsTime>[] hostListPerCore = new LinkedList[CoreManager.coreCount];
         for (int coreId = 0; coreId < CoreManager.coreCount; coreId++) {
         hostListPerCore[coreId] = new LinkedList();
         }

         Set<String> resources = ss.getHostNames();
         for (String res : resources) {
         HostSlotsTime hst = new HostSlotsTime();
         hst.hostName = res;
         hst.slotsTime = ss.getSlotsWaitingTime(res, ss.coreMeanExecutionTime);
         hst.slotCoreCount = ss.getLastReadSlotsCoreCount();
         for (int coreId : ss.getLastReadAbleCores()) {
         hostListPerCore[coreId].add(hst);
         }
         }

         for (int coreId = 0; coreId < CoreManager.coreCount; coreId++) {
         //Busquem els recursos que poden executar pel core
         LinkedList<CoreTransferRequest> sender = new LinkedList();
         LinkedList<CoreTransferRequest> receiver = new LinkedList();
         LinkedList<HostSlotsTime> resourceList = hostListPerCore[coreId];
         int slotCount = 0;
         long slotTime = 0l;

         //Fem la suma del nombre de slots i del temps total
         for (HostSlotsTime hst : resourceList) {
         for (int i = 0; i < hst.slotsTime.length; i++) {
         slotCount++;
         slotTime += hst.slotsTime[i];
         }
         }
         if (slotCount == 0) {
         continue;
         }
         //Fem la mitja del temps ocupat      
         long average = slotTime / (long) slotCount;

         //Calculem la  donacio/recepció
         for (HostSlotsTime hst : resourceList) {
         for (int i = 0; i < hst.slotsTime.length; i++) {
         double ratio = (double) (hst.slotsTime[i] - average) / (double) ss.coreMeanExecutionTime[coreId];
         if (ratio < 0) {
         ratio -= 0.5;
         int change = (int) ratio;
         receiver.add(new CoreTransferRequest(hst, i, Math.abs(change)));
         } else if (ratio > 0) {
         ratio += 0.5;
         int change = (int) ratio;
         change = Math.min(change, hst.slotCoreCount[i][coreId]);
         sender.add(new CoreTransferRequest(hst, i, change));
         }
         }
         }
         //Fem el moviment
         for (CoreTransferRequest sndr : sender) {
         if (receiver.isEmpty()) {
         break;
         }
         while (sndr.amount > 0) {
         if (receiver.isEmpty()) {
         break;
         }
         CoreTransferRequest rcvr = receiver.get(0);
         int move = Math.min(rcvr.amount, sndr.amount);
         moveTask(sndr, rcvr, move, coreId, ss.coreMeanExecutionTime[coreId]);
         sd.addMovement(sndr.hst.hostName, sndr.slot, rcvr.hst.hostName, rcvr.slot, coreId, move);
         if (rcvr.amount == 0) {
         receiver.remove(0);
         }
         }
         }
         }*/
    }

    /*private void moveTask(CoreTransferRequest sndr, CoreTransferRequest rcvr, int amount, int coreId, long coreTime) {
     sndr.amount -= amount;
     rcvr.amount -= amount;
     sndr.hst.slotCoreCount[sndr.slot][coreId] -= amount;
     rcvr.hst.slotCoreCount[rcvr.slot][coreId] += amount;
     sndr.hst.slotsTime[sndr.slot] -= coreTime * amount;
     rcvr.hst.slotsTime[rcvr.slot] += coreTime * amount;
     }*/
    /*
     private class HostSlotsTime {

     String hostName;
     long[] slotsTime;
     int[][] slotCoreCount;
     }

     private class CoreTransferRequest {

     HostSlotsTime hst;
     int slot;
     int amount;

     public CoreTransferRequest(HostSlotsTime hst, int slot, int amount) {
     this.hst = hst;
     this.slot = slot;
     this.amount = amount;
     }
     }
     */
}
