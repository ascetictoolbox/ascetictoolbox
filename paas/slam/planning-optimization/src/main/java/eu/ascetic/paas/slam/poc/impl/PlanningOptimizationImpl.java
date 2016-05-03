/**
 * Copyright 2012 Hewlett-Packard Development Company, L.P.                
 *                                                                          
 * Licensed under the Apache License, Version 2.0 (the "License");         
 * you may not use this file except in compliance with the License.        
 * You may obtain a copy of the License at                                 
 *                                                                          
 *     http://www.apache.org/licenses/LICENSE-2.0                          
 *                                                                          
 * Unless required by applicable law or agreed to in writing, software     
 * distributed under the License is distributed on an "AS IS" BASIS,       
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and     
 * limitations under the License.                                          
 */

/**
 * Copyright (c) 2008-2010, SLASOI
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *     * Redistributions of source code must retain the above copyright
 *       notice, this list of conditions and the following disclaimer.
 *     * Redistributions in binary form must reproduce the above copyright
 *       notice, this list of conditions and the following disclaimer in the
 *       documentation and/or other materials provided with the distribution.
 *     * Neither the name of SLASOI nor the
 *       names of its contributors may be used to endorse or promote products
 *       derived from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL SLASOI BE LIABLE FOR ANY
 * DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 *
 * @author         Miguel Rojas - miguel.rojas@uni-dortmund.de
 * @version        $Rev$
 * @lastrevision   $Date$
 * @filesource     $URL$
 */

package eu.ascetic.paas.slam.poc.impl;


import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.jms.Connection;
import javax.jms.ConnectionFactory;
import javax.jms.MessageProducer;
import javax.jms.Session;
import javax.jms.TextMessage;
import javax.jms.Topic;

import org.apache.activemq.ActiveMQConnectionFactory;
import org.apache.log4j.Logger;
import org.slasoi.gslam.core.context.SLAMContextAware;
import org.slasoi.gslam.core.context.SLAManagerContext;
import org.slasoi.gslam.core.context.SLAManagerContext.SLAManagerContextException;
import org.slasoi.gslam.core.negotiation.INegotiation.TerminationReason;
import org.slasoi.gslam.core.negotiation.SLARegistry;
import org.slasoi.gslam.core.negotiation.SLARegistry.IRegister;
import org.slasoi.gslam.core.negotiation.SLARegistry.InvalidUUIDException;
import org.slasoi.gslam.core.negotiation.SLARegistry.SLAState;
import org.slasoi.gslam.core.negotiation.SLARegistry.UpdateFailureException;
import org.slasoi.gslam.core.pac.ProvisioningAdjustment;
import org.slasoi.gslam.core.pac.ProvisioningAdjustment.Plan;
import org.slasoi.gslam.core.pac.ProvisioningAdjustment.PlanFormatException;
import org.slasoi.gslam.core.pac.ProvisioningAdjustment.PlanFoundException;
import org.slasoi.gslam.core.pac.ProvisioningAdjustment.PlanNotFoundException;
import org.slasoi.gslam.core.pac.ProvisioningAdjustment.Status;
import org.slasoi.gslam.core.poc.PlanningOptimization;
import org.slasoi.slamodel.primitives.STND;
import org.slasoi.slamodel.primitives.TIME;
import org.slasoi.slamodel.primitives.UUID;
import org.slasoi.slamodel.sla.InterfaceDeclr;
import org.slasoi.slamodel.sla.SLA;
import org.slasoi.slamodel.sla.SLATemplate;

import eu.ascetic.paas.slam.poc.exceptions.SubNegotiationException;
import eu.ascetic.paas.slam.poc.impl.config.ConfigManager;
import eu.ascetic.paas.slam.poc.impl.paasapi.PaaSApiManager;
import eu.ascetic.paas.slam.poc.impl.provider.manager.ProviderManager;
import eu.ascetic.paas.slam.poc.impl.provider.manager.ProviderManagerImpl;
import eu.ascetic.paas.slam.poc.impl.provider.selection.Criterion;
import eu.ascetic.paas.slam.poc.impl.provider.selection.OfferSelector;
import eu.ascetic.paas.slam.poc.impl.provider.selection.algorithms.MaxAverageVirtualSystemDistance;
import eu.ascetic.paas.slam.poc.impl.provider.selection.algorithms.PriceSelection;
import eu.ascetic.paas.slam.poc.impl.slaparser.SlaTemplateEntitiesParser;
import eu.ascetic.paas.slam.poc.negotiation.SLAT2SLAImpl;
import eu.ascetic.paas.slam.poc.provision.PlanHandlerImpl;
import eu.ascetic.paas.slam.poc.replan.ReplanImpl;





