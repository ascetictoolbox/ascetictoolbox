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

package integratedtoolkit.util;

import integratedtoolkit.components.ResourceUser;
import integratedtoolkit.components.ResourceUser.WorkloadStatus;
import integratedtoolkit.log.Loggers;
import integratedtoolkit.types.Implementation;
import integratedtoolkit.types.MethodImplementation;
import integratedtoolkit.types.ResourceCreationRequest;
import integratedtoolkit.types.resources.CloudMethodWorker;
import integratedtoolkit.types.resources.MethodResourceDescription;
import integratedtoolkit.types.resources.description.CloudMethodResourceDescription;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;

import java.util.LinkedList;
import java.util.Map;
import java.util.PriorityQueue;
import org.apache.log4j.Logger;

public class ResourceOptimizer extends Thread {

    private final Object alarmClock = new Object();
    private boolean running;
    private Integer maxNumberOfVMs;
    private Integer minNumberOfVMs;
    private ResourceUser resUser;

    private static boolean cleanUp;
    private static boolean redo;

    //Loggers
    private static final Logger resourcesLogger = Logger.getLogger(Loggers.RESOURCES);

    private static final Logger logger = Logger.getLogger(Loggers.TS_COMP);
    private static final boolean debug = logger.isDebugEnabled();

    ResourceOptimizer(ResourceUser resUser) {
        if (debug) {
            logger.debug("Initializing Resource Optimizer");
        }
        this.resUser = resUser;
        redo = false;
        logger.info("Initialization finished");
    }

    public void shutdown() {
        synchronized (alarmClock) {
            running = false;
            alarmClock.notify();
            cleanUp = true;
        }
    }

