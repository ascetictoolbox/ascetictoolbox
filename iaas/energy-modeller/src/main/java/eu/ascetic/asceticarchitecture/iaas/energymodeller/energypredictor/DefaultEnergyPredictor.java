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
import eu.ascetic.asceticarchitecture.iaas.energymodeller.training.DefaultEnergyModelTrainer;
import eu.ascetic.asceticarchitecture.iaas.energymodeller.training.EnergyModelTrainerInterface;
import eu.ascetic.asceticarchitecture.iaas.energymodeller.types.TimePeriod;
import eu.ascetic.asceticarchitecture.iaas.energymodeller.types.energymodel.EnergyModel;
import eu.ascetic.asceticarchitecture.iaas.energymodeller.types.energyuser.Host;
import eu.ascetic.asceticarchitecture.iaas.energymodeller.types.energyuser.VM;
import eu.ascetic.asceticarchitecture.iaas.energymodeller.types.usage.EnergyUsagePrediction;
import java.io.File;
import java.util.Collection;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.configuration.PropertiesConfiguration;

/**
 * This implements the default energy predictor for the ASCETiC project.
 * @deprecated This will no longer be supported use the CPU only energy predictor
 * @author Richard Kavanagh and Eleni Agiatzidou
 */
public class DefaultEnergyPredictor extends AbstractEnergyPredictor {

    private EnergyModelTrainerInterface trainer = new DefaultEnergyModelTrainer();
    private static final String CONFIG_FILE = "energymodeller_cpu_predictor.properties";
    private double usageCPU = 1;

    /**
     * This creates a new Default energy predictor, that compares CPU and RAM
     * utilisation.
     */
    public DefaultEnergyPredictor() {
        try {
            PropertiesConfiguration config;
            if (new File(CONFIG_FILE).exists()) {
                config = new PropertiesConfiguration(CONFIG_FILE);
            } else {
                config = new PropertiesConfiguration();
                config.setFile(new File(CONFIG_FILE));
            }
            //This will save the configuration file back to disk. In case the defaults need setting.
            config.setAutoSave(true);
            usageCPU = config.getDouble("iaas.energy.modeller.cpu.energy.predcitor.default_load", usageCPU);
            config.setProperty("iaas.energy.modeller.cpu.energy.predcitor.default_load", usageCPU);
        } catch (ConfigurationException ex) {
            Logger.getLogger(DefaultEnergyPredictor.class.getName()).log(Level.SEVERE,
                    "Taking the default load from the settings file did not work", ex);
        }
    }

    @Override
    public EnergyUsagePrediction getHostPredictedEnergy(Host host, Collection<VM> virtualMachines, TimePeriod duration) {
        double usageMemory = 0;
        for (VM vm : virtualMachines) {
            usageMemory = usageMemory + vm.getRamMb();
        }
        usageMemory = usageMemory + host.getIdleRamUsage();
        EnergyUsagePrediction wattsUsed;
        wattsUsed = predictTotalEnergy(host, usageCPU, usageMemory / host.getRamMb(), duration);
        return wattsUsed;
    }

    /**
     * This predicts the total amount of energy used by a host.
     *
     * @param host The host to get the energy prediction for
     * @param usageCPU The amount of CPU load placed on the host
     * @param usageRAM The amount of ram used
     * @param timePeriod The time period the prediction is for
     * @return The predicted energy usage.
     */
    public EnergyUsagePrediction predictTotalEnergy(Host host, double usageCPU, double usageRAM, TimePeriod timePeriod) {
        EnergyUsagePrediction answer = new EnergyUsagePrediction(host);
        //Test for training then load the store with the correct values.

        if (!DefaultEnergyModelTrainer.storeValues.containsKey(host)) {
            trainer.trainModel(host, host.getCalibrationData());
        }
        EnergyModel model = trainer.retrieveModel(host);
        double powerUsed;
        powerUsed = model.getIntercept() + model.getCoefCPU() * usageCPU + model.getCoefRAM() * usageRAM;
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
        EnergyDivision division = getEnergyUsage(host, virtualMachines);
        double usageRAM = 0;
        //Obtain the total RAM usage for the host
        for (VM currentVM : virtualMachines) { //VMs
            usageRAM = usageRAM + currentVM.getRamMb();
        }
        //Plus Idle memory usage
        usageRAM = usageRAM + host.getIdleRamUsage();
        //Obtain the total energy usage for the host
        EnergyUsagePrediction hostAnswer = predictTotalEnergy(host, usageCPU, usageRAM / host.getRamMb(), timePeriod);
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
     * This gets the current energy trainer in use.
     *
     * @return the trainer
     */
    public EnergyModelTrainerInterface getTrainer() {
        return trainer;
    }

    /**
     * This sets the energy trainer to be used.
     *
     * @param trainer the trainer to set
     */
    public void setTrainer(DefaultEnergyModelTrainer trainer) {
        this.trainer = trainer;
    }

}