/**
 * DOMAIN SPECIFIC POC
 * 
 * @author Miguel Rojas (UDO)
 * 
 */
public class PlanningOptimizationImpl implements PlanningOptimization, SLAMContextAware {

	/**
	 * Initializes a newly created <code>PlanningOptimizationImpl</code> object.
	 */
	public PlanningOptimizationImpl() {
		ConfigManager cm = ConfigManager.getInstance();

		String algorithm = cm.getAlgorithmClass();
		if (algorithm.equalsIgnoreCase("eu.ascetic.paas.slam.poc.impl.provider.selection.algorithms.PriceSelection")) {
			LOGGER.debug("Price Selection Algorithm chosen.");
			offerSelector = new PriceSelection();
		}
		else if (algorithm.equalsIgnoreCase("eu.ascetic.paas.slam.poc.impl.provider.selection.algorithms.MaxAverageVirtualSystemDistance")) {
			LOGGER.debug("Max Average Virtual System Distance Algorithm chosen.");
			offerSelector = new MaxAverageVirtualSystemDistance();
		}
		else {
			LOGGER.debug("Unknown selection Algorithm. Choosing Price Selection...");
			offerSelector = new PriceSelection();
		}

		dsAssestmentAndCustomized = new DomainSpecAssessmentAndCustomize();
		dsINotification = new POCINotification();
		dsIPlanStatus = new POCIPlanStatus();
		dsIReplan = new POCIReplan();
	}

	/**
	 * Gets the inner class <code>IAssessmentAndCustomize</code> instance.
	 * 
	 * @see #pocIAssessmentAndCustomize
	 */
	public IAssessmentAndCustomize getIAssessmentAndCustomize() {
		return dsAssestmentAndCustomized;
	}

	/**
	 * Gets the inner class <code>INotification</code> instance.
	 * 
	 * @see #pocINotification
	 */
	public INotification getINotification() {
		return dsINotification;
	}

	/**
	 * Gets the inner class <code>IPlanStatus</code> instance.
	 * 
	 * @see #pocIPlanStatus
	 */
	public IPlanStatus getIPlanStatus() {
		return dsIPlanStatus;
	}

	/**
	 * Gets the inner class <code>IReplan</code> instance.
	 * 
	 * @see #pocIReplan
	 */
	public IReplan getIReplan() {
		return dsIReplan;
	}

	/**
	 * Injects the infrastructure SLA manager instance into the planning and optimization context.
	 * 
	 * @param context
	 *            The instance of infrastructure SLA manager
	 */
	public void setSLAManagerContext(SLAManagerContext context) {
		this.context = context;
	}

