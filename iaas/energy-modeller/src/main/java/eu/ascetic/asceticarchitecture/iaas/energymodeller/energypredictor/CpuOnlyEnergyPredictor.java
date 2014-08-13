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

import eu.ascetic.asceticarchitecture.iaas.energymodeller.energypredictor.vmenergyshare.DefaultEnergyShareRule;
import eu.ascetic.asceticarchitecture.iaas.energymodeller.energypredictor.vmenergyshare.EnergyDivision;
import eu.ascetic.asceticarchitecture.iaas.energymodeller.energypredictor.vmenergyshare.EnergyShareRule;
import eu.ascetic.asceticarchitecture.iaas.energymodeller.types.TimePeriod;
import eu.ascetic.asceticarchitecture.iaas.energymodeller.types.energymodel.EnergyModel;
import eu.ascetic.asceticarchitecture.iaas.energymodeller.types.energyuser.Host;
import eu.ascetic.asceticarchitecture.iaas.energymodeller.types.energyuser.VM;
import eu.ascetic.asceticarchitecture.iaas.energymodeller.types.energyuser.usage.HostEnergyCalibrationData;
import eu.ascetic.asceticarchitecture.iaas.energymodeller.types.usage.EnergyUsagePrediction;
import java.io.File;
import java.util.Collection;
import java.util.GregorianCalendar;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.configuration.PropertiesConfiguration;
import org.apache.commons.math3.stat.regression.SimpleRegression;

/**
 * This implements the cpu only energy predictor for the ASCETiC project.
 *
 * @author Richard Kavanagh and derived soley from work of Eleni Agiatzidou
 */
public class CpuOnlyEnergyPredictor extends AbstractEnergyPredictor {

    private EnergyShareRule rule = new DefaultEnergyShareRule();
    private static final String CONFIG_FILE = "energymodeller_cpu_predictor.properties";
    private double usageCPU = 0.6; //assumed 60 percent usage, by default

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
            usageCPU = config.getDouble("iaas.energy.modeller.cpu.energy.predcitor.default_load", usageCPU);
            config.setProperty("iaas.energy.modeller.cpu.energy.predcitor.default_load", usageCPU);
        } catch (ConfigurationException ex) {
            Logger.getLogger(CpuOnlyEnergyPredictor.class.getName()).log(Level.SEVERE,
                    "Taking the default load from the settings file did not work", ex);
        }
    }

    /**
     * This provides a prediction of how much energy is to be used by a host
     *
     * @param host The host to get the energy prediction for
     * @param virtualMachines The virtual machines giving a workload on the host
     * machine
     * @return The prediction of the energy to be used.
     */
    @Override
    public EnergyUsagePrediction getHostPredictedEnergy(Host host, Collection<VM> virtualMachines) {
        EnergyUsagePrediction wattsUsed;
        TimePeriod duration = new TimePeriod(new GregorianCalendar(), 1, TimeUnit.HOURS);
        wattsUsed = predictTotalEnergy(host, usageCPU, duration);
        return wattsUsed;
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
        EnergyModel model = retrieveModel(host);
        double powerUsed;
        powerUsed = model.getIntercept() + model.getCoefCPU() * usageCPU;
        answer.setAvgPowerUsed(powerUsed);
        answer.setTotalEnergyUsed(powerUsed * ((double) TimeUnit.SECONDS.toHours(timePeriod.getDuration())));
        answer.setDuration(timePeriod);
        return answer;
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
        EnergyDivision division = rule.getEnergyUsage(host, virtualMachines);
        EnergyUsagePrediction hostAnswer = predictTotalEnergy(host, usageCPU, timePeriod);
        hostAnswer.setAvgPowerUsed(hostAnswer.getTotalEnergyUsed()
                / ((double) TimeUnit.SECONDS.toHours(timePeriod.getDuration())));
        EnergyUsagePrediction answer = new EnergyUsagePrediction(vm);
        answer.setDuration(hostAnswer.getDuration());
        //Find the fraction to be associated with the VM
        double vmsEnergyFraction = division.getEnergyUsage(hostAnswer.getTotalEnergyUsed(), vm);
        answer.setTotalEnergyUsed(vmsEnergyFraction);
        double vmsPowerFraction = division.getEnergyUsage(hostAnswer.getAvgPowerUsed(), vm);
        answer.setAvgPowerUsed(vmsPowerFraction);
        return answer;
    }

    /**
     * This function should use the values stored to create the coefficients of
     * the model.
     *
     * @param host The host (key) for which the values are going to be printed.
     *
     * @return The coefficients and the intercept of the model.
     */
    public EnergyModel retrieveModel(Host host) {
        EnergyModel answer = new EnergyModel();
        SimpleRegression regressor = new SimpleRegression(true);
        for (HostEnergyCalibrationData data : host.getCalibrationData()) {
            regressor.addData(data.getCpuUsage(), data.getWattsUsed());
        }
        answer.setIntercept(regressor.getIntercept());
        answer.setCoefCPU(regressor.getSlope());
        return answer;

    }

    /**
     * This provides a prediction of how much energy is to be used by a VM
     *
     * @param vm The vm to be deployed
     * @param virtualMachines The virtual machines giving a workload on the host
     * machine
     * @param host The host that the VMs will be running on
     * @return The prediction of the energy to be used.
     */
    @Override
    public EnergyUsagePrediction getVMPredictedEnergy(VM vm, Collection<VM> virtualMachines, Host host) {
        //Run the prediction for the next hour.
        TimePeriod duration = new TimePeriod(new GregorianCalendar(), TimeUnit.HOURS.toSeconds(1));
        return getVMPredictedEnergy(vm, virtualMachines, host, duration);
    }

    /**
     * This returns the energy sharing rule used to divide the host energy among
     * the vms.
     *
     * @return the rule used to share energy among vms.
     */
    public EnergyShareRule getRule() {
        return rule;
    }

    /**
     * This sets the energy sharing rule used to divide the host energy among
     * the vms.
     *
     * @param rule the rule to be used to share energy among vms.
     */
    public void setRule(EnergyShareRule rule) {
        this.rule = rule;
    }

}
