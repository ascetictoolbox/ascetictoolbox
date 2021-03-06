/**
 *  Copyright 2015 Hewlett-Packard Development Company, L.P.
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

package eu.ascetic.paas.slam.pac;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Scanner;

import javax.jms.Connection;
import javax.jms.ConnectionFactory;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageConsumer;
import javax.jms.MessageListener;
import javax.jms.MessageProducer;
import javax.jms.Session;
import javax.jms.TextMessage;
import javax.jms.Topic;

import org.apache.activemq.ActiveMQConnectionFactory;
import org.apache.log4j.Logger;
import org.slasoi.slamodel.primitives.STND;
import org.slasoi.slamodel.sla.AgreementTerm;
import org.slasoi.slamodel.sla.Guaranteed;
import org.slasoi.slamodel.sla.SLA;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

import eu.ascetic.paas.slam.pac.amqp.AmqpMessageProducer;
import eu.ascetic.paas.slam.pac.amqp.AmqpMessageReceiver;
import eu.ascetic.paas.slam.pac.applicationmanager.ModelConverter;
import eu.ascetic.paas.slam.pac.applicationmanager.amqp.model.ApplicationManagerMessage;
import eu.ascetic.paas.slam.pac.events.Value;
import eu.ascetic.paas.slam.pac.events.ViolationMessage;
import eu.ascetic.paas.slam.pac.events.ViolationMessage.Alert;
import eu.ascetic.paas.slam.pac.events.ViolationMessage.Alert.SlaGuaranteedState;
import eu.ascetic.paas.slam.pac.events.ViolationMessageTranslator;
import eu.ascetic.paas.slam.pac.impl.provider.reporting.GetSLAClient;
import eu.ascetic.paas.slam.pac.impl.provider.translation.AsceticAgreementTerm;
import eu.ascetic.paas.slam.pac.impl.provider.translation.MeasurableAgreementTerm;

public class PaasViolationChecker implements Runnable {
	private static Logger logger = Logger.getLogger(PaasViolationChecker.class.getName());



	private Properties properties;
	protected static String ACTIVEMQ_URL = "activemq_url";
	private static String ACTIVEMQ_CHANNEL = "activemq_channel";
	private static String TERMINATED_APPS_QUEUE = "terminated_apps_queue";
	private static String BUSINESS_REPORTING_URL = "business_reporting_url";
	private static String MONITORABLE_TERMS = "monitorable_terms";
	private static String NOTIFICATION_INTERVAL = "notification_interval";
	private static String MARGIN_OF_ERROR = "margin_of_error";

	public static final String FIELD_APP_ID = "ApplicationId";
	public static final String FIELD_DEPLOYMENT_ID = "DeploymentId";
	public static final String FIELD_TERMS = "data";
	public static final String FIELD_TERMS_2 = "Terms";
	public static final String FIELD_TIMESTAMP = "Timestamp";
	public static final String FIELD_SLA_ID = "SLAId";


	private String topicId;
	private String slaId;
	private String deploymentId;
	private String appId;
	private Connection measurementsConnection;
	private Connection applicationEventsConnection;
	private Connection renegotiationConnection;
	private boolean recovered;

	public long violationNotificationTime = 0;
	public long warningNotificationTime = 0;

	public PaasViolationChecker(Properties properties, String topicId, String appId, String deploymentId, String slaId, boolean recovered) {
		super();
		this.properties = properties;
		this.topicId = topicId;
		this.appId = appId;
		this.deploymentId = deploymentId;
		this.slaId = slaId;
		this.recovered = recovered;
	}

	public void run() {
		logger.info("New PaaS Violation Checker instantiated: topicId "+topicId+", appId "+appId+", deploymentId "+deploymentId+", slaId "+slaId);

		//write monitoring info to file
		String monitoringString = topicId + "%%%" + appId + "%%%" + deploymentId + "%%%"+slaId;

		String sepr = System.getProperty("file.separator");
		String confPath = System.getenv("SLASOI_HOME");
		String monitoringPath = confPath + sepr	+ "ascetic-slamanager" + sepr + "provisioning-adjustment" + sepr + "activeMonitorings";
		File monitoringFile = new File(monitoringPath);

		Scanner scanner = null;
		PrintWriter writer = null;
		try {
			scanner = new Scanner(monitoringFile);
			writer = new PrintWriter(new FileWriter(monitoringFile,true));

			boolean found = false;
			while (scanner.hasNextLine()) 
			{
				String lineFromFile = scanner.nextLine();
				if (monitoringString.equals(lineFromFile))
				{
					found = true;
					break;
				}
			}

			if (!found) {
				writer.write(monitoringString+ System.getProperty("line.separator"));
				writer.close();
				scanner.close();
			}
			else {
				if (!recovered) {
					logger.info("PaaS Violation Checker : topicId "+topicId+", appId "+appId+", deploymentId "+deploymentId+", slaId "+slaId + " is still being monitored. Exiting...");
					if (writer!=null) writer.close();
					if (scanner!=null) scanner.close();
					return;
				}
				if (writer!=null) writer.close();
				if (scanner!=null) scanner.close();
			}
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			if (writer!=null) writer.close();
			if (scanner!=null) scanner.close();
		}
		//end

		logger.info("Retrieving application events from the message queue...");
		retrieveApplicationEvents();

		logger.info("Retrieving measurements from the message queue...");
		retrieveMeasurements();
//		retrieveMeasurementsPusher();
		
		logger.info("Retrieving estimations from the message queue...");
		retrieveEstimations();

		logger.info("Retrieving renegotiation events from the message queue...");
		retrieveRenegotiationEvents();
	}


	/**
	 * 1. Gets 'application terminated' events from the message queue
	 * When an application terminated event is notified, the monitoring is stopped.
	 */
	private void retrieveApplicationEvents() {
		try{

			// Getting JMS connection from the server

			ConnectionFactory connectionFactory = new ActiveMQConnectionFactory(properties.getProperty(ACTIVEMQ_URL));
			applicationEventsConnection = connectionFactory.createConnection();


			applicationEventsConnection.setClientID("PaaS Violation Checker Application Event Listener "+System.currentTimeMillis());


			applicationEventsConnection.start();

			Session session = applicationEventsConnection.createSession(false,
					Session.AUTO_ACKNOWLEDGE);

			Topic topic = session.createTopic(properties.getProperty(TERMINATED_APPS_QUEUE));

			MessageConsumer consumer = session.createDurableSubscriber(topic,
					properties.getProperty(ACTIVEMQ_CHANNEL));

			MessageListener listener = new MessageListener() {
				public void onMessage(Message message) {
					try {
						if (message instanceof TextMessage) {
							TextMessage textMessage = (TextMessage) message;
							logger.info("ACTIVEMQ: Received message in the terminated apps queue: "+ textMessage.getText() );

							//se il messaggio riguarda appId,slaId e deploymentId relativi a questo ViolationChecker, interrompo il monitoring
							ApplicationManagerMessage amMessage = ModelConverter.jsonToApplicationManagerMessage(textMessage.getText());

							if (appId.equalsIgnoreCase(amMessage.getApplicationId()) &&
									deploymentId.equalsIgnoreCase(amMessage.getDeploymentId())) {
								/*
								 * TODO: confronto anche su slaId?
								 */
								logger.info("Closing message queue connections and stopping the monitoring...");
								measurementsConnection.close();
								applicationEventsConnection.close();

								//remove monitoring info to file
								String monitoringString = topicId + "%%%" + appId + "%%%" + deploymentId + "%%%"+slaId;
								logger.info("Removing monitoring info from file, entry: "+monitoringString );

								String sepr = System.getProperty("file.separator");
								String confPath = System.getenv("SLASOI_HOME");
								String monitoringPath = confPath + sepr	+ "ascetic-slamanager" + sepr + "provisioning-adjustment" + sepr + "activeMonitorings";
								String tempPath = confPath + sepr	+ "ascetic-slamanager" + sepr + "provisioning-adjustment" + sepr + "temp";
								File monitoringFile = new File(monitoringPath);
								File tempFile = new File(tempPath);
								try {
									BufferedReader reader = new BufferedReader(new FileReader(monitoringFile));
									BufferedWriter writer = new BufferedWriter(new FileWriter(tempFile));

									String currentLine;

									while((currentLine = reader.readLine()) != null) {
										// trim newline when comparing with lineToRemove
										String trimmedLine = currentLine.trim();
										if(trimmedLine.equals(monitoringString)) {

											continue;
										}
										writer.write(currentLine + System.getProperty("line.separator"));
									}
									reader.close(); 
									writer.close(); 
									monitoringFile.setWritable(true);
									boolean rimozione = monitoringFile.delete();
									if (rimozione) {
										boolean successful = tempFile.renameTo(monitoringFile);
									}
								} catch (Exception ex) {
									ex.printStackTrace();
									logger.error(ex.getMessage());
								}
								//end
								return;
							}

						}
					} catch (JMSException e) {
						logger.error("Caught:" + e);
						e.printStackTrace();
					}
				}
			};
			consumer.setMessageListener(listener);
			//connection.close();
		}catch(Exception e){
			e.printStackTrace();
			logger.error(e.getMessage());
		}
	}




	/**
	 * 2. Retrieves the measurements for the given app, dep and SLA,
	 * and compares them with thresholds.
	 */
	private void retrieveMeasurements() {
		try{
			logger.debug("Attivo la raccolta di misurazioni sulla coda "+topicId);
			AmqpMessageReceiver receiver = new AmqpMessageReceiver("192.168.3.222:5673", "guest", "guest",  topicId, true);

			
			
			
			
			// Getting JMS connection from the server

//			ConnectionFactory connectionFactory = new ActiveMQConnectionFactory(properties.getProperty(ACTIVEMQ_URL));
//			measurementsConnection = connectionFactory.createConnection();
//
//			measurementsConnection.setClientID("PaaS Violation Checker "+System.currentTimeMillis());
//
//
//			measurementsConnection.start();
//
//			Session session = measurementsConnection.createSession(false,
//					Session.AUTO_ACKNOWLEDGE);
//
//			Topic topic = session.createTopic(topicId);
//
//			MessageConsumer consumer = session.createDurableSubscriber(topic,
//					properties.getProperty(ACTIVEMQ_CHANNEL));

			MessageListener listener = new MessageListener() {
				public void onMessage(Message message) {
					try {
						if (message instanceof TextMessage) {
							TextMessage textMessage = (TextMessage) message;
							logger.info("ACTIVEMQ: Received measurement '"
									+ textMessage.getText() + "' in the topic "+topicId);

							//							{"ApplicationId":"SinusApp","Timestamp":1431592067367,"Terms":{"metric":9.862471417356321}}

							ObjectMapper mapper = new ObjectMapper(); 
							ObjectNode msgBody = null;
							try {
								msgBody = (ObjectNode) mapper.readTree(textMessage.getText());
							} catch (JsonProcessingException e) {
								// TODO Auto-generated catch block
								e.printStackTrace();
							} catch (IOException e) {
								// TODO Auto-generated catch block
								e.printStackTrace();
							}

							String measuredApplicationId = "";
							String measuredTimestamp = "";
							String measuredDeploymentId = "";
							String measuredSlaId = "";

							Iterator<String> rootNames = msgBody.fieldNames();
							while(rootNames.hasNext()){
								String fieldName = rootNames.next();
								String fieldValue = msgBody.get(fieldName).asText();
								if (fieldName.equalsIgnoreCase(FIELD_APP_ID)) {
									measuredApplicationId = fieldValue;
								}
								if (fieldName.equalsIgnoreCase(FIELD_TIMESTAMP)) {
									measuredTimestamp = fieldValue;
								}
								if (fieldName.equalsIgnoreCase(FIELD_DEPLOYMENT_ID)) {
									measuredDeploymentId = fieldValue;
								}
								if (fieldName.equalsIgnoreCase(FIELD_SLA_ID)) {
									measuredSlaId = fieldValue;
								}
							}
							
							if (!measuredDeploymentId.equalsIgnoreCase(deploymentId)) {
								logger.debug("Deployment Id is different, skipping...");
								return;
							}

							JsonNode termsJson = msgBody.get(FIELD_TERMS);
							if (termsJson==null || termsJson.size()<1 || termsJson.fieldNames()==null)  {
								termsJson = msgBody.get(FIELD_TERMS_2);
							}
							Map<String,String> measuredTerms = new HashMap<String,String>();

							Iterator<String> fieldNames = termsJson.fieldNames();
							while(fieldNames.hasNext()){
								String fieldName = fieldNames.next();
								String fieldValue = termsJson.get(fieldName).asText();
								logger.debug("Measurement --> "+fieldName+" : "+fieldValue);

								if (fieldName.equalsIgnoreCase("powerConsumption")) fieldName = "power_usage_per_app";
								else if (fieldName.equalsIgnoreCase("energyConsumption")) fieldName = "energy_usage_per_app";
								
								measuredTerms.put(fieldName, fieldValue);
							}


							logger.info("Getting SLA...(SLA ID "+slaId+")");
							SLA sla = null;
							GetSLAClient gsc = new GetSLAClient(properties.getProperty(BUSINESS_REPORTING_URL),slaId);
							try {
								sla = gsc.getSLA();
							} catch (Exception e) {
								// TODO Auto-generated catch block
								e.printStackTrace();
							} 

							if (sla!=null) {
								logger.info("Comparing measurement with the threshold...");
								List<MeasurableAgreementTerm> terms = gsc.getMeasurableTerms(sla);



								for (MeasurableAgreementTerm m:terms) {
									logger.debug("m.getName(): "+m.getName());
									boolean violated = false;


									String[] monitorableTerms = properties.getProperty(MONITORABLE_TERMS).split(",");

									/*
									 * margin of error included
									 */
									Double marginOfError = new Double(properties.getProperty(MARGIN_OF_ERROR));

									for (String monitorableTerm:monitorableTerms) {
										String monitorableTerm2 = monitorableTerm;
										logger.debug("Found monitorable term "+monitorableTerm);
										
										if (m.getName().equalsIgnoreCase(monitorableTerm)) {

											//gestione particolare termine parametrico
											if (m.getName().equalsIgnoreCase("aggregated_event_metric_over_period")) {
												logger.debug("Caso termine aggregato");
												
												for (AgreementTerm at:sla.getAgreementTerms()) {

													for (Guaranteed g:at.getGuarantees()) {
														logger.debug(g.toString());
														if (g.toString().indexOf("aggregated_event_metric_over_period")>-1) {
															String[] parameters = g.toString().split("\"");
															logger.debug("parametro7 "+parameters[7]);
															switch (parameters[7]) {
															case "percentile": monitorableTerm2="percentile_"+parameters[1]+"_"+parameters[3]+"_"+parameters[9];break;
															case "max": monitorableTerm2="max_"+parameters[1]+"_"+parameters[3];break;
															case "last": monitorableTerm2="last_"+parameters[1]+"_"+parameters[3];break;
															}
														}
													}
												}
											}
											logger.debug("monitorable term 2 aggiornato "+monitorableTerm2);
											
											
											if (measuredTerms.containsKey(monitorableTerm2)) {
												logger.debug("measuredTerms.containsKey(monitorableTerm2)");
												if (m.getOperator().equals(AsceticAgreementTerm.operatorType.EQUALS)) {
													logger.debug(m.getOperator());
													if (!m.getValue().equals(new Double(measuredTerms.get(monitorableTerm2)))) {
														logger.debug("Violation detected. Value: "+measuredTerms.get(monitorableTerm2)+" Condition: "+m); violated = true;
													}
												}
												else if (m.getOperator().equals(AsceticAgreementTerm.operatorType.GREATER)) {
													logger.debug(m.getOperator());
													logger.debug("m.getValue "+m.getValue());
													logger.debug("measuredTerms" +measuredTerms.get(monitorableTerm2));
													if (!(m.getValue() <(new Double(measuredTerms.get(monitorableTerm2))  + marginOfError))) {
														logger.debug("Violation detected. Value: "+measuredTerms.get(monitorableTerm2)+" Condition: "+m); violated = true;
													}
													else logger.debug("non risulta violazione");
												}
												else if (m.getOperator().equals(AsceticAgreementTerm.operatorType.GREATER_EQUAL)) {
													logger.debug(m.getOperator());
													if (!(m.getValue()+marginOfError <=(new Double(measuredTerms.get(monitorableTerm2)) + marginOfError))) {
														logger.debug("Violation detected. Value: "+measuredTerms.get(monitorableTerm2)+" Condition: "+m); violated = true;
													}
												}
												else if (m.getOperator().equals(AsceticAgreementTerm.operatorType.LESS)) {
													logger.debug(m.getOperator());
													if (!(m.getValue() + marginOfError >(new Double(measuredTerms.get(monitorableTerm2))))) {
														logger.debug("Violation detected. Value: "+measuredTerms.get(monitorableTerm2)+" Condition: "+m); violated = true;
													}
												}
												else if (m.getOperator().equals(AsceticAgreementTerm.operatorType.LESS_EQUAL)) {
													logger.debug(m.getOperator());
													if (!(m.getValue() + marginOfError >=(new Double(measuredTerms.get(monitorableTerm2))))) {
														logger.debug("Violation detected. Value: "+measuredTerms.get(monitorableTerm2)+" Condition: "+m); violated = true;
													}
												}
											}
											if (violated) {
												logger.info("Notifying violation...");  

												//String violationMessage = "<ViolationMessage xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" appId=\"XXX\" deploymentId=\"4\"><time>2015-02-25T09:30:47.0Z</time>  <value id=\"free\">11</value>  <alert>    <type>violation</type>    <slaUUID>f28d4719-5f98-4c87-9365-6be602da9a4a</slaUUID>    <slaAgreementTerm>power_usage_per_app</slaAgreementTerm>    <slaGuaranteedState>      <guaranteedId>power_usage_per_app</guaranteedId>      <operator>less_than_or_equals</operator>      <guaranteedValue>10</guaranteedValue>    </slaGuaranteedState>  </alert></ViolationMessage>";
												ViolationMessage violationMessage = new ViolationMessage(Calendar.getInstance(),appId,deploymentId);
												Alert alert = violationMessage.new Alert();
												alert.setType("violation");

												/*
												 * gestione information messages
												 */
												for (AgreementTerm at:sla.getAgreementTerms()) {

													for (Guaranteed g:at.getGuarantees()) {

														if (g.toString().indexOf(m.getName())>-1) {
															if (g.getPropertyKeys()!=null && g.getPropertyKeys().length>0) {
																for (STND s:g.getPropertyKeys()) {
																	if (s.getValue().equalsIgnoreCase("violation_type")) {
																		alert.setType(g.getPropertyValue(s));
																	}
																}
															}
														}
													}
												}
												/*
												 * fine gestione information messages
												 */

												alert.setSlaUUID(slaId);
												Value v = new Value("usage", measuredTerms.get(monitorableTerm2));
												violationMessage.setValue(v);
												alert.setSlaAgreementTerm(monitorableTerm2);
												SlaGuaranteedState sgs = alert.new SlaGuaranteedState();
												sgs.setGuaranteedId(monitorableTerm);
												sgs.setGuaranteedValue(m.getValue());
												sgs.setOperator(m.getOperator().toString());
												alert.setSlaGuaranteedState(sgs);
												violationMessage.setAlert(alert);

												ViolationMessageTranslator vmt = new ViolationMessageTranslator();
												notifyViolation(vmt.toXML(violationMessage), "violation");
											}
										}
									}

								}

							}

						}
					} catch (JMSException e) {
						logger.error("Caught:" + e);
						e.printStackTrace();
					}
				}
			};

//			consumer.setMessageListener(listener);
			receiver.setMessageConsumer(listener);
			//connection.close();
		}catch(Exception e){
			e.printStackTrace();
			logger.error(e.getMessage());
		}
	}

	
	
	/**
	 * 2. Retrieves the measurements for the given app, dep and SLA,
	 * and compares them with thresholds.
	 */
	private void retrieveMeasurementsPusher() {
		try{
			logger.debug("Attivo la raccolta di misurazioni in modalita' pusher sulla coda "+topicId);
//			AmqpMessageReceiver receiver = new AmqpMessageReceiver("192.168.3.222:5673", "guest", "guest",  topicId, true);

			
			
			
			
			// Getting JMS connection from the server

			ConnectionFactory connectionFactory = new ActiveMQConnectionFactory(properties.getProperty(ACTIVEMQ_URL));
			measurementsConnection = connectionFactory.createConnection();

			measurementsConnection.setClientID("PaaS Violation Checker "+System.currentTimeMillis());


			measurementsConnection.start();

			Session session = measurementsConnection.createSession(false,
					Session.AUTO_ACKNOWLEDGE);

			Topic topic = session.createTopic(topicId);

			MessageConsumer consumer = session.createDurableSubscriber(topic,
					properties.getProperty(ACTIVEMQ_CHANNEL));

			MessageListener listener = new MessageListener() {
				public void onMessage(Message message) {
					try {
						if (message instanceof TextMessage) {
							TextMessage textMessage = (TextMessage) message;
							logger.info("ACTIVEMQ: Received measurement '"
									+ textMessage.getText() + "' in the topic "+topicId);

							//							{"ApplicationId":"SinusApp","Timestamp":1431592067367,"Terms":{"metric":9.862471417356321}}

							ObjectMapper mapper = new ObjectMapper(); 
							ObjectNode msgBody = null;
							try {
								msgBody = (ObjectNode) mapper.readTree(textMessage.getText());
							} catch (JsonProcessingException e) {
								// TODO Auto-generated catch block
								e.printStackTrace();
							} catch (IOException e) {
								// TODO Auto-generated catch block
								e.printStackTrace();
							}

							String measuredApplicationId = "";
							String measuredTimestamp = "";
							String measuredDeploymentId = "";
							String measuredSlaId = "";

							Iterator<String> rootNames = msgBody.fieldNames();
							while(rootNames.hasNext()){
								String fieldName = rootNames.next();
								String fieldValue = msgBody.get(fieldName).asText();
								if (fieldName.equalsIgnoreCase(FIELD_APP_ID)) {
									measuredApplicationId = fieldValue;
								}
								if (fieldName.equalsIgnoreCase(FIELD_TIMESTAMP)) {
									measuredTimestamp = fieldValue;
								}
								if (fieldName.equalsIgnoreCase(FIELD_DEPLOYMENT_ID)) {
									measuredDeploymentId = fieldValue;
								}
								if (fieldName.equalsIgnoreCase(FIELD_SLA_ID)) {
									measuredSlaId = fieldValue;
								}
							}
							
							if (!measuredDeploymentId.equalsIgnoreCase(deploymentId)) {
								logger.debug("Deployment Id is different, skipping...");
								return;
							}

							JsonNode termsJson = msgBody.get(FIELD_TERMS);
							if (termsJson==null || termsJson.size()<1 || termsJson.fieldNames()==null)  {
								termsJson = msgBody.get(FIELD_TERMS_2);
							}
							Map<String,String> measuredTerms = new HashMap<String,String>();

							Iterator<String> fieldNames = termsJson.fieldNames();
							while(fieldNames.hasNext()){
								String fieldName = fieldNames.next();
								String fieldValue = termsJson.get(fieldName).asText();
								logger.debug("Measurement --> "+fieldName+" : "+fieldValue);

								if (fieldName.equalsIgnoreCase("powerConsumption")) fieldName = "power_usage_per_app";
								else if (fieldName.equalsIgnoreCase("energyConsumption")) fieldName = "energy_usage_per_app";
								else if (fieldName.equalsIgnoreCase("priceConsumption")) fieldName = "app_price_for_next_hour";
								
								measuredTerms.put(fieldName, fieldValue);
							}


							logger.info("Getting SLA...(SLA ID "+slaId+")");
							SLA sla = null;
							GetSLAClient gsc = new GetSLAClient(properties.getProperty(BUSINESS_REPORTING_URL),slaId);
							try {
								sla = gsc.getSLA();
							} catch (Exception e) {
								// TODO Auto-generated catch block
								e.printStackTrace();
							} 

							if (sla!=null) {
								logger.info("Comparing measurement with the threshold...");
								List<MeasurableAgreementTerm> terms = gsc.getMeasurableTerms(sla);



								for (MeasurableAgreementTerm m:terms) {
									logger.debug("m.getName(): "+m.getName());
									boolean violated = false;


									String[] monitorableTerms = properties.getProperty(MONITORABLE_TERMS).split(",");

									/*
									 * margin of error included
									 */
									Double marginOfError = new Double(properties.getProperty(MARGIN_OF_ERROR));

									for (String monitorableTerm:monitorableTerms) {
										String monitorableTerm2 = monitorableTerm;
										logger.debug("Found monitorable term "+monitorableTerm);
										
										if (m.getName().equalsIgnoreCase(monitorableTerm)) {

											//gestione particolare termine parametrico
											if (m.getName().equalsIgnoreCase("aggregated_event_metric_over_period")) {
												logger.debug("Caso termine aggregato");
												
												for (AgreementTerm at:sla.getAgreementTerms()) {

													for (Guaranteed g:at.getGuarantees()) {
														logger.debug(g.toString());
														if (g.toString().indexOf("aggregated_event_metric_over_period")>-1) {
															String[] parameters = g.toString().split("\"");
															logger.debug("parametro7 "+parameters[7]);
															switch (parameters[7]) {
															case "percentile": monitorableTerm2="percentile_"+parameters[1]+"_"+parameters[3]+"_"+parameters[9];break;
															case "max": monitorableTerm2="max_"+parameters[1]+"_"+parameters[3];break;
															case "last": monitorableTerm2="last_"+parameters[1]+"_"+parameters[3];break;
															}
														}
													}
												}
											}
											logger.debug("monitorable term 2 aggiornato "+monitorableTerm2);
											
											
											if (measuredTerms.containsKey(monitorableTerm2)) {
												logger.debug("measuredTerms.containsKey(monitorableTerm2)");
												if (m.getOperator().equals(AsceticAgreementTerm.operatorType.EQUALS)) {
													logger.debug(m.getOperator());
													if (!m.getValue().equals(new Double(measuredTerms.get(monitorableTerm2)))) {
														logger.debug("Violation detected. Value: "+measuredTerms.get(monitorableTerm2)+" Condition: "+m); violated = true;
													}
												}
												else if (m.getOperator().equals(AsceticAgreementTerm.operatorType.GREATER)) {
													logger.debug(m.getOperator());
													logger.debug("m.getValue "+m.getValue());
													logger.debug("measuredTerms" +measuredTerms.get(monitorableTerm2));
													if (!(m.getValue() <(new Double(measuredTerms.get(monitorableTerm2))  + marginOfError))) {
														logger.debug("Violation detected. Value: "+measuredTerms.get(monitorableTerm2)+" Condition: "+m); violated = true;
													}
													else logger.debug("non risulta violazione");
												}
												else if (m.getOperator().equals(AsceticAgreementTerm.operatorType.GREATER_EQUAL)) {
													logger.debug(m.getOperator());
													if (!(m.getValue()+marginOfError <=(new Double(measuredTerms.get(monitorableTerm2)) + marginOfError))) {
														logger.debug("Violation detected. Value: "+measuredTerms.get(monitorableTerm2)+" Condition: "+m); violated = true;
													}
												}
												else if (m.getOperator().equals(AsceticAgreementTerm.operatorType.LESS)) {
													logger.debug(m.getOperator());
													if (!(m.getValue() + marginOfError >(new Double(measuredTerms.get(monitorableTerm2))))) {
														logger.debug("Violation detected. Value: "+measuredTerms.get(monitorableTerm2)+" Condition: "+m); violated = true;
													}
												}
												else if (m.getOperator().equals(AsceticAgreementTerm.operatorType.LESS_EQUAL)) {
													logger.debug(m.getOperator());
													if (!(m.getValue() + marginOfError >=(new Double(measuredTerms.get(monitorableTerm2))))) {
														logger.debug("Violation detected. Value: "+measuredTerms.get(monitorableTerm2)+" Condition: "+m); violated = true;
													}
												}
											}
											if (violated) {
												logger.info("Notifying violation...");  

												//String violationMessage = "<ViolationMessage xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" appId=\"XXX\" deploymentId=\"4\"><time>2015-02-25T09:30:47.0Z</time>  <value id=\"free\">11</value>  <alert>    <type>violation</type>    <slaUUID>f28d4719-5f98-4c87-9365-6be602da9a4a</slaUUID>    <slaAgreementTerm>power_usage_per_app</slaAgreementTerm>    <slaGuaranteedState>      <guaranteedId>power_usage_per_app</guaranteedId>      <operator>less_than_or_equals</operator>      <guaranteedValue>10</guaranteedValue>    </slaGuaranteedState>  </alert></ViolationMessage>";
												ViolationMessage violationMessage = new ViolationMessage(Calendar.getInstance(),appId,deploymentId);
												Alert alert = violationMessage.new Alert();
												alert.setType("violation");

												/*
												 * gestione information messages
												 */
												for (AgreementTerm at:sla.getAgreementTerms()) {

													for (Guaranteed g:at.getGuarantees()) {

														if (g.toString().indexOf(m.getName())>-1) {
															if (g.getPropertyKeys()!=null && g.getPropertyKeys().length>0) {
																for (STND s:g.getPropertyKeys()) {
																	if (s.getValue().equalsIgnoreCase("violation_type")) {
																		alert.setType(g.getPropertyValue(s));
																	}
																}
															}
														}
													}
												}
												/*
												 * fine gestione information messages
												 */

												alert.setSlaUUID(slaId);
												Value v = new Value("usage", measuredTerms.get(monitorableTerm2));
												violationMessage.setValue(v);
												alert.setSlaAgreementTerm(monitorableTerm2);
												SlaGuaranteedState sgs = alert.new SlaGuaranteedState();
												sgs.setGuaranteedId(monitorableTerm);
												sgs.setGuaranteedValue(m.getValue());
												sgs.setOperator(m.getOperator().toString());
												alert.setSlaGuaranteedState(sgs);
												violationMessage.setAlert(alert);

												ViolationMessageTranslator vmt = new ViolationMessageTranslator();
												notifyViolation(vmt.toXML(violationMessage), "violation");
											}
										}
									}

								}

							}

						}
					} catch (JMSException e) {
						logger.error("Caught:" + e);
						e.printStackTrace();
					}
				}
			};

			consumer.setMessageListener(listener);
//			receiver.setMessageConsumer(listener);
			//connection.close();
		}catch(Exception e){
			e.printStackTrace();
			logger.error(e.getMessage());
		}
	}

	

	/**
	 * 3. Gets 'renegotiation' events from the message queue
	 */
	private void retrieveRenegotiationEvents() {
		try{
			// Getting JMS connection from the server

			ConnectionFactory connectionFactory = new ActiveMQConnectionFactory(properties.getProperty(ACTIVEMQ_URL));
			renegotiationConnection = connectionFactory.createConnection();

			// need to setClientID value, any string value you wish
			renegotiationConnection.setClientID("IaaS Violation Checker Renegotiation Listener "+System.currentTimeMillis());


			renegotiationConnection.start();

			final Session session = renegotiationConnection.createSession(false,
					Session.AUTO_ACKNOWLEDGE);

			final Topic topic = session.createTopic("paas-slam.monitoring.*.renegotiated");

			//need to use createDurableSubscriber() method instead of createConsumer() for topic
			// MessageConsumer consumer = session.createConsumer(topic);
			MessageConsumer consumer = session.createDurableSubscriber(topic,
					properties.getProperty(ACTIVEMQ_CHANNEL));

			MessageListener listener = new MessageListener() {
				public void onMessage(Message message) {
					if (message.toString().indexOf(slaId)==-1) return;

					try {
						if (message instanceof TextMessage) {
							TextMessage textMessage = (TextMessage) message;
							logger.info("ACTIVEMQ: Received message in the renegotiated apps queue. Actual SLAId: "+slaId+ ", New SLAId:"
									+ textMessage.getText());


							/*
							 * change string in activemonitorings file
							 */

							//remove monitoring info to file
							String monitoringString = topicId + "%%%" + appId + "%%%" + deploymentId + "%%%"+slaId;
							logger.info("Removing monitoring info from file, entry: "+monitoringString );

							String sepr = System.getProperty("file.separator");
							String confPath = System.getenv("SLASOI_HOME");
							String monitoringPath = confPath + sepr	+ "ascetic-iaas-slamanager" + sepr + "provisioning-adjustment" + sepr + "activeMonitorings";
							String tempPath = confPath + sepr	+ "ascetic-iaas-slamanager" + sepr + "provisioning-adjustment" + sepr + "temp";
							File monitoringFile = new File(monitoringPath);
							File tempFile = new File(tempPath);
							try {
								BufferedReader reader = new BufferedReader(new FileReader(monitoringFile));
								BufferedWriter writer = new BufferedWriter(new FileWriter(tempFile));

								String currentLine;

								while((currentLine = reader.readLine()) != null) {
									// trim newline when comparing with lineToRemove
									String trimmedLine = currentLine.trim();
									if(trimmedLine.equals(monitoringString)) {

										continue;
									}
									writer.write(currentLine + System.getProperty("line.separator"));
								}
								reader.close(); 
								writer.close(); 
								monitoringFile.setWritable(true);
								boolean rimozione = monitoringFile.delete();
								if (rimozione) {
									boolean successful = tempFile.renameTo(monitoringFile);
								}
							} catch (Exception ex) {
								ex.printStackTrace();
								logger.error(ex.getMessage());
							}
							//end


							slaId = textMessage.getText();
							logger.info("SLAID changed to "+slaId);


							//write new monitoring info to file
							monitoringString = topicId + "%%%" + appId + "%%%" + deploymentId + "%%%"+slaId;

							sepr = System.getProperty("file.separator");
							confPath = System.getenv("SLASOI_HOME");
							monitoringPath = confPath + sepr	+ "ascetic-iaas-slamanager" + sepr + "provisioning-adjustment" + sepr + "activeMonitorings";
							monitoringFile = new File(monitoringPath);

							Scanner scanner = null;
							PrintWriter writer = null;
							try {
								scanner = new Scanner(monitoringFile);
								writer = new PrintWriter(new FileWriter(monitoringFile,true));

								boolean found = false;
								while (scanner.hasNextLine()) 
								{
									String lineFromFile = scanner.nextLine();
									if (monitoringString.equals(lineFromFile))
									{
										found = true;
										break;
									}
								}

								if (!found) {
									writer.write(monitoringString+ System.getProperty("line.separator"));
									writer.close();
									scanner.close();
								}
								else {
									if (!recovered) {
										logger.info("PaaS Violation Checker : topicId "+topicId+", appId "+appId+", deploymentId "+deploymentId+", slaId "+slaId + " is still being monitored. Exiting...");
										if (writer!=null) writer.close();
										if (scanner!=null) scanner.close();
										return;
									}
									if (writer!=null) writer.close();
									if (scanner!=null) scanner.close();
								}
							} catch (Exception e) {
								// TODO Auto-generated catch block
								e.printStackTrace();
								if (writer!=null) writer.close();
								if (scanner!=null) scanner.close();
							}
							//end	


							//monitoring the new renegotiation queue
							//retrieveRenegotiationEvents();

						}
					} catch (JMSException e) {
						logger.error("Caught:" + e);
						e.printStackTrace();
					}
				}
			};
			consumer.setMessageListener(listener);
			//connection.close();
		}catch(Exception e){
			e.printStackTrace();
			logger.error(e.getMessage());
			//            System.err.println("NOT CONNECTED!!!");
		}
	}


	/**
	 * 3. Writes a violation to the message queue
	 */
	private void notifyViolation(String violationMessage, String type) {
		
		if (type.equalsIgnoreCase("violation")) {
		/*
		 * verify if the same violation has been notified recently
		 */
		long seconds = System.currentTimeMillis() / 1000;
		if (violationNotificationTime!=0) {	
			if (new Long(properties.getProperty(NOTIFICATION_INTERVAL))!=0 && seconds-violationNotificationTime<new Long(properties.getProperty(NOTIFICATION_INTERVAL))) {
				logger.info("Seconds: "+seconds+", violationNotificationTime: "+violationNotificationTime+": The violation has already been notified recently, skipping...");
				return;
			}
		}
		violationNotificationTime = seconds;
		}
		
		else {
			/*
			 * verify if the same violation has been notified recently
			 */
			long seconds = System.currentTimeMillis() / 1000;
			if (warningNotificationTime!=0) {	
				if (new Long(properties.getProperty(NOTIFICATION_INTERVAL))!=0 && seconds-warningNotificationTime<new Long(properties.getProperty(NOTIFICATION_INTERVAL))) {
					logger.info("Seconds: "+seconds+", warningNotificationTime: "+warningNotificationTime+": The warning has already been notified recently, skipping...");
					return;
				}
			}
			warningNotificationTime = seconds;	
		}
		/*
		 * 
		 */

		try{

			AmqpMessageProducer producer = new AmqpMessageProducer("192.168.3.222:5673", "guest", "guest", "paas-slam.monitoring."+slaId+"."+appId+".violationNotified", true);

			producer.sendMessage(violationMessage);

			//			ConnectionFactory connectionFactory = new ActiveMQConnectionFactory(properties.getProperty(ACTIVEMQ_URL));
			//			Connection connection = connectionFactory.createConnection();
			//			connection.start();
			//
			//			// JMS messages are sent and received using a Session. We will
			//			// create here a non-transactional session object. If you want
			//			// to use transactions you should set the first parameter to 'true'
			//			Session session = connection.createSession(false,
			//					Session.AUTO_ACKNOWLEDGE);
			//
			//			Topic topic = session.createTopic("paas-slam.monitoring."+slaId+"."+appId+".violationNotified");
			//
			//			MessageProducer producer = session.createProducer(topic);
			//
			//
			//			TextMessage message = session.createTextMessage();
			//
			//			message.setText(violationMessage);
			//
			//			producer.send(message);
			//			logger.info("Sent message '" + message.getText() + "'");
			//
			//			connection.close();

			producer.close();
		}catch(Exception e){
			e.printStackTrace();
			logger.error(e.getMessage());

		}
	}

	//	/*
	//	 * parse sla test
	//	 */
	//	public static void main(String[] args) {
	//
	//		String TERM_POWER_USAGE_PER_APP = "power_usage_per_app";
	//		String TERM_ENERGY_USAGE_PER_APP = "energy_usage_per_app";
	//
	//		logger.info("Getting SLA...");
	//		SLA sla = null;
	//		HashMap<String,String> measuredValues = new HashMap<String,String>();		
	//		measuredValues.put("power_usage_per_app", "45");
	//		GetSLAClient gsc = new GetSLAClient("http://10.4.0.16:8080/services/BusinessManager_Reporting?wsdl", null);
	//		try {
	//			sla = gsc.getSLA();
	//			List<MeasurableAgreementTerm> mis = gsc.getMeasurableTerms(sla);
	//			for (MeasurableAgreementTerm m:mis) {
	//				boolean violated = false;
	//
	//
	//				if (m.getName().equalsIgnoreCase(TERM_POWER_USAGE_PER_APP)) {
	//					if (measuredValues.containsKey(TERM_POWER_USAGE_PER_APP)) {
	//						if (m.getOperator().equals(AsceticAgreementTerm.operatorType.EQUALS)) {
	//							if (!m.getValue().equals(new Double(measuredValues.get(TERM_POWER_USAGE_PER_APP)))) {
	//								System.out.println("Violation detected. Value: "+measuredValues.get(TERM_POWER_USAGE_PER_APP)+" Condition: "+m); violated = true;
	//							}
	//						}
	//						else if (m.getOperator().equals(AsceticAgreementTerm.operatorType.GREATER)) {
	//							if (!(m.getValue()<(new Double(measuredValues.get(TERM_POWER_USAGE_PER_APP))))) {
	//								System.out.println("Violation detected. Value: "+measuredValues.get(TERM_POWER_USAGE_PER_APP)+" Condition: "+m); violated = true;
	//							}
	//						}
	//						else if (m.getOperator().equals(AsceticAgreementTerm.operatorType.GREATER_EQUAL)) {
	//							if (!(m.getValue()<=(new Double(measuredValues.get(TERM_POWER_USAGE_PER_APP))))) {
	//								System.out.println("Violation detected. Value: "+measuredValues.get(TERM_POWER_USAGE_PER_APP)+" Condition: "+m); violated = true;
	//							}
	//						}
	//						else if (m.getOperator().equals(AsceticAgreementTerm.operatorType.LESS)) {
	//							if (!(m.getValue()>(new Double(measuredValues.get(TERM_POWER_USAGE_PER_APP))))) {
	//								System.out.println("Violation detected. Value: "+measuredValues.get(TERM_POWER_USAGE_PER_APP)+" Condition: "+m); violated = true;
	//							}
	//						}
	//						else if (m.getOperator().equals(AsceticAgreementTerm.operatorType.LESS_EQUAL)) {
	//							if (!(m.getValue()>=(new Double(measuredValues.get(TERM_POWER_USAGE_PER_APP))))) {
	//								System.out.println("Violation detected. Value: "+measuredValues.get(TERM_POWER_USAGE_PER_APP)+" Condition: "+m); violated = true;
	//							}
	//						}
	//					}
	//				}
	//
	//				if (m.getName().equalsIgnoreCase(TERM_ENERGY_USAGE_PER_APP)) {
	//					if (measuredValues.containsKey(TERM_ENERGY_USAGE_PER_APP)) {
	//						if (m.getOperator().equals(AsceticAgreementTerm.operatorType.EQUALS)) {
	//							if (!m.getValue().equals(new Double(measuredValues.get(TERM_ENERGY_USAGE_PER_APP)))) {
	//								System.out.println("Violation detected. Value: "+measuredValues.get(measuredValues)+" Condition: "+m); violated = true;
	//							}
	//						}
	//						else if (m.getOperator().equals(AsceticAgreementTerm.operatorType.GREATER)) {
	//							if (!(m.getValue()<(new Double(measuredValues.get(TERM_ENERGY_USAGE_PER_APP))))) {
	//								System.out.println("Violation detected. Value: "+measuredValues.get(measuredValues)+" Condition: "+m); violated = true;
	//							}
	//						}
	//						else if (m.getOperator().equals(AsceticAgreementTerm.operatorType.GREATER_EQUAL)) {
	//							if (!(m.getValue()<=(new Double(measuredValues.get(TERM_ENERGY_USAGE_PER_APP))))) {
	//								System.out.println("Violation detected. Value: "+measuredValues.get(measuredValues)+" Condition: "+m); violated = true;
	//							}
	//						}
	//						else if (m.getOperator().equals(AsceticAgreementTerm.operatorType.LESS)) {
	//							if (!(m.getValue()>(new Double(measuredValues.get(TERM_ENERGY_USAGE_PER_APP))))) {
	//								System.out.println("Violation detected. Value: "+measuredValues.get(measuredValues)+" Condition: "+m); violated = true;
	//							}
	//						}
	//						else if (m.getOperator().equals(AsceticAgreementTerm.operatorType.LESS_EQUAL)) {
	//							if (!(m.getValue()>=(new Double(measuredValues.get(TERM_ENERGY_USAGE_PER_APP))))) {
	//								System.out.println("Violation detected. Value: "+measuredValues.get(measuredValues)+" Condition: "+m); violated = true;
	//							}
	//						}
	//					}
	//				}
	//			}
	//		} catch (Exception e) {
	//			// TODO Auto-generated catch block
	//			e.printStackTrace();
	//		} 
	//	}

	/**
	 * 2. Retrieves the estimations for the given app, dep and SLA,
	 * and compares them with thresholds.
	 */
	private void retrieveEstimations() {
		try{
	
			// Getting JMS connection from the server
	
			ConnectionFactory connectionFactory = new ActiveMQConnectionFactory(properties.getProperty(ACTIVEMQ_URL));
			measurementsConnection = connectionFactory.createConnection();
	
			measurementsConnection.setClientID("PaaS Violation Checker "+System.currentTimeMillis());
	
	
			measurementsConnection.start();
	
			Session session = measurementsConnection.createSession(false,
					Session.AUTO_ACKNOWLEDGE);
	
			Topic topic = session.createTopic("application-monitor.monitoring."+appId+"."+deploymentId+".estimation");
	
			MessageConsumer consumer = session.createDurableSubscriber(topic,
					properties.getProperty(ACTIVEMQ_CHANNEL));
	
			MessageListener listener = new MessageListener() {
				public void onMessage(Message message) {
					try {
						if (message instanceof TextMessage) {
							TextMessage textMessage = (TextMessage) message;
							logger.info("ACTIVEMQ: Received estimation '"
									+ textMessage.getText() + "' in the topic application-monitor.monitoring."+appId+"."+deploymentId+".estimation");
	
							//							{"ApplicationId":"SinusApp","Timestamp":1431592067367,"Terms":{"metric":9.862471417356321}}
	
							ObjectMapper mapper = new ObjectMapper(); 
							ObjectNode msgBody = null;
							try {
								msgBody = (ObjectNode) mapper.readTree(textMessage.getText());
							} catch (JsonProcessingException e) {
								// TODO Auto-generated catch block
								e.printStackTrace();
							} catch (IOException e) {
								// TODO Auto-generated catch block
								e.printStackTrace();
							}
	
							String measuredApplicationId = "";
							String measuredTimestamp = "";
							String measuredDeploymentId = "";
							String measuredSlaId = "";
	
							Iterator<String> rootNames = msgBody.fieldNames();
							while(rootNames.hasNext()){
								String fieldName = rootNames.next();
								String fieldValue = msgBody.get(fieldName).asText();
								if (fieldName.equalsIgnoreCase(FIELD_APP_ID)) {
									measuredApplicationId = fieldValue;
								}
								if (fieldName.equalsIgnoreCase(FIELD_TIMESTAMP)) {
									measuredTimestamp = fieldValue;
								}
								if (fieldName.equalsIgnoreCase(FIELD_DEPLOYMENT_ID)) {
									measuredDeploymentId = fieldValue;
								}
								if (fieldName.equalsIgnoreCase(FIELD_SLA_ID)) {
									measuredSlaId = fieldValue;
								}
							}
	
							JsonNode termsJson = msgBody.get(FIELD_TERMS);
							Map<String,String> measuredTerms = new HashMap<String,String>();
	
							Iterator<String> fieldNames = termsJson.fieldNames();
							while(fieldNames.hasNext()){
								String fieldName = fieldNames.next();
								String fieldValue = termsJson.get(fieldName).asText();
								logger.debug("Estimation --> "+fieldName+" : "+fieldValue);
	
								if (fieldName.equalsIgnoreCase("powerEstimation")) fieldName = "power_usage_per_app";
								else if (fieldName.equalsIgnoreCase("energyEstimation")) fieldName = "energy_usage_per_app";
								else if (fieldName.equalsIgnoreCase("priceEstimation")) fieldName = "app_price_for_next_hour";
								
								measuredTerms.put(fieldName, fieldValue);
							}
	
	
							logger.info("Getting SLA...(SLA ID "+slaId+")");
							SLA sla = null;
							GetSLAClient gsc = new GetSLAClient(properties.getProperty(BUSINESS_REPORTING_URL),slaId);
							try {
								sla = gsc.getSLA();
							} catch (Exception e) {
								// TODO Auto-generated catch block
								e.printStackTrace();
							} 
	
							if (sla!=null) {
								logger.info("Comparing estimation with the threshold...");
								List<MeasurableAgreementTerm> terms = gsc.getMeasurableTerms(sla);
	
	
	
								for (MeasurableAgreementTerm m:terms) {
									boolean violated = false;
	
	
									String[] monitorableTerms = properties.getProperty(MONITORABLE_TERMS).split(",");
	
									/*
									 * margin of error included
									 */
									Double marginOfError = new Double(properties.getProperty(MARGIN_OF_ERROR));
	
									for (String monitorableTerm:monitorableTerms) {
										if (m.getName().equalsIgnoreCase(monitorableTerm)) {
	
											//gestione particolare termine parametrico
											if (m.getName().equalsIgnoreCase("aggregated_event_metric_over_period")) {
	
												for (AgreementTerm at:sla.getAgreementTerms()) {
	
													for (Guaranteed g:at.getGuarantees()) {
	
														if (g.toString().indexOf("aggregated_event_metric_over_period")>-1) {
															String[] parameters = g.toString().split("\"");
	
															switch (parameters[7]) {
															case "percentile": monitorableTerm="percentile_"+parameters[1]+"_"+parameters[3]+"_"+parameters[9];break;
															case "max": monitorableTerm="max_"+parameters[1]+"_"+parameters[3];break;
															case "last": monitorableTerm="last_"+parameters[1]+"_"+parameters[3];break;
															}
														}
													}
												}
											}
	
											if (measuredTerms.containsKey(monitorableTerm)) {
												if (m.getOperator().equals(AsceticAgreementTerm.operatorType.EQUALS)) {
													if (!m.getValue().equals(new Double(measuredTerms.get(monitorableTerm)))) {
														logger.debug("Warning detected. Value: "+measuredTerms.get(monitorableTerm)+" Condition: "+m); violated = true;
													}
												}
												else if (m.getOperator().equals(AsceticAgreementTerm.operatorType.GREATER)) {
													if (!(m.getValue() <(new Double(measuredTerms.get(monitorableTerm))  + marginOfError))) {
														logger.debug("Warning detected. Value: "+measuredTerms.get(monitorableTerm)+" Condition: "+m); violated = true;
													}
												}
												else if (m.getOperator().equals(AsceticAgreementTerm.operatorType.GREATER_EQUAL)) {
													if (!(m.getValue()+marginOfError <=(new Double(measuredTerms.get(monitorableTerm)) + marginOfError))) {
														logger.debug("Warning detected. Value: "+measuredTerms.get(monitorableTerm)+" Condition: "+m); violated = true;
													}
												}
												else if (m.getOperator().equals(AsceticAgreementTerm.operatorType.LESS)) {
													if (!(m.getValue() + marginOfError >(new Double(measuredTerms.get(monitorableTerm))))) {
														logger.debug("Warning detected. Value: "+measuredTerms.get(monitorableTerm)+" Condition: "+m); violated = true;
													}
												}
												else if (m.getOperator().equals(AsceticAgreementTerm.operatorType.LESS_EQUAL)) {
													if (!(m.getValue() + marginOfError >=(new Double(measuredTerms.get(monitorableTerm))))) {
														logger.debug("Warning detected. Value: "+measuredTerms.get(monitorableTerm)+" Condition: "+m); violated = true;
													}
												}
											}
											if (violated) {
												logger.info("Notifying Warning...");  
	
												//String violationMessage = "<ViolationMessage xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" appId=\"XXX\" deploymentId=\"4\"><time>2015-02-25T09:30:47.0Z</time>  <value id=\"free\">11</value>  <alert>    <type>violation</type>    <slaUUID>f28d4719-5f98-4c87-9365-6be602da9a4a</slaUUID>    <slaAgreementTerm>power_usage_per_app</slaAgreementTerm>    <slaGuaranteedState>      <guaranteedId>power_usage_per_app</guaranteedId>      <operator>less_than_or_equals</operator>      <guaranteedValue>10</guaranteedValue>    </slaGuaranteedState>  </alert></ViolationMessage>";
												Calendar calendar = Calendar.getInstance();
												calendar.setTimeInMillis(new Long(measuredTimestamp));
														
												ViolationMessage violationMessage = new ViolationMessage(calendar,appId,deploymentId);
												Alert alert = violationMessage.new Alert();
												alert.setType("warning");
	
												alert.setSlaUUID(slaId);
												Value v = new Value("usage", measuredTerms.get(monitorableTerm));
												violationMessage.setValue(v);
												alert.setSlaAgreementTerm(monitorableTerm);
												SlaGuaranteedState sgs = alert.new SlaGuaranteedState();
												sgs.setGuaranteedId(monitorableTerm);
												sgs.setGuaranteedValue(m.getValue());
												sgs.setOperator(m.getOperator().toString());
												alert.setSlaGuaranteedState(sgs);
												violationMessage.setAlert(alert);
	
												ViolationMessageTranslator vmt = new ViolationMessageTranslator();
												notifyViolation(vmt.toXML(violationMessage),"warning");
											}
										}
									}
	
								}
	
							}
	
						}
					} catch (JMSException e) {
						logger.error("Caught:" + e);
						e.printStackTrace();
					}
				}
			};
	
			consumer.setMessageListener(listener);
			//connection.close();
		}catch(Exception e){
			e.printStackTrace();
			logger.error(e.getMessage());
		}
	}

	public static void main(String[] args) {
		logger.info("Getting SLA...(SLA ID "+"b467bd04-c1b2-4ec4-a198-f3de1091df1c"+")");
		SLA sla = null;
		GetSLAClient gsc = new GetSLAClient("http://192.168.3.222:8080/services/BusinessManager_Reporting?wsdl","b467bd04-c1b2-4ec4-a198-f3de1091df1c");
		try {
			sla = gsc.getSLA();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} 

		if (sla!=null) {
			logger.info("Comparing measurement with the threshold...");
			List<MeasurableAgreementTerm> terms = gsc.getMeasurableTerms(sla);



			for (MeasurableAgreementTerm m:terms) {
				System.out.println("m.getName(): "+m.getName());
			}
		}
	}


	}


