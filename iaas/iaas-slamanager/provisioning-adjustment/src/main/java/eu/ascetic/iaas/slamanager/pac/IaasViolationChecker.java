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

package eu.ascetic.iaas.slamanager.pac;

import java.io.IOException;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Properties;

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
import org.slasoi.slamodel.sla.SLA;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

import eu.ascetic.iaas.slamanager.pac.events.Value;
import eu.ascetic.iaas.slamanager.pac.events.ViolationMessage;
import eu.ascetic.iaas.slamanager.pac.events.ViolationMessage.Alert;
import eu.ascetic.iaas.slamanager.pac.events.ViolationMessage.Alert.SlaGuaranteedState;
import eu.ascetic.iaas.slamanager.pac.provider.reporting.GetSLAClient;
import eu.ascetic.iaas.slamanager.pac.provider.translation.AsceticAgreementTerm;
import eu.ascetic.iaas.slamanager.pac.provider.translation.MeasurableAgreementTerm;

public class IaasViolationChecker implements Runnable {
	private static Logger logger = Logger.getLogger(IaasViolationChecker.class.getName());

	private Properties properties;
	protected static String ACTIVEMQ_URL = "activemq_url";
	private static String ACTIVEMQ_CHANNEL = "activemq_channel";
	private static String TERMINATED_VMS_QUEUE = "terminated_vms_queue";
	private static String BUSINESS_REPORTING_URL = "business_reporting_url";
	private static String MONITORABLE_TERMS = "monitorable_terms";

	private String topicId;
	private String slaId;
	private String ovfId;
	private String vmId;
	private Connection measurementsConnection;
	private Connection vmEventsConnection;

    public static final String FIELD_VM_ID = "id";
	public static final String FIELD_OVF_ID = "ovfId";
	public static final String FIELD_SLA_ID = "slaId";
	
	
	public IaasViolationChecker(Properties properties, String topicId, String vmId, String ovfId, String slaId) {
		super();
		this.topicId = topicId;
		this.vmId = vmId;
		this.ovfId = ovfId;
		this.slaId = slaId;
		this.properties = properties;
	}

	public void run() {
		logger.info("New IaaS Violation Checker instantiated: topicId "+topicId+", vmId "+vmId+", slaId "+slaId);
		logger.info("Retrieving VM events from the message queue...");
		retrieveVmEvents();

		logger.info("Retrieving measurements from the message queue...");
		retrieveMeasurements();
	}


