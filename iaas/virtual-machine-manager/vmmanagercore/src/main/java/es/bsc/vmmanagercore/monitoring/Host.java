package es.bsc.vmmanagercore.monitoring;


import es.bsc.vmmanagercore.model.ServerLoad;
import es.bsc.vmmanagercore.model.Vm;

import java.util.List;

/**
 * Status of a host of an infrastructure.
 * 
 * @author David Ortiz Lopez (david.ortiz@bsc.es)
 *
 */
public abstract class Host {

    protected String hostname;
    protected int totalCpus;
    protected double totalMemoryMb;
    protected double totalDiskGb;
    protected double assignedCpus;
    protected double assignedMemoryMb;
    protected double assignedDiskGb;
    protected double currentPower;

    /**
     * Class constructor
     * @param hostname host name
     */
    public Host(String hostname) {
        this.hostname = hostname;
    }

    /**
     * Checks whether a host has enough available resources to host a VM.
     * @param cpus number of CPUs needed by the VM
     * @param memoryMb memory needed by the VM (in MB)
     * @param diskGb disk space needed by the VM (in GB)
     * @return Returns true if the host has enough available resources to host the VM.
     * Returns false if the host does not have enough available resources
     */
    public boolean hasEnoughResources(int cpus, int memoryMb, int diskGb) {
        return (getFreeCpus() >= cpus) && (getFreeMemoryMb() >= memoryMb) && (getFreeDiskGb() >= diskGb);
    }

    /**
     * Checks whether a specific host has enough resources to deploy a set of VMs.
     *
     * @param vms the list of VMs
     * @return true if the host has enough resources available, false otherwise
     */
    public boolean hasEnoughResourcesToDeployVms(List<Vm> vms) {
        int totalCpus, totalRamMb, totalDiskGb;
        totalCpus = totalRamMb = totalDiskGb = 0;
        for (Vm vm: vms) {
            totalCpus += vm.getCpus();
            totalRamMb += vm.getRamMb();
            totalDiskGb += vm.getDiskGb();
        }
        return hasEnoughResources(totalCpus, totalRamMb, totalDiskGb);
    }

    /**
     * @param hostname host name to set
     */
    public void setHostname(String hostname) {
        this.hostname = hostname;
    }

    /**
     * @return host name
     */
    public String getHostname() {
        return hostname;
    }

    /**
     * @return total number of CPUs of the host
     */
    public int getTotalCpus() {
        return totalCpus;
    }

    /**
     * @return total memory of the host (in MB)
     */
    public double getTotalMemoryMb() {
        return totalMemoryMb;
    }

    /**
     * @return total disk space of the host (in GB)
     */
    public double getTotalDiskGb() {
        return totalDiskGb;
    }

    /**
     * @return assigned CPUs of the host
     */
    public double getAssignedCpus() {
        return assignedCpus;
    }

    /**
     * @return assigned memory of the host (in MB)
     */
    public double getAssignedMemoryMb() {
        return assignedMemoryMb;
    }

    /**
     * @return assigned disk space of the host (in GB)
     */
    public double getAssignedDiskGb() {
        return assignedDiskGb;
    }

    /**
     * @return number of available CPUs of the host
     */
    public double getFreeCpus() {
        return totalCpus - assignedCpus;
    }

    /**
     * @return available memory of the host (in MB)
     */
    public double getFreeMemoryMb() {
        return totalMemoryMb - assignedMemoryMb;
    }

    /**
     * @return available disk space of the host (in GB)
     */
    public double getFreeDiskGb() {
        return totalDiskGb - assignedDiskGb;
    }

    /**
     * Returns the load that a host would have if a VM was deployed in it.
     *
     * @param vm the VM to deploy
     * @return the future load
     */
    public ServerLoad getFutureLoadIfVMDeployed(Vm vm) {
        double cpus = getAssignedCpus() + vm.getCpus();
        double ramMb = getAssignedMemoryMb() + vm.getRamMb();
        double diskGb = getAssignedDiskGb() + vm.getDiskGb();
        return new ServerLoad(cpus/getTotalCpus(), ramMb/getTotalMemoryMb(), diskGb/getTotalDiskGb());
    }

    public void updateAssignedCpus(double assignedCpus) {
        this.assignedCpus = assignedCpus;
    }

    public void updateAssignedMemoryMb(double assignedMemoryMb) {
        this.assignedMemoryMb = assignedMemoryMb;
    }

    public void updateAssignedDiskGb(double assignedDiskGb) {
        this.assignedDiskGb = assignedDiskGb;
    }

    public ServerLoad getServerLoad() {
        return new ServerLoad(assignedCpus/totalCpus, assignedMemoryMb/totalMemoryMb, assignedDiskGb/totalDiskGb);
    }

}
