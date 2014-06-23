/**
 *  Copyright 2013 University of Leeds
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package eu.ascetic.vmc.api.datamodel;

import eu.ascetic.utils.ovf.api.OvfDefinition;
import eu.ascetic.vmc.api.dataaggregator.SecurityClient;
import eu.ascetic.vmc.api.dataaggregator.OvfDefinitionClient;

/**
 * Contains functions for the purpose of gathering context data and storing the
 * ContextData object of a service.
 * 
 * @author Django Armstrong (ULeeds)
 * @version 0.0.2
 */
public class Service {

	private String serviceId;
	private OvfDefinitionClient ovfDefinitionClient;
	private ContextData contextData;
	private SecurityClient securityClient;

	/**
	 * Default Constructor
	 * 
	 * @param ovfDefinition
	 *            The OVF to associate with a given service.
	 */
	public Service(OvfDefinition ovfDefinition) {
		if (ovfDefinition != null) {
			
			// FIXME: Should we be getting the serviceId from a property here, confirm with consortium? 
			serviceId = ovfDefinition.getVirtualSystemCollection().getId();
			ovfDefinitionClient = new OvfDefinitionClient(ovfDefinition);
		}
	}

	/**
	 * Parses the OVF Definition for contextualization data
	 */
	public void parseOvfDefinition() {
		contextData = ovfDefinitionClient.parse();
	}

	/**
	 * Generates contextualization data from other ASCETiC components
	 */
	public void generateContextData() {
		securityClient = new SecurityClient();
		contextData = securityClient.generateKeys(contextData);
	}

	/**
	 * Getter for ContextData objects
	 * 
	 * @return the contextData
	 */
	public ContextData getContextData() {
		return contextData;
	}

	/**
	 * Setter for ContextData objects
	 * 
	 * @param contextData
	 *            the contextData to set
	 */
	public void setContextData(ContextData contextData) {
		this.contextData = contextData;
	}

	/**
	 * Getter for Service ID
	 * 
	 * @return the serviceId
	 */
	public String getServiceId() {
		return serviceId;
	}

	/**
	 * FIXME: Not currently used
	 * 
	 * @return the securityClient
	 */
	public SecurityClient getSecurityClient() {
		return securityClient;
	}

	/**
	 * FIXME: Not currently used
	 * 
	 * @param securityClient the securityClient to set
	 */
	public void setSecurityClient(SecurityClient securityClient) {
		this.securityClient = securityClient;
	}

}
