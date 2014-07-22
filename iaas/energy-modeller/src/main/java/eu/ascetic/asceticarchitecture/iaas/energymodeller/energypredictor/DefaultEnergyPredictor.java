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

import eu.ascetic.asceticarchitecture.iaas.energymodeller.energypredictor.transformation.vmenergyshare.DefaultEnergyShareRule;
import eu.ascetic.asceticarchitecture.iaas.energymodeller.energypredictor.transformation.vmenergyshare.EnergyDivision;
import eu.ascetic.asceticarchitecture.iaas.energymodeller.energypredictor.transformation.vmenergyshare.EnergyShareRule;
import eu.ascetic.asceticarchitecture.iaas.energymodeller.training.DefaultEnergyModelTrainer;
import eu.ascetic.asceticarchitecture.iaas.energymodeller.training.EnergyModelTrainerInterface;
import eu.ascetic.asceticarchitecture.iaas.energymodeller.types.TimePeriod;
import eu.ascetic.asceticarchitecture.iaas.energymodeller.types.energymodel.EnergyModel;
import eu.ascetic.asceticarchitecture.iaas.energymodeller.types.energyuser.Host;
import eu.ascetic.asceticarchitecture.iaas.energymodeller.types.energyuser.HostEnergyCalibrationData;
import eu.ascetic.asceticarchitecture.iaas.energymodeller.types.energyuser.VM;
import eu.ascetic.asceticarchitecture.iaas.energymodeller.types.usage.EnergyUsagePrediction;
import java.util.ArrayList;
import java.util.Collection;
import java.util.GregorianCalendar;
import java.util.concurrent.TimeUnit;

/**
 * This implements the default energy predictor for the ASCETiC project.
 *
 * @author Richard Kavanagh and Eleni Agiatzidou
 */
public class DefaultEnergyPredictor extends AbstractEnergyPredictor {

    //TODO for the average power!!! 
    //TO FIX TIME!!
    private EnergyModelTrainerInterface trainer = new DefaultEnergyModelTrainer();
    private EnergyShareRule rule = new DefaultEnergyShareRule();

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
        //Last measurement should contain the usage and the time period for which we need a prediction!!!     
        //take the last measurement
        ArrayList<HostEnergyCalibrationData> calibrationData = host.getCalibrationData();
        int lastElement = calibrationData.size();
        HostEnergyCalibrationData data = calibrationData.get(lastElement);
        double usageCPU = data.getCpuUsage();
        double usageMemory = data.getMemoryUsage();
        EnergyUsagePrediction wattsUsed;
        TimePeriod duration = new TimePeriod(new GregorianCalendar(), 1, TimeUnit.HOURS);
        wattsUsed = predictTotalEnergy(host, usageCPU, usageMemory, duration);
        return wattsUsed;
    }
    
    /**
     * This predicts the total amount of energy used by a host. 
     * @param host The host to get the energy prediction for
     * @param virtualMachines A collection of VMs 
     * @return The predicted energy usage.
     */

    private EnergyUsagePrediction predictTotalEnergy(Host host, double usageCPU, double usageRAM, TimePeriod duration) {
        EnergyUsagePrediction answer = new EnergyUsagePrediction(host);
        EnergyModel model = trainer.retrieveModel(host);

        double temp;
        temp = model.getIntercept() + model.getCoefCPU() * usageCPU + model.getCoefRAM() * usageRAM;
        if (duration.getDuration() == 60) {
            answer.setAvgPowerUsed(temp);
            answer.setTotalEnergyUsed(temp * duration.getDuration());
            answer.setDuration(duration);
            return answer;
        } else {
            long time = TimePeriod.convertToMinutes(duration);
            answer.setAvgPowerUsed(temp);
            answer.setTotalEnergyUsed(temp * duration.getDuration());
            answer.setDuration(duration);
            return answer;
        }
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
        double usageCPU = 0;
        int usageRAM = 0;
        //TODO Fix assumptions here
        for (VM currentVM : virtualMachines) {
            usageRAM = usageRAM + currentVM.getRamMb();
            usageCPU = 100; //assumed 100 percent usage.
        }
        //Obtain the total for the VM
        EnergyUsagePrediction answer = predictTotalEnergy(host, usageCPU, usageRAM, timePeriod);
        //Find the fraction to be associated with the VM
        double vmsEnergyFraction = division.getEnergyUsage(answer.getTotalEnergyUsed(), vm);
        answer.setTotalEnergyUsed(vmsEnergyFraction);
        double vmsPowerFraction = division.getEnergyUsage(answer.getAvgPowerUsed(), vm);
        answer.setTotalEnergyUsed(vmsPowerFraction);
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
    public void setTrainer(EnergyModelTrainerInterface trainer) {
        this.trainer = trainer;
    }

    /**
     * @return the rule
     */
    public EnergyShareRule getRule() {
        return rule;
    }

    /**
     * @param rule the rule to set
     */
    public void setRule(EnergyShareRule rule) {
        this.rule = rule;
    }

}
