package eu.ascetic.paas.applicationmanager.rest.util;

import java.util.ArrayList;
import java.util.List;

import eu.ascetic.asceticarchitecture.paas.component.energymodeller.datatype.EnergySample;

/**
 * 
 * Copyright 2014 ATOS SPAIN S.A. 
 * 
 * Licensed under the Apache License, Version 2.0 (the License);
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * 
 * @author David Garcia Perez. Atos Research and Innovation, Atos SPAIN SA
 * e-mail david.garciaperez@atos.net 
 * 
 * Converts data objects comming from the Energy Modeller to something the AM understands
 *
 */
public class EnergyModellerConverter {

	public static eu.ascetic.paas.applicationmanager.model.EnergySample convert(EnergySample energySample) {
		if(energySample == null) {
			return null;
		}
		
		eu.ascetic.paas.applicationmanager.model.EnergySample newEnergySample = new eu.ascetic.paas.applicationmanager.model.EnergySample();
		newEnergySample.setEvalue(energySample.getE_value());
		newEnergySample.setPvalue(energySample.getP_value());
		newEnergySample.setTimestampBeging(energySample.getTimestampBeging());
		newEnergySample.setTimestampEnd(energySample.getTimestampEnd());
		newEnergySample.setVmid(energySample.getVmid());
		
		return newEnergySample;
	}
	
	public static List<eu.ascetic.paas.applicationmanager.model.EnergySample> convertList(List<EnergySample> energySamples) {
		if(energySamples == null) {
			return null;
		}
		
		List<eu.ascetic.paas.applicationmanager.model.EnergySample> samples = new ArrayList<eu.ascetic.paas.applicationmanager.model.EnergySample>();
		
		for(EnergySample eSample : energySamples) {
			eu.ascetic.paas.applicationmanager.model.EnergySample energySample = convert(eSample);
			samples.add(energySample);
		}
		
		return samples;
	}
}
