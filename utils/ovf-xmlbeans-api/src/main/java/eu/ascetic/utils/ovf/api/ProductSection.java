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
import eu.ascetic.utils.ovf.api.utils.XmlSimpleTypeConverter;

/**
 * Provides access to elements in the ProductSection element. This section
 * specifies product-information for a package, such as product name and
 * version, along with a set of properties that can be configured (see
 * {@link ProductProperty}). ProductSection is a valid section for
 * {@link VirtualSystem} and {@link VirtualSystemCollection} entities.<br>
 * <br>
 * TODO: Refactor ASCETiC specific properties to another package/class so as to
 * not pollute the API (possibly storing static final keys in an enum).<br>
 * TODO: Implement the FullVersion, ProductURL, VendorURL elements.<br>
 * TODO: Implement ovf:class and ovf:instance attribute pair to correctly
 * support multiple product sections.
 * 
 * @author Django Armstrong (ULeeds)
 * 
 */
public class ProductSection extends AbstractElement<XmlBeanProductSectionType> {

    /**
     * A static reference to the {@link ProductSectionFactory} class for
     * generating new instances of this object.
     */
    public static ProductSectionFactory Factory = new ProductSectionFactory();

    /**
     * The static KEY used to get and set the upper bound on the number of
     * {@link VirtualSystem}s to instantiate.
     */
    private static final String ASCETIC_VIRTUAL_SYSTEM_UPPER_BOUND_KEY = "asceticUpperBound";
    /**
     * The static KEY used to get and set the upper bound on the number of
     * {@link VirtualSystem}s to instantiate.
     */
    private static final String ASCETIC_VIRTUAL_SYSTEM_LOWER_BOUND_KEY = "asceticLowerBound";
    /**
     * The static KEY used to get and set the deployment ID of a
     * {@link VirtualSystemCollection}.
     */
    private static final String ASCETIC_VIRTUAL_SYSTEM_COLLECTION_DEPLOYMENT_ID_KEY = "asceticDeploymentId";
    /**
     * The static KEY used to get and set a PKC combination of a private key and
     * a public key either in the global scope of
     * {@link VirtualSystemCollection} or locally in {@link VirtualSystem}.
     */
    private static final String ASCETIC_SECURITY_KEY = "asceticSecurityKey";

    /**
     * Default constructor.
     * 
     * @param base
     *            The XMLBeans base type used for data storage
     */
    public ProductSection(XmlBeanProductSectionType base) {
        super(base);
    }

    /**
     * Gets the Product element. The optional Product element specifies the name
     * of the product.
     * 
     * @return The product name
     */
    public String getProduct() {
        return delegate.getProduct().getStringValue();
    }

    /**
     * Sets the Product element. The optional Product element specifies the name
     * of the product.
     * 
     * @param product
     *            The product name to set
     */
    public void setProduct(String product) {
        delegate.setProduct(XmlSimpleTypeConverter.toMsgType(product));
    }

    /**
     * Gets the version element. The optional Version element specifies the
     * product version in short form.
     * 
     * @return The version
     */
    public String getVersion() {
        return delegate.getVersion().getStringValue();
    }

    /**
     * Sets the version element. The optional Version element specifies the
     * product version in short form.
     * 
     * @param version
     *            The version to set
     */
    public void setVersion(String version) {
        CimString cimString = CimString.Factory.newInstance();
        cimString.setStringValue(version);
        delegate.setVersion(cimString);
    }

    /**
     * Gets the info element, a human readable description of the meaning of
     * this section.
     * 
     * @return The content of the info element
     */
    public String getInfo() {
        return delegate.getInfo().getStringValue();
    }

    /**
     * Sets the info element, a human readable description of the meaning of
     * this section.
     * 
     * @param info
     *            The content to set within the info element
     */
    public void setInfo(String info) {
        delegate.setInfo(XmlSimpleTypeConverter.toMsgType(info));
    }

    /**
     * Gets the {@link ProductProperty} array held in this object.
     * 
     * @return The product property array
     */
    public ProductProperty[] getPropertyArray() {
        Vector<ProductProperty> vector = new Vector<ProductProperty>();
        for (XmlBeanProductSectionType.Property type : delegate
                .getPropertyArray()) {
            vector.add(new ProductProperty(type));
        }
        return vector.toArray(new ProductProperty[vector.size()]);
    }

    /**
     * Sets the {@link ProductProperty} array held in this object.
     * 
     * @param productPropertiesArray
     *            The product property array to set
     */
    public void setPropertyArray(ProductProperty[] productPropertiesArray) {
        Vector<XmlBeanProductSectionType.Property> newPropertyArray = new Vector<XmlBeanProductSectionType.Property>();
        for (int i = 0; i < productPropertiesArray.length; i++) {
            newPropertyArray.add(productPropertiesArray[i].getXmlObject());
        }
        delegate.setPropertyArray((XmlBeanProductSectionType.Property[]) newPropertyArray
                .toArray());
    }

    /**
     * Gets a {@link ProductProperty} by key held within an array stored in this
     * object.
     * 
     * @param key
     *            The unique key of the property
     * @return The property
     */
    public ProductProperty getPropertyByKey(String key) {
        for (XmlBeanProductSectionType.Property p : delegate.getPropertyArray()) {
            if (p.getKey().equals(key)) {
                return new ProductProperty(p);
            }
        }
        return null;
    }

