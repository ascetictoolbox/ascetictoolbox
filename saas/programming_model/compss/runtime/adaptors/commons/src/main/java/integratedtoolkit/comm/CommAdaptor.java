/**
 *
 *   Copyright 2013-2015 Barcelona Supercomputing Center (www.bsc.es) All rights reserved.
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

package integratedtoolkit.comm;

import integratedtoolkit.types.data.location.URI;
import java.util.LinkedList;

import org.apache.log4j.Logger;

import integratedtoolkit.log.Loggers;

import integratedtoolkit.types.COMPSsWorker;
import integratedtoolkit.types.data.operation.DataOperation;

import java.util.HashMap;

public interface CommAdaptor {

    static final Logger logger = Logger.getLogger(Loggers.COMM);
    static final boolean debug = logger.isDebugEnabled();

    public void init();

    public COMPSsWorker initWorker(String workerName, HashMap<String, String> properties) throws Exception;

    public void stop();

    public LinkedList<DataOperation> getPending();

    public void completeMasterURI(URI u);

    public void stopSubmittedJobs();

}
