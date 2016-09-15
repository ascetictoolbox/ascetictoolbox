package integratedtoolkit.types;

import integratedtoolkit.scheduler.ascetic.AsceticResourceScheduler;
import integratedtoolkit.scheduler.ascetic.AsceticSchedulingInformation;
import integratedtoolkit.scheduler.types.AllocatableAction;
import integratedtoolkit.types.resources.ResourceDescription;
import integratedtoolkit.types.resources.WorkerResourceDescription;
import java.util.Comparator;

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
                    PriorityQueue<Gap> outGaps = fillGap(worker, tmpGap, rescheduledActions, state);
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
                PriorityQueue<AllocatableAction> rescheduledActions,
                LocalOptimizationState state
        ) {
        	System.out.println("Filling Gaps in " + worker.getName() +" with gap: " + gap.toString());
            //Find  selected action predecessors
            PriorityQueue<Gap> availableGaps = new PriorityQueue(1, new Comparator<Gap>() {
                @Override
                public int compare(Gap g1, Gap g2) {
                    return Long.compare(g1.getInitialTime(), g2.getInitialTime());
                }

            });

            AllocatableAction gapAction = state.pollActionForGap(gap);

            if (gapAction != null) {
                //Compute method start
                AsceticSchedulingInformation gapActionDSI = (AsceticSchedulingInformation) gapAction.getSchedulingInfo();
                gapActionDSI.setToReschedule(false);
                long gapActionStart = Math.max(gapActionDSI.getExpectedStart(), gap.getInitialTime());

                //Fill previous gap space
                if (gap.getInitialTime() != gapActionStart) {
                    Gap previousGap = new Gap(gap.getInitialTime(), gapActionStart, gap.getOrigin(), gap.getResources(), 0);
                    state.replaceTmpGap(gap, previousGap);
                    availableGaps = fillGap(worker, previousGap, rescheduledActions, state);
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
                    if (peekGap!= null){
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
                    }else{
                    	System.out.println("****** Peek in availableGaps return a null");
                    	//I have added this if not if remains in the while
                    	break;
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
                    availableGaps.addAll(fillGap(worker, eg, rescheduledActions, state));
                }

                gapActionDSI.clearSuccessors();
                rescheduledActions.add(gapAction);

                gapActionDSI.setOnOptimization(false);
                //Release Data Successors
                state.releaseDataSuccessors(gapActionDSI, expectedEnd);

                //Fill Post action gap space
                Gap actionGap = new Gap(expectedEnd, gap.getEndTime(), gapAction, gapAction.getAssignedImplementation().getRequirements(), 0);
                state.addTmpGap(actionGap);
                availableGaps.addAll(fillGap(worker, actionGap, rescheduledActions, state));
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
                PriorityQueue<AllocatableAction> rescheduledActions
        ) {
            System.out.println(this);
            LinkedList<SchedulingEvent<P, T>> enabledEvents = new LinkedList<SchedulingEvent<P, T>>();
            AsceticSchedulingInformation dsi = (AsceticSchedulingInformation) action.getSchedulingInfo();
            dsi.setOnOptimization(false);

            //Move from readyActions to selectable
            state.progressOnTime(expectedTimeStamp);

            //Detect released Actions
            state.releaseDataSuccessors(dsi, expectedTimeStamp);

            //Get Top Action
            AllocatableAction currentTop = state.getMostPrioritaryRunnableAction();
            if (state.getAction() != currentTop) {
                state.replaceAction(currentTop);
            }
            state.releaseResources(expectedTimeStamp, action);
            state.updateConsumptions(action);

            while (state.canActionRun()) {
                state.removeMostPrioritaryRunnableAction(currentTop.getCoreId());
                AsceticSchedulingInformation topDSI = (AsceticSchedulingInformation) currentTop.getSchedulingInfo();
                topDSI.lock();
                topDSI.setToReschedule(false);
                if (action.isToReleaseResources()) {
                    SchedulingEvent se = new Start(state.getActionStartTime(), currentTop);
                    enabledEvents.addAll(se.process(state, worker, rescheduledActions));
                } else {
                    SchedulingEvent se = new ResourceBlocked(state.getActionStartTime(), currentTop);
                    enabledEvents.addAll(se.process(state, worker, rescheduledActions));
                }

                currentTop = state.getMostPrioritaryRunnableAction();
                state.replaceAction(currentTop);
            }
            return enabledEvents;
        }

        public String toString() {
            return action + " end @ " + expectedTimeStamp;
        }
    }

    public static class ResourceBlocked<P extends Profile, T extends WorkerResourceDescription> extends SchedulingEvent<P, T> {

        public ResourceBlocked(long timeStamp, AllocatableAction<P, T> action) {
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
                PriorityQueue<AllocatableAction> rescheduledActions
        ) {
            AsceticSchedulingInformation dsi = (AsceticSchedulingInformation) action.getSchedulingInfo();
            dsi.setOnOptimization(false);
            dsi.clearPredecessors();
            dsi.clearSuccessors();
            dsi.setExpectedStart(Long.MAX_VALUE);
            dsi.setExpectedEnd(Long.MAX_VALUE);
            //Actions is registered as blocked because of lack of resources
            state.resourceBlockedAction(action);

            //Register all successors as Blocked Actions
            state.blockDataSuccessors(dsi);
			rescheduledActions.add(action);
            return new LinkedList<SchedulingEvent<P, T>>();
        }

        public String toString() {
            return action + " resourceBlocked";
        }
    }

}
