/**
 Copyright (C) 2013-2014  Barcelona Supercomputing Center

 This library is free software; you can redistribute it and/or
 modify it under the terms of the GNU Lesser General Public
 License as published by the Free Software Foundation; either
 version 2.1 of the License, or (at your option) any later version.

 This library is distributed in the hope that it will be useful,
 but WITHOUT ANY WARRANTY; without even the implied warranty of
 MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 Lesser General Public License for more details.

 You should have received a copy of the GNU Lesser General Public
 License along with this library; if not, write to the Free Software
 Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301  USA
 */

package es.bsc.vmmanagercore.manager.components;

import es.bsc.vmmanagercore.db.VmManagerDb;
import es.bsc.vmmanagercore.modellers.energy.EnergyModeller;
import es.bsc.vmmanagercore.modellers.price.PricingModeller;
import es.bsc.vmmanagercore.models.estimates.ListVmEstimates;
import es.bsc.vmmanagercore.models.estimates.VmToBeEstimated;
import es.bsc.vmmanagercore.models.vms.Vm;
import es.bsc.vmmanagercore.scheduler.EstimatesGenerator;
import es.bsc.vmmanagercore.scheduler.Scheduler;

import java.util.ArrayList;
import java.util.List;

/**
 * @author David Ortiz Lopez (david.ortiz@bsc.es)
 */
public class EstimatesManager {

    private final EstimatesGenerator estimatesGenerator = new EstimatesGenerator();
    private final VmsManager vmsManager;
    private final HostsManager hostsManager;
    private final VmManagerDb db;
    private final EnergyModeller energyModeller;
    private final PricingModeller pricingModeller;
    
    public EstimatesManager(VmsManager vmsManager, HostsManager hostsManager, VmManagerDb db,
                            EnergyModeller energyModeller, PricingModeller pricingModeller) {
        this.vmsManager = vmsManager;
        this.hostsManager = hostsManager;
        this.db = db;
        this.energyModeller = energyModeller;
        this.pricingModeller = pricingModeller;
    }
    
    public ListVmEstimates getVmEstimates(List<VmToBeEstimated> vmsToBeEstimated) {
        Scheduler scheduler = new Scheduler(db.getCurrentSchedulingAlg(), vmsManager.getAllVms(), 
                energyModeller, pricingModeller);
        return estimatesGenerator.getVmEstimates(
                scheduler.chooseBestDeploymentPlan(
                        vmsToBeEstimatedToVms(vmsToBeEstimated), 
                        hostsManager.getHosts()), 
                        vmsManager.getAllVms(), 
                energyModeller, 
                pricingModeller);
    }

    /**
     * Transforms a list of VMs to be estimated to a list of VMs.
     *
     * @param vmsToBeEstimated the list of VMs to be estimated
     * @return the list of VMs
     */
    // Note: this function would not be needed if VmToBeEstimated inherited from Vm
    private List<Vm> vmsToBeEstimatedToVms(List<VmToBeEstimated> vmsToBeEstimated) {
        List<Vm> result = new ArrayList<>();
        for (VmToBeEstimated vmToBeEstimated: vmsToBeEstimated) {
            result.add(vmToBeEstimated.toVm());
        }
        return result;
    }
    
}
