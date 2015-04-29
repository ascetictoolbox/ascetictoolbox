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
package eu.ascetic.asceticarchitecture.iaas.energymodeller.datastore;

import eu.ascetic.asceticarchitecture.iaas.energymodeller.energypredictor.vmenergyshare.EnergyDivision;
import eu.ascetic.asceticarchitecture.iaas.energymodeller.energypredictor.vmenergyshare.LoadFractionShareRule;
import eu.ascetic.asceticarchitecture.iaas.energymodeller.queryinterface.datasourceclient.HostMeasurement;
import eu.ascetic.asceticarchitecture.iaas.energymodeller.types.energyuser.VM;
import eu.ascetic.asceticarchitecture.iaas.energymodeller.types.energyuser.VmDeployed;
import eu.ascetic.asceticarchitecture.iaas.energymodeller.types.energyuser.usage.HostVmLoadFraction;
import eu.ascetic.ioutils.GenericLogger;
import eu.ascetic.ioutils.ResultsStore;
import java.io.File;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;

/**
 * This provides logging facilities for VM energy usage data.
 *
 * @author Richard Kavanagh
 */
public class VmEnergyUsageLogger extends GenericLogger<VmEnergyUsageLogger.Pair> {

    private final LoadFractionShareRule rule = new LoadFractionShareRule();
    private boolean considerIdleEnergy = true;

    /**
     * This creates a new VM energy user logger
     *
     * @param file The file to write the log out to.
     * @param overwrite If the file should be overwritten on starting the energy
     * modeller.
     */
    public VmEnergyUsageLogger(File file, boolean overwrite) {
        super(file, overwrite);
        saveFile.setDelimeter(" ");
    }

    @Override
    public void writeHeader(ResultsStore store) {
        store.setDelimeter(" ");
        /**
         * No header should be provided. The <Zabbix_Host> <Metric_Key> <Value>
         */
    }

    /**
     * This writes a host energy record and vm load fraction data to disk.
     *
     * @param hostMeasurement The host energy record relating to the fraction
     * data.
     * @param vmLoadFraction The VM load fraction data.
     * @param store The storage that this data will be written to disk for.
     */
    public void writebody(HostMeasurement hostMeasurement, HostVmLoadFraction vmLoadFraction, ResultsStore store) {
        store.setDelimeter(" ");
        ArrayList<VM> vmsArr = new ArrayList<>();
        vmsArr.addAll(vmLoadFraction.getVMs());
        ArrayList<HostVmLoadFraction> loadFractionData = new ArrayList<>();
        loadFractionData.add(vmLoadFraction);
        rule.setFractions(loadFractionData.get(0).getFraction());
        EnergyDivision division = rule.getEnergyUsage(hostMeasurement.getHost(), vmsArr);
        division.setConsiderIdleEnergy(considerIdleEnergy);

        for (VmDeployed vm : vmLoadFraction.getVMs()) {
            if (vm.getAllocatedTo() == null) {
                vm.setAllocatedTo(hostMeasurement.getHost());
            }
            store.add(vm.getName());
            store.append("power");
            store.append(division.getEnergyUsage(formatDouble(hostMeasurement.getPower(), 1), vm));
        }
    }

    @Override
    public void writebody(Pair item, ResultsStore store) {
        writebody(item.getHost(), item.getVmLoadFraction(), store);
    }

    /**
     * Indicates if the logged value should include idle energy or not.
     *
     * @return if idle energy is taken account of or not
     */
    public boolean isConsiderIdleEnergy() {
        return considerIdleEnergy;
    }

    /**
     * Sets if the logged value should include idle energy or not.
     *
     * @param considerIdleEnergy if idle energy is taken account of or not
     */
    public void setConsiderIdleEnergy(boolean considerIdleEnergy) {
        this.considerIdleEnergy = considerIdleEnergy;
    }

    /**
     * This formats a double to a set amount of decimal places.
     *
     * @param number The number to format
     * @param decimalPlaces The amount of decimal places to format to
     * @return The number formatted to a given amount of decimal places.
     */
    public static double formatDouble(double number, int decimalPlaces) {
        return BigDecimal.valueOf(number).setScale(decimalPlaces, RoundingMode.HALF_UP).doubleValue();
    }

    /**
     * This binds a host energy record to VM load fraction information. Thus
     * allowing for the calculations to take place.
     */
    public class Pair {

        private final HostMeasurement host;
        private final HostVmLoadFraction vmLoadFraction;

        /**
         * This creates a new pair object that links host energy records toVM
         * load fraction data.
         *
         * @param host The host energy record
         * @param vmLoadFraction The VM load fraction data.
         */
        public Pair(HostMeasurement host, HostVmLoadFraction vmLoadFraction) {
            this.host = host;
            this.vmLoadFraction = vmLoadFraction;
        }

        /**
         * The host of the records that have been paired together.
         *
         * @return the host The host
         */
        public HostMeasurement getHost() {
            return host;
        }

        /**
         * The VM load fraction data of the records that have been paired
         * together.
         *
         * @return the vmLoadFraction The VM load fraction data
         */
        public HostVmLoadFraction getVmLoadFraction() {
            return vmLoadFraction;
        }
    }

}
