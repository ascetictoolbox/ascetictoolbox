package es.bsc.vmmanagercore.model;

import org.apache.commons.lang3.builder.ReflectionToStringBuilder;

/**
 * @author David Ortiz Lopez (david.ortiz@bsc.es)
 */
public class ServerLoad {

    private double cpuLoad;
    private double ramLoad;
    private double diskLoad;

    public ServerLoad(double cpuLoad, double ramLoad, double diskLoad) {
        this.cpuLoad = cpuLoad;
        this.ramLoad = ramLoad;
        this.diskLoad = diskLoad;
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

}
