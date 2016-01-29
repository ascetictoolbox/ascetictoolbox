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

package es.bsc.vmm.ascetic.modellers.energy.ascetic;

import es.bsc.demiurge.core.models.vms.Vm;
import eu.ascetic.asceticarchitecture.iaas.energymodeller.EnergyModeller;
import eu.ascetic.asceticarchitecture.iaas.energymodeller.types.energyuser.VM;

import java.util.ArrayList;
import java.util.List;

/**
 * This class converts the classes used in the VMM to the classes used in the Energy Modeller.
 * For example, the VM class is similar in the two components, but we need to apply some transformations.
 *
 * @author Mario Macias (github.com/mariomac), David Ortiz Lopez (david.ortiz@bsc.es)
 */
public class VMMToEMConversor {

    // Suppress default constructor for non-instantiability
    private VMMToEMConversor() {
        throw new AssertionError();
    }
    
    public static List<VM> getVmsEnergyModFromVms(List<Vm> vms) {
        List<VM> result = new ArrayList<>();
        for (Vm vm: vms) {
            VM emVM = EnergyModeller.getVM(vm.getCpus(), vm.getRamMb(), vm.getDiskGb());
            emVM.addDiskImage(vm.getImage());

            // For now, I send the application ID as a tag.
            // Is the application ID that I receive from the Application Manager valid for the
            // Energy Modeller too?
            emVM.addApplicationTag(vm.getApplicationId());

            result.add(emVM);
        }
        return result;
    }

}
