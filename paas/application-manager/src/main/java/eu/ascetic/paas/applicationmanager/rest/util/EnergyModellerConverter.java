package eu.ascetic.paas.applicationmanager.rest.util;

import java.util.ArrayList;
import java.util.List;

import eu.ascetic.asceticarchitecture.paas.component.energymodeller.datatype.ApplicationSample;
import eu.ascetic.asceticarchitecture.paas.component.energymodeller.datatype.EventSample;

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
	
	public static eu.ascetic.paas.applicationmanager.model.EventSample convert(EventSample eventSample) {
		if(eventSample == null) {
			return null;
		}
		
		eu.ascetic.paas.applicationmanager.model.EventSample newEventSample = new eu.ascetic.paas.applicationmanager.model.EventSample();
		newEventSample.setAppid(eventSample.getAppid());
		newEventSample.setCvalue(eventSample.getCvalue());
		newEventSample.setEvalue(eventSample.getEvalue());
		newEventSample.setPvalue(eventSample.getPvalue());
		newEventSample.setTimestampBeging(eventSample.getTimestampBeging());
		newEventSample.setTimestampEnd(eventSample.getTimestampEnd());
		newEventSample.setVmid(eventSample.getVmid());
		
		return newEventSample;
	}
	
	public static eu.ascetic.paas.applicationmanager.model.ApplicationSample convert(ApplicationSample applicationSample) {
		if(applicationSample == null) {
			return null;
		}
		
		eu.ascetic.paas.applicationmanager.model.ApplicationSample newApplicationSample = new eu.ascetic.paas.applicationmanager.model.ApplicationSample();
		newApplicationSample.setAppid(applicationSample.getAppid());
		newApplicationSample.setcValue(applicationSample.getC_value());
		newApplicationSample.seteValue(applicationSample.getE_value());
		newApplicationSample.setOrderID(applicationSample.getOrderID());
		newApplicationSample.setpValue(applicationSample.getP_value());
		newApplicationSample.setTime(applicationSample.getTime());
		newApplicationSample.setVmid(applicationSample.getVmid());
		
		return newApplicationSample;
	}
	
	public static List<eu.ascetic.paas.applicationmanager.model.EventSample> convertList(List<EventSample> eventSamples) {
		if(eventSamples == null) {
			return null;
		}
		
		List<eu.ascetic.paas.applicationmanager.model.EventSample> newEventSamples = new ArrayList<eu.ascetic.paas.applicationmanager.model.EventSample>();
		
		for(EventSample eventSample : eventSamples) {
			eu.ascetic.paas.applicationmanager.model.EventSample newEventSample = convert(eventSample);
			newEventSamples.add(newEventSample);
		}
		
		return newEventSamples;
	}
	
	public static List<eu.ascetic.paas.applicationmanager.model.ApplicationSample> convertSampleList(List<ApplicationSample> applicationSamples) {
		if(applicationSamples == null) {
			return null;
		}
		
		List<eu.ascetic.paas.applicationmanager.model.ApplicationSample> newApplicationSamples = new ArrayList<eu.ascetic.paas.applicationmanager.model.ApplicationSample>();
		
		for(ApplicationSample applicationSample : applicationSamples) {
			eu.ascetic.paas.applicationmanager.model.ApplicationSample newApplicationSample = convert(applicationSample);
			newApplicationSamples.add(newApplicationSample);
		}
		
		return newApplicationSamples;
	}
}
