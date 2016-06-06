package eu.ascetic.paas.applicationmanager.event.deployment;

import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;

import es.bsc.vmmclient.models.Vm;
import es.bsc.vmmclient.models.VmDeployed;
import eu.ascetic.paas.applicationmanager.amqp.AmqpProducer;
import eu.ascetic.paas.applicationmanager.dao.ApplicationDAO;
import eu.ascetic.paas.applicationmanager.dao.DeploymentDAO;
import eu.ascetic.paas.applicationmanager.dao.ImageDAO;
import eu.ascetic.paas.applicationmanager.dao.VMDAO;
import eu.ascetic.paas.applicationmanager.event.DeploymentEvent;
import eu.ascetic.paas.applicationmanager.model.Deployment;
import eu.ascetic.paas.applicationmanager.model.Dictionary;
import eu.ascetic.paas.applicationmanager.model.Image;
import eu.ascetic.paas.applicationmanager.model.VM;
import eu.ascetic.paas.applicationmanager.ovf.OVFUtils;
import eu.ascetic.paas.applicationmanager.ovf.VMLimits;
import eu.ascetic.paas.applicationmanager.providerregistry.PRClient;
import eu.ascetic.paas.applicationmanager.vmmanager.client.ImageUploader;
import eu.ascetic.paas.applicationmanager.vmmanager.client.VmManagerClient;
import eu.ascetic.utils.ovf.api.OvfDefinition;
import eu.ascetic.utils.ovf.api.VirtualHardwareSection;
import eu.ascetic.utils.ovf.api.VirtualSystem;
import eu.ascetic.utils.ovf.api.VirtualSystemCollection;
import eu.ascetic.utils.ovf.api.utils.OvfRuntimeException;
import reactor.event.Event;
import reactor.spring.annotation.Consumer;
import reactor.spring.annotation.Selector;

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
 * @author David Garcia Perez. Atos Research and Innovation, Atos SPAIN SA
 * e-mail david.garciaperez@atos.net 
 * 
 * This handler reacts to the event a deployment has been contextualized.
 * Everything is ready to start deploying the deployment to the selected
 * IaaS provider
 */

@Consumer
public class DeployEventHandler {
	private static Logger logger = Logger.getLogger(DeployEventHandler.class);
	@Autowired
	protected ApplicationDAO applicationDAO;
	@Autowired
	protected DeploymentDAO deploymentDAO;
	@Autowired
	protected DeploymentEventService deploymentEventService;
	@Autowired
	protected VMDAO vmDAO;
	@Autowired
	protected ImageDAO imageDAO;
	protected PRClient prClient = new PRClient();

