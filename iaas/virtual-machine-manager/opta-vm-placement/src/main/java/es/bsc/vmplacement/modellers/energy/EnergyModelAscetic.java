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

package es.bsc.vmplacement.modellers.energy;

import es.bsc.vmplacement.domain.Host;
import es.bsc.vmplacement.domain.Vm;
import eu.ascetic.asceticarchitecture.iaas.energymodeller.EnergyModeller;
import eu.ascetic.asceticarchitecture.iaas.energymodeller.types.energyuser.VM;

import java.util.ArrayList;
import java.util.List;

/**
 * @author David Ortiz (david.ortiz@bsc.es)
 */
public class EnergyModelAscetic implements EnergyModel {

    private final EnergyModeller energyModeller = new EnergyModeller();

    @Override
    public double getPowerConsumption(Host host, List<Vm> vmsDeployedInHost) {
        return energyModeller.getHostPredictedEnergy(energyModeller.getHost(host.getHostname()),
                convertVmsToEMAsceticVMs(vmsDeployedInHost)).getAvgPowerUsed();
    }

    private List<VM> convertVmsToEMAsceticVMs(List<Vm> vms) {
        List<VM> result = new ArrayList<>();
        for (Vm vm: vms) {
            result.add(EnergyModeller.getVM(vm.getNcpus(), vm.getRamMb(), vm.getDiskGb()));
        }
        return result;
    }

}