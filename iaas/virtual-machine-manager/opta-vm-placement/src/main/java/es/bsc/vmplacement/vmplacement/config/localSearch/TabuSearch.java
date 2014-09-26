package es.bsc.vmplacement.vmplacement.config.localSearch;

import org.optaplanner.core.config.localsearch.decider.acceptor.AcceptorConfig;

/**
 * @author David Ortiz (david.ortiz@bsc.es)
 */
public class TabuSearch extends LocalSearch {
    
    private final int entityTabuSize;

    public TabuSearch(int entityTabuSize, int acceptedCountLimit) {
        this.acceptedCountLimit = acceptedCountLimit;
        this.entityTabuSize = entityTabuSize;
    }

    @Override
    public AcceptorConfig getAcceptorConfig() {
        AcceptorConfig result = new AcceptorConfig();
        result.setEntityTabuSize(entityTabuSize);
        return result;
    }

}
