//
// This file was generated by the JavaTM Architecture for XML Binding(JAXB) Reference Implementation, v2.2.4 
// See <a href="http://java.sun.com/xml/jaxb">http://java.sun.com/xml/jaxb</a> 
// Any modifications to this file will be lost upon recompilation of the source schema. 
// Generated on: 2015.08.06 at 03:09:21 PM CEST 
//


package integratedtoolkit.types.resources.jaxb;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for memoryType complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="memoryType">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="PhysicalSize" type="{http://www.w3.org/2001/XMLSchema}float"/>
 *         &lt;element name="VirtualSize" type="{http://www.w3.org/2001/XMLSchema}float" minOccurs="0"/>
 *         &lt;element name="AccessTime" type="{http://www.w3.org/2001/XMLSchema}float" minOccurs="0"/>
 *         &lt;element name="STR" type="{http://www.w3.org/2001/XMLSchema}float" minOccurs="0"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "memoryType", propOrder = {
    "physicalSize",
    "virtualSize",
    "accessTime",
    "str"
})
public class MemoryType {

    @XmlElement(name = "PhysicalSize")
    protected float physicalSize;
    @XmlElement(name = "VirtualSize")
    protected Float virtualSize;
    @XmlElement(name = "AccessTime")
    protected Float accessTime;
    @XmlElement(name = "STR")
    protected Float str;

    /**
     * Gets the value of the physicalSize property.
     * 
     */
    public float getPhysicalSize() {
        return physicalSize;
    }

    /**
     * Sets the value of the physicalSize property.
     * 
     */
    public void setPhysicalSize(float value) {
        this.physicalSize = value;
    }

    /**
     * Gets the value of the virtualSize property.
     * 
     * @return
     *     possible object is
     *     {@link Float }
     *     
     */
    public Float getVirtualSize() {
        return virtualSize;
    }

    /**
     * Sets the value of the virtualSize property.
     * 
     * @param value
     *     allowed object is
     *     {@link Float }
     *     
     */
    public void setVirtualSize(Float value) {
        this.virtualSize = value;
    }

    /**
     * Gets the value of the accessTime property.
     * 
     * @return
     *     possible object is
     *     {@link Float }
     *     
     */
    public Float getAccessTime() {
        return accessTime;
    }

    /**
     * Sets the value of the accessTime property.
     * 
     * @param value
     *     allowed object is
     *     {@link Float }
     *     
     */
    public void setAccessTime(Float value) {
        this.accessTime = value;
    }

    /**
     * Gets the value of the str property.
     * 
     * @return
     *     possible object is
     *     {@link Float }
     *     
     */
    public Float getSTR() {
        return str;
    }

    /**
     * Sets the value of the str property.
     * 
     * @param value
     *     allowed object is
     *     {@link Float }
     *     
     */
    public void setSTR(Float value) {
        this.str = value;
    }

}
