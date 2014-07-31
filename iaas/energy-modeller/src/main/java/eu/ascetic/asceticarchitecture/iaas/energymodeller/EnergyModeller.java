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
import eu.ascetic.asceticarchitecture.iaas.energymodeller.energypredictor.DefaultEnergyPredictor;
import eu.ascetic.asceticarchitecture.iaas.energymodeller.energypredictor.EnergyPredictorInterface;
import eu.ascetic.asceticarchitecture.iaas.energymodeller.energypredictor.vmenergyshare.EnergyDivision;
import eu.ascetic.asceticarchitecture.iaas.energymodeller.energypredictor.vmenergyshare.LoadBasedDivision;
import eu.ascetic.asceticarchitecture.iaas.energymodeller.energypredictor.vmenergyshare.LoadFractionShareRule;
import eu.ascetic.asceticarchitecture.iaas.energymodeller.queryinterface.datasourceclient.HostDataSource;
import eu.ascetic.asceticarchitecture.iaas.energymodeller.queryinterface.datasourceclient.ZabbixDataSourceAdaptor;
import eu.ascetic.asceticarchitecture.iaas.energymodeller.types.TimePeriod;
import eu.ascetic.asceticarchitecture.iaas.energymodeller.types.energyuser.Host;
import eu.ascetic.asceticarchitecture.iaas.energymodeller.types.energyuser.VM;
import eu.ascetic.asceticarchitecture.iaas.energymodeller.types.energyuser.VmDeployed;
import eu.ascetic.asceticarchitecture.iaas.energymodeller.types.energyuser.usage.HostVmLoadFraction;
import eu.ascetic.asceticarchitecture.iaas.energymodeller.types.usage.CurrentUsageRecord;
import eu.ascetic.asceticarchitecture.iaas.energymodeller.types.usage.EnergyUsagePrediction;
import eu.ascetic.asceticarchitecture.iaas.energymodeller.types.usage.HistoricUsageRecord;
import eu.ascetic.asceticarchitecture.iaas.energymodeller.types.usage.HostEnergyRecord;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
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
 * @author Richard
 */
public class EnergyModeller {

    private static final String DEFAULT_PREDICTOR_PACKAGE = "eu.ascetic.asceticarchitecture.iaas.energymodeller.energypredictor";
    private static final String DEFAULT_DATA_SOURCE_PACKAGE = "eu.ascetic.asceticarchitecture.iaas.energymodeller.queryinterface.datasourceclient";
    private EnergyPredictorInterface predictor = new DefaultEnergyPredictor();
    private HostDataSource datasource = new ZabbixDataSourceAdaptor();
    private DatabaseConnector database = new DefaultDatabaseConnector();
    private Calibrator calibrator = new Calibrator(datasource);
    private Thread calibratorThread;
    private DataGatherer dataGatherer = new DataGatherer(datasource, new DefaultDatabaseConnector(), calibrator);
    private Thread dataGatherThread;
    
    /**
     * This creates a new energy modeller.
     */
    @SuppressWarnings("CallToThreadStartDuringObjectConstruction")
    public EnergyModeller() {
        try {
            calibratorThread = new Thread(calibrator);
            calibratorThread.setDaemon(true);
            calibratorThread.start();
            calibrateAllHostsWithoutData();
            dataGatherThread = new Thread(dataGatherer);
            dataGatherThread.setDaemon(true);
            dataGatherThread.start();
        } catch (Exception ex) {
            Logger.getLogger(EnergyModeller.class.getName()).log(Level.WARNING, "The host list was not populated");
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
                predictor = new DefaultEnergyPredictor();
            }
            Logger.getLogger(EnergyModeller.class.getName()).log(Level.WARNING, "The scheduling algorithm specified was not found");
        } catch (InstantiationException | IllegalAccessException ex) {
            if (predictor == null) {
                predictor = new DefaultEnergyPredictor();
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
            datasource = (HostDataSource) (Class.forName(dataSource).newInstance());
        } catch (ClassNotFoundException ex) {
            if (datasource == null) {
                datasource = new ZabbixDataSourceAdaptor();
            }
            Logger.getLogger(EnergyModeller.class.getName()).log(Level.WARNING, "The data source specified was not found");
        } catch (InstantiationException | IllegalAccessException ex) {
            if (datasource == null) {
                datasource = new ZabbixDataSourceAdaptor();
            }
            Logger.getLogger(EnergyModeller.class.getName()).log(Level.WARNING, "The data source did not work", ex);
        }
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
     * This returns the energy usage for a named virtual machine.
     *
     * @param vm A reference to the VM
     * @return
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
        CurrentUsageRecord answer = new CurrentUsageRecord(vm);
        answer.setTime(hostAnswer.getTime());
        answer.setPower(divider.getEnergyUsage(hostAnswer.getPower(), vm));
        return answer;
    }

