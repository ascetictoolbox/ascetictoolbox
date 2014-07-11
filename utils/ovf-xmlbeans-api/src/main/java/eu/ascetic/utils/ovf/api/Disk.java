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

import org.dmtf.schemas.ovf.envelope.x1.XmlBeanVirtualDiskDescType;

import eu.ascetic.utils.ovf.api.AbstractElement;
import eu.ascetic.utils.ovf.api.enums.DiskFormatType;
import eu.ascetic.utils.ovf.api.factories.DiskFactory;

/**
 * Provides access to the Disk elements of an OVF document. A Disk contained
 * within the {@link DiskSection} represents virtual disk characteristics.
 * 
 * @author Django Armstrong (ULeeds)
 * 
 */
public class Disk extends AbstractElement<XmlBeanVirtualDiskDescType> {

	/**
	 * A static reference to the {@link DiskFactory} class for generating new
	 * instances of this object.
	 */
	public static DiskFactory Factory = new DiskFactory();

	/**
	 * Default constructor.
	 * 
	 * @param base
	 *            The XMLBeans base type used for data storage
	 */
	public Disk(XmlBeanVirtualDiskDescType base) {
		super(base);
	}

	/**
	 * Gets the unique identifier for this Disk.
	 * 
	 * @return The disk ID
	 */
	public String getDiskId() {
		return delegate.getDiskId();
	}

	/**
	 * Sets the unique identifier for this Disk.
	 * 
	 * @param id
	 *            The disk ID to set
	 */
	public void setDiskId(String id) {
		delegate.setDiskId(id);
	}

	/**
	 * Gets the file reference. The fileRef attribute denotes the virtual disk
	 * content by identifying an existing {@link File} in the {@link References}
	 * element, the {@link File} element is identified by matching its id
	 * attribute value with the fileRef attribute value. Omitting the fileRef
	 * attribute indicates an empty disk. In this case, the disk is created and
	 * the entire disk content zeroed at installation time.
	 * 
	 * @return The file reference
	 */
	public String getFileRef() {
		return delegate.getFileRef();
	}

	/**
	 * Sets the file reference. The fileRef attribute denotes the virtual disk
	 * content by identifying an existing {@link File} in the {@link References}
	 * element, the {@link File} element is identified by matching its id
	 * attribute value with the fileRef attribute value. Omitting the fileRef
	 * attribute indicates an empty disk. In this case, the disk is created and
	 * the entire disk content zeroed at installation time.
	 * 
	 * @param ref
	 *            The file reference to set
	 */
	public void setFileRef(String ref) {
		delegate.setFileRef(ref);
	}

	/**
	 * Gets the capacity of the disk. The default unit of allocation is bytes
	 * and can be altered by setting CapacityAllocationUnits.
	 * 
	 * @return The capacity
	 */
	public String getCapacity() {
		return delegate.getCapacity();
	}

	/**
	 * Sets the capacity of the disk. The default unit of allocation is bytes
	 * and can be altered by setting CapacityAllocationUnits.
	 * 
	 * @param capacity
	 *            The capacity to set
	 */
	public void setCapacity(String capacity) {
		delegate.setCapacity(capacity);
	}

	/**
	 * Gets the optional capacity allocation units. The optional string
	 * attribute capacityAllocationUnits may be used to specify a particular
	 * unit of allocation. Values for capacityAllocationUnits must match the
	 * format for programmatic units defined in DSP0004.C1 with the restriction
	 * that the base unit is "byte". (See <a
	 * href="http://www.dmtf.org/standards/cim">http://www.dmtf.org/standards
	 * /cim</a>).<br>
	 * <br>
	 * For example:<br>
	 * <br>
	 * 1 GByte = "byte * 2^30"<br>
	 * 1 MByte = "byte * 2^20"<br>
	 * 1 KByte = "byte * 2^10"
	 * 
	 * @return The capacity allocation units
	 */
	public String getCapacityAllocationUnits() {
		return delegate.getCapacityAllocationUnits();
	}

