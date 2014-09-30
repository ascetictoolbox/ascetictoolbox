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
import es.bsc.vmmanagercore.monitoring.Host;

/**
 * This class implements the REST calls that are related with the nodes of the infrastructure.
 *
 * @author David Ortiz Lopez (david.ortiz@bsc.es)
 */
public class NodeCallsManager {

    private Gson gson = new Gson();
    private VmManager vmManager;

    /**
     * Class constructor.
     */
    public NodeCallsManager(VmManager vmManager) {
        this.vmManager = vmManager;
    }

    public String getNodes() {
        // TODO Refactor this ugly hack
        String result = "{\"nodes\":[";
        for (int i = 0; i < vmManager.getHosts().size(); ++i) {
            result = result.concat(gson.toJson(vmManager.getHosts().get(i), Host.class));
            if (i != vmManager.getHosts().size() -1) {
                result = result.concat(",");
            }
        }
        return result.concat("]}");
    }

    public String getVMsDeployedInNode(String hostname) {
        return gson.toJson(vmManager.getHost(hostname), Host.class);
    }

}
