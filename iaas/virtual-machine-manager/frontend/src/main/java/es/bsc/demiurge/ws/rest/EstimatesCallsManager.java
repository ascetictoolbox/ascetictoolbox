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
import es.bsc.demiurge.core.cloudmiddleware.CloudMiddlewareException;
import es.bsc.demiurge.core.configuration.Config;
import es.bsc.demiurge.core.models.vms.ListVmsToBeEstimated;

/**
 * This class implements the REST calls that are related with the pricing and energy estimates.
 *
 * @author Mario Macias (github.com/mariomac), David Ortiz Lopez (david.ortiz@bsc.es)
 */
public class EstimatesCallsManager {

    private Gson gson = new Gson();

    /**
     * Returns the price and energy estimates for a set of VMs.
     *
     * @param vms the JSON document that contains the descriptions of the VMs
     * @return the JSON document that contains the price and energy estimates
     */
    public String getEstimates(String vms) throws CloudMiddlewareException {
        ListVmsToBeEstimated listVmsToBeEstimated = gson.fromJson(vms, ListVmsToBeEstimated.class);
        return Config.INSTANCE.getVmManager().getVmEstimates(listVmsToBeEstimated.getVms()).toJSON();
    }

}
