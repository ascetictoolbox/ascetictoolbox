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
package integratedtoolkit.nio.master;

import es.bsc.comm.nio.NIONode;
import integratedtoolkit.ITConstants;
import integratedtoolkit.log.Loggers;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.concurrent.atomic.AtomicInteger;

import org.apache.log4j.Logger;

public class WorkerStarter {

    protected static final Logger logger = Logger.getLogger(Loggers.COMM);
    private static final AtomicInteger workerPort = new AtomicInteger(NIOAdaptor.MASTER_PORT);
    private static final int NUM_THREADS = 16;

    public static NIONode startWorker(NIOWorkerNode nw) throws Exception {
        String name = nw.getName();

        int port = workerPort.incrementAndGet();
        logger.debug("Initializing worker " + name);
        logger.debug("Adding worker " + name + " with port " + port);

        String[] command = getStartCommand(nw, port);
        String user = nw.getUser();
        ProcessOut po = executeCommand(user, name, command);

        // Close the ssh connection
        if (po.exitValue == 0) {
            String output = po.output.toString();
            String[] lines = output.split("\n");
            int pid = Integer.parseInt(lines[lines.length - 1]);
            Runtime.getRuntime().addShutdownHook(new Ender(nw, pid));
            Thread.sleep(5000);
            return new NIONode(name, port);
        } else {
            throw new Exception("Could not start the NIO worker in resource " + name + " through user " + user + ".\n"
                    + "OUTPUT:" + po.output.toString()
                    + "ERROR:" + po.error.toString());
        }
    }

    static class ProcessOut {

        int exitValue = -1;
        StringBuffer output = new StringBuffer();
        StringBuffer error = new StringBuffer();

        public void setExitValue(int exit) {
            exitValue = exit;
        }

        public void appendError(String line) {
            error.append(line);
        }

        public void appendOutput(String line) {
            output.append(line + "\n");
        }
    }

    static class Ender extends Thread {

        NIOWorkerNode node;
        int pid;

        Ender(NIOWorkerNode node, int pid) {
            this.node = node;
            this.pid = pid;
        }

        public void run() {
            String user = node.getUser();
            String[] command = getStopCommand(pid);
            executeCommand(user, node.getName(), command);
            command = getCleanCommand(node.getWorkingDir());
            executeCommand(user, node.getName(), command);
        }
    }

    // Arguments needed for worker.sh
    // lang workingDir libpath appDir classpath installDir debug workingDir numThreads maxSend maxReceive name workerPort masterPort
    private static String[] getStartCommand(NIOWorkerNode node, int workerPort) {
        String libPath = node.getLibPath();
        String appDir = node.getAppDir();
        String workingDir = node.getWorkingDir();
        String cp = (System.getProperty(ITConstants.IT_WORKER_CP) != null && System.getProperty(ITConstants.IT_WORKER_CP).compareTo("") != 0) ? System.getProperty(ITConstants.IT_WORKER_CP) : "\"\"";
        String installDir = node.getInstallDir();
        String workerDebug = Boolean.toString(Logger.getLogger(Loggers.WORKER).isDebugEnabled());

        // Gets the max cores of the machine
        // int numThreads = r.getMaxTaskCount();
        String[] cmd = new String[12];
        cmd[0] = installDir + (installDir.endsWith(File.separator) ? "" : File.separator) + "adaptors/nio/persistent_worker.sh";
        cmd[1] = libPath.isEmpty() ? "null" : libPath;
        cmd[2] = appDir.isEmpty() ? "null" : appDir;
        cmd[3] = cp.isEmpty() ? "null" : cp;
        cmd[4] = workerDebug;
        cmd[5] = workingDir;
        cmd[6] = String.valueOf(NUM_THREADS);
        cmd[7] = String.valueOf(NIOAdaptor.MAX_SEND_WORKER);
        cmd[8] = String.valueOf(NIOAdaptor.MAX_RECEIVE_WORKER);
        cmd[9] = node.getName();
        cmd[10] = String.valueOf(workerPort);
        cmd[11] = String.valueOf(NIOAdaptor.MASTER_PORT);

        return cmd;
    }

    private static String[] getStopCommand(int pid) {
        String[] cmd = new String[3];
        cmd[0] = "kill";
        cmd[1] = "-9";
        cmd[2] = String.valueOf(pid);
        return cmd;
    }

    private static String[] getCleanCommand(String wDir) {
        wDir = wDir.endsWith(File.separator) ? wDir : wDir + File.separator;
        String[] cmd = new String[5];
        cmd[0] = "rm";
        cmd[1] = "-rf";
        cmd[2] = wDir + "*.IT";
        cmd[3] = wDir + "jobs";
        cmd[4] = wDir + "log";
        return cmd;
    }

    private static ProcessOut executeCommand(String user, String resource, String[] command) {
        ProcessOut processOut = new ProcessOut();

        String[] cmd = new String[5 + command.length];
        cmd[0] = "ssh";
        cmd[1] = "-o StrictHostKeyChecking=no";
        cmd[2] = "-o BatchMode=yes";
        cmd[3] = "-o ChallengeResponseAuthentication=no";
        cmd[4] = ((user == null) ? "" : user + "@") + resource;
        System.arraycopy(command, 0, cmd, 5, command.length);

        StringBuilder sb = new StringBuilder("");
        for (String param : cmd) {
            sb.append(param).append(" ");
        }
        logger.debug("COMM CMD: " + sb.toString());

        try {
            Process process = Runtime.getRuntime().exec(cmd);
            InputStream stderr = process.getErrorStream();
            InputStream stdout = process.getInputStream();

            process.getOutputStream().close();
            process.waitFor();
            processOut.setExitValue(process.exitValue());

            BufferedReader reader = new BufferedReader(new InputStreamReader(stdout));
            String line;
            while ((line = reader.readLine()) != null) {
                processOut.appendOutput(line);
                logger.debug("COMM CMD OUT: " + line);
            }
            reader = new BufferedReader(new InputStreamReader(stderr));
            while ((line = reader.readLine()) != null) {
                processOut.appendError(line);
                logger.debug("COMM CMD ERR: " + line);
            }
        } catch (Exception e) {

        }
        return processOut;
    }

}
