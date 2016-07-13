package eu.ascetic.saas.applicationpackager.utils;

import java.io.IOException;
import java.io.StringReader;
import java.math.BigInteger;
import java.util.List;
import java.util.Vector;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;

import eu.ascetic.saas.applicationpackager.ovf.OVFUtils;
import eu.ascetic.saas.applicationpackager.xml.model.Adapt;
import eu.ascetic.saas.applicationpackager.xml.model.AdaptationRule;
import eu.ascetic.saas.applicationpackager.xml.model.AdaptationSlaTarget;
import eu.ascetic.saas.applicationpackager.xml.model.ApplicationConfig;
import eu.ascetic.saas.applicationpackager.xml.model.ApplicationSlaInfo;
import eu.ascetic.saas.applicationpackager.xml.model.ApplicationSlaTarget;
import eu.ascetic.saas.applicationpackager.xml.model.Attribute;
import eu.ascetic.saas.applicationpackager.xml.model.CpuSpeed;
import eu.ascetic.saas.applicationpackager.xml.model.Node;
import eu.ascetic.saas.applicationpackager.xml.model.NodeSlaTarget;
import eu.ascetic.saas.applicationpackager.xml.model.SoftwareInstall;
import eu.ascetic.saas.applicationpackager.xml.model.StorageResource;
import eu.ascetic.utils.ovf.api.Disk;
import eu.ascetic.utils.ovf.api.DiskSection;
import eu.ascetic.utils.ovf.api.Item;
import eu.ascetic.utils.ovf.api.OperatingSystem;
import eu.ascetic.utils.ovf.api.OvfDefinition;
import eu.ascetic.utils.ovf.api.ProductSection;
import eu.ascetic.utils.ovf.api.References;
import eu.ascetic.utils.ovf.api.VirtualHardwareSection;
import eu.ascetic.utils.ovf.api.VirtualSystem;
import eu.ascetic.utils.ovf.api.VirtualSystemCollection;
import eu.ascetic.utils.ovf.api.enums.DiskFormatType;
import eu.ascetic.utils.ovf.api.enums.OperatingSystemType;
import eu.ascetic.utils.ovf.api.enums.ProductPropertyType;
import eu.ascetic.utils.ovf.api.enums.ResourceType;

/**
 * 
 * Copyright 2015 ATOS SPAIN S.A. 
 * 
 * Licensed under the Apache License, Version 2.0 (the License);
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * 
 * @author David Rojo Antona. Atos Research and Innovation, Atos SPAIN SA
 * e-mail david.rojoa@atos.net 
 * 
 * This class allows to translate from XML to OVF code
 *
 */
public class Xml2OvfTranslator {

	/** The path. */
	private String path;
	
	/**
	 * Instantiates a new xml2 ovf translator.
	 *
	 * @param xmlPath the xml path
	 */
	public Xml2OvfTranslator(String xmlPath){
		path = xmlPath;
	}
	
