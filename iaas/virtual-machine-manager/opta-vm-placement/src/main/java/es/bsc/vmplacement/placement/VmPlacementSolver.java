package es.bsc.vmplacement.placement;

import es.bsc.vmplacement.placement.config.VmPlacementConfig;
import es.bsc.vmplacement.scorecalculators.*;
import org.optaplanner.core.api.solver.Solver;
import org.optaplanner.core.api.solver.SolverFactory;
import org.optaplanner.core.config.constructionheuristic.ConstructionHeuristicSolverPhaseConfig;
import org.optaplanner.core.config.localsearch.LocalSearchSolverPhaseConfig;
import org.optaplanner.core.config.score.director.ScoreDirectorFactoryConfig;
import org.optaplanner.core.config.solver.SolverConfig;
import org.optaplanner.core.config.solver.XmlSolverFactory;

/**
 * @author David Ortiz (david.ortiz@bsc.es)
 */
public class VmPlacementSolver {

    public static Solver buildSolver(VmPlacementConfig vmPlacementConfig) {
        SolverFactory solverFactory = new XmlSolverFactory("/vmplacementSolverConfig.xml");
        configureSolver(solverFactory.getSolverConfig(), vmPlacementConfig);
        return solverFactory.buildSolver();
    }

    private static void configureSolver(SolverConfig solverConfig, VmPlacementConfig vmPlacementConfig) {
        configurePolicy(solverConfig, vmPlacementConfig);
        configureTimeout(solverConfig, vmPlacementConfig);
        configureConstructionHeuristic(solverConfig, vmPlacementConfig);
        configureLocalSearch(solverConfig, vmPlacementConfig);
    }

    private static void configurePolicy(SolverConfig solverConfig, VmPlacementConfig vmPlacementConfig) {
        solverConfig.getScoreDirectorFactoryConfig().setScoreDefinitionType(
                ScoreDirectorFactoryConfig.ScoreDefinitionType.HARD_SOFT);
        switch(vmPlacementConfig.getPolicy()) {
            case CONSOLIDATION:
                solverConfig.getScoreDirectorFactoryConfig().setSimpleScoreCalculatorClass(
                        ScoreCalculatorConsolidation.class);
                break;
            case DISTRIBUTION:
                solverConfig.getScoreDirectorFactoryConfig().setSimpleScoreCalculatorClass(
                        ScoreCalculatorDistributionStdDev.class);
                break;
            case PRICE:
                if (VmPlacementConfig.priceModel == null) {
                    throw new IllegalArgumentException("The price policy cannot be applied without a pricing model");
                }
                solverConfig.getScoreDirectorFactoryConfig().setSimpleScoreCalculatorClass(ScoreCalculatorPrice.class);
                break;
            case ENERGY:
                if (VmPlacementConfig.energyModel == null) {
                    throw new IllegalArgumentException("The energy policy cannot be applied without an energy model");
                }
                solverConfig.getScoreDirectorFactoryConfig().setSimpleScoreCalculatorClass(ScoreCalculatorEnergy.class);
                break;
            case GROUP_BY_APP:
                solverConfig.getScoreDirectorFactoryConfig().setSimpleScoreCalculatorClass(
                        ScoreCalculatorGroupByApp.class);
                break;
            case RANDOM:
                solverConfig.getScoreDirectorFactoryConfig().setSimpleScoreCalculatorClass(ScoreCalculatorRandom.class);
                break;
            default:
                throw new IllegalArgumentException("The selected policy is not supported");
        }
    }

    private static void configureTimeout(SolverConfig solverConfig, VmPlacementConfig vmPlacementConfig) {
        solverConfig.getTerminationConfig().setMaximumSecondsSpend((long) vmPlacementConfig.getTimeLimitSeconds());
    }

    private static void configureConstructionHeuristic(SolverConfig solverConfig, VmPlacementConfig vmPlacementConfig) {
        // If we do not want to use a const. heuristic, remove the default one
        if (vmPlacementConfig.getConstructionHeuristic() == null) {
            solverConfig.getSolverPhaseConfigList().remove(0);
        }

        else {
            ConstructionHeuristicSolverPhaseConfig heuristicConfig =
                    (ConstructionHeuristicSolverPhaseConfig) solverConfig.getSolverPhaseConfigList().toArray()[0];
            switch (vmPlacementConfig.getConstructionHeuristic()) {
                case FIRST_FIT:
                    heuristicConfig.setConstructionHeuristicType(
                            ConstructionHeuristicSolverPhaseConfig.ConstructionHeuristicType.FIRST_FIT);
                    break;
                case FIRST_FIT_DECREASING:
                    heuristicConfig.setConstructionHeuristicType(
                            ConstructionHeuristicSolverPhaseConfig.ConstructionHeuristicType.FIRST_FIT_DECREASING);
                    break;
                case BEST_FIT:
                    heuristicConfig.setConstructionHeuristicType(
                            ConstructionHeuristicSolverPhaseConfig.ConstructionHeuristicType.BEST_FIT);
                    break;
                case BEST_FIT_DECREASING:
                    heuristicConfig.setConstructionHeuristicType(
                            ConstructionHeuristicSolverPhaseConfig.ConstructionHeuristicType.BEST_FIT_DECREASING);
                    break;
                default:
                    throw new IllegalArgumentException("The construction heuristic selected is not supported");
            }
        }
    }

    private static void configureLocalSearch(SolverConfig solverConfig, VmPlacementConfig vmPlacementConfig) {
        // remove the local search alg included by default. It is in position 0 if the const.heuristic
        // was removed, and 1 otherwise
        solverConfig.getSolverPhaseConfigList().remove(solverConfig.getSolverPhaseConfigList().size() - 1);

        // Local search can be null if we are only interested in applying the construction step
        if (vmPlacementConfig.getLocalSearch() != null) {
            LocalSearchSolverPhaseConfig localSearchSolverPhaseConfig = new LocalSearchSolverPhaseConfig();
            localSearchSolverPhaseConfig.setAcceptorConfig(vmPlacementConfig.getLocalSearch().getAcceptorConfig());
            localSearchSolverPhaseConfig.setForagerConfig(vmPlacementConfig.getLocalSearch().getForagerConfig());
            solverConfig.getSolverPhaseConfigList().add(localSearchSolverPhaseConfig);
        }
    }

}
