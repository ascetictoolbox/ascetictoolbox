package es.bsc.vmmanagercore.monitoring;


/**
 * This class contains information about the status of a fake host of the infrastructure.
 * This class is useful to perform tests without configuring real hosts.
 * 
 * @author David Ortiz Lopez (david.ortiz@bsc.es)
 *
 */
public class HostInfoFake extends HostInfo {

    /**
     * Class constructor
     * @param hostname host name
     */
    public HostInfoFake(String hostname) {
        super(hostname);
    }

    /**
     * Class constructor
     * @param hostname host name
     * @param totalCpus total number of CPUs of the host
     * @param totalMemoryMb total memory of the host (in MB)
     * @param totalDiskGb total disk space of the host (in GB)
     * @param assignedCpus assigned CPUs of the host
     * @param assignedMemoryMb assigned memory of the host (in MB)
     * @param assignedDiskGb assigned disk space of the host (in GB)
     */
    public HostInfoFake(String hostname, int totalCpus, int totalMemoryMb, int totalDiskGb,
            double assignedCpus, int assignedMemoryMb, int assignedDiskGb) {
        super(hostname);
        checkConstructorParams(totalCpus, totalMemoryMb, totalDiskGb, assignedCpus,
                assignedMemoryMb, assignedDiskGb);
        this.totalCpus = totalCpus;
        this.totalMemoryMb = totalMemoryMb;
        this.totalDiskGb = totalDiskGb;
        this.assignedCpus = assignedCpus;
        this.assignedMemoryMb = assignedMemoryMb;
        this.assignedDiskGb = assignedDiskGb;
    }

    //TODO: I think this method should be in the class HostInfo.
    private void checkConstructorParams(int totalCpus, int totalMemoryMb, int totalDiskGb,
            double assignedCpus, int assignedMemoryMb, int assignedDiskGb) {
        if (totalCpus <= 0) {
            throw new IllegalArgumentException("The number of total cpus has to be greater than 0");
        }
        if (totalMemoryMb <= 0) {
            throw new IllegalArgumentException("The total memory has to be greater than 0");
        }
        if (totalDiskGb <= 0) {
            throw new IllegalArgumentException("The total disk size has to be greater than 0");
        }
        if (assignedCpus < 0) {
            throw new IllegalArgumentException("The number of assigned cpus cannot be negative");
        }
        if (assignedMemoryMb < 0) {
            throw new IllegalArgumentException("The amount of assigned memory cannot be negative");
        }
        if (assignedDiskGb < 0) {
            throw new IllegalArgumentException("The amount of assigned disk cannot be negative");
        }
        if (assignedCpus > totalCpus) {
            throw new IllegalArgumentException("The number of assigned cpus cannot be greater"
                    + " than the total number of cpus");
        }
        if (assignedMemoryMb > totalMemoryMb) {
            throw new IllegalArgumentException("The amount of assigned memory cannot be greater"
                    + " than the total amount of memory");
        }
        if (assignedDiskGb > totalDiskGb) {
            throw new IllegalArgumentException("The assigned disk space cannot be greater"
                    + " than the total amount of disk space");
        }
    }

}
