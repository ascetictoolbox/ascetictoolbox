package es.bsc.vmmanagercore.monitoring;

import eu.ascetic.monitoring.api.client.ZabbixClient;
import eu.ascetic.monitoring.api.datamodel.Item;

import java.util.List;

/**
 * Status of a host monitored by Zabbix.
 *
 * @author David Ortiz Lopez (david.ortiz@bsc.es)
 *
 */
public class HostInfoZabbix extends HostInfo {

    ZabbixClient zabbixClient = new ZabbixClient();
    List<Item> hostItems;

    public HostInfoZabbix(String hostname) {
        super(hostname);
        hostItems = zabbixClient.getItemsFromHost(hostname);
    }

}
