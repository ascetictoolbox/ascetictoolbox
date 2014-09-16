package eu.ascetic.monitoring.api.datamodel;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

import eu.ascetic.asceticarchitecture.iaas.zabbixApi.datamodel.Item;

/**
*
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
* @author: David Rojo Antona. Atos Research and Innovation, Atos SPAIN SA
* @email david.rojoa@atos.net 
* 
* Java representation of tests for item class
* 
*/
public class ItemTest {

	/**
	 * Pojo test.
	 */
	@Test
	public void pojoTest() {
		Item item = new Item("Available memory");
		item.setDelay("60");
		item.setHistory("7");
		item.setHostid("10084");
		item.setItemid("23298");
		item.setKey("vm.memory.size[available]");
		item.setLastClock(1351090998);
		item.setLastValue("2552");
		item.setTrends("365");
		
		assertEquals("Available memory", item.getName());
		assertEquals("60", item.getDelay());
		assertEquals("7", item.getHistory());
		assertEquals("10084", item.getHostid());
		assertEquals("23298", item.getItemid());
		assertEquals("vm.memory.size[available]", item.getKey());
		assertEquals(1351090998, item.getLastClock());
		assertEquals("2552", item.getLastValue());
		assertEquals("365", item.getTrends());
	}

}
