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

import eu.ascetic.saas.application_uploader.ApplicationUploader;
import eu.ascetic.saas.application_uploader.ApplicationUploaderException;
import java.util.Collection;
import java.util.HashMap;

public class AppManager {

    private static final String applicationId;
    private static final String deploymentId;

    private static final HashMap<String, VM> detectedVMs;
    public static final ApplicationUploader uploader;

    static {
        applicationId = Configuration.getApplicationId();
        deploymentId = Configuration.getDeploymentId();
        uploader = new ApplicationUploader(Configuration.getApplicationManagerEndpoint());
        detectedVMs = new HashMap<String, VM>();
    }

    public static Collection<VM> getResources() throws ApplicationUploaderException {
        for (eu.ascetic.paas.applicationmanager.model.VM rvm : uploader.getDeploymentVMDescriptions(applicationId, deploymentId)) {
            String IPv4 = rvm.getIp();
            VM vm = detectedVMs.get(IPv4);
            if (vm == null) {
                vm = new VM(rvm);
                detectedVMs.put(IPv4, vm);
            }            
        }
        return detectedVMs.values();
    }

    public static double getConsumption(String IPv4, String eventId) throws ApplicationUploaderException {
        return uploader.getEventEnergyEstimationInVM(applicationId, deploymentId, eventId, IPv4);        
    }

}
