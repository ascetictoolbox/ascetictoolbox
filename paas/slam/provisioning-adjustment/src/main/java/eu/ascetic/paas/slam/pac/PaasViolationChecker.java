package eu.ascetic.paas.slam.pac;

import java.util.Calendar;
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
import org.slasoi.slamodel.sla.AgreementTerm;
import org.slasoi.slamodel.sla.Guaranteed;
import org.slasoi.slamodel.sla.SLA;
import org.slasoi.slamodel.sla.VariableDeclr;

import eu.ascetic.paas.slam.pac.applicationmanager.ModelConverter;
import eu.ascetic.paas.slam.pac.applicationmanager.amqp.model.ApplicationManagerMessage;
import eu.ascetic.paas.slam.pac.events.Value;
import eu.ascetic.paas.slam.pac.events.ViolationMessage;
import eu.ascetic.paas.slam.pac.events.ViolationMessage.Alert;
import eu.ascetic.paas.slam.pac.events.ViolationMessage.Alert.SlaGuaranteedState;
import eu.ascetic.paas.slam.pac.events.ViolationMessageTranslator;
import eu.ascetic.paas.slam.pac.impl.provider.reporting.GetSLAClient;

public class PaasViolationChecker implements Runnable {
	private static Logger logger = Logger.getLogger(PaasViolationChecker.class.getName());

	private Properties properties;
    protected static String ACTIVEMQ_URL = "activemq_url";
    private static String ACTIVEMQ_CHANNEL = "activemq_channel";
    private static String TERMINATED_APPS_QUEUE = "terminated_apps_queue";
    private static String BUSINESS_REPORTING_URL = "business_reporting_url";
    
    
	private String topicId;
	private String slaId;
	private String deploymentId;
	private String appId;
	private Connection measurementsConnection;
	private Connection applicationEventsConnection;

	public PaasViolationChecker(Properties properties, String topicId, String appId, String deploymentId, String slaId) {
		super();
		this.properties = properties;
		this.topicId = topicId;
		this.appId = appId;
		this.deploymentId = deploymentId;
		this.slaId = slaId;
	}

	public void run() {
		logger.info("New PaaS Violation Checker instantiated: topicId "+topicId+", appId "+appId+", slaId "+slaId);
		logger.info("Retrieving application events from the message queue...");
		retrieveApplicationEvents();

		logger.info("Retrieving measurements from the message queue...");
		retrieveMeasurements();
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
								/*
								 * TODO
								 */
							}


							logger.info("Notifying violation...");  
							
							/*
							 * dummy message, replace with real one
							 */
							//String violationMessage = "<ViolationMessage xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" appId=\"XXX\" deploymentId=\"4\"><time>2015-02-25T09:30:47.0Z</time>  <value id=\"free\">11</value>  <alert>    <type>violation</type>    <slaUUID>f28d4719-5f98-4c87-9365-6be602da9a4a</slaUUID>    <slaAgreementTerm>power_usage_per_app</slaAgreementTerm>    <slaGuaranteedState>      <guaranteedId>power_usage_per_app</guaranteedId>      <operator>less_than_or_equals</operator>      <guaranteedValue>10</guaranteedValue>    </slaGuaranteedState>  </alert></ViolationMessage>";
							ViolationMessage violationMessage = new ViolationMessage(Calendar.getInstance(),appId,deploymentId);
							Alert alert = violationMessage.new Alert();
							alert.setType("violation");
							alert.setSlaUUID(slaId);
							Value v = new Value("free", "11");
							violationMessage.setValue(v);
							alert.setSlaAgreementTerm(textMessage.getText());
							SlaGuaranteedState sgs = alert.new SlaGuaranteedState();
							sgs.setGuaranteedId(textMessage.getText());
							sgs.setGuaranteedValue(10.0);
							sgs.setOperator("less_than_or_equals");
							alert.setSlaGuaranteedState(sgs);
							violationMessage.setAlert(alert);
							
							ViolationMessageTranslator vmt = new ViolationMessageTranslator();
							notifyViolation(vmt.toXML(violationMessage));
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

			Topic topic = session.createTopic("paas-slam.monitoring"+slaId+"."+appId+".violationNotified");

			MessageProducer producer = session.createProducer(topic);


			TextMessage message = session.createTextMessage();

			message.setText(violationMessage);

			producer.send(message);
			logger.info("Sent message '" + message.getText() + "'");

			connection.close();
		}catch(Exception e){
			e.printStackTrace();
			logger.error(e.getMessage());

		}
	}

	/*
	 * parse sla test
	 */
	public static void main(String[] args) {
		logger.info("Getting SLA...");
		SLA sla = null;
		GetSLAClient gsc = new GetSLAClient("http://10.4.0.16:8080/services/BusinessManager_Reporting?wsdl", null);
		try {
			sla = gsc.getSLA();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} 
		
		if (sla!=null) {
			AgreementTerm[] terms = sla.getAgreementTerms();
			if (terms!=null) {
			for (AgreementTerm term:terms) {
//				System.out.println("Found Term "+term);
//				System.out.println("Precondition "+term.getPrecondition());
				if (term.getGuarantees()!=null) {
					for (Guaranteed g:term.getGuarantees()) {
						System.out.println(g.getId());
						
//						System.out.println(g.getPropertyKeys().length);
//						for (STND st:g.getPropertyKeys()) System.out.println(st);
						System.out.println("Garanzia "+g);
						
					}
				}
				if (term.getVariableDeclrs()!=null) {
					for (VariableDeclr v:term.getVariableDeclrs()) System.out.println("VariableDeclr "+v);
				}
			}
			}
		}
	}
}
