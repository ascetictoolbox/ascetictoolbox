//
// This file was generated by the JavaTM Architecture for XML Binding(JAXB) Reference Implementation, v2.2.11 
// See <a href="http://java.sun.com/xml/jaxb">http://java.sun.com/xml/jaxb</a> 
// Any modifications to this file will be lost upon recompilation of the source schema. 
// Generated on: 2016.08.03 at 01:32:11 PM CEST 
//


package integratedtoolkit.types.project.jaxb;

import java.util.ArrayList;
import java.util.List;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElements;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for CloudProviderType complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="CloudProviderType"&gt;
 *   &lt;complexContent&gt;
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType"&gt;
 *       &lt;choice maxOccurs="unbounded" minOccurs="2"&gt;
 *         &lt;element name="Images" type="{}ImagesType"/&gt;
 *         &lt;element name="InstanceTypes" type="{}InstanceTypesType"/&gt;
 *         &lt;element name="LimitOfVMs" type="{http://www.w3.org/2001/XMLSchema}int" minOccurs="0"/&gt;
 *         &lt;element name="Properties" type="{}CloudPropertiesType" minOccurs="0"/&gt;
 *       &lt;/choice&gt;
 *       &lt;attribute name="Name" use="required" type="{http://www.w3.org/2001/XMLSchema}string" /&gt;
 *     &lt;/restriction&gt;
 *   &lt;/complexContent&gt;
 * &lt;/complexType&gt;
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "CloudProviderType", propOrder = {
    "imagesOrInstanceTypesOrLimitOfVMs"
})
public class CloudProviderType {

    @XmlElements({
        @XmlElement(name = "Images", type = ImagesType.class),
        @XmlElement(name = "InstanceTypes", type = InstanceTypesType.class),
        @XmlElement(name = "LimitOfVMs", type = Integer.class),
        @XmlElement(name = "Properties", type = CloudPropertiesType.class)
    })
    protected List<Object> imagesOrInstanceTypesOrLimitOfVMs;
    @XmlAttribute(name = "Name", required = true)
    protected String name;

    /**
     * Gets the value of the imagesOrInstanceTypesOrLimitOfVMs property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the imagesOrInstanceTypesOrLimitOfVMs property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getImagesOrInstanceTypesOrLimitOfVMs().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link ImagesType }
     * {@link InstanceTypesType }
     * {@link Integer }
     * {@link CloudPropertiesType }
     * 
     * 
     */
    public List<Object> getImagesOrInstanceTypesOrLimitOfVMs() {
        if (imagesOrInstanceTypesOrLimitOfVMs == null) {
            imagesOrInstanceTypesOrLimitOfVMs = new ArrayList<Object>();
        }
        return this.imagesOrInstanceTypesOrLimitOfVMs;
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