	// ------------------------------------- INNER CLASSES ----------------------------------------
	/**
	 * The <code>POCIAssessmentAndCustomize</code> class represents all the operations during SLA negotiation phase.
	 */
	class DomainSpecAssessmentAndCustomize implements IAssessmentAndCustomize {
		/**
		 * Customer invokes this method for creating a final agreement.
		 * 
		 * @param negotiationID
		 *            the ID of a specific negotiation.
		 * @param slaTemplate
		 *            the SLA template based on which a final SLA will be created.
		 */
		@SuppressWarnings("finally")
		public SLA createAgreement(String negotiationID, SLATemplate slaTemplate) {

			boolean renegotiationFlag = false;
			SLA[] asceticSLAs = null;
			SLA asceticSLA = null;
			SLA sla = null;
			try {

				// prepares in advance PaaS SLA ID (used by P-SLAM for violation messages forwarding)
				UUID paasSLAId = new UUID(java.util.UUID.randomUUID().toString());

				slaTemplate.setPropertyValue(new STND("PaaSSlaId"), paasSLAId.toString());

				// creates agreement with the provider 
				sla = providerManager.createAgreement(slaTemplate);

				SLARegistry slaRegistry = context.getSLARegistry();

				asceticSLAs = slaRegistry.getIQuery().getSLA(new UUID[] { new UUID(negotiationID) });
				if (asceticSLAs != null && asceticSLAs.length > 0) {
					renegotiationFlag = true;
					asceticSLA = asceticSLAs[0];
					LOGGER.info("*** SLA found with ID = " + asceticSLA.getUuid().getValue());
				}

				// set SLA ID
				sla.setUuid(paasSLAId);
				// set AgreedAt
				sla.setAgreedAt(new TIME(SLAT2SLAImpl.getCurrentTime_yyyyMMMddHH_mm_ss()));
				// create now time
				sla.setAgreedAt(new TIME(SLAT2SLAImpl.getCurrentTime_yyyyMMMddHH_mm_ss()));
				// set effective From
				sla.setEffectiveFrom(new TIME(SLAT2SLAImpl.getEffectiveFromTime_yyyyMMMddHH_mm_ss()));
				// set effective Until
				sla.setEffectiveUntil(new TIME(SLAT2SLAImpl.getEffectiveUntilTime_yyyyMMMddHH_mm_ss()));

				//sla.setPropertyValue(PlanHandlerImpl.PLAN_ID_SLA, java.util.UUID.randomUUID().toString());

				LOGGER.info("SLA is created successfully!");
				LOGGER.info("The SLA can be fully/partially monitored by monitoring manager.");
				LOGGER.info("Start to register SLA into SLA registry...");

				SLARegistry registry = context.getSLARegistry();
				IRegister register = registry.getIRegister();
				if (renegotiationFlag == false) {
					register.register(sla, null, SLAState.OBSERVED);
					LOGGER.info("SLA is registered successfully into SLA registry!");
				}
				else {
					LOGGER.info("Before updating own SLA in SLA registry.");
					register.update(asceticSLA.getUuid(), sla, null, SLAState.OBSERVED);
					LOGGER.info("Own SLA updated successfully in SLA registry.");
					
					/*
					 * send a message to the queue to change monitoring parameters
					 */
					sendRenegotiationMessage(negotiationID, sla.getUuid().getValue());
					/*
					 * END
					 */
				}

				// invoking service to set minimumLoA
				Integer loa = SlaTemplateEntitiesParser.getMinimumLoAValue(slaTemplate);

				if (loa != null) {
					LOGGER.info("Minimum Loa: " + loa.toString());
					setMinimumLoa(loa, slaTemplate);
				}

			}
			catch (Exception e) {
				LOGGER.debug(e);
			}
			finally {
				return sla;
			}
		}

		
		
		/**
		 *  Writes a renegotiation message to the message queue to update monitoring parameters
		 */
		private void sendRenegotiationMessage(String oldId, String newId) {
			
			try{

				//ConnectionFactory connectionFactory = new ActiveMQConnectionFactory(ConfigManager.getInstance().getActivemqUrl());
				ConnectionFactory connectionFactory = new ActiveMQConnectionFactory("tcp://localhost:61616");
				Connection connection = connectionFactory.createConnection();
				connection.start();

				// JMS messages are sent and received using a Session. We will
				// create here a non-transactional session object. If you want
				// to use transactions you should set the first parameter to 'true'
				Session session = connection.createSession(false,
						Session.AUTO_ACKNOWLEDGE);

				Topic topic = session.createTopic("paas-slam.monitoring."+oldId+".renegotiated");

				MessageProducer producer = session.createProducer(topic);


				TextMessage message = session.createTextMessage();

				message.setText(newId);
				
				// Here we are sending the message!
				producer.send(message);
				LOGGER.info("Renegotiation message sent.\nOldId: "+oldId+"\nNewId: " + message.getText());

				connection.close();
			}catch(Exception e){
				e.printStackTrace();
				LOGGER.error(e.getMessage());
				//            System.err.println("NOT CONNECTED!!!");
			}
		}
		

