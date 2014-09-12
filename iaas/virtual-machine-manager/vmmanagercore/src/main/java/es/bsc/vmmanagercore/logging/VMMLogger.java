package es.bsc.vmmanagercore.logging;

import es.bsc.vmmanagercore.model.DeploymentPlan;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

/**
 * Logs system for the VMM.
 *
 * @author David Ortiz Lopez (david.ortiz@bsc.es)
 */
public class VMMLogger {

    private static Logger logger = LogManager.getLogger(VMMLogger.class);

    public static void logStartOfDeploymentPlansEvaluation(String schedAlgorithmName) {
        logger.debug("[VMM] ***EVALUATION OF DEPLOYMENT PLANS STARTS: " + schedAlgorithmName + " ***");
    }

    public static void logEndOfDeploymentPlansEvaluation(String schedAlgorithmName) {
        logger.debug("[VMM] ***EVALUATION OF DEPLOYMENT PLANS ENDS: " + schedAlgorithmName + " ***");
    }

    public static void logChosenDeploymentPlan(String deploymentPlan) {
        logger.debug("[VMM] chosen deployment plan: " + deploymentPlan);
    }

    public static void logOverbookingNeeded() {
        logger.debug("[VMM] no plans could be applied without overbooking.");
    }

    public static void logDeploymentPlanRandomScore(DeploymentPlan deploymentPlan, double randomScore) {
        logger.debug("[VMM] random score for deployment plan [ " + deploymentPlan.toString() + "]: " + randomScore);
    }

    public static void logPredictedAvgPowerForDeploymentPlan(DeploymentPlan deploymentPlan, double avgPower) {
        logger.debug("[VMM] predicted avg power deployment plan [ " + deploymentPlan.toString()
                + "]: " + avgPower + "W");
    }

    public static void logPredictedCostForDeploymentPlan(DeploymentPlan deploymentPlan, double cost) {
        logger.debug("[VMM] predicted cost for deployment plan [ " + deploymentPlan.toString()
                + "]: " + cost + " euros");
    }

    public static void logVmsSameAppInSameHost(DeploymentPlan deploymentPlan, int numberOfVmsSameAppSameHost) {
        logger.debug("[VMM] number of VMs of same application in the same host for deployment plan [ " +
                deploymentPlan.toString() + "]: " + numberOfVmsSameAppSameHost);
    }

    public static void logServersLoadsAfterDeploymentPlan(DeploymentPlan deploymentPlan, int idleServers,
            double stdDevCpu, double stdDevRam, double stdDevDisk) {
        logger.debug("[VMM] Server loads for deployment plan [ " + deploymentPlan.toString() + "]:\n idle servers:"
                + idleServers + ", stdDevCpu:" + stdDevCpu + ", stdDevRam: " + stdDevRam + ", stdDevDisk: "
                + stdDevDisk);
    }

    public static void logUnusedServerLoadsAfterDeploymentPlan(DeploymentPlan deploymentPlan, int idleServers,
            double unusedCpu, double unusedRam, double unusedDisk) {
        logger.debug("[VMM] Total unused loads for deployment plan [ " + deploymentPlan.toString()
                + "]:\n idle servers:" + idleServers + ", unusedCpu:" + unusedCpu + ", unusedRam: "
                + unusedRam + ", unusedDisk: " + unusedDisk);
    }

}
