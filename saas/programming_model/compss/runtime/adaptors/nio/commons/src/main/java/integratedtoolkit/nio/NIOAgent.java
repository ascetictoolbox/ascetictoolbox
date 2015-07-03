package integratedtoolkit.nio;

import java.util.LinkedList;

import es.bsc.comm.Connection;
import es.bsc.comm.TransferManager;
import es.bsc.comm.nio.NIONode;
import es.bsc.comm.transfers.Transfer;
import es.bsc.comm.transfers.Transfer.Destination;
import integratedtoolkit.api.ITExecution;
import integratedtoolkit.log.Loggers;
import integratedtoolkit.nio.commands.CommandDataDemand;
import integratedtoolkit.nio.commands.CommandDataNegate;
import integratedtoolkit.nio.commands.Data;
import integratedtoolkit.types.data.operation.DataOperation;
import integratedtoolkit.util.Serializer;

import java.io.File;
import java.nio.file.Files;
import java.util.HashMap;

import org.apache.log4j.Logger;

public abstract class NIOAgent {

    public static final String ID = NIOAgent.class.getCanonicalName();
    private int sendTransfers;
    private final int MAX_SEND_TRANSFERS;
    private final Connection[] trasmittingConnections;
    private int receiveTransfers;
    private final int MAX_RECEIVE_TRANSFERS;

    public static boolean debug = Logger.getLogger(Loggers.WORKER).isDebugEnabled();

    private boolean finish;
    private Connection closingConnection = null;

    // Requests related to a DataId
    private final HashMap<String, LinkedList<DataRequest>> dataToRequests;
    // Data requests that will be transferred
    private final LinkedList<DataRequest> pendingRequests;
    // Ongoing transfers
    private final HashMap<Connection, String> ongoingTransfers;

    // Transfers to send as soon as there is a slot available
    private LinkedList<Data> prioritaryData;

    // IP of the master node
    protected String masterIP;
    protected static int masterPort;
    protected NIONode masterNode;

    public NIOAgent(int snd, int rcv, int port) {
        sendTransfers = 0;
        MAX_SEND_TRANSFERS = snd;
        trasmittingConnections = new Connection[MAX_SEND_TRANSFERS];
        receiveTransfers = 0;
        MAX_RECEIVE_TRANSFERS = rcv;
        masterPort = port;
        ongoingTransfers = new HashMap<Connection, String>();
        pendingRequests = new LinkedList<DataRequest>();
        dataToRequests = new HashMap<String, LinkedList<DataRequest>>();
        finish = false;
    }

    public abstract void receivedNewTask(NIONode master, NIOTask t, LinkedList<String> obsoleteFiles);

    // Reply the data
    public void sendData(Connection c, Data d) {
        String path = d.getFirstURI().getPath();
        if (path.startsWith("/")) {
            File f = new File(path);
            if (f.exists()) {
                c.sendDataFile(path);
            }
        } else {
            Object o = getObject(path);
            c.sendDataObject(o);
        }
        c.finishConnection();
    }

    // Reply a node saying that we can't send the data
    public void sendDataNegate(Connection c, Data d, boolean hosted) {
        CommandDataNegate cmd = new CommandDataNegate(this, d, hosted);
        c.sendCommand(cmd);
        c.finishConnection();
    }

    // Check if there is a transfer slot available
    private boolean tryAcquireReceiveSlot() {
        boolean b = false;
        synchronized (this) {
            if (receiveTransfers < MAX_RECEIVE_TRANSFERS) {
                receiveTransfers++;
                b = true;
            }
        }
        return b;
    }

    private void releaseReceiveSlot() {
        synchronized (this) {
            receiveTransfers--;
        }
    }

    // Check if there is a transfer slot available
    public boolean tryAcquireSendSlot(Connection c) {
        boolean b = false;
        if (sendTransfers < MAX_SEND_TRANSFERS) {
            sendTransfers++;
            b = true;
            for (int i = 0; i < MAX_SEND_TRANSFERS; i++) {
                if (trasmittingConnections[i] == null) {
                    trasmittingConnections[i] = c;
                    break;
                }
            }
        }
        return b;
    }

    public void releaseSendSlot(Connection c) {
        synchronized (this) {
            for (int i = 0; i < MAX_SEND_TRANSFERS; i++) {
                if (trasmittingConnections[i] == c) {
                    trasmittingConnections[i] = null;
                    sendTransfers--;
                    if (finish) {
                        if (!hasPendingTransfers()) {
                            shutdown(closingConnection);
                        }
                    }
                    break;
                }
            }
        }

    }

    // Check if this node has the data
    public abstract boolean checkData(Data d);

    // A node answered that could not send us the data
    public void receivedDataNegate(Connection c, boolean b) {

    }

    public NIONode getMaster() {
        return masterNode;
    }

    public abstract Object getObject(String s);

    public abstract String getWorkingDir();

    public void receivedShutdown(Connection requester, LinkedList<Data> filesToSend) {

        closingConnection = requester;
        finish = true;

        //ordenar les còpes dels filesToSend
        if (!hasPendingTransfers()) {
            shutdown(closingConnection);
        }
    }

