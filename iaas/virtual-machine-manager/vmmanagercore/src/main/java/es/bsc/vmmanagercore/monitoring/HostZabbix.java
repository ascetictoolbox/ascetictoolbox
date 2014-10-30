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

import java.util.*;

/**
 * Status of a host monitored by Zabbix.
 *
 * @author David Ortiz Lopez (david.ortiz@bsc.es)
 *
 */
public class HostZabbix extends Host {

    // Keys to identify each metric in Zabbix.
    // Note: The metrics used for the disk space are specific for the Ascetic project. Also, some metrics might not
    // be available by default in Zabbix. I had to add some of them manually.
    private static final String NUMBER_OF_CPUS_KEY = "system.cpu.num";
    private static final String SYSTEM_CPU_LOAD_KEY = "system.cpu.load[all,avg1]";
    private static final String TOTAL_MEMORY_BYTES_KEY = "vm.memory.size[total]";
    private static final String AVAILABLE_MEMORY_BYTES_KEY = "vm.memory.size[available]";
    private static final String TOTAL_DISK_BYTES_KEY = "vfs.fs.size[/var/lib/nova/instances,total]";
    private static final String USED_DISK_BYTES_KEY = "vfs.fs.size[/var/lib/nova/instances,used]";
    private static final String POWER_KEY = "power";

    private final int zabbixId; // Each host has an ID in Zabbix and this ID is not the hostname

    /**
     * Class constructor.
     *
     * @param hostname the hostname
     */
    public HostZabbix(String hostname) {
        super(hostname);
        zabbixId = getZabbixId(hostname);
        updateMetrics();
    }

    /**
     * Returns the Zabbix ID for a specific host given its hostname.
     *
     * @param hostname the hostname
     * @return the Zabbix ID
     */
    private int getZabbixId(String hostname) {
        // I am cheating here. The Zabbix ID should not be hardcoded
        switch(hostname) {
            case "asok09":
                return ZabbixConnector.ASOK09_ID;
            case "asok10":
                return ZabbixConnector.ASOK10_ID;
            case "asok11":
                return ZabbixConnector.ASOK11_ID;
            case "asok12":
                return ZabbixConnector.ASOK12_ID;
            default:
                throw new IllegalArgumentException("Invalid hostname");
        }
    }

    /**
     * This function updates the metrics (assigned cpus, memory, etc.) of the host based on the information that
     * it receives from Zabbix.
     */
    private void updateMetrics() {
        Map<String, Double> latestMetricValues = ZabbixConnector.getHostItems(zabbixId);
        totalCpus = latestMetricValues.get(NUMBER_OF_CPUS_KEY).intValue();
        totalMemoryMb = (int) (latestMetricValues.get(TOTAL_MEMORY_BYTES_KEY)/(1024*1024));
        totalDiskGb = latestMetricValues.get(TOTAL_DISK_BYTES_KEY)/(1024.0*1024*1024);
        assignedCpus = latestMetricValues.get(SYSTEM_CPU_LOAD_KEY);
        assignedMemoryMb = totalMemoryMb - latestMetricValues.get(AVAILABLE_MEMORY_BYTES_KEY)/(1024*1024);
        assignedDiskGb = latestMetricValues.get(USED_DISK_BYTES_KEY)/(1024.0*1024*1024);
        currentPower = latestMetricValues.get(POWER_KEY);
    }

    @Override
    public void refreshMonitoringInfo() {
        updateMetrics();
    }

}
