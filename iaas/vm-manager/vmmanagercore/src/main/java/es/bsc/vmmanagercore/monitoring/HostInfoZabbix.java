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

    ZabbixClient zabbixClient;
    List<Item> hostItems = new ArrayList<>();

    public HostInfoZabbix(String hostname) {
        super(hostname);
        zabbixClient = new ZabbixClient();
        hostItems = zabbixClient.getItemsFromHost(hostname);
    }

}
