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
package integratedtoolkit.util;

import integratedtoolkit.ITConstants;
import integratedtoolkit.log.Loggers;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.util.LinkedList;
import java.util.HashMap;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.log4j.Logger;
import org.gridlab.gat.GAT;
import org.gridlab.gat.GATContext;
import org.gridlab.gat.URI;
import org.gridlab.gat.io.File;
import org.gridlab.gat.resources.Job;
import org.gridlab.gat.resources.JobDescription;
import org.gridlab.gat.resources.ResourceBroker;
import org.gridlab.gat.resources.SoftwareDescription;
import org.gridlab.gat.resources.Job.JobState;

import es.bsc.cepbatools.extrae.Wrapper;
import integratedtoolkit.types.WorkerNode;

public class Tracer {

    private static final String TRACE_SCRIPT = "trace.sh";

    public static enum EventType {

        TASK(8000000),
        TRANSFER(8000001);
        // WAIT_FOR_TRANSFER

        private int code;

        private EventType(int code) {
            this.code = code;
        }

        public int getCode() {
            return code;
        }

    }

    private static final String taskDesc = "Task";

    private static final Logger logger = Logger.getLogger(Loggers.JM_COMP);

    private static List<String> hosts;
    private static Map<String, TraceHost> hostToSlots;

    public static void init(List<String> nodes) {
        logger.info("Init tracing");
        Wrapper.SetTaskID(0);
        Wrapper.SetNumTasks(1);
        Wrapper.Init();

        hosts = nodes;
        hostToSlots = new HashMap<String, TraceHost>(hosts.size());

        GATContext context = new GATContext();
        String brokerAdaptor = System.getProperty(ITConstants.GAT_BROKER_ADAPTOR);
        context.addPreference("ResourceBroker.adaptor.name", brokerAdaptor);

        // Initialize the tracing system by sending to each node the node id and the number of slots (threads) in that node
        int hostId = 1;
        for (String host : hosts) {
            if (host.startsWith("http://")) {
                continue;
            }

            String nslots = ProjectManager.getResourceProperty(host, ITConstants.LIMIT_OF_TASKS);
            int intNSlots = Integer.parseInt(nslots);
            if (intNSlots <= 0) {
                continue;
            }

            if (intNSlots == Integer.MAX_VALUE) {
                WorkerNode worker = (WorkerNode) ResourceManager.getResource(host);
                intNSlots = worker.getDescription().getProcessorCoreCount();
            }

            if (intNSlots <= 0) {
                logger.debug("Resource " + host + " has 0 slots, it won't appear in the trace");
                continue;
            }
            hostToSlots.put(host, new TraceHost(intNSlots));
            String installDir = ProjectManager.getResourceProperty(host, ITConstants.INSTALL_DIR);
            String workingDir = ProjectManager.getResourceProperty(host, ITConstants.WORKING_DIR);
            String user = ProjectManager.getResourceProperty(host, ITConstants.USER);
            if (user == null) {
                user = "";
            } else {
                user += "@";
            }

            SoftwareDescription sd = new SoftwareDescription();
            String uriString = "any://" + user + host;
            sd.addAttribute("uri", uriString);
            sd.setExecutable(installDir + "/" + TRACE_SCRIPT);
            sd.setArguments(new String[]{"init", workingDir, String.valueOf(hostId++), String.valueOf(intNSlots)});

            if (logger.isDebugEnabled()) {
                try {
                    org.gridlab.gat.io.File outFile = GAT.createFile(context, "any:///tracer.out");
                    sd.setStdout(outFile);
                    org.gridlab.gat.io.File errFile = GAT.createFile(context, "any:///tracer.err");
                    sd.setStderr(errFile);
                } catch (Exception e) {
                }
            }

            sd.addAttribute(SoftwareDescription.SANDBOX_ROOT, "/tmp/");
            sd.addAttribute(SoftwareDescription.SANDBOX_USEROOT, "true");
            sd.addAttribute(SoftwareDescription.SANDBOX_DELETE, "false");

            LinkedList<Job> jobs = new LinkedList<Job>();
            try {
                URI brokerURI = new URI(uriString);
                ResourceBroker broker = GAT.createResourceBroker(context, brokerURI);
                jobs.add(broker.submitJob(new JobDescription(sd)));
            } catch (Exception e) {
                logger.error("Error initializing tracing system in node " + host, e);
                return;
            }

            Long timeout = System.currentTimeMillis() + 60000l;
            while (jobs.size() > 0 && System.currentTimeMillis() < timeout) {
                ListIterator<Job> it = jobs.listIterator();
                while (it.hasNext()) {
                    Job job = it.next();
                    if (job.getState() == JobState.STOPPED) {
                        String uri = (String) ((JobDescription) job.getJobDescription()).getSoftwareDescription().getAttributes().get("uri");
                        it.remove();
                        logger.info("Initialized tracing system in " + uri);
                    } else if (job.getState() == JobState.SUBMISSION_ERROR) {
                        logger.error("Error initializing tracing system, job " + job);
                        return;
                    }
                }
                try {
                    Thread.sleep(50);
                } catch (Exception e) {
                }
            }
            if (jobs.size() > 0) {
                logger.error("Error initializing tracing system, " + jobs.size() + " nodes still pending: " + jobs);
            }
        }
    }

