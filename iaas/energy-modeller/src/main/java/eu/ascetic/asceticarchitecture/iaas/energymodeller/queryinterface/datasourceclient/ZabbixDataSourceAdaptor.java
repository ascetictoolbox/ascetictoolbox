/**
 * Copyright 2014 University of Leeds
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
package eu.ascetic.asceticarchitecture.iaas.energymodeller.queryinterface.datasourceclient;

import eu.ascetic.monitoring.api.client.ZabbixClient;
import eu.ascetic.monitoring.api.datamodel.Host;
import eu.ascetic.monitoring.api.datamodel.Item;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * The aim of this class is initially to take data from the Zabbix Client and to
 * place it into a format that is suitable for the Energy modeller.
 *
 * @author Richard
 */
public class ZabbixDataSourceAdaptor implements HostDataSource {

    private ZabbixClient client = new ZabbixClient();

    /**
     * The main method.
     *
     * @deprecated For testing purposes only
     * @param args the arguments
     */
    public static void main(String[] args) {

        ZabbixDataSourceAdaptor adaptor = new ZabbixDataSourceAdaptor();
        List<eu.ascetic.asceticarchitecture.iaas.energymodeller.types.energyuser.Host> hosts = adaptor.getHostList();
        for (eu.ascetic.asceticarchitecture.iaas.energymodeller.types.energyuser.Host host : hosts) {
            System.out.println(host);
        }
        System.out.println("----------------");
        for (HostMeasurement measurement : adaptor.getHostData(hosts)) {
            System.out.println(measurement);
        }
        System.out.println("----------------");
        HostMeasurement measure = adaptor.getHostData(adaptor.getHostByName("asok10.cit.tu-berlin.de"));
        System.out.println(measure);
    }

    /**
     * @deprecated This is the old method copied across
     * @param client
     */
    public static void getHostInformation(ZabbixClient client) {
        List<Item> itemsList = client.getItemsFromHost("asok10.cit.tu-berlin.de");
        int index = 0;
        for (Item i : itemsList) {
            System.out.println("ITEM " + index + ":");
            System.out.println("name: " + i.getName());
            System.out.println("key: " + i.getKey());
            System.out.println("hostid: " + i.getHostid());
            System.out.println("delay: " + i.getDelay());
            System.out.println("history: " + i.getHistory());
            System.out.println("lastvalue: " + i.getLastValue());
            System.out.println("lastclock: " + i.getLastClock());
            index++;
            System.out.println();
        }
    }

    /**
     * This returns a host given its unique name.
     *
     * @param hostname The name of the host to get.
     * @return The object representation of a host in the energy modeller.
     */
    @Override
    public eu.ascetic.asceticarchitecture.iaas.energymodeller.types.energyuser.Host getHostByName(String hostname) {
        Host host = client.getHostByName(hostname);
        return convert(host);
    }

    /**
     * This provides a list of hosts for the energy modeller
     *
     * @param client The client to get the host list from
     * @return A list of hosts for the energy modeller.
     */
    @Override
    public List<eu.ascetic.asceticarchitecture.iaas.energymodeller.types.energyuser.Host> getHostList() {
        List<Host> hostsList = client.getAllHosts();
        ArrayList<eu.ascetic.asceticarchitecture.iaas.energymodeller.types.energyuser.Host> hosts = new ArrayList<>();
        for (Host h : hostsList) {
            hosts.add(convert(h));
        }
        return hosts;
    }

    /**
     * This converts a monitoring infrastructure host into a Energy Modeller
     * host.
     *
     * @param host The host to convert
     * @return The converted host.
     */
    private eu.ascetic.asceticarchitecture.iaas.energymodeller.types.energyuser.Host convert(Host host) {
        String hostname = host.getHost();
        int hostId = Integer.parseInt(host.getHostid());
        eu.ascetic.asceticarchitecture.iaas.energymodeller.types.energyuser.Host answer = new eu.ascetic.asceticarchitecture.iaas.energymodeller.types.energyuser.Host(hostId, hostname);
        answer.setAvailable((host.getAvailable()).equals("0") ? false : true);
        return answer;
    }

