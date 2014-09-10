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
package eu.ascetic.energy.modeller.display.tool;

import eu.ascetic.asceticarchitecture.iaas.energymodeller.datastore.DatabaseConnector;
import eu.ascetic.asceticarchitecture.iaas.energymodeller.energypredictor.vmenergyshare.EnergyDivision;
import eu.ascetic.asceticarchitecture.iaas.energymodeller.energypredictor.vmenergyshare.LoadFractionShareRule;
import eu.ascetic.asceticarchitecture.iaas.energymodeller.queryinterface.datasourceclient.HostDataSource;
import eu.ascetic.asceticarchitecture.iaas.energymodeller.types.TimePeriod;
import eu.ascetic.asceticarchitecture.iaas.energymodeller.types.energyuser.EnergyUsageSource;
import eu.ascetic.asceticarchitecture.iaas.energymodeller.types.energyuser.Host;
import eu.ascetic.asceticarchitecture.iaas.energymodeller.types.energyuser.VM;
import eu.ascetic.asceticarchitecture.iaas.energymodeller.types.energyuser.VmDeployed;
import eu.ascetic.asceticarchitecture.iaas.energymodeller.types.energyuser.usage.HostVmLoadFraction;
import eu.ascetic.asceticarchitecture.iaas.energymodeller.types.usage.CurrentUsageRecord;
import eu.ascetic.asceticarchitecture.iaas.energymodeller.types.usage.HostEnergyRecord;
import java.util.ArrayList;
import java.util.Collection;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * This data collector displays energy data for the IaaS Energy Modeller.
 *
 * @author Richard
 */
public class DataCollector implements Runnable {

    private final HostDataSource datasource;
    private final DatabaseConnector connector;
    private boolean running = true;
    private int faultCount = 0;
    private HashMap<String, Host> knownHosts = new HashMap<>(); //TODO add a listner structure here
    private final ArrayList<DataAvailableListener> listners = new ArrayList<>();
    private final HashMap<String, List<CurrentUsageRecord>> hostAnswer = new HashMap<>();

    public DataCollector(HostDataSource datasource, DatabaseConnector connector) {
        this.datasource = datasource;
        this.connector = connector;
    }

    public void registerListener(DataAvailableListener listner) {
        listners.add(listner);
    }

    /**
     * This given a host determines its Host. It is used for the initial
     * assignment of this value.
     *
     * @param vm The deployed vm
     * @return The host that it belongs to.
     */
    private Host getVMsHost(VmDeployed vm) {
        if (vm.getAllocatedTo() != null) {
            return vm.getAllocatedTo();
        }
        //TODO remove this temporary fix code here!
        if (vm.getName().equals("cloudsuite---data-analytics")) {
            return getHost("asok12");
        }
        //end of this temporary code fix
        /**
         * This block of code takes the agreed assumption that the host name
         * ends with "_<hostname>" and that "_" exist nowhere else in the name.
         */
        String name = vm.getName();
        int parseTokenPos = name.indexOf("_");
        if (parseTokenPos == -1 && vm.getAllocatedTo() == null) {
            return null;
        }
        //TODO consider adding a file based map system here.
        return getHost(name.substring(parseTokenPos + 1, name.length()));
    }

    /**
     * This gets the named host from the known host list.
     *
     * @param hostname The name of the host
     * @return The host that has the name specified.
     */
    private Host getHost(String hostname) {
        return knownHosts.get(hostname);
    }

    @Override
    public void run() {
        /**
         * Polls the data source and write values to the database. TODO consider
         * buffering the db writes.
         */
        List<Host> hostList = datasource.getHostList();
        refreshKnownHostList(hostList);
        for (Host host : hostList) {
            hostAnswer.put(host.getHostName(), new ArrayList<CurrentUsageRecord>());
        }

        LoadFractionShareRule rule = new LoadFractionShareRule();

        while (running) {
            try {
                for (Host host : hostList) {
                    GregorianCalendar cal = new GregorianCalendar();
                    long durationSec = TimeUnit.SECONDS.toMillis(60 * 10);
                    cal.setTimeInMillis(cal.getTimeInMillis() - durationSec);
                    TimePeriod period = new TimePeriod(cal, durationSec);
                    List<CurrentUsageRecord> hostTraceData = hostAnswer.get(host.getHostName());
                    Collection<HostVmLoadFraction> vmMeasurements = connector.getHostVmHistoryLoadData(host, period);
                    hostTraceData.clear();
                    List<HostEnergyRecord> uncleanedHost = connector.getHostHistoryData(host, period);
                    hostAnswer.put(host.getHostName(), convert(uncleanedHost));

                    LinkedHashMap<HostEnergyRecord, HostVmLoadFraction> hostToVmDataMap;
                    if (vmMeasurements != null && !vmMeasurements.isEmpty()) {
                        hostToVmDataMap = getData(vmMeasurements, uncleanedHost);
                        hostTraceData.addAll(convert(hostToVmDataMap.keySet()));
                        vmMeasurements.clear();
                        vmMeasurements.addAll(hostToVmDataMap.values());
                        for (Map.Entry<HostEnergyRecord, HostVmLoadFraction> vmData : hostToVmDataMap.entrySet()) {
                            ArrayList<VM> vmsArr = new ArrayList<>();
                            vmsArr.addAll(vmData.getValue().getVMs());
                            ArrayList<HostVmLoadFraction> loadFractionData = new ArrayList<>();
                            loadFractionData.add(vmData.getValue());
                            rule.setFractions(loadFractionData.get(0).getFraction());
                            EnergyDivision division = rule.getEnergyUsage(vmData.getKey().getHost(), vmsArr);
                            division.setConsiderIdleEnergy(false); //TODO parameterise this!!

                            for (VmDeployed vm : vmData.getValue().getVMs()) {
                                if (vm.getAllocatedTo() == null) {
                                    vm.setAllocatedTo(getVMsHost(vm));
                                }
                                List<CurrentUsageRecord> vmAnswer = hostAnswer.get(vm.getName());
                                if (vmAnswer == null) {
                                    vmAnswer = new ArrayList<>();
                                }
                                HashSet<EnergyUsageSource> source = new HashSet<>();
                                source.add(vm);
                                CurrentUsageRecord vmsUsage = new CurrentUsageRecord(source, division.getEnergyUsage(vmData.getKey().getPower(), vm), -1, -1);
                                vmAnswer.add(vmsUsage);
                                hostAnswer.put(vm.getName(), vmAnswer);
                            }
                        }
                    }
                }
                listners.get(0).processDataAvailable(hostAnswer);
                try {
                    Thread.sleep(5000);
                } catch (InterruptedException ex) {
                    Logger.getLogger(DataCollector.class.getName()).log(Level.SEVERE, "The data collector was interupted.", ex);
                }
                faultCount = (faultCount > 0 ? faultCount - 1 : 0);
            } catch (Exception ex) { //This should always try to gather data from the data source.
                Logger.getLogger(DataCollector.class.getName()).log(Level.SEVERE, "The data collector had a problem.", ex);
                faultCount = faultCount + 1;
                if (faultCount > 25) {
                    stop(); //Exit if faults keep occuring in a sequence.
                    System.exit(-1);
                }
            }
        }
    }

