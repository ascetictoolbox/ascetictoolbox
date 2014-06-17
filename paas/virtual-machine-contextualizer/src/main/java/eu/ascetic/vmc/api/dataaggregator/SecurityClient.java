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
package eu.ascetic.vmc.api.dataaggregator;

import eu.ascetic.vmc.api.datamodel.ContextData;

/**
 * Class to generate keys using PKC to enable remote password free access to a
 * VM
 * 
 * @author Django Armstrong (ULeeds)
 * @version 0.0.1
 */
public class SecurityClient {

	/**
	 * Constructor initiates PKC library.
	 */
	public SecurityClient() {
		// TODO
	}

	/**
	 * Generates keys for the given context data.
	 * 
	 * @param contextData
	 *            The Context data to generate keys for.
	 * @return A new context data object with the references to the security
	 *         keys
	 */
	public ContextData generateKeys(ContextData contextData) {
		// TODO
		return contextData;
	}

}
