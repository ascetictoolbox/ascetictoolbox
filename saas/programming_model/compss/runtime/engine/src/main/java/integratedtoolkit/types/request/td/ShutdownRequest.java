package integratedtoolkit.types.request.td;

import integratedtoolkit.components.impl.JobManager;
import integratedtoolkit.components.impl.TaskScheduler;
import integratedtoolkit.util.ResourceManager;
import java.util.concurrent.Semaphore;

/**
 * This class represents a notification to end the execution
 */
public class ShutdownRequest extends TDRequest {

    /**
     * Semaphore where to synchronize until the operation is done
     */
    private Semaphore semaphore;

    /**
     * Constructs a new ShutdownRequest
     *
     */
    public ShutdownRequest(Semaphore sem) {
        this.semaphore = sem;
    }

    /**
     * Returns the semaphore where to synchronize until the object can be read
     *
     * @return the semaphore where to synchronize until the object can be read
     */
    public Semaphore getSemaphore() {
        return semaphore;
    }

    /**
     * Sets the semaphore where to synchronize until the requested object can be
     * read
     *
     * @param sem the semaphore where to synchronize until the requested object
     * can be read
     */
    public void setSemaphore(Semaphore sem) {
        this.semaphore = sem;
    }

    @Override
    public TDRequestType getRequestType() {
        return TDRequestType.SHUTDOWN;
    }

    @Override
    public void process(TaskScheduler ts, JobManager jm) throws ShutdownException {
        //ts.shutdown();
        jm.shutdown();
        ResourceManager.stopNodes();
        semaphore.release();
        throw new ShutdownException();
    }

    public static class ShutdownException extends Exception {

    }
}
