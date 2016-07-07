/**
 *  Copyright 2014 Hewlett-Packard Development Company, L.P.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
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
 * @author         Beatriz Fuentes - fuentes@tid.es
 * @version        $Rev$
 * @lastrevision   $Date$
 * @filesource     $URL$
 */

package eu.ascetic.paas.slam.pac.impl;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.sql.Timestamp;
import java.util.List;
import java.util.Scanner;

import javax.jms.ConnectionFactory;
import javax.jms.Message;
import javax.jms.MessageListener;
import javax.jms.MessageProducer;
import javax.jms.Queue;
import javax.jms.Session;
import javax.jms.TextMessage;

import org.apache.activemq.ActiveMQConnectionFactory;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;
import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;
import org.slasoi.gslam.core.control.Policy;
import org.slasoi.gslam.pac.ProvisioningAndAdjustment;
import org.slasoi.slamodel.primitives.STND;
import org.slasoi.slamodel.sla.AgreementTerm;
import org.slasoi.slamodel.sla.Guaranteed;
import org.slasoi.slamodel.sla.SLA;

import eu.ascetic.paas.slam.pac.PaasViolationChecker;
import eu.ascetic.paas.slam.pac.amqp.AmqpMessageReceiver;
import eu.ascetic.paas.slam.pac.applicationmanager.ModelConverter;
import eu.ascetic.paas.slam.pac.applicationmanager.amqp.model.ApplicationManagerMessage;
import eu.ascetic.paas.slam.pac.applicationmanager.model.Deployment;
import eu.ascetic.paas.slam.pac.impl.provider.reporting.GetSLAClient;
import eu.ascetic.paas.slam.pac.impl.provider.translation.MeasurableAgreementTerm;


/**
 * DOMAIN SPECIFIC PAC.
 * 
 * @author Miguel Rojas (UDO)
 * 
 */
public class ProvisioningAdjustmentImpl extends ProvisioningAndAdjustment {
	/**
	 * logger.
	 */
	private static Logger logger = Logger.getLogger(ProvisioningAdjustmentImpl.class.getName());

	protected static String ACTIVEMQ_URL = "activemq_url";
	private static String ACTIVEMQ_CHANNEL = "activemq_channel";
	private static String DEPLOYED_APPS_QUEUE = "deployed_apps_queue";
	private static String APPMANAGER_URL = "appmanager_url";
	private static String BUSINESS_REPORTING_URL = "business_reporting_url";
	private static String MONITORABLE_TERMS = "monitorable_terms";

	//    private static String configurationFileImpl = null;

	/*
	 * TODO
	 * retrieve url from configuration file
	 */

	/**
	 * Constructor.
	 * 
	 * @param configFile
	 *            configuration file to be used by the PAC
	 */
	public ProvisioningAdjustmentImpl(final String configFile) {
		super(configFile);
		logger.info("Creating Skeleton PAC...");
		logger.info("Searching for Application Startup Events on ActiveMQ (configfile)...");
	}

	/**
	 * Default constructor.
	 */
	public ProvisioningAdjustmentImpl() {
		logger.info("Creating Skeleton PAC...");
		logger.info("Searching for Application Startup Events on ActiveMQ...");
		init();
		//        logger.info("Doing nothing...");
		retrieveApplicationEvents();
	}


