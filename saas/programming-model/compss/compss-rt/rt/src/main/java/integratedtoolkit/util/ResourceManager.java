/*
 *  Copyright 2002-2012 Barcelona Supercomputing Center (www.bsc.es)
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package integratedtoolkit.util;

import java.util.List;
import java.util.LinkedList;

import integratedtoolkit.types.ResourceDescription;
import integratedtoolkit.ITConstants;

import integratedtoolkit.api.impl.IntegratedToolkitImpl;
import javax.xml.XMLConstants;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;
import javax.xml.validation.Validator;

import org.apache.log4j.Logger;
import integratedtoolkit.log.Loggers;

import org.w3c.dom.Document;
import org.w3c.dom.NodeList;
import org.w3c.dom.Node;

import javax.xml.parsers.DocumentBuilderFactory;

import javax.xml.transform.Source;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamSource;
import integratedtoolkit.connectors.ConnectorException;
import integratedtoolkit.types.CloudImageDescription;
import integratedtoolkit.types.Implementation;
import integratedtoolkit.types.Resource;
import integratedtoolkit.types.ResourceCreationRequest;
import integratedtoolkit.types.ScheduleState;
import integratedtoolkit.types.ResourceDestructionRequest;
import java.util.HashMap;
import java.util.Map.Entry;
import java.util.PriorityQueue;

/**
 * The ResourceManager class is an utility to manage all the resources available
 * for the cores execution. It keeps information about the features of each
 * resource and is used as an endpoint to discover which resources can run a
 * core in a certain moment, the total and the available number of slots.
 *
 */
public class ResourceManager {

    //XML Document
    private static Document resourcesDoc;

    //Information about resources
    private static ResourcePool pool;

    //Hostname -> Pending Modifications
    private static final HashMap<String, LinkedList<ResourceDestructionRequest>> host2PendingModifications = new HashMap<String, LinkedList<ResourceDestructionRequest>>();

    private static final Logger logger = Logger.getLogger(Loggers.RESOURCES);
    private static final boolean isDebug = logger.isDebugEnabled();

