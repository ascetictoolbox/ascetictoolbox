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
package eu.ascetic.asceticarchitecture.iaas.energymodeller;

import eu.ascetic.asceticarchitecture.iaas.energymodeller.calibration.Calibrator;
import eu.ascetic.asceticarchitecture.iaas.energymodeller.datastore.DataGatherer;
import eu.ascetic.asceticarchitecture.iaas.energymodeller.datastore.DatabaseConnector;
import eu.ascetic.asceticarchitecture.iaas.energymodeller.datastore.DefaultDatabaseConnector;
import eu.ascetic.asceticarchitecture.iaas.energymodeller.energypredictor.CpuOnlyEnergyPredictor;
import eu.ascetic.asceticarchitecture.iaas.energymodeller.energypredictor.EnergyPredictorInterface;
import eu.ascetic.asceticarchitecture.iaas.energymodeller.energypredictor.vmenergyshare.EnergyDivision;
import eu.ascetic.asceticarchitecture.iaas.energymodeller.energypredictor.vmenergyshare.LoadFractionShareRule;
import eu.ascetic.asceticarchitecture.iaas.energymodeller.energypredictor.vmenergyshare.historic.HistoricLoadBasedDivision;
import eu.ascetic.asceticarchitecture.iaas.energymodeller.energypredictor.vmenergyshare.historic.LoadBasedDivision;
import eu.ascetic.asceticarchitecture.iaas.energymodeller.queryinterface.datasourceclient.HostDataSource;
import eu.ascetic.asceticarchitecture.iaas.energymodeller.queryinterface.datasourceclient.WattsUpMeterDataSourceAdaptor;
import eu.ascetic.asceticarchitecture.iaas.energymodeller.queryinterface.datasourceclient.ZabbixDirectDbDataSourceAdaptor;
import eu.ascetic.asceticarchitecture.iaas.energymodeller.types.OVFConverterFactory;
import eu.ascetic.asceticarchitecture.iaas.energymodeller.types.TimePeriod;
import eu.ascetic.asceticarchitecture.iaas.energymodeller.types.energyuser.Host;
import eu.ascetic.asceticarchitecture.iaas.energymodeller.types.energyuser.VM;
import eu.ascetic.asceticarchitecture.iaas.energymodeller.types.energyuser.VmDeployed;
import eu.ascetic.asceticarchitecture.iaas.energymodeller.types.energyuser.usage.HostVmLoadFraction;
import eu.ascetic.asceticarchitecture.iaas.energymodeller.types.usage.CurrentUsageRecord;
import eu.ascetic.asceticarchitecture.iaas.energymodeller.types.usage.EnergyUsagePrediction;
import eu.ascetic.asceticarchitecture.iaas.energymodeller.types.usage.HistoricUsageRecord;
import eu.ascetic.asceticarchitecture.iaas.energymodeller.types.usage.HostEnergyRecord;
import eu.ascetic.utils.ovf.api.OvfDefinition;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.LogManager;
import java.util.logging.Logger;

/**
 * This is the main entry point for the energy modeller. It is going to have the
 * following core functionality:
 *
 * The ability to provide current energy usage values for VMs. The main aim of
 * this will be to aid the placement of VMs on machines. The ability to provide
 * current energy usage for the infrastructure the VMs run upon.
 *
 * Predicted values for the future, energy usage. Including: projecting current
 * deployments energy usage and candidate deployments energy usage i.e. a VM
 * with a workload mapping to a machine.
 *
 * A historic record of energy usage for billing purposes.
 *
 * @author Richard Kavanagh
 */
public class EnergyModeller {

