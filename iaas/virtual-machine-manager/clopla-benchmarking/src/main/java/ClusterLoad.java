import com.google.common.base.MoreObjects;

public class ClusterLoad {

    private final double cpuLoad;
    private final double ramLoad;
    private final double diskLoad;

    public ClusterLoad(double cpuLoad, double ramLoad, double diskLoad) {
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

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
                .add("cpuLoad", cpuLoad)
                .add("ramLoad", ramLoad)
                .add("diskLoad", diskLoad)
                .toString();
    }

}
