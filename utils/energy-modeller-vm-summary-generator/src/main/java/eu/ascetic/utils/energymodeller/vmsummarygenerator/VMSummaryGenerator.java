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
package eu.ascetic.utils.energymodeller.vmsummarygenerator;

import eu.ascetic.asceticarchitecture.iaas.energymodeller.EnergyModeller;
import eu.ascetic.asceticarchitecture.iaas.energymodeller.datastore.DatabaseConnector;
import eu.ascetic.asceticarchitecture.iaas.energymodeller.datastore.DefaultDatabaseConnector;
import eu.ascetic.asceticarchitecture.iaas.energymodeller.energypredictor.vmenergyshare.HistoricLoadBasedDivision;
import eu.ascetic.asceticarchitecture.iaas.energymodeller.energypredictor.vmenergyshare.LoadBasedDivision;
import eu.ascetic.asceticarchitecture.iaas.energymodeller.energypredictor.vmenergyshare.LoadBasedDivisionWithIdleEnergy;
import eu.ascetic.asceticarchitecture.iaas.energymodeller.queryinterface.datasourceclient.FastDataSourceAdaptor;
import eu.ascetic.asceticarchitecture.iaas.energymodeller.queryinterface.datasourceclient.HostDataSource;
import eu.ascetic.asceticarchitecture.iaas.energymodeller.types.TimePeriod;
import eu.ascetic.asceticarchitecture.iaas.energymodeller.types.energyuser.Host;
import eu.ascetic.asceticarchitecture.iaas.energymodeller.types.energyuser.VM;
import eu.ascetic.asceticarchitecture.iaas.energymodeller.types.energyuser.VmDeployed;
import eu.ascetic.asceticarchitecture.iaas.energymodeller.types.energyuser.usage.HostVmLoadFraction;
import eu.ascetic.asceticarchitecture.iaas.energymodeller.types.usage.HistoricUsageRecord;
import eu.ascetic.asceticarchitecture.iaas.energymodeller.types.usage.HostEnergyRecord;
import eu.ascetic.ioutils.ResultsStore;
import eu.ascetic.ioutils.Settings;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashSet;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * The role of this application is to log out to disk a summary of a VMs energy 
 * usage. This includes data such as: 
 * it's average power usage 
 * and it's total energy consumption.
 * @author Richard
 */
public class VMSummaryGenerator {

    private final HostDataSource datasource;
    private final DatabaseConnector database;
    private final Class<?> historicEnergyDivisionMethod = LoadBasedDivisionWithIdleEnergy.class;
    private ResultsStore vmList;
    private final HashSet<VmDeployed> vmsInExperiment = new HashSet<>();
    private static final String PROPERTIES_FILE = "energymodeller_summary_generator.properties";
    private static TimePeriod period = null;

    /**
     * This creates an instance of the VM Summary Generator
     */
    public VMSummaryGenerator() {
        datasource = new FastDataSourceAdaptor();
        database = new DefaultDatabaseConnector();
    }

    /**
     * This method takes a list of VM id's and VM names and adds them into the list
     * of VMs to gather data for. The file vm_experimental_list.csv is the 
     * data source for this information.
     */
    private void getVMsInExperiment() {
        vmList = new ResultsStore();
        vmList.setResultsFile("vm_experimental_list.csv");
        vmList.load();
        for (int i = 0; i < vmList.size(); i++) {
            try {
                VmDeployed vm = new VmDeployed(Integer.valueOf(vmList.getElement(i, 0)), vmList.getElement(i, 1));
                vm.setAllocatedTo(getHost(vm));
                vmsInExperiment.add(vm);
            } catch (NumberFormatException ex) {

            }
        }
    }

    /**
     * This gets the energy and power consumption summary values for a named set
     * of VMs.
     * @param args 
     * @throws java.text.ParseException In cases where values cannot be read
     * correctly from the configuration file.
     */
    public static void main(String[] args) throws ParseException {
        DateFormat format = new SimpleDateFormat("E, dd MMM yyyy hh:mm:ss Z");
        Settings settings = new Settings(PROPERTIES_FILE);
        //Sets the duration of the experimental data capture, default = 1 hour
        int durationSeconds = settings.getInt("duration_seconds", (int) TimeUnit.HOURS.toSeconds(1));
        GregorianCalendar defaultStart = new GregorianCalendar();
        long durationMills = TimeUnit.HOURS.toMillis(1);
        defaultStart.setTimeInMillis(defaultStart.getTimeInMillis() - durationMills);
        //Sets the start of the experimental data capture, default = now - 1 hour
        String dateString = settings.getString("start_time", format.format(defaultStart.getTime()));
        Date parsed = format.parse(dateString);
        Calendar cal = Calendar.getInstance();
        cal.setTime(parsed);
        period = new TimePeriod(cal, durationSeconds);
        if (settings.isChanged()) {
            settings.save(PROPERTIES_FILE);
        }
        VMSummaryGenerator app = new VMSummaryGenerator();
        app.getVMSummaryData();
    }    

    /**
     * This iterates through the list of VMs and obtains summary values for each
     * VM named in the list.
     */
    private void getVMSummaryData() {
        getVMsInExperiment();
        for (VmDeployed vm : vmsInExperiment) {
            HistoricUsageRecord answer = getEnergyRecordForVM(vm, period);
            System.out.println(vm.getId() + ", " +  
                    vm.getName() + ", " + 
                    answer.getAvgPowerUsed() + ", " + 
                    answer.getTotalEnergyUsed() + ", " + 
                    answer.getDuration().getDuration());
        }
    }

    /**
     * This takes a host name and provides the object representation of this
     * host.
     *
     * @param vmName The host name to get the host object for
     * @return The host for the specified host name.
     */
    public VmDeployed getVM(String vmName) {
        Collection<VmDeployed> vms = database.getVms();
        for (VmDeployed vm : vms) {
            if (vm.getName().equalsIgnoreCase(vmName)) {
                vm.setAllocatedTo(getHost(vm));
                return vm;
            }
        }
        return null;
    }

    /**
     * This takes a VM and provides the object representation of the host 
     * associated with it. If vm.getAllocatedTo() is not set then will return
     * the host that is associated with the VM anyway, in cases where the 
     * host name is the last part of the VM's name i.e. "_<hostname>".
     * host.
     * @param vm The vm to return the physical host object for
     * @return The physical host belonging to the VM.
     */
    public Host getHost(VmDeployed vm) {
        if (vm.getAllocatedTo() != null) {
            return vm.getAllocatedTo();
        }
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
     * This takes a host name and provides the object representation of this
     * host.
     *
     * @param hostname The host name to get the host object for
     * @return The host for the specified host name.
     */
    public Host getHost(String hostname) {
        Host host = datasource.getHostByName(hostname);
        host = database.getHostCalibrationData(host);
        return host;
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
        answer.setAvgPowerUsed(totalEnergy / (((double) shareRule.getDuration(vm)) / 3600));
        answer.setDuration(new TimePeriod(shareRule.getStart(), shareRule.getEnd()));
        return answer;
    }
}
