public class ClusterGenerator {

    // Supress default constructor for non-instantiability
    private ClusterGenerator() {
        throw new AssertionError();
    }

    public static Cluster generateCluster(int nVms, VmDimensions vmDimensions,
                                          int nHosts, HostDimensions hostDimensions) {
        return new Cluster(
                VmCollectionGenerator.generateVmCollection(nVms, vmDimensions),
                HostCollectionGenerator.generateHostCollection(nHosts, hostDimensions));
    }

    // Basic algorithm. Does not cover many cases
    // loadCpuCluster = (min_cpu_size_vm + max_cpu_size_vm)/(min_cpu_size_host + min_cpu_size_host).
    // Assuming uniform distribution!
    public static Cluster generateCluster(int nVms, int loadPerc, int nHosts, HostDimensions hostDimensions) {
        VmDimensions vmDimensions = new VmDimensions(
                1, getMaxVmDimensionSize(1, hostDimensions.getMinCpus(), hostDimensions.getMaxCpus(), loadPerc),
                1, getMaxVmDimensionSize(1, hostDimensions.getMinRamGb(), hostDimensions.getMaxRamGb(), loadPerc),
                1, getMaxVmDimensionSize(1, hostDimensions.getMinDiskGb(), hostDimensions.getMaxDiskGb(), loadPerc));
        return new Cluster(
                VmCollectionGenerator.generateVmCollection(nVms, vmDimensions),
                HostCollectionGenerator.generateHostCollection(nHosts, hostDimensions));
    }

    private static int getMaxVmDimensionSize(int minVmDimensionSize, int minHostDimensionSize,
                                             int maxHostDimensionSize, int loadPerc) {
        return (int)(((loadPerc/100.0)*(minHostDimensionSize + maxHostDimensionSize)) - minVmDimensionSize);
    }

}
