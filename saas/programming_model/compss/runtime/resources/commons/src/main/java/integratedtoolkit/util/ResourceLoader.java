package integratedtoolkit.util;

import java.io.File;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

import javax.xml.bind.JAXBException;

import integratedtoolkit.ITConstants;
import integratedtoolkit.comm.Comm;
import integratedtoolkit.exceptions.NoResourceAvailableException;
import integratedtoolkit.log.Loggers;
import integratedtoolkit.types.CloudImageDescription;
import integratedtoolkit.types.Implementation;
import integratedtoolkit.types.project.ProjectFile;
import integratedtoolkit.types.project.exceptions.ProjectFileValidationException;
import integratedtoolkit.types.project.jaxb.*;
import integratedtoolkit.types.resources.DataResourceDescription;
import integratedtoolkit.types.resources.MethodResourceDescription;
import integratedtoolkit.types.resources.ResourcesFile;
import integratedtoolkit.types.resources.ServiceResourceDescription;
import integratedtoolkit.types.resources.configuration.MethodConfiguration;
import integratedtoolkit.types.resources.configuration.ServiceConfiguration;
import integratedtoolkit.types.resources.description.CloudMethodResourceDescription;
import integratedtoolkit.types.resources.exceptions.ResourcesFileValidationException;
import integratedtoolkit.types.resources.jaxb.EndpointType;
import integratedtoolkit.types.resources.jaxb.ProcessorPropertyType;

import org.apache.log4j.Logger;
import org.xml.sax.SAXException;

public class ResourceLoader {

    // Resources XML and XSD
    private static String resources_XML;
    private static String resources_XSD;

    // Project XML and XSD
    private static String project_XML;
    private static String project_XSD;

    // File instances (for cross validation)
    private static ResourcesFile resources;
    private static ProjectFile project;

    // Logger
    private static final Logger LOGGER = Logger.getLogger(Loggers.RM_COMP);

    public static void load(String resources_XML, String resources_XSD,
            String project_XML, String project_XSD) throws ResourcesFileValidationException, ProjectFileValidationException, NoResourceAvailableException {

        init(resources_XML, resources_XSD, project_XML, project_XSD);
        loadFiles();
        loadRuntime();
    }

    private static void init(String resources_XML, String resources_XSD, String project_XML, String project_XSD) {
        LOGGER.info("ResourceLoader init");
        ResourceLoader.resources_XML = resources_XML;
        ResourceLoader.resources_XSD = resources_XSD;
        ResourceLoader.project_XML = project_XML;
        ResourceLoader.project_XSD = project_XSD;
    }

    private static void loadFiles() throws ResourcesFileValidationException, ProjectFileValidationException {
        LOGGER.info("ResourceLoader loading files");

        // Load resources
        try {
            LOGGER.debug("ResourceLoader loading resources.xml");
            ResourceLoader.resources = new ResourcesFile(new File(resources_XML), resources_XSD, LOGGER);
        } catch (SAXException | JAXBException e) {
            throw new ResourcesFileValidationException(e);
        }

        // Load project
        try {
            LOGGER.debug("ResourceLoader loading project.xml");
            ResourceLoader.project = new ProjectFile(new File(project_XML), project_XSD, LOGGER);
        } catch (SAXException | JAXBException e) {
            throw new ProjectFileValidationException(e);
        }
    }

