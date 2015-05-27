package es.bsc.clopla_benchmarking.experiments;

import com.google.common.base.MoreObjects;
import es.bsc.clopla.placement.config.localsearch.LocalSearch;

public class ExperimentExecutionResults {

    private final int nHosts;
    private final int nVms;
    private final double avgLoad;
    private final LocalSearch localSearchAlg;
    private final int seconds;
    private final int score;

    public ExperimentExecutionResults(int nHosts, int nVms, double avgLoad, LocalSearch localSearchAlg,
                                      int seconds, int score) {
        this.nHosts = nHosts;
        this.nVms = nVms;
        this.avgLoad = avgLoad;
        this.localSearchAlg = localSearchAlg;
        this.seconds = seconds;
        this.score = score;
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

    public int getSeconds() {
        return seconds;
    }

    public LocalSearch getLocalSearch() {
        return localSearchAlg;
    }

    public int getScore() {
        return score;
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
                .add("nHosts", nHosts)
                .add("nVms", nVms)
                .add("avgLoad", avgLoad)
                .add("localSearchAlg", localSearchAlg)
                .add("seconds", seconds)
                .add("score", score)
                .toString();
    }

}
