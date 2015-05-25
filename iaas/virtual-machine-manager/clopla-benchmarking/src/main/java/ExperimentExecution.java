import es.bsc.clopla.placement.config.VmPlacementConfig;

public class ExperimentExecution {

    private final Cluster cluster;
    private final VmPlacementConfig vmPlacementConfig;

    public ExperimentExecution(Cluster cluster, VmPlacementConfig vmPlacementConfig) {
        this.cluster = cluster;
        this.vmPlacementConfig = vmPlacementConfig;
    }

    public Cluster getCluster() {
        return cluster;
    }

    public VmPlacementConfig getVmPlacementConfig() {
        return vmPlacementConfig;
    }

}
