//
// This file was generated by the JavaTM Architecture for XML Binding(JAXB) Reference Implementation, v2.2.4 
// See <a href="http://java.sun.com/xml/jaxb">http://java.sun.com/xml/jaxb</a> 
// Any modifications to this file will be lost upon recompilation of the source schema. 
// Generated on: 2015.08.06 at 03:09:21 PM CEST 
//


package integratedtoolkit.types.resources.jaxb;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for imageType complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="imageType">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="Architecture" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="OSType" type="{}OSTypeType" minOccurs="0"/>
 *         &lt;element name="ApplicationSoftware" type="{}applicationSoftwareType" minOccurs="0"/>
 *         &lt;element name="SharedDisks" type="{}SharedDiskListType" minOccurs="0"/>
 *       &lt;/sequence>
 *       &lt;attribute name="name" use="required" type="{http://www.w3.org/2001/XMLSchema}string" />
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "imageType", propOrder = {
    "architecture",
    "osType",
    "applicationSoftware",
    "sharedDisks"
})
public class ImageType {

    @XmlElement(name = "Architecture")
    protected String architecture;
    @XmlElement(name = "OSType")
    protected OSTypeType osType;
    @XmlElement(name = "ApplicationSoftware")
    protected ApplicationSoftwareType applicationSoftware;
    @XmlElement(name = "SharedDisks")
    protected SharedDiskListType sharedDisks;
    @XmlAttribute(name = "name", required = true)
    protected String name;

    /**
     * Gets the value of the architecture property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getArchitecture() {
        return architecture;
    }

    /**
     * Sets the value of the architecture property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setArchitecture(String value) {
        this.architecture = value;
    }

    /**
     * Gets the value of the osType property.
     * 
     * @return
     *     possible object is
     *     {@link OSTypeType }
     *     
     */
    public OSTypeType getOSType() {
        return osType;
    }

    /**
     * Sets the value of the osType property.
     * 
     * @param value
     *     allowed object is
     *     {@link OSTypeType }
     *     
     */
    public void setOSType(OSTypeType value) {
        this.osType = value;
    }

    /**
     * Gets the value of the applicationSoftware property.
     * 
     * @return
     *     possible object is
     *     {@link ApplicationSoftwareType }
     *     
     */
    public ApplicationSoftwareType getApplicationSoftware() {
        return applicationSoftware;
    }

    /**
     * Sets the value of the applicationSoftware property.
     * 
     * @param value
     *     allowed object is
     *     {@link ApplicationSoftwareType }
     *     
     */
    public void setApplicationSoftware(ApplicationSoftwareType value) {
        this.applicationSoftware = value;
    }

    /**
     * Gets the value of the sharedDisks property.
     * 
     * @return
     *     possible object is
     *     {@link SharedDiskListType }
     *     
     */
    public SharedDiskListType getSharedDisks() {
        return sharedDisks;
    }

    /**
     * Sets the value of the sharedDisks property.
     * 
     * @param value
     *     allowed object is
     *     {@link SharedDiskListType }
     *     
     */
    public void setSharedDisks(SharedDiskListType value) {
        this.sharedDisks = value;
    }

    /**
     * Gets the value of the name property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getName() {
        return name;
    }

    /**
     * Sets the value of the name property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setName(String value) {
        this.name = value;
    }

}