    private static void loadRuntime() throws NoResourceAvailableException {
        LOGGER.info("ResourceLoader loading Runtime");

        /* ***************************************************************
         * Resources and project have been loaded and validated
         * We need to cross validate them and load the information in the runtime
         * 
         * WARNING: When the JAXB class is set by package information, it refers to the PROJECT.XML,
         *          when the JAXB class is fully qualified name, it refers to the RESOURCES.XML
         */
        // Booleans to control initializated structures
        boolean computeNodeExist = false;
        boolean serviceExist = false;
        boolean cloudProviderExist = false;

        // Load master node information
        MasterNodeType master = ResourceLoader.project.getMasterNode();
        loadMaster(master);

        // Load ComputeNodes 
        List<ComputeNodeType> computeNodes = ResourceLoader.project.getComputeNodes_list();
        if (computeNodes != null) {
            for (ComputeNodeType cn_project : computeNodes) {
                integratedtoolkit.types.resources.jaxb.ComputeNodeType cn_resources = ResourceLoader.resources.getComputeNode(cn_project.getName());
                if (cn_resources != null) {
                    boolean exist = loadComputeNode(cn_project, cn_resources);
                    computeNodeExist = (computeNodeExist || exist);
                } else {
                    ErrorManager.warn("ComputeNode " + cn_project.getName() + " not defined in the resources file");
                }
            }
        }

        // Load Services
        List<ServiceType> services = ResourceLoader.project.getServices_list();
        if (services != null) {
            for (ServiceType s_project : services) {
                integratedtoolkit.types.resources.jaxb.ServiceType s_resources = ResourceLoader.resources.getService(s_project.getWsdl());
                if (s_resources != null) {
                    boolean exist = loadService(s_project, s_resources);
                    serviceExist = (serviceExist || exist);
                } else {
                    ErrorManager.warn("Service " + s_project.getWsdl() + " not defined in the resources file");
                }
            }
        }

        // Load DataNodes
        List<DataNodeType> dataNodes = ResourceLoader.project.getDataNodes_list();
        if (dataNodes != null) {
            for (DataNodeType dn_project : dataNodes) {
                integratedtoolkit.types.resources.jaxb.DataNodeType dn_resources = ResourceLoader.resources.getDataNode(dn_project.getName());
                if (dn_resources != null) {
                    loadDataNode(dn_project, dn_resources);
                } else {
                    ErrorManager.warn("DataNode " + dn_project.getName() + " not defined in the resources file");
                }
            }
        }

        // Load Cloud
        CloudType cloud = ResourceLoader.project.getCloud();
        if (cloud != null) {
            cloudProviderExist = loadCloud(cloud);
        }

        // Availability checker
        if (!computeNodeExist && !serviceExist && !cloudProviderExist) {
            throw new NoResourceAvailableException();
        }
    }

    private static void loadMaster(MasterNodeType master) {
        HashMap<String, String> sharedDisks = new HashMap<String, String>();
        List<Object> masterInformation = master.getSharedDisksOrPrice();
        if (masterInformation != null) {
            for (Object obj : masterInformation) {
                if (obj instanceof AttachedDisksListType) {
                    AttachedDisksListType disks = (AttachedDisksListType) obj;
                    if (disks != null) {
                        List<AttachedDiskType> disksList = disks.getAttachedDisk();
                        if (disksList != null) {
                            for (AttachedDiskType disk : disksList) {
                                integratedtoolkit.types.resources.jaxb.SharedDiskType disk_resources = ResourceLoader.resources.getSharedDisk(disk.getName());
                                if (disk_resources != null) {
                                    // TODO: Check the disk information against the resources file (size and type)
                                    String diskName = disk.getName();
                                    String diskMountPoint = disk.getMountPoint();
                                    sharedDisks.put(diskName, diskMountPoint);
                                } else {
                                    ErrorManager.warn("SharedDisk " + disk.getName() + " defined in the master node is not defined in the resources.xml. Skipping");
                                }
                            }
                        }
                    }
                } else if (obj instanceof PriceType) {
                    // TODO: Consider the price for the master node
                } else {
                    // The XML is validated, we should never execute this part of code
                    LOGGER.warn("MasterNode has an unrecognized parameter: " + obj.getClass());
                }
            }
        }
        ResourceManager.updateMasterConfiguration(sharedDisks);
    }

