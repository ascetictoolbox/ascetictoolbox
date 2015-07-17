package integratedtoolkit.components.impl;

import java.util.List;
import java.util.Map;
import java.util.LinkedList;
import org.apache.log4j.Logger;

import integratedtoolkit.ITConstants;
import integratedtoolkit.api.ITExecution.*;
import integratedtoolkit.comm.Comm;
import integratedtoolkit.types.job.Job;
import integratedtoolkit.types.job.Job.JobHistory;
import integratedtoolkit.types.job.Job.JobKind;
import integratedtoolkit.log.Loggers;
import integratedtoolkit.types.parameter.Parameter;
import integratedtoolkit.types.parameter.Parameter.*;
import integratedtoolkit.types.data.DataInstanceId;
import integratedtoolkit.types.data.DataAccessId.*;

import integratedtoolkit.types.Implementation;
import integratedtoolkit.types.Task;
import integratedtoolkit.types.Task.TaskState;
import integratedtoolkit.types.data.DataAccessId;
import integratedtoolkit.types.data.LogicalData;
import integratedtoolkit.types.data.location.DataLocation;
import integratedtoolkit.types.data.operation.JobTransfersListener;
import integratedtoolkit.types.job.Job.JobListener.JobEndStatus;
import integratedtoolkit.types.job.JobStatusListener;
import integratedtoolkit.types.resources.Worker;

import integratedtoolkit.util.RequestDispatcher;
import integratedtoolkit.util.RequestQueue;
import integratedtoolkit.util.ThreadPool;
import java.util.HashMap;

public class JobManager {

    // Constants definition
    // private static final int WORKER_ERROR_CODE = 7;
    private static final String STAGING_ERR = "Error staging in job files";

    // Components
    private AccessProcessor TP;
    private TaskDispatcher TD;

    // Component logger - No need to configure, ProActive does
    private static final Logger logger = Logger.getLogger(Loggers.JM_COMP);
    private static final boolean debug = logger.isDebugEnabled();

    private static final boolean presched = System.getProperty(ITConstants.IT_PRESCHED) != null
            && System.getProperty(ITConstants.IT_PRESCHED).equals("true") ? true
            : false;

    public Map<Integer, Worker> enfDataToService;
    public JobDispatcher jd;
    public static final int POOL_SIZE = 1;
    public static final String POOL_NAME = "Job Submitter";

    public JobManager() {
        jd = new JobDispatcher(POOL_SIZE, POOL_NAME);
        enfDataToService = new HashMap<Integer, Worker>();
        /*
         * We need to synchronize this maps, since it can be accessed by the job
         * dispatcher thread or by the callback notifier thread from GAT
         */

        logger.info("Initialization finished");
    }

    public void setCoWorkers(AccessProcessor TP, TaskDispatcher TD) {
        this.TP = TP;
        this.TD = TD;
    }

    public void shutdown() {
        // Cancel all submitted jobs
        jd.stop();
    }

    // JobCreation interface
    public void newJob(Task task, Implementation impl, Worker res) {
        processJob(task, impl, res, JobHistory.NEW);

    }

    public void jobRescheduled(Task task, Implementation impl, Worker res) {
        processJob(task, impl, res, JobHistory.RESCHEDULED);
    }

