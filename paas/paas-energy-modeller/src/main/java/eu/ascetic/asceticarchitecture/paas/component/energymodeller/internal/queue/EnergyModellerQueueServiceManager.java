package eu.ascetic.asceticarchitecture.paas.component.energymodeller.internal.queue;

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

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import eu.ascetic.asceticarchitecture.paas.component.energymodeller.datatype.messages.GenericEnergyMessage;
import eu.ascetic.asceticarchitecture.paas.component.energymodeller.internal.common.data.database.table.DataConsumption;
import eu.ascetic.asceticarchitecture.paas.component.energymodeller.internal.common.data.ibatis.ApplicationRegistry;
import eu.ascetic.asceticarchitecture.paas.component.energymodeller.internal.common.data.ibatis.DataConsumptionHandler;
import eu.ascetic.asceticarchitecture.paas.component.energymodeller.internal.common.data.ibatis.mapper.AppRegistryMapper;
import eu.ascetic.asceticarchitecture.paas.component.energymodeller.internal.common.data.ibatis.mapper.DataConsumptionMapper;
import eu.ascetic.asceticarchitecture.paas.component.energymodeller.internal.common.data.ibatis.mapper.VirtualMachine;
import eu.ascetic.asceticarchitecture.paas.component.energymodeller.internal.common.data.queue.MessageParserUtility;
import eu.ascetic.asceticarchitecture.paas.component.energymodeller.internal.queue.client.AmqpClient;

public class EnergyModellerQueueServiceManager {
	
	private AmqpClient paasQueuePublisher;
	private AmqpClient iaasQueuePublisher;
	
	
	private ApplicationRegistry registry;
	private DataConsumptionHandler dataConsumptionHandler;
	
	
	private final static Logger LOGGER = Logger.getLogger(EnergyModellerQueueServiceManager.class.getName());
	
		
	public EnergyModellerQueueServiceManager(AmqpClient paasQueuePublisher, ApplicationRegistry registry,DataConsumptionHandler dataConsumptionHandler) {
		
		this.paasQueuePublisher = paasQueuePublisher;
		
		this.registry = registry;
		LOGGER.info("EM queue manager set");
	
	}
	
	public EnergyModellerQueueServiceManager(AmqpClient iaasQueuePublisher, AmqpClient paasQueuePublisher, ApplicationRegistry registry,DataConsumptionHandler dataConsumptionHandler) {
		
		this.paasQueuePublisher = paasQueuePublisher;
		this.iaasQueuePublisher = iaasQueuePublisher;
		this.dataConsumptionHandler = dataConsumptionHandler;
		this.registry = registry;
		LOGGER.info("EM queue manager set");
	
	}

