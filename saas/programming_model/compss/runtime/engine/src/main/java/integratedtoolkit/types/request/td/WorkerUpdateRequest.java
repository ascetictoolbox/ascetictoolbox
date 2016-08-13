package integratedtoolkit.types.request.td;

import integratedtoolkit.components.impl.TaskScheduler;
import integratedtoolkit.types.Profile;
import integratedtoolkit.types.resources.Worker;
import integratedtoolkit.types.resources.WorkerResourceDescription;
import integratedtoolkit.types.resources.updates.ResourceUpdate;

/**
 * The AddCloudNodeRequest represents a request to add a new resource ready to
 * execute to the resource pool
 */
public class WorkerUpdateRequest<P extends Profile, T extends WorkerResourceDescription> extends TDRequest<P, T> {

    public final Worker<T> worker;
    public final ResourceUpdate ru;

    /**
     * Constructs a AddCloudNodeRequest with all its parameters
     *
     * @param worker Worker that has been added
     *
     */
    public WorkerUpdateRequest(Worker<T> worker, ResourceUpdate ru) {
        this.worker = worker;
        this.ru = ru;
    }

    public Worker<T> getWorker() {
        return worker;
    }

    @Override
    public void process(TaskScheduler<P, T> ts) {
        ts.updateWorker(worker, ru);
    }

    @Override
    public TDRequestType getType() {
        return TDRequestType.WORKER_UPDATE_REQUEST;
    }
}
