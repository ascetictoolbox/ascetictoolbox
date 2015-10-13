/**
 *
 *   Copyright 2014-2015 Barcelona Supercomputing Center (www.bsc.es) All rights reserved.
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

package integratedtoolkit.gat.master;

import integratedtoolkit.ITConstants;
import integratedtoolkit.util.Tracer;
import java.net.URISyntaxException;
import java.util.LinkedList;
import org.gridlab.gat.GAT;
import org.gridlab.gat.GATContext;
import org.gridlab.gat.URI;
import org.gridlab.gat.resources.Job;
import org.gridlab.gat.resources.JobDescription;
import org.gridlab.gat.resources.ResourceBroker;
import org.gridlab.gat.resources.SoftwareDescription;

public class GATTracer extends Tracer {

    private final static GATContext context;

    static {
        context = new GATContext();
        String brokerAdaptor = System.getProperty(ITConstants.GAT_BROKER_ADAPTOR);
        context.addPreference("ResourceBroker.adaptor.name", brokerAdaptor);
    }

    public static void addPreference(String name, String value) {
        context.addPreference(name, value);
    }

    public static Job startTracing(GATWorkerNode worker) {
        if (worker.getLimitOfTasks() <= 0) {
            if (debug) {
                logger.debug("Resource " + worker.getName() + " has 0 slots, it won't appear in the trace");
            }
            return null;
        }
        int hostId = Tracer.registerHost(worker.getName(), worker.getLimitOfTasks());

        String user;
        if (worker.getUser() == null) {
            user = "";
        } else {
            user = worker.getUser() + "@";
        }

        SoftwareDescription sd = new SoftwareDescription();
        String uriString = "any://" + user + worker.getHost();
        sd.addAttribute("uri", uriString);
        sd.setExecutable(worker.getInstallDir() + "/" + Tracer.TRACE_SCRIPT);
        sd.setArguments(new String[]{"init", worker.getWorkingDir(), String.valueOf(hostId), String.valueOf(worker.getLimitOfTasks())});

        if (debug) {
            try {
                org.gridlab.gat.io.File outFile = GAT.createFile(context, "any:///" + System.getProperty(ITConstants.IT_APP_LOG_DIR) + traceOutRelativePath);
                sd.setStdout(outFile);
                org.gridlab.gat.io.File errFile = GAT.createFile(context, "any:///" + System.getProperty(ITConstants.IT_APP_LOG_DIR) + traceErrRelativePath);
                sd.setStderr(errFile);
            } catch (Exception e) {
            }
        }

        sd.addAttribute(SoftwareDescription.SANDBOX_ROOT, "/tmp/");
        sd.addAttribute(SoftwareDescription.SANDBOX_USEROOT, "true");
        sd.addAttribute(SoftwareDescription.SANDBOX_DELETE, "false");

        Job job = null;
        try {
            URI brokerURI = new URI(uriString);
            ResourceBroker broker = GAT.createResourceBroker(context, brokerURI);
            job = broker.submitJob(new JobDescription(sd));
        } catch (Exception e) {
            logger.error("Error initializing tracing system in node " + worker.getHost(), e);
            return null;
        }
        return job;
    }

    public static boolean isReady(Job job) {
        if (job.getState() == Job.JobState.STOPPED) {
            String uri = (String) ((JobDescription) job.getJobDescription()).getSoftwareDescription().getAttributes().get("uri");
            if (logger.isDebugEnabled()) {
                logger.debug("Initialized tracing system in " + uri);
            }
            return true;
        } else if (job.getState() == Job.JobState.SUBMISSION_ERROR) {
            logger.error("Error initializing tracing system, host " + job);
            return true;
        }
        return false;
    }

    public static void waitForTracing(Job job) {
        Long timeout = System.currentTimeMillis() + 60000l;
        while (System.currentTimeMillis() < timeout) {
            if (isReady(job)) {
                return;
            }

            try {
                Thread.sleep(50);
            } catch (Exception e) {
            }
        }
        logger.error("Error initializing tracing system, " + job + " job still pending.");
    }

    public static void generatePackage(GATWorkerNode node) {
        LinkedList<URI> traceScripts = new LinkedList<URI>();
        LinkedList<String> traceParams = new LinkedList<String>();
        String host = node.getHost();
        String installDir = node.getInstallDir();
        String workingDir = node.getWorkingDir();

        String user = node.getUser();
        if (user == null) {
            user = "";
        } else {
            user += "@";
        }

        try {
            traceScripts.add(new URI("any://" + user + host + "/" + installDir + "/" + TRACE_SCRIPT));
        } catch (URISyntaxException e) {
            logger.error("Error deleting tracing host", e);
        }
        String pars = "package " + workingDir + " " + host;

        traceParams.add(pars);

        // Use cleaner to run the trace script and generate the package
        new ScriptExecutor(traceScripts, traceParams);
    }

    /*
     logger.debug("Transfer Trace Files");
     TransferTraceFilesRequest ttracefRequest = (TransferTraceFilesRequest) request;
     String host = ttracefRequest.getHost();
     if (host == null) { // all hosts
     logger.debug("Transfer ALL trace files");
     FTM.transferTraceFiles(ResourceManager.getPhysicalWorkers(), ttracefRequest.getLocation(), ttracefRequest.getSemaphore());
     } else {
     logger.debug("Transfer one file for host " + host);
     List<String> hosts = new LinkedList<String>();
     hosts.add(host);
     FTM.transferTraceFiles(hosts, ttracefRequest.getLocation(), ttracefRequest.getSemaphore());
     }
    
    
    
     public void transferTraceFiles(List<String> hosts, Location targetLocation, Semaphore sem) {
     if (hosts.isEmpty()) {
     sem.release();
     return;
     }
     int groupId = opGroups.addGroup(hosts.size(), FileRole.TRACE_FILE, sem);
     for (String host : hosts) {
     int nslots = Integer.parseInt(ProjectManager.getResourceProperty(host, ITConstants.LIMIT_OF_TASKS));
     if (nslots <= 0) {
     try {
     int nOps = opGroups.removeMember(groupId);
     if (nOps == 0) {
     opGroups.removeGroup(groupId);
     }
     } catch (ElementNotFoundException e) {
     break;
     }
     continue;
     }

     String fileName = host + "_compss_trace.tar.gz";
     String workingDir = ProjectManager.getResourceProperty(host, ITConstants.WORKING_DIR);
     Comm.transferFile(groupId, host, workingDir, fileName, targetLocation);
     }
     }
     */
}
