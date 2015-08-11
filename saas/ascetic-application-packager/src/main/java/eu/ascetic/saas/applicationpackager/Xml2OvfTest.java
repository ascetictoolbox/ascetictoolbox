package eu.ascetic.saas.applicationpackager;

import java.io.IOException;
import java.io.StringReader;
import java.math.BigInteger;
import java.util.List;
import java.util.Vector;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;

import eu.ascetic.saas.applicationpackager.ovf.OVFUtils;
import eu.ascetic.saas.applicationpackager.utils.Utils;
import eu.ascetic.saas.applicationpackager.xml.model.ApplicationConfig;
import eu.ascetic.saas.applicationpackager.xml.model.CpuSpeed;
import eu.ascetic.saas.applicationpackager.xml.model.Node;
import eu.ascetic.saas.applicationpackager.xml.model.StorageResource;
import eu.ascetic.utils.ovf.api.Disk;
import eu.ascetic.utils.ovf.api.DiskSection;
import eu.ascetic.utils.ovf.api.File;
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

public class Xml2OvfTest {

	public static void main(String args[]){
		ApplicationConfig appCfg = getXml();
		generateOvf(appCfg);
	}

	private static void generateOvf(ApplicationConfig appCfg) {
		
		OvfDefinition ovfDefinition = OvfDefinition.Factory.newInstance();
		
		// Virtual System Collection
        VirtualSystemCollection virtualSystemCollection = VirtualSystemCollection.Factory
                .newInstance();
        virtualSystemCollection.setId(appCfg.getName());
        virtualSystemCollection.setInfo("test description.");
        // Product Section
        ProductSection productSection = ProductSection.Factory.newInstance();
        productSection
                .setInfo("Product configuration for then entire VirtualSystemCollection.");
        productSection.setProduct("product");
        productSection.setVersion("1.0");
        productSection.addNewProperty("asceticVmicMode", ProductPropertyType.STRING,
                appCfg.getMode());
        productSection.setVmicMode("online");
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
		        virtualSystem.setInfo(n.getName() + "Test Virtual System");
		        OperatingSystem operatingSystem = OperatingSystem.Factory.newInstance();
		        operatingSystem.setInfo("Description of " + n.getName() + " Operating System.");
		        operatingSystem
		                .setId(OperatingSystemType.LINUX);
		        operatingSystem.setVersion(n.getBaseDependency().getOs());
		        virtualSystem.setOperatingSystem(operatingSystem);

		        // Product Section
		        ProductSection productSection2 = ProductSection.Factory.newInstance();
		        productSection2.setInfo("Product configuration for the " + n.getName() +  " VirtualSystem.");
		        productSection2.setProduct(n.getName());
		        productSection2.setVersion("2.0");
		        productSection2.setLowerBound(Integer.parseInt(n.getMinInstance()));
		        productSection2.setUpperBound(Integer.parseInt(n.getMaxInstance()));
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
		        itemCpuNumber.setVirtualQuantity(new BigInteger("" + n.getNumCore()));
		        virtualHardwareSection.addItem(itemCpuNumber);

		        // CPU Speed
		        if (!virtualHardwareSection.setCPUSpeed(2000)) {
		            Item itemCpuSpeed = Item.Factory.newInstance();
		            itemCpuSpeed.setDescription("CPU Speed");
		            itemCpuSpeed.setElementName(n.getCpuFreq() + " CPU speed reservation");
		            itemCpuSpeed.setInstanceId("1");
		            itemCpuSpeed.setResourceType(ResourceType.PROCESSOR);
		            itemCpuSpeed.setResourceSubType("cpuspeed");
		            CpuSpeed cpu = OVFUtils.getCpuSpeedFormatted(n.getCpuFreq());
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
		        itemMemory.setElementName(n.getMemSize() + " of memory");
		        itemMemory.setInstanceId("2");
		        itemMemory.setResourceType(ResourceType.MEMORY);
		        StorageResource m = OVFUtils.getStorageResourceFormatted(n.getMemSize());
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
		        disk.setFormat(DiskFormatType.QCOW2);
		        StorageResource d = OVFUtils.getStorageResourceFormatted(n.getDiskSize());
		        if (d != null){
		        	disk.setCapacityAllocationUnits(d.getUnits());
			        disk.setCapacity("" + d.getCapacity());
		        }
		        diskSection.addDisk(disk);
		        
		        // File references		      
		        File file = File.Factory.newInstance(n.getName() + "-img", "/DFS/some/location/" + n.getName() + ".img");
//		        file.setSize(new BigInteger("1000000000"));
		        references.addFile(file);
	
		        virtualSystem.setVirtualHardwareSection(virtualHardwareSection);
		        virtualSystems.add(virtualSystem);	        	
	        }
	        
	        virtualSystemCollection.setVirtualSystemArray(virtualSystems
	                .toArray(new VirtualSystem[virtualSystems.size()]));

	        ovfDefinition.setReferences(references);
	        ovfDefinition.setDiskSection(diskSection);
	        ovfDefinition.setVirtualSystemCollection(virtualSystemCollection);
	        
	        System.out.println(ovfDefinition.toString());
        }
        	
	}

	private static ApplicationConfig getXml() {
    	String xmlFileTxt = "";
    	try {
    		xmlFileTxt = Utils.readFile("C:/tests/app-packager/xmlToTest.txt");
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
