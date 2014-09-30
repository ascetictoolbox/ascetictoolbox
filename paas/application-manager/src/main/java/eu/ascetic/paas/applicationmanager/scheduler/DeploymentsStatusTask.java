package eu.ascetic.paas.applicationmanager.scheduler;

import java.util.List;

import org.apache.axis2.AxisFault;
import org.apache.log4j.Logger;
import org.slasoi.gslam.core.negotiation.INegotiation;
import org.slasoi.slamodel.sla.SLA;
import org.slasoi.slamodel.sla.SLATemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;

import eu.ascetic.applicationmanager.slam.stub.InvalidNegotiationIDExceptionException;
import eu.ascetic.paas.applicationmanager.conf.Configuration;
import eu.ascetic.paas.applicationmanager.contextualizer.VmcClient;
import eu.ascetic.paas.applicationmanager.dao.DeploymentDAO;
import eu.ascetic.paas.applicationmanager.model.Deployment;
import eu.ascetic.paas.applicationmanager.model.Dictionary;
import eu.ascetic.paas.applicationmanager.ovf.OVFUtils;
import eu.ascetic.paas.applicationmanager.pm.PriceModellerClient;
import eu.ascetic.paas.applicationmanager.slam.SLAMClient;
import eu.ascetic.paas.applicationmanager.slam.SLATemplateCreator;
import eu.ascetic.paas.applicationmanager.vmmanager.VmManagerUtils;
import eu.ascetic.paas.applicationmanager.vmmanager.client.VmManagerClientHC;
import eu.ascetic.paas.applicationmanager.vmmanager.datamodel.Vm;
import eu.ascetic.utils.ovf.api.OvfDefinition;
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
 * @author: David Garcia Perez. Atos Research and Innovation, Atos SPAIN SA
 * @email david.garciaperez@atos.net 
 * 
 * This periodical taks will go through the deployments table, and look the ones that need some update
 * to be moved to a new state
 *
 */

public class DeploymentsStatusTask {
	private static Logger logger = Logger.getLogger(DeploymentsStatusTask.class);
	
	@Autowired
	protected DeploymentDAO deploymentDAO;
	
	@Scheduled(cron="${check.deployments.status}")
	public void checkDeploymentStatus() {
		logger.info("Retrieving application deployments from DB to see if it is necessary to move applications to other state");
		
		List<Deployment> deployments = deploymentDAO.getAll();
		
		for(Deployment deployment : deployments) {
			logger.info("Checking deployment id: " + deployment.getId() + " Status: " + deployment.getStatus());
			if(deployment.getStatus().equals(Dictionary.APPLICATION_STATUS_SUBMITTED)) {
				logger.info(" Moving deployment id: " + deployment.getId()  + " to NEGOTIATION state");
				deploymentSubmittedActions(deployment);
			} else if(deployment.getStatus().equals(Dictionary.APPLICATION_STATUS_NEGOTIATION)) {
				logger.info(" Checking status of NEGOTIATION proccess for deployment: " + deployment.getId());
				deploymentNegotiationActions(deployment);
			} else if(deployment.getStatus().equals(Dictionary.APPLICATION_STATUS_NEGOTIATIED)) {
				// TODO this check needs to be deleted when we enable the user to be able to 
				//      manually accept the negotiation proccess... for the moment we just give the 
				//      deployment an automatic push... 
				logger.info("  TO BE DELETED -> automatically moving deployment " + deployment.getId() + " to SLA_AGREEMENT_ACCEPTED");
				
				deploymentAcceptAgreementActions(deployment);
			} else if(deployment.getStatus().equals(Dictionary.APPLICATION_STATUS_CONTEXTUALIZATION)) {
				logger.info(" The deployment " + deployment.getId() + " is ready to be contextualized, starting the contextualization process");
				
				deploymentStartContextualizationActions(deployment);
			} else if(deployment.getStatus().equals(Dictionary.APPLICATION_STATUS_CONTEXTUALIZED)) {
				logger.info(" The deployment is ready to be deployed " + deployment.getId() + " deploying it!!!");
				
				deploymentDeployApplicationActions(deployment);
			}
		}
	}

	/**
	 * Starts to interact over a recently deployed application
	 * @param deployment 
	 */
	protected void deploymentSubmittedActions(Deployment deployment) {
		// TODO in this case it should move the application to the NEGOTIATION STATE
		//      and start the interactions with the SLA Manager
		//      this also involve passing the SLA Manager sla aggreement to the
		//      Energy Modeller and Price Modeller, for the specifics of those interactions
		//      apart for the two diagrams, it is possible to contact Davide from HP that has a 
		//      clear picture about how this could be done
		
		// Since we are not doing this step right now, we just move the application to the next state
		deployment.setStatus(Dictionary.APPLICATION_STATUS_NEGOTIATION);
		
		// We save the changes to the DB
		deploymentDAO.update(deployment);
	}
	
