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

package integratedtoolkit.ws.master;

import integratedtoolkit.comm.CommAdaptor;
import integratedtoolkit.types.data.location.URI;
import integratedtoolkit.types.data.operation.DataOperation;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

public class WSAdaptor implements CommAdaptor {

    @Override
    public void init() {
        try{
        WSJob.init();
        }catch(Exception e){
            logger.error("Can not initialize WS Adaptor");
        }
    }

    @Override
    public ServiceInstance initWorker(String workerName, HashMap<String, String> properties) {
        return new ServiceInstance(workerName, properties);
    }

    @Override
    public void stop() {
        WSJob.end();
    }

    @Override
    public LinkedList<DataOperation> getPending() {
        return null;
    }

    @Override
    public void stopSubmittedJobs() {

    }

    @Override
    public void completeMasterURI(URI u) {
        //No need to do nothing
    }
}
