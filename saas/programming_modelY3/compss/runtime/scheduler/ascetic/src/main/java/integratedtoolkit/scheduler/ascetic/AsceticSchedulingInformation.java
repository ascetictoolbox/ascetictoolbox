package integratedtoolkit.scheduler.ascetic;

import integratedtoolkit.scheduler.types.AllocatableAction;
import integratedtoolkit.types.Profile;
import integratedtoolkit.types.SchedulingInformation;
import integratedtoolkit.types.resources.WorkerResourceDescription;

import java.util.LinkedList;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReentrantLock;

public class AsceticSchedulingInformation<P extends Profile, T extends WorkerResourceDescription> extends SchedulingInformation<P, T> {

    //Lock to avoid multiple threads to modify the content at the same time
    private final ReentrantLock l = new ReentrantLock();

    private boolean scheduled = false;
    private long lastUpdate;
    private long expectedStart;
    private long expectedEnd;

    private int openGaps = 0;

    //Allocatable actions that the action depends on due to resource availability
    private final LinkedList<AllocatableAction<P, T>> resourcePredecessors;

    //Allocatable actions depending on the allocatable action due to resource availability
    private LinkedList<AllocatableAction<P, T>> resourceSuccessors;

    //Action Scheduling is being optimized locally
    private boolean onOptimization = false;
    private boolean toReschedule = false;
    private final LinkedList<AllocatableAction<P, T>> optimizingSuccessors;

    public AsceticSchedulingInformation() {
        resourcePredecessors = new LinkedList<AllocatableAction<P, T>>();
        resourceSuccessors = new LinkedList<AllocatableAction<P, T>>();

        lastUpdate = System.currentTimeMillis();
        expectedStart = 0;
        expectedEnd = 0;

        optimizingSuccessors = new LinkedList<AllocatableAction<P, T>>();
    }

    public void addPredecessor(AllocatableAction<P, T> predecessor) {
        resourcePredecessors.add(predecessor);
    }

    public boolean hasPredecessors() {
        return !resourcePredecessors.isEmpty();
    }

    @Override
    public final boolean isExecutable() {
        boolean b;
        lock();
        b = resourcePredecessors.isEmpty();
        unlock();
        return b;
    }

    public LinkedList<AllocatableAction<P, T>> getPredecessors() {
        return resourcePredecessors;
    }

    public void removePredecessor(AllocatableAction<P, T> successor) {
        resourcePredecessors.remove(successor);
    }

    public void clearPredecessors() {
        resourcePredecessors.clear();
    }

    public void addSuccessor(AllocatableAction<P, T> successor) {
        resourceSuccessors.add(successor);
    }

    public LinkedList<AllocatableAction<P, T>> getSuccessors() {
        return resourceSuccessors;
    }

    public void removeSuccessor(AllocatableAction<P, T> successor) {
        resourceSuccessors.remove(successor);
    }

    public void clearSuccessors() {
        resourceSuccessors.clear();
    }

    public LinkedList<AllocatableAction<P, T>> replaceSuccessors(LinkedList<AllocatableAction<P, T>> newSuccessors) {
        LinkedList<AllocatableAction<P, T>> oldSuccessors = resourceSuccessors;
        resourceSuccessors = newSuccessors;
        return oldSuccessors;
    }

    public void setExpectedStart(long expectedStart) {
        this.expectedStart = expectedStart;
    }

    public long getExpectedStart() {
        return expectedStart;
    }

    public void setExpectedEnd(long expectedEnd) {
        this.expectedEnd = expectedEnd;
    }

    public long getExpectedEnd() {
        return expectedEnd;
    }

    public void setLastUpdate(long lastUpdate) {
        this.lastUpdate = lastUpdate;
    }

    public long getLastUpdate() {
        return lastUpdate;
    }

    public String toString() {
        StringBuilder sb = new StringBuilder(
                "\tlastUpdate: " + lastUpdate + "\n"
                + "\texpectedStart: " + expectedStart + "\n"
                + "\texpectedEnd:" + expectedEnd + "\n");
        sb.append("\t").append("schedPredecessors: ");
        for (AllocatableAction<P, T> aa : getPredecessors()) {
            sb.append(" ").append(aa);
        }
        sb.append("\n");
        sb.append("\t").append("schedSuccessors: ");
        for (AllocatableAction<P, T> aa : getSuccessors()) {
            sb.append(" ").append(aa);
        }
        sb.append("\n");
        sb.append("\tOptimization Successors").append(optimizingSuccessors);
        return sb.toString();
    }

    public boolean tryToLock() {
        try {
            return l.tryLock(1, TimeUnit.MILLISECONDS);
        } catch (InterruptedException ie) {
            return false;
        }
    }

    public void lock() {
        l.lock();
    }

    public void unlock() {
        l.unlock();
    }

    public void unlockCompletely() {
        while (l.getHoldCount() > 1) {
            l.unlock();
        }
    }

    public void scheduled() {
        scheduled = true;
    }

    public void unscheduled() {
        scheduled = false;
        resourcePredecessors.clear();
        resourceSuccessors.clear();
    }

    boolean isScheduled() {
        return scheduled;
    }

    public void setOnOptimization(boolean b) {
        onOptimization = b;
    }

    public boolean isOnOptimization() {
        return onOptimization;
    }

    public void setToReschedule(boolean b) {
        this.toReschedule = b;
    }

    public boolean isToReschedule() {
        return this.toReschedule;
    }

    public void optimizingSuccessor(AllocatableAction action) {
        optimizingSuccessors.add(action);
    }

    public LinkedList<AllocatableAction<P, T>> getOptimizingSuccessors() {
        return optimizingSuccessors;
    }

    public void clearOptimizingSuccessors() {
        optimizingSuccessors.clear();
    }

    public void addGap() {
        openGaps++;
    }

    public void removeGap() {
        openGaps--;
    }

    public void clearGaps() {
        openGaps = 0;
    }

    public boolean hasGaps() {
        return openGaps > 0;
    }

    public int getLockCount() {
        return l.getHoldCount();
    }

    public int getGapCount() {
        return openGaps;
    }
}
