package es.bsc.clopla_benchmarking.models;

import com.google.common.base.MoreObjects;

import static com.google.common.base.Preconditions.checkArgument;

public class ClusterLoad {

    private final double cpuLoad;
    private final double ramLoad;
    private final double diskLoad;

    public ClusterLoad(double cpuLoad, double ramLoad, double diskLoad) {
        checkConstructorParams(cpuLoad, ramLoad, diskLoad);
        this.cpuLoad = cpuLoad;
        this.ramLoad = ramLoad;
        this.diskLoad = diskLoad;
    }

    public double getCpuLoad() {
        return cpuLoad;
    }

    public double getRamLoad() {
        return ramLoad;
    }

    public double getDiskLoad() {
        return diskLoad;
    }

    public double getAvgLoad() {
        return (cpuLoad + ramLoad + diskLoad)/3;
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
                .add("cpuLoad", cpuLoad)
                .add("ramLoad", ramLoad)
                .add("diskLoad", diskLoad)
                .toString();
    }

    private void checkConstructorParams(double cpuLoad, double ramLoad, double diskLoad) {
        checkArgument(cpuLoad >= 0, "cpuLoad needs to be >= 0");
        checkArgument(ramLoad >= 0, "ramLoad needs to be >= 0");
        checkArgument(diskLoad >= 0, "diskLoad needs to be >= 0");
    }

}
