package eu.ascetic.utils.ovf.api;

import org.dmtf.schemas.ovf.envelope.x1.XmlBeanProductSectionType;

import eu.ascetic.utils.ovf.api.AbstractElement;

public class ProductProperty extends
		AbstractElement<XmlBeanProductSectionType.Property> {
	
	public static ProductPropertyFactory Factory = new ProductPropertyFactory();
	
	public ProductProperty(XmlBeanProductSectionType.Property base) {
		super(base);
	}

	public void setType(ProductPropertyType type) {
		delegate.setType(type.getType());
	}

	public ProductPropertyType getType() {
		return ProductPropertyType.findByType(delegate.getType());
	}
	
	public void setKey(String key) {
		delegate.setKey(key);
	}

	public String getKey() {
		return delegate.getKey();
	}

	public void setValue(String value) {
		delegate.setValue2(value);
	}
	
	public String getValue() {
		return delegate.getValue2();
	}
	
	public Object getValueAsObject() {
		ProductPropertyType productPropertyType = ProductPropertyType.findByType(delegate.getType());
		switch (productPropertyType) {
		case UINT8:
			return Integer.parseInt(delegate.getValue2());
		case SINT8:
			return Integer.parseInt(delegate.getValue2());
		case UINT16:
			return Integer.parseInt(delegate.getValue2());
		case SINT16:
			return Integer.parseInt(delegate.getValue2());
		case UINT32:
			return Integer.parseInt(delegate.getValue2());
		case SINT32:
			return Integer.parseInt(delegate.getValue2());
		case UINT64:
			return Long.parseLong(delegate.getValue2());
		case SINT64:
			return Long.parseLong(delegate.getValue2());
		case REAL32:
			return Float.parseFloat(delegate.getValue2());
		case REAL64:
			return Double.parseDouble(delegate.getValue2());
		// String
		default:
			return productPropertyType.getType();
		}
	}
}