    private void processJob(Task task, Implementation impl, Worker res, JobHistory history) {
        Job job = null;
        try {
            JobStatusListener listener = new JobStatusListener(res, this);
            job = res.newJob(task, impl, listener);
            job.setHistory(history);
            if (task.getEnforcingData() != null && !task.isSchedulingStrongForced()) { // First operation of the chain registers target service
                enfDataToService.put(task.getEnforcingData().getDataId(), res);
            }
            logger.info((history == JobHistory.NEW ? "New" : "Rescheduled") + " Job " + job.getJobId() + " (Task: " + task.getId() + ")");
            logger.info("  * Method name: " + task.getTaskParams().getName());
            logger.info("  * Target host: " + res.getName());

            checkTransfers(job, res);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void checkTransfers(Job job, Worker res) {
        List<DependencyParameter> parametersToTransfer = new LinkedList<DependencyParameter>();
        Parameter[] params = job.getTask().getTaskParams().getParameters();
        JobTransfersListener listener = new JobTransfersListener(job, res, this);
        job.setTransferGroupId(listener.getId());

        for (Parameter p : params) {
            if (debug) {
                logger.debug("    * " + p);
            }
            if (p instanceof DependencyParameter) {
                DependencyParameter dp = (DependencyParameter) p;
                if (job.getKind() != JobKind.SERVICE || dp.getDirection() != ParamDirection.INOUT) {
                    transferJobData(dp, res, listener);
                }
            }
        }
        listener.enable();
    }

    // Private method that performs file transfers
    private void transferJobData(DependencyParameter param, Worker targetResource, JobTransfersListener listener) {
        DataAccessId access = param.getDataAccessId();
        if (access instanceof WAccessId) {
            String tgtName = ((WAccessId) access).getWrittenDataInstance().getRenaming();
            param.setDataTarget(targetResource.getCompleteRemotePath(param.getType(), tgtName));
            return;
        }

        listener.addOperation();
        if (access instanceof RAccessId) {
            String srcName = ((RAccessId) access).getReadDataInstance().getRenaming();
            targetResource.getData(srcName, srcName, param, listener);
        } else {
            String srcName = ((RWAccessId) access).getReadDataInstance().getRenaming();
            String tgtName = ((RWAccessId) access).getWrittenDataInstance().getRenaming();
            targetResource.getData(srcName, tgtName, (LogicalData) null, param, listener);
        }
    }

    // TransferStatus interface
    // Transfer threads / TD (only DONE)
    public void failedTransfers(Job job, int failedtransfers, Worker res) {
        if (debug) {
            logger.debug("Received a notification for the transfers of job " + job.getJobId() + " with state FAILED");
        }
        Task task = job.getTask();
        int implId = job.getImplementation().getImplementationId();
        if (job.getHistory() == JobHistory.RESCHEDULED) {
            // Already rescheduled job, notify the failure to the Task
            // Scheduler
            if (debug) {
                logger.debug(STAGING_ERR + ": " + failedtransfers + " transfers failed.");
            }
            task.setStatus(TaskState.FAILED);
            TP.notifyTaskEnd(task, implId, res);
        } else if (job.getHistory() == JobHistory.RESUBMITTED_FILES) {
            // Try to reschedule
            if (debug) {
                logger.debug("Asking for reschedule of job " + job.getJobId() + " since " + failedtransfers + " transfers failed.");
            }
            TD.rescheduleJob(task, implId, res);
        } else {
            // Try resubmission of the files to the same host
            if (debug) {
                logger.debug("Resubmitting input files of job " + job.getJobId() + " to host "
                        + job.getResource().getName() + " since " + failedtransfers + " transfers failed.");
            }
            job.setHistory(JobHistory.RESUBMITTED_FILES);
            checkTransfers(job, res);
        }

    }

    public void submitJob(Job job, Worker host) {
        if (debug) {
            logger.debug("Received a notification for the transfers of job " + job.getJobId() + " with state DONE");
        }
        if (presched) {
            if (host.tryAcquirePreschSlot()) {
                // There is at least one free processor on the host, enqueue
                // for submission
                jd.dispatch(job);
            } else {
                // All the processors in the host are busy, put in pending
                if (debug) {
                    logger.debug("Prescheduling job " + job.getJobId() + " at host " + host + ", now pending");
                }
                host.addPendingJob(job);
            }
        } else {
            jd.dispatch(job);
        }

    }

    public void completedJob(Job job, Worker host) {
        int jobId = job.getJobId();
        Task task = job.getTask();
        logger.info("Received a notification for job " + jobId + " with state OK");

        // Job finished, update info about the generated/updated data
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
                        if (job.getKind() == JobKind.SERVICE) {
                            continue;
                        }
                        break;
                }
                String name = dId.getRenaming();

                if (job.getKind() == JobKind.METHOD) {
                    DataLocation outLoc = DataLocation.getLocation(host, dp.getDataTarget());
                    Comm.registerLocation(name, outLoc);
                } else {
                    Object value = job.getReturnValue();
                    Comm.registerValue(name, value);
                }
            }
        }
        task.setStatus(TaskState.FINISHED);
        //TD.notifyJobEnd(task);
        TP.notifyTaskEnd(task, job.getImplementation().getImplementationId(), host);
        if (presched) {
            checkPending(host);
        }
    }

    public void failedJob(Job job, JobEndStatus endStatus, Worker host) {
        int jobId = job.getJobId();
        Task task = job.getTask();
        Implementation impl = job.getImplementation();
        String hostName = host.getName();
        logger.info("Received a notification for job " + jobId + " with state FAILED");
        switch (job.getHistory()) {
            case NEW:
                // Try resubmission to the same host
                if (debug) {
                    logger.debug("Resubmitting job " + jobId + " to host " + hostName);
                }
                job.setHistory(JobHistory.RESUBMITTED);
                jd.dispatch(job);
                break;
            case RESUBMITTED_FILES:
                // Try resubmission to the same host
                if (debug) {
                    logger.debug("Resubmitting job " + jobId + " to host " + hostName);
                }
                job.setHistory(JobHistory.RESUBMITTED);
                jd.dispatch(job);
                break;
            case RESUBMITTED:
                // Already resubmitted, ask the Task Scheduler for a reschedule
                // on another host
                if (debug) {
                    logger.debug("Asking for reschedule of job " + jobId + " due to job failure: " + endStatus);
                }
                TD.rescheduleJob(task, impl.getImplementationId(), host);
                if (presched) {
                    checkPending(host);
                }
                break;
            case RESCHEDULED:
                // Already rescheduled, notify the failure to the Task Scheduler
                if (debug) {
                    logger.debug("The rescheduled job " + jobId + " failed again, now in host "
                            + hostName + ": " + endStatus);
                }
                task.setStatus(TaskState.FAILED);
                TP.notifyTaskEnd(task, impl.getImplementationId(), host);
                if (presched) {
                    checkPending(host);
                }
                break;
        }
    }

    // Private method for preschedule
    private void checkPending(Worker host) {
        Job preschedJob = host.getPendingJob();
        if (preschedJob != null) {
            if (debug) {
                logger.debug("Putting in queue the prescheduled job "
                        + preschedJob.getJobId() + " for host " + host);
            }
            jd.dispatch(preschedJob);
        } else {
            host.releasePreschSlot();
        }
    }

    private class JobDispatcher {

        protected RequestQueue<Job> queue;
        // Pool of worker threads and queue of requests
        private ThreadPool pool;

        private static final String THREAD_POOL_ERR = "Error starting pool of threads";
        private static final String SUBMISSION_ERROR = "Error submitting job ";

        public JobDispatcher(int poolSize, String poolName) {
            queue = new RequestQueue<Job>();
            pool = new ThreadPool(poolSize, poolName, new JobSubmitter(queue));
            try {
                pool.startThreads();
            } catch (Exception e) {
                logger.fatal(THREAD_POOL_ERR, e);
                System.exit(1);
            }
        }

        public void dispatch(Job job) {
            queue.enqueue(job);
        }

        public void stop() {
            try {
                pool.stopThreads();
            } catch (Exception e) {
                // Ignore, we are terminating
            }
        }

        class JobSubmitter extends RequestDispatcher<Job> {

            public JobSubmitter(RequestQueue<Job> queue) {
                super(queue);
            }

            public void processRequests() {
                while (true) {
                    Job job = queue.dequeue();
                    if (job == null) {
                        break;
                    }
                    try {
                        job.submit();
                        if (debug) {
                            logger.debug("Job " + job.getJobId() + " submitted");
                        }
                    } catch (Exception ex) {
                        logger.error(SUBMISSION_ERROR + job.getJobId(), ex);
                        job.getListener().jobFailed(job, Job.JobListener.JobEndStatus.SUBMISSION_FAILED);
                    }
                }
            }
        }
    }
}
