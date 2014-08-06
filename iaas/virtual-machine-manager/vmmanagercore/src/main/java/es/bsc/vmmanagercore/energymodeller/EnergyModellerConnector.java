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

        VM vmem = VMMToEMConversor.getVmEnergyModFromVM(vm);
        System.out.println("VM input - cpus: " + vmem.getCpus() + "ram: " + vmem.getRamMb() + "disk: "  +
                vmem.getDiskGb());

        List<VM> vmsem = VMMToEMConversor.getVmsEnergyModFromVms(getVmsDeployedInHost(host.getHostname(), vmsDeployed));
        System.out.println("LIST OF VMS input");
        for (VM vmem_: vmsem) {
            System.out.println("VM input - cpus: " + vmem_.getCpus() + "ram: " + vmem_.getRamMb() + "disk: "  +
                    vmem_.getDiskGb());
        }

        eu.ascetic.asceticarchitecture.iaas.energymodeller.types.energyuser.Host hostem =
                VMMToEMConversor.getHostEnergyModFromHost(host);
        System.out.println("HOST input - id:" + hostem.getId() + "hostname: " + hostem.getHostName());


        /*return energyModeller.getPredictedEnergyForVM(
                VMMToEMConversor.getVmEnergyModFromVM(vm),
                VMMToEMConversor.getVmsEnergyModFromVms(getVmsDeployedInHost(host.getHostname(), vmsDeployed)),
                VMMToEMConversor.getHostEnergyModFromHost(host));
        */
        EnergyUsagePrediction result = energyModeller.getPredictedEnergyForVM(
                VMMToEMConversor.getVmEnergyModFromVM(vm),
                VMMToEMConversor.getVmsEnergyModFromVms(getVmsDeployedInHost(host.getHostname(), vmsDeployed)),
                VMMToEMConversor.getHostEnergyModFromHost(host));

        System.out.println(
                "Avg power " + result.getAvgPowerUsed()
                + "total energy used " + result.getTotalEnergyUsed()
                + "duration " + result.getDuration()
                + "start time " + result.getPredictionStartTime()
                + "end time " + result.getPredictionEndTime());

        return result;
    }

    public static double getPredictedAvgPowerVm(Vm vm, Host host, List<VmDeployed> vmsDeployed) {
        return getEnergyUsagePrediction(vm, host, vmsDeployed).getAvgPowerUsed();
    }

    public static double getPredictedEnergyVm(Vm vm, Host host, List<VmDeployed> vmsDeployed) {
        return getEnergyUsagePrediction(vm, host, vmsDeployed).getTotalEnergyUsed();
    }

}
