//
// This file was generated by the JavaTM Architecture for XML Binding(JAXB) Reference Implementation, v2.2.4 
// See <a href="http://java.sun.com/xml/jaxb">http://java.sun.com/xml/jaxb</a> 
// Any modifications to this file will be lost upon recompilation of the source schema. 
// Generated on: 2014.05.06 at 06:28:09 PM CEST 
//


package integratedtoolkit.types.project.jaxb;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for workerType complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="workerType">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="InstallDir" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="WorkingDir" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="AppDir" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="LibraryPath" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="User" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="LimitOfTasks" type="{http://www.w3.org/2001/XMLSchema}int" minOccurs="0"/>
 *         &lt;element name="LimitOfJobs" type="{http://www.w3.org/2001/XMLSchema}int" minOccurs="0"/>
 *         &lt;element name="MaxClusterSize" type="{http://www.w3.org/2001/XMLSchema}int" minOccurs="0"/>
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
@XmlType(name = "workerType", propOrder = {
    "installDir",
    "workingDir",
    "appDir",
    "libraryPath",
    "user",
    "limitOfTasks",
    "limitOfJobs",
    "maxClusterSize"
})
public class WorkerType {

    @XmlElement(name = "InstallDir")
    protected String installDir;
    @XmlElement(name = "WorkingDir")
    protected String workingDir;
    @XmlElement(name = "AppDir")
    protected String appDir;
    @XmlElement(name = "LibraryPath")
    protected String libraryPath;
    @XmlElement(name = "User")
    protected String user;
    @XmlElement(name = "LimitOfTasks")
    protected Integer limitOfTasks;
    @XmlElement(name = "LimitOfJobs")
    protected Integer limitOfJobs;
    @XmlElement(name = "MaxClusterSize")
    protected Integer maxClusterSize;
    @XmlAttribute(name = "Name", required = true)
    protected String name;

    /**
     * Gets the value of the installDir property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getInstallDir() {
        return installDir;
    }

    /**
     * Sets the value of the installDir property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setInstallDir(String value) {
        this.installDir = value;
    }

    /**
     * Gets the value of the workingDir property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getWorkingDir() {
        return workingDir;
    }

    /**
     * Sets the value of the workingDir property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setWorkingDir(String value) {
        this.workingDir = value;
    }

    /**
     * Gets the value of the appDir property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getAppDir() {
        return appDir;
    }

    /**
     * Sets the value of the appDir property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setAppDir(String value) {
        this.appDir = value;
    }

    /**
     * Gets the value of the libraryPath property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getLibraryPath() {
        return libraryPath;
    }

    /**
     * Sets the value of the libraryPath property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setLibraryPath(String value) {
        this.libraryPath = value;
    }

    /**
     * Gets the value of the user property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getUser() {
        return user;
    }

    /**
     * Sets the value of the user property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setUser(String value) {
        this.user = value;
    }

    /**
     * Gets the value of the limitOfTasks property.
     * 
     * @return
     *     possible object is
     *     {@link Integer }
     *     
     */
    public Integer getLimitOfTasks() {
        return limitOfTasks;
    }

    /**
     * Sets the value of the limitOfTasks property.
     * 
     * @param value
     *     allowed object is
     *     {@link Integer }
     *     
     */
    public void setLimitOfTasks(Integer value) {
        this.limitOfTasks = value;
    }

    /**
     * Gets the value of the limitOfJobs property.
     * 
     * @return
     *     possible object is
     *     {@link Integer }
     *     
     */
    public Integer getLimitOfJobs() {
        return limitOfJobs;
    }

    /**
     * Sets the value of the limitOfJobs property.
     * 
     * @param value
     *     allowed object is
     *     {@link Integer }
     *     
     */
    public void setLimitOfJobs(Integer value) {
        this.limitOfJobs = value;
    }

    /**
     * Gets the value of the maxClusterSize property.
     * 
     * @return
     *     possible object is
     *     {@link Integer }
     *     
     */
    public Integer getMaxClusterSize() {
        return maxClusterSize;
    }

    /**
     * Sets the value of the maxClusterSize property.
     * 
     * @param value
     *     allowed object is
     *     {@link Integer }
     *     
     */
    public void setMaxClusterSize(Integer value) {
        this.maxClusterSize = value;
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
