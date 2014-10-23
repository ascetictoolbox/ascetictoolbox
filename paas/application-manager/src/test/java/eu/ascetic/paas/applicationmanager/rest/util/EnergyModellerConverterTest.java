package eu.ascetic.paas.applicationmanager.rest.util;

import static org.junit.Assert.assertEquals;

import java.util.ArrayList;
import java.util.List;

import org.junit.Test;

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
 * @author: David Garcia Perez. Atos Research and Innovation, Atos SPAIN SA
 * e-mail david.garciaperez@atos.net 
 * 
 * Collection of Unit test that verify the correct work of the REST service for VMs entities
 *
 */
public class EnergyModellerConverterTest {

	@Test
	public void convertEnergySampleTest() {
		
		eu.ascetic.paas.applicationmanager.model.EnergySample energySample = EnergyModellerConverter.convert(null);
		assertEquals(null, energySample);
		
		EnergySample eSample = new EnergySample();
		eSample.setE_value(1);
		eSample.setP_value(2);
		eSample.setTimestampBeging(11l);
		eSample.setTimestampEnd(22l);
		eSample.setVmid("aaa");
		
		energySample = EnergyModellerConverter.convert(eSample);
		
		assertEquals(1.0, energySample.getEvalue(), 0.00001);
		assertEquals(2.0, energySample.getPvalue(), 0.00001);
		assertEquals(11l, energySample.getTimestampBeging());
		assertEquals(22l, energySample.getTimestampEnd());
		assertEquals("aaa", energySample.getVmid());
	}
	
	@Test
	public void convertEnergySampleListTest() {
		List<eu.ascetic.paas.applicationmanager.model.EnergySample> samples = EnergyModellerConverter.convertList(null);
		assertEquals(null, samples);
		
		EnergySample eSample1 = new EnergySample();
		eSample1.setE_value(1);
		eSample1.setP_value(2);
		eSample1.setTimestampBeging(11l);
		eSample1.setTimestampEnd(22l);
		eSample1.setVmid("aaa");
		EnergySample eSample2 = new EnergySample();
		eSample2.setE_value(3);
		eSample2.setP_value(4);
		eSample2.setTimestampBeging(33l);
		eSample2.setTimestampEnd(44l);
		eSample2.setVmid("bbb");
		
 		List<EnergySample> eSamples = new ArrayList<EnergySample>();
 		eSamples.add(eSample1);
 		eSamples.add(eSample2);
 		
 		samples = EnergyModellerConverter.convertList(eSamples);
 		
 		assertEquals(2, samples.size());
		assertEquals(1.0, samples.get(0).getEvalue(), 0.00001);
		assertEquals(2.0, samples.get(0).getPvalue(), 0.00001);
		assertEquals(11l, samples.get(0).getTimestampBeging());
		assertEquals(22l, samples.get(0).getTimestampEnd());
		assertEquals("aaa", samples.get(0).getVmid());
		assertEquals(3.0, samples.get(1).getEvalue(), 0.00001);
		assertEquals(4.0, samples.get(1).getPvalue(), 0.00001);
		assertEquals(33l, samples.get(1).getTimestampBeging());
		assertEquals(44l, samples.get(1).getTimestampEnd());
		assertEquals("bbb", samples.get(1).getVmid());
	}
}
