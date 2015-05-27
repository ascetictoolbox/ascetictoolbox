package es.bsc.clopla_benchmarking.experiments;

import com.google.common.base.MoreObjects;
import es.bsc.clopla.placement.config.localsearch.LocalSearch;

import java.util.ArrayList;
import java.util.List;

public class ExperimentExecutionResults {

    private final int nHosts;
    private final int nVms;
    private final double avgLoad;
    private final LocalSearch localSearchAlg;
    private final List<Integer> scores = new ArrayList<>();

    public ExperimentExecutionResults(int nHosts, int nVms, double avgLoad, LocalSearch localSearchAlg,
                                      List<Integer> scores) {
        this.nHosts = nHosts;
        this.nVms = nVms;
        this.avgLoad = avgLoad;
        this.localSearchAlg = localSearchAlg;
        this.scores.addAll(scores);
    }

    public int getnHosts() {
        return nHosts;
    }

    public int getnVms() {
        return nVms;
    }

    public double getAvgLoad() {
        return avgLoad;
    }

    public LocalSearch getLocalSearch() {
        return localSearchAlg;
    }

    public List<Integer> getScores() {
        return scores;
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
                .add("nHosts", nHosts)
                .add("nVms", nVms)
                .add("avgLoad", avgLoad)
                .add("localSearchAlg", localSearchAlg)
                .add("scores", scores)
                .toString();
    }

}
