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
package integratedtoolkit.connectors.utils;

import integratedtoolkit.connectors.ConnectorException;
import org.apache.log4j.Logger;
import integratedtoolkit.log.Loggers;

import integratedtoolkit.types.ResourceDescription;
import integratedtoolkit.types.ResourceCreationRequest;
import integratedtoolkit.components.impl.TaskDispatcher;
import integratedtoolkit.types.CloudImageDescription;
import integratedtoolkit.types.ProjectWorker;
import integratedtoolkit.util.ProjectManager;

public class CreationThread extends Thread {

    private Operations operations;
    private String name; //Id for the CloudProvider or IP if VM is reused
    private String provider;
    private ResourceCreationRequest rcr;
    private Object vm;
    private static TaskDispatcher TD;
    private static Integer count = 0;
    private static final Logger logger = Logger.getLogger(Loggers.RESOURCES);
    private boolean reused;

    public CreationThread(Operations operations, String name, String provider, ResourceCreationRequest rR, boolean reused) {
        this.setName("creationThread");
        this.operations = operations;
        this.provider = provider;
        this.name = name;
        this.rcr = rR;
        this.reused = reused;
        synchronized (count) {
            count++;
        }
    }

    public static int getCount() {
        return count;
    }

    public void run() {
        ResourceDescription requested = rcr.getRequested();
        ResourceDescription granted;
        if (reused) {
            //rcr.setGranted(rcr.getRequested());
            TD.addCloudNode(name, rcr, provider, (int) rcr.getGranted().getProcessorCoreCount(), operations.getCheck());
            synchronized (count) {
                count--;
            }
            return;
        }
        boolean check = operations.getCheck();
        //ASK FOR THE VIRTUAL RESOURCE
        try {
            //turn on the VM and expects the new mr description
            vm = operations.poweron(name, requested);
        } catch (Exception e) {
            logger.info("Error Creating asking a new Resource to " + provider, e);
            vm = null;
        }
        if (vm == null) {
            logger.info(provider + " can not provide this resource.");
            TD.refuseCloudWorkerRequest(rcr, provider);
            synchronized (count) {
                count--;
            }
            return;
        }
        //WAITING FOR THE RESOURCES TO BE RUNNING
        try {
            //wait 'till the vm has been created
            granted = operations.waitCreation(vm, requested);
        } catch (ConnectorException e) {
            logger.info("Error waiting for a machine that should be provided by " + provider, e);
            try {
                operations.destroy(vm);
            } catch (ConnectorException ex) {
                logger.info("Can not poweroff the machine");
            }
            TD.refuseCloudWorkerRequest(rcr, provider);
            synchronized (count) {
                count--;
            }
            return;
        }
        this.setName("creationThread "+granted.getName());
        logger.debug("Requested: ");
        logger.debug("Proc: " + requested.getProcessorCoreCount() + " " + requested.getProcessorArchitecture() + " cores @ " + requested.getProcessorSpeed());
        logger.debug("OS: " + requested.getOperatingSystemType());
        logger.debug("Mem: " + requested.getMemoryVirtualSize() + "(" + requested.getMemoryPhysicalSize() + ")");

        logger.debug("Granted: ");
        logger.debug("Proc: " + granted.getProcessorCoreCount() + " " + granted.getProcessorArchitecture() + " cores @ " + granted.getProcessorSpeed());
        logger.debug("OS: " + granted.getOperatingSystemType());
        logger.debug("Mem: " + granted.getMemoryVirtualSize() + "(" + granted.getMemoryPhysicalSize() + ")");

        logger.info("New resource granted " + granted.getName() + ". Configuring ...");

        ProjectWorker pw;
        CloudImageDescription cid = granted.getImage();
        try {
            //Copy ssh keys
            pw = new ProjectWorker(granted.getName(), provider, cid.getUser(), cid.getiDir(), cid.getwDir());
            ProjectManager.addProjectWorker(pw);
            //CloudManager.addCloudMachine(schedulerName, provider, rr.getGranted().getType());
            operations.configureAccess(granted.getName(), cid.getUser());
            operations.announceCreation(granted.getName(), cid.getUser(), ProjectManager.getAllRegisteredMachines());
        } catch (ConnectorException e) {
            logger.info("Error announcing the machine " + granted.getName() + " in " + provider, e);
            try {
                operations.poweroff(granted);
                //CloudManager.terminate(schedulerName);
                ProjectManager.removeProjectWorker(granted.getName());
                operations.announceDestruction(granted.getName(), ProjectManager.getAllRegisteredMachines());
            } catch (ConnectorException ex) {
            }
            TD.refuseCloudWorkerRequest(rcr, provider);
            synchronized (count) {
                count--;
            }
            return;
        }
        try {
            operations.prepareMachine(granted.getName(), cid);
        } catch (Exception e) {
            e.printStackTrace();
            try {
                operations.poweroff(granted);
                operations.announceDestruction(granted.getName(), ProjectManager.getAllRegisteredMachines());
            } catch (Exception e2) {
                logger.info("Can not poweroff the new resource", e);
            }
            TD.refuseCloudWorkerRequest(rcr, provider);
            synchronized (count) {
                count--;
            }
            logger.info("Exception creating a new Resource ", e);
        }

        //add the new machine to ResourceManager
        if (operations.getTerminate()) {
            logger.info("New resource " + granted.getName() + " has been refused because integratedtoolkit has been stopped");
            try {
                operations.poweroff(granted);
            } catch (Exception e) {
                logger.info("Can not poweroff the new resource", e);
            }
            //CloudManager.terminate(schedulerName);
            try {
                ProjectManager.removeProjectWorker(granted.getName());
                operations.announceDestruction(name, ProjectManager.getAllRegisteredMachines());
            } catch (Exception e) {
                logger.info("Error announcing VM " + granted.getName() + " destruction", e);
            }
            TD.refuseCloudWorkerRequest(rcr, provider);
        } else {
            rcr.setGranted(granted);
            TD.addCloudNode(granted.getName(), rcr, provider, granted.getProcessorCoreCount(), !check && operations.getCheck());
        }
        synchronized (count) {
            count--;
        }
    }

    public static void setTaskDispatcher(TaskDispatcher TD) {
        CreationThread.TD = TD;
    }
}
