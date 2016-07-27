/*
 *  Copyright 2002-2016 Barcelona Supercomputing Center (www.bsc.es)
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
package integratedtoolkit.ascetic.fake;

import eu.ascetic.paas.applicationmanager.model.Cost;
import eu.ascetic.saas.application_uploader.ApplicationUploaderException;
import integratedtoolkit.ascetic.AppManager;
import integratedtoolkit.ascetic.Ascetic;
import integratedtoolkit.ascetic.VM;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;

public class FakeAppManager extends AppManager {

    private final HashMap<VM, Long> vmsToDetect;
    private long lastUpdate = Long.MIN_VALUE;

    public FakeAppManager(String appId, String depId) {
        super(appId, depId, "");
        vmsToDetect = new HashMap<VM, Long>();
        eu.ascetic.paas.applicationmanager.model.VM vm = new eu.ascetic.paas.applicationmanager.model.VM();
        vm.setCpuActual(1);
        vm.setDiskActual(1000);
        vm.setRamActual(1024);
        vm.setOvfId("ascetic-pm-autoMethod");
        vm.setIp("COMPSsWorker01");
        vmsToDetect.put(new VM(vm), System.currentTimeMillis());

        eu.ascetic.paas.applicationmanager.model.VM vm2 = new eu.ascetic.paas.applicationmanager.model.VM();
        vm2.setCpuActual(1);
        vm2.setDiskActual(1000);
        vm2.setRamActual(1024);
        vm2.setOvfId("ascetic-pm-autoMethod");
        vm2.setIp("COMPSsWorker02");
        vmsToDetect.put(new VM(vm2), System.currentTimeMillis() + 2000);

    }

    public Collection<VM> getNewResources() throws ApplicationUploaderException {
        LinkedList<VM> newResources = new LinkedList<VM>();
        long currentUpdate = System.currentTimeMillis();
        for (java.util.Map.Entry<VM, Long> entry : vmsToDetect.entrySet()) {
            if (entry.getValue() < currentUpdate && entry.getValue() > lastUpdate) {
                VM vm = entry.getKey();
                vm.updateConsumptions(this);
                newResources.add(vm);
            }
        }
        lastUpdate = currentUpdate;
        return newResources;
    }

    public double getPower(String id, int coreId, int implId) throws ApplicationUploaderException {
        return Math.random() * 100d;
    }

    public double getPrice(String id, int coreId, int implId) throws ApplicationUploaderException {
        return Math.random() * 100d;
    }

    public Cost getEstimations(String id, int coreId, int implId) throws ApplicationUploaderException {
        double power;
        if (implId == 1) {
            power = 25.00 + (Math.random() * 1.5);
        } else {
            power = 13.00 + (Math.random() * 1.5);
        }
        double price = 0.085 + (0.0002 * power);

        Cost c = new Cost();
        c.setPowerValue(power);
        c.setCharges(price);
        return c;
    }

    public double getAccumulatedEnergy() throws ApplicationUploaderException {
        double energy = 0;
        for (VM vm : Ascetic.getResources()) {
            energy += vm.getAccumulatedEnergy();
            energy += vm.getRunningEnergy();
        }
        return energy;
    }

    public double getAccumulatedCost() throws ApplicationUploaderException {
        double cost = 0;
        for (VM vm : Ascetic.getResources()) {
            cost += vm.getAccumulatedCost();
            cost += vm.getRunningCost();
        }
        return cost;
    }

}