	@Selector(value="topic.deployment.status", reactor="@rootReactor")
	public void deployDeployment(Event<DeploymentEvent> event) {

		DeploymentEvent deploymentEvent = event.getData();
		
		if(deploymentEvent.getDeploymentStatus().equals(Dictionary.APPLICATION_STATUS_CONTEXTUALIZED)) {

			// We need to know the provider information first... 
			VmManagerClient vmManagerClient = prClient.getVMMClient(deploymentEvent.getProviderId());

			// We need first to read the deployment from the DB:
			Deployment deployment = deploymentDAO.getById(deploymentEvent.getDeploymentId());
			
			if(vmManagerClient != null) {
				logger.info("Creating a new thread to deploy in infrastructure the deployment: " + deploymentEvent.getDeploymentId());
				logger.info("Connecting to the VMM: " + vmManagerClient.getURL());

				// First we change the status of the deployment proccess... 
				deployment.setStatus(Dictionary.APPLICATION_STATUS_DEPLOYING);
				deploymentDAO.update(deployment);

				// We sent the message that the deploying state starts:
				AmqpProducer.sendDeploymentDeployingMessage(deploymentEvent.getApplicationName(), deployment);

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
						String diskId = OVFUtils.getDiskId(virtualHardwareSection);

						// We find the file id and for each resource // ovfId
						String fileId = OVFUtils.getFileId(diskId, ovfDocument.getDiskSection().getDiskArray());

						// We get the images urls... // ovfHref 
						String urlImg = OVFUtils.getUrlImg(ovfDocument, fileId);

						Image image = null;

						if(OVFUtils.usesACacheImage(virtualSystem)) {
							logger.info("This virtual system uses a cache demo image");
							image = imageDAO.getDemoCacheImage(fileId, urlImg);
							if(image == null) {
								logger.info("The image was not cached, we need to uplaod first");
								image = ImageUploader.uploadImage(urlImg, fileId, true, applicationName, vmManagerClient, applicationDAO, imageDAO);
							}
						} else {
							image = ImageUploader.uploadImage(urlImg, fileId, false, applicationName, vmManagerClient, applicationDAO, imageDAO);
						}

						//Now we have the image... lets see what it is the rest to build the VM to Upload...
						String ovfVirtualSystemID = virtualSystem.getId();
						// We determine how many VMs of this type we need to create
						VMLimits vmLimits = OVFUtils.getUpperAndLowerVMlimits(virtualSystem.getProductSectionAtIndex(0));
						long minNumberVMs = vmLimits.getLowerNumberOfVMs();
						long maxNumberVMs = vmLimits.getUpperNumberOfVMs();

						//We determine if it needs a public IP/Floating IP
						boolean publicIP = false;	
						try {
							publicIP = virtualSystem.getProductSectionAtIndex(0).isAssociatePublicIp();
						} catch(NullPointerException ex) {}

						String vmName = virtualSystem.getName();
						int cpus = virtualSystem.getVirtualHardwareSection().getNumberOfVirtualCPUs();
						int ramMb = virtualSystem.getVirtualHardwareSection().getMemorySize();
						String isoPath = OVFUtils.getIsoPathFromVm(virtualSystem.getVirtualHardwareSection(), ovfDocument);
						logger.info("ISO patth: " + isoPath);
						int capacity = OVFUtils.getCapacity(ovfDocument, diskId);

						// We force to refresh the image from the DB... 
						image = imageDAO.getById(image.getId());

						for(int j = 0; j < minNumberVMs; j++) {

							logger.info(" OVF-ID: " + ovfVirtualSystemID + " #VMs: " + minNumberVMs + " Name: " + vmName + " CPU: " + cpus + " RAM: " + ramMb + " Disk capacity: " + capacity + " ISO Path: " + isoPath + " PUBLIC IP: " + publicIP);

							int suffixInt = j + 1;
							String suffix = "_" + suffixInt;
							String iso = "";
							if(isoPath != null) iso = isoPath + suffix ;

							// TOOD I need to add here the slaagreement id. 
							Vm virtMachine = new Vm(vmName + suffix, image.getProviderImageId(), cpus, ramMb, capacity, 0, iso , ovfDocument.getVirtualSystemCollection().getId(), ovfID, ""/*deployment.getSlaAgreement()*/, publicIP );

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
								// TODO I need to update this to get it from the table Agreements... 
								//vmToDB.setSlaAgreement(deployment.getSlaAgreement());
								vmToDB.setNumberVMsMax(maxNumberVMs);
								vmToDB.setNumberVMsMin(minNumberVMs);
								vmToDB.setCpuMin(cpus);
								vmToDB.setCpuActual(cpus);
								vmToDB.setCpuMax(cpus);
								vmToDB.setDiskMin(capacity);
								vmToDB.setDiskActual(capacity);
								vmToDB.setDiskMax(capacity);
								vmToDB.setRamMin(ramMb);
								vmToDB.setRamActual(ramMb);
								vmToDB.setRamMax(ramMb);
								vmToDB.setSwapMax(0);
								vmToDB.setSwapActual(0);
								vmToDB.setSwapMin(0);
								vmToDB.setProviderId("" + deploymentEvent.getProviderId());
								vmDAO.save(vmToDB);

								vmToDB.addImage(image);
								vmDAO.update(vmToDB);

								deployment.addVM(vmToDB);
								deploymentDAO.update(deployment);
								//deployment = deploymentDAO.getById(deployment.getId());

								AmqpProducer.sendVMDeployedMessage(applicationName, deployment, vmToDB);
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
				AmqpProducer.sendDeploymentErrorMessage(deploymentEvent.getApplicationName(), deployment);
			} catch (Exception ex){
				logger.info("Error triying to deploy new VMs: " + ex.getMessage());
				ex.printStackTrace();
				deployment.setStatus(Dictionary.APPLICATION_STATUS_ERROR);
				// We save the changes to the DB
				deploymentDAO.update(deployment);
				AmqpProducer.sendDeploymentErrorMessage(deploymentEvent.getApplicationName(), deployment);
			}
			
			
			deploymentEvent.setDeploymentStatus(deployment.getStatus());
			
			// We sent the message that the deployed state starts:
			AmqpProducer.sendDeploymentDeployedMessage(deploymentEvent.getApplicationName(), deployment);
					
			//We notify that the deployment has been modified
			deploymentEventService.fireDeploymentEvent(deploymentEvent);
		
		
			} else {
				logger.info("Error, not available VMM Client");
				deployment.setStatus(Dictionary.APPLICATION_STATUS_ERROR);
				// We save the changes to the DB
				deploymentDAO.update(deployment);
				AmqpProducer.sendDeploymentErrorMessage(deploymentEvent.getApplicationName(), deployment);
			}
		} 
	}
}
