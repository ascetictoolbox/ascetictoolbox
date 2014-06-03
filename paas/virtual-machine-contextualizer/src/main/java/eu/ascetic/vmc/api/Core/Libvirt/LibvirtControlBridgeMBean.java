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
package eu.ascetic.vmc.api.Core.Libvirt;

import eu.ascetic.vmc.libvirt.LibvirtException;

public interface LibvirtControlBridgeMBean {

	/**
	 * Attach an iso to a domain
	 * @param domainName The name of the Domain
	 * @param interfaceName The interface name to attach
	 * @throws LibvirtException Thrown if connecting or attach fails
	 */
	void attachIso(String domainName, String isoPath,
			String interfaceName) throws LibvirtException;

	/**
	 * Detach an iso from a domain
	 * @param domainName The name of the Domain
	 * @param interfaceName The interface name to detach
	 * @throws LibvirtException Thrown if connecting or detaching fails
	 */
	void detachIso(String domainName, String interfaceName)
			throws LibvirtException;

	/**
	 * Get the existing iso file path of a device mounted at an interface
	 * @param domainName Domain to query
	 * @param interfaceName The name of the interface of interest 
	 */
	String getExistingIsoPath(String domainName,
			String interfaceName) throws LibvirtException;

}