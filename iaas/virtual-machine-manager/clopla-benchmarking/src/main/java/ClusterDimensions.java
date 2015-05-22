public class ClusterDimensions {

    private final int nhosts;
    private final HostDimensions hostDimensions;

    public ClusterDimensions(int nhosts, HostDimensions hostDimensions) {
        this.nhosts = nhosts;
        this.hostDimensions = hostDimensions;
    }

    public int getNhosts() {
        return nhosts;
    }

    public HostDimensions getHostDimensions() {
        return hostDimensions;
    }

}
