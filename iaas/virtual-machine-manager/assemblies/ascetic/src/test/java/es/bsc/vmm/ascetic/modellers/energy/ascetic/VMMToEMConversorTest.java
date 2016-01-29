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
import eu.ascetic.asceticarchitecture.iaas.energymodeller.types.energyuser.VM;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

import static junit.framework.TestCase.assertTrue;

/**
* @author Mario Macias (github.com/mariomac), David Ortiz Lopez (david.ortiz@bsc.es)
*/
public class VMMToEMConversorTest {
    
    @Test
    public void getVmsEnergyModFromVms() {
        List<Vm> vms = new ArrayList<>();
        vms.add(new Vm("vm1", "image", 1, 1024, 10, "", ""));
        vms.add(new Vm("vm2", "image", 2, 2048, 20, "", ""));
        
        List<VM> convertedVMs = VMMToEMConversor.getVmsEnergyModFromVms(vms);
        VM firstVm = convertedVMs.get(0);
        VM secondVm = convertedVMs.get(1);
        assertTrue(firstVm.getCpus() == 1 && firstVm.getRamMb() == 1024 && firstVm.getDiskGb() == 10);
        assertTrue(secondVm.getCpus() == 2 && secondVm.getRamMb() == 2048 && secondVm.getDiskGb() == 20);
    }
    
    @Test
    public void getVmsEnergyModFromVmsReturnsEmptyWhenItReceivesAnEmptyList() {
        assertTrue(VMMToEMConversor.getVmsEnergyModFromVms(new ArrayList<Vm>()).isEmpty());
    }
    
}