	public void sendToQueue(String queue,String providerid,String applicationid, String deploymentid, List<String> vms, String eventid, GenericEnergyMessage.Unit unit, String referenceTime,double value){
		GenericEnergyMessage message= new GenericEnergyMessage();
		message.setProvider(providerid);
		message.setApplicationid(applicationid);
		message.setEventid(eventid);
		message.setDeploymentid(deploymentid);
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
		paasQueuePublisher.sendMessage(queue, MessageParserUtility.buildStringMessage(message));
		LOGGER.info("EM queue manager has sent a message to "+queue);
		LOGGER.debug("EM queue manager built this message "+MessageParserUtility.buildStringMessage(message));
		
	}	
	

//	public void createConsumers(String appTopic, String measurementsTopic){
//		LOGGER.info("PaaS only queue data connected");
//		LOGGER.info("Registering consumer for application " + appTopic + " and for energy measurements "+measurementsTopic);
//		
//        MessageListener appListener = new MessageListener() {
//        
//        AppRegistryMapper mapper = registry.getMapper();
//        	
//        public void onMessage(Message message) {
//	            try {
//	            	
//	                if (message instanceof TextMessage) {
//	                	 
//	                    TextMessage textMessage = (TextMessage) message;
//	                    LOGGER.info("Received start message" + textMessage.getText() + "'"+textMessage.getJMSDestination());
//	                    String dest = message.getJMSDestination().toString();
//	                    String[] topic = dest.split("\\.");
//	                    
//	                    if (topic.length<6){
//	                    	LOGGER.debug("Received a message of no interest for the EM");
//	                    	return;
//	                    }
//	                    
//	                    LOGGER.info("Received " +topic[6] + topic[5]+topic[3]+topic[1] );
//	                    
//	                    
//	                    if (topic[6].equals("DEPLOYED")){
//	                    	VirtualMachine vm = new VirtualMachine();
//		                    vm.setApp_id( topic[1]);
//		                    vm.setDeploy_id(Integer.parseInt(topic[3]));
//		                    vm.setVm_id(Integer.parseInt(topic[5]));
//		                    Date date = new Date();
//	                    	vm.setStart(date.getTime());
//	                    	int checkvm = mapper.checkVM(vm.getApp_id(), vm.getDeploy_id(), vm.getVm_id());
//	                    	if (checkvm>0){
//	                    		LOGGER.warn("Received again a deployd message for an app already registered");
//	                    		return;
//	                    	}
//	                    	mapper.createVM(vm);
//	                    	LOGGER.info("Received DEPLOYED message");
//	                    }
//	                    if (topic[6].equals("DELETED")){
//	                    	VirtualMachine vm = new VirtualMachine();
//		                    vm.setApp_id( topic[1]);
//		                    vm.setDeploy_id(Integer.parseInt(topic[3]));
//		                    vm.setVm_id(Integer.parseInt(topic[5]));
//		                    Date date = new Date();
//	                    	vm.setStop(date.getTime());
//	                    	int checkvm = mapper.checkVM(vm.getApp_id(), vm.getDeploy_id(), vm.getVm_id());
//	                    	if (checkvm==0){
//	                    		LOGGER.warn("Received a message for an app not being created before");
//	                    		return;
//	                    	}
//	                    	mapper.stopVM(vm);
//	                    	LOGGER.info("Received TERMINATED stop");
//	                    }
//	                    
//	                    if (topic[6].equals("TERMINATED")){
//	                    		           
//	                    	LOGGER.info("Received TERMINATED stop");
//	                    }
//	                	 
//	                }
//	            } catch (Exception e) {
//	                System.out.println("Caught:" + e);
//	                e.printStackTrace();
//	            }
//	        }
//	    };		
//	    
//	    
//		
//	    LOGGER.debug("Received "+appTopic);
//	    LOGGER.debug("Received "+appListener);
//	    LOGGER.debug("Received "+paasQueuePublisher);
//	    paasQueuePublisher.registerListener(appTopic,appListener);
//		
////		Topic name: vm.<VMid>.item.<itemId> (for example: vm.wally159.item.energy)
////		-          Message structure:
////		{              
////		                “name”:<String>,             //energy or power
////		                “value”: <double>,
////		                “units”:<String>,       //W for power and KWh or Wh for energy
////		                “timestamp”:<long>
////		}
//		
//		MessageListener measureListener = new MessageListener(){
//			//DataConsumptionMapper mapper = dataConsumptionHandler.getMapper();
//        	
//	        public void onMessage(Message message) {
//		            try {
//		            	
//		                if (message instanceof TextMessage) {
//		                	 
//		                    TextMessage textMessage = (TextMessage) message;
//		                    LOGGER.info("Received start message" + textMessage.getText() + "'"+textMessage.getJMSDestination());
//		                    String dest = message.getJMSDestination().toString();
//		                    String payload = textMessage.getText();
//		                    String[] topic = dest.split("\\.");
//		                    LOGGER.info("Received " +topic[3]+topic[1] );
//		                    
//		                    int i =0;
//		                    LOGGER.info(i);
//		                    DataConsumption dc= new DataConsumption();
//		                    ObjectMapper jmapper = new ObjectMapper();
//		                    Map<String,Object> userData = jmapper.readValue(payload, Map.class);
//		                    
//		                    if (!(topic[3].equals("energy"))){
//		                    	LOGGER.debug("The topic is energy");
//		                    	return;
//		                    }
//		                    if (!(topic[3].equals("power"))){
//		                    	LOGGER.debug("The topic is power");
//		                    	return;
//		                    }
//		                    
//		                    return;
//		                    
//		                }
//		            } catch (Exception e) {
//		                System.out.println("Caught:" + e);
//		                e.printStackTrace();
//		            }
//		        }
//		};
//		
//		LOGGER.debug("Registering "+measurementsTopic);
//	    paasQueuePublisher.registerListener(measurementsTopic,measureListener);
//		
//	}
	

