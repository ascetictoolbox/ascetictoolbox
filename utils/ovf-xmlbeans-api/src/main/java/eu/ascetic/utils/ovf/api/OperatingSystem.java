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
import eu.ascetic.utils.ovf.api.factories.OperatingSystemFactory;
import eu.ascetic.utils.ovf.api.utils.XmlSimpleTypeConverter;

/**
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

	public OperatingSystem(XmlBeanOperatingSystemSectionType base) {
		super(base);
	}

	// TODO: use enum
	public int getId() {
		return delegate.getId();
	}

	// TODO: use enum
	public void setId(int operatingSystemId) {
		delegate.setId(operatingSystemId);
	}

	public String getDescription() {
		if (delegate.isSetDescription()) {
			return delegate.getDescription().getStringValue();
		}
		return null;
	}

	public void setDescription(String description) {
		delegate.setDescription(XmlSimpleTypeConverter.toMsgType(description));
	}

	public String getVersion() {
		return delegate.getVersion();
	}

	public void setVersion(String version) {
		delegate.setVersion(version);
	}

}
