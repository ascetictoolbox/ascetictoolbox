/**
 * Copyright 2015 University of Leeds
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

import eu.ascetic.asceticarchitecture.iaas.energymodeller.queryinterface.datasourceclient.HostDataSource;
import eu.ascetic.asceticarchitecture.iaas.energymodeller.queryinterface.datasourceclient.HostMeasurement;
import static eu.ascetic.asceticarchitecture.iaas.energymodeller.queryinterface.datasourceclient.KpiList.POWER_KPI_NAME;
import eu.ascetic.asceticarchitecture.iaas.energymodeller.queryinterface.datasourceclient.VmMeasurement;
import eu.ascetic.asceticarchitecture.iaas.energymodeller.types.energyuser.EnergyUsageSource;
import eu.ascetic.asceticarchitecture.iaas.energymodeller.types.energyuser.Host;
import eu.ascetic.asceticarchitecture.iaas.energymodeller.types.energyuser.VmDeployed;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * This data collector displays energy data for the IaaS Energy Modeller.
 *
 * @author Richard Kavanagh
 */
public class CounterDataCollector implements Runnable {

    private final HostDataSource datasource;
    private boolean running = true;
    private int faultCount = 0;
    private HashMap<String, EnergyUsageSource> knownEnergyUsers = new HashMap<>();
    private EnergyCounter listner = null;
    private final HashMap<String, CounterData> counters = new HashMap<>();
    private boolean count = true;

    /**
     * This creates a new data collector for the energy modeller display tool.
     *
     * @param datasource The data source to use.
     */
    public CounterDataCollector(HostDataSource datasource) {
        this.datasource = datasource;
    }

    /**
     * This gets the list of know hosts
     *
     * @return
     */
    private ArrayList<Host> getHostList() {
        ArrayList<Host> answer = new ArrayList<>();
        for (EnergyUsageSource current : knownEnergyUsers.values()) {
            if (isHost(current)) {
                answer.add((Host) current);
            }
        }
        return answer;
    }

    /**
     * This gets the list of know VMs
     *
     * @return
     */
    private ArrayList<VmDeployed> getVMList() {
        ArrayList<VmDeployed> answer = new ArrayList<>();
        for (EnergyUsageSource current : knownEnergyUsers.values()) {
            if (!isHost(current)) {
                answer.add((VmDeployed) current);
            }
        }
        return answer;
    }

    /**
     * Indicates if this counter data is for a physical host or not
     *
     * @param source
     * @return
     */
    public boolean isHost(EnergyUsageSource source) {
        return source.getClass().equals(Host.class);
    }

    @Override
    public void run() {
        List<EnergyUsageSource> energyUsers = datasource.getHostAndVmList();
        refreshKnownEnergyUsersList(energyUsers);
        for (EnergyUsageSource host : energyUsers) {
            CounterData data = new CounterData(host);
            counters.put(data.getName(), data);
        }

        while (running) {
            try {
                for (EnergyUsageSource energyUser : energyUsers) {
                    CounterData energyUserCounter = counters.get(getName(energyUser));
                    if (energyUserCounter == null) {
                        energyUserCounter = new CounterData(energyUser);
                        counters.put(getName(energyUser), energyUserCounter);
                    }
                }
                if (count) {
                    List<VmMeasurement> vmMeasurements = datasource.getVmData(getVMList());
                    List<HostMeasurement> measurements = datasource.getHostData(getHostList());
                    adjustHostCounters(measurements);
                    adjustVmCounters(vmMeasurements);
                }
                ArrayList<CounterData> data = new ArrayList<>(counters.values());
                listner.processDataAvailable(data);
                try {
                    Thread.sleep(2000);
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

    /**
     * This takes a collection of current measurements and appends them onto the
     * counters running score.
     *
     * @param data The data to onto the counter
     */
    public void adjustVmCounters(Collection<VmMeasurement> data) {
        for (VmMeasurement current : data) {
            if (current.metricExists(POWER_KPI_NAME)) {            
            CounterData counter = counters.get(current.getVm().getName());
                counter.add(current.getClock(), current.getMetric(POWER_KPI_NAME).getValue());
            }
        }
    }

    /**
     * This takes a collection of current measurements and appends them onto the
     * counters running score.
     *
     * @param data The data to onto the counter
     */
    public void adjustHostCounters(Collection<HostMeasurement> data) {
        for (HostMeasurement current : data) {
            if (current.metricExists(POWER_KPI_NAME)) {
                CounterData counter = counters.get(current.getHost().getHostName());
                counter.add(current.getClock(), current.getPower());
            }
        }
    }

    /**
     * This provides from a raw list of energy users a hashmap of energy users.
     *
     * @param hostList The host list
     * @return The hashed host list
     */
    private HashMap<String, EnergyUsageSource> toHashMap(List<EnergyUsageSource> hostList) {
        HashMap<String, EnergyUsageSource> answer = new HashMap<>();
        for (EnergyUsageSource host : hostList) {
            answer.put(getName(host), host);
        }
        return answer;
    }

    /**
     * This sets and refreshes the knownHosts list in the data gatherer.
     *
     * @param energyUsers The list of host gained from the data source.
     */
    private void refreshKnownEnergyUsersList(List<EnergyUsageSource> energyUsers) {
        //Perform a refresh to make sure the host has been written to backing store
        if (knownEnergyUsers == null) {
            knownEnergyUsers = toHashMap(energyUsers);
        } else {
            List<EnergyUsageSource> newHosts = discoverNewEnergyUsers(energyUsers);
            for (EnergyUsageSource host : newHosts) {
                knownEnergyUsers.put(getName(host), host);
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
    private List<EnergyUsageSource> discoverNewEnergyUsers(List<EnergyUsageSource> newList) {
        List<EnergyUsageSource> answer = new ArrayList<>();
        for (EnergyUsageSource host : newList) {
            if (!knownEnergyUsers.containsKey(getName(host))) {
                answer.add(host);
            }
        }
        return answer;
    }

    private String getName(EnergyUsageSource energyUser) {
        if (Host.class.equals(energyUser.getClass())) {
            return ((Host) energyUser).getHostName();
        } else if (VmDeployed.class.equals(energyUser.getClass())) {
            return ((VmDeployed) energyUser).getName();
        }
        return null;
    }

    /**
     * This stops the data gatherer from running.
     */
    public void stop() {
        running = false;
    }

    /**
     * This checks to see if the counter is currently working or not
     *
     * @return the count
     */
    public boolean isCount() {
        return count;
    }

    /**
     * This sets the status of the counter
     *
     * @param count the count to set
     */
    public void setCount(boolean count) {
        this.count = count;
    }

    /**
     * This resets all energy counters
     */
    public void resetCounters() {
        for (CounterData counter : counters.values()) {
            counter.resetCounter();
        }
    }

    /**
     * @param listner the listner to set
     */
    public void setListner(EnergyCounter listner) {
        this.listner = listner;
    }

}
