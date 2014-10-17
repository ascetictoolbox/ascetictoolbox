/**
 * Copyright 2014 University of Leeds
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
package eu.ascetic.energy.modeller.trace.file.generator;

import eu.ascetic.asceticarchitecture.iaas.energymodeller.energypredictor.vmenergyshare.EnergyDivision;
import eu.ascetic.asceticarchitecture.iaas.energymodeller.energypredictor.vmenergyshare.LoadFractionShareRule;
import eu.ascetic.asceticarchitecture.iaas.energymodeller.types.energyuser.Host;
import eu.ascetic.asceticarchitecture.iaas.energymodeller.types.energyuser.VM;
import eu.ascetic.asceticarchitecture.iaas.energymodeller.types.energyuser.VmDeployed;
import eu.ascetic.asceticarchitecture.iaas.energymodeller.types.energyuser.usage.HostVmLoadFraction;
import eu.ascetic.ioutils.GenericLogger;
import eu.ascetic.ioutils.ResultsStore;
import java.io.File;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.concurrent.TimeUnit;

/**
 * This tool logs out to disk all energy information and its respective trace
 * data.
 *
 * @author Richard
 */
public class VMEnergyTraceDataLogger extends GenericLogger<DataCollector.Pair> {

    private LoadFractionShareRule rule = new LoadFractionShareRule();
    private HashMap<String, Host> knownHosts = new HashMap<>();

    public VMEnergyTraceDataLogger(File file, boolean overwrite) {
        super(file, overwrite);
    }

    @Override
    public void writeHeader(ResultsStore store) {
        store.add("Time");
        store.append("Host Name");
        store.append("Host Id");
        store.append("Host Power");
        store.append("Host Energy");
        store.append("VMs on Host");
        store.append("VM Name");
        store.append("VM Id");
        store.append("VM Power Usage");
        store.append("VM Power Usage (Considering Idle)");
    }

    @Override
    public void writebody(DataCollector.Pair item, ResultsStore store) {
        ArrayList<VM> vmsArr = new ArrayList<>();
        vmsArr.addAll(item.getVmLoadFraction().getVMs());
        ArrayList<HostVmLoadFraction> loadFractionData = new ArrayList<>();
        loadFractionData.add(item.getVmLoadFraction());
        rule.setFractions(loadFractionData.get(0).getFraction());
        EnergyDivision division = rule.getEnergyUsage(item.getHost().getHost(), vmsArr);
        division.setConsiderIdleEnergy(true);

        for (VmDeployed vm : item.getVmLoadFraction().getVMs()) {
            if (vm.getAllocatedTo() == null) {
                vm.setAllocatedTo(getVMsHost(vm));
            }
            Date measurementTime = new Date();
            measurementTime.setTime(TimeUnit.SECONDS.toMillis(item.getHost().getTime()));
            store.add(item.getHost().getTime());
            store.append(item.getHost().getHost().getHostName());
            store.append(item.getHost().getHost().getId());
            store.append(item.getHost().getPower());
            store.append(item.getHost().getEnergy());
            store.append(item.getVmLoadFraction().getVMs().size());
            store.append(vm.getName());
            store.append(vm.getId());
            division.setConsiderIdleEnergy(false);
            store.append(division.getEnergyUsage(item.getHost().getPower(), vm));
            division.setConsiderIdleEnergy(true);
            store.append(division.getEnergyUsage(item.getHost().getPower(), vm));
        }
    }

    /**
     * This given a host determines its Host. It is used for the initial
     * assignment of this value.
     *
     * @param vm The deployed vm
     * @return The host that it belongs to.
     */
    private Host getVMsHost(VmDeployed vm) {
        if (vm.getAllocatedTo() != null) {
            return vm.getAllocatedTo();
        }
        /**
         * This block of code takes the agreed assumption that the host name
         * ends with "_<hostname>" and that "_" exist nowhere else in the name.
         */
        String name = vm.getName();
        int parseTokenPos = name.indexOf("_");
        if (parseTokenPos == -1 && vm.getAllocatedTo() == null) {
            return null;
        }
        return getHost(name.substring(parseTokenPos + 1, name.length()));
    }

    /**
     * @param knownHosts the knownHosts to set
     */
    public void setKnownHosts(HashMap<String, Host> knownHosts) {
        this.knownHosts = knownHosts;
    }
    
    /**
     * This gets the named host from the known host list.
     *
     * @param hostname The name of the host
     * @return The host that has the name specified.
     */
    private Host getHost(String hostname) {
        return knownHosts.get(hostname);
    }    

}
