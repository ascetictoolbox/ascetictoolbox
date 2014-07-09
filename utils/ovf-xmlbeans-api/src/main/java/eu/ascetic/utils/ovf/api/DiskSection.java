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

import eu.ascetic.utils.ovf.api.AbstractElement;
import eu.ascetic.utils.ovf.api.DiskSection;
import eu.ascetic.utils.ovf.api.VirtualDiskDesc;
import eu.ascetic.utils.ovf.api.utils.XmlSimpleTypeConverter;

import org.dmtf.schemas.ovf.envelope.x1.XmlBeanDiskSectionType;
import org.dmtf.schemas.ovf.envelope.x1.XmlBeanVirtualDiskDescType;

import java.util.ArrayList;
import java.util.List;
import java.util.Vector;

/**
 * Provides access to elements within the DiskSection of an OVF document. A
 * DiskSection describes meta-information about virtual disks in the OVF
 * package.
 * 
 * @author Django Armstrong (ULeeds)
 * 
 */
public class DiskSection extends AbstractElement<XmlBeanDiskSectionType> {

	/**
	 * A static reference to the Factory class for generating new instances of
	 * this object.
	 */
	public static DiskSectionFactory Factory = new DiskSectionFactory();

	/**
	 * Default constructor.
	 * 
	 * @param base
	 *            The XMLBeans base type used for data storage
	 */
	public DiskSection(XmlBeanDiskSectionType base) {
		super(base);
	}

	/**
	 * Gets an array of all the VirtualDiskDesc held in this object.
	 * 
	 * @return An array of VirtualDiskDesc
	 */
	public VirtualDiskDesc[] getDiskArray() {

		List<VirtualDiskDesc> diskArray = new ArrayList<VirtualDiskDesc>();
		for (XmlBeanVirtualDiskDescType diskSectionType : delegate
				.getDiskArray()) {
			diskArray.add(new VirtualDiskDesc(diskSectionType));
		}
		return diskArray.toArray(new VirtualDiskDesc[diskArray.size()]);
	}

	/**
	 * Sets the VirtualDiskDesc array held in this object.
	 * 
	 * @param virtualDiskDescArray
	 *            The array of VirtualDiskDesc to set.
	 */
	public void setDiskArray(VirtualDiskDesc[] virtualDiskDescArray) {
		Vector<XmlBeanVirtualDiskDescType> diskArray = new Vector<XmlBeanVirtualDiskDescType>();
		for (int i = 0; i < virtualDiskDescArray.length; i++) {
			diskArray.add(virtualDiskDescArray[i].getXmlObject());
		}
		delegate.setDiskArray((XmlBeanVirtualDiskDescType[]) diskArray
				.toArray());
	}

	/**
	 * Gets a VirtualDiskDesc at held within the VirtualDiskDesc array at index
	 * i
	 * 
	 * @param i
	 *            The index value
	 * @return The VirtualDiskDesc at index i
	 */
	public VirtualDiskDesc getDiskAtIndex(int i) {
		return new VirtualDiskDesc(delegate.getDiskArray(i));
	}

	/**
	 * Adds a VirtualDiskDesc to the end of the VirtualDiskDesc array
	 * 
	 * @param virtualDiskDesc
	 *            The VirtualDiskDesc to add to the end of the array.
	 */
	public void addDisk(VirtualDiskDesc virtualDiskDesc) {
		XmlBeanVirtualDiskDescType xmlBeanVirtualDiskDescType = delegate
				.addNewDisk();
		xmlBeanVirtualDiskDescType.set(virtualDiskDesc.getXmlObject());
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
