package eu.ascetic.asceticarchitecture.iaas.iaaspricingmodeller.queue;


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


public class Client {


	
	private MessageProducer producerMeasurement;
	private MessageProducer producerPrediction;
	private Destination destinationPrediction;
	private Destination destinationMeasurement;
	
	private String monitoringTopic="MEASUREMENTS";
	private String predictionTopic="PREDICTION";
	private String monitoringQueueTopic="PEM.ENERGY";

	private ConnectionFactory factory;
	private Connection connection;
	private String user = "guest";
	private String password = "guest";
	private Session session;
	

	private String url = "localhost:5672";

	
	
	/**
	 * generic initialization class for an AMQP queue, it uses the Java JNI library, it requires a topic to subscribe 
	 */
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
	//	System.out.println("Connection url "+connectionURL);
		// Set the properties ...
		Properties properties = new Properties();
		properties.put(Context.INITIAL_CONTEXT_FACTORY, initialContextFactory);
		properties.put("connectionfactory."+connectionJNDIName , connectionURL);

		properties.put("topic"+"."+monitoringQueueTopic+"."+monitoringTopic , monitoringTopic);
		properties.put("topic"+"."+monitoringQueueTopic+"."+predictionTopic , predictionTopic);
	//	System.out.println("Connection param "+"topic"+"."+monitoringQueueTopic+"."+monitoringTopic);
	//	System.out.println("Connection param"+topicName);
		
		javax.naming.Context context = new InitialContext(properties);
		
     factory = (ConnectionFactory) context.lookup(connectionJNDIName);
     connection = factory.createConnection(this.user, this.password);
     connection.setExceptionListener(new MyExceptionListener());
     connection.start();
     session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
 //    System.out.println("Connection topic "+this.monitoringQueueTopic+"."+this.monitoringTopic);
		
		// Create a Session for each queue
     destinationPrediction = (Destination) context.lookup(this.monitoringQueueTopic+"."+this.predictionTopic);
     destinationMeasurement = (Destination) context.lookup(this.monitoringQueueTopic+"."+this.monitoringTopic);
     
		producerPrediction = session.createProducer(destinationPrediction);
		producerMeasurement = session.createProducer(destinationMeasurement);

//		System.out.println("Connection started");
		
	}
	
	/**
	 * generic initialization class for an AMQP queue, it has no topic
	 */
	public void setup(String url, String username, String password) throws Exception {
		
		if(username != null) {
			this.user = username;
		} 
		
		if(password != null) {
			this.password = password;
		}
		
		if(url != null) {
			this.url = url;
		}
				
		String initialContextFactory = "org.apache.qpid.jms.jndi.JmsInitialContextFactory";
		String connectionJNDIName = UUID.randomUUID().toString();
		String connectionURL = "amqp://" + this.user + ":" + this.password + "@" + this.url;
	//	System.out.println("Connection url "+connectionURL);
		Properties properties = new Properties();
		properties.put(Context.INITIAL_CONTEXT_FACTORY, initialContextFactory);
		properties.put("connectionfactory."+connectionJNDIName , connectionURL);

		javax.naming.Context context = new InitialContext(properties);

     factory = (ConnectionFactory) context.lookup(connectionJNDIName);
     connection = factory.createConnection(this.user, this.password);
     connection.setExceptionListener(new MyExceptionListener());
     connection.start();
		session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
	//	System.out.println("Connection topic "+this.monitoringQueueTopic+"."+this.monitoringTopic);

	//	System.out.println("Connection started without topics registration");		
		
	}
	
	/**
	 * send a message to the queue with two option, sending it with the prediction topic or with the monitoring topic
	 */
	public void sendMessage(String queue, String message){
		System.out.println("Sending Message to queue "+queue);
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
	 
	/**
	 * register a listener of a specific topic
	 */
	public void registerListener(String topic, MessageListener listener){
		System.out.println("Registering listener");
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

	public void sendMessageTopic(String string, String gmtString) {
		// TODO Auto-generated method stub
		
	}

}
