/**
 *
 *   Copyright 2014-2015 Barcelona Supercomputing Center (www.bsc.es) All rights reserved.
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

package integratedtoolkit.connectors.utils;

import integratedtoolkit.ITConstants;
import integratedtoolkit.components.ResourceUser;
import integratedtoolkit.connectors.ConnectorException;
import integratedtoolkit.connectors.VM;
import integratedtoolkit.log.Loggers;
import integratedtoolkit.types.resources.description.CloudMethodResourceDescription;
import integratedtoolkit.types.ResourceCreationRequest;
import integratedtoolkit.types.CloudImageDescription;
import integratedtoolkit.types.resources.CloudMethodWorker;
import integratedtoolkit.types.resources.ShutdownListener;
import integratedtoolkit.util.ResourceManager;
import org.apache.log4j.Logger;
import java.util.HashMap;
import java.util.concurrent.Semaphore;

public class CreationThread extends Thread {

    private static ResourceUser listener;
    private static Integer count = 0;
    private static final Logger logger = Logger.getLogger(Loggers.CONNECTORS);

    private final Operations operations;
    private final String name; //Id for the CloudProvider or IP if VM is reused
    private final String provider;
    private final ResourceCreationRequest rcr;
    private final VM reused;

    public CreationThread(Operations operations, String name, String provider, ResourceCreationRequest rR, VM reused) {
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
        boolean check = operations.getCheck();
        CloudMethodResourceDescription requested = rcr.getRequested();
        VM granted;

        if (reused == null) { //If the resources does not exist --> Create 
            this.setName("creationThread " + name);
            try {
                granted = createResourceOnProvider(requested);
            } catch (Exception e) {
                notifyFailure();
                return;
            }
            logger.info("RESOURCE_GRANTED = [\n\tNAME = " + granted.getName() + "\n\tSTATUS = ID " + granted.getEnvId() + " CREATED\n]");
        } else {
            granted = reused;
            logger.info("RESOURCE_GRANTED = [\n\tNAME = " + reused.getName() + "\n\tSTATUS = ID " + granted.getEnvId() + " REUSED\n]");
        }

        this.setName("creationThread " + granted.getName());
        CloudMethodWorker r = ResourceManager.getDynamicResource(granted.getName());
        if (r == null) {  // Resources are provided in a new VM
            if (reused == null) { // And are new --> Initiate VM
                try {
                    r = prepareNewResource(granted);
                    operations.vmReady(granted);
                } catch (Exception e) {
                    e.printStackTrace();
                    powerOff(granted);
                    notifyFailure();
                    return;
                }
            } else {
                r = new CloudMethodWorker(granted.getDescription(), granted.getNode(), granted.getDescription().getProcessorCoreCount());
                granted.setWorker(r);
            }
            ResourceManager.addCloudWorker(rcr, r);
        } else {          //Resources are provided in an existing VM
            ResourceManager.increasedCloudWorker(rcr, r, granted.getDescription());
        }

        synchronized (count) {
            count--;
        }
    }

    public static void setTaskDispatcher(ResourceUser listener) {
        CreationThread.listener = listener;
    }

    private VM createResourceOnProvider(CloudMethodResourceDescription requested) throws Exception {
        VM granted;
        Object envID;
        //ASK FOR THE VIRTUAL RESOURCE
        try {
            //Turn on the VM and expects the new mr description
            envID = operations.poweron(name, requested);
        } catch (Exception e) {
            logger.error("ERROR_MSG = [\n\tError asking a new Resource to " + provider + "\n]", e);
            throw e;
        }

        if (envID == null) {
            logger.info("INFO_MSG = [\n\t" + provider + " cannot provide this resource.\n]");
            throw new Exception("Provider can not provide the vm");
        }

        //WAITING FOR THE RESOURCES TO BE RUNNING
        try {
            //Wait until the VM has been created
            granted = operations.waitCreation(envID, requested);
        } catch (ConnectorException e) {
            e.printStackTrace();
            logger.error("ERROR_MSG = [\n\tError waiting for a machine that should be provided by " + provider + "\n]", e);
            try {
                operations.destroy(envID);
            } catch (ConnectorException ex) {
                logger.error("ERROR_MSG = [\n\tCannot poweroff the machine\n]");
            }
            throw new Exception("Error waiting for the vm");
        }

        if (granted != null) {
            logger.debug("CONNECTOR_REQUEST = [");
            logger.debug("\tPROC = " + requested.getProcessorCoreCount() + " " + requested.getProcessorArchitecture() + " cores @ " + requested.getProcessorSpeed());
            logger.debug("\tOS = " + requested.getOperatingSystemType());
            logger.debug("\tMEM = " + requested.getMemoryVirtualSize() + "(" + requested.getMemoryPhysicalSize() + ")");
            logger.debug("]");
            CloudMethodResourceDescription desc = granted.getDescription();
            logger.debug("CONNECTOR_GRANTED = [");
            logger.debug("\tPROC = " + desc.getProcessorCoreCount() + " " + desc.getProcessorArchitecture() + " cores @ " + desc.getProcessorSpeed());
            logger.debug("\tOS = " + desc.getOperatingSystemType());
            logger.debug("\tMEM = " + desc.getMemoryVirtualSize() + "(" + desc.getMemoryPhysicalSize() + ")");
            logger.debug("]");
        } else {
            throw new Exception("Granted description is null");
        }
        return granted;
    }

    private CloudMethodWorker prepareNewResource(VM vm) throws Exception {
        CloudMethodResourceDescription granted = vm.getDescription();
        CloudImageDescription cid = granted.getImage();
        HashMap<String, String> workerProperties = cid.getProperties();
        String user = workerProperties.get(ITConstants.USER);
        String password = workerProperties.get(ITConstants.PASSWORD);
        try {
            operations.configureAccess(granted.getName(), user, password);
        } catch (ConnectorException e) {
            logger.error("ERROR_MSG = [\n\tError configuring access to machine\n\tNAME = " + granted.getName() + "\n\tPROVIDER =  " + provider + "\n]", e);
            throw e;
        }

        try {
            operations.prepareMachine(granted.getName(), cid);
        } catch (ConnectorException e) {
            logger.error("ERROR_MSG = [\n\tException creating a new Resource\n]", e);
            throw e;
        }

        CloudMethodWorker worker;
        try {
            worker = new CloudMethodWorker(granted.getName(), granted, cid.getAdaptor(), workerProperties, granted.getProcessorCoreCount());
        } catch (Exception e) {
            logger.error("ERROR_MSG = [\n\tError starting the worker application in machine\n\tNAME = " + granted.getName() + "\n\tPROVIDER =  " + provider + "\n]");
            throw new Exception("Could not turn on the VM", e);
        }

        try {
            worker.announceCreation();
        } catch (Exception e) {
            Semaphore sem = new Semaphore(0);
            ShutdownListener sl = new ShutdownListener(sem);
            worker.stop(false, sl);
            sl.enable();
            try {
                sem.acquire();
            } catch (InterruptedException ie) {
                //Interrupted. No need to do anything.
            }
            logger.error("ERROR_MSG = [\n\tError announcing the machine\n\tNAME = " + granted.getName() + "\n\tPROVIDER =  " + provider + "\n]", e);
            throw e;
        }

        //add the new machine to ResourceManager
        if (operations.getTerminate()) {
            logger.info("INFO_MSG = [\n\tNew resource has been refused because integratedtoolkit has been stopped\n\tRESOURCE_NAME = " + granted.getName() + "\n]");
            try {
                worker.announceDestruction();
            } catch (Exception e) {
                logger.error("ERROR_MSG = [\n\tError announcing VM destruction\n\tVM_NAME = " + granted.getName() + "\n]", e);
            }
            Semaphore sem = new Semaphore(0);
            ShutdownListener sl = new ShutdownListener(sem);
            worker.stop(false, sl);
            sl.enable();
            try {
                sem.acquire();
            } catch (InterruptedException e) {
                //Interrupted. No need to do anything.
            }
            throw new Exception("Useless VM");
        }

        for (java.util.Map.Entry<String, String> disk : cid.getSharedDisks().entrySet()) {
            String diskName = disk.getKey();
            String mounpoint = disk.getValue();
            worker.addSharedDisk(diskName, mounpoint);
        }

        return worker;
    }

    private void powerOff(VM granted) {
        try {
            operations.poweroff(granted);
        } catch (Exception e) {
            logger.error("ERROR_MSG = [\n\tCannot poweroff the new resource\n]", e);
        }
    }

    private void notifyFailure() {
        synchronized (count) {
            count--;
        }
    }
}
