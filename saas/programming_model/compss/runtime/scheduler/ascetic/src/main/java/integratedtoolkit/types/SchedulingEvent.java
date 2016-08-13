package integratedtoolkit.types;

import integratedtoolkit.scheduler.ascetic.AsceticResourceScheduler;
import integratedtoolkit.scheduler.ascetic.AsceticSchedulingInformation;
import integratedtoolkit.scheduler.types.AllocatableAction;
import integratedtoolkit.types.resources.ResourceDescription;
import integratedtoolkit.types.resources.WorkerResourceDescription;
import java.util.Comparator;
import java.util.ConcurrentModificationException;

import java.util.LinkedList;
import java.util.PriorityQueue;

public abstract class SchedulingEvent<P extends Profile, T extends WorkerResourceDescription> implements Comparable<SchedulingEvent<P, T>> {

    public long expectedTimeStamp;
    protected AllocatableAction<P, T> action;

    public SchedulingEvent(long timeStamp, AllocatableAction<P, T> action) {
        this.expectedTimeStamp = timeStamp;
        this.action = action;
    }

    @Override
    public int compareTo(SchedulingEvent<P, T> e) {
        int time = Long.compare(expectedTimeStamp, e.expectedTimeStamp);
        if (time == 0) {
            return (getPriority() - e.getPriority());
        }
        return time;
    }

    public AllocatableAction<P, T> getAction() {
        return action;
    }

    protected abstract int getPriority();

    public abstract LinkedList<SchedulingEvent<P, T>> process(
            LocalOptimizationState state,
            AsceticResourceScheduler<P, T> worker,
            PriorityQueue<AllocatableAction> readyActions,
            PriorityActionSet selectableActions,
            PriorityQueue<AllocatableAction> rescheduledActions
    );

    public static class Start<P extends Profile, T extends WorkerResourceDescription> extends SchedulingEvent<P, T> {

        public Start(long timeStamp, AllocatableAction<P, T> action) {
            super(timeStamp, action);
        }

        @Override
        protected int getPriority() {
            return 1;
        }

        public String toString() {
            return action + " start @ " + expectedTimeStamp;
        }

        @Override
        public LinkedList<SchedulingEvent<P, T>> process(
                LocalOptimizationState state,
                AsceticResourceScheduler<P, T> worker,
                PriorityQueue<AllocatableAction> readyActions,
                PriorityActionSet selectableActions,
                PriorityQueue<AllocatableAction> rescheduledActions
        ) {
            LinkedList<SchedulingEvent<P, T>> enabledEvents = new LinkedList<SchedulingEvent<P, T>>();
            AsceticSchedulingInformation<P, T> dsi = (AsceticSchedulingInformation<P, T>) action.getSchedulingInfo();

            //Set the expected Start time and endTime of the action
            dsi.setExpectedStart(expectedTimeStamp);
            long expectedEndTime = getExpectedEnd(action, worker, expectedTimeStamp);
            dsi.setExpectedEnd(expectedEndTime);
            //Add corresponding end event
            SchedulingEvent<P, T> endEvent = new End<P, T>(expectedEndTime, action);
            enabledEvents.add(endEvent);

            //Remove resources from the state and fill the gaps before its execution
            dsi.clearPredecessors();
            dsi.clearSuccessors();
            LinkedList<Gap> tmpGaps = state.reserveResources(action.getAssignedImplementation().getRequirements(), expectedTimeStamp);

            for (Gap tmpGap : tmpGaps) {
                AllocatableAction gapAction = tmpGap.getOrigin();
                if (tmpGap.getInitialTime() == tmpGap.getEndTime()) {
                    if (gapAction != null) {
                        AsceticSchedulingInformation gapActionDSI = (AsceticSchedulingInformation) gapAction.getSchedulingInfo();
                        gapActionDSI.addSuccessor(action);
                        dsi.addPredecessor(tmpGap);
                        //System.out.println(gapAction + "->" + action);
                        state.removeTmpGap(tmpGap);
                    }
                } else {
                    PriorityQueue<Gap> outGaps = fillGap(worker, tmpGap, readyActions, selectableActions, rescheduledActions, state);
                    for (Gap outGap : outGaps) {
                        AllocatableAction pred = outGap.getOrigin();
                        if (pred != null) {
                            AsceticSchedulingInformation predDSI = (AsceticSchedulingInformation) pred.getSchedulingInfo();
                            predDSI.addSuccessor(action);
                            dsi.addPredecessor(outGap);
                            //System.out.println(gapAction + "->" + action);
                        }
                        state.removeTmpGap(outGap);
                    }
                }
            }
            rescheduledActions.offer(action);
            return enabledEvents;
        }

