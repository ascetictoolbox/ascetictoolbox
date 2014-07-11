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

import java.math.BigInteger;
import java.util.Vector;

import org.dmtf.schemas.ovf.envelope.x1.XmlBeanRASDType;
import org.dmtf.schemas.wbem.wscim.x1.common.CimString;

import eu.ascetic.utils.ovf.api.AbstractElement;
import eu.ascetic.utils.ovf.api.utils.XmlSimpleTypeConverter;
import eu.ascetic.utils.ovf.api.ResourceType;

/**
 * Provides access to Virtual Hardware Elements (Item) contained within
 * {@link VirtualHardwareSection}. An Item represents virtual hardware
 * characteristics and can describe all memory and CPU requirements as well as
 * virtual hardware devices.<br>
 * <br>
 * TODO: Add support for the ovf:bound attribute (min, max, normal).<br>
 * TODO: Add support for the rasd:ResourceSubType element to support vendor
 * specific virtual hardware.
 * 
 * @author Django Armstrong (ULeeds)
 * 
 */
public class Item extends AbstractElement<XmlBeanRASDType> {

	/**
	 * A static reference to the {@link ItemFactory} class for generating new
	 * instances of this object.
	 */
	public static ItemFactory Factory = new ItemFactory();

	/**
	 * Default constructor.
	 * 
	 * @param base
	 *            The XMLBeans base type used for data storage
	 */
	public Item(XmlBeanRASDType base) {
		super(base);
	}

	/**
	 * Gets the description of the virtual hardware Item element. The
	 * Description element is used to provide additional metadata about the Item
	 * element itself. This element enables a consumer of the OVF package to
	 * provide descriptive information about all items, including items that
	 * were unknown at the time the application was written.
	 * 
	 * @return The description of the Item
	 */
	public String getDescription() {
		if (delegate.isSetDescription()) {
			return delegate.getDescription().getStringValue();
		}
		return null;
	}

	/**
	 * Sets the description of the virtual hardware Item element. The
	 * Description element is used to provide additional metadata about the Item
	 * element itself. This element enables a consumer of the OVF package to
	 * provide descriptive information about all items, including items that
	 * were unknown at the time the application was written.
	 * 
	 * @param description
	 *            The description of the Item to set
	 */
	public void setDescription(String description) {

		if (delegate.isSetDescription()) {
			delegate.unsetResourceType();
		}

		delegate.setDescription(XmlSimpleTypeConverter.toCimString(description));
	}

	/**
	 * Gets the element name, a human-readable description of the content. For
	 * example, "256MB memory".
	 * 
	 * @return The element name
	 */
	public String getElementName() {
		return delegate.getElementName().getStringValue();
	}

	/**
	 * Sets the element name, a human-readable description of the content. For
	 * example, "256MB memory".
	 * 
	 * @param elementName
	 *            The element name to set
	 */
	public void setElementName(String elementName) {
		delegate.setElementName(XmlSimpleTypeConverter.toCimString(elementName));
	}

	/**
	 * Gets the instance ID, a unique instance ID of the element within the
	 * {@link VirtualHardwareSection}.
	 * 
	 * @return The instance ID
	 */
	public String getInstanceID() {
		return delegate.getInstanceID().getStringValue();
	}

	/**
	 * Sets the instance ID, a unique instance ID of the element within the
	 * {@link VirtualHardwareSection}.
	 * 
	 * @param instanceId
	 *            The instance ID to set
	 */
	public void setInstanceId(String instanceId) {
		delegate.setInstanceID(XmlSimpleTypeConverter.toCimString(instanceId));
	}

	/**
	 * Gets the resource type that specifies the kind of device that is being
	 * described.
	 * 
	 * @return The {@link ResourceType}
	 */
	public ResourceType getResourceType() {
		return ResourceType.findByNumber(delegate.getResourceType()
				.getIntValue());
	}

