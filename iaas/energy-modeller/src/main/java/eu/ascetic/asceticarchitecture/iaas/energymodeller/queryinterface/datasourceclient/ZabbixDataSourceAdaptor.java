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

import static eu.ascetic.asceticarchitecture.iaas.energymodeller.queryinterface.datasourceclient.KpiList.BOOT_TIME_KPI_NAME;
import static eu.ascetic.asceticarchitecture.iaas.energymodeller.queryinterface.datasourceclient.KpiList.CPU_COUNT_KPI_NAME;
import static eu.ascetic.asceticarchitecture.iaas.energymodeller.queryinterface.datasourceclient.KpiList.CPU_INTERUPT_KPI_NAME;
import static eu.ascetic.asceticarchitecture.iaas.energymodeller.queryinterface.datasourceclient.KpiList.CPU_IO_WAIT_KPI_NAME;
import static eu.ascetic.asceticarchitecture.iaas.energymodeller.queryinterface.datasourceclient.KpiList.CPU_NICE_KPI_NAME;
import static eu.ascetic.asceticarchitecture.iaas.energymodeller.queryinterface.datasourceclient.KpiList.CPU_SOFT_IRQ_KPI_NAME;
import static eu.ascetic.asceticarchitecture.iaas.energymodeller.queryinterface.datasourceclient.KpiList.CPU_STEAL_KPI_NAME;
import static eu.ascetic.asceticarchitecture.iaas.energymodeller.queryinterface.datasourceclient.KpiList.CPU_SYSTEM_KPI_NAME;
import static eu.ascetic.asceticarchitecture.iaas.energymodeller.queryinterface.datasourceclient.KpiList.CPU_USER_KPI_NAME;
import static eu.ascetic.asceticarchitecture.iaas.energymodeller.queryinterface.datasourceclient.KpiList.DISK_TOTAL_KPI_NAME;
import static eu.ascetic.asceticarchitecture.iaas.energymodeller.queryinterface.datasourceclient.KpiList.ENERGY_KPI_NAME;
import static eu.ascetic.asceticarchitecture.iaas.energymodeller.queryinterface.datasourceclient.KpiList.MEMORY_TOTAL_KPI_NAME;
import static eu.ascetic.asceticarchitecture.iaas.energymodeller.queryinterface.datasourceclient.KpiList.POWER_KPI_NAME;
import eu.ascetic.asceticarchitecture.iaas.energymodeller.queryinterface.datasourceclient.hostvmfilter.NameBeginsFilter;
import eu.ascetic.asceticarchitecture.iaas.energymodeller.queryinterface.datasourceclient.hostvmfilter.ZabbixHostVMFilter;
import eu.ascetic.asceticarchitecture.iaas.energymodeller.types.energyuser.VmDeployed;
import eu.ascetic.asceticarchitecture.iaas.energymodeller.types.usage.CurrentUsageRecord;
import eu.ascetic.asceticarchitecture.iaas.zabbixApi.client.ZabbixClient;
import eu.ascetic.asceticarchitecture.iaas.zabbixApi.datamodel.HistoryItem;
import eu.ascetic.asceticarchitecture.iaas.zabbixApi.datamodel.Host;
import eu.ascetic.asceticarchitecture.iaas.zabbixApi.datamodel.Item;
import eu.ascetic.asceticarchitecture.iaas.zabbixApi.utils.Dictionary;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * The aim of this class is initially to take data from the Zabbix Client and to
 * place it into a format that is suitable for the Energy modeller.
 *
 * @author Richard
 */
public class ZabbixDataSourceAdaptor implements HostDataSource {

