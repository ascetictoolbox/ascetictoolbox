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

import eu.ascetic.asceticarchitecture.iaas.energymodeller.queryinterface.datasourceclient.HostDataSource;
import eu.ascetic.asceticarchitecture.iaas.energymodeller.queryinterface.datasourceclient.HostMeasurement;
import eu.ascetic.asceticarchitecture.iaas.energymodeller.types.energyuser.Host;
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
    private boolean running = true;
    private int faultCount = 0;
    private List<Host> knownHosts = null;
    private HashMap<Host, Long> lastTimeStampSeen = new HashMap<>();

    public DataGatherer(HostDataSource datasource, DatabaseConnector connector) {
        this.datasource = datasource;
        this.connector = connector;
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
            //Perform a refresh to make sure the host has been written to backing store
            if (knownHosts == null) {
                knownHosts = hostList;
                connector.setHosts(hostList);
            }
            for (Host host : hostList) {
                HostMeasurement measurement = datasource.getHostData(host);
                /**
                 * Update only if a value has not been provided before or the timestamp value has changed.
                 * This keeps the data written to backing store as clean as possible.
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
}
