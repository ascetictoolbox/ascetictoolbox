package eu.ascetic.paas.applicationmanager.scheduler;

import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;

import eu.ascetic.paas.applicationmanager.contextualizer.VmcClient;
import eu.ascetic.paas.applicationmanager.dao.ApplicationDAO;
import eu.ascetic.paas.applicationmanager.dao.DeploymentDAO;
import eu.ascetic.paas.applicationmanager.dao.ImageDAO;
import eu.ascetic.paas.applicationmanager.dao.VMDAO;
import eu.ascetic.paas.applicationmanager.model.Application;
import eu.ascetic.paas.applicationmanager.model.Deployment;
import eu.ascetic.paas.applicationmanager.model.Dictionary;
import eu.ascetic.paas.applicationmanager.model.Image;
import eu.ascetic.paas.applicationmanager.model.VM;
import eu.ascetic.paas.applicationmanager.ovf.OVFUtils;
import eu.ascetic.paas.applicationmanager.vmmanager.client.VmManagerClient;
import eu.ascetic.paas.applicationmanager.vmmanager.client.VmManagerClientHC;
import eu.ascetic.paas.applicationmanager.vmmanager.datamodel.ImageToUpload;
import eu.ascetic.paas.applicationmanager.vmmanager.datamodel.Vm;
import eu.ascetic.paas.applicationmanager.vmmanager.datamodel.VmDeployed;
import eu.ascetic.utils.ovf.api.Disk;
import eu.ascetic.utils.ovf.api.File;
import eu.ascetic.utils.ovf.api.Item;
import eu.ascetic.utils.ovf.api.OvfDefinition;
import eu.ascetic.utils.ovf.api.VirtualHardwareSection;
import eu.ascetic.utils.ovf.api.VirtualSystem;
import eu.ascetic.utils.ovf.api.VirtualSystemCollection;
import eu.ascetic.utils.ovf.api.utils.OvfRuntimeException;
import eu.ascetic.vmc.api.core.ProgressException;
import eu.ascetic.vmc.api.datamodel.ProgressData;

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
 * This periodical taks will go through the deployments table, and look the ones that need some update
 * to be moved to a new state
 *
 */

public class DeploymentsStatusTask {
	private static Logger logger = Logger.getLogger(DeploymentsStatusTask.class);
	
	@Autowired
	protected ApplicationDAO applicationDAO;
	@Autowired
	protected DeploymentDAO deploymentDAO;
	@Autowired
	protected VMDAO vmDAO;
	@Autowired
	protected ImageDAO imageDAO;
	protected VmManagerClient vmManagerClient = new VmManagerClientHC();
	
	//@Scheduled(cron="${check.deployments.status}")
	@Scheduled(fixedDelay=5000)
	public void checkDeploymentStatus() {
		logger.info("Retrieving application deployments from DB to see if it is necessary to move applications to other state");
		
		List<Deployment> deployments = deploymentDAO.getAll();
		
		for(Deployment deployment : deployments) {
			logger.info("Checking deployment id: " + deployment.getId() + " Status: " + deployment.getStatus());
			
			if(deployment.getStatus().equals(Dictionary.APPLICATION_STATUS_CONTEXTUALIZED)) {
				logger.info(" The deployment is ready to be deployed " + deployment.getId() + " deploying it!!!");
				
				deploymentDeployApplicationActions(deployment);
			}
		}
	}
	
