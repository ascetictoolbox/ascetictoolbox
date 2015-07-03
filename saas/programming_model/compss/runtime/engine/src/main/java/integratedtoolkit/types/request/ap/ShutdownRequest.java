package integratedtoolkit.types.request.ap;

import integratedtoolkit.components.impl.DataInfoProvider;
import integratedtoolkit.components.impl.TaskAnalyser;
import integratedtoolkit.components.impl.TaskDispatcher;
import java.util.concurrent.Semaphore;

public class ShutdownRequest extends APRequest {

    private Semaphore sem;

    public ShutdownRequest(Semaphore sem) {
        this.sem = sem;
    }

    /**
     * Returns the semaphore where to synchronize until the object can be read
     *
     * @return the semaphore where to synchronize until the object can be read
     */
    public Semaphore getSemaphore() {
        return sem;
    }

    /**
     * Sets the semaphore where to synchronize until the requested object can be
     * read
     *
     * @param sem the semaphore where to synchronize until the requested object
     * can be read
     */
    public void setSemaphore(Semaphore sem) {
        this.sem = sem;
    }

    @Override
    public void process(TaskAnalyser ta, DataInfoProvider dip, TaskDispatcher td) throws ShutdownException {
        //Close Graph
        ta.shutdown(); 
        //clear delete Intermediate Files
        dip.shutdown();
        sem.release();
        throw new ShutdownException();
    }

    @Override
    public APRequestType getRequestType() {
        return APRequestType.SHUTDOWN;
    }

    public static class ShutdownException extends Exception {

    }
}
