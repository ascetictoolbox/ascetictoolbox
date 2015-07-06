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
package eu.ascetic.asceticarchitecture.iaas.energymodeller.energypredictor;

import eu.ascetic.asceticarchitecture.iaas.energymodeller.datastore.DatabaseConnector;
import eu.ascetic.asceticarchitecture.iaas.energymodeller.datastore.DefaultDatabaseConnector;
import eu.ascetic.asceticarchitecture.iaas.energymodeller.energypredictor.vmenergyshare.DefaultEnergyShareRule;
import eu.ascetic.asceticarchitecture.iaas.energymodeller.energypredictor.vmenergyshare.EnergyDivision;
import eu.ascetic.asceticarchitecture.iaas.energymodeller.energypredictor.vmenergyshare.EnergyShareRule;
import eu.ascetic.asceticarchitecture.iaas.energymodeller.energypredictor.workloadpredictor.CpuRecentHistoryWorkloadPredictor;
import eu.ascetic.asceticarchitecture.iaas.energymodeller.energypredictor.workloadpredictor.WorkloadEstimator;
import eu.ascetic.asceticarchitecture.iaas.energymodeller.queryinterface.datasourceclient.HostDataSource;
import eu.ascetic.asceticarchitecture.iaas.energymodeller.queryinterface.datasourceclient.WattsUpMeterDataSourceAdaptor;
import eu.ascetic.asceticarchitecture.iaas.energymodeller.queryinterface.datasourceclient.ZabbixDataSourceAdaptor;
import eu.ascetic.asceticarchitecture.iaas.energymodeller.types.TimePeriod;
import eu.ascetic.asceticarchitecture.iaas.energymodeller.types.energyuser.Host;
import eu.ascetic.asceticarchitecture.iaas.energymodeller.types.energyuser.VM;
import eu.ascetic.asceticarchitecture.iaas.energymodeller.types.usage.EnergyUsagePrediction;
import java.io.File;
import java.util.Collection;
import java.util.GregorianCalendar;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.configuration.PropertiesConfiguration;

/**
 * This implements the default and utility functions for an energy predictor. It
 * is expected that any energy predictor loaded into the ASCETiC architecture,
 * will override this class.
 *
 * @author Richard Kavanagh
 */
public abstract class AbstractEnergyPredictor implements EnergyPredictorInterface {

    private static final String CONFIG_FILE = "energy-modeller-predictor.properties";
    private static final String DEFAULT_DATA_SOURCE_PACKAGE = "eu.ascetic.asceticarchitecture.iaas.energymodeller.queryinterface.datasourceclient";
    private static final String DEFAULT_WORKLOAD_PREDICTOR_PACKAGE = "eu.ascetic.asceticarchitecture.iaas.energymodeller.energypredictor.workloadpredictor";
    private double defaultAssumedCpuUsage = 0.6; //assumed 60 percent usage, by default
    private HostDataSource source = null;
    private DatabaseConnector database = null;
    private boolean considerIdleEnergy = true;
    private WorkloadEstimator workloadEstimator = null;

    private EnergyShareRule energyShareRule = new DefaultEnergyShareRule();
    private static final String DEFAULT_ENERGY_SHARE_RULE_PACKAGE
            = "eu.ascetic.asceticarchitecture.iaas.energymodeller.energypredictor.vmenergyshare";

