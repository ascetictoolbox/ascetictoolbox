//
// This file was generated by the JavaTM Architecture for XML Binding(JAXB) Reference Implementation, v2.2.11 
// See <a href="http://java.sun.com/xml/jaxb">http://java.sun.com/xml/jaxb</a> 
// Any modifications to this file will be lost upon recompilation of the source schema. 
// Generated on: 2016.07.27 at 10:24:28 AM CEST 
//


package integratedtoolkit.types.project.jaxb;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for NIOAdaptorProperties complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="NIOAdaptorProperties"&gt;
 *   &lt;complexContent&gt;
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType"&gt;
 *       &lt;sequence&gt;
 *         &lt;element name="MinPort" type="{http://www.w3.org/2001/XMLSchema}int"/&gt;
 *         &lt;element name="MaxPort" type="{http://www.w3.org/2001/XMLSchema}int"/&gt;
 *       &lt;/sequence&gt;
 *     &lt;/restriction&gt;
 *   &lt;/complexContent&gt;
 * &lt;/complexType&gt;
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "NIOAdaptorProperties", propOrder = {
    "minPort",
    "maxPort"
})
public class NIOAdaptorProperties {

    @XmlElement(name = "MinPort")
    protected int minPort;
    @XmlElement(name = "MaxPort")
    protected int maxPort;

    /**
     * Gets the value of the minPort property.
     * 
     */
    public int getMinPort() {
        return minPort;
    }

    /**
     * Sets the value of the minPort property.
     * 
     */
    public void setMinPort(int value) {
        this.minPort = value;
    }

    /**
     * Gets the value of the maxPort property.
     * 
     */
    public int getMaxPort() {
        return maxPort;
    }

    /**
     * Sets the value of the maxPort property.
     * 
     */
    public void setMaxPort(int value) {
        this.maxPort = value;
    }

}
