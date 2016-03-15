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

package es.bsc.demiurge.ws.rest;

import com.google.gson.Gson;
import es.bsc.demiurge.core.models.scheduling.RecommendedPlanRequest;
import es.bsc.demiurge.core.models.scheduling.VmPlacement;
import es.bsc.demiurge.core.models.vms.Vm;
import es.bsc.demiurge.core.cloudmiddleware.CloudMiddlewareException;
import es.bsc.demiurge.core.manager.VmManager;

import java.util.ArrayList;

/**
 * This class implements the REST calls that are related with the placement of VMs.
 *
 * @author Mario Macias (github.com/mariomac), David Ortiz Lopez (david.ortiz@bsc.es)
 */
public class VmPlacementCallsManager {

    private VmManager vmManager;
    private Gson gson = new Gson();

    public VmPlacementCallsManager(VmManager vmManager) {
        this.vmManager = vmManager;
    }

    public String getConstructionHeuristics() {
        return gson.toJson(vmManager.getConstructionHeuristics());
    }

    public String getLocalSearchAlgorithms() {
        return gson.toJson(vmManager.getLocalSearchAlgorithms());
    }

    public String getRecommendedPlan(String recommendedPlanRequest) throws CloudMiddlewareException {
        return gson.toJson(vmManager.getRecommendedPlan(gson.fromJson(recommendedPlanRequest,
                RecommendedPlanRequest.class), false, new ArrayList<Vm>()));
    }

    public void executeDeploymentPlan(String deploymentPlan) throws CloudMiddlewareException {
        vmManager.executeDeploymentPlan(gson.fromJson(deploymentPlan, VmPlacement[].class));
    }

}
