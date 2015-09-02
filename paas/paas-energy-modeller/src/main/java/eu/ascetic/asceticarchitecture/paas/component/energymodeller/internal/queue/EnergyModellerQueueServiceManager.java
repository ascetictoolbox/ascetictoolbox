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
	
	private AmqpClient queuePublisher;
 
	private ApplicationRegistry registry;
	private DataConsumptionHandler dataConsumptionHandler;
	
	
	private final static Logger LOGGER = Logger.getLogger(EnergyModellerQueueServiceManager.class.getName());
	
		
	public EnergyModellerQueueServiceManager(AmqpClient queuePublisher, ApplicationRegistry registry,DataConsumptionHandler dataConsumptionHandler) {
		
		this.queuePublisher = queuePublisher;
		this.registry = registry;
		this.dataConsumptionHandler = dataConsumptionHandler;
		LOGGER.info("EM queue manager set");
	
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
		queuePublisher.sendMessage(queue, MessageParserUtility.buildStringMessage(message));
		LOGGER.info("EM queue manager has sent a message to "+queue);
		LOGGER.debug("EM queue manager built this message "+MessageParserUtility.buildStringMessage(message));
		
	}	
	

	public void createConsumers(String appTopic, String measurementsTopic){

		LOGGER.info("Registering consumer for application " + appTopic + " and for energy measurements "+measurementsTopic);
		
        MessageListener appListener = new MessageListener() {
        
        AppRegistryMapper mapper = registry.getMapper();
        	
        public void onMessage(Message message) {
	            try {
	            	
	                if (message instanceof TextMessage) {
	                	 
	                    TextMessage textMessage = (TextMessage) message;
	                    LOGGER.info("Received start message" + textMessage.getText() + "'"+textMessage.getJMSDestination());
	                    String dest = message.getJMSDestination().toString();
	                    String[] topic = dest.split("\\.");
	                    LOGGER.info("Received " +topic[6] + topic[5]+topic[3]+topic[1] );
	                    
	                    int i =0;
	                    LOGGER.info(i);
	                    VirtualMachine vm = new VirtualMachine();
	                    vm.setApp_id( Integer.parseInt(topic[1]));
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
		
//		Topic name: vm.<VMid>.item.<itemId> (for example: vm.wally159.item.energy)
//		-          Message structure:
//		{              
//		                “name”:<String>,             //energy or power
//		                “value”: <double>,
//		                “units”:<String>,       //W for power and KWh or Wh for energy
//		                “timestamp”:<long>
//		}
		
		MessageListener measureListener = new MessageListener(){
			DataConsumptionMapper mapper = dataConsumptionHandler.getMapper();
        	
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
		                    double value = (double) userData.get("value");
		                    long ts = (long) userData.get("timestamp");
		                    dc.setVmid(topic[1] );
		                    if (ts <=0){
		                    	LOGGER.info("Received non valid measure" +ts );
		                    	return;
		                    }
		                    if (value <0){
		                    	LOGGER.info("Received non valid measure" +value);
		                    	return;
		                    }
		                    if (topic[3].equals("energy")){
			                    dc.setVmenergy(value);
			                    dc.setTime(ts);		                    	
		                    	LOGGER.info("Received energy reading");
		                    }
		                    if (topic[3].equals("power")){
			                    dc.setVmpower(value);
			                    dc.setTime(ts);	                    	
		                    	LOGGER.info("Received power reading");
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
	    LOGGER.debug("Received "+queuePublisher);
		queuePublisher.registerListener(measurementsTopic,measureListener);
		
	}
	
	
}
