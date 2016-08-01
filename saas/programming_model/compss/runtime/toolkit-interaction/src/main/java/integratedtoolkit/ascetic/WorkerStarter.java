/**
 *
 * Copyright 2013-2015 Barcelona Supercomputing Center (www.bsc.es) All rights
 * reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
package integratedtoolkit.ascetic;

import integratedtoolkit.ITConstants;
import integratedtoolkit.nio.master.configuration.NIOConfiguration;
import integratedtoolkit.types.ResourceCreationRequest;
import integratedtoolkit.types.resources.CloudMethodWorker;
import integratedtoolkit.types.resources.description.CloudMethodResourceDescription;
import integratedtoolkit.util.CoreManager;
import integratedtoolkit.util.ResourceManager;
import java.io.IOException;
import java.util.HashMap;

public class WorkerStarter extends Thread {

    private final VM vm;

    public WorkerStarter(VM vm) {
        this.vm = vm;
    }

    public void run() {
        NIOConfiguration conf = vm.getConfiguration();
        String user = conf.getUser();
        while (!connectionAvailable(vm.getIPv4(), user)) {
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                //DO NOTHING AND CHECK AGAIN IF NODE IS AVAILABLE
            }
        }

        CloudMethodWorker worker = null;
        CloudMethodResourceDescription desc = (CloudMethodResourceDescription) vm.getDescription();
        try {
            String adaptor = System.getProperty(ITConstants.COMM_ADAPTOR);
            if (adaptor == null) {
                adaptor = "integratedtoolkit.nio.master.NIOAdaptor";
            }
            worker = new CloudMethodWorker(vm.getIPv4(), desc, conf, new HashMap<String, String>());
        } catch (Exception e) {
            System.out.println("Could not turn on the VM");
            e.printStackTrace();
            return;
        }
        vm.setWorker(worker);
        ResourceCreationRequest rcr = new ResourceCreationRequest(desc, new int[CoreManager.getCoreCount()][0], "Ascetic");
        ResourceManager.addCloudWorker(rcr, worker, vm.getCompatibleImplementations());

    }

    private static boolean connectionAvailable(String resourceName, String user) {

        String[] command;
        if (resourceName.startsWith("http://")) {
            command = new String[]{"/bin/sh", "-c", "wget " + resourceName};
        } else {
            command = new String[]{"/bin/sh", "-c", "timeout 10 ssh -o StrictHostKeyChecking=no " + user + "@" + resourceName + " ls"};
        }
        Process p;
        int exitValue = -1;
        try {
            p = Runtime.getRuntime().exec(command);
            p.waitFor();
            exitValue = p.exitValue();
        } catch (IllegalThreadStateException ex) {
            exitValue = -1;
        } catch (IOException ex) {
            exitValue = -1;
        } catch (InterruptedException ex) {
            exitValue = -1;
        }
        return (exitValue == 0);
    }
}
