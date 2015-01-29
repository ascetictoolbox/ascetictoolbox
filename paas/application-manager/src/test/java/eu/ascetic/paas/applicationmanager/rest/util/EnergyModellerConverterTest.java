package eu.ascetic.paas.applicationmanager.rest.util;

import static org.junit.Assert.assertEquals;

import java.util.ArrayList;
import java.util.List;

import org.junit.Test;

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
 * Collection of Unit test that verify the correct work of the REST service for VMs entities
 *
 */
public class EnergyModellerConverterTest {

	@Test
	public void convertEnergySampleTest() {
		EventSample nullEventSample = null;
		eu.ascetic.paas.applicationmanager.model.EventSample newEventSample = EnergyModellerConverter.convert(nullEventSample);
		assertEquals(null, newEventSample);
		
		EventSample eventSample = new EventSample();
		eventSample.setCvalue(1.0);
		eventSample.setEvalue(2.0);
		eventSample.setPvalue(3.0);
		eventSample.setTimestampBeging(1l);
		eventSample.setTimestampEnd(2l);
		eventSample.setVmid("vmid");
		eventSample.setAppid("appid");
		
		newEventSample = EnergyModellerConverter.convert(eventSample);
		
		assertEquals("vmid", newEventSample.getVmid());
		assertEquals(1.0, newEventSample.getCvalue(), 0.00001);
		assertEquals(2.0, newEventSample.getEvalue(), 0.00001);
		assertEquals(3.0, newEventSample.getPvalue(), 0.00001);
		assertEquals(1l, newEventSample.getTimestampBeging());
		assertEquals(2l, newEventSample.getTimestampEnd());
		assertEquals("appid", newEventSample.getAppid());
	}
	
	@Test
	public void convertSampleTest() {
		ApplicationSample nullApplicationSample = null;
		eu.ascetic.paas.applicationmanager.model.ApplicationSample newApplicationSample = EnergyModellerConverter.convert(nullApplicationSample);
		assertEquals(null, newApplicationSample);
		
		ApplicationSample applicationSample = new ApplicationSample();
		applicationSample.setAppid("appid");
		applicationSample.setC_value(1.0);
		applicationSample.setE_value(2.0);
		applicationSample.setOrderID(5);
		applicationSample.setP_value(3.0);
		applicationSample.setTime(2l);
		applicationSample.setVmid("vmid");
		
		newApplicationSample = EnergyModellerConverter.convert(applicationSample);
		
		assertEquals("appid", newApplicationSample.getAppid());
		assertEquals(1.0, newApplicationSample.getcValue(), 0.0001);
		assertEquals(2.0, newApplicationSample.geteValue(), 0.0001);
		assertEquals(5, newApplicationSample.getOrderID());
		assertEquals(3.0, newApplicationSample.getpValue(), 0.0001);
		assertEquals(2l, newApplicationSample.getTime());
		assertEquals("vmid", newApplicationSample.getVmid());
	}
	