    /**
     * This returns the energy usage for a named virtual machine.
     *
     * @param vm A reference to the VM
     * @param timePeriod The time period for which the query applies.
     * @return
     *
     * Historic Values: Avg Watts over time Avg Current (useful??) Avg Voltage
     * (useful??) kWh of energy used since instantiation
     */
    public HistoricUsageRecord getEnergyRecordForVM(VmDeployed vm, TimePeriod timePeriod) {
        HistoricUsageRecord answer = new HistoricUsageRecord(vm);
        Host host = vm.getAllocatedTo();
        ArrayList<VmDeployed> otherVms = dataGatherer.getVMsOnHost(host);
        ArrayList<VM> vms = new ArrayList<>();
        vms.addAll(otherVms);
        List<HostEnergyRecord> hostsData = database.getHostHistoryData(host, timePeriod);
        List<HostVmLoadFraction> loadFractionData = (List<HostVmLoadFraction>) database.getHostVmHistoryLoadData(host, timePeriod);
        //Fraction off energy used based upon this.
        LoadBasedDivision shareRule = new LoadBasedDivision(host);
        shareRule.addVM(vms);
        shareRule.setEnergyUsage(hostsData);
        shareRule.setLoadFraction(loadFractionData);
        double totalEnergy = shareRule.getEnergyUsage(vm);
        answer.setTotalEnergyUsed(totalEnergy);
        answer.setAvgPowerUsed(totalEnergy / ((double) TimeUnit.SECONDS.toHours(shareRule.getDuration())));
        answer.setDuration(new TimePeriod(shareRule.getStart(), shareRule.getEnd()));
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
        HistoricUsageRecord answer = new HistoricUsageRecord(host, data);
        return answer;
    }

    /**
     * This returns the energy usage for a named physical machine.
     *
     * @param host A reference to physical machine
     * @return
     *
     * Current Values for: Power (Watts), current and voltage
     */
    public CurrentUsageRecord getCurrentEnergyForHost(Host host) {
        CurrentUsageRecord answer = datasource.getCurrentEnergyUsage(host);
        return answer;
    }

    /**
     * This provides the amount of energy predicted to be used by the placement
     * of a VM.
     *
     * @param vmImage The VM that is to be deployed
     * @param vMsOnHost The VMs that are already on the host
     * @param host The host on which the VM is to be placed
     * @return
     *
     * Avg Watts that is expected to use over time by the VM Predicted energy
     * used (kWh) during life of VM
     */
    public EnergyUsagePrediction getPredictedEnergyForVM(VM vmImage, Collection<VM> vMsOnHost, Host host) {
        /**
         * Adding a sanity check that ensures the vmImage (deployed or otherwise, represents load
         * on the VM.
         */
        if (!vMsOnHost.contains(vmImage)) {
            vMsOnHost.add(vmImage);
        }
        return predictor.getVMPredictedEnergy(vmImage, vMsOnHost, host);
    }

    /**
     * This provides the amount of energy predicted to be used by a given host.
     *
     * @param host The host that the energy prediction is for
     * @param virtualMachines The VMs that are on the host.
     * @return
     */
    public EnergyUsagePrediction getHostPredictedEnergy(Host host, Collection<VM> virtualMachines) {
        return predictor.getHostPredictedEnergy(host, virtualMachines);
    }

    //Use a program called stress, benchmarking tool to test this
    /**
     * This takes a set of hostname names and provides the object representation
     * of this host.
     *
     * @param hostname The hostname
     * @return
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
     * @param hostname
     * @return
     */
    public Host getHost(String hostname) {
        if (dataGatherer.getHostList().containsKey(hostname)) {
            return dataGatherer.getHost(hostname);
        } else {
            dataGatherer.populateHostList();
            if (dataGatherer.getHostList().containsKey(hostname)) {
                return dataGatherer.getHostList().get(hostname);
            }
        }
        return null;
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
     * This given a name of a VM provides the object representation of it
     *
     * @param name The name of the VM
     * @return The VM with the specified name, null if not known to the energy
     * modeller.
     */
    public VmDeployed getVM(String name) {
        if (dataGatherer.getVmList().containsKey(name)) {
            return dataGatherer.getVmList().get(name);
        } else {
            dataGatherer.populateHostList();
            if (dataGatherer.getVmList().containsKey(name)) {
                return dataGatherer.getVm(name);
            }
            return null;
        }
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
                database.setHostCalibrationData(host);
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
        database.setHostCalibrationData(host);
    }

}
