/**
 * Copyright 2014 University of Leeds
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
package eu.ascetic.energymodellerloadinducer;

import eu.ascetic.ioutils.execution.CompletedListener;
import eu.ascetic.ioutils.execution.ManagedProcessSequenceExecutor;
import javax.jws.WebService;
import javax.jws.WebMethod;
import javax.jws.WebParam;

/**
 *
 * @author Richard
 */
@WebService(serviceName = "CalibrationLoadGenerator")
public class CalibrationLoadGenerator implements CompletedListener {

    private boolean working = false;
    private ManagedProcessSequenceExecutor executor;

    /**
     * This induces load on the web server
     *
     * @return If the executor has finished or not.
     */
    @WebMethod(operationName = "induceLoad")
    public boolean induceLoad() {
        if (!working) {
            executor = new ManagedProcessSequenceExecutor(this);
            working = true;
        }
        return working;
    }

    /**
     * This indicates if it has finished inducing load on the web server
     *
     * @return If the executor has finished or not.
     */
    @WebMethod(operationName = "currentlyWorking")
    public boolean currentlyWorking() {
        return working;
    }

    @Override
    public void finished() {
        working = false;
    }
}
