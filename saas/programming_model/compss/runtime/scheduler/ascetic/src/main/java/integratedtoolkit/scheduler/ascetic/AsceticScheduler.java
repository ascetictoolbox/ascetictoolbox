package integratedtoolkit.scheduler.ascetic;

import integratedtoolkit.ascetic.Ascetic;
import integratedtoolkit.types.AsceticScore;
import integratedtoolkit.components.impl.TaskScheduler;
import integratedtoolkit.scheduler.types.AllocatableAction;
import integratedtoolkit.types.Profile;
import integratedtoolkit.types.SchedulingInformation;
import integratedtoolkit.types.Score;
import integratedtoolkit.util.ResourceScheduler;
import integratedtoolkit.types.resources.Worker;
import integratedtoolkit.types.resources.WorkerResourceDescription;

public class AsceticScheduler<P extends Profile, T extends WorkerResourceDescription> extends TaskScheduler<P, T> {

    public static final String OPTIM_PARAM = Ascetic.getSchedulerOptimization();

    private final AsceticScore<P, T> dummyScore = new AsceticScore<P, T>(0, 0, 0, 0, 0, 0);
    private final ScheduleOptimizer optimizer = new ScheduleOptimizer(this);

    /*
     * scheduleAction(Action action)
     * Behaves as the basic Task Scheduler, as tasks arrive their executions are
     * scheduled into a worker node
     * 
     */
    public AsceticScheduler() {
        optimizer.start();
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

    public void shutdown() {
        try {
            optimizer.shutdown();
            Ascetic.stop();
        } catch (InterruptedException ie) {
            //No need to do anything.
        }
    }

}
