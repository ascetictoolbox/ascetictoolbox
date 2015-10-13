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

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for capabilitiesType complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="capabilitiesType">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="Host" type="{}hostType" minOccurs="0"/>
 *         &lt;element name="Processor" type="{}processorType"/>
 *         &lt;element name="OS" type="{}osType" minOccurs="0"/>
 *         &lt;element name="StorageElement" type="{}storageElementType"/>
 *         &lt;element name="Memory" type="{}memoryType"/>
 *         &lt;element name="ApplicationSoftware" type="{}applicationSoftwareType" minOccurs="0"/>
 *         &lt;element name="Service" type="{}serviceCapType" minOccurs="0"/>
 *         &lt;element name="VO" type="{}voType" minOccurs="0"/>
 *         &lt;element name="Cluster" type="{}clusterType" minOccurs="0"/>
 *         &lt;element name="FileSystem" type="{}fileSystemType" minOccurs="0"/>
 *         &lt;element name="NetworkAdaptor" type="{}networkAdaptorType" minOccurs="0"/>
 *         &lt;element name="JobPolicy" type="{}jobPolicyType" minOccurs="0"/>
 *         &lt;element name="AccessControlPolicy" type="{}accessControlPolicyType" minOccurs="0"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "capabilitiesType", propOrder = {
    "host",
    "processor",
    "os",
    "storageElement",
    "memory",
    "applicationSoftware",
    "service",
    "vo",
    "cluster",
    "fileSystem",
    "networkAdaptor",
    "jobPolicy",
    "accessControlPolicy"
})
public class CapabilitiesType {

    @XmlElement(name = "Host")
    protected HostType host;
    @XmlElement(name = "Processor", required = true)
    protected ProcessorType processor;
    @XmlElement(name = "OS")
    protected OsType os;
    @XmlElement(name = "StorageElement", required = true)
    protected StorageElementType storageElement;
    @XmlElement(name = "Memory", required = true)
    protected MemoryType memory;
    @XmlElement(name = "ApplicationSoftware")
    protected ApplicationSoftwareType applicationSoftware;
    @XmlElement(name = "Service")
    protected ServiceCapType service;
    @XmlElement(name = "VO")
    protected VoType vo;
    @XmlElement(name = "Cluster")
    protected ClusterType cluster;
    @XmlElement(name = "FileSystem")
    protected FileSystemType fileSystem;
    @XmlElement(name = "NetworkAdaptor")
    protected NetworkAdaptorType networkAdaptor;
    @XmlElement(name = "JobPolicy")
    protected JobPolicyType jobPolicy;
    @XmlElement(name = "AccessControlPolicy")
    protected AccessControlPolicyType accessControlPolicy;

    /**
     * Gets the value of the host property.
     * 
     * @return
     *     possible object is
     *     {@link HostType }
     *     
     */
    public HostType getHost() {
        return host;
    }

    /**
     * Sets the value of the host property.
     * 
     * @param value
     *     allowed object is
     *     {@link HostType }
     *     
     */
    public void setHost(HostType value) {
        this.host = value;
    }

    /**
     * Gets the value of the processor property.
     * 
     * @return
     *     possible object is
     *     {@link ProcessorType }
     *     
     */
    public ProcessorType getProcessor() {
        return processor;
    }

    /**
     * Sets the value of the processor property.
     * 
     * @param value
     *     allowed object is
     *     {@link ProcessorType }
     *     
     */
    public void setProcessor(ProcessorType value) {
        this.processor = value;
    }

    /**
     * Gets the value of the os property.
     * 
     * @return
     *     possible object is
     *     {@link OsType }
     *     
     */
    public OsType getOS() {
        return os;
    }

    /**
     * Sets the value of the os property.
     * 
     * @param value
     *     allowed object is
     *     {@link OsType }
     *     
     */
    public void setOS(OsType value) {
        this.os = value;
    }

    /**
     * Gets the value of the storageElement property.
     * 
     * @return
     *     possible object is
     *     {@link StorageElementType }
     *     
     */
    public StorageElementType getStorageElement() {
        return storageElement;
    }

    /**
     * Sets the value of the storageElement property.
     * 
     * @param value
     *     allowed object is
     *     {@link StorageElementType }
     *     
     */
    public void setStorageElement(StorageElementType value) {
        this.storageElement = value;
    }