    /**
     * Constructs a new ResourceManager using the Resources xml file content.
     * First of all, an empty resource pool is created and the Cloud Manager is
     * initialized without any providers. Secondly the resource file is
     * validated and parsed and the toplevel xml nodes are processed in
     * different ways depending on its type: - Resource: a new Physical resource
     * is added to the resource pool with the same id as its Name attribute and
     * as many slots as indicated in the project file. If it has 0 slots or it
     * is not on the project xml, the resource is not included.
     *
     * - Service: a new Physical resource is added to the resource pool with the
     * same id as its wsdl attribute and as many slots as indicated in the
     * project file. If it has 0 slots or it is not on the project xml, the
     * resource is not included.
     *
     * - Cloud Provider: if there is any CloudProvider in the project file with
     * the same name, a new Cloud Provider is added to the CloudManager with its
     * name attribute value as identifier. The CloudManager is configured as
     * described between the project xml and the resources file. From the
     * resource file it gets the properties which describe how to connect with
     * it: the connector path, the endpoint, ... Other properties required to
     * manage the resources are specified on the project file: i.e. the maximum
     * amount of resource dpeloyed on that provider. Some configurations depend
     * on both files. One of them is the list of usable images. The images
     * offered by the cloud provider are on a list on the resources file, where
     * there are specified the name and the software description of that image.
     * On the project file there is a description of how the resources created
     * with that image must be used: username, working directory,... Only the
     * images that have been described in both files are added to the
     * CloudManager
     *
     * @param constraintManager constraint Manager with the constraints of the
     * core that will be run in the managed resources
     * @throws Exception Parsing the xml file or creating new instances for the
     * Cloud providers connectors
     */
    public static void load() throws Exception {
        CloudManager.initialize();
        SharedDiskManager.addMachine(IntegratedToolkitImpl.appHost);
        pool = new ResourcePool(CoreManager.coreCount);

        String resourceFile = System.getProperty(ITConstants.IT_RES_FILE);
        // Parse the XML document which contains resource information
        DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
        docFactory.setNamespaceAware(true);

        resourcesDoc = docFactory.newDocumentBuilder().parse(resourceFile);

        // Validate the document against an XML Schema
        SchemaFactory schemaFactory = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
        Source schemaFile = new StreamSource(System.getProperty(ITConstants.IT_RES_SCHEMA));
        Schema schema = schemaFactory.newSchema(schemaFile);
        Validator validator = schema.newValidator();
        validator.validate(new DOMSource(resourcesDoc));

        // resolver = evaluator.createNSResolver(resourcesDoc);
        NodeList nl = resourcesDoc.getChildNodes().item(0).getChildNodes();
        int numRes = 0;
        for (int i = 0; i < nl.getLength(); i++) {
            Node n = nl.item(i);
            if (n.getNodeName().equals("Resource")) {
                numRes++;
                ResourceDescription rd = new ResourceDescription(n);
                String name = rd.getName();
                SharedDiskManager.addMachine(name);
                String taskCount = ProjectManager.getResourceProperty(name, ITConstants.LIMIT_OF_TASKS);
                if (taskCount != null && Integer.parseInt(taskCount) > 0) {
                    pool.addPhysical(name, Integer.parseInt(taskCount), rd);
                } else if (taskCount == null) {
                    pool.addPhysical(name, rd.getProcessorCoreCount(), rd);
                }
                for (int j = 0; j < n.getChildNodes().getLength(); j++) {
                    if (n.getChildNodes().item(j).getNodeName().compareTo("Disks") == 0) {
                        Node disks = n.getChildNodes().item(j);
                        for (int k = 0; k < disks.getChildNodes().getLength(); k++) {
                            if (disks.getChildNodes().item(k).getNodeName().compareTo("Disk") == 0) {
                                Node disk = disks.getChildNodes().item(k);
                                String diskName = disk.getAttributes().getNamedItem("Name").getTextContent();
                                String diskMountpoint = "";
                                for (int ki = 0; ki < disk.getChildNodes().getLength(); ki++) {

                                    if (disk.getChildNodes().item(ki).getNodeName().compareTo("MountPoint") == 0) {
                                        diskMountpoint = disk.getChildNodes().item(ki).getTextContent();
                                    }
                                }
                                SharedDiskManager.addSharedToMachine(diskName, diskMountpoint, name);
                            }
                        }
                    }
                }
            } else if (n.getNodeName().equals("Service")) {
                numRes++;
                String name = n.getAttributes().getNamedItem("wsdl").getTextContent();
                String serviceName = "";
                String namespace = "";
                String portName = "";
                for (int j = 0; j < n.getChildNodes().getLength(); j++) {
                    if (n.getChildNodes().item(j).getNodeName().compareTo("Name") == 0) {
                        serviceName = n.getChildNodes().item(j).getTextContent();
                    }
                    if (n.getChildNodes().item(j).getNodeName().compareTo("Namespace") == 0) {
                        namespace = n.getChildNodes().item(j).getTextContent();
                    }
                    if (n.getChildNodes().item(j).getNodeName().compareTo("Port") == 0) {
                        portName = n.getChildNodes().item(j).getTextContent();
                    }
                }
                String taskCount = ProjectManager.getResourceProperty(name, ITConstants.LIMIT_OF_TASKS);
                if (taskCount != null && Integer.parseInt(taskCount) > 0) {
                    pool.addPhysical(name, serviceName, namespace, portName, Integer.parseInt(taskCount));
                }
            } else if (n.getNodeName().equals("CloudProvider")) {
                String cloudProviderName = n.getAttributes().getNamedItem("name").getTextContent();
                if (!ProjectManager.existsCloudProvider(cloudProviderName)) {
                    continue;
                }
                String connectorPath = "";
                HashMap<String, String> h = new HashMap<String, String>();
                LinkedList<CloudImageDescription> images = new LinkedList<CloudImageDescription>();
                LinkedList<ResourceDescription> instanceTypes = new LinkedList<ResourceDescription>();
                for (int ki = 0; ki < n.getChildNodes().getLength(); ki++) {
                    if (n.getChildNodes().item(ki).getNodeName().compareTo("#text") == 0) {
                    } else if (n.getChildNodes().item(ki).getNodeName().compareTo("Connector") == 0) {
                        connectorPath = n.getChildNodes().item(ki).getTextContent();
                    } else if (n.getChildNodes().item(ki).getNodeName().compareTo("ImageList") == 0) {
                        Node imageList = n.getChildNodes().item(ki);
                        for (int image = 0; image < imageList.getChildNodes().getLength(); image++) {
                            Node resourcesImageNode = imageList.getChildNodes().item(image);
                            if (resourcesImageNode.getNodeName().compareTo("Image") == 0) {
                                String imageName = resourcesImageNode.getAttributes().getNamedItem("name").getTextContent();
                                Node projectImageNode = ProjectManager.existsImageOnProvider(cloudProviderName, imageName);
                                if (projectImageNode != null) {
                                    CloudImageDescription cid = new CloudImageDescription(resourcesImageNode, projectImageNode);
                                    images.add(cid);
                                }
                            }
                        }
                    } else if (n.getChildNodes().item(ki).getNodeName().compareTo("InstanceTypes") == 0) {
                        Node instanceTypesList = n.getChildNodes().item(ki);
                        for (int image = 0; image < instanceTypesList.getChildNodes().getLength(); image++) {
                            Node resourcesInstanceTypeNode = instanceTypesList.getChildNodes().item(image);
                            if (resourcesInstanceTypeNode.getNodeName().compareTo("Resource") == 0) {
                                String instanceCode = resourcesInstanceTypeNode.getAttributes().getNamedItem("Name").getTextContent();
                                Node projectTypeNode = ProjectManager.existsInstanceTypeOnProvider(cloudProviderName, instanceCode);
                                if (projectTypeNode != null) {
                                    ResourceDescription rd = new ResourceDescription(resourcesInstanceTypeNode);
                                    instanceTypes.add(rd);
                                }
                            }
                        }
                    } else {
                        h.put(n.getChildNodes().item(ki).getNodeName(), n.getChildNodes().item(ki).getTextContent());
                    }
                }

                HashMap<String, String> properties = ProjectManager.getCloudProviderProperties(cloudProviderName);
                for (Entry<String, String> e : properties.entrySet()) {
                    h.put(e.getKey(), e.getValue());
                }
                CloudManager.newCloudProvider(cloudProviderName, ProjectManager.getCloudProviderLimitOfVMs(cloudProviderName), connectorPath, h);
                try {
                    for (CloudImageDescription cid : images) {
                        CloudManager.addImageToProvider(cloudProviderName, cid);
                    }
                    for (ResourceDescription instance : instanceTypes) {
                        CloudManager.addInstanceTypeToProvider(cloudProviderName, instance);
                    }
                } catch (Exception e) { /* will never be thrown here, we just added the provider */ }
                CloudManager.setUseCloud(true);
            } else if (n.getNodeName().equals("Disk")) {
                String diskName = n.getAttributes().getNamedItem("Name").getTextContent();
                String mountPoint = "";
                for (int j = 0; j < n.getChildNodes().getLength(); j++) {
                    if (n.getChildNodes().item(j).getNodeName().compareTo("Name") == 0) {
                        diskName = n.getChildNodes().item(j).getTextContent();
                    } else if (n.getChildNodes().item(j).getNodeName().compareTo("MountPoint") == 0) {
                        mountPoint = n.getChildNodes().item(j).getTextContent();
                    }
                }
                SharedDiskManager.addSharedToMachine(diskName, mountPoint, IntegratedToolkitImpl.appHost);
            } else if (n.getNodeName().equals("DataNode")) {
                String host = "";
                String path = "";
                for (int j = 0; j < n.getChildNodes().getLength(); j++) {
                    if (n.getChildNodes().item(j).getNodeName().compareTo("Host") == 0) {
                        host = n.getChildNodes().item(j).getTextContent();
                    } else if (n.getChildNodes().item(j).getNodeName().compareTo("Path") == 0) {
                        path = n.getChildNodes().item(j).getTextContent();
                    }
                }
            }
        }
        linkMachinesToCores();
        logger.info("Initial Resources configuration:");
        logger.info(getCurrentState("\t"));
    }

