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
package integratedtoolkit.components.impl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.LinkedList;
import java.util.TreeMap;
import java.util.Collections;
import org.apache.log4j.Logger;

import integratedtoolkit.ITConstants;
import integratedtoolkit.api.ITExecution.*;
import integratedtoolkit.ascetic.Ascetic;
import integratedtoolkit.components.FileTransfer;
import integratedtoolkit.components.JobCreation;
import integratedtoolkit.components.TransferStatus;
import integratedtoolkit.components.JobStatus.JobEndStatus;
import integratedtoolkit.log.Loggers;
import integratedtoolkit.types.Parameter;
import integratedtoolkit.types.Parameter.*;
import integratedtoolkit.types.data.DataInstanceId;
import integratedtoolkit.types.data.Location;
import integratedtoolkit.types.data.DataAccessId.*;
import integratedtoolkit.types.TaskParams;

import integratedtoolkit.util.ProjectManager;
import integratedtoolkit.util.RequestDispatcher;
import integratedtoolkit.util.RequestQueue;
import integratedtoolkit.util.ThreadPool;
import integratedtoolkit.util.Tracer;

import integratedtoolkit.types.Job;
import integratedtoolkit.types.Job.JobHistory;
import integratedtoolkit.types.GATJob;
import integratedtoolkit.types.Implementation;
import integratedtoolkit.types.Job.JobKind;
import integratedtoolkit.types.Task;
import integratedtoolkit.types.Task.TaskState;
import integratedtoolkit.types.WSJob;
import integratedtoolkit.types.Resource;
import integratedtoolkit.util.CoreManager;
import java.util.Hashtable;
import java.util.Map.Entry;

public class JobManager implements JobCreation, TransferStatus {

    // Constants definition
    private static final int POOL_SIZE = 1;
    private static final String POOL_NAME = "JM";
    // private static final int WORKER_ERROR_CODE = 7;
    private static final String THREAD_POOL_ERR = "Error starting pool of threads";
    private static final String STAGING_ERR = "Error staging in job files";
    private static final String TERM_ERR = "Error terminating";
    // Components
    private FileTransferManager FTM;
    private TaskProcessor TP;
    private TaskDispatcher TD;
    // Map : job identifier -> job information
    private Map<Integer, Job> idToJob;
    // Map : requested transfers identifier -> job information
    private Map<Integer, Integer> transferToJob;
    // Pool of worker threads and queue of requests
    private ThreadPool pool;
    private RequestQueue<Integer> queue;
    // Component logger - No need to configure, ProActive does
    private static final Logger logger = Logger.getLogger(Loggers.JM_COMP);
    private static final boolean debug = logger.isDebugEnabled();
    // Tracing
    private static final boolean tracing = System.getProperty(ITConstants.IT_TRACING) != null
            && System.getProperty(ITConstants.IT_TRACING).equals("true")
            ? true : false;
    // Preschedule
    private Map<String, Integer> hostToSlots; // host name -> number of free
    // processors
    private Map<String, List<Integer>> hostToPending; // host name -> list of
    // pending prescheduled
    // jobs
    private static final boolean presched = System.getProperty(ITConstants.IT_PRESCHED) != null
            && System.getProperty(ITConstants.IT_PRESCHED).equals("true") ? true
            : false;
    public static Map<String, LinkedList<String>> hostToObsolete;

    public JobManager() {
        Job.init(this);
        GATJob.init();
        try {
            WSJob.init();
        } catch (Exception e) {
            logger.fatal(THREAD_POOL_ERR, e);
            System.exit(1);
        }

        /*
         * We need to synchronize this maps, since it can be accessed by the job
         * dispatcher thread or by the callback notifier thread from GAT
         */
        idToJob = Collections.synchronizedMap(new TreeMap<Integer, Job>());
        transferToJob = Collections.synchronizedMap(new TreeMap<Integer, Integer>());

        // Create thread that will handle job submission requests
        queue = new RequestQueue<Integer>();

        pool = new ThreadPool(POOL_SIZE, POOL_NAME, new JobDispatcher(queue, this));
        try {
            pool.startThreads();
        } catch (Exception e) {
            logger.fatal(THREAD_POOL_ERR, e);
            System.exit(1);
        }

        if (presched) {
            List<String> hosts = ProjectManager.getWorkers();
            int numHosts = hosts.size();
            hostToSlots = new Hashtable<String, Integer>(numHosts + numHosts / 2 + 1);
            hostToPending = new Hashtable<String, List<Integer>>(numHosts + numHosts / 2 + 1);
            hostToObsolete = new HashMap<String, LinkedList<String>>(numHosts + numHosts / 2 + 1);
            for (String host : hosts) {
                int slots = Integer.parseInt(ProjectManager.getResourceProperty(host,
                        ITConstants.LIMIT_OF_TASKS));
                hostToSlots.put(host, slots);
                hostToPending.put(host, new ArrayList<Integer>(slots));
                hostToObsolete.put(host, new LinkedList<String>());
            }
        } else {
            hostToSlots = new HashMap<String, Integer>();
            hostToPending = new HashMap<String, List<Integer>>();
            hostToObsolete = new HashMap<String, LinkedList<String>>();
        }

        if (tracing) {
            Tracer.init(ProjectManager.getWorkers());
        }

        logger.info("Initialization finished");
    }

