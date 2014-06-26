package es.bsc.vmmanagercore.integration;

import es.bsc.vmmanagercore.model.Vm;
import es.bsc.vmmanagercore.monitoring.HostInfo;
import eu.ascetic.asceticarchitecture.iaas.energymodeller.types.energyuser.VM;

import java.util.ArrayList;
import java.util.Collection;

/**
 * This class converts the classes used in the VMM to the classes used in the Energy Modeller.
 * For example, the VM class is similar in the two components, but we need to apply some transformations.
 *
 * @author David Ortiz Lopez (david.ortiz@bsc.es)
 */
public class VMMToEMConversor {

    public static eu.ascetic.asceticarchitecture.iaas.energymodeller.types.energyuser.VM getVmEnergyModFromVM(Vm vm) {
        return new eu.ascetic.asceticarchitecture.iaas.energymodeller.types.energyuser.VM(
                vm.getCpus(), vm.getRamMb(), vm.getDiskGb());
    }

    public static ArrayList<VM> getVmsEnergyModFromVms(Collection<Vm> vms) {
        ArrayList<eu.ascetic.asceticarchitecture.iaas.energymodeller.types.energyuser.VM> result = new ArrayList<>();
        for (Vm vm: vms) {
            result.add(getVmEnergyModFromVM(vm));
        }
        return result;
    }

    public static eu.ascetic.asceticarchitecture.iaas.energymodeller.types.energyuser.Host getHostEnergyModFromHost(
            HostInfo host) {
        return new eu.ascetic.asceticarchitecture.iaas.energymodeller.types.energyuser.Host(1, host.getHostname());
    }
}