	public void init() {
		logger.debug("ASCETIC: ProvisioningAndAdjustment, init() method");
		//		configurationFileImpl = configurationFile;
		//		configurationFileImpl = configurationFileImpl.replace("/", System.getProperty("file.separator"));

		String sepr = System.getProperty("file.separator");
		String confPath = System.getenv("SLASOI_HOME");
		String configFile = confPath + sepr
				+ "ascetic-slamanager" + sepr + "provisioning-adjustment" + sepr
				+ "provisioning_adjustment.properties";
		try {

			//			String initialPath = configurationFilesPath + configurationFile;
			String initialPath = configFile;		
			//			logger.debug("Initial path = " + initialPath);
			String thePath = initialPath.replace("/", System.getProperty("file.separator"));
			logger.debug("Configuration file path = " + thePath);
			properties.load(new FileInputStream(thePath));
			PropertyConfigurator.configure(configurationFilesPath + properties.getProperty(LOG4J_FILE));
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}

		logger.debug("Properties file loaded...");

		
		
		//recover old monitorings...
		logger.debug("Recovering old monitorings...");
		String monitoringPath = confPath + sepr	+ "ascetic-slamanager" + sepr + "provisioning-adjustment" + sepr + "activeMonitorings";
		File monitoringFile = new File(monitoringPath);
		Scanner scanner = null;
		try {
			scanner = new Scanner(monitoringFile);

			while (scanner.hasNextLine()) 
			{
				String lineFromFile = scanner.nextLine();
				String[] parameters = lineFromFile.split("%%%");

				logger.info("PaaS Violation Checker - recovering an active monitoring with this parameters: topicId "+parameters[0]+", appId "+parameters[1]+", deploymentId "+parameters[2]+", slaId "+parameters[3]);
				new Thread(new PaasViolationChecker(properties, parameters[0], parameters[1], parameters[2], parameters[3], true)).start();
			}
			
			
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			if (scanner!=null) scanner.close();
		}
		if (scanner!=null) scanner.close();
		//end
	}

	/**
	 * 1. Gets 'application started' events from the message queue
	 */
	private void retrieveApplicationEvents() {
		try{

			AmqpMessageReceiver receiver = new AmqpMessageReceiver("192.168.3.16:5673", "guest", "guest",  properties.getProperty(DEPLOYED_APPS_QUEUE), true);



			//			// Getting JMS connection from the server
			//			logger.info("Reading application events from ACTIVEMQ: "+properties.getProperty(ACTIVEMQ_URL));
			//			ConnectionFactory connectionFactory = new ActiveMQConnectionFactory(properties.getProperty(ACTIVEMQ_URL));
			//			Connection connection = connectionFactory.createConnection();
			//
			//			// need to setClientID value, any string value you wish
			//			connection.setClientID("PaaS SLAM Application Event Listener");
			//
			//			logger.info("Starting connection...");
			//
			//			connection.start();
			//
			//			logger.info("Creating session...");
			//
			//			Session session = connection.createSession(false,
			//					Session.AUTO_ACKNOWLEDGE);
			//
			//			logger.info("Registering to the topic: "+properties.getProperty(DEPLOYED_APPS_QUEUE));
			//
			//			Topic topic = session.createTopic(properties.getProperty(DEPLOYED_APPS_QUEUE));
			//
			//			logger.info("Creating subscriber on the channel: "+properties.getProperty(ACTIVEMQ_CHANNEL));
			//			//need to use createDurableSubscriber() method instead of createConsumer() for topic
			//			// MessageConsumer consumer = session.createConsumer(topic);
			//			MessageConsumer consumer = session.createDurableSubscriber(topic,
			//					properties.getProperty(ACTIVEMQ_CHANNEL));

			MessageListener listener = new MessageListener() {
				public void onMessage(Message message) {
					try {
						if (message instanceof TextMessage) {
							TextMessage textMessage = (TextMessage) message;
							logger.info("ACTIVEMQ: Received message in the deployed apps topic: "+ textMessage.getText());

							ApplicationManagerMessage amMessage = ModelConverter.jsonToApplicationManagerMessage(textMessage.getText());

							logger.info("Retrieving application Details from the Application Manager with these parameters: "+amMessage.getApplicationId()+", "+amMessage.getDeploymentId());
							String slaId = retrieveApplicationDetails(amMessage.getApplicationId(),amMessage.getDeploymentId());

							logger.info("Asking ApplicationMonitor to initiateMonitoring...");
							String topicId = initiateMonitoring(amMessage.getApplicationId(), amMessage.getDeploymentId(), slaId);

							logger.info("Creating an instance of Paas Violation Checker...");
							new Thread(new PaasViolationChecker(properties, topicId, amMessage.getApplicationId(), amMessage.getDeploymentId(), slaId, false)).start();

						}
					} catch (Exception e) {
						logger.error("Caught:" + e);
						e.printStackTrace();
					}
				}
			};

			receiver.setMessageConsumer(listener);
			//			consumer.setMessageListener(listener);
			//connection.close();
		}catch(Exception e){
			e.printStackTrace();
			logger.error(e.getMessage());
			//            System.err.println("NOT CONNECTED!!!");
		}
	}


