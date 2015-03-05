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

import eu.ascetic.asceticarchitecture.iaas.energymodeller.energypredictor.vmenergyshare.EnergyDivision;
import eu.ascetic.asceticarchitecture.iaas.energymodeller.queryinterface.datasourceclient.HostDataSource;
import eu.ascetic.asceticarchitecture.iaas.energymodeller.queryinterface.datasourceclient.WattsUpMeterDataSourceAdaptor;
import eu.ascetic.asceticarchitecture.iaas.energymodeller.queryinterface.datasourceclient.ZabbixDataSourceAdaptor;
import eu.ascetic.asceticarchitecture.iaas.energymodeller.types.TimePeriod;
import eu.ascetic.asceticarchitecture.iaas.energymodeller.types.energymodel.LinearFunction;
import eu.ascetic.asceticarchitecture.iaas.energymodeller.types.energyuser.Host;
import eu.ascetic.asceticarchitecture.iaas.energymodeller.types.energyuser.VM;
import eu.ascetic.asceticarchitecture.iaas.energymodeller.types.energyuser.usage.HostEnergyCalibrationData;
import eu.ascetic.asceticarchitecture.iaas.energymodeller.types.usage.EnergyUsagePrediction;
import eu.ascetic.ioutils.caching.LRUCache;
import java.io.File;
import java.util.Collection;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.configuration.PropertiesConfiguration;
import org.apache.commons.math3.stat.regression.SimpleRegression;

/**
 * This implements the CPU only energy predictor for the ASCETiC project.
 *
 * It performs simple linear regression in order to determine from the CPU load
 * the current power consumption.
 *
 * @author Richard Kavanagh
 *
 */
public class CpuOnlyEnergyPredictor extends AbstractEnergyPredictor {

    private static final String CONFIG_FILE = "energymodeller_cpu_predictor.properties";
    private static final String DEFAULT_DATA_SOURCE_PACKAGE = "eu.ascetic.asceticarchitecture.iaas.energymodeller.queryinterface.datasourceclient";
    private double usageCPU = 0.6; //assumed 60 percent usage, by default
    private HostDataSource source = null;
    private int cpuUtilObservationTimeMin = 15;
    private int cpuUtilObservationTimeSec = 0;
    private int cpuUtilObservationTimeSecTotal = 0;
    private boolean considerIdleEnergy = true;
    private final LRUCache<Host, PredictorFunction<LinearFunction>> modelCache = new LRUCache<>(5, 50);

    /**
     * This creates a new CPU only energy predictor.
     *
     * It will create a energymodeller_CPU_predictor properties file if it
     * doesn't exist.
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
    public CpuOnlyEnergyPredictor() {
        try {
            PropertiesConfiguration config;
            if (new File(CONFIG_FILE).exists()) {
                config = new PropertiesConfiguration(CONFIG_FILE);
            } else {
                config = new PropertiesConfiguration();
                config.setFile(new File(CONFIG_FILE));
            }
            config.setAutoSave(true); //This will save the configuration file back to disk. In case the defaults need setting.
            usageCPU = config.getDouble("iaas.energy.modeller.cpu.energy.predictor.default_load", usageCPU);
            String shareRule = config.getString("iaas.energy.modeller.cpu.energy.predictor.vm_share_rule", "DefaultEnergyShareRule");
            setEnergyShareRule(shareRule);
            considerIdleEnergy = config.getBoolean("iaas.energy.modeller.cpu.energy.predictor.consider_idle_energy", considerIdleEnergy);
            config.setProperty("iaas.energy.modeller.cpu.energy.predictor.default_load", usageCPU);
            if (usageCPU == -1) {
                String dataSrcStr = config.getString("iaas.energy.modeller.cpu.energy.predictor.datasource", "ZabbixDataSourceAdaptor");
                config.setProperty("iaas.energy.modeller.cpu.energy.predictor.datasource", dataSrcStr);
                setDataSource(dataSrcStr);
                cpuUtilObservationTimeMin = config.getInt("iaas.energy.modeller.cpu.energy.predictor.utilisation.observe_time.min", cpuUtilObservationTimeMin);
                config.setProperty("iaas.energy.modeller.cpu.energy.predictor.utilisation.observe_time.min", cpuUtilObservationTimeMin);
                cpuUtilObservationTimeSec = config.getInt("iaas.energy.modeller.cpu.energy.predictor.utilisation.observe_time.sec", cpuUtilObservationTimeSec);
                config.setProperty("iaas.energy.modeller.cpu.energy.predictor.utilisation.observe_time.sec", cpuUtilObservationTimeSec);
                cpuUtilObservationTimeSecTotal = cpuUtilObservationTimeSec + (int) TimeUnit.MINUTES.toSeconds(cpuUtilObservationTimeMin);

            }
        } catch (ConfigurationException ex) {
            Logger.getLogger(CpuOnlyEnergyPredictor.class.getName()).log(Level.SEVERE,
                    "Taking the default load from the settings file did not work", ex);
        }
    }

    /**
     * This allows the CPU only energy predictors data source to be set
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
            Logger.getLogger(CpuOnlyEnergyPredictor.class.getName()).log(Level.WARNING, "The data source specified was not found", ex);
        } catch (InstantiationException | IllegalAccessException ex) {
            if (source == null) {
                source = new ZabbixDataSourceAdaptor();
            }
            Logger.getLogger(CpuOnlyEnergyPredictor.class.getName()).log(Level.WARNING, "The data source did not work", ex);
        }
    }

    @Override
    public EnergyUsagePrediction getHostPredictedEnergy(Host host, Collection<VM> virtualMachines, TimePeriod duration) {
        EnergyUsagePrediction wattsUsed;
        if (usageCPU == -1) {
            wattsUsed = predictTotalEnergy(host, source.getCpuUtilisation(host, cpuUtilObservationTimeSecTotal), duration);
        } else {
            wattsUsed = predictTotalEnergy(host, usageCPU, duration);
        }
        return wattsUsed;
    }

    /**
     * This provides a prediction of how much energy is to be used by a VM
     *
     * @param vm The vm to be deployed
     * @param virtualMachines The virtual machines giving a workload on the host
     * machine
     * @param host The host that the VMs will be running on
     * @param timePeriod The time period the query should run for.
     * @return The prediction of the energy to be used.
     */
    @Override
    public EnergyUsagePrediction getVMPredictedEnergy(VM vm, Collection<VM> virtualMachines, Host host, TimePeriod timePeriod) {
        EnergyDivision division = getEnergyUsage(host, virtualMachines);
        EnergyUsagePrediction hostAnswer;
        if (usageCPU == -1) {
            hostAnswer = predictTotalEnergy(host, source.getCpuUtilisation(host, cpuUtilObservationTimeSecTotal), timePeriod);
        } else {
            hostAnswer = predictTotalEnergy(host, usageCPU, timePeriod);
        }
        hostAnswer.setAvgPowerUsed(hostAnswer.getTotalEnergyUsed()
                / ((double) TimeUnit.SECONDS.toHours(timePeriod.getDuration())));
        EnergyUsagePrediction answer = new EnergyUsagePrediction(vm);
        answer.setDuration(hostAnswer.getDuration());
        //Find the fraction to be associated with the VM
        double vmsEnergyFraction = division.getEnergyUsage(hostAnswer.getTotalEnergyUsed(), vm);
        division.setConsiderIdleEnergy(considerIdleEnergy);
        answer.setTotalEnergyUsed(vmsEnergyFraction);
        double vmsPowerFraction = division.getEnergyUsage(hostAnswer.getAvgPowerUsed(), vm);
        answer.setAvgPowerUsed(vmsPowerFraction);
        return answer;
    }

