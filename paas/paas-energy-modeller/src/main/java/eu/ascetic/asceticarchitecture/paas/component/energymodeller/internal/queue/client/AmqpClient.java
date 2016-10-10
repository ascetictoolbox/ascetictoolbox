/**
   Copyright 2014-2015 Hewlett-Packard Development Company, L.P.  
   Licensed under the Apache License, Version 2.0 (the "License");
   you may not use this file except in compliance with the License.
   You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

   Unless required by applicable law or agreed to in writing, software
   distributed under the License is distributed on an "AS IS" BASIS,
   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   See the License for the specific language governing permissions and
   limitations under the License.

 */
package eu.ascetic.asceticarchitecture.paas.component.energymodeller.internal.queue.client;

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
	// M. Fontanella - 10/10/2016 - BEGIN
	//private Session session;
	private Session sessionProducer;
	private Session sessionConsumer;
	// M. Fontanella - 10/10/2016 - END
		
	private final static Logger LOGGER = Logger.getLogger(AmqpClient.class.getName());
	
	//	private String url = "10.15.5.55:61616";
	private String url = "192.168.0.8:32777";
	
	
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
		LOGGER.info("Connection url "+connectionURL);
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
        // M. Fontanella - 10/10/2016 - BEGIN
        //session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
        sessionProducer = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
        sessionConsumer = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
        // M. Fontanella - 10/10/2016 - END
        LOGGER.info("Connection topic "+this.monitoringQueueTopic+"."+this.monitoringTopic);
		
		// Create a Session for each queue
        destinationPrediction = (Destination) context.lookup(this.monitoringQueueTopic+"."+this.predictionTopic);
        destinationMeasurement = (Destination) context.lookup(this.monitoringQueueTopic+"."+this.monitoringTopic);
        
        // M. Fontanella - 10/10/2016 - BEGIN
		//producerPrediction = session.createProducer(destinationPrediction);
		//producerMeasurement = session.createProducer(destinationMeasurement);
		producerPrediction = sessionProducer.createProducer(destinationPrediction);
		producerMeasurement = sessionProducer.createProducer(destinationMeasurement);
		// M. Fontanella - 10/10/2016 - END

		LOGGER.info("Connection started");
		
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
		LOGGER.info("Connection url "+connectionURL);
		Properties properties = new Properties();
		properties.put(Context.INITIAL_CONTEXT_FACTORY, initialContextFactory);
		properties.put("connectionfactory."+connectionJNDIName , connectionURL);

		javax.naming.Context context = new InitialContext(properties);

        factory = (ConnectionFactory) context.lookup(connectionJNDIName);
        connection = factory.createConnection(this.user, this.password);
        connection.setExceptionListener(new MyExceptionListener());
        connection.start();
        // M. Fontanella - 10/10/2016 - BEGIN        
		//session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
		sessionProducer = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
		sessionConsumer = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
		// M. Fontanella - 10/10/2016 - END
		LOGGER.info("Connection topic "+this.monitoringQueueTopic+"."+this.monitoringTopic);

		LOGGER.info("Connection started without topics registration");		
		
	}
	
	/**
	 * send a message to the queue with two option, sending it with the prediction topic or with the monitoring topic
	 */
	public void sendMessage(String queue, String message){
		LOGGER.info("Sending to queue "+queue+" Message "+ message);		
		try {
			if (queue=="prediction"){
				// M. Fontanella - 10/10/2016 - BEGIN				
				//TextMessage messagetext = session.createTextMessage(message);
				TextMessage messagetext = sessionProducer.createTextMessage(message);
				// M. Fontanella - 10/10/2016 - END
				producerPrediction.send( messagetext);
			} else {
				// M. Fontanella - 10/10/2016 - BEGIN				
				//TextMessage messagetext = session.createTextMessage(message);
				TextMessage messagetext = sessionProducer.createTextMessage(message);
				// M. Fontanella - 10/10/2016 - END
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
		/* 26-09-2016 - BEGIN */
		LOGGER.info("Registering listener "+topic);
		/* 26-09-2016 - END */
		try {
			// M. Fontanella - 10/10/2016 - BEGIN
			//Destination thisDestination = session.createTopic(topic);			
			//MessageConsumer thisConsumer = session.createConsumer(thisDestination);
			Destination thisDestination = sessionConsumer.createTopic(topic);			
			MessageConsumer thisConsumer = sessionConsumer.createConsumer(thisDestination);
			// M. Fontanella - 10/10/2016 - END			
			thisConsumer.setMessageListener(listener);
		
		} catch (JMSException e1) {
        	/* 26-09-2016 - BEGIN */
        	// System.out.println("AmqpClient-registerListener - topic " + topic + " - (EXCEPTION) Caught:" + e1);
			LOGGER.info("Received EXCEPTION in registerListener topic "+topic+": "+e1);
        	e1.printStackTrace();        	
        	/* 26-09-2016 - END */			
		}		
	}
	
    public void destroy() throws JMSException {

    	producerMeasurement.close();
    	producerPrediction.close();
    	// M. Fontanella - 10/10/2016 - BEGIN
    	//session.close();
    	sessionProducer.close();
    	sessionConsumer.close();
    	// M. Fontanella - 10/10/2016 - END
        connection.close();
    }

    private static class MyExceptionListener implements ExceptionListener {
        @Override
        public void onException(JMSException exception) {
        	LOGGER.info("Connection ExceptionListener fired, exiting.");
            System.out.println("Connection ExceptionListener fired, exiting.");
            exception.printStackTrace(System.out);
            System.exit(1);
        }
    }

	public void sendMessageTopic(String string, String gmtString) {
		// TODO Auto-generated method stub
		
	}

}
