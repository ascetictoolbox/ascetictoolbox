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

}
