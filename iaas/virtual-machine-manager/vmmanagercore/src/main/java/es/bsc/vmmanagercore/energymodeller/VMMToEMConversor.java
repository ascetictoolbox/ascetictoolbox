package es.bsc.vmmanagercore.energymodeller;

import es.bsc.vmmanagercore.model.Vm;
import eu.ascetic.asceticarchitecture.iaas.energymodeller.EnergyModeller;
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

    public static List<VM> getVmsEnergyModFromVms(List<Vm> vms) {
        List<eu.ascetic.asceticarchitecture.iaas.energymodeller.types.energyuser.VM> result = new ArrayList<>();
        for (Vm vm: vms) {
            result.add(EnergyModeller.getVM(vm.getCpus(), vm.getRamMb(), vm.getDiskGb()));
        }
        return result;
    }

}
