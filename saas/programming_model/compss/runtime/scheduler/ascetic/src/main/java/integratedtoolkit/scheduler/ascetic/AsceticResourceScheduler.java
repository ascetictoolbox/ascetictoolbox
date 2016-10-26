package integratedtoolkit.scheduler.ascetic;

import integratedtoolkit.ascetic.Ascetic;
import integratedtoolkit.scheduler.exceptions.ActionNotFoundException;
import integratedtoolkit.scheduler.exceptions.BlockedActionException;
import integratedtoolkit.scheduler.exceptions.InvalidSchedulingException;
import integratedtoolkit.scheduler.exceptions.UnassignedActionException;
import integratedtoolkit.types.Gap;
import integratedtoolkit.scheduler.types.AllocatableAction;
import integratedtoolkit.types.AsceticProfile;
import integratedtoolkit.types.AsceticScore;
import integratedtoolkit.types.Implementation;
import integratedtoolkit.types.Profile;
import integratedtoolkit.types.SchedulingEvent;
import integratedtoolkit.types.LocalOptimizationState;
import integratedtoolkit.types.OptimizationAction;
import integratedtoolkit.types.Score;
import integratedtoolkit.types.TaskParams;
import integratedtoolkit.types.resources.ResourceDescription;
import integratedtoolkit.types.resources.Worker;
import integratedtoolkit.types.resources.WorkerResourceDescription;
import integratedtoolkit.util.CoreManager;
import integratedtoolkit.util.ResourceScheduler;
import java.util.Comparator;
import java.util.ConcurrentModificationException;
import java.util.HashSet;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.PriorityQueue;
import java.util.Set;

public class AsceticResourceScheduler<P extends Profile, T extends WorkerResourceDescription> extends ResourceScheduler<P, T> {

    public static final long DATA_TRANSFER_DELAY = 200;

    private final LinkedList<Gap> gaps;
    private double pendingActionsEnergy = 0;
    private double pendingActionsCost = 0;
    private int[][] implementationsCount;
    private int[][] runningImplementationsCount;
    private double runningActionsEnergy = 0;
    private double runningActionsCost = 0;

    private OptimizationAction opAction;
    private Set<AllocatableAction> pendingUnschedulings = new HashSet<AllocatableAction>();
    private AllocatableAction resourceBlockingAction = new OptimizationAction();
    private AllocatableAction dataBlockingAction = new OptimizationAction();
    private long expectedEndTimeRunning;

    public AsceticResourceScheduler(Worker<T> w) {
        super(w);
        gaps = new LinkedList<Gap>();
        addGap(new Gap(Long.MIN_VALUE, Long.MAX_VALUE, null, myWorker.getDescription().copy(), 0));
        implementationsCount = new int[CoreManager.getCoreCount()][];
        runningImplementationsCount = new int[CoreManager.getCoreCount()][];
        for (int coreId = 0; coreId < CoreManager.getCoreCount(); coreId++) {
            implementationsCount[coreId] = new int[CoreManager.getCoreImplementations(coreId).length];
            runningImplementationsCount[coreId] = new int[CoreManager.getCoreImplementations(coreId).length];
        }
        expectedEndTimeRunning = 0;
    }

    /*--------------------------------------------------
	 ---------------------------------------------------
	 ------------------ Score Methods ------------------
	 ---------------------------------------------------
	 --------------------------------------------------*/
    /**
     *
     * @param action
     * @param params
     * @param actionScore
     * @return
     */
    public Score getResourceScore(AllocatableAction<P, T> action, TaskParams params, Score actionScore) {
        long resScore = Score.getLocalityScore(params, myWorker);
        for (AllocatableAction pred : action.getDataPredecessors()) {
            if (pred.isPending() && pred.getAssignedResource() == this) {
                resScore++;
            }
        }
        resScore = params.getParameters().length - resScore;
        long lessTimeStamp = Long.MAX_VALUE;
        Gap g = gaps.peekFirst();
        if (g != null) {
            lessTimeStamp = g.getInitialTime();
            if (lessTimeStamp < 0) {
                lessTimeStamp = 0;
            }
        }
        return new AsceticScore<P, T>((AsceticScore<P, T>) actionScore, resScore * DATA_TRANSFER_DELAY, lessTimeStamp, 0, 0, 0);
    }

    /**
     *
     * @param action
     * @param params
     * @param impl
     * @param resourceScore
     * @return
     */
    public Score getImplementationScore(AllocatableAction<P, T> action, TaskParams params, Implementation<T> impl, Score resourceScore) {
        ResourceDescription rd = impl.getRequirements().copy();
        long resourceFreeTime = 0;
        try {
            for (Gap g : gaps) {
                rd.reduceDynamic(g.getResources());
                if (rd.isDynamicUseless()) {
                    resourceFreeTime = g.getInitialTime();
                    break;
                }
            }
        } catch (ConcurrentModificationException cme) {
            resourceFreeTime = 0;
        }
        if (resourceFreeTime < 0) {
            resourceFreeTime = 0;
        }
        long implScore;
        AsceticProfile p = (AsceticProfile) this.getProfile(impl);
        if (p != null) {
            implScore = p.getAverageExecutionTime();
        } else {
            implScore = 0;
        }
        double energy = p.getPower() * implScore;
        double cost = p.getPrice();
        //The data transfer penalty is already included on the datadependency time of the resourceScore
        return new AsceticScore<P, T>((AsceticScore<P, T>) resourceScore, 0, resourceFreeTime, implScore, energy, cost);
    }

