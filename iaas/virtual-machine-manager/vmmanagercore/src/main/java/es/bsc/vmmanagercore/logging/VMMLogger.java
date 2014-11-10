/**
 Copyright (C) 2013-2014  Barcelona Supercomputing Center

 This library is free software; you can redistribute it and/or
 modify it under the terms of the GNU Lesser General Public
 License as published by the Free Software Foundation; either
 version 2.1 of the License, or (at your option) any later version.

 This library is distributed in the hope that it will be useful,
 but WITHOUT ANY WARRANTY; without even the implied warranty of
 MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 Lesser General Public License for more details.

 You should have received a copy of the GNU Lesser General Public
 License along with this library; if not, write to the Free Software
 Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301  USA
 */

package es.bsc.vmmanagercore.logging;

import es.bsc.vmmanagercore.model.scheduling.DeploymentPlan;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

/**
 * Logs system for the VMM.
 *
 * @author David Ortiz Lopez (david.ortiz@bsc.es)
 */
public class VMMLogger {

    private static Logger logger = LogManager.getLogger(VMMLogger.class);

    public static void logStartOfDeploymentPlansEvaluation(String schedAlgorithmName, String deploymentId) {
        logger.debug("[VMM] ***EVALUATION OF DEPLOYMENT PLANS STARTS: " + schedAlgorithmName + " ***"
                + " --id:" + deploymentId);
    }

    public static void logEndOfDeploymentPlansEvaluation(String schedAlgorithmName, String deploymentId) {
        logger.debug("[VMM] ***EVALUATION OF DEPLOYMENT PLANS ENDS: " + schedAlgorithmName + " ***"
                + " --id:" + deploymentId);
    }

    public static void logChosenDeploymentPlan(String deploymentPlan, String deploymentId) {
        logger.debug("[VMM] chosen deployment plan: [ " + deploymentPlan + "] --id:" + deploymentId);
    }

    public static void logOverbookingNeeded(String deploymentId) {
        logger.debug("[VMM] no plans could be applied without overbooking. --id:" + deploymentId);
    }

    public static void logDeploymentPlanRandomScore(DeploymentPlan deploymentPlan, double randomScore,
            String deploymentId) {
        logger.debug("[VMM] random score for deployment plan [ " + deploymentPlan.toString() + "]: " + randomScore
                + " --id:" + deploymentId);
    }

    public static void logPredictedAvgPowerForDeploymentPlan(DeploymentPlan deploymentPlan, double avgPower,
            String deploymentId) {
        logger.debug("[VMM] predicted avg power deployment plan [ " + deploymentPlan.toString()
                + "]: " + avgPower + "W --id:" + deploymentId);
    }

    public static void logPredictedCostForDeploymentPlan(DeploymentPlan deploymentPlan, double cost,
            String deploymentId) {
        logger.debug("[VMM] predicted cost for deployment plan [ " + deploymentPlan.toString()
                + "]: " + cost + " euros --id:" + deploymentId);
    }

    public static void logVmsSameAppInSameHost(DeploymentPlan deploymentPlan, int numberOfVmsSameAppSameHost,
            String deploymentId) {
        logger.debug("[VMM] number of VMs of same application in the same host for deployment plan [ " +
                deploymentPlan.toString() + "]: " + numberOfVmsSameAppSameHost + " --id:" + deploymentId);
    }

    public static void logServersLoadsAfterDeploymentPlan(DeploymentPlan deploymentPlan, int idleServers,
            double stdDevCpu, double stdDevRam, double stdDevDisk, String deploymentId) {
        logger.debug("[VMM] Server loads for deployment plan [ " + deploymentPlan.toString() + "]: idle servers:"
                + idleServers + ", stdDevCpu:" + stdDevCpu + ", stdDevRam: " + stdDevRam + ", stdDevDisk: "
                + stdDevDisk + " --id:" + deploymentId);
    }

    public static void logUnusedServerLoadsAfterDeploymentPlan(DeploymentPlan deploymentPlan, int idleServers,
            double unusedCpu, double unusedRam, double unusedDisk, String deploymentId) {
        logger.debug("[VMM] Total unused loads for deployment plan [ " + deploymentPlan.toString()
                + "]: idle servers:" + idleServers + ", unusedCpu:" + unusedCpu + ", unusedRam: "
                + unusedRam + ", unusedDisk: " + unusedDisk + " --id:" + deploymentId);
    }

    public static void logMigration(String vmId, String hostname) {
        logger.debug("[VMM] Requested to migrate VM with ID=" + vmId + " to host " + hostname);
    }

}
