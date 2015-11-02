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

package integratedtoolkit.types;

import integratedtoolkit.ITConstants;
import integratedtoolkit.api.ITExecution.ParamType;
import integratedtoolkit.types.data.location.DataLocation;
import integratedtoolkit.types.data.LogicalData;
import integratedtoolkit.types.data.location.URI;
import integratedtoolkit.log.Loggers;
import integratedtoolkit.types.data.Transferable;
import integratedtoolkit.types.data.operation.DataOperation;
import integratedtoolkit.types.job.Job;
import integratedtoolkit.types.job.Job.JobListener;
import integratedtoolkit.types.resources.Resource;
import integratedtoolkit.types.resources.ShutdownListener;

import org.apache.log4j.Logger;

public abstract class COMPSsNode implements Comparable<COMPSsNode> {

    // Log and debug
    protected static final Logger logger = Logger.getLogger(Loggers.COMM);
    public static final boolean debug = logger.isDebugEnabled();

    // Tracing
    protected static final boolean tracing = System.getProperty(ITConstants.IT_TRACING) != null
            && System.getProperty(ITConstants.IT_TRACING).equals("true")
            ? true : false;

    protected static final String ANY_PROT = "any://";

    protected static final String DELETE_ERR = "Error deleting intermediate files";
    protected static final String URI_CREATION_ERR = "Error creating new URI";

    public abstract String getName();

    public COMPSsNode() {
    }

    public abstract void setInternalURI(URI u);

    public abstract void sendData(LogicalData srcData, DataLocation loc, DataLocation target, LogicalData tgtData, Transferable reason, DataOperation.EventListener listener);

    public abstract void obtainData(LogicalData srcData, DataLocation source, DataLocation target, LogicalData tgtData, Transferable reason, DataOperation.EventListener listener);

    public abstract Job newJob(Task task, Implementation impl, Resource res, JobListener listener);

    public int compareTo(COMPSsNode host) {
        return getName().compareTo(host.getName());
    }

    public abstract void stop(ShutdownListener sl);

    public abstract String getCompletePath(ParamType type, String name);

    public abstract void deleteTemporary();
}
