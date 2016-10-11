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
import eu.ascetic.asceticarchitecture.paas.component.energymodeller.internal.common.data.database.table.CpuFeatures;
import eu.ascetic.asceticarchitecture.paas.component.energymodeller.internal.common.data.database.table.VirtualMachine;
import eu.ascetic.asceticarchitecture.paas.component.energymodeller.internal.common.data.ibatis.ApplicationRegistry;
import eu.ascetic.asceticarchitecture.paas.component.energymodeller.internal.common.data.ibatis.DataConsumptionHandler;
import eu.ascetic.asceticarchitecture.paas.component.energymodeller.internal.common.data.ibatis.CpuFeaturesHandler;
import eu.ascetic.asceticarchitecture.paas.component.energymodeller.internal.common.data.ibatis.mapper.AppRegistryMapper;
import eu.ascetic.asceticarchitecture.paas.component.energymodeller.internal.common.data.ibatis.mapper.DataConsumptionMapper;
import eu.ascetic.asceticarchitecture.paas.component.energymodeller.internal.common.data.ibatis.mapper.CpuFeaturesMapper;
import eu.ascetic.asceticarchitecture.paas.component.energymodeller.internal.common.data.queue.MessageParserUtility;
import eu.ascetic.asceticarchitecture.paas.component.energymodeller.internal.queue.client.AmqpClient;

public class EnergyModellerQueueServiceManager {
	
	private AmqpClient paasQueuePublisher;
	private AmqpClient iaasQueuePublisher;
	
	
	private ApplicationRegistry registry;
	private DataConsumptionHandler dataConsumptionHandler;
	private CpuFeaturesHandler cpuFeaturesHandler;
	
	
	private final static Logger LOGGER = Logger.getLogger(EnergyModellerQueueServiceManager.class.getName());
	
	/**
	 * Initialize the component by getting a queuePublisher where messages are sent to the queue, it needs the application registry that
	 * allows to store data into db about application, data consumption handler that handle consumption information
	 * 
	 */
	
	public EnergyModellerQueueServiceManager(AmqpClient paasQueuePublisher, ApplicationRegistry registry,DataConsumptionHandler dataConsumptionHandler,CpuFeaturesHandler cpuFeaturesHandler) {
		
		this.paasQueuePublisher = paasQueuePublisher;
		this.dataConsumptionHandler = dataConsumptionHandler;
		this.registry = registry;
		this.cpuFeaturesHandler = cpuFeaturesHandler;
		LOGGER.info("EM queue manager set1");
	
	}
	
	/**
	 * 
	 * another version of the builder class that handler two client for sending and receiving messages from IaaS and PaaS queue
	 */

	public EnergyModellerQueueServiceManager(AmqpClient iaasQueuePublisher, AmqpClient paasQueuePublisher, ApplicationRegistry registry,DataConsumptionHandler dataConsumptionHandler,CpuFeaturesHandler cpuFeaturesHandler) {
		
		this.paasQueuePublisher = paasQueuePublisher;
		this.iaasQueuePublisher = iaasQueuePublisher;
		this.dataConsumptionHandler = dataConsumptionHandler;		
		this.registry = registry;
		this.cpuFeaturesHandler = cpuFeaturesHandler;
		LOGGER.info("EM queue manager set2");
	
	}
	
	
	/**
	 * method that sends a message to a queue, the message is a metric and has a specific structure that is built by te MessageParserUtility by
	 * parsing an object like GenericEnergyMessage and it generated the correstpondign JSON string
	 */

