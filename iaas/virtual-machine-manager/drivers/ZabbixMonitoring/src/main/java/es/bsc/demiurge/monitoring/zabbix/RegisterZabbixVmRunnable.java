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

package es.bsc.demiurge.monitoring.zabbix;

/**
 * Runnable to register a VM in Zabbix. A client who makes a deployment request should not wait
 * for Zabbix to register a VM. This is why we execute the register action in a separated thread.
 *
 * @author Mario Macias (github.com/mariomac), David Ortiz Lopez (david.ortiz@bsc.es)
 */
public class RegisterZabbixVmRunnable implements Runnable {

    private final String vmId;
    private final String hostname;
    private final String ipAddress;

    public RegisterZabbixVmRunnable(String vmId, String hostname, String ipAddress) {
        this.vmId = vmId;
        this.hostname = hostname;
        this.ipAddress = ipAddress;
    }

    @Override
    public void run() {
        ZabbixConnector.getZabbixClient().createVM(vmId, ipAddress);
    }

}
