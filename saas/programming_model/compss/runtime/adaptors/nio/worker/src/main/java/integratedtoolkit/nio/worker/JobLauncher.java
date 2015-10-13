/**
 *
 *   Copyright 2014-2015 Barcelona Supercomputing Center (www.bsc.es) All rights reserved.
 *
 *   Licensed under the Apache License, Version 2.0 (the "License");
 *   you may not use this file except in compliance with the License.
 *   You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *   Unless required by applicable law or agreed to in writing, software
 *   distributed under the License is distributed on an "AS IS" BASIS,
 *   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *   See the License for the specific language governing permissions and
 *   limitations under the License.
 */

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