    /**
     * This creates a new abstract energy predictor.
     *
     * It will create a energy-modeller-predictor properties file if it doesn't
     * exist.
     *
     * The main property: iaas.energy.modeller.cpu.energy.predictor.default_load
     * should be in the range 0..1 or -1. This indicates the predictor's default
     * assumption on how much load is been induced. -1 measures the CPU's
     * current load and uses that to forecast into the future.
     *
     * In the case of using -1 as a parameter to additional parameters are used:
     * iaas.energy.modeller.cpu.energy.predictor.utilisation.observe_time.sec
     * iaas.energy.modeller.cpu.energy.predictor.utilisation.observe_time.min
     *
     * These indicate the window of how long the CPU should be monitored for, to
     * determine the current load.
     */
    public AbstractEnergyPredictor() {
        try {
            PropertiesConfiguration config;
            if (new File(CONFIG_FILE).exists()) {
                config = new PropertiesConfiguration(CONFIG_FILE);
            } else {
                config = new PropertiesConfiguration();
                config.setFile(new File(CONFIG_FILE));
            }
            config.setAutoSave(true); //This will save the configuration file back to disk. In case the defaults need setting.
            defaultAssumedCpuUsage = config.getDouble("iaas.energy.modeller.cpu.energy.predictor.default_load", defaultAssumedCpuUsage);
            String shareRule = config.getString("iaas.energy.modeller.cpu.energy.predictor.vm_share_rule", "DefaultEnergyShareRule");
            setEnergyShareRule(shareRule);
            considerIdleEnergy = config.getBoolean("iaas.energy.modeller.cpu.energy.predictor.consider_idle_energy", considerIdleEnergy);
            config.setProperty("iaas.energy.modeller.cpu.energy.predictor.default_load", defaultAssumedCpuUsage);
            if (defaultAssumedCpuUsage == -1) {
                String dataSrcStr = config.getString("iaas.energy.modeller.cpu.energy.predictor.datasource", "ZabbixDataSourceAdaptor");
                config.setProperty("iaas.energy.modeller.cpu.energy.predictor.datasource", dataSrcStr);
                setDataSource(dataSrcStr);
            }
            String workloadPredictorStr = config.getString("iaas.energy.modeller.cpu.energy.predictor.workload", "CpuRecentHistoryWorkloadPredictor");
            config.setProperty("iaas.energy.modeller.cpu.energy.predictor.workload", workloadPredictorStr);
            setWorkloadPredictor(workloadPredictorStr);
        } catch (ConfigurationException ex) {
            Logger.getLogger(CpuOnlyEnergyPredictor.class.getName()).log(Level.SEVERE,
                    "Taking the default load from the settings file did not work", ex);
        }
    }

