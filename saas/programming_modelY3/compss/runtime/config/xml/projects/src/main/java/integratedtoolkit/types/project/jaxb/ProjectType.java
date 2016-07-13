//
// This file was generated by the JavaTM Architecture for XML Binding(JAXB) Reference Implementation, v2.2.11 
// See <a href="http://java.sun.com/xml/jaxb">http://java.sun.com/xml/jaxb</a> 
// Any modifications to this file will be lost upon recompilation of the source schema. 
// Generated on: 2016.07.12 at 03:07:25 PM CEST 
//


package integratedtoolkit.types.project.jaxb;

import java.util.ArrayList;
import java.util.List;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElements;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for ProjectType complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="ProjectType"&gt;
 *   &lt;complexContent&gt;
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType"&gt;
 *       &lt;choice maxOccurs="unbounded" minOccurs="2"&gt;
 *         &lt;element name="MasterNode" type="{}MasterNodeType"/&gt;
 *         &lt;element name="ComputeNode" type="{}ComputeNodeType" maxOccurs="unbounded" minOccurs="0"/&gt;
 *         &lt;element name="DataNode" type="{}DataNodeType" maxOccurs="unbounded" minOccurs="0"/&gt;
 *         &lt;element name="Service" type="{}ServiceType" maxOccurs="unbounded" minOccurs="0"/&gt;
 *         &lt;element name="Cloud" type="{}CloudType" minOccurs="0"/&gt;
 *       &lt;/choice&gt;
 *     &lt;/restriction&gt;
 *   &lt;/complexContent&gt;
 * &lt;/complexType&gt;
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "ProjectType", propOrder = {
    "masterNodeOrComputeNodeOrDataNode"
})
public class ProjectType {

    @XmlElements({
        @XmlElement(name = "MasterNode", type = MasterNodeType.class),
        @XmlElement(name = "ComputeNode", type = ComputeNodeType.class),
        @XmlElement(name = "DataNode", type = DataNodeType.class),
        @XmlElement(name = "Service", type = ServiceType.class),
        @XmlElement(name = "Cloud", type = CloudType.class)
    })
    protected List<Object> masterNodeOrComputeNodeOrDataNode;

    /**
     * Gets the value of the masterNodeOrComputeNodeOrDataNode property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the masterNodeOrComputeNodeOrDataNode property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getMasterNodeOrComputeNodeOrDataNode().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link MasterNodeType }
     * {@link ComputeNodeType }
     * {@link DataNodeType }
     * {@link ServiceType }
     * {@link CloudType }
     * 
     * 
     */
    public List<Object> getMasterNodeOrComputeNodeOrDataNode() {
        if (masterNodeOrComputeNodeOrDataNode == null) {
            masterNodeOrComputeNodeOrDataNode = new ArrayList<Object>();
        }
        return this.masterNodeOrComputeNodeOrDataNode;
    }

}
