package es.bsc.clopla_benchmarking.experiments;

import es.bsc.clopla.placement.config.localsearch.*;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class ResultsCsvConverter {

    /*
    There is a "tricky" attribute in the ExperimentExecutionResults class when
    converting an instance to CSV. This attribute is the local search algorithm,
    because each local search algorithm has different configuration parameters.
     */

    // Suppress default constructor for non-instantiability
    private ResultsCsvConverter() {
        throw new AssertionError();
    }

    private static final List<String> CSV_HEADERS =
            Collections.unmodifiableList(Arrays.asList("hosts", "vms", "avgLoad", "algorithm", "score"));

    public static String experimentExecutionResultsToCsv(List<ExperimentExecutionResults> experimentExecutionResults) {
        StringBuilder resultStringBuilder = new StringBuilder(csvHeaders()).append("\n");

        for (ExperimentExecutionResults experimentExecutionResult : experimentExecutionResults) {
            resultStringBuilder
                    .append(experimentExecutionResultToCsv(experimentExecutionResult))
                    .append("\n");
        }

        return resultStringBuilder.toString();
    }

    private static String csvHeaders() {
        StringBuilder resultStringBuilder = new StringBuilder();

        for (String fixedHeader : CSV_HEADERS) {
            resultStringBuilder.append(fixedHeader).append(",");
        }

        return resultStringBuilder.substring(0, resultStringBuilder.length() - 1); // Remove last comma
    }

    private static String experimentExecutionResultToCsv(ExperimentExecutionResults experimentExecutionResults) {
        StringBuilder resultStringBuilder = new StringBuilder(experimentExecutionResults.getnHosts() + ","
                + experimentExecutionResults.getnVms() + ","
                + (int) (experimentExecutionResults.getAvgLoad()*100) + ","
                + localSearchAlgToString(experimentExecutionResults.getLocalSearch()) + ","
                + experimentExecutionResults.getScore());

        return resultStringBuilder.substring(0, resultStringBuilder.length() - 1); // Remove last comma
    }

    // This function would not be needed if Clopla implemented the toString method for each LocalSearch subclass
    private static String localSearchAlgToString(LocalSearch localSearch) {
        String result = localSearch.getClass().getSimpleName();
        if (localSearch.getClass() == HillClimbing.class) {
            return result;
        }
        else if (localSearch.getClass() == LateAcceptance.class) {
            return result + "-" + localSearch.getAcceptorConfig().getLateAcceptanceSize();
        }
        else if (localSearch.getClass() == LateSimulatedAnnealing.class) {
            return result + "-" + localSearch.getAcceptorConfig().getLateSimulatedAnnealingSize()
                    + "-" + localSearch.getForagerConfig().getAcceptedCountLimit();
        }
        else if (localSearch.getClass() == SimulatedAnnealing.class) {
            return result + "-" + localSearch.getAcceptorConfig().getSimulatedAnnealingStartingTemperature();
        }
        else if (localSearch.getClass() == StepCountingHC.class) {
            return result + "-" + localSearch.getAcceptorConfig().getStepCountingHillClimbingSize();
        }
        else if (localSearch.getClass() == TabuSearch.class) {
            return result + "-" + localSearch.getAcceptorConfig().getEntityTabuSize()
                    + "-" + localSearch.getForagerConfig().getAcceptedCountLimit();
        }
        throw new RuntimeException("Error while converting local search alg to string.");
    }

}
