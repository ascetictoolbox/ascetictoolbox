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

import org.dmtf.schemas.ovf.envelope.x1.XmlBeanNetworkSectionType;

import eu.ascetic.utils.ovf.api.AbstractElement;
import eu.ascetic.utils.ovf.api.factories.NetworkFactory;
import eu.ascetic.utils.ovf.api.utils.XmlSimpleTypeConverter;

/**
 * @author Django Armstrong (ULeeds)
 *
 */
public class Network extends AbstractElement<XmlBeanNetworkSectionType.Network> {
	
	/**
	 * A static reference to the {@link NetworkFactory} class for generating new
	 * instances of this object.
	 */
	public static NetworkFactory Factory = new NetworkFactory();

	public Network(XmlBeanNetworkSectionType.Network base) {
		super(base);
	}

	public String getDescription() {
		return delegate.getDescription().getStringValue();
	}

	public void setDescription(String description) {
		delegate.setDescription(XmlSimpleTypeConverter.toMsgType(description));
	}

	public String getName() {
		return delegate.getName();
	}

	public void setName(String name) {
		delegate.setName(name);
	}

}
