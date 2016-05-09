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
import es.bsc.comm.transfers.Transfer.Destination;
import java.io.File;
import java.util.HashMap;
import java.util.LinkedList;

import es.bsc.comm.nio.NIONode;
import es.bsc.comm.TransferManager;
import integratedtoolkit.ITConstants;
import integratedtoolkit.api.ITExecution.ParamType;
import integratedtoolkit.comm.Comm;

import integratedtoolkit.comm.CommAdaptor;
import integratedtoolkit.types.job.Job;
import integratedtoolkit.types.data.location.URI;
import integratedtoolkit.nio.NIOAgent;
import integratedtoolkit.nio.NIOAgent.DataRequest.MasterDataRequest;
import integratedtoolkit.nio.NIOMessageHandler;
import integratedtoolkit.nio.NIOTask;
import integratedtoolkit.nio.NIOURI;
import integratedtoolkit.nio.commands.CommandNewTask;
import integratedtoolkit.nio.commands.Data;
import integratedtoolkit.types.data.LogicalData;
import integratedtoolkit.types.data.location.DataLocation;
import integratedtoolkit.types.data.operation.DataOperation;
import integratedtoolkit.types.data.operation.Copy;
import integratedtoolkit.types.data.operation.DataOperation.EventListener;
import integratedtoolkit.types.job.Job.JobHistory;
import integratedtoolkit.types.resources.Resource;
import java.util.HashSet;
import java.util.concurrent.ConcurrentHashMap;
import integratedtoolkit.types.resources.ShutdownListener;
import java.util.concurrent.Semaphore;

public class NIOAdaptor extends NIOAgent implements CommAdaptor {

    public static final int MAX_SEND = 1000;
    public static final int MAX_RECEIVE = 1000;

    public static final int MAX_SEND_WORKER = 5;
    public static final int MAX_RECEIVE_WORKER = 5;
    public static final int MASTER_PORT = 43000;

    private static final String JOBS_DIR = System.getProperty(ITConstants.IT_APP_LOG_DIR) + "jobs" + File.separator;

    private static final String TERM_ERR = "Error terminating";
    private static final String SER_RCV_ERR = "Error serializing received object";

    private static final HashSet<NIOWorkerNode> nodes = new HashSet<NIOWorkerNode>();

    private static final ConcurrentHashMap<Integer, NIOJob> runningJobs = new ConcurrentHashMap<Integer, NIOJob>();

    private static final HashMap<Integer, LinkedList<Copy>> groupToCopy = new HashMap<Integer, LinkedList<Copy>>();

    private static final HashMap<Connection, ClosingWorker> stoppingNodes = new HashMap<Connection, ClosingWorker>();

    public NIOAdaptor() {
        super(MAX_SEND, MAX_RECEIVE, MASTER_PORT);
        File file = new File("jobs/");
        if (!file.exists()) {
            file.mkdir();
        }

    }

    public void init() {
        masterNode = new NIONode(null, MASTER_PORT);
        final NIOMessageHandler mhm = new NIOMessageHandler(this, masterNode);
        (new Thread() {
            public void run() {
                TransferManager.init("NIO", null, mhm);
            }
        }).start();
    }

    public NIOWorkerNode initWorker(String name, HashMap<String, String> properties) throws Exception {
        NIOWorkerNode worker = null;
        worker = new NIOWorkerNode(name, properties, this);
        NIONode n = WorkerStarter.startWorker(worker);
        worker.setNode(n);
        nodes.add(worker);
        return worker;
    }

    public void removedNode(NIOWorkerNode worker) {
        nodes.remove(worker);
    }

    public void stop() {
        HashSet<NIOWorkerNode> workers = new HashSet();
        workers.addAll(nodes);
        Semaphore sem = new Semaphore(0);
        ShutdownListener sl = new ShutdownListener(sem);
        for (NIOWorkerNode worker : workers) {
            logger.info("Stopping "+ worker.getName());
        	worker.stop(sl);
        }
        sl.enable();
        try {
        	logger.debug("Waiting for workers to stop");
            sem.acquire();
        } catch (Exception e) {
        }
        TransferManager.shutdown(null);
    }

    protected static void submitTask(NIOJob job) {
        Resource res = job.getResource();
        NIOWorkerNode node = (NIOWorkerNode) res.getNode();
        NIONode hostNode = node.getNode();
        LinkedList<String> obsolete = res.clearObsoletes();
        runningJobs.put(job.getJobId(), job);
        NIOTask t = job.prepareJob();
        Connection c = TransferManager.startConnection(hostNode);
        CommandNewTask cmd = new CommandNewTask(t, obsolete);
        c.sendCommand(cmd);
        c.finishConnection();
    }

