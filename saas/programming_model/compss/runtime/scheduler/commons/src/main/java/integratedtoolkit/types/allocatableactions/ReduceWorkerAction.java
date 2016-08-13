package integratedtoolkit.types.allocatableactions;

import integratedtoolkit.components.impl.TaskScheduler;
import integratedtoolkit.scheduler.exceptions.BlockedActionException;
import integratedtoolkit.scheduler.exceptions.FailedActionException;
import integratedtoolkit.scheduler.exceptions.UnassignedActionException;
import integratedtoolkit.scheduler.types.AllocatableAction;
import integratedtoolkit.types.Implementation;
import integratedtoolkit.types.MethodImplementation;
import integratedtoolkit.types.Profile;
import integratedtoolkit.types.SchedulingInformation;
import integratedtoolkit.types.Score;
import integratedtoolkit.types.ServiceImplementation;
import integratedtoolkit.types.resources.CloudMethodWorker;
import integratedtoolkit.types.resources.MethodResourceDescription;
import integratedtoolkit.types.resources.Resource.Type;
import integratedtoolkit.types.resources.Worker;
import integratedtoolkit.types.resources.WorkerResourceDescription;
import integratedtoolkit.types.resources.updates.PendingReduction;
import integratedtoolkit.types.resources.updates.ResourceUpdate;
import integratedtoolkit.util.ErrorManager;
import integratedtoolkit.util.ResourceManager;
import integratedtoolkit.util.ResourceScheduler;
import java.util.LinkedList;

public class ReduceWorkerAction<T extends WorkerResourceDescription> extends AllocatableAction<Profile, T> {

    private final ResourceScheduler<Profile, T> worker;
    private final TaskScheduler ts;
    private final Implementation impl;
    private final PendingReduction<MethodResourceDescription> ru;

    public ReduceWorkerAction(SchedulingInformation schedulingInformation, ResourceScheduler<Profile, T> worker, TaskScheduler ts, ResourceUpdate modification) {
        super(schedulingInformation);
        this.worker = worker;
        this.ts = ts;
        this.ru = (PendingReduction<MethodResourceDescription>) modification;
        if (worker.getResource().getType() == Type.WORKER) {
            impl = new MethodImplementation("", null, null, (MethodResourceDescription) modification.getModification());
        } else {
            impl = new ServiceImplementation(null, "", "", "", "");
        }
    }

    @Override
    protected void doAction() {
        try {
            (new Thread() {
                public void run() {
                    Thread.currentThread().setName(selectedResource.getResource().getName() + " stopper");

                    ResourceManager.reduceResource((CloudMethodWorker) worker.getResource(), (PendingReduction<MethodResourceDescription>) ru, ru.getCompatibleImplementations());
                    Worker w = selectedResource.getResource();
                    w.endTask(resourceConsumption);
                    try {
                        ru.waitForCompletion();
                    } catch (Exception e) {
                        logger.error("ERROR: Exception raised on worker reduction", e);
                        ErrorManager.warn("Exception reducing worker. Check runtime.log for more details", e);
                        notifyError();
                    }
                    notifyCompleted();
                }
            }).start();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void doCompleted() {
        ts.completedResourceUpdate(worker, ru);
    }

    @Override
    protected void doError() throws FailedActionException {
        throw new FailedActionException();
    }

    @Override
    protected void doFailed() {
        logger.error("Error waiting for tasks to end");
        ts.completedResourceUpdate(worker, ru);
    }

    @Override
    public Integer getCoreId() {
        return null;
    }

    @Override
    public LinkedList<ResourceScheduler<?, ?>> getCompatibleWorkers() {
        LinkedList<ResourceScheduler<?, ?>> workers = new LinkedList<ResourceScheduler<?, ?>>();
        workers.add(worker);
        return workers;
    }

    @Override
    public Implementation<T>[] getImplementations() {
        Implementation<T>[] impls = new Implementation[1];
        impls[0] = impl;
        return impls;
    }

    @Override
    public boolean isCompatible(Worker<T> r) {
        return (r == worker.getResource());
    }

    @Override
    public LinkedList<Implementation<T>> getCompatibleImplementations(ResourceScheduler<Profile, T> r) {
        LinkedList<Implementation<T>> impls = new LinkedList<Implementation<T>>();
        if (r == worker) {
            impls.add(impl);
        }
        return impls;
    }

    @Override
    public Score schedulingScore(ResourceScheduler<Profile, T> targetWorker, Score actionScore) {
        return null;
    }

    @Override
    public void schedule(Score actionScore) throws BlockedActionException, UnassignedActionException {
        this.selectedResource = worker;
        assignImplementation(impl);
        worker.initialSchedule(this);
    }

    @Override
    public void schedule(ResourceScheduler<Profile, T> targetWorker, Score actionScore) throws BlockedActionException, UnassignedActionException {
        this.selectedResource = targetWorker;
        assignImplementation(impl);
        targetWorker.initialSchedule(this);
    }

    @Override
    public void schedule(ResourceScheduler<Profile, T> targetWorker, Implementation impl) throws BlockedActionException, UnassignedActionException {
        this.selectedResource = targetWorker;
        assignImplementation(impl);
        targetWorker.initialSchedule(this);
    }

    public String toString() {
        return "ReduceWorkerAction for worker " + worker.getName();
    }

    @Override
    public int getPriority() {
        return Integer.MAX_VALUE;
    }

    @Override
    public boolean isToReserveResources() {
        return true;
    }

    @Override
    public boolean isToReleaseResources() {
        return false;
    }
}