    private static void linkMachinesToCores() {
        for (Resource r : pool.findAllResources()) {
            linkMachineToCores(r);
        }
    }

    private static void linkMachineToCores(Resource r) {
        int[] maxSims = new int[CoreManager.coreCount];
        for (int coreId = 0; coreId < CoreManager.coreCount; coreId++) {
            Implementation[] impls = CoreManager.getCoreImplementations(coreId);
            for (Implementation impl : impls) {
                if (r.canRun(impl)) {
                    maxSims[coreId] = Math.max(maxSims[coreId], r.simultaneousCapacity(impl));
                }
            }
        }
        pool.setResourceCoreLinks(r, maxSims);
    }

    /**
     * Asks for the vm needed for the runtime to be able to execute all method
     * cores.
     *
     * First it groups the constraints of all the methods per Architecture and
     * tries to merge included resource descriptions in order to reduce the
     * amount of required VMs. It also tries to join the unassigned architecture
     * methods with the closer constraints of a defined one. After that it
     * distributes the Initial Vm Count among the architectures taking into
     * account the number of methods that can be run in each architecture.
     *
     * If the amount of different constraints is higher than the Initial VM
     * count it applies an agressive merge method to each architecture in order
     * to fulfill the initial Constraint. It creates a single VM for each final
     * method constraint.
     *
     * Although these agressive merges, the amount of different constraints can
     * be higher than the initial VM Count constraint. In this case, it violates
     * the initial Vm contraint and asks for more resources.
     *
     * @return the amount of requested VM
     */
    public static int addBasicNodes() {
        LinkedList<ResourceDescription> unfulfilledConstraints = getUnfulfilledConstraints();

        if (unfulfilledConstraints.size() == 0) {
            return 0;
        }
        String initialVMs = ProjectManager.getCloudProperty("InitialVMs");
        int initialVMsCount = 0;
        if (initialVMs != null) {
            initialVMsCount = Integer.parseInt(initialVMs);
        }
        int machineCount = initialVMsCount;
        LinkedList<LinkedList<ResourceDescription>> constraintsPerArquitecture = new LinkedList<LinkedList<ResourceDescription>>();
        /*
         * constraintsPerArquitecture has loaded all constraint for each task.
         * architectures has a list of all the architecture names.
         *
         * e.g.
         * architectures                     constraintsPerArquitecture
         * Intel                =               |MR1|--|MR2|
         * AMD                  =               |MR3|
         * [unassigned]         =               |MR4|--|MR5|
         */
        LinkedList<String> architectures = classifyArchitectures(constraintsPerArquitecture, unfulfilledConstraints);

        /*
         * Tries to reduce the number of machines per architecture by
         * entering constraints in another core's constraints
         *
         */
        constraintsPerArquitecture = reduceArchitecturesConstraints(constraintsPerArquitecture);


        /*
         * Checks if there are enough Vm for a Unassigned Arquitecture
         * If not it set each unassigned task into the architecture with the most similar task
         * e.g.
         * constraintsPerArquitecture
         * Intel --> |MR1|--|MR2|--|MR5|
         * AMD --> |MR3|--|MR4|
         *
         */
        constraintsPerArquitecture = reassignUnassignedConstraints(architectures, constraintsPerArquitecture, machineCount);

        /*
         * Tries to reduce the number of machines per architecture by
         * entering constraints in another core's constraints
         *
         */
        constraintsPerArquitecture = reduceArchitecturesConstraints(constraintsPerArquitecture);

        /*
         * Distributes all VMs among all the architectures
         * e.g. Total Vm = 10 Intel -->6    AMD --> 4
         *
         */
        int numArchitectures = constraintsPerArquitecture.size();
        int[] machinesCountPerArchitecture = new int[numArchitectures];
        int[] constraintsCountPerArchitecture = new int[numArchitectures];
        for (int index = 0; index < numArchitectures; index++) {
            machinesCountPerArchitecture[index] = 1;
            constraintsCountPerArchitecture[index] = 0;
            for (int constraintIndex = 0; constraintIndex < constraintsPerArquitecture.get(index).size(); constraintIndex++) {
                constraintsCountPerArchitecture[index] += constraintsPerArquitecture.get(index).get(constraintIndex).getSlots();
            }
        }

        for (int i = numArchitectures; i < machineCount; i++) {
            int opcio = 0;
            float millor = (float) constraintsCountPerArchitecture[0] / (float) machinesCountPerArchitecture[0];
            for (int index = 1; index < constraintsPerArquitecture.size(); index++) {
                if (millor < (float) constraintsCountPerArchitecture[index] / (float) machinesCountPerArchitecture[index]) {
                    millor = (float) constraintsCountPerArchitecture[index] / (float) machinesCountPerArchitecture[index];
                    opcio = index;
                }
            }
            machinesCountPerArchitecture[opcio]++;
        }

        // Asks for the necessary VM
        int createdCount = 0;
        for (int index = 0; index < numArchitectures; index++) {
            // Create 6 machines for |MR1|--|MR2|--|MR5|
            createdCount += createBasicType(constraintsPerArquitecture.get(index), machinesCountPerArchitecture[index]);
        }

        logger.info("In order to be able to execute all cores, Resource Manager has asked for " + createdCount + " Cloud resources");
        return createdCount;
    }

