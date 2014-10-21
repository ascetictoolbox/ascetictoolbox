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
package integratedtoolkit.ascetic;

import integratedtoolkit.types.CloudImageDescription;
import integratedtoolkit.types.Implementation;
import integratedtoolkit.types.Job;
import integratedtoolkit.types.ProjectWorker;
import integratedtoolkit.types.ResourceDescription;
import integratedtoolkit.util.ProjectManager;
import java.io.IOException;
import java.util.HashMap;
import java.util.LinkedList;

public class Ascetic {

    private static final HashMap<String, VM> resources = new HashMap<String, VM>();

    public static LinkedList<ResourceDescription> getNewResources() {
        LinkedList<ResourceDescription> newResources = new LinkedList<ResourceDescription>();
        try {
            for (VM vm : AppManager.getResources()) {
                if (resources.get(vm.getIPv4()) == null && connectionAvailable(vm.getIPv4(), "root")) {
                    newResources.add(vm.getDescription());
                    resources.put(vm.getIPv4(), vm);
                    CloudImageDescription cid = vm.getDescription().getImage();
                    ProjectWorker pw = new ProjectWorker(vm.getIPv4(), "ASCETIC", cid.getUser(), Integer.MAX_VALUE, cid.getiDir(), cid.getwDir(), cid.getaDir(), cid.getlPath());
                    ProjectManager.addProjectWorker(pw);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();

        }
        return newResources;
    }

    public static double[] getConsumptions(String IPv4, int coreId) {
        return resources.get(IPv4).getConsumptions(coreId);
    }

    public static void updateConsumptions() {
        for (VM vm : resources.values()) {
            vm.updateConsumptions();
        }
    }

    public static void startEvent(Job job) {
        Implementation impl = job.getImplementation();
        String eventType = "core" + impl.getCoreId() + "impl" + impl.getImplementationId();
        String IPv4 = job.getResource().getName();
        VM vm = resources.get(IPv4);
        String eventId = ApplicationMonitor.startEvent(vm, eventType);
        job.setEventId(eventId);
    }

    public static void stopEvent(Job job) {
        ApplicationMonitor.stopEvent(job.getEventId());
    }

    public static LinkedList<Implementation> getComponentImplementations(String name) {
        return Configuration.getComponentImplementations(name);
    }

    private static boolean connectionAvailable(String resourceName, String user) {

        String[] command;
        if (resourceName.startsWith("http://")) {
            command = new String[]{"/bin/sh", "-c", "wget " + resourceName};
        } else {
            command = new String[]{"/bin/sh", "-c", "timeout 10 ssh " + user + "@" + resourceName + " ls"};
        }
        /*System.out.println("--------------- Connection Available??----------");
         System.out.println("USER:" + System.getProperty("user.name"));
         System.out.println("COMMAND:" + command[2]);
         System.out.println("--------------- Connection Available  END----------");*/
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
