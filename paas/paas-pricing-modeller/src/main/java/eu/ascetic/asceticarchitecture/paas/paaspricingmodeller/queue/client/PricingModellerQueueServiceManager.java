package eu.ascetic.asceticarchitecture.paas.paaspricingmodeller.queue.client;

import java.io.File; 
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.StringTokenizer;

import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageListener;
import javax.jms.TextMessage;

import org.apache.log4j.Logger;

import com.fasterxml.jackson.databind.ObjectMapper;

import eu.ascetic.asceticarchitecture.paas.paaspricingmodeller.PaaSPricingModeller;
import eu.ascetic.asceticarchitecture.paas.type.DeploymentInfo;
import eu.ascetic.asceticarchitecture.paas.type.VMinfo;


public class PricingModellerQueueServiceManager {
	
	private AmqpClientPM queuePublisher;
	
	public static final String DEPLOYMENTID = "DEPLOYMENTID";
	public static final String VMID = "VMID";

	
	private final static Logger logger = Logger.getLogger(PricingModellerQueueServiceManager.class.getName());
	PaaSPricingModeller prmodeller;
	static LinkedList<Integer> allAppIDs = new LinkedList<Integer> ();
		
	public PricingModellerQueueServiceManager(AmqpClientPM queuePublisher, PaaSPricingModeller prmodeller) {
		
		this.queuePublisher = queuePublisher;
		this.prmodeller = prmodeller;
		
		System.out.println("PricingModellerQueueServiceManager: PM queue manager set");
		logger.info("PricingModellerQueueServiceManager: PM queue manager set");
	
	}

	
	public void sendToQueue(String queue,int deploymentid, int VMid, int schemeID, VMMessage.Unit unit, double value){
		VMMessage message= new VMMessage();
	
		message.setVMMessage(deploymentid, VMid, schemeID, unit, value);
		try{
			queuePublisher.sendMessageTopic(queue +"."+DEPLOYMENTID + "." + deploymentid + "." + "VMID"+ "."+VMid + "."+unit , MessageParserUtility.buildStringMessage(message));
		}
		catch (Exception e) {
         System.out.println("PM could not send message to queue:");
         logger.info("PM could not send message to queue:");
     }
		
		//System.out.println("PM queue manager has sent a message to "+queue +"."+DEPLOYMENTID + "." + deploymentid + "." + "VMID"+ "."+VMid + "."+unit);
		
	//	System.out.println("PM queue manager built this message "+MessageParserUtility.buildStringMessage(message));
		
	}	