    public void setCoWorkers(TaskProcessor TP, TaskDispatcher TD, FileTransferManager FTM) {
        this.TP = TP;
        this.TD = TD;
        this.FTM = FTM;
    }

    public void cleanup() {
        // Make pool threads finish
        try {
            pool.stopThreads();
        } catch (Exception e) {
            // Ignore, we are terminating
        }

        // Cancel all submitted jobs
        synchronized (idToJob) {
            for (Job job : idToJob.values()) {
                try {
                    job.stop();
                } catch (Exception e) {
                    logger.error(TERM_ERR, e);
                }
            }
        }

        GATJob.end();
        WSJob.end();

        if (tracing) {
            Tracer.fini(CoreManager.signatureToId, System.getProperty(ITConstants.IT_SCRIPT_DIR), System.getProperty(ITConstants.IT_APP_NAME));
        }

        logger.info("Cleanup done");
    }

    // JobCreation interface
    public void newJob(Task task, Implementation impl, Resource res) {
        // Store the information of the job
        TaskParams core = task.getTaskParams();
        boolean isService;
        Job job;
        Location transfersLocation;
        if (core.getType() == TaskParams.Type.METHOD) {
            job = new GATJob(task, impl, res);
            isService = false;
        } else {
            job = new WSJob(task, impl, res);
            isService = true;
        }
        transfersLocation = job.getTransfersLocation();
        int jobId = job.getJobId();
        idToJob.put(jobId, job);

        if (debug) {
            logger.debug("New Job (" + jobId + ")");
            logger.debug("  * Method name: " + core.getName());
            logger.debug("  * Target host: " + res.getName());
        }
        orderTransfers(jobId, core.getParameters(), transfersLocation, isService);
    }

    private void orderTransfers(Integer jobId, Parameter[] params,
            Location fileLocation, boolean isService) {
        List<DependencyParameter> parametersToTransfer = new LinkedList<DependencyParameter>();
        for (Parameter p : params) {
            if (debug) {
                logger.debug("    * " + p);
            }

            if (p instanceof DependencyParameter) {
                DependencyParameter dp = (DependencyParameter) p;
                if (isService && dp.getDirection() == ParamDirection.IN) {
                    RAccessId raId = (RAccessId) dp.getDataAccessId();
                    if (WSJob.isInMemory(raId.getReadDataInstance().getRenaming())) {
                        continue;
                    }
                }

                parametersToTransfer.add(dp);
            }
        }

        /*
         * Request needed transfers to the File Transfer Manager, given the file
         * accesses that the task performs and the execution location decided by
         * the Task Scheduler. The working directory is the folder where files
         * will be transferred to. We can transfer either files or objects
         * serialized to files.
         */
        int transferId;
        if (parametersToTransfer.size() > 0) {
            transferId = FTM.getTransferId(parametersToTransfer.size(), FileTransfer.FileRole.JOB_FILE);
            transferToJob.put(transferId, jobId);
            transferId = FTM.transferFiles(transferId, parametersToTransfer, fileLocation);
        } else {
            transferId = FileTransfer.FILES_READY;
        }

        // If no transfers were necessary, we are ready to run the job
        if (transferId == FileTransfer.FILES_READY) {
            transferToJob.remove(transferId);
            submitJob(jobId);
        }
    }

