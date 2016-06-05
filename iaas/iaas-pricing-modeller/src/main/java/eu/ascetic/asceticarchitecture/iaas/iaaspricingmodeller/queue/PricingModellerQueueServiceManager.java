package eu.ascetic.asceticarchitecture.iaas.iaaspricingmodeller.queue;

import java.util.Properties;
import java.util.UUID;

import eu.ascetic.amqp.client.*;

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

import java.io.File;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;

import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageListener;
import javax.jms.TextMessage;

import org.apache.log4j.Logger;

import com.fasterxml.jackson.databind.ObjectMapper;



public class PricingModellerQueueServiceManager {

   //the class for send and receive messages


		
		private AmqpMessageProducer IaaSPricingQueuePublisher;
	 	
		private final static Logger LOGGER = Logger.getLogger(PricingModellerQueueServiceManager.class.getName());
			
		public PricingModellerQueueServiceManager(AmqpMessageProducer IaaSPricingQueuePublisher) {
			
			this.IaaSPricingQueuePublisher = IaaSPricingQueuePublisher;
			LOGGER.info("IaaS Pricing Modeller queue manager initiated");
		
		}

		public void sendToQueue(String queue,GenericPricingMessage msg) throws Exception{
			try{
			
			//queuePublisher.sendMessage(queue, MessageParserUtility.buildStringMessage(message));
				IaaSPricingQueuePublisher.sendMessage(msg.MsgToString(msg));
			
			LOGGER.info("PaaS Pr. Modeller queue manager has sent a message to "+queue);
		//	LOGGER.debug("EM queue manager built this message "+MessageParserUtility.buildStringMessage(message));
			}
			catch (NullPointerException ex){
				LOGGER.info("PaaS pricing modeller queue manager could not send the message to "+queue);
			}
		}	
		

/*		public void createConsumers(String appTopic, String measurementsTopic){

			LOGGER.info("Registering consumer for application " + appTopic + " and for energy measurements "+measurementsTopic);
			
	        MessageListener appListener = new MessageListener() {
	        
	        //AppRegistryMapper mapper = registry.getMapper();
	        	
	        public void onMessage(Message message) {
		            try {
		            	
		                if (message instanceof TextMessage) {
		                	 
		                    TextMessage textMessage = (TextMessage) message;
		                    LOGGER.info("Received start message" + textMessage.getText() + "'"+textMessage.getJMSDestination());
		                    String dest = message.getJMSDestination().toString();
		                    String[] topic = dest.split("\\.");
		                    LOGGER.info("Received " +topic[6] + topic[5]+topic[3]+topic[1] );
		                    
		             /*       int i =0;
		                    LOGGER.info(i);
		                    VirtualMachine vm = new VirtualMachine();
		                    vm.setApp_id( topic[1]);
		                    vm.setDeploy_id(Integer.parseInt(topic[3]));
		                    vm.setVm_id(Integer.parseInt(topic[5]));
		                    Date date = new Date();
		                    if (topic[6].equals("DEPLOYED")){
		                    	vm.setStart(date.getTime());
		                    	mapper.createVM(vm);
		                    	LOGGER.info("Received start");
		                    }
		                    if (topic[6].equals("TERMINATED")){
		                    	vm.setStop(date.getTime());
		                    	mapper.stopVM(vm);
		                    	LOGGER.info("Received stop");
		                    }
		                	 
		                }
		            } catch (Exception e) {
		                System.out.println("Caught:" + e);
		                e.printStackTrace();
		            }
		        }
		    };		
		    
		  
			
		    LOGGER.debug("Received "+appTopic);
		    LOGGER.debug("Received "+appListener);
		    LOGGER.debug("Received "+queuePublisher);
			queuePublisher.registerListener(appTopic,appListener);
			
//			Topic name: vm.<VMid>.item.<itemId> (for example: vm.wally159.item.energy)
//			-          Message structure:
//			{              
//			                “name”:<String>,             //energy or power
//			                “value”: <double>,
//			                “units”:<String>,       //W for power and KWh or Wh for energy
//			                “timestamp”:<long>
//			}
			
			MessageListener measureListener = new MessageListener(){
				//DataConsumptionMapper mapper = dataConsumptionHandler.getMapper();
	        	
		        public void onMessage(Message message) {
			            try {
			            	
			                if (message instanceof TextMessage) {
			                	 
			                    TextMessage textMessage = (TextMessage) message;
			                    LOGGER.info("Received start message" + textMessage.getText() + "'"+textMessage.getJMSDestination());
			                    String dest = message.getJMSDestination().toString();
			                    String payload = textMessage.getText();
			                    String[] topic = dest.split("\\.");
			                    LOGGER.info("Received " +topic[3]+topic[1] );
			                    
			                 int i =0;
			                    LOGGER.info(i);
			                    DataConsumption dc= new DataConsumption();
			                    ObjectMapper jmapper = new ObjectMapper();
			                    Map<String,Object> userData = jmapper.readValue(payload, Map.class);
			                    
			                    if (!(topic[3].equals("energy"))){
			                    	LOGGER.debug("The topic is energy");
			                    	return;
			                    }
			                    if (!(topic[3].equals("power"))){
			                    	LOGGER.debug("The topic is power");
			                    	return;
			                    }
			                    
			                    return;
			                   
			                    
//			                    double value = (double) userData.get("value");
//			                    long ts = (long) userData.get("timestamp");
//			                    dc.setVmid(topic[1] );
			                    
//			                    if (ts <=0){
//			                    	LOGGER.info("Received non valid measure" +ts );
//			                    	return;
//			                    }
//			                    if (value <0){
//			                    	LOGGER.info("Received non valid measure" +value);
//			                    	return;
//			                    }
//			                    if (topic[3].equals("energy")){
//				                    dc.setVmenergy(value);
//				                    dc.setTime(ts);		                    	
//			                    	LOGGER.info("Received energy reading");
//			                    }
//			                    if (topic[3].equals("power")){
//				                    dc.setVmpower(value);
//				                    dc.setTime(ts);	                    	
//			                    	LOGGER.info("Received power reading");
//			                    }
//			                    
			                    
			                	 
			                }
			            } catch (Exception e) {
			                System.out.println("Caught:" + e);
			                e.printStackTrace();
			            }
			        }
			};
			
			LOGGER.debug("Received "+measurementsTopic);
		    LOGGER.debug("Received "+measureListener);
		    LOGGER.debug("Received "+queuePublisher);
			queuePublisher.registerListener(measurementsTopic,measureListener);
			
		}
		*/
		
	}

	
	

