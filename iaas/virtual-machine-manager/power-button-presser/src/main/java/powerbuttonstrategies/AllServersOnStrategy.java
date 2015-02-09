package powerbuttonstrategies;

import models.ClusterState;
import models.Host;
import vmm.VmmClient;

import java.util.List;

public class AllServersOnStrategy implements PowerButtonStrategy {

    private final VmmClient vmmClient;

    public AllServersOnStrategy(VmmClient vmmClient) {
        this.vmmClient = vmmClient;
    }

    @Override
    public void applyStrategy(ClusterState clusterState) {
        pressPowerButton(clusterState.getSwitchedOffHosts());
    }
    
    private void pressPowerButton(List<Host> hosts) {
        for (Host host: hosts) {
            vmmClient.pressPowerButton(host.getHostname());
        }
    }

}
