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

import java.io.IOException;
import java.util.Map;

/**
 * The Runtime Monitor class represents the component in charge to provide user
 * with the current state of the execution.
 */
public class RuntimeMonitor implements Runnable {

    /**
     * Task Dispatcher associated to the monitor
     */
    private TaskDispatcher TD;
    /**
     * Task Processor associated to the monitor
     */
    private TaskProcessor TP;
    /**
     * Time inbetween two state queries
     */
    private long sleepTime;
    /**
     * Monitor keeps making queries
     */
    private boolean keepRunning;
    /**
     * The monitor thread is still alive
     */
    private boolean running;
    /**
     * Monitor Thread
     */
    private Thread monitor;

    /**
     * COMPSs installation directory
     */
    String installDir;

    /**
     * Contructs a new Runtime monitor which periodically checks the current
     * state of the execution and gives the outputs to the user.
     *
     * @param TP Task Processor associated to the monitor
     * @param TD Task Dispatcher associated to the monitor
     * @param sleepTime interval of time between state queries
     */
    public RuntimeMonitor(TaskProcessor TP, TaskDispatcher TD, long sleepTime) {
        this.TD = TD;
        this.TP = TP;
        this.keepRunning = true;
        this.sleepTime = sleepTime;
        installDir = System.getenv().get("IT_HOME");
        monitor = new Thread(this);
        monitor.setName("Monitor Thread");
        monitor.start();
    }

    /**
     * Stops the monitoring
     */
    public void cleanup() {
        this.keepRunning = false;
        monitor.interrupt();

        try {
            while (running) {
                Thread.sleep(50);
            }
            printGraphState();
            getXMLTaskState();
        } catch (Exception e) {
        }

    }

    /**
     * Checks periodically the status of the execution and returns the results
     * to the user
     */
    public void run() {
        running = true;
        while (keepRunning) {
            try {
                printGraphState();
                getXMLTaskState();
                /*for (java.util.Map.Entry<Thread, StackTraceElement[]> entry : Thread.getAllStackTraces().entrySet()) {
                    Thread t = entry.getKey();
                    StackTraceElement[] stackTrace = entry.getValue();
                    StringBuilder sb = new StringBuilder(t.getName() + "\n");
                    String prefix = "└";
                    for (int i = stackTrace.length; i > 0; i--) {
                        sb.append(prefix).append(stackTrace[i - 1]).append("\n");
                        prefix = " " + prefix;
                    }
                    System.out.println(sb.toString());
                }*/
                Thread.sleep(sleepTime);
            } catch (Exception e) {
            }
        }
        running = false;
    }

    /**
     * prints in a file the current state of the dependency graph on a file
     */
    private void printGraphState() throws IOException {
        java.io.BufferedWriter fw = new java.io.BufferedWriter(new java.io.FileWriter(System.getProperty("user.home") + "/monitor.dot"));
        fw.write("digraph {");
        fw.newLine();
        fw.write("ranksep=0.20;");
        fw.newLine();
        fw.write("node[height=0.75];");
        fw.newLine();
        fw.write(TP.getCurrentGraphState());
        fw.newLine();
        fw.write("}");
        fw.close();
        fw = null;
    }

    /**
     * prints in a file the current state of the dependency graph on a file
     */
    private void getXMLTaskState() throws IOException {
        java.io.BufferedWriter fw = new java.io.BufferedWriter(new java.io.FileWriter(System.getProperty("user.home") + "/monitor.xml"));
        StringBuilder sb = new StringBuilder("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n"
                + "<?xml-stylesheet type=\"text/xsl\" href=\"" + installDir + "/xml/monitor/monitor.xsl\"?>\n"
                + "<COMPSsState>\n");
        sb.append(TP.getCurrentTaskState());
        sb.append(TD.getCurrentMonitoringData());
        sb.append("</COMPSsState>");
        fw.write(sb.toString());
        fw.close();
        fw = null;

    }
}
