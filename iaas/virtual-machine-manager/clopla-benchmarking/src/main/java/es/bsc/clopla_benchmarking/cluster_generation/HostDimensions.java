package es.bsc.clopla_benchmarking.cluster_generation;

import com.google.common.base.MoreObjects;

import static com.google.common.base.Preconditions.checkArgument;

public class HostDimensions {

    private final int minCpus;
    private final int maxCpus;
    private final int minRamGb;
    private final int maxRamGb;
    private final int minDiskGb;
    private final int maxDiskGb;

    public HostDimensions(int minCpus, int maxCpus, int minRamGb, int maxRamGb,
                          int minDiskGb, int maxDiskGb) {
        checkConstructorParams(minCpus, maxCpus, minRamGb, maxRamGb, minDiskGb, maxDiskGb);
        this.minCpus = minCpus;
        this.maxCpus = maxCpus;
        this.minRamGb = minRamGb;
        this.maxRamGb = maxRamGb;
        this.minDiskGb = minDiskGb;
        this.maxDiskGb = maxDiskGb;
    }

    public int getMinCpus() {
        return minCpus;
    }

    public int getMaxCpus() {
        return maxCpus;
    }

    public int getMinRamGb() {
        return minRamGb;
    }

    public int getMaxRamGb() {
        return maxRamGb;
    }

    public int getMinDiskGb() {
        return minDiskGb;
    }

    public int getMaxDiskGb() {
        return maxDiskGb;
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
                .add("minCpus", minCpus)
                .add("maxCpus", maxCpus)
                .add("minRamGb", minRamGb)
                .add("maxRamGb", maxRamGb)
                .add("minDiskGb", minDiskGb)
                .add("maxDiskGb", maxDiskGb)
                .toString();
    }

    private void checkConstructorParams(int minCpus, int maxCpus, int minRamGb, int maxRamGb,
                                        int minDiskGb, int maxDiskGb) {
        checkArgument(minCpus >= 0, "minCpus needs to be >= 0");
        checkArgument(maxCpus >= 0, "maxCpus needs to be >= 0");
        checkArgument(minRamGb >= 0, "minRamGb needs to be >= 0");
        checkArgument(maxRamGb >= 0, "maxRamGb needs to be >= 0");
        checkArgument(minDiskGb >= 0, "minDiskGb needs to be >= 0");
        checkArgument(maxDiskGb >= 0, "maxDiskGb needs to be >= 0");
    }

}
