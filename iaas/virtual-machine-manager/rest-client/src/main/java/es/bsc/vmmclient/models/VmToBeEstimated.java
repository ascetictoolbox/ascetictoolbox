package es.bsc.vmmclient.models;

import com.google.common.base.MoreObjects;

public class VmToBeEstimated {

    private final String id;
    private final int vcpus;
    private final int cpuFreq;
    private final int ramMb;
    private final int diskGb;
    private final int swapMb;

    public VmToBeEstimated(String id, int vcpus, int cpuFreq, int ramMb, int diskGb, int swapMb) {
        this.id = id;
        this.vcpus = vcpus;
        this.cpuFreq = cpuFreq;
        this.ramMb = ramMb;
        this.diskGb = diskGb;
        this.swapMb = swapMb;
    }

    public String getId() {
        return id;
    }

    public int getVcpus() {
        return vcpus;
    }

    public int getCpuFreq() {
        return cpuFreq;
    }

    public int getRamMb() {
        return ramMb;
    }

    public int getDiskGb() {
        return diskGb;
    }

    public int getSwapMb() {
        return swapMb;
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
                .add("id", id)
                .add("vcpus", vcpus)
                .add("cpuFreq", cpuFreq)
                .add("ramMb", ramMb)
                .add("diskGb", diskGb)
                .add("swapMb", swapMb)
                .toString();
    }

}
