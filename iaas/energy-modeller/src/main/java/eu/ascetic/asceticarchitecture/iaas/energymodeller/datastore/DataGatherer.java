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
import eu.ascetic.asceticarchitecture.iaas.energymodeller.types.energyuser.EnergyUsageSource;
import eu.ascetic.asceticarchitecture.iaas.energymodeller.types.energyuser.Host;
import eu.ascetic.asceticarchitecture.iaas.energymodeller.types.energyuser.VmDeployed;
import eu.ascetic.asceticarchitecture.iaas.energymodeller.types.energyuser.usage.HostVmLoadFraction;
import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.configuration.PropertiesConfiguration;

/**
 * The data gatherer takes data from the data source and writes them into the
 * database for further usage.
 *
 * @author Richard
 */
public class DataGatherer implements Runnable {

    private final HostDataSource datasource;
    private final DatabaseConnector database;
    private final Calibrator calibrator;
    private boolean running = true;
    private int faultCount = 0;
    private HashMap<String, Host> knownHosts = new HashMap<>();
    private HashMap<String, VmDeployed> knownVms = new HashMap<>();
    private final HashMap<Host, Long> lastTimeStampSeen = new HashMap<>();
    private static final String CONFIG_FILE = "energy-modeller-data-gatherer.properties";
    private boolean logVmsToDisk = false;
    private boolean performDataGathering = false;
    private boolean loggerConsiderIdleEnergy = true;

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
        this.database = connector;
        this.calibrator = calibrator;
        populateHostList();
        try {
            PropertiesConfiguration config;
            if (new File(CONFIG_FILE).exists()) {
                config = new PropertiesConfiguration(CONFIG_FILE);
            } else {
                config = new PropertiesConfiguration();
                config.setFile(new File(CONFIG_FILE));
            }
            config.setAutoSave(true); //This will save the configuration file back to disk. In case the defaults need setting.
            logVmsToDisk = config.getBoolean("iaas.energy.modeller.data.gatherer.log.vms", logVmsToDisk);
            config.setProperty("iaas.energy.modeller.data.gatherer.log.vms", logVmsToDisk);
            loggerConsiderIdleEnergy = config.getBoolean("iaas.energy.modeller.data.gatherer.log.consider_idle_energy", loggerConsiderIdleEnergy);
            config.setProperty("iaas.energy.modeller.data.gatherer.log.consider_idle_energy", loggerConsiderIdleEnergy);

        } catch (ConfigurationException ex) {
            Logger.getLogger(DataGatherer.class.getName()).log(Level.INFO, "Error loading the configuration of the IaaS energy modeller", ex);
        }
    }

    /**
     * This populates the list of hosts and their VMs that are known to the
     * energy modeller.
     */
    private void populateHostList() {
        Collection<Host> hosts = datasource.getHostList();
        database.setHosts(hosts);
        //Ensure calibration data is recovered from the db.
        for (Host host : hosts) {
            checkAndCalibrateHost(host);
        }
        Collection<VmDeployed> vms = datasource.getVmList();
        vms = database.getVMProfileData(vms);
        database.setVms(vms);
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
     * This given a VM determines its Host. It is used for the initial
     * assignment of this value.
     *
     * @param vm The deployed vm
     * @return The host that it belongs to.
     */
    private Host getVMsHost(VmDeployed vm) {
        //This returns the already allocated answer
        if (vm.getAllocatedTo() != null) {
            return vm.getAllocatedTo();
        }
        //TODO consider using the VM property for this mapping here
        //TODO consider adding a file based map system here.
        /**
         * This block of code takes the agreed assumption that the host name
         * ends with "_<hostname>" and that "_" exist nowhere else in the name.
         */
        String name = vm.getName();
        int parseTokenPos = name.indexOf("_");
        if (parseTokenPos == -1 && vm.getAllocatedTo() == null) {
            return null;
        }
        return getHost(name.substring(parseTokenPos + 1, name.length()));
    }

    /**
     * This stops the data gatherer from running.
     */
    public void stop() {
        running = false;
        database.closeConnection();
    }

    /**
     * This filters a list of energy users and returns only the list of hosts
     *
     * @param energyUsers The list of energy users
     * @return The list of hosts.
     */
    private List<Host> getHostList(List<EnergyUsageSource> input) {
        ArrayList<Host> answer = new ArrayList<>();
        for (EnergyUsageSource item : input) {
            if (item.getClass().equals(Host.class)) {
                answer.add((Host) item);
            }
        }
        return answer;
    }

    /**
     * This filters a list of energy users and returns only the list of vms that
     * have been deployed
     *
     * @param energyUsers The list of energy users
     * @return The list of VMs that have been deployed.
     */
    private List<VmDeployed> getVMList(List<EnergyUsageSource> energyUsers) {
        ArrayList<VmDeployed> answer = new ArrayList<>();
        for (EnergyUsageSource item : energyUsers) {
            if (item.getClass().equals(VmDeployed.class)) {
                answer.add((VmDeployed) item);
            }
        }
        return answer;
    }

    @Override
    public void run() {
        VmEnergyUsageLogger vmUsageLogger = null;
        if (logVmsToDisk && performDataGathering) {
            vmUsageLogger = new VmEnergyUsageLogger(new File("VmEnergyUsageData.txt"), true);
            vmUsageLogger.setConsiderIdleEnergy(loggerConsiderIdleEnergy);
            Thread vmUsageLoggerThread = new Thread(vmUsageLogger);
            vmUsageLoggerThread.setDaemon(true);
            vmUsageLoggerThread.start();
        }
        /**
         * Polls the data source and write values to the database.
         */
        while (running) {
            try {
                Logger.getLogger(DataGatherer.class.getName()).log(Level.INFO, "Data gatherer: Obtaining online host and vm list");
                List<EnergyUsageSource> energyConsumers = datasource.getHostAndVmList();
                List<Host> hostList = getHostList(energyConsumers);
                refreshKnownHostList(hostList);
                List<VmDeployed> vmList = getVMList(energyConsumers);
                refreshKnownVMList(vmList);
                Logger.getLogger(DataGatherer.class.getName()).log(Level.INFO, "Data gatherer: Obtaining specific host information");
                List<HostMeasurement> measurements = datasource.getHostData(hostList);
                for (HostMeasurement measurement : measurements) {
                    Host host = knownHosts.get(measurement.getHost().getHostName());
                    /**
                     * This ensures all the calibration data is available, by
                     * setting the host from the cached data. HostList
                     * originates from the data source and not from the
                     * database/cache, which includes information such as idle
                     * energy usage.
                     */
                    measurement.setHost(host);
                    /**
                     * Update only if a value has not been provided before or
                     * the timestamp value has changed. This keeps the data
                     * written to backing store as clean as possible.
                     */
                    if (performDataGathering) {
                        gatherMeasurements(host, measurement, vmList, vmUsageLogger);
                    }
                }
                try {
                    //Note: The Zabbix API takes a few seconds to call, so don't call it faster than 3-4 seconds
                    Thread.sleep(1000);
                } catch (InterruptedException ex) {
                    Logger.getLogger(DataGatherer.class.getName()).log(Level.SEVERE, "The data gatherer was interupted.", ex);
                }
                faultCount = (faultCount > 0 ? faultCount - 1 : 0);
            } catch (Exception ex) { //This should always try to gather data from the data source.
                faultCount = faultCount + 1;
                Logger.getLogger(DataGatherer.class.getName()).log(Level.SEVERE, "The data gatherer encountered a fault, Fault Number:" + faultCount, ex);
                if (faultCount > 100) {
                    Logger.getLogger(DataGatherer.class.getName()).log(Level.SEVERE, "Many faults were seen in a row the energy modeller is now pausing from gathering data.");
                    faultCount = 0;
                    try {
                        Thread.sleep(TimeUnit.MINUTES.toMillis(5));
                    } catch (InterruptedException e) {
                        Logger.getLogger(DataGatherer.class.getName()).log(Level.SEVERE, "The data gatherer was interupted.", ex);
                    }
                }
            }
        }
    }

    /**
     * This method gathers and writes host measurements to disk and to the
     * background database for future usage.
     *
     * @param host The host to gather data for
     * @param measurement The measurement data to write to disk.
     * @param vmList The list of VMs that are currently running
     * @param logger The logger that is used to write VM data to disk.
     */
    private void gatherMeasurements(Host host, HostMeasurement measurement, List<VmDeployed> vmList, VmEnergyUsageLogger logger) {
        if (lastTimeStampSeen.get(host) == null || measurement.getClock() > lastTimeStampSeen.get(host)) {
            lastTimeStampSeen.put(host, measurement.getClock());
            Logger.getLogger(DataGatherer.class.getName()).log(Level.INFO, "Data gatherer: Writing out host information");
            if (!measurement.getPowerAndEnergyMetricsExist()) {
                return; //This guards against not having a Watt meter attached.
            }
            database.writeHostHistoricData(host, measurement.getClock(), measurement.getPower(), measurement.getEnergy());
            Logger.getLogger(DataGatherer.class.getName()).log(Level.INFO, "Data gatherer: Obtaining list of vms on host {0}", host.getHostName());
            ArrayList<VmDeployed> vms = getVMsOnHost(host, vmList);
            if (!vms.isEmpty()) {
                HostVmLoadFraction fraction = new HostVmLoadFraction(host, measurement.getClock());
                Logger.getLogger(DataGatherer.class.getName()).log(Level.INFO, "Data gatherer: Obtaining specific vm information");
                List<VmMeasurement> vmMeasurements = datasource.getVmData(vms);
                fraction.setFraction(vmMeasurements);
                Logger.getLogger(DataGatherer.class.getName()).log(Level.INFO, "Data gatherer: Writing out vm information");
                database.writeHostVMHistoricData(host, measurement.getClock(), fraction);
                if (logger != null) {
                    Logger.getLogger(DataGatherer.class.getName()).log(Level.INFO, "Data gatherer: Logging out to Zabbix file");
                    logger.printToFile(logger.new Pair(measurement, fraction));
                }
            }
        }
    }

    /**
     * The hash map gives a faster way to find a specific host. This converts
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
            database.setHosts(hostList);
        } else {
            List<Host> newHosts = discoverNewHosts(hostList);
            database.setHosts(newHosts);
            for (Host host : newHosts) {
                host = checkAndCalibrateHost(host);
                knownHosts.put(host.getHostName(), host);
            }
        }
    }

    /**
     * This checks a host to see if its calibrated then performs calibration if
     * it is not calibrated.
     *
     * @param host The host to check.
     * @return The calibrated host.
     */
    private Host checkAndCalibrateHost(Host host) {
        if (!host.isCalibrated()) {
            host.setCalibrationData(database.getHostCalibrationData(host).getCalibrationData());
            host = database.getHostProfileData(host);
        }
        if (!host.isCalibrated()) {
            calibrator.calibrateHostEnergyData(host);
        }
        return host;
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
        //Perform a refresh to make sure the VMs have been written to backing store
        if (knownVms == null) {
            knownVms = toHashMapVm(vmList);
            database.getVMProfileData(vmList);
            database.setVms(vmList);
        } else {
            List<VmDeployed> newVms = discoverNewVMs(vmList);
            database.getVMProfileData(vmList);
            database.setVms(newVms);
            for (VmDeployed vm : newVms) {
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
            if (vm.getAllocatedTo() == null) {
                vm.setAllocatedTo(getVMsHost(vm));
            }
            if (!knownVms.containsKey(vm.getName())) {
                answer.add(vm);
            } else if (vm.getAllocatedTo() != null
                    && vm.getAllocatedTo() != knownVms.get(vm.getName()).getAllocatedTo()) {
                /**
                 * Force an update if the host mapping changes, i.e. due to a VM
                 * migration.
                 */
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
     * This provides the list of known Vms
     *
     * @param deploymentId The deployment ID of the VM set
     * @return The list of known Vms
     */
    public HashSet<VmDeployed> getVmList(String deploymentId) {
        HashSet<VmDeployed> answer = new HashSet<>();
        for (VmDeployed vmDeployed : knownVms.values()) {
            if (deploymentId.equals(vmDeployed.getDeploymentID())) {
                answer.add(vmDeployed);
            }
        }
        return answer;
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
     * This allows a VM that has been deployed to have extra details about it
     * set such as tag information. This only allows already known VMs to be
     * edited.
     *
     * @param vm The vm to add to the list of known VMs
     * @return Indicates if the VM was in the list of known VMs.
     */
    public boolean setVm(VmDeployed vm) {
        if (knownVms.containsKey(vm.getName())) {
            knownVms.put(vm.getName(), vm);
            return true;
        }
        return false;
    }

    /**
     * This gets a list of the VMs that are currently on a host machine.
     *
     * @param host The host machine to get the VM list for
     * @return The list of VMs on the specified host
     */
    public ArrayList<VmDeployed> getVMsOnHost(Host host) {
        return getVMsOnHost(host, datasource.getVmList());
    }

    /**
     * This gets a list of the VMs that are currently on a host machine.
     *
     * @param host The host machine to get the VM list for
     * @param activeVMs The list of VMs known to be active on the host.
     * @return The list of VMs on the specified host
     */
    public ArrayList<VmDeployed> getVMsOnHost(Host host, List<VmDeployed> activeVMs) {
        HashSet<VmDeployed> currentVMs = new HashSet<>();
        currentVMs.addAll(activeVMs);
        ArrayList<VmDeployed> answer = new ArrayList<>();
        for (VmDeployed vm : knownVms.values()) {
            if (host.equals(vm.getAllocatedTo()) && currentVMs.contains(vm)) {
                answer.add(vm);
            }
        }
        return answer;
    }

    /**
     * This sets if this data gatherer should log to disk VM data and write the
     * data it finds regarding hosts to its backing store. It is expected that a
     * energy modeller running in standalone mode will gather data while one
     * using for querying that is instantiated as a class inside a larger
     * program will not gather data.
     *
     * @param performDataGathering true if this energy modeller is responsible
     * for writing to the energy modeller's database.
     */
    public void setPerformDataGathering(boolean performDataGathering) {
        this.performDataGathering = performDataGathering;
    }

    /**
     * This indicates if this data gatherer should log to disk VM data and write
     * the data it finds regarding hosts to its backing store.
     *
     * @return true if data is to be written to disk and to the background
     * database otherwise false.
     */
    public boolean performDataGathering() {
        return this.performDataGathering;
    }

}
