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

package es.bsc.vmmanagercore.scheduler.schedulingalgorithms;

import es.bsc.vmmanagercore.energymodeller.EnergyModellerConnector;
import es.bsc.vmmanagercore.logging.VMMLogger;
import es.bsc.vmmanagercore.model.DeploymentPlan;
import es.bsc.vmmanagercore.model.VmAssignmentToHost;
import es.bsc.vmmanagercore.model.VmDeployed;
import es.bsc.vmmanagercore.monitoring.Host;

import java.util.ArrayList;
import java.util.List;

/**
 * Energy-aware scheduling algorithm.
 * This scheduling algorithm chooses the host where the energy consumed will be the lowest.
 * This decision is taken according to the predictions performed by the Energy Modeller.
 *
 * @author David Ortiz Lopez (david.ortiz@bsc.es)
 *
 */
public class SchedAlgEnergyAware implements SchedAlgorithm {

    List<VmDeployed> vmsDeployed = new ArrayList<>();

    public SchedAlgEnergyAware(List<VmDeployed> vmsDeployed) {
        this.vmsDeployed = vmsDeployed;
    }

    private double getPredictedAvgPowerDeploymentPlan(DeploymentPlan deploymentPlan) {
        double result = 0;
        for (VmAssignmentToHost vmAssignmentToHost: deploymentPlan.getVmsAssignationsToHosts()) {
            double predictedAvgPower = EnergyModellerConnector.getPredictedAvgPowerVm(vmAssignmentToHost.getVm(),
                    vmAssignmentToHost.getHost(), vmsDeployed);
            result += predictedAvgPower;
        }
        return result;
    }

    @Override
    public DeploymentPlan chooseBestDeploymentPlan(List<DeploymentPlan> deploymentPlans, List<Host> hosts) {
        DeploymentPlan bestDeploymentPlan = null;
        double avgPowerBestDeploymentPlan = Double.MAX_VALUE;
        for (DeploymentPlan deploymentPlan: deploymentPlans) {
            double predictedAvgPower = getPredictedAvgPowerDeploymentPlan(deploymentPlan);
            VMMLogger.logPredictedAvgPowerForDeploymentPlan(deploymentPlan, predictedAvgPower);
            if (predictedAvgPower < avgPowerBestDeploymentPlan) {
                bestDeploymentPlan = deploymentPlan;
                avgPowerBestDeploymentPlan = predictedAvgPower;
            }
            // If the score is the same, choose randomly
            else if (predictedAvgPower == avgPowerBestDeploymentPlan) {
                if (Math.random() > 0.5) {
                    bestDeploymentPlan = deploymentPlan;
                }
            }
        }
        return bestDeploymentPlan;
    }

}
