package es.bsc.vmplacement.placement;

import es.bsc.vmplacement.placement.config.VmPlacementConfig;
import es.bsc.vmplacement.scorecalculators.*;
import org.optaplanner.core.api.solver.Solver;
import org.optaplanner.core.api.solver.SolverFactory;
import org.optaplanner.core.config.constructionheuristic.ConstructionHeuristicSolverPhaseConfig;
import org.optaplanner.core.config.localsearch.LocalSearchSolverPhaseConfig;
import org.optaplanner.core.config.solver.SolverConfig;
import org.optaplanner.core.config.solver.XmlSolverFactory;

/**
 * This class creates an instance of an OptaPlanner Solver from an instance of VmPlacementConfig.
 *  
 * @author David Ortiz (david.ortiz@bsc.es)
 */
public class VmPlacementSolver {

    public static Solver buildSolver(VmPlacementConfig vmPlacementConfig) {
        SolverFactory solverFactory = new XmlSolverFactory("/vmplacementSolverConfig.xml");
        configureSolver(solverFactory.getSolverConfig(), vmPlacementConfig);
        return solverFactory.buildSolver();
    }

    /**
     * Modifies the given SolverConfig according to the given placement configuration.
     * This function sets the policy, the timeout, the construction heuristic, and the local search
     * algorithm for the given SolverConfig.
     * 
     * @param solverConfig the instance of SolverConfig
     * @param vmPlacementConfig the configuration for the VM placement problem
     */
    private static void configureSolver(SolverConfig solverConfig, VmPlacementConfig vmPlacementConfig) {
        configurePolicy(solverConfig, vmPlacementConfig);
        configureTimeout(solverConfig, vmPlacementConfig);
        configureConstructionHeuristic(solverConfig, vmPlacementConfig);
        configureLocalSearch(solverConfig, vmPlacementConfig);
    }

    private static void configurePolicy(SolverConfig solverConfig, VmPlacementConfig vmPlacementConfig) {
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
                if (VmPlacementConfig.priceModeller == null) {
                    throw new IllegalArgumentException(
                            "The price policy cannot be applied without a pricing model");
                }
                solverConfig.getScoreDirectorFactoryConfig()
                        .setSimpleScoreCalculatorClass(ScoreCalculatorPrice.class);
                break;
            case ENERGY:
                if (VmPlacementConfig.energyModeller == null) {
                    throw new IllegalArgumentException(
                            "The energy policy cannot be applied without an energy model");
                }
                solverConfig.getScoreDirectorFactoryConfig()
                        .setSimpleScoreCalculatorClass(ScoreCalculatorEnergy.class);
                break;
            case GROUP_BY_APP:
                solverConfig.getScoreDirectorFactoryConfig().setSimpleScoreCalculatorClass(
                        ScoreCalculatorGroupByApp.class);
                break;
            case RANDOM:
                solverConfig.getScoreDirectorFactoryConfig()
                        .setSimpleScoreCalculatorClass(ScoreCalculatorRandom.class);
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
            ConstructionHeuristicSolverPhaseConfig.ConstructionHeuristicType constructionHeuristicType;
            switch (vmPlacementConfig.getConstructionHeuristic()) {
                case FIRST_FIT:
                    constructionHeuristicType = 
                            ConstructionHeuristicSolverPhaseConfig.ConstructionHeuristicType.FIRST_FIT;
                    break;
                case FIRST_FIT_DECREASING:
                    constructionHeuristicType =
                            ConstructionHeuristicSolverPhaseConfig.ConstructionHeuristicType.FIRST_FIT_DECREASING;
                    break;
                case BEST_FIT:
                    constructionHeuristicType = 
                            ConstructionHeuristicSolverPhaseConfig.ConstructionHeuristicType.BEST_FIT;
                    break;
                case BEST_FIT_DECREASING:
                    constructionHeuristicType =
                            ConstructionHeuristicSolverPhaseConfig.ConstructionHeuristicType.BEST_FIT_DECREASING;
                    break;
                default:
                    throw new IllegalArgumentException("The construction heuristic selected is not supported");
            }
            heuristicConfig.setConstructionHeuristicType(constructionHeuristicType);
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