        private PriorityQueue<Gap> fillGap(
                AsceticResourceScheduler<P, T> worker,
                Gap gap,
                PriorityQueue<AllocatableAction> readyActions,
                PriorityActionSet selectableActions,
                PriorityQueue<AllocatableAction> rescheduledActions,
                LocalOptimizationState state
        ) {
            //Find  selected action predecessors
            PriorityQueue<Gap> availableGaps = new PriorityQueue(1, new Comparator<Gap>() {
                @Override
                public int compare(Gap g1, Gap g2) {
                    return Long.compare(g1.getInitialTime(), g2.getInitialTime());
                }

            });

            AllocatableAction gapAction = pollActionForGap(gap, worker, selectableActions);

            if (gapAction != null) {
                //Compute method start
                AsceticSchedulingInformation gapActionDSI = (AsceticSchedulingInformation) gapAction.getSchedulingInfo();
                gapActionDSI.setToReschedule(false);
                long gapActionStart = Math.max(gapActionDSI.getExpectedStart(), gap.getInitialTime());

                //Fill previous gap space
                if (gap.getInitialTime() != gapActionStart) {
                    Gap previousGap = new Gap(gap.getInitialTime(), gapActionStart, gap.getOrigin(), gap.getResources(), 0);
                    state.replaceTmpGap(gap, previousGap);
                    availableGaps = fillGap(worker, previousGap, readyActions, selectableActions, rescheduledActions, state);
                } else {
                    availableGaps.add(gap);
                }

                gapActionDSI.lock();
                //Update Information
                gapActionDSI.setExpectedStart(gapActionStart);
                long expectedEnd = getExpectedEnd(gapAction, worker, gapActionStart);
                gapActionDSI.setExpectedEnd(expectedEnd);
                gapActionDSI.clearPredecessors();

                ResourceDescription desc = gapAction.getAssignedImplementation().getRequirements().copy();
                while (!desc.isDynamicUseless()) {
                    Gap peekGap = availableGaps.peek();
                    AllocatableAction peekAction = peekGap.getOrigin();
                    if (peekAction != null) {
                        AsceticSchedulingInformation predActionDSI = (AsceticSchedulingInformation) peekAction.getSchedulingInfo();
                        gapActionDSI.addPredecessor(peekGap);
                        predActionDSI.addSuccessor(gapAction);
                        //System.out.println(peekAction + "->" + gapAction);
                    }
                    ResourceDescription.reduceCommonDynamics(desc, peekGap.getResources());
                    if (peekGap.getResources().isDynamicUseless()) {
                        availableGaps.poll();
                        state.removeTmpGap(gap);
                    }
                }

                LinkedList<Gap> extendedGaps = new LinkedList();
                //Fill Concurrent 
                for (Gap g : availableGaps) {
                    Gap extendedGap = new Gap(g.getInitialTime(), gap.getEndTime(), g.getOrigin(), g.getResources(), g.getCapacity());
                    state.replaceTmpGap(extendedGap, gap);
                    extendedGaps.add(extendedGap);
                }

                availableGaps.clear();
                for (Gap eg : extendedGaps) {
                    availableGaps.addAll(fillGap(worker, eg, readyActions, selectableActions, rescheduledActions, state));
                }

                gapActionDSI.clearSuccessors();
                rescheduledActions.add(gapAction);

                gapActionDSI.setOnOptimization(false);
                //Release Data Successors
                releaseSuccessors(gapActionDSI, worker, readyActions, selectableActions, expectedEnd);

                //Fill Post action gap space
                Gap actionGap = new Gap(expectedEnd, gap.getEndTime(), gapAction, gapAction.getAssignedImplementation().getRequirements(), 0);
                state.addTmpGap(actionGap);
                availableGaps.addAll(fillGap(worker, actionGap, readyActions, selectableActions, rescheduledActions, state));
            } else {
                availableGaps.add(gap);
            }
            return availableGaps;
        }

        private long getExpectedEnd(AllocatableAction action, AsceticResourceScheduler worker, long expectedStart) {
            long theoreticalEnd;
            if (action.isToReleaseResources()) {
                Implementation<T> impl = action.getAssignedImplementation();
                Profile p = worker.getProfile(impl);
                long endTime = expectedStart;
                if (p != null) {
                    endTime += p.getAverageExecutionTime();
                }
                if (endTime < 0) {
                    endTime = 0;
                }
                theoreticalEnd = endTime;
            } else {
                theoreticalEnd = Long.MAX_VALUE;
            }
            if (theoreticalEnd < expectedStart) {
                return Long.MAX_VALUE;
            } else {
                return theoreticalEnd;
            }
        }

        private AllocatableAction pollActionForGap(Gap gap, AsceticResourceScheduler worker, PriorityActionSet selectableActions) {
            AllocatableAction gapAction = null;
            PriorityQueue<AllocatableAction> peeks = selectableActions.peekAll();
            //Get Main action to fill the gap
            while (!peeks.isEmpty() && gapAction == null) {
                AllocatableAction candidate = peeks.poll();
                //Check times
                AsceticSchedulingInformation candidateDSI = (AsceticSchedulingInformation) candidate.getSchedulingInfo();
                long start = candidateDSI.getExpectedStart();
                if (start > gap.getEndTime()) {
                    continue;
                }
                Implementation<T> impl = candidate.getAssignedImplementation();
                Profile p = worker.getProfile(impl);
                long expectedLength = p.getAverageExecutionTime();
                if ((gap.getEndTime() - gap.getInitialTime()) < expectedLength) {
                    continue;
                }
                if ((start + expectedLength) > gap.getEndTime()) {
                    continue;
                }

                //Check description
                if (gap.getResources().canHostDynamic(impl)) {
                    selectableActions.removeFirst(candidate.getCoreId());
                    gapAction = candidate;
                }
            }
            return gapAction;

        }
    }

