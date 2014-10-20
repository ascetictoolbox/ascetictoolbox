package eu.ascetic.paas.applicationmanager.amonitor;

import eu.ascetic.paas.applicationmanager.amonitor.model.EnergyCosumed;

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
 * The Interface Application Monitor Client.
 */
public interface ApplicationMonitorClient {

	/**
	 * Gets the url.
	 *
	 * @return the url
	 */
	public String getURL();

	/**
	 * Sets the url.
	 *
	 * @param url the new url
	 */
	public void setURL(String url);
	
	/**
	 * Sends the final energy consumption report to the Application Manager at the end of the execution of an application
	 * @param energyCosumed
	 * @return true if the communication with AM went ok
	 */
	public boolean postFinalEnergyConsumption(EnergyCosumed energyCosumed);
}