    public abstract void receivedTaskDone(Connection c, int jobID, boolean successful);

    public abstract void copiedData(int transfergroupID);

    public abstract void shutdownNotification(Connection c);

    public abstract static class DataRequest {

        private final Data source;
        private final ITExecution.ParamType type;
        private final String getTarget;

        public DataRequest(ITExecution.ParamType type, Data source, String target) {
            this.source = source;
            this.getTarget = target;
            this.type = type;
        }

        public Data getSource() {
            return source;
        }

        public String getTarget() {
            return getTarget;
        }

        public ITExecution.ParamType getType() {
            return type;
        }

        public static class MasterDataRequest extends DataRequest {

            final DataOperation fOp;

            public MasterDataRequest(DataOperation fOp, ITExecution.ParamType type, Data source, String target) {
                super(type, source, target);
                this.fOp = fOp;
            }

            public DataOperation getOperation() {
                return this.fOp;
            }

        }
    }

    public void addTransferRequest(DataRequest dr) {
        LinkedList<DataRequest> list = dataToRequests.get(dr.source.getName());
        if (list == null) {
            list = new LinkedList<DataRequest>();
            dataToRequests.put(dr.source.getName(), list);
            synchronized (pendingRequests) {
                pendingRequests.add(dr);
            }
        }
        list.add(dr);
    }

    // Check if receive slots available
    public void requestTransfers() {
        DataRequest dr = null;
        synchronized (pendingRequests) {
            if (!pendingRequests.isEmpty() && tryAcquireReceiveSlot()) {
                dr = pendingRequests.remove();
            }
        }
        while (dr != null) {
            Data source = dr.source;
            NIOURI uri = source.getFirstURI();

            NIONode nn = uri.getHost();
            if (nn.ip == null) {
                nn = masterNode;
            }
            Connection c = null;
            try {
                c = TransferManager.startConnection(nn);
                Data remoteData = new Data(source.getName(), uri);
                CommandDataDemand cdd = new CommandDataDemand(this, remoteData);
                ongoingTransfers.put(c, dr.source.getName());
                c.sendCommand(cdd);
                if (dr.type == ITExecution.ParamType.FILE_T) {
                    c.receiveDataFile(dr.getTarget);
                } else {
                    c.receiveDataObject();
                }

            } catch (Exception e) {
                e.printStackTrace(System.err);
            } finally {
                if (c != null) {
                    c.finishConnection();
                }
            }
            synchronized (pendingRequests) {
                if (!pendingRequests.isEmpty() && tryAcquireReceiveSlot()) {
                    dr = pendingRequests.remove();
                } else {
                    dr = null;
                }
            }
        }
    }

    public void receivedData(Connection c, Transfer t) {

        String dataId = ongoingTransfers.remove(c);
        if (dataId == null) { // It has received the output and error of a job execution
            return;
        }
        releaseReceiveSlot();
        LinkedList<DataRequest> requests = dataToRequests.remove(dataId);
        HashMap<String, LinkedList<DataRequest>> byTarget = new HashMap<String, LinkedList<DataRequest>>();
        for (DataRequest req : requests) {
            LinkedList<DataRequest> sameTarget = byTarget.get(req.getTarget());
            if (sameTarget == null) {
                sameTarget = new LinkedList<DataRequest>();
                byTarget.put(req.getTarget(), sameTarget);
            }
            sameTarget.add(req);
        }

        if (byTarget.size() == 1) {
            String targetName = requests.getFirst().getTarget();
            receivedValue(t.getDestination(), targetName, t.getObject(), requests);
        } else {
            if (t.isFile()) {
                receivedValue(t.getDestination(), t.getFileName(), t.getObject(), byTarget.remove(t.getFileName()));
            } else {
                receivedValue(t.getDestination(), dataId, t.getObject(), byTarget.remove(dataId));
            }
            for (java.util.Map.Entry<String, LinkedList<DataRequest>> entry : byTarget.entrySet()) {
                String targetName = entry.getKey();
                LinkedList<DataRequest> reqs = entry.getValue();
                try {
                    if (t.isFile()) {
                        Files.copy((new File(t.getFileName())).toPath(), (new File(targetName)).toPath());
                        receivedValue(t.getDestination(), targetName, t.getObject(), byTarget.remove(targetName));
                    } else {
                        Object o = Serializer.deserialize(t.getArray());
                        receivedValue(t.getDestination(), targetName, o, reqs);
                    }
                } catch (Exception e) {
                    System.err.println("Can not replicate received Data");
                    e.printStackTrace(System.err);
                }

            }
        }
        requestTransfers();

        // Check if shutdown and ready
        if (finish == true && !hasPendingTransfers()) {
            shutdown(closingConnection);
        }
    }

    public abstract void receivedValue(Destination type, String dataId, Object object, LinkedList<DataRequest> achievedRequests);

    public abstract void shutdown(Connection closingConnection);

    public boolean hasPendingTransfers() {
        return !pendingRequests.isEmpty() || sendTransfers != 0 || receiveTransfers != 0;
    }
}
