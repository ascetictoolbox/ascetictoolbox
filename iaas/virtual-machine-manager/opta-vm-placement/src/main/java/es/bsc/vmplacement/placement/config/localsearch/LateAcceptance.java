package es.bsc.vmplacement.placement.config.localsearch;

import org.optaplanner.core.config.localsearch.decider.acceptor.AcceptorConfig;

/**
 * @author David Ortiz (david.ortiz@bsc.es)
 */
public class LateAcceptance extends LocalSearch {

    private final int lateAcceptanceSize;

    public LateAcceptance(int lateAcceptanceSize) {
        this.lateAcceptanceSize = lateAcceptanceSize;
        this.acceptedCountLimit = 1;
    }

    @Override
    public AcceptorConfig getAcceptorConfig() {
        AcceptorConfig result = new AcceptorConfig();
        result.setLateAcceptanceSize(lateAcceptanceSize);
        return result;
    }

}