    /**
     * Asks for the rest of VM that user wants to start with.
     *
     * After executing the addBasicNodes, it might happen that the number of
     * initial VMs contrained by the user is still not been fulfilled. The
     * addBasicNodes creates up to as much VMs as different methods. If the
     * initial VM Count is higher than this number of methods then there will be
     * still some VM requests missing.
     *
     * The addExtraNodes creates this difference of VMs. First it tries to merge
     * the method constraints that are included into another methods. And
     * performs a less aggressive and more equal distribution.
     *
     * @param alreadyCreated number of already requested VMs
     * @return the number of extra VMs created to fulfill the Initial VM Count
     * constaint
     */
    public static int addExtraNodes(int alreadyCreated) {
        String initialVMs = ProjectManager.getCloudProperty("InitialVMs");
        int initialVMsCount = 0;
        if (initialVMs != null) {
            initialVMsCount = Integer.parseInt(initialVMs);
        }
        int vmCount = initialVMsCount - alreadyCreated;
        if (vmCount <= 0) {
            return 0;
        }
        logger.info(alreadyCreated + " instances were already ordered. ResourceManager can still ask for " + vmCount + " more.");


        /*
         * Tries to reduce the number of machines by
         * entering methodConstraints in another method's machine
         *
         */
        LinkedList<ResourceDescription> constraints = new LinkedList<ResourceDescription>();
        for (int i = 0; i < CoreManager.coreCount; i++) {
            ResourceDescription rc = CoreManager.getResourceConstraints(i)[0];
            if (rc != null) {
                constraints.add(new ResourceDescription(rc));
            }
        }
        if (constraints.size() == 0) {
            return 0;
        }
        constraints = reduceConstraints(constraints);

        int numTasks = constraints.size();
        int[] vmCountPerContraint = new int[numTasks];
        int[] coreCountPerConstraint = new int[numTasks];

        for (int index = 0; index < numTasks; index++) {
            vmCountPerContraint[index] = 1;
            coreCountPerConstraint[index] = constraints.get(index).getSlots();
        }

        for (int i = 0; i < vmCount; i++) {
            float millor = 0.0f;
            int opcio = 0;
            for (int j = 0; j < constraints.size(); j++) {
                if (millor < ((float) coreCountPerConstraint[j] / (float) vmCountPerContraint[j])) {
                    opcio = j;
                    millor = ((float) coreCountPerConstraint[j] / (float) vmCountPerContraint[j]);
                }
            }
            ResourceCreationRequest rcr = CloudManager.askForResources(constraints.get(opcio), false);

            logger.info("Ordering the creation of a " + rcr.getRequested().getType() + " instance to " + rcr.getProvider() + " to fulfill the initial Cloud instances constraint.");
            if (isDebug) {
                StringBuilder sb = new StringBuilder("Expecting to obtain an instance able to run [");
                sb.append(rcr.requestedSimultaneousTaskCount()[0]);
                for (int k = 1; k < CoreManager.coreCount; k++) {
                    sb.append(", ").append(rcr.requestedSimultaneousTaskCount()[k]);
                }
                sb.append("] simultaneous tasks.");
                logger.debug(sb.toString());
            }

            vmCountPerContraint[opcio]++;
        }

        return vmCount;
    }

    public static void confirmCloudRequest(ResourceCreationRequest rcr, String provider, String vmName) {
        //Remove creation counters
        CloudManager.respondedRequest(rcr);
        //Confirm Resource Creation
        CloudManager.confirmedRequest(vmName, provider, rcr);

    }

    public static void addCloudResource(String vmName, ResourceDescription res, Integer limitOfTasks) {
        Resource r;
        if ((r = pool.upgradeResource(vmName, res, limitOfTasks)) == null) {
            r = pool.addCritical(vmName, limitOfTasks, res);

            SharedDiskManager.addMachine(vmName);
            for (java.util.Map.Entry<String, String> e : res.getImage().getSharedDisks().entrySet()) {
                SharedDiskManager.addSharedToMachine(e.getKey(), e.getValue(), vmName);
            }
        }
        linkMachineToCores(r);
        pool.defineCriticalSet();
        logger.info("Resource configuration after adding the resource\n" + getCurrentState("\t"));
    }

    public static void refuseCloudRequest(ResourceCreationRequest rcr, String provider) {
        //Terminate Resource
        //TODO: TERMINATE CLOUD VM
        //Remove all counters
        CloudManager.respondedRequest(rcr);
        CloudManager.refusedRequest(provider, rcr);
    }

    public static void errorCloudRequest(ResourceCreationRequest rcr, String provider) {
        //Remove all counters
        CloudManager.respondedRequest(rcr);
        CloudManager.refusedRequest(provider, rcr);
    }

    public static Resource getResource(String name) {
        return pool.getResource(name);
    }

    /**
     * Return a list of all the resources
     *
     * @param coreId Id of the task's core
     * @return list of all the resources
     */
    public static List<Resource> getAllResources() {
        return pool.findAllResources();
    }

    /**
     * Return a list of all the resources able to run a task of the core coreId
     *
     * @param coreId Id of the task's core
     * @return list of all the resources able to run a task of the core coreId
     */
    public static List<Resource> findCompatibleResources(int coreId) {
        return pool.findCompatibleResources(coreId);
    }

    public static HashMap<Resource, LinkedList<Implementation>> findAvailableResources(Implementation[] implementations, List<Resource> resources) {
        HashMap<Resource, LinkedList<Implementation>> available = new HashMap<Resource, LinkedList<Implementation>>();
        for (Resource r : resources) {
            LinkedList<Implementation> impls = new LinkedList<Implementation>();
            for (Implementation implementation : implementations) {
                if (r.canRunNow(implementation.getResource())) {
                    impls.add(implementation);
                }
            }
            if (!impls.isEmpty()) {
                available.put(r, impls);
            }
        }
        return available;
    }

    /**
     * Checks if a resource can execute a certain core
     *
     * @param resourceName name of the resource
     * @param coreId Id of the core
     * @return true if it can run the core
     */
    public static boolean matches(String resourceName, int coreId) {
        return pool.matches(resourceName, coreId);
    }

    //Occupies a free slot
    public static void reserveResource(String resourceName, Implementation impl) {
        pool.reserveCapabilities(resourceName, impl.getResource());
    }

