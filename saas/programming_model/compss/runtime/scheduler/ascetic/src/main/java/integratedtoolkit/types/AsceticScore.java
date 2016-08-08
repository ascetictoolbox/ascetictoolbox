package integratedtoolkit.types;

import integratedtoolkit.ascetic.Ascetic;
import integratedtoolkit.ascetic.Ascetic.OptimizationParameter;
import integratedtoolkit.scheduler.ascetic.AsceticSchedulingInformation;
import integratedtoolkit.scheduler.types.AllocatableAction;
import integratedtoolkit.types.resources.WorkerResourceDescription;

import java.util.LinkedList;

public class AsceticScore<P extends Profile, T extends WorkerResourceDescription> extends Score {

    private static final OptimizationParameter opParam = Ascetic.getSchedulerOptimization();
    /*
     * ActionScore -> task Priority
     * expectedDataAvailable -> expected time when data dependencies will be ready (take into account transfers)
     * resourceScore -> Expected ResourceAvailability
     * implementationScore -> ExecutionTime
     */
    private final long expectedDataAvailable;
    private final long expectedStart;
    private final double expectedCost;
    private final double expectedEnergy;

    public AsceticScore(long actionScore, long dataAvailability, long res, long impl, double energy, double cost) {
        super(actionScore, res, impl);
        expectedDataAvailable = dataAvailability;
        expectedStart = Math.max(resourceScore, expectedDataAvailable);
        expectedCost = cost;
        expectedEnergy = energy;
    }

    public AsceticScore(AsceticScore<P, T> actionScore, long transferTime, long resourceTime, long impl, double energy, double cost) {
        super(actionScore, resourceTime, impl);
        expectedDataAvailable = actionScore.expectedDataAvailable + transferTime;
        expectedStart = Math.max(resourceScore, expectedDataAvailable);
        expectedCost = cost;
        expectedEnergy = energy;
    }

    @Override
    public boolean isBetter(Score other) {
        AsceticScore<P, T> otherDS = (AsceticScore<P, T>) other;
        if (actionScore != other.actionScore) {
            return actionScore > other.actionScore;
        }

        double diffCost = expectedCost - otherDS.expectedCost;
        double diffEnergy = expectedEnergy - otherDS.expectedEnergy;
        long ownEnd = expectedStart + implementationScore;
        long otherEnd = otherDS.expectedStart + otherDS.implementationScore;
        long diffEnd = ownEnd - otherEnd;
        switch (opParam) {
            case COST:
                if (diffCost == 0) {
                    if (diffEnd == 0) {
                        return diffEnergy < 0;
                    } else {
                        return diffEnd < 0;
                    }
                } else {
                    return diffCost < 0;
                }
            case ENERGY:
                if (diffEnergy == 0) {
                    if (diffEnd == 0) {
                        return diffCost < 0;
                    } else {
                        return diffEnd < 0;
                    }
                } else {
                    return diffEnergy < 0;
                }
            default:
                if (diffEnd == 0) {
                    if (diffEnergy == 0) {
                        return diffCost < 0;
                    } else {
                        return diffEnergy < 0;
                    }
                } else {
                    return diffEnd < 0;
                }
        }
    }

    public static long getActionScore(AllocatableAction action) {
        return action.getPriority();
    }

    public long getDataPredecessorTime(LinkedList<AllocatableAction<P, T>> predecessors) {
        long dataTime = 0;
        for (AllocatableAction<P, T> pred : predecessors) {
            dataTime = Math.max(dataTime, ((AsceticSchedulingInformation<P, T>) pred.getSchedulingInfo()).getExpectedEnd());
        }
        return dataTime;
    }

    public long getActionScore() {
        return actionScore;
    }

    public long getExpectedDataAvailable() {
        return expectedDataAvailable;
    }

    public long getResourceScore() {
        return resourceScore;
    }

    public long getExpectedStart() {
        return expectedStart;
    }

    public long getImplementationScore() {
        return implementationScore;
    }

    public String toString() {
        return "action " + actionScore + " availableData " + expectedDataAvailable + " resource " + resourceScore + " expectedStart " + expectedStart + " length " + implementationScore + " cost " + expectedCost + " energy " + expectedEnergy;
    }
}