	/**
	 * Gets the resource type that specifies the kind of device that is being
	 * described.
	 * 
	 * @param resourceType
	 *            The {@link ResourceType} to set
	 */
	public void setResourceType(ResourceType resourceType) {

		if (delegate.isSetResourceType()) {
			delegate.unsetResourceType();
		}

		// @formatter:off
		org.dmtf.schemas.wbem.wscim.x1.cimSchema.x2.cimResourceAllocationSettingData.
			XmlBeanResourceTypeDocument.ResourceType newResourceType;
		// @formatter:on

		newResourceType = delegate.addNewResourceType();
		newResourceType.setIntValue(resourceType.getNumber());
		delegate.setResourceType(newResourceType);
	}

	/**
	 * Gets the specified quantity of resources presented to the guest operating
	 * system. For example, "256".
	 * 
	 * @return The virtual quantity of this resource type
	 */
	public BigInteger getVirtualQuantity() {
		if (delegate.isSetVirtualQuantity()) {
			return delegate.getVirtualQuantity().getBigIntegerValue();
		}
		return null;
	}

	/**
	 * Sets the specified quantity of resources presented to the guest operating
	 * system. For example, "256".
	 * 
	 * @param virtualQuantity
	 *            The virtual quantity of this resource type to set
	 */
	public void setVirtualQuantity(BigInteger virtualQuantity) {
		if (delegate.isSetVirtualQuantity()) {
			delegate.unsetVirtualQuantity();
		}
		delegate.setVirtualQuantity(XmlSimpleTypeConverter
				.toCimUnsignedLong(virtualQuantity.longValue()));
	}

	/**
	 * Gets the specified units of allocation to use. Values for allocationUnits
	 * must match the format for programmatic units defined in DSP0004.C1 with
	 * the restriction that the base unit is "byte".<br>
	 * (see {@link http://www.dmtf.org/standards/cim}).<br>
	 * <br>
	 * For example:<br>
	 * <br>
	 * 1 GByte = "byte * 2^30"<br>
	 * 1 MByte = "byte * 2^20"<br>
	 * 1 KByte = "byte * 2^10"
	 * 
	 * @return The allocation units
	 */
	public String getAllocationUnits() {
		if (delegate.isSetAllocationUnits()) {
			return delegate.getAllocationUnits().getStringValue();
		}
		return null;
	}

	/**
	 * Sets the specified units of allocation to use. Values for allocationUnits
	 * must match the format for programmatic units defined in DSP0004.C1 with
	 * the restriction that the base unit is "byte".<br>
	 * (see {@link http://www.dmtf.org/standards/cim}).<br>
	 * <br>
	 * For example:<br>
	 * <br>
	 * 1 GByte = "byte * 2^30"<br>
	 * 1 MByte = "byte * 2^20"<br>
	 * 1 KByte = "byte * 2^10"
	 * 
	 * @param allocationUnits
	 *            The allocation units to set
	 */
	public void setAllocationUnits(String allocationUnits) {
		if (delegate.isSetAllocationUnits()) {
			delegate.unsetAllocationUnits();
		}
		delegate.setAllocationUnits(XmlSimpleTypeConverter
				.toCimString(allocationUnits));
	}

	/**
	 * Gets automatic allocation policy for this resource type. For devices that
	 * are connectable, such as floppies, CD-ROMs, and Ethernet adaptors, this
	 * element specifies whether the device should be connected at power on.
	 * 
	 * @return The automatic allocation policy
	 */
	public Boolean getAutomaticAllocation() {
		return delegate.getAutomaticAllocation().getBooleanValue();
	}

	/**
	 * Sets automatic allocation policy for this resource type. For devices that
	 * are connectable, such as floppies, CD-ROMs, and Ethernet adaptors, this
	 * element specifies whether the device should be connected at power on.
	 * 
	 * @param automaticAllocation
	 *            The automatic allocation policy to set
	 */
	public void setAutomaticAllocation(Boolean automaticAllocation) {
		delegate.setAutomaticAllocation(XmlSimpleTypeConverter
				.toCimBoolean(automaticAllocation));
	}

