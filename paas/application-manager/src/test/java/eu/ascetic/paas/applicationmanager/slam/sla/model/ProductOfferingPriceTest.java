package eu.ascetic.paas.applicationmanager.slam.sla.model;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

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
 * @email david.garciaperez@atos.net
 * 
 * This class is the Unit test that verifies the correct parsing of the SLA ProductOfferingPrice
 */
public class ProductOfferingPriceTest {

	@Test
	public void pojo() {
		ProductOfferingPrice productOfferingPrice = new ProductOfferingPrice();
		productOfferingPrice.setId("id");
		ComponentProdOfferingPrice componentProdOfferingPrice = new ComponentProdOfferingPrice();
		productOfferingPrice.setComponentProdOfferingPrice(componentProdOfferingPrice);
		
		assertEquals("id", productOfferingPrice.getId());
		assertEquals(componentProdOfferingPrice, productOfferingPrice.getComponentProdOfferingPrice());
	}
}
