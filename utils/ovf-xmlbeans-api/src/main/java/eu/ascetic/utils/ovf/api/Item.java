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
 * virtual hardware devices.
 * 
 * TODO: Add support for the ovf:bound attribute (min, max, normal). TODO: Add
 * support for the rasd:ResourceSubType element to support vendor specific
 * virtual hardware.
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
	 * @return
	 */
	public BigInteger getVirtualQuantity() {
		if (delegate.isSetVirtualQuantity()) {
			return delegate.getVirtualQuantity().getBigIntegerValue();
		}
		return null;
	}

	/**
	 * @param virtualQuantity
	 */
	public void setVirtualQuantity(BigInteger virtualQuantity) {
		if (delegate.isSetVirtualQuantity()) {
			delegate.unsetVirtualQuantity();
		}
		delegate.setVirtualQuantity(XmlSimpleTypeConverter
				.toCimUnsignedLong(virtualQuantity.longValue()));
	}

	/**
	 * @return
	 */
	public String getAllocationUnits() {
		if (delegate.isSetAllocationUnits()) {
			return delegate.getAllocationUnits().getStringValue();
		}
		return null;
	}

	/**
	 * @param allocationUnits
	 */
	public void setAllocationUnits(String allocationUnits) {
		if (delegate.isSetAllocationUnits()) {
			delegate.unsetAllocationUnits();
		}
		delegate.setAllocationUnits(XmlSimpleTypeConverter
				.toCimString(allocationUnits));
	}

	/**
	 * @return
	 */
	public Boolean getAutomaticAllocation() {
		return delegate.getAutomaticAllocation().getBooleanValue();
	}

	/**
	 * @param automaticAllocation
	 */
	public void setAutomaticAllocation(Boolean automaticAllocation) {
		delegate.setAutomaticAllocation(XmlSimpleTypeConverter
				.toCimBoolean(automaticAllocation));
	}

	/**
	 * @return
	 */
	public String[] getConnectionArray() {
		Vector<String> vector = new Vector<String>();
		for (CimString type : delegate.getConnectionArray()) {
			vector.add(type.getStringValue());
		}
		return vector.toArray(new String[vector.size()]);
	}

	/**
	 * @param connectionArray
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
	 * @param i
	 * @return
	 */
	public String getConnectionAtIndex(int i) {
		return delegate.getConnectionArray(i).getStringValue();
	}

	/**
	 * @param connection
	 */
	public void addConnection(String connection) {
		CimString cimString = delegate.addNewConnection();
		cimString.setStringValue(connection);
	}

	/**
	 * @return
	 */
	public String getParent() {
		if (delegate.isSetParent()) {
			return delegate.getParent().getStringValue();
		}
		return null;
	}

	/**
	 * @param parent
	 */
	public void setParent(String parent) {
		if (delegate.isSetParent()) {
			delegate.unsetParent();
		}
		delegate.setParent(XmlSimpleTypeConverter.toCimString(parent));
	}

	/**
	 * @return
	 */
	public String[] getHostResourceArray() {
		Vector<String> vector = new Vector<String>();
		for (CimString cimString : delegate.getHostResourceArray()) {
			vector.add(cimString.getStringValue());
		}
		return vector.toArray(new String[vector.size()]);
	}

	/**
	 * @param hostResourceArray
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
	 * @param i
	 * @return
	 */
	public String getHostResourceArray(int i) {
		if (delegate.getHostResourceArray().length > i) {
			return delegate.getHostResourceArray(i).getStringValue();
		}
		return null;
	}

	/**
	 * @param hostResource
	 */
	public void addHostResource(String hostResource) {
		CimString cimString = delegate.addNewHostResource();
		cimString.setStringValue(hostResource);
	}
}