	@Test
	public void convertEnergySampleListTest() {
		List<EventSample> nullEventSamples = null;
		List<eu.ascetic.paas.applicationmanager.model.EventSample> newEventSamples = EnergyModellerConverter.convertList(nullEventSamples);
		assertEquals(null, newEventSamples);
		
		EventSample eventSample1 = new EventSample();
		eventSample1.setCvalue(1.0);
		eventSample1.setEvalue(2.0);
		eventSample1.setPvalue(3.0);
		eventSample1.setTimestampBeging(1l);
		eventSample1.setTimestampEnd(2l);
		eventSample1.setVmid("vmid");
		eventSample1.setAppid("appid");

		EventSample eventSample2 = new EventSample();
		eventSample2.setCvalue(4.0);
		eventSample2.setEvalue(5.0);
		eventSample2.setPvalue(6.0);
		eventSample2.setTimestampBeging(3l);
		eventSample2.setTimestampEnd(4l);
		eventSample2.setVmid("vmid2");
		eventSample2.setAppid("appid2");
		
 		List<EventSample> eventSamples = new ArrayList<EventSample>();
 		eventSamples.add(eventSample1);
 		eventSamples.add(eventSample2);
 		
 		newEventSamples = EnergyModellerConverter.convertList(eventSamples);
 		
 		assertEquals(2, newEventSamples.size());
 		assertEquals(1.0, newEventSamples.get(0).getCvalue(), 0.00001);
		assertEquals(2.0, newEventSamples.get(0).getEvalue(), 0.00001);
		assertEquals(3.0, newEventSamples.get(0).getPvalue(), 0.00001);
		assertEquals(1l, newEventSamples.get(0).getTimestampBeging());
		assertEquals(2l, newEventSamples.get(0).getTimestampEnd());
		assertEquals("vmid", newEventSamples.get(0).getVmid());
		assertEquals("appid", newEventSamples.get(0).getAppid());
 		assertEquals(4.0, newEventSamples.get(1).getCvalue(), 0.00001);
		assertEquals(5.0, newEventSamples.get(1).getEvalue(), 0.00001);
		assertEquals(6.0, newEventSamples.get(1).getPvalue(), 0.00001);
		assertEquals(3l, newEventSamples.get(1).getTimestampBeging());
		assertEquals(4l, newEventSamples.get(1).getTimestampEnd());
		assertEquals("vmid2", newEventSamples.get(1).getVmid());
		assertEquals("appid2", newEventSamples.get(1).getAppid());
	}
	
	@Test
	public void convertSampleListTest() {
		List<eu.ascetic.paas.applicationmanager.model.ApplicationSample> newApplicationSamples = EnergyModellerConverter.convertSampleList(null);
		assertEquals(null, newApplicationSamples);
		
		ApplicationSample applicationSample1 = new ApplicationSample();
		applicationSample1.setAppid("appid");
		applicationSample1.setC_value(1.0);
		applicationSample1.setE_value(2.0);
		applicationSample1.setOrderID(5);
		applicationSample1.setP_value(3.0);
		applicationSample1.setTime(2l);
		applicationSample1.setVmid("vmid");
		ApplicationSample applicationSample2 = new ApplicationSample();
		applicationSample2.setAppid("appid2");
		applicationSample2.setC_value(4.0);
		applicationSample2.setE_value(5.0);
		applicationSample2.setOrderID(7);
		applicationSample2.setP_value(6.0);
		applicationSample2.setTime(3l);
		applicationSample2.setVmid("vmid2");
		
		List<ApplicationSample> applicationSamples = new ArrayList<ApplicationSample>();
		applicationSamples.add(applicationSample1);
		applicationSamples.add(applicationSample2);
 		
 		newApplicationSamples = EnergyModellerConverter.convertSampleList(applicationSamples);
 		
 		assertEquals(2, newApplicationSamples.size());
		assertEquals(1.0, newApplicationSamples.get(0).getcValue(), 0.00001);
		assertEquals(2.0, newApplicationSamples.get(0).geteValue(), 0.00001);
		assertEquals(3.0, newApplicationSamples.get(0).getpValue(), 0.00001);
		assertEquals(2l, newApplicationSamples.get(0).getTime());
		assertEquals("appid", newApplicationSamples.get(0).getAppid());
		assertEquals("vmid", newApplicationSamples.get(0).getVmid());
		assertEquals(5, newApplicationSamples.get(0).getOrderID());
		assertEquals(4.0, newApplicationSamples.get(1).getcValue(), 0.00001);
		assertEquals(5.0, newApplicationSamples.get(1).geteValue(), 0.00001);
		assertEquals(6.0, newApplicationSamples.get(1).getpValue(), 0.00001);
		assertEquals(3l, newApplicationSamples.get(1).getTime());
		assertEquals("appid2", newApplicationSamples.get(1).getAppid());
		assertEquals("vmid2", newApplicationSamples.get(1).getVmid());
		assertEquals(7, newApplicationSamples.get(1).getOrderID());	}
}
