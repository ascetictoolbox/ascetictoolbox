//
// This file was generated by the JavaTM Architecture for XML Binding(JAXB) Reference Implementation, v2.2.4 
// See <a href="http://java.sun.com/xml/jaxb">http://java.sun.com/xml/jaxb</a> 
// Any modifications to this file will be lost upon recompilation of the source schema. 
// Generated on: 2014.05.06 at 06:28:10 PM CEST 
//


package integratedtoolkit.types.resources.jaxb;

import javax.xml.bind.JAXBElement;
import javax.xml.bind.annotation.XmlElementDecl;
import javax.xml.bind.annotation.XmlRegistry;
import javax.xml.namespace.QName;


/**
 * This object contains factory methods for each 
 * Java content interface and Java element interface 
 * generated in the integratedtoolkit.types.resources.jaxb package. 
 * <p>An ObjectFactory allows you to programatically 
 * construct new instances of the Java representation 
 * for XML content. The Java representation of XML 
 * content can consist of schema derived interfaces 
 * and classes representing the binding of schema 
 * type definitions, element declarations and model 
 * groups.  Factory methods for each of these are 
 * provided in this class.
 * 
 */
@XmlRegistry
public class ObjectFactory {

    private final static QName _ResourceList_QNAME = new QName("", "ResourceList");

    /**
     * Create a new ObjectFactory that can be used to create new instances of schema derived classes for package: integratedtoolkit.types.resources.jaxb
     * 
     */
    public ObjectFactory() {
    }

    /**
     * Create an instance of {@link HostType }
     * 
     */
    public HostType createHostType() {
        return new HostType();
    }

    /**
     * Create an instance of {@link ResourceListType }
     * 
     */
    public ResourceListType createResourceListType() {
        return new ResourceListType();
    }

    /**
     * Create an instance of {@link ClusterType }
     * 
     */
    public ClusterType createClusterType() {
        return new ClusterType();
    }

    /**
     * Create an instance of {@link FileSystemType }
     * 
     */
    public FileSystemType createFileSystemType() {
        return new FileSystemType();
    }

    /**
     * Create an instance of {@link ApplicationSoftwareType }
     * 
     */
    public ApplicationSoftwareType createApplicationSoftwareType() {
        return new ApplicationSoftwareType();
    }

    /**
     * Create an instance of {@link DataNodeType }
     * 
     */
    public DataNodeType createDataNodeType() {
        return new DataNodeType();
    }

    /**
     * Create an instance of {@link ServiceCapType }
     * 
     */
    public ServiceCapType createServiceCapType() {
        return new ServiceCapType();
    }

    /**
     * Create an instance of {@link ImageListType }
     * 
     */
    public ImageListType createImageListType() {
        return new ImageListType();
    }

    /**
     * Create an instance of {@link AccessControlPolicyType }
     * 
     */
    public AccessControlPolicyType createAccessControlPolicyType() {
        return new AccessControlPolicyType();
    }

    /**
     * Create an instance of {@link CapabilitiesType }
     * 
     */
    public CapabilitiesType createCapabilitiesType() {
        return new CapabilitiesType();
    }

    /**
     * Create an instance of {@link VoType }
     * 
     */
    public VoType createVoType() {
        return new VoType();
    }

    /**
     * Create an instance of {@link MemoryType }
     * 
     */
    public MemoryType createMemoryType() {
        return new MemoryType();
    }

    /**
     * Create an instance of {@link SharedDiskListType }
     * 
     */
    public SharedDiskListType createSharedDiskListType() {
        return new SharedDiskListType();
    }

    /**
     * Create an instance of {@link OsType }
     * 
     */
    public OsType createOsType() {
        return new OsType();
    }

    /**
     * Create an instance of {@link JobPolicyType }
     * 
     */
    public JobPolicyType createJobPolicyType() {
        return new JobPolicyType();
    }

    /**
     * Create an instance of {@link ServiceType }
     * 
     */
    public ServiceType createServiceType() {
        return new ServiceType();
    }

    /**
     * Create an instance of {@link ResourceType }
     * 
     */
    public ResourceType createResourceType() {
        return new ResourceType();
    }

    /**
     * Create an instance of {@link RequirementsType }
     * 
     */
    public RequirementsType createRequirementsType() {
        return new RequirementsType();
    }

    /**
     * Create an instance of {@link StorageElementType }
     * 
     */
    public StorageElementType createStorageElementType() {
        return new StorageElementType();
    }

    /**
     * Create an instance of {@link ProcessorType }
     * 
     */
    public ProcessorType createProcessorType() {
        return new ProcessorType();
    }

    /**
     * Create an instance of {@link ImageType }
     * 
     */
    public ImageType createImageType() {
        return new ImageType();
    }

    /**
     * Create an instance of {@link NetworkAdaptorType }
     * 
     */
    public NetworkAdaptorType createNetworkAdaptorType() {
        return new NetworkAdaptorType();
    }

    /**
     * Create an instance of {@link InstanceTypesList }
     * 
     */
    public InstanceTypesList createInstanceTypesList() {
        return new InstanceTypesList();
    }

    /**
     * Create an instance of {@link CloudType }
     * 
     */
    public CloudType createCloudType() {
        return new CloudType();
    }

    /**
     * Create an instance of {@link DiskType }
     * 
     */
    public DiskType createDiskType() {
        return new DiskType();
    }

    /**
     * Create an instance of {@link HostType.TaskCount }
     * 
     */
    public HostType.TaskCount createHostTypeTaskCount() {
        return new HostType.TaskCount();
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link ResourceListType }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "", name = "ResourceList")
    public JAXBElement<ResourceListType> createResourceList(ResourceListType value) {
        return new JAXBElement<ResourceListType>(_ResourceList_QNAME, ResourceListType.class, null, value);
    }

}
