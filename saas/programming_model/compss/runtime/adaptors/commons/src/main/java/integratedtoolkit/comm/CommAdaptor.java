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