    private static boolean loadComputeNode(ComputeNodeType cn_project,
            integratedtoolkit.types.resources.jaxb.ComputeNodeType cn_resources) {

        // Add the name
        String name = cn_project.getName();

        /* Add properties given by the resources file *****************************************/
        MethodResourceDescription mrd = new MethodResourceDescription();
        List<integratedtoolkit.types.resources.jaxb.ProcessorType> processors = resources.getProcessors(cn_resources);
        if (processors != null) {
            for (integratedtoolkit.types.resources.jaxb.ProcessorType p : processors) {
                String procName = p.getName();
                int computingUnits = resources.getProcessorComputingUnits(p);
                String architecture = resources.getProcessorArchitecture(p);
                float speed = resources.getProcessorSpeed(p);
                ProcessorPropertyType procProp = resources.getProcessorProperty(p);
                String propKey = (procProp != null) ? procProp.getKey() : "";
                String propValue = (procProp != null) ? procProp.getValue() : "";
                mrd.addProcessor(procName, computingUnits, architecture, speed, propKey, propValue);
            }
        }
        mrd.setMemorySize(resources.getMemorySize(cn_resources));
        mrd.setMemoryType(resources.getMemoryType(cn_resources));
        mrd.setStorageSize(resources.getStorageSize(cn_resources));
        mrd.setStorageType(resources.getStorageType(cn_resources));
        mrd.setOperatingSystemType(resources.getOperatingSystemType(cn_resources));
        mrd.setOperatingSystemDistribution(resources.getOperatingSystemDistribution(cn_resources));
        mrd.setOperatingSystemVersion(resources.getOperatingSystemVersion(cn_resources));
        List<String> apps = resources.getApplications(cn_resources);
        if (apps != null) {
            for (String appName : apps) {
                mrd.addApplication(appName);
            }
        }
        integratedtoolkit.types.resources.jaxb.PriceType p = resources.getPrice(cn_resources);
        if (p != null) {
            mrd.setPriceTimeUnit(p.getTimeUnit());
            mrd.setPricePerUnit(p.getPricePerUnit());
        }

        // Add Shared Disks (Name, mountpoint)
        HashMap<String, String> sharedDisks = resources.getSharedDisks(cn_resources);
        if (sharedDisks != null) {
            List<String> declaredSharedDisks = resources.getSharedDisks_names();
            for (String diskName : sharedDisks.keySet()) {
                if (declaredSharedDisks == null || !declaredSharedDisks.contains(diskName)) {
                    ErrorManager.warn("SharedDisk " + diskName + " defined in the ComputeNode " + name + " is not defined in the resources.xml. Skipping");
                    sharedDisks.remove(diskName);
                    // TODO: Check the disk information (size and type)
                }
            }
        }

        // Add the adaptors properties (queue types and adaptor properties)
        // TODO Support multiple adaptor properties
        String loadedAdaptor = System.getProperty(ITConstants.COMM_ADAPTOR);
        List<String> queues_project = project.getAdaptorQueues(cn_project, loadedAdaptor);
        List<String> queues_resources = resources.getAdaptorQueues(cn_resources, loadedAdaptor);
        if (queues_project == null) {
            // Has no tag adaptors on project, get default resources complete
            for (String queue : queues_resources) {
                mrd.addHostQueue(queue);
            }
        } else {
            // Project defines a subset of queues
            for (String queue : queues_resources) {
                if (queues_project.contains(queue)) {
                    mrd.addHostQueue(queue);
                }
            }
        }
        Object adaptorProperties_project = project.getAdaptorProperties(cn_project, loadedAdaptor);
        Object adaptorProperties_resources = resources.getAdaptorProperties(cn_resources, loadedAdaptor);

        MethodConfiguration config = null;
        try {
            config = (MethodConfiguration) Comm.constructConfiguration(loadedAdaptor, adaptorProperties_project, adaptorProperties_resources);
        } catch (Exception e) {
            ErrorManager.warn("Adaptor configuration constructor failed", e);
            return false;
        }

        // If we have reached this point the config is SURELY not null

        /* Add properties given by the project file *****************************************/
        config.setHost(cn_project.getName());
        config.setUser(project.getUser(cn_project));
        config.setInstallDir(project.getInstallDir(cn_project));
        config.setWorkingDir(project.getWorkingDir(cn_project));
        int limitOfTasks = project.getLimitOfTasks(cn_project);
        if (limitOfTasks >= 0) {
            config.setLimitOfTasks(limitOfTasks);
        } else {
            config.setLimitOfTasks(mrd.getTotalComputingUnits());
        }
        config.setTotalComputingUnits(mrd.getTotalComputingUnits());

        ApplicationType app = project.getApplication(cn_project);
        if (app != null) {
            config.setAppDir(app.getAppDir());
            config.setLibraryPath(app.getLibraryPath());
            config.setClasspath(app.getClasspath());
            config.setPythonpath(app.getPythonpath());
        }

        /* Pass all the information to the ResourceManager to insert it into the Runtime ***/
        LOGGER.debug("Adding method worker " + name);
        ResourceManager.newMethodWorker(name, mrd, sharedDisks, config, new LinkedList<Implementation>());

        // If we have reached this point the method worker has been correctly created
        return true;
    }

