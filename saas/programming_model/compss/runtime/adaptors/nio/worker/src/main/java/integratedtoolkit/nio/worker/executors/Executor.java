package integratedtoolkit.nio.worker.executors;

import org.apache.log4j.Logger;

import integratedtoolkit.log.Loggers;
import integratedtoolkit.nio.NIOTask;
import integratedtoolkit.nio.worker.NIOWorker;

public abstract class Executor {
	
	protected static final Logger logger = Logger.getLogger(Loggers.WORKER);

    public final boolean execute(NIOTask nt, NIOWorker nw) {
        NIOWorker.registerOutputs(NIOWorker.workingDir + "/jobs/job" + nt.getJobId() + "_" + nt.getHist());
        String sandBox;
        try {
        	logger.debug("Creating sandbox for job "+nt.getJobId());
        	sandBox = createSandBox();
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            NIOWorker.unregisterOutputs();
            return false;
        }

        try {
            executeTask(sandBox, nt, nw);
        } catch (Exception e) {
        	logger.error(e.getMessage(), e);
            return false;
        } finally {
            try {
            	logger.debug("Removing sandbox for job "+nt.getJobId());
            	removeSandBox(sandBox);
            } catch (Exception e1) {
            	logger.error(e1.getMessage(), e1);
                return false;
            } finally {
                NIOWorker.unregisterOutputs();
            }
        }
        return true;
    }

    abstract String createSandBox() throws Exception;

    abstract void executeTask(String sandBox, NIOTask nt, NIOWorker nw) throws Exception;

    abstract void removeSandBox(String sandBox) throws Exception;

    public static class JobExecutionException extends Exception {

        public JobExecutionException(String message) {
            super(message);
        }

        public JobExecutionException(String message, Exception e) {
            super(message, e);
        }
    }
}