	public void sendToQueue(String queue,String providerid,String applicationid, String deploymentid, List<String> vms, String eventid, GenericEnergyMessage.Unit unit, long referenceTime,double value){
	
		GenericEnergyMessage message= new GenericEnergyMessage();
		message.setProvider(providerid);
		message.setApplicationid(applicationid);
		message.setEventid(eventid);
		message.setDeploymentid(deploymentid);
		TimeZone tz = TimeZone.getTimeZone("UTC");
		DateFormat df = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");	    
		df.setTimeZone(tz);
				    
		String data = df.format(new Date());		
		message.setGenerattiontimestamp(data);
		
		// used to specify the time the forecast is referred to, 
		// but if refers to a measurement it is the same as the reference time (becayse it referes to the same time it has bee generated
		
		if (referenceTime!=0l)
			data = df.format(new java.util.Date(referenceTime));
		
		message.setReferredtimestamp(data);
		message.setVms(vms);
		message.setUnit(unit);
		message.setValue(value);
		paasQueuePublisher.sendMessage(queue, MessageParserUtility.buildStringMessage(message));
		LOGGER.info("EM queue manager has sent a message to "+queue);
		LOGGER.debug("EM queue manager built this message "+MessageParserUtility.buildStringMessage(message));
		
	}	
	
	// M. Fontanella - 29/09/2016 - BEGIN
	// M. Fontanella - 29/09/2016 - END

	/**
	 * 
	 * creates subscription to the iaas queue for consuming messages of power and paas queue for consuming messages about application deployment
	 * is very critical to map iaas id with paas id as the consumption is not published the the paas id 
	 * subscription is based on topic provided by configuration files at the PaaS EM initialization
	 */		