    //Releases a busy slot
    public static void freeResource(String resourceName, Implementation impl) {
        pool.freeCapabilities(resourceName, impl.getResource());
    }

    public static void getResourcesState(ScheduleState state) {
        state.useCloud = CloudManager.isUseCloud();
        try {
            state.creationTime = CloudManager.getNextCreationTime();
        } catch (Exception ex) {
            state.creationTime = 120000l;
        }
        state.currentCloudVMCount = CloudManager.getCurrentVMCount();
        for (Resource resource : ResourceManager.getAllResources()) {
            state.addHost(resource.getName(), resource.getSimultaneousTasks());
        }

        for (ResourceCreationRequest rcr : CloudManager.getPendingRequests()) {
            int[] simTasks = rcr.requestedSimultaneousTaskCount();
            for (int i = 0; i < simTasks.length; i++) {
                state.cloudSlots[i] += simTasks[i];
            }
        }
    }

    public static String getResourceMonitoringData(String prefix, String resourceName) {
        Resource res = pool.getResource(resourceName);
        return res.getMonitoringData(prefix);
    }

    //Returns a list with all coreIds that can be executed by the resource res
    public static List<Integer> getExecutableCores(String resourceName) {
        return pool.getExecutableCores(resourceName);
    }

    //Removes a resource from the pool if its an useless noncritical cloud resource
    public static LinkedList<ResourceDestructionRequest> tryToTerminate(String resourceName, int[] counts) {
        Resource res = null;
        if (ResourceManager.useCloud() && (res = pool.getDynamicResource(resourceName)) != null) {
            boolean useful = checkResourceUsefulness(res, counts);
            if (useful) {
                return new LinkedList<ResourceDestructionRequest>();
            }
            return destroyUselessResource(res);
        }
        return new LinkedList<ResourceDestructionRequest>();
    }

    public static LinkedList<ResourceDestructionRequest> terminateUnbounded(int[] counts) {
        LinkedList<ResourceDestructionRequest> rdrs = new LinkedList<ResourceDestructionRequest>();
        CloudManager.stopReached();
        if (CloudManager.isUseCloud()) {
            LinkedList<Resource> resources = pool.getDynamicResources();
            for (Resource candidate : resources) {
                boolean useful = checkResourceUsefulness(candidate, counts);
                if (useful) {
                    continue;
                }
                rdrs.addAll(destroyUselessResource(candidate));
            }
        }
        return rdrs;
    }

    private static boolean checkResourceUsefulness(Resource res, int[] counts) {
        boolean useful = res.getTaskCount() > 0;
        LinkedList<Integer> executableCores = res.getExecutableCores();
        int[] simTasks = res.getSimultaneousTasks();
        int[] totalSimTasks = pool.getCoreMaxTaskCount();
        for (int i = 0; i < executableCores.size() && !useful; i++) {
            int coreId = executableCores.get(i);
            useful = counts[coreId] > (totalSimTasks[coreId] - simTasks[coreId]);
        }
        return useful;
    }

    private static LinkedList<ResourceDestructionRequest> destroyUselessResource(Resource res) {
        LinkedList<ResourceDestructionRequest> deletions = new LinkedList<ResourceDestructionRequest>();
        pool.delete(res);
        HashMap<ResourceDescription, Integer> composition = CloudManager.getVMComposition(res.getName());
        for (java.util.Map.Entry<ResourceDescription, Integer> entry : composition.entrySet()) {
            ResourceDescription type = entry.getKey();
            Integer amount = entry.getValue();
            for (int i = 0; i < amount; i++) {
                ResourceDescription rd = new ResourceDescription(type);
                rd.setName(res.getName());
                ResourceDestructionRequest rdr = new ResourceDestructionRequest(rd, false);
                CloudManager.performReduction(rdr);
                if (!rdr.isTerminate()) {
                    CloudManager.terminate(rdr);
                }
                deletions.add(rdr);
            }
        }
        return deletions;
    }

//Shuts down all cloud resources
    public static void stopVirtualNodes()
            throws Exception {
        CloudManager.terminateALL();
        logger.info("Total Execution Cost :" + CloudManager.getTotalCost());
    }

    //Shuts down all the machine associated to the name name
    public static void terminate(ResourceDestructionRequest rdr) {
        CloudManager.terminate(rdr);
        logger.info("The current Cloud resources set costs " + CloudManager.currentCostPerHour());
    }

    //Removes from the list all the Constraints fullfilled by existing resources
    private static LinkedList<ResourceDescription> getUnfulfilledConstraints() {
        LinkedList<ResourceDescription> unfulfilledConstraints = new LinkedList<ResourceDescription>();
        int[] maxSimTasks = pool.getCoreMaxTaskCount();
        for (int i = 0; i < CoreManager.coreCount; i++) {
            ResourceDescription rc = CoreManager.getResourceConstraints(i)[0];
            if (maxSimTasks[i] == 0 && rc != null) {
                unfulfilledConstraints.add(new ResourceDescription(rc));
            }
        }
        return unfulfilledConstraints;
    }

    //classifies the constraints  depending on their arquitecture and leaves it on coreResourceList
    //Return a list with all the Architectures Names
    private static LinkedList<String> classifyArchitectures(LinkedList<LinkedList<ResourceDescription>> constraintsPerArquitecture, LinkedList<ResourceDescription> constraints) {
        LinkedList<String> architectures = new LinkedList<String>();

        //For each core
        for (int i = 0; i < constraints.size(); i++) {
            ResourceDescription mr = constraints.get(i);

            //checks the architecture
            //   Not exists --> creates a new List
            Boolean found = false;
            int indexArchs;
            for (indexArchs = 0; indexArchs < architectures.size() && !found; indexArchs++) {
                found = (architectures.get(indexArchs).compareTo(mr.getProcessorArchitecture()) == 0);
            }
            indexArchs--;

            LinkedList<ResourceDescription> assignedList;
            if (!found) {
                architectures.add(mr.getProcessorArchitecture());
                assignedList = new LinkedList<ResourceDescription>();
                constraintsPerArquitecture.add(assignedList);
            } else {
                assignedList = constraintsPerArquitecture.get(indexArchs);
            }
            //AssignedList has  the list for the resource constraints

            assignedList.add(mr);
            mr.addSlot();
        }

        return architectures;
    }