	/**
	 * 2. Retrieve application details from the application manager whenever an 
	 * "application starting" event is catched
	 */
	private String retrieveApplicationDetails(String appId, String deploymentId) {
		String slaId = "UNKNOWN";

		try {
			HttpClient client = new DefaultHttpClient();
			String appManagerUrl = properties.getProperty(APPMANAGER_URL);
			HttpGet request = new HttpGet(appManagerUrl+"/applications/"+appId+"/deployments/"+deploymentId);
			HttpResponse response = client.execute(request);

			//			System.out.println("status: " + response.getStatusLine());
			//			System.out.println("headers: " + response.getAllHeaders());
			//			System.out.println("body:" + response.getEntity());

			HttpEntity entity = response.getEntity();
			String responseString = EntityUtils.toString(entity, "UTF-8");
			logger.debug("Response string from the ApplicationManager: "+responseString);

			Deployment deployment = ModelConverter.xmlDeploymentToObject(responseString);
			logger.debug("Deployment Id: "+deployment.getId());
			logger.debug("SLA Agreement: "+deployment.getSlaAgreement());

			slaId = deployment.getSlaAgreement();

		} catch (Exception e) {
			e.printStackTrace();
			slaId = "UNKNOWN";
		}

		return slaId;
	}


	/**
	 * 3. Asks the application monitor to initiate the monitoring on the given app, deployment
	 *  and SLA
	 * Returns the id of the topic where to read the measurements
	 */
	private String initiateMonitoring(String appId, String deploymentId, String slaId) {
		int monitoringFrequency = 10000; //msec
		
		//possiamo impostare l'intervallo di misura almeno al doppio della monitoringFrequency
		Timestamp Start_time =  new Timestamp(new java.util.Date().getTime()-2*monitoringFrequency);
		try {
			
			/*
			 * TODO: Verificare versione di ActiveMQ da utilizzare
			 */
//			AmqpMessageProducer producer = new AmqpMessageProducer("192.168.3.16:5673", "guest", "guest", "appmon", false);
			
			
			
						ConnectionFactory connectionFactory = new ActiveMQConnectionFactory(properties.getProperty(ACTIVEMQ_URL));
						javax.jms.Connection connection = connectionFactory.createConnection();
						connection.start();
			
						// JMS messages are sent and received using a Session. We will
						// create here a non-transactional session object. If you want
						// to use transactions you should set the first parameter to 'true'
						Session session = connection.createSession(false,
								Session.AUTO_ACKNOWLEDGE);
			
						Queue queue = session.createQueue("appmon");
			
						MessageProducer producer = session.createProducer(queue);
			
			
			

			
//			Context context;
//			//			ConnectionFactory connectionFActory;
//			TopicSession session;
//			MessageProducer messageProducer;
//
//			String topicName = "application-monitor.monitoring." + appId + ".measurement";
//
//			//        String nodeId = "TheSinusNode";
//
//			System.out.println("Initiating " + appId);
//			Properties p = new Properties();
//
//			String sepr = System.getProperty("file.separator");
//			String confPath = System.getenv("SLASOI_HOME");
//			p.load(new FileInputStream(confPath + sepr
//					+ "ascetic-slamanager" + sepr + "provisioning-adjustment" + sepr
//					+ "jndi.properties"));
//			p.put("topic.topic",topicName);
//			context = new InitialContext(p);
//
//			TopicConnectionFactory connectionFactory
//			= (TopicConnectionFactory) context.lookup("asceticpaas");
//
//
//			TopicConnection connection = 
//					connectionFactory.createTopicConnection();
//
//			session = connection.createTopicSession(false, 
//					Session.AUTO_ACKNOWLEDGE);
//
//
//			Queue sendQueue = (Queue) context.lookup("appmon");
//			Topic topic = (Topic) context.lookup("topic");
//
//
//			TopicSubscriber clientTopic = session.createSubscriber(topic);
//			connection.start();
//
//			messageProducer = 
//					session.createProducer(sendQueue);

			String termsString = "";
			String period = "";

			String[] monitorableTerms = properties.getProperty(MONITORABLE_TERMS).split(",");

			SLA sla = null;
			GetSLAClient gsc = new GetSLAClient(properties.getProperty(BUSINESS_REPORTING_URL),slaId);
			try {
				sla = gsc.getSLA();
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} 


			if (sla!=null) {
				List<MeasurableAgreementTerm> terms = gsc.getMeasurableTerms(sla);

				for (MeasurableAgreementTerm m:terms) {

					for (String monitorableTerm:monitorableTerms) {
						if (m.getName().equalsIgnoreCase(monitorableTerm)) {
							//gestione particolare termine parametrico
							if (m.getName().equalsIgnoreCase("aggregated_event_metric_over_period")) {

								for (AgreementTerm at:sla.getAgreementTerms()) {

									for (Guaranteed g:at.getGuarantees()) {

										if (g.toString().indexOf("aggregated_event_metric_over_period")>-1) {
											String[] parameters = g.toString().split("\"");

											switch (parameters[7]) {
											case "percentile": if (!termsString.equals("")) termsString+=", ";termsString+="\\\"percentile("+parameters[1]+"_"+parameters[3]+","+parameters[9]+")\\\"";period=""+60000*new Integer(parameters[5]);break;
											case "max": if (!termsString.equals("")) termsString+=", ";termsString+="\\\"max("+parameters[1]+"_"+parameters[3]+")\\\"";period=""+60000*new Integer(parameters[5]);break;
											case "last": if (!termsString.equals("")) termsString+=", ";termsString+="\\\"last("+parameters[1]+"_"+parameters[3]+")\\\"";break;
											}
										}
									}
								}
							}
							else {
								if (!termsString.equals("")) termsString+=", ";
								termsString+="\\\""+monitorableTerm+"\\\"";
							}
						}
					}
				}
			}

			logger.debug("Terms to be monitored: "+termsString);

			TextMessage message = session.createTextMessage("{\n" +
					"\t\"Command\" : \"initiateMonitoring\",\n" +
					"\t\"ApplicationId\" : \""+ appId + "\",\n" +
					"\t\"DeploymentId\" : \""+ deploymentId + "\",\n" +
					"\t\"SlaId\" : \""+ slaId + "\",\n" +
					(period.equals("")?"":"\t\"Period\" : \""+ period + "\",\n")+
					"\t\"Start_time\" : \""+ Start_time + "\",\n" +
					"\t\"Terms\" : ["+termsString+" ],\n" +
					"\t\"Frequency\" : "+monitoringFrequency+"\n" +
					"}");
//			messageProducer.send(message);
			
//			String message = "{\n" +
//					"\t\"Command\" : \"initiateMonitoring\",\n" +
//					"\t\"ApplicationId\" : \""+ appId + "\",\n" +
//					"\t\"DeploymentId\" : \""+ deploymentId + "\",\n" +
//					"\t\"SlaId\" : \""+ slaId + "\",\n" +
//					(period.equals("")?"":"\t\"Period\" : \""+ period + "\",\n")+
//					"\t\"Terms\" : ["+termsString+" ],\n" +
//					"\t\"Frequency\" : 10000\n" +
//					"}";
//			
//			producer.sendMessage(message);

			logger.debug("Message: "+message.getText());
			producer.send(message);
			
			logger.debug("Message sent: "+message);

			System.out.println("Message sent");


			connection.close();
//			context.close();
		}catch(Exception e){
			e.printStackTrace();
			logger.error(e.getMessage());

		}

		return "application-monitor.monitoring."+appId+".measurement";
	}





