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

import java.util.Vector;

import org.dmtf.schemas.ovf.envelope.x1.XmlBeanNetworkSectionType;

import eu.ascetic.utils.ovf.api.AbstractElement;
import eu.ascetic.utils.ovf.api.factories.NetworkSectionFactory;
import eu.ascetic.utils.ovf.api.utils.XmlSimpleTypeConverter;

/**
 * @author Django Armstrong (ULeeds)
 *
 */
public class NetworkSection extends AbstractElement<XmlBeanNetworkSectionType> {

	/**
	 * A static reference to the {@link NetworkSectionFactory} class for
	 * generating new instances of this object.
	 */
	public static NetworkSectionFactory Factory = new NetworkSectionFactory();
	
	public NetworkSection(XmlBeanNetworkSectionType base) {
		super(base);
	}

	public Network[] getNetworkArray() {
		Vector<Network> networkArray = new Vector<Network>();
		for (XmlBeanNetworkSectionType.Network network : delegate
				.getNetworkArray()) {
			networkArray.add(new Network(network));
		}
		return networkArray.toArray(new Network[networkArray.size()]);
	}
	
	public void  setNetworkArray(Network[] networkArray) {
		Vector<XmlBeanNetworkSectionType.Network> diskArray = new Vector<XmlBeanNetworkSectionType.Network>();
		for (int i = 0; i < networkArray.length; i++) {
			diskArray.add(networkArray[i].getXmlObject());
		}
		delegate.setNetworkArray((XmlBeanNetworkSectionType.Network[]) diskArray.toArray());
	}
	
	public Network getNetworkAtIndex(int i) {
		return new Network(delegate.getNetworkArray(i));
	}
	
	public void addNetwork(Network network) {
		XmlBeanNetworkSectionType.Network newNetwork = delegate.addNewNetwork();
		newNetwork.set(network.getXmlObject());
	}

	public String getInfo() {
		return delegate.getInfo().getStringValue();
	}

	public void setInfo(String info) {
		delegate.setInfo(XmlSimpleTypeConverter.toMsgType(info));
	}
}
