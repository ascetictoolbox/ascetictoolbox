package es.bsc.clopla_benchmarking;

import es.bsc.clopla.placement.config.localsearch.*;

import java.util.List;

public class ResultsCsvConverter {

    /*
    There are a couple of "tricky" attributes in the ExperimentExecutionResults class when converting
    an instance to CSV. The first one is the local search algorithm, because each local search algorithm has
    different configuration parameters. The second is a list of scores, one for each time interval defined.
    For this reason, and because ExperimentExecutionResults is the only class that needs to be converted to CSV,
    I decided to use a custom CSV converter.
     */

    // Suppress default constructor for non-instantiability
    private ResultsCsvConverter() {
        throw new AssertionError();
    }

    public static String[] CSV_FIXED_HEADERS = {"hosts", "vms", "avgLoad", "algorithm"};

    public static String experimentExecutionResultsToCsv(List<ExperimentExecutionResults> experimentExecutionResults) {
        StringBuilder resultStringBuilder = new StringBuilder(
                csvHeaders(numberOfScores(experimentExecutionResults))).append("\n");

        for (ExperimentExecutionResults experimentExecutionResult : experimentExecutionResults) {
            resultStringBuilder
                    .append(experimentExecutionResultToCsv(experimentExecutionResult))
                    .append("\n");
        }

        return resultStringBuilder.toString();
    }

    private static int numberOfScores(List<ExperimentExecutionResults> experimentExecutionResults) {
        // All the experiments have the same number of intervals (= number of scores).
        // Therefore, we can just simply grab the first one and check how many scores it has.
        return experimentExecutionResults.get(0).getScores().size();
    }

    private static String csvHeaders(int nscores) {
        StringBuilder resultStringBuilder = new StringBuilder();

        for (String fixedHeader : CSV_FIXED_HEADERS) {
            resultStringBuilder.append(fixedHeader).append(",");
        }

        for (int i = 0; i < nscores; ++i) {
            resultStringBuilder.append("sc").append(Integer.toString(i)).append(",");
        }

        return resultStringBuilder.substring(0, resultStringBuilder.length() - 1); // Remove last comma
    }

    private static String experimentExecutionResultToCsv(ExperimentExecutionResults experimentExecutionResults) {
        StringBuilder resultStringBuilder = new StringBuilder(experimentExecutionResults.getnHosts() + ","
                + experimentExecutionResults.getnVms() + ","
                + (int) (experimentExecutionResults.getAvgLoad()*100) + ","
                + localSearchAlgToString(experimentExecutionResults.getLocalSearch()) + ",");

        // Append all the intermediate scores
        for (int scoreAtTimeInterval : experimentExecutionResults.getScores()) {
            resultStringBuilder.append(scoreAtTimeInterval).append(",");
        }

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
