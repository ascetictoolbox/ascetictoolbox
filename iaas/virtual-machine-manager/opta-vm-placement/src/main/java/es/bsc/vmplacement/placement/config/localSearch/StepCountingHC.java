package es.bsc.vmplacement.vmplacement.config.localSearch;

import org.optaplanner.core.config.localsearch.decider.acceptor.AcceptorConfig;

/**
 * @author David Ortiz (david.ortiz@bsc.es)
 */
public class StepCountingHC extends LocalSearch {

    private final int stepCountingHillClimbingSize;

    public StepCountingHC(int stepCountingHillClimbingSize) {
        this.stepCountingHillClimbingSize = stepCountingHillClimbingSize;
        this.acceptedCountLimit = 1;
    }

    @Override
    public AcceptorConfig getAcceptorConfig() {
        AcceptorConfig result = new AcceptorConfig();
        result.setStepCountingHillClimbingSize(stepCountingHillClimbingSize);
        return result;
    }

}
