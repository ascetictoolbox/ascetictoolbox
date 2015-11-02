/**
 *
 *   Copyright 2015-2015 Barcelona Supercomputing Center (www.bsc.es) All rights reserved.
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
