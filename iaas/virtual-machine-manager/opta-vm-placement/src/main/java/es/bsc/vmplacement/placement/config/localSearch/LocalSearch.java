package es.bsc.vmplacement.vmplacement.config.localSearch;

import org.optaplanner.core.config.localsearch.decider.acceptor.AcceptorConfig;
import org.optaplanner.core.config.localsearch.decider.forager.ForagerConfig;

/**
 * @author David Ortiz (david.ortiz@bsc.es)
 */
public abstract class LocalSearch {

    protected int acceptedCountLimit = 1;

    public abstract AcceptorConfig getAcceptorConfig();

    public ForagerConfig getForagerConfig() {
        ForagerConfig result = new ForagerConfig();
        result.setAcceptedCountLimit(acceptedCountLimit);
        return result;
    }

}
