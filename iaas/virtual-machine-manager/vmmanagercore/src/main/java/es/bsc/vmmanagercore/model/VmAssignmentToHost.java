package es.bsc.vmmanagercore.model;

import es.bsc.vmmanagercore.energymodeller.EnergyModellerConnector;
import es.bsc.vmmanagercore.monitoring.Host;
import es.bsc.vmmanagercore.pricingmodeller.PricingModellerConnector;

import java.util.List;

/**
 * VM placement to a Host.
 *
 * @author David Ortiz Lopez (david.ortiz@bsc.es)
 */
public class VmAssignmentToHost {
    private Vm vm;
    private Host host;

    public VmAssignmentToHost(Vm vm, Host host) {
        this.vm = vm;
        this.host = host;
    }

    public Vm getVm() {
        return vm;
    }

    public Host getHost() {
        return host;
    }

    public VmEstimate getVmEstimate(List<VmDeployed> vmsDeployed) {
        return new VmEstimate(vm.getName(), getPowerEstimate(vmsDeployed), getPriceEstimate(vmsDeployed));
    }

    private double getPowerEstimate(List<VmDeployed> vmsDeployed) {
        return EnergyModellerConnector.getPredictedAvgPowerVm(vm, host, vmsDeployed);
    }

    private double getEnergyEstimate(List<VmDeployed> vmsDeployed) {
        return EnergyModellerConnector.getPredictedEnergyVm(vm, host, vmsDeployed);
    }

    private double getPriceEstimate(List<VmDeployed> vmsDeployed) {
        return PricingModellerConnector.getVmCost(getEnergyEstimate(vmsDeployed), host.getHostname());
    }

}
