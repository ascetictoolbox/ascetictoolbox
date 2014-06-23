package eu.ascetic.utils.ovf.api;

import java.util.Vector;

import org.dmtf.schemas.ovf.envelope.x1.XmlBeanOperatingSystemSectionType;
import org.dmtf.schemas.ovf.envelope.x1.XmlBeanProductSectionType;
import org.dmtf.schemas.ovf.envelope.x1.XmlBeanSectionType;
import org.dmtf.schemas.ovf.envelope.x1.XmlBeanVirtualHardwareSectionType;
import org.dmtf.schemas.ovf.envelope.x1.XmlBeanVirtualSystemType;

import eu.ascetic.utils.ovf.api.AbstractElement;
import eu.ascetic.utils.ovf.api.utils.XmlSimpleTypeConverter;

public class VirtualSystem extends AbstractElement<XmlBeanVirtualSystemType> {

	public VirtualSystem(XmlBeanVirtualSystemType base) {
		super(base);
	}

	public String getInfo() {
		return delegate.getInfo().getStringValue();
	}

	public void setInfo(String info) {
		delegate.setInfo(XmlSimpleTypeConverter.toMsgType(info));
	}

	public String getName() {
		if (delegate.isSetName()) {
			return delegate.getName().getStringValue();
		}
		return null;
	}

	public void setName(String name) {
		delegate.setName(XmlSimpleTypeConverter.toMsgType(name));
	}

	public String getId() {
		return delegate.getId();
	}

	public void setId(String id) {
		delegate.setId(id);
	}

	public ProductSection[] getProductSectionArray() {
		Vector<ProductSection> vector = new Vector<ProductSection>();
		XmlBeanSectionType[] sectionArray = (XmlBeanSectionType[]) delegate
				.getSectionArray();
		if (sectionArray != null) {
			for (XmlBeanSectionType xmlBeanSections : sectionArray) {
				if (xmlBeanSections instanceof XmlBeanProductSectionType) {
					vector.add(new ProductSection(
							(XmlBeanProductSectionType) xmlBeanSections));
				}
			}
			return vector.toArray(new ProductSection[vector.size()]);
		}
		return null;
	}

	public ProductSection getProductSectionAtIndex(int i) {
		return getProductSectionArray()[i];
	}

	public OperatingSystem getOperatingSystem() {
		XmlBeanSectionType[] sectionArray = (XmlBeanSectionType[]) delegate
				.getSectionArray();
		if (sectionArray != null) {
			for (XmlBeanSectionType xmlBeanSections : sectionArray) {
				if (xmlBeanSections instanceof XmlBeanOperatingSystemSectionType) {
					return new OperatingSystem(
							(XmlBeanOperatingSystemSectionType) xmlBeanSections);
				}
			}
		}
		return null;
	}

	public VirtualHardwareSection getVirtualHardwareSection() {
		XmlBeanSectionType[] sectionArray = (XmlBeanSectionType[]) delegate
				.getSectionArray();
		if (sectionArray != null) {
			for (XmlBeanSectionType xmlBeanSections : sectionArray) {
				if (xmlBeanSections instanceof XmlBeanVirtualHardwareSectionType) {
					return new VirtualHardwareSection(
							(XmlBeanVirtualHardwareSectionType) xmlBeanSections);
				}
			}
		}
		return null;
	}

}
