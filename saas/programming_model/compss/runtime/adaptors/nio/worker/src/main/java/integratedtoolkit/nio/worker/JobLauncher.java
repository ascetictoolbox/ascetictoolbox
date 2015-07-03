package integratedtoolkit.nio.worker;

import integratedtoolkit.ITConstants.Lang;
import integratedtoolkit.nio.NIOTask;
import integratedtoolkit.nio.worker.executors.CExecutor;
import integratedtoolkit.nio.worker.executors.JavaExecutor;
import integratedtoolkit.nio.worker.executors.PythonExecutor;
import integratedtoolkit.util.RequestDispatcher;
import integratedtoolkit.util.RequestQueue;


public class JobLauncher extends RequestDispatcher<NIOTask> {

    protected static final int NUM_HEADER_PARS = 5;

    private final NIOWorker nw;
    private final JavaExecutor java = new JavaExecutor();
    private final CExecutor c = new CExecutor();
    private final PythonExecutor python = new PythonExecutor();

    public JobLauncher(RequestQueue<NIOTask> queue, NIOWorker nw) {
        super(queue);
        this.nw = nw;
    }

    public void processRequests() {
        NIOTask nt;

        while (true) {
            nt = queue.dequeue();   // Get tasks until there are no more tasks pending
            if (nt == null) {
                break;
            }
            boolean success = executeTask(nt);

            nw.sendTaskDone(nt, success);
        }

    }

    private boolean executeTask(NIOTask nt) {
        //There is no sandbox create the sandbox??
        //call trace.sh start eventType task_Id slot
        switch (Lang.valueOf(nt.lang.toUpperCase())) {
            case JAVA:
                return java.execute(nt, nw);
            case PYTHON:
                return python.execute(nt, nw);
            case C:
                return c.execute(nt, nw);
            default:
                System.err.println("Incorrect language " + nt.lang + " in job " + nt.getJobId());
                return false;
        }
    }

}