		/**
		 * Customer invokes this method for starting a negotiation
		 * 
		 * @param negotiationID
		 *            the ID of a specific negotiation.
		 * @param slaTemplate
		 *            the counter-offer from customer side that service provider has to analyze.
		 */
		public SLATemplate[] negotiate(String negotiationID, SLATemplate slaTemplate) {
			assert (negotiationID != null && !negotiationID.equals("") && slaTemplate != null) : "negotiate method requires an negotiationID != null or not empty and an slaTemplate != null.";

			/*** 1. call provider registry the get the provider list ***/
			String[] providerEndpoints = providerManager.getProvidersList(slaTemplate);
			//			System.out.println("Endpoint IaaS da file di configurazione...");
			//			String[] providerEndpoints = {ConfigManager.getInstance().getDefaultIaasProvider()};

			List<SLATemplate[]> providerSlats = new ArrayList<SLATemplate[]>();

			String iaasSlaId = null;
			/*
			 * GESTIONE RINEGOZIAZIONE
			 */
			SLA[] asceticSLAs = null;
			SLA asceticSLA = null;
			SLA sla = null;
			try {

				//				slaTemplate.setPropertyValue(new STND("PaaSSlaId"), paasSLAId.toString());
				SLARegistry slaRegistry = context.getSLARegistry();			
				asceticSLAs = slaRegistry.getIQuery().getSLA(new UUID[] { new UUID(negotiationID) });
				
				//renegotiation = true
				if (asceticSLAs != null && asceticSLAs.length > 0) {
					asceticSLA = asceticSLAs[0];
					LOGGER.info("*** SLA found with ID = " + asceticSLA.getUuid().getValue());
					
					//recovering iaas sla id
					for (InterfaceDeclr i:asceticSLA.getInterfaceDeclrs()) {
						String slaproviderlist = i.getPropertyValue(new STND("SLA-ProvidersList"));
						String pattern = "\\\"sla-id\\\":\\\"(.+)\\\",\\\"provider-uuid\\\":\\\"[\\d+]\\\"";
						Pattern r = Pattern.compile(pattern);
						Matcher m = r.matcher(slaproviderlist);
						if (m.find( )) {
							 iaasSlaId = m.group(1);
							 LOGGER.info("Found IaaS SLA ID (IaaS renegotiationID): " + iaasSlaId);
					      } else {
					    	  LOGGER.info("NO IaaS SLA ID found");
					      }
//						System.out.println(slaproviderlist.substring(slaproviderlist.indexOf("\"", 6)));
					}
				}
			} catch (Exception e) {
				LOGGER.debug(e);
			}
			/*
			 * FINE GESTIONE RINEGOZIAZIONE
			 */

			/*** 2. foreach provider in the list, make a negotiation round and save the SLA ***/
			for (String providerEndpoint : providerEndpoints) {
				System.out.println("Interrogo endpoint "+providerEndpoint);
				try {
					//iaasSLaId == null --> initiateNegotiation
					//iaasSlaId != null --> renegotiation
					SLATemplate[] slats = providerManager.negotiate(providerEndpoint, slaTemplate, iaasSlaId);

					providerSlats.add(slats);
				} catch (SubNegotiationException e) {
					LOGGER.error(e);
				}
			}

			/*** 3. provider selection ***/
			Criterion[] criteria = providerManager.getCriteria(slaTemplate);
			List<SLATemplate> flattenOffers = new ArrayList<SLATemplate>();
			for (SLATemplate[] ps : providerSlats) {
				for (SLATemplate s : ps) {
					flattenOffers.add(s);
				}
			}
			return offerSelector.selectOptimaSlaTemplates(flattenOffers, slaTemplate, criteria); 
		}

		/**
		 * Customer invokes this method for starting provisioning
		 * 
		 * @param slaID
		 *            the UUID of a specific SLA.
		 */
		public SLA provision(UUID slaID) {
			assert (slaID != null && slaID.getValue() != null && !slaID.getValue().equals("")) : "provision method requires an slaID != null or not empty";
			return new SLA();
		}

