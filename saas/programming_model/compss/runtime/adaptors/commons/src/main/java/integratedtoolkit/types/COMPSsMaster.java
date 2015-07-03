package integratedtoolkit.types;

import integratedtoolkit.api.ITExecution;
import integratedtoolkit.comm.Comm;
import integratedtoolkit.comm.CommAdaptor;
import integratedtoolkit.types.data.location.DataLocation;
import integratedtoolkit.types.data.LogicalData;
import integratedtoolkit.types.data.Transferable;
import integratedtoolkit.types.data.location.URI;
import integratedtoolkit.types.data.operation.DataOperation;
import integratedtoolkit.types.job.Job;
import integratedtoolkit.types.resources.Resource;
import integratedtoolkit.types.resources.ShutdownListener;
import integratedtoolkit.util.Serializer;
import java.io.File;
import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.nio.file.Files;

public class COMPSsMaster extends COMPSsNode {

    protected static final String ERROR_UNKNOWN_HOST = "ERROR: Cannot determine the IP address of the local host";

    private final String name;

    //private final String workingDirectory;
    public COMPSsMaster() {
        super();
        // Initializing host attributes
        String hostName;
        try {
            InetAddress localHost = InetAddress.getLocalHost();
            hostName = localHost.getCanonicalHostName();
        } catch (UnknownHostException e) {
            logger.fatal(ERROR_UNKNOWN_HOST, e);
            hostName = "";
            System.exit(1);

        }
        name = hostName;

    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public void setInternalURI(URI u) {
        for (CommAdaptor adaptor : Comm.getAdaptors().values()) {
            adaptor.completeMasterURI(u);
        }
    }

    @Override
    public void stop(ShutdownListener sl) {
        //NO need to do anything
    }

    @Override
    public void sendData(LogicalData ld, DataLocation source, DataLocation target, LogicalData tgtData, Transferable reason, DataOperation.EventListener listener) {
        for (Resource targetRes : target.getHosts()) {
            if (targetRes.getNode() != this) {
                try {
                    targetRes.getNode().obtainData(ld, source, target, tgtData, reason, listener);
                } catch (Exception e) {
                    continue;
                }
                return;
            }
        }
    }

    @Override
    public void obtainData(LogicalData ld, DataLocation source, DataLocation target, LogicalData tgtData, Transferable reason, DataOperation.EventListener listener) {
        if (ld.isInMemory()) {
            try {
                Serializer.serialize(ld.getValue(), target.getPath());
                if (tgtData != null) {
                    tgtData.addLocation(target);
                }
                reason.setDataTarget(target.getPath());
                listener.notifyEnd(null);
                return;
            } catch (IOException ex) {

            }
        }
        for (URI u : ld.getURIs()) {
            if (u.getHost() == Comm.appHost) {
                try {
                    Files.copy((new File(u.getPath())).toPath(), new File(target.getPath()).toPath());
                    if (tgtData != null) {
                        tgtData.addLocation(target);
                    }
                    reason.setDataTarget(target.getPath());
                    listener.notifyEnd(null);
                    return;
                } catch (IOException ex) {

                }
            }
        }
        if (source != null) {
            for (Resource sourceRes : source.getHosts()) {
                if (sourceRes.getNode() != this) {
                    try {
                        sourceRes.getNode().sendData(ld, source, target, tgtData, reason, listener);
                    } catch (Exception e) {
                        e.printStackTrace();
                        continue;
                    }
                    return;
                }
            }
        }
        for (Resource sourceRes : ld.getAllHosts()) {
            if (sourceRes.getNode() != this) {
                try {
                    sourceRes.getNode().sendData(ld, source, target, tgtData, reason, listener);
                } catch (Exception e) {
                    e.printStackTrace();
                    continue;
                }
                return;
            }
        }
    }

    @Override
    public Job newJob(Task task, Implementation impl, Resource res, Job.JobListener listener) {
        //Cannot run jobs
        return null;
    }

    @Override
    public String getCompletePath(ITExecution.ParamType type, String name) {
        switch (type) {
            case FILE_T:
                return Comm.appHost.getTempDirPath() + name;
            case OBJECT_T:
                return name;
            default:
                return null;
        }
    }

    @Override
    public void deleteTemporary() {
        File dir = new File(Comm.appHost.getTempDirPath());
        for (File f : dir.listFiles()) {
            deleteFolder(f);
        }
    }

    private void deleteFolder(File folder) {
        if (folder != null) {
            if (folder.isDirectory()) {
                for (File f : folder.listFiles()) {
                    deleteFolder(f);
                }
            }
            folder.delete();
        }
    }
}
