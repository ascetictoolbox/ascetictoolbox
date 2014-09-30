/*
 *  Copyright 2002-2014 Barcelona Supercomputing Center (www.bsc.es)
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
package integratedtoolkit.types;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.TreeMap;
import java.util.Map;

import org.gridlab.gat.GAT;
import org.gridlab.gat.GATContext;
import org.gridlab.gat.GATInvocationException;
import org.gridlab.gat.URI;
import org.gridlab.gat.io.File;
import org.gridlab.gat.monitoring.Metric;
import org.gridlab.gat.monitoring.MetricDefinition;
import org.gridlab.gat.monitoring.MetricEvent;
import org.gridlab.gat.monitoring.MetricListener;
import org.gridlab.gat.resources.HardwareResourceDescription;
import org.gridlab.gat.resources.Job;
import org.gridlab.gat.resources.Job.JobState;
import org.gridlab.gat.resources.JobDescription;
import org.gridlab.gat.resources.ResourceBroker;
import org.gridlab.gat.resources.ResourceDescription;
import org.gridlab.gat.resources.SoftwareDescription;

import integratedtoolkit.ITConstants;
import integratedtoolkit.api.ITExecution.*;
import integratedtoolkit.components.JobStatus.JobEndStatus;
import integratedtoolkit.components.impl.JobManager;
import integratedtoolkit.types.Parameter.*;
import integratedtoolkit.types.data.DataAccessId;
import integratedtoolkit.types.data.DataAccessId.*;
import integratedtoolkit.types.data.Location;
import integratedtoolkit.util.ProjectManager;
import integratedtoolkit.util.Tracer;

import java.util.LinkedList;
import java.util.Map.Entry;

public class GATJob extends integratedtoolkit.types.Job implements MetricListener {

    private static final String JOB_DIR_CREATION_ERR = "Error creating job output/error directory";
    private static final String CALLBACK_PROCESSING_ERR = "Error processing callback for job";
    private static final String ANY_PROT = "any://";
    private static final String JOB_STATUS = "job.status";
    private static final String RES_ATTR = "machine.node";
    private static final String JOB_PREPARATION_ERR = "Error preparing job";
    private static final String RB_CREATION_ERR = "Error creating resource broker";
    private static final String JOB_SUBMISSION_ERR = "Error submitting job";
    private Job GATjob;
    // GAT context
    private static GATContext context;
    // GAT broker adaptor information
    private static boolean usingGlobus;
    private static boolean userNeeded;
    // Brokers - TODO: Problem if many resources used
    private static Map<String, ResourceBroker> brokers;
    // Worker classpath
    private static final String workerClasspath
            = (System.getProperty(ITConstants.IT_WORKER_CP) != null && System.getProperty(ITConstants.IT_WORKER_CP).compareTo("") != 0)
            ? System.getProperty(ITConstants.IT_WORKER_CP)
            : "\"\"";
    // Language
    private static final String lang = System.getProperty(ITConstants.IT_LANG);
    // Tracing
    private static final boolean tracing = System.getProperty(ITConstants.IT_TRACING) != null
            && System.getProperty(ITConstants.IT_TRACING).equals("true")
            ? true : false;
    private static final String WORKER_SCRIPT = "worker.sh";

    private final ExecutionParams execParams;

    public static void init() {
        if (context == null) {
            context = new GATContext();
            String brokerAdaptor = System.getProperty(ITConstants.GAT_BROKER_ADAPTOR),
                    fileAdaptor = System.getProperty(ITConstants.GAT_FILE_ADAPTOR);
            context.addPreference("ResourceBroker.adaptor.name", brokerAdaptor);
            context.addPreference("File.adaptor.name", fileAdaptor + ", local");
            usingGlobus = brokerAdaptor.equalsIgnoreCase("globus");
            userNeeded = brokerAdaptor.regionMatches(true, 0, "ssh", 0, 3);
            for (Entry<String, String> e : ProjectManager.getJobAdaptorPreferences().entrySet()) {
                context.addPreference(e.getKey(), e.getValue());
            }
            if (debug) {
                try {
                    File jobsDir = GAT.createFile(context, "any:///jobs");
                    jobsDir.mkdir();
                } catch (Exception e) {
                    logger.fatal(JOB_DIR_CREATION_ERR, e);
                    System.exit(1);
                }
            }

        }
        brokers = new TreeMap<String, ResourceBroker>();
    }

    public GATJob(Task task, Implementation impl, Resource res) {
        super(task, impl, res);
        this.execParams = getExecParams();
    }

    public JobKind getKind() {
        return JobKind.METHOD;
    }

    public void submit() throws Exception {
        // Prepare the job
        JobDescription jobDescr = null;
        try {
            jobDescr = prepareJob();
        } catch (Exception e) {
            logger.fatal(JOB_PREPARATION_ERR + ": " + this, e);
            System.exit(1);
        }

        // Get a broker for the host
        ResourceBroker broker = null;
        try {
            String dest = (String) jobDescr.getResourceDescription().getResourceAttribute(RES_ATTR);
            if ((broker = brokers.get(dest)) == null) {
                broker = GAT.createResourceBroker(context, new URI(dest));
                brokers.put(dest, broker);
            }
        } catch (Exception e) {
            logger.fatal(RB_CREATION_ERR, e);
            System.exit(1);
        }
        // Submit the job, registering for notifications of job state transitions (associatedJM is the metric listener)
        Job job = null;

        try {
            job = broker.submitJob(jobDescr, this, JOB_STATUS);
        } catch (Exception e) {
            logger.error(JOB_SUBMISSION_ERR + ": " + this, e);
            Tracer.freeSlot(execParams.getHost(), (Integer) jobDescr.getSoftwareDescription().getAttributes().get("slot"));
            associatedJM.jobStatusNotification(this, JobEndStatus.SUBMISSION_FAILED);
            throw e;
        }

        // Update mapping
        GATjob = job;
    }

    public void stop() throws Exception {
        if (GATjob != null) {
            MetricDefinition md = GATjob.getMetricDefinitionByName(JOB_STATUS);
            Metric m = md.createMetric();
            GATjob.removeMetricListener(this, m);
            GATjob.stop();
        }
    }

    // MetricListener interface implementation
    public void processMetricEvent(MetricEvent value) {
        Job job = (Job) value.getSource();
        JobState newJobState = (JobState) value.getValue();
        JobDescription jd = (JobDescription) job.getJobDescription();
        SoftwareDescription sd = jd.getSoftwareDescription();
        Integer jobId = (Integer) sd.getAttributes().get("jobId");

        /* Check if either the job has finished or there has been a submission error.
         * We don't care about other state transitions
         */
        if (newJobState == JobState.STOPPED) {
            if (tracing) {
                Integer slot = (Integer) sd.getAttributes().get("slot");
                Tracer.freeSlot(execParams.getHost(), slot);
            }

            /* We must check whether the chosen adaptor is globus
             * In that case, since globus doesn't provide the exit status of a job,
             * we must examine the standard error file
             */
            try {
                if (usingGlobus) {
                    File errFile = sd.getStderr();
                    // Error file should always be in the same host as the IT
                    File localFile = GAT.createFile(context, errFile.toGATURI());
                    if (localFile.length() > 0) {
                        GATjob = null;
                        associatedJM.jobStatusNotification(this, JobEndStatus.EXECUTION_FAILED);
                    } else {
                        if (!debug) {
                            localFile.delete();
                        }
                        associatedJM.jobStatusNotification(this, JobEndStatus.OK);
                    }
                } else {
                    if (job.getExitStatus() == 0) {
                        associatedJM.jobStatusNotification(this, JobEndStatus.OK);
                    } else {
                        GATjob = null;
                        associatedJM.jobStatusNotification(this, JobEndStatus.EXECUTION_FAILED);
                    }
                }
            } catch (Exception e) {
                logger.fatal(CALLBACK_PROCESSING_ERR + ": " + this, e);
                System.exit(1);
            }
        } else if (newJobState == JobState.SUBMISSION_ERROR) {
            if (tracing) {
                Integer slot = (Integer) sd.getAttributes().get("slot");
                Tracer.freeSlot(execParams.getHost(), slot);
            }

            try {
                if (debug) {
                    logger.debug("Job info for job " + jobId + ": " + job.getInfo() + "\n" + this);
                }

                if (usingGlobus && job.getInfo().get("resManError").equals("NO_ERROR")) {
                    associatedJM.jobStatusNotification(this, JobEndStatus.OK);
                } else {
                    GATjob = null;
                    associatedJM.jobStatusNotification(this, JobEndStatus.SUBMISSION_FAILED);
                }
            } catch (GATInvocationException e) {
                logger.fatal(CALLBACK_PROCESSING_ERR + ": " + this, e);
                System.exit(1);
            }
        }
    }

    public static void end() {
        GAT.end();
    }

    private JobDescription prepareJob() throws Exception {
        // Get the information related to the job
        Method method = (Method) this.impl;
        TaskParams taskParams = this.task.getTaskParams();
        String methodName = taskParams.getName();

        String targetPath = execParams.getInstallDir();
        String targetHost = execParams.getHost();
        String targetUser = execParams.getUser();
        if (userNeeded && targetUser != null) {
            targetUser += "@";
        } else {
            targetUser = "";
        }

        SoftwareDescription sd = new SoftwareDescription();
        sd.setExecutable(targetPath + "/" + WORKER_SCRIPT);
        ArrayList<String> lArgs = new ArrayList<String>();

        // Common arguments: language working_dir lib_path num_obsolete [obs1 ... obsN] tracing [event_type task_id slot_id]
        lArgs.add(lang);
        lArgs.add(execParams.getWorkingDir());
        lArgs.add(execParams.getLibPath());
        LinkedList<String> obsoleteFiles = JobManager.hostToObsolete.get(targetHost);
        if (obsoleteFiles != null) {
            lArgs.add("" + obsoleteFiles.size());
            synchronized (obsoleteFiles) {
                for (String renaming : obsoleteFiles) {
                    if (debug) {
                        logger.debug("Ordering the removal of obsolete file " + renaming + " in host " + targetHost);
                    }
                    lArgs.add(renaming);
                }
                obsoleteFiles.clear();
            }
        } else {
            lArgs.add("0");
        }
        lArgs.add(Boolean.toString(tracing));
        if (tracing) {
            lArgs.add(String.valueOf(Tracer.EventType.TASK.getCode())); // event type
            lArgs.add(String.valueOf(this.task.getId() + 1)); // task id
            int slot = Tracer.getNextSlot(targetHost);
            lArgs.add(String.valueOf(slot)); // slot id
            sd.addAttribute("slot", slot);
        }

        // Language-dependent arguments: app_dir classpath debug method_class method_name has_target num_params par_type_1 par_1 ... par_type_n par_n
        lArgs.add(execParams.getAppDir());
        lArgs.add(workerClasspath);
        lArgs.add(workerDebug);
        lArgs.add(method.getDeclaringClass());
        lArgs.add(methodName);
        lArgs.add(Boolean.toString(taskParams.hasTargetObject()));
        int numParams = taskParams.getParameters().length;
        if (taskParams.hasReturnValue()) {
            numParams--;
        }
        lArgs.add(Integer.toString(numParams));
        for (Parameter param : taskParams.getParameters()) {
            ParamType type = param.getType();
            lArgs.add(Integer.toString(type.ordinal()));
            if (type == ParamType.FILE_T || type == ParamType.OBJECT_T) {
                DependencyParameter dPar = (DependencyParameter) param;
                DataAccessId dAccId = dPar.getDataAccessId();
                lArgs.add(dPar.getDataRemotePath());
                if (type == ParamType.OBJECT_T) {
                    if (dAccId instanceof RAccessId) {
                        lArgs.add("R");

                    } else {
                        lArgs.add("W"); // for the worker to know it must write the object to disk

                    }
                }

            } else if (type == ParamType.STRING_T) {
                BasicTypeParameter btParS = (BasicTypeParameter) param;
                // Check spaces
                String value = btParS.getValue().toString();
                int numSubStrings = value.split(" ").length;
                lArgs.add(Integer.toString(numSubStrings));
                lArgs.add(value);
            } else { // Basic types
                BasicTypeParameter btParB = (BasicTypeParameter) param;
                lArgs.add(btParB.getValue().toString());
            }
        }

        // Conversion vector -> array
        String[] arguments = new String[lArgs.size()];
        arguments = lArgs.toArray(arguments);
        sd.setArguments(arguments);

        sd.addAttribute("jobId", jobId);
        sd.addAttribute(SoftwareDescription.SANDBOX_ROOT, "/tmp/");
        sd.addAttribute(SoftwareDescription.SANDBOX_USEROOT, "true");
        sd.addAttribute(SoftwareDescription.SANDBOX_DELETE, "false");
        /*sd.addAttribute(SoftwareDescription.SANDBOX_PRESTAGE_STDIN, "false");
         sd.addAttribute(SoftwareDescription.SANDBOX_POSTSTAGE_STDOUT, "false");
         sd.addAttribute(SoftwareDescription.SANDBOX_POSTSTAGE_STDERR, "false");*/

        if (debug) {
            // Set standard output file for job
            File outFile = GAT.createFile(context, "any:///jobs/job" + jobId + "_" + this.getHistory() + ".out");
            sd.setStdout(outFile);
        }

        if (debug || usingGlobus) {
            // Set standard error file for job
            File errFile = GAT.createFile(context, "any:///jobs/job" + jobId + "_" + this.getHistory() + ".err");
            sd.setStderr(errFile);
        }

        Map<String, Object> attributes = new HashMap<String, Object>();
        attributes.put(RES_ATTR, ANY_PROT + targetUser + targetHost);
        ResourceDescription rd = new HardwareResourceDescription(attributes);

        if (debug) {
            logger.debug("Ready to submit job " + jobId + ":");
            logger.debug("  * Host: " + targetHost);
            logger.debug("  * Executable: " + sd.getExecutable());

            StringBuilder sb = new StringBuilder("  - Arguments:");
            for (String arg : sd.getArguments()) {
                sb.append(" ").append(arg);
            }
            logger.debug(sb.toString());
        }

        return new JobDescription(sd, rd);
    }

    public String toString() {
        StringBuilder buffer = new StringBuilder();
        buffer.append("[[Job id: ").append(getJobId()).append("]");
        buffer.append(", ").append(getCore().toString());
        buffer.append(", [Target host: ").append(execParams.getHost()).append("]");
        buffer.append(", [User: ").append(execParams.getUser()).append("]]");
        return buffer.toString();
    }

    @Override
    public Location getTransfersLocation() {
        return new Location(res.getName(), execParams.getWorkingDir());
    }

    @Override
    public String getHostName() {
        return res.getName();
    }

    private ExecutionParams getExecParams() {
        String resourceName = res.getName();
        String installDir = ProjectManager.getResourceProperty(resourceName, ITConstants.INSTALL_DIR);
        String workingDir = ProjectManager.getResourceProperty(resourceName, ITConstants.WORKING_DIR);
        String appDir = ProjectManager.getResourceProperty(resourceName, ITConstants.APP_DIR);
        String libPath = ProjectManager.getResourceProperty(resourceName, ITConstants.LIB_PATH);
        String user = ProjectManager.getResourceProperty(resourceName, ITConstants.USER);

        // Prepare the execution parameters
        return new ExecutionParams(user,
                resourceName,
                installDir,
                workingDir,
                appDir,
                libPath);
    }
}
