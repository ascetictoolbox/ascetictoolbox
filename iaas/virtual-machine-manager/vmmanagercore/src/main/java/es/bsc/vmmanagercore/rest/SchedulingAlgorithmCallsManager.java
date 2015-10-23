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
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import es.bsc.vmmanagercore.manager.VmManager;
import es.bsc.vmmanagercore.models.scheduling.SchedAlgorithmNameEnum;

import javax.ws.rs.WebApplicationException;

/**
 * This class implements the REST calls that are related with scheduling algorithms.
 *
 * @author David Ortiz Lopez (david.ortiz@bsc.es)
 */
public class SchedulingAlgorithmCallsManager {

    private Gson gson = new Gson();
    private VmManager vmManager;

    /**
     * Class constructor.
     *
     * @param vmManager the VM manager
     */
    public SchedulingAlgorithmCallsManager(VmManager vmManager) {
        this.vmManager = vmManager;
    }

    /**
     * Returns a JSON document that contains the scheduling algorithms supported.
     *
     * @return the JSON document
     */
    public String getSchedulingAlgorithms() {
        JsonArray schedAlgsArrayJson = new JsonArray();
        for (SchedAlgorithmNameEnum schedAlg: vmManager.getAvailableSchedulingAlgorithms()) {
            JsonObject schedAlgJson = new JsonObject();
            schedAlgJson.addProperty("name", schedAlg.getName());
            schedAlgsArrayJson.add(schedAlgJson);
        }
        JsonObject result = new JsonObject();
        result.add("scheduling_algorithms", schedAlgsArrayJson);
        return result.toString();
    }

    /**
     * Returns a JSON document that contains the scheduling algorithms being used now.
     *
     * @return the JSON document
     */
    public String getCurrentSchedulingAlgorithm() {
        JsonObject result = new JsonObject();
        result.addProperty("name", vmManager.getCurrentSchedulingAlgorithm().getName());
        return result.toString();
    }

    /**
     * Sets the scheduling algorithm.
     *
     * @param schedAlgToSet the scheduling algorithm
     */
    public void setSchedulingAlgorithm(String schedAlgToSet) {
        // Get the algorithm
        JsonObject jsonObject = gson.fromJson(schedAlgToSet, JsonObject.class);
        if (jsonObject.get("algorithm") == null) { // Invalid JSON format. Throw error.
            throw new WebApplicationException(400);
        }
        String algorithm = jsonObject.get("algorithm").getAsString();

        SchedAlgorithmNameEnum schedulingAlg;
        if (algorithm.equals(SchedAlgorithmNameEnum.CONSOLIDATION.getName())) {
            schedulingAlg = SchedAlgorithmNameEnum.CONSOLIDATION;
        }
        else if (algorithm.equals(SchedAlgorithmNameEnum.COST_AWARE.getName())) {
            schedulingAlg = SchedAlgorithmNameEnum.COST_AWARE;
        }
        else if (algorithm.equals(SchedAlgorithmNameEnum.DISTRIBUTION.getName())) {
            schedulingAlg = SchedAlgorithmNameEnum.DISTRIBUTION;
        }
        else if (algorithm.equals(SchedAlgorithmNameEnum.ENERGY_AWARE.getName())) {
            schedulingAlg = SchedAlgorithmNameEnum.ENERGY_AWARE;
        }
        else if (algorithm.equals(SchedAlgorithmNameEnum.GROUP_BY_APP.getName())) {
            schedulingAlg = SchedAlgorithmNameEnum.GROUP_BY_APP;
        }
        else if (algorithm.equals(SchedAlgorithmNameEnum.RANDOM.getName())) {
            schedulingAlg = SchedAlgorithmNameEnum.RANDOM;
        }
        else { // Invalid algorithm. Throw error.
            throw new WebApplicationException(400);
        }
        vmManager.setSchedulingAlgorithm(schedulingAlg);
    }


}
