package eu.ascetic.asceticarchitecture.paas.component.energymodeller.internal.queue;

import java.util.Date;
import java.util.List;

import javax.jms.Connection;
import javax.jms.Destination;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageConsumer;
import javax.jms.MessageListener;
import javax.jms.MessageProducer;
import javax.jms.Session;
import javax.jms.TextMessage;

import org.apache.activemq.ActiveMQConnectionFactory;
import org.apache.log4j.Logger;

import eu.ascetic.asceticarchitecture.paas.component.energymodeller.datatype.messages.GenericEnergyMessage;
import eu.ascetic.asceticarchitecture.paas.component.energymodeller.internal.common.data.database.dao.EMSettings;
import eu.ascetic.asceticarchitecture.paas.component.energymodeller.internal.common.data.queue.MessageParserUtility;


public class QueueManager {

   
	private MessageProducer producerMeasurement;
	private MessageProducer producerPrediction;
	private Destination destinationPrediction;
	private Destination destinationMeasurement;
	private String monitoringTopic="MEASUREMENTS";
	private String predictionTopic="PREDICTION";
	private String monitoringQueueTopic="PEM.ENERGY";
	private Connection connection;
	private String user = "admin";
	private String password = "admin";
	private Session session;
	private final static Logger LOGGER = Logger.getLogger(QueueManager.class.getName());
	
	private String url = "tcp://10.15.5.55:61616";
	
	public void setup(EMSettings emsettings) throws Exception {
		setup(emsettings.getAmqpUrl(),emsettings.getAmqpUser(),emsettings.getAmqpPassword(),emsettings.getMonitoringQueueTopic());		
	}
	
	public void setup(String url, String username, String password,  String monitoringQueueTopic) throws Exception {
		
		if(username != null) {
			this.user = username;
		} 
		
		if(password != null) {
			this.password = password;
		}
		
		if(url != null) {
			this.url = url;
		}
		
		if(monitoringQueueTopic != null) {
			this.monitoringQueueTopic = monitoringQueueTopic;
		}
		
		// Create a ConnectionFactory
		ActiveMQConnectionFactory connectionFactory = new ActiveMQConnectionFactory(this.user,this.password,this.url);

		// Create a Connection
		connection = connectionFactory.createConnection();
		
		// Create a Session for each queue
		session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
        destinationPrediction = session.createTopic(this.monitoringQueueTopic+"."+this.predictionTopic);
        destinationMeasurement = session.createTopic(this.monitoringQueueTopic+"."+this.monitoringTopic);
		// Create a MessageProducer from the Session to the Queue
		producerPrediction = session.createProducer(destinationPrediction);
		producerMeasurement = session.createProducer(destinationMeasurement);
		
		// Start the connection
		connection.start();
		
	}
	
	public void sendMessage(String queue, String message){
		  
		try {
			if (queue=="prediction"){
				TextMessage messagetext = session.createTextMessage(message);
				producerPrediction.send( messagetext);
			} else {
				TextMessage messagetext = session.createTextMessage(message);
				producerMeasurement.send( messagetext);
			}
		} catch (JMSException e) {
			e.printStackTrace();
		}
		 
	}
	
	public void sendToQueue(String queue,String providerid,String applicationid, List<String> vms, String eventid, GenericEnergyMessage.Unit unit, String referenceTime,double value){
		
		GenericEnergyMessage message= new GenericEnergyMessage();
		message.setProvider(providerid);
		message.setApplicationid(applicationid);
		message.setEventid(eventid);
		Date data = new Date();
		message.setGenerattiontimestamp(data.toGMTString());
		// used to specify the time the forecast is referred to, 
		// but if refers to a measurement it is the same as the reference time (becayse it referes to the same time it has bee generated
		if (referenceTime==null){
			message.setReferredtimestamp(data.toGMTString());
		} else {
			message.setReferredtimestamp(referenceTime);
		}
		message.setVms(vms);
		message.setUnit(unit);
		message.setValue(value);
		sendMessage(queue, MessageParserUtility.buildStringMessage(message));
		LOGGER.info("EM queue manager has sent a message to "+queue);
		LOGGER.debug("EM queue manager built this message "+MessageParserUtility.buildStringMessage(message));
		
		
	}
	
	
	
	public void createConsumers(){
		
		MessageConsumer consumerPrediction;
		MessageConsumer consumerMeasurement;
		try {
			consumerMeasurement = session.createConsumer(destinationMeasurement);
			consumerPrediction = session.createConsumer(destinationPrediction);
		
	        MessageListener listner = new MessageListener() {
	            public void onMessage(Message message) {
	                try {
	                    if (message instanceof TextMessage) {
	                        TextMessage textMessage = (TextMessage) message;
	                        System.out.println("Received message"
	                                + textMessage.getText() + "'");
	                    }
	                } catch (JMSException e) {
	                    System.out.println("Caught:" + e);
	                    e.printStackTrace();
	                }
	            }
	        };
	
	        consumerMeasurement.setMessageListener(listner);
	        consumerPrediction.setMessageListener(listner);
		
		} catch (JMSException e1) {
			e1.printStackTrace();
		}

	}

    public void destroy() throws JMSException {

    	producerMeasurement.close();
    	producerPrediction.close();
        session.close();
        connection.close();
    }

}
