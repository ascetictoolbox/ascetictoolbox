package eu.ascetic.saas.applicationpackager.ovf;

//import java.util.ArrayList;
//import java.util.List;

import org.apache.log4j.Logger;

import eu.ascetic.saas.applicationpackager.Dictionary;
import eu.ascetic.saas.applicationpackager.xml.model.CpuSpeed;
import eu.ascetic.saas.applicationpackager.xml.model.StorageResource;
//import eu.ascetic.paas.applicationmanager.model.Deployment;
//import eu.ascetic.paas.applicationmanager.vmmanager.client.VmManagerClientHC2;
//import eu.ascetic.paas.applicationmanager.vmmanager.datamodel.ImageToUpload;
//import eu.ascetic.paas.applicationmanager.vmmanager.datamodel.Vm;
import eu.ascetic.utils.ovf.api.Disk;
import eu.ascetic.utils.ovf.api.File;
import eu.ascetic.utils.ovf.api.Item;
import eu.ascetic.utils.ovf.api.OvfDefinition;
import eu.ascetic.utils.ovf.api.ProductProperty;
import eu.ascetic.utils.ovf.api.ProductSection;
import eu.ascetic.utils.ovf.api.VirtualHardwareSection;
import eu.ascetic.utils.ovf.api.VirtualSystem;
import eu.ascetic.utils.ovf.api.VirtualSystemCollection;
//import eu.ascetic.utils.ovf.api.VirtualSystem;
//import eu.ascetic.utils.ovf.api.VirtualSystemCollection;
import eu.ascetic.utils.ovf.api.utils.OvfRuntimeException;

/**
 * 
 * Copyright 2014 ATOS SPAIN S.A. 
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
 * @author David Garcia Perez. Atos Research and Innovation, Atos SPAIN SA
 * e-mail david.garciaperez@atos.net 
 * 
 * Class that handles all the parsing of the OVF incoming from the different modules in the Application Manager.
 *
 */

public class OVFUtils {
	
	/** The logger. */
	private static Logger logger = Logger.getLogger(OVFUtils.class);
			
