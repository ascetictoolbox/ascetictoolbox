package es.bsc.vmmanagercore.monitoring;

import eu.ascetic.asceticarchitecture.iaas.zabbixApi.client.ZabbixClient;

/**
 *
 * @author David Ortiz Lopez (david.ortiz@bsc.es)
 */
public class ZabbixConnector {

    private final static ZabbixClient zabbixClient = new ZabbixClient();

    public static ZabbixClient getZabbixClient() {
        return zabbixClient;
    }

}
