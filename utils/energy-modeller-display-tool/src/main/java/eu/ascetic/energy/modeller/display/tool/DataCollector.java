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
    private HashMap<String, VmDeployed> knownVms = new HashMap<>();
    private final ArrayList<DataAvailableListener> listners = new ArrayList<>();
    private boolean discoverVms = false;

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

    /**
     * This gets a list of the VMs that are currently on a host machine.
     *
     * @param host The host machine to get the VM list for
     * @return The list of VMs on the specified host
     */
    public ArrayList<VmDeployed> getVMsOnHost(Host host) {
        ArrayList<VmDeployed> answer = new ArrayList<>();
        for (VmDeployed vm : knownVms.values()) {
            if (host.equals(vm.getAllocatedTo())) {
                answer.add(vm);
            }
        }
        return answer;
    }

    public void setDiscoverVms(boolean discoverVms) {
        this.discoverVms = discoverVms;
    }

    HashMap<String, List<CurrentUsageRecord>> hostAnswer = new HashMap<>();

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

        while (running) {
            try {
                if (discoverVms) {
                    refreshKnownVMList(datasource.getVmList());
                    discoverVms = false;
                }
                for (Host host : hostList) {
                    GregorianCalendar cal = new GregorianCalendar();
                    long durationSec = TimeUnit.SECONDS.toMillis(60 * 10);
                    cal.setTimeInMillis(cal.getTimeInMillis() - durationSec);
                    TimePeriod period = new TimePeriod(cal, durationSec);
                    List<CurrentUsageRecord> hostTraceData = hostAnswer.get(host.getHostName());
                    ArrayList<VmDeployed> vms = getVMsOnHost(host);
                    Collection<HostVmLoadFraction> vmMeasurements = null;
                    if (!vms.isEmpty()) {
                        vmMeasurements = connector.getHostVmHistoryLoadData(host, period);
                    }
//                    hostTraceData.add(datasource.getCurrentEnergyUsage(host));
                    hostTraceData.clear();
                    List<HostEnergyRecord> uncleanedHost = connector.getHostHistoryData(host, period);
                    LinkedHashMap<HostEnergyRecord, HostVmLoadFraction> hostToVmDataMap = null;
                    if (!vms.isEmpty() && vmMeasurements != null) {
                        hostToVmDataMap = getData(vmMeasurements, uncleanedHost);
                        hostTraceData.addAll(convert(hostToVmDataMap.keySet()));
                        vmMeasurements.clear();
                        vmMeasurements.addAll(hostToVmDataMap.values());
                    } else {
                        hostTraceData.addAll(convert(uncleanedHost));
                    }

                    hostAnswer.put(host.getHostName(), hostTraceData);

                    if (!vms.isEmpty() && vmMeasurements != null && hostToVmDataMap != null) {
//                        if (vmMeasurements.size() != hostTraceData.size()) {
//                            System.out.println("Size mismatch");
//                            System.out.println("VM Size:" + vmMeasurements.size());
//                            System.out.println("Host Size:" + hostTraceData.size());
//                            System.out.println("Host:" + host.getHostName());
//                            //System.exit(0);
//                        } else {
//                            System.out.println("Things are running ok");
//                        }
                        LoadFractionShareRule rule = new LoadFractionShareRule();
                        for (Map.Entry<HostEnergyRecord, HostVmLoadFraction> vmData : hostToVmDataMap.entrySet()) {
                            ArrayList<VM> vmsArr = new ArrayList<>();
                            vmsArr.addAll(vmData.getValue().getVMs());
                            ArrayList<HostVmLoadFraction> loadFractionData = new ArrayList<>();
                            loadFractionData.add(vmData.getValue());
                            rule.setFractions(loadFractionData.get(0).getFraction());
                            EnergyDivision division = rule.getEnergyUsage(vmData.getKey().getHost(), vmsArr);

                            for (VmDeployed vm : vmData.getValue().getVMs()) { //vmData.getValue().getVMs() or vms??
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
                }
            }
        }
    }

    public CurrentUsageRecord getCurrentEnergyForVM(Host host, VmDeployed vm, ArrayList<VmDeployed> otherVms, CurrentUsageRecord hostAnswer) {
        LoadFractionShareRule rule = new LoadFractionShareRule();
        ArrayList<VmDeployed> vmsDeployedOnHost = new ArrayList<>();
        ArrayList<VM> vmsOnHost = new ArrayList<>();
        vmsDeployedOnHost.addAll(otherVms);
        vmsDeployedOnHost.add(vm);
        vmsOnHost.addAll(otherVms);
        vmsOnHost.add(vm);
        rule.setVmMeasurements(datasource.getVmData(vmsDeployedOnHost));
        EnergyDivision divider = rule.getEnergyUsage(host, vmsOnHost);
        divider.setConsiderIdleEnergy(false);
        CurrentUsageRecord answer = new CurrentUsageRecord(vm);
        answer.setTime(hostAnswer.getTime());
        answer.setPower(divider.getEnergyUsage(hostAnswer.getPower(), vm));
        return answer;
    }

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
     * The hashmap gives a faster way to find a specific vm. This converts from
     * a raw list of vms into the indexed structure.
     *
     * @param vmList The vm list
     * @return The hashed vm list
     */
    private HashMap<String, VmDeployed> toHashMapVm(List<VmDeployed> vmList) {
        HashMap<String, VmDeployed> answer = new HashMap<>();
        for (VmDeployed vm : vmList) {
            answer.put(vm.getName(), vm);
        }
        return answer;
    }

    /**
     * This sets and refreshes the knownVMs list in the data gatherer.
     *
     * @param vmList The list of VMs gained from the data source.
     */
    private void refreshKnownVMList(List<VmDeployed> vmList) {
        //Perform a refresh to make sure the host has been written to backing store
        if (knownVms == null) {
            knownVms = toHashMapVm(vmList);
            connector.setVms(vmList);
        } else {
            List<VmDeployed> newVms = discoverNewVMs(vmList);
            connector.setVms(newVms);
            for (VmDeployed vm : newVms) {
                vm.setAllocatedTo(getVMsHost(vm));
                knownVms.put(vm.getName(), vm);
            }
        }
    }

    /**
     * This compares a list of vms that has been found to the known list of vms.
     *
     * @param newList The new list of vms.
     * @return The list of vms that were otherwise unknown to the data gatherer.
     */
    private List<VmDeployed> discoverNewVMs(List<VmDeployed> newList) {
        List<VmDeployed> answer = new ArrayList<>();
        for (VmDeployed vm : newList) {
            if (!knownVms.containsKey(vm.getName())) {
                answer.add(vm);
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