	/**
	 * Extracts the field ovf:Name from VirtualSystemCollection to differenciate between applications.
	 *
	 * @param ovf String representing the OVF definition of an Application
	 * @return the application name
	 */
	public static String getApplicationName(String ovf) {
		
		try {
			OvfDefinition ovfDocument = OvfDefinition.Factory.newInstance(ovf);
			return ovfDocument.getVirtualSystemCollection().getId();
		} catch(OvfRuntimeException ex) {
			logger.info("Error parsing OVF file: " + ex.getMessage());
			ex.printStackTrace();
			return null;
		}
	}
	
	
	/**
	 * Gets the ovf definition object.
	 *
	 * @param ovf the ovf
	 * @return the ovf definition
	 */
	public static OvfDefinition getOvfDefinition(String ovf){
		try {
			OvfDefinition ovfDocument = OvfDefinition.Factory.newInstance(ovf);
			return ovfDocument;
		} catch(OvfRuntimeException ex) {
			logger.info("Error parsing OVF file: " + ex.getMessage());
			ex.printStackTrace();
			return null;
		}
	}
	
	
	public static String removeSoftwareDependencyElements(String ovf) {
		if (ovf != null){
			try {
				OvfDefinition ovfDocument = OvfDefinition.Factory.newInstance(ovf);
				if (ovfDocument != null){
					VirtualSystemCollection vsc = ovfDocument.getVirtualSystemCollection();
					VirtualSystem[] vsList = vsc.getVirtualSystemArray();
					for (VirtualSystem vs : vsList){
						ProductSection[] psList = vs.getProductSectionArray();
						for (ProductSection ps : psList){
							int num = ps.getSoftwareDependencyNumber();
							for (int i=0; i<num; i++){
								ps.removeSoftwareDependencyProperties(i);
							}
							ps.removePropertyByKey("asceticSoftwareDependencyNumber");
//							ps.removeSoftwareDependencyProperties(index);ps.getSoftwareDependency
//							addSoftwareDependencyProperties
						}
					}
				}
				return ovfDocument.toString();
			} catch(OvfRuntimeException ex) {
				logger.info("Error parsing OVF file: " + ex.getMessage());
				ex.printStackTrace();
				return null;
			}
		}
		return ovf;
	}
	
	
//	/**
//	 * Gets the vms from ovf and upload to VM manager the images in order to be able to deploy the VMs later.
//	 *
//	 * @param deployment the deployment
//	 * @param vmManagerClient the vm manager client
//	 * @return the vms from ovf
//	 */
//	public static List<Vm> getVmsFromOvf(Deployment deployment, VmManagerClientHC2 vmManagerClient){
//		List<Vm> vmList = null;
//		OvfDefinition ovfDocument = getOvfDefinition(deployment.getOvf());
//		if (ovfDocument != null){
//			VirtualSystemCollection vsc = ovfDocument.getVirtualSystemCollection();
//			int index = 0;
//			vmList = new ArrayList<Vm>();
//			VirtualSystem virtSystem = null;
//			String appId = OVFUtils.getApplicationName(deployment.getOvf());
//			while (index < vsc.getVirtualSystemArray().length){
//				//Retrieve data from every VM
//				virtSystem = vsc.getVirtualSystemAtIndex(index);
//				String ovfID = virtSystem.getId();
//				
//				int asceticUpperBound = virtSystem.getProductSectionAtIndex(0).getUpperBound();
//				
//				String vmName = virtSystem.getName();
//				int cpus = virtSystem.getVirtualHardwareSection().getNumberOfVirtualCPUs();
//				int ramMb = virtSystem.getVirtualHardwareSection().getMemorySize();
//				int diskSize = OVFUtils.getDiskSizeFromVm(getDiskId(virtSystem.getVirtualHardwareSection()), ovfDocument);
//				String isoPath = OVFUtils.getIsoPathFromVm(virtSystem.getVirtualHardwareSection(), ovfDocument);
//				
//				ImageToUpload imgToUpload = OVFUtils.getImageToUpload(getImgFileRefOvfDocument(virtSystem.getVirtualHardwareSection(), 
//						ovfDocument));
//				
//				String imgId = vmManagerClient.uploadImage(imgToUpload);
//				
//				if (imgId != null && !imgId.equalsIgnoreCase("")){	
//					//Create new VMs and add it to VM list to deploy
//					if (asceticUpperBound == 1){
//						//ISO names in /DFS/... ends with _1
//						String suffix = "_1";
//						Vm virtMachine = new Vm(vmName + suffix, imgId, cpus, ramMb, diskSize, isoPath + suffix , appId);
//						virtMachine.setOvfId(ovfID);
//						logger.debug("ADDING NEW VM TO THE LIST: ovf-id: " + virtMachine.getOvfId() + " name: " + virtMachine.getName());
//						vmList.add(virtMachine);	
//					}
//					else if (asceticUpperBound <= 0){
//						logger.info("AsceticUpperBound for " + vmName + " is " + asceticUpperBound + ". No VMs will be created");
//					}
//					else if (asceticUpperBound > 1){
//						//Create many VMs as asceticUpperBound values with different names and differents ISO files every VM
//						//example: 3 VMs
//						//			names: 		vm_1, vm_2, vm_3
//						//			isoPath:	/DFS/myIso.iso_1, /DFS/myIso.iso_2, /DFS/myIso.iso_3
//						Vm virtMachine = null;
//						String suffix = "";
//						int iteraction = 0;
//						for (int i=0;i<asceticUpperBound;i++){
//							iteraction = i+1;
//							suffix = "_" + iteraction;
//							virtMachine = new Vm(vmName + suffix, imgId, cpus, ramMb, diskSize, isoPath + suffix, appId);
//							vmList.add(virtMachine);
//						}
//					}
//					
//				}
//				
//				index++;
//			}
//			
//		}
//		return vmList;
//	}


//	/**
//	 * Gets the image to upload.
//	 *
//	 * @param imgFileRefOvfDocument the img file ref ovf document
//	 * @return the image to upload
//	 */
//	private static ImageToUpload getImageToUpload(String imgFileRefOvfDocument) {
//		ImageToUpload imgToUpload = null;
//		if (!imgFileRefOvfDocument.equalsIgnoreCase("")){
//			String name = imgFileRefOvfDocument.substring(imgFileRefOvfDocument.lastIndexOf("/")+1, imgFileRefOvfDocument.length());
//			imgToUpload = new ImageToUpload(name, imgFileRefOvfDocument);
//		}
//		return imgToUpload;
//	}


//	/**
//	 * Gets the disk id.
//	 *
//	 * @param virtHwSection the virt hw section
//	 * @return the disk id
//	 */
//	private static String getDiskId(VirtualHardwareSection virtHwSection){
//		String diskId = "";
//		Item item = null;
//		for (int i=0; i<virtHwSection.getItemArray().length; i++){
//			item = virtHwSection.getItemAtIndex(i);
//			if (item.getResourceType().getNumber() == 17){
//				String list[] = item.getHostResourceArray();
//				String hostResource = "";
//				if (list!=null && list.length >0){
//					hostResource = list[0];
//					diskId = hostResource.substring(hostResource.lastIndexOf("/")+1, hostResource.length());
//					return diskId;
//				}				
//			}
//		}
//		return diskId;
//	}
	
//	/**
//	 * Gets the disk size from vm.
//	 *
//	 * @param diskId the disk id
//	 * @param ovfDocument the ovf document
//	 * @return the disk size from vm
//	 */
//	private static int getDiskSizeFromVm(String diskId, OvfDefinition ovfDocument){
//		int diskSize = 0;
//		Disk[] diskList = ovfDocument.getDiskSection().getDiskArray();
//		if (diskList != null && diskList.length>0){
//			Disk disk = null;
//			for (int i = 0; i<diskList.length; i++){
//				disk = diskList[i];
//				if (disk.getDiskId().equalsIgnoreCase(diskId)){
//					String units = disk.getCapacityAllocationUnits();
//					int capacity = Integer.parseInt(disk.getCapacity());
//					return getDiskCapacityInGb(capacity, units);
//				}
//			}
//		}
//		else {
//			logger.debug("No disk section available in OVF!!");
//		}
//		return diskSize;
//	}
	
	
	/**
	 * Gets the disk capacity in gb with the info retrieved from OVF.
	 *
	 * @param capacity the capacity
	 * @param units the units
	 * @return the disk capacity in gb
	 */
	public static int getDiskCapacityInGb(int capacity, String units) {
		int diskCapacity = 0;
		if (capacity > 0) {
			if (units.equalsIgnoreCase(Dictionary.DISK_SIZE_UNIT_GBYTE)){
				//capacity specified in GBytes
				diskCapacity = capacity;
			}
			else if (units.equalsIgnoreCase(Dictionary.DISK_SIZE_UNIT_MBYTE)){
				//capacity specified in MBytes				
				diskCapacity = capacity / 1024;			
			}
			else if (units.equalsIgnoreCase(Dictionary.DISK_SIZE_UNIT_KBYTE)){
				//capacity specified in KBytes
				diskCapacity = capacity / 1048576;				
			}
		}
		return diskCapacity;
	}
	
	
//	/**
//	 * Gets the img file ref ovf document.
//	 *
//	 * @param virtualHardwareSection the virtual hardware section
//	 * @param ovfDefinition the ovf definition
//	 * @return the img file ref ovf document
//	 */
//	private static String getImgFileRefOvfDocument(VirtualHardwareSection virtualHardwareSection, 
//			OvfDefinition ovfDefinition) {
//		String urlImg = null;
//		String fileId = getFileIdFromDiskId(getDiskId(virtualHardwareSection), ovfDefinition);
//		File[] files = ovfDefinition.getReferences().getFileArray();
//		if (files != null && files.length>0){
//			File file = null;
//			for (int i = 0; i<files.length; i++){
//				file = files[i];
//				if (file.getId().equalsIgnoreCase(fileId)){
//					return file.getHref();
//				}
//			}
//		}
//		else {
//			logger.debug("No references section available in OVF!!");
//		}
//		
//		return urlImg;
//	}
	
	
	/**
	 * Gets the file id from disk id.
	 *
	 * @param diskId the disk id
	 * @param ovfDocument the ovf document
	 * @return the file id from disk id
	 */
	private static String getFileIdFromDiskId(String diskId, OvfDefinition ovfDocument){
		String fileId = null;
		if (!diskId.equalsIgnoreCase("")){
			Disk[] diskList = ovfDocument.getDiskSection().getDiskArray();
			if (diskList != null && diskList.length>0){
				Disk disk = null;
				for (int i = 0; i<diskList.length; i++){
					disk = diskList[i];
					if (disk.getDiskId().equalsIgnoreCase(diskId)){
						return disk.getFileRef();
					}
				}
			}
			else {
				logger.debug("No disk section available in OVF!!");
			}
		}
		return fileId;
	}
	
	
	/**
	 * Gets the iso id.
	 *
	 * @param virtHwSection the virt hw section
	 * @return the iso id
	 */
	private static String getIsoId(VirtualHardwareSection virtHwSection){
		String isoId = "";
		Item item = null;
		for (int i=0; i<virtHwSection.getItemArray().length; i++){
			item = virtHwSection.getItemAtIndex(i);
			if (item.getDescription().equalsIgnoreCase("VM CDROM")){
				String list[] = item.getHostResourceArray();
				String hostResource = "";
				if (list!=null && list.length >0){
					hostResource = list[0];
					isoId = hostResource.substring(hostResource.lastIndexOf("/")+1, hostResource.length());
					return isoId;
				}				
			}
		}
		return isoId;
	}
	
	
	/**
	 * Gets the iso path from vm.
	 *
	 * @param virtHwSection the virt hw section
	 * @param ovfDocument the ovf document
	 * @return the iso path from vm
	 */
	public static String getIsoPathFromVm(VirtualHardwareSection virtHwSection, OvfDefinition ovfDocument){
		String isoPath = null;
		String fileId = getFileIdFromDiskId(getIsoId(virtHwSection), ovfDocument);
		File[] files = ovfDocument.getReferences().getFileArray();
		if (files != null && files.length>0){
			File file = null;
			for (int i = 0; i<files.length; i++){
				file = files[i];
				if (file.getId().equalsIgnoreCase(fileId)){
					return file.getHref();
				}
			}
		}
		else {
			logger.debug("No references section available in OVF!!");
		}
		
		return isoPath;
	}


