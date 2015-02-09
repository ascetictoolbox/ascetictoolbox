package powerbuttonstrategies;

import models.ClusterState;

public interface PowerButtonStrategy {

    public void applyStrategy(ClusterState clusterState);
    
}
