package integratedtoolkit.scheduler.ascetic;

import integratedtoolkit.ascetic.Ascetic;
import integratedtoolkit.types.AsceticScore;
import integratedtoolkit.components.impl.TaskScheduler;
import integratedtoolkit.scheduler.types.AllocatableAction;
import integratedtoolkit.types.AsceticProfile;
import integratedtoolkit.types.Implementation;
import integratedtoolkit.types.Profile;
import integratedtoolkit.types.SchedulingInformation;
import integratedtoolkit.types.Score;
import integratedtoolkit.types.WorkloadState;
import integratedtoolkit.util.ResourceScheduler;
import integratedtoolkit.types.resources.Worker;
import integratedtoolkit.types.resources.WorkerResourceDescription;
import integratedtoolkit.util.CoreManager;
import integratedtoolkit.util.ResourceOptimizer;

public class AsceticScheduler<P extends Profile, T extends WorkerResourceDescription> extends TaskScheduler<P, T> {

    private final AsceticScore<P, T> dummyScore = new AsceticScore<P, T>(0, 0, 0, 0, 0, 0);
    private final ScheduleOptimizer schedOptimizer = new ScheduleOptimizer(this);

    /*
     * scheduleAction(Action action)
     * Behaves as the basic Task Scheduler, as tasks arrive their executions are
     * scheduled into a worker node
     * 
     */
    public AsceticScheduler() {
        schedOptimizer.start();
    }

    @Override
    public ResourceScheduler<P, T> generateSchedulerForResource(Worker<T> w) {
        return new AsceticResourceScheduler<P, T>(w);
    }

    @Override
    public SchedulingInformation<P, T> generateSchedulingInformation() {
        return new AsceticSchedulingInformation<P, T>();
    }

    @Override
    public Score getActionScore(AllocatableAction<P, T> action) {
        long actionScore = AsceticScore.getActionScore(action);
        long dataTime = dummyScore.getDataPredecessorTime(action.getDataPredecessors());
        return new AsceticScore<P, T>(actionScore, dataTime, 0, 0, 0, 0);
    }

    @Override
    public void shutdown() {
        super.shutdown();
        ResourceScheduler[] workers = this.getWorkers();
        System.out.println("End Profiles:");
        for (ResourceScheduler worker : workers) {
            System.out.println("\t" + worker.getName());
            for (java.util.Map.Entry<String, Implementation> entry : CoreManager.SIGNATURE_TO_IMPL.entrySet()) {
                System.out.println("\t\t" + entry.getKey());
                AsceticProfile profile = (AsceticProfile) worker.getProfile(entry.getValue());
                System.out.println("\t\t\tTime " + profile.getAverageExecutionTime() + " ms");
                System.out.println("\t\t\tPower " + profile.getPower() + " W");
                System.out.println("\t\t\tCost " + profile.getPrice() + " €");
            }
        }
        try {
            schedOptimizer.shutdown();
            Ascetic.stop();
        } catch (InterruptedException ie) {
            //No need to do anything.
        }
    }

    @Override
    protected ResourceOptimizer getResourceOptimizer() {
        return new AsceticResourceOptimizer(this);
    }

    @Override
    protected WorkloadState createWorkloadState() {
        return new AsceticWorkloadState();
    }

    @Override
    protected void updateWorkloadState(WorkloadState state) {
        super.updateWorkloadState(state);
    }

    protected void workerRemoved(ResourceScheduler<P, T> resource) {
        AsceticResourceScheduler ars = (AsceticResourceScheduler) resource;
    }
}
