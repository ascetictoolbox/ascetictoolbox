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
 * Provides access to elements within the NetworkSection of an OVF document. The
 * NetworkSection describes meta-information about virtual networks and lists
 * all logical networks used in the OVF package. All networks referred to from
 * Connection elements (see {@link Item}) in all {@link VirtualHardwareSection}
 * elements are defined in the NetworkSection. Virtual networks and their
 * metadata are described outside the virtual hardware to facilitate sharing
 * between virtual machines within an OVF document.
 * 
 * @author Django Armstrong (ULeeds)
 * 
 */
public class NetworkSection extends AbstractElement<XmlBeanNetworkSectionType> {

	/**
	 * A static reference to the {@link NetworkSectionFactory} class for
	 * generating new instances of this object.
	 */
	public static NetworkSectionFactory Factory = new NetworkSectionFactory();

	/**
	 * Default constructor.
	 * 
	 * @param base
	 *            The XMLBeans base type used for data storage
	 */
	public NetworkSection(XmlBeanNetworkSectionType base) {
		super(base);
	}

	/**
	 * Gets the {@link Network} array held in this object.
	 * 
	 * @return The Network[]
	 */
	public Network[] getNetworkArray() {
		Vector<Network> networkArray = new Vector<Network>();
		for (XmlBeanNetworkSectionType.Network network : delegate
				.getNetworkArray()) {
			networkArray.add(new Network(network));
		}
		return networkArray.toArray(new Network[networkArray.size()]);
	}

	/**
	 * Sets the {@link Network} array held in this object.
	 * 
	 * @param networkArray The Network[] to set
	 */
	public void setNetworkArray(Network[] networkArray) {
		Vector<XmlBeanNetworkSectionType.Network> newNetworkArray = new Vector<XmlBeanNetworkSectionType.Network>();
		for (int i = 0; i < networkArray.length; i++) {
			newNetworkArray.add(networkArray[i].getXmlObject());
		}
		delegate.setNetworkArray((XmlBeanNetworkSectionType.Network[]) newNetworkArray
				.toArray());
	}

	/**
	 * Gets the {@link Network} element at index i of the array.
	 * 
	 * @param i
	 *            The index value
	 * @return The Network at index i
	 */
	public Network getNetworkAtIndex(int i) {
		return new Network(delegate.getNetworkArray(i));
	}

	/**
	 * Adds a new {@link Network} element to the end of the Network array.
	 * 
	 * @param network
	 *            The Network to add to the end of the array.
	 */
	public void addNetwork(Network network) {
		XmlBeanNetworkSectionType.Network newNetwork = delegate.addNewNetwork();
		newNetwork.set(network.getXmlObject());
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
