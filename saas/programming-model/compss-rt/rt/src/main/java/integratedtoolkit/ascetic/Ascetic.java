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
package integratedtoolkit.ascetic;

import integratedtoolkit.types.Implementation;
import integratedtoolkit.types.Job;
import integratedtoolkit.types.ResourceDescription;
import java.util.HashMap;
import java.util.LinkedList;

public class Ascetic {

    private static final HashMap<String, VM> resources = new HashMap<String, VM>();

    public static LinkedList<ResourceDescription> getNewResources() {
        LinkedList<ResourceDescription> newResources = new LinkedList<ResourceDescription>();
        for (VM vm : AppManager.getResources()) {
            if (resources.get(vm.getIPv4()) == null) {
                newResources.add(vm.getDescription());
                resources.put(vm.getIPv4(), vm);
            }

        }
        return newResources;
    }

    public static int[] getConsumptions(String IPv4, int coreId) {
        return resources.get(IPv4).getConsumptions(coreId);
    }

    public static void updateConsumptions() {
        for (VM vm : resources.values()) {
            vm.updateConsumptions();
        }
    }
    
    public static void startEvent(Job job){
        
        Implementation impl=job.getImplementation();
        String eventType="core"+impl.getCoreId()+"impl"+impl.getImplementationId();
        String IPv4= job.getResource().getName();
        VM vm = resources.get(IPv4);
        String eventId =ApplicationMonitor.startEvent(vm, eventType);
        job.setEventId(eventId);
    }
    public static void stopEvent(Job job){
        ApplicationMonitor.stopEvent(job.getEventId());
    }

}
