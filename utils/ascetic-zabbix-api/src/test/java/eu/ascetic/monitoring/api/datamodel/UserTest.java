package eu.ascetic.monitoring.api.datamodel;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

import eu.ascetic.asceticarchitecture.iaas.zabbixApi.datamodel.User;

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
* Java representation of tests for user class
* 
*/
public class UserTest {

	/**
	 * Pojo test.
	 */
	@Test
	public void pojoTest() {
		User user = new User("Admin", "zabbix");
		user.setUserid("1");
		user.setAuth("cc911407fd49dcea238da2654d5ee929");
		
		assertEquals("Admin", user.getLogin());
		assertEquals("zabbix", user.getPassword());
		assertEquals("cc911407fd49dcea238da2654d5ee929", user.getAuth());
		assertEquals("1", user.getUserid());
	}
}
