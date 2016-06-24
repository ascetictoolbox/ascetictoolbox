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

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.TimeZone;

import javax.jms.Message;
import javax.jms.MessageListener;
import javax.jms.TextMessage;

import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.session.SqlSession;
import org.apache.log4j.Logger;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import eu.ascetic.asceticarchitecture.paas.component.energymodeller.datatype.messages.GenericEnergyMessage;
import eu.ascetic.asceticarchitecture.paas.component.energymodeller.internal.common.data.database.table.DataConsumption;
/* M. Fontanella - 20 Jun 2016 - begin */
import eu.ascetic.asceticarchitecture.paas.component.energymodeller.internal.common.data.database.table.CpuFeatures;
/* M. Fontanella - 20 Jun 2016 - end */
import eu.ascetic.asceticarchitecture.paas.component.energymodeller.internal.common.data.database.table.VirtualMachine;
import eu.ascetic.asceticarchitecture.paas.component.energymodeller.internal.common.data.ibatis.ApplicationRegistry;
import eu.ascetic.asceticarchitecture.paas.component.energymodeller.internal.common.data.ibatis.DataConsumptionHandler;
/* M. Fontanella - 20 Jun 2016 - begin */
import eu.ascetic.asceticarchitecture.paas.component.energymodeller.internal.common.data.ibatis.CpuFeaturesHandler;
/* M. Fontanella - 20 Jun 2016 - end */
import eu.ascetic.asceticarchitecture.paas.component.energymodeller.internal.common.data.ibatis.mapper.AppRegistryMapper;
import eu.ascetic.asceticarchitecture.paas.component.energymodeller.internal.common.data.ibatis.mapper.DataConsumptionMapper;
/* M. Fontanella - 20 Jun 2016 - begin */
import eu.ascetic.asceticarchitecture.paas.component.energymodeller.internal.common.data.ibatis.mapper.CpuFeaturesMapper;
/* M. Fontanella - 20 Jun 2016 - end */
import eu.ascetic.asceticarchitecture.paas.component.energymodeller.internal.common.data.queue.MessageParserUtility;
import eu.ascetic.asceticarchitecture.paas.component.energymodeller.internal.queue.client.AmqpClient;

public class EnergyModellerQueueServiceManager {
	
	private AmqpClient paasQueuePublisher;
	private AmqpClient iaasQueuePublisher;
	
	
	private ApplicationRegistry registry;
	private DataConsumptionHandler dataConsumptionHandler;
	/* M. Fontanella - 20 Jun 2016 - begin */
	private CpuFeaturesHandler cpuFeaturesHandler;
	/* M. Fontanella - 20 Jun 2016 - end */
	
	
	private final static Logger LOGGER = Logger.getLogger(EnergyModellerQueueServiceManager.class.getName());
	
	/**
	 * Initialize the component by getting a queuePublisher where messages are sent to the queue, it needs the application registry that
	 * allows to store data into db about application, data consumption handler that handle consumption information
	 * 
	 */
	/* M. Fontanella - 20 Jun 2016 - begin */	
	public EnergyModellerQueueServiceManager(AmqpClient paasQueuePublisher, ApplicationRegistry registry,DataConsumptionHandler dataConsumptionHandler,CpuFeaturesHandler cpuFeaturesHandler) {
	/* M. Fontanella - 20 Jun 2016 - end */
		
		this.paasQueuePublisher = paasQueuePublisher;
		// M. Fontanella - 06 Jun 2016 - begin
		this.dataConsumptionHandler = dataConsumptionHandler;
		// M. Fontanella - 06 Jun 2016 - end
		this.registry = registry;
		/* M. Fontanella - 20 Jun 2016 - begin */
		this.cpuFeaturesHandler = cpuFeaturesHandler;
		/* M. Fontanella - 20 Jun 2016 - end */
		LOGGER.info("EM queue manager set");
	
	}
	
