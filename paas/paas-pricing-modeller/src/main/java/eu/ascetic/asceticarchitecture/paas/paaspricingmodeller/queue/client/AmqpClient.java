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


public class AmqpClient {

   
	
	private MessageProducer producerMeasurement;
	private MessageProducer producerPrediction;
	private Destination destinationPrediction;
	private Destination destinationMeasurement;
	
	private String monitoringTopic="MEASUREMENTS";
	private String predictionTopic="PREDICTION";
	private String monitoringQueueTopic="PEM.ENERGY";

	private ConnectionFactory factory;
	private Connection connection;
	private String user = "admin";
	private String password = "admin";
	private Session session;
	
	private final static Logger LOGGER = Logger.getLogger(AmqpClient.class.getName());
	
	private String url = "10.15.5.55:61616";
	
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
		
		String initialContextFactory = "org.apache.qpid.jms.jndi.JmsInitialContextFactory";
		String connectionJNDIName = UUID.randomUUID().toString();
		String connectionURL = "amqp://" + this.user + ":" + this.password + "@" + this.url;
		this.monitoringQueueTopic = monitoringQueueTopic.replaceAll("\\.", "");
		String topicName = monitoringQueueTopic;
		
		// Set the properties ...
		Properties properties = new Properties();
		properties.put(Context.INITIAL_CONTEXT_FACTORY, initialContextFactory);
		properties.put("connectionfactory."+connectionJNDIName , connectionURL);

		properties.put("topic"+"."+monitoringQueueTopic+"."+monitoringTopic , monitoringTopic);
		properties.put("topic"+"."+monitoringQueueTopic+"."+predictionTopic , predictionTopic);
		LOGGER.info("Connection param "+"topic"+"."+monitoringQueueTopic+"."+monitoringTopic);
		LOGGER.info("Connection param"+topicName);
		
		javax.naming.Context context = new InitialContext(properties);

        factory = (ConnectionFactory) context.lookup(connectionJNDIName);
        connection = factory.createConnection(this.user, this.password);
        connection.setExceptionListener(new MyExceptionListener());
        connection.start();
		session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
		LOGGER.info("Connection topic "+this.monitoringQueueTopic+"."+this.monitoringTopic);
		
		// Create a Session for each queue
        destinationPrediction = (Destination) context.lookup(this.monitoringQueueTopic+"."+this.predictionTopic);
        destinationMeasurement = (Destination) context.lookup(this.monitoringQueueTopic+"."+this.monitoringTopic);
        
		producerPrediction = session.createProducer(destinationPrediction);
		producerMeasurement = session.createProducer(destinationMeasurement);

		LOGGER.info("Connection started");
		
	}
	
	public void sendMessage(String queue, String message){
		LOGGER.info("Sending Message");
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
	
	public void sendMessageTopic(String topic, String message){
		LOGGER.info("Sending Message");
		try {
			Destination destination = session.createTopic(topic);
			MessageProducer producer = session.createProducer(destination);
			TextMessage messagetext = session.createTextMessage(message);
			producer.send( messagetext);
			producer.close();
		
		} catch (JMSException e) {
			e.printStackTrace();
		}
		 
	}
	
	public void registerListener(String topic, MessageListener listener){
		LOGGER.info("Registering listener");
		try {
			Destination thisDestination = session.createTopic(topic);
			MessageConsumer thisConsumer = session.createConsumer(thisDestination);
			thisConsumer.setMessageListener(listener);
		
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

    private static class MyExceptionListener implements ExceptionListener {
        @Override
        public void onException(JMSException exception) {
            System.out.println("Connection ExceptionListener fired, exiting.");
            exception.printStackTrace(System.out);
            System.exit(1);
        }
    }

}