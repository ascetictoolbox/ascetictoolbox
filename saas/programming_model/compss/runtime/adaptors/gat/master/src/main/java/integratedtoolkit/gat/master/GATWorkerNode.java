/*
 *  Copyright 2002-2012 Barcelona Supercomputing Center (www.bsc.es)
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package integratedtoolkit.gat.master;

import integratedtoolkit.ITConstants;
import integratedtoolkit.api.ITExecution;
import integratedtoolkit.types.data.location.DataLocation;
import integratedtoolkit.types.job.Job;
import integratedtoolkit.types.data.LogicalData;
import integratedtoolkit.types.data.location.URI;
import integratedtoolkit.types.COMPSsWorker;
import integratedtoolkit.types.Implementation;
import integratedtoolkit.types.Task;
import integratedtoolkit.types.data.Transferable;
import integratedtoolkit.types.data.operation.Copy;
import integratedtoolkit.types.data.operation.DataOperation.EventListener;
import integratedtoolkit.types.job.Job.JobListener;
import integratedtoolkit.types.resources.Resource;
import integratedtoolkit.types.resources.ShutdownListener;
import integratedtoolkit.util.SSHManager;

import java.io.File;
import java.net.URISyntaxException;
import java.util.HashMap;

public class GATWorkerNode extends COMPSsWorker {

    private static final String GAT_CLEAN_SCRIPT = "adaptors/gat/clean.sh";

    private String host = "";
    private String user = "";
    private String installDir = "";
    private String workingDir = "";
    private String appDir = "";
    private String libPath = "";
    private String queue = "";
    private int limitOfTasks;

    private org.gridlab.gat.resources.Job tracingJob;

    @Override
    public String getName() {
        return host;
    }

    public GATWorkerNode(String name, HashMap<String, String> properties) {
        super(name, properties);

        this.host = name;

        this.installDir = properties.remove(ITConstants.INSTALL_DIR);
        if (this.installDir == null) {
            this.installDir = "";
        } else if (!this.installDir.endsWith(File.separator)) {
            this.installDir = this.installDir + File.separator;
        }

        this.workingDir = properties.remove(ITConstants.WORKING_DIR);
        if (this.workingDir == null) {
            this.workingDir = "";
        } else if (!this.workingDir.endsWith(File.separator)) {
            this.workingDir = this.workingDir + File.separator;
        }

        if ((this.user = properties.remove(ITConstants.USER)) == null) {
            this.user = "";
        }

        if ((this.appDir = properties.remove(ITConstants.APP_DIR)) == null) {
            this.appDir = "null";
        }

        if ((this.libPath = properties.remove(ITConstants.LIB_PATH)) == null) {
            this.libPath = "null";
        }
        if ((this.queue = properties.remove("queue")) == null) {
            this.queue = "";
        }

        String value;
        if ((value = properties.remove(ITConstants.LIMIT_OF_TASKS)) != null) {
            this.limitOfTasks = Integer.parseInt(value);
        }

        for (java.util.Map.Entry<String, String> entry : properties.entrySet()) {
            String propName = entry.getKey();
            String propValue = entry.getValue();
            if (propName.startsWith("[context=job]")) {
                propName = propName.substring(13);
                GATJob.addAdaptorPreference(propName, propValue);
                if (tracing) {
                    GATTracer.addPreference(propName, propValue);
                }
            }
            if (propName.startsWith("[context=file]")) {
                GATAdaptor.addFileAdaptorPreferences(propName.substring(14), propValue);
            }
        }
        if (tracing) {
            tracingJob = GATTracer.startTracing(this);
        }
    }

    public String getUser() {
        return user;
    }

    public String getHost() {
        return host;
    }

    public String getInstallDir() {
        return installDir;
    }

    public String getWorkingDir() {
        return workingDir;
    }

    public String getAppDir() {
        return appDir;
    }

    public String getLibPath() {
        return libPath;
    }

    public String getQueue() {
        return queue;
    }

    public int getLimitOfTasks() {
        return this.limitOfTasks;
    }

    @Override
    public boolean isTracingReady() {
        return !tracing || GATTracer.isReady(tracingJob);
    }

    @Override
    public void waitForTracingReady() {
        if (!tracing) {
            return;
        }
        GATTracer.waitForTracing(tracingJob);
    }

    @Override
    public Job newJob(Task task, Implementation impl, Resource res, JobListener listener) {
        return new GATJob(task, impl, res, listener);
    }

    @Override
    public void setInternalURI(URI uri) {
        String scheme = uri.getScheme();
        String user = this.user.isEmpty() ? "" : this.user + "@";
        String host = this.host;
        String filePath = uri.getPath();

        String s = (scheme
                + user
                + host + "/"
                + filePath);
        org.gridlab.gat.URI gat;
        try {
            gat = new org.gridlab.gat.URI(s);
            uri.setInternalURI(GATAdaptor.ID, gat);
        } catch (URISyntaxException e) {
            logger.error(URI_CREATION_ERR, e);
        }
    }

    @Override
    public void stop(ShutdownListener sl) {

        if (tracing) {
            GATTracer.generatePackage(this);
            //There's a 
        }
        sl.notifyEnd();
    }

    public org.gridlab.gat.URI getCleanScript() {
        String user = this.user.isEmpty() ? "" : this.user + "@";
        try {
            return (new org.gridlab.gat.URI(ANY_PROT + user + host + "/" + installDir + "/" + GAT_CLEAN_SCRIPT));
        } catch (URISyntaxException e) {
            logger.error(DELETE_ERR, e);
            return null;
        }
    }

    public String getCleanParams() {
        String pars = workingDir + " " + tracing;
        if (tracing) {
            pars += " " + host;
        }
        return pars;
    }

    public void processCopy(Copy c) {
        GATAdaptor.enqueueCopy(c);
    }

    @Override
    public void sendData(LogicalData srcData, DataLocation source, DataLocation target, LogicalData tgtData, Transferable reason, EventListener listener) {
        Copy c = new GATCopy(srcData, source, target, tgtData, reason, listener);
        GATAdaptor.enqueueCopy(c);
    }

    @Override
    public void obtainData(LogicalData ld, DataLocation source, DataLocation target, LogicalData tgtData, Transferable reason, EventListener listener) {
        Copy c = new GATCopy(ld, source, target, tgtData, reason, listener);
        GATAdaptor.enqueueCopy(c);
    }

    @Override
    public void updateTaskCount(int processorCoreCount) {
        if (tracing) {
            System.err.println("TRACING I CLOUD NO FUNCIONEN BE");
        }
    }

    @Override
    public void announceCreation() throws Exception {
        SSHManager.registerWorker(this);
        SSHManager.announceCreation(this);
    }

    @Override
    public void announceDestruction() throws Exception {
        SSHManager.removeKey(this);
        SSHManager.announceDestruction(this);
        SSHManager.removeWorker(this);
    }

    @Override
    public String getCompletePath(ITExecution.ParamType type, String name) {
        switch (type) {
            case FILE_T:
                return workingDir + name;
            case OBJECT_T:
                return workingDir + name;
            default:
                return null;
        }
    }

    @Override
    public void deleteTemporary() {
        System.out.println("GATWorkerNode hauria d'eliminar " + workingDir + " a " + getName());
    }

}
