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
import eu.ascetic.asceticarchitecture.iaas.energymodeller.types.energyuser.Host;
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

    private HostDataSource datasource;
    private DatabaseConnector connector;
    private Calibrator calibrator;
    private boolean running = true;
    private int faultCount = 0;
    private HashMap<String, Host> knownHosts = new HashMap<>();
    private HashMap<Host, Long> lastTimeStampSeen = new HashMap<>();

    /**
     * This creates a data gather component for the energy modeller.
     *
     * @param datasource The data source that provides information about the
     * host resources and the virtual machines running on them.
     * @param connector The database connector used to do this. It is best to
     * give this component its own database connection as it will make heavy use
     * of it.
     * @param calibrator The calibrator to call in the event a new host is detected.
     */
    public DataGatherer(HostDataSource datasource, DatabaseConnector connector, Calibrator calibrator) {
        this.datasource = datasource;
        this.connector = connector;
        this.calibrator = calibrator;
        for (Host dbHost : connector.getHosts()) {
            knownHosts.put(dbHost.getHostName(), dbHost);
        }
    }

    /**
     * This populates the list of hosts that is known to the energy modeller.
     */
    public void populateHostList() {
        Collection<Host> hosts = datasource.getHostList();
        for (Host host : hosts) {
            if (!knownHosts.containsKey(host.getHostName())) {
                knownHosts.put(host.getHostName(), host);
            }
        }
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
            List<Host> hostList = datasource.getHostList();
            refreshKnownHostList(hostList);
            for (Host host : hostList) {
                HostMeasurement measurement = datasource.getHostData(host);
                /**
                 * Update only if a value has not been provided before or the
                 * timestamp value has changed. This keeps the data written to
                 * backing store as clean as possible.
                 */
                if (lastTimeStampSeen.get(host) == null || measurement.getClock() > lastTimeStampSeen.get(host)) {
                    lastTimeStampSeen.put(host, measurement.getClock());
                    connector.writeHostHistoricData(host, measurement.getClock(), measurement.getPower(), measurement.getEnergy());
                }
            }
            try {
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
     * @param hostList  The list of host gained from the data source.
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
     * @return the knownHosts
     */
    public HashMap<String, Host> getHostList() {
        return knownHosts;
    }

}
