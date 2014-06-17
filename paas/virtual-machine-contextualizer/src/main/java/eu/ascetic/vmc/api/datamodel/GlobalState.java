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

import java.util.HashMap;
import java.util.Map;

import eu.ascetic.utils.ovf.api.OvfDefinition;

/**
 * Class for storing the state of the VMC.
 * 
 * TODO: Add cleanup mechanism to purge completed services after X time?
 * 
 * @author Django Armstrong (ULeeds)
 * @version 0.0.4
 */
public class GlobalState {

	private GlobalConfiguration globalConfiguration;
	private Map<String, ProgressData> progressDataHashMap;
	// TODO improve this, maybe return an object
	private boolean recontextRunning = false;

	/**
	 * Default constructor that creates a {@link ProgressData} {@link Map} for
	 * storing the state of all service's being contextualized.
	 * 
	 * @param globalConfiguration
	 *            The global configuration object storing environmental
	 *            properties to setup the VMC.
	 */
	public GlobalState(GlobalConfiguration globalConfiguration) {
		this.globalConfiguration = globalConfiguration;
		progressDataHashMap = new HashMap<String, ProgressData>();
	}

	/**
	 * @return the configuration
	 */
	public GlobalConfiguration getConfiguration() {
		return globalConfiguration;
	}

	/**
	 * Adds a new instance of a {@link ProgressData} object to the
	 * {@link GlobalState} of the VMC
	 * 
	 * @param serviceId
	 *            The Service ID to create a {@link ProgressData} object for.
	 */
	public void addProgress(String serviceId) {
		progressDataHashMap.put(serviceId, new ProgressData());
	}

	/**
	 * Sets the current progress phase for a given service
	 * 
	 * @param serviceId
	 *            The Service ID to set the progress phase for.
	 * @param currentPhaseId
	 *            The current phase ID to set (see {@link ProgressData})
	 */
	public void setProgressPhase(String serviceId, int currentPhaseId) {
		ProgressData progressData = progressDataHashMap.get(serviceId);
		progressData.setCurrentPhaseId(currentPhaseId);
		progressDataHashMap.put(serviceId, progressData);
	}

	/**
	 * Sets the current progress percentage for the phase of a given service
	 * 
	 * @param serviceId
	 *            The Service ID to set the progress phase for.
	 * @param currentPercentageCompletion
	 *            The current completion % of a phase to set (see
	 *            {@link ProgressData}).
	 */
	public void setProgressPercentage(String serviceId,
			Double currentPercentageCompletion) {
		ProgressData progressData = progressDataHashMap.get(serviceId);
		progressData
				.setCurrentPercentageCompletion(currentPercentageCompletion);
		progressDataHashMap.put(serviceId, progressData);
	}

	/**
	 * Get the ProgressData object for a given Service ID.
	 * 
	 * @param serviceId
	 *            The Service ID of the ProgressData object to get.
	 * @return The ProgressData object to return.
	 */
	public ProgressData getProgressData(String serviceId) {
		return progressDataHashMap.get(serviceId);
	}

	/**
	 * Set the ovfDefinition to be returned through the API via a services
	 * associated ProgressData object.
	 * 
	 * @param ovfDefinition
	 *            The ovfDefinition to return.
	 */
	public void setOvfDefinition(OvfDefinition ovfDefinition) {
		// FIXME: Should we be getting the serviceId from a property here,
		// confirm with consortium?
		progressDataHashMap.get(
				ovfDefinition.getVirtualSystemArray(0).getProductSection()
						.getPropertyByKey("serviceId").getValue())
				.setOvfDefinition(ovfDefinition);
	}

	/**
	 * Specify that a service has finished the contextualization process.
	 * 
	 * @param serviceId
	 *            The service ID to set as complete.
	 */
	public void setComplete(String serviceId) {
		progressDataHashMap.get(serviceId).setComplete(true);
	}

	/**
	 * Returns a string representing the progress of a service for logging
	 * purposes.
	 * 
	 * @param serviceId
	 *            The service ID to get a progress string for.
	 * @return The string representation of the progress.
	 */
	public String getProgressLogString(String serviceId) {
		return progressDataHashMap.get(serviceId).getProgressString()
				+ " [serviceId: " + serviceId + "]";
	}

	/**
	 * Returns the status of recontext thread.
	 * 
	 * @return the recontextRunning
	 */
	public boolean isRecontextRunning() {
		return recontextRunning;
	}

	/**
	 * Sets the status of recontext thread.
	 * 
	 * @param recontextRunning
	 *            the recontextRunning to set
	 */
	public void setRecontextRunning(boolean recontextRunning) {
		this.recontextRunning = recontextRunning;
	}
}
