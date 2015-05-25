import com.google.common.base.MoreObjects;

import java.util.ArrayList;
import java.util.List;

public class ExperimentExecutionResults {

    private final int nHosts;
    private final int nVms;
    private final ClusterLoad clusterLoad;
    private final String localSearchAlg;
    private final List<Integer> scores = new ArrayList<>();

    public ExperimentExecutionResults(int nHosts, int nVms, ClusterLoad clusterLoad, String localSearchAlg,
                                      List<Integer> scores) {
        this.nHosts = nHosts;
        this.nVms = nVms;
        this.clusterLoad = clusterLoad;
        this.localSearchAlg = localSearchAlg;
        this.scores.addAll(scores);
    }

    public int getnHosts() {
        return nHosts;
    }

    public int getnVms() {
        return nVms;
    }

    public ClusterLoad getClusterLoad() {
        return clusterLoad;
    }

    public String getLocalSearch() {
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
                .add("clusterLoad", clusterLoad)
                .add("localSearchAlg", localSearchAlg)
                .add("scores", scores)
                .toString();
    }

}
