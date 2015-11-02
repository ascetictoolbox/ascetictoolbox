/**
 *
 *   Copyright 2015-2015 Barcelona Supercomputing Center (www.bsc.es) All rights reserved.
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

package integratedtoolkit.types.request.ap;

import integratedtoolkit.components.impl.DataInfoProvider;
import integratedtoolkit.components.impl.TaskAnalyser;
import integratedtoolkit.components.impl.TaskDispatcher;
import java.util.concurrent.Semaphore;

/**
 * The GraphDescriptionRequest class represents a request to obtain a dot format
 * description of the dependency graph
 */
public class GraphDescriptionRequest extends APRequest {

    /**
     * Semaphore where to synchronize until the operation is done
     */
    private Semaphore sem;
    /**
     * Graph description
     */
    private String response;

    /**
     * Constructs a new GraphDescription request
     *
     * @param sem Semaphore where to synchronize until the operation is done
     */
    public GraphDescriptionRequest(Semaphore sem) {
        this.sem = sem;
    }

    /**
     * Returns the semaphore where to synchronize until the graph is described
     *
     * @return the semaphore where to synchronize until the graph is described
     */
    public Semaphore getSemaphore() {
        return sem;
    }

    /**
     * Sets the semaphore where to synchronize until the graph is described
     *
     * @param sem the semaphore where to synchronize until the graph is
     * described
     */
    public void setSemaphore(Semaphore sem) {
        this.sem = sem;
    }

    /**
     * Returns the graph description in a dot format
     *
     * @return graph description in a dot format
     */
    public String getResponse() {
        return response;
    }

    /**
     * Sets the graph description
     *
     * @param response graph description
     */
    public void setResponse(String response) {
        this.response = response;
    }

    @Override
    public void process(TaskAnalyser ta, DataInfoProvider dip, TaskDispatcher td) {
        response = ta.getGraphDOTFormat();
        sem.release();
    }

    @Override
    public APRequestType getRequestType() {
        return APRequestType.GRAPHSTATE;
    }
}