	/**
	 * Triggers the execution of a provisioning plan.
	 * 
	 * @param plan
	 *            the plan to be executed
	 * @throws PlanFoundException
	 *             if the plan has been already sent for execution
	 * @throws PlanFormatException
	 *             if the plan is not corrected built
	 */
	public void executePlan(Plan plan) throws PlanFoundException, PlanFormatException {
		logger.info("Execute plan " + plan.getPlanId());
	}

	/**
	 * Cancels the execution of a plan.
	 * 
	 * @param planId
	 *            the identifier of the plan to be cancelled
	 * @throws PlanNotFoundException
	 *             if the plan is not being executed
	 */
	public void cancelExecution(String planId) throws PlanNotFoundException {
		logger.info("Cancel plan " + planId);
	}

	/**
	 * POC informs the PAC that a given action is being executed.
	 * 
	 * @param planId
	 *            identifier of the plan affected by the action
	 * @param action
	 *            action being executed
	 * @param estimatedTime
	 *            estimation of time for the action to finish
	 * @throws PlanNotFoundException
	 *             if the plan is not under PAC's control
	 */
	public void ongoingAction(String planId, Task action, long estimatedTime) throws PlanNotFoundException {
		logger.info("Ongoing action " + action.getActionName() + " affecting plan " + planId);
	}

