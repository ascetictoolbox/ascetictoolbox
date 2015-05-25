import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Experiment {

    private final List<ExperimentExecution> experimentExecutions = new ArrayList<>();

    public Experiment(List<ExperimentExecution> experimentExecutions) {
        this.experimentExecutions.addAll(experimentExecutions);
    }

    public List<ExperimentExecution> getExperimentExecutions() {
        return Collections.unmodifiableList(experimentExecutions);
    }

}
