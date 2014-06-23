package eu.ascetic.utils.ovf.api;

import eu.ascetic.utils.ovf.api.AbstractElement;
import eu.ascetic.utils.ovf.api.DiskSection;
import eu.ascetic.utils.ovf.api.VirtualDiskDesc;
import eu.ascetic.utils.ovf.api.utils.XmlSimpleTypeConverter;

import org.dmtf.schemas.ovf.envelope.x1.XmlBeanDiskSectionType;
import org.dmtf.schemas.ovf.envelope.x1.XmlBeanVirtualDiskDescType;

import java.util.ArrayList;
import java.util.List;

public class DiskSection extends AbstractElement<XmlBeanDiskSectionType> {

	public DiskSection(XmlBeanDiskSectionType base) {
		super(base);
	}

	public VirtualDiskDesc[] getDiskArray() {

		List<VirtualDiskDesc> diskArray = new ArrayList<VirtualDiskDesc>();
		for (XmlBeanVirtualDiskDescType diskSectionType : delegate
				.getDiskArray()) {
			diskArray.add(new VirtualDiskDesc(diskSectionType));
		}
		return diskArray.toArray(new VirtualDiskDesc[diskArray.size()]);
	}

	public VirtualDiskDesc getDiskAtIndex(int i) {
		return new VirtualDiskDesc(delegate.getDiskArray(i));
	}

	// FIXME: This should not be hardcoded?
	public VirtualDiskDesc getImageDisk() {
		return new VirtualDiskDesc(delegate.getDiskArray(0));
	}

	// FIXME: This should not be hardcoded?
	public VirtualDiskDesc getContextualizationDisk() {
		return new VirtualDiskDesc(delegate.getDiskArray(1));
	}

	public String getInfo() {
		return delegate.getInfo().getStringValue();
	}

	public void setInfo(String info) {
		delegate.setInfo(XmlSimpleTypeConverter.toMsgType(info));
	}
}