	public void sendToQueue(String queue,int providerid,int deploymentid, List<Integer> vms, ApplicationMessage.Unit unit, double value){
		ApplicationMessage message= new ApplicationMessage();
		
		message.setAppMessage(deploymentid, unit, value, vms);
	//	System.out.println("PM queue manager trying to sent a message to "+queue +"."+DEPLOYMENTID + "." + deploymentid + "." + unit);
		try{
			queuePublisher.sendMessageTopic(queue +"."+DEPLOYMENTID + "." + deploymentid + "." + unit, MessageParserUtility.buildStringMessage(message));
		}
		catch (Exception e) {
	         System.out.println("PM could not send message to queue:");
	         logger.error("PM could not send message to queue:");
	     }
			//queuePublisher.sendMessageTopic(queue, MessageParserUtility.buildStringMessage(message));
	//	System.out.println("PM queue manager has sent a message to "+queue +"."+DEPLOYMENTID + "." + deploymentid + "." + unit);
	//	System.out.println("PM queue manager built this message "+MessageParserUtility.buildStringMessage(message));
		
	}	
	
	
	public void createConsumers(String appTopic, String measurementsTopic){

		 System.out.println("Registering consumer for application " + appTopic + " and for price measurements "+measurementsTopic);
		
        MessageListener appListener = new MessageListener() {
        
       
        public void onMessage(Message message) {
	            try {
	            	
	                if (message instanceof TextMessage) {
	                	 
	                    TextMessage textMessage = (TextMessage) message;
	                    System.out.println("Received start message" + textMessage.getText() + "'"+textMessage.getJMSDestination());
	                    String dest = message.getJMSDestination().toString();
	                    String[] topic = dest.split("\\.");
	                    System.out.println("Received " +topic[6] + topic[5]+topic[3]+topic[1] );
	                    String payload = textMessage.getText();
	                    String[] text = payload.split(",");
	                    
	                    int i =0;

	                    int vmid = Integer.parseInt(topic[5]);
	                    int depid = Integer.parseInt(topic[3]);
	                    
	                    if (topic[6].equals("DEPLOYED")){
	                    	////HERE I SHOULD TAKE THE CHARS OF THE VM
	                    //	String[] text2 = text[2].split(":");
	                    	
	                    	if (allAppIDs.contains(depid)){
	                    	//-------------	VMinfo VM = new VMinfo(vmid, RAM, CPU, storage, scheme, IaaSProviderID);
	                    ///		prmodeller.addVM(depid, VM);
	                    	}
	                    	else {
	                    	//==========	VMinfo VM = new VMinfo(vmid, RAM, CPU, storage, scheme, IaaSProviderID);
	                    		LinkedList<VMinfo> list = new LinkedList<>();
	                   ///---         list.add(VM);
	                    		prmodeller.initializeApp(topic[1], depid, list);
	                    	}
	                    		
	                    	
	                    }
	                    if (topic[6].equals("TERMINATED")){
	                    	prmodeller.removeVM(depid, vmid);

	                    }
	                    
	                    if (topic[6].equals("RESIZE")){
	                ///    	prmodeller.resizeVM(depid, vmid, CPU, RAM, storage);
	                    }
	                	 
	                }
	            } catch (Exception e) {
	                System.out.println("Caught:" + e);
	                e.printStackTrace();
	            }
	        }
	    };	
	    
	   
		
	    logger.debug("Received "+appTopic);
	   logger.debug("Received "+appListener);
	    logger.debug("Received "+queuePublisher);
		queuePublisher.registerListener(appTopic,appListener);
		
		
		MessageListener measureListener = new MessageListener(){
        	
	        public void onMessage(Message message) {
		            try {
		            	
		                if (message instanceof TextMessage) {
		                	 
		                    TextMessage textMessage = (TextMessage) message;
		                    System.out.println("Received message" + textMessage.getText() + "'"+textMessage.getJMSDestination());
		                    String dest = message.getJMSDestination().toString();
		                  // String dest = message.toString();
		                //    System.out.println(dest);
		                    String payload = textMessage.getText();
		                    String[] text = payload.split(",");
		                    
		                    String[] topic = dest.split("\\.");
		                 /*   System.out.println(topic.length);
		                    for (int i=0; i<topic.length; i++){
		                    	System.out.println(topic[i]);
		                    }*/
		                    if (topic[0].equals("PMPREDICTION")){
		                    	if (topic.length == 6){
		                    		String[] text2 = text[2].split(":");
		                    		System.out.println("-------------->Prediction for app" + topic[2] + " for VM " + topic[4] + " is " + text2[1]);
		                    		
		                    	}
		                    	else {
		                    		String[] text2 = text[2].split(":");
		                    		System.out.println("--------------->Prediction for app " + topic[2] + " is " + text2[1]);
		                    	}
		                    	return;
		                    }
		                    if ((topic[topic.length-1].equals("CHARGES"))){
		                    	if (topic.length == 6){
		                    		String[] text2 = text[2].split(":");
		                    		System.out.println("-------------->Charges for app" + topic[2] + " for VM " + topic[4] + " are " + text2[1]);
		                    		
		                    	}
		                    	else {
		                    		String[] text2 = text[2].split(":");
		                    		System.out.println("--------------->Charges for app " + topic[2] + " are " + text2[1]);
		                    	}
		                    	return;
		                    	
		                    }
		                    if ((topic[topic.length-1].equals("TOTALCHARGES"))){
		                    	if (topic.length == 6){
		                    		String[] text2 = text[2].split(":");
		                    		System.out.println("--------------->Total Charges for app" + topic[2] + " for VM " + topic[4] + " are " + text2[1]);
		                    		
		                    	}
		                    	else {
		                    		String[] text2 = text[2].split(":");
		                    		System.out.println("---------------->Total Charges for app " + topic[2] + " are " + text2[1]);
		                    	}
		                    	return;
		                    	
		                    }

		                    return;
		                    
		                  
		                }
		            } catch (Exception e) {
		                System.out.println("Caught:" + e);
		                e.printStackTrace();
		            }
		        }
		};
		
		System.out.println("Received "+measurementsTopic);
		System.out.println("Received "+measureListener);
		System.out.println("Received "+queuePublisher);
		queuePublisher.registerListener(measurementsTopic,measureListener);
		
		
	}
		
	
}