	/**
	 * 
	 * another version of the builder class that handler two client for sending and receiving messages from IaaS and PaaS queue
	 */
	/* M. Fontanella - 20 Jun 2016 - begin */
	public EnergyModellerQueueServiceManager(AmqpClient iaasQueuePublisher, AmqpClient paasQueuePublisher, ApplicationRegistry registry,DataConsumptionHandler dataConsumptionHandler,CpuFeaturesHandler cpuFeaturesHandler) {
	/* M. Fontanella - 20 Jun 2016 - end */
		
		this.paasQueuePublisher = paasQueuePublisher;
		this.iaasQueuePublisher = iaasQueuePublisher;
		this.dataConsumptionHandler = dataConsumptionHandler;		
		this.registry = registry;
		/* M. Fontanella - 20 Jun 2016 - begin */
		this.cpuFeaturesHandler = cpuFeaturesHandler;
		/* M. Fontanella - 20 Jun 2016 - end */
		LOGGER.info("EM queue manager set");
	
	}
	
	
	/**
	 * method that sends a message to a queue, the message is a metric and has a specific structure that is built by te MessageParserUtility by
	 * parsing an object like GenericEnergyMessage and it generated the correstpondign JSON string
	 */
	// M. Fontanella - 23 Jun 2016 - begin
	// public void sendToQueue(String queue,String providerid,String applicationid, String deploymentid, List<String> vms, String eventid, GenericEnergyMessage.Unit unit, String referenceTime,double value){
	public void sendToQueue(String queue,String providerid,String applicationid, String deploymentid, List<String> vms, String eventid, GenericEnergyMessage.Unit unit, long referenceTime,double value){
	// M. Fontanella - 23 Jun 2016 - end
		GenericEnergyMessage message= new GenericEnergyMessage();
		message.setProvider(providerid);
		message.setApplicationid(applicationid);
		message.setEventid(eventid);
		message.setDeploymentid(deploymentid);
		// M. Fontanella - 23 Jun 2016 - begin		
		// Date data = new Date();
		// message.setGenerattiontimestamp(data.toGMTString());
		TimeZone tz = TimeZone.getTimeZone("UTC");
		DateFormat df = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");	    
		df.setTimeZone(tz);
				    
		String data = df.format(new Date());		
		message.setGenerattiontimestamp(data);
		// M. Fontanella - 23 Jun 2016 - end		
		
		// used to specify the time the forecast is referred to, 
		// but if refers to a measurement it is the same as the reference time (becayse it referes to the same time it has bee generated
		
		// M. Fontanella - 23 Jun 2016 - begin
		/*
		if (referenceTime==null){		
			message.setReferredtimestamp(data.toGMTString());
		} else {
			message.setReferredtimestamp(referenceTime);
		}
		*/
		
		if (referenceTime!=0l)
			data = df.format(new java.util.Date(referenceTime));
		
		message.setReferredtimestamp(data);
		// M. Fontanella - 23 Jun 2016 - end
		message.setVms(vms);
		message.setUnit(unit);
		message.setValue(value);
		paasQueuePublisher.sendMessage(queue, MessageParserUtility.buildStringMessage(message));
		LOGGER.info("EM queue manager has sent a message to "+queue);
		LOGGER.debug("EM queue manager built this message "+MessageParserUtility.buildStringMessage(message));
		
	}	
	

