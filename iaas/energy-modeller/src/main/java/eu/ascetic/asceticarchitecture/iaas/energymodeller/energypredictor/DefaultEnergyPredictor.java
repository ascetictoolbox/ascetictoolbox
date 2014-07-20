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

import eu.ascetic.asceticarchitecture.iaas.energymodeller.training.DefaultEnergyModelTrainer;
import eu.ascetic.asceticarchitecture.iaas.energymodeller.training.EnergyModelTrainerInterface;
import eu.ascetic.asceticarchitecture.iaas.energymodeller.types.energymodel.EnergyModel;
import eu.ascetic.asceticarchitecture.iaas.energymodeller.types.energyuser.Host;
import eu.ascetic.asceticarchitecture.iaas.energymodeller.types.energyuser.HostEnergyCalibrationData;
import eu.ascetic.asceticarchitecture.iaas.energymodeller.types.energyuser.VM;
import eu.ascetic.asceticarchitecture.iaas.energymodeller.types.usage.EnergyUsagePrediction;
import eu.ascetic.asceticarchitecture.iaas.energymodeller.types.TimePeriod;





import java.util.ArrayList;
import java.util.Collection;
import java.util.GregorianCalendar;
import java.util.concurrent.TimeUnit;

/**
 * This implements the default energy predictor for the ASCETiC project.
 *
 * @author Richard
 */
public class DefaultEnergyPredictor extends AbstractEnergyPredictor {

    private EnergyModelTrainerInterface trainer = new DefaultEnergyModelTrainer();

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
        //TODO Write get host predicted energy i.e. implement the model here.
    	ArrayList<HostEnergyCalibrationData> calibrationData = new ArrayList<>();
    	calibrationData=host.getCalibrationData();
    	int lastElement = calibrationData.size();
    	HostEnergyCalibrationData data = calibrationData.get(lastElement);
    	double usageCPU=data.getCpuUsage();
    	double usageMemory = data.getMemoryUsage();
    	EnergyUsagePrediction totalEnergy =  new EnergyUsagePrediction();
    	TimePeriod duration = new TimePeriod(new GregorianCalendar(), 1, TimeUnit.HOURS);
    	totalEnergy=predictTotalEnergy (host, usageCPU, usageMemory, duration);
        return totalEnergy;
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
        //TODO Write get VM predicted energy i.e. implement the model here.
    	
    	
        throw new UnsupportedOperationException("Not supported yet.");
    }
    
    @Override
    public EnergyUsagePrediction predictTotalEnergy (Host host, double usageCPU, double usageRAM, TimePeriod timePeriod){
    	EnergyUsagePrediction totalEnergy = new EnergyUsagePrediction(host);
    	EnergyModel model = trainer.retrieveModel(host);
    	double temp;
    	temp = model.getIntercept()+model.getCoefCPU()*usageCPU+model.getCoefRAM()*usageRAM;
    	totalEnergy.setTotalEnergyUsed(temp);
    	totalEnergy.setDuration(timePeriod);
    	return totalEnergy;
    }

}
