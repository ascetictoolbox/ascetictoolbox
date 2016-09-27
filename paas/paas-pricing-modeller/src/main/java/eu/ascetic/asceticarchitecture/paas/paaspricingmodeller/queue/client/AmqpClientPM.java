package eu.ascetic.asceticarchitecture.paas.paaspricingmodeller.queue.client;

import java.util.Properties;
import java.util.UUID;

import javax.jms.Connection;
import javax.jms.ConnectionFactory;
import javax.jms.Destination;
import javax.jms.ExceptionListener;
import javax.jms.JMSException;
import javax.jms.MessageConsumer;
import javax.jms.MessageListener;
import javax.jms.MessageProducer;
import javax.jms.Session;
import javax.jms.TextMessage;
import javax.naming.Context;
import javax.naming.InitialContext;

import org.apache.log4j.Logger;


public class AmqpClientPM {

   
	//Message producer
	private MessageProducer producerMeasurements;
	private MessageProducer producerPrediction;
	
	//Destination
	private Destination destinationMeasurements;
	private Destination destinationPrediction;
	
	
	//Topics
	private String billingTopic="PMBILLING";
	private String predictionTopic="PMPREDICTION";
	private String pmQueueTopic="PRICING";	
	//Connection details
	private ConnectionFactory factory;
	private Connection connection;
	private String user = "admin";
	private String password = "admin";
	private Session session;
	
	private final static Logger logger = Logger.getLogger(AmqpClientPM.class.getName());
	
	//Local
	//private String url = "localhost:5672";
	//AM
	private String url = "localhost:5673";
	
	//setting up the queue
	public void setup(String url, String username, String password,  String pmQueueTopic) throws Exception {
		
		if(username != null) {
			this.user = username;
		} 
		
		if(password != null) {
			this.password = password;
		}
		
		if(url != null) {
			this.url = url;
		}
		
		if(pmQueueTopic != null) {
			this.pmQueueTopic = pmQueueTopic;
		}
		
		String initialContextFactory = "org.apache.qpid.jms.jndi.JmsInitialContextFactory";
		
		String connectionJNDIName = UUID.randomUUID().toString();
		String connectionURL = "amqp://" + this.user + ":" + this.password + "@" + this.url;
		this.pmQueueTopic = pmQueueTopic.replaceAll("\\.", "");
		String topicName = pmQueueTopic;
		
		// Set the properties ...
		Properties properties = new Properties();
		properties.put(Context.INITIAL_CONTEXT_FACTORY, initialContextFactory);
		properties.put("connectionfactory."+connectionJNDIName , connectionURL);

		properties.put("topic"+"."+pmQueueTopic+"."+billingTopic , billingTopic);
		properties.put("topic"+"."+pmQueueTopic+"."+predictionTopic , predictionTopic);
		
		javax.naming.Context context = new InitialContext(properties);

        factory = (ConnectionFactory) context.lookup(connectionJNDIName);
        connection = factory.createConnection(this.user, this.password);
        connection.setExceptionListener(new MyExceptionListener());
        connection.start();
		session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
	//	System.out.println("here");
		
		// Create a Session for each queue
     
        destinationPrediction = (Destination) context.lookup(this.pmQueueTopic+"."+this.predictionTopic);
        destinationMeasurements = (Destination) context.lookup(this.pmQueueTopic+"."+this.billingTopic);
        
		producerPrediction = session.createProducer(destinationPrediction);
        producerMeasurements = session.createProducer(destinationMeasurements);
       
     //   System.out.println("Connection started to queue " + pmQueueTopic + " to topics " + billingTopic + " and "+ predictionTopic);
    
	}
	
	public void sendMessage(String queue, String message){
		//System.out.println("Sending Message to queue");
		try {
			if (queue=="prediction"){
				TextMessage messagetext = session.createTextMessage(message);
				producerPrediction.send( messagetext);
			} else {
				TextMessage messagetext = session.createTextMessage(message);
				producerMeasurements.send( messagetext);
			}
		} catch (JMSException e) {
			e.printStackTrace();
		}
		 
	}
	
	public void sendMessageTopic(String topic, String message){
	//	System.out.println("Sending Message to topic" + topic);
		try {
			Destination destination = session.createTopic(topic);
			MessageProducer producer = session.createProducer(destination);
			TextMessage messagetext = session.createTextMessage(message);
			producer.send(messagetext);
			producer.close();
		
		} catch (JMSException e) {
			e.printStackTrace();
		}
		 
	}
	
	public void registerListener(String topic, MessageListener listener){
	//	System.out.println("Registering listener with topic: " + topic);
		try {
			
			Destination thisDestination = session.createTopic(topic);
			MessageConsumer thisConsumer = session.createConsumer(thisDestination);
			thisConsumer.setMessageListener(listener);
		
		} catch (JMSException e1) {
			e1.printStackTrace();
		}
	}
	
    public void destroy() throws JMSException {

    	producerMeasurements.close();
    	producerPrediction.close();
        session.close();
        connection.close();
    }

    private static class MyExceptionListener implements ExceptionListener {
        @Override
        public void onException(JMSException exception) {
           // System.out.println("Connection ExceptionListener fired, exiting.");
        	 logger.error("Connection ExceptionListener fired, exiting.");
            exception.printStackTrace(System.out);
            System.exit(1);
        }
    }


}
