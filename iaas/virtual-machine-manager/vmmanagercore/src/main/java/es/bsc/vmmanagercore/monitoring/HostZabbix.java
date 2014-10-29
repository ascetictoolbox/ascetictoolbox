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

import eu.ascetic.asceticarchitecture.iaas.zabbixApi.client.ZabbixClient;
import eu.ascetic.asceticarchitecture.iaas.zabbixApi.datamodel.Item;

import java.util.ArrayList;
import java.util.List;

/**
 * Status of a host monitored by Zabbix.
 *
 * @author David Ortiz Lopez (david.ortiz@bsc.es)
 *
 */
public class HostZabbix extends Host {

    // Keys to identify each metric in Zabbix.
    // Note: The metrics used for the disk space are specific for the Ascetic project.
    private static final String NUMBER_OF_CPUS_KEY = "system.cpu.num";
    private static final String SYSTEM_CPU_LOAD_KEY = "system.cpu.load[all,avg1]";
    private static final String TOTAL_MEMORY_BYTES_KEY = "vm.memory.size[total]";
    private static final String AVAILABLE_MEMORY_BYTES_KEY = "vm.memory.size[available]";
    private static final String TOTAL_DISK_BYTES_KEY = "vfs.fs.size[/var/lib/nova/instances,total]";
    private static final String USED_DISK_BYTES_KEY = "vfs.fs.size[/var/lib/nova/instances,used]";
    private static final String POWER_KEY = "power";

    private final static ZabbixClient zabbixClient = ZabbixConnector.getZabbixClient();
    private List<Item> items = new ArrayList<>(); // Metrics available in the host

    public HostZabbix(String hostname) {
        super(hostname);
        items = zabbixClient.getItemsFromHost(hostname);
        initTotalResources();
        initAssignedResources();
        currentPower = Float.parseFloat(getItemByKey(POWER_KEY).getLastValue());
    }

    private Item getItemByKey(String key) {
        for (Item item: items) {
            if (item.getKey().equals(key)) {
                return item;
            }
        }
        return null;
    }

    private void initTotalResources() {
        totalCpus = Integer.parseInt(getItemByKey(NUMBER_OF_CPUS_KEY).getLastValue());
        totalMemoryMb = (int) ((Long.parseLong(getItemByKey(TOTAL_MEMORY_BYTES_KEY).getLastValue()))/(1024*1024));
        totalDiskGb = (Long.parseLong(getItemByKey(TOTAL_DISK_BYTES_KEY).getLastValue()))/(1024.0*1024*1024);
    }

    private void initAssignedResources() {
        assignedCpus = getAssignedCpus();
        assignedMemoryMb = getAssignedMemoryMb();
        assignedDiskGb = getAssignedDiskGb();
    }

    @Override
    public double getAssignedCpus() {
        double assignedCpus = Double.parseDouble(getItemByKey(SYSTEM_CPU_LOAD_KEY).getLastValue());
        updateAssignedCpus(assignedCpus);
        return assignedCpus;
    }

    @Override
    public double getAssignedMemoryMb() {
        int availableMemoryMb = (int) (Long.parseLong(getItemByKey(AVAILABLE_MEMORY_BYTES_KEY)
                .getLastValue())/(1024*1024));
        double assignedMemoryMb = totalMemoryMb - availableMemoryMb;
        updateAssignedMemoryMb(assignedMemoryMb);
        return assignedMemoryMb;
    }

    @Override
    public double getAssignedDiskGb() {
        double assignedDiskGb = (Double.parseDouble(getItemByKey(USED_DISK_BYTES_KEY)
                .getLastValue())/(1024.0*1024*1024));
        updateAssignedDiskGb(assignedDiskGb);
        return assignedDiskGb;
    }

    /**
     * @return number of available CPUs of the host
     */
    @Override
    public double getFreeCpus() {
        return totalCpus - getAssignedCpus();
    }

    /**
     * @return available memory of the host (in MB)
     */
    @Override
    public double getFreeMemoryMb() {
        return totalMemoryMb - getAssignedMemoryMb();
    }

    /**
     * @return available disk space of the host (in GB)
     */
    @Override
    public double getFreeDiskGb() {
        return totalDiskGb - getAssignedDiskGb();
    }

    @Override
    public void refreshMonitoringInfo() {
        items = zabbixClient.getItemsFromHost(hostname);
        initTotalResources();
        initAssignedResources();
        currentPower = Float.parseFloat(getItemByKey(POWER_KEY).getLastValue());
    }

}
