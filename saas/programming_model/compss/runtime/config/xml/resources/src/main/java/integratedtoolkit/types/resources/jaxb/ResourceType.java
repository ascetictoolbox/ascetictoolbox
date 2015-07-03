//
// This file was generated by the JavaTM Architecture for XML Binding(JAXB) Reference Implementation, v2.2.4 
// See <a href="http://java.sun.com/xml/jaxb">http://java.sun.com/xml/jaxb</a> 
// Any modifications to this file will be lost upon recompilation of the source schema. 
// Generated on: 2015.07.03 at 05:21:11 PM CEST 
//


package integratedtoolkit.types.resources.jaxb;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for resourceType complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="resourceType">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="Capabilities" type="{}capabilitiesType"/>
 *         &lt;element name="Requirements" type="{}requirementsType" minOccurs="0"/>
 *         &lt;element name="Disks" type="{}SharedDiskListType" minOccurs="0"/>
 *         &lt;element name="Price" type="{http://www.w3.org/2001/XMLSchema}float" minOccurs="0"/>
 *       &lt;/sequence>
 *       &lt;attribute name="Name" use="required" type="{http://www.w3.org/2001/XMLSchema}string" />
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "resourceType", propOrder = {
    "capabilities",
    "requirements",
    "disks",
    "price"
})
public class ResourceType {

    @XmlElement(name = "Capabilities", required = true)
    protected CapabilitiesType capabilities;
    @XmlElement(name = "Requirements")
    protected RequirementsType requirements;
    @XmlElement(name = "Disks")
    protected SharedDiskListType disks;
    @XmlElement(name = "Price")
    protected Float price;
    @XmlAttribute(name = "Name", required = true)
    protected String name;

    /**
     * Gets the value of the capabilities property.
     * 
     * @return
     *     possible object is
     *     {@link CapabilitiesType }
     *     
     */
    public CapabilitiesType getCapabilities() {
        return capabilities;
    }

    /**
     * Sets the value of the capabilities property.
     * 
     * @param value
     *     allowed object is
     *     {@link CapabilitiesType }
     *     
     */
    public void setCapabilities(CapabilitiesType value) {
        this.capabilities = value;
    }

    /**
     * Gets the value of the requirements property.
     * 
     * @return
     *     possible object is
     *     {@link RequirementsType }
     *     
     */
    public RequirementsType getRequirements() {
        return requirements;
    }

    /**
     * Sets the value of the requirements property.
     * 
     * @param value
     *     allowed object is
     *     {@link RequirementsType }
     *     
     */
    public void setRequirements(RequirementsType value) {
        this.requirements = value;
    }

    /**
     * Gets the value of the disks property.
     * 
     * @return
     *     possible object is
     *     {@link SharedDiskListType }
     *     
     */
    public SharedDiskListType getDisks() {
        return disks;
    }

    /**
     * Sets the value of the disks property.
     * 
     * @param value
     *     allowed object is
     *     {@link SharedDiskListType }
     *     
     */
    public void setDisks(SharedDiskListType value) {
        this.disks = value;
    }

    /**
     * Gets the value of the price property.
     * 
     * @return
     *     possible object is
     *     {@link Float }
     *     
     */
    public Float getPrice() {
        return price;
    }

    /**
     * Sets the value of the price property.
     * 
     * @param value
     *     allowed object is
     *     {@link Float }
     *     
     */
    public void setPrice(Float value) {
        this.price = value;
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
