package integratedtoolkit.types.resources;

import integratedtoolkit.api.ITExecution.ParamType;
import integratedtoolkit.comm.Comm;
import integratedtoolkit.log.Loggers;
import integratedtoolkit.types.COMPSsNode;
import integratedtoolkit.types.Implementation;
import integratedtoolkit.types.Task;
import integratedtoolkit.types.data.LogicalData;
import integratedtoolkit.types.data.Transferable;
import integratedtoolkit.types.data.location.DataLocation;
import integratedtoolkit.types.data.location.URI;
import integratedtoolkit.types.data.operation.DataOperation.EventListener;
import integratedtoolkit.types.data.operation.SafeCopyListener;
import integratedtoolkit.types.data.operation.SafeCopyTransferable;
import integratedtoolkit.types.job.Job;
import integratedtoolkit.types.job.Job.JobListener;
import integratedtoolkit.util.SharedDiskManager;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.concurrent.Semaphore;
import org.apache.log4j.Logger;

public abstract class Resource implements Comparable<Resource> {

    public enum Type {

        MASTER,
        WORKER,
        SERVICE
    }

    // Log and debug
    protected static final Logger logger = Logger.getLogger(Loggers.COMM);
    public static final boolean debug = logger.isDebugEnabled();

    private final COMPSsNode node;

    private LinkedList<String> obsoletes = new LinkedList<String>();

    public Resource(COMPSsNode node) {
        this.node = node;
        SharedDiskManager.addMachine(this);
    }

    public Resource(String adaptor, String name, HashMap<String, String> properties) throws Exception {
        this.node = Comm.initWorker(adaptor, name, properties);
        SharedDiskManager.addMachine(this);
    }

    public void addSharedDisk(String diskName, String diskMountpoint) {
        SharedDiskManager.addSharedToMachine(diskName, diskMountpoint, this);
    }

    public final void addObsolete(String obsoleteFile) {
        synchronized (obsoletes) {
            obsoletes.add(obsoleteFile);
        }
    }

    public final void addObsoletes(LinkedList<String> obsoleteFiles) {
        synchronized (obsoletes) {
            obsoletes.addAll(obsoleteFiles);
        }
    }

    public final LinkedList<String> clearObsoletes() {
        synchronized (obsoletes) {
            LinkedList<String> obs = obsoletes;
            obsoletes = new LinkedList<String>();
            return obs;
        }
    }

    public String getName() {
        return node.getName();
    }

    public COMPSsNode getNode() {
        return node;
    }

    public void setInternalURI(URI u) {
        node.setInternalURI(u);
    }

    public Job newJob(Task task, Implementation impl, JobListener listener) {
        return node.newJob(task, impl, this, listener);
    }

    public void getData(String dataId, String tgtDataId, Transferable reason, EventListener listener) {
        LogicalData srcData = Comm.getData(dataId);
        LogicalData tgtData = null;
        if (tgtDataId != null) {
            tgtData = Comm.getData(tgtDataId);
        }
        getData(srcData, dataId, tgtData, reason, listener);
    }

    public void getData(LogicalData ld, LogicalData tgtData, Transferable reason, EventListener listener) {
        getData(ld, ld.getName(), tgtData, reason, listener);
    }

    public void getData(String dataId, String newName, String tgtDataId, Transferable reason, EventListener listener) {
        LogicalData srcData = Comm.getData(dataId);
        LogicalData tgtData = Comm.getData(tgtDataId);
        this.getData(srcData, newName, tgtData, reason, listener);
    }

    public void getData(String dataId, String newName, LogicalData tgtData, Transferable reason, EventListener listener) {
        LogicalData ld = Comm.getData(dataId);
        this.getData(ld, newName, tgtData, reason, listener);
    }

    public void getData(LogicalData ld, String newName, LogicalData tgtData, Transferable reason, EventListener listener) {
        String workingPath = node.getCompletePath(reason.getType(), newName);
        DataLocation target = DataLocation.getLocation(this, workingPath);
        getData(ld, target, tgtData, reason, listener);
    }

    public void getData(String dataId, DataLocation target, Transferable reason, EventListener listener) {
        LogicalData srcData = Comm.getData(dataId);
        getData(srcData, target, srcData, reason, listener);
    }

    public void getData(String dataId, DataLocation target, String tgtDataId, Transferable reason, EventListener listener) {
        LogicalData srcData = Comm.getData(dataId);
        LogicalData tgtData = Comm.getData(tgtDataId);
        getData(srcData, target, tgtData, reason, listener);
    }

    public void getData(String dataId, DataLocation target, LogicalData tgtData, Transferable reason, EventListener listener) {
        LogicalData ld = Comm.getData(dataId);
        getData(ld, target, tgtData, reason, listener);
    }

    public void getData(LogicalData srcData, DataLocation target, LogicalData tgtData, Transferable reason, EventListener listener) {
        node.obtainData(srcData, null, target, tgtData, reason, listener);
    }

    public String getCompleteRemotePath(ParamType type, String name) {
        return node.getCompletePath(type, name);
    }

    public void stop(boolean saveUniqueData, ShutdownListener sl) {
        Semaphore sem = new Semaphore(0);
        SafeCopyListener listener = new SafeCopyListener(sem);
        HashSet<LogicalData> lds = LogicalData.getAllDataFromHost(this);
        HashMap<String, String> disks = SharedDiskManager.terminate(this);
        COMPSsNode masterNode = Comm.appHost.getNode();
        for (LogicalData ld : lds) {
            ld.notifyToInProgressCopiesEnd(listener);
            DataLocation lastLoc = ld.removeHostAndCheckLocationToSave(this, disks);
            if (lastLoc != null && saveUniqueData) {
                listener.addOperation();
                DataLocation safeLoc = DataLocation.getLocation(Comm.appHost, Comm.appHost.getTempDirPath() + ld.getName());
                masterNode.obtainData(ld, lastLoc, safeLoc, ld, new SafeCopyTransferable(), listener);
            }
        }
        listener.enable();
        try {
            sem.acquire();
        } catch (InterruptedException ex) {
            logger.error("Error waiting for files in resource " + getName() + " to get saved");
        }
        sl.addOperation();
        node.stop(sl);
    }

    public abstract Type getType();

    public abstract int compareTo(Resource t);

    public void deleteIntermediate() {
        node.deleteTemporary();
    }
}
