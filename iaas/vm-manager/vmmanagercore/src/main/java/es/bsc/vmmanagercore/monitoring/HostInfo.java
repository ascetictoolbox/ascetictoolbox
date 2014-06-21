package es.bsc.vmmanagercore.monitoring;


/**
 * Status of a host of an infrastructure.
 * 
 * @author David Ortiz Lopez (david.ortiz@bsc.es)
 *
 */
public abstract class HostInfo {

    protected String hostname;
    protected int totalCpus;
    protected int totalMemoryMb;
    protected double totalDiskGb;
    protected double assignedCpus;
    protected int assignedMemoryMb;
    protected double assignedDiskGb;

    //these reserved attributes are used when it has been decided to deploy a VM on a host, but
    //the deployment has not been done yet
    protected double reservedCpus;
    protected int reservedMemoryMb;
    protected int reservedDiskGb;

    /**
     * Class constructor
     * @param hostname host name
     */
    public HostInfo(String hostname) {
        this.hostname = hostname;
        reservedCpus = reservedMemoryMb = reservedDiskGb = 0;
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
        return (getFreeCpus() >= cpus) && (getFreeMemoryMb() >= memoryMb) &&
                (getFreeDiskGb() >= diskGb);
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
    public int getTotalMemoryMb() {
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
    public int getAssignedMemoryMb() {
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
        return totalCpus - assignedCpus - reservedCpus;
    }

    /**
     * @return available memory of the host (in MB)
     */
    public int getFreeMemoryMb() {
        return totalMemoryMb - assignedMemoryMb - reservedMemoryMb;
    }

    /**
     * @return available disk space of the host (in GB)
     */
    public double getFreeDiskGb() {
        return totalDiskGb - assignedDiskGb - reservedDiskGb;
    }

    public void resetReserved() {
        reservedCpus = reservedMemoryMb = reservedDiskGb = 0;
    }

    public double getReservedCpus() {
        return reservedCpus;
    }

    public void setReservedCpus(int reservedCpus) {
        if (reservedCpus < 0) {
            throw new IllegalArgumentException("The number of reserved cpus cannot be negative");
        }
        this.reservedCpus = reservedCpus;
    }

    public int getReservedMemoryMb() {
        return reservedMemoryMb;
    }

    public void setReservedMemoryMb(int reservedMemoryMb) {
        if (reservedMemoryMb < 0) {
            throw new IllegalArgumentException("The amount of reserved memory cannot be negative");
        }
        this.reservedMemoryMb = reservedMemoryMb;
    }

    public int getReservedDiskGb() {
        return reservedDiskGb;
    }

    public void setReservedDiskGb(int reservedDiskGb) {
        if (reservedDiskGb < 0) {
            throw new IllegalArgumentException("The amount of reserved disk cannot be negative");
        }
        this.reservedDiskGb = reservedDiskGb;
    }

}