	public static boolean usesACacheImage(VirtualSystem virtualSystem1) {
		ProductProperty[] productPropertyArray = virtualSystem1.getProductSectionAtIndex(0).getPropertyArray();
		
		for(int i = 0; i < productPropertyArray.length; i++ ) {
			ProductProperty productProperty = productPropertyArray[i];
			if(productProperty.getKey().equals("asceticCacheImage")) {
				if(productProperty.getValue().equals("1")) {
					return true;
				}
			}
		}
		
		return false;
	}
	
	
	public static StorageResource getStorageResourceFormatted(String strStorageResourceSize){
		StorageResource storageResource = null;
		int index = 0;
		boolean stop = false;
		while (!stop && index<strStorageResourceSize.length()-1){
//			if ((!Character.isDigit(strStorageResourceSize.charAt(index)) && strStorageResourceSize.charAt(index) != '.')){
			if (!Character.isDigit(strStorageResourceSize.charAt(index))){
				stop = true;
			}	
			else {
				index++;
			}
		}
		
		if (stop){
			storageResource = new StorageResource();
			storageResource.setCapacity(Integer.parseInt(strStorageResourceSize.substring(0, index)));
			storageResource.setUnits(getStorageUnitsInOvfFormat(strStorageResourceSize.substring(index,strStorageResourceSize.length())));		
		}		
		return storageResource;
	}

	
	public static String getStorageUnitsInOvfFormat(String units) {
		String ovfCapacity = "";

		if (units.equalsIgnoreCase(Dictionary.GB)){
			//capacity specified in GBytes
			ovfCapacity = Dictionary.DISK_SIZE_UNIT_GBYTE;
		}
		else if (units.equalsIgnoreCase(Dictionary.MB)){
			//capacity specified in MBytes				
			ovfCapacity = Dictionary.DISK_SIZE_UNIT_MBYTE;			
		}

		return ovfCapacity;
	}
	
