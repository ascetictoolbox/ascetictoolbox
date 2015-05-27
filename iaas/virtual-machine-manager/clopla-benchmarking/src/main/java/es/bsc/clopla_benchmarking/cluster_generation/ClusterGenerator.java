package es.bsc.clopla_benchmarking.cluster_generation;

import es.bsc.clopla_benchmarking.models.Cluster;

public class ClusterGenerator {

    // Suppress default constructor for non-instantiability
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
        int maxVmCpus = getMaxVmDimensionSize(nVms, nHosts, 1,
                hostDimensions.getMinCpus(), hostDimensions.getMaxCpus(), loadPerc);
        int maxVmRamGb = getMaxVmDimensionSize(nVms, nHosts,1, hostDimensions.getMinRamGb(),
                hostDimensions.getMaxRamGb(), loadPerc);
        int maxVmDiskGb = getMaxVmDimensionSize(nVms, nHosts,1, hostDimensions.getMinDiskGb(),
                hostDimensions.getMaxDiskGb(), loadPerc);
        VmDimensions vmDimensions = new VmDimensions(1, maxVmCpus, 1, maxVmRamGb, 1, maxVmDiskGb);

        return new Cluster(
                VmCollectionGenerator.generateVmCollection(nVms, vmDimensions),
                HostCollectionGenerator.generateHostCollection(nHosts, hostDimensions));
    }

    private static int getMaxVmDimensionSize(int nVms, int nHosts, int minVmDimensionSize, int minHostDimensionSize,
                                             int maxHostDimensionSize, int loadPerc) {
        return (int) Math.round(((((loadPerc/100.0)*(minHostDimensionSize + maxHostDimensionSize)) - minVmDimensionSize))
                *((1.0*nHosts)/nVms));
    }

}