    private ZabbixClient client = new ZabbixClient();
    private ZabbixHostVMFilter hostFilter = new NameBeginsFilter();

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
        HostMeasurement measure = adaptor.getHostData(adaptor.getHostByName("asok10"));
        System.out.println(measure);
        System.out.println("----------------");
        for (eu.ascetic.asceticarchitecture.iaas.energymodeller.types.energyuser.Host host : hosts) {
            System.out.println("Host: " + host.getHostName() + " Watts In Use: " + adaptor.getLowestHostPowerUsage(host));
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
     * This returns a host given its unique name.
     *
     * @param name The name of the host to get.
     * @return The object representation of a host in the energy modeller.
     */
    @Override
    public VmDeployed getVmByName(String name) {
        Host host = client.getHostByName(name);
        return convertToVm(host, client.getItemsFromHost(name));
    }

    /**
     * This provides a list of hosts for the energy modeller
     *
     * @return A list of hosts for the energy modeller.
     */
    @Override
    public List<eu.ascetic.asceticarchitecture.iaas.energymodeller.types.energyuser.Host> getHostList() {
        List<Host> hostsList = client.getAllHosts();
        ArrayList<eu.ascetic.asceticarchitecture.iaas.energymodeller.types.energyuser.Host> hosts = new ArrayList<>();
        for (Host h : hostsList) {
            if (hostFilter.isHost(h)) {
                hosts.add(convert(h));
            }
        }
        return hosts;
    }

    /**
     * This provides a list of VMs for the energy modeller
     *
     * @return A list of vms for the energy modeller.
     */
    @Override
    public List<VmDeployed> getVmList() {
        List<Host> hostsList = client.getAllHosts();
        ArrayList<VmDeployed> vms = new ArrayList<>();
        for (Host h : hostsList) {
            if (!hostFilter.isHost(h)) {
                vms.add(convertToVm(h, client.getItemsFromHost(h.getHost())));
            }
        }
        return vms;
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
        answer.setAvailable("1".equals(host.getAvailable()));
        Item memory = client.getItemByKeyFromHost(MEMORY_TOTAL_KPI_NAME, hostname);
        Item disk = client.getItemByKeyFromHost(DISK_TOTAL_KPI_NAME, hostname);
        if (memory != null) {
            //Original value given in bytes. 1024 * 1024 = 1048576
            answer.setRamMb((int) (Double.valueOf(memory.getLastValue()) / 1048576));
        }
        if (disk != null) {
            //Original value given in bytes. 1024 * 1024 * 1024 = 1073741824
            answer.setDiskGb((Double.valueOf(disk.getLastValue()) / 1073741824));
        }
        return answer;
    }

    /**
     * This converts a monitoring infrastructure host into a Energy Modeller
     * host.
     *
     * @param host The host to convert
     * @param items The data for a given vm.
     * @return The converted host.
     */
    private VmDeployed convertToVm(Host host, List<Item> items) {
        String hostname = host.getHost();
        int hostId = Integer.parseInt(host.getHostid());
        VmDeployed answer = new VmDeployed(hostId, hostname);
        for (Item item : items) {
            if (item.getKey().equals(MEMORY_TOTAL_KPI_NAME)) { //Convert to Mb
                //Original value given in bytes. 1024 * 1024 = 1048576
                answer.setRamMb((int) (Double.valueOf(item.getLastValue()) / 1048576));
            }
            if (item.getKey().equals(DISK_TOTAL_KPI_NAME)) { //covert to Gb
                //Original value given in bytes. 1024 * 1024 * 1024 = 1073741824
                answer.setDiskGb((Double.valueOf(item.getLastValue()) / 1073741824));
            }
            if (item.getKey().equals(BOOT_TIME_KPI_NAME)) {
                Calendar cal = new GregorianCalendar();
                //This converts from milliseconds into the correct time value
                cal.setTimeInMillis(TimeUnit.SECONDS.toMillis(Long.valueOf(item.getLastValue())));
                answer.setCreated(cal);
            }
            if (item.getKey().equals(CPU_COUNT_KPI_NAME)) {
                answer.setCpus(Integer.valueOf(item.getLastValue()));
            }
            //TODO set the information correctly below!
            answer.setIpAddress("127.0.0.1");
            answer.setState("Work in Progress");

        }
        //A fall back incase the information is not available!
        if (answer.getCpus() == 0) {
            answer.setCpus(Integer.valueOf(1));
        }
        return answer;
    }

    /**
     * This lists for all host all the metric data on them.
     *
     * @return A list of host measurements
     */
    @Override
    public List<HostMeasurement> getHostData() {
        return getHostData(getHostList());
    }

    /**
     * This provides for the named host all the information that is available.
     *
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
     * @return A list of host measurements
     */
    @Override
    public List<HostMeasurement> getHostData(List<eu.ascetic.asceticarchitecture.iaas.energymodeller.types.energyuser.Host> hostList) {
        HashMap<Integer, HostMeasurement> hostMeasurements = new HashMap<>();
        for (eu.ascetic.asceticarchitecture.iaas.energymodeller.types.energyuser.Host host : hostList) {
            hostMeasurements.put(host.getId(), new HostMeasurement(host));
        }

        List<Item> itemsList = client.getAllItems();
        for (Item item : itemsList) {
            Integer hostID = Integer.parseInt(item.getHostid());
            HostMeasurement hostMeasurement = hostMeasurements.get(hostID);
            /**
             * Note: Additional hosts could be discovered using the following
             * code: host = new HostMeasurement(new
             * eu.ascetic.asceticarchitecture.iaas.energymodeller.types.energyuser.Host(hostID,
             * "UNKNOWN")); hostMeasurements.put(hostID, host);
             *
             * This is the case if the host id in the metric does not match any
             * of the named hosts.
             */
            if (hostMeasurement != null) {
                if (item.getLastClock() > hostMeasurement.getClock()) {
                    /**
                     * Ensures the clock value is the latest value seen. It
                     * represents the most upto date piece of data for a given
                     * host.
                     */
                    hostMeasurement.setClock(item.getLastClock());
                }
                hostMeasurement.addMetric(convert(item));
            }
        }
        return new ArrayList<>(hostMeasurements.values());
    }

    /**
     * This lists for all vms all the metric data on them.
     *
     * @return A list of vm measurements
     */
    @Override
    public List<VmMeasurement> getVmData() {
        return getVmData(getVmList());
    }

    /**
     * This provides for the named vm all the information that is available.
     *
     * @param vm The vm to get the measurement data for.
     * @return The vm measurement data
     */
    @Override
    public VmMeasurement getVmData(VmDeployed vm) {
        ArrayList<VmDeployed> vmList = new ArrayList<>();
        vmList.add(vm);
        List<VmMeasurement> measurement = getVmData(vmList);
        if (!measurement.isEmpty()) {
            return measurement.get(0);
        }
        return null;
    }

    /**
     * This takes a list of vms and provides all the metric data on them.
     *
     * @param vmList The list of vms to get the data from
     * @return A list of vm measurements
     */
    @Override
    public List<VmMeasurement> getVmData(List<VmDeployed> vmList) {
        HashMap<Integer, VmMeasurement> vmMeasurements = new HashMap<>();
        for (VmDeployed vm : vmList) {
            vmMeasurements.put(vm.getId(), new VmMeasurement(vm));
        }

        List<Item> itemsList = client.getAllItems();
        for (Item item : itemsList) {
            Integer hostID = Integer.parseInt(item.getHostid());
            VmMeasurement vmMeasurement = vmMeasurements.get(hostID);
            /**
             * Note: Additional hosts could be discovered using the following
             * code: host = new HostMeasurement(new
             * eu.ascetic.asceticarchitecture.iaas.energymodeller.types.energyuser.Host(hostID,
             * "UNKNOWN")); hostMeasurements.put(hostID, host);
             *
             * This is the case if the host id in the metric does not match any
             * of the named hosts.
             */
            if (vmMeasurement != null) {
                if (item.getKey().equals(ENERGY_KPI_NAME)) {
                    /**
                     * Ensures the clock value closely follows the energy,
                     * measurement.
                     */
                    vmMeasurement.setClock(item.getLastClock());
                }
                vmMeasurement.addMetric(convert(item));
            }
        }
        return new ArrayList<>(vmMeasurements.values());
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

    /**
     * This provides the current energy usage for a named host.
     *
     * @param host The host to get the current energy data for.
     * @return The current energy usage data of the named host.
     */
    @Override
    public CurrentUsageRecord getCurrentEnergyUsage(eu.ascetic.asceticarchitecture.iaas.energymodeller.types.energyuser.Host host) {
        CurrentUsageRecord answer = new CurrentUsageRecord(host);
        HostMeasurement measurement = getHostData(host);
        answer.setPower(measurement.getMetric(POWER_KPI_NAME).getValue());
        answer.setVoltage(-1);
        answer.setCurrent(-1);
        return answer;
    }

    /**
     * This finds the lowest/resting power usage by a host.
     *
     * @param host The host to get the lowest power usage data for.
     * @return The lowest i.e. resting power usage of a host
     */
    @Override
    public double getLowestHostPowerUsage(eu.ascetic.asceticarchitecture.iaas.energymodeller.types.energyuser.Host host) {
        //This returns the last 200 items and finds the lowest energy value possible.
        List<HistoryItem> energyData = client.getHistoryDataFromItem(POWER_KPI_NAME, host.getHostName(), Dictionary.HISTORY_ITEM_FORMAT_FLOAT, 200);
        double lowestValue = Double.MAX_VALUE;
        for (HistoryItem historyItem : energyData) {
            double current = Double.parseDouble(historyItem.getValue());
            if (current < lowestValue) {
                lowestValue = current;
            }
        }
        return lowestValue;
    }

    /**
     * This finds the highest power usage by a host.
     *
     * @param host The host to get the highest power usage data for.
     * @return The highest usage of a host
     */
    @Override
    public double getHighestHostPowerUsage(eu.ascetic.asceticarchitecture.iaas.energymodeller.types.energyuser.Host host) {
        //This returns the last 200 items and finds the highest energy value possible.
        List<HistoryItem> energyData = client.getHistoryDataFromItem(POWER_KPI_NAME, host.getHostName(), Dictionary.HISTORY_ITEM_FORMAT_FLOAT, 200);
        double highestValue = Double.MIN_VALUE;
        for (HistoryItem historyItem : energyData) {
            double current = Double.parseDouble(historyItem.getValue());
            if (current > highestValue) {
                highestValue = current;
            }
        }
        return highestValue;
    }

    /**
     * This finds the cpu utilisation of a host, over the last n minutes.
     * @param host The host to get the cpu utilisation data for.
     * @param lastNSeconds The amount of minutes to get the data for
     * @return The average utilisation of the host.
     */
    @Override
    public double getCpuUtilisation(eu.ascetic.asceticarchitecture.iaas.energymodeller.types.energyuser.Host host, int lastNSeconds) {
        long currentTime = new GregorianCalendar().getTimeInMillis();
        long timeInPast = currentTime - TimeUnit.SECONDS.toMillis(lastNSeconds);
        List<HistoryItem> interruptData = client.getHistoryDataFromItem(CPU_INTERUPT_KPI_NAME, host.getHostName(), Dictionary.HISTORY_ITEM_FORMAT_FLOAT, timeInPast, currentTime);
        List<HistoryItem> iowaitData = client.getHistoryDataFromItem(CPU_IO_WAIT_KPI_NAME, host.getHostName(), Dictionary.HISTORY_ITEM_FORMAT_FLOAT, timeInPast, currentTime);
        List<HistoryItem> niceData = client.getHistoryDataFromItem(CPU_NICE_KPI_NAME, host.getHostName(), Dictionary.HISTORY_ITEM_FORMAT_FLOAT, timeInPast, currentTime);
        List<HistoryItem> softirqData = client.getHistoryDataFromItem(CPU_SOFT_IRQ_KPI_NAME, host.getHostName(), Dictionary.HISTORY_ITEM_FORMAT_FLOAT, timeInPast, currentTime);
        List<HistoryItem> stealData = client.getHistoryDataFromItem(CPU_STEAL_KPI_NAME, host.getHostName(), Dictionary.HISTORY_ITEM_FORMAT_FLOAT, timeInPast, currentTime);
        List<HistoryItem> systemData = client.getHistoryDataFromItem(CPU_SYSTEM_KPI_NAME, host.getHostName(), Dictionary.HISTORY_ITEM_FORMAT_FLOAT, timeInPast, currentTime);
        List<HistoryItem> userData = client.getHistoryDataFromItem(CPU_USER_KPI_NAME, host.getHostName(), Dictionary.HISTORY_ITEM_FORMAT_FLOAT, timeInPast, currentTime);
        double interrupt = sumArray(interruptData) / ((double) interruptData.size());
        double iowait = sumArray(iowaitData) / ((double) iowaitData.size());
        double nice = sumArray(niceData) / ((double) niceData.size());
        double softirq = sumArray(softirqData) / ((double) softirqData.size());
        double steal = sumArray(stealData) / ((double) stealData.size());
        double system = sumArray(systemData) / ((double) systemData.size());
        double user = sumArray(userData) / ((double) userData.size());
        return (system + user + interrupt + iowait + nice + softirq + steal) / 100;
    }

    /**
     * This function sums a list of numbers for historic data.
     *
     * @param list The ArrayList to calculate the values of.
     * @return The sum of the values.
     */
    private double sumArray(List<HistoryItem> list) {
        double answer = 0.0;
        for (HistoryItem current : list) {
            answer = answer + Double.valueOf(current.getValue());
        }
        return answer;
    }

    /**
     * @return the hostFilter
     */
    public ZabbixHostVMFilter getHostFilter() {
        return hostFilter;
    }

    /**
     * @param hostFilter the hostFilter to set
     */
    public void setHostFilter(ZabbixHostVMFilter hostFilter) {
        this.hostFilter = hostFilter;
    }
    
    /**
     * This converts a Zabbix Item into the universal Metric Value, that is used
     * by the Energy modeller.
     * @param item
     * @return 
     */
    private MetricValue convert(Item item) {
        MetricValue answer = new MetricValue(item.getName(), item.getKey(), item.getLastValue(), item.getLastClock());
        return answer;
    }
}
