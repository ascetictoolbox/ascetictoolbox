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
package eu.ascetic.asceticarchitecture.iaas.energymodeller.energypredictor.vmenergyshare;

import eu.ascetic.asceticarchitecture.iaas.energymodeller.types.energyuser.Host;
import eu.ascetic.asceticarchitecture.iaas.energymodeller.types.energyuser.VM;
import eu.ascetic.asceticarchitecture.iaas.energymodeller.types.energyuser.VmDeployed;
import eu.ascetic.asceticarchitecture.iaas.energymodeller.types.usage.HostEnergyRecord;
import eu.ascetic.asceticarchitecture.iaas.energymodeller.types.usage.HostVmLoadFraction;
import java.util.Calendar;
import java.util.Collection;
import java.util.Collections;
import java.util.GregorianCalendar;
import java.util.HashSet;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 *
 * @author Richard
 */
public class LoadBasedDivision {

    private Host host;
    private final HashSet<VM> vms = new HashSet<>();
    private List<HostEnergyRecord> energyUsage;
    private List<HostVmLoadFraction> loadFraction;

    public LoadBasedDivision(Host host) {
        this.host = host;
    }

    /**
     * This returns the host who's energy is to be fractioned out.
     *
     * @return the host The host in which the VMs reside/are planned to reside.
     */
    public Host getHost() {
        return host;
    }

    /**
     * This sets the host who's energy is to be fractioned out.
     *
     * @param host The host who's energy is to be divided among the VMs
     */
    public void setHost(Host host) {
        this.host = host;
    }

    /**
     * This adds a VM to the energy division record.
     *
     * @param vm The VM to add to the energy division
     */
    public void addVM(VM vm) {
        vms.add(vm);
    }

    /**
     * This adds a collection of VM to the energy division record.
     *
     * @param vm The VM to add to the energy division
     */
    public void addVM(Collection<VM> vm) {
        vms.addAll(vm);
    }

    /**
     * This removes a VM from the energy division record.
     *
     * @param vm The VM to remove from the energy division
     */
    public void removeVM(VM vm) {
        vms.remove(vm);
    }

    /**
     * This gets the duration that the energy records describe.
     *
     * @return The duration in seconds the energy records describe.
     */
    public long getDuration() {
        HostEnergyRecord first = energyUsage.get(0);
        HostEnergyRecord last = energyUsage.get(energyUsage.size() - 1);
        return last.getTime() - first.getTime();
    }

    /**
     * This returns the time of the first energy usage record that is set.
     *
     * @return The time of the first energy usage record. Null if no records are
     * set.
     */
    public Calendar getStart() {
        if (energyUsage == null || energyUsage.isEmpty()) {
            return null;
        }
        GregorianCalendar answer = new GregorianCalendar();
        answer.setTimeInMillis(TimeUnit.SECONDS.toMillis(energyUsage.get(0).getTime()));
        return answer;
    }

    /**
     * This returns the time of the last energy usage record that is set.
     *
     * @return The time of the first energy usage record. Null if no records are
     * set.
     */
    public Calendar getEnd() {
        if (energyUsage == null || energyUsage.isEmpty()) {
            return null;
        }
        GregorianCalendar answer = new GregorianCalendar();
        answer.setTimeInMillis(TimeUnit.SECONDS.toMillis(energyUsage.get(energyUsage.size() - 1).getTime()));
        return answer;
    }

    /**
     * This returns the energy usage for a named VM
     *
     * @param vm The VM to get energy usage for.
     * @return The energy used by this VM.
     */
    public double getEnergyUsage(VM vm) {
        VmDeployed deployed = (VmDeployed) vm;
        int recordCount = (energyUsage.size() <= loadFraction.size() ? energyUsage.size() : loadFraction.size());
        
        /**
         * Calculate the energy used by a VM taking into account the work it has performed.
         */
        double vmEnergy = 0;
        //Access two records at once hence ensure size() -2
        for (int i = 0; i <= recordCount - 2; i++) {
            HostEnergyRecord energy1 = energyUsage.get(i);
            HostEnergyRecord energy2 = energyUsage.get(i + 1);
            HostVmLoadFraction load1 = loadFraction.get(i);
            HostVmLoadFraction load2 = loadFraction.get(i + 1);
            double deltaEnergy = energy2.getEnergy() - energy1.getEnergy();
            double avgLoad = (load1.getFraction(deployed) + load2.getFraction(deployed)) / 2;
            vmEnergy = vmEnergy + (deltaEnergy * avgLoad);
        }
        return vmEnergy;
    }

    public Collection<VM> getVMList() {
        return vms;
    }

    public int getVMCount() {
        return vms.size();
    }

    /**
     * This sets energy usage record for the load based division mechanism. It
     * also places them in sorted order.
     *
     * @param energyUsage The energy usage data to use.
     */
    public void setEnergyUsage(List<HostEnergyRecord> energyUsage) {
        /**
         * The base assumption is that the energy usage records match 1:1 in
         * time frame with the load fraction and are in sorted order.
         */
        this.energyUsage = energyUsage;
        Collections.sort(energyUsage);
    }

    /**
     * This sets load fraction records for the load based division mechanism. It
     * also places them in sorted order.
     *
     * @param loadFraction The load fraction data to use.
     */
    public void setLoadFraction(List<HostVmLoadFraction> loadFraction) {
        /**
         * The base assumption is that the energy usage records match 1:1 in
         * time frame with the load fraction and are in sorted order.
         */
        this.loadFraction = loadFraction;
        Collections.sort(loadFraction);
    }
}