    /*--------------------------------------------------
	 ---------------------------------------------------
	 ---------------- Scheduler Methods ----------------
	 ---------------------------------------------------
	 --------------------------------------------------*/
    @Override
    public void initialSchedule(AllocatableAction<P, T> action) {
        try {
            synchronized (gaps) {
                if (opAction != null) {
                    ((AsceticSchedulingInformation) opAction.getSchedulingInfo()).addSuccessor(action);
                    Gap opActionGap = new Gap(0, 0, opAction, null, 0);
                    ((AsceticSchedulingInformation) action.getSchedulingInfo()).addPredecessor(opActionGap);
                } else {
                    scheduleUsingGaps(action, gaps);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public LinkedList<AllocatableAction<P, T>> unscheduleAction(AllocatableAction<P, T> action) throws ActionNotFoundException {
        super.unscheduleAction(action);
        LinkedList<AllocatableAction<P, T>> freeActions = new LinkedList<AllocatableAction<P, T>>();

        AsceticSchedulingInformation<P, T> actionDSI = (AsceticSchedulingInformation<P, T>) action.getSchedulingInfo();
        LinkedList<Gap> resources = new LinkedList<>();

        //Block all predecessors
        for (Gap pGap : actionDSI.getPredecessors()) {
            AllocatableAction pred = pGap.getOrigin();
            AsceticSchedulingInformation predDSI = (AsceticSchedulingInformation<P, T>) pred.getSchedulingInfo();
            predDSI.lock();
        }
        //Block Action
        actionDSI.lock();

        if (!actionDSI.isScheduled() || action.getAssignedResource() != this) {
            for (Gap pGap : actionDSI.getPredecessors()) {
                AllocatableAction pred = pGap.getOrigin();
                AsceticSchedulingInformation predDSI = (AsceticSchedulingInformation<P, T>) pred.getSchedulingInfo();
                predDSI.unlock();
            }
            actionDSI.unscheduled();
            actionDSI.unlock();
            throw new ActionNotFoundException();
        }

        //For each predecessor consuming resources ->
        //  Register resources depending on a predecessor
        //  Remove the scheduling dependency on the predecessor
        for (Gap pGap : actionDSI.getPredecessors()) {
            AllocatableAction pred = pGap.getOrigin();
            if (!(pred instanceof OptimizationAction)) {
                resources.add(new Gap(pGap.getInitialTime(), Long.MAX_VALUE, pred, pGap.getResources().copy(), 0));
            }
            AsceticSchedulingInformation predDSI = (AsceticSchedulingInformation<P, T>) pred.getSchedulingInfo();
            predDSI.removeSuccessor(action);
        }
        //Remove all predecessors for 
        actionDSI.clearPredecessors();

        //Block all successors
        LinkedList<AsceticSchedulingInformation> successorsDSIs = new LinkedList<AsceticSchedulingInformation>();
        for (AllocatableAction<P, T> successor : actionDSI.getSuccessors()) {
            AsceticSchedulingInformation<P, T> succDSI = (AsceticSchedulingInformation<P, T>) successor.getSchedulingInfo();
            succDSI.lock();
            successorsDSIs.add(succDSI);
        }

        //For each successor look for the resources
        for (AllocatableAction<P, T> successor : actionDSI.getSuccessors()) {
            AsceticSchedulingInformation<P, T> succDSI = (AsceticSchedulingInformation<P, T>) successor.getSchedulingInfo();
            //Gets the resources that was supose to get from the task and remove the dependency
            Gap toCover = succDSI.removePredecessor(action);
            ResourceDescription resToCover = toCover.getResources();

            //Scans the resources related to the task to cover its requirements
            Iterator<Gap> gIt = resources.iterator();
            while (gIt.hasNext()) {
                Gap availableGap = gIt.next();
                //Takes the resources from a predecessor,
                ResourceDescription availableDesc = availableGap.getResources();
                ResourceDescription usedResources = ResourceDescription.reduceCommonDynamics(availableDesc, resToCover);

                //If it could take some of the resources -> adds a dependency
                //If all the resources from the predecessor are used -> removes from the list & unlock
                //If all the resources required for the successor are covered -> move to the next successor
                if (!usedResources.isDynamicUseless()) {
                    AllocatableAction availableOrigin = availableGap.getOrigin();
                    AsceticSchedulingInformation<P, T> availableDSI = null;
                    if (availableOrigin != null) {
                        availableDSI = (AsceticSchedulingInformation<P, T>) availableOrigin.getSchedulingInfo();
                        availableDSI.addSuccessor(successor);
                        succDSI.addPredecessor(new Gap(availableGap.getInitialTime(), Long.MAX_VALUE, availableOrigin, usedResources, 0));
                    }
                    if (availableDesc.isDynamicUseless()) {
                        gIt.remove();
                        if (availableDSI != null) {
                            availableDSI.unlock();
                        }
                    }
                    if (resToCover.isDynamicUseless()) {
                        break;
                    }
                }
            }

            if (succDSI.isExecutable()) {
                freeActions.add(successor);
            }
        }
        //Clear action's successors
        actionDSI.clearSuccessors();

        //Indicate that the task is fully unsheduled
        actionDSI.unscheduled();

        //Register those resources occupied by the task that haven't been used as free
        synchronized (gaps) {
            if (actionDSI.isOnOptimization()) {
                pendingUnschedulings.add(action);
            }
            Iterator<Gap> gIt = gaps.iterator();
            while (gIt.hasNext()) {
                Gap g = gIt.next();
                if (g.getOrigin() == action) {
                    gIt.remove();
                }
            }
            for (Gap newGap : resources) {
                AllocatableAction gapAction = newGap.getOrigin();
                addGap(newGap);
                if (gapAction != null) {
                    ((AsceticSchedulingInformation) gapAction.getSchedulingInfo()).unlock();
                }
            }
        }

        Implementation impl = action.getAssignedImplementation();
        AsceticProfile p = (AsceticProfile) getProfile(impl);
        if (p != null) {
            long length = actionDSI.getExpectedEnd() - (actionDSI.getExpectedStart() < 0 ? 0 : actionDSI.getExpectedStart());
            pendingActionsCost -= p.getPrice();
            pendingActionsEnergy -= p.getPower() * length;
        }
        actionDSI.unlock();
        for (AsceticSchedulingInformation successorsDSI : successorsDSIs) {
            successorsDSI.unlock();
        }
        return freeActions;
    }

    @Override
    public void clear() {
        super.clear();
        gaps.clear();
        addGap(new Gap(Long.MIN_VALUE, Long.MAX_VALUE, null, myWorker.getDescription().copy(), 0));
    }

    private void scheduleUsingGaps(AllocatableAction<P, T> action, LinkedList<Gap> gaps) {
        long expectedStart = 0;
        // Compute start time due to data dependencies
        for (AllocatableAction predecessor : action.getDataPredecessors()) {
            AsceticSchedulingInformation<P, T> predDSI = ((AsceticSchedulingInformation<P, T>) predecessor.getSchedulingInfo());
            if (predDSI.isScheduled()) {
                long predEnd = predDSI.getExpectedEnd();
                expectedStart = Math.max(expectedStart, predEnd);
            }
        }

        AsceticSchedulingInformation<P, T> schedInfo = (AsceticSchedulingInformation<P, T>) action.getSchedulingInfo();

        if (expectedStart == Long.MAX_VALUE) {
            //There is some data dependency with blocked tasks in some resource
            Gap opActionGap = new Gap(0, 0, dataBlockingAction, action.getAssignedImplementation().getRequirements().copy(), 0);
            AsceticSchedulingInformation dbaDSI = (AsceticSchedulingInformation) dataBlockingAction.getSchedulingInfo();
            dbaDSI.lock();
            schedInfo.lock();
            dbaDSI.addSuccessor(action);
            schedInfo.addPredecessor(opActionGap);
            schedInfo.setExpectedStart(Long.MAX_VALUE);
            schedInfo.setExpectedEnd(Long.MAX_VALUE);
            schedInfo.scheduled();
            dbaDSI.unlock();
            schedInfo.unlock();
            return;
        }

        Implementation<T> impl = action.getAssignedImplementation();
        AsceticProfile p = (AsceticProfile) getProfile(impl);
        ResourceDescription constraints = impl.getRequirements().copy();
        LinkedList<Gap> predecessors = new LinkedList();

        Iterator<Gap> gapIt = gaps.descendingIterator();
        boolean fullyCoveredReqs = false;
        // Compute predecessors and update gaps
        // Check gaps before data start
        while (gapIt.hasNext() && !fullyCoveredReqs) {
            Gap gap = gapIt.next();
            if (gap.getInitialTime() <= expectedStart) {
                useGap(gap, constraints, predecessors);
                fullyCoveredReqs = constraints.isDynamicUseless();
                if (gap.getResources().isDynamicUseless()) {
                    gapIt.remove();
                }
            }
        }
        // Check gaps after data start
        gapIt = gaps.iterator();
        while (gapIt.hasNext() && !fullyCoveredReqs) {
            Gap gap = gapIt.next();
            if (gap.getInitialTime() > expectedStart) {
                if (gap.getInitialTime() < Long.MAX_VALUE) {
                    useGap(gap, constraints, predecessors);
                    fullyCoveredReqs = constraints.isDynamicUseless();
                    if (gap.getResources().isDynamicUseless()) {
                        gapIt.remove();
                    }
                }
            }
        }

        if (!fullyCoveredReqs) {
            //Action gets blocked due to lack of resources
            for (Gap pGap : predecessors) {
                addGap(pGap);
                AllocatableAction<P, T> predecessor = (AllocatableAction<P, T>) pGap.getOrigin();
                AsceticSchedulingInformation<P, T> predDSI = ((AsceticSchedulingInformation<P, T>) predecessor.getSchedulingInfo());
                predDSI.unlock();
            }
            Gap opActionGap = new Gap(0, 0, resourceBlockingAction, action.getAssignedImplementation().getRequirements(), 0);
            AsceticSchedulingInformation rbaDSI = (AsceticSchedulingInformation) resourceBlockingAction.getSchedulingInfo();
            rbaDSI.lock();
            schedInfo.lock();
            rbaDSI.addSuccessor(action);
            schedInfo.addPredecessor(opActionGap);
            schedInfo.scheduled();
            schedInfo.setExpectedStart(Long.MAX_VALUE);
            schedInfo.setExpectedEnd(Long.MAX_VALUE);
            rbaDSI.unlock();
            schedInfo.unlock();
            return;
        }

        // Lock acces to the current task
        schedInfo.lock();
        schedInfo.scheduled();

        // Add dependencies
        // Unlock access to predecessor
        for (Gap pGap : predecessors) {
            AllocatableAction predecessor = pGap.getOrigin();
            AsceticSchedulingInformation<P, T> predDSI = ((AsceticSchedulingInformation<P, T>) predecessor.getSchedulingInfo());
            if (predDSI.isScheduled()) {
                long predEnd = predDSI.getExpectedEnd();
                expectedStart = Math.max(expectedStart, predEnd);
                predDSI.addSuccessor(action);
            }
            predDSI.unlock();
            schedInfo.addPredecessor(pGap);
        }

        //Compute end time
        schedInfo.setExpectedStart(expectedStart);
        long expectedEnd = expectedStart;
        if (p != null) {
            expectedEnd += p.getAverageExecutionTime();
            pendingActionsCost += p.getPrice();
            pendingActionsEnergy += p.getPower() * p.getAverageExecutionTime();
        }
        schedInfo.setExpectedEnd(expectedEnd);

        //Unlock access to current task
        schedInfo.unlock();
        if (action.isToReleaseResources()) {//Create new Gap correspondin to the resources released by the action
            addGap(new Gap(expectedEnd, Long.MAX_VALUE, action, impl.getRequirements().copy(), 0));
        } else {
            addGap(new Gap(Long.MAX_VALUE, Long.MAX_VALUE, action, impl.getRequirements().copy(), 0));
        }
    }

    private void useGap(Gap gap, ResourceDescription resources, LinkedList<Gap> predecessors) {
        AllocatableAction<P, T> predecessor = (AllocatableAction<P, T>) gap.getOrigin();
        ResourceDescription gapResource = gap.getResources();
        ResourceDescription usedResources = ResourceDescription.reduceCommonDynamics(gapResource, resources);
        if (predecessor != null && !usedResources.isDynamicUseless()) {
            AsceticSchedulingInformation<P, T> predDSI = ((AsceticSchedulingInformation<P, T>) predecessor.getSchedulingInfo());
            predDSI.lock();
            Gap g = new Gap(gap.getInitialTime(), Long.MAX_VALUE, predecessor, usedResources, 0);
            predecessors.add(g);
        }
    }

    /*--------------------------------------------------
	 ---------------------------------------------------
	 -------------- Optimization Methods ---------------
	 ---------------------------------------------------
	 --------------------------------------------------*/
    public PriorityQueue<AllocatableAction> localOptimization(
            long updateId,
            Comparator<AllocatableAction> selectionComparator,
            Comparator<AllocatableAction> donorComparator
    ) {
        System.out.println("Local Optimization for " + this.getName() + " starts");
        LocalOptimizationState state = new LocalOptimizationState(updateId, this, getReadyComparator(), selectionComparator);
        PriorityQueue<AllocatableAction> actions = new PriorityQueue<AllocatableAction>(1, donorComparator);

        synchronized (gaps) {
            opAction = new OptimizationAction();
        }
        //No changes in the Gap structure

        //Scan actions: Filters ready and selectable actions
        scanActions(state);
        //Gets all the pending schedulings
        LinkedList<AllocatableAction> newPendingSchedulings = new LinkedList();
        LinkedList<AllocatableAction> pendingSchedulings;
        synchronized (gaps) {
            AsceticSchedulingInformation opDSI = (AsceticSchedulingInformation) opAction.getSchedulingInfo();
            pendingSchedulings = opDSI.replaceSuccessors(newPendingSchedulings);
        }
        //Classify pending actions: Filters ready and selectable actions
        classifyPendingSchedulings(pendingSchedulings, state);
        classifyPendingUnschedulings(state);

        //ClassifyActions
        LinkedList<Gap> newGaps = rescheduleTasks(state, actions);
        System.out.println("\t is running: ");
        for (AllocatableAction aa : state.getRunningActions()) {
            System.out.println("\t\t" + aa + " with"
                    + " implementation " + ((aa.getAssignedImplementation() == null) ? "null" : aa.getAssignedImplementation().getImplementationId())
                    + " started " + ((aa.getStartTime() == null) ? "-" : (System.currentTimeMillis() - aa.getStartTime()))
            );

        }
        /*System.out.println(this.getName() + " has no resources for: ");
		for (AllocatableAction aa : this.resourceBlockedActions) {
			System.out.println("\t" + aa + " with"
					+ " implementation " + ((aa.getAssignedImplementation() == null) ? "null" : aa.getAssignedImplementation().getImplementationId())
			);
		}
		System.out.println(this.getName() + " will wait for data producers to be rescheduled for actions:");
		for (AllocatableAction aa : this.dataBlockedActions) {
			System.out.println("\t" + aa + " with"
					+ " implementation " + ((aa.getAssignedImplementation() == null) ? "null" : aa.getAssignedImplementation().getImplementationId())
			);
		}*/

        //Schedules all the pending scheduligns and unblocks the scheduling of new actions
        synchronized (gaps) {
            gaps.clear();
            gaps.addAll(newGaps);
            AsceticSchedulingInformation opDSI = (AsceticSchedulingInformation) opAction.getSchedulingInfo();
            LinkedList<AllocatableAction> successors = opDSI.getSuccessors();
            for (AllocatableAction action : successors) {
                actions.add(action);
                AsceticSchedulingInformation actionDSI = (AsceticSchedulingInformation) action.getSchedulingInfo();
                actionDSI.lock();
                actionDSI.removePredecessor(opAction);
                this.scheduleUsingGaps(action, gaps);
                actionDSI.unlock();
            }
            opDSI.clearSuccessors();
            opAction = null;
        }
        System.out.println("Local optimization for " + this.getName() + " ends");
        return actions;
    }

    // Classifies actions according to their start times. Selectable actions are
    // those that can be selected to run from t=0. Ready actions are those actions
    // that have data dependencies with tasks scheduled in other nodes. Actions 
    // with dependencies with actions scheduled in the same node, are not 
    // classified in any list since we cannot know the start time.
    public LinkedList<AllocatableAction> scanActions(LocalOptimizationState state) {
        LinkedList<AllocatableAction> runningActions = new LinkedList<AllocatableAction>();
        PriorityQueue<AllocatableAction> actions = new PriorityQueue<AllocatableAction>(1, getScanComparator());
        AsceticSchedulingInformation blockSI = (AsceticSchedulingInformation) dataBlockingAction.getSchedulingInfo();
        LinkedList<AllocatableAction> blockActions = blockSI.getSuccessors();
        for (AllocatableAction gapAction : blockActions) {
            AsceticSchedulingInformation dsi = (AsceticSchedulingInformation) gapAction.getSchedulingInfo();
            dsi.lock();
            dsi.setOnOptimization(true);
            actions.add(gapAction);
        }

        blockSI = (AsceticSchedulingInformation) resourceBlockingAction.getSchedulingInfo();
        blockActions = blockSI.getSuccessors();
        for (AllocatableAction gapAction : blockActions) {
            AsceticSchedulingInformation dsi = (AsceticSchedulingInformation) gapAction.getSchedulingInfo();
            dsi.lock();
            dsi.setOnOptimization(true);
            actions.add(gapAction);
        }

        for (Gap g : gaps) {
            AllocatableAction gapAction = g.getOrigin();
            if (gapAction != null) {
                AsceticSchedulingInformation dsi = (AsceticSchedulingInformation) gapAction.getSchedulingInfo();
                dsi.lock();
                dsi.setOnOptimization(true);
                actions.add(gapAction);
            }
        }

        AllocatableAction action;
        while ((action = actions.poll()) != null) {
            AsceticSchedulingInformation actionDSI = (AsceticSchedulingInformation) action.getSchedulingInfo();
            if (!actionDSI.isScheduled()) {
                actionDSI.unlock();
                //Task was already executed. Ignore
                continue;
            }

            //Data Dependencies analysis
            boolean hasInternal = false;
            boolean hasExternal = false;
            long startTime = 0;
            try {
                LinkedList<AllocatableAction> dPreds = action.getDataPredecessors();
                for (AllocatableAction dPred : dPreds) {
                    AsceticSchedulingInformation dPredDSI = (AsceticSchedulingInformation) dPred.getSchedulingInfo();
                    if (dPred.getAssignedResource() == this) {
                        if (dPredDSI.tryToLock()) {
                            if (dPredDSI.isScheduled()) {
                                hasInternal = true;
                                dPredDSI.optimizingSuccessor(action);
                            }
                            dPredDSI.unlock();
                        }
                        //else 
                        //The predecessor is trying to be unscheduled but it is
                        //blocked by another successor reschedule. 
                    } else {
                        hasExternal = true;
                        startTime = Math.max(startTime, dPredDSI.getExpectedEnd());
                    }
                }
            } catch (ConcurrentModificationException cme) {
                hasInternal = false;
                hasExternal = false;
                startTime = 0;
            }

            //Resource Dependencies analysis
            boolean hasResourcePredecessors = false;
            LinkedList<Gap> rPredGaps = actionDSI.getPredecessors();
            for (Gap rPredGap : rPredGaps) {
                AllocatableAction rPred = rPredGap.getOrigin();
                AsceticSchedulingInformation rPredDSI = (AsceticSchedulingInformation) rPred.getSchedulingInfo();
                if (rPredDSI.tryToLock()) {
                    if (rPredDSI.isScheduled()) {
                        hasResourcePredecessors = true;
                        if (!rPredDSI.isOnOptimization()) {
                            rPredDSI.setOnOptimization(true);
                            actions.add(rPred);
                        } else {
                            rPredDSI.unlock();
                        }
                    } else {
                        rPredDSI.unlock();
                    }
                }
                //else the predecessor was already executed
            }
            actionDSI.setExpectedStart(startTime);
            actionDSI.setToReschedule(true);
            state.classifyAction(action, hasInternal, hasExternal, hasResourcePredecessors, startTime);
            if (hasResourcePredecessors || hasInternal) {
                //The action has a blocked predecessor in the resource that will block its execution
                actionDSI.unlock();
            }
        }
        return runningActions;
    }

    public void classifyPendingSchedulings(LinkedList<AllocatableAction> pendingSchedulings, LocalOptimizationState state) {
        for (AllocatableAction action : pendingSchedulings) {
            //Action has an artificial resource dependency with the opAction
            AsceticSchedulingInformation actionDSI = (AsceticSchedulingInformation) action.getSchedulingInfo();
            actionDSI.scheduled();
            actionDSI.setOnOptimization(true);
            actionDSI.setToReschedule(true);
            //Data Dependencies analysis
            boolean hasInternal = false;
            boolean hasExternal = false;
            long startTime = 0;
            try {
                LinkedList<AllocatableAction> dPreds = action.getDataPredecessors();
                for (AllocatableAction dPred : dPreds) {
                    AsceticSchedulingInformation dPredDSI = (AsceticSchedulingInformation) dPred.getSchedulingInfo();
                    if (dPred.getAssignedResource() == this) {
                        if (dPredDSI.tryToLock()) {
                            if (dPredDSI.isScheduled()) {
                                hasInternal = true;
                                dPredDSI.optimizingSuccessor(action);
                            }
                            dPredDSI.unlock();
                        }
                        //else 
                        //The predecessor is trying to be unscheduled but it is
                        //blocked by another successor reschedule. 
                    } else {
                        hasExternal = true;
                        startTime = Math.max(startTime, dPredDSI.getExpectedEnd());
                    }
                }
            } catch (ConcurrentModificationException cme) {
                hasInternal = false;
                hasExternal = false;
                startTime = 0;
            }

            actionDSI.setExpectedStart(startTime);
            state.classifyAction(action, hasInternal, hasExternal, true, startTime);
        }
    }

    public void classifyPendingUnschedulings(LocalOptimizationState state) {
        for (AllocatableAction unscheduledAction : pendingUnschedulings) {
            AsceticSchedulingInformation actionDSI = (AsceticSchedulingInformation) unscheduledAction.getSchedulingInfo();
            LinkedList<AllocatableAction> successors = actionDSI.getOptimizingSuccessors();
            for (AllocatableAction successor : successors) {
                //Data Dependencies analysis
                boolean hasInternal = false;
                boolean hasExternal = false;
                long startTime = 0;
                try {
                    LinkedList<AllocatableAction> dPreds = successor.getDataPredecessors();
                    for (AllocatableAction dPred : dPreds) {
                        AsceticSchedulingInformation dPredDSI = (AsceticSchedulingInformation) dPred.getSchedulingInfo();
                        if (dPred.getAssignedResource() == this) {
                            if (dPredDSI.tryToLock()) {
                                if (dPredDSI.isScheduled()) {
                                    hasInternal = true;
                                    dPredDSI.optimizingSuccessor(successor);
                                }
                                dPredDSI.unlock();
                            }
                            //else 
                            //The predecessor is trying to be unscheduled but it is
                            //blocked by another successor reschedule. 
                        } else {
                            hasExternal = true;
                            startTime = Math.max(startTime, dPredDSI.getExpectedEnd());
                        }
                    }
                } catch (ConcurrentModificationException cme) {
                    hasInternal = false;
                    hasExternal = false;
                    startTime = 0;
                }

                actionDSI.setExpectedStart(startTime);
                state.classifyAction(successor, hasInternal, hasExternal, true, startTime);
            }
        }
        pendingUnschedulings.clear();
    }

    public LinkedList<Gap> rescheduleTasks(
            LocalOptimizationState state,
            PriorityQueue<AllocatableAction> rescheduledActions
    ) {
        /*
		 * 
		 * ReadyActions contains those actions that have no dependencies with
		 * other actions scheduled on the node, but they have data dependencies
		 * with tasks on other resources. They are sorted by the expected time 
		 * when these dependencies will be solved.
		 *
		 * SelectableActions contains those actions that have no data dependencies
		 * with other actions but they wait for resources to be released.
		 * 
		 * Running actions contains a list of Actions that are executing or 
		 * potentially executing at the moment.
		 * 
		 * All Actions that need to be rescheduled have the onOptimization and
		 * scheduled flags on.
		 * 
		 * Those actions that are running or could potentially be started ( no 
		 * dependencies with other actions in the resource) are already locked 
		 * to avoid their start without being on the runningActions set.
         */
        Gap gap = state.peekFirstGap();
        ResourceDescription gapResource = gap.getResources();
        PriorityQueue<SchedulingEvent<P, T>> schedulingQueue = new PriorityQueue<SchedulingEvent<P, T>>();
        //For every running action we create a start event on their real start timeStamp
        for (AllocatableAction action : state.getRunningActions()) {
            manageRunningAction(action, state);
            AsceticSchedulingInformation actionDSI = (AsceticSchedulingInformation) action.getSchedulingInfo();
            schedulingQueue.offer(new SchedulingEvent.End<P, T>(actionDSI.getExpectedEnd(), action));
        }
        while (state.areRunnableActions() && !gapResource.isDynamicUseless()) {
            AllocatableAction top = state.getMostPrioritaryRunnableAction();
            state.replaceAction(top);
            if (state.canActionRun()) {
                state.removeMostPrioritaryRunnableAction();
                //Start the current action
                AsceticSchedulingInformation topDSI = (AsceticSchedulingInformation) top.getSchedulingInfo();
                topDSI.lock();
                topDSI.clearPredecessors();
                manageRunningAction(top, state);
                if (tryToLaunch(top)) {
                    schedulingQueue.offer(new SchedulingEvent.End<P, T>(topDSI.getExpectedEnd(), top));
                }
            } else {
                break;
            }
        }

        while (!schedulingQueue.isEmpty() || state.areActionsToBeRescheduled()) {
            // We reschedule as many tasks as possible by processing start and end SchedulingEvents

            while (!schedulingQueue.isEmpty()) {
                SchedulingEvent<P, T> e = schedulingQueue.poll();
                /*
				 * Start Event:
				 *  - sets the expected start and end times
				 *  - adds resource dependencies with the previous actions
				 *  - if there's a gap before the dependency 
				 *      -tries to fill it with other tasks
				 *  - if all the resources released by the predecessor are used later
				 *      - the action is unlocked
				 *
				 * End Event:
				 * 
                 */
                LinkedList<SchedulingEvent<P, T>> result = e.process(state, this, rescheduledActions);
                for (SchedulingEvent<P, T> r : result) {
                    schedulingQueue.offer(r);
                }
            }

            if (state.areActionsToBeRescheduled()) {
                AllocatableAction topAction = state.getEarliestActionToBeRescheduled();
                AsceticSchedulingInformation topActionDSI = (AsceticSchedulingInformation) topAction.getSchedulingInfo();
                topActionDSI.lock();
                topActionDSI.setToReschedule(false);
                schedulingQueue.offer(new SchedulingEvent.Start<P, T>(topActionDSI.getExpectedStart(), topAction));
            }
        }

        for (Gap g : state.getGaps()) {
            state.removeTmpGap(g);
        }
        this.pendingActionsCost = state.getTotalCost();
        this.pendingActionsEnergy = state.getTotalEnergy();
        this.implementationsCount = state.getImplementationsCount();
        this.expectedEndTimeRunning = state.getEndRunningTime();
        this.runningImplementationsCount = state.getRunningImplementations();
        this.runningActionsEnergy = state.getRunningEnergy();
        this.runningActionsCost = state.getRunningCost();
        this.resourceBlockingAction = state.getResourceBlockingAction();
        this.dataBlockingAction = state.getDataBlockingAction();

        return state.getGaps();
    }

    private void manageRunningAction(
            AllocatableAction action,
            LocalOptimizationState state
    ) {

        Implementation impl = action.getAssignedImplementation();
        AsceticSchedulingInformation actionDSI = (AsceticSchedulingInformation) action.getSchedulingInfo();

        //Set start Time
        Long startTime = action.getStartTime();
        long start;
        if (startTime != null) {
            start = startTime - state.getId();
        } else {
            start = 0;
        }
        actionDSI.setExpectedStart(start);

        //Set End  Time
        AsceticProfile p = (AsceticProfile) getProfile(impl);
        long endTime = start;
        if (p != null) {
            endTime += p.getAverageExecutionTime();
        }
        if (endTime < 0) {
            endTime = 0;
        }
        actionDSI.setExpectedEnd(endTime);

        actionDSI.clearPredecessors();
        actionDSI.clearSuccessors();
        actionDSI.setToReschedule(false);
        state.runningAction(impl, p, endTime);
    }

    private boolean tryToLaunch(AllocatableAction action) {
        try {
            action.tryToLaunch();
            return true;
        } catch (InvalidSchedulingException ise) {
            ise.printStackTrace();
            try {
                long actionScore = AsceticScore.getActionScore(action);
                long dataTime = (new AsceticScore(0, 0, 0, 0, 0, 0)).getDataPredecessorTime(action.getDataPredecessors());
                Score aScore = new AsceticScore(actionScore, dataTime, 0, 0, 0, 0);
                action.schedule(action.getConstrainingPredecessor().getAssignedResource(), aScore);
                try {
                    action.tryToLaunch();
                } catch (InvalidSchedulingException ise2) {
                    //Impossible exception.
                    ise2.printStackTrace();
                }
            } catch (BlockedActionException | UnassignedActionException be) {
                //Can not happen since there was an original source
                be.printStackTrace();
            }
        }
        return false;
    }

    public static final Comparator getScanComparator() {
        return new Comparator<AllocatableAction>() {
            @Override
            public int compare(AllocatableAction action1, AllocatableAction action2) {
                AsceticSchedulingInformation action1DSI = (AsceticSchedulingInformation) action1.getSchedulingInfo();
                AsceticSchedulingInformation action2DSI = (AsceticSchedulingInformation) action2.getSchedulingInfo();
                int compare = Long.compare(action2DSI.getExpectedStart(), action1DSI.getExpectedStart());
                if (compare == 0) {
                    return Long.compare(action2.getId(), action1.getId());
                }
                return compare;
            }
        };
    }

    public static final Comparator getReadyComparator() {
        return new Comparator<AllocatableAction>() {
            @Override
            public int compare(AllocatableAction action1, AllocatableAction action2) {
                AsceticSchedulingInformation action1DSI = (AsceticSchedulingInformation) action1.getSchedulingInfo();
                AsceticSchedulingInformation action2DSI = (AsceticSchedulingInformation) action2.getSchedulingInfo();
                int compare = Long.compare(action1DSI.getExpectedStart(), action2DSI.getExpectedStart());
                if (compare == 0) {
                    return Long.compare(action1.getId(), action2.getId());
                }
                return compare;
            }
        };
    }

    private void addGap(Gap g) {

        AllocatableAction gapAction = g.getOrigin();
        ResourceDescription releasedResources = g.getResources();
        boolean merged = false;
        for (Gap registeredGap : gaps) {
            if (registeredGap.getOrigin() == gapAction) {
                ResourceDescription registeredResources = registeredGap.getResources();
                registeredResources.increaseDynamic(releasedResources);
                merged = true;
                break;
            }
        }
        if (!merged) {
            Iterator<Gap> gapIt = gaps.iterator();
            int index = 0;
            Gap gap;
            while (gapIt.hasNext()
                    && (gap = gapIt.next()) != null
                    && gap.getInitialTime() <= g.getInitialTime()) {
                index++;
            }
            gaps.add(index, g);
        }
    }

    public long getFirstGapExpectedStart() {
        Gap g = gaps.peekFirst();
        if (g == null) {
            return 0;
        }
        return g.getInitialTime();
    }

    public long getLastGapExpectedStart() {
        Gap g = gaps.peekLast();
        if (g == null) {
            return 0;
        }
        return g.getInitialTime();
    }

    @Override
    protected Profile[][] loadProfiles() {
        Profile[][] profiles;
        int coreCount = CoreManager.getCoreCount();
        profiles = new Profile[coreCount][];
        for (int coreId = 0; coreId < coreCount; ++coreId) {
            Implementation[] impls = CoreManager.getCoreImplementations(coreId);
            int implCount = impls.length;
            profiles[coreId] = new Profile[implCount];
            for (Implementation impl : impls) {
                profiles[coreId][impl.getImplementationId()] = new AsceticProfile(myWorker, impl);
            }
        }
        return profiles;
    }

    @Override
    public P generateProfileForAllocatable(AllocatableAction action) {
        return (P) new AsceticProfile(myWorker, action.getAssignedImplementation(), action);
    }

    public double getIdlePower() {
        return Ascetic.getPower(myWorker);
    }

    public double getRunningActionsEnergy() {
        return this.runningActionsEnergy;
    }

    public double getScheduledActionsEnergy() {
        return this.pendingActionsEnergy;
    }

    public double getIdlePrice() {
        return Ascetic.getPrice(myWorker);
    }

    public double getRunningActionsCost() {
        return this.runningActionsCost;
    }

    public double getActionsCost() {
        return this.pendingActionsCost;
    }

    public int getSimultaneousCapacity(Implementation impl) {
        return myWorker.fitCount(impl);
    }

    public int[][] getImplementationCounts() {
        return this.implementationsCount;
    }

    public long getExpectedEndTimeRunning() {
        return this.expectedEndTimeRunning;
    }

    public int[][] getRunningImplementationCounts() {
        return this.runningImplementationsCount;
    }

    public LinkedList<AllocatableAction> getAllBlockedActions() {
        LinkedList<AllocatableAction> blockedActions = new LinkedList();
        blockedActions.addAll(super.getBlockedActions());

        AsceticSchedulingInformation baDSI = (AsceticSchedulingInformation) dataBlockingAction.getSchedulingInfo();
        baDSI.lock();
        blockedActions.addAll(baDSI.getSuccessors());
        baDSI.unlock();

        baDSI = (AsceticSchedulingInformation) resourceBlockingAction.getSchedulingInfo();
        baDSI.lock();
        blockedActions.addAll(baDSI.getSuccessors());
        baDSI.unlock();

        return blockedActions;
    }

}
