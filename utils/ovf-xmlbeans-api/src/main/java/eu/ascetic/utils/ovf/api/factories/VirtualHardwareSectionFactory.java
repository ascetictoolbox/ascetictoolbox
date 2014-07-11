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
package eu.ascetic.utils.ovf.api.factories;

import org.dmtf.schemas.ovf.envelope.x1.XmlBeanVirtualHardwareSectionType;

import eu.ascetic.utils.ovf.api.VirtualHardwareSection;

/**
 * Provides factory methods for creating instances of
 * {@link VirtualHardwareSection}.
 * 
 * @author Django Armstrong (ULeeds)
 * 
 */
public class VirtualHardwareSectionFactory {

	/**
	 * Creates a new empty instance of {@link VirtualHardwareSection} with null
	 * internal object references.
	 * 
	 * @return The new VirtualHardwareSection instance
	 */
	public VirtualHardwareSection newInstance() {
		return new VirtualHardwareSection(
				XmlBeanVirtualHardwareSectionType.Factory.newInstance());
	}
}