	/**
	 * 
	 * creates subscription to the iaas queue for consuming messages of power and paas queue for consuming messages about application deployment
	 * is very critical to map iaas id with paas id as the consumption is not published the the paas id 
	 * subscription is based on topic provided by configuration files at the PaaS EM initialization
	 */		
	// M. Fontanella - 17 May 2016 - begin
	// M. Fontanella - 26 Apr 2016 - begin
	// M. Fontanella - 20 Jun 2016 - begin	
	public void createTwoLayersConsumers(String appTopic, String measurementsTopic, String measurementsFromVMTopic, final String defaultProviderId, final boolean enablePowerFromIaas){
	// M. Fontanella - 20 Jun 2016 - end
	// M. Fontanella - 26 Apr 2016 - end
	// M. Fontanella - 17 May 2016 - end	
		// M. Fontanella - 06 Jun 2016 - begin
		// LOGGER.info("PaaS/IaaS queue data connected");
		LOGGER.info("PaaS queue data connected");
		// M. Fontanella - 06 Jun 2016 - end
		LOGGER.info("Registering consumer for application " + appTopic + " and for energy measurements "+measurementsTopic);		
		
        MessageListener appListener = new MessageListener() {
        
        // M. Fontanella - 06 Jun 2016 - begin
        DataConsumptionHandler dataMapper = dataConsumptionHandler;
        // M. Fontanella - 06 Jun 2016 - end
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
		                    vm.setApplicationid( topic[1]);
		                    // M. Fontanella - 10 Feb 2016 - begin
		                    // vm.setDeploymentid(Integer.parseInt(topic[3]));
		                    vm.setDeploymentid( topic[3]);
		                    // vm.setVmid(Integer.parseInt(topic[5]));
		                    vm.setVmid(topic[5]);
		                    // M. Fontanella - 10 Feb 2016 - end
		                    Date date = new Date();
	                    	vm.setStart(date.getTime());
	                    	
	                    	// M. Fontanella - 17 May 2016 - begin
	                    	String payload = textMessage.getText();
		                    ObjectMapper jmapper = new ObjectMapper();
		                    JsonNode jsontext = jmapper.readValue(payload, JsonNode.class);
		                    LOGGER.info("Received DEPLOYED message for provider id"+ jsontext.findValue("providerId"));
		                    if (jsontext.findValue("providerId")==null){
	                    		LOGGER.info("Unable to parse AMQP deployment message, missing provider id (default value will be used)");
	                    		vm.setProviderid(defaultProviderId);
	                    	} else {
	                    		String providerid = jsontext.findValue("providerId").textValue();
	                    		vm.setProviderid(providerid);
	                    	}
	                    	// M. Fontanella - 17 May 2016 - end

	                    	SqlSession session = appRegistry.getSession();
		                	AppRegistryMapper mapper = session.getMapper(AppRegistryMapper.class); 
							// M. Fontanella - 20 Jan 2016 - begin
	                    	int checkvm = mapper.checkVM(vm.getProviderid(), vm.getApplicationid(), vm.getDeploymentid(), vm.getVmid());
							// M. Fontanella - 20 Jan 2016 - end
	                    	if (checkvm>0){
	                    		LOGGER.warn("Received again a deployd message for an app already registered");
	                    		
	                    		return;
	                    	}
	                    	// compute the iaas id                   	

	                    	// M. Fontanella - 17 May 2016 - begin
	                    	// Moved above in the providerid management:
	                    	// String payload = textMessage.getText();
		                    // ObjectMapper jmapper = new ObjectMapper();
		                    // JsonNode jsontext = jmapper.readValue(payload, JsonNode.class);
	                    	// M. Fontanella - 17 May 2016 - end
		                    
		                    LOGGER.info("Received DEPLOYED message for iaas id"+ jsontext.findValue("iaasVmId"));
		        
	                    	if (jsontext.findValue("iaasVmId")==null){
	                    		LOGGER.info("Unable to parse AMQP deployment message, missing iaas id");
	                    		return;
	                    	}
		                    
		                    
		                    String iaasid = jsontext.findValue("iaasVmId").textValue();
	                    	vm.setIaasid(iaasid);
	                    	
	                    	mapper.createVM(vm);
	                    	session.close();
	                    	LOGGER.info("Received DEPLOYED message");
	                    }
	                    if (topic[6].equals("DELETED")){
	                    	
	                    	VirtualMachine vm = new VirtualMachine();
		                    vm.setApplicationid( topic[1]);
		                    // M. Fontanella - 10 Feb 2016 - begin
		                    // vm.setDeploymentid(Integer.parseInt(topic[3]));
		                    vm.setDeploymentid( topic[3]);
		                    vm.setVmid(topic[5]);
		                    // M. Fontanella - 10 Feb 2016 - end
		                    Date date = new Date();
	                    	vm.setStop(date.getTime());
	                    	// M. Fontanella - 17 May 2016 - begin
	                    	String payload = textMessage.getText();
		                    ObjectMapper jmapper = new ObjectMapper();
		                    JsonNode jsontext = jmapper.readValue(payload, JsonNode.class);
		                    LOGGER.info("Received DELETED message for provider id"+ jsontext.findValue("providerId"));
		                    if (jsontext.findValue("providerId")==null){
	                    		LOGGER.info("Unable to parse AMQP deployment message, missing provider id (default value will be used)");
	                    		vm.setProviderid(defaultProviderId);
	                    	} else {
	                    		String providerid = jsontext.findValue("providerId").textValue();
	                    		vm.setProviderid(providerid);
	                    	}
	                    	// M. Fontanella - 17 May 2016 - end
	                    	SqlSession session = appRegistry.getSession();
		                	AppRegistryMapper mapper = session.getMapper(AppRegistryMapper.class); 
							// M. Fontanella - 20 Jan 2016 - begin
	                    	int checkvm = mapper.checkVM(vm.getProviderid(), vm.getApplicationid(), vm.getDeploymentid(), vm.getVmid());
	                    	LOGGER.info("Received DELETED for VM"+vm.getProviderid()+ vm.getApplicationid()+ vm.getDeploymentid()+ vm.getVmid());
							// M. Fontanella - 20 Jan 2016 - end
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
	        
	    /* M. Fontanella - 06 Jun 2016 - begin
	     * IaaS --> Paas
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
		                    ObjectMapper jmapper = new ObjectMapper();
		                    Map<String,Object> userData = jmapper.readValue(payload, Map.class);
		                    
		                    // M. Fontanella - 09 Feb 2016 - begin
		                    // M. Fontanella - 21 Mar 2016 - begin
		                    double value = Double.valueOf(userData.get("value").toString());
		                    // M. Fontanella - 21 Mar 2016 - end
		                    long ts =  Long.valueOf(userData.get("timestamp").toString());
		                    // M. Fontanella - 17 May 2016 - begin
		                    // storeMeasurement("00000", topic[1], ts, value, topic[3]);
		                    storeMeasurement(topic[1], ts, value, topic[3]);
		                    // M. Fontanella - 17 May 2016 - end
		                    // M. Fontanella - 09 Feb 2016 - end
		                	 
		                }
		            } catch (Exception e) {
		                System.out.println("Exception while inserting data about measurements:" + e);
		                e.printStackTrace();
		            }
		        }
	        
	        	// M. Fontanella - 09 Feb 2016 - begin
	        	// M. Fontanella - 17 May 2016 - begin
	        	public void storeMeasurement(String iaasvmid, long timestamp, double value, String measureType) {
	        	// M. Fontanella - 17 May 2016 - end
	        		
	        		boolean isManagedMeasureType;
	        		switch(measureType) {
            		case "power":
            			isManagedMeasureType = true;
            			break;
            		case "cpu":
            			isManagedMeasureType = true;
            			break;
            		case "memory": 
            			isManagedMeasureType = true;
            			break;
            		default:
            			isManagedMeasureType = false;
	        		}
	        		
	        		if (!isManagedMeasureType){
                    	LOGGER.debug("Received  non valid measure type "+measureType);
                    	return;
                    }
	        		
	        		LOGGER.debug("Received "+measureType +" measure "+value );
	        		
                     if (timestamp <=0){
                    	LOGGER.warn("Received non valid measure " +timestamp );
                    	return;
                    }
                    if (value <0){
                    	LOGGER.warn("Received non valid measure " +value);
                    	return;
                    }
                    // TODO now is iaas vm id later this will be the real paas id a	
                    SqlSession appsession = appRegistry.getSession();
                	AppRegistryMapper appmapper = appsession.getMapper(AppRegistryMapper.class); 
                    int count = appmapper.checkIaaSVM(iaasvmid);
                    appsession.close();
                    if (count==0){
                    	LOGGER.debug("Received  valid measure for a vm not in the registry"+iaasvmid);
                    	return;
                    }
                    
                    // TODO now is iaas vm id later this will be the real paas id a
                    DataConsumption dc= new DataConsumption();
                    // M. Fontanella - 17 May 2016 - begin
                    // dc.setProviderid(providerid);
                    // M. Fontanella - 17 May 2016 - end
                    dc.setVmid(iaasvmid);
                    
                    switch(measureType) {
                		case "power":
                			dc.setVmpower(value);
                			break;
                		case "cpu":
                			dc.setVmcpu(value);
                			break;
                		case "memory": 
                			dc.setVmmemory(value);
                			break;
                    }
                                        
                    dc.setTime(timestamp);	
                    dc.setMetrictype(measureType);
                    SqlSession datasession = dataMapper.getSession();
                	DataConsumptionMapper datamapper = datasession.getMapper(DataConsumptionMapper.class);
                	
                	datamapper.createMeasurement(dc);
                	
                	// M. Fontanella - 26 Apr 2016 - begin
            		if (!enablePowerFromIaas) { 
                		
                		double valueCPU = 0.0;
                		double valueMemory = 0.0;
                		
                        if (measureType == "cpu"  || measureType == "memory") {
                    	
                        	// Prerequisite: the CPU and memory values must be written in DATACONSUMPTION table with the same timestamp
                        	if (measureType == "cpu") {
                			
                        		valueCPU = value;
                        		// M. Fontanella - 17 May 2016 - begin
                        		// valueMemory = datamapper.getMemorySampleAtTime(providerid, iaasvmid, timestamp);
                        		valueMemory = datamapper.getMemorySampleAtTime(iaasvmid, timestamp);
                        		// M. Fontanella - 17 May 2016 - end
                        		// valueMemory=-1 : if row was not found in DATACONSUMPTION table
                			} else {
                				
                				valueMemory = value;
                				// M. Fontanella - 17 May 2016 - begin
                				// valueCPU = datamapper.getCPUSampleAtTime(providerid, iaasvmid, timestamp);
                				valueCPU = datamapper.getCPUSampleAtTime(iaasvmid, timestamp);
                				// M. Fontanella - 17 May 2016 - end
                				// valueCPU=-1 : if row was not found in DATACONSUMPTION table
                			}
                			
                        	if (valueCPU != -1 && valueMemory != -1) {
                        	
                        		DataConsumption dcVirtualPower= new DataConsumption();
                        		
                        		value = calculatePaasPower(iaasvmid, valueCPU, valueMemory);
                        		                        		
                        		// M. Fontanella - 17 May 2016 - begin
                                // dcVirtualPower.setProviderid(providerid);
                                // M. Fontanella - 17 May 2016 - end
                                dcVirtualPower.setVmid(iaasvmid);
                                dcVirtualPower.setVmpower(value);
                                dcVirtualPower.setTime(timestamp);	
                                dcVirtualPower.setMetrictype("virtualpower");
                                                            	
                            	datamapper.createMeasurement(dcVirtualPower);
                            	// M. Fontanella - 17 May 2016 - begin
                            	// LOGGER.info("Write virtualpower "+value+" for provider "+providerid+", vm "+iaasvmid+"time "+timestamp+" (cpu="+valueCPU+" - memory="+valueMemory+")");
                            	LOGGER.info("Write virtualpower "+value+" for vm "+iaasvmid+"time "+timestamp+" (cpu="+valueCPU+" - memory="+valueMemory+")");
                                // M. Fontanella - 17 May 2016 - end
                        	}
                		}
                	}
                	// M. Fontanella - 26 Apr 2016 - end
                	
                	datasession.close();
	        	}
	        	// M. Fontanella - 09 Feb 2016 - end
	        
	        	
	        	// M. Fontanella - 17 May 2016 - begin
	        	public double calculatePaasPower(String iaasvmid, double valueCPU, double valueMemory) {
	        	
	        		// TODO: Manage PaaS VirtualPower
	        		double valuePower = 5.0;
	        			
	        		return valuePower;
	        	}
	        	// M. Fontanella - 17 May 2016 - end
		};
		M. Fontanella - 06 Jun 2016 - end */
		

	    // M. Fontanella - 06 Jun 2016 - begin	    	
		MessageListener measureListener = new MessageListener(){
			DataConsumptionHandler dataMapper = dataConsumptionHandler;
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
		                    LOGGER.info("Received: METRIC " +topic[7]+", VM "+topic[5]+", deployment "+topic[3]+",application "+topic[1] );
		                    
		                    if (topic[6].equals("METRIC")){
		                    	
		                    	boolean isManagedMeasureType = false;
		                    	String measureType = topic[7];
		                    	
	        	        		switch(measureType) {
	                    		case "net-power":
	                    			isManagedMeasureType = true;	                    			
	                    			break;
	                    		case "cpu":
	                    			isManagedMeasureType = true;
	                    			break;
	                    		case "memory": 
	                    			isManagedMeasureType = true;
	                    			break;
	                    		default:
	                    			isManagedMeasureType = false;
	        	        		}
	        	        		
	        	        		if (!isManagedMeasureType){
	                            	LOGGER.debug("Ignored measure type "+measureType);
	                            	return;
	                            } 	        		
	        	        			                    
			                    DataConsumption dc= new DataConsumption();		                    
			                    dc.setApplicationid(topic[1]);
			                    dc.setDeploymentid(topic[3]);
			                    // M. Fontanella - 16 Jun 2016 - begin
			                    String paasvmid = topic[5];
			                    dc.setVmid(paasvmid);
			                    // M. Fontanella - 16 Jun 2016 - begin
			                    
			                    if (measureType.equals("net-power"))
			                    	dc.setMetrictype("power");
			                    else
			                    	dc.setMetrictype(measureType);
			                    	                    		                    	
		                    	String payload = textMessage.getText();
			                    ObjectMapper jmapper = new ObjectMapper();
			                    JsonNode jsontext = jmapper.readValue(payload, JsonNode.class);
			                    
			                    String providerid = defaultProviderId;
			                    LOGGER.info("Received METRIC message for provider id"+ jsontext.findValue("providerId"));
			                    if (jsontext.findValue("providerId")==null){
		                    		LOGGER.info("Unable to parse AMQP deployment message, missing provider id (default value will be used)");	                    		
		                    	} else {
		                    		providerid = jsontext.findValue("providerId").textValue();                  		
		                    	}
			                    
			                    dc.setProviderid(providerid);
			                    
			                    // M. Fontanella - 16 Jun 2016 - begin
			                    /*
		                    	LOGGER.info("Received METRIC message for iaas id"+ jsontext.findValue("iaasVmId"));
			        
		                    	if (jsontext.findValue("iaasVmId")==null){
		                    		LOGGER.info("Unable to parse AMQP deployment message, missing iaas id");
		                    		return;
		                    	}		                    
			                    
			                    String iaasvmid = jsontext.findValue("iaasVmId").textValue();
			                    
			                    // TODO now is iaas vm id later this will be the real paas id a	
		                    	SqlSession appsession = appRegistry.getSession();
		                    	AppRegistryMapper appmapper = appsession.getMapper(AppRegistryMapper.class); 
		                    	int count = appmapper.checkIaaSVM(iaasvmid);
		                    	appsession.close();
		                    	if (count==0){
		                    		LOGGER.debug("Received  valid measure for a vm not in the registry"+iaasvmid);
		                    		return;
		                    	}
			                    
		                    	dc.setVmid(iaasvmid);
		                    	*/
		                    	
			                    SqlSession appsession = appRegistry.getSession();
		                    	AppRegistryMapper appmapper = appsession.getMapper(AppRegistryMapper.class); 
		                    	int count = appmapper.checkVM(dc.getProviderid(), dc.getApplicationid(), dc.getDeploymentid(), paasvmid);
		                    	appsession.close();
		                    	if (count==0){
		                    		LOGGER.debug("Received  valid measure for a provider/application/deployment/vm not in the registry");
		                    		return;
		                    	}
		                    	// M. Fontanella - 16 Jun 2016 - end
		                    	
		                    	if (jsontext.findValue("timestamp")==null){
		                    		LOGGER.info("Unable to parse AMQP deployment message, missing timestamp");
		                    		return;
		                    	}                	
		                    		                    	
		                    	long timestamp =  jsontext.findValue("timestamp").longValue();
		                    	if (timestamp <=0){
		                    	 	LOGGER.warn("Received non valid timestamp " +timestamp );
		                    	 	return;
		                    	}
		                    			                    	
		                    	dc.setTime(timestamp);
		                    	
		                    	if (jsontext.findValue("value")==null){
		                    		LOGGER.info("Unable to parse AMQP deployment message, missing value");
		                    		return;
		                    	}	                    
			                                        	
		                    	double value = jsontext.findValue("value").doubleValue();		                    	
		                    	if (value <0){
	                            	LOGGER.warn("Received non valid measure " +value);
	                            	return;
	                            }
		                    	
		                    	LOGGER.debug("Received "+measureType +" measure "+value );
		                    	
		                    	switch(measureType) {
	                    			case "net-power":
	                    				dc.setVmpower(value);
	                    				break;
	                    			case "cpu":
	                    				dc.setVmcpu(value);
	                    				break;
	                    			case "memory": 
	                    				dc.setVmmemory(value);
	                    				break;
		                    	}                    	                    	
		                    		                                
		                    	SqlSession datasession = dataMapper.getSession();
		                    	DataConsumptionMapper datamapper = datasession.getMapper(DataConsumptionMapper.class);
		                        
		                    	// M. Fontanella - 14 Jun 2016 - begin
		                    	int samples = 0;
		                    	// M. Fontanella - 16 Jun 2016 - begin
		                    	// LOGGER.info("***** CERCO: prov="+providerid+", deployment="+topic[3]+", VM="+paasvmid+", Metric="+dc.getMetrictype()+", Time="+timestamp ); //MAXIM
		                    	samples = datamapper.getSamplesAtTime( providerid, topic[3], paasvmid, dc.getMetrictype(), timestamp);
		                		// M. Fontanella - 16 Jun 2016 - end
		                    	
		                		if (samples == 0){		                    	
		                    	
		                    		datamapper.createMeasurement(dc);		                    		
		                    		// M. Fontanella - 16 Jun 2016 - begin
		                    		LOGGER.info("Write "+measureType+" "+value+" for provider "+providerid+", vm "+paasvmid+"time "+timestamp);		                    	
		                    		
		                    		// M. Fontanella - 20 Jun 2016 - begin
		                    		/*
		                    		if (!enablePowerFromIaas) { 
		                            		
		                    			double valueCPU = 0.0;
		                    			double valueMemory = 0.0;
		                            		
		                    			if (measureType == "cpu"  || measureType == "memory") {
		                                	
		                    				// Prerequisite: the CPU and memory values must be written in DATACONSUMPTION table with the same timestamp
		                    				if (measureType == "cpu") {
		                            			
		                    					valueCPU = value;
		                    					// M. Fontanella - 16 Jun 2016 - begin		                    					
		                    					valueMemory = datamapper.getMemorySampleAtTime(providerid, topic[3], paasvmid, timestamp);		                    					
		                    					// M. Fontanella - 16 Jun 2016 - end
		                    					// valueMemory=-1 : if row was not found in DATACONSUMPTION table
		                    				} else {
		                            				
		                    					valueMemory = value;
		                    					// M. Fontanella - 16 Jun 2016 - begin		                    					
		                    					valueCPU = datamapper.getCPUSampleAtTime(providerid, topic[3], paasvmid, timestamp);		                    					
		                    					// M. Fontanella - 16 Jun 2016 - end
		                    					// valueCPU=-1 : if row was not found in DATACONSUMPTION table
		                    				}	                            			
		                    				if (valueCPU != -1 && valueMemory != -1) {
		                                    	
		                    					DataConsumption dcVirtualPower= new DataConsumption();		                                    	
		                    					
		                    					// M. Fontanella - 16 Jun 2016 - begin
		                    					value = calculatePaasPower(paasvmid, valueCPU, valueMemory);
		                    					// M. Fontanella - 16 Jun 2016 - end		                    					
		                                    		                        		
		                    					dcVirtualPower.setProviderid(providerid);
		                    					dcVirtualPower.setApplicationid(topic[1]);
		                    					dcVirtualPower.setDeploymentid(topic[3]);
		                    					// M. Fontanella - 16 Jun 2016 - begin
		                    					dcVirtualPower.setVmid(paasvmid);
		                    					// M. Fontanella - 16 Jun 2016 - end
		                    					dcVirtualPower.setVmpower(value);
		                    					dcVirtualPower.setTime(timestamp);	
		                    					dcVirtualPower.setMetrictype("virtualpower");
		                                                                        	
		                    					datamapper.createMeasurement(dcVirtualPower);

		                    					// M. Fontanella - 16 Jun 2016 - begin
		                    					LOGGER.info("Write virtualpower "+value+" for provider "+providerid+", vm "+paasvmid+"time "+timestamp+" (cpu="+valueCPU+" - memory="+valueMemory+")");
		                    					// M. Fontanella - 16 Jun 2016 - end
		                    				}
		                    			}
		                    		}
		                    		*/
		                    		// M. Fontanella - 20 Jun 2016 - end
		                    		
		                    		LOGGER.info("Received METRIC message");
		                    	} else {
		                    		// M. Fontanella - 16 Jun 2016 - begin
		                    		LOGGER.info("Received an existing METRIC message for timestamp="+timestamp+" (ignored)");
		                    		// M. Fontanella - 16 Jun 2016 - end
		                    	}
		                    	
		                    	datasession.close();		                    		                    	
		                    	// M. Fontanella - 14 Jun 2016 - end
		                    	
		                    }
			                }
		            } catch (Exception e) {
		                System.out.println("Exception while inserting data about measurements:" + e);
		                e.printStackTrace();
		            }
		        }
	        
