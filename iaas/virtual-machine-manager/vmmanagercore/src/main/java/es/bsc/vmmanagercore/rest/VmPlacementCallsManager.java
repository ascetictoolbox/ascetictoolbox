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

package es.bsc.vmmanagercore.rest;

import com.google.gson.Gson;
import es.bsc.vmmanagercore.manager.VmManager;
import es.bsc.vmmanagercore.model.scheduling.RecommendedPlanRequest;

/**
 * This class implements the REST calls that are related with the placement of VMs.
 *
 * @author David Ortiz Lopez (david.ortiz@bsc.es)
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

    public String getRecommendedPlan(String recommendedPlanRequest) {
        return gson.toJson(vmManager.getRecommendedPlan(gson.fromJson(recommendedPlanRequest,
                RecommendedPlanRequest.class)));
    }

}
