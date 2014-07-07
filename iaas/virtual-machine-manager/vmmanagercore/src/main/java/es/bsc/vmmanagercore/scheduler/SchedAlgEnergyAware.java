package es.bsc.vmmanagercore.scheduler;

import es.bsc.vmmanagercore.integration.VMMToEMConversor;
import es.bsc.vmmanagercore.model.DeploymentPlan;
import es.bsc.vmmanagercore.model.Vm;
import es.bsc.vmmanagercore.model.VmAssignmentToHost;
import es.bsc.vmmanagercore.model.VmDeployed;
import es.bsc.vmmanagercore.monitoring.Host;
import eu.ascetic.asceticarchitecture.iaas.energymodeller.EnergyModeller;

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

    List<VmDeployed> vmsDeployed = new ArrayList<>();
    EnergyModeller energyModeller = new EnergyModeller();

    public SchedAlgEnergyAware(List<VmDeployed> vmsDeployed) {
        this.vmsDeployed = vmsDeployed;
    }

    /**
     * Returns the VMs deployed in a given host.
     *
     * @param hostname the host name
     * @return the VMs deployed in the host
     */
    private List<Vm> getVmsDeployedInHost(String hostname) {
        List<Vm> vms = new ArrayList<>();
        for (VmDeployed vm: vmsDeployed) {
            if (vm.getHostName().equals(hostname)) {
                vms.add(vm);
            }
        }
        return vms;
    }

    private double getPredictedAvgPowerVm(Vm vm, Host host) {
        return energyModeller.getPredictedEnergyForVM(
                VMMToEMConversor.getVmEnergyModFromVM(vm),
                VMMToEMConversor.getVmsEnergyModFromVms(getVmsDeployedInHost(host.getHostname())),
                VMMToEMConversor.getHostEnergyModFromHost(host))
                .getAvgPowerUsed();
    }

    private double getPredictedAvgPowerDeploymentPlan(DeploymentPlan deploymentPlan) {
        double result = 0;
        for (VmAssignmentToHost vmAssignmentToHost: deploymentPlan.getVmsAssignationsToHosts()) {
            result += getPredictedAvgPowerVm(vmAssignmentToHost.getVm(), vmAssignmentToHost.getHost());
        }
        return result;
    }

    @Override
    public boolean isBetterDeploymentPlan(DeploymentPlan deploymentPlan1, DeploymentPlan deploymentPlan2,
            List<Host> hosts) {
        return getPredictedAvgPowerDeploymentPlan(deploymentPlan1) <=
                getPredictedAvgPowerDeploymentPlan(deploymentPlan2);
    }

}
