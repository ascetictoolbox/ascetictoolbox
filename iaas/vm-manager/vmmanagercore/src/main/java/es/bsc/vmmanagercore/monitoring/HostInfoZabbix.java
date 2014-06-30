package es.bsc.vmmanagercore.monitoring;

import eu.ascetic.monitoring.api.client.ZabbixClient;
import eu.ascetic.monitoring.api.datamodel.Item;

import java.util.ArrayList;
import java.util.List;

/**
 * Status of a host monitored by Zabbix.
 *
 * @author David Ortiz Lopez (david.ortiz@bsc.es)
 *
 */
public class HostInfoZabbix extends HostInfo {

    private final static ZabbixClient zabbixClient = new ZabbixClient();
    private List<Item> hostItems = new ArrayList<>();

    public HostInfoZabbix(String hostname) {
        super(hostname);
        hostItems = zabbixClient.getItemsFromHost(hostname);
        /*System.out.println(hostname);
        for (Item item: hostItems) {
            System.out.println(item.getName());
        }*/
    }

}
