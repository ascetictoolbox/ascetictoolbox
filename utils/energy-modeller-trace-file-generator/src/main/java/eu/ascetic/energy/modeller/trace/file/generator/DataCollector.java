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
import eu.ascetic.asceticarchitecture.iaas.energymodeller.types.TimePeriod;
import eu.ascetic.asceticarchitecture.iaas.energymodeller.types.energyuser.Host;
import eu.ascetic.asceticarchitecture.iaas.energymodeller.types.energyuser.usage.HostVmLoadFraction;
import eu.ascetic.asceticarchitecture.iaas.energymodeller.types.usage.HostEnergyRecord;
import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

/**
 * This data collector displays energy data for the IaaS Energy Modeller.
 *
 * @author Richard Kavanagh
 */
public class DataCollector {

    private final DatabaseConnector connector;
    private final HostEnergyTraceDataLogger logger;
    private final VMEnergyTraceDataLogger vmLogger;

    /**
     * This creates a new data collector for the energy modeller display tool.
     *
     * @param connector Database connector to use.
     */
    public DataCollector(DatabaseConnector connector) {
        this.connector = connector;
        logger = new HostEnergyTraceDataLogger(new File("HostData.csv"), true);
        new Thread(logger).start();
        vmLogger = new VMEnergyTraceDataLogger(new File("VMData.csv"), true);
        new Thread(vmLogger).start();
    }

    public void gatherData(TimePeriod period) {
        List<Host> hostList = new ArrayList<>();
        hostList.addAll(connector.getHosts());

        for (Host host : hostList) {
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
        while (vmLogger.stillWorking() && logger.stillWorking()) {
            try {
                Thread.sleep(1000);
            } catch (InterruptedException ex) {
            }
        }
        connector.closeConnection();
        logger.stop();
        vmLogger.stop();
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

        private final HostEnergyRecord host;
        private final HostVmLoadFraction vmLoadFraction;

        /**
         * This create a pair of data items linking host energy records to VM
         * load fraction information.
         *
         * @param host The host record
         * @param vmLoadFraction The vm load fraction record.
         */
        public Pair(HostEnergyRecord host, HostVmLoadFraction vmLoadFraction) {
            this.host = host;
            this.vmLoadFraction = vmLoadFraction;
        }

        /**
         * This returns the host data for the paired data items.
         *
         * @return the host
         */
        public HostEnergyRecord getHost() {
            return host;
        }

        /**
         * This returns the vm load fraction data for the paired data items.
         *
         * @return the vmLoadFraction
         */
        public HostVmLoadFraction getVmLoadFraction() {
            return vmLoadFraction;
        }
    }

}