	/**
	 * 1. Gets 'application terminated' events from the message queue
	 * When an application terminated event is notified, the monitoring is stopped.
	 */
	private void retrieveVmEvents() {
		try{

			// Getting JMS connection from the server

			ConnectionFactory connectionFactory = new ActiveMQConnectionFactory(properties.getProperty(ACTIVEMQ_URL));
			vmEventsConnection = connectionFactory.createConnection();

			// need to setClientID value, any string value you wish
			vmEventsConnection.setClientID("IaaS Violation Checker VM Event Listener "+System.currentTimeMillis());


			vmEventsConnection.start();

			Session session = vmEventsConnection.createSession(false,
					Session.AUTO_ACKNOWLEDGE);

			Topic topic = session.createTopic(properties.getProperty(TERMINATED_VMS_QUEUE));

			//need to use createDurableSubscriber() method instead of createConsumer() for topic
			// MessageConsumer consumer = session.createConsumer(topic);
			MessageConsumer consumer = session.createDurableSubscriber(topic,
					properties.getProperty(ACTIVEMQ_CHANNEL));

			MessageListener listener = new MessageListener() {
				public void onMessage(Message message) {
					try {
						if (message instanceof TextMessage) {
							TextMessage textMessage = (TextMessage) message;
							logger.info("ACTIVEMQ: Received message in the terminated vms queue: '"
									+ textMessage.getText());
							
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
							
							String messageVmId = "";
							String messageOvfId = "";
							String messageSlaId = "";
							
							Iterator<String> rootNames = msgBody.fieldNames();
							while(rootNames.hasNext()){
								String fieldName = rootNames.next();
								String fieldValue = msgBody.get(fieldName).asText();
								if (fieldName.equalsIgnoreCase(FIELD_VM_ID)) {
									messageVmId = fieldValue;
								}
								if (fieldName.equalsIgnoreCase(FIELD_OVF_ID)) {
									messageOvfId = fieldValue;
								}
								if (fieldName.equalsIgnoreCase(FIELD_SLA_ID)) {
									messageSlaId = fieldValue;
								}
							}
	                        logger.debug("VM details: id "+vmId+", ovfId "+ovfId+", slaId "+slaId);

	                        if (vmId.equalsIgnoreCase(messageVmId) &&
									ovfId.equalsIgnoreCase(messageOvfId) &&
									slaId.equalsIgnoreCase(messageSlaId)) {
								/*
								 * TODO: confronto anche su slaId?
								 */
								logger.info("Closing message queue connections and stopping the monitoring...");
								measurementsConnection.close();
								vmEventsConnection.close();
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
			//            System.err.println("NOT CONNECTED!!!");
		}
	}

	/**
	 * 2. Retrieves the measurements for the given vm, ovf and SLA,
	 * and compares them with thresholds.
	 */
	private void retrieveMeasurements() {
		try{

			// Getting JMS connection from the server

			ConnectionFactory connectionFactory = new ActiveMQConnectionFactory(properties.getProperty(ACTIVEMQ_URL));
			measurementsConnection = connectionFactory.createConnection();

			// need to setClientID value, any string value you wish
			measurementsConnection.setClientID("IaaS Violation Checker "+System.currentTimeMillis());


			measurementsConnection.start();

			Session session = measurementsConnection.createSession(false,
					Session.AUTO_ACKNOWLEDGE);

			Topic topic = session.createTopic(topicId);

			//need to use createDurableSubscriber() method instead of createConsumer() for topic
			// MessageConsumer consumer = session.createConsumer(topic);
			MessageConsumer consumer = session.createDurableSubscriber(topic,
					properties.getProperty(ACTIVEMQ_CHANNEL));

			MessageListener listener = new MessageListener() {
				public void onMessage(Message message) {
					try {
						if (message instanceof TextMessage) {
							TextMessage textMessage = (TextMessage) message;
							logger.info("ACTIVEMQ: Received measurement '"
									+ textMessage.getText() + "' in the topic "+topicId);
							
							
//							{              
//				                “name”:<String>,             //energy or power
//				                “value”: <double>,
//				                “units”:<String>,
//				                “timestamp”:<long>
//							}
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
							
							//TODO: prendere dal nome topic
							String measuredApplicationId = "";
							String measuredTimestamp = "";
							String measuredTerm = "";
							Map<String,String> measuredTerms = new HashMap<String,String>();

							Iterator<String> rootNames = msgBody.fieldNames();
							while(rootNames.hasNext()){
								String fieldName = rootNames.next();
								String fieldValue = msgBody.get(fieldName).asText();
								
								if (fieldName.equalsIgnoreCase("timestamp")) {
									measuredTimestamp = fieldValue;
								}
								if (fieldName.equalsIgnoreCase("name")) {
									if ("energy".equalsIgnoreCase(fieldValue)) measuredTerm = "energy_usage_per_vm";
									else if ("power".equalsIgnoreCase(fieldValue)) measuredTerm = "power_usage_per_vm";
								}
								if (fieldName.equalsIgnoreCase("value")) {
									measuredTerms.put(measuredTerm, fieldValue);
									logger.debug("Measurement --> "+measuredTerm+" : "+fieldValue);
								}

							}


							logger.info("Getting SLA...");
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
									boolean violated = false;
									
									
									String[] monitorableTerms = properties.getProperty(MONITORABLE_TERMS).split(",");
									
									for (String monitorableTerm:monitorableTerms) {
										if (m.getName().equalsIgnoreCase(monitorableTerm)) {
											if (measuredTerms.containsKey(monitorableTerm)) {
											if (m.getOperator().equals(AsceticAgreementTerm.operatorType.EQUALS)) {
												if (!m.getValue().equals(new Double(measuredTerms.get(monitorableTerm)))) {
													logger.debug("Violation detected. Value: "+measuredTerms.get(monitorableTerm)+" Condition: "+m); violated = true;
												}
											}
											else if (m.getOperator().equals(AsceticAgreementTerm.operatorType.GREATER)) {
												if (!(m.getValue()<(new Double(measuredTerms.get(monitorableTerm))))) {
													logger.debug("Violation detected. Value: "+measuredTerms.get(monitorableTerm)+" Condition: "+m); violated = true;
												}
											}
											else if (m.getOperator().equals(AsceticAgreementTerm.operatorType.GREATER_EQUAL)) {
												if (!(m.getValue()<=(new Double(measuredTerms.get(monitorableTerm))))) {
													logger.debug("Violation detected. Value: "+measuredTerms.get(monitorableTerm)+" Condition: "+m); violated = true;
												}
											}
											else if (m.getOperator().equals(AsceticAgreementTerm.operatorType.LESS)) {
												if (!(m.getValue()>(new Double(measuredTerms.get(monitorableTerm))))) {
													logger.debug("Violation detected. Value: "+measuredTerms.get(monitorableTerm)+" Condition: "+m); violated = true;
												}
											}
											else if (m.getOperator().equals(AsceticAgreementTerm.operatorType.LESS_EQUAL)) {
												if (!(m.getValue()>=(new Double(measuredTerms.get(monitorableTerm))))) {
													logger.debug("Violation detected. Value: "+measuredTerms.get(monitorableTerm)+" Condition: "+m); violated = true;
												}
											}
										}
											if (violated) {
												logger.info("Notifying violation...");  
												
												//String violationMessage = "<ViolationMessage xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" appId=\"XXX\" deploymentId=\"4\"><time>2015-02-25T09:30:47.0Z</time>  <value id=\"free\">11</value>  <alert>    <type>violation</type>    <slaUUID>f28d4719-5f98-4c87-9365-6be602da9a4a</slaUUID>    <slaAgreementTerm>power_usage_per_app</slaAgreementTerm>    <slaGuaranteedState>      <guaranteedId>power_usage_per_app</guaranteedId>      <operator>less_than_or_equals</operator>      <guaranteedValue>10</guaranteedValue>    </slaGuaranteedState>  </alert></ViolationMessage>";
												ViolationMessage violationMessage = new ViolationMessage(Calendar.getInstance(),vmId,ovfId);
												Alert alert = violationMessage.new Alert();
												alert.setType("violation");
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
												notifyViolation(vmt.toXML(violationMessage));
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
			//            System.err.println("NOT CONNECTED!!!");
		}
	}



	/**
	 * 3. Writes a violation to the message queue
	 */
	private void notifyViolation(String violationMessage) {
		try{

			ConnectionFactory connectionFactory = new ActiveMQConnectionFactory(properties.getProperty(ACTIVEMQ_URL));
			Connection connection = connectionFactory.createConnection();
			connection.start();

			// JMS messages are sent and received using a Session. We will
			// create here a non-transactional session object. If you want
			// to use transactions you should set the first parameter to 'true'
			Session session = connection.createSession(false,
					Session.AUTO_ACKNOWLEDGE);

			Topic topic = session.createTopic("iaas-slam.monitoring"+slaId+"."+vmId+".violationNotified");

			MessageProducer producer = session.createProducer(topic);


			TextMessage message = session.createTextMessage();

			message.setText(violationMessage);
			// Here we are sending the message!
			producer.send(message);
			logger.info("Sent message '" + message.getText() + "'");

			connection.close();
		}catch(Exception e){
			e.printStackTrace();
			logger.error(e.getMessage());
			//            System.err.println("NOT CONNECTED!!!");
		}
	}


}
