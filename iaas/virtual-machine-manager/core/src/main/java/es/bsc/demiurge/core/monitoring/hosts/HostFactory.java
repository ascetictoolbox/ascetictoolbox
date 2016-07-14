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

package es.bsc.demiurge.core.monitoring.hosts;

import com.google.gson.Gson;
import es.bsc.demiurge.core.cloudmiddleware.CloudMiddleware;
import es.bsc.demiurge.core.drivers.Monitoring;

import java.util.HashMap;
import java.util.Map;

/**
 * Host factory
 *
 * @author Mario Macias (github.com/mariomac), David Ortiz Lopez (david.ortiz@bsc.es)
 */
public class HostFactory {
    private Map<String, Host> hosts = new HashMap<>(); // List of hosts already created
	private CloudMiddleware cloudMiddleware = null;
	private Monitoring<Host> monitoring = null;
    
    /**
     * 
     * @param cloudMiddleware the cloud middleware information about hosts [mandatory].
     * @param monitoring the monitoring information about hosts [optional] - can be set to null.
     */
	public HostFactory(CloudMiddleware cloudMiddleware, Monitoring monitoring) {
		this.cloudMiddleware = cloudMiddleware;
		this.monitoring = monitoring;
	}

	/**
     * Returns a host given a hostname, a type of host, and the openStackJclouds connector
     *
     * @param hostname the hostname
     * @return the host
     */
    public Host getHost(String hostname) {
        Host host = (Host) hosts.get(hostname);
        if (host != null) {
            host.refreshMonitoringInfo();
            return host;
        }
        
        Host newHost = (monitoring != null) ? 
            monitoring.createHost(hostname) : cloudMiddleware.createHost(hostname);
		
        hosts.put(hostname, newHost);
        return newHost;
    }
}