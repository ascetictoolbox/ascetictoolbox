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

package es.bsc.vmmanagercore.monitoring;

import es.bsc.vmmanagercore.cloudmiddleware.openstack.OpenStackJclouds;

import java.util.HashMap;
import java.util.Map;

/**
 * Host factory
 *
 * @author David Ortiz Lopez (david.ortiz@bsc.es)
 */
public class HostFactory {

    // Suppress default constructor for non-instantiability
    private HostFactory() {
        throw new AssertionError();
    }

    private static Map<String, Host> hosts = new HashMap<>(); // List of hosts already created

    /**
     * Returns a host given a hostname, a type of host, and the openStackJclouds connector
     *
     * @param hostname the hostname
     * @param type the type of the host
     * @param openStackJclouds the openStackJclouds connector
     * @return the host
     */
    // Note: I should get rid of the OpenStackJclouds dependency here
    public static Host getHost(String hostname, HostType type, OpenStackJclouds openStackJclouds) {
        // If the host already exists, return it
        Host host = hosts.get(hostname);
        if (host != null) {
            if (host instanceof HostOpenStack) {
                ((HostOpenStack) host).setOpenStackJclouds(openStackJclouds);
            }
            host.refreshMonitoringInfo();
            return host;
        }

        // If the host does not already exist, create and return it
        Host newHost = null;
        switch(type) {
            case GANGLIA:
                newHost = new HostGanglia(hostname);
                break;
            case ZABBIX:
                newHost = new HostZabbix(hostname);
                break;
            case OPENSTACK:
                newHost = new HostOpenStack(hostname, openStackJclouds);
                break;
            default:
                break;
        }
        hosts.put(hostname, newHost);
        return newHost;
    }
}
