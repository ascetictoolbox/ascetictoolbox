package integratedtoolkit.types.request.td;

import integratedtoolkit.components.impl.JobManager;
import integratedtoolkit.components.impl.TaskScheduler;
import integratedtoolkit.types.resources.Worker;

/**
 * The AddCloudNodeRequest represents a request to add a new resource ready to
 * execute to the resource pool
 */
public class UpdatedWorkerConsumption extends TDRequest {

    public final Worker worker;

    /**
     * Constructs a AddCloudNodeRequest with all its parameters
     *
     * @param worker Worker that has been added
     *
     */
    public UpdatedWorkerConsumption(Worker worker) {
        this.worker = worker;
    }

    public Worker getWorker() {
        return worker;
    }

    @Override
    public TDRequestType getRequestType() {
        return TDRequestType.UPDATED_WORKER_CONSUMPTIONS;
    }

    @Override
    public void process(TaskScheduler ts, JobManager jm) {
        ts.scheduleToResource(worker);
    }

}
