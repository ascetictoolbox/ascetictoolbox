package integratedtoolkit.types.request.td;

import integratedtoolkit.components.impl.JobManager;
import integratedtoolkit.components.impl.TaskScheduler;
import integratedtoolkit.util.CEIParser;
import java.util.concurrent.Semaphore;

import integratedtoolkit.util.ResourceManager;
import java.util.LinkedList;

public class UpdateLocalCEIRequest extends TDRequest {

    private Class<?> ceiClass;
    private Semaphore sem;

    public UpdateLocalCEIRequest(Class<?> ceiClass, Semaphore sem) {
        this.ceiClass = ceiClass;
        this.sem = sem;
    }

    /**
     * Returns the semaphore where to synchronize until the operation is done
     *
     * @return Semaphore where to synchronize until the operation is done
     */
    public Semaphore getSemaphore() {
        return sem;
    }

    /**
     * Sets the semaphore where to synchronize until the operation is done
     *
     * @param sem Semaphore where to synchronize until the operation is done
     */
    public void setSemaphore(Semaphore sem) {
        this.sem = sem;
    }

    public void setCeiClass(Class<?> ceiClass) {
        this.ceiClass = ceiClass;
    }

    public Class<?> getCeiClass() {
        return this.ceiClass;
    }

    @Override
    public void process(TaskScheduler ts, JobManager jm) {
        logger.debug("Treating request to update core elements");
        LinkedList<Integer> newCores = CEIParser.loadJava(this.ceiClass);
        if (debug) {
            logger.debug("New methods: " + newCores);
        }
        ResourceManager.coreElementUpdates(newCores);
        ts.resizeDataStructures();
        logger.debug("Data structures resized and CE-resources links updated");
        sem.release();
    }

    @Override
    public TDRequestType getRequestType() {
        return TDRequestType.UPDATE_LOCAL_CEI;
    }

}
