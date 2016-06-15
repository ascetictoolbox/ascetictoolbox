/**
   Copyright 2014-2015 Hewlett-Packard Development Company, L.P.  
   Licensed under the Apache License, Version 2.0 (the "License");
   you may not use this file except in compliance with the License.
   You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

   Unless required by applicable law or agreed to in writing, software
   distributed under the License is distributed on an "AS IS" BASIS,
   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   See the License for the specific language governing permissions and
   limitations under the License.

 */
package eu.ascetic.asceticarchitecture.paas.component.energymodeller.internal.model.predictor;

import java.util.List;

import eu.ascetic.asceticarchitecture.paas.component.energymodeller.datatype.Unit;
import eu.ascetic.asceticarchitecture.paas.component.energymodeller.internal.common.data.database.table.DataConsumption;
import eu.ascetic.asceticarchitecture.paas.component.energymodeller.internal.common.dataservice.EnergyDataAggregatorServiceQueue;

/**
 * 
 * @author sommacam
 * generic predictor interface there are methods for estimating with different inputs
 */
public interface PredictorInterface {
	
	// M. Fontanella - 26 Apr 2016 - begin
	double estimate(String providerid, String applicationid, String deploymentid, List<String> vmids, String eventid, Unit unit, long timelater, boolean enablePowerFromIaas);
	
	double estimate(String providerid, String applicationid, String deploymentid, String vm, String eventid, Unit unit, long timelater, boolean enablePowerFromIaas);
		
	double estimate(List<DataConsumption> samples, Unit unit, long timelater, boolean enablePowerFromIaas);
	// M. Fontanella - 26 Apr 2016 - end
	
	void setEnergyService(EnergyDataAggregatorServiceQueue service);
	
}
