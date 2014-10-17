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
package eu.ascetic.energy.modeller.trace.file.generator;

import eu.ascetic.asceticarchitecture.iaas.energymodeller.datastore.DatabaseConnector;
import eu.ascetic.asceticarchitecture.iaas.energymodeller.queryinterface.datasourceclient.HostDataSource;
import eu.ascetic.asceticarchitecture.iaas.energymodeller.types.TimePeriod;
import eu.ascetic.asceticarchitecture.iaas.energymodeller.types.energyuser.Host;
import eu.ascetic.asceticarchitecture.iaas.energymodeller.types.energyuser.usage.HostVmLoadFraction;
import eu.ascetic.asceticarchitecture.iaas.energymodeller.types.usage.HostEnergyRecord;
import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.GregorianCalendar;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * This data collector displays energy data for the IaaS Energy Modeller.
 *
 * @author Richard
 */
public class DataCollector {

    private final HostDataSource datasource;
    private final DatabaseConnector connector;
    private final HostEnergyTraceDataLogger logger;
    private final VMEnergyTraceDataLogger vmLogger;

    /**
     * This creates a new data collector for the energy modeller display tool.
     *
     * @param datasource The data source to use.
     * @param connector Database connector to use.
     */
    public DataCollector(HostDataSource datasource, DatabaseConnector connector) {
        this.datasource = datasource;
        this.connector = connector;
        logger = new HostEnergyTraceDataLogger(new File("HostData.csv"), true);
        new Thread(logger).start();
        vmLogger = new VMEnergyTraceDataLogger(new File("VMData.csv"), true);
        new Thread(vmLogger).start();
    }

    public void gatherData() {
        List<Host> hostList = new ArrayList<>();
        hostList.addAll(connector.getHosts());

        for (Host host : hostList) {
            GregorianCalendar cal = new GregorianCalendar();
            long durationSec = TimeUnit.DAYS.toMillis(7);
            cal.setTimeInMillis(cal.getTimeInMillis() - durationSec);
            TimePeriod period = new TimePeriod(cal, durationSec);
            //TODO This aquires the time series needed.
            Collection<HostVmLoadFraction> vmMeasurements = connector.getHostVmHistoryLoadData(host, period);
            List<HostEnergyRecord> uncleanedHost = connector.getHostHistoryData(host, period);
            for (HostEnergyRecord hostEnergyRecord : uncleanedHost) {
                logger.printToFile(hostEnergyRecord);
            }
            ArrayList<Pair> hostToVmDataMap;
            if (vmMeasurements != null && !vmMeasurements.isEmpty()) {
                hostToVmDataMap = getData(vmMeasurements, uncleanedHost);
                for (Pair vmData : hostToVmDataMap) {
                    vmLogger.printToFile(vmData);
                }
            }
        }
        connector.closeConnection();
        logger.stop();
        vmLogger.stop();
        /**
         * TODO NOTE: The sequence is complete, here so in the display tool all
         * the data is logged out to disk quickly.
         */
    }

    /**
     * This compares the vm resource utilisation dataset and the host energy
     * data and ensures that they have a 1:1 mapping
     *
     * @param vmData The Vms usage dataset
     * @param hostData The host's energy usage dataset
     * @return The mappings between each dataset elements
     */
    public ArrayList<Pair> getData(Collection<HostVmLoadFraction> vmData, List<HostEnergyRecord> hostData) {
        ArrayList<Pair> answer = new ArrayList<>();
        //Make a copy and compare times remove them each time.
        LinkedList<HostVmLoadFraction> vmDataCopy = new LinkedList<>();
        vmDataCopy.addAll(vmData);
        LinkedList<HostEnergyRecord> hostDataCopy = new LinkedList<>();
        hostDataCopy.addAll(hostData);
        HostVmLoadFraction vmHead = vmDataCopy.pop();
        HostEnergyRecord hostHead = hostDataCopy.pop();
        while (!vmDataCopy.isEmpty() && !hostDataCopy.isEmpty()) {
            if (vmHead.getTime() == hostHead.getTime()) {
                answer.add(new Pair(hostHead, vmHead));
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
     * This binds a host energy record to VM load fraction information. Thus 
     * allowing for the calculations to take place.
     */
    public class Pair {
        private HostEnergyRecord host;
        private HostVmLoadFraction vmLoadFraction;

        public Pair(HostEnergyRecord host, HostVmLoadFraction vmLoadFraction) {
            this.host = host;
            this.vmLoadFraction = vmLoadFraction;
        }
        
        /**
         * @return the host
         */
        public HostEnergyRecord getHost() {
            return host;
        }

        /**
         * @return the vmLoadFraction
         */
        public HostVmLoadFraction getVmLoadFraction() {
            return vmLoadFraction;
        }
    }

}
