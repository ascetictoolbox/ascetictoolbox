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
package eu.ascetic.asceticarchitecture.iaas.energymodeller.datastore;

import eu.ascetic.asceticarchitecture.iaas.energymodeller.calibration.Calibrator;
import eu.ascetic.asceticarchitecture.iaas.energymodeller.queryinterface.datasourceclient.HostDataSource;
import eu.ascetic.asceticarchitecture.iaas.energymodeller.queryinterface.datasourceclient.HostMeasurement;
import eu.ascetic.asceticarchitecture.iaas.energymodeller.queryinterface.datasourceclient.VmMeasurement;
import eu.ascetic.asceticarchitecture.iaas.energymodeller.types.energyuser.Host;
import eu.ascetic.asceticarchitecture.iaas.energymodeller.types.energyuser.VmDeployed;
import eu.ascetic.asceticarchitecture.iaas.energymodeller.types.energyuser.usage.HostVmLoadFraction;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * The data gatherer takes data from the data source and writes them into the
 * database for further usage.
 *
 * @author Richard
 */
public class DataGatherer implements Runnable {

    private final HostDataSource datasource;
    private final DatabaseConnector connector;
    private final Calibrator calibrator;
    private boolean running = true;
    private int faultCount = 0;
    private HashMap<String, Host> knownHosts = new HashMap<>();
    private HashMap<String, VmDeployed> knownVms = new HashMap<>();
    private final HashMap<Host, Long> lastTimeStampSeen = new HashMap<>();

    /**
     * This creates a data gather component for the energy modeller.
     *
     * @param datasource The data source that provides information about the
     * host resources and the virtual machines running on them.
     * @param connector The database connector used to do this. It is best to
     * give this component its own database connection as it will make heavy use
     * of it.
     * @param calibrator The calibrator to call in the event a new host is
     * detected.
     */
    public DataGatherer(HostDataSource datasource, DatabaseConnector connector, Calibrator calibrator) {
        this.datasource = datasource;
        this.connector = connector;
        this.calibrator = calibrator;
        populateHostList();
    }

    /**
     * This populates the list of hosts and their VMs that are known to the
     * energy modeller.
     */
    private void populateHostList() {
        Collection<Host> hosts = datasource.getHostList();
        connector.setHosts(hosts);
        //Ensure calibration data is recovered from the db.
        for (Host host : hosts) {
            if (!host.isCalibrated()) {
                host.setCalibrationData(connector.getHostCalibrationData(host).getCalibrationData());
            }
            if (!host.isCalibrated()) {
                calibrator.calibrateHostEnergyData(host);
            }
        }
        Collection<VmDeployed> vms = datasource.getVmList();
        connector.setVms(vms);
        for (Host host : hosts) {
            if (!knownHosts.containsKey(host.getHostName())) {
                knownHosts.put(host.getHostName(), host);
            }
        }
        for (VmDeployed vm : vms) {
            if (!knownVms.containsKey(vm.getName())) {
                vm.setAllocatedTo(getVMsHost(vm));
                knownVms.put(vm.getName(), vm);
            }
        }
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
     * This stops the data gatherer from running.
     */
    public void stop() {
        running = false;
        connector.closeConnection();
    }

    @Override
    public void run() {
        /**
         * Polls the data source and write values to the database. TODO consider
         * buffering the db writes.
         */
        while (running) {
            try {
                List<Host> hostList = datasource.getHostList();
                refreshKnownHostList(hostList);
                refreshKnownVMList(datasource.getVmList());
                for (Host host : hostList) {
                    HostMeasurement measurement = datasource.getHostData(host);
                    /**
                     * Update only if a value has not been provided before or
                     * the timestamp value has changed. This keeps the data
                     * written to backing store as clean as possible.
                     */
                    if (lastTimeStampSeen.get(host) == null || measurement.getClock() > lastTimeStampSeen.get(host)) {
                        lastTimeStampSeen.put(host, measurement.getClock());
                        connector.writeHostHistoricData(host, measurement.getClock(), measurement.getPower(), measurement.getEnergy());
                    }
                    ArrayList<VmDeployed> vms = getVMsOnHost(host);
                    if (!vms.isEmpty()) {
                        HostVmLoadFraction fraction = new HostVmLoadFraction(host, measurement.getClock());
                        List<VmMeasurement> vmMeasurements = datasource.getVmData(vms);
                        fraction.setFraction(vmMeasurements);
                        connector.writeHostVMHistoricData(host, measurement.getClock(), fraction);
                    }
                }
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException ex) {
                    Logger.getLogger(DataGatherer.class.getName()).log(Level.SEVERE, "The data gatherer was interupted.", ex);
                }
                faultCount = (faultCount > 0 ? faultCount - 1 : 0);
            } catch (Exception e) { //This should always try to gather data from the data source.
                faultCount = faultCount + 1;
                if (faultCount > 25) {
                    stop(); //Exit if faults keep occuring in a sequence.
                }
            }
        }
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
                calibrator.calibrateHostEnergyData(host);
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
     * This provides the list of known hosts
     *
     * @return The list of known hosts
     */
    public HashMap<String, Host> getHostList() {
        return knownHosts;
    }

    /**
     * This gets the named host from the known host list.
     *
     * @param hostname The name of the host
     * @return The host that has the name specified.
     */
    public Host getHost(String hostname) {
        return knownHosts.get(hostname);
    }

    /**
     * This provides the list of known Vms
     *
     * @return The list of known Vms
     */
    public HashMap<String, VmDeployed> getVmList() {
        return knownVms;
    }

    /**
     * This gets the named VM from the known VM list.
     *
     * @param name The name of the VM
     * @return The VM that has the name specified.
     */
    public VmDeployed getVm(String name) {
        return knownVms.get(name);
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

}
