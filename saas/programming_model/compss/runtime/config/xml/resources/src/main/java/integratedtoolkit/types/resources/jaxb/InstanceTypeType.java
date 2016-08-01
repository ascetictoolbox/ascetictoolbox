//
// This file was generated by the JavaTM Architecture for XML Binding(JAXB) Reference Implementation, v2.2.11 
// See <a href="http://java.sun.com/xml/jaxb">http://java.sun.com/xml/jaxb</a> 
// Any modifications to this file will be lost upon recompilation of the source schema. 
// Generated on: 2016.08.01 at 07:45:16 PM CEST 
//


package integratedtoolkit.types.resources.jaxb;

import java.util.ArrayList;
import java.util.List;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElements;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for InstanceTypeType complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="InstanceTypeType"&gt;
 *   &lt;complexContent&gt;
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType"&gt;
 *       &lt;choice maxOccurs="unbounded"&gt;
 *         &lt;element name="Processor" type="{}ProcessorType" maxOccurs="unbounded"/&gt;
 *         &lt;element name="Memory" type="{}MemoryType" minOccurs="0"/&gt;
 *         &lt;element name="Storage" type="{}StorageType" minOccurs="0"/&gt;
 *         &lt;element name="Price" type="{}PriceType" minOccurs="0"/&gt;
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
@XmlType(name = "InstanceTypeType", propOrder = {
    "processorOrMemoryOrStorage"
})
public class InstanceTypeType {

    @XmlElements({
        @XmlElement(name = "Processor", type = ProcessorType.class),
        @XmlElement(name = "Memory", type = MemoryType.class),
        @XmlElement(name = "Storage", type = StorageType.class),
        @XmlElement(name = "Price", type = PriceType.class)
    })
    protected List<Object> processorOrMemoryOrStorage;
    @XmlAttribute(name = "Name", required = true)
    protected String name;

    /**
     * Gets the value of the processorOrMemoryOrStorage property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the processorOrMemoryOrStorage property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getProcessorOrMemoryOrStorage().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link ProcessorType }
     * {@link MemoryType }
     * {@link StorageType }
     * {@link PriceType }
     * 
     * 
     */
    public List<Object> getProcessorOrMemoryOrStorage() {
        if (processorOrMemoryOrStorage == null) {
            processorOrMemoryOrStorage = new ArrayList<Object>();
        }
        return this.processorOrMemoryOrStorage;
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