    /**
     * Gets the value of the memory property.
     * 
     * @return
     *     possible object is
     *     {@link MemoryType }
     *     
     */
    public MemoryType getMemory() {
        return memory;
    }

    /**
     * Sets the value of the memory property.
     * 
     * @param value
     *     allowed object is
     *     {@link MemoryType }
     *     
     */
    public void setMemory(MemoryType value) {
        this.memory = value;
    }

    /**
     * Gets the value of the applicationSoftware property.
     * 
     * @return
     *     possible object is
     *     {@link ApplicationSoftwareType }
     *     
     */
    public ApplicationSoftwareType getApplicationSoftware() {
        return applicationSoftware;
    }

    /**
     * Sets the value of the applicationSoftware property.
     * 
     * @param value
     *     allowed object is
     *     {@link ApplicationSoftwareType }
     *     
     */
    public void setApplicationSoftware(ApplicationSoftwareType value) {
        this.applicationSoftware = value;
    }

    /**
     * Gets the value of the service property.
     * 
     * @return
     *     possible object is
     *     {@link ServiceCapType }
     *     
     */
    public ServiceCapType getService() {
        return service;
    }

    /**
     * Sets the value of the service property.
     * 
     * @param value
     *     allowed object is
     *     {@link ServiceCapType }
     *     
     */
    public void setService(ServiceCapType value) {
        this.service = value;
    }

    /**
     * Gets the value of the vo property.
     * 
     * @return
     *     possible object is
     *     {@link VoType }
     *     
     */
    public VoType getVO() {
        return vo;
    }

    /**
     * Sets the value of the vo property.
     * 
     * @param value
     *     allowed object is
     *     {@link VoType }
     *     
     */
    public void setVO(VoType value) {
        this.vo = value;
    }

    /**
     * Gets the value of the cluster property.
     * 
     * @return
     *     possible object is
     *     {@link ClusterType }
     *     
     */
    public ClusterType getCluster() {
        return cluster;
    }

    /**
     * Sets the value of the cluster property.
     * 
     * @param value
     *     allowed object is
     *     {@link ClusterType }
     *     
     */
    public void setCluster(ClusterType value) {
        this.cluster = value;
    }

    /**
     * Gets the value of the fileSystem property.
     * 
     * @return
     *     possible object is
     *     {@link FileSystemType }
     *     
     */
    public FileSystemType getFileSystem() {
        return fileSystem;
    }

    /**
     * Sets the value of the fileSystem property.
     * 
     * @param value
     *     allowed object is
     *     {@link FileSystemType }
     *     
     */
    public void setFileSystem(FileSystemType value) {
        this.fileSystem = value;
    }

    /**
     * Gets the value of the networkAdaptor property.
     * 
     * @return
     *     possible object is
     *     {@link NetworkAdaptorType }
     *     
     */
    public NetworkAdaptorType getNetworkAdaptor() {
        return networkAdaptor;
    }

    /**
     * Sets the value of the networkAdaptor property.
     * 
     * @param value
     *     allowed object is
     *     {@link NetworkAdaptorType }
     *     
     */
    public void setNetworkAdaptor(NetworkAdaptorType value) {
        this.networkAdaptor = value;
    }

    /**
     * Gets the value of the jobPolicy property.
     * 
     * @return
     *     possible object is
     *     {@link JobPolicyType }
     *     
     */
    public JobPolicyType getJobPolicy() {
        return jobPolicy;
    }

    /**
     * Sets the value of the jobPolicy property.
     * 
     * @param value
     *     allowed object is
     *     {@link JobPolicyType }
     *     
     */
    public void setJobPolicy(JobPolicyType value) {
        this.jobPolicy = value;
    }

    /**
     * Gets the value of the accessControlPolicy property.
     * 
     * @return
     *     possible object is
     *     {@link AccessControlPolicyType }
     *     
     */
    public AccessControlPolicyType getAccessControlPolicy() {
        return accessControlPolicy;
    }

    /**
     * Sets the value of the accessControlPolicy property.
     * 
     * @param value
     *     allowed object is
     *     {@link AccessControlPolicyType }
     *     
     */
    public void setAccessControlPolicy(AccessControlPolicyType value) {
        this.accessControlPolicy = value;
    }

}
