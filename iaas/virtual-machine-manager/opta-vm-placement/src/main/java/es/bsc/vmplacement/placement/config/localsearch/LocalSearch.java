package es.bsc.vmplacement.placement.config.localsearch;

import org.optaplanner.core.config.localsearch.decider.acceptor.AcceptorConfig;
import org.optaplanner.core.config.localsearch.decider.forager.ForagerConfig;

/**
 * Local search algorithm.
 *
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
