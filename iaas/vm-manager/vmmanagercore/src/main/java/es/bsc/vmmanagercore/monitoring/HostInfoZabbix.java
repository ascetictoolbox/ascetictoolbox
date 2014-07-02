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
public class HostInfoZabbix extends HostInfo {

    // Keys to identify each metric in Zabbix.
    // Note: The metric used for the disk space is specific for Ascetic.
    private static final String NUMBER_OF_CPUS_KEY = "system.cpu.num";
    private static final String TOTAL_MEMORY_BYTES_KEY = "vm.memory.size[total]";
    private static final String TOTAL_DISK_BYTES_KEY = "vfs.fs.size[/var/lib/nova/instances,total]";

    private final static ZabbixClient zabbixClient = new ZabbixClient();
    private List<Item> hostItems = new ArrayList<>(); // Metrics available in the host

    private Item getItemByKey(String key) {
        for (Item item: hostItems) {
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

    public HostInfoZabbix(String hostname) {
        super(hostname);
        hostItems = zabbixClient.getItemsFromHost(hostname);
        initTotalResources();
    }

    @Override
    public double getAssignedCpus() {

        return 0;
    }

    @Override
    public int getAssignedMemoryMb() {

        return 0;
    }

    @Override
    public double getAssignedDiskGb() {

        return 0;
    }

    /**
     * @return number of available CPUs of the host
     */
    @Override
    public double getFreeCpus() {
        return totalCpus - getAssignedCpus() - reservedCpus;
    }

    /**
     * @return available memory of the host (in MB)
     */
    @Override
    public int getFreeMemoryMb() {
        return totalMemoryMb - getAssignedMemoryMb() - reservedMemoryMb;
    }

    /**
     * @return available disk space of the host (in GB)
     */
    @Override
    public double getFreeDiskGb() {
        return totalDiskGb - getAssignedDiskGb() - reservedDiskGb;
    }

}
