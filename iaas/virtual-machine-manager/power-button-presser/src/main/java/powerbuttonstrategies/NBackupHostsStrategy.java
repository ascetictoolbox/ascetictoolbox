package powerbuttonstrategies;

import models.ClusterState;
import vmm.VmmClient;

public class NBackupHostsStrategy implements PowerButtonStrategy {

    private final VmmClient vmmClient;

    public NBackupHostsStrategy(VmmClient vmmClient) {
        this.vmmClient = vmmClient;
    }

    @Override
    public void applyStrategy(ClusterState clusterState) {
        
    }
    
}
