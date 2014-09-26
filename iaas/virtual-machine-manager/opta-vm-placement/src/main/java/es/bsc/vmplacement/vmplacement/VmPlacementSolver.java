package es.bsc.vmplacement.vmplacement;

import es.bsc.vmplacement.scorecalculators.*;
import es.bsc.vmplacement.vmplacement.config.VmPlacementConfig;
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
                        ScoreCalculatorDistribution.class);
                break;
            case PRICE:
                solverConfig.getScoreDirectorFactoryConfig().setSimpleScoreCalculatorClass(ScoreCalculatorPrice.class);
                break;
            case ENERGY:
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
        ConstructionHeuristicSolverPhaseConfig heuristicConfig =
                (ConstructionHeuristicSolverPhaseConfig) solverConfig.getSolverPhaseConfigList().toArray()[0];
        switch(vmPlacementConfig.getConstructionHeuristic()) {
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

    private static void configureLocalSearch(SolverConfig solverConfig, VmPlacementConfig vmPlacementConfig) {
        LocalSearchSolverPhaseConfig localSearchSolverPhaseConfig = new LocalSearchSolverPhaseConfig();
        localSearchSolverPhaseConfig.setAcceptorConfig(vmPlacementConfig.getLocalSearch().getAcceptorConfig());
        localSearchSolverPhaseConfig.setForagerConfig(vmPlacementConfig.getLocalSearch().getForagerConfig());
        solverConfig.getSolverPhaseConfigList().remove(1);
        solverConfig.getSolverPhaseConfigList().add(localSearchSolverPhaseConfig);
    }

}
