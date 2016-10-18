package integratedtoolkit.components.monitor.impl;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

import org.apache.log4j.Logger;

import integratedtoolkit.ITConstants;
import integratedtoolkit.components.impl.AccessProcessor;
import integratedtoolkit.components.impl.TaskDispatcher;
import integratedtoolkit.log.Loggers;
import integratedtoolkit.types.WorkloadState;
import integratedtoolkit.util.ResourceManager;

public class RuntimeMonitor implements Runnable {

    private static final boolean monitorEnabled = System.getProperty(ITConstants.IT_MONITOR) != null
            && !System.getProperty(ITConstants.IT_MONITOR).equals("0") ? true : false;

    private static final String monitorDirPath;

    private static final Logger logger = Logger.getLogger(Loggers.ALL_COMP);
    private static final Logger resLogger = Logger.getLogger(Loggers.RESOURCES);
    private static final String ERROR_GENERATING_DATA = "Error generating monitoring data";

    /**
     * Task Dispatcher associated to the monitor
     */
    private TaskDispatcher TD;
    /**
     * Access Processor associated to the monitor
     */
    private AccessProcessor AP;
    /**
     * Graph Generator associated to the monitor
     */
    private GraphGenerator GM;
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

    static {
        // Get the monitorDirPath from the graph because it is always initialized before the RuntimeMonitor
        monitorDirPath = GraphGenerator.getMonitorDirPath();
    }

    /**
     * Constructs a new Runtime monitor. If the monitor parameter has been used,
     * it starts a new thread which periodically checks the current state of the
     * execution and gives the outputs to the user.
     *
     * If only the graph parameter (or none) has been used, the monitor starts
     * but NOT as a thread.
     *
     * @param AP Task Processor associated to the monitor
     * @param TD Task Dispatcher associated to the monitor
     * @param sleepTime interval of time between state queries
     */
    public RuntimeMonitor(AccessProcessor AP, TaskDispatcher TD, GraphGenerator GM, long sleepTime) {
        this.TD = TD;
        this.AP = AP;
        this.GM = GM;

        // Configure and start internal monitor thread
        this.keepRunning = true;
        this.sleepTime = sleepTime;
        installDir = System.getenv().get(ITConstants.IT_HOME);
        monitor = new Thread(this);
        monitor.setName("Monitor Thread");
        monitor.start();
    }

    /**
     * Checks periodically the status of the execution and returns the results
     * to the user
     */
    public void run() {
        running = true;
        while (keepRunning) {
            try {
                // Print XML state for Monitor
            	logger.debug("GetXMLTaskState");
            	getXMLTaskState();

                // Print current task graph
            	logger.debug("printCurrentTaskGraph");
                printCurrentGraph();

                // Print load and resources information on log
                logger.debug("printCurrentLoad");
                printCurrentLoad();
                logger.debug("printResourcesState");
                ResourceManager.printResourcesState();
                logger.debug("Sleeping for "+sleepTime);
                Thread.sleep(sleepTime);
            } catch (Exception e) {
                logger.error(ERROR_GENERATING_DATA, e);
            }
        }
        running = false;
    }

    /**
     * Stops the monitoring
     */
    public void shutdown() {
        this.keepRunning = false;

        try {
            while (running) {
            	logger.debug("Shutdown Sleep for "+sleepTime);
                Thread.sleep(sleepTime);
            }
            
            // Print XML state for Monitor
            logger.debug("SD: GetXMLTaskState");
            getXMLTaskState();

            // Print current task graph
            logger.debug("SD: printCurrentTaskGraph");
            printCurrentGraph();
        } catch (Exception e) {
            logger.error(ERROR_GENERATING_DATA, e);
        }
        //Clears the execution files
        new File(monitorDirPath + "monitor.xml").delete();
    }

    /**
     * Prints in a file the current state of the Task load
     */
    private void getXMLTaskState() throws IOException {
        StringBuilder sb = new StringBuilder();
        sb.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>").append("\n");
        sb.append("<?xml-stylesheet type=\"text/xsl\" href=\"").append(installDir).append("/xml/monitor/monitor.xsl\"?>").append("\n");
        sb.append("<COMPSsState>").append("\n");
        sb.append(AP.getCurrentTaskState());
        sb.append(TD.getCurrentMonitoringData());
        sb.append("</COMPSsState>");

        BufferedWriter fw = new BufferedWriter(new FileWriter(monitorDirPath + "COMPSs_state.xml"));
        fw.write(sb.toString());
        fw.close();
        fw = null;
    }

    /**
     * Prints the current graph to the specified GM file
     */
    private void printCurrentGraph() {
        BufferedWriter graph = this.GM.getAndOpenCurrentGraph();
        this.TD.printCurrentGraph(graph);
        this.GM.closeCurrentGraph();
    }

    public static String getMonitorDirPath() {
        return monitorDirPath;
    }

    public static boolean isEnabled() {
        return monitorEnabled;
    }

    private void printCurrentLoad() {
        WorkloadState load = this.TD.getWorkload();
        resLogger.info(load.toString());
    }

}
