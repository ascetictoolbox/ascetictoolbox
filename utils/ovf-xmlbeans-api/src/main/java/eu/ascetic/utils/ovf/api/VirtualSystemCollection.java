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

import org.dmtf.schemas.ovf.envelope.x1.XmlBeanContentType;
import org.dmtf.schemas.ovf.envelope.x1.XmlBeanProductSectionType;
import org.dmtf.schemas.ovf.envelope.x1.XmlBeanSectionType;
import org.dmtf.schemas.ovf.envelope.x1.XmlBeanVirtualSystemCollectionType;
import org.dmtf.schemas.ovf.envelope.x1.XmlBeanVirtualSystemType;

import eu.ascetic.utils.ovf.api.AbstractElement;
import eu.ascetic.utils.ovf.api.utils.XmlSimpleTypeConverter;

/**
 * Provides access to the VirtualSystemCollection element of an OVF document.
 * Virtual machine configurations in OVF are represented by a
 * {@link VirtualSystem} or VirtualSystemCollection element. The
 * VirtualSystemCollection element is a container of multiple
 * {@link VirtualSystem} or VirtualSystemCollection elements. Thus, arbitrary
 * complex configurations can be described. The section elements at the
 * VirtualSystemCollection level describe appliance information, properties and
 * resource requirements. A VirtualSystem can contain the following sections
 * (ordered preferable):<br>
 * <br>
 * ResourceAllocationSection - TODO: Specifies reservations, limits, and shares
 * on a given resource, such as memory or CPU for a virtual machine collection.<br>
 * AnnotationSection - TODO: Specifies a free-form annotation on an entity.<br>
 * {@link ProductSection} (0-n instances) - Specifies optional
 * product-information for a package, such as product name and version, along
 * with a set of properties that can be configured.<br>
 * EulaSection - TODO: Specifies a license agreement for the software in the
 * package.<br>
 * StartupSection - TODO: Specifies how a virtual machine collection is powered
 * on.<br>
 * {@link VirtualSystem} (1-n instances) - Specifies virtual machine
 * configurations.<br>
 * {@link VirtualSystemCollection} (0-n instances) - TODO: Specifies other
 * nested {@link VirtualSystemCollection}s.<br>
 * <br>
 * 
 * @author Django Armstrong (ULeeds)
 * 
 */
public class VirtualSystemCollection extends
        AbstractElement<XmlBeanVirtualSystemCollectionType> {

    /**
     * A static reference to the {@link VirtualSystemCollectionFactory} class
     * for generating new instances of this object.
     */
    public static VirtualSystemCollectionFactory Factory = new VirtualSystemCollectionFactory();

    /**
     * Default constructor.
     * 
     * @param base
     *            The XMLBeans base type used for data storage
     */
    public VirtualSystemCollection(XmlBeanVirtualSystemCollectionType base) {
        super(base);
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
     * Gets the unique ID of this VirtualSystemCollection.
     * 
     * @return The ID
     */
    public String getId() {
        return delegate.getId();
    }

    /**
     * Sets the unique ID of this VirtualSystemCollection.
     * 
     * @param id
     *            The ID to set
     */
    public void setId(String id) {
        delegate.setId(id);
    }

    /**
     * Gets the human readable name of this VirtualSystemCollection.
     * 
     * @return The name to get
     */
    public String getName() {
        if (delegate.isSetName()) {
            return delegate.getName().getStringValue();
        }
        return null;
    }

    /**
     * Sets the human readable name of this VirtualSystemCollection.
     * 
     * @param name
     *            The name to set
     */
    public void setName(String name) {
        delegate.setName(XmlSimpleTypeConverter.toMsgType(name));
    }

    /**
     * Gets the {@link ProductSection} array held in this object. They describes
     * product information along with a set of properties that can be
     * configured.
     * 
     * @return The ProductSection[]
     */
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

    /**
     * Sets the {@link ProductSection} array held in this object. They describes
     * product information along with a set of properties that can be
     * configured.
     * 
     * @param productSectionArray
     *            The ProductSection[] to set
     */
    public void setProductSectionArray(ProductSection[] productSectionArray) {
        Vector<XmlBeanSectionType> sectionVector = new Vector<XmlBeanSectionType>();

        XmlBeanSectionType[] sectionArray = (XmlBeanSectionType[]) delegate
                .getSectionArray();

        // Add everything else that's not a ProductSection
        if (sectionArray != null) {
            for (XmlBeanSectionType xmlBeanSection : sectionArray) {
                if (!(xmlBeanSection instanceof XmlBeanProductSectionType)) {
                    sectionVector
                            .add((XmlBeanProductSectionType) xmlBeanSection);
                }
            }
        }

        // Add the new elements
        for (int i = 0; i < productSectionArray.length; i++) {
            sectionVector.add(productSectionArray[i].getXmlObject());
        }

        delegate.setSectionArray((XmlBeanSectionType[]) sectionVector.toArray());
    }

    /**
     * Gets the {@link ProductSection} element at index i of the array. It
     * describes product information along with a set of properties that can be
     * configured.
     * 
     * @param i
     *            The index value
     * @return The ProductSection at index i
     */
    public ProductSection getProductSectionAtIndex(int i) {
        return getProductSectionArray()[i];
    }

    /**
     * Adds a new {@link ProductSection} element to the end of the
     * ProductSection array. It describes product information along with a set
     * of properties that can be configured.
     * 
     * @param productSection
     *            The ProductSection to add to the end of the array.
     */
    public void addProductSection(ProductSection productSection) {
        XmlBeanSectionType xmlBeanSectionType = delegate.addNewSection();
        xmlBeanSectionType.set(productSection.getXmlObject());
    }

    /**
     * Gets the {@link VirtualSystem} array held in this object. They describes
     * virtual machine configuration including virtual hardware requirements.
     * 
     * @return The VirtualSystem[]
     */
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

    /**
     * Sets the {@link VirtualSystem} array held in this object. They describes
     * virtual machine configuration including virtual hardware requirements.
     * 
     * @param virtualSystemArray
     *            The VirtualSystem[] to set
     */
    public void setVirtualSystemArray(VirtualSystem[] virtualSystemArray) {
        Vector<XmlBeanContentType> contentVector = new Vector<XmlBeanContentType>();

        for (int i = 0; i < virtualSystemArray.length; i++) {
            contentVector.add(virtualSystemArray[i].getXmlObject());
        }

        delegate.setContentArray((XmlBeanContentType[]) contentVector.toArray());
    }

    /**
     * Gets the {@link VirtualSystem} element at index i of the array. It
     * describes virtual machine configuration including virtual hardware
     * requirements.
     * 
     * @param i
     *            The index
     * @return The VirtualSystem at index i
     */
    public VirtualSystem getVirtualSystemAtIndex(int i) {
        return getVirtualSystemArray()[i];
    }

    /**
     * Adds a {@link VirtualSystem} element to the end of the VirtualSystem
     * array. It describes virtual machine configuration including virtual
     * hardware requirements.
     * 
     * @param virtualSystem
     */
    public void addVirtualSystem(VirtualSystem virtualSystem) {
        XmlBeanContentType xmlBeanContentType = delegate.addNewContent();
        xmlBeanContentType.set(virtualSystem.getXmlObject());
    }
}
