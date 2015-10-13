/**
 *
 *   Copyright 2013-2015 Barcelona Supercomputing Center (www.bsc.es) All rights reserved.
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

package integratedtoolkit.ascetic;

import integratedtoolkit.types.Implementation;
import integratedtoolkit.types.resources.MethodResourceDescription;
import integratedtoolkit.types.resources.MethodWorker;
import integratedtoolkit.types.resources.ResourceDescription;
import integratedtoolkit.types.resources.Worker;
import integratedtoolkit.util.CoreManager;

import java.util.HashMap;
import java.util.LinkedList;

import eu.ascetic.paas.applicationmanager.model.Cost;
import eu.ascetic.saas.application_uploader.ApplicationUploaderException;

public class VM {

    private static final long UPDATE_FREQ = 30000;
    private long lastUpdate = 0l;
    private LinkedList<Implementation> runningJobs = new LinkedList<Implementation>();

    private final static int[] implCount = new int[CoreManager.getCoreCount()];

    static {
        for (int coreId = 0; coreId < CoreManager.getCoreCount(); coreId++) {
            implCount[coreId] = CoreManager.getCoreImplementations(coreId).length;
        }
    }

    private final eu.ascetic.paas.applicationmanager.model.VM vm;

    private Worker worker;
    private final ResourceDescription description;
    private final HashMap<String, String> properties;
    private double[][] power;
    private double[][] price;
    private double[][] energy;

    public VM(eu.ascetic.paas.applicationmanager.model.VM vm) {
        System.out.println("Creating a new VM");
        this.vm = vm;
        MethodResourceDescription rd = Configuration.getComponentDescriptions(vm.getOvfId());
        description = new MethodResourceDescription(rd);
        properties = Configuration.getComponentProperties(vm.getOvfId());
        power = new double[CoreManager.getCoreCount()][];
        for (int coreId = 0; coreId < CoreManager.getCoreCount(); coreId++) {
            power[coreId] = new double[implCount[coreId]];
        }
        price = new double[CoreManager.getCoreCount()][];
        for (int coreId = 0; coreId < CoreManager.getCoreCount(); coreId++) {
            price[coreId] = new double[implCount[coreId]];
        }
        System.out.println("Updating consumptions");
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

    public void updateConsumptions() {
        if (System.currentTimeMillis() - lastUpdate > UPDATE_FREQ) {
            System.out.println(System.currentTimeMillis() + ": Updating Consumptions for VM " + getIPv4());
            //REAL SOLUTION
            for (int coreId = 0; coreId < CoreManager.getCoreCount(); coreId++) {
                for (int implId = 0; implId < implCount[coreId]; implId++) {
                    String eventType = "core" + coreId + "impl" + implId;
                    try {
                    	Cost c = AppManager.getEstimations("" + vm.getId(), eventType);
                    	price[coreId][implId] = c.getCharges();
                    	power[coreId][implId] = c.getPowerValue();
                    	energy[coreId][implId] = c.getEnergyValue();
                     } catch (ApplicationUploaderException ex) {
                    	 power[coreId][implId] = 0;
                    	 price[coreId][implId] = 0;
                    	 energy[coreId][implId] = 0;
                     	System.err.println("Could not update the energy consumtion for " + eventType + " in " + vm.getIp());
                     	ex.printStackTrace(System.err);
                     }
                    System.out.println("\t\t OBTAINED : Core " + coreId + " impl " + implId + " Power:  " + power[coreId][implId]+" Charges: "+ price[coreId][implId]+ " Energy: "+ price[coreId][implId]);
                }
            }

            //HARDCODED SOLUTION
            /*Ascetic.changes = false;
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
             consumption[coreId][impl.getImplementationId()] = 160.00 + Math.random();
             } else {
             consumption[coreId][impl.getImplementationId()] = 172.00 + Math.random();
             }
             }
             }*/
            //COMMON CODE
            lastUpdate = System.currentTimeMillis();
        }
    }

    public HashMap<String, String> getProperties() {
        return this.properties;
    }

    public void startJob(Implementation impl) {
        runningJobs.add(impl);
    }

    public void endJob(Implementation impl) {
        runningJobs.remove(impl);
    }

    public double getCurrentCost() {
        double cost = 0d;
        for (Implementation impl : runningJobs) {
            int coreId = impl.getCoreId();
            int implId = impl.getImplementationId();
            cost += price[coreId][implId];
        }
        return cost;
    }

    public double[] getCost(int coreId) {
        return price[coreId];
    }

    public double getCost(int coreId, int implId) {
        return price[coreId][implId];
    }

    public double getCurrentPower() {
        double currentPower = 0d;
        for (Implementation impl : runningJobs) {
            int coreId = impl.getCoreId();
            int implId = impl.getImplementationId();
            currentPower += power[coreId][implId];
        }
        return currentPower;
    }

    public double[] getPower(int coreId) {
        return power[coreId];
    }

    public double getPower(int coreId, int implId) {
        return power[coreId][implId];
    }

    public void setWorker(MethodWorker worker) {
        this.worker = worker;
    }

    public Worker getWorker() {
        return this.worker;
    }
}
