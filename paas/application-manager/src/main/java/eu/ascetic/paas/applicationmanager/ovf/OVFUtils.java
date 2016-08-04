package eu.ascetic.paas.applicationmanager.ovf;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang.math.NumberUtils;

//import java.util.ArrayList;
//import java.util.List;

import org.apache.log4j.Logger;

import eu.ascetic.paas.applicationmanager.Dictionary;
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
	 * Extracts the field deploymentName from Product section of VirtualSystemCollection to differenciate between applications.
	 *
	 * @param ovf String representing the OVF definition of an Application
	 * @return the application name
	 */
	public static String getDeploymentName(String ovf) {
		try {
			OvfDefinition ovfDocument = OvfDefinition.Factory.newInstance(ovf);
			return ovfDocument.getVirtualSystemCollection().getProductSectionAtIndex(0).getDeploymentName();
		} catch(OvfRuntimeException ex) {
			logger.info("Error parsing OVF file: " + ex.getMessage());
			ex.printStackTrace();
			return null;
		} catch(NullPointerException ex) {
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
	
	/**
	 * Returns true if the OVF contains a VM with that specific ovf-id
	 * @param ovf Ovf to be checked
	 * @param ovfId ovf-id to see if it exits in the ovf file
	 * @return true if the ovf contains a VM with that specific ovf id, false otherwise.
	 */
	public static boolean containsVMWithThatOvfId(String ovf, String ovfId) {
		
		if(ovfId == null) {
			return false;
		}
		
		try {
			OvfDefinition ovfDocument = OvfDefinition.Factory.newInstance(ovf);
			
			VirtualSystemCollection vsc = ovfDocument.getVirtualSystemCollection();
			// We check all the Virtual Systems in the OVF file
			for(int i = 0; i < vsc.getVirtualSystemArray().length; i++) {
				VirtualSystem virtualSystem = vsc.getVirtualSystemAtIndex(i);
				String ovfVirtualSystemID = virtualSystem.getId();
				
				if(ovfId.equals(ovfVirtualSystemID)) {
					return true;
				}
			}
			
		} catch(OvfRuntimeException ex) {
			logger.info("Error parsing OVF file: " + ex.getMessage());
			ex.printStackTrace();
			return false;
		}
		
		return false;
	}
	
	public static VMLimits getUpperAndLowerVMlimits(ProductSection productSection) {
	
		VMLimits vmLimits = new VMLimits();
		
		try {
			vmLimits.setLowerNumberOfVMs(productSection.getLowerBound());
			vmLimits.setUpperNumberOfVMs(productSection.getUpperBound());
		} catch(NullPointerException ex) {
			vmLimits.setLowerNumberOfVMs(productSection.getUpperBound());
			vmLimits.setUpperNumberOfVMs(productSection.getUpperBound());
		}
		
		return vmLimits;
	}
	
	public static int getPriceSchema(ProductSection productSection) {
                return productSection.getPriceSchema();
	}
	
	/**
	 * Returns the ProductSection for an specific ovfID
	 * @param ovf String representing the ovf file where to look
	 * @param ovfId Ovf ID of the wanted product section
	 * @return Returns a ProductSection object if the sections exits or null otherwise
	 */
	public static ProductSection getProductionSectionForOvfID(String ovf, String ovfId) {
		
		VirtualSystem virtualSystem = getVirtualSystemForOvfId(ovf, ovfId);
				
		if(virtualSystem != null) {
			return virtualSystem.getProductSectionAtIndex(0);
		}
		
		return null;
	}
	
	/**
	 * Returns the VirtualSystem object representation for an specific ovf, null if does not exits
	 * @param ovf
	 * @param ovfId
	 * @return
	 */
	public static VirtualSystem getVirtualSystemForOvfId(String ovf, String ovfId) {
		OvfDefinition ovfDocument = OvfDefinition.Factory.newInstance(ovf);
		return getVirtualSystemForOvfIdNotString(ovfDocument, ovfId);
	}
	
	private static VirtualSystem getVirtualSystemForOvfIdNotString(OvfDefinition ovfDocument, String ovfId) {

		VirtualSystemCollection vsc = ovfDocument.getVirtualSystemCollection();
		
		for(int i = 0; i < vsc.getVirtualSystemArray().length; i++) {
			VirtualSystem virtualSystem = vsc.getVirtualSystemAtIndex(i);
			String ovfID = virtualSystem.getId();
			
			if(ovfID.equals(ovfId)) {
				return virtualSystem;
			}
		}
		
		return null;
	}
	
	/**
	 * Returns the disc id for an specifc virtual Hardware section
	 * @param virtualHardwareSection
	 * @return
	 */
	public static String getDiskId(VirtualHardwareSection virtualHardwareSection) {
		String diskId = "";
		
		for (int j=0; j<virtualHardwareSection.getItemArray().length; j++) {
			Item item = virtualHardwareSection.getItemAtIndex(j);
			if (item.getResourceType().getNumber() == 17){
				String list[] = item.getHostResourceArray();
				
				if (list!=null && list.length >0) {
					String hostResource = list[0];
					logger.debug("Host Resource: " + hostResource + "####");
					diskId = hostResource.substring(hostResource.lastIndexOf("/")+1, hostResource.length());
					logger.debug("Disk Id: " + diskId + "####");
				}				
			}
		}
		
		return diskId;
	}
	
	/**
	 * Determines the capacity of a Disk
	 * @param ovfDocument
	 * @param diskId
	 * @return
	 */
	public static int getCapacity(OvfDefinition ovfDocument, String diskId) {
		// This method needs to have its own unit test... 
		int capacity = 0;
		// We find the file id for each resource
		Disk[] diskList = ovfDocument.getDiskSection().getDiskArray();
		if (diskList != null && diskList.length>0) {
			for (int k = 0; k<diskList.length; k++) {
				Disk disk = diskList[k];
				if (disk.getDiskId().equalsIgnoreCase(diskId)) {
					String units = disk.getCapacityAllocationUnits();
					capacity = Integer.parseInt(disk.getCapacity());
					capacity = OVFUtils.getDiskCapacityInGb(capacity, units);
				}
			}
		}
		
		return capacity;
	}
	
	/**
	 * Returns the fileID for an specific Disk in an OVF document.
	 * @param diskId
	 * @param diskList
	 * @return
	 */
	public static String getFileId(String diskId, Disk[] diskList) {
		String fileId = "";
		
		if (diskList != null && diskList.length>0) {
			for (int k = 0; k<diskList.length; k++) {
				Disk disk = diskList[k];
				if (disk.getDiskId().equalsIgnoreCase(diskId)) {
					fileId = disk.getFileRef();
					logger.debug("Disk reference: " + fileId);
				}
			}
		}
		
		return fileId;
	}
	
	/**
	 * Returns the URL of an image in the OVF
	 * @param ovfDocument
	 * @param fileId
	 * @return
	 */
	public static String getUrlImg(OvfDefinition ovfDocument, String fileId) {
		String urlImg = "";
		
		File[] files = ovfDocument.getReferences().getFileArray();
		if (files != null && files.length>0){
			for (int j = 0; j < files.length; j++){
				File file = files[j];
				if (file.getId().equalsIgnoreCase(fileId)){
					urlImg = file.getHref();
				}
			}
		}
		else {
			logger.debug("No references section available in OVF!!");
		}
	
		return urlImg;
	}
	
	/**
	 * Returns an specific VM Guarantee
	 * @param ovfDocument from which to find the guarantee
	 * @param slaInfoTerm the name of the guarantee
	 * @param ovfId of the VM we are going to extract the info.
	 * @return null if the guarantee does not exists, if not the guarantee itself
	 */
	public static AsceticSLAInfo getVMSlaInfo(OvfDefinition ovfDocument, String slaInfoTerm, String ovfId) {
		logger.info("SLA IFNO TERM: " + slaInfoTerm + " OVF ID: " + ovfId );
		try {
			ProductSection productSection = getVirtualSystemForOvfIdNotString(ovfDocument, ovfId).getProductSectionAtIndex(0);
			//logger.info("PRODUCT SECTION " + productSection);
			return getSlaInfoInProductSection(productSection, slaInfoTerm);
		} catch(NullPointerException ex) {
			logger.error("No Product section for vm with ID: " + ovfId);
			return null;
		}
	}
	
	private static AsceticSLAInfo getSlaInfoInProductSection(ProductSection productSection, String slaInfoTerm) {
		ProductProperty propertyCount = productSection.getPropertyByKey("asceticSlaInfoNumber");
		
		if(propertyCount != null) {
			int count = Integer.parseInt(propertyCount.getValue());
			
			for(int i = 0; i < count; i++) {
				ProductProperty property = productSection.getPropertyByKey("asceticSlaInfoSlaTerm_" + i);
				
				System.out.println("####################################");
				System.out.println(property.getValue());
				
				if(slaInfoTerm.equals(property.getValue())) {
					System.out.println(property.getValue() + " " + slaInfoTerm + " counter: " + i);
					AsceticSLAInfo slaInfo = new AsceticSLAInfo();
					slaInfo.setTerm(slaInfoTerm);
					slaInfo.setBoundaryValue(productSection.getPropertyByKey("asceticSlaInfoBoundaryValue_"+ i).getValue());
					logger.info("Boundary: " + slaInfo.getBoundaryValue());
					slaInfo.setComparator(productSection.getPropertyByKey("asceticSlaInfoComparator_"+ i).getValue());
					logger.info("Comparator " + slaInfo.getComparator());
					slaInfo.setMetricUnit(productSection.getPropertyByKey("asceticSlaInfoMetricUnit_"+ i).getValue());
					logger.info("Metric Units: " + slaInfo.getMetricUnit());
					slaInfo.setType(productSection.getPropertyByKey("asceticSlaInfoSlaType_"+ i).getValue());
					logger.info("Type: " + slaInfo.getType());
					
					return slaInfo;
				}
			}
		}
		
		return null;
	}

	/**
	 * Returns an specifci App Guarantee
	 * @param ovfDocument from which to find the guarantee
	 * @param slaInfoTerm the name of the guarantee
	 * @return null if the guarantee does not exists, if not the guarantee itself
	 */
	public static AsceticSLAInfo getAppSlaInfo(OvfDefinition ovfDocument, String slaInfoTerm) {
		ProductSection productSection = ovfDocument.getVirtualSystemCollection().getProductSectionAtIndex(0);
		
		return getSlaInfoInProductSection(productSection, slaInfoTerm);
	}

	public static List<AsceticTermMeasurement> getVMTermMeasurement(OvfDefinition ovfDocument, String ovfId) {
		List<AsceticTermMeasurement> termMeasurements = new ArrayList<AsceticTermMeasurement>();
		
		try {
			ProductSection productSection = getVirtualSystemForOvfIdNotString(ovfDocument, ovfId).getProductSectionAtIndex(0);
			
			ProductProperty propertyCount = productSection.getPropertyByKey("asceticTermMeasurementNumber");
			
			if(propertyCount != null) {
				int count = Integer.parseInt(propertyCount.getValue());
				
				for(int i = 0; i < count; i++) {
					AsceticTermMeasurement termMeasurement = new AsceticTermMeasurement();
					
					termMeasurement.setEvent(productSection.getPropertyByKey("asceticTermMeasurementApplicationEvent_" + i).getValue());
					termMeasurement.setMetric(productSection.getPropertyByKey("asceticTermMeasurementApplicationMetric_" + i).getValue());
					termMeasurement.setAggregator(productSection.getPropertyByKey("asceticTermMeasurementAggregator_" + i).getValue());
					
					String period = productSection.getPropertyByKey("asceticTermMeasurementPeriod_" + i).getValue();
					if(NumberUtils.isNumber(period)) {
						termMeasurement.setPeriod(new Integer(Integer.parseInt(period)));
					}
					
					String param = productSection.getPropertyByKey("asceticTermMeasurementAggregatorParams_" + i).getValue();
					if(NumberUtils.isNumber(param)) {
						termMeasurement.setParams(new Integer(Integer.parseInt(param)));
					}
					
					String boundaryValue = productSection.getPropertyByKey("asceticTermMeasurementBoundaryValue_" + i).getValue();
					if(NumberUtils.isNumber(boundaryValue)) {
						termMeasurement.setBoundary(new Double(Double.parseDouble(boundaryValue)));
					}
						
					termMeasurements.add(termMeasurement);
				}
			}
			
		} catch(NullPointerException ex) {
			logger.error("No Product section for vm with ID: " + ovfId);
		}
		
		return termMeasurements;
	}

	public static List<String> getOVFVMIds(OvfDefinition ovfDocument) {
		List<String> ids = new ArrayList<String>();
		
		VirtualSystemCollection vsc = ovfDocument.getVirtualSystemCollection();

		for(int i = 0; i < vsc.getVirtualSystemArray().length; i++) {
			VirtualSystem virtualSystem = vsc.getVirtualSystemAtIndex(i);
			ids.add(virtualSystem.getId());
		}
		
		return ids;
	}
}