	/**
	 * Actions to deploy an application
	 * @param deployment
	 */
	protected void deploymentDeployApplicationActions(Deployment deployment) {
		// First we change the status of the deployment proccess... 
		deployment.setStatus(Dictionary.APPLICATION_STATUS_DEPLOYING);
		deploymentDAO.update(deployment);
		
		try {
			OvfDefinition ovfDocument = OVFUtils.getOvfDefinition(deployment.getOvf());
			String applicationName = OVFUtils.getApplicationName(deployment.getOvf());
			VirtualSystemCollection vsc = ovfDocument.getVirtualSystemCollection();
			
			// We check all the Virtual Systems in the OVF file
			for(int i = 0; i < vsc.getVirtualSystemArray().length; i++) {
				VirtualSystem virtualSystem = vsc.getVirtualSystemAtIndex(i);
				String ovfID = virtualSystem.getId();
				
				logger.info(" Starting to deploy Virtual System: " + virtualSystem.getName());
				
				VirtualHardwareSection virtualHardwareSection = virtualSystem.getVirtualHardwareSection();
				
				// We find the disk id for each resource... 
				String diskId = getDiskId(virtualHardwareSection);

				// We find the file id and for each resource // ovfId
				String fileId = getFileId(diskId, ovfDocument.getDiskSection().getDiskArray());
				
				// We get the images urls... // ovfHref 
				String urlImg = getUrlImg(ovfDocument, fileId);

				Image image = null;
				
				if(OVFUtils.usesACacheImage(virtualSystem)) {
					logger.info("This virtual system uses a cache demo image");
					image = imageDAO.getDemoCacheImage(fileId, urlImg);
					if(image == null) {
						logger.info("The image was not cached, we need to uplaod first");
						image = uploadImage(urlImg, fileId, true, applicationName);
					}
				} else {
					image = uploadImage(urlImg, fileId, false, applicationName);
				}

				//Now we have the image... lets see what it is the rest to build the VM to Upload...
				String ovfVirtualSystemID = virtualSystem.getId();
				int asceticUpperBound = virtualSystem.getProductSectionAtIndex(0).getUpperBound();
				
				String vmName = virtualSystem.getName();
				int cpus = virtualSystem.getVirtualHardwareSection().getNumberOfVirtualCPUs();
				int ramMb = virtualSystem.getVirtualHardwareSection().getMemorySize();
				String isoPath = OVFUtils.getIsoPathFromVm(virtualSystem.getVirtualHardwareSection(), ovfDocument);
				int capacity = getCapacity(ovfDocument, diskId);
				
				// We force to refresh the image from the DB... 
				image = imageDAO.getById(image.getId());
				
				for(int j = 0; j < asceticUpperBound; j++) {

					logger.debug(" OVF-ID: " + ovfVirtualSystemID + " #VMs: " + asceticUpperBound + " Name: " + vmName + " CPU: " + cpus + " RAM: " + ramMb + " Disk capacity: " + capacity + " ISO Path: " + isoPath);

					int suffixInt = j + 1;
					String suffix = "_" + suffixInt;
					String iso = "";
					if(isoPath != null) iso = isoPath + suffix ;
					
					Vm virtMachine = new Vm(vmName + suffix, image.getProviderImageId(), cpus, ramMb, capacity, iso , ovfDocument.getVirtualSystemCollection().getId() );
					virtMachine.setOvfId(ovfID);
					logger.debug("virtMachine: " + virtMachine);
					
					List<Vm> vms = new ArrayList<Vm>();
					vms.add(virtMachine);
					List<String> vmIds = vmManagerClient.deployVMs(vms);
					logger.debug("Id: " + vmIds.get(0));
					
					for(String id : vmIds) {
						VmDeployed vmDeployed = vmManagerClient.getVM(id);
						
						VM vmToDB = new VM();
						vmToDB.setIp(vmDeployed.getIpAddress());
						vmToDB.setOvfId(ovfVirtualSystemID);
						vmToDB.setStatus(vmDeployed.getState());
						vmToDB.setProviderVmId(id);
						vmDAO.save(vmToDB);
						
						vmToDB.addImage(image);
						vmDAO.update(vmToDB);
						
						deployment.addVM(vmToDB);
						deploymentDAO.update(deployment);
						//deployment = deploymentDAO.getById(deployment.getId());
					}
				}
			}
			
			deployment.setStatus(Dictionary.APPLICATION_STATUS_DEPLOYED);
			// We save the changes to the DB
			deploymentDAO.update(deployment);
			
		} catch(OvfRuntimeException ex) {
			logger.info("Error parsing OVF file: " + ex.getMessage());
			ex.printStackTrace();
			deployment.setStatus(Dictionary.APPLICATION_STATUS_ERROR);
			// We save the changes to the DB
			deploymentDAO.update(deployment);
		} catch (Exception ex){
			logger.info("Error triying to deploy new VMs: " + ex.getMessage());
			ex.printStackTrace();
			deployment.setStatus(Dictionary.APPLICATION_STATUS_ERROR);
			// We save the changes to the DB
			deploymentDAO.update(deployment);
		}
	}
	
	private Image uploadImage(String urlImg, String fileId, boolean demo, String applicationName) {
		String name = urlImg.substring(urlImg.lastIndexOf("/")+1, urlImg.length());
		
		ImageToUpload imgToUpload = new ImageToUpload(name, urlImg);
		logger.info("Image to upload name: '" + imgToUpload.getName() + "' url '" + imgToUpload.getUrl() + "'");
		
		String imageProviderId = vmManagerClient.uploadImage(imgToUpload);
		logger.info("Provider image id: " + imageProviderId);
		
		//Saving the new image to the database
		Image image = new Image();
		image.setProviderImageId(imageProviderId);
		image.setOvfHref(urlImg);
		image.setDemo(false);
		image.setOvfId(fileId);
		image.setDemo(demo);
		
		imageDAO.save(image);
		logger.info("Image storaged to the DB: id: " + image.getId() + ", ovf-id: " + image.getOvfId() + ", ovf-href: " + image.getOvfHref() 
				                                 + ", provider-image-id: " + image.getProviderImageId() + ", is demo?: " + image.isDemo());
		
		logger.debug("#### applicationName: " + applicationName);
		
		Application application = applicationDAO.getByName(applicationName);
		logger.debug("#### applicationName: " + application);
		if(application != null) {
			logger.debug("#### applicationName: <-->");
			application.addImage(image);
			boolean x = applicationDAO.update(application);
			logger.debug("#### applicationName: " + x);
		}
		
		return image;
	}
	
	private int getCapacity(OvfDefinition ovfDocument, String diskId) {
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
	
	private String getUrlImg(OvfDefinition ovfDocument, String fileId) {
		// TODO move this method to OVFUtils and create a test
		String urlImg = "";
		
		File[] files = ovfDocument.getReferences().getFileArray();
		if (files != null && files.length>0){
			for (int j = 0; j < files.length; j++){
				File file = files[j];
				if (file.getId().equalsIgnoreCase(fileId)){
					urlImg = file.getHref();
					System.out.println("URL to image: " + urlImg);
				}
			}
		}
		else {
			System.out.println("No references section available in OVF!!");
		}
	
		return urlImg;
	}
	
	private String getFileId(String diskId, Disk[] diskList) {
		// TODO move this method to OVFUtils and create a test
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
	
	private String getDiskId(VirtualHardwareSection virtualHardwareSection) {
		// TODO move this method to OVFUtils and create a test
		String diskId = "";
		
		for (int j=0; j<virtualHardwareSection.getItemArray().length; j++) {
			Item item = virtualHardwareSection.getItemAtIndex(j);
			if (item.getResourceType().getNumber() == 17){
				String list[] = item.getHostResourceArray();
				
				if (list!=null && list.length >0) {
					String hostResource = list[0];
					logger.debug("Host Resource: " + hostResource);
					diskId = hostResource.substring(hostResource.lastIndexOf("/")+1, hostResource.length());
					logger.debug("Disk Id: " + diskId);
				}				
			}
		}
		
		return diskId;
	}
}