	/**
	 * Gets the connection array for this resource type. For an Ethernet
	 * adapter, this specifies the abstract network connection name for the
	 * virtual machine. All Ethernet adapters that specify the same abstract
	 * network connection name within an OVF package shall be deployed on the
	 * same network. The abstract network connection name is listed in the
	 * {@link NetworkSection}.
	 * 
	 * @return The connection array for an Ethernet adapter
	 */
	public String[] getConnectionArray() {
		Vector<String> vector = new Vector<String>();
		for (CimString type : delegate.getConnectionArray()) {
			vector.add(type.getStringValue());
		}
		return vector.toArray(new String[vector.size()]);
	}

	/**
	 * Sets the connection array for this resource type. For an Ethernet
	 * adapter, this specifies the abstract network connection name for the
	 * virtual machine. All Ethernet adapters that specify the same abstract
	 * network connection name within an OVF package shall be deployed on the
	 * same network. The abstract network connection name is listed in the
	 * {@link NetworkSection}.
	 * 
	 * @param connectionArray
	 *            The connection array for an Ethernet adapter to set
	 */
	public void setConnectionArray(String[] connectionArray) {
		Vector<CimString> newConnectionArray = new Vector<CimString>();
		for (int i = 0; i < connectionArray.length; i++) {
			CimString cimString = CimString.Factory.newInstance();
			cimString.setStringValue(connectionArray[i]);
			newConnectionArray.add(cimString);
		}
		delegate.setConnectionArray((CimString[]) newConnectionArray.toArray());
	}

	/**
	 * Gets a connection at a specific index from the connection array. For an
	 * Ethernet adapter, this specifies the abstract network connection name for
	 * the virtual machine. All Ethernet adapters that specify the same abstract
	 * network connection name within an OVF package shall be deployed on the
	 * same network. The abstract network connection name is listed in the
	 * {@link NetworkSection}.
	 * 
	 * @param i
	 *            The index value
	 * @return The connection for an Ethernet adapter at a specific index.
	 */
	public String getConnectionAtIndex(int i) {
		return delegate.getConnectionArray(i).getStringValue();
	}

	/**
	 * Adds a new connection at the end of the connection array. For an Ethernet
	 * adapter, this specifies the abstract network connection name for the
	 * virtual machine. All Ethernet adapters that specify the same abstract
	 * network connection name within an OVF package shall be deployed on the
	 * same network. The abstract network connection name is listed in the
	 * {@link NetworkSection}.
	 * 
	 * @param connection
	 *            The string value of the connection to add.
	 */
	public void addConnection(String connection) {
		CimString cimString = delegate.addNewConnection();
		cimString.setStringValue(connection);
	}

	/**
	 * Gets the InstanceID of the parent controller (if any).
	 * 
	 * @return The instance ID of the parent controller
	 */
	public String getParent() {
		if (delegate.isSetParent()) {
			return delegate.getParent().getStringValue();
		}
		return null;
	}

	/**
	 * Sets the InstanceID of the parent controller (if any).
	 * 
	 * @param parent
	 *            The instance ID of the parent controller to set
	 */
	public void setParent(String parent) {
		if (delegate.isSetParent()) {
			delegate.unsetParent();
		}
		delegate.setParent(XmlSimpleTypeConverter.toCimString(parent));
	}

	/**
	 * Gets the host resource array. A HostResource is used to refer to
	 * resources included in the OVF descriptor as well as logical devices on
	 * the deployment platform. Abstractly it specifies how a device shall
	 * connect to a resource on the deployment platform. Values for HostResource
	 * referring to resources included in the OVF descriptor are formatted as
	 * URIs and are specified as follows:<br>
	 * <br>
	 * ovf:/file/&lt;id&gt; - A reference to a file in the OVF, as specified in
	 * {@link References}. &lt;id> is the value of the id attribute of the
	 * {@link File} being referenced.<br>
	 * <br>
	 * ovf:/disk/&lt;id&gt; - A reference to a virtual disk, as specified in the
	 * {@link DiskSection}. &lt;id&gt; is the value of the diskId attribute of
	 * the {@link Disk} element being referenced.
	 * 
	 * @return The host resource array
	 */
	public String[] getHostResourceArray() {
		Vector<String> vector = new Vector<String>();
		for (CimString cimString : delegate.getHostResourceArray()) {
			vector.add(cimString.getStringValue());
		}
		return vector.toArray(new String[vector.size()]);
	}