    public static void fini(Map<String, Integer> signatureToId, String scriptDir, String appName) {
        defineTaskEvent(signatureToId);
        Wrapper.Fini();
        generateTrace(scriptDir, appName);
    }

    private static void defineTaskEvent(Map<String, Integer> signatureToId) {
        int nvalues = signatureToId.size() + 1;
        long[] values = new long[nvalues];
        String[] descriptionValues = new String[nvalues];
        int i = 1;
        values[0] = 0;
        descriptionValues[0] = "End";
        for (Entry<String, Integer> entry : signatureToId.entrySet()) {
            String signature = entry.getKey();
            Integer methodId = entry.getValue();
            String methodName = signature.substring(0, signature.indexOf('('));
            values[i] = methodId + 1;
            descriptionValues[i] = methodName;
            i++;
        }
        Wrapper.defineEventType(EventType.TASK.getCode(), taskDesc, (long) nvalues, values, descriptionValues);
    }

    private static void generateTrace(String scriptDir, String appName) {
        ProcessBuilder pb = new ProcessBuilder(scriptDir + "/" + TRACE_SCRIPT, "gentrace", appName);
        Process p = null;
        try {
            p = pb.start();
        } catch (IOException e) {
            logger.error("Error generating trace", e);
            return;
        }
        StreamGobbler outputGobbler = new StreamGobbler(p.getInputStream(), System.out);
        StreamGobbler errorGobbler = new StreamGobbler(p.getErrorStream(), System.err);
        outputGobbler.start();
        errorGobbler.start();

        try {
            int exitCode = p.waitFor();
            if (exitCode != 0) {
                logger.error("Error generating trace, exit code " + exitCode);
            }
        } catch (InterruptedException e) {
        }
    }

    public static int getNextSlot(String host) {
        int slot = hostToSlots.get(host).getNextSlot();
        logger.debug("Getting slot " + slot + " of host " + host);
        return slot;
    }

    public static void freeSlot(String host, int slot) {
        logger.debug("Freeing slot " + slot + " of host " + host);
        hostToSlots.get(host).freeSlot(slot);
    }

    private static class TraceHost {

        private boolean[] slots;
        private int numFreeSlots;
        private int nextSlot;

        public TraceHost(int nslots) {
            this.slots = new boolean[nslots];
            this.numFreeSlots = nslots;
            this.nextSlot = 0;
        }

        public int getNextSlot() {
            if (numFreeSlots-- > 0) {
                while (slots[nextSlot]) {
                    nextSlot = (nextSlot + 1) % slots.length;
                }
                slots[nextSlot] = true;
                return nextSlot;
            } else {
                return -1;
            }
        }

        public void freeSlot(int slot) {
            slots[slot] = false;
            nextSlot = slot;
            numFreeSlots++;
        }
    }

    private static class StreamGobbler extends Thread {

        InputStream is;
        PrintStream ps;

        private StreamGobbler(InputStream is, PrintStream ps) {
            this.is = is;
            this.ps = ps;
        }

        @Override
        public void run() {
            this.setName("Tracer Stream Gobbler");
            try {
                InputStreamReader isr = new InputStreamReader(is);
                BufferedReader br = new BufferedReader(isr);
                String line = null;
                while ((line = br.readLine()) != null) {
                    ps.println(line);
                }
            } catch (IOException ioe) {
                ioe.printStackTrace();
            }
        }
    }

}
