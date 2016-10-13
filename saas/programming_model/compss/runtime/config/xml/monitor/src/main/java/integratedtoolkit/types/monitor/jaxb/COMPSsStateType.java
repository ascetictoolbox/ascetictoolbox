//
// This file was generated by the JavaTM Architecture for XML Binding(JAXB) Reference Implementation, v2.2.11 
// See <a href="http://java.sun.com/xml/jaxb">http://java.sun.com/xml/jaxb</a> 
// Any modifications to this file will be lost upon recompilation of the source schema. 
// Generated on: 2016.10.13 at 05:57:47 PM CDT 
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
 * &lt;complexType name="COMPSsStateType"&gt;
 *   &lt;complexContent&gt;
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType"&gt;
 *       &lt;sequence&gt;
 *         &lt;element name="TasksInfo" type="{}TasksInfoType"/&gt;
 *         &lt;element name="CoresInfo" type="{}CoresInfoType"/&gt;
 *         &lt;element name="ResourceInfo" type="{}ResourceInfoType"/&gt;
 *         &lt;element name="AccumulatedCost" type="{http://www.w3.org/2001/XMLSchema}float"/&gt;
 *       &lt;/sequence&gt;
 *     &lt;/restriction&gt;
 *   &lt;/complexContent&gt;
 * &lt;/complexType&gt;
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "COMPSsStateType", propOrder = {
    "tasksInfo",
    "coresInfo",
    "resourceInfo",
    "accumulatedCost"
})
public class COMPSsStateType {

    @XmlElement(name = "TasksInfo", required = true)
    protected TasksInfoType tasksInfo;
    @XmlElement(name = "CoresInfo", required = true)
    protected CoresInfoType coresInfo;
    @XmlElement(name = "ResourceInfo", required = true)
    protected ResourceInfoType resourceInfo;
    @XmlElement(name = "AccumulatedCost")
    protected float accumulatedCost;

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

    /**
     * Gets the value of the accumulatedCost property.
     * 
     */
    public float getAccumulatedCost() {
        return accumulatedCost;
    }

    /**
     * Sets the value of the accumulatedCost property.
     * 
     */
    public void setAccumulatedCost(float value) {
        this.accumulatedCost = value;
    }

}
