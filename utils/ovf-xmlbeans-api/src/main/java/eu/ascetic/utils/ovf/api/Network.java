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
import eu.ascetic.utils.ovf.api.utils.XmlSimpleTypeConverter;

/**
 * Provides access to the Network element of an OVF document. A Network
 * contained within the {@link NetworkSection} stores the network name for cross
 * referencing in the hardware description of a virtual machine (see
 * {@link VirtualHardwareSection} and {@link Item}) and provides a human
 * readable descriptive.
 * 
 * @author Django Armstrong (ULeeds)
 * 
 */
public class Network extends AbstractElement<XmlBeanNetworkSectionType.Network> {

    /**
     * A static reference to the {@link NetworkFactory} class for generating new
     * instances of this object.
     */
    public static NetworkFactory Factory = new NetworkFactory();

    /**
     * Default constructor.
     * 
     * @param base
     *            The XMLBeans base type used for data storage
     */
    public Network(XmlBeanNetworkSectionType.Network base) {
        super(base);
    }

    /**
     * Gets the description of the Network element. The Description is used to
     * provide additional metadata about the Network. This element enables a
     * consumer of the OVF package to provide descriptive information about a
     * network.
     * 
     * @return The description of the Network
     */
    public String getDescription() {
        return delegate.getDescription().getStringValue();
    }

    /**
     * Sets the description of the Network element. The Description element is
     * used to provide additional metadata about the Network element. This
     * element enables a consumer of the OVF package to provide descriptive
     * information about a network.
     * 
     * @param description
     *            The description of the Network to set
     */
    public void setDescription(String description) {
        delegate.setDescription(XmlSimpleTypeConverter.toMsgType(description));
    }

    /**
     * Gets the unique network name referenced by the Connection element of
     * {@link Item} in the following methods:<br>
     * <br>
     * {@link Item#addConnection(String)}<br>
     * {@link Item#setConnectionArray(String[])}<br>
     * {@link Item#getConnectionArray()}<br>
     * {@link Item#getConnectionAtIndex(int)}<br>
     * 
     * @return The name to the Network
     */
    public String getName() {
        return delegate.getName();
    }

    /**
     * Sets the unique network name referenced by the Connection element of
     * {@link Item} in the following methods:<br>
     * <br>
     * {@link Item#addConnection(String)}<br>
     * {@link Item#setConnectionArray(String[])}<br>
     * {@link Item#getConnectionArray()}<br>
     * {@link Item#getConnectionAtIndex(int)}<br>
     * 
     * @param name
     *            The name to the Network to set
     */
    public void setName(String name) {
        delegate.setName(name);
    }

}
