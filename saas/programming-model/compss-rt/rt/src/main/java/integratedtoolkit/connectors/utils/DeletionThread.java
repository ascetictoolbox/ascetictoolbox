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
package integratedtoolkit.connectors.utils;

import integratedtoolkit.components.impl.TaskDispatcher;
import integratedtoolkit.connectors.ConnectorException;
import org.apache.log4j.Logger;
import integratedtoolkit.log.Loggers;
import integratedtoolkit.types.ResourceDestructionRequest;
import integratedtoolkit.util.ProjectManager;

public class DeletionThread extends Thread {

    private Operations operations;
    private ResourceDestructionRequest rdr;
    private static final Logger logger = Logger.getLogger(Loggers.RESOURCES);
    private static Integer count = 0;
    private static TaskDispatcher TD;

    public DeletionThread(Operations connector, ResourceDestructionRequest rdr) {
        this.setName("DeletionThread " + rdr.getRequested().getName());
        synchronized (count) {
            count++;
        }
        this.operations = connector;
        this.rdr = rdr;
    }

    public static int getCount() {
        return count;
    }

    public static void setTaskDispatcher(TaskDispatcher TD) {
        DeletionThread.TD = TD;
    }

    public void run() {
        String name = rdr.getRequested().getName();
        logger.info("Shutting down " + rdr.getRequested().getType() + " of " + name);
        try {
            operations.poweroff(rdr.getRequested());
        } catch (ConnectorException ex) {
            logger.info("Error while trying to shut down the virtual machine " + name);
        }
        if (rdr.isTerminate()) {
            ProjectManager.removeProjectWorker(name);
            try {
                operations.announceDestruction(name, ProjectManager.getAllRegisteredMachines());
            } catch (Exception e) {
                logger.info("Error announcing the termination of virtual machine " + name);
            }
        }
        TD.notifyShutdown(rdr);
        synchronized (count) {
            count--;
        }
    }
}
