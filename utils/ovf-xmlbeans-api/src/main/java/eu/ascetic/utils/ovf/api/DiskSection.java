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

public class DiskSection extends AbstractElement<XmlBeanDiskSectionType> {

	public static DiskSectionFactory Factory = new DiskSectionFactory();
	
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

	public void setDiskArray(VirtualDiskDesc[] virtualDiskDescArray) {
		Vector<XmlBeanVirtualDiskDescType> diskArray = new Vector<XmlBeanVirtualDiskDescType>();
		for (int i = 0; i < virtualDiskDescArray.length; i++) {
			diskArray.add(virtualDiskDescArray[i].getXmlObject());
		}
		delegate.setDiskArray((XmlBeanVirtualDiskDescType[]) diskArray.toArray());
	}
	
	public VirtualDiskDesc getDiskAtIndex(int i) {
		return new VirtualDiskDesc(delegate.getDiskArray(i));
	}
	
	public void addDisk(VirtualDiskDesc virtualDiskDesc) {
		XmlBeanVirtualDiskDescType xmlBeanVirtualDiskDescType = delegate.addNewDisk();
		xmlBeanVirtualDiskDescType.set(virtualDiskDesc.getXmlObject());
	}

	public String getInfo() {
		return delegate.getInfo().getStringValue();
	}

	public void setInfo(String info) {
		delegate.setInfo(XmlSimpleTypeConverter.toMsgType(info));
	}
}
