package es.bsc.vmmanagercore.energymodeller;

import es.bsc.vmmanagercore.model.Vm;
import es.bsc.vmmanagercore.monitoring.Host;
import eu.ascetic.asceticarchitecture.iaas.energymodeller.types.energyuser.VM;

import java.util.ArrayList;
import java.util.List;

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

    public static List<VM> getVmsEnergyModFromVms(List<Vm> vms) {
        List<eu.ascetic.asceticarchitecture.iaas.energymodeller.types.energyuser.VM> result = new ArrayList<>();
        for (Vm vm: vms) {
            result.add(getVmEnergyModFromVM(vm));
        }
        return result;
    }

}
