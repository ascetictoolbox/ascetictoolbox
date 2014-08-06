package es.bsc.vmmanagercore.energymodeller;

import es.bsc.vmmanagercore.model.Vm;
import es.bsc.vmmanagercore.model.VmDeployed;
import es.bsc.vmmanagercore.monitoring.Host;
import eu.ascetic.asceticarchitecture.iaas.energymodeller.EnergyModeller;
import eu.ascetic.asceticarchitecture.iaas.energymodeller.types.energyuser.VM;
import eu.ascetic.asceticarchitecture.iaas.energymodeller.types.usage.EnergyUsagePrediction;

import java.util.ArrayList;
import java.util.List;

/**
 * Connector for the energy modeller developed by University of Leeds and AUEB.
 *
 * @author David Ortiz Lopez (david.ortiz@bsc.es)
 */
public class EnergyModellerConnector {

    private static EnergyModeller energyModeller = new EnergyModeller();

    /**
     * Returns the VMs deployed in a given host.
     *
     * @param hostname the host name
     * @return the VMs deployed in the host
     */
    public static List<Vm> getVmsDeployedInHost(String hostname, List<VmDeployed> vmsDeployed) {
        List<Vm> vms = new ArrayList<>();
        for (VmDeployed vm: vmsDeployed) {
            if (vm.getHostName().equals(hostname)) {
                vms.add(vm);
            }
        }
        return vms;
    }

    private static EnergyUsagePrediction getEnergyUsagePrediction(Vm vm, Host host, List<VmDeployed> vmsDeployed) {
        return energyModeller.getPredictedEnergyForVM(
                VMMToEMConversor.getVmEnergyModFromVM(vm),
                VMMToEMConversor.getVmsEnergyModFromVms(getVmsDeployedInHost(host.getHostname(), vmsDeployed)),
                energyModeller.getHost(host.getHostname()));
    }

    public static double getPredictedAvgPowerVm(Vm vm, Host host, List<VmDeployed> vmsDeployed) {
        return getEnergyUsagePrediction(vm, host, vmsDeployed).getAvgPowerUsed();
    }

    public static double getPredictedEnergyVm(Vm vm, Host host, List<VmDeployed> vmsDeployed) {
        return getEnergyUsagePrediction(vm, host, vmsDeployed).getTotalEnergyUsed();
    }

}
