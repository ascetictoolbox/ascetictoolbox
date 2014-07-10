package eu.ascetic.utils.ovf.api;

import org.dmtf.schemas.ovf.envelope.x1.XmlBeanVirtualDiskDescType;

import eu.ascetic.utils.ovf.api.AbstractElement;

public class VirtualDiskDesc extends
		AbstractElement<XmlBeanVirtualDiskDescType> {

	public static VirtualDiskDescFactory Factory = new VirtualDiskDescFactory();
	
	public VirtualDiskDesc(XmlBeanVirtualDiskDescType base) {
		super(base);
	}

	public String getDiskId() {
		return delegate.getDiskId();
	}
	
	public void setDiskId(String id) {
		delegate.setDiskId(id);
	}

	public String getFileRef() {
		return delegate.getFileRef();
	}
	
	public void setFileRef(String ref) {
		delegate.setFileRef(ref);
	}

	public String getCapacity() {
		return delegate.getCapacity();
	}

	public void setCapacity(String capacity) {
		delegate.setCapacity(capacity);
	}

	public String getCapacityAllocationUnits() {
		return delegate.getCapacityAllocationUnits();
	}

	public void setCapacityAllocationUnits(String capacityAllocationUnits) {
		delegate.setCapacityAllocationUnits(capacityAllocationUnits);
	}

	// FIXME: change to DiskFormatType enumeration 
	public String getFormat() {
		return delegate.getFormat();
	}

	// FIXME: change to DiskFormatType enumeration 
	public void setFormat(String format) {
		delegate.setFormat(format);
	}

	public long getPopulatedSize() {
		return delegate.getPopulatedSize();
	}

	public void setPopulatedSize(long populatedSize) {
		if (!(populatedSize > -1)) {
			throw new IllegalArgumentException("populated size must be > -1");
		}
		delegate.setPopulatedSize(populatedSize);
	}

	public String getParentRef() {
		return delegate.getParentRef();
	}

	public void setParentRef(String parentRef) {
		delegate.setParentRef(parentRef);
	}

}