    private static final String DEFAULT_PREDICTOR_PACKAGE = "eu.ascetic.asceticarchitecture.iaas.energymodeller.energypredictor";
    private static final String DEFAULT_DATA_SOURCE_PACKAGE = "eu.ascetic.asceticarchitecture.iaas.energymodeller.queryinterface.datasourceclient";
    private static final String DEFAULT_HISTORIC_ENERGY_DIVISION_RULE_PACKAGE = "eu.ascetic.asceticarchitecture.iaas.energymodeller.energypredictor.vmenergyshare.historic";
    private EnergyPredictorInterface predictor = new CpuOnlyEnergyPredictor();
    private HostDataSource datasource;
    private final DatabaseConnector database;
    private Calibrator calibrator;
    private Thread calibratorThread;
    private DataGatherer dataGatherer;
    private Thread dataGatherThread;
    private Class<?> historicEnergyDivisionMethod = LoadBasedDivision.class;
    private boolean considerIdleEnergyCurrentVm = true;

    /**
     * This runs the energy modeller in standalone mode.
     *
     * @param args no args are expected.
     */
    public static void main(String[] args) {
        try {
            if (new File("energy-modeller-logging.properties").exists()) {
                LogManager.getLogManager().readConfiguration(new FileInputStream(new File("energy-modeller-logging.properties")));
            }
        } catch (FileNotFoundException ex) {
            Logger.getLogger(EnergyModeller.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException | SecurityException ex) {
            Logger.getLogger(EnergyModeller.class.getName()).log(Level.SEVERE,
                    "Could not read the energy modeller's log settings file", ex);
        }
        /**
         * Only the instance that is stated in standalone mode should write to
         * the background database and log VM data out to disk. All other
         * instances should read from the database only.
         */
        EnergyModeller modeller = new EnergyModeller(true);
        Logger.getLogger(EnergyModeller.class.getName()).log(Level.SEVERE,
                "The logger for the energy modeller has now started");
        while (true) {
            try {
                Thread.sleep(1000);
            } catch (InterruptedException ex) {
                Logger.getLogger(EnergyModeller.class.getName()).log(Level.SEVERE, "The energy modeller was interupted.", ex);
            }
        }
    }

    /**
     * SingletonHolder is loaded on the first execution of
     * Singleton.getInstance() or the first access to SingletonHolder.INSTANCE,
     * not before.
     */
    private static class SingletonHolder {

        private static final EnergyModeller INSTANCE = new EnergyModeller();
    }

    /**
     * This creates a new singleton instance of the energy modeller.
     *
     * @return A singleton instance of a energy modeller.
     */
    public static EnergyModeller getInstance() {
        return SingletonHolder.INSTANCE;
    }

    /**
     * This creates a new energy modeller.
     */
    public EnergyModeller() {
        datasource = new ZabbixDirectDbDataSourceAdaptor();
        database = new DefaultDatabaseConnector();
        startup(false);
    }

    /**
     * This creates a new energy modeller.
     *
     * @param performDataGathering Indicates if this energy modeller should,
     * write to disk and also write to the background database.
     */
    public EnergyModeller(boolean performDataGathering) {
        datasource = new ZabbixDirectDbDataSourceAdaptor();
        database = new DefaultDatabaseConnector();
        startup(performDataGathering);
    }

    /**
     * This creates a new energy modeller.
     *
     * @param datasource The data source to use for the energy modeller
     * @param database The database to use for the energy modeller.
     */
    public EnergyModeller(HostDataSource datasource, DatabaseConnector database) {
        this.datasource = datasource;
        this.database = database;
        startup(false);
    }

    /**
     * This is common code for the constructors
     */
    private void startup(boolean performDataGathering) {
        calibrator = new Calibrator(datasource, database);
        dataGatherer = new DataGatherer(datasource, new DefaultDatabaseConnector(), calibrator);
        dataGatherer.setPerformDataGathering(performDataGathering);
        try {
            calibratorThread = new Thread(calibrator);
            calibratorThread.setDaemon(true);
            calibratorThread.start();
            calibrateAllHostsWithoutData();
            dataGatherThread = new Thread(dataGatherer);
            dataGatherThread.setDaemon(true);
            dataGatherThread.start();
        } catch (Exception ex) {
            Logger.getLogger(EnergyModeller.class.getName()).log(Level.SEVERE, "The energry modeller failed to start correctly", ex);
        }
    }

    /**
     * This allows the energy predictor to be set
     *
     * @param energyPredictor The name of the algorithm to set
     */
    public void setEnergyPredictor(String energyPredictor) {
        try {
            if (!energyPredictor.startsWith(DEFAULT_PREDICTOR_PACKAGE)) {
                energyPredictor = DEFAULT_PREDICTOR_PACKAGE + "." + energyPredictor;
            }
            predictor = (EnergyPredictorInterface) (Class.forName(energyPredictor).newInstance());
        } catch (ClassNotFoundException ex) {
            if (predictor == null) {
                predictor = new CpuOnlyEnergyPredictor();
            }
            Logger.getLogger(EnergyModeller.class.getName()).log(Level.WARNING, "The scheduling algorithm specified was not found");
        } catch (InstantiationException | IllegalAccessException ex) {
            if (predictor == null) {
                predictor = new CpuOnlyEnergyPredictor();
            }
            Logger.getLogger(EnergyModeller.class.getName()).log(Level.WARNING, "The scheduling algorithm did not work", ex);
        }
    }

    /**
     * This allows the energy modellers data source to be set
     *
     * @param dataSource The name of the data source to use for the energy
     * modeller
     */
    public void setDataSource(String dataSource) {
        try {
            if (!dataSource.startsWith(DEFAULT_DATA_SOURCE_PACKAGE)) {
                dataSource = DEFAULT_DATA_SOURCE_PACKAGE + "." + dataSource;
            }
            /**
             * This is a special case that requires it to be loaded under the
             * singleton design pattern.
             */
            String wattsUpMeter = DEFAULT_DATA_SOURCE_PACKAGE + ".WattsUpMeterDataSourceAdaptor";
            if (wattsUpMeter.equals(dataSource)) {
                datasource = WattsUpMeterDataSourceAdaptor.getInstance();
            } else {
                datasource = (HostDataSource) (Class.forName(dataSource).newInstance());
            }
            calibrator.setDatasource(datasource);
        } catch (ClassNotFoundException ex) {
            if (datasource == null) {
                datasource = new ZabbixDirectDbDataSourceAdaptor();
                calibrator.setDatasource(datasource);
            }
            Logger.getLogger(EnergyModeller.class.getName()).log(Level.WARNING, "The data source specified was not found");
        } catch (InstantiationException | IllegalAccessException ex) {
            if (datasource == null) {
                datasource = new ZabbixDirectDbDataSourceAdaptor();
                calibrator.setDatasource(datasource);
            }
            Logger.getLogger(EnergyModeller.class.getName()).log(Level.WARNING, "The data source did not work", ex);
        }
    }

    /**
     * This allows the energy modellers energy division rule be set for historic
     * data.
     *
     * @param divisionRule The name of the divisionRule to use for the energy
     * modeller
     */
    public void setHistoricEnergyDivisionRule(String divisionRule) {
        try {
            if (!divisionRule.startsWith(DEFAULT_HISTORIC_ENERGY_DIVISION_RULE_PACKAGE)) {
                divisionRule = DEFAULT_HISTORIC_ENERGY_DIVISION_RULE_PACKAGE + "." + divisionRule;
            }
            historicEnergyDivisionMethod = (Class.forName(divisionRule));
        } catch (ClassNotFoundException ex) {
            if (historicEnergyDivisionMethod == null) {
                historicEnergyDivisionMethod = LoadBasedDivision.class;
            }
            Logger.getLogger(EnergyModeller.class.getName()).log(Level.WARNING, "The energy division rule specified was not found", ex);
        }
    }

    /**
     * This indicates if the current energy usage for a VM should consider idle
     * energy of the host or not.
     *
     * @param considerIdleEnergy If idle energy should be considered or not
     */
    public void setConsiderIdleEnergyCurrentVm(boolean considerIdleEnergy) {
        this.considerIdleEnergyCurrentVm = considerIdleEnergy;
    }

    /**
     * This returns the energy usage for a named virtual machine.
     *
     * @param vm A reference to the VM
     * @param timePeriod The time period for which the query applies.
     * @return The energy usage record for the named VM.
     *
     * Historic Values: Avg Watts over time Avg Current (useful??) Avg Voltage
     * (useful??) kWh of energy used since instantiation
     */
    public HistoricUsageRecord getEnergyRecordForVM(VmDeployed vm, TimePeriod timePeriod) {
        HistoricUsageRecord answer = new HistoricUsageRecord(vm);
        Host host = vm.getAllocatedTo();
        List<HostEnergyRecord> hostsData = database.getHostHistoryData(host, timePeriod);
        List<HostVmLoadFraction> loadFractionData = (List<HostVmLoadFraction>) database.getHostVmHistoryLoadData(host, timePeriod);
        HistoricLoadBasedDivision shareRule;
        try {
            shareRule = (HistoricLoadBasedDivision) historicEnergyDivisionMethod.newInstance();
            shareRule.setHost(host);
        } catch (InstantiationException | IllegalAccessException ex) {
            shareRule = new LoadBasedDivision(host);
            Logger.getLogger(EnergyModeller.class.getName()).log(Level.SEVERE,
                    "A new instance of the energy division mechanism specified "
                    + "failed to be created, falling back to defaults.", ex);
        }
        //Fraction off energy used based upon this share rule.
        for (VmDeployed deployed : HostVmLoadFraction.getVMs(loadFractionData)) {
            shareRule.addVM(((VM) deployed));
        }
        shareRule.setEnergyUsage(hostsData);
        shareRule.setLoadFraction(loadFractionData);
        double totalEnergy = shareRule.getEnergyUsage(vm);
        answer.setTotalEnergyUsed(totalEnergy);
        answer.setAvgPowerUsed(totalEnergy / (((double) shareRule.getDuration()) / 3600));
        answer.setDuration(new TimePeriod(shareRule.getStart(), shareRule.getEnd()));
        return answer;
    }

    /**
     * This provides for a collection of VMs the amount of energy that has
     * historically been used.
     *
     * @param vms The set of virtual machines.
     * @param timePeriod The time period for which the query applies.
     * @return The value returned should be separated on per VM basis. This
     * allows for a set of values to be queried at the same time and also
     * aggregated correctly.
     *
     * Envisaged purpose: Determining the amount of energy being used/having
     * been used by an SLA.
     *
     * Historic Values: Avg Watts over time Avg Current (useful??) Avg Voltage
     * (useful??) kWh of energy used since instantiation
     */
    public HashSet<HistoricUsageRecord> getEnergyRecordForVM(Collection<VmDeployed> vms, TimePeriod timePeriod) {
        HashSet<HistoricUsageRecord> answer = new HashSet<>();
        for (VmDeployed vm : vms) {
            answer.add(getEnergyRecordForVM(vm, timePeriod));
        }
        return answer;
    }

    /**
     * This provides for a collection of VMs the amount of energy that has
     * historically been used.
     *
     * @param deploymentId The deployment id of the set of virtual machines.
     * @param timePeriod The time period for which the query applies.
     * @return The value returned should be separated on per VM basis. This
     * allows for a set of values to be queried at the same time and also
     * aggregated correctly.
     *
     * Envisaged purpose: Determining the amount of energy being used/having
     * been used by an SLA.
     *
     * Historic Values: Avg Watts over time Avg Current (useful??) Avg Voltage
     * (useful??) kWh of energy used since instantiation
     */
    public HashSet<HistoricUsageRecord> getEnergyRecordForDeployment(String deploymentId, TimePeriod timePeriod) {
        HashSet<HistoricUsageRecord> answer = new HashSet<>();
        HashSet<VmDeployed> vms = dataGatherer.getVmList(deploymentId);
        for (VmDeployed vm : vms) {
            answer.add(getEnergyRecordForVM(vm, timePeriod));
        }
        return answer;
    }

    /**
     * This returns the energy usage for a named physical machine.
     *
     * @param host A reference to physical machine
     * @param timePeriod The time period for which the query applies.
     * @return
     *
     * Historic Values: Avg Watts over time (+ Max/Min??) Avg Current (useful??)
     * Avg Voltage (useful??) kWh of energy used since boot/time immemorial
     * (i.e. long as recorded data)
     *
     */
    public HistoricUsageRecord getEnergyRecordForHost(Host host, TimePeriod timePeriod) {
        List<HostEnergyRecord> data = database.getHostHistoryData(host, timePeriod);
        HistoricUsageRecord answer = new HistoricUsageRecord(host);
        double totalEnergy = 0;
        if (data.size() > 2) {
            Collections.sort(data);
            for (int i = 0; i <= data.size() - 2; i++) {
                HostEnergyRecord energy1 = data.get(i);
                HostEnergyRecord energy2 = data.get(i + 1);
                long deltaTime = energy2.getTime() - energy1.getTime();
                double deltaEnergy = Math.abs((((double) deltaTime) / 3600d) * (energy1.getPower() + energy2.getPower()) * 0.5);
                totalEnergy = totalEnergy + deltaEnergy;
            }
            TimePeriod period = new TimePeriod(data.get(0).getTime() / 1000l, data.get(data.size() - 1).getTime() / 1000l);
            answer.setAvgPowerUsed(totalEnergy / (((double) period.getDuration()) / 3600d));
            answer.setTotalEnergyUsed(totalEnergy);
            answer.setDuration(period);
        }
        if (data.size() == 1) {
            TimePeriod duration = new TimePeriod(data.get(0).getTime(), data.get(0).getTime());
            answer.setDuration(duration);
            answer.setAvgPowerUsed(data.get(0).getPower());
            answer.setTotalEnergyUsed(0);
        }
        return answer;
    }

    /**
     * This provides for a collection of physical machines the amount of energy
     * that has historically been used.
     *
     * @param hosts The set of physical machines.
     * @param timePeriod The time period for which the query applies.
     * @return The value returned should be separated on per machine basis. This
     * allows for a set of values to be queried at the same time and also
     * aggregated correctly.
     *
     * Historic Values: Avg Watts over time (+ Max/Min??) Avg Current (useful??)
     * Avg Voltage (useful??) kWh of energy used since boot/time immemorial
     * (i.e. long as recorded data)
     *
     */
    public HashSet<HistoricUsageRecord> getEnergyRecordForHost(Collection<Host> hosts, TimePeriod timePeriod) {
        HashSet<HistoricUsageRecord> answer = new HashSet<>();
        for (Host host : hosts) {
            answer.add(getEnergyRecordForHost(host, timePeriod));
        }
        return answer;
    }

    /**
     * This returns the energy usage for a named virtual machine.
     *
     * @param vm A reference to the VM
     * @return The current power usage record for the named VM.
     *
     * Current Values: Watts, current and voltage
     *
     */
    public CurrentUsageRecord getCurrentEnergyForVM(VmDeployed vm) {
        LoadFractionShareRule rule = new LoadFractionShareRule();
        Host host = vm.getAllocatedTo();
        ArrayList<VmDeployed> otherVms = dataGatherer.getVMsOnHost(host);
        ArrayList<VmDeployed> vmsDeployedOnHost = new ArrayList<>();
        ArrayList<VM> vmsOnHost = new ArrayList<>();
        vmsDeployedOnHost.addAll(otherVms);
        vmsDeployedOnHost.add(vm);
        vmsOnHost.addAll(otherVms);
        vmsOnHost.add(vm);
        rule.setVmMeasurements(datasource.getVmData(vmsDeployedOnHost));
        CurrentUsageRecord hostAnswer = datasource.getCurrentEnergyUsage(host);
        EnergyDivision divider = rule.getEnergyUsage(host, vmsOnHost);
        divider.setConsiderIdleEnergy(considerIdleEnergyCurrentVm);
        CurrentUsageRecord answer = new CurrentUsageRecord(vm);
        answer.setTime(hostAnswer.getTime());
        answer.setPower(divider.getEnergyUsage(hostAnswer.getPower(), vm));
        return answer;
    }

    /**
     * This provides for a collection of VMs the amount of energy currently in
     * use.
     *
     * @param vms The set of virtual machines.
     * @return The value returned should be separated on per VM basis. This
     * allows for a set of values to be queried at the same time and also
     * aggregated correctly.
     *
     * Envisaged purpose: Determining the amount of energy being used/having
     * been used by an SLA.
     *
     * Current Values: Watts, current and voltage
     *
     */
    public HashSet<CurrentUsageRecord> getCurrentEnergyForVM(Collection<VmDeployed> vms) {
        HashSet<CurrentUsageRecord> answer = new HashSet<>();
        for (VmDeployed vm : vms) {
            answer.add(getCurrentEnergyForVM(vm));
        }
        return answer;
    }

    /**
     * This returns the energy usage for a named physical machine.
     *
     * @param host A reference to physical machine
     * @return The power usage record for the named physical host.
     *
     * Current Values for: Power (Watts), current and voltage
     */
    public CurrentUsageRecord getCurrentEnergyForHost(Host host) {
        CurrentUsageRecord answer = datasource.getCurrentEnergyUsage(host);
        return answer;
    }

    /**
     * This provides for a collection of physical machines the amount of energy
     * currently in use.
     *
     * @param hosts The set of physical machines.
     * @return The value returned should be separated on per machine basis. This
     * allows for a set of values to be queried at the same time and also
     * aggregated correctly.
     *
     * Current Values: Watts, current and voltage
     *
     */
    public HashSet<CurrentUsageRecord> getCurrentEnergyForHost(Collection<Host> hosts) {
        HashSet<CurrentUsageRecord> answer = new HashSet<>();
        for (Host host : hosts) {
            answer.add(getCurrentEnergyForHost(host));
        }
        return answer;
    }

    /**
     * This provides the amount of energy predicted to be used by the placement
     * of a VM.
     *
     * @param vmImage The VM that is to be deployed
     * @param vMsOnHost The collection of VMs that are expected to be running on
     * the host
     * @param host The host on which the VM is to be placed
     * @return the predicted average power and total energy usage for a VM.
     *
     * Avg Watts that is expected to use over time by the VM Predicted energy
     * used (kWh) during life of VM
     */
    public EnergyUsagePrediction getPredictedEnergyForVM(VM vmImage, Collection<VM> vMsOnHost, Host host) {
        /**
         * There is an expectation that the vmImage that is expected to be be
         * deployed is in the collection of VMs that induce workload on the
         * physical host. This line below is a sanity check that ensures the
         * vmImage (deployed or otherwise, represents load on the VM.
         */
        if (!vMsOnHost.contains(vmImage)) {
            vMsOnHost.add(vmImage);
        }
        return predictor.getVMPredictedEnergy(vmImage, vMsOnHost, host);
    }
    
    /**
     * This provides the amount of energy predicted to be used by the placement
     * of a VM.
     *
     * @param vmImage The VM that is to be deployed
     * @param vMsOnHost The collection of VMs that are expected to be running on
     * the host
     * @param host The host on which the VM is to be placed
     * @param duration The period of time the estimate should run for.
     * @return the predicted average power and total energy usage for a VM.
     *
     * Avg Watts that is expected to use over time by the VM Predicted energy
     * used (kWh) during life of VM
     */
    public EnergyUsagePrediction getPredictedEnergyForVM(VM vmImage, Collection<VM> vMsOnHost, Host host, TimePeriod duration) {
        /**
         * There is an expectation that the vmImage that is expected to be be
         * deployed is in the collection of VMs that induce workload on the
         * physical host. This line below is a sanity check that ensures the
         * vmImage (deployed or otherwise, represents load on the VM.
         */
        if (!vMsOnHost.contains(vmImage)) {
            vMsOnHost.add(vmImage);
        }
        return predictor.getVMPredictedEnergy(vmImage, vMsOnHost, host, duration);
    }    

    /**
     * This provides the amount of energy predicted to be used by a given host.
     *
     * @param host The host that the energy prediction is for
     * @param virtualMachines The VMs that are on the host.
     * @return the predicted average power and total energy usage for a physical 
     * host.
     */
    public EnergyUsagePrediction getHostPredictedEnergy(Host host, Collection<VM> virtualMachines) {
        return predictor.getHostPredictedEnergy(host, virtualMachines);
    }
    
    /**
     * This provides the amount of energy predicted to be used by a given host.
     *
     * @param host The host that the energy prediction is for
     * @param virtualMachines The VMs that are on the host.
     * @param duration The period of time the estimate should run for.
     * @return the predicted average power and total energy usage for a physical 
     * host.
     */
    public EnergyUsagePrediction getHostPredictedEnergy(Host host, Collection<VM> virtualMachines, TimePeriod duration) {
        return predictor.getHostPredictedEnergy(host, virtualMachines, duration);
    }    

    //Use a program called stress, benchmarking tool to test this
    /**
     * This takes a set of hostname names and provides the object representation
     * of this host.
     *
     * @param hostname The set of hosts to return.
     * @return The host objects of the hosts that were requested by name. 
     */
    public Collection<Host> getHost(Collection<String> hostname) {
        Collection<Host> answer = new ArrayList<>();
        for (String string : hostname) {
            answer.add(getHost(string));
        }
        return answer;
    }

    /**
     * This takes a host name and provides the object representation of this
     * host.
     *
     * @param hostname The host name to get the host object for
     * @return The host for the specified host name.
     */
    public Host getHost(String hostname) {
        if (dataGatherer.getHostList().containsKey(hostname)) {
            return dataGatherer.getHost(hostname);
        }
        return null;
    }

    /**
     * This gets the list of hosts that the energy modeller knows about.
     *
     * @return The list of hosts currently known to the energy modeller.
     */
    public Collection<Host> getHostList() {
        return dataGatherer.getHostList().values();
    }

    /**
     * This gets the list of hosts that the energy modeller knows about.
     *
     * @param sort The sort order for the list of hosts. A list of prefabricated
     * comparators are available in the energy user comparators package. If the 
     * sort comparator is null the natural order of hosts will be provided.
     * @see
     * eu.ascetic.asceticarchitecture.iaas.energymodeller.types.energyuser.comparators;
     * @return The list of hosts the energy modeller knows about.
     */
    public List<Host> getHostList(Comparator<Host> sort) {
        ArrayList<Host> hosts = new ArrayList<>(dataGatherer.getHostList().values());
        if (sort == null) {
            Collections.sort(hosts);
        } else {
            Collections.sort(hosts, sort);
        }
        return hosts;
    }

    /**
     * This creates a VM object in cases where the VM has yet to be
     * instantiated.
     *
     * @param cpuCount The cpu count
     * @param ramMb The amount of memory in Mb
     * @param diskGb The amount of disk space in Gb
     * @return A new VM with the parameters specified above.
     */
    public static VM getVM(int cpuCount, int ramMb, int diskGb) {
        return new VM(cpuCount, ramMb, diskGb);
    }

    /**
     * This converts an OVF description into collection of VM objects for cases
     * where the VM has yet to be instantiated.
     *
     * @param deploymentOVF The OVF file containing VMs to be deployed.
     * @return The list of VM objects
     */
    public static Collection<VM> getVMs(OvfDefinition deploymentOVF) {
        return OVFConverterFactory.getVMs(deploymentOVF);
    }
    
    /**
     * This gets the list of VMs on a named host that the energy modeller 
     * knows about.
     * @param host The host to get the VMs for
     * @return The list of VMs that are currently running on the named host.
     */
    public ArrayList<VmDeployed> getVMsOnHost(Host host) {
        return dataGatherer.getVMsOnHost(host);
    }

    /**
     * This given a name of a VM provides the object representation of it.
     * Note: This will only return the representation of a VM if the VM has been
     * deployed and reported by the monitoring infrastructure.
     *
     * @param name The name of the VM
     * @return The VM with the specified name, null if not known to the energy
     * modeller.
     */
    public VmDeployed getVM(String name) {
        return dataGatherer.getVm(name);
    }

    /**
     * This given a name of a VM provides the object representation of it.
     * Note: This will only return the representation of a VM if the VM has been
     * deployed and reported by the monitoring infrastructure.
     *
     * @param vmId The name of the VM
     * @return The VM with the specified name, null if not known to the energy
     * modeller.
     */
    public VmDeployed getVM(int vmId) {
        return dataGatherer.getVm(vmId);
    }    
    
    /**
     * This sets information about the VM that describes the applications,
     * that are inside it.
     * @param vm The VM that is to be deployed.
     */
    public void setVMProfileData(VmDeployed vm) {
        Collection<VmDeployed> vms = new ArrayList<>();
        vms.add(vm);
        /**
         * Write the data to the database, for later retrieval when the VM boots
         * and is discovered via the data source adaptor.
         */
        database.setVms(vms);
        database.setVMProfileData(vm);
    }
    
    /**
     * This gets the total current power consumption of all VMs that are known
     * to the energy modeller.
     * @return The total power consumption allocated to all known Vms.
     */
    public double getVmTotalCurrentPowerConsumption() {
        double answer = 0;
        HashSet<CurrentUsageRecord> vmData = getCurrentEnergyForVM(datasource.getVmList());
        for (CurrentUsageRecord current : vmData) {
                answer = answer + current.getPower();
        }        
        return answer;
    }
    
    /**
     * This gets the total current power consumption of all physical hosts that 
     * are known to the energy modeller. This is a notion of how efficient the
     * cloud data centre is currently. It is sensitive to physical hosts that 
     * have no VMs but are still powered.
     * @return The total power consumption of all physical hosts.
     */
     public double getHostsTotalCurrentPowerConsumption() {
        double answer = 0;
        HashSet<CurrentUsageRecord> hostData = getCurrentEnergyForHost(datasource.getHostList());
        for (CurrentUsageRecord current : hostData) {
                answer = answer + current.getPower();
        }        
        return answer;
    }   
    
    /**
     * This provides the current fraction VM power consumption is of the overall 
     * host power consumption.
     * @return The total VM power consumption / total physical power consumption.
     */
    public double getVMToHostPowerRatio() {
        return getVmTotalCurrentPowerConsumption() / getHostsTotalCurrentPowerConsumption();
    }

    /**
     * This provides the current amount of host power that has not been allocated
     * to a running VM.
     * @return The total physical host power consumption - total VM allocated 
     * power consumption.
     */
    public double getHostPowerUnallocatedToVMs() {
        return getHostsTotalCurrentPowerConsumption() - getVmTotalCurrentPowerConsumption();
    }    
    
    /**
     * This calibrates all hosts that are known to the energy modeller, that
     * currently are uncalibrated i.e. energy values for the host is unknown.
     */
    private void calibrateAllHostsWithoutData() {
        for (Host host : dataGatherer.getHostList().values()) {
            if (!host.isCalibrated()) {
                host = database.getHostCalibrationData(host);
            }
            if (!host.isCalibrated()) {
                calibrateModelForHost(host);
            }
        }
    }

    /**
     * This makes the energy model calibrate itself for several hosts. This
     * should only need to be done once per host, when it is first introduced to
     * the system.
     *
     * @param hosts The hosts to calibrate
     */
    public void calibrateModelForHost(Collection<Host> hosts) {
        for (Host host : hosts) {
            calibrateModelForHost(host);
        }
    }

    /**
     * This makes the energy model calibrate itself for several hosts. This
     * should only need to be done once per host, when it is first introduced to
     * the system.
     *
     * @param host The host to calibrate
     */
    public void calibrateModelForHost(Host host) {
        calibrator.calibrateHostEnergyData(host);
    }

    /**
     * This permanently stops the energy modeller from running, closing threads
     * and ensuring it no longer consumes resources.
     */
    public void stop() {
        calibrator.stop();
        dataGatherer.stop();
        database.closeConnection();
    }

}