	public static String getCpuSpeedUnitsInOvfFormat(String units) {
		String ovfCapacity = "";

		if (units.equalsIgnoreCase(Dictionary.GHZ)){
			//capacity specified in GHz
			ovfCapacity = Dictionary.CPU_SPEED_UNIT_GHZ;
		}
		else if (units.equalsIgnoreCase(Dictionary.MHZ)){
			//capacity specified in MHz				
			ovfCapacity = Dictionary.CPU_SPEED_UNIT_MHZ;			
		}

		return ovfCapacity;
	}
	
	public static CpuSpeed getCpuSpeedFormatted(String strCpuSpeed){
		CpuSpeed cpuSpeed = null;
		int index = 0;
		boolean stop = false;
		while (!stop && index<strCpuSpeed.length()-1){
			if ((!Character.isDigit(strCpuSpeed.charAt(index)) && strCpuSpeed.charAt(index) != '.')){
				stop = true;
			}	
			else {
				index++;
			}
		}
		
		if (stop){
			cpuSpeed = new CpuSpeed();
			cpuSpeed.setSpeed(strCpuSpeed.substring(0, index));
			cpuSpeed.setAllocationUnits(getCpuSpeedUnitsInOvfFormat(strCpuSpeed.substring(index,strCpuSpeed.length())));		
			if (cpuSpeed.getSpeed().contains(".")){
				if (cpuSpeed.getAllocationUnits().equalsIgnoreCase(Dictionary.CPU_SPEED_UNIT_GHZ)){
					cpuSpeed.setSpeed(Math.round((Float.parseFloat(cpuSpeed.getSpeed()) * 1000)) + "");
					cpuSpeed.setAllocationUnits(Dictionary.CPU_SPEED_UNIT_MHZ);
				}
			}
		}		
		return cpuSpeed;
	}
}