    private static boolean loadService(ServiceType s_project,
            integratedtoolkit.types.resources.jaxb.ServiceType s_resources) {

        // Get service adaptor name from properties
        String serviceAdaptorName = ITConstants.SERVICE_ADAPTOR;

        // Add the name
        String wsdl = s_project.getWsdl();

        /* Add properties given by the resources file *****************************************/
        String serviceName = s_resources.getName();
        String namespace = s_resources.getNamespace();
        String port = s_resources.getPort();
        ServiceResourceDescription srd = new ServiceResourceDescription(serviceName, namespace, port, Integer.MAX_VALUE);

        ServiceConfiguration config = null;
        try {
            config = (ServiceConfiguration) Comm.constructConfiguration(serviceAdaptorName, s_project, s_resources);
        } catch (Exception e) {
            ErrorManager.warn("Service configuration constructor failed", e);
            return false;
        }

        // If we have reached this point the mp is SURELY not null

        /* Add properties given by the project file *****************************************/
        int limitOfTasks = s_project.getLimitOfTasks();
        if (limitOfTasks >= 0) {
            config.setLimitOfTasks(limitOfTasks);
        } else {
            config.setLimitOfTasks(Integer.MAX_VALUE);
        }

        /* Pass all the information to the ResourceManager to insert it into the Runtime ***/
        LOGGER.debug("Adding service worker " + wsdl);
        ResourceManager.newServiceWorker(wsdl, srd, config, new LinkedList<Implementation>());
        return true;
    }

    private static boolean loadCloud(CloudType cloud) {
        int initialVMs = project.getInitialVMs(cloud);
        int minVMs = project.getMinVMs(cloud);
        int maxVMs = project.getMaxVMs(cloud);

        // Make sure maxVMs is bigger than minVMs
        if (maxVMs != -1 && minVMs != -1 && maxVMs < minVMs) {
            ErrorManager.warn("Cloud: MaxVMs (" + maxVMs + ") is lower than MinVMs (" + minVMs + "). Setting MaxVMs value to MinVMs");
            maxVMs = minVMs;
        }
        // Make initialVMs between min and max
        if (initialVMs < minVMs) {
            initialVMs = minVMs;
        }
        if (initialVMs > maxVMs) {
            initialVMs = maxVMs;
        }

        // Set parameters to CloudManager taking into account that if they are not defined we load default values
        if (initialVMs > 0) {
            CloudManager.setInitialVMs(initialVMs);
        }
        if (minVMs > 0) {
            CloudManager.setMinVMs(minVMs);
        }
        if (maxVMs > 0) {
            CloudManager.setMaxVMs(maxVMs);
        }

        // Load cloud providers
        boolean cloudEnabled = false;
        List<String> cp_resources = resources.getCloudProviders_names();
        for (CloudProviderType cp : project.getCloudProviders_list()) {
            if (cp_resources.contains(cp.getName())) {
                // Resources contains information, loading cp
                boolean isEnabled = loadCloudProvider(cp, resources.getCloudProvider(cp.getName()));
                cloudEnabled = (cloudEnabled || isEnabled);
            } else {
                ErrorManager.warn("CloudProvider " + cp.getName() + " not defined in resources.xml file. Skipping");
            }
        }

        CloudManager.setUseCloud(cloudEnabled);
        return cloudEnabled;
    }