	/**
	 * Checks if the negotiation ended
	 * @param deployment
	 */
	protected void deploymentNegotiationActions(Deployment deployment) {
		deployment.setStatus(Dictionary.APPLICATION_STATUS_NEGOTIATING);
		// We save the changes to the DB
		deploymentDAO.update(deployment);
		
		// First we create the SLA template from the OVF
		OvfDefinition ovfDefinition = OVFUtils.getOvfDefinition(deployment.getOvf());
		SLATemplate slaTemplate = SLATemplateCreator.generateSLATemplate(ovfDefinition, "http://10.4.0.16/application-manager" + deployment.getHref() + "/ovf");
		logger.debug("Initial SLA Template document: " + slaTemplate);
		
		try {
			// Then we initiate the Negotiation
			SLAMClient client = new SLAMClient(Configuration.slamURL);
			
			String initiatieNegotiationID = client.initiateNegotiation(slaTemplate);
			logger.info("Negotiation ID: " + initiatieNegotiationID);
			
			// After the negotiation it is initiated, we get and negotiation ID, we use it to start the actual negotiation process
			SLATemplate[] slaTemplates = client.negotiate(initiatieNegotiationID, slaTemplate);
			logger.info("Agreement selected: " + slaTemplates[0]);

			// Then we get a list of possible SLAs
			// Since we only have a provider the first year, we actually accept the first contract (this could be changed)
			SLA slaAgreement = client.createAgreement(initiatieNegotiationID, slaTemplates[0]);
			logger.info("Agreement reached... "  + slaAgreement);
		}
		catch(AxisFault exception) {
			logger.warn("ERROR connecting to PaaS SLAM");
			exception.printStackTrace();
		}
		catch(INegotiation.OperationNotPossibleException exception) {
			logger.warn("ERROR starting the initialization of the Negotiation with the PaaS SLAM");
			exception.printStackTrace();
		}
		catch(INegotiation.OperationInProgressException exception) {
			logger.warn("ERROR trying to negotiate an SLA Negotiation");
			exception.printStackTrace();
		}
		catch(INegotiation.InvalidNegotiationIDException exception) {
			logger.warn("ERROR trying to negotiate an SLA Negotiation");
			exception.printStackTrace();
		}
		catch(INegotiation.SLACreationException exception) {
			logger.warn("ERROR creating the SLA Agreement");
			exception.printStackTrace();
		}
		
		deployment.setStatus(Dictionary.APPLICATION_STATUS_NEGOTIATIED);
		// We save the changes to the DB
		deploymentDAO.update(deployment);
	}
	
	/**
	 * Actions to be performed when an agreement is accepted by the user for an
	 * Specific deployment
	 * @param deployment
	 */
	protected void deploymentAcceptAgreementActions(Deployment deployment) {
		// TODO Right now this just moves the state of the Deployment from 
		//      NEGOTIATED to CONTEXTUALIZATION state ... now it is automatica
		//      but this process needs to be manual and started by the user by an update
		//      of DEPLOYMENT STATE (PUT in REST interface), when negotitation process is
		//      enabled in the Application Manager
		
		// We calculate the new price, since we are not updating the SLATemplate Price I'm doing this here:
		PriceModellerClient.calculatePrice(1, deployment.getId(), 100.0);
		
		// Since we are not doing this right now, we move the application to the next step
		deployment.setStatus(Dictionary.APPLICATION_STATUS_CONTEXTUALIZATION);
		
		// We save the changes to the DB
		deploymentDAO.update(deployment);
	}
	
	/**
	 * Actions to contact the VM Contextualizer when the VM is ready to be contextualiced
	 * @param deployment
	 */
	protected void deploymentStartContextualizationActions(Deployment deployment) {
		// TODO Here we need to talk with Django and move the application to CONTEXTUALIZING STATE
		//      We submit the negotiated OVF (if we have negotiation phase), and get the updated
		//      OVF object back from the Contextualization, we need to update the OVF in the database
		//      Since this process takes time, it should move the application state to CONTEXTUALIZING
		//      and after that create a method that checks if the Deployment is in contextualizing
		//      and ask the VM Contextualization if the process has finish... 
		
		logger.info(" Starting to contextualize deployment: " + deployment.getId());
	
		OvfDefinition ovf = OVFUtils.getOvfDefinition(deployment.getOvf());
		
		if (ovf != null){
			try {
				
				logger.debug(" Creating the VM Client");
				VmcClient vmcClient = new VmcClient(ovf);
				
				if (vmcClient.getVmcClient() != null){
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
		            
		            //update the deployment 
		            deployment.setOvf(ovfContextualized.toString());

		            // Since we are not doing this right now, we move the application to the next step
		            deployment.setStatus(Dictionary.APPLICATION_STATUS_CONTEXTUALIZED);

		            // We save the changes to the DB
		            deploymentDAO.update(deployment);
				}
				
	            
			}
			catch (Exception e){
				logger.error("Exception ocurred. Cause: " + e.getMessage());
			}			
		}
	}
	
	/**
	 * Actions to deploy an application...
	 * @param deployment
	 */
	protected void deploymentDeployApplicationActions(Deployment deployment) {
		// TODO We need to change the workflow for the images... it can not be this way... 
		
		deployment.setStatus(Dictionary.APPLICATION_STATUS_DEPLOYING);
		deploymentDAO.update(deployment);
		
		
		try {
			VmManagerClientHC vmManagerClient = new VmManagerClientHC();
			List<Vm> vmsToDeploy = OVFUtils.getVmsFromOvf(deployment, vmManagerClient);	
			List<String> vmsDeployedIds = vmManagerClient.deployVMs(vmsToDeploy);
			
			if (vmsDeployedIds != null && !vmsDeployedIds.isEmpty()){
				if (vmsDeployedIds.size() == vmsToDeploy.size()){
					//all VMs deployed successfully, update deployment with new info from VMs
					boolean updated = VmManagerUtils.updateVms(vmManagerClient, deployment, vmsDeployedIds, vmsToDeploy);
					if (updated){
						deployment.setStatus(Dictionary.APPLICATION_STATUS_DEPLOYED);							
						
					} else {
						deployment.setStatus(Dictionary.APPLICATION_STATUS_ERROR);	
					}
					
					// We save the changes to the DB
					deploymentDAO.update(deployment);
				}
				else {
					logger.info("All VMs cannot be created. VMs deployed: " + vmsDeployedIds.size());
					deployment.setStatus(Dictionary.APPLICATION_STATUS_ERROR);
					// We save the changes to the DB
					deploymentDAO.update(deployment);
				}
			}	
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
}