	public void createTwoLayersConsumers(String appTopic, String measurementsTopic, String measurementsFromVMTopic, final String defaultProviderId, final boolean enablePowerFromIaas){
	
		LOGGER.info("PaaS queue data connected");
		LOGGER.info("Registering consumer for application " + appTopic + ", for IaaS energy measurements "+measurementsTopic+ " and for VMs energy measurements "+measurementsFromVMTopic);
		
        MessageListener appListener = new MessageListener() {
               
        // DataConsumptionHandler dataMapper = dataConsumptionHandler;
        private ApplicationRegistry appRegistry=registry;
        	
        public void onMessage(Message message) {
	            try {
	            	
	            	// 26-09-2016 - BEGIN
                	LOGGER.info("Before received information from ActiveMQ (DEPLOYED/DELETED topic)");
                	// 26-09-2016 - END
                	
                	if (message instanceof TextMessage) {
	                	// 26-09-2016 - BEGIN
	                	LOGGER.info("Received information from ActiveMQ");
	                	// 26-09-2016 - END
	                    TextMessage textMessage = (TextMessage) message;
	                    // 26-09-2016 - BEGIN
	                    //LOGGER.debug("Received start message" + textMessage.getText() + "'"+textMessage.getJMSDestination());
	                    LOGGER.info("Received start message" + textMessage.getText() + "'"+textMessage.getJMSDestination());
	                    // 26-09-2016 - END
	                    String dest = message.getJMSDestination().toString();
	                    String[] topic = dest.split("\\.");
	                    	                    
	                    int counter;
	                    String ArgString;
	                    ArgString="-->";
	                    
	                    for (counter = 0; counter < topic.length; counter++) {
	                    	
	                    	if (counter == (topic.length-1))
	                    		ArgString = ArgString + topic[counter] + "<--";
	                    	else
	                    		ArgString = ArgString + topic[counter] + ".";
	                    }
	                    
	                    if (topic.length < 7){
	                    	LOGGER.info("Received a message of no interest for the EM:" + ArgString);
	                    	return;
	                    }
	                    else {
	                    	LOGGER.info("Received: " + ArgString);
	                    }
	                    	                    
	                    if (topic[6].equals("DEPLOYED")){
	                    	VirtualMachine vm = new VirtualMachine();
		                    vm.setApplicationid( topic[1]);
		                    vm.setDeploymentid( topic[3]);
		                    vm.setVmid(topic[5]);
		                    Date date = new Date();
	                    	vm.setStart(date.getTime());
	                    	
	                    	String payload = textMessage.getText();
		                    ObjectMapper jmapper = new ObjectMapper();
		                    JsonNode jsontext = jmapper.readValue(payload, JsonNode.class);
		                    
		                    if (jsontext.findValue("providerId")==null){
	                    		LOGGER.info("Unable to parse AMQP deployment message, missing provider id (default value will be used)");
	                    		vm.setProviderid(defaultProviderId);
	                    	} else {
	                    		String providerid = jsontext.findValue("providerId").textValue();
	                    		vm.setProviderid(providerid);
	                    		LOGGER.info("Received DEPLOYED message for provider id"+ providerid);
	                    	}

	                    	SqlSession session = appRegistry.getSession();
		                	AppRegistryMapper mapper = session.getMapper(AppRegistryMapper.class); 

	                    	int checkvm = mapper.checkVM(vm.getProviderid(), vm.getApplicationid(), vm.getDeploymentid(), vm.getVmid());
	                    	if (checkvm>0){
	                    		// 26-09-2016 - BEGIN
	                    		// LOGGER.warn("Received again a deployed message for an app already registered");
	                    		LOGGER.info("Received again a deployed message for an app already registered");
	                    		session.close();
	                    		// 26-09-2016 - END
	                    		
	                    		return;
	                    	}

	                    	if (jsontext.findValue("iaasVmId")==null){
	                    		LOGGER.info("Unable to parse AMQP deployment message, missing iaas id");
	                    		return;
	                    	} else {		                    
		                    	String iaasid = jsontext.findValue("iaasVmId").textValue();
	                    		vm.setIaasid(iaasid);
	                    		LOGGER.info("Received DEPLOYED message for iaas id"+ iaasid);
	                    	}
	                    	
	                    	mapper.createVM(vm);
	                    	session.close();
	                    	LOGGER.info("Received DEPLOYED message");
	                    }
	                    if (topic[6].equals("DELETED")){
	                    	
	                    	VirtualMachine vm = new VirtualMachine();
		                    vm.setApplicationid( topic[1]);
		                    vm.setDeploymentid( topic[3]);
		                    vm.setVmid(topic[5]);
		                    Date date = new Date();
	                    	vm.setStop(date.getTime());
	                    	String payload = textMessage.getText();
		                    ObjectMapper jmapper = new ObjectMapper();
		                    JsonNode jsontext = jmapper.readValue(payload, JsonNode.class);
		                    
		                    if (jsontext.findValue("providerId")==null){
	                    		LOGGER.info("Unable to parse AMQP deployment message, missing provider id (default value will be used)");
	                    		vm.setProviderid(defaultProviderId);
	                    	} else {
	                    		String providerid = jsontext.findValue("providerId").textValue();
	                    		vm.setProviderid(providerid);
	                    		LOGGER.info("Received DELETED message for provider id"+ providerid);
	                    	}

	                    	SqlSession session = appRegistry.getSession();
		                	AppRegistryMapper mapper = session.getMapper(AppRegistryMapper.class); 
	                    	int checkvm = mapper.checkVM(vm.getProviderid(), vm.getApplicationid(), vm.getDeploymentid(), vm.getVmid());
	                    	
	                    	LOGGER.info("Received DELETED for VM"+vm.getProviderid()+ vm.getApplicationid()+ vm.getDeploymentid()+ vm.getVmid());
	                    	if (checkvm==0){
	                    		// 26-09-2016 - BEGIN
	                    		//LOGGER.warn("Received a message for an app not being created");
	                    		LOGGER.info("Received a message for an app not being created");
	                    		session.close();
	                    		// 26-09-2016 - END
	                    		return;
	                    	}
	                    	
	                    	mapper.stopVM(vm);
	                    	session.close();
	                    	LOGGER.info("Received DELETED stop recorded");
	                    }
	                }	                
	            } catch (Exception e) {
	            	// 26-09-2016 - BEGIN
	            	LOGGER.info("Received EXCEPTION while writing app events:" + e);
	            	e.printStackTrace();
	            	//LOGGER.error("Received EXCEPTION while writing app events");
	            	// 26-09-2016 - END	                
	            }
	        }      
	    };
	    
	    // 26-09-2016 - BEGIN
	    //LOGGER.debug("Registering  "+appTopic);
	    LOGGER.info("Registering  "+appTopic);
	    // 26-09-2016 - END
	    paasQueuePublisher.registerListener(appTopic,appListener);    
        
	    
		MessageListener measureListener = new MessageListener(){
			DataConsumptionHandler dataMapper = dataConsumptionHandler;
			private ApplicationRegistry appRegistry=registry;
			
	        public void onMessage(Message message) {
		            try {
		            	// 26-09-2016 - BEGIN
	                	LOGGER.info("Before received information from ActiveMQ (METRIC topic)");
	                	// 26-09-2016 - END
	                	
		                if (message instanceof TextMessage) {
		                	TextMessage textMessage = (TextMessage) message;
		                    LOGGER.debug("Received start message" + textMessage.getText() + "'"+textMessage.getJMSDestination());
		                    String dest = message.getJMSDestination().toString();
		                    String[] topic = dest.split("\\.");
		                    		                    
		                    int counter;
		                    String ArgString;
		                    ArgString="-->";
		                    
		                    for (counter = 0; counter < topic.length; counter++) {
		                    	
		                    	if (counter == (topic.length-1))
		                    		ArgString = ArgString + topic[counter] + "<--";
		                    	else
		                    		ArgString = ArgString + topic[counter] + ".";
		                    }
		                    
		                    if (topic.length < 8){
		                    	LOGGER.info("Received a message of no interest for the EM:" + ArgString);
		                    	return;
		                    }
		                    else {
		                    	LOGGER.info("Received: " + ArgString);
		                    }
		                    		                    
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
			                    String paasvmid = topic[5];
			                    dc.setVmid(paasvmid);
			                    
			                    if (measureType.equals("net-power"))
			                    	dc.setMetrictype("power");
			                    else
			                    	dc.setMetrictype(measureType);
			                    	                    		                    	
		                    	String payload = textMessage.getText();
			                    ObjectMapper jmapper = new ObjectMapper();
			                    JsonNode jsontext = jmapper.readValue(payload, JsonNode.class);
			                    
			                    String providerid = defaultProviderId;
			                    if (jsontext.findValue("providerId")==null){
		                    		LOGGER.info("Unable to parse AMQP deployment message, missing provider id (default value will be used)");	                    		
		                    	} else {
		                    		providerid = jsontext.findValue("providerId").textValue();
		                    		LOGGER.info("Received METRIC message for provider id"+ providerid);
		                    	}
			                    
			                    dc.setProviderid(providerid); 

			                    SqlSession appsession = appRegistry.getSession();
		                    	AppRegistryMapper appmapper = appsession.getMapper(AppRegistryMapper.class); 
		                    	int count = appmapper.checkVM(dc.getProviderid(), dc.getApplicationid(), dc.getDeploymentid(), paasvmid);
		                    	appsession.close();
		                    	if (count==0){
		                    		// 26-09-2016 - BEGIN
		                    		// LOGGER.debug("Received  valid measure for a provider/application/deployment/vm not in the registry ("+ArgString+")");
		                    		LOGGER.info("Received  valid measure for a provider/application/deployment/vm not in the registry ("+ArgString+")");
		                    		// 26-09-2016 - END
		                    		return;
		                    	}
		                    	
		                    	if (jsontext.findValue("timestamp")==null){
		                    		LOGGER.info("Unable to parse AMQP deployment message, missing timestamp ("+ArgString+")");
		                    		return;
		                    	}                	
		                    		                    	
		                    	long timestamp =  jsontext.findValue("timestamp").longValue();
		                    	if (timestamp <=0){
		                    		// 26-09-2016 - BEGIN
		                    	 	//LOGGER.warn("Received non valid timestamp " +timestamp+" ("+ArgString+")");
		                    	 	LOGGER.info("Received non valid timestamp " +timestamp+" ("+ArgString+")");
		                    	 	// 26-09-2016 - END
		                    	 	return;
		                    	}
		                    			                    	
		                    	dc.setTime(timestamp);
		                    	
		                    	if (jsontext.findValue("value")==null){
		                    		LOGGER.info("Unable to parse AMQP deployment message, missing value ("+ArgString+")");
		                    		return;
		                    	}	                    
			                                        	
		                    	double value = jsontext.findValue("value").doubleValue();		                    	
		                    	if (value <0){
		                    		// 26-09-2016 - BEGIN
	                            	//LOGGER.warn("Received non valid measure " +value+" ("+ArgString+")");
	                            	LOGGER.info("Received non valid measure " +value+" ("+ArgString+")");
	                            	// 26-09-2016 - END
	                            	return;
	                            }
		                    	
		                    	// 26-09-2016 - BEGIN
		                    	//LOGGER.debug("Received "+measureType +" measure "+value+" ("+ArgString+")");
		                    	LOGGER.info("Received "+measureType +" measure "+value+" ("+ArgString+")");
		                    	// 26-09-2016 - END
		                    	
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
		                        
		                    	int samples = 0;
		                    	// LOGGER.info("***** FIND: prov="+providerid+", deployment="+topic[3]+", VM="+paasvmid+", Metric="+dc.getMetrictype()+", Time="+timestamp ); //MAXIM
		                    	samples = datamapper.getSamplesAtTime( providerid, topic[3], paasvmid, dc.getMetrictype(), timestamp);
		                    	
		                		if (samples == 0){		                    	
		                    	
		                    		datamapper.createMeasurement(dc);		                    		
		                    		LOGGER.info("Write "+measureType+" "+value+" timestamp "+timestamp+" ("+ArgString+")");		                    	
        	
       		
		                    		LOGGER.info("Received METRIC message ("+ArgString+")");
		                    	} else {

		                    		LOGGER.info("Received an existing METRIC message for timestamp="+timestamp+" (ignored)"+" ("+ArgString+")");
		                    	}
		                    	
		                    	datasession.close();		                    		                    	
		                    }
		                }
		            } catch (Exception e) {
		            	LOGGER.info("Received EXCEPTION while inserting data about measurements:" + e);
		                // System.out.println("EXCEPTION while inserting data about measurements:" + e);
		                e.printStackTrace();
		            }
		        }
		};

		
		LOGGER.debug("Received "+measurementsTopic);		
	    LOGGER.debug("Received "+measureListener);
		LOGGER.debug("Received "+paasQueuePublisher);
	    paasQueuePublisher.registerListener(measurementsTopic,measureListener);	

	    	
		MessageListener measureFromVMListener = new MessageListener(){
			
			DataConsumptionHandler dataMapper = dataConsumptionHandler;
			private CpuFeaturesHandler cpuMapper = cpuFeaturesHandler;
			private ApplicationRegistry appRegistry=registry;			
			
			public void onMessage(Message message) {				
				
				try {					
					
					if (message instanceof TextMessage) {
						// if (!enablePowerFromIaas) {						
						
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
		                	if (countcpu==0) {
		                		LOGGER.warn("Received measure for a cpu model ("+modelCPU+") not in the registry");
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
		                    	
		                	LOGGER.debug("Received measures: Timestamp="+timestamp+", CPU model="+modelCPU +", core="+coreNumber+", cpu_load="+valueCPULoad+", cpu_steal="+valueCPUSteal+", memory="+valueMemory );                    	
		                    		                                
		                	SqlSession datasession = dataMapper.getSession();
		                	DataConsumptionMapper datamapper = datasession.getMapper(DataConsumptionMapper.class);                      
	                    	
		                	
		                	int samplesMemory = datamapper.getSamplesAtTime( dc.getProviderid(), dc.getDeploymentid(), dc.getVmid(), "virtualmemory", timestamp);
		                	int samplesCPU = datamapper.getSamplesAtTime( dc.getProviderid(), dc.getDeploymentid(), dc.getVmid(), "virtualcpu", timestamp);
		                	int samplesPower = datamapper.getSamplesAtTime( dc.getProviderid(), dc.getDeploymentid(), dc.getVmid(), "virtualpower", timestamp);
              	
		                	if (samplesMemory == 0 && samplesCPU == 0 && samplesPower == 0){		                    	
		                    	
		                		dc.setMetrictype("virtualmemory");
			                	dc.setVmmemory(valueMemory);
		                		datamapper.createMeasurement(dc);
		                		LOGGER.info("Write memory "+valueMemory+" for provider="+dc.getProviderid()+", application="+dc.getApplicationid()+", deployment="+dc.getDeploymentid()+", vm="+dc.getVmid()+", time="+timestamp);	                    	
		                    			                			                		
		                		dc.setMetrictype("virtualcpu");
		                		dc.setVmmemory(0.0);
		                		dc.setVmcpu(valueCPULoad);
		                		datamapper.createMeasurement(dc);
		                		LOGGER.info("Write CPU "+valueCPULoad+" for provider="+dc.getProviderid()+", application="+dc.getApplicationid()+", deployment="+dc.getDeploymentid()+", vm="+dc.getVmid()+", time="+timestamp);
		                		
		                		dc.setMetrictype("virtualpower");
		                		dc.setVmmemory(0.0);
		                		dc.setVmcpu(0.0);
		                		double valuePower = calculateVMPower(modelCPU, coreNumber, valueCPULoad, valueCPUSteal);
		                		dc.setVmpower(valuePower);
		                		datamapper.createMeasurement(dc);
		                		LOGGER.info("Write power="+valuePower+" for provider="+dc.getProviderid()+", application="+dc.getApplicationid()+", deployment="+dc.getDeploymentid()+", vm="+dc.getVmid()+", time="+timestamp);                				                				
		                	} else {

		                		LOGGER.info("Received an existing memory METRIC message for timestamp="+timestamp+" (ignored)");
		                	}
		                    
		                	datasession.close();
		                	
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
	        	
				double netPower = 0.0;
				double totalPower = 0.0;
				
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
            		LOGGER.debug("Features for "+modelCPU+" cpu model: core "+hostCore+", TDP "+tdp+", maxpower "+maxPower+", minpower "+minPower);
        		
            		double notStealFactor=(100.0-valueCPUSteal)/100.0;
            		double coreFactor = (double )vmCore / (double )hostCore;
            		double vmMaxPower = coreFactor * notStealFactor * maxPower;
            		double vmMinPower = coreFactor * notStealFactor * minPower;
            		double vmDeltaPower = vmMaxPower - vmMinPower;
            		netPower = (valueCPULoad / 100) * vmDeltaPower;
            		totalPower = vmMinPower + netPower;
            		
            		LOGGER.debug("Calculated values: notStealFactor="+notStealFactor+", coreFactor="+coreFactor+", vmMaxPower="+vmMaxPower+", vmMinPower"+vmMinPower+", vmDeltaPower="+vmDeltaPower+", netPower="+netPower+", totalPower="+totalPower);            		
            		            	
            		LOGGER.info("Calculated VM power "+netPower);
            	
            	}
	        			
				return netPower;
			}	        	
		};
		
		LOGGER.debug("Received "+measurementsFromVMTopic);		
	    LOGGER.debug("Received "+measureFromVMListener);
		LOGGER.debug("Received "+paasQueuePublisher);
		paasQueuePublisher.registerListener(measurementsFromVMTopic,measureFromVMListener);		
	}
	
}