	public void createTwoLayersConsumers(String appTopic, String measurementsTopic){
		LOGGER.info("PaaS/IaaS queue data connected");
		LOGGER.info("Registering consumer for application " + appTopic + " and for energy measurements "+measurementsTopic);
		
        MessageListener appListener = new MessageListener() {
        
        AppRegistryMapper mapper = registry.getMapper();
        	
        public void onMessage(Message message) {
	            try {
	            	
	                if (message instanceof TextMessage) {
	                	AppRegistryMapper mapper = registry.getMapper(); 
	                    TextMessage textMessage = (TextMessage) message;
	                    LOGGER.info("Received start message" + textMessage.getText() + "'"+textMessage.getJMSDestination());
	                    String dest = message.getJMSDestination().toString();
	                    String[] topic = dest.split("\\.");
	                    
	                    if (topic.length<6){
	                    	LOGGER.debug("Received a message of no interest for the EM");
	                    	return;
	                    }
	                    
	                    LOGGER.info("Received " +topic[6] + topic[5]+topic[3]+topic[1] );
	                    
	                    
	                    if (topic[6].equals("DEPLOYED")){
	                    	VirtualMachine vm = new VirtualMachine();
		                    vm.setApp_id( topic[1]);
		                    vm.setDeploy_id(Integer.parseInt(topic[3]));
		                    vm.setVm_id(Integer.parseInt(topic[5]));
		                    Date date = new Date();
	                    	vm.setStart(date.getTime());
	                    	int checkvm = mapper.checkVM(vm.getApp_id(), vm.getDeploy_id(), vm.getVm_id());
	                    	if (checkvm>0){
	                    		LOGGER.warn("Received again a deployd message for an app already registered");
	                    		
	                    		return;
	                    	}
	                    	// compute the iaas id                   	
	                    	
	                    	String payload = textMessage.getText();
		                    ObjectMapper jmapper = new ObjectMapper();
		                    JsonNode jsontext = jmapper.readValue(payload, JsonNode.class);
	                        
	                		System.out.println("Received DEPLOYED message for iaas id"+ jsontext.findValue("iaasVmId"));
		        
	                    	if (jsontext.findValue("iaasVmId")==null){
	                    		LOGGER.info("Unable to parse AMQP deployment message, missing iaas id");
	                    		return;
	                    	}
		                    
		                    
		                    String iaasid = jsontext.findValue("iaasVmId").textValue();
	                    	vm.setIaas_id(iaasid);
	                    	
	                    	mapper.createVM(vm);
	                    	LOGGER.info("Received DEPLOYED message");
	                    }
	                    if (topic[6].equals("TERMINATED")){
		                    
	                    	VirtualMachine vm = new VirtualMachine();
		                    vm.setApp_id( topic[1]);
		                    vm.setDeploy_id(Integer.parseInt(topic[3]));
		                    vm.setVm_id(Integer.parseInt(topic[5]));
		                    Date date = new Date();
	                    	vm.setStop(date.getTime());
	                    	int checkvm = mapper.checkVM(vm.getApp_id(), vm.getDeploy_id(), vm.getVm_id());
	                    	if (checkvm==0){
	                    		LOGGER.warn("Received a message for an app not being created");
	                    		return;
	                    	}
	                    	mapper.stopVM(vm);
	                    	LOGGER.info("Received TERMINATED stop");
	                    }
	                	 
	                }
	            } catch (Exception e) {
	                System.out.println("Caught:" + e);
	                e.printStackTrace();
	            }
	        }
	    };		
	    
	    
		
	    LOGGER.debug("Registering  "+appTopic);
	    paasQueuePublisher.registerListener(appTopic,appListener);
		
		MessageListener measureListener = new MessageListener(){
			DataConsumptionMapper mapper = dataConsumptionHandler.getMapper();
			AppRegistryMapper appregistry = registry.getMapper();
	        public void onMessage(Message message) {
		            try {
		            	
		                if (message instanceof TextMessage) {
		                	 
		                    TextMessage textMessage = (TextMessage) message;
		                    LOGGER.debug("Received message" + textMessage.getText() + "'"+textMessage.getJMSDestination());
		                    String dest = message.getJMSDestination().toString();
		                    String payload = textMessage.getText();
		                    String[] topic = dest.split("\\.");
		                   
		  
		                    DataConsumption dc= new DataConsumption();
		                    ObjectMapper jmapper = new ObjectMapper();
		                    Map<String,Object> userData = jmapper.readValue(payload, Map.class);
		                    
		                    if ((topic[3].equals("power"))){
		                    	//LOGGER.info("The topic is power");
		                    	LOGGER.debug("Received power " +topic[3]+topic[1] );
			                    double value = (double) userData.get("value");
			                    //LOGGER.info("Received ts" +Long.valueOf(userData.get("timestamp").toString() ));
			                    long ts =  Long.valueOf(userData.get("timestamp").toString());
			                    if (ts <=0){
			                    	LOGGER.warn("Received non valid measure" +ts );
			                    	return;
			                    }
			                    if (value <0){
			                    	LOGGER.warn("Received non valid measure" +value);
			                    	return;
			                    }
			                    // TODO now is iaas vm id later this will be the real paas id a	
			                    int count = appregistry.checkIaaSVM(topic[1]);
			                    if (count==0){
			                    	LOGGER.info("Received  valid measure for a vm not in the registry"+topic[1]);
			                    	return;
			                    }
			                    LOGGER.info("Received power " +topic[3]+topic[1] );
			                    dc.setVmid(topic[1] );
			                    
			                    
			                    
			                    dc.setVmpower(value);
			                    dc.setTime(ts);	
			                    dc.setMetrictype("power");
		                    	mapper.createMeasurement(dc);
		                    } 
		                    if ((topic[3].equals("cpu"))){
		                    	double value = (double) userData.get("value");
		                    	LOGGER.debug("Received cpu measure " +value);
		                    	long ts =  Long.valueOf(userData.get("timestamp").toString());
			                    if (ts <=0){
			                    	LOGGER.info("Received non valid measure" +ts );
			                    	return;
			                    }
			                    if (value <0){
			                    	LOGGER.info("Received non valid measure" +value);
			                    	return;
			                    }
			                    // TODO now is iaas vm id later this will be the real paas id a	
			                    int count = appregistry.checkIaaSVM(topic[1]);
			                    if (count==0){
			                    	LOGGER.info("Received  valid measure for a vm not in the registry"+topic[1]);
			                    	return;
			                    }
			                    // TODO now is iaas vm id later this will be the real paas id a	
			                    dc.setVmid(topic[1] );
			                    dc.setVmcpu(value);
			                    dc.setTime(ts);	
			                    dc.setMetrictype("cpu");
		                    	mapper.createMeasurement(dc);
		                    }
		                    
		                    if ((topic[3].equals("memory"))){
		                    	double value = Double.valueOf(userData.get("value").toString());
		                    	LOGGER.debug("Received memory measure " +value);
		                    	long ts =  Long.valueOf(userData.get("timestamp").toString());
			                    if (ts <=0){
			                    	LOGGER.warn("Received non valid measure" +ts );
			                    	return;
			                    }
			                    if (value <0){
			                    	LOGGER.warn("Received non valid measure" +value);
			                    	return;
			                    }
			                    // TODO now is iaas vm id later this will be the real paas id a	
			                    int count = appregistry.checkIaaSVM(topic[1]);
			                    if (count==0){
			                    	LOGGER.info("Received  valid measure for a vm not in the registry"+topic[1]);
			                    	return;
			                    }
			                    // TODO now is iaas vm id later this will be the real paas id a	
			                    dc.setVmid(topic[1] );

			                    dc.setVmmemory(value);
			                    dc.setTime(ts);	
			                    dc.setMetrictype("memory");
		                    	mapper.createMeasurement(dc);
		                    	
		                    }
		                    
		                	 
		                }
		            } catch (Exception e) {
		                System.out.println("Caught:" + e);
		                e.printStackTrace();
		            }
		        }
		};
		
		LOGGER.debug("Received "+measurementsTopic);
	    LOGGER.debug("Received "+measureListener);
	    LOGGER.debug("Received "+paasQueuePublisher);
	    iaasQueuePublisher.registerListener(measurementsTopic,measureListener);
		
	}
	
}
