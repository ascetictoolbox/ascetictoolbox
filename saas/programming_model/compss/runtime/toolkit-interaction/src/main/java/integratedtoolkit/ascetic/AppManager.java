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

/*import eu.ascetic.saas.application_uploader.ApplicationUploader;
 import eu.ascetic.saas.application_uploader.ApplicationUploaderException;*/
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;

public class AppManager {

    private static final String applicationId;
    private static final String deploymentId;

    private static final HashMap<String, VM> detectedVMs;
    //public static final ApplicationUploader uploader;

    static {
        applicationId = Configuration.getApplicationId();
        deploymentId = Configuration.getDeploymentId();
        //  uploader = new ApplicationUploader(Configuration.getApplicationManagerEndpoint());
        detectedVMs = new HashMap<String, VM>();
    }

    /* 
     public static double getConsumption(String id, String eventId) throws ApplicationUploaderException {
     //return Math.random()*100d;               
     return uploader.getEventEnergyEstimationInVM(applicationId, deploymentId, eventId, id);
     }
     */
    
    public static Collection<VM> getNewResources() {
        LinkedList<VM> newResources = new LinkedList<VM>();
        LinkedList<eu.ascetic.paas.applicationmanager.model.VM> vms = new LinkedList();
        eu.ascetic.paas.applicationmanager.model.VM fakeVM = new eu.ascetic.paas.applicationmanager.model.VM();
        fakeVM.setIp("127.0.0.1");
        fakeVM.setOvfId("ascetic-pm-autoMethod");
        vms.add(fakeVM);
        fakeVM = new eu.ascetic.paas.applicationmanager.model.VM();
        fakeVM.setIp("127.0.0.2");
        fakeVM.setOvfId("ascetic-pm-autoMethod");
        vms.add(fakeVM);

        for (eu.ascetic.paas.applicationmanager.model.VM rvm : vms) {
            String IPv4 = rvm.getIp();
            VM vm = detectedVMs.get(IPv4);
            if (vm == null) {
                vm = new VM(rvm);
                detectedVMs.put(IPv4, vm);
                newResources.add(vm);
            }
        }
        return newResources;
    }
}