	/**
	 * Method to inform about the status of a given plan.
	 * 
	 * @param planId
	 *            the identifier of the plan
	 * @return the plan status.
	 * @throws PlanNotFoundException
	 */
	public Status getPlanStatus(String planId) throws PlanNotFoundException {
		logger.info("get status of plan " + planId);
		return Status.PROVISIONING;
	}

	/**
	 * Method to query the LLMS database.
	 * 
	 * @param ServiceManagerId
	 *            ID of the ServiceManager to forward the query
	 * @param query
	 *            database query
	 * @return the result of the query
	 */
	// To be changed once the ServiceManagers define the query interface
	public String queryMonitoringDatabase(String ServiceManagerId, String query) {
		logger.info("Executing query " + query + " in Service Manager " + ServiceManagerId);
		return "hello";
	}

	/**
	 * Set policies to the PAC. To be used from the business layer.
	 * 
	 * @param policyClassType
	 *            type of policy (Adjustment/Negotiation)
	 * @param policies
	 *            new policies
	 * @return result of the action
	 */
	public int setPolicies(String policyClassType, Policy[] policies) {
		logger.info("Setting policies ");
		for (Policy policy : policies) {
			logger.info(policy.toString());
		}

		return 0;
	}

	/**
	 * Get the policies used by the PAC. To be used from the business layer.
	 * 
	 * @param policyClassType
	 *            type of policy (Adjustment/Negotiation)
	 * @return policies the PAC policies
	 */
	public Policy[] getPolicies(String policyClassType) {
		logger.info("Getting policies");
		return null;
	}


	public static void main(String[] args) {
		String termsString = "";
		String period = "";
		String[] monitorableTerms = "power_usage_per_app,energy_usage_per_app,aggregated_event_metric_over_period".split(",");

		SLA sla = null;
		GetSLAClient gsc = new GetSLAClient("http://192.168.3.16:8080/services/BusinessManager_Reporting?wsdl","8f08b0f6-362c-4cd4-98ac-4a3d18578841");
		try {
			sla = gsc.getSLA();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} 


		if (sla!=null) {
			List<MeasurableAgreementTerm> terms = gsc.getMeasurableTerms(sla);

			for (MeasurableAgreementTerm m:terms) {

				for (String monitorableTerm:monitorableTerms) {
					if (m.getName().equalsIgnoreCase(monitorableTerm)) {
						//gestione particolare termine parametrico
						if (m.getName().equalsIgnoreCase("aggregated_event_metric_over_period")) {
							System.out.println(m);
							for (AgreementTerm at:sla.getAgreementTerms()) {

								for (Guaranteed g:at.getGuarantees()) {
									if (g.toString().indexOf("aggregated_event_metric_over_period")>-1) {
										String[] parameters = g.toString().split("\"");
										System.out.println(g.getPropertyKeys().length);
										for (STND s:g.getPropertyKeys()) {
											System.out.println(s.getValue());
											System.out.println(g.getPropertyValue(s));
										}
										
										switch (parameters[7]) {
										case "percentile": termsString+="\\\"percentile("+parameters[1]+"_"+parameters[3]+","+parameters[9]+")\\\"";period=""+60000*new Integer(parameters[5]);break;
										case "max": termsString+="\\\"max("+parameters[1]+"_"+parameters[3]+")\\\"";period=""+60000*new Integer(parameters[5]);break;
										case "last": termsString+="\\\"last("+parameters[1]+"_"+parameters[3]+")\\\"";break;
										}
									}
								}
							}
						}
						else {
							if (!termsString.equals("")) termsString+=", ";
							termsString+="\\\""+monitorableTerm+"\\\"";
						}
					}
				}
			}
		}
		System.out.println(termsString);
		System.out.println(period);
	}

}
