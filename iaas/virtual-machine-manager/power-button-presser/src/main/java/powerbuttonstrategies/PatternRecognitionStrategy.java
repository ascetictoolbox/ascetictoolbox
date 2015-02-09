package powerbuttonstrategies;

import models.ClusterState;
import vmm.VmmClient;

public class PatternRecognitionStrategy implements PowerButtonStrategy {
    
    private final VmmClient vmmClient;

    public PatternRecognitionStrategy(VmmClient vmmClient) {
        this.vmmClient = vmmClient;
    }

    @Override
    public void applyStrategy(ClusterState clusterState) {
        
    }
    
}