    private static LinkedList<LinkedList<ResourceDescription>> reduceArchitecturesConstraints(LinkedList<LinkedList<ResourceDescription>> mrList) {
        LinkedList<LinkedList<ResourceDescription>> reduced = new LinkedList<LinkedList<ResourceDescription>>();
        for (int i = 0; i < mrList.size(); i++) {
            reduced.add(reduceConstraints(mrList.get(i)));
        }
        return reduced;
    }

    private static LinkedList<ResourceDescription> reduceConstraints(LinkedList<ResourceDescription> architecture) {

        LinkedList<ResourceDescription> reducedArchitecture = new LinkedList<ResourceDescription>();
        LinkedList<boolean[]> values = new LinkedList<boolean[]>();

        for (int j = 0; j < architecture.size(); j++) {
            boolean[] taskCount = new boolean[architecture.size()];
            values.add(taskCount);
        }

        boolean[] invalid = new boolean[architecture.size()];
        for (int j = 0; j < architecture.size(); j++) {
            for (int k = j + 1; k < architecture.size(); k++) {
                Integer compared = architecture.get(j).into(architecture.get(k));
                if (compared == null) {
                } else {
                    if (compared == 1) { // They are the same resource
                        invalid[j] = true;
                        values.get(k)[j] = true;
                        values.get(j)[k] = true;
                    } else { //They are differents
                        if (compared < 1) { // the passed in resource is bigger
                            //Invalidate the first
                            invalid[j] = true;
                            values.get(k)[j] = true;
                        } else {// the callee resource is bigger
                            //Invalidate the second
                            invalid[k] = true;
                            values.get(j)[k] = true;
                        }
                    }
                }
            }
        }

        for (int j = 0; j < architecture.size(); j++) {
            if (!invalid[j]) {
                reducedArchitecture.add(architecture.get(j));
                int count = 1;
                for (int k = 0; k < architecture.size(); k++) {
                    if (values.get(j)[k]) {
                        count++;
                    }
                }
                architecture.get(j).setSlots(count);
            }
        }

        return reducedArchitecture;
    }

    private static LinkedList<LinkedList<ResourceDescription>> reassignUnassignedConstraints(LinkedList<String> architectures, LinkedList<LinkedList<ResourceDescription>> mrList, int machineCount) {
        LinkedList<ResourceDescription> unassigned = new LinkedList<ResourceDescription>();
        LinkedList<LinkedList<ResourceDescription>> assigned = new LinkedList<LinkedList<ResourceDescription>>();
        for (int i = 0; i < architectures.size(); i++) {
            if (architectures.get(i).compareTo("[unassigned]") == 0) {
                unassigned.addAll(mrList.get(i));
            } else {
                assigned.add(mrList.get(i));
            }
        }

        if ((assigned.size() < machineCount || assigned.size() == 0) && unassigned.size() != 0) {
            assigned.add(unassigned);
        } else {
            for (int unassignedIndex = 0; unassignedIndex < unassigned.size(); unassignedIndex++) {
                Boolean posat = false;

                int optionsBestArchitecture = 0;
                Float optionsBestDifference = Float.MAX_VALUE;

                ResourceDescription candidate = unassigned.get(unassignedIndex);

                for (int architecturesIndex = 0; architecturesIndex < assigned.size() && !posat; architecturesIndex++) {
                    for (int taskIndex = 0; taskIndex < assigned.get(architecturesIndex).size() && !posat; taskIndex++) {
                        float difference = candidate.difference(assigned.get(architecturesIndex).get(taskIndex));
                        if (optionsBestDifference < 0) {
                            if (difference < 0) {
                                if (difference > optionsBestDifference) {
                                    optionsBestArchitecture = architecturesIndex;
                                    optionsBestDifference = difference;
                                }
                            }
                        } else {
                            if (difference < optionsBestDifference) {
                                optionsBestArchitecture = architecturesIndex;
                                optionsBestDifference = difference;
                            }
                        }
                    }
                }
                assigned.get(optionsBestArchitecture).add(candidate);
            }
        }
        return assigned;
    }

    public static String[] getBestSafeResourcePerCore() {
        int coreCount = CoreManager.coreCount;
        String[] bestResource = new String[coreCount];
        for (int i = 0; i < coreCount; i++) {
            if (CoreManager.getResourceConstraints(i) != null) {
                bestResource[i] = pool.getSafeResource(i);
            } else {
                bestResource[i] = null;
            }
        }
        return bestResource;

    }