    /**
     * This allows the energy predictor's data source to be set
     *
     * @param dataSource The name of the data source to use for this energy
     * predictor
     */
    private void setDataSource(String dataSource) {
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
                source = WattsUpMeterDataSourceAdaptor.getInstance();
            } else {
                source = (HostDataSource) (Class.forName(dataSource).newInstance());
            }
        } catch (ClassNotFoundException ex) {
            if (source == null) {
                source = new ZabbixDataSourceAdaptor();
            }
            Logger.getLogger(AbstractEnergyPredictor.class.getName()).log(Level.WARNING, "The data source specified was not found", ex);
        } catch (InstantiationException | IllegalAccessException ex) {
            if (source == null) {
                source = new ZabbixDataSourceAdaptor();
            }
            Logger.getLogger(AbstractEnergyPredictor.class.getName()).log(Level.WARNING, "The data source did not work", ex);
        }
    }

    /**
     * This allows the energy predictor's workload predictor to be set
     *
     * @param workloadPredictor The name of the workload predictor to use for
     * this energy predictor
     */
    private void setWorkloadPredictor(String workloadPredictor) {
        try {
            if (!workloadPredictor.startsWith(DEFAULT_WORKLOAD_PREDICTOR_PACKAGE)) {
                workloadPredictor = DEFAULT_WORKLOAD_PREDICTOR_PACKAGE + "." + workloadPredictor;
            }
            workloadEstimator = (WorkloadEstimator) (Class.forName(workloadPredictor).newInstance());
            workloadEstimator.setDataSource(source);
        } catch (ClassNotFoundException ex) {
            if (workloadEstimator == null) {
                workloadEstimator = new CpuRecentHistoryWorkloadPredictor();
                workloadEstimator.setDataSource(source);
            }
            Logger.getLogger(AbstractEnergyPredictor.class.getName()).log(Level.WARNING, "The workload predictor specified was not found", ex);
        } catch (InstantiationException | IllegalAccessException ex) {
            if (workloadEstimator == null) {
                workloadEstimator = new CpuRecentHistoryWorkloadPredictor();
                workloadEstimator.setDataSource(source);
            }
            Logger.getLogger(AbstractEnergyPredictor.class.getName()).log(Level.WARNING, "The workload predictor did not work", ex);
        }
        //Set the workload estimators database if it requires one.
        if (workloadEstimator.requiresVMInformation()) {
            database = new DefaultDatabaseConnector();
        } else {
            if (database != null) {
                database.closeConnection();
                database = null;
            }
        }
    }

    /**
     * This uses the current energy share rule for the energy predictor allowing
     * for the translation between host energy usage and VMs energy usage.
     *
     * @param host The host to analyse
     * @param vms The VMs that are on/to be on the host
     * @return The fraction of energy or used per host.
     */
    public EnergyDivision getEnergyUsage(Host host, Collection<VM> vms) {
        return energyShareRule.getEnergyUsage(host, vms);
    }

    /**
     * This returns the current energy share rule that is in use by the energy
     * predictor.
     *
     * @return the energyShareRule The rule that divides the energy usage of
     * hosts into each VM.
     */
    public EnergyShareRule getEnergyShareRule() {
        return energyShareRule;
    }

    /**
     * This sets the current energy share rule that is in use by the energy
     * predictor.
     *
     * @param energyShareRule The rule that divides the energy usage of hosts
     * into each VM.
     */
    public final void setEnergyShareRule(EnergyShareRule energyShareRule) {
        this.energyShareRule = energyShareRule;
    }

    /**
     * This sets the current energy share rule that is in use by the energy
     * predictor.
     *
     * @param energyShareRule The rule that divides the energy usage of hosts
     * into each VM.
     */
    public final void setEnergyShareRule(String energyShareRule) {
        try {
            if (!energyShareRule.startsWith(DEFAULT_ENERGY_SHARE_RULE_PACKAGE)) {
                energyShareRule = DEFAULT_ENERGY_SHARE_RULE_PACKAGE + "." + energyShareRule;
            }
            this.energyShareRule = (EnergyShareRule) (Class.forName(energyShareRule).newInstance());
        } catch (IllegalAccessException | InstantiationException | ClassNotFoundException ex) {
            if (energyShareRule == null) {
                this.energyShareRule = new DefaultEnergyShareRule();
            }
            Logger.getLogger(AbstractEnergyPredictor.class.getName()).log(Level.WARNING, "The energy share rule specified was not found", ex);
        }
    }

    /**
     * This indicates if this energy predictor when estimating VM energy usage
     * should consider idle energy or not.
     *
     * @return If idle energy is been considered when allocating energy to VMs
     */
    public boolean isConsiderIdleEnergy() {
        return considerIdleEnergy;
    }

    /**
     * This sets if this energy predictor when estimating VM energy usage should
     * consider idle energy or not.
     *
     * @param considerIdleEnergy If idle energy is been considered when
     * allocating energy to VMs
     */
    public void setConsiderIdleEnergy(boolean considerIdleEnergy) {
        this.considerIdleEnergy = considerIdleEnergy;
    }

    /**
     * This returns the default amount of CPU utilisation that is assumed, if an
     * estimation mechanism is not utilised.
     *
     * @return the default amount of CPU utilisation to be used during energy
     * estimation.
     */
    public double getDefaultAssumedCpuUsage() {
        return defaultAssumedCpuUsage;
    }

    /**
     * This sets the default amount of CPU utilisation that is assumed, if an
     * estimation mechanism is not utilised.
     *
     * @param usageCPU the default amount of CPU utilisation to be used during
     * energy estimation.
     */
    public void setDefaultAssumedCpuUsage(double usageCPU) {
        this.defaultAssumedCpuUsage = usageCPU;
    }

    /**
     * This provides a prediction of how much energy is to be used by a VM, over
     * the next hour.
     *
     * @param vm The vm to be deployed
     * @param virtualMachines The virtual machines giving a workload on the host
     * machine
     * @param host The host that the VMs will be running on
     * @return The prediction of the energy to be used.
     */
    @Override
    public EnergyUsagePrediction getVMPredictedEnergy(VM vm, Collection<VM> virtualMachines, Host host) {
        TimePeriod duration = new TimePeriod(new GregorianCalendar(), TimeUnit.HOURS.toSeconds(1));
        return getVMPredictedEnergy(vm, virtualMachines, host, duration);
    }

    /**
     * This provides a prediction of how much energy is to be used by a host in
     * the next hour.
     *
     * @param host The host to get the energy prediction for
     * @param virtualMachines The virtual machines giving a workload on the host
     * machine
     * @return The prediction of the energy to be used.
     */
    @Override
    public EnergyUsagePrediction getHostPredictedEnergy(Host host, Collection<VM> virtualMachines) {
        TimePeriod duration = new TimePeriod(new GregorianCalendar(), 1, TimeUnit.HOURS);
        return getHostPredictedEnergy(host, virtualMachines, duration);
    }

    /**
     * This for a set of VMs provides the amount of memory allocated in Mb.
     *
     * @param virtualMachines The VMs to get the memory used.
     * @return The amount of memory allocated to VMs in Mb.
     */
    public static int getAlloacatedMemory(Collection<VM> virtualMachines) {
        int answer = 0;
        for (VM vm : virtualMachines) {
            answer = answer + vm.getRamMb();
        }
        return answer;
    }

    /**
     * This for a set of VMs provides the amount of memory allocated in Mb.
     *
     * @param virtualMachines The VMs to get the memory used.
     * @return The amount of memory allocated to VMs in Mb.
     */
    public static int getAlloacatedCpus(Collection<VM> virtualMachines) {
        int answer = 0;
        for (VM vm : virtualMachines) {
            answer = answer + vm.getCpus();
        }
        return answer;
    }

    /**
     * This for a set of VMs provides the amount of memory allocated in Mb.
     *
     * @param virtualMachines The VMs to get the memory used.
     * @return The amount of memory allocated to VMs in Mb.
     */
    public static double getAlloacatedDiskSpace(Collection<VM> virtualMachines) {
        double answer = 0;
        for (VM vm : virtualMachines) {
            answer = answer + vm.getDiskGb();
        }
        return answer;
    }

    /**
     * This provides an average of the recent CPU utilisation for a given host,
     * based upon the CPU utilisation time window set for the energy predictor.
     *
     * @param host The host for which the average CPU utilisation over the last
     * n seconds will be calculated for.
     * @return The average recent CPU utilisation based upon the energy
     * predictor's configured observation window.
     */
    protected double getCpuUtilisation(Host host) {
        return workloadEstimator.getCpuUtilisation(host, null);
    }    
    
    /**
     * This provides an average of the recent CPU utilisation for a given host,
     * based upon the CPU utilisation time window set for the energy predictor.
     *
     * @param host The host for which the average CPU utilisation over the last
     * n seconds will be calculated for.
     * @param virtualMachines
     * @return The average recent CPU utilisation based upon the energy
     * predictor's configured observation window.
     */
    protected double getCpuUtilisation(Host host, Collection<VM> virtualMachines) {
        if (workloadEstimator.requiresVMInformation()) {
            return workloadEstimator.getCpuUtilisation(host, virtualMachines);
        } else {
            return workloadEstimator.getCpuUtilisation(host, null);
        }
    }

    /**
     * This is a method that picks the best predictor for a host based upon the
     * root mean square error produced by the predictor.
     *
     * @param host The host the predictors should be assessed against.
     * @param predictors The collection of predictors to assess.
     * @return The predictor with the least calibration error.
     */
    public static EnergyPredictorInterface getBestPredictor(Host host, Collection<EnergyPredictorInterface> predictors) {
        EnergyPredictorInterface answer = null;
        for (EnergyPredictorInterface predictor : predictors) {
            if (answer == null || predictor.getRootMeanSquareError(host) < answer.getRootMeanSquareError(host)) {
                answer = predictor;
            }
        }
        return answer;
    }

    /**
     * TODO Add utility functions here that may be used by the energy models
     * that are created over the time of the project.
     */
    /**
     * The predictor function class represents a wrap around of a predictor and
     * its estimated error.
     *
     * @param <T> The type of the object that is to be used to generate the
     * prediction.
     */
    public class PredictorFunction<T> {

        T function;
        double sumOfSquareError;
        double rootMeanSquareError;

        /**
         * This creates a new instance of a prediction function.
         *
         * @param function The function that the predictor is to use to estimate
         * power/energy consumption.
         * @param sumOfSquareError The sum of the square error for the
         * prediction function.
         * @param rootMeanSquareError The root mean square error for the
         * prediction function.
         */
        public PredictorFunction(T function, double sumOfSquareError, double rootMeanSquareError) {
            this.function = function;
            this.sumOfSquareError = sumOfSquareError;
            this.rootMeanSquareError = rootMeanSquareError;
        }

        /**
         * This returns the object that provides the prediction function
         *
         * @return The function that the predictor is to use to estimate
         * power/energy consumption.
         */
        public T getFunction() {
            return function;
        }

        /**
         * This returns the sum of the square error for the prediction function.
         *
         * @return The sum of the square error for the prediction function.
         */
        public double getSumOfSquareError() {
            return sumOfSquareError;
        }

        /**
         * This returns the sum of the room mean error for the prediction
         * function.
         *
         * @return The sum of the square error for the prediction function.
         */
        public double getRootMeanSquareError() {
            return rootMeanSquareError;
        }
    }

}