    /**
     * This predicts the total amount of energy used by a host.
     *
     * @param host The host to get the energy prediction for
     * @param usageCPU The amount of CPU load placed on the host
     * @param timePeriod The time period the prediction is for
     * @return The predicted energy usage.
     */
    public EnergyUsagePrediction predictTotalEnergy(Host host, double usageCPU, TimePeriod timePeriod) {
        EnergyUsagePrediction answer = new EnergyUsagePrediction(host);
        LinearFunction model = retrieveModel(host).getFunction();
        double powerUsed = model.value(usageCPU);
        answer.setAvgPowerUsed(powerUsed);
        answer.setTotalEnergyUsed(powerUsed * ((double) TimeUnit.SECONDS.toHours(timePeriod.getDuration())));
        answer.setDuration(timePeriod);
        return answer;
    }

    /**
     * This estimates the power used by a host, given its CPU load. The CPU load
     * value is determined from the settings file.
     *
     * @param host The host to get the energy prediction for.
     * @return The predicted power usage.
     */
    public double predictPowerUsed(Host host) {
        LinearFunction model = retrieveModel(host).getFunction();
        if (usageCPU == -1) {
            return model.value(source.getCpuUtilisation(host, cpuUtilObservationTimeSecTotal));
        } else {
            return model.value(usageCPU);
        }
    }

    /**
     * This estimates the power used by a host, given its CPU load.
     *
     * @param host The host to get the energy prediction for
     * @param usageCPU The amount of CPU load placed on the host
     * @return The predicted power usage.
     */
    public double predictPowerUsed(Host host, double usageCPU) {
        LinearFunction model = retrieveModel(host).getFunction();
        return model.value(usageCPU);
    }

    /**
     * This calculates the mathematical function that predicts the power
     * consumption given the cpu utilisation.
     *
     * @param host The host to get the function for
     * @return The mathematical function that predicts the power consumption
     * given the cpu utilisation.
     */
    private PredictorFunction<LinearFunction> retrieveModel(Host host) {
        if (modelCache.containsKey(host)) {
            /**
             * A small cache avoids recalculating the regression so often.
             */
            return modelCache.get(host);
        }
        LinearFunction model = new LinearFunction();
        SimpleRegression regressor = new SimpleRegression(true);
        for (HostEnergyCalibrationData data : host.getCalibrationData()) {
            regressor.addData(data.getCpuUsage(), data.getWattsUsed());
        }
        model.setIntercept(regressor.getIntercept());
        model.setCoefficient(regressor.getSlope());
        PredictorFunction<LinearFunction> answer = new PredictorFunction<>(model,
                regressor.getSumSquaredErrors(),
                Math.sqrt(regressor.getMeanSquareError()));
        modelCache.put(host, answer);
        return answer;
    }

    @Override
    public double getSumOfSquareError(Host host) {
        return retrieveModel(host).getSumOfSquareError();
    }

    @Override
    public double getRootMeanSquareError(Host host) {
        return retrieveModel(host).getRootMeanSquareError();
    }

}
