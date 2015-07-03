package integratedtoolkit.nio.worker;

import java.io.File;
import java.util.LinkedList;

import org.apache.log4j.ConsoleAppender;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.apache.log4j.PatternLayout;

import es.bsc.comm.Connection;

import es.bsc.comm.TransferManager;
import es.bsc.comm.nio.NIONode;
import es.bsc.comm.transfers.Transfer;
import es.bsc.comm.transfers.Transfer.Destination;
import integratedtoolkit.api.ITExecution.ParamType;
import integratedtoolkit.nio.NIOAgent;
import integratedtoolkit.nio.NIOParam;
import integratedtoolkit.nio.NIOTask;
import integratedtoolkit.nio.commands.CommandDataReceived;
import integratedtoolkit.nio.commands.Data;
import integratedtoolkit.log.Loggers;
import integratedtoolkit.nio.NIOMessageHandler;
import integratedtoolkit.nio.commands.CommandShutdownACK;
import integratedtoolkit.nio.commands.CommandTaskDone;
import integratedtoolkit.util.RequestQueue;
import integratedtoolkit.util.ThreadPool;

public class NIOWorker extends NIOAgent {

    public static String workingDir;

    public static int jobThreads;
    public static String POOL_NAME = "NIO_JOBS";
    protected static final String THREAD_POOL_ERR = "Error starting pool of threads";

    protected static final Logger logger = Logger.getLogger(Loggers.WORKER);

    protected static ThreadPool pool;

    public static RequestQueue<NIOTask> jobQueue;

    public static ThreadPrintStream out;
    public static ThreadPrintStream err;

    private final ObjectCache objectCache;

