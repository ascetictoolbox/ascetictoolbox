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
package eu.ascetic.utils.ovf.api;

import org.dmtf.schemas.ovf.envelope.x1.XmlBeanOperatingSystemSectionType;

import eu.ascetic.utils.ovf.api.AbstractElement;
import eu.ascetic.utils.ovf.api.enums.OperatingSystemType;
import eu.ascetic.utils.ovf.api.factories.OperatingSystemFactory;
import eu.ascetic.utils.ovf.api.utils.XmlSimpleTypeConverter;

/**
 * Provides access to elements in the OperatingSystemSection of an OVF
 * document. This section specifies the operating system installed on a virtual
 * machine and is a valid section within a {@link VirtualSystem}.
 * 
 * @author Django Armstrong (ULeeds)
 * 
 */
public class OperatingSystem extends
		AbstractElement<XmlBeanOperatingSystemSectionType> {

	/**
	 * A static reference to the {@link OperatingSystemFactory} class for
	 * generating new instances of this object.
	 */
	public static OperatingSystemFactory Factory = new OperatingSystemFactory();

	/**
	 * Default constructor.
	 * 
	 * @param base
	 *            The XMLBeans base type used for data storage
	 */
	public OperatingSystem(XmlBeanOperatingSystemSectionType base) {
		super(base);
	}

	/**
	 * Gets the ID of the operating system as a {@link OperatingSystemType} representation.
	 * 
	 * @return The OperatingSystemType
	 */
	public OperatingSystemType getId() {
		return OperatingSystemType.findByNumber(delegate.getId());
	}

	/**
	 * Sets the ID of the operating system as a {@link OperatingSystemType} representation.
	 * 
	 * @param operatingSystemType The OperatingSystemType to set
	 */
	public void setId(OperatingSystemType operatingSystemType) {
		delegate.setId(operatingSystemType.getNumber());
	}

	/**
	 * Gets the human readable description of the operating system.
	 * 
	 * @return The human readable description
	 */
	public String getDescription() {
		if (delegate.isSetDescription()) {
			return delegate.getDescription().getStringValue();
		}
		return null;
	}

	/**
	 * Sets the human readable description of the operating system.
	 * 
	 * @param description The human readable description to set
	 */
	public void setDescription(String description) {
		delegate.setDescription(XmlSimpleTypeConverter.toMsgType(description));
	}

	/**
	 * Gets the version of operating system used.
	 * 
	 * @return The version
	 */
	public String getVersion() {
		return delegate.getVersion();
	}

	/**
	 * Sets the version of operating system used.
	 * 
	 * @param version The version to set
	 */
	public void setVersion(String version) {
		delegate.setVersion(version);
	}

	/**
	 * Gets the info element, a human readable description of the meaning of
	 * this section.
	 * 
	 * @return The content of the info element
	 */
	public String getInfo() {
		return delegate.getInfo().getStringValue();
	}

	/**
	 * Sets the info element, a human readable description of the meaning of
	 * this section.
	 * 
	 * @param info
	 *            The content to set within the info element
	 */
	public void setInfo(String info) {
		delegate.setInfo(XmlSimpleTypeConverter.toMsgType(info));
	}

}
