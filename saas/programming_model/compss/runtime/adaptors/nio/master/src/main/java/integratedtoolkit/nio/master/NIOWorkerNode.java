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

package integratedtoolkit.nio.master;

import es.bsc.comm.Connection;
import es.bsc.comm.TransferManager;
import es.bsc.comm.nio.NIONode;
import integratedtoolkit.ITConstants;
import integratedtoolkit.api.ITExecution;
import integratedtoolkit.comm.Comm;
import integratedtoolkit.log.Loggers;
import integratedtoolkit.types.data.location.DataLocation;
import integratedtoolkit.types.job.Job;
import integratedtoolkit.types.data.LogicalData;
import integratedtoolkit.types.data.location.URI;
import integratedtoolkit.nio.NIOAgent;
import integratedtoolkit.nio.NIOAgent.DataRequest.MasterDataRequest;
import integratedtoolkit.nio.NIOURI;
import integratedtoolkit.nio.commands.CommandShutdown;
import integratedtoolkit.nio.commands.Data;
import integratedtoolkit.types.COMPSsWorker;
import integratedtoolkit.types.Implementation;
import integratedtoolkit.types.Task;
import integratedtoolkit.types.data.Transferable;
import integratedtoolkit.types.data.operation.Copy;
import integratedtoolkit.types.data.operation.Copy.DeferredCopy;
import integratedtoolkit.types.data.operation.DataOperation;
import integratedtoolkit.types.data.operation.DataOperation.EventListener;
import integratedtoolkit.types.job.Job.JobListener;
import integratedtoolkit.types.resources.Resource;
import integratedtoolkit.types.resources.ShutdownListener;

import java.io.File;
import java.util.HashMap;
import org.apache.log4j.Logger;

public class NIOWorkerNode extends COMPSsWorker {

    protected static final Logger logger = Logger.getLogger(Loggers.COMM);

    private String user;
    private String host;
    private String installDir;
    private String workingDir;
    private String appDir;
    private String libPath;
    private String queue;

    private NIONode node;
    private NIOAdaptor commManager;

    @Override
    public String getName() {
        return host;
    }

    public NIOWorkerNode(String name, HashMap<String, String> properties, NIOAdaptor adaptor) {
        super(name, properties);

        this.user = properties.get(ITConstants.USER);

        this.host = name;
        this.installDir = properties.get(ITConstants.INSTALL_DIR);
        if (this.installDir == null) {
            this.installDir = "";
        } else if (!this.installDir.endsWith(File.separator)) {
            this.installDir = this.installDir + File.separator;
        }

        this.workingDir = properties.get(ITConstants.WORKING_DIR);
        if (this.workingDir == null) {
            this.workingDir = "";
        } else if (!this.workingDir.endsWith(File.separator)) {
            this.workingDir = this.workingDir + File.separator;
        }

        this.appDir = properties.get(ITConstants.APP_DIR);
        if (this.appDir == null) {
            this.appDir = "";
        }

        this.libPath = properties.get(ITConstants.LIB_PATH);
        if (this.libPath == null) {
            this.libPath = "";
        }

        this.queue = properties.get("queue");
        if (this.queue == null) {
            this.queue = "";
        }

        this.commManager = adaptor;
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

    public void setNode(NIONode node) {
        this.node = node;
    }

    public NIONode getNode() {
        return this.node;
    }

    @Override
    public boolean isTracingReady() {
        return true;
    }

    @Override
    public void waitForTracingReady() {

    }

    @Override
    public void setInternalURI(URI uri) {
        NIOURI nio = new NIOURI(node, uri.getPath());
        uri.setInternalURI(NIOAdaptor.ID, nio);
    }

    @Override
    public Job newJob(Task task, Implementation impl, Resource res, JobListener listener) {
        return new NIOJob(task, impl, res, listener);
    }

    @Override
    public void stop(ShutdownListener sl) {
        System.out.println("Stopping worker " + node);
        logger.info("Shutting down " + node);
        Connection c = TransferManager.startConnection(node);
        commManager.shuttingDown(this, c, sl);
        CommandShutdown cmd = new CommandShutdown(null, null);
        c.sendCommand(cmd);
        c.receive();
        c.finishConnection();
    }

    @Override
    public void sendData(LogicalData ld, DataLocation source, DataLocation target, LogicalData tgtData, Transferable reason, EventListener listener) {
        if (target.getHosts().contains(Comm.appHost)) {//Si es pel master
            //Ordenar la petició directament
            if (tgtData != null) {
                URI u;
                if ((u = ld.alreadyAvailable(Comm.appHost)) != null) {//Already present at the master
                    reason.setDataTarget(u.getPath());
                    listener.notifyEnd(null);
                    return;
                }
            }

            Copy c = new DeferredCopy(ld, null, target, tgtData, reason, listener);
            Data d = new Data(ld);
            if (source != null) {
                for (URI uri : source.getURIs()) {
                    NIOURI nURI = (NIOURI) uri.getInternalURI(NIOAdaptor.ID);
                    if (nURI != null) {
                        d.getSources().add(nURI);
                    }
                }
            }
            String path = target.getPath();
            ld.startCopy(c, c.getTargetLoc());
            NIOAgent.DataRequest dr = new MasterDataRequest(c, reason.getType(), d, path);
            commManager.addTransferRequest(dr);
            commManager.requestTransfers();
        } else {
            orderCopy(new DeferredCopy(ld, source, target, tgtData, reason, listener));
        }

    }

    @Override
    public void obtainData(LogicalData ld, DataLocation source, DataLocation target, LogicalData tgtData, Transferable reason, EventListener listener) {
        orderCopy(new DeferredCopy(ld, source, target, tgtData, reason, listener));
    }

    private void orderCopy(DeferredCopy c) {
        Resource tgtRes = c.getTargetLoc().getHosts().getFirst();
        LogicalData ld = c.getSourceData();
        String path;
        synchronized (ld) {
            URI u;

            if ((c.getTargetData() != null) && (u = ld.alreadyAvailable(tgtRes)) != null) {
                path = u.getPath();
            } else {
                path = c.getTargetLoc().getPath();
            }
            //TODO: MISSING CHECK IF FILE IS ALREADY BEEN COPIED IN A SHARED LOCATION    
            ld.startCopy(c, c.getTargetLoc());
            commManager.registerCopy(c);
        }
        c.setProposedSource(new Data(ld));
        c.setFinalTarget(path);
        c.end(DataOperation.OpEndState.OP_OK);
    }

    @Override
    public void updateTaskCount(int processorCoreCount) {
    }

    @Override
    public void announceDestruction() {
        //No need to do nothing
    }

    @Override
    public void announceCreation() {
        //No need to do nothing
    }

    @Override
    public String getCompletePath(ITExecution.ParamType type, String name) {
        switch (type) {
            case FILE_T:
                return workingDir + name;
            case OBJECT_T:
                return name;
            default:
                return null;
        }
    }

    @Override
    public void deleteTemporary() {
        //System.out.println("NIOWorkerNode hauria d'eliminar " + workingDir + " a " + getName());
    }
}