    static {
        try {
            out = new ThreadPrintStream(".out", System.out);
            err = new ThreadPrintStream(".err", System.err);
            System.setErr(err);
            System.setOut(out);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public NIOWorker(int snd, int rcv, int masterPort) {
        super(snd, rcv, masterPort);
        objectCache = new ObjectCache();
        /*tasks = new HashMap<Integer, NIOTask>();
         jobsPending = new HashMap<Integer, LinkedList<String>>();
         transferToJobs = new HashMap<String, LinkedList<Integer>>();*/

        masterNode = null;

        // Start pool of workers
        jobQueue = new RequestQueue<NIOTask>();
        pool = new ThreadPool(jobThreads, POOL_NAME, new JobLauncher(jobQueue, this));
        try {
            pool.startThreads();
        } catch (Exception e) {
            logger.error(THREAD_POOL_ERR, e);
            System.exit(1);
        }
    }

    @Override
    public void receivedNewTask(NIONode master, NIOTask task, LinkedList<String> obsoleteFiles) {
        logger.info("Received Job " + task);
        if (masterNode == null) {
            masterNode = new NIONode(master.ip, masterPort);
        }

        // Remove obsolete
        if (obsoleteFiles != null) {
            removeObsolete(obsoleteFiles);
        }

        TransferringTask tt = new TransferringTask(task);
        // Demand files
        int i = 0;
        for (NIOParam param : task.getParams()) {
            i++;
            if (param.getData() != null) { // A copy was ordered
                boolean exists = false;
                if (param.getType() == ParamType.OBJECT_T) {
                    exists = objectCache.checkPresence((String) param.getValue());
                } else {//paramType == FILE_T
                    File f = new File((String) param.getValue());
                    exists = f.exists();
                }
                if (!exists) {
                    logger.info("\tParameter " + i + "(" + (String) param.getValue() + ") does not exist, requesting data transfer");
                    DataRequest dr = new WorkerDataRequest(tt, param.getType(), param.getData(), (String) param.getValue());
                    addTransferRequest(dr);
                    continue;
                } else {
                    logger.info("\tParameter " + i + "(" + (String) param.getValue() + ") already exists.");
                }
            }
            tt.params--;
        }
        // Request the transfers
        requestTransfers();

        if (tt.params == 0) {
            executeTask(tt.task);
        }

    }

    @Override
    public void receivedValue(Destination type, String dataId, Object object, LinkedList<DataRequest> achievedRequests) {
        logger.info("Received data " + dataId);
        if (type == Transfer.Destination.OBJECT) {
            storeInCache(dataId, object);
        }
        for (DataRequest dr : achievedRequests) {
            WorkerDataRequest wdr = (WorkerDataRequest) dr;
            wdr.task.params--;
            if (wdr.task.params == 0) {
                executeTask(wdr.task.task);
            }
        }
    }

    public void sendTaskDone(NIOTask nt, boolean successful) {
        int taskID = nt.getJobId();
        Connection c = TransferManager.startConnection(masterNode);
        CommandTaskDone cmd = new CommandTaskDone(this, taskID, successful);
        c.sendCommand(cmd);
        if (debug) {
            c.sendDataFile(workingDir + "/jobs/job" + nt.getJobId() + "_" + nt.getHist() + ".out");
            c.sendDataFile(workingDir + "/jobs/job" + nt.getJobId() + "_" + nt.getHist() + ".err");
        } else {
            if (!successful) {
                c.sendDataFile(workingDir + "/jobs/job" + nt.getJobId() + "_" + nt.getHist() + ".err");
            }
        }
        c.finishConnection();
    }

    // Check if this task is ready to execute
    private void executeTask(NIOTask task) {
        logger.debug("Notifying presence of all data for job " + task.getJobId());
        // Notify the master that the data has been transfered
        CommandDataReceived cdr = new CommandDataReceived(this, task.getTransferGroupId());
        Connection c = TransferManager.startConnection(masterNode);
        c.sendCommand(cdr);
        c.finishConnection();

        // Execute the job
        jobQueue.enqueue(task);
    }

    // Remove obsolete files and objects
    public void removeObsolete(LinkedList<String> obsolete) {
        try {
            for (String name : obsolete) {
                if (name.startsWith(File.separator)) {
                    File f = new File(name);
                    f.delete();
                } else {
                    removeFromCache(name);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void receivedUpdateSources(Connection c) {

    }

    // Shutdown the worker, at this point there are no active transfers
    public void shutdown(Connection closingConnection) {
        try {
            // Stop the job threads
            pool.stopThreads();

            // Finish the main thread
            closingConnection.sendCommand(new CommandShutdownACK());
            closingConnection.finishConnection();
            TransferManager.shutdown(closingConnection);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public Object getObject(String s) {
        String realName = s.substring(s.lastIndexOf('/') + 1);
        return objectCache.get(realName);
    }

    public void storeInCache(String name, Object value) {
        objectCache.store(name, value);
    }

    public void removeFromCache(String name) {
        objectCache.remove(name);
    }

    public String getWorkingDir() {
        return workingDir + "/";
    }

    // Check if the data is in this node
    public boolean checkData(Data d) {

        /*boolean b;
         if (d.isFile()) {
         b = fileList.contains(d.getName());
         } else {
         synchronized (objectCache) {
         b = objectCache.containsKey(d.getName());
         }
         }
         return b;*/
        return false;
    }

    // args: debug workingDir numThreads maxSnd maxRcv
    public static void main(String[] args) {
        // Get args
        debug = new Boolean(args[0]);
        workingDir = args[1];
        jobThreads = new Integer(args[2]);
        int maxSnd = new Integer(args[3]);
        int maxRcv = new Integer(args[4]);
        String workerIP = args[5];
        int wPort = new Integer(args[6]);
        int mPort = new Integer(args[7]);

        // Configure worker logger since it doesn't receive the it-log4j file
        ConsoleAppender console = new ConsoleAppender();
        Logger.getRootLogger().setLevel(debug ? Level.DEBUG : Level.OFF);
        String PATTERN = "%d [%p|%c|%C{1}] %m%n";
        console.setLayout(new PatternLayout(PATTERN));
        console.activateOptions();
        Logger.getRootLogger().addAppender(console);

        // Log args
        logger.debug("WorkingDir:" + workingDir);
        logger.debug("jobThreads: " + String.valueOf(jobThreads));
        logger.debug("maxSnd: " + String.valueOf(maxSnd));
        logger.debug("maxRcv: " + String.valueOf(maxRcv));
        logger.debug("WorkerName: " + workerIP);
        logger.debug("WorkerPort: " + String.valueOf(wPort));
        logger.debug("MasterPort: " + String.valueOf(mPort));

        NIOWorker nw = new NIOWorker(maxSnd, maxRcv, mPort);
        NIOMessageHandler mh = new NIOMessageHandler(nw, new NIONode(null, wPort));
        TransferManager.init("NIO", null, mh);
    }

    public static void registerOutputs(String path) {
        err.registerThread(path);
        out.registerThread(path);
    }

    public static void unregisterOutputs() {
        err.unregisterThread();
        out.unregisterThread();
    }

    @Override
    public void receivedTaskDone(Connection c, int jobID, boolean successful) {
        //Should not receive this call
    }

    @Override
    public void copiedData(int transfergroupID) {
        //Should not receive this call
    }

    @Override
    public void shutdownNotification(Connection c) {
        //Never orders the shutdown of a worker peer
    }

    private class WorkerDataRequest extends DataRequest {

        private final TransferringTask task;

        public WorkerDataRequest(TransferringTask task, ParamType type, Data source, String target) {
            super(type, source, target);
            this.task = task;
        }

    }

    private static class TransferringTask {

        NIOTask task;
        int params;

        public TransferringTask(NIOTask task) {
            this.task = task;
            params = task.getParams().size();
        }
    }

}
