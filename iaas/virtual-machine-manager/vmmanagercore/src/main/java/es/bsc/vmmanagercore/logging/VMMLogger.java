package es.bsc.vmmanagercore.logging;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

/**
 *
 *
 * @author David Ortiz Lopez (david.ortiz@bsc.es)
 */
public class VMMLogger {

    private static Logger logger = LogManager.getLogger(VMMLogger.class);

    public static void logStartOfDeploymentPlansEvaluation() {
        logger.debug("[VMM] ***EVALUATION OF DEPLOYMENT PLANS STARTS***");
    }

    public static void logEndOfDeploymentPlansEvaluation() {
        logger.debug("[VMM] ***EVALUATION OF DEPLOYMENT PLANS ENDS***");
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

    public static void logPredictedAvgPowerOfVmInHost(String vmName, String hostName, double avgPower) {
        logger.debug("[VMM] predicted avg power vm=" + vmName + ", host=" + hostName
                + " is " + avgPower + "W");
    }

    public static void logPredictedAvgPowerForDeploymentPlan(int deploymentPlanNumber, double avgPower) {
        logger.debug("[VMM] predicted avg power deployment plan" + deploymentPlanNumber + ": " + avgPower + "W");
    }
}
