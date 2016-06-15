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

import org.apache.ibatis.annotations.Param;
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
		// M. Fontanella - 06 Jun 2016 - begin
		this.dataConsumptionHandler = dataConsumptionHandler;
		// M. Fontanella - 06 Jun 2016 - end
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
	 * creates subscription to the iaas queue for consuming messages of power and paas queue for consuming messages about application deployment
	 * is very critical to map iaas id with paas id as the consumption is not published the the paas id 
	 * subscription is based on topic provided by configuration files at the PaaS EM initialization
	 */		
	// M. Fontanella - 17 May 2016 - begin
	// M. Fontanella - 26 Apr 2016 - begin
	public void createTwoLayersConsumers(String appTopic, String measurementsTopic, final String defaultProviderId, final boolean enablePowerFromIaas){	
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
		                		samples = datamapper.getSamplesAtTime( topic[3], iaasvmid, dc.getMetrictype(), timestamp);
		                    	
		                		if (samples == 0){		                    	
		                    	
		                    		datamapper.createMeasurement(dc);
		                    		LOGGER.info("Write "+measureType+" "+value+" for provider "+providerid+", vm "+iaasvmid+"time "+timestamp);
		                        
		                    		if (!enablePowerFromIaas) { 
		                            		
		                    			double valueCPU = 0.0;
		                    			double valueMemory = 0.0;
		                            		
		                    			if (measureType == "cpu"  || measureType == "memory") {
		                                	
		                    				// Prerequisite: the CPU and memory values must be written in DATACONSUMPTION table with the same timestamp
		                    				if (measureType == "cpu") {
		                            			
		                    					valueCPU = value;
		                    					// valueMemory = datamapper.getMemorySampleAtTime(providerid, topic[3], iaasvmid, timestamp);
		                    					valueMemory = datamapper.getMemorySampleAtTime(topic[3], iaasvmid, timestamp);
		                    					// valueMemory=-1 : if row was not found in DATACONSUMPTION table
		                    				} else {
		                            				
		                    					valueMemory = value;
		                    					// valueCPU = datamapper.getCPUSampleAtTime(providerid, topic[3], iaasvmid, timestamp);
		                    					valueCPU = datamapper.getCPUSampleAtTime(topic[3], iaasvmid, timestamp);
		                    					// valueCPU=-1 : if row was not found in DATACONSUMPTION table
		                    				}	                            			
		                    				if (valueCPU != -1 && valueMemory != -1) {
		                                    	
		                    					DataConsumption dcVirtualPower= new DataConsumption();
		                                    		
		                    					value = calculatePaasPower(iaasvmid, valueCPU, valueMemory);
		                                    		                        		
		                    					dcVirtualPower.setProviderid(providerid);
		                    					dcVirtualPower.setApplicationid(topic[1]);
		                    					dcVirtualPower.setDeploymentid(topic[3]);
		                    					dcVirtualPower.setVmid(iaasvmid);
		                    					dcVirtualPower.setVmpower(value);
		                    					dcVirtualPower.setTime(timestamp);	
		                    					dcVirtualPower.setMetrictype("virtualpower");
		                                                                        	
		                    					datamapper.createMeasurement(dcVirtualPower);

		                    					LOGGER.info("Write virtualpower "+value+" for provider "+providerid+", vm "+iaasvmid+"time "+timestamp+" (cpu="+valueCPU+" - memory="+valueMemory+")");                    				
		                    				}
		                    			}
		                    		}
		                    		
		                    		LOGGER.info("Received METRIC message");
		                    	} else {
		                    		LOGGER.info("Received an existing METRIC message (ignored)");
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
	        
	
	        	public double calculatePaasPower(String iaasvmid, double valueCPU, double valueMemory) {
	        	
	        		// TODO: Manage PaaS VirtualPower
	        		double valuePower = 5.0;
	        			
	        		return valuePower;
	        	}	        	
		};
		// M. Fontanella - 06 Jun 2016 - end
	    
	    LOGGER.debug("Received "+measurementsTopic);		
	    LOGGER.debug("Received "+measureListener);
		LOGGER.debug("Received "+paasQueuePublisher);
		// M. Fontanella - 06 Jun 2016 - begin
	    // iaasQueuePublisher.registerListener(measurementsTopic,measureListener);
	    paasQueuePublisher.registerListener(measurementsTopic,measureListener);	
		// M. Fontanella - 06 Jun 2016 - end
		
	}
	
}
