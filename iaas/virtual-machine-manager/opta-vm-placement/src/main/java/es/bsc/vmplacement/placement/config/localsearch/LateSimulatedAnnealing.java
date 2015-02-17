package es.bsc.vmplacement.placement.config.localsearch;

import org.optaplanner.core.config.localsearch.decider.acceptor.AcceptorConfig;

/**
 * Late simulated annealing algorithm.
 *
 * @author David Ortiz (david.ortiz@bsc.es)
 */
public class LateSimulatedAnnealing extends LocalSearch {

    private final int lateSimulatedAnnealingSize;

    public LateSimulatedAnnealing(int lateSimulatedAnnealingSize, int acceptedCountLimit) {
        this.lateSimulatedAnnealingSize = lateSimulatedAnnealingSize;
        this.acceptedCountLimit = acceptedCountLimit;
    }

    @Override
    public AcceptorConfig getAcceptorConfig() {
        AcceptorConfig result = new AcceptorConfig();
        result.setLateSimulatedAnnealingSize(lateSimulatedAnnealingSize);
        return result;
    }

}
