package es.bsc.vmplacement.placement.config;

import es.bsc.vmplacement.domain.ConstructionHeuristic;
import es.bsc.vmplacement.placement.config.localsearch.LocalSearch;

/**
 * This class defines the configuration for the solver of the VM Placement problem.
 *
 * @author David Ortiz (david.ortiz@bsc.es)
 */
public class VmPlacementConfig {

    private final Policy policy;
    private final int timeLimitSeconds;
    private final ConstructionHeuristic constructionHeuristic;
    private final LocalSearch localSearch;
    private final boolean vmsAreFixed; // When set to true, the VMs that are already assigned to a host should not be
                                       // moved to a different one

    public VmPlacementConfig(Policy policy, int timeLimitSeconds, ConstructionHeuristic constructionHeuristic,
            LocalSearch localSearch, boolean vmsAreFixed) {
        this.policy = policy;
        this.timeLimitSeconds = timeLimitSeconds;
        this.constructionHeuristic = constructionHeuristic;
        this.localSearch = localSearch;
        this.vmsAreFixed = vmsAreFixed;
    }

    public Policy getPolicy() {
        return policy;
    }

    public int getTimeLimitSeconds() {
        return timeLimitSeconds;
    }

    public ConstructionHeuristic getConstructionHeuristic() {
        return constructionHeuristic;
    }

    public LocalSearch getLocalSearch() {
        return localSearch;
    }

    public boolean vmsAreFixed() {
        return vmsAreFixed;
    }

}
