/**
 *
 *   Copyright 2014-2015 Barcelona Supercomputing Center (www.bsc.es) All rights reserved.
 *
 *   Licensed under the Apache License, Version 2.0 (the "License");
 *   you may not use this file except in compliance with the License.
 *   You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *   Unless required by applicable law or agreed to in writing, software
 *   distributed under the License is distributed on an "AS IS" BASIS,
 *   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *   See the License for the specific language governing permissions and
 *   limitations under the License.
 */

//
// This file was generated by the JavaTM Architecture for XML Binding(JAXB) Reference Implementation, v2.2.4 
// See <a href="http://java.sun.com/xml/jaxb">http://java.sun.com/xml/jaxb</a> 
// Any modifications to this file will be lost upon recompilation of the source schema. 
// Generated on: 2015.10.13 at 02:42:04 PM CEST 
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
 * <p>Java class for resourceListType complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="resourceListType">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;choice maxOccurs="unbounded" minOccurs="0">
 *           &lt;element name="Disk" type="{}DiskType"/>
 *           &lt;element name="DataNode" type="{}DataNodeType"/>
 *           &lt;element name="Resource" type="{}resourceType"/>
 *           &lt;element name="Service" type="{}serviceType"/>
 *           &lt;element name="CloudProvider" type="{}CloudType"/>
 *         &lt;/choice>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "resourceListType", propOrder = {
    "diskOrDataNodeOrResource"
})
public class ResourceListType {

    @XmlElements({
        @XmlElement(name = "Disk", type = DiskType.class),
        @XmlElement(name = "DataNode", type = DataNodeType.class),
        @XmlElement(name = "Resource", type = ResourceType.class),
        @XmlElement(name = "Service", type = ServiceType.class),
        @XmlElement(name = "CloudProvider", type = CloudType.class)
    })
    protected List<Object> diskOrDataNodeOrResource;

    /**
     * Gets the value of the diskOrDataNodeOrResource property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the diskOrDataNodeOrResource property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getDiskOrDataNodeOrResource().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link DiskType }
     * {@link DataNodeType }
     * {@link ResourceType }
     * {@link ServiceType }
     * {@link CloudType }
     * 
     * 
     */
    public List<Object> getDiskOrDataNodeOrResource() {
        if (diskOrDataNodeOrResource == null) {
            diskOrDataNodeOrResource = new ArrayList<Object>();
        }
        return this.diskOrDataNodeOrResource;
    }

}
