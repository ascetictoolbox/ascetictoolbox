package es.bsc.vmmanagercore.scheduler;

import es.bsc.vmmanagercore.model.Vm;
import es.bsc.vmmanagercore.model.VmDeployed;
import es.bsc.vmmanagercore.monitoring.HostInfo;
import eu.ascetic.asceticarchitecture.iaas.energymodeller.EnergyModeller;
import es.bsc.vmmanagercore.integration.VMMToEMConversor;

import java.util.ArrayList;
import java.util.List;

/**
 * Energy-aware scheduling algorithm.
 * This scheduling algorithm chooses the host where the energy consumed will be the lowest.
 * This decision is taken according to the predictions performed by the Energy Modeller.
 *
 * @author David Ortiz Lopez (david.ortiz@bsc.es)
 *
 */
public class SchedAlgEnergyAware implements SchedAlgorithm {

    ArrayList<VmDeployed> vmsDeployed = new ArrayList<>();
    EnergyModeller energyModeller = new EnergyModeller();

    public SchedAlgEnergyAware(ArrayList<VmDeployed> vmsDeployed) {
        this.vmsDeployed = vmsDeployed;
    }

    /**
     * Returns the VMs deployed in a given host.
     *
     * @param hostname the host name
     * @return the VMs deployed in the host
     */
    private ArrayList<Vm> getVmsDeployedInHost(String hostname) {
        ArrayList<Vm> vms = new ArrayList<>();
        for (VmDeployed vm: vmsDeployed) {
            if (vm.getHostName().equals(hostname)) {
                vms.add(vm);
            }
        }
        return vms;
    }

    @Override
    public String chooseHost(List<HostInfo> hostsInfo, Vm vm) {
        String bestHost = null; // Host that consumes less energy
        double minimumAvgPower = Integer.MAX_VALUE; // Avg. power consumed in the host that consumes less energy
        for (HostInfo host: hostsInfo) {
            double predictedAvgPower = energyModeller.getPredictedEnergyForVM(
                   VMMToEMConversor.getVmEnergyModFromVM(vm),
                   VMMToEMConversor.getVmsEnergyModFromVms(getVmsDeployedInHost(host.getHostname())),
                   VMMToEMConversor.getHostEnergyModFromHost(host))
                   .getAvgPowerUsed();
            if (bestHost == null || predictedAvgPower < minimumAvgPower) {
                bestHost = host.getHostname();
                minimumAvgPower = predictedAvgPower;
            }
        }
        System.out.println(bestHost + " " + minimumAvgPower);
        return bestHost;
    }

}