    private static boolean loadCloudProvider(CloudProviderType cp_project,
            integratedtoolkit.types.resources.jaxb.CloudProviderType cp_resources) {

        String cpName = cp_project.getName();
        String connectorName = "";
        HashMap<String, String> properties = new HashMap<String, String>();

        /* Add Endpoint information from resources.xml */
        EndpointType endpoint = cp_resources.getEndpoint();
        connectorName = resources.getConnector(endpoint);
        properties.put("Server", resources.getServer(endpoint));
        properties.put("Port", resources.getPort(endpoint));

        /* Add properties information *******************/
        List<Object> objList = cp_project.getImagesOrInstanceTypesOrLimitOfVMs();
        if (objList != null) {
            for (Object obj : objList) {
                if (obj instanceof CloudPropertiesType) {
                    CloudPropertiesType cloud_props = (CloudPropertiesType) obj;
                    for (CloudPropertyType prop : cloud_props.getProperty()) {
                        // TODO CloudProperties have context, name, value. Consider context (it is ignored now)
                        properties.put(prop.getName(), prop.getValue());
                    }
                }
            }
        }

        /* Add images/instances information *********************/
        int limitOfVMs = -1;
        int maxCreationTime = -1;	// Seconds
        LinkedList<CloudImageDescription> images = new LinkedList<CloudImageDescription>();
        LinkedList<CloudMethodResourceDescription> instanceTypes = new LinkedList<CloudMethodResourceDescription>();
        objList = cp_project.getImagesOrInstanceTypesOrLimitOfVMs();
        if (objList != null) {
            for (Object obj : objList) {
                if (obj instanceof ImagesType) {
                    // Load images
                    ImagesType imageList = (ImagesType) obj;
                    for (ImageType im_project : imageList.getImage()) {
                        // Try to create image
                        integratedtoolkit.types.resources.jaxb.ImageType im_resources = resources.getImage(cp_resources, im_project.getName());
                        CloudImageDescription cid = createImage(cpName, im_project, im_resources, properties);

                        // Add to images list
                        if (cid != null) {
                            images.add(cid);
                            // Update max creation time
                            int localCT = cid.getCreationTime();
                            if (localCT > maxCreationTime) {
                                maxCreationTime = localCT;
                            }
                        }
                    }
                } else if (obj instanceof InstanceTypesType) {
                    // Load images
                    InstanceTypesType instancesList = (InstanceTypesType) obj;
                    for (InstanceTypeType instance_project : instancesList.getInstanceType()) {
                        // Try to create instance
                        String instanceName = instance_project.getName();
                        integratedtoolkit.types.resources.jaxb.InstanceTypeType instance_resources = resources.getInstance(cp_resources, instanceName);
                        if (instance_resources != null) {
                            CloudMethodResourceDescription cmrd = createInstance(cpName, instance_resources);

                            // Add to instance list
                            if (cmrd != null) {
                                instanceTypes.add(cmrd);
                            }
                        } else {
                            ErrorManager.warn("Instance " + instanceName + " not defined in resources.xml. Skipping");
                        }

                    }
                } else if (obj instanceof Integer) { // Limit Of VMs
                    limitOfVMs = (Integer) obj;
                }
            }
        }

        // Add Cloud Provider to CloudManager *****************************************/
        try {
            CloudManager.newCloudProvider(cpName, limitOfVMs, connectorName, properties);
            for (CloudImageDescription cid : images) {
                CloudManager.addImageToProvider(cpName, cid);
            }
            for (CloudMethodResourceDescription instance : instanceTypes) {
                CloudManager.addInstanceTypeToProvider(cpName, instance);
            }
        } catch (Exception e) {
            ErrorManager.warn("Exception loading CloudProvider " + cpName, e);
            return false;
        }

        /* If we reach this point CP is loaded *****************************************/
        return true;
    }

    private static CloudImageDescription createImage(String cp_name, ImageType im_project, integratedtoolkit.types.resources.jaxb.ImageType im_resources,
            HashMap<String, String> properties) {

        String imageName = im_project.getName();
        CloudImageDescription cid = new CloudImageDescription(cp_name, imageName, properties);

        /* Add properties given by the resources file *****************************************/
        cid.setOperatingSystemType(resources.getOperatingSystemType(im_resources));
        cid.setOperatingSystemDistribution(resources.getOperatingSystemDistribution(im_resources));
        cid.setOperatingSystemVersion(resources.getOperatingSystemVersion(im_resources));

        List<String> apps = resources.getApplications(im_resources);
        if (apps != null) {
            for (String appName : apps) {
                cid.addApplication(appName);
            }
        }

        integratedtoolkit.types.resources.jaxb.PriceType p = resources.getPrice(im_resources);
        if (p != null) {
            cid.setPriceTimeUnit(p.getTimeUnit());
            cid.setPricePerUnit(p.getPricePerUnit());
        }

        cid.setCreationTime(resources.getCreationTime(im_resources));

        // Add Shared Disks (Name, mountpoint)
        HashMap<String, String> sharedDisks = resources.getSharedDisks(im_resources);
        if (sharedDisks != null) {
            List<String> declaredSharedDisks = resources.getSharedDisks_names();
            for (String diskName : sharedDisks.keySet()) {
                if (declaredSharedDisks == null || !declaredSharedDisks.contains(diskName)) {
                    ErrorManager.warn("SharedDisk " + diskName + " defined in the Image " + imageName + " is not defined in the resources.xml. Skipping");
                    sharedDisks.remove(diskName);
                    // TODO: Check the disk information (size and type)
                }
            }
        }
        cid.setSharedDisks(sharedDisks);

        // Add the adaptors properties (queue types and adaptor properties)
        // TODO Support multiple adaptor properties
        String loadedAdaptor = System.getProperty(ITConstants.COMM_ADAPTOR);
        List<String> queues_project = project.getAdaptorQueues(im_project, loadedAdaptor);
        List<String> queues_resources = resources.getAdaptorQueues(im_resources, loadedAdaptor);
        for (String queue : queues_resources) {
            if (queues_project.contains(queue)) {
                cid.addQueue(queue);
            }
        }
        Object adaptorProperties_project = project.getAdaptorProperties(im_project, loadedAdaptor);
        Object adaptorProperties_resources = resources.getAdaptorProperties(im_resources, loadedAdaptor);

        MethodConfiguration config = null;
        try {
            config = (MethodConfiguration) Comm.constructConfiguration(loadedAdaptor, adaptorProperties_project, adaptorProperties_resources);
        } catch (Exception e) {
            ErrorManager.warn("Adaptor configuration constructor failed", e);
            return null;
        }

        // If we have reached this point the mp is SURELY not null
        /* Add properties given by the project file *****************************************/
        config.setHost(im_project.getName());
        config.setUser(project.getUser(im_project));
        config.setInstallDir(project.getInstallDir(im_project));
        config.setWorkingDir(project.getWorkingDir(im_project));
        config.setLimitOfTasks(project.getLimitOfTasks(im_project));
        ApplicationType app = project.getApplication(im_project);
        if (app != null) {
            config.setAppDir(app.getAppDir());
            config.setLibraryPath(app.getLibraryPath());
            config.setClasspath(app.getClasspath());
            config.setPythonpath(app.getClasspath());
        }
        List<PackageType> packages = project.getPackages(im_project);
        for (PackageType pack : packages) {
            cid.addPackage(pack.getSource(), pack.getTarget());

            SoftwareListType software = pack.getIncludedSoftware();
            if (software != null) {
                cid.addAllApplications(software.getApplication());
            }
        }

        // Add configuration to image
        cid.setConfig(config);

        return cid;
    }