	/**
	 * Sets the host resource array. A HostResource is used to refer to
	 * resources included in the OVF descriptor as well as logical devices on
	 * the deployment platform. Abstractly it specifies how a device shall
	 * connect to a resource on the deployment platform. Values for HostResource
	 * referring to resources included in the OVF descriptor are formatted as
	 * URIs and are specified as follows:<br>
	 * <br>
	 * ovf:/file/&lt;id&gt; - A reference to a file in the OVF, as specified in
	 * {@link References}. &lt;id> is the value of the id attribute of the
	 * {@link File} being referenced.<br>
	 * <br>
	 * ovf:/disk/&lt;id&gt; - A reference to a virtual disk, as specified in the
	 * {@link DiskSection}. &lt;id&gt; is the value of the diskId attribute of
	 * the {@link Disk} element being referenced.
	 * 
	 * @param hostResourceArray
	 *            The host resource array to set
	 */
	public void setHostResourceArray(String[] hostResourceArray) {
		Vector<CimString> newHostResourceArray = new Vector<CimString>();
		for (int i = 0; i < hostResourceArray.length; i++) {
			CimString cimString = CimString.Factory.newInstance();
			cimString.setStringValue(hostResourceArray[i]);
			newHostResourceArray.add(cimString);
		}
		delegate.setHostResourceArray((CimString[]) newHostResourceArray
				.toArray());
	}

	/**
	 * Gets a host resource from a specific index within the array. A
	 * HostResource is used to refer to resources included in the OVF descriptor
	 * as well as logical devices on the deployment platform. Abstractly it
	 * specifies how a device shall connect to a resource on the deployment
	 * platform. Values for HostResource referring to resources included in the
	 * OVF descriptor are formatted as URIs and are specified as follows:<br>
	 * <br>
	 * ovf:/file/&lt;id&gt; - A reference to a file in the OVF, as specified in
	 * {@link References}. &lt;id> is the value of the id attribute of the
	 * {@link File} being referenced.<br>
	 * <br>
	 * ovf:/disk/&lt;id&gt; - A reference to a virtual disk, as specified in the
	 * {@link DiskSection}. &lt;id&gt; is the value of the diskId attribute of
	 * the {@link Disk} element being referenced.
	 * 
	 * @param i
	 *            The index value
	 * @return The host resource
	 */
	public String getHostResourceArray(int i) {
		if (delegate.getHostResourceArray().length > i) {
			return delegate.getHostResourceArray(i).getStringValue();
		}
		return null;
	}

	/**
	 * Adds a host resource to the end of the array. A HostResource is used to
	 * refer to resources included in the OVF descriptor as well as logical
	 * devices on the deployment platform. Abstractly it specifies how a device
	 * shall connect to a resource on the deployment platform. Values for
	 * HostResource referring to resources included in the OVF descriptor are
	 * formatted as URIs and are specified as follows:<br>
	 * <br>
	 * ovf:/file/&lt;id&gt; - A reference to a file in the OVF, as specified in
	 * {@link References}. &lt;id> is the value of the id attribute of the
	 * {@link File} being referenced.<br>
	 * <br>
	 * ovf:/disk/&lt;id&gt; - A reference to a virtual disk, as specified in the
	 * {@link DiskSection}. &lt;id&gt; is the value of the diskId attribute of
	 * the {@link Disk} element being referenced.
	 * 
	 * @param hostResource
	 *            The string representation of the host resource to add
	 */
	public void addHostResource(String hostResource) {
		CimString cimString = delegate.addNewHostResource();
		cimString.setStringValue(hostResource);
	}

	/**
	 * Returns the ID of the referenced file or disk from a host resource URI.
	 * 
	 * @param hostResource
	 *            The host resource URI to find an ID for
	 * @return The the ID of the references file or disk
	 */
	public String findHostRosourceId(String hostResource) {
		if (hostResource.lastIndexOf('/') != -1) {
			return hostResource.substring(+1);
		} else {
			return null;
		}
	}
}
