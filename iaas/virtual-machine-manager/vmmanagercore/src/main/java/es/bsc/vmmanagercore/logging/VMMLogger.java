package es.bsc.vmmanagercore.logging;

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

    public static void logStartOfDeploymentPlanComparison(String deploymentPlan1, String deploymentPlan2) {
        logger.debug("[VMM] --Comparison of deployment plans starts--");
        logger.debug("[VMM] Deployment plan 1: " + deploymentPlan1);
        logger.debug("[VMM] Deployment plan 2: " + deploymentPlan2);
    }

    public static void logEndOfDeploymentPlanComparison() {
        logger.debug("[VMM] --Comparison of deployment plans ends--");
    }

    public static void logChosenDeploymentPlan(String deploymentPlan) {
        logger.debug("[VMM] chosen deployment plan: " + deploymentPlan);
    }

    public static void logOverbookingNeeded() {
        logger.debug("[VMM] no plans could be applied without overbooking.");
    }

    public static void logPredictedAvgPowerOfVmInHost(String vmName, String hostName, double avgPower) {
        logger.debug("[VMM] predicted avg power vm=" + vmName + ", host=" + hostName + " is " + avgPower + "W");
    }

    public static void logPredictedAvgPowerForDeploymentPlan(int deploymentPlanNumber, double avgPower) {
        logger.debug("[VMM] predicted avg power deployment plan " + deploymentPlanNumber + ": " + avgPower + "W");
    }

    public static void logPredictedCostOfVmInHost(String vmName, String hostName, double cost) {
        logger.debug("[VMM] predicted cost of vm=" + vmName + ", host=" + hostName + " is " + cost + " euros");
    }

    public static void logPredictedCostForDeploymentPlan(int deploymentPlanNumber, double cost) {
        logger.debug("[VMM] predicted cost for deployment plan " + deploymentPlanNumber + ": " + cost + " euros");
    }

    public static void logVmsSameAppInSameHost(int deploymentPlanNumber, int numberOfVmsSameAppSameHost) {
        logger.debug("[VMM] number of VMs of same application in the same host for deployment plan " +
                deploymentPlanNumber + ": " + numberOfVmsSameAppSameHost);
    }

    public static void logServersLoadsAfterDeploymentPlan(int deploymentPlanNumber, int idleServers, double stdDevCpu,
            double stdDevRam, double stdDevDisk) {
        logger.debug("[VMM] Server loads for deployment plan " + deploymentPlanNumber + " idle servers:" + idleServers
                + ", stdDevCpu:" + stdDevCpu + ", stdDevRam: " + stdDevRam + ", stdDevDisk: " + stdDevDisk);
    }

    public static void logUnusedServerLoadsAfterDeploymentPlan(int deploymentPlanNumber, int idleServers,
            double unusedCpu, double unusedRam, double unusedDisk) {
        logger.debug("[VMM] Total unused loads for deployment plan " + deploymentPlanNumber + " idle servers:"
                + idleServers + ", unusedCpu:" + unusedCpu + ", unusedRam: " + unusedRam + ", unusedDisk: "
                + unusedDisk);
    }

}
