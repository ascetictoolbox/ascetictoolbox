package es.bsc.vmmanagercore.model;

import org.apache.commons.lang3.builder.ReflectionToStringBuilder;

/**
 * Load of a server (% of cpu, ram, and disk used).
 *
 * @author David Ortiz Lopez (david.ortiz@bsc.es)
 */
public class ServerLoad {

    // All the loads have values in the [0,1] range
    private double cpuLoad;
    private double ramLoad;
    private double diskLoad;

    /**
     * Class constructor.
     *
     * @param cpuLoad the CPU load
     * @param ramLoad the RAM load
     * @param diskLoad the disk load
     */
    public ServerLoad(double cpuLoad, double ramLoad, double diskLoad) {
        this.cpuLoad = cpuLoad;
        this.ramLoad = ramLoad;
        this.diskLoad = diskLoad;
    }

    public double getTotalOverload() {
        return getCpuOverload() + getRamOverload() + getDiskOverload();
    }

    public double getCpuLoad() {
        return cpuLoad;
    }

    public void setCpuLoad(double cpuLoad) {
        this.cpuLoad = cpuLoad;
    }

    public double getRamLoad() {
        return ramLoad;
    }

    public void setRamLoad(double ramLoad) {
        this.ramLoad = ramLoad;
    }

    public double getDiskLoad() {
        return diskLoad;
    }

    public void setDiskLoad(double diskLoad) {
        this.diskLoad = diskLoad;
    }

    @Override
    public String toString() {
        return ReflectionToStringBuilder.toString(this);
    }

    private double getCpuOverload() {
        return cpuLoad > 1 ? cpuLoad - 1 : 0;
    }

    private double getRamOverload() {
        return ramLoad > 1 ? ramLoad - 1 : 0;
    }

    private double getDiskOverload() {
        return diskLoad > 1 ? diskLoad - 1 : 0;
    }

}
