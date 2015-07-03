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
