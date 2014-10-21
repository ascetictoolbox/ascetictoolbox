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

import eu.ascetic.saas.application_uploader.ApplicationUploaderException;
import integratedtoolkit.types.Implementation;
import integratedtoolkit.types.Method;
import integratedtoolkit.types.ResourceDescription;
import integratedtoolkit.types.Service;
import integratedtoolkit.util.CoreManager;
import java.util.HashMap;
import java.util.logging.Level;
import java.util.logging.Logger;

public class VM {

    private static final long UPDATE_FREQ = 30000;
    private long lastUpdate = 0l;

    private final static int[] implCount = new int[CoreManager.coreCount];

    static {
        for (int coreId = 0; coreId < CoreManager.coreCount; coreId++) {
            implCount[coreId] = CoreManager.getCoreImplementations(coreId).length;
        }
    }

    private final eu.ascetic.paas.applicationmanager.model.VM vm;

    private final ResourceDescription description;

    private double[][] consumption;

    public VM(eu.ascetic.paas.applicationmanager.model.VM vm) {
        String IPv4 = vm.getIp();
        this.vm = vm;
        ResourceDescription rd = Configuration.getComponentDescriptions(vm.getOvfId());
        description = new ResourceDescription(rd);
        description.setName(IPv4);
        consumption = new double[CoreManager.coreCount][];
        for (int coreId = 0; coreId < CoreManager.coreCount; coreId++) {
            consumption[coreId] = new double[implCount[coreId]];
        }
        updateConsumptions();
    }

    public String getIPv4() {
        return vm.getIp();
    }

    public String getProviderId() {
        return vm.getProviderVmId();
    }

    public String getComponentId() {
        return vm.getOvfId();
    }

    public ResourceDescription getDescription() {
        return description;
    }

    public double[] getConsumptions(int coreId) {
        return consumption[coreId];
    }

    public void updateConsumptions() {
        if (System.currentTimeMillis() - lastUpdate > UPDATE_FREQ) {

            /*
             for (int coreId = 0; coreId < CoreManager.coreCount; coreId++) {
             for (int implId = 0; implId < implCount[coreId]; implId++) {
             String eventType = "core" + coreId + "impl" + implId;
             try {
             consumption[coreId][implId] = AppManager.getConsumption(vm.getIp(), eventType);
             } catch (ApplicationUploaderException ex) {
             System.err.println("Could not update the energy consumtion for " + eventType + " in " + vm.getIp());
             }
             }
             }*/
            for (int coreId = 0; coreId < CoreManager.coreCount; coreId++) {
                for (Implementation impl : CoreManager.getCoreImplementations(coreId)) {
                    String definingClass = "";
                    if (impl.getType() == Implementation.Type.SERVICE) {
                        Service s = (Service) impl;
                        definingClass = s.getNamespace() + "." + s.getServiceName() + "." + s.getPortName();
                    } else {
                        Method m = (Method) impl;
                        definingClass = m.getDeclaringClass();
                    }
                    if (definingClass.compareTo("jeplus.worker.JEPlusImplOptimized") == 0) {
                       consumption[coreId][impl.getImplementationId()]=100; 
                    }else{
                       consumption[coreId][impl.getImplementationId()]=3;  
                    }
                }
            }
            lastUpdate = System.currentTimeMillis();
        }
    }

}
