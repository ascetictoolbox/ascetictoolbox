package eu.ascetic.paas.applicationmanager.pm;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.LinkedList;

import org.junit.Before;
import org.junit.Test;

import eu.ascetic.asceticarchitecture.paas.paaspricingmodeller.PaaSPricingModeller;
import eu.ascetic.asceticarchitecture.paas.type.VMinfo;

/**
 * 
 * Copyright 2015 ATOS SPAIN S.A. 
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
 * Checks the right behaviour of the Price Modeller.
 *
 */
public class PriceModellerClientTest {
	private PriceModellerClient pmc;
	private PaaSPricingModeller ppm;
	
	@Before
	public void before() {
		this.pmc = PriceModellerClient.getInstance();
		this.ppm = mock(PaaSPricingModeller.class);
		pmc.priceModeller = ppm;
	}

	@Test
	public void testSingleton() {
		PriceModellerClient pmc1 = PriceModellerClient.getInstance();
		PriceModellerClient pmc2 = PriceModellerClient.getInstance();
	
		assertEquals(pmc1, pmc2);
	}
	
	@Test
	public void intializeAppTest() {		
		pmc.initializeApplication(222, 3);
		verify(ppm, times(1)).initializeApp(222, 3);
	}
	
	@Test
	public void getAppPredictedChargesTest() {
		when(ppm.getAppPredictedCharges(111, 2, 3.1)).thenReturn(3.1);
		
		double charges = pmc.getAppPredictedCharges(111, 2, 3.1);
		assertEquals(3.1, charges, 0.000001);
	}
	
	@Test
	public void getAppPredictedPriceTest() {
		when(ppm.getAppPredictedPrice(111, 2, 4.2, 12l)).thenReturn(6.1);
		
		double charges = pmc.getAppPredictedPrice(111, 2, 4.2, 12l);
		assertEquals(6.1, charges, 0.000001);
	}
	
	@Test
	public void getAppTotalCharges() {
		when(ppm.getAppTotalCharges(111, 4, 3.4)).thenReturn(6.2);
		
		double charges = pmc.getAppTotalCharges(111, 4, 3.4);
		assertEquals(6.2, charges, 0.000001);
	}
	
	@Test
	public void getEventPredictedChargesTest() {
		when(ppm.getEventPredictedCharges(111, 22, 33, 3.0, 1.2, 2, 3, 3)).thenReturn(3.2);
		
		double charges = pmc.getEventPredictedCharges(111, 22, 33, 3.0, 1.2, 2, 3, 3);
		assertEquals(3.2, charges, 0.00001);
	}
	
	@Test
	public void getEventPredictedChargesOfAppTest() {
		LinkedList<VMinfo> vmInfos = new LinkedList<VMinfo>();
		
		when(ppm.getEventPredictedChargesOfApp(111, vmInfos, 33, 3)).thenReturn(3.2);
		double charges = pmc.getEventPredictedChargesOfApp(111, vmInfos, 33, 3);
		assertEquals(3.2, charges, 0.00001);
	}
}