	/**
	 * Sets the optional capacity allocation units. The optional string
	 * attribute capacityAllocationUnits may be used to specify a particular
	 * unit of allocation. Values for capacityAllocationUnits must match the
	 * format for programmatic units defined in DSP0004.C1 with the restriction
	 * that the base unit is "byte". (See <a
	 * href="http://www.dmtf.org/standards/cim"
	 * >http://www.dmtf.org/standards/cim</a>).<br>
	 * <br>
	 * For example:<br>
	 * <br>
	 * 1 GByte = "byte * 2^30"<br>
	 * 1 MByte = "byte * 2^20"<br>
	 * 1 KByte = "byte * 2^10"
	 * 
	 * @param capacityAllocationUnits
	 */
	public void setCapacityAllocationUnits(String capacityAllocationUnits) {
		delegate.setCapacityAllocationUnits(capacityAllocationUnits);
	}

	/**
	 * Gets the disk format as a {@link DiskFormatType} representation.
	 * 
	 * @return The disk format type
	 */
	public DiskFormatType getFormat() {
		return DiskFormatType.findBySpecificationUrl(delegate.getFormat());
	}

	/**
	 * Sets the disk format as a {@link DiskFormatType} representation.
	 * 
	 * @param diskFormatType
	 *            The disk format type to set
	 */
	public void setFormat(DiskFormatType diskFormatType) {
		delegate.setFormat(diskFormatType.getSpecificationUrl());
	}

	/**
	 * Gets the populated size of the disk in bytes. For non-empty disks, the
	 * actual used size of the disk may optionally be specified using the
	 * populatedSize attribute. The unit of this attribute is always bytes.
	 * populatedSize is allowed to be an estimate of used disk size but is not
	 * to be larger than the capacity attribute.
	 * 
	 * @return The populated size in bytes
	 */
	public long getPopulatedSize() {
		return delegate.getPopulatedSize();
	}

	/**
	 * Sets the populated size of the disk in bytes. For non-empty disks, the
	 * actual used size of the disk may optionally be specified using the
	 * populatedSize attribute. The unit of this attribute is always bytes.
	 * populatedSize is allowed to be an estimate of used disk size but is not
	 * to be larger than the capacity attribute.
	 * 
	 * @param populatedSize
	 *            The populated size to set in bytes
	 */
	public void setPopulatedSize(long populatedSize) {
		if (!(populatedSize > -1)) {
			throw new IllegalArgumentException("populated size must be > -1");
		}
		delegate.setPopulatedSize(populatedSize);
	}

	/**
	 * Gets the parent disk reference as an ID. OVF allows a disk image to be
	 * represented as a set of modified blocks in comparison to a parent image
	 * (e.g. a copy on write or qcow disk). The use of parent disks can often
	 * significantly reduce the size of an OVF package, if it contains multiple
	 * disks with similar content. For a Disk element, a parent disk may
	 * optionally be specified using the parentRef attribute, which contains a
	 * valid diskId reference to a different Disk element. If a disk block does
	 * not exist locally, lookup for that disk block then occurs in the parent
	 * disk. In {@link DiskSection}, parent Disk elements must occur before
	 * child {@link Disk} elements that refer to them.
	 * 
	 * @return The parent disk reference
	 */
	public String getParentRef() {
		return delegate.getParentRef();
	}

	/**
	 * Sets the parent disk reference as an ID. OVF allows a disk image to be
	 * represented as a set of modified blocks in comparison to a parent image
	 * (e.g. a copy on write or qcow disk). The use of parent disks can often
	 * significantly reduce the size of an OVF package, if it contains multiple
	 * disks with similar content. For a Disk element, a parent disk may
	 * optionally be specified using the parentRef attribute, which contains a
	 * valid diskId reference to a different Disk element. If a disk block does
	 * not exist locally, lookup for that disk block then occurs in the parent
	 * disk. In {@link DiskSection}, parent Disk elements must occur before
	 * child {@link Disk} elements that refer to them.
	 * 
	 * @param parentRef
	 *            The parent disk reference to set
	 */
	public void setParentRef(String parentRef) {
		delegate.setParentRef(parentRef);
	}

}