    /**
     * This compares the vm resource utilisation dataset and the host energy
     * data and ensures that they have a 1:1 mapping
     *
     * @param vmData The Vms usage dataset
     * @param hostData The host's energy usage dataset
     * @return The mappings between each dataset elements
     */
    public LinkedHashMap<HostEnergyRecord, HostVmLoadFraction> getData(Collection<HostVmLoadFraction> vmData, List<HostEnergyRecord> hostData) {
        LinkedHashMap<HostEnergyRecord, HostVmLoadFraction> answer = new LinkedHashMap<>();
        //Make a copy and compare times remove them each time.
        LinkedList<HostVmLoadFraction> vmDataCopy = new LinkedList<>();
        vmDataCopy.addAll(vmData);
        LinkedList<HostEnergyRecord> hostDataCopy = new LinkedList<>();
        hostDataCopy.addAll(hostData);
        HostVmLoadFraction vmHead = vmDataCopy.pop();
        HostEnergyRecord hostHead = hostDataCopy.pop();
        while (!vmDataCopy.isEmpty() && !hostDataCopy.isEmpty()) {
            if (vmHead.getTime() == hostHead.getTime()) {
                answer.put(hostHead, vmHead);
                vmHead = vmDataCopy.pop();
                hostHead = hostDataCopy.pop();
            } else {
                //replace the youngest, given this is a sorted list.
                if (vmHead.getTime() < hostHead.getTime()) {
                    vmHead = vmDataCopy.pop();
                } else {
                    hostHead = hostDataCopy.pop();
                }
            }
        }
        return answer;
    }

    /**
     * This converts a host energy record into a current usage record dataset
     *
     * @param data The data to convert
     * @return The current usage record dataset
     */
    public ArrayList<CurrentUsageRecord> convert(Collection<HostEnergyRecord> data) {
        ArrayList<CurrentUsageRecord> answer = new ArrayList<>();
        for (HostEnergyRecord current : data) {
            CurrentUsageRecord converted = new CurrentUsageRecord(current.getHost());
            converted.setPower(current.getPower());
            GregorianCalendar cal = new GregorianCalendar();
            cal.setTimeInMillis(TimeUnit.SECONDS.toMillis(current.getTime()));
            converted.setTime(cal);
            answer.add(converted);
        }
        return answer;
    }

    /**
     * The hashmap gives a faster way to find a specific host. This converts
     * from a raw list of hosts into the indexed structure.
     *
     * @param hostList The host list
     * @return The hashed host list
     */
    private HashMap<String, Host> toHashMap(List<Host> hostList) {
        HashMap<String, Host> answer = new HashMap<>();
        for (Host host : hostList) {
            answer.put(host.getHostName(), host);
        }
        return answer;
    }

    /**
     * This sets and refreshes the knownHosts list in the data gatherer.
     *
     * @param hostList The list of host gained from the data source.
     */
    private void refreshKnownHostList(List<Host> hostList) {
        //Perform a refresh to make sure the host has been written to backing store
        if (knownHosts == null) {
            knownHosts = toHashMap(hostList);
            connector.setHosts(hostList);
        } else {
            List<Host> newHosts = discoverNewHosts(hostList);
            connector.setHosts(newHosts);
            for (Host host : newHosts) {
                knownHosts.put(host.getHostName(), host);
            }
        }
    }

    /**
     * This compares a list of hosts that has been found to the known list of
     * hosts.
     *
     * @param newList The new list of hosts.
     * @return The list of hosts that were otherwise unknown to the data
     * gatherer.
     */
    private List<Host> discoverNewHosts(List<Host> newList) {
        List<Host> answer = new ArrayList<>();
        for (Host host : newList) {
            if (!knownHosts.containsKey(host.getHostName())) {
                answer.add(host);
            }
        }
        return answer;
    }

    /**
     * This stops the data gatherer from running.
     */
    public void stop() {
        running = false;
        connector.closeConnection();
    }

}
