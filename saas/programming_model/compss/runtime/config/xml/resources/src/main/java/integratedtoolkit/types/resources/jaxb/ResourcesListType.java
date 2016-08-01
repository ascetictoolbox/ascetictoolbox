//
// This file was generated by the JavaTM Architecture for XML Binding(JAXB) Reference Implementation, v2.2.11 
// See <a href="http://java.sun.com/xml/jaxb">http://java.sun.com/xml/jaxb</a> 
// Any modifications to this file will be lost upon recompilation of the source schema. 
// Generated on: 2016.08.01 at 05:42:22 PM CEST 
//


package integratedtoolkit.types.resources.jaxb;

import java.util.ArrayList;
import java.util.List;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElements;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for ResourcesListType complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="ResourcesListType"&gt;
 *   &lt;complexContent&gt;
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType"&gt;
 *       &lt;choice maxOccurs="unbounded"&gt;
 *         &lt;element name="SharedDisk" type="{}SharedDiskType"/&gt;
 *         &lt;element name="DataNode" type="{}DataNodeType"/&gt;
 *         &lt;element name="ComputeNode" type="{}ComputeNodeType"/&gt;
 *         &lt;element name="Service" type="{}ServiceType"/&gt;
 *         &lt;element name="CloudProvider" type="{}CloudProviderType"/&gt;
 *       &lt;/choice&gt;
 *     &lt;/restriction&gt;
 *   &lt;/complexContent&gt;
 * &lt;/complexType&gt;
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "ResourcesListType", propOrder = {
    "sharedDiskOrDataNodeOrComputeNode"
})
public class ResourcesListType {

    @XmlElements({
        @XmlElement(name = "SharedDisk", type = SharedDiskType.class),
        @XmlElement(name = "DataNode", type = DataNodeType.class),
        @XmlElement(name = "ComputeNode", type = ComputeNodeType.class),
        @XmlElement(name = "Service", type = ServiceType.class),
        @XmlElement(name = "CloudProvider", type = CloudProviderType.class)
    })
    protected List<Object> sharedDiskOrDataNodeOrComputeNode;

    /**
     * Gets the value of the sharedDiskOrDataNodeOrComputeNode property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the sharedDiskOrDataNodeOrComputeNode property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getSharedDiskOrDataNodeOrComputeNode().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link SharedDiskType }
     * {@link DataNodeType }
     * {@link ComputeNodeType }
     * {@link ServiceType }
     * {@link CloudProviderType }
     * 
     * 
     */
    public List<Object> getSharedDiskOrDataNodeOrComputeNode() {
        if (sharedDiskOrDataNodeOrComputeNode == null) {
            sharedDiskOrDataNodeOrComputeNode = new ArrayList<Object>();
        }
        return this.sharedDiskOrDataNodeOrComputeNode;
    }

}
