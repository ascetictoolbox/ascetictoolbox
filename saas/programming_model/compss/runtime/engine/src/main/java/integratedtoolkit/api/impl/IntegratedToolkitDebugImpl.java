package integratedtoolkit.api.impl;

import integratedtoolkit.ITConstants;
import integratedtoolkit.api.ITDebug;
import integratedtoolkit.components.impl.RuntimeMonitor;
import integratedtoolkit.components.impl.debug.TaskDispatcherDebug;
import integratedtoolkit.components.impl.debug.AccessProcessorDebug;


public class IntegratedToolkitDebugImpl extends IntegratedToolkitImpl implements ITDebug {

    // Components
    AccessProcessorDebug ap;

    public IntegratedToolkitDebugImpl() {
        super();
    }

    // Integrated Toolkit user interface implementation
    public void startIT() {
        if (COMPSs_VERSION == null) {
            logger.info("Starting COMPSs Runtime");
        } else {
            logger.info("Starting COMPSs Runtime v" + COMPSs_VERSION + " (build " + COMPSs_BUILDNUMBER + ")");
        }

        logger.debug("Initializing components");

        td = new TaskDispatcherDebug();
        ap = new AccessProcessorDebug();
        super.ap = ap;
        String sleepTime = System.getProperty(ITConstants.IT_MONITOR);
        if (sleepTime != null) {
            monitor = new RuntimeMonitor(ap, td, Long.parseLong(sleepTime));
        }

        ap.setTD(td);
        td.setTP(ap);

        logger.info("Ready to process tasks");
    }

}