    public void jobRescheduled(Task task, Implementation impl, Resource res) {
        TaskParams core = task.getTaskParams();
        boolean isService;
        Job job;
        Location transfersLocation;
        if (core.getType() == TaskParams.Type.METHOD) {
            job = new GATJob(task, impl, res);
            isService = false;
        } else {
            job = new WSJob(task, impl, res);
            isService = true;
        }
        transfersLocation = job.getTransfersLocation();
        job.setHistory(JobHistory.RESCHEDULED);
        int jobId = job.getJobId();
        idToJob.put(jobId, job);

        if (debug) {
            logger.debug("Rescheduled Job (" + jobId + ")");
            logger.debug("  * Method name: " + core.getName());
            logger.debug("  * Target host: " + res.getName());
        }

        orderTransfers(jobId, core.getParameters(), transfersLocation, isService);
    }

    // TransferStatus interface
    // Transfer threads / TD (only DONE)
    public void fileTransferInfo(int transferId, TransferState status, String message) {
        Integer jobId = transferToJob.remove(transferId);
        if (debug) {
            logger.debug("Received a notification for the transfers of job "
                    + jobId + " with state " + status);
        }

        switch (status) {
            case DONE:
                // Request the submission of the job
                submitJob(jobId);
                break;
            case FAILED:
                Job job = idToJob.get(jobId);
                Task task = job.getTask();
                String resourceName = job.getHostName();
                int implId = job.getImplementation().getImplementationId();
                if (job.getHistory() == JobHistory.RESCHEDULED) {
                    // Already rescheduled job, notify the failure to the Task
                    // Scheduler
                    logger.debug(STAGING_ERR + ": " + message);
                    task.setStatus(TaskState.FAILED);
                    idToJob.remove(jobId);
                    TP.notifyTaskEnd(task, implId, resourceName);
                } else if (job.getHistory() == JobHistory.RESUBMITTED_FILES) {
                    // Try to reschedule
                    logger.debug("Asking for reschedule of job " + jobId
                            + " since " + message);
                    idToJob.remove(jobId);
                    TD.rescheduleJob(task, implId, job.getHostName());
                } else {
                    // Try resubmission of the files to the same host
                    logger.debug("Resubmitting input files of job " + jobId + " to host "
                            + job.getResource().getName() + " since " + message);
                    job.setHistory(JobHistory.RESUBMITTED_FILES);
                    TaskParams core = task.getTaskParams();
                    boolean isService;
                    Location transfersLocation = job.getTransfersLocation();
                    isService = job.getKind() == Job.JobKind.METHOD;
                    orderTransfers(jobId, core.getParameters(), transfersLocation, isService);
                }
                break;
            default:
        }
    }

    private void submitJob(int jobId) {
        if (presched) {
            Job job = idToJob.get(jobId);
            String host = job.getHostName();
            int numSlots = hostToSlots.get(host);
            if (hostToSlots.get(host) > 0) {
                // There is at least one free processor on the host, enqueue
                // for submission
                queue.enqueue(jobId);
                hostToSlots.put(host, --numSlots);
            } else {
                // All the processors in the host are busy, put in pending
                if (debug) {
                    logger.debug("Prescheduling job " + jobId + " at host "
                            + host + ", now pending");
                }
                List<Integer> pending = hostToPending.get(host);
                pending.add(jobId);
            }
        } else {
            queue.enqueue(jobId);
        }

    }

