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
     * The static KEY used to get and set the number of end points in the global
     * scope of {@link VirtualSystemCollection} or locally in
     * {@link VirtualSystem}.
     */
    private static final String ASCETIC_ENDPOINT_NUMBER = "asceticEndPointNumber";
    /**
     * The static base KEY used to get and set an end point ID either in the
     * global scope of {@link VirtualSystemCollection} or locally in
     * {@link VirtualSystem}.
     */
    private static final String ASCETIC_ENDPOINT_ID_KEY = "asceticEndPointId_";
    /**
     * The static base KEY used to get and set an end point URI either in the
     * global scope of {@link VirtualSystemCollection} or locally in
     * {@link VirtualSystem}.
     */
    private static final String ASCETIC_ENDPOINT_URI_KEY = "asceticEndPointUri_";
    /**
     * The static base KEY used to get and set an end point type (e.g. "probe")
     * either in the global scope of {@link VirtualSystemCollection} or locally
     * in {@link VirtualSystem}.
     */
    private static final String ASCETIC_ENDPOINT_TYPE_KEY = "asceticEndPointType_";
    /**
     * The static base KEY used to get and set an end point subtype (e.g. "mem")
     * either in the global scope of {@link VirtualSystemCollection} or locally
     * in {@link VirtualSystem}.
     */
    private static final String ASCETIC_ENDPOINT_SUBTYPE_KEY = "asceticEndPointSubtype_";
    /**
     * The static base KEY used to get and set an end point reporting interval
     * either in the global scope of {@link VirtualSystemCollection} or locally
     * in {@link VirtualSystem}.
     */
    private static final String ASCETIC_ENDPOINT_INTERVAL_KEY = "asceticEndPointInterval_";

    /**
     * The static KEY used to get and set the VMIC mode (offline or online). a
     * per {@link VirtualSystem} basis.
     */
    private static final String ASCETIC_VMIC_MODE_KEY = "asceticVmicMode";
    /**
     * The static KEY used to get and set a script to be executed by the VMIC on
     * a per {@link VirtualSystem} basis.
     */
    private static final String ASCETIC_VMIC_SCRIPT_KEY = "asceticVmicScript";

    /**
     * The static KEY used to get and set the number of software package
     * {@link ProductProperty} sets in the global scope of
     * {@link VirtualSystemCollection} or locally in {@link VirtualSystem}.
     */
    private static final String ASCETIC_SOFTWARE_DEPENDENCY_NUMBER = "asceticSoftwareDependencyNumber";
    /**
     * The static KEY used to get and set a software package ID on a per
     * {@link VirtualSystem} basis.
     */
    private static final String ASCETIC_SOFTWARE_DEPENDENCY_ID_KEY = "asceticSoftwareDependencyId_";
    /**
     * The static KEY used to get and set a software package's type in the
     * global scope of {@link VirtualSystemCollection} or locally in
     * {@link VirtualSystem}.
     */
    private static final String ASCETIC_SOFTWARE_DEPENDENCY_TYPE_KEY = "asceticSoftwareDependencyType_";
    /**
     * The static KEY used to get and set a software package URI in the global
     * scope of {@link VirtualSystemCollection} or locally in
     * {@link VirtualSystem}.
     */
    private static final String ASCETIC_SOFTWARE_DEPENDENCY_URI_KEY = "asceticSoftwareDependencyUri_";
    /**
     * The static KEY used to get and set a software package installation script
     * in the global scope of {@link VirtualSystemCollection} or locally in
     * {@link VirtualSystem}.
     */
    private static final String ASCETIC_SOFTWARE_DEPENDENCY_INSTALL_SCRIPT_KEY = "asceticSoftwareDependencyInstallScript_";

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
        delegate.setPropertyArray(newPropertyArray
                .toArray(new XmlBeanProductSectionType.Property[newPropertyArray
                        .size()]));
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
     * Gets the index into the internal {@link ProductProperty}[] for a
     * {@link ProductProperty} with a given key.
     * 
     * @param key
     *            The key identifying the {@link ProductProperty}
     * @return The index of the {@link ProductProperty}
     */
    public int getPropertyIndexByKey(String key) {
        ProductProperty[] productProperties = getPropertyArray();
        for (int i = 0; i < productProperties.length; i++) {
            if (key.equals(productProperties[i].getKey())) {
                return i;
            }
        }
        return -1;
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

    /**
     * Removes a {@link ProductProperty} identified by a given key.
     * 
     * @param key
     *            The key of the {@link ProductProperty} to remove
     */
    public void removePropertyByKey(String key) {
        ProductProperty[] productProperties = getPropertyArray();
        for (int i = 0; i < productProperties.length; i++) {
            if (key.equals(productProperties[i].getKey())) {
                delegate.removeProperty(i);
                break;
            }
        }
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
     * Gets the security Keys (public/private pair) for a
     * {@link VirtualSystemCollection} or {@link VirtualSystem}.
     * 
     * @return The security keys
     */
    public String getSecurityKeys() {
        return getPropertyByKey(ASCETIC_SECURITY_KEY).getValue();
    }

    /**
     * Sets the security Keys (public/private pair) for a
     * {@link VirtualSystemCollection} or {@link VirtualSystem}.
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

    /**
     * Adds a new set of properties that define an end point.
     * 
     * @param id
     *            The ID of the end point (e.g. "memory-probe")
     * @param uri
     *            The URI of the end point (e.g.
     *            ("uri://some-end-point/application-monitor")
     * @param type
     *            The type of the end point (e.g. "probe")
     * @param subtype
     *            The subtype of the end point (e.g. "memory")
     * @param interval
     *            The interval at which to report to the end point
     * @return The index of the new end point (not to be confused with the index
     *         of a {@link ProductProperty})
     */
    public int addEndPointProperties(String id, String uri, String type,
            String subtype, String interval) {

        // Find the next end point index
        int i = 0;
        while (true) {
            ProductProperty productProperty = getPropertyByKey(ASCETIC_ENDPOINT_ID_KEY
                    + i);
            if (productProperty == null) {
                break;
            }
            i++;
        }

        addNewProperty(ASCETIC_ENDPOINT_ID_KEY + i, ProductPropertyType.STRING,
                id);
        addNewProperty(ASCETIC_ENDPOINT_URI_KEY + i,
                ProductPropertyType.STRING, uri);
        addNewProperty(ASCETIC_ENDPOINT_TYPE_KEY + i,
                ProductPropertyType.STRING, type);
        addNewProperty(ASCETIC_ENDPOINT_SUBTYPE_KEY + i,
                ProductPropertyType.STRING, subtype);
        addNewProperty(ASCETIC_ENDPOINT_INTERVAL_KEY + i,
                ProductPropertyType.STRING, interval);

        // Increment the number of end points stored
        ProductProperty productProperty = getPropertyByKey(ASCETIC_ENDPOINT_NUMBER);
        if (productProperty == null) {
            addNewProperty(ASCETIC_ENDPOINT_NUMBER, ProductPropertyType.UINT32,
                    "1");
        } else {
            Integer newEndPointNumber = ((Integer) productProperty
                    .getValueAsJavaObject()) + 1;
            productProperty.setValue(newEndPointNumber.toString());
        }

        // Return the end point index
        return i;
    }

    /**
     * Gets an end point's index by its ID in the array of end point property
     * sets (not to be confused with the index of a {@link ProductProperty}).
     * 
     * @param id
     *            The ID of the end point
     * @return The end point index
     */
    public int getEndPointIndexById(String id) {

        for (int i = 0; i < getEndPointNumber(); i++) {
            ProductProperty productProperty = getPropertyByKey(ASCETIC_ENDPOINT_ID_KEY
                    + i);

            if (productProperty != null
                    && id.equals(productProperty.getValue())) {
                return i;
            }
        }
        return -1;
    }

    /**
     * Sets a set of properties that define an end point at a end point property
     * set index.
     * 
     * @param index
     *            The index of the end point (not to be confused with the index
     *            of a {@link ProductProperty}, see
     *            {@link ProductSection#getEndPointIndexById(String)})
     * @param id
     *            The ID of the end point (e.g. "memory-probe")
     * @param uri
     *            The URI of the end point (e.g.
     *            ("uri://some-end-point/application-monitor")
     * @param type
     *            The type of the end point (e.g. "probe")
     * @param subtype
     *            The subtype of the end point (e.g. "memory")
     * @param interval
     *            The interval at which to report to the end point
     */
    public void setEndPointProperties(int index, String id, String uri,
            String type, String subtype, String interval) {
        setEndPointId(index, id);
        setEndPointUri(index, uri);
        setEndPointType(index, subtype);
        setEndPointSubtype(index, subtype);
        setEndPointInterval(index, interval);
    }

    /**
     * Gets the ID of an end point at a specific index.
     * 
     * @param index
     *            The index of the end point (not to be confused with the index
     *            of a {@link ProductProperty}, see
     *            {@link ProductSection#getEndPointIndexById(String)})
     * @return The ID of the end point (e.g. "memory-probe")
     */
    public String getEndPointId(int index) {
        return getPropertyByKey(ASCETIC_ENDPOINT_ID_KEY + index).getValue();
    }

    /**
     * Sets the ID of an end point at a specific index.
     * 
     * @param index
     *            The index of the end point (not to be confused with the index
     *            of a {@link ProductProperty}, see
     *            {@link ProductSection#getEndPointIndexById(String)})
     * @param id
     *            The ID of the end point (e.g. "memory-probe")
     */
    public void setEndPointId(int index, String id) {
        getPropertyByKey(ASCETIC_ENDPOINT_ID_KEY + index).setValue(id);
    }

    /**
     * Gets the URI of an end point at a specific index.
     * 
     * @param index
     *            The index of the end point (not to be confused with the index
     *            of a {@link ProductProperty}, see
     *            {@link ProductSection#getEndPointIndexById(String)})
     * @return The URI of the end point (e.g.
     *         ("uri://some-end-point/application-monitor")
     */
    public String getEndPointUri(int index) {
        return getPropertyByKey(ASCETIC_ENDPOINT_URI_KEY + index).getValue();
    }

    /**
     * Sets the URI of an end point at a specific index.
     * 
     * @param index
     *            The index of the end point (not to be confused with the index
     *            of a {@link ProductProperty}, see
     *            {@link ProductSection#getEndPointIndexById(String)})
     * @param uri
     *            The URI of the end point (e.g.
     *            ("uri://some-end-point/application-monitor")
     */
    public void setEndPointUri(int index, String uri) {
        getPropertyByKey(ASCETIC_ENDPOINT_URI_KEY + index).setValue(uri);
    }

    /**
     * Gets the type of an end point at a specific index.
     * 
     * @param index
     *            The index of the end point (not to be confused with the index
     *            of a {@link ProductProperty}, see
     *            {@link ProductSection#getEndPointIndexById(String)})
     * @return The type of the end point (e.g. "probe")
     */
    public String getEndPointType(int index) {
        return getPropertyByKey(ASCETIC_ENDPOINT_TYPE_KEY + index).getValue();
    }

    /**
     * Sets the type of an end point at a specific index.
     * 
     * @param index
     *            The index of the end point (not to be confused with the index
     *            of a {@link ProductProperty}, see
     *            {@link ProductSection#getEndPointIndexById(String)})
     * @param type
     *            The type of the end point (e.g. "probe")
     */
    public void setEndPointType(int index, String type) {
        getPropertyByKey(ASCETIC_ENDPOINT_TYPE_KEY + index).setValue(type);
    }

    /**
     * Gets the subtype of an end point at a specific index.
     * 
     * @param index
     *            The index of the end point (not to be confused with the index
     *            of a {@link ProductProperty}, see
     *            {@link ProductSection#getEndPointIndexById(String)})
     * @return The subtype of the end point (e.g. "memory")
     */
    public String getEndPointSubtype(int index) {
        return getPropertyByKey(ASCETIC_ENDPOINT_SUBTYPE_KEY + index)
                .getValue();
    }

    /**
     * Sets the subtype of an end point at a specific index.
     * 
     * @param index
     *            The index of the end point (not to be confused with the index
     *            of a {@link ProductProperty}, see
     *            {@link ProductSection#getEndPointIndexById(String)})
     * @param subtype
     *            The subtype of the end point (e.g. "memory")
     */
    public void setEndPointSubtype(int index, String subtype) {
        getPropertyByKey(ASCETIC_ENDPOINT_SUBTYPE_KEY + index)
                .setValue(subtype);
    }

    /**
     * Gets the reporting interval of an end point at a specific index.
     * 
     * @param index
     *            The index of the end point (not to be confused with the index
     *            of a {@link ProductProperty}, see
     *            {@link ProductSection#getEndPointIndexById(String)})
     * @return The interval at which to report to the end point
     */
    public String getEndPointInterval(int index) {
        return getPropertyByKey(ASCETIC_ENDPOINT_INTERVAL_KEY + index)
                .getValue();
    }

    /**
     * Sets the reporting interval of an end point at a specific index.
     * 
     * @param index
     *            The index of the end point (not to be confused with the index
     *            of a {@link ProductProperty}, see
     *            {@link ProductSection#getEndPointIndexById(String)})
     * @param interval
     *            The interval at which to report to the end point
     */
    public void setEndPointInterval(int index, String interval) {
        getPropertyByKey(ASCETIC_ENDPOINT_INTERVAL_KEY + index).setValue(
                interval);
    }

    /**
     * Remove a set of end point properties at a specific index.
     * 
     * @param index
     *            The index of the end point to remove (not to be confused with
     *            the index of a {@link ProductProperty}, see
     *            {@link ProductSection#getEndPointIndexById(String)})
     */
    public void removeEndPointProperties(int index) {
        removePropertyByKey(ASCETIC_ENDPOINT_ID_KEY + index);
        removePropertyByKey(ASCETIC_ENDPOINT_URI_KEY + index);
        removePropertyByKey(ASCETIC_ENDPOINT_TYPE_KEY + index);
        removePropertyByKey(ASCETIC_ENDPOINT_SUBTYPE_KEY + index);
        removePropertyByKey(ASCETIC_ENDPOINT_INTERVAL_KEY + index);

        // FIXME: We should decrement by 1 the index of all subsequent property
        // sets

        ProductProperty productProperty = getPropertyByKey(ASCETIC_ENDPOINT_NUMBER);
        Integer newEndPointNumber = ((Integer) productProperty
                .getValueAsJavaObject()) - 1;
        productProperty.setValue(newEndPointNumber.toString());
    }

    /**
     * Gets the number of end point property sets stored in this
     * {@link ProductSection}.
     * 
     * @return The number of end points
     */
    public int getEndPointNumber() {
        ProductProperty productProperty = getPropertyByKey(ASCETIC_ENDPOINT_NUMBER);
        if (productProperty == null) {
            return 0;
        } else {
            return ((Integer) productProperty.getValueAsJavaObject());
        }
    }

    /**
     * Gets the VMIC offline mode script for this {@link VirtualSystem}.
     * 
     * @return The VMIC script
     */
    public String getVmicScript() {
        return getPropertyByKey(ASCETIC_VMIC_SCRIPT_KEY).getValue();
    }

    /**
     * Sets the VMIC offline mode script for this {@link VirtualSystem}.
     * 
     * @param script
     *            The VMIC script to set
     */
    public void setVmicScript(String script) {
        ProductProperty productProperty = getPropertyByKey(ASCETIC_VMIC_SCRIPT_KEY);
        if (productProperty == null) {
            addNewProperty(ASCETIC_VMIC_SCRIPT_KEY, ProductPropertyType.STRING,
                    script);
        } else {
            productProperty.setValue(script);
        }
    }

    /**
     * Gets the VMIC mode, either "offline" or "online".
     * 
     * @return The VMIC mode
     */
    public String getVmicMode() {
        return getPropertyByKey(ASCETIC_VMIC_MODE_KEY).getValue();
    }

    /**
     * Sets the VMIC mode, either "offline" or "online".
     * 
     * @param mode
     *            The VMIC mode to set
     */
    public void setVmicMode(String mode) {
        ProductProperty productProperty = getPropertyByKey(ASCETIC_VMIC_MODE_KEY);
        if (productProperty == null) {
            addNewProperty(ASCETIC_VMIC_MODE_KEY, ProductPropertyType.UINT32,
                    mode);
        } else {
            productProperty.setValue(mode);
        }
    }

    /**
     * Adds a new set of properties that define a software dependency.
     * 
     * @param id
     *            The ID of the software dependency (e.g. "memory-probe")
     * @param uri
     *            The URI to the software dependency (e.g.
     *            ("uri://some-end-point/probe-repository/memory-probe.zip")
     * @param type
     *            The type of the software dependency (e.g. "zip")
     * @param script
     *            The installation script of the software dependency
     * @return The index of the new software dependency (not to be confused with
     *         the index of a {@link ProductProperty})
     */
    public int addSoftwareDependencyProperties(String id, String uri,
            String type, String script) {

        // Find the next software property index
        int i = 0;
        while (true) {
            ProductProperty productProperty = getPropertyByKey(ASCETIC_SOFTWARE_DEPENDENCY_ID_KEY
                    + i);
            if (productProperty == null) {
                break;
            }
            i++;
        }

        addNewProperty(ASCETIC_SOFTWARE_DEPENDENCY_ID_KEY + i,
                ProductPropertyType.STRING, id);
        addNewProperty(ASCETIC_SOFTWARE_DEPENDENCY_URI_KEY + i,
                ProductPropertyType.STRING, uri);
        addNewProperty(ASCETIC_SOFTWARE_DEPENDENCY_TYPE_KEY + i,
                ProductPropertyType.STRING, type);
        addNewProperty(ASCETIC_SOFTWARE_DEPENDENCY_INSTALL_SCRIPT_KEY + i,
                ProductPropertyType.STRING, script);

        // Increment the number of software dependencies stored
        ProductProperty productProperty = getPropertyByKey(ASCETIC_SOFTWARE_DEPENDENCY_NUMBER);
        if (productProperty == null) {
            addNewProperty(ASCETIC_SOFTWARE_DEPENDENCY_NUMBER,
                    ProductPropertyType.UINT32, "1");
        } else {
            Integer newSoftwareDependencyNumber = ((Integer) productProperty
                    .getValueAsJavaObject()) + 1;
            productProperty.setValue(newSoftwareDependencyNumber.toString());
        }

        // Return the software dependency index
        return i;
    }

    /**
     * Gets an software dependency's index by its ID in the array of software
     * dependency property sets (not to be confused with the index of a
     * {@link ProductProperty}).
     * 
     * @param id
     *            The ID of the software dependency
     * @return The software dependency's index
     */
    public int getSoftwareDependencyIndexById(String id) {

        for (int i = 0; i < getSoftwareDependencyNumber(); i++) {
            ProductProperty productProperty = getPropertyByKey(ASCETIC_SOFTWARE_DEPENDENCY_ID_KEY
                    + i);

            if (productProperty != null
                    && id.equals(productProperty.getValue())) {
                return i;
            }
        }
        return -1;
    }

    /**
     * Sets a set of properties that define a software dependency at a software
     * dependency property index.
     * 
     * @param index
     *            The index of the software dependency (not to be confused with
     *            the index of a {@link ProductProperty}, see
     *            {@link ProductSection#getSoftwareDependencyIndexById(String)})
     * @param id
     *            The ID of the software dependency (e.g. "memory-probe")
     * @param uri
     *            The URI to the software dependency (e.g.
     *            ("uri://some-end-point/probe-repository/memory-probe.zip")
     * @param type
     *            The type of the software dependency (e.g. "zip")
     * @param script
     *            The installation script of the software dependency
     */
    public void setSoftwareDependencyProperties(int index, String id,
            String uri, String type, String script) {
        setSoftwareDependencyId(index, id);
        setSoftwareDependencyUri(index, uri);
        setSoftwareDependencyType(index, type);
        setSoftwareDependencyScript(index, script);
    }

    /**
     * Gets the ID of a software dependency at a specific index.
     * 
     * @param index
     *            The index of the software dependency (not to be confused with
     *            the index of a {@link ProductProperty}, see
     *            {@link ProductSection#getSoftwareDependencyIndexById(String)})
     * @return The ID of the software dependency (e.g. "memory-probe")
     */
    public String getSoftwareDependencyId(int index) {
        return getPropertyByKey(ASCETIC_SOFTWARE_DEPENDENCY_ID_KEY + index)
                .getValue();
    }

    /**
     * Sets the ID of an software dependency at a specific index.
     * 
     * @param index
     *            The index of the software dependency (not to be confused with
     *            the index of a {@link ProductProperty}, see
     *            {@link ProductSection#getSoftwareDependencyIndexById(String)})
     * @param id
     *            The ID of the software dependency (e.g. "memory-probe")
     */
    public void setSoftwareDependencyId(int index, String id) {
        getPropertyByKey(ASCETIC_SOFTWARE_DEPENDENCY_ID_KEY + index).setValue(
                id);
    }

    /**
     * Gets the URI of an software dependency at a specific index.
     * 
     * @param index
     *            The index of the software dependency (not to be confused with
     *            the index of a {@link ProductProperty}, see
     *            {@link ProductSection#getSoftwareDependencyIndexById(String)})
     * @Return The URI to the software dependency (e.g.
     *         ("uri://some-end-point/probe-repository/memory-probe.zip")
     */
    public String getSoftwareDependencyUri(int index) {
        return getPropertyByKey(ASCETIC_SOFTWARE_DEPENDENCY_URI_KEY + index)
                .getValue();
    }

    /**
     * Sets the URI of an software dependency at a specific index.
     * 
     * @param index
     *            The index of the software dependency (not to be confused with
     *            the index of a {@link ProductProperty}, see
     *            {@link ProductSection#getSoftwareDependencyIndexById(String)})
     * @param uri
     *            The URI to the software dependency (e.g.
     *            ("uri://some-end-point/probe-repository/memory-probe.zip")
     */
    public void setSoftwareDependencyUri(int index, String uri) {
        getPropertyByKey(ASCETIC_SOFTWARE_DEPENDENCY_URI_KEY + index).setValue(
                uri);
    }

    /**
     * Gets the type of a software dependency at a specific index.
     * 
     * @param index
     *            The index of the software dependency (not to be confused with
     *            the index of a {@link ProductProperty}, see
     *            {@link ProductSection#getSoftwareDependencyIndexById(String)})
     * @return The type of the software dependency (e.g. "zip")
     */
    public String getSoftwareDependencyType(int index) {
        return getPropertyByKey(ASCETIC_SOFTWARE_DEPENDENCY_TYPE_KEY + index)
                .getValue();
    }

    /**
     * Sets the type of a software dependency at a specific index.
     * 
     * @param index
     *            The index of the software dependency (not to be confused with
     *            the index of a {@link ProductProperty}, see
     *            {@link ProductSection#getSoftwareDependencyIndexById(String)})
     * @param type
     *            The type of the software dependency (e.g. "zip")
     */
    public void setSoftwareDependencyType(int index, String type) {
        getPropertyByKey(ASCETIC_SOFTWARE_DEPENDENCY_TYPE_KEY + index)
                .setValue(type);
    }

    /**
     * Gets the installation script of a software dependency at a specific
     * index.
     * 
     * @param index
     *            The index of the software dependency (not to be confused with
     *            the index of a {@link ProductProperty}, see
     *            {@link ProductSection#getSoftwareDependencyIndexById(String)})
     * @return The installation script of the software dependency
     */
    public String getSoftwareDependencyScript(int index) {
        return getPropertyByKey(
                ASCETIC_SOFTWARE_DEPENDENCY_INSTALL_SCRIPT_KEY + index)
                .getValue();
    }

    /**
     * Sets the installation script of a software dependency at a specific
     * index.
     * 
     * @param index
     *            The index of the software dependency (not to be confused with
     *            the index of a {@link ProductProperty}, see
     *            {@link ProductSection#getSoftwareDependencyIndexById(String)})
     * @param script
     *            The installation script of the software dependency
     */
    public void setSoftwareDependencyScript(int index, String script) {
        getPropertyByKey(ASCETIC_SOFTWARE_DEPENDENCY_INSTALL_SCRIPT_KEY + index)
                .setValue(script);
    }

    /**
     * Remove a set of software dependency properties at a specific index.
     * 
     * @param index
     *            The index of the software dependency (not to be confused with
     *            the index of a {@link ProductProperty}, see
     *            {@link ProductSection#getSoftwareDependencyIndexById(String)})
     */
    public void removeSoftwareDependencyProperties(int index) {
        removePropertyByKey(ASCETIC_SOFTWARE_DEPENDENCY_ID_KEY + index);
        removePropertyByKey(ASCETIC_SOFTWARE_DEPENDENCY_URI_KEY + index);
        removePropertyByKey(ASCETIC_SOFTWARE_DEPENDENCY_TYPE_KEY + index);
        removePropertyByKey(ASCETIC_SOFTWARE_DEPENDENCY_INSTALL_SCRIPT_KEY
                + index);

        // FIXME: We should decrement by 1 the index of all subsequent property
        // sets

        ProductProperty productProperty = getPropertyByKey(ASCETIC_SOFTWARE_DEPENDENCY_NUMBER);
        Integer newSoftwareDependencyNumber = ((Integer) productProperty
                .getValueAsJavaObject()) - 1;
        productProperty.setValue(newSoftwareDependencyNumber.toString());
    }

    /**
     * Gets the number of software dependency property sets stored in this
     * {@link ProductSection}.
     * 
     * @return The number of software dependencies
     */
    public int getSoftwareDependencyNumber() {
        ProductProperty productProperty = getPropertyByKey(ASCETIC_SOFTWARE_DEPENDENCY_NUMBER);
        if (productProperty == null) {
            return 0;
        } else {
            return ((Integer) productProperty.getValueAsJavaObject());
        }
    }

    // TODO: Add additional helper methods here to standardise access to ASCETIC
    // specific product properties.
}