	/**
	 * Get the OVF from XML file setted as path class attribute.
	 *
	 * @return the string
	 */
	public String translate(){
		ApplicationConfig appCfg = getXml();
		OvfDefinition ovfDefinition = OvfDefinition.Factory.newInstance();
		
		// Virtual System Collection
        VirtualSystemCollection virtualSystemCollection = VirtualSystemCollection.Factory
                .newInstance();
        virtualSystemCollection.setId(appCfg.getApplicationName());
        virtualSystemCollection.setInfo("test description.");
        // Product Section
        ProductSection productSection = ProductSection.Factory.newInstance();
        productSection
                .setInfo("Product configuration for then entire VirtualSystemCollection.");
        productSection.setProduct("product");
        productSection.setVersion("1.0");
        productSection.setVmicMode("online");
        productSection.setDeploymentName(appCfg.getDeploymentName());
        
        //node applicationSLAInfo, get all SLATarget nodes
        ApplicationSlaInfo appSlaInfo = appCfg.getApplicationSLAInfo();
        List<ApplicationSlaTarget> appSlaTargetList = appSlaInfo.getApplicationSlaTarget();
        for (ApplicationSlaTarget applicationSlaTarget : appSlaTargetList){
        	productSection.addSlaInfo(
        			applicationSlaTarget.getSlaTerm(),
        			applicationSlaTarget.getSlaMetricUnit(),
        			OVFUtils.getComparatorOvfFormat(applicationSlaTarget.getComparator()),
        			applicationSlaTarget.getBoundaryValue(),
        			applicationSlaTarget.getSlaType());        	
        }
        
//        productSection.addNewProperty("asceticVmicMode", ProductPropertyType.STRING,
//                appCfg.getMode());
        virtualSystemCollection.addProductSection(productSection);
        
     
        List<Node> nodes = appCfg.getNodes();
        
        if (nodes != null && !nodes.isEmpty()){
        	
        	Vector<VirtualSystem> virtualSystems = new Vector<VirtualSystem>();
        	References references = References.Factory.newInstance();
        	DiskSection diskSection = DiskSection.Factory.newInstance();
        	diskSection.setInfo("List of the virtual disks used.");
        	
        	
	        //iterate for all list and process every machine
	        for (Node n : nodes){

	        	 // Virtual System
		        VirtualSystem virtualSystem = VirtualSystem.Factory.newInstance();
		        virtualSystem.setId(n.getName());
                virtualSystem.setName(n.getName());
		        virtualSystem.setInfo(n.getName() + "Test Virtual System");
		        OperatingSystem operatingSystem = OperatingSystem.Factory.newInstance();
		        operatingSystem.setInfo("Description of " + n.getName() + " Operating System.");
		        if (n.getBaseDependency().getOs().equalsIgnoreCase("Linux")){
		        	//Linux
			        operatingSystem
			                .setId(OperatingSystemType.LINUX);
			        operatingSystem.setVersion(n.getBaseDependency().getOsVersion());
			        //operatingSystem.setVersion("debian-7");
		        }
		        else {
		        	//Windows
		        	 operatingSystem
		                .setId(OperatingSystemType.MICROSOFT_WINDOWS_SERVER_2003);
		        	 operatingSystem.setVersion(n.getBaseDependency().getOsVersion());
		        	 //operatingSystem.setVersion("2003");
		        }
		        virtualSystem.setOperatingSystem(operatingSystem);

		        // Product Section
		        ProductSection productSection2 = ProductSection.Factory.newInstance();
		        productSection2.setInfo("Product configuration for the " + n.getName() +  " VirtualSystem.");
		        productSection2.setProduct(n.getName());
		        productSection2.setVersion("2.0");
		        productSection2.setLowerBound(Integer.parseInt(n.getMinInstance()));
		        productSection2.setUpperBound(Integer.parseInt(n.getMaxInstance()));
		        productSection2.setStartingBound(Integer.parseInt(n.getPrefInstance()));
                productSection2.setAssociatePublicIp(true);
		        productSection2.addNewProperty("asceticCacheImage", ProductPropertyType.UINT32 , "1");
		        
		        List<SoftwareInstall> swList = n.getSoftwareInstalls();
		        if (swList != null){
		        	for (SoftwareInstall sw : n.getSoftwareInstalls()){
		        		 int index = productSection2.addSoftwareDependencyProperties(sw.getName(), "chef-cookbook", sw.getChefUri(), "");
                         List<Attribute> attList = sw.getAttributes();
		        		 for (int i=0; i<attList.size(); i++){
		        			 productSection2.addSoftwareDependencyPackageAttribute(
		        					 index, 
		        					 sw.getName(), 
		        					 attList.get(i).getName(), 
		        					 attList.get(i).getValue());
		        		 }
			        }
			       
		        }
		        
		        //<vmSLAInfo> node inside <node>
		        List<NodeSlaTarget> nodeSlaTargetList = n.getVmSLAInfo().getNodeSlaTarget();
		        if (nodeSlaTargetList != null){
			        for (NodeSlaTarget nodeSlaTarget : nodeSlaTargetList){
			        	productSection2.addSlaInfo(
			        			nodeSlaTarget.getSlaTerm(),
			        			nodeSlaTarget.getSlaMetricUnit(),
			        			OVFUtils.getComparatorOvfFormat(nodeSlaTarget.getComparator()),
			        			nodeSlaTarget.getBoundaryValue(),
			        			nodeSlaTarget.getSlaType());
			        }
		        }
		        
		        //<vmAdaptationRules> node, composed by <adaptation-rule> list
		        if (n.getVmAdaptationRules() != null && n.getVmAdaptationRules().getAdaptationRules() != null){
		        	List<AdaptationRule> adaptationRuleList = n.getVmAdaptationRules().getAdaptationRules();
			        for (AdaptationRule adaptationRule : adaptationRuleList){
			        	AdaptationSlaTarget adaptationSlaTarget = adaptationRule.getAdaptationSlaTarget();
			        	productSection2.addTermMeasurement(
			        			adaptationSlaTarget.getApplicationEvent(),
			        			adaptationSlaTarget.getApplicationMetric(), 
			        			adaptationSlaTarget.getPeriod(), 
			        			adaptationSlaTarget.getBoundaryValue(),
			        			adaptationSlaTarget.getAggregator(), 
			        			adaptationSlaTarget.getAggregatorParams());	
			        	//<adapt> node list inside <adaptation-rule>
			        	List<Adapt> adaptList = adaptationRule.getAdapt();
			        	for (Adapt adapt : adaptList){
			        		if (adapt.getResetLevel() != null && adapt.getMinimalNumOfVMs() != null){
			        			//<adapt resetLevel="1" minimalNumOfVMs="1"/> node type
			        			int index = productSection2.addAdaptationRule(
			        					adaptationSlaTarget.getSlaTerm(),
			        					OVFUtils.getComparatorOvfFormat(adaptationSlaTarget.getComparator()), 
			        					OVFUtils.getResponseType(adaptationSlaTarget, adapt));
			        			productSection2.setAdaptationRuleParameters(index, "VM_TYPE=" + n.getName() + "-img; VM_COUNT=" + adapt.getMinimalNumOfVMs());
			        		}
			        		else {
			        			//<adapt weightdistanceMin="0%" weightedDistanceMax="50%" type="vertical" direction="up"/> node type
			        			productSection2.addAdaptationRule(
			        					adaptationSlaTarget.getSlaTerm(),
			        					OVFUtils.getComparatorOvfFormat(adaptationSlaTarget.getComparator()), 
			        					OVFUtils.getResponseType(adaptationSlaTarget, adapt),
			        					adapt.getTriggerBreachDistancePercentageMin(), 
			        					adapt.getTriggerBreachDistancePercentageMax(), 
			        					adaptationSlaTarget.getSLAType());
//			        			productSection2.addAdaptationRule(agreementTerm, direction, responseType, lowerBound, upperBound, notificationType)
//			        			productSection2.addAdaptationRule(agreementTerm, direction, responseType, lowerBound, upperBound)
			        		}
			        	}
			        }	
		        }     
		        
		        Vector<ProductSection> productSections = new Vector<ProductSection>();
		        productSections.add(productSection2);
		        virtualSystem.setProductSectionArray(productSections
		                .toArray(new ProductSection[productSections.size()]));

		        // Virtual Hardware Section
		        VirtualHardwareSection virtualHardwareSection = VirtualHardwareSection.Factory
		                .newInstance();
		        virtualHardwareSection
		                .setInfo("Description of virtual hardware requirements.");
		        eu.ascetic.utils.ovf.api.System system = eu.ascetic.utils.ovf.api.System.Factory
		                .newInstance();
		        // System
//		        system.setElementName("Virtual System Type");
//		        system.setInstanceID("0");
//		        system.setVirtualSystemType("kvm");
//		        virtualHardwareSection.setSystem(system);

		        // CPU Number
		        Item itemCpuNumber = Item.Factory.newInstance();
		        itemCpuNumber.setDescription("Number of virtual CPUs");
		        itemCpuNumber.setElementName("virtual CPU");
		        itemCpuNumber.setInstanceId("1");
		        itemCpuNumber.setResourceType(ResourceType.PROCESSOR);
		        itemCpuNumber.setVirtualQuantity(new BigInteger("" + n.getPrefNumCore()));
		        virtualHardwareSection.addItem(itemCpuNumber);

		        // CPU Speed
		        if (!virtualHardwareSection.setCPUSpeed(2000)) {
		            Item itemCpuSpeed = Item.Factory.newInstance();
		            itemCpuSpeed.setDescription("CPU Speed");
		            itemCpuSpeed.setElementName(n.getPrefCpuFreq() + " CPU speed reservation");
		            itemCpuSpeed.setInstanceId("1");
		            itemCpuSpeed.setResourceType(ResourceType.PROCESSOR);
		            itemCpuSpeed.setResourceSubType("cpuspeed");
		            CpuSpeed cpu = OVFUtils.getCpuSpeedFormatted(n.getPrefCpuFreq());
			        if (cpu != null){
			        	itemCpuSpeed.setAllocationUnits(cpu.getAllocationUnits());
			        	itemCpuSpeed.setReservation(new BigInteger("" + cpu.getSpeed()));
			        }
			        
//		            itemCpuSpeed.setAllocationUnits("hertz * 2^20");
//		            itemCpuSpeed.setReservation(new BigInteger("2000"));
		            virtualHardwareSection.addItem(itemCpuSpeed);
		        }

		        // Memory
		        Item itemMemory = Item.Factory.newInstance();
		        itemMemory.setDescription("Memory Size");
		        itemMemory.setElementName(n.getPrefMemSize() + " of memory");
		        itemMemory.setInstanceId("2");
		        itemMemory.setResourceType(ResourceType.MEMORY);
		        StorageResource m = OVFUtils.getStorageResourceFormatted(n.getPrefMemSize());
		        if (m != null){
		        	itemMemory.setAllocationUnits(m.getUnits());
		        	itemMemory.setVirtualQuantity(new BigInteger("" + m.getCapacity()));
		        }
		        virtualHardwareSection.addItem(itemMemory);

		        // Network
//		        Item itemNetwork = Item.Factory.newInstance();
//		        itemNetwork.setDescription("Virtual Network");
//		        itemNetwork.addConnection("network");
//		        itemNetwork.setElementName("Ethernet adapter on network");
//		        itemNetwork.setInstanceId("3");
//		        itemNetwork.setResourceType(ResourceType.ETHERNET_ADAPTER);
//		        itemNetwork.setAutomaticAllocation(true);
//		        virtualHardwareSection.addItem(itemNetwork);

		        // Disk
		        Item itemDisk = Item.Factory.newInstance();
		        itemDisk.setDescription("VM Disk");
		        itemDisk.setElementName("VM Disk Drive 1");
		        itemDisk.setInstanceId("4");
		        itemDisk.setResourceType(ResourceType.DISK_DRIVE);
		        itemDisk.addHostResource("ovf:/disk/" + n.getName() + "-img-disk");
		        virtualHardwareSection.addItem(itemDisk);
		        
		        // Disk section		        
		        Disk disk = Disk.Factory.newInstance();
		        disk.setDiskId(n.getName() + "-img-disk");
		        disk.setFileRef(n.getName() + "-img");
		        disk.setFormat(DiskFormatType.RAW);
		        StorageResource d = OVFUtils.getStorageResourceFormatted(n.getPrefDiskSize());
		        if (d != null){
		        	disk.setCapacityAllocationUnits(d.getUnits());
			        disk.setCapacity("" + d.getCapacity());
		        }
		        diskSection.addDisk(disk);
		        
		        // File references		      
//		        File file = File.Factory.newInstance(n.getName() + "-img", "/DFS/some/location/" + n.getName() + ".img");
////		        file.setSize(new BigInteger("1000000000"));
//		        references.addFile(file);
	
		        virtualSystem.setVirtualHardwareSection(virtualHardwareSection);
		        virtualSystems.add(virtualSystem);	        	
	        }
	        
	        virtualSystemCollection.setVirtualSystemArray(virtualSystems
	                .toArray(new VirtualSystem[virtualSystems.size()]));

	        ovfDefinition.setReferences(references);
	        ovfDefinition.setDiskSection(diskSection);
	        ovfDefinition.setVirtualSystemCollection(virtualSystemCollection);

        }

	String s = ovfDefinition.toString();
//culo
//	s = Utils.replaceAmpersand(s);
//	System.out.println("POST-REPLACE AMPERSAND HTML CODE:");
	System.out.println(s);
//	System.out.println("*************************************");
	return s;
	
	}
	
	/**
	 * Gets the xml.
	 *
	 * @return the xml
	 */
	private ApplicationConfig getXml() {
    	String xmlFileTxt = "";
    	try {
    		xmlFileTxt = Utils.readFile(path);
    	} catch (IOException e1) {
    		// TODO Auto-generated catch block
    		e1.printStackTrace();
    	}

    	ApplicationConfig appCfg = null;
    	try {
    		JAXBContext jaxbContext = JAXBContext.newInstance(ApplicationConfig.class);
    		Unmarshaller jaxbUnmarshaller = jaxbContext.createUnmarshaller();
    		appCfg = (ApplicationConfig) jaxbUnmarshaller.unmarshal(new StringReader(xmlFileTxt));
    		System.out.println(xmlFileTxt);
    	} catch (JAXBException e) {
    		// TODO Auto-generated catch block
    		e.printStackTrace();
    	}
    	
    	return appCfg;
	}
}