    private static int createBasicType(LinkedList<ResourceDescription> resourceConstraintsList, int vmCount) {
        LinkedList<ResourceDescription> sortedList = new LinkedList<ResourceDescription>();
        sortedList.add(resourceConstraintsList.get(0));

        //sort Resources by Processor & MemoryPhysicalSize
        for (int originalIndex = 1; originalIndex < resourceConstraintsList.size(); originalIndex++) {
            int sortedIndex;
            boolean minimum = false;
            for (sortedIndex = 0; sortedIndex < sortedList.size() && !minimum; sortedIndex++) {
                Float difference = resourceConstraintsList.get(originalIndex).difference(sortedList.get(sortedIndex));
                minimum = difference >= 0;
            }
            if (minimum) {
                sortedIndex--;
            }
            sortedList.add(sortedIndex, resourceConstraintsList.get(originalIndex));
        }

        // join Resources if needed
        LinkedList<Float> differences = new LinkedList<Float>();
        for (int i = 1; i < sortedList.size(); i++) {
            differences.add(sortedList.get(i - 1).difference(sortedList.get(i)));
        }

        while (sortedList.size() > vmCount) {
            int index = 0;
            float min = differences.get(0);
            for (int i = 0; i < differences.size(); i++) {
                if (differences.get(i) <= min) {
                    min = differences.get(i);
                    index = i;
                }
            }

            sortedList.get(index).join(sortedList.get(index + 1));
            sortedList.remove(index + 1);
            if (index - 1 >= 0) {
                differences.set(index - 1, sortedList.get(index - 1).difference(sortedList.get(index)));
            }
            if (index + 1 < sortedList.size()) {
                differences.set(index + 1, sortedList.get(index).difference(sortedList.get(index + 1)));
            }
            differences.remove(index);
        }

        int typeCount = 0;
        for (ResourceDescription rd : sortedList) {
            ResourceCreationRequest rcr = CloudManager.askForResources(rd, false);
            if (rcr != null) {
                logger.info("Ordering the creation of a " + rcr.getRequested().getType() + " instance to " + rcr.getProvider() + " so the runtime can execute all cores.");
                if (isDebug) {
                    StringBuilder sb = new StringBuilder("Expecting to obtain an instance able to run [");
                    sb.append(rcr.requestedSimultaneousTaskCount()[0]);
                    for (int i = 1; i < CoreManager.coreCount; i++) {
                        sb.append(", ").append(rcr.requestedSimultaneousTaskCount()[i]);
                    }
                    sb.append("] simultaneous tasks.");
                    logger.debug(sb.toString());
                }
                typeCount++;
            }
        }
        return typeCount;
    }

    public static Long getCreationTime()
            throws Exception {
        try {
            return CloudManager.getNextCreationTime();
        } catch (ConnectorException e) {
            throw new Exception(e);
        }
    }

    public static boolean useCloud() {
        return CloudManager.isUseCloud();
    }

    public static ResourceCreationRequest increaseResources(float[] recomendations, LinkedList<Integer> required, boolean mandatoryCreation) {
        ValueResourceDescription v;
        PriorityQueue<ValueResourceDescription> pq = new PriorityQueue<ValueResourceDescription>();
        for (Integer coreId : required) {
            ResourceDescription[] rds = CoreManager.getResourceConstraints(coreId);
            if (rds[0] != null) {
                v = new ValueResourceDescription();
                v.rd = rds[0];
                v.value = recomendations[coreId];
                pq.add(v);
                recomendations[coreId] = 0;
            }
        }
        while ((v = pq.poll()) != null) {
            ResourceCreationRequest rcr = CloudManager.askForResources((int) v.value, v.rd, true);
            if (rcr != null) {
                logger.info("Ordering the creation of a " + rcr.getRequested().getType() + " instance to " + rcr.getProvider() + ".");
                if (isDebug) {
                    StringBuilder sb = new StringBuilder("Expecting to obtain an instance able to run [");
                    sb.append(rcr.requestedSimultaneousTaskCount()[0]);
                    for (int i = 1; i < CoreManager.coreCount; i++) {
                        sb.append(", ").append(rcr.requestedSimultaneousTaskCount()[i]);
                    }
                    sb.append("] simultaneous tasks.");
                    logger.debug(sb.toString());
                }
                return rcr;
            }
        }
        for (int coreId = 0; coreId < recomendations.length; coreId++) {
            if (recomendations[coreId] != 0) {
                ResourceDescription[] rds = CoreManager.getResourceConstraints(coreId);
                if (rds[0] != null) {
                    if (mandatoryCreation || recomendations[coreId] > 0) {
                        v = new ValueResourceDescription();
                        v.rd = rds[0];
                        v.value = recomendations[coreId];
                        pq.add(v);
                    }
                    recomendations[coreId] = 0;
                }
            }
        }
        while ((v = pq.poll()) != null) {
            ResourceCreationRequest rcr = CloudManager.askForResources((int) v.value, v.rd, true);
            if (rcr != null) {
                logger.info("Ordering the creation of a " + rcr.getRequested().getType() + " instance to " + rcr.getProvider() + ".");
                if (isDebug) {
                    StringBuilder sb = new StringBuilder("Expecting to obtain an instance able to run [");
                    sb.append(rcr.requestedSimultaneousTaskCount()[0]);
                    for (int i = 1; i < CoreManager.coreCount; i++) {
                        sb.append(", ").append(rcr.requestedSimultaneousTaskCount()[i]);
                    }
                    sb.append("] simultaneous tasks.");
                    logger.debug(sb.toString());
                }
                return rcr;
            }
        }

        return null;

    }

    public static void notifyShutdown(ResourceDestructionRequest rdr) {
        CloudManager.notifyShutdown(rdr);
    }

    private static class ValueResourceDescription implements Comparable<ValueResourceDescription> {

        float value;
        ResourceDescription rd;

        @Override
        public int compareTo(ValueResourceDescription o) {
            float dif = value - o.value;
            if (dif > 0) {
                return 1;
            } else if (dif < 0) {
                return -1;
            } else {
                return 0;
            }
        }
    }