    public static class End<P extends Profile, T extends WorkerResourceDescription> extends SchedulingEvent<P, T> {

        public End(long timeStamp, AllocatableAction<P, T> action) {
            super(timeStamp, action);
        }

        @Override
        protected int getPriority() {
            return 0;
        }

        @Override
        public LinkedList<SchedulingEvent<P, T>> process(
                LocalOptimizationState state,
                AsceticResourceScheduler<P, T> worker,
                PriorityQueue<AllocatableAction> readyActions,
                PriorityActionSet selectableActions,
                PriorityQueue<AllocatableAction> rescheduledActions
        ) {
            LinkedList<SchedulingEvent<P, T>> enabledEvents = new LinkedList<SchedulingEvent<P, T>>();
            AsceticSchedulingInformation dsi = (AsceticSchedulingInformation) action.getSchedulingInfo();
            dsi.setOnOptimization(false);

            //Move from readyActions to Ready
            while (readyActions.size() > 0) {
                AllocatableAction top = readyActions.peek();
                AsceticSchedulingInformation topDSI = (AsceticSchedulingInformation) top.getSchedulingInfo();
                long start = topDSI.getExpectedStart();
                if (start > expectedTimeStamp) {
                    break;
                }
                readyActions.poll();
                selectableActions.offer(top);
            }

            //Detect released Actions
            releaseSuccessors(dsi, worker, readyActions, selectableActions, expectedTimeStamp);

            //Get Top Action
            AllocatableAction currentTop = selectableActions.peek();

            if (state.getAction() != currentTop) {
                state.replaceAction(currentTop);
            }
            state.releaseResources(expectedTimeStamp, action);
            updateConsumptions(state, worker, action);

            while (state.canActionRun()) {
                selectableActions.removeFirst(currentTop.getCoreId());
                AsceticSchedulingInformation topDSI = (AsceticSchedulingInformation) currentTop.getSchedulingInfo();
                topDSI.lock();
                topDSI.setToReschedule(false);
                SchedulingEvent se = new Start(state.getActionStartTime(), currentTop);
                enabledEvents.addAll(se.process(state, worker, readyActions, selectableActions, rescheduledActions));

                currentTop = selectableActions.peek();
                state.replaceAction(currentTop);
            }
            return enabledEvents;
        }

        public String toString() {
            return action + " end @ " + expectedTimeStamp;
        }
    }

    private static void updateConsumptions(LocalOptimizationState state, AsceticResourceScheduler worker, AllocatableAction action) {
        Implementation impl = action.getAssignedImplementation();
        AsceticProfile p = (AsceticProfile) worker.getProfile(impl);
        if (p != null) {
            AsceticSchedulingInformation dsi = (AsceticSchedulingInformation) action.getSchedulingInfo();
            long length = dsi.getExpectedEnd() - (dsi.getExpectedStart() < 0 ? 0 : dsi.getExpectedStart());
            state.runImplementation(impl);
            state.consumeEnergy(p.getPower() * length);
            state.addCost(p.getPrice());
        }
    }

    public void releaseSuccessors(
            AsceticSchedulingInformation dsi,
            AsceticResourceScheduler worker,
            PriorityQueue<AllocatableAction> readyActions,
            PriorityActionSet selectableActions,
            long timeLimit) {

        LinkedList<AllocatableAction<P, T>> successors = dsi.getOptimizingSuccessors();
        for (AllocatableAction successor : successors) {
            AsceticSchedulingInformation<P, T> successorDSI = (AsceticSchedulingInformation<P, T>) successor.getSchedulingInfo();
            int missingParams = 0;
            long startTime = 0;
            boolean retry = true;
            while (retry) {
                try {
                    LinkedList<AllocatableAction> predecessors = successor.getDataPredecessors();
                    for (AllocatableAction predecessor : predecessors) {
                        AsceticSchedulingInformation predDSI = ((AsceticSchedulingInformation) predecessor.getSchedulingInfo());
                        if (predecessor.getAssignedResource() != worker) {
                            startTime = Math.max(startTime, predDSI.getExpectedEnd());
                        } else {
                            if (predDSI.isOnOptimization()) {
                                missingParams++;
                            } else {
                                startTime = Math.max(startTime, predDSI.getExpectedEnd());
                            }
                        }
                    }
                    retry = false;
                } catch (ConcurrentModificationException cme) {
                    missingParams = 0;
                    startTime = 0;
                }
            }
            successorDSI.setExpectedStart(startTime);
            if (missingParams == 0) {
                if (successorDSI.getExpectedStart() <= timeLimit) {
                    selectableActions.offer(successor);
                } else {
                    readyActions.add(successor);
                }
            }
        }
        dsi.clearOptimizingSuccessors();
    }
}
