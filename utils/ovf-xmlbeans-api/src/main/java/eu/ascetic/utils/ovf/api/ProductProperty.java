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

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import eu.ascetic.utils.ovf.api.AbstractElement;
import eu.ascetic.utils.ovf.api.enums.ProductPropertyType;
import eu.ascetic.utils.ovf.api.factories.ProductPropertyFactory;

/**
 * Provides access to Property elements of an OVF document. These elements that
 * act like a key-value store, specify application-level customisation
 * parameters and are particularly relevant to appliances that need to be
 * customised during deployment with specific settings such as network identity,
 * the IP addresses of DNS servers and gateways amongst others.<br>
 * <br>
 * TODO: Implement Category, Description and Label elements.<br>
 * TODO: Implement ovf:userConfigurable, ovf:qualifiers attribute.<br>
 * TODO: Implement Value elements with ovf:configuration attribute.
 * 
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

	/**
	 * Default constructor.
	 * 
	 * @param base
	 *            The XMLBeans base type used for data storage
	 */
	public ProductProperty(XmlBeanProductSectionType.Property base) {
		super(base);
	}

	/**
	 * Gets the property's type as a {@link ProductPropertyType} representation.
	 * 
	 * @return The type
	 */
	public ProductPropertyType getType() {
		return ProductPropertyType.findByType(delegate.getType());
	}

	/**
	 * Sets the property's type as a {@link ProductPropertyType} representation.
	 * 
	 * @param type
	 *            The type to set
	 */
	public void setType(ProductPropertyType type) {
		delegate.setType(type.getType());
	}

	/**
	 * Gets the property's key. Each Property element must be given an
	 * identifier that is unique within the {@link ProductSection} using the
	 * ovf:key attribute. The ovf:key attribute must not contain the period
	 * character ('.') or the colon character (':')
	 * 
	 * @return The key
	 */
	public String getKey() {
		return delegate.getKey();
	}

	/**
	 * Sets the property's key. Each Property element must be given an
	 * identifier that is unique within the {@link ProductSection} using the
	 * ovf:key attribute. The ovf:key attribute must not contain the period
	 * character ('.') or the colon character (':')
	 * 
	 * @param key
	 *            The key to set
	 */
	public void setKey(String key) {
		delegate.setKey(key);
	}

	/**
	 * Gets the property's value as a String.
	 * 
	 * @return The value
	 */
	public String getValue() {
		return delegate.getValue2();
	}

	/**
	 * Sets the property's value as a String.
	 * 
	 * @param value
	 *            The value to set
	 */
	public void setValue(String value) {
		delegate.setValue2(value);
	}

	/**
	 * Gets the property's value as a java Object for later casting using the
	 * <i>instanceof</i> binary operator to test.
	 * 
	 * @return The value as an Object
	 */
	@SuppressFBWarnings(
			value = "MethodCyclomaticComplexity", 
			justification = "Switch statment is easier to read")
	public Object getValueAsJavaObject() {
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
		default:
			// String
			return productPropertyType.getType();
		}
	}
}
