package es.bsc.clopla_benchmarking.experiments;

import java.util.ArrayList;
import java.util.List;

public class ExperimentRunner {

    // Suppress default constructor for non-instantiability
    private ExperimentRunner() {
        throw new AssertionError();
    }

    public static List<ExperimentExecutionResults> runExperiment(Experiment experiment) {
        List<ExperimentExecutionResults> result = new ArrayList<>();
        for (ExperimentExecution experimentExecution : experiment.getExperimentExecutions()) {
            result.add(ExperimentExecutionRunner.runExperimentExecution(experimentExecution));
        }
        return result;
    }

}
