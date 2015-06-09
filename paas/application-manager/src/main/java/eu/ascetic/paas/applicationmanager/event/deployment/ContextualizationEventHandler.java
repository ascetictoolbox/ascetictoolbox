package eu.ascetic.paas.applicationmanager.event.deployment;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;

import eu.ascetic.paas.applicationmanager.amqp.AmqpProducer;
import eu.ascetic.paas.applicationmanager.contextualizer.VmcClient;
import eu.ascetic.paas.applicationmanager.dao.DeploymentDAO;
import eu.ascetic.paas.applicationmanager.event.DeploymentEvent;
import eu.ascetic.paas.applicationmanager.model.Deployment;
import eu.ascetic.paas.applicationmanager.model.Dictionary;
import eu.ascetic.paas.applicationmanager.ovf.OVFUtils;
import eu.ascetic.utils.ovf.api.OvfDefinition;
import eu.ascetic.utils.ovf.api.VirtualSystemCollection;
import eu.ascetic.vmc.api.core.ProgressException;
import eu.ascetic.vmc.api.datamodel.ProgressData;
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
 * This class reacts to the event that a deployment has been Negotiated and 
 * the contract accept by the user. After this happens, the Contextualization process
 * starts in the selected provider.
 */

@Consumer
public class ContextualizationEventHandler {
	private static Logger logger = Logger.getLogger(ContextualizationEventHandler.class);
	
	@Autowired
	protected DeploymentDAO deploymentDAO;
	@Autowired
	protected DeploymentEventService deploymentEventService;

	@Selector(value="topic.deployment.status", reactor="@rootReactor")
	public void contextualizeImagesOfADeployment(Event<DeploymentEvent> event) {
		DeploymentEvent deploymentEvent = event.getData();

		if(deploymentEvent.getDeploymentStatus().equals(Dictionary.APPLICATION_STATUS_CONTEXTUALIZATION)) {
			logger.info(" Moving deployment id: " + deploymentEvent.getDeploymentId()  + " to " + Dictionary.APPLICATION_STATUS_CONTEXTUALIZING + " state");
			
			// We need first to read the deployment from the DB:
			Deployment deployment = deploymentDAO.getById(deploymentEvent.getDeploymentId());
			
            // Since we are not doing this right now, we move the application to the next step
            deployment.setStatus(Dictionary.APPLICATION_STATUS_CONTEXTUALIZING);
			
			// We save the changes to the DB
			deploymentDAO.update(deployment);
			
			// We sent the message that the negottiating state starts:
			AmqpProducer.sendDeploymentContextualizingMessage(deploymentEvent.getApplicationName(), deployment);
			
			OvfDefinition ovf = OVFUtils.getOvfDefinition(deployment.getOvf());
			
			if (ovf != null){
				try {
					
					logger.debug(" Creating the VM Client");
					VmcClient vmcClient = new VmcClient(ovf);
					
					if (vmcClient.getVmcClient() != null) {
						logger.debug(" VM Contextualizer successfully created");
						//only continue with process if vm contextualizer connection has been successful
						
						ProgressData progressData = null;
						
						//Set the deployment in contextualizing status in DB
						deployment.setStatus(Dictionary.APPLICATION_STATUS_CONTEXTUALIZING);
						VirtualSystemCollection vsc = ovf.getVirtualSystemCollection();
						vsc.getProductSectionAtIndex(0).setDeploymentId("" + deployment.getId());
						deployment.setOvf(ovf.toString());
						deploymentDAO.update(deployment);
						
						// Creating the service
						vmcClient.getVmcClient().contextualizeService(ovf);
						
						// Wait until the service has been registered with the VMC before
			            // polling the progress data...
			            while (true) {
			                try {
			                    logger.info("TEST: Trying to fetch progress data...");

			                    vmcClient.getVmcClient().contextualizeServiceCallback(ovf.getVirtualSystemCollection().getId());
			                    logger.info("TEST: No ProgressException...");
			                    break;
			                } catch (ProgressException e) {
			                	logger.warn("TEST: Caught ProgressException due to: "
			                            + e.getMessage());
			                    Thread.sleep(250);
			                }
			            }
			            
			            // Poll the progress data until the completion...
			            while (true) {

			                // We have progress data, do something with it...
			                progressData = vmcClient.getVmcClient().contextualizeServiceCallback(ovf.getVirtualSystemCollection().getId());
			                logger.debug(" Trying to get the progress of the contextualization. Progress: " + progressData.getCurrentPercentageCompletion() + " %");

			                // We have an error so stop everything!
			                if (progressData.isError()) {
			                    // Say what the error is...
			                    logger.error(progressData.getException().getMessage(),
			                            progressData.getException());
			                   
			                    return;
			                } else {
			                    logger.info("TEST: contextualizeServiceCallbackObject total progress is: "
			                            + progressData.getTotalProgress());
			                    logger.info("TEST: contextualizeServiceCallbackObject phase history of Phase with ID: "
			                            + progressData.getPhaseName(progressData
			                                    .getCurrentPhaseId())
			                            + ", % is: "
			                            + progressData.getHistory().get(
			                                    progressData.getCurrentPhaseId()));
			                }

			                // 250ms delay between polling...
			                Thread.sleep(250);

			                // Test to see if contextualization has finished...
			                if (vmcClient.getVmcClient().contextualizeServiceCallback(
			                        ovf.getVirtualSystemCollection().getId())
			                        .isComplete()) {
			                    logger.warn("TEST: Detected contextualization has completed!");
			                    break;
			                }
			            }
			            
			            logger.debug("Updating the OVF from the Contextualization...");
			            
			            //retrieve new ovf from VM contextualizer
			            OvfDefinition ovfContextualized = vmcClient.getVmcClient()
			                    .contextualizeServiceCallback(
			                            ovf.getVirtualSystemCollection().getId())
			                    .getOvfDefinition();
			            
			            logger.debug("New OVF from VMC: " + ovfContextualized.toString());
			            
			            //update the deployment 
			            deployment.setOvf(ovfContextualized.toString());

			            // Since we are not doing this right now, we move the application to the next step
			            deployment.setStatus(Dictionary.APPLICATION_STATUS_CONTEXTUALIZED);
						deploymentEvent.setDeploymentStatus(deployment.getStatus());
						
						// We save the changes to the DB
						deploymentDAO.update(deployment);
						
						// We sent the message that the contextualization ends:
						AmqpProducer.sendDeploymentContextualizedMessage(deploymentEvent.getApplicationName(), deployment);
						
						//We notify that the deployment has been modified
						deploymentEventService.fireDeploymentEvent(deploymentEvent);
					}
				}
				catch (Exception e){
					logger.error("Exception ocurred. Cause: " + e.getMessage());
					AmqpProducer.sendDeploymentErrorMessage(deploymentEvent.getApplicationName(), deployment);
				}			
			}
		}
	}
}
