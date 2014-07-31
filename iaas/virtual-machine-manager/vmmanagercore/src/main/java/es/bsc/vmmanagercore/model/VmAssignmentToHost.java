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

    /**
     * Class constructor.
     *
     * @param vm the VM
     * @param host the host
     */
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

    /**
     * Returns the predicted avg power of the placement.
     *
     * @param vmsDeployed VMs deployed in the infrastructure
     * @return the predicted avg power
     */
    private double getPowerEstimate(List<VmDeployed> vmsDeployed) {
        return EnergyModellerConnector.getPredictedAvgPowerVm(vm, host, vmsDeployed);
    }

    /**
     * Returns the predicted energy estimate of the placement.
     *
     * @param vmsDeployed VMs deployed in the infrastructure
     * @return the predicted energy
     */
    private double getEnergyEstimate(List<VmDeployed> vmsDeployed) {
        return EnergyModellerConnector.getPredictedEnergyVm(vm, host, vmsDeployed);
    }

    /**
     * Returns the predicted price of the placement.
     *
     * @param vmsDeployed VMs deployed in the infrastructure
     * @return the predicted price
     */
    private double getPriceEstimate(List<VmDeployed> vmsDeployed) {
        return PricingModellerConnector.getVmCost(getEnergyEstimate(vmsDeployed), host.getHostname());
    }

    @Override
    public String toString() {
        return getVm().getName() + "-->" + getHost().getHostname();
    }

}
