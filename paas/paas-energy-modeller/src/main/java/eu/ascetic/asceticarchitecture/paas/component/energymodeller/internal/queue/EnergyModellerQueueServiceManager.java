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
package eu.ascetic.asceticarchitecture.paas.component.energymodeller.internal.queue;

import java.util.Date;
import java.util.List;
import java.util.Map;

import javax.jms.Message;
import javax.jms.MessageListener;
import javax.jms.TextMessage;

import org.apache.ibatis.session.SqlSession;
import org.apache.log4j.Logger;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import eu.ascetic.asceticarchitecture.paas.component.energymodeller.datatype.messages.GenericEnergyMessage;
import eu.ascetic.asceticarchitecture.paas.component.energymodeller.internal.common.data.database.table.DataConsumption;
import eu.ascetic.asceticarchitecture.paas.component.energymodeller.internal.common.data.database.table.VirtualMachine;
import eu.ascetic.asceticarchitecture.paas.component.energymodeller.internal.common.data.ibatis.ApplicationRegistry;
import eu.ascetic.asceticarchitecture.paas.component.energymodeller.internal.common.data.ibatis.DataConsumptionHandler;
import eu.ascetic.asceticarchitecture.paas.component.energymodeller.internal.common.data.ibatis.mapper.AppRegistryMapper;
import eu.ascetic.asceticarchitecture.paas.component.energymodeller.internal.common.data.ibatis.mapper.DataConsumptionMapper;
import eu.ascetic.asceticarchitecture.paas.component.energymodeller.internal.common.data.queue.MessageParserUtility;
import eu.ascetic.asceticarchitecture.paas.component.energymodeller.internal.queue.client.AmqpClient;

public class EnergyModellerQueueServiceManager {
	
	private AmqpClient paasQueuePublisher;
	private AmqpClient iaasQueuePublisher;
	
	
	private ApplicationRegistry registry;
	private DataConsumptionHandler dataConsumptionHandler;
	
	
	private final static Logger LOGGER = Logger.getLogger(EnergyModellerQueueServiceManager.class.getName());
	
	/**
	 * Initialize the component by getting a queuePublisher where messages are sent to the queue, it needs the application registry that
	 * allows to store data into db about application, data consumption handler that handle consumption information
	 * 
	 */
	public EnergyModellerQueueServiceManager(AmqpClient paasQueuePublisher, ApplicationRegistry registry,DataConsumptionHandler dataConsumptionHandler) {
		
		this.paasQueuePublisher = paasQueuePublisher;
		this.registry = registry;
		LOGGER.info("EM queue manager set");
	
	}
	
	/**
	 * 
	 * another version of the builder class that handler two client for sending and receiving messages from IaaS and PaaS queue
	 */
	public EnergyModellerQueueServiceManager(AmqpClient iaasQueuePublisher, AmqpClient paasQueuePublisher, ApplicationRegistry registry,DataConsumptionHandler dataConsumptionHandler) {
		
		this.paasQueuePublisher = paasQueuePublisher;
		this.iaasQueuePublisher = iaasQueuePublisher;
		this.dataConsumptionHandler = dataConsumptionHandler;
		this.registry = registry;
		LOGGER.info("EM queue manager set");
	
	}
	
	
	/**
	 * method that sends a message to a queue, the message is a metric and has a specific structure that is built by te MessageParserUtility by
	 * parsing an object like GenericEnergyMessage and it generated the correstpondign JSON string
	 */
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
	

	/**
	 * 
	 * creates subscription to the iaas queue for consuming messages of power and paas queue for consuming messages about applocation deployment
	 * is very critical to map iaas id with paas id as the consumption is not published the the paas id 
	 * subscription is based on topic provided by configuration files at the PaaS EM initialization
	 */
	public void createTwoLayersConsumers(String appTopic, String measurementsTopic){
		LOGGER.info("PaaS/IaaS queue data connected");
		LOGGER.info("Registering consumer for application " + appTopic + " and for energy measurements "+measurementsTopic);
		
        MessageListener appListener = new MessageListener() {
        
        private ApplicationRegistry appRegistry=registry;
        	
        public void onMessage(Message message) {
	            try {
	            	
	                if (message instanceof TextMessage) {
	                    TextMessage textMessage = (TextMessage) message;
	                    LOGGER.debug("Received start message" + textMessage.getText() + "'"+textMessage.getJMSDestination());
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
	                    	SqlSession session = appRegistry.getSession();
		                	AppRegistryMapper mapper = session.getMapper(AppRegistryMapper.class); 
	                    	int checkvm = mapper.checkVM(vm.getApp_id(), vm.getDeploy_id(), vm.getVm_id());
	                    	if (checkvm>0){
	                    		LOGGER.warn("Received again a deployd message for an app already registered");
	                    		
	                    		return;
	                    	}
	                    	// compute the iaas id                   	
	                    	
	                    	String payload = textMessage.getText();
		                    ObjectMapper jmapper = new ObjectMapper();
		                    JsonNode jsontext = jmapper.readValue(payload, JsonNode.class);
	                        
		                    LOGGER.info("Received DEPLOYED message for iaas id"+ jsontext.findValue("iaasVmId"));
		        
	                    	if (jsontext.findValue("iaasVmId")==null){
	                    		LOGGER.info("Unable to parse AMQP deployment message, missing iaas id");
	                    		return;
	                    	}
		                    
		                    
		                    String iaasid = jsontext.findValue("iaasVmId").textValue();
	                    	vm.setIaas_id(iaasid);
	                    	
	                    	mapper.createVM(vm);
	                    	session.close();
	                    	LOGGER.info("Received DEPLOYED message");
	                    }
	                    if (topic[6].equals("DELETED")){
	                    	
	                    	VirtualMachine vm = new VirtualMachine();
		                    vm.setApp_id( topic[1]);
		                    vm.setDeploy_id(Integer.parseInt(topic[3]));
		                    vm.setVm_id(Integer.parseInt(topic[5]));
		                    Date date = new Date();
	                    	vm.setStop(date.getTime());
	                    	SqlSession session = appRegistry.getSession();
		                	AppRegistryMapper mapper = session.getMapper(AppRegistryMapper.class); 
	                    	int checkvm = mapper.checkVM(vm.getApp_id(), vm.getDeploy_id(), vm.getVm_id());
	                    	LOGGER.info("Received DELETED for VM"+vm.getApp_id()+ vm.getDeploy_id()+ vm.getVm_id());
	                    	if (checkvm==0){
	                    		LOGGER.warn("Received a message for an app not being created");
	                    		return;
	                    	}
	                    	
	                    	mapper.stopVM(vm);
	                    	session.close();
	                    	LOGGER.info("Received DELETED stop recorded");
	                    }
	                    
	                    
	                	 
	                }
	            } catch (Exception e) {
	            	LOGGER.error("Received EXCEPTION while writing app events");
	                System.out.println("Caught:" + e);
	                e.printStackTrace();
	            }
	        }
	    };		
	    
	    
		