    // Notification thread
    public void jobStatusNotification(Job job, JobEndStatus endStatus) {
        Ascetic.stopEvent(job);
        int jobId = job.getJobId();
        if (debug) {
            logger.debug("Received a notification for job " + jobId
                    + " with state " + endStatus);
        }

        Task task = job.getTask();
        Implementation impl = job.getImplementation();
        String hostName = job.getHostName();
        switch (endStatus) {
            case OK:
                // Job finished, update info about the generated/updated data
                idToJob.remove(jobId);
                Location loc = job.getTransfersLocation();
                for (Parameter p : job.getCore().getParameters()) {
                    if (p instanceof DependencyParameter) {
                        // OUT or INOUT: we must tell the FTM about the generated/updated datum
                        DataInstanceId dId = null;
                        DependencyParameter dp = (DependencyParameter) p;
                        switch (p.getDirection()) {
                            case IN:
                                // FTM already knows about this datum
                                continue;
                            case OUT:
                                dId = ((WAccessId) dp.getDataAccessId()).getWrittenDataInstance();
                                break;
                            case INOUT:
                                dId = ((RWAccessId) dp.getDataAccessId()).getWrittenDataInstance();
                                break;
                        }
                        String name = dId.getRenaming();
                        if (job.getKind() == JobKind.METHOD) {
                            FTM.newDataVersion(name, name, loc);
                        } else {
                            Object value = null; // For dummies (INOUT), we'll pass a null value for the object
                            if (dp.getDirection().equals(ParamDirection.OUT)) {
                                // For WS, we need to store the returned value, if any
                                value = job.getReturnValue();
                                WSJob.setObjectVersionValue(name, value);
                            }
                            FTM.newDataVersion(name, name, value);
                        }
                    }
                }
                task.setStatus(TaskState.FINISHED);
                //TD.notifyJobEnd(task);
                TP.notifyTaskEnd(task, job.getImplementation().getImplementationId(), job.getHostName());
                if (presched) {
                    checkPending(hostName);
                }
                break;

            default: // EXECUTION_FAILED
                switch (job.getHistory()) {
                    case NEW:
                        // Try resubmission to the same host
                        logger.debug("Resubmitting job " + jobId + " to host "
                                + hostName);
                        job.setHistory(JobHistory.RESUBMITTED);
                        queue.enqueue(jobId);
                        break;
                    case RESUBMITTED_FILES:
                        // Try resubmission to the same host
                        logger.debug("Resubmitting job " + jobId + " to host "
                                + hostName);
                        job.setHistory(JobHistory.RESUBMITTED);
                        queue.enqueue(jobId);
                        break;
                    case RESUBMITTED:
                        // Already resubmitted, ask the Task Scheduler for a reschedule
                        // on another host
                        logger.debug("Asking for reschedule of job " + jobId
                                + " due to job failure: " + endStatus);
                        idToJob.remove(jobId);
                        TD.rescheduleJob(task, impl.getImplementationId(), hostName);
                        if (presched) {
                            checkPending(hostName);
                        }
                        break;
                    case RESCHEDULED:
                        // Already rescheduled, notify the failure to the Task Scheduler
                        logger.debug("The rescheduled job " + jobId
                                + " failed again, now in host "
                                + hostName + ": " + endStatus);
                        idToJob.remove(jobId);
                        task.setStatus(TaskState.FAILED);
                        TP.notifyTaskEnd(task, impl.getImplementationId(), hostName);
                        if (presched) {
                            checkPending(hostName);
                        }
                        break;
                }
                break;
        }
    }

    // Private method for preschedule
    private void checkPending(String host) {
        List<Integer> pending = hostToPending.get(host);
        logger.debug("Pending for host " + host + " is " + pending.size());
        if (pending.size() > 0) {
            int preschedJobId = pending.remove(0);
            if (debug) {
                logger.debug("Putting in queue the prescheduled job "
                        + preschedJobId + " for host " + host);
            }
            queue.enqueue(preschedJobId);
        } else {
            int slots = hostToSlots.get(host);
            hostToSlots.put(host, ++slots);
            logger.debug("Now there are " + hostToSlots.get(host)
                    + " empty slots");
        }
    }

    public void obsoleteVersions(HashMap<String, LinkedList<String>> obsoletesMap) {
        for (Entry<String, LinkedList<String>> e : obsoletesMap.entrySet()) {
            logger.info("adding obsoletes to " + e.getKey());
            LinkedList<String> resource = hostToObsolete.get(e.getKey());
            if (resource == null) {
                logger.info("No existing list");
                hostToObsolete.put(e.getKey(), e.getValue());
            } else {
                logger.info("Add all");
                synchronized (resource) {
                    resource.addAll(e.getValue());
                }
            }
        }
    }

    // Thread that handles job submission requests
    private class JobDispatcher extends RequestDispatcher<Integer> {

        public JobDispatcher(RequestQueue<Integer> queue,
                JobManager associatedJM) {
            super(queue);
        }

        public void processRequests() {
            while (true) {
                Integer jobId = null;
                jobId = queue.dequeue();
                if (jobId == null) {
                    break;
                }
                Job job = idToJob.get(jobId);
                try {
                    Ascetic.startEvent(job);
                    job.submit();
                    if (debug) {
                        logger.debug("Job " + jobId + " submitted");
                    }
                } catch (Exception e) {
                    continue;
                }
            }
        }
    }
}
