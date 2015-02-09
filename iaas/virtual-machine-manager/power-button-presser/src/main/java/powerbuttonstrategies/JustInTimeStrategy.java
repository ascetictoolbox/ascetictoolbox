package powerbuttonstrategies;

import models.ClusterState;
import models.Host;
import vmm.VmmClient;

import java.util.List;

public class JustInTimeStrategy implements PowerButtonStrategy {

    private final VmmClient vmmClient;

    public JustInTimeStrategy(VmmClient vmmClient) {
        this.vmmClient = vmmClient;
    }

    @Override
    public void applyStrategy(ClusterState clusterState) {
        pressPowerButton(clusterState.getHostsWithoutVmsAndSwitchedOn());
    }

    private void pressPowerButton(List<Host> hosts) {
        for (Host host: hosts) {
            vmmClient.pressPowerButton(host.getHostname());
        }
    }
    
}