    public void run() {
        running = true;
        if (ResourceManager.useCloud()) {
            initialCreations();
        }

        WorkloadStatus workload;
        while (running) {
            try {
                do {
                    redo = false;
                    workload = resUser.getWorkload();
                    resourcesLogger.info(workload.toString());
                    
                    if (ResourceManager.useCloud()) {
                        applyPolicies(workload);
                    }
                } while (redo);

                try {
                    synchronized (alarmClock) {
                        if (running) {
                            alarmClock.wait(20000);
                        }
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

    public void cleanUp() {
        cleanUp = true;
    }

    /**
     * ********************************************************
     * ********************************************************
     * ********************************************************
     * ********************************************************
     * ********************************************************
     * ********************************************************
     * *********** INITIAL RESOURCES CREATION *****************
     * ********************************************************
     * ********************************************************
     * ********************************************************
     * ********************************************************
     * ********************************************************
     */
    private void initialCreations() {
        int alreadyCreated = addBasicNodes();
        //Distributes the rest of the VM
        addExtraNodes(alreadyCreated);
    }

    /**
     * Asks for the vm needed for the runtime to be able to execute all method
     * cores.
     *
     * First it groups the constraints of all the methods per Architecture and
     * tries to merge included resource descriptions in order to reduce the
     * amount of required VMs. It also tries to join the unassigned architecture
     * methods with the closer constraints of a defined one. After that it
     * distributes the Initial VM Count among the architectures taking into
     * account the number of methods that can be run in each architecture.
     *
     * If the amount of different constraints is higher than the Initial VM
     * count it applies an agressive merge method to each architecture in order
     * to fulfill the initial Constraint. It creates a single VM for each final
     * method constraint.
     *
     * Although these agressive merges, the amount of different constraints can
     * be higher than the initial VM Count constraint. In this case, it violates
     * the initial Vm constraint and asks for more resources.
     *
     * @return the amount of requested VM
     */
    public static int addBasicNodes() {
        int coreCount = CoreManager.getCoreCount();
        LinkedList<ConstraintsCore>[] unfulfilledConstraints = getUnfulfilledConstraints();
        int unfulfilledConstraintsCores = 0;
        for (int coreId = 0; coreId < coreCount; coreId++) {
            if (unfulfilledConstraints[coreId].size() > 0) {
                unfulfilledConstraintsCores += unfulfilledConstraints[coreId].size();
                break;
            }
        }
        if (unfulfilledConstraintsCores == 0) {
            return 0;
        }

        /*
         * constraintsPerArquitecture has loaded all constraint for each task.
         * architectures has a list of all the architecture names.
         *
         * e.g.
         * architectures                     constraintsPerArquitecture
         * Intel                =               |MR1|--|MR2|
         * AMD                  =               |MR3|
         * [unassigned]         =               |MR4|--|MR5|
         */
        HashMap<String, LinkedList<ConstraintsCore>> arch2Constraints = classifyArchitectures(unfulfilledConstraints);


        /*
         * Tries to reduce the number of machines per architecture by
         * entering constraints in another core's constraints
         *
         */
        reduceArchitecturesConstraints(arch2Constraints);


        /*
         * Checks if there are enough Vm for a Unassigned Arquitecture
         * If not it set each unassigned task into the architecture with the most similar task
         * e.g.
         * constraintsPerArquitecture
         * Intel --> |MR1|--|MR2|--|MR5|
         * AMD --> |MR3|--|MR4|
         *
         */
        reassignUnassignedConstraints(arch2Constraints);

        /*
         * Tries to reduce the number of machines per architecture by
         * entering constraints in another core's constraints
         *
         */
        reduceArchitecturesConstraints(arch2Constraints);

        int createdCount = 0;
        for (int coreId = 0; coreId < coreCount; coreId++) {
            while (!unfulfilledConstraints[coreId].isEmpty()) {
                ConstraintsCore cc = unfulfilledConstraints[coreId].removeFirst();
                cc.confirmed();
                ResourceCreationRequest rcr = CloudManager.askForResources(cc.desc, false);
                if (rcr != null) {
                    resourcesLogger.info("ORDER_CREATION = [\n\tTYPE = " + rcr.getRequested().getType() + "\n\tPROVIDER = " + rcr.getProvider() + "\n]");
                    if (debug) {
                        StringBuilder sb = new StringBuilder();
                        sb.append("EXPECTED_SIM_TASKS = [").append("\n");
                        for (int i = 0; i < rcr.requestedSimultaneousTaskCount().length; i++) {
                            for (int j = 0; j < rcr.requestedSimultaneousTaskCount()[i].length; ++j) {
                                sb.append("\t").append("IMPLEMENTATION_INFO = [").append("\n");
                                sb.append("\t").append("\t").append("COREID = ").append(i).append("\n");
                                sb.append("\t").append("\t").append("IMPLID = ").append(j).append("\n");
                                sb.append("\t").append("\t").append("SIM_TASKS = ").append(rcr.requestedSimultaneousTaskCount()[i][j]).append("\n");
                                sb.append("\t").append("]").append("\n");
                            }
                        }
                        sb.append("]");
                        resourcesLogger.debug(sb.toString());
                    }
                    createdCount++;
                }
            }
        }

        if (debug) {
            resourcesLogger.debug("DEBUG_MSG = [\n\tIn order to be able to execute all cores, Resource Manager has asked for " + createdCount + " Cloud resources\n]");
        }
        return createdCount;
    }

    private static class ConstraintsCore {

        CloudMethodResourceDescription desc;
        LinkedList<ConstraintsCore>[] cores;

        public ConstraintsCore(CloudMethodResourceDescription desc, int core, LinkedList<ConstraintsCore> coreList) {
            this.desc = desc;
            this.cores = new LinkedList[CoreManager.getCoreCount()];
            this.cores[core] = coreList;
        }

        public void join(ConstraintsCore c2) {
            desc.join(c2.desc);
            c2.desc = desc;
            for (int coreId = 0; coreId < CoreManager.getCoreCount(); coreId++) {
                if (cores[coreId] != null) {
                    if (c2.cores[coreId] != null) {
                        //Remove one instance of the list to avoid replication
                        cores[coreId].remove(c2);
                    } else {
                        c2.cores[coreId] = cores[coreId];
                    }
                } else {
                    cores[coreId] = c2.cores[coreId];
                }
            }
        }

        public void confirmed() {
            for (int coreId = 0; coreId < CoreManager.getCoreCount(); coreId++) {
                if (cores[coreId] != null) {
                    cores[coreId].clear();
                }
            }
        }

        public String toString() {
            LinkedList<Integer> cores = new LinkedList();
            for (int i = 0; i < CoreManager.getCoreCount(); i++) {
                if (this.cores[i] != null) {
                    cores.add(i);
                }
            }
            return desc.toString() + " compleix pels cores " + cores;
        }
    }

    //Removes from the list all the Constraints fullfilled by existing resources
    private static LinkedList<ConstraintsCore>[] getUnfulfilledConstraints() {
        int coreCount = CoreManager.getCoreCount();
        LinkedList<ConstraintsCore>[] unfulfilledConstraints = new LinkedList[coreCount];
        int[] maxSimTasks = ResourceManager.getTotalSlots();
        for (int coreId = 0; coreId < coreCount; coreId++) {
            unfulfilledConstraints[coreId] = new LinkedList<ConstraintsCore>();
            if (maxSimTasks[coreId] == 0) {
                Implementation[] impls = CoreManager.getCoreImplementations(coreId);
                for (int implId = 0; implId < impls.length; implId++) {
                    Implementation impl = impls[implId];
                    if (impl.getType() == Implementation.Type.METHOD) {
                        MethodResourceDescription requirements = (MethodResourceDescription) impl.getRequirements();
                        CloudMethodResourceDescription cd = new CloudMethodResourceDescription(requirements);
                        ConstraintsCore cc = new ConstraintsCore(cd, coreId, unfulfilledConstraints[coreId]);
                        unfulfilledConstraints[coreId].add(cc);
                    }
                }
            }
        }
        return unfulfilledConstraints;
    }

    //classifies the constraints  depending on their arquitecture and leaves it on coreResourceList
    //Return a list with all the Architectures Names
    private static HashMap<String, LinkedList<ConstraintsCore>> classifyArchitectures(LinkedList<ConstraintsCore>[] constraints) {
        HashMap<String, LinkedList<ConstraintsCore>> archs = new HashMap<String, LinkedList<ConstraintsCore>>();
        for (int coreId = 0; coreId < CoreManager.getCoreCount(); coreId++) {
            if (constraints[coreId] != null) {
                for (ConstraintsCore cc : constraints[coreId]) {
                    String arch = cc.desc.getProcessorArchitecture();
                    LinkedList<ConstraintsCore> archConstr = archs.get(arch);
                    if (archConstr == null) {
                        archConstr = new LinkedList<ConstraintsCore>();
                        archs.put(arch, archConstr);
                    }
                    archConstr.add(cc);
                }
            }
        }
        return archs;
    }

    private static void reduceArchitecturesConstraints(HashMap<String, LinkedList<ConstraintsCore>> arch2Ctrs) {
        for (LinkedList<ConstraintsCore> arch : arch2Ctrs.values()) {
            ConstraintsCore[] ctrs = new ConstraintsCore[arch.size()];
            int i = 0;
            for (ConstraintsCore cc : arch) {
                ctrs[i++] = cc;
            }

            Integer[] mergedTo = new Integer[arch.size()];
            for (i = 0; i < ctrs.length; i++) {
                if (mergedTo[i] != null) {
                    continue;
                }
                String OS = ctrs[i].desc.getOperatingSystemType();
                for (int j = i + 1; j < ctrs.length; j++) {
                    if (mergedTo[j] != null) {
                        continue;
                    }
                    if (OS.compareTo(ctrs[j].desc.getOperatingSystemType()) == 0
                            || OS.compareTo("[unassigned]") == 0
                            || ctrs[j].desc.getOperatingSystemType().compareTo("[unassigned]") == 0) {
                        mergedTo[j] = i;
                        ctrs[i].join(ctrs[j]);
                        arch.remove(ctrs[j]);
                    }
                }
            }
        }

    }

    private static void reassignUnassignedConstraints(HashMap<String, LinkedList<ConstraintsCore>> arch2Ctrs) {
        LinkedList<ConstraintsCore> unassignedList = arch2Ctrs.get("[unassigned]");
        if (unassignedList == null) {
            return;
        }
        if (arch2Ctrs.size() == 1) {
            return;
        }
        if (arch2Ctrs.size() == 2) {
            for (Map.Entry<String, LinkedList<ConstraintsCore>> ctrs : arch2Ctrs.entrySet()) {
                if (ctrs.getKey().compareTo("[unassigned]") == 0) {
                    continue;
                } else {
                    ctrs.getValue().addAll(unassignedList);
                    return;
                }
            }
        }

        LinkedList<ConstraintsCore> assignedList = new LinkedList<ConstraintsCore>();
        for (Map.Entry<String, LinkedList<ConstraintsCore>> ctrs : arch2Ctrs.entrySet()) {
            if (ctrs.getKey().compareTo("[unassigned]") == 0) {
                continue;
            } else {
                assignedList.addAll(ctrs.getValue());

            }
        }

        while (!unassignedList.isEmpty()) {
            ConstraintsCore unassigned = unassignedList.removeFirst();
            CloudMethodResourceDescription candidate = unassigned.desc;
            String bestArch = "";
            Float bestDifference = Float.MAX_VALUE;
            for (ConstraintsCore assigned : assignedList) {
                CloudMethodResourceDescription option = assigned.desc;
                float difference = candidate.difference(option);
                if (bestDifference < 0) {
                    if (difference < 0) {
                        if (difference > bestDifference) {
                            bestArch = option.getProcessorArchitecture();
                            bestDifference = difference;
                        }
                    }
                } else {
                    if (difference < bestDifference) {
                        bestArch = option.getProcessorArchitecture();
                        bestDifference = difference;
                    }
                }
            }
            unassigned.desc.setProcessorArchitecture(bestArch);
            arch2Ctrs.get(bestArch).add(unassigned);

        }
    }

    /**
     * Asks for the rest of VM that user wants to start with.
     *
     * After executing the addBasicNodes, it might happen that the number of
     * initial VMs constrained by the user is still not been fulfilled. The
     * addBasicNodes creates up to as much VMs as different methods. If the
     * initial VM Count is higher than this number of methods then there will be
     * still some VM requests missing.
     *
     * The addExtraNodes creates this difference of VMs. First it tries to merge
     * the method constraints that are included into another methods. And
     * performs a less aggressive and more equal distribution.
     *
     * @param alreadyCreated number of already requested VMs
     * @return the number of extra VMs created to fulfill the Initial VM Count
     * constraint
     */
    public static int addExtraNodes(int alreadyCreated) {
        String minVMs = ProjectManager.getCloudProperty("minVMCount");
        int minVMsCount = 0;
        if (minVMs != null) {
            minVMsCount = Integer.parseInt(minVMs);
        }
        String initialVMs = ProjectManager.getCloudProperty("InitialVMs");
        int initialVMsCount = 0;
        if (initialVMs != null) {
            initialVMsCount = Integer.parseInt(initialVMs);
        }

        //Check that initial VMs aren't lower than minVMs
        initialVMsCount = Math.max(minVMsCount, initialVMsCount);

        int vmCount = initialVMsCount - alreadyCreated;
        if (vmCount <= 0) {
            return 0;
        }
        if (debug) {
            resourcesLogger.debug("DEBUG_MSG = [\n\tALREADY_CREATED_INSTANCES = " + alreadyCreated + "\n\tMAXIMUM_NEW_PETITIONS = " + vmCount + "\n]");
        }

        /*
         * Tries to reduce the number of machines by
         * entering methodConstraints in another method's machine
         *
         */
        /*LinkedList<CloudWorkerDescription> requirements = new LinkedList<CloudWorkerDescription>();
         for (int coreId = 0; coreId < CoreManager.coreCount; coreId++) {
         Implementation impl = CoreManager.getCoreImplementations(coreId)[0];
         if (impl.getType() == Type.METHOD) {
         WorkerDescription wd = (WorkerDescription) impl.getRequirements();
         requirements.add(new CloudWorkerDescription(wd));
         }
         }
         if (requirements.size() == 0) {
         return 0;
         }
         requirements = reduceConstraints(requirements);

         int numTasks = requirements.size();
         int[] vmCountPerContraint = new int[numTasks];
         int[] coreCountPerConstraint = new int[numTasks];

         for (int index = 0; index < numTasks; index++) {
         vmCountPerContraint[index] = 1;
         coreCountPerConstraint[index] = requirements.get(index).getSlots();
         }

         for (int i = 0; i < vmCount; i++) {
         float millor = 0.0f;
         int opcio = 0;
         for (int j = 0; j < requirements.size(); j++) {
         if (millor < ((float) coreCountPerConstraint[j] / (float) vmCountPerContraint[j])) {
         opcio = j;
         millor = ((float) coreCountPerConstraint[j] / (float) vmCountPerContraint[j]);
         }
         }
         ResourceCreationRequest rcr = CloudManager.askForResources(requirements.get(opcio), false);

         logger.info("CREATION_ORDER = [\n\tTYPE = " + rcr.getRequested().getType() + "\n\tPROVIDER = " + rcr.getProvider() + "\n\tREASON = Fulfill the initial Cloud instances constraint\n]");
         if (debug) {
         StringBuilder sb = new StringBuilder("EXPECTED_INSTANCE_SIM_TASKS = [");
         int[][] simultaneousImpls = rcr.requestedSimultaneousTaskCount();
         for (int core = 0; core < simultaneousImpls.length; ++core) {
         int simultaneousTasks = 0;
         for (int j = 0; j < simultaneousImpls[core].length; ++j) {
         if (simultaneousTasks < simultaneousImpls[core][j]) {
         simultaneousTasks = simultaneousImpls[core][j];
         }
         }
         sb.append("\t").append("CORE = [").append("\n");
         sb.append("\t").append("\t").append("COREID = ").append(core).append("\n");
         sb.append("\t").append("\t").append("SIM_TASKS = ").append(simultaneousTasks).append("\n");
         sb.append("\t").append("]").append("\n");
         sb.append(", ").append(simultaneousTasks);
         }
         sb.append("]");
         logger.debug(sb.toString());
         }

         vmCountPerContraint[opcio]++;
         }
         */
        return vmCount;
    }

    /**
     * ********************************************************
     * ********************************************************
     * ********************************************************
     * ********************************************************
     * ********************************************************
     * ********************************************************
     * ************ DYNAMIC RESOURCES MANAGEMENT **************
     * ********************************************************
     * ********************************************************
     * ********************************************************
     * ********************************************************
     * ********************************************************
     */
    private void applyPolicies(WorkloadStatus workload) {
        int currentCloudVMCount = CloudManager.getCurrentVMCount();
        long creationTime;
        try {
            creationTime = CloudManager.getNextCreationTime();
        } catch (Exception ex) {
            creationTime = 120000l;
        }
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

        int coreCount = workload.getCoreCount();
        int noResourceCount = workload.getNoResourceCount();
        int[] noResourceCounts = workload.getNoResourceCounts();

        long[] minCoreTime = new long[coreCount];
        long[] meanCoreTime = new long[coreCount];
        long[] maxCoreTime = new long[coreCount];

        for (int coreId = 0; coreId < coreCount; coreId++) {
            minCoreTime[coreId] = Math.min(workload.getCoreMinTime(coreId), creationTime);
            meanCoreTime[coreId] = Math.min(workload.getCoreMeanTime(coreId), creationTime);
            maxCoreTime[coreId] = Math.min(workload.getCoreMaxTime(coreId), creationTime);
        }

        long[] readyMinCoreTime = new long[coreCount];
        long[] readyMeanCoreTime = new long[coreCount];
        long[] readyMaxCoreTime = new long[coreCount];
        long[] pendingMinCoreTime = new long[coreCount];
        long[] pendingMeanCoreTime = new long[coreCount];
        long[] pendingMaxCoreTime = new long[coreCount];

        int[] realSlots = ResourceManager.getAvailableSlots();
        int[] totalSlots = ResourceManager.getTotalSlots();

        int[] readyCounts = workload.getReadyTaskCounts();
        int[] pendingCounts = workload.getWaitingTaskCounts();

        for (int i = 0; i < coreCount; i++) {
            readyMinCoreTime[i] = minCoreTime[i] * readyCounts[i];
            pendingMinCoreTime[i] = readyMinCoreTime[i] + minCoreTime[i] * pendingCounts[i];
            readyMeanCoreTime[i] = meanCoreTime[i] * readyCounts[i];
            pendingMeanCoreTime[i] = readyMeanCoreTime[i] + meanCoreTime[i] * pendingCounts[i];
            readyMaxCoreTime[i] = maxCoreTime[i] * readyCounts[i];
            pendingMaxCoreTime[i] = readyMaxCoreTime[i] + maxCoreTime[i] * pendingCounts[i];
        }

        //Check if there is some mandatory creation/destruction
        if (!cleanUp) {
            //For CE without resources where to run
            LinkedList<Integer> requiredVMs = checkNeededMachines(noResourceCount, noResourceCounts, totalSlots);

            if (!requiredVMs.isEmpty()) {
                float[] creationRecommendations = recommendCreations(coreCount, creationTime, readyMinCoreTime, readyMeanCoreTime, readyMaxCoreTime, totalSlots, realSlots);
                for (Integer coreId : requiredVMs) {
                    //Ensure that we ask one slot for it
                    creationRecommendations[coreId] = Math.max(creationRecommendations[coreId], 1);
                }
                mandatoryIncrease(creationRecommendations, requiredVMs);
                return;
            }

            //For accomplishing the minimum amount of vms
            if (minNumberOfVMs != null && minNumberOfVMs > currentCloudVMCount) {
                float[] creationRecommendations = orderCreations(coreCount, creationTime, readyMinCoreTime, readyMeanCoreTime, readyMaxCoreTime, totalSlots, realSlots);
                mandatoryIncrease(creationRecommendations, new LinkedList());
                return;
            }
            //For not exceeding the VM top limit
            if (maxNumberOfVMs != null && maxNumberOfVMs < currentCloudVMCount) {
                float[] destroyRecommendations = deleteRecommendations(coreCount, creationTime, pendingMinCoreTime, pendingMeanCoreTime, pendingMaxCoreTime, totalSlots, realSlots);
                mandatoryReduction(destroyRecommendations);
                return;
            } else {
            }
        }

        //Check Recommended creations
        if (maxNumberOfVMs == null || maxNumberOfVMs > currentCloudVMCount) {
            float[] creationRecommendations = recommendCreations(coreCount, creationTime, readyMinCoreTime, readyMeanCoreTime, readyMaxCoreTime, totalSlots, realSlots);
            if (optionalIncrease(creationRecommendations)) {
                return;
            }
        }
        //Check Recommended creations
        if (minNumberOfVMs == null || minNumberOfVMs < currentCloudVMCount) {
            float[] destroyRecommendations = deleteRecommendations(coreCount, creationTime, pendingMinCoreTime, pendingMeanCoreTime, pendingMaxCoreTime, totalSlots, realSlots);
            if (optionalReduction(destroyRecommendations)) {
                return;
            }
        }
    }

    private void mandatoryIncrease(float[] creationRecommendations, LinkedList<Integer> requiredVMs) {
        ValueResourceDescription v;
        PriorityQueue<ValueResourceDescription> pq = new PriorityQueue<ValueResourceDescription>();
        boolean[] required = new boolean[creationRecommendations.length];
        for (int coreId : requiredVMs) {
            required[coreId] = true;
        }
        for (int coreId = 0; coreId < creationRecommendations.length; coreId++) {
            Implementation[] impls = CoreManager.getCoreImplementations(coreId);
            for (Implementation impl : impls) {
                if (impl.getType() == Implementation.Type.SERVICE) {
                    continue;
                }
                MethodResourceDescription contraints = ((MethodImplementation) impl).getRequirements();
                v = new ValueResourceDescription();
                v.constraints = contraints;
                v.value = creationRecommendations[coreId];
                v.prioritary = required[coreId];
                pq.add(v);
            }
        }
        requestOneCreation(pq, true);
    }

    private boolean optionalIncrease(float[] creationRecommendations) {
        ValueResourceDescription v;
        PriorityQueue<ValueResourceDescription> pq = new PriorityQueue<ValueResourceDescription>();

        for (int coreId = 0; coreId < creationRecommendations.length; coreId++) {
            if (creationRecommendations[coreId] > 1) {
                Implementation[] impls = CoreManager.getCoreImplementations(coreId);
                for (Implementation impl : impls) {
                    if (impl.getType() == Implementation.Type.SERVICE) {
                        continue;
                    }
                    MethodResourceDescription contraints = ((MethodImplementation) impl).getRequirements();
                    v = new ValueResourceDescription();
                    v.constraints = contraints;
                    v.value = creationRecommendations[coreId];
                    v.prioritary = false;
                    pq.add(v);
                }
            }
        }
        ResourceCreationRequest rcr = requestOneCreation(pq, false);
        return rcr != null;
    }

    private boolean optionalReduction(float[] destroyRecommendations) {
        LinkedList<CloudMethodWorker> nonCritical = trimReductionOptions(ResourceManager.getNonCriticalDynamicResources(), destroyRecommendations, false);
        Object[] nonCriticalSolution = CloudManager.getBestDestruction(nonCritical, destroyRecommendations);

        CloudMethodWorker res;
        float[] record;
        CloudMethodResourceDescription rd;
        int[][] slotsRemovingCount;

        if (nonCriticalSolution == null) {
            return false;
        }
        res = (CloudMethodWorker) nonCriticalSolution[0];
        record = (float[]) nonCriticalSolution[1];
        slotsRemovingCount = (int[][]) nonCriticalSolution[2];
        rd = (CloudMethodResourceDescription) nonCriticalSolution[3];

        if (record[1] > 0) {
            return false;
        } else {
            CloudMethodResourceDescription finalDescription = rd;
            finalDescription.setName(res.getName());
            CloudManager.destroyResources(res, finalDescription);
            return true;
        }
    }

    private void mandatoryReduction(float[] destroyRecommendations) {
        LinkedList<CloudMethodWorker> critical = trimReductionOptions(ResourceManager.getCriticalDynamicResources(), destroyRecommendations, false);
        //LinkedList<CloudMethodWorker> critical = checkCriticalSafeness (critical);
        LinkedList<CloudMethodWorker> nonCritical = trimReductionOptions(ResourceManager.getNonCriticalDynamicResources(), destroyRecommendations, false);
        Object[] criticalSolution = CloudManager.getBestDestruction(critical, destroyRecommendations);
        Object[] nonCriticalSolution = CloudManager.getBestDestruction(nonCritical, destroyRecommendations);

        boolean criticalIsBetter;
        if (criticalSolution == null) {
            if (nonCriticalSolution == null) {
                return;
            } else {
                criticalIsBetter = false;
            }
        } else {
            if (nonCriticalSolution == null) {
                criticalIsBetter = true;
            } else {
                criticalIsBetter = false;
                float[] noncriticalValues = (float[]) nonCriticalSolution[1];
                float[] criticalValues = (float[]) criticalSolution[1];

                if (noncriticalValues[0] == criticalValues[0]) {
                    if (noncriticalValues[1] == criticalValues[1]) {
                        if (noncriticalValues[2] < criticalValues[2]) {
                            criticalIsBetter = true;
                        }
                    } else {
                        if (noncriticalValues[1] > criticalValues[1]) {
                            criticalIsBetter = true;
                        }
                    }
                } else {
                    if (noncriticalValues[0] > criticalValues[0]) {
                        criticalIsBetter = true;
                    }
                }
            }
        }

        CloudMethodWorker res;
        float[] record;
        CloudMethodResourceDescription rd;
        int[][] slotsRemovingCount;

        if (criticalIsBetter) {
            res = (CloudMethodWorker) criticalSolution[0];
            record = (float[]) criticalSolution[1];
            slotsRemovingCount = (int[][]) criticalSolution[2];
            rd = (CloudMethodResourceDescription) criticalSolution[3];
        } else {
            if (nonCriticalSolution == null) {
                return;
            }
            res = (CloudMethodWorker) nonCriticalSolution[0];
            record = (float[]) nonCriticalSolution[1];
            slotsRemovingCount = (int[][]) nonCriticalSolution[2];
            rd = (CloudMethodResourceDescription) nonCriticalSolution[3];
        }
        CloudMethodResourceDescription finalDescription = new CloudMethodResourceDescription(rd);
        finalDescription.setName(res.getName());
        CloudManager.destroyResources(res, finalDescription);
    }

    private LinkedList<CloudMethodWorker> trimReductionOptions(Collection<CloudMethodWorker> options, float[] recommendations, boolean aggressive) {
        LinkedList<CloudMethodWorker> resources = new LinkedList<CloudMethodWorker>();
        Iterator<CloudMethodWorker> it = options.iterator();
        while (it.hasNext()) {
            CloudMethodWorker resource = it.next();
            boolean add = !aggressive;
            LinkedList<Integer> executableCores = resource.getExecutableCores();
            for (int coreId : executableCores) {
                if (!aggressive && recommendations[coreId] < 1) {
                    add = false;
                    break;
                }
                if (aggressive && recommendations[coreId] > 0) {
                    add = true;
                    break;
                }
            }
            if (add) {
                resources.add(resource);
            }
        }
        return resources;
    }

    private static ResourceCreationRequest requestOneCreation(PriorityQueue<ValueResourceDescription> pq, boolean include) {
        ValueResourceDescription v;
        while ((v = pq.poll()) != null) {
            ResourceCreationRequest rcr = CloudManager.askForResources(v.value < 1 ? 1 : (int) v.value, v.constraints, include);
            if (rcr != null) {
                resourcesLogger.info("ORDER_CREATION = [\n\tTYPE = " + rcr.getRequested().getType() + "\n\tPROVIDER = " + rcr.getProvider() + "\n]");
                if (debug) {
                    StringBuilder sb = new StringBuilder();
                    sb.append("EXPECTED_SIM_TASKS = [").append("\n");
                    for (int i = 0; i < CoreManager.getCoreCount(); i++) {
                        for (int j = 0; j < rcr.requestedSimultaneousTaskCount()[i].length; ++j) {
                            sb.append("\t").append("IMPLEMENTATION_INFO = [").append("\n");
                            sb.append("\t").append("\t").append("COREID = ").append(i).append("\n");
                            sb.append("\t").append("\t").append("IMPLID = ").append(j).append("\n");
                            sb.append("\t").append("\t").append("SIM_TASKS = ").append(rcr.requestedSimultaneousTaskCount()[i][j]).append("\n");
                            sb.append("\t").append("]").append("\n");
                        }
                    }
                    sb.append("]");
                    resourcesLogger.debug(sb.toString());
                }
                return rcr;
            }
        }

        return null;
    }

    /**
     * ********************************************************
     * ********************************************************
     * ********************************************************
     * ********************************************************
     * ********************************************************
     * ********************************************************
     * ********* DYNAMIC RESOURCES RECOMMENDATIONS ************
     * ********************************************************
     * ********************************************************
     * ********************************************************
     * ********************************************************
     * ********************************************************
     */
    private LinkedList<Integer> checkNeededMachines(int noResourceCount, int[] noResourceCountPerCore, int[] slotCountPerCore) {
        LinkedList<Integer> needed = new LinkedList<Integer>();
        if (noResourceCount == 0) {
            return needed;
        }
        for (int i = 0; i < CoreManager.getCoreCount(); i++) {
            if (noResourceCountPerCore[i] > 0 && slotCountPerCore[i] == 0) {
                needed.add(i);
            }
        }
        return needed;
    }

    private float[] recommendCreations(int coreCount, long creationTime, long[] aggregatedMinCoreTime, long[] aggregatedMeanCoreTime, long[] aggregatedMaxCoreTime, int[] totalSlots, int[] realSlots) {
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

    private float[] orderCreations(int coreCount, long creationTime, long[] aggregatedMinCoreTime, long[] aggregatedMeanCoreTime, long[] aggregatedMaxCoreTime, int[] totalSlots, int[] realSlots) {
        float[] creations = new float[coreCount];
        int maxI = 0;
        float maxRatio = 0;
        for (int i = 0; i < CoreManager.getCoreCount(); i++) {
            if (aggregatedMeanCoreTime[i] > 0 && totalSlots[i] > 0) {
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

    private float[] deleteRecommendations(int coreCount, long limitTime, long[] aggregatedMinCoreTime, long[] aggregatedMeanCoreTime, long[] aggregatedMaxCoreTime, int[] totalSlots, int[] realSlots) {

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

    private String recomendations(String title, float[] values) {
        StringBuilder sb = new StringBuilder(title);
        sb.append(" [");
        for (int i = 0; i < values.length; i++) {
            sb.append(", ").append(values[i]);
        }
        sb.append("]");
        return sb.toString();
    }

    private static class ValueResourceDescription implements Comparable<ValueResourceDescription> {

        boolean prioritary = false;
        float value;
        MethodResourceDescription constraints;

        @Override
        public int compareTo(ValueResourceDescription o) {
            if (this.prioritary && !o.prioritary) {
                return 1;
            }
            if (!this.prioritary && o.prioritary) {
                return -1;
            }
            float dif = value - o.value;
            if (dif > 0) {
                return 1;
            } else if (dif < 0) {
                return -1;
            } else {
                return 0;
            }
        }

        public String toString() {
            return value + (prioritary ? "!" : "") + constraints;
        }
    }
}
