package eu.ascetic.utils.ovf.api;

import java.util.Vector;

import org.dmtf.schemas.ovf.envelope.x1.XmlBeanProductSectionType;
import org.dmtf.schemas.wbem.wscim.x1.common.CimString;

import eu.ascetic.utils.ovf.api.AbstractElement;
import eu.ascetic.utils.ovf.api.utils.XmlSimpleTypeConverter;

public class ProductSection extends AbstractElement<XmlBeanProductSectionType> {
	public ProductSection(XmlBeanProductSectionType base) {
		super(base);
	}

	public void setProduct(String product) {
		delegate.setProduct(XmlSimpleTypeConverter.toMsgType(product));
	}

	public String getProduct() {
		return delegate.getProduct().getStringValue();
	}

	public String getVersion() {
		return delegate.getVersion().getStringValue();
	}

	public void setVersion(String version) {
		CimString cimString = CimString.Factory.newInstance();
		cimString.setStringValue(version);
		delegate.setVersion(cimString);
	}

	public String getInfo() {
		return delegate.getInfo().getStringValue();
	}

	public void setInfo(String info) {
		delegate.setInfo(XmlSimpleTypeConverter.toMsgType(info));
	}

	public ProductProperty getPropertyByKey(String key) {
		for (XmlBeanProductSectionType.Property p : delegate.getPropertyArray()) {
			if (p.getKey().equals(key)) {
				return new ProductProperty(p);
			}
		}
		return null;
	}

	public ProductProperty addNewProperty(String key, String type, String value) {
		XmlBeanProductSectionType.Property p = delegate.addNewProperty();
		p.setKey(key);
		p.setType(type);
		p.setValue2(value);
		return new ProductProperty(p);
	}

	public ProductProperty[] getPropertyArray() {
		Vector<ProductProperty> vector = new Vector<ProductProperty>();
		for (XmlBeanProductSectionType.Property type : delegate
				.getPropertyArray()) {
			vector.add(new ProductProperty(type));
		}
		return vector.toArray(new ProductProperty[vector.size()]);
	}

	public ProductProperty getPropertyArray(int i) {
		return new ProductProperty(delegate.getPropertyArray(i));
	}

	public void removeProperty(int i) {
		delegate.removeProperty(i);
	}

	// TODO: Add additional helper methods here standardise access to ASCETIC
	// specific product properties (e.g. probe end-points)
	
	// TODO: add function to store and fetch upper and lower bound of VMs per Virtual System
	
	// TODO: store and fetch deployment ID
	
	// TODO: store and fetch SSH key (decide when to add keys construction VMIC or deployment VMC duplicate?)
}
