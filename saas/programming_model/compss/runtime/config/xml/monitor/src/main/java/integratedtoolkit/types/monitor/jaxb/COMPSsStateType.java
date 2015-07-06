//
// This file was generated by the JavaTM Architecture for XML Binding(JAXB) Reference Implementation, v2.2.4 
// See <a href="http://java.sun.com/xml/jaxb">http://java.sun.com/xml/jaxb</a> 
// Any modifications to this file will be lost upon recompilation of the source schema. 
// Generated on: 2015.07.03 at 04:44:12 PM CEST 
//


package integratedtoolkit.types.monitor.jaxb;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for COMPSsStateType complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="COMPSsStateType">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="TasksInfo" type="{}TasksInfoType"/>
 *         &lt;element name="CoresInfo" type="{}CoresInfoType"/>
 *         &lt;element name="ResourceInfo" type="{}ResourceInfoType"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "COMPSsStateType", propOrder = {
    "tasksInfo",
    "coresInfo",
    "resourceInfo"
})
public class COMPSsStateType {

    @XmlElement(name = "TasksInfo", required = true)
    protected TasksInfoType tasksInfo;
    @XmlElement(name = "CoresInfo", required = true)
    protected CoresInfoType coresInfo;
    @XmlElement(name = "ResourceInfo", required = true)
    protected ResourceInfoType resourceInfo;

    /**
     * Gets the value of the tasksInfo property.
     * 
     * @return
     *     possible object is
     *     {@link TasksInfoType }
     *     
     */
    public TasksInfoType getTasksInfo() {
        return tasksInfo;
    }

    /**
     * Sets the value of the tasksInfo property.
     * 
     * @param value
     *     allowed object is
     *     {@link TasksInfoType }
     *     
     */
    public void setTasksInfo(TasksInfoType value) {
        this.tasksInfo = value;
    }

    /**
     * Gets the value of the coresInfo property.
     * 
     * @return
     *     possible object is
     *     {@link CoresInfoType }
     *     
     */
    public CoresInfoType getCoresInfo() {
        return coresInfo;
    }

    /**
     * Sets the value of the coresInfo property.
     * 
     * @param value
     *     allowed object is
     *     {@link CoresInfoType }
     *     
     */
    public void setCoresInfo(CoresInfoType value) {
        this.coresInfo = value;
    }

    /**
     * Gets the value of the resourceInfo property.
     * 
     * @return
     *     possible object is
     *     {@link ResourceInfoType }
     *     
     */
    public ResourceInfoType getResourceInfo() {
        return resourceInfo;
    }

    /**
     * Sets the value of the resourceInfo property.
     * 
     * @param value
     *     allowed object is
     *     {@link ResourceInfoType }
     *     
     */
    public void setResourceInfo(ResourceInfoType value) {
        this.resourceInfo = value;
    }

}