    public static ResourceDestructionRequest reduceResources(float[] destroyRecommendations, boolean mandatoryDestruction) {
        //Getting all modifiable resources
        java.util.Set<Resource> noncritical = pool.getNonCriticalResources(destroyRecommendations, mandatoryDestruction);
        java.util.Set<Resource> critical = pool.getCriticalResources(destroyRecommendations, mandatoryDestruction);
        //Getting best destruction option for each set
        Object[] noncriticalSolution = CloudManager.getBestDestruction(noncritical, destroyRecommendations);
        Object[] criticalSolution = CloudManager.getBestDestruction(critical, destroyRecommendations);

        boolean criticalIsBetter;
        if (criticalSolution == null) {
            if (noncriticalSolution == null) {
                return null;
            } else {
                criticalIsBetter = false;
            }
        } else {
            if (noncriticalSolution == null) {

                criticalIsBetter = true;
            } else {
                criticalIsBetter = false;
                float[] noncriticalValues = (float[]) noncriticalSolution[1];
                float[] criticalValues = (float[]) criticalSolution[1];

                if (noncriticalValues[0] == criticalValues[0]) {
                    if (noncriticalValues[1] == criticalValues[1]) {
                        if (noncriticalValues[2] < criticalValues[2]) {
                            criticalIsBetter = true;
                        }
                    } else {
                        if (noncriticalValues[1] > criticalValues[1]) {
                            criticalIsBetter = true;
                        }
                    }
                } else {
                    if (noncriticalValues[0] > criticalValues[0]) {
                        criticalIsBetter = true;
                    }
                }
            }

        }

        Resource res;
        float[] record;
        ResourceDescription rd;
        int[] slotsRemovingCount;
        if (criticalIsBetter && pool.isCriticalRemovalSafe((int[]) criticalSolution[2])) {
            slotsRemovingCount = (int[]) criticalSolution[2];
            res = (Resource) criticalSolution[0];
            record = (float[]) criticalSolution[1];
            rd = (ResourceDescription) criticalSolution[3];
        } else {
            if (noncriticalSolution == null) {
                return null;
            }
            res = (Resource) noncriticalSolution[0];
            record = (float[]) noncriticalSolution[1];
            slotsRemovingCount = (int[]) noncriticalSolution[2];
            rd = (ResourceDescription) noncriticalSolution[3];
        }

        if (!mandatoryDestruction && record[1] > 0) {
            return null;
        } else {
            ResourceDescription finalDescription = new ResourceDescription(rd);
            finalDescription.setName(res.getName());
            ResourceDestructionRequest rdr = new ResourceDestructionRequest(finalDescription, false);
            manageResourceDestruction(res, rdr, slotsRemovingCount);
            return rdr;
        }

    }

    public static LinkedList<Implementation> canRunNow(String resourceName, Implementation[] impls) {
        Resource resource = pool.getResource(resourceName);
        return canRunNow(resource, impls);
    }

    public static LinkedList<Implementation> canRunNow(String resourceName, LinkedList<Implementation> impls) {
        Resource resource = pool.getResource(resourceName);
        return canRunNow(resource, impls);
    }

    public static LinkedList<Implementation> canRunNow(Resource resource, Implementation[] impls) {

        LinkedList<Implementation> validImplementations = new LinkedList<Implementation>();
        for (Implementation impl : impls) {
            if (resource.canRunNow(impl.getResource())) {
                validImplementations.add(impl);
            }
        }
        return validImplementations;
    }

    public static LinkedList<Implementation> canRunNow(Resource resource, LinkedList<Implementation> impls) {
        LinkedList<Implementation> validImplementations = new LinkedList<Implementation>();
        for (Implementation impl : impls) {
            if (resource.canRunNow(impl.getResource())) {
                validImplementations.add(impl);
            }
        }
        return validImplementations;
    }

    public static void newCoreElementsDetected(LinkedList<Integer> newCores) {
        pool.newCoreElementsDetected(newCores);
        CloudManager.newCoreElementsDetected(newCores);
    }

    private static void manageResourceDestruction(Resource res, ResourceDestructionRequest rdr, int[] slotReduction) {
        logger.info("Ordering the destruction of a " + rdr.getRequested().getType() + " instance of " + rdr.getRequested().getName() + ".");
        if (isDebug) {
            StringBuilder sb = new StringBuilder("Expecting to destroy resources able to run [");
            sb.append(slotReduction[0]);
            for (int i = 1; i < CoreManager.coreCount; i++) {
                sb.append(", ").append(slotReduction[i]);
            }
            sb.append("] simultaneous tasks.");
            logger.debug(sb.toString());
        }
        //Fa la reserva i fa al pool si hi ha prous recursos pendents
        if (pool.markResourcesToRemove(res, rdr.getRequested(), slotReduction)) {
            //Afegeix la destrucció a la màquina i indica si es destrueix totalment
            CloudManager.performReduction(rdr);
            if (!rdr.isTerminate()) {
                logger.info("There are enough available resources to be destroyed and the Vm is not completely killed. Terminating resource.");
                CloudManager.terminate(rdr);
            } else {
                logger.info("There are enough available resources to be destroyed and the vm is completely killed. All the unique data must be saved");
                pool.delete(res);
            }
        } else {
            logger.info("There are not enough available resources to perform the modification. Registering modification as pending");
            LinkedList<ResourceDestructionRequest> modifications = host2PendingModifications.get(res.getName());
            if (modifications == null) {
                modifications = new LinkedList<ResourceDestructionRequest>();
                host2PendingModifications.put(res.getName(), modifications);
            }
            CloudManager.addPendingReduction(rdr);
            modifications.add(rdr);
        }
    }

    public static ResourceDestructionRequest checkPendingModifications(String resourceName) {
        LinkedList<ResourceDestructionRequest> modifications = host2PendingModifications.get(resourceName);
        if (modifications != null && !modifications.isEmpty()) { //if there are pending modifications
            ResourceDestructionRequest modification = modifications.get(0);
            Resource res = pool.getResource(resourceName);
            if (res.isAvailable(modification.getRequested())) {
                modifications.removeFirst();
                pool.confirmPendingReduction(res, modification.getRequested());
                CloudManager.confirmReduction(modification);
                if (!modification.isTerminate()) {
                    logger.info("There is a pending modification and enough available resources to be destroyed. The vm will be alive after performing the modification so the resources are destroyed.");
                    CloudManager.terminate(modification);
                } else {
                    logger.info("There is a pending modification and enough available resources to be destroyed. The vm will be completely killed and unique data must be saved.");
                    pool.delete(res);
                }
                return modification;
            } else {
                logger.debug("There is a pending modification for node " + resourceName + ", but there are not enough available resources yet.");
            }
        }
        return null;
    }

    public static String getCurrentState(String prefix) {
        StringBuilder sb = new StringBuilder();
        sb.append(pool.getCurrentState(prefix));
        sb.append(CloudManager.getCurrentState(prefix));
        return sb.toString();
    }
}
