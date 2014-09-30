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
import eu.ascetic.asceticarchitecture.iaas.energymodeller.types.energyuser.usage.HostVmLoadFraction;
import eu.ascetic.asceticarchitecture.iaas.energymodeller.types.usage.HostEnergyRecord;
import java.util.Calendar;
import java.util.Collection;
import java.util.List;

/**
 *
 * @author Richard
 */
public interface HistoricLoadBasedDivision {

    /**
     * This returns the host who's energy is to be fractioned out.
     *
     * @return the host The host in which the VMs reside/are planned to reside.
     */
    public Host getHost();
    /**
     * This sets the host who's energy is to be fractioned out.
     *
     * @param host The host who's energy is to be divided among the VMs
     */
    public void setHost(Host host);
    
/**
     * This adds a VM to the energy division record.
     *
     * @param vm The VM to add to the energy division
     */
    public void addVM(VM vm);

    /**
     * This adds a collection of VM to the energy division record.
     *
     * @param vm The VM to add to the energy division
     */
    public void addVM(Collection<VM> vm);

    /**
     * This removes a VM from the energy division record.
     *
     * @param vm The VM to remove from the energy division
     */
    public void removeVM(VM vm);

    /**
     * This gets the duration that the energy records describe.
     *
     * @return The duration in seconds the energy records describe.
     */
    public long getDuration();

    /**
     * This returns the time of the first energy usage record that is set.
     *
     * @return The time of the first energy usage record. Null if no records are
     * set.
     */
    public Calendar getStart();

    /**
     * This returns the time of the last energy usage record that is set.
     *
     * @return The time of the first energy usage record. Null if no records are
     * set.
     */
    public Calendar getEnd();

    /**
     * This returns the energy usage for a named VM
     *
     * @param vm The VM to get energy usage for.
     * @return The energy used by this VM.
     */
    public double getEnergyUsage(VM vm);

    /**
     * This lists VMs on the host machine.
     * @return  This VMs on the host machine.
     */
    public Collection<VM> getVMList();

    /**
     * The amount of VMs on the host machine.
     * @return This count of how many VMs are on the host machine.
     */
    public int getVMCount();

    /**
     * This sets energy usage record for the load based division mechanism. It
     * also places them in sorted order.
     *
     * @param energyUsage The energy usage data to use.
     */
    public void setEnergyUsage(List<HostEnergyRecord> energyUsage);

    /**
     * This sets load fraction records for the load based division mechanism. It
     * also places them in sorted order.
     *
     * @param loadFraction The load fraction data to use.
     */
    public void setLoadFraction(List<HostVmLoadFraction> loadFraction);
    
}
