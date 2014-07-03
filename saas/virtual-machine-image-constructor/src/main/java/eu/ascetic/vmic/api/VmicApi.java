/**
 *  Copyright 2014 University of Leeds
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
package eu.ascetic.vmic.api;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

import org.apache.log4j.Logger;

import eu.ascetic.utils.ovf.api.OvfDefinition;
import eu.ascetic.vmic.api.core.ProgressException;
import eu.ascetic.vmic.api.datamodel.ProgressData;
import eu.ascetic.vmic.api.core.VirtualMachineImageConstructor;
import eu.ascetic.vmic.api.datamodel.GlobalConfiguration;
import eu.ascetic.vmic.api.datamodel.GlobalState;

/**
 * Implementation of the core API providing access to VMIC functionality
 * 
 * @author Django Armstrong (ULeeds)
 *
 */
public class VmicApi implements Api {

	protected static final Logger LOGGER = Logger
			.getLogger(VmicApi.class);
	
	private GlobalState globalState;
	private Map<String, Thread> threads;
	
	/**
	 * Constructor for setting up the VMIC with configuration data
	 * 
	 * @param globalConfiguration The config data to use
	 */
	public VmicApi(GlobalConfiguration globalConfiguration) {
		globalState = new GlobalState(globalConfiguration);
		threads = new HashMap<String, Thread>();
	}

	/* (non-Javadoc)
	 * @see eu.ascetic.vmic.api.Api#generateImage(eu.ascetic.utils.ovf.api.OvfDefinition)
	 */
	@Override
	public void generateImage(OvfDefinition ovfDefinition) {
		Runnable virtualMachineImageConstructor = new VirtualMachineImageConstructor(
				this, ovfDefinition);
		Thread thread = new Thread(virtualMachineImageConstructor);
		
		threads.put(ovfDefinition.getVirtualSystemCollection().getId(), thread);
		thread.start();	
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see eu.ascetic.vmc.api.Api#contextualizeServiceCallback(java.lang.String)
	 */
	public ProgressData progressCallback(String serviceId)
			throws ProgressException {
		// If there is no configuration then no contextualization threads are
		// running...
		if (this.globalState == null) {
			throw new ProgressException(
					"No previous call to contextualizeService()");
		} else {

			ProgressData progressData = globalState.getProgressData(serviceId);
			if (progressData == null) {
				throw new ProgressException("Service does not exist with id: "
						+ serviceId);
			} else {
				return progressData;
			}
		}
	}
	
	/* (non-Javadoc)
	 * @see eu.ascetic.vmic.api.Api#uploadFile(java.lang.String, java.io.File)
	 */
	@Override
	public String uploadFile(String ovfDefinitionId, File file) {
		// FIXME: Change this method once the interface technology has been selected for this component to enable remote invocation
		return null;
	}

}
