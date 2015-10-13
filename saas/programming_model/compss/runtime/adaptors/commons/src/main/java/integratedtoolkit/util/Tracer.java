/**
 *
 *   Copyright 2013-2015 Barcelona Supercomputing Center (www.bsc.es) All rights reserved.
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

package integratedtoolkit.util;

import integratedtoolkit.ITConstants;
import integratedtoolkit.log.Loggers;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.atomic.AtomicInteger;

import org.apache.log4j.Logger;

import es.bsc.cepbatools.extrae.Wrapper;
import integratedtoolkit.comm.Comm;
import java.util.Hashtable;

public abstract class Tracer {

    protected static final String ERROR_TRACE_DIR = "ERROR: Cannot create trace directory";
    private static final String taskDesc = "Task";

    protected static final String TRACE_SCRIPT = "trace.sh";
    protected static final String traceOutRelativePath = "/trace/tracer.out";
    protected static final String traceErrRelativePath = "/trace/tracer.err";

    protected static final Logger logger = Logger.getLogger(Loggers.JM_COMP);
    protected static final boolean debug = logger.isDebugEnabled();

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

    private static final String traceDirPath;
    private final static Map<String, TraceHost> hostToSlots;
    private static AtomicInteger hostId;

    static {
    	hostId = new AtomicInteger(1);
        if (debug) {
            logger.debug("Initializing tracing");
        }
        traceDirPath = Comm.appHost.getAppLogDirPath() + "trace" + File.separator;
        if (!new File(traceDirPath).mkdir()) {
            System.err.println(ERROR_TRACE_DIR);
            System.exit(1);
        }

        Wrapper.SetTaskID(0);
        Wrapper.SetNumTasks(1);
        Wrapper.Init();

        hostToSlots = new Hashtable<String, TraceHost>();
    }

    public static String getTraceDirPath() {
        return traceDirPath;
    }

    public static int registerHost(String name, int slots) {
        int id;
        synchronized (hostToSlots) {
            if (hostToSlots.containsKey(name)) {
                if (debug) {
                    logger.debug("Host " + name + " already in tracing system, skipping");
                }
                return -1;
            }
            id = hostId.getAndIncrement();
            hostToSlots.put(name, new TraceHost(slots));
        }
        return id;
    }

    public static int getNextSlot(String host) {
        int slot = hostToSlots.get(host).getNextSlot();
        //logger.debug("Getting slot " + slot + " of host " + host);
        return slot;
    }

    public static void freeSlot(String host, int slot) {
        //logger.debug("Freeing slot " + slot + " of host " + host);
        hostToSlots.get(host).freeSlot(slot);
    }

    public static void fini(Map<String, Integer> signatureToId, String scriptDir, String appName) {
        defineTaskEvent(signatureToId);
        Wrapper.Fini();
        generateTrace(scriptDir, appName);

        //Move trace.out logs to default logger
        try {
            FileReader traceOut = new FileReader(System.getProperty("user.dir") + traceOutRelativePath);
            BufferedReader br = new BufferedReader(traceOut);
            String line = br.readLine();
            while (line != null) {
                logger.debug(line);
                line = br.readLine();
            }
            br.close();
        } catch (Exception e) {
            logger.error("Error moving trace.out", e);
        }
        new File(System.getProperty("user.dir") + traceOutRelativePath).delete();

        //Move trace.err logs to default logger
        try {
            FileReader traceErr = new FileReader(System.getProperty("user.dir") + traceErrRelativePath);
            BufferedReader br = new BufferedReader(traceErr);
            String line = br.readLine();
            while (line != null) {
                logger.error(line);
                line = br.readLine();
            }
            br.close();
        } catch (Exception e) {
            logger.error("Error moving trace.err", e);
        }
        new File(System.getProperty("user.dir") + traceErrRelativePath).delete();
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
        ProcessBuilder pb = new ProcessBuilder(scriptDir + File.separator + TRACE_SCRIPT, "gentrace", System.getProperty(ITConstants.IT_APP_LOG_DIR), appName);
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

    

}
