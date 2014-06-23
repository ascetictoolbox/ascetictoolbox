package eu.ascetic.utils.ovf.api;

import java.util.Vector;

import org.dmtf.schemas.ovf.envelope.x1.XmlBeanContentType;
import org.dmtf.schemas.ovf.envelope.x1.XmlBeanProductSectionType;
import org.dmtf.schemas.ovf.envelope.x1.XmlBeanSectionType;
import org.dmtf.schemas.ovf.envelope.x1.XmlBeanVirtualSystemCollectionType;
import org.dmtf.schemas.ovf.envelope.x1.XmlBeanVirtualSystemType;

import eu.ascetic.utils.ovf.api.AbstractElement;
import eu.ascetic.utils.ovf.api.utils.XmlSimpleTypeConverter;

public class VirtualSystemCollection extends
		AbstractElement<XmlBeanVirtualSystemCollectionType> {

	public VirtualSystemCollection(XmlBeanVirtualSystemCollectionType base) {
		super(base);
	}

	public String getId() {
		return delegate.getId();
	}

	public void setId(String id) {
		delegate.setId(id);
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

	public ProductSection[] getProductSectionArray() {
		Vector<ProductSection> vector = new Vector<ProductSection>();
		XmlBeanSectionType[] sectionArray = (XmlBeanSectionType[]) delegate
				.getSectionArray();
		if (sectionArray != null) {
			for (XmlBeanSectionType xmlBeanSection : sectionArray) {
				if (xmlBeanSection instanceof XmlBeanProductSectionType) {
					vector.add(new ProductSection(
							(XmlBeanProductSectionType) xmlBeanSection));
				}
			}
			return vector.toArray(new ProductSection[vector.size()]);
		}
		return null;
	}

	public ProductSection getProductSectionAtIndex(int i) {
		return getProductSectionArray()[i];
	}

	public VirtualSystem[] getVirtualSystemArray() {
		Vector<VirtualSystem> vector = new Vector<VirtualSystem>();
		XmlBeanContentType[] contentArray = (XmlBeanContentType[]) delegate
				.getContentArray();
		if (contentArray != null) {
			for (XmlBeanContentType contentType : contentArray) {
				if (contentType instanceof XmlBeanVirtualSystemType) {
					vector.add(new VirtualSystem(
							(XmlBeanVirtualSystemType) contentType));
				}
			}
			return vector.toArray(new VirtualSystem[vector.size()]);
		}
		return null;
	}

	public VirtualSystem getVirtualSystemAtIndex(int i) {
		return getVirtualSystemArray()[i];
	}

}
