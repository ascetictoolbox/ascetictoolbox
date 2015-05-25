import java.util.ArrayList;
import java.util.List;

public class ExperimentRunner {

    private ExperimentRunner() {
        throw new AssertionError();
    }

    public static List<ExperimentExecutionResults> runExperiment(Experiment experiment, int intervalSeconds) {
        List<ExperimentExecutionResults> result = new ArrayList<>();
        for (ExperimentExecution experimentExecution : experiment.getExperimentExecutions()) {
            result.add(ExperimentExecutionRunner.runExperimentExecution(experimentExecution, intervalSeconds));
        }
        return result;
    }

}
