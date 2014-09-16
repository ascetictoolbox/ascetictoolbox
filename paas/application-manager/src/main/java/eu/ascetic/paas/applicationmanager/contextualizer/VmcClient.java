package eu.ascetic.paas.applicationmanager.contextualizer;

import org.apache.log4j.Logger;

import eu.ascetic.paas.applicationmanager.conf.Configuration;
import eu.ascetic.utils.ovf.api.OvfDefinition;
import eu.ascetic.vmc.api.VmcApi;
import eu.ascetic.vmc.api.datamodel.GlobalConfiguration;

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
 * @author: David Rojo. Atos Research and Innovation, Atos SPAIN SA
 * @email david.rojo@atos.net
 *
 * Class that connects to the VM Contextualizer API
 *
 */
public class VmcClient {
	
	private VmcApi vmcApi;
	private static Logger logger = Logger.getLogger(VmcClient.class);
	
	public VmcClient(OvfDefinition ovfDefinition){
		GlobalConfiguration globalConfiguration = null;
		try {
			globalConfiguration = new GlobalConfiguration(Configuration.vmcontextualizerConfigurationFileDirectory);
		} catch (Exception e) {
			logger.info("Error creating globalConfiguration object to connect with VM contextualizer. Details: " + e.getMessage());
		}
		if (globalConfiguration != null){
			vmcApi = new VmcApi(globalConfiguration);
		}
	}
	
	public VmcApi getVmcClient(){
		return vmcApi;
	}
}
