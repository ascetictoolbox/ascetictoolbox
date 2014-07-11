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

import org.dmtf.schemas.ovf.envelope.x1.XmlBeanVSSDType;

import eu.ascetic.utils.ovf.api.AbstractElement;
import eu.ascetic.utils.ovf.api.factories.SystemFactory;
import eu.ascetic.utils.ovf.api.utils.XmlSimpleTypeConverter;

/**
 * @author Django Armstrong (ULeeds)
 * 
 */
public class System extends AbstractElement<XmlBeanVSSDType> {

	/**
	 * A static reference to the {@link SystemFactory} class for generating new
	 * instances of this object.
	 */
	public static SystemFactory Factory = new SystemFactory();

	public System(XmlBeanVSSDType base) {
		super(base);
	}

	public String getElementName() {
		return delegate.getElementName().getStringValue();
	}

	public void setElementName(String elementName) {
		delegate.setElementName(XmlSimpleTypeConverter.toCimString(elementName));
	}

	public String getInstanceID() {
		return delegate.getInstanceID().getStringValue();
	}

	public void setInstanceID(String instanceID) {
		delegate.setInstanceID(XmlSimpleTypeConverter.toCimString(instanceID));
	}

	public String getVirtualSystemType() {
		return delegate.getVirtualSystemType().getStringValue();
	}

	public void setVirtualSystemType(String virtualSystemType) {
		if (!delegate.isSetVirtualSystemType()) {
			delegate.addNewVirtualSystemType();
		}
		delegate.setVirtualSystemType(XmlSimpleTypeConverter
				.toCimString(virtualSystemType));
	}

}
