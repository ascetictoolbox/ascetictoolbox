package es.bsc.vmplacement.placement.config.localsearch;

import org.optaplanner.core.config.localsearch.decider.acceptor.AcceptorConfig;

/**
 * Simulated annealing algorithm.
 *
 * @author David Ortiz (david.ortiz@bsc.es)
 */
public class SimulatedAnnealing extends LocalSearch {

    private final int initialHardTemp;
    private final int initialSoftTemp;

    public SimulatedAnnealing(int initialHardTemp, int initialSoftTemp) {
        this.initialHardTemp = initialHardTemp;
        this.initialSoftTemp = initialSoftTemp;
        this.acceptedCountLimit = 1;
    }

    @Override
    public AcceptorConfig getAcceptorConfig() {
        AcceptorConfig result = new AcceptorConfig();
        result.setSimulatedAnnealingStartingTemperature(initialTemperatureToString());
        return result;
    }

    private String initialTemperatureToString() {
        return Integer.toString(initialHardTemp) + "hard/" + Integer.toString(initialSoftTemp) + "soft";
    }

}