	    LOGGER.debug("Registering  "+appTopic);
	    paasQueuePublisher.registerListener(appTopic,appListener);
		
		MessageListener measureListener = new MessageListener(){
			DataConsumptionHandler dataMapper = dataConsumptionHandler;
			private ApplicationRegistry appRegistry=registry;
			
	        public void onMessage(Message message) {
		            try {
		            	
		                if (message instanceof TextMessage) {
		                	
		                    TextMessage textMessage = (TextMessage) message;
		                    LOGGER.debug("Received message" + textMessage.getText() + "'"+textMessage.getJMSDestination());
		                    String dest = message.getJMSDestination().toString();
		                    String payload = textMessage.getText();
		                    String[] topic = dest.split("\\.");
		                   
		                    LOGGER.debug("this is for "+topic[3]);
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
			                    SqlSession appsession = appRegistry.getSession();
			                	AppRegistryMapper appmapper = appsession.getMapper(AppRegistryMapper.class); 
			                    int count = appmapper.checkIaaSVM(topic[1]);
			                    appsession.close();
			                    if (count==0){
			                    	LOGGER.debug("Received  valid measure for a vm not in the registry"+topic[1]);
			                    	return;
			                    }
			                    LOGGER.debug("Received power " +topic[3]+topic[1] );
			                    dc.setVmid(topic[1] );
			                    dc.setVmpower(value);
			                    dc.setTime(ts);	
			                    dc.setMetrictype("power");
			                    SqlSession datasession = dataMapper.getSession();
			                	DataConsumptionMapper datamapper = datasession.getMapper(DataConsumptionMapper.class); 
			                	datamapper.createMeasurement(dc);
			                	datasession.close();
		                    } 
		                    if ((topic[3].equals("cpu"))){
		                    	double value = (double) userData.get("value");
		                    	LOGGER.debug("Received cpu measure " +value);
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
			                    SqlSession appsession = appRegistry.getSession();
			                	AppRegistryMapper appmapper = appsession.getMapper(AppRegistryMapper.class); 
			                    int count = appmapper.checkIaaSVM(topic[1]);
			                    appsession.close();
			                    if (count==0){
			                    	LOGGER.debug("Received  valid measure for a vm not in the registry"+topic[1]);
			                    	return;
			                    }
			                    // TODO now is iaas vm id later this will be the real paas id a	
			                    dc.setVmid(topic[1] );
			                    dc.setVmcpu(value);
			                    dc.setTime(ts);	
			                    dc.setMetrictype("cpu");
			                    SqlSession datasession = dataMapper.getSession();
			                	DataConsumptionMapper datamapper = datasession.getMapper(DataConsumptionMapper.class); 
			                	datamapper.createMeasurement(dc);
			                	datasession.close();
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
			                    SqlSession appsession = appRegistry.getSession();
			                	AppRegistryMapper appmapper = appsession.getMapper(AppRegistryMapper.class); 
			                    int count = appmapper.checkIaaSVM(topic[1]);
			                    appsession.close();
			                    
			                    if (count==0){
			                    	LOGGER.debug("Received  valid measure for a vm not in the registry"+topic[1]);
			                    	return;
			                    }
			                    // TODO now is iaas vm id later this will be the real paas id a	
			                    dc.setVmid(topic[1] );

			                    dc.setVmmemory(value);
			                    dc.setTime(ts);	
			                    dc.setMetrictype("memory");
			                    SqlSession datasession = dataMapper.getSession();
			                	DataConsumptionMapper datamapper = datasession.getMapper(DataConsumptionMapper.class); 
			                	datamapper.createMeasurement(dc);
			                	datasession.close();
		                    	
		                    }
		                    
		                	 
		                }
		            } catch (Exception e) {
		                System.out.println("Exception while inserting data about measurements:" + e);
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