    @Override
    public void receivedNewTask(NIONode master, NIOTask t, LinkedList<String> obsoleteFiles) {
        //Can not run any task. Do nothing
    }

    public void receivedTaskDone(Connection c, int jobId, boolean successful) {
        NIOJob nj = runningJobs.remove(jobId);
        JobHistory h = nj.getHistory();
        nj.taskFinished(successful);
        if (NIOAgent.debug) {
            c.receiveDataFile(JOBS_DIR + "job" + nj.getJobId() + "_" + h + ".out");
            c.receiveDataFile(JOBS_DIR + "job" + nj.getJobId() + "_" + h + ".err");
        } else {
            if (!successful) {
                c.receiveDataFile(JOBS_DIR + "job" + nj.getJobId() + "_" + nj.getHistory() + ".err");
            }
        }
        c.finishConnection();
    }

    public void registerCopy(Copy c) {
        for (EventListener el : c.getEventListeners()) {
            Integer groupId = el.getId();
            LinkedList<Copy> copies = groupToCopy.get(groupId);
            if (copies == null) {
                copies = new LinkedList<Copy>();
                groupToCopy.put(groupId, copies);
            }
            copies.add(c);
        }
    }

    @Override
    public void receivedValue(Destination type, String dataId, Object object, LinkedList<DataRequest> achievedRequests) {
        for (DataRequest dr : achievedRequests) {
            MasterDataRequest mdr = (MasterDataRequest) dr;
            Copy c = (Copy) mdr.getOperation();
            DataLocation actualLocation = c.getSourceData().finishedCopy(c);
            LogicalData tgtData = c.getTargetData();
            if (tgtData != null) {
                tgtData.addLocation(actualLocation);
                if (object != null) {
                    tgtData.setValue(object);
                }
            }
            c.end(DataOperation.OpEndState.OP_OK);
        }
    }

    public void copiedData(int transferGroupId) {
        LinkedList<Copy> copies = groupToCopy.remove(transferGroupId);
        if (copies == null) {
            return;
        }
        for (Copy c : copies) {
            if (!c.isRegistered()) {
                continue;
            }
            DataLocation actualLocation = c.getSourceData().finishedCopy(c);
            LogicalData tgtData = c.getTargetData();
            if (tgtData != null) {
                tgtData.addLocation(actualLocation);
            }
        }
    }

    // Return the data that a worker should be obtaining
    // and has not yet confirmed
    public LinkedList<DataOperation> getPending() {
        return new LinkedList<DataOperation>();
    }

    public boolean checkData(Data d) {
        boolean data = false;
        /*for (Entry<String, LogicalData> e : Comm.DC.nameToLogicalData.entrySet()) {
         if (d.getSourceName().equals(e.getValue().getName())) {
         data = true;
         break;
         }
         }*/
        return data;
    }

    public Object getObject(String name) {
        return Comm.getData(name).getValue();
    }

    public String getWorkingDir() {
        return "";
    }

    public void stopSubmittedJobs() {

        synchronized (runningJobs) {
            for (Job job : runningJobs.values()) {
                try {
                    job.stop();
                } catch (Exception e) {
                    logger.error(TERM_ERR, e);
                }
            }
        }
    }

    @Override
    public void completeMasterURI(URI u) {
        u.setInternalURI(ID, new NIOURI(masterNode, u.getPath()));
    }

    public void requestData(Copy c, ParamType paramType, Data d, String path) {
        DataRequest dr = new MasterDataRequest(c, paramType, d, path);
        addTransferRequest(dr);
        requestTransfers();

    }

    public void shuttingDown(NIOWorkerNode worker, Connection c, ShutdownListener listener) {
        stoppingNodes.put(c, new ClosingWorker(worker, listener));
    }

    @Override
    public void shutdownNotification(Connection c) {
        ClosingWorker closing = stoppingNodes.remove(c);
        NIOWorkerNode worker = closing.worker;
        removedNode(worker);
        ShutdownListener listener = closing.listener;
        listener.notifyEnd();
    }

    @Override
    public void shutdown(Connection closingConnection) {

    }

    private class ClosingWorker {

        private final NIOWorkerNode worker;
        private final ShutdownListener listener;

        public ClosingWorker(NIOWorkerNode w, ShutdownListener l) {
            worker = w;
            listener = l;
        }
    }
}
