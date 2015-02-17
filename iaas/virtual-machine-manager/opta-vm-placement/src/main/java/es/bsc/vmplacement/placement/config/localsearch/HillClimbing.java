package es.bsc.vmplacement.placement.config.localsearch;

import org.optaplanner.core.config.localsearch.decider.acceptor.AcceptorConfig;

import java.util.ArrayList;
import java.util.List;

/**
 * Hill Climbing algorithm.
 *
 * @author David Ortiz (david.ortiz@bsc.es)
 */
public class HillClimbing extends LocalSearch {

    public HillClimbing() {
        this.acceptedCountLimit = 1;
    }

    @Override
    public AcceptorConfig getAcceptorConfig() {
        AcceptorConfig result = new AcceptorConfig();
        List<AcceptorConfig.AcceptorType> acceptorTypeList = new ArrayList<>();
        acceptorTypeList.add(AcceptorConfig.AcceptorType.HILL_CLIMBING);
        result.setAcceptorTypeList(acceptorTypeList);
        return result;
    }

}
