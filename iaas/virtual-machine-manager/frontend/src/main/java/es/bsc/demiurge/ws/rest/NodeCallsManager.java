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
import es.bsc.demiurge.core.monitoring.hosts.Host;
import es.bsc.demiurge.core.manager.VmManager;

import java.util.List;

/**
 * This class implements the REST calls that are related with the nodes of the infrastructure.
 *
 * @author Mario Macias (github.com/mariomac), David Ortiz Lopez (david.ortiz@bsc.es)
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
        List<Host> hosts = vmManager.getHosts();
        StringBuilder result = new StringBuilder("{\"nodes\":[");
		boolean first = true;
		for(Host h : hosts) {
			if(!first) result.append(",");
			first = false;
			result.append("{\"hostname\":\"").append(h.getHostname())
				    .append("\",\"totalCpus\":").append(h.getTotalCpus())
					.append(",\"totalMemoryMb\":").append(h.getTotalMemoryMb())
					.append(",\"totalDiskGb\":").append(h.getTotalDiskGb())
					.append(",\"assignedCpus\":").append(h.getAssignedCpus())
					.append(",\"assignedMemoryMb\":").append(h.getAssignedMemoryMb())
					.append(",\"assignedDiskGb\":").append(h.getAssignedDiskGb())
					.append(",\"currentPower\":").append(h.getCurrentPower())
					.append(",\"turnedOff\":{\"value\":").append(h.isOn()?1:0)
					.append("}}");

			/*
			{"hostname":"wally158","totalCpus":8,"totalMemoryMb":16022.0,"totalDiskGb":1805.6862564086914,"assignedCpus":0.07,
			"assignedMemoryMb":2499.9921875,"assignedDiskGb":0.06598281860351562,"currentPower":-1.0,"turnedOff":{"value":0},
			"turnOnDelaySeconds":30,"turnOffDelaySeconds":30}
			*/
        }
        return result.append("]}").toString();
    }

    public String getVMsDeployedInNode(String hostname) {
        return gson.toJson(vmManager.getHost(hostname), Host.class);
    }
    
    public void pressHostPowerButton(String hostname) {
        vmManager.pressHostPowerButton(hostname);
    }

}
