/**
 *  Copyright 2014 University of Leeds
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package eu.ascetic.asceticarchitecture.iaas.energymodeller.energypredictor;

import eu.ascetic.asceticarchitecture.iaas.energymodeller.types.TimePeriod;
import eu.ascetic.asceticarchitecture.iaas.energymodeller.types.energyuser.CandidateVMHostMapping;
import eu.ascetic.asceticarchitecture.iaas.energymodeller.types.energyuser.Host;
import eu.ascetic.asceticarchitecture.iaas.energymodeller.types.energyuser.VM;
import eu.ascetic.asceticarchitecture.iaas.energymodeller.types.usage.EnergyUsagePrediction;
import java.util.Collection;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.concurrent.TimeUnit;

/**
 * This implements the default behaviour for an energy predictor. 
 * It is expected that any energy predictor loaded into the ASCETiC architecture, 
 * will override this class.
 * @author Richard
 */
public class DefaultEnergyPredictor implements EnergyPredictorInterface {

    
    HashMap<Host, Double> tempAvgPowerUsed = new HashMap<>();
    HashMap<Host, Double> tempTotalEnergyUsed = new HashMap<>();
     
    @Override
    public EnergyUsagePrediction getHostPredictedEnergy(Host host, Collection<VM> virtualMachines) {
        EnergyUsagePrediction answer = new EnergyUsagePrediction(host);
        //TODO add model code here
        TimePeriod duration = new TimePeriod(new GregorianCalendar(), 1, TimeUnit.HOURS);
        answer.setDuration(duration);
        if (tempAvgPowerUsed.containsKey(host)) {
            answer.setAvgPowerUsed(tempAvgPowerUsed.get(host));
            answer.setTotalEnergyUsed(tempTotalEnergyUsed.get(host));             
        } else {
            double tempPower = Math.random() * 20;
            double tempEnergy = Math.random() * 20;
            tempAvgPowerUsed.put(host, tempPower);
            tempTotalEnergyUsed.put(host, tempEnergy);
            answer.setAvgPowerUsed(tempPower);
            answer.setTotalEnergyUsed(tempEnergy); 
        }
        return answer;
    }
    
    HashMap<CandidateVMHostMapping, Double> temp2AvgPowerUsed = new HashMap<>();
    HashMap<CandidateVMHostMapping, Double> temp2TotalEnergyUsed = new HashMap<>();    

    @Override
    public EnergyUsagePrediction getVMPredictedEnergy(VM vm, Collection<VM> virtualMachines, Host host) {
        EnergyUsagePrediction answer = new EnergyUsagePrediction(vm);
        //TODO add model code here
        TimePeriod duration = new TimePeriod(new GregorianCalendar(), 1, TimeUnit.HOURS);
        answer.setDuration(duration);
        if (tempAvgPowerUsed.containsKey(host)) {
            answer.setAvgPowerUsed(temp2AvgPowerUsed.get(new CandidateVMHostMapping(vm, host)));
            answer.setTotalEnergyUsed(temp2TotalEnergyUsed.get(new CandidateVMHostMapping(vm, host)));             
        } else {
            double tempPower = Math.random() * 20;
            double tempEnergy = Math.random() * 20;
            temp2AvgPowerUsed.put(new CandidateVMHostMapping(vm, host), tempPower);
            temp2TotalEnergyUsed.put(new CandidateVMHostMapping(vm, host), tempEnergy);
            answer.setAvgPowerUsed(tempPower);
            answer.setTotalEnergyUsed(tempEnergy); 
        }
        return answer;
    }
    
}