	        	// M. Fontanella - 20 Jun 2016 - begin
	        	/*
	        	// M. Fontanella - 16 Jun 2016 - begin
	        	public double calculatePaasPower(String paasvmid, double valueCPU, double valueMemory) {
	        	// M. Fontanella - 16 Jun 2016 - end
	        		
	        		// TODO: Manage PaaS VirtualPower
	        		double valuePower = 5.0;
	        			
	        		return valuePower;
	        	}
	        	*/
	        	// M. Fontanella - 20 Jun 2016 - end
		};
		// M. Fontanella - 06 Jun 2016 - end
		
		LOGGER.debug("Received "+measurementsTopic);		
	    LOGGER.debug("Received "+measureListener);
		LOGGER.debug("Received "+paasQueuePublisher);
		// M. Fontanella - 06 Jun 2016 - begin
	    // iaasQueuePublisher.registerListener(measurementsTopic,measureListener);
	    paasQueuePublisher.registerListener(measurementsTopic,measureListener);	
		// M. Fontanella - 06 Jun 2016 - end
	    
	    
		/* ----------------------------------------------- QUAAA
	    // M. Fontanella - 20 Jun 2016 - begin	    	
		MessageListener measureFromVMListener = new MessageListener(){
			DataConsumptionHandler dataMapper = dataConsumptionHandler;
			private CpuFeaturesHandler cpuMapper = cpuFeaturesHandler;
			private ApplicationRegistry appRegistry=registry;
			
			public void onMessage(Message message) {
				try {
		            	
					if (message instanceof TextMessage) {
						// if (!enablePowerFromIaas) {
						
						LOGGER.info("************** SONO DENTRO *********"); // MAXIM
                            
		                TextMessage textMessage = (TextMessage) message;
		                LOGGER.debug("Received start message" + textMessage.getText() + "'"+textMessage.getJMSDestination());
		                String dest = message.getJMSDestination().toString();
		                String[] topic = dest.split("\\.");
		                if (topic.length<9){
		                	LOGGER.debug("Received a message of no interest for the EM");
		                	return;
		                }
		                
		                LOGGER.info("Received: METRICS FROM VM provider " +topic[1]+", application "+topic[3]+", deployment "+topic[5]+", VM "+topic[7]);
		                    
		                if (topic[8].equals("FROMVM")){		                    	
                  
		                	DataConsumption dc= new DataConsumption();		                    
		                	dc.setProviderid(topic[1]);
		                	dc.setApplicationid(topic[3]);
		                	dc.setDeploymentid(topic[5]);
		                	dc.setVmid(topic[7]);			                    			                    
			                    
		                	SqlSession appsession = appRegistry.getSession();
		                	AppRegistryMapper appmapper = appsession.getMapper(AppRegistryMapper.class); 
		                	int count = appmapper.checkVM(dc.getProviderid(), dc.getApplicationid(), dc.getDeploymentid(), dc.getVmid());
		                	appsession.close();
		                	if (count==0){
		                		LOGGER.debug("Received measure for a provider/application/deployment/vm not in the registry");
		                		return;
		                	}
		                    	
		                	String payload = textMessage.getText();
		                	ObjectMapper jmapper = new ObjectMapper();
		                	JsonNode jsontext = jmapper.readValue(payload, JsonNode.class);			                    
			                    	                    	
		                	if (jsontext.findValue("cpumodel")==null){
		                		LOGGER.info("Unable to parse AMQP deployment message, missing CPU model");
		                		return;
		                	}
		                	
		                	String modelCPU = jsontext.findValue("cpumodel").textValue();		                    	
		                	if (modelCPU == null){
		                		LOGGER.warn("Received non valid measure for CPU model (null)");
		                		return;
		                	}                 	
               	  
		                	SqlSession cpusession = cpuMapper.getSession();
		                	CpuFeaturesMapper cpumapper = cpusession.getMapper(CpuFeaturesMapper.class); 
		                	int countcpu = cpumapper.checkCpuModel(modelCPU);
		                	cpusession.close();
		                	if (countcpu==0){
		                		LOGGER.debug("Received measure for a cpu model not in the registry");
		                		return;
		                	}
		                	
		                	if (jsontext.findValue("core")==null){
		                		LOGGER.info("Unable to parse AMQP deployment message, missing core number");
		                		return;
		                	}	                    
			                                        	
		                	int coreNumber = jsontext.findValue("core").intValue();		                    	
		                	if (coreNumber <= 0){
		                		LOGGER.warn("Received non valid measure for core number" +coreNumber);
		                		return;
		                	}
		                	
		                    if (jsontext.findValue("cpuload")==null){
		                		LOGGER.info("Unable to parse AMQP deployment message, missing CPU load");
		                		return;
		                	}	                    
			                                        	
		                	double valueCPULoad = jsontext.findValue("cpuload").doubleValue();		                    	
		                	if (valueCPULoad <0){
		                		LOGGER.warn("Received non valid measure for CPU load" +valueCPULoad);
		                		return;
		                	}
		                    
		                	if (jsontext.findValue("cpusteal")==null){
		                		LOGGER.info("Unable to parse AMQP deployment message, missing CPU steal");
		                		return;
		                	}
		                	
		                	double valueCPUSteal = jsontext.findValue("cpusteal").doubleValue();		                    	
		                	if (valueCPUSteal <0){
		                		LOGGER.warn("Received non valid measure for CPU Steal" +valueCPUSteal);
		                		return;
		                	}
		                	
		                	if (jsontext.findValue("memory")==null){
		                		LOGGER.info("Unable to parse AMQP deployment message, missing memory");
		                		return;
		                	}	                    
			                                        	
		                	double valueMemory = jsontext.findValue("memory").doubleValue();		                    	
		                	if (valueMemory <0){
		                		LOGGER.warn("Received non valid measure for memory " +valueMemory);
		                		return;
		                	}	                    	
		                	                 
		                	if (jsontext.findValue("timestamp")==null){
		                		LOGGER.info("Unable to parse AMQP deployment message, missing timestamp");
		                		return;
		                	}			                                        	
		                	
		                	long timestamp =  jsontext.findValue("timestamp").longValue();
		                	if (timestamp <=0){
		                		LOGGER.warn("Received non valid timestamp " +timestamp );
		                		return;
		                	}		                    			                    	
		                	dc.setTime(timestamp);
		                    	
		                	LOGGER.debug("Received measures: CPU model="+modelCPU +", core="+coreNumber+", cpu_load="+valueCPULoad+", cpu_steal="+valueCPUSteal+", memory="+valueMemory );                    	
		                    		                                
		                	SqlSession datasession = dataMapper.getSession();
		                	DataConsumptionMapper datamapper = datasession.getMapper(DataConsumptionMapper.class);                      
	                    	
		                	
		                	int samplesMemory = datamapper.getSamplesAtTime( dc.getProviderid(), dc.getDeploymentid(), dc.getVmid(), "virtualmemory", timestamp);
		                	int samplesCPU = datamapper.getSamplesAtTime( dc.getProviderid(), dc.getDeploymentid(), dc.getVmid(), "virtualcpu", timestamp);
		                	int samplesPower = datamapper.getSamplesAtTime( dc.getProviderid(), dc.getDeploymentid(), dc.getVmid(), "virtualpower", timestamp);
		                	
		                	datasession.close();                  
		                			                	
		                	if (samplesMemory == 0 && samplesCPU == 0 && samplesPower == 0){		                    	
		                    	
		                		dc.setMetrictype("virtualmemory");
			                	dc.setVmmemory(valueMemory);
		                		datamapper.createMeasurement(dc);
		                		LOGGER.info("Write memory "+valueMemory+" for provider="+dc.getProviderid()+", application="+dc.getApplicationid()+", deployment="+dc.getDeploymentid()+", vm="+dc.getVmid()+", time="+timestamp);		                    	
		                    	
		                		dc.setMetrictype("virtualcpu");
		                		dc.setVmcpu(valueCPULoad);
		                		datamapper.createMeasurement(dc);
		                		LOGGER.info("Write CPU "+valueCPULoad+" for provider="+dc.getProviderid()+", application="+dc.getApplicationid()+", deployment="+dc.getDeploymentid()+", vm="+dc.getVmid()+", time="+timestamp);
		                		
		                		dc.setMetrictype("virtualpower");
		                		double valuePower = calculateVMPower(modelCPU, coreNumber, valueCPULoad, valueCPUSteal);
		                		dc.setVmpower(valuePower);
		                		datamapper.createMeasurement(dc);
		                		LOGGER.info("Write power="+valuePower+" for provider="+dc.getProviderid()+", application="+dc.getApplicationid()+", deployment="+dc.getDeploymentid()+", vm="+dc.getVmid()+", time="+timestamp);                				                				
		                	} else {

		                		LOGGER.info("Received an existing memory METRIC message for timestamp="+timestamp+" (ignored)");
		                	}
		                    
		                	dc.setMetrictype("memory");
		                	dc.setVmmemory(valueMemory);
		                    	
		                	LOGGER.info("Received METRIC message");
		                    	
		                    	
		                }
					}
				} catch (Exception e) {
					System.out.println("Exception while inserting data about measurements:" + e);
					e.printStackTrace();
				}
			}
	        
			public double calculateVMPower(String modelCPU, int vmCore, double valueCPULoad, double valueCPUSteal) {
	        	
				double valuePower = 0.0;
				
				CpuFeatures cpuFeatures;
				
				SqlSession cpusession = cpuMapper.getSession();
            	CpuFeaturesMapper cpumapper = cpusession.getMapper(CpuFeaturesMapper.class); 
            	cpuFeatures = cpumapper.selectByModel(modelCPU);
            	cpusession.close();
            	if (cpuFeatures==null){
            		LOGGER.warn("Received measure for a cpu model not in the registry");            		
            	} else {
				
            		int hostCore = cpuFeatures.getCore();
            		double tdp = cpuFeatures.getTdp();
            		double maxPower = cpuFeatures.getMaxPower();
            		double minPower = cpuFeatures.getMinPower();				
        		
            		// valuePower = (N. core VM / N. core Host) * (Pmax - Pmin) * ((avg_us + avg_sy) / 100) + (N. core VM / N. core Host) *  Pmin * ((100 - avg_st) /100);
            		double coreFactor = (double )vmCore / (double )hostCore;
            		valuePower = coreFactor * ((maxPower - minPower) * (valueCPULoad / 100) + minPower * ((100 - valueCPUSteal) / 100));
            		            	
            		LOGGER.info("Calculated VM power "+valuePower);
            	
            	}
	        			
				return valuePower;
			}	        	
		};
		
		LOGGER.debug("Received "+measurementsFromVMTopic);		
	    LOGGER.debug("Received "+measureFromVMListener);
		LOGGER.debug("Received "+paasQueuePublisher);
		paasQueuePublisher.registerListener(measurementsFromVMTopic,measureFromVMListener);		
		// M. Fontanella - 20 Jun 2016 - end
		QUAAAA ----------------------------------------------- */
		
	    
		
	}
	
}