    /**
     * Gets a {@link ProductProperty} at index i of the array of this object.
     * 
     * @param i
     *            The index of the property
     * @return The property
     */
    public ProductProperty getPropertyAtIndex(int i) {
        return new ProductProperty(delegate.getPropertyArray(i));
    }

    /**
     * Adds a new {@link ProductProperty} to the end of the array held by this
     * object.
     * 
     * @param key
     *            The unique property key
     * @param type
     *            The property type (see {@link ProductPropertyType})
     * @param value
     *            The property value
     * @return The ProductProperty added to the array.
     */
    public ProductProperty addNewProperty(String key, ProductPropertyType type,
            String value) {
        XmlBeanProductSectionType.Property p = delegate.addNewProperty();
        p.setKey(key);
        p.setType(type.getType());
        p.setValue2(value);
        return new ProductProperty(p);
    }

    /**
     * Removes a new ProductProperty from the array at index i.
     * 
     * @param i
     *            The index value to remove
     */
    public void removeProperty(int i) {
        delegate.removeProperty(i);
    }

    // Start of ASCETiC specific helper functions

    /**
     * Gets the upper bound on the number of virtual machines to instantiate on
     * a per {@link VirtualSystem} basis.
     * 
     * @return The upper bound
     */
    public int getUpperBound() {
        return Integer.parseInt(getPropertyByKey(
                ASCETIC_VIRTUAL_SYSTEM_UPPER_BOUND_KEY).getValue());
    }

    /**
     * Sets the upper bound on the number of virtual machines to instantiate on
     * a per {@link VirtualSystem} basis.
     * 
     * @param upperBound
     *            The upper bound to set
     */
    public void setUpperBound(Integer upperBound) {
        ProductProperty productProperty = getPropertyByKey(ASCETIC_VIRTUAL_SYSTEM_UPPER_BOUND_KEY);
        if (productProperty == null) {
            addNewProperty(ASCETIC_VIRTUAL_SYSTEM_UPPER_BOUND_KEY,
                    ProductPropertyType.UINT32, upperBound.toString());
        } else {
            productProperty.setValue(upperBound.toString());
        }
    }

    /**
     * Gets the lower bound on the number of virtual machines to instantiate on
     * a per {@link VirtualSystem} basis.
     * 
     * @return The lower bound
     */
    public int getLowerBound() {
        return Integer.parseInt(getPropertyByKey(
                ASCETIC_VIRTUAL_SYSTEM_LOWER_BOUND_KEY).getValue());
    }

    /**
     * Sets the lower bound on the number of virtual machines to instantiate on
     * a per {@link VirtualSystem} basis.
     * 
     * @param lowerBound
     *            The lower bound to set
     */
    public void setLowerBound(Integer lowerBound) {
        ProductProperty productProperty = getPropertyByKey(ASCETIC_VIRTUAL_SYSTEM_LOWER_BOUND_KEY);
        if (productProperty == null) {
            addNewProperty(ASCETIC_VIRTUAL_SYSTEM_LOWER_BOUND_KEY,
                    ProductPropertyType.UINT32, lowerBound.toString());
        } else {
            productProperty.setValue(lowerBound.toString());
        }
    }

    /**
     * Gets the deployment ID for a {@link VirtualSystemCollection}.
     * 
     * @return The deployment ID
     */
    public String getDeploymentId() {
        return getPropertyByKey(
                ASCETIC_VIRTUAL_SYSTEM_COLLECTION_DEPLOYMENT_ID_KEY).getValue();
    }

    /**
     * Sets the deployment ID for a {@link VirtualSystemCollection}.
     * 
     * @param id
     *            The deployment ID to set
     */
    public void setDeploymentId(String id) {
        ProductProperty productProperty = getPropertyByKey(ASCETIC_VIRTUAL_SYSTEM_COLLECTION_DEPLOYMENT_ID_KEY);
        if (productProperty == null) {
            addNewProperty(ASCETIC_VIRTUAL_SYSTEM_COLLECTION_DEPLOYMENT_ID_KEY,
                    ProductPropertyType.STRING, id);
        } else {
            productProperty.setValue(id);
        }
    }

    /**
     * Gets the security Keys for a {@link VirtualSystemCollection} or
     * {@link VirtualSystem}.
     * 
     * @return The security keys
     */
    public String getSecurityKeys() {
        return getPropertyByKey(ASCETIC_SECURITY_KEY).getValue();
    }

    /**
     * Sets the security Keys for a {@link VirtualSystemCollection} or
     * {@link VirtualSystem}.
     * 
     * @param securityKeys
     *            The security keys
     */
    public void setSecurityKeys(String securityKeys) {
        ProductProperty productProperty = getPropertyByKey(ASCETIC_SECURITY_KEY);
        if (productProperty == null) {
            addNewProperty(ASCETIC_SECURITY_KEY, ProductPropertyType.STRING,
                    securityKeys);
        } else {
            productProperty.setValue(securityKeys);
        }
    }

    // TODO: Add additional helper methods here to standardise access to ASCETIC
    // specific product properties (e.g. probe end-points etc.)
}
