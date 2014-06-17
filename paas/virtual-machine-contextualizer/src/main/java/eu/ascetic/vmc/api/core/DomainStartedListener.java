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
package eu.ascetic.vmc.api.core;

/**
 * Listener for when new VM domains are started
 * 
 * @author Django Armstrong (ULeeds), Daniel Espling (UMU)
 * @version 0.0.1
 */
public interface DomainStartedListener {

	/**
	 * Called by EventBridge to indicate that a new domain has started. Check
	 * list of domains that are due for recontextualization and see if this is
	 * one of the missing domains that we're waiting for
	 * 
	 * @param domainName
	 *            The domain name of the new domain
	 */
	void vmDomainCreated(String domainName);

}
