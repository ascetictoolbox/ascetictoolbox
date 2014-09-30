package eu.ascetic.providerregistry.model;

import static org.junit.Assert.assertEquals;

import java.util.ArrayList;
import java.util.List;

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
 * Test the POJO Item funcitonality.
 */
public class ItemTest {
	
	@Test
	public void pojo() {
		Items items = new Items();
		items.setOffset(1);
		items.setTotal(2);
		
		List<Provider> providers = new ArrayList<Provider>();
		items.setProviders(providers);
		
		assertEquals(1, items.getOffset());
		assertEquals(2, items.getTotal());
		assertEquals(providers, items.getProviders());
	}
	
	@Test
	public void addExperiment() {
		Items items = new Items();
		assertEquals(null, items.getProviders());
		
		Provider provider = new Provider();
		items.addProvider(provider);
		
		assertEquals(1, items.getProviders().size());
		assertEquals(provider, items.getProviders().get(0));
	}
}
