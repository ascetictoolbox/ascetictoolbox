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
package integratedtoolkit.api.impl;

import integratedtoolkit.ITConstants;
import integratedtoolkit.api.ITDebug;
import static integratedtoolkit.api.impl.IntegratedToolkitImpl.appHost;
import static integratedtoolkit.api.impl.IntegratedToolkitImpl.masterSafeLocation;
import integratedtoolkit.components.impl.RuntimeMonitor;
import integratedtoolkit.components.impl.debug.TaskDispatcherDebug;
import integratedtoolkit.components.impl.debug.TaskProcessorDebug;
import integratedtoolkit.types.data.DataInstanceId;
import integratedtoolkit.types.data.Location;
import java.util.LinkedList;
import java.util.TreeSet;

public class IntegratedToolkitDebugImpl extends IntegratedToolkitImpl implements ITDebug {
    // Components

    TaskProcessorDebug TP;
    TaskDispatcherDebug TD;

    public IntegratedToolkitDebugImpl() {
        super();
    }

    // Integrated Toolkit user interface implementation
    public void startIT() {
        logger.info("Starting the Integrated Toolkit");

        logger.info("Initializing components");

        TP = new TaskProcessorDebug(appHost, tempDirPath);
        super.TP = TP;
        TD = new TaskDispatcherDebug();
        super.TD = TD;
        String sleepTime = System.getProperty(ITConstants.IT_MONITOR);
        if (sleepTime != null) {
            monitor = new RuntimeMonitor(TP, TD, Long.parseLong(sleepTime));
        }

        TP.setTD(TD);
        TD.setTP(TP);

        masterSafeLocation = new Location(appHost, tempDirPath);

        logger.info("Ready to process tasks");
    }

    public boolean isTaskEnded(int taskId) {
        return TP.isTaskEnded(taskId);
    }

    public LinkedList<DataInstanceId> getFileInstances(String fileName) {
        String name = null, path = null, host = null;
        try {
            String[] hostPathName = extractHostPathName(fileName);
            host = hostPathName[0];
            path = hostPathName[1];
            name = hostPathName[2];
        } catch (Exception e) {
            logger.fatal(FILE_NAME_ERR, e);
            System.exit(1);
        }
        return TP.getFileInstances(host, path, name);
    }

    public TreeSet<String> getDataInstanceLocations(DataInstanceId daId) {
        return TP.getFileLocations(daId);
    }
}