		/**
		 * Customer invokes this method for canceling provisioning
		 * 
		 * @param arg0
		 *            the UUID of a specific SLA.
		 * @param arg1
		 *            the reason for canceling the SLA.
		 */
		public boolean terminate(UUID arg0, List<TerminationReason> arg1) {
			assert (arg0 != null && arg0.getValue() != null && !arg0.getValue().equals("") && arg1 != null && arg1
					.size() >= 1) : "it requires a UUID != null or not empty and termination reason not null and the size of arg1 list is greater or equal to 1.";
					SLARegistry registry = null;
					SLA infSLAToTerminate = null;
					try {
						registry = context.getSLARegistry();
						infSLAToTerminate = registry.getIQuery().getSLA(new UUID[] { arg0 })[0];
						// no dependencies
						registry.getIRegister().update(arg0, infSLAToTerminate, null, SLAState.EXPIRED);
					}
					catch (SLAManagerContextException e) {
						e.printStackTrace();
					}
					catch (UpdateFailureException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					catch (InvalidUUIDException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}

					ProvisioningAdjustment pac;
					try {
						pac = context.getProvisioningAdjustment();
						pac.cancelExecution(infSLAToTerminate.getPropertyValue(PlanHandlerImpl.PLAN_ID_SLA));
						return true;
					}
					catch (SLAManagerContextException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
						return false;
					}
					catch (PlanNotFoundException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
						return false;
					}
		}
	}

	/**
	 * The <code>POCINotification</code> class represents all the operations during SLA provisioning phase.
	 * 
	 */
	class POCINotification implements INotification {
		/**
		 * Starts provisioning
		 * 
		 * @param newSLA
		 *            the object of a specific SLA.
		 */
		public void activate(SLA newSLA) {
			assert (newSLA != null) : "it requires an SLA != null.";
			try {
				PlanHandlerImpl planHandler= new PlanHandlerImpl(newSLA);
				Plan plan = planHandler.planMaker();
				if (plan != null) {
					ProvisioningAdjustment pac = context.getProvisioningAdjustment();
					pac.executePlan(plan);
				}
				else {
					LOGGER
					.error("Infrastructure does not have enough resources while querying, plan can not be executed.");
				}
			}
			catch (SLAManagerContextException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			catch (PlanFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			catch (PlanFormatException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}

	/**
	 * The <code>POCIPlanStatus</code> class represents the operations related to plan status during SLA provisioning
	 * phase.
	 */
	class POCIPlanStatus implements IPlanStatus {

		/**
		 * Accepts the current status of a plan given its ID
		 * 
		 * @param planId
		 *            the ID of a specific plan.
		 * @param status
		 *            the status of a specific plan.
		 */
		public void planStatus(String planId, Status status) {
			assert (planId != null && !planId.equals("") && status != null) : "it requires a planId != null or not empty and plan status not null.";
			// TODO Auto-generated method stub
		}
	}

	/**
	 * The <code>POCIReplan</code> class represents the re-plan operations during SLA provisioning phase.
	 * 
	 */
	class POCIReplan implements IReplan {

		/**
		 * Accepts the analysis of a specific plan
		 * 
		 * @param planId
		 *            the ID of a specific plan.
		 * @param analysis
		 *            the reason for re-plan.
		 */
		public void rePlan(String uuid, String analysis) {
			assert (uuid != null && !uuid.equals("") && analysis != null && !analysis.equals("")) : "it requires a planId != null or not empty and analysis not null or not empty.";
			ReplanImpl replan = new ReplanImpl();
			UUID id = new UUID("");
			replan.rePlan(id);
			// TODO Auto-generated method stub
		}
	}

	protected IAssessmentAndCustomize dsAssestmentAndCustomized;
	protected INotification dsINotification;
	protected IPlanStatus dsIPlanStatus;
	protected IReplan dsIReplan;


	private ProviderManager providerManager = new ProviderManagerImpl();

	private OfferSelector offerSelector;

	protected SLAManagerContext context;



	private void setMinimumLoa(int loa, SLATemplate slaTemplate) {

		String userUuid = SlaTemplateEntitiesParser.getRootPropertyValue(slaTemplate, "UserUUID");
		LOGGER.info("User uuid: " + userUuid);
		String appUuid = SlaTemplateEntitiesParser.getRootPropertyValue(slaTemplate, "AppUUID");
		LOGGER.info("App uuid: " + appUuid);

		PaaSApiManager fedApiManager = PaaSApiManager.getInstance();
		fedApiManager.setMinimumLoa(loa, userUuid, appUuid);
	}


	private static final Logger LOGGER = Logger.getLogger(PlanningOptimizationImpl.class);



}
