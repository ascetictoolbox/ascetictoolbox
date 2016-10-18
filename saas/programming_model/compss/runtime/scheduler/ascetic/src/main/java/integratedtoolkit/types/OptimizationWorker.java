package integratedtoolkit.types;

import integratedtoolkit.scheduler.ascetic.AsceticResourceScheduler;
import integratedtoolkit.scheduler.ascetic.ScheduleOptimizer;
import integratedtoolkit.scheduler.types.AllocatableAction;
import java.util.PriorityQueue;

public class OptimizationWorker {

    private AsceticResourceScheduler resource;
    private PriorityQueue<AllocatableAction> donorActions;

    public OptimizationWorker(AsceticResourceScheduler resource) {
        this.resource = resource;
    }

    public void localOptimization(long optimizationTS) {
        donorActions = resource.localOptimization(optimizationTS, ScheduleOptimizer.getSelectionComparator(), ScheduleOptimizer.getDonationComparator());
    }

    public AllocatableAction pollDonorAction() {
        return donorActions.poll();
    }

    public long getDonationIndicator() {
        return resource.getLastGapExpectedStart();
    }

    public String getName() {
        return resource.getName();
    }

    public AsceticResourceScheduler getResource() {
        return resource;
    }

}