    private static CloudMethodResourceDescription createInstance(String cpName, integratedtoolkit.types.resources.jaxb.InstanceTypeType instance) {
        // Add the name
        String name = instance.getName();
        String type = instance.getName();
        CloudMethodResourceDescription cmrd = new CloudMethodResourceDescription(cpName, name, type);

        List<integratedtoolkit.types.resources.jaxb.ProcessorType> processors = resources.getProcessors(instance);
        if (processors != null) {
            for (integratedtoolkit.types.resources.jaxb.ProcessorType p : processors) {
                String procName = p.getName();
                int computingUnits = resources.getProcessorComputingUnits(p);
                String architecture = resources.getProcessorArchitecture(p);
                float speed = resources.getProcessorSpeed(p);
                ProcessorPropertyType procProp = resources.getProcessorProperty(p);
                String propKey = (procProp != null) ? procProp.getKey() : "";
                String propValue = (procProp != null) ? procProp.getValue() : "";
                cmrd.addProcessor(procName, computingUnits, architecture, speed, propKey, propValue);
            }
        }

        cmrd.setMemorySize(resources.getMemorySize(instance));
        cmrd.setMemoryType(resources.getMemoryType(instance));

        cmrd.setStorageSize(resources.getStorageSize(instance));
        cmrd.setStorageType(resources.getStorageType(instance));

        integratedtoolkit.types.resources.jaxb.PriceType p = resources.getPrice(instance);
        if (p != null) {
            cmrd.setPriceTimeUnit(p.getTimeUnit());
            cmrd.setPricePerUnit(p.getPricePerUnit());
        }

        return cmrd;
    }

    private static void loadDataNode(DataNodeType dn_project,
            integratedtoolkit.types.resources.jaxb.DataNodeType dn_resources) {

        // Add the name
        String name = dn_project.getName();
        String host = resources.getHost(dn_resources);
        String path = resources.getPath(dn_resources);

        /* Add properties given by the resources file *****************************************/
        DataResourceDescription dd = new DataResourceDescription(host, path);
        dd.setStorageSize(resources.getStorageSize(dn_resources));
        dd.setStorageType(resources.getStorageType(dn_resources));

		// Add the adaptors properties (queue types and adaptor properties)
        // TODO Support adaptor properties on DataNodes
        /* Pass all the information to the ResourceManager to insert it into the Runtime ***/
        // TODO Insert DataNode into the runtime
        LOGGER.warn("Cannot load DataNode " + name + ". DataNodes are not supported inside the Runtime");
        //LOGGER.debug("Adding data worker " + name);
        //ResourceManager.newDataWorker(name, dd);
    }

}
