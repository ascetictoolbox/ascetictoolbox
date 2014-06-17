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
package eu.ascetic.vmc.api;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.apache.log4j.Logger;

import eu.ascetic.utils.ovf.api.OvfDefinition;
import eu.ascetic.vmc.api.core.ProgressException;
import eu.ascetic.vmc.api.core.VirtualMachineContextualizer;
import eu.ascetic.vmc.api.core.VirtualMachineRecontextualizer;
import eu.ascetic.vmc.api.datamodel.GlobalConfiguration;
import eu.ascetic.vmc.api.datamodel.GlobalState;
import eu.ascetic.vmc.api.datamodel.ProgressData;

/**
 * Core API to access VM Contextualizer
 * 
 * @author Django Armstrong (ULeeds)
 * @version 0.0.5
 */
public class VmcApi implements Api {

	protected static final Logger LOGGER = Logger
			.getLogger(VmcApi.class);

	private GlobalState globalState;
	private Map<String, Thread> threads;
	private Thread recontextThread;
	private VirtualMachineRecontextualizer virtualMachineRecontextualizer;

	/**
	 * Constructor providing a configuration to VMC
	 * 
	 * @param globalConfiguration
	 *            The configuration object to pass (see
	 *            {@link GlobalConfiguration})
	 */
	public VmcApi(GlobalConfiguration globalConfiguration) {
		globalState = new GlobalState(globalConfiguration);
		threads = new HashMap<String, Thread>();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * eu.ascetic.vmc.api.Api#contextualizeService(eu.ascetic.utils.ovf.api.OvfDefinition)
	 */
	public void contextualizeService(OvfDefinition ovfDefinition) {
		
		Runnable virtualMachineContextualizer = new VirtualMachineContextualizer(
				this, ovfDefinition);
		Thread thread = new Thread(virtualMachineContextualizer);
		
		// FIXME: Should we be getting the serviceId from a property here, confirm with consortium? 
		threads.put(ovfDefinition.getVirtualSystemArray(0).getProductSection().getPropertyByKey("serviceId").getValue(), thread);
		thread.start();
	}

	/**
	 * Overloaded method for conversion of VM images until support is available
	 * in the OVF Definition for specifying desired format. Given an object of
	 * OvfDefinition containing the details of the service to be
	 * contextualized, this function performs a number of operations
	 * asynchronously.
	 * 
	 * @param ovfDefinition
	 *            Ovf Definition to contextualize.
	 * @param imageFormat
	 *            Format to convert images to. Possible values are: "raw",
	 *            "vmdk" and "qcow2"
	 */
	public void contextualizeService(OvfDefinition ovfDefinition, String imageFormat) {
		Runnable virtualMachineContextualizer = new VirtualMachineContextualizer(
				this, ovfDefinition, imageFormat);
		Thread thread = new Thread(virtualMachineContextualizer);
		// FIXME: Should we be getting the serviceId from a property here, confirm with consortium? 
		threads.put(ovfDefinition.getVirtualSystemArray(0).getProductSection().getPropertyByKey("serviceId").getValue(), thread);
		thread.start();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see eu.ascetic.vmc.api.Api#contextualizeServiceCallback(java.lang.String)
	 */
	public ProgressData contextualizeServiceCallback(String serviceId)
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

	/**
	 * Given a OVF definition ID, wait for the associated contextualization thread to
	 * exit. This should only be used when performing serialised unit tests for
	 * the purpose of improving logger readability.
	 * 
	 * @param ovfDefinitionId
	 *            Contextualize thread for a given OVF Definition ID to wait for.
	 */
	public void contextualizeServiceJoin(String ovfDefinitionId) {
		try {
			threads.get(ovfDefinitionId).join();
		} catch (InterruptedException e) {
			LOGGER.error("Error!", e);
		}
	}

	/**
	 * Starts the recontextualization process.
	 * 
	 */
	public void recontextualize(String hypervisorString) {
		try {
			virtualMachineRecontextualizer = new VirtualMachineRecontextualizer(
					this, hypervisorString);
			recontextThread = new Thread(virtualMachineRecontextualizer);
			recontextThread.start();
		} catch (IOException e) {
			LOGGER.error("Failed to start Recontextualizer", e);
		}
	}
	
	/**
	 * Recontextualization a specific domain.
	 * 
	 */
	public void startRecontext(String domainName) {
			if (virtualMachineRecontextualizer != null) {
				try {
					virtualMachineRecontextualizer.startRecontextualization(domainName);
				} catch (IOException e) {
					LOGGER.error("Failed to start Recontextualizer on specific domain", e);
				}
			}
	}

	/**
	 * Provides details on the status of recontextualization.
	 * 
	 * @return The status of the recontextualization .
	 * 
	 * @throws ProgressException
	 *             Thrown if no recontextualization process running.
	 */
	public boolean recontextualizeStatus() throws ProgressException {
		// If there is no configuration then no recontextualization threads are
		// running...
		if (this.globalState == null) {
			throw new ProgressException("No previous call to recontextualize()");
		} else {
			// TODO: Get some more useful info here, possibly as an object
			return this.globalState.isRecontextRunning();
		}
	}

	/**
	 * Waits for the recontextualization thread to finish.
	 * 
	 */
	public void recontextualizeJoin() {
		try {
			recontextThread.join();
		} catch (InterruptedException e) {
			LOGGER.error("Error!", e);
		}
	}

	/**
	 * Stops (via an interrupt) the recontextualization thread.
	 */
	public void recontextualizeStop() {
		recontextThread.interrupt();
	}

	/**
	 * Getter for fetching the global state object.
	 * 
	 * @return the globalState object
	 */
	public synchronized GlobalState getGlobalState() {
		return globalState;
	}

}
