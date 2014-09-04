package eu.ascetic.paas.applicationmanager.ovf;

import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;

import eu.ascetic.paas.applicationmanager.model.Deployment;
import eu.ascetic.paas.applicationmanager.vmmanager.client.VmManagerClientHC;
import eu.ascetic.paas.applicationmanager.vmmanager.datamodel.ImageToUpload;
import eu.ascetic.paas.applicationmanager.vmmanager.datamodel.Vm;
import eu.ascetic.utils.ovf.api.Disk;
import eu.ascetic.utils.ovf.api.File;
import eu.ascetic.utils.ovf.api.Item;
import eu.ascetic.utils.ovf.api.OvfDefinition;
import eu.ascetic.utils.ovf.api.VirtualHardwareSection;
import eu.ascetic.utils.ovf.api.VirtualSystem;
import eu.ascetic.utils.ovf.api.VirtualSystemCollection;
import eu.ascetic.utils.ovf.api.utils.OvfRuntimeException;

/**
 * Class that handles all the parsing of the OVF incoming from the different modules in the Application Manager
 * @author David Garcia Perez - Atos
 */
public class OVFUtils {
	private static Logger logger = Logger.getLogger(OVFUtils.class);
			
	/**
	 * Extracts the field ovf:Name from VirtualSystemCollection to differenciate between applications
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
	 * Gets the ovf definition object
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
	
	
	/**
	 * Gets the vms from ovf and upload to VM manager the images in order to be able to deploy the VMs later.
	 *
	 * @param deployment the deployment
	 * @param vmManagerClient the vm manager client
	 * @return the vms from ovf
	 */
	public static List<Vm> getVmsFromOvf(Deployment deployment, VmManagerClientHC vmManagerClient){
		List<Vm> vmList = null;
		OvfDefinition ovfDocument = getOvfDefinition(deployment.getOvf());
		if (ovfDocument != null){
			VirtualSystemCollection vsc = ovfDocument.getVirtualSystemCollection();
			int index = 0;
			vmList = new ArrayList<Vm>();
			VirtualSystem virtSystem = null;
			String appId = OVFUtils.getApplicationName(deployment.getOvf());
			while (index < vsc.getVirtualSystemArray().length){
				//Retrieve data from every VM
				virtSystem = vsc.getVirtualSystemAtIndex(index);
				int asceticUpperBound = virtSystem.getProductSectionAtIndex(0).getUpperBound();
				String vmName = virtSystem.getName();
				int cpus = virtSystem.getVirtualHardwareSection().getNumberOfVirtualCPUs();
				int ramMb = virtSystem.getVirtualHardwareSection().getMemorySize();
				int diskSize = OVFUtils.getDiskSizeFromVm(getDiskId(virtSystem.getVirtualHardwareSection()), ovfDocument);
				String isoPath = OVFUtils.getIsoPathFromVm(virtSystem.getVirtualHardwareSection(), ovfDocument);
				
				ImageToUpload imgToUpload = OVFUtils.getImageToUpload(getImgFileRefOvfDocument(virtSystem.getVirtualHardwareSection(), 
						ovfDocument));
				
				String imgId = vmManagerClient.uploadImage(imgToUpload);
				
				if (imgId != null && !imgId.equalsIgnoreCase("")){	
					//Create new VMs and add it to VM list to deploy
					if (asceticUpperBound == 1){
						//ISO names in /DFS/... ends with _1
						String suffix = "_1";
						Vm virtMachine = new Vm(vmName + suffix, imgId, cpus, ramMb, diskSize, isoPath + suffix , appId);
						vmList.add(virtMachine);	
					}
					else if (asceticUpperBound <= 0){
						logger.info("AsceticUpperBound for " + vmName + " is " + asceticUpperBound + ". No VMs will be created");
					}
					else if (asceticUpperBound > 1){
						//Create many VMs as asceticUpperBound values with different names and differents ISO files every VM
						//example: 3 VMs
						//			names: 		vm_1, vm_2, vm_3
						//			isoPath:	/DFS/myIso.iso_1, /DFS/myIso.iso_2, /DFS/myIso.iso_3
						Vm virtMachine = null;
						String suffix = "";
						int iteraction = 0;
						for (int i=0;i<asceticUpperBound;i++){
							iteraction = i+1;
							suffix = "_" + iteraction;
							virtMachine = new Vm(vmName + suffix, imgId, cpus, ramMb, diskSize, isoPath + suffix, appId);
							vmList.add(virtMachine);
						}
					}
					
				}
				
				index++;
			}
			
		}
		return vmList;
	}


	/**
	 * Gets the image to upload.
	 *
	 * @param imgFileRefOvfDocument the img file ref ovf document
	 * @return the image to upload
	 */
	private static ImageToUpload getImageToUpload(String imgFileRefOvfDocument) {
		ImageToUpload imgToUpload = null;
		if (!imgFileRefOvfDocument.equalsIgnoreCase("")){
			String name = imgFileRefOvfDocument.substring(imgFileRefOvfDocument.lastIndexOf("/")+1, imgFileRefOvfDocument.length());
			imgToUpload = new ImageToUpload(name, imgFileRefOvfDocument);
		}
		return imgToUpload;
	}


	/**
	 * Gets the disk id.
	 *
	 * @param virtHwSection the virt hw section
	 * @return the disk id
	 */
	private static String getDiskId(VirtualHardwareSection virtHwSection){
		String diskId = "";
		Item item = null;
		for (int i=0; i<virtHwSection.getItemArray().length; i++){
			item = virtHwSection.getItemAtIndex(i);
			if (item.getDescription().equalsIgnoreCase("VM Disk")){
				String list[] = item.getHostResourceArray();
				String hostResource = "";
				if (list!=null && list.length >0){
					hostResource = list[0];
					diskId = hostResource.substring(hostResource.lastIndexOf("/")+1, hostResource.length());
					return diskId;
				}				
			}
		}
		return diskId;
	}
	
	/**
	 * Gets the disk size from vm.
	 *
	 * @param diskId the disk id
	 * @param ovfDocument the ovf document
	 * @return the disk size from vm
	 */
	private static int getDiskSizeFromVm(String diskId, OvfDefinition ovfDocument){
		int diskSize = 0;
		Disk[] diskList = ovfDocument.getDiskSection().getDiskArray();
		if (diskList != null && diskList.length>0){
			Disk disk = null;
			for (int i = 0; i<diskList.length; i++){
				disk = diskList[i];
				if (disk.getDiskId().equalsIgnoreCase(diskId)){
					return Integer.parseInt(disk.getCapacity());
				}
			}
		}
		else {
			logger.debug("No disk section available in OVF!!");
		}
		return diskSize;
	}
	
	
	/**
	 * Gets the img file ref ovf document.
	 *
	 * @param virtualHardwareSection the virtual hardware section
	 * @param ovfDefinition the ovf definition
	 * @return the img file ref ovf document
	 */
	private static String getImgFileRefOvfDocument(VirtualHardwareSection virtualHardwareSection, 
			OvfDefinition ovfDefinition) {
		String urlImg = null;
		String fileId = getFileIdFromDiskId(getDiskId(virtualHardwareSection), ovfDefinition);
		File[] files = ovfDefinition.getReferences().getFileArray();
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
		
		return urlImg;
	}
	
	
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
	
	
	private static String getIsoPathFromVm(VirtualHardwareSection virtHwSection, OvfDefinition ovfDocument){
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
	
	
	
	
}
