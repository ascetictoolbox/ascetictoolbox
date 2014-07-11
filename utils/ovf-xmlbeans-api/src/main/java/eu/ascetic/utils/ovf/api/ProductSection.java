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

import java.util.Vector;

import org.dmtf.schemas.ovf.envelope.x1.XmlBeanProductSectionType;
import org.dmtf.schemas.wbem.wscim.x1.common.CimString;

import eu.ascetic.utils.ovf.api.AbstractElement;
import eu.ascetic.utils.ovf.api.enums.ProductPropertyType;
import eu.ascetic.utils.ovf.api.factories.ProductSectionFactory;
import eu.ascetic.utils.ovf.api.utils.XmlSimpleTypeConverter;

/**
 * @author Django Armstrong (ULeeds)
 * 
 */
public class ProductSection extends AbstractElement<XmlBeanProductSectionType> {

	/**
	 * A static reference to the {@link ProductSectionFactory} class for
	 * generating new instances of this object.
	 */
	public static ProductSectionFactory Factory = new ProductSectionFactory();

	private static final String ASCETIC_VIRTUAL_SYSTEM_UPPER_BOUND_KEY = "asceticUpperBound";
	private static final String ASCETIC_VIRTUAL_SYSTEM_LOWER_BOUND_KEY = "asceticLowerBound";
	private static final String ASCETIC_VIRTUAL_SYSTEM_COLLECTION_DEPLOYMENT_ID_KEY = "asceticDeploymentId";
	private static final String ASCETIC_SECURITY_KEY = "asceticSecurityKey";

	public ProductSection(XmlBeanProductSectionType base) {
		super(base);
	}

	public void setProduct(String product) {
		delegate.setProduct(XmlSimpleTypeConverter.toMsgType(product));
	}

	public String getProduct() {
		return delegate.getProduct().getStringValue();
	}

	public void setVersion(String version) {
		CimString cimString = CimString.Factory.newInstance();
		cimString.setStringValue(version);
		delegate.setVersion(cimString);
	}

	public String getVersion() {
		return delegate.getVersion().getStringValue();
	}

	public void setInfo(String info) {
		delegate.setInfo(XmlSimpleTypeConverter.toMsgType(info));
	}

	public String getInfo() {
		return delegate.getInfo().getStringValue();
	}

	public ProductProperty getPropertyByKey(String key) {
		for (XmlBeanProductSectionType.Property p : delegate.getPropertyArray()) {
			if (p.getKey().equals(key)) {
				return new ProductProperty(p);
			}
		}
		return null;
	}

	public ProductProperty addNewProperty(String key, ProductPropertyType type,
			String value) {
		XmlBeanProductSectionType.Property p = delegate.addNewProperty();
		p.setKey(key);
		p.setType(type.getType());
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

	/*
	 * Start of ASCETiC specific helper functions to store and fetch upper and
	 * lower bound of VMs per Virtual System
	 * 
	 * FIXME: Move these to somewhere else so as to not pollute the API
	 */

	public void setUpperBound(Integer upperBound) {
		ProductProperty productProperty = getPropertyByKey(ASCETIC_VIRTUAL_SYSTEM_UPPER_BOUND_KEY);
		if (productProperty.equals(null)) {
			addNewProperty(ASCETIC_VIRTUAL_SYSTEM_UPPER_BOUND_KEY,
					ProductPropertyType.UINT32, upperBound.toString());
		} else {
			productProperty.setValue(upperBound.toString());
		}
	}

	public int getUpperBound() {
		return Integer.parseInt(getPropertyByKey(
				ASCETIC_VIRTUAL_SYSTEM_UPPER_BOUND_KEY).getValue());
	}

	public void setLowerBound(Integer lowerBound) {
		ProductProperty productProperty = getPropertyByKey(ASCETIC_VIRTUAL_SYSTEM_LOWER_BOUND_KEY);
		if (productProperty.equals(null)) {
			addNewProperty(ASCETIC_VIRTUAL_SYSTEM_LOWER_BOUND_KEY,
					ProductPropertyType.UINT32, lowerBound.toString());
		} else {
			productProperty.setValue(lowerBound.toString());
		}
	}

	public int getLowerBound() {
		return Integer.parseInt(getPropertyByKey(
				ASCETIC_VIRTUAL_SYSTEM_LOWER_BOUND_KEY).getValue());
	}

	// Store and fetch deployment ID
	public void setDeploymentId(String id) {
		ProductProperty productProperty = getPropertyByKey(ASCETIC_VIRTUAL_SYSTEM_COLLECTION_DEPLOYMENT_ID_KEY);
		if (productProperty == null) {
			addNewProperty(ASCETIC_VIRTUAL_SYSTEM_COLLECTION_DEPLOYMENT_ID_KEY,
					ProductPropertyType.STRING, id);
		} else {
			productProperty.setValue(id);
		}
	}

	public String getDeploymentId() {
		return getPropertyByKey(
				ASCETIC_VIRTUAL_SYSTEM_COLLECTION_DEPLOYMENT_ID_KEY).getValue();
	}

	// Store and fetch SSH key
	public void setSecurityKey(String securityKey) {
		ProductProperty productProperty = getPropertyByKey(ASCETIC_SECURITY_KEY);
		if (productProperty == null) {
			addNewProperty(ASCETIC_SECURITY_KEY, ProductPropertyType.STRING,
					securityKey);
		} else {
			productProperty.setValue(securityKey);
		}
	}

	public String getSecurityKey() {
		return getPropertyByKey(ASCETIC_SECURITY_KEY).getValue();
	}

	// TODO: Add additional helper methods here standardise access to ASCETIC
	// specific product properties (e.g. probe end-points)
}
