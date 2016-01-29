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

package es.bsc.vmm.ascetic.scheduler.legacy;

import es.bsc.vmm.ascetic.modellers.energy.EnergyModeller;
import es.bsc.vmm.ascetic.modellers.energy.ascetic.AsceticEnergyModellerAdapter;
import es.bsc.demiurge.core.manager.components.EstimatesManager;
import es.bsc.demiurge.core.models.scheduling.DeploymentPlan;
import es.bsc.demiurge.core.models.scheduling.VmAssignmentToHost;
import es.bsc.demiurge.core.models.vms.VmDeployed;
import es.bsc.demiurge.core.monitoring.hosts.Host;
import es.bsc.demiurge.core.scheduler.schedulingalgorithms.SchedAlgorithm;
import org.apache.log4j.LogManager;


import java.util.List;

/**
 * Energy-aware scheduling algorithm.
 * This scheduling algorithm chooses the host where the energy consumed will be the lowest.
 * This decision is taken according to the predictions performed by the Energy Modeller.
 *
 * @author Mario Macias (github.com/mariomac), David Ortiz Lopez (david.ortiz@bsc.es)
 *
 */
@Deprecated
public class SchedAlgEnergyAware implements SchedAlgorithm {



    private double getPredictedAvgPowerDeploymentPlan(EnergyModeller energyModeller, List<VmDeployed> vmsDeployed, DeploymentPlan deploymentPlan) {

        double result = 0;
        for (VmAssignmentToHost vmAssignmentToHost: deploymentPlan.getVmsAssignationsToHosts()) {
            double predictedAvgPower = energyModeller.getPredictedAvgPowerVm(vmAssignmentToHost.getVm(),
                    vmAssignmentToHost.getHost(), vmsDeployed, deploymentPlan);
            result += predictedAvgPower;
        }
        return result;
    }

    @Override
    public DeploymentPlan chooseBestDeploymentPlan(List<VmDeployed> vmsDeployed, EstimatesManager estimatorsManager, List<DeploymentPlan> deploymentPlans, List<Host> hosts,
            String deploymentId) {

		AsceticEnergyModellerAdapter energyModeller = ((AsceticEnergyModellerAdapter)estimatorsManager.get(AsceticEnergyModellerAdapter.class));
        DeploymentPlan bestDeploymentPlan = null;

        double avgPowerBestDeploymentPlan = Double.MAX_VALUE;
        for (DeploymentPlan deploymentPlan: deploymentPlans) {
            double predictedAvgPower = getPredictedAvgPowerDeploymentPlan(energyModeller, vmsDeployed, deploymentPlan);
			LogManager.getLogger(SchedAlgEnergyAware.class).debug("[VMM] predicted avg power deployment plan [ " + deploymentPlan.toString()
                    + "]: " + predictedAvgPower + "W --id:" + deploymentId);
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

    @Override
    public String getName() {
        return "energyAware";
    }

}
