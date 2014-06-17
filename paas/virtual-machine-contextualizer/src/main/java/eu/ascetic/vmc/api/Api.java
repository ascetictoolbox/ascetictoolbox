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

import eu.ascetic.utils.ovf.api.OvfDefinition;
import eu.ascetic.vmc.api.core.ProgressException;
import eu.ascetic.vmc.api.datamodel.ProgressData;

/**
 * Static Year2 Interface to VMC capabilities
 * 
 * @author Django Armstrong (ULeeds)
 * @version 0.0.4
 */
public interface Api {

	/**
	 * Given an object of OvfDefinition containing the details of the service
	 * to be contextualized, this function performs a number of operations
	 * asynchronously.
	 * 
	 * @param ovfDefinition
	 *            OvfDefinition to contextualize.
	 */
	void contextualizeService(OvfDefinition ovfDefinition);

	/**
	 * Given a ServiceId this function returns the progress status and
	 * percentage completion of a previous call to contextualizeService() as a
	 * ProgressData object.
	 * 
	 * @param serviceId
	 *            Service ID to check progress of.
	 * @return ProgressData object with status of contextualization (see
	 *         {@link ProgressData})
	 * @throws ProgressException
	 *             Exception on failure to get progress string.
	 */
	ProgressData contextualizeServiceCallback(String serviceId)
			throws ProgressException;
}
