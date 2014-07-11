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

import org.dmtf.schemas.ovf.envelope.x1.XmlBeanProductSectionType;

import eu.ascetic.utils.ovf.api.AbstractElement;
import eu.ascetic.utils.ovf.api.enums.ProductPropertyType;
import eu.ascetic.utils.ovf.api.factories.ProductPropertyFactory;

/**
 * @author Django Armstrong (ULeeds)
 * 
 */
public class ProductProperty extends
		AbstractElement<XmlBeanProductSectionType.Property> {

	/**
	 * A static reference to the {@link ProductPropertyFactory} class for
	 * generating new instances of this object.
	 */
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
		ProductPropertyType productPropertyType = ProductPropertyType
				.findByType(delegate.getType());
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
