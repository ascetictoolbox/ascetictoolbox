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

import javax.xml.namespace.QName;

import org.dmtf.schemas.ovf.envelope.x1.XmlBeanOperatingSystemSectionType;
import org.dmtf.schemas.ovf.envelope.x1.XmlBeanProductSectionType;
import org.dmtf.schemas.ovf.envelope.x1.XmlBeanSectionType;
import org.dmtf.schemas.ovf.envelope.x1.XmlBeanVirtualHardwareSectionType;
import org.dmtf.schemas.ovf.envelope.x1.XmlBeanVirtualSystemType;

import eu.ascetic.utils.ovf.api.AbstractElement;
import eu.ascetic.utils.ovf.api.utils.XmlSimpleTypeConverter;

/**
 * Provides access to the VirtualSystem element of an OVF document. Virtual
 * machine configurations in OVF are represented by a VirtualSystem or
 * {@link VirtualSystemCollection} element. The VirtualSystem element describes
 * a single virtual machine and is simply a container of section elements. A
 * VirtualSystem can contain the following sections (ordered preferable):<br>
 * <br>
 * AnnotationSection - TODO: Specifies a free-form annotation on an entity.<br>
 * {@link ProductSection} (0-n instances) - Specifies optional
 * product-information for a package, such as product name and version, along
 * with a set of properties that can be configured.<br>
 * EulaSection - TODO: Specifies a license agreement for the software in the
 * package.<br>
 * {@link OperatingSystem} - Specifies the installed guest operating system of a
 * virtual machine.<br>
 * InstallSection - TODO: Specifies that the virtual machine needs to be
 * initially booted to install and configure the software.<br>
 * {@link VirtualHardwareSection} - Specifies the virtual hardware required by a
 * virtual machine.<br>
 * 
 * @author Django Armstrong (ULeeds)
 * 
 */
public class VirtualSystem extends AbstractElement<XmlBeanVirtualSystemType> {

    /**
     * A static reference to the {@link VirtualSystemFactory} class for
     * generating new instances of this object.
     */
    public static VirtualSystemFactory Factory = new VirtualSystemFactory();

    /**
     * Default constructor.
     * 
     * @param base
     *            The XMLBeans base type used for data storage
     */
    public VirtualSystem(XmlBeanVirtualSystemType base) {
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
     * Sets the unique ID of this VirtualSystem.
     * 
     * @return The ID
     */
    public String getId() {
        return delegate.getId();
    }

    /**
     * Gets the unique ID of this VirtualSystem.
     * 
     * @param id
     *            The ID to set
     */
    public void setId(String id) {
        delegate.setId(id);
    }

    /**
     * Gets the human readable name of this VirtualSystem.
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
     * Sets the human readable name of this VirtualSystem.
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

    /**
     * Sets the {@link ProductSection} array held in this object. They describes
     * product information along with a set of properties that can be
     * configured.
     * 
     * @param productSectionArray
     *            The ProductSection[] to set
     */
    public void setProductSectionArray(ProductSection[] productSectionArray) {
        XmlBeanSectionType[] sectionArray = delegate.getSectionArray();

        // Remove the old ProductSections
        if (sectionArray != null) {
            for (int i = 0; i < sectionArray.length; i++) {
                if (sectionArray[i] instanceof XmlBeanProductSectionType) {
                    delegate.removeSection(i);
                }
            }
        }

        // Add in the new product sections
        for (int i = 0; i < productSectionArray.length; i++) {
            XmlBeanSectionType xmlBeanSectionType = delegate.addNewSection();

            xmlBeanSectionType.newCursor().setName(
                    new QName("http://schemas.dmtf.org/ovf/envelope/1",
                            "ProductSection"));
            delegate.setSectionArray(delegate.getSectionArray().length - 1,
                    productSectionArray[i].getXmlObject());
        }
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
        xmlBeanSectionType.newCursor().setName(
                new QName("http://schemas.dmtf.org/ovf/envelope/1",
                        "ProductSection"));
        delegate.setSectionArray(delegate.getSectionArray().length - 1,
                productSection.getXmlObject());
    }

    /**
     * Gets the {@link OperatingSystem} element. It describes the operating
     * system used in the VirtualSystem.
     * 
     * @return The OperatingSystem
     */
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

    /**
     * Sets the {@link OperatingSystem} element. It describes the operating
     * system used in the VirtualSystem.
     * 
     * @param operatingSystem
     *            The OperatingSystem element to set
     */
    public void setOperatingSystem(OperatingSystem operatingSystem) {
        XmlBeanSectionType[] sectionArray = (XmlBeanSectionType[]) delegate
                .getSectionArray();
        if (sectionArray != null) {
            for (int i = 0; i < sectionArray.length; i++) {
                XmlBeanSectionType xmlBeanSectionType = sectionArray[i];
                if (xmlBeanSectionType instanceof XmlBeanOperatingSystemSectionType) {
                    xmlBeanSectionType.newCursor().setName(
                            new QName("http://schemas.dmtf.org/ovf/envelope/1",
                                    "OperatingSystem"));
                    delegate.setSectionArray(i, operatingSystem.getXmlObject());
                    return;
                }
            }
        }

        XmlBeanSectionType xmlBeanSectionType = delegate.addNewSection();
        xmlBeanSectionType.newCursor().setName(
                new QName("http://schemas.dmtf.org/ovf/envelope/1",
                        "OperatingSystemSection"));
        delegate.setSectionArray(delegate.getSectionArray().length - 1,
                operatingSystem.getXmlObject());
    }

    /**
     * Gets the {@link VirtualHardwareSection} element. It describes the virtual
     * hardware required by the VirtualSystem.
     * 
     * @return The VirtualHardwareSection
     */
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

    /**
     * Sets the {@link VirtualHardwareSection} element. It describes the virtual
     * hardware required by the VirtualSystem.
     * 
     * @param virtualHardwareSection
     *            The VirtualHardwareSection to set
     */
    public void setVirtualHardwareSection(
            VirtualHardwareSection virtualHardwareSection) {
        XmlBeanSectionType[] sectionArray = (XmlBeanSectionType[]) delegate
                .getSectionArray();

        if (sectionArray != null) {
            for (int i = 0; i < sectionArray.length; i++) {
                XmlBeanSectionType xmlBeanSectionType = sectionArray[i];
                if (xmlBeanSectionType instanceof XmlBeanVirtualHardwareSectionType) {
                    xmlBeanSectionType.newCursor().setName(
                            new QName("http://schemas.dmtf.org/ovf/envelope/1",
                                    "VirtualHardwareSection"));
                    delegate.setSectionArray(i,
                            virtualHardwareSection.getXmlObject());
                    return;
                }
            }
        }

        XmlBeanSectionType xmlBeanSectionType = delegate.addNewSection();
        xmlBeanSectionType.newCursor().setName(
                new QName("http://schemas.dmtf.org/ovf/envelope/1",
                        "VirtualHardwareSection"));
        delegate.setSectionArray(delegate.getSectionArray().length - 1,
                virtualHardwareSection.getXmlObject());
    }

}