    /**
     * This lists for all host all the metric data on them.
     *
     * @param client The client to get the metrics from
     * @return A list of host measurements
     */
    @Override
    public List<HostMeasurement> getHostData() {
        return getHostData(getHostList());
    }

    /**
     * This provides for the named host all the information that is available.
     * @param host The host to get the measurement data for.
     * @return The host measurement data
     */
    @Override
    public HostMeasurement getHostData(eu.ascetic.asceticarchitecture.iaas.energymodeller.types.energyuser.Host host) {
        ArrayList<eu.ascetic.asceticarchitecture.iaas.energymodeller.types.energyuser.Host> hostList = new ArrayList<>();
        hostList.add(host);
        List<HostMeasurement> measurement = getHostData(hostList);
        if (!measurement.isEmpty()) {
            return measurement.get(0);
        }
        return null;
    }

    /**
     * This takes a list of hosts and provides all the metric data on them.
     *
     * @param hostList The list of hosts to get the data from
     * @param client The client to get the metrics from
     * @return A list of host measurements
     */
    @Override
    public List<HostMeasurement> getHostData(List<eu.ascetic.asceticarchitecture.iaas.energymodeller.types.energyuser.Host> hostList) {
        HashMap<Integer, HostMeasurement> hostMeasurements = new HashMap<>();
        for (eu.ascetic.asceticarchitecture.iaas.energymodeller.types.energyuser.Host host : hostList) {
            hostMeasurements.put(host.getId(), new HostMeasurement(host));
        }

        List<Item> itemsList = client.getAllItems();
        for (Item i : itemsList) {
            Integer hostID = Integer.parseInt(i.getHostid());
            HostMeasurement host = hostMeasurements.get(hostID);
            /**
             * Note: Additional hosts could be discovered using the following
             * code: host = new HostMeasurement(new
             * eu.ascetic.asceticarchitecture.iaas.energymodeller.types.energyuser.Host(hostID,
             * "UNKNOWN")); hostMeasurements.put(hostID, host);
             *
             * This is the case if the host id in the metric does not match any
             * of the named hosts.
             */
            if (host != null) {
                host.setClock(i.getLastClock());
                host.addMetric(i);
            }

        }
        return new ArrayList<>(hostMeasurements.values());
    }

    /**
     * @deprecated This is the old method copied across
     * @param client
     */
    public static void getAllHostInformation(ZabbixClient client) {
        List<Item> itemsList = client.getAllItems();
        int index = 0;
        for (Item i : itemsList) {
            System.out.println("ITEM " + index + ":");
            System.out.println("name: " + i.getName());
            System.out.println("key: " + i.getKey());
            System.out.println("hostid: " + i.getHostid());
            System.out.println("delay: " + i.getDelay());
            System.out.println("history: " + i.getHistory());
            System.out.println("lastvalue: " + i.getLastValue());
            System.out.println("lastclock: " + i.getLastClock());
            index++;
            System.out.println();
        }
    }

    /**
     * @deprecated This is the old method copied across
     * @param client
     */
    public static void getAllHosts(ZabbixClient client) {
        List<Host> hostsList = client.getAllHosts();
        int index = 0;
        for (Host h : hostsList) {
            System.out.println("HOST " + index + ":");
            System.out.println("host: " + h.getHost());
            System.out.println("host id: " + h.getHostid());
            System.out.println("available: " + h.getAvailable());
            index++;
            System.out.println();
        }
    }

    /**
     * This returns the Zabbix client that is used to get at the data.
     *
     * @return The client used to get the dataset.
     */
    public ZabbixClient getClient() {
        return client;
    }

    /**
     * This sets the Zabbix client that is used to get at the data.
     *
     * @param client the client to use to gather the data.
     */
    public void setClient(ZabbixClient client) {
        this.client = client;
    }
}
