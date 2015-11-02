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

import java.util.Date;
import java.util.List;
import java.util.Properties;

import javax.jms.Connection;
import javax.jms.Destination;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageConsumer;
import javax.jms.MessageListener;
import javax.jms.MessageProducer;
import javax.jms.Session;
import javax.jms.TextMessage;
import javax.jms.Topic;
import javax.jms.TopicConnection;
import javax.jms.TopicConnectionFactory;
import javax.jms.TopicPublisher;
import javax.jms.TopicSession;
import javax.naming.InitialContext;

import org.apache.activemq.ActiveMQConnectionFactory;
import org.apache.log4j.Logger;

import eu.ascetic.asceticarchitecture.paas.component.energymodeller.datatype.messages.GenericEnergyMessage;
import eu.ascetic.asceticarchitecture.paas.component.energymodeller.internal.common.data.database.dao.EMSettings;
import eu.ascetic.asceticarchitecture.paas.component.energymodeller.internal.common.data.queue.MessageParserUtility;


public class JNIAmqpClient {

   
	private MessageProducer producerMeasurement;
	private MessageProducer producerPrediction;

	
	private String monitoringTopic="MEASUREMENTS";
	private String predictionTopic="PREDICTION";
	private String monitoringQueueTopic="PEM.ENERGY";
	private String startTopic = "APPLICATION.*.DEPLOYMENT.*.VM.*.DEPLOYED";
	private String stopTopic = "APPLICATION.*.DEPLOYMENT.*.VM.*.DELETED";
	//private Connection connection;
	private String user = "admin";
	private String password = "admin";
	private TopicSession session;
	TopicConnection conn;
	TopicConnectionFactory factory;
	TopicPublisher publisherPrediction;
    TopicPublisher publisherMonitoring;
	private final static Logger LOGGER = Logger.getLogger(JNIAmqpClient.class.getName());
	
	private String url = "tcp://10.15.5.55:61616";
	
	
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
		
		
		Properties props=new Properties();
	    props.put("java.naming.factory.initial","org.apache.activemq.jndi.ActiveMQInitialContextFactory");
	    props.put("java.naming.provider.url",url);
	    props.put("topic.topicNamePrediction",monitoringQueueTopic+this.predictionTopic);
	    props.put("topic.topicNameMonitoring",monitoringQueueTopic+this.monitoringTopic);
	    javax.naming.Context ctx=new InitialContext(props);
	    factory=(TopicConnectionFactory)ctx.lookup("ConnectionFactory");
	    conn=factory.createTopicConnection(username,password);
	    Topic topicprediction=(Topic)ctx.lookup("topicNamePrediction");
	    Topic topicmonitoring=(Topic)ctx.lookup("topicNameMonitoring");
	    
	    session=conn.createTopicSession(false,Session.AUTO_ACKNOWLEDGE);
	    publisherPrediction=session.createPublisher(topicprediction);
	    publisherMonitoring=session.createPublisher(topicmonitoring);
	    
		    
		
//		// Create a ConnectionFactory
//		ActiveMQConnectionFactory connectionFactory = new ActiveMQConnectionFactory(this.user,this.password,this.url);
//
//		// Create a Connection
//		connection = connectionFactory.createConnection();
//		
//		// Create a Session for each queue
//		session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
//       destinationPrediction = session.createTopic(this.monitoringQueueTopic+"."+this.predictionTopic);
//       destinationMeasurement = session.createTopic(this.monitoringQueueTopic+"."+this.monitoringTopic);
//       
//		// Create a MessageProducer from the Session to the Queue
//		producerPrediction = session.createProducer(destinationPrediction);
//		producerMeasurement = session.createProducer(destinationMeasurement);
//		
//		// Start the connection
		conn.start();
		LOGGER.info("Connection started");
		
	}
	
	public void sendMessage(String queue, String message){
		LOGGER.info("Sending Message");
		try {
			if (queue=="prediction"){
				TextMessage messagetext = session.createTextMessage(message);
				publisherPrediction.send( messagetext);
			} else {
				TextMessage messagetext = session.createTextMessage(message);
				publisherMonitoring.send( messagetext);
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
	
//	public void sendToQueue(String queue,String providerid,String applicationid, List<String> vms, String eventid, GenericEnergyMessage.Unit unit, String referenceTime,double value){
//		
//		GenericEnergyMessage message= new GenericEnergyMessage();
//		message.setProvider(providerid);
//		message.setApplicationid(applicationid);
//		message.setEventid(eventid);
//		Date data = new Date();
//		message.setGenerattiontimestamp(data.toGMTString());
//		// used to specify the time the forecast is referred to, 
//		// but if refers to a measurement it is the same as the reference time (becayse it referes to the same time it has bee generated
//		if (referenceTime==null){
//			message.setReferredtimestamp(data.toGMTString());
//		} else {
//			message.setReferredtimestamp(referenceTime);
//		}
//		message.setVms(vms);
//		message.setUnit(unit);
//		message.setValue(value);
//		sendMessage(queue, MessageParserUtility.buildStringMessage(message));
//		LOGGER.info("EM queue manager has sent a message to "+queue);
//		LOGGER.debug("EM queue manager built this message "+MessageParserUtility.buildStringMessage(message));
//		
//		
//	}
		
//	public void createConsumers(){
//		//APPLICATION.davidgpTestApp.DEPLOYMENT.456.SUBMITTED
//		//APPLICATION.davidgpTestApp.DEPLOYMENT.456.VM.1711.DEPLOYED
//		//APPLICATION.davidgpTestApp.DEPLOYMENT.456.VM.1712.DELETED
//		//APPLICATION.davidgpTestApp.DEPLOYMENT.456.TERMINATED
//		
//		
//		
//		MessageConsumer consumerStart;
//		MessageConsumer consumerStop;
//		try {
//			Destination topic = session.createTopic(startTopic);
//			consumerStart = session.createConsumer(startDestination);
//			Destination stopDestination = session.createTopic(stopTopic);
//			consumerStop = session.createConsumer(stopDestination);
//			
//	        startListner = new MessageListener() {
//	            public void onMessage(Message message) {
//	                try {
//	                    if (message instanceof TextMessage) {
//	                        TextMessage textMessage = (TextMessage) message;
//	                        System.out.println("Received start message"
//	                                + textMessage.getText() + "'");
//	                    }
//	                } catch (JMSException e) {
//	                    System.out.println("Caught:" + e);
//	                    e.printStackTrace();
//	                }
//	            }
//	        };
//	        
//	        stopListener = new MessageListener() {
//	            public void onMessage(Message message) {
//	                try {
//	                    if (message instanceof TextMessage) {
//	                        TextMessage textMessage = (TextMessage) message;
//	                        System.out.println("Received stop message"
//	                                + textMessage.getText() + "'");
//	                    }
//	                } catch (JMSException e) {
//	                    System.out.println("Caught:" + e);
//	                    e.printStackTrace();
//	                }
//	            }
//	        };
//	
//	        consumerStart.setMessageListener(startListner);
//	        consumerStop.setMessageListener(stopListener);
//		
//		} catch (JMSException e1) {
//			e1.printStackTrace();
//		}
//
//	}

    public void destroy() throws JMSException {

    	producerMeasurement.close();
    	producerPrediction.close();
        session.close();
        conn.close();
    }

}
