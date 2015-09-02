/**
* Copyright 2014 Hewlett-Packard Development Company, L.P.                                         
 */
package eu.ascetic.asceticarchitecture.paas.component.energymodeller.internal.service;


import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.sql.Timestamp;
import java.util.Calendar;
import java.util.List;
import java.util.Properties;
import java.util.Vector;

import org.apache.log4j.Logger;

import eu.ascetic.asceticarchitecture.paas.component.energymodeller.datatype.ApplicationSample;
import eu.ascetic.asceticarchitecture.paas.component.energymodeller.datatype.EventSample;
import eu.ascetic.asceticarchitecture.paas.component.energymodeller.datatype.Unit;
import eu.ascetic.asceticarchitecture.paas.component.energymodeller.datatype.messages.GenericEnergyMessage;
import eu.ascetic.asceticarchitecture.paas.component.energymodeller.interfaces.PaaSEnergyModeller;
import eu.ascetic.asceticarchitecture.paas.component.energymodeller.internal.common.data.database.PaaSEMDatabaseManager;
import eu.ascetic.asceticarchitecture.paas.component.energymodeller.internal.common.data.database.dao.EMSettings;
import eu.ascetic.asceticarchitecture.paas.component.energymodeller.internal.common.data.database.table.DataConsumption;
import eu.ascetic.asceticarchitecture.paas.component.energymodeller.internal.common.data.database.table.DataEvent;
import eu.ascetic.asceticarchitecture.paas.component.energymodeller.internal.common.data.ibatis.ApplicationRegistry;
import eu.ascetic.asceticarchitecture.paas.component.energymodeller.internal.common.data.ibatis.DataConsumptionHandler;
import eu.ascetic.asceticarchitecture.paas.component.energymodeller.internal.common.dataservice.EnergyDataAggregatorServiceQueue;
import eu.ascetic.asceticarchitecture.paas.component.energymodeller.internal.common.dataservice.EnergyDataAggregatorServiceZabbix;
import eu.ascetic.asceticarchitecture.paas.component.energymodeller.internal.common.dataservice.EventDataAggregatorService;
import eu.ascetic.asceticarchitecture.paas.component.energymodeller.internal.common.dataservice.MonitoringDataService;
import eu.ascetic.asceticarchitecture.paas.component.energymodeller.internal.common.dataservice.ZabbixDataCollectorService;
import eu.ascetic.asceticarchitecture.paas.component.energymodeller.internal.loadinjector.LoadInjectorService;
import eu.ascetic.asceticarchitecture.paas.component.energymodeller.internal.model.PredictorBuilder;
import eu.ascetic.asceticarchitecture.paas.component.energymodeller.internal.model.predictor.PredictorInterface;
import eu.ascetic.asceticarchitecture.paas.component.energymodeller.internal.queue.EnergyModellerQueueServiceManager;
import eu.ascetic.asceticarchitecture.paas.component.energymodeller.internal.queue.client.AmqpClient;

/**
 * @author davide sommacampagna
 */

public class EnergyModellerService implements PaaSEnergyModeller {

	private final static Logger LOGGER = Logger.getLogger(EnergyModellerService.class.getName());
	
	private String propertyFile = "config.properties";
	private EMSettings emsettings;
	
	private PaaSEMDatabaseManager dbmanager;
	private LoadInjectorService loadInjector;	
	//private ZabbixDataCollectorService datacollector;
	private MonitoringDataService monitoringDataService;
	private DataConsumptionHandler dataCollectorHandler;
	private PredictorInterface predictor;
	// provides access to the registry by mean of session with mappers
	
	
	// amqp client allows publishing messages, while queue manager handle scheduling of measurement publishing and subscribe to special topics
	//private AmqpClient messageClient;
	private ApplicationRegistry appRegistry;
	private EnergyModellerQueueServiceManager queueManager;
	private AmqpClient queueclient;
	private String monitoringTopic="monitoring";
	private String predictionTopic="prediction";
	private Boolean queueEnabled = false;
	private EnergyDataAggregatorServiceQueue energyService;
	private EventDataAggregatorService eventService;

	
	/**
	 * 
	 * 
	 * MEASURE: get measurements from available samples, 
	 * 			Unit tells if the method should compute value of average instant power or accumulated energy consumption
	 * 
	 */	
	
	@Override
	public double measure(String providerid, String applicationid,List<String> vmids, String eventid, Unit unit,Timestamp start, Timestamp end) {
		
		
		if((start==null)&&(end==null)){
			LOGGER.info("Measuring from all available data since both timestamps are not specified");
			if (unit==Unit.ENERGY){
				LOGGER.info("Measuring energy consumption");
				double result = energyConsumption(providerid,applicationid,vmids,eventid,null,null);
				sendToQueue(monitoringTopic, providerid, applicationid, vmids, eventid, GenericEnergyMessage.Unit.WATTHOUR, null, result);
				return result;
			} else {
				LOGGER.info("Measuring average instant power"); 
				double result = averagePower(providerid,applicationid, vmids,  eventid,null,null);
				sendToQueue(monitoringTopic, providerid, applicationid, vmids, eventid, GenericEnergyMessage.Unit.WATT, null, result);
				LOGGER.info("Sending to queue"); 
				return result;
			}
		}else {
			LOGGER.info("Checking the timestamps"); 
			if (start==null){
				Calendar calendar = Calendar.getInstance();
				long now = calendar.getTime().getTime();
				now = now - (86400000);
				if (now>end.getTime()){
					now = end.getTime()-(86400000);
				}
				Timestamp currentTimestamp = new Timestamp(now);
				end = currentTimestamp;
				LOGGER.info("Going to get data from the last 24h :  "+now);
			}
			if (end==null){
				Calendar calendar = Calendar.getInstance();
				Timestamp currentTimestamp = new Timestamp(calendar.getTime().getTime());
				end = currentTimestamp;
				LOGGER.info("Going to get data untill now :  "+end.getTime());
			}
			if (unit==Unit.ENERGY){
				LOGGER.info("Measuring energy consumption");
				double result =  energyConsumption( providerid, applicationid, vmids,  eventid,  start,  end);
				sendToQueue(monitoringTopic, providerid, applicationid, vmids, eventid, GenericEnergyMessage.Unit.WATTHOUR, null, result);
				return result;
			} else {
				LOGGER.info("Measuring average instant power"); 
				double result =  averagePower(providerid,applicationid, vmids,  eventid,start,end);
				sendToQueue(monitoringTopic, providerid, applicationid, vmids, eventid, GenericEnergyMessage.Unit.WATT, null, result);
				return result;
			}
			
		}

	}

	@Override
	public double estimate(String providerid, String applicationid,	List<String> vmids, String eventid, Unit unit, long window) {
		LOGGER.info("Forecasting instant power"); 
		double currentval = predictor.estimate(providerid,applicationid, vmids, eventid, unit, window);	
		sendToQueue(predictionTopic, providerid, applicationid, vmids, eventid, GenericEnergyMessage.Unit.WATT, null, currentval);
		return currentval;
	}


	/**
	 * 
	 * 
	 * TRAINING function to be fully implemented in Y2
	 * 
	 */	
	
	@Override
	public boolean trainApplication(String providerid, String applicationid,String deploymentid, String eventid) {
		// TODO Y2 Implementation
		LOGGER.info("Starting to train application " + applicationid + " for this deployment " + deploymentid);
		LOGGER.info("Registering training");
		dbmanager.getMonitoringData().createTraining(applicationid, deploymentid, eventid);
		if (eventid==null){
			LOGGER.info("Application training");
			dbmanager.getMonitoringData().createMonitoring(applicationid, deploymentid, "");
			loadInjector.deployTrainingForApplication(applicationid, deploymentid);
		} else {
			LOGGER.info("Event training");
			loadInjector.deployTrainingForApplicationEvent(applicationid, deploymentid, eventid);
		}
		LOGGER.info("Training terminated");
		dbmanager.getMonitoringData().terminateTraining(applicationid, deploymentid);
		dbmanager.getMonitoringData().terminateMonitoring(applicationid, deploymentid);	
		return true;
	}

	/**
	 * 
	 * 
	 * Returns an EventSample for each event in the time interval, providing average instant power and energy consumption of each
	 * 
	 */	
	
	@Override
	public List<EventSample> eventsData(String providerid,String applicationid, List<String> vmids, String eventid,Timestamp start, Timestamp end) {
		List<EventSample> results = new Vector<EventSample>();
		for (String vm : vmids){
			results.addAll(eventsSamplesInInterval( providerid, applicationid,  vm,  eventid,start, end));
		}
		return results;
	}

	/**
	 * 
	 * 
	 * Returns an Application Sample for each sample in the give interval, sampled at the samplingperiod time
	 * 
	 */	
	
	@Override
	public List<ApplicationSample> applicationData( String providerid, String applicationid,List<String> vmids, long samplingperiod,Timestamp start, Timestamp end){
		List<DataConsumption> resultssamples = new Vector<DataConsumption>();
		List<ApplicationSample> results = new Vector<ApplicationSample>();
		for(String vm : vmids){
			List<DataConsumption> estim_sample = energyService.sampleMeasurements("app",applicationid, vm, start.getTime(),end.getTime(),samplingperiod);
			if (estim_sample!=null)resultssamples.addAll(estim_sample);
		}
		for(DataConsumption dc : resultssamples){
			ApplicationSample as = new ApplicationSample();
			as.setAppid(dc.getApplicationid());
			as.setVmid(dc.getVmid());
			as.setP_value(dc.getVmpower());
			as.setTime(dc.getTime());
			as.setC_value(dc.getCpu());
			as.setE_value(dc.getVmpower());
			results.add(as);
		}
		
		
		return results;
	}

	
	/**
	 * 
	 * GET ENERGY CONSUMED BY AN APPLICATION IF EVENT ID IS NULL  , OR THE TOTAL ENERGY CONSUMED BY THE SAME EVENT TYPE IF ITs ID IS PROVIDED
	 * 
	 */
	private double energyConsumption(String providerid,String applicationid, List<String> vmids, String eventid,Timestamp start,Timestamp end) {
		
			double total_energy=0;
			if (eventid==null){
				LOGGER.debug("Analytzing application");
				for (String vm : vmids) {
					double energy = energyForVM(providerid, applicationid, vm, eventid,null,null);
					LOGGER.info("This VM "+ vm + " consumed " + String.format( "%.2f", energy ));
					total_energy = total_energy +energy;
				}			
				LOGGER.info("Application consumed " + String.format( "%.2f", total_energy ));
			} else {
				LOGGER.debug("Analytzing event");
				for (String vm : vmids) {
					LOGGER.debug("Analyzing events on VM "+vm); 
					total_energy = total_energy + energyForVM(providerid, applicationid, vm, eventid,null,null);
				}
				LOGGER.info("Event energy Wh " +String.format( "%.2f", total_energy ));
			}
			return total_energy;
			
	}

	/**
	 * 
	 * GET THE AVERAGE OF INSTANT POWER MEASURED FOR AN APPLICATION IF EVENT ID IS NULL, OR EVENT IF IT IS PROVIDED
	 * 
	 */	
	
	private double averagePower(String providerid, String applicationid, List<String> vmids, String eventid, Timestamp start, Timestamp end) {
		if((start==null)&&(end==null)){
			if (eventid!=null){
				LOGGER.info("Measuring event average instant power (W)"); 
				double totalPower = 0;
				double countEvents = 0;
				for (String vm : vmids) {
					List<DataEvent> events = eventService.getEvents(applicationid, applicationid, vm, eventid,null,null);
					for (DataEvent de: events){
						countEvents++;
						double power  = energyService.getMeasureInIntervalFromVM(Unit.POWER, applicationid, vm, de.getBegintime(), de.getEndtime());
						LOGGER.info("This event power :  "+power);
						totalPower = totalPower + power;
					}
				}
				if (countEvents<=0) return 0;
				return totalPower/countEvents;
			}else{
				LOGGER.info("Measuring application average instant power (W) in the last 24 hours"); 
				double totalPower = 0;
				double countEvents = 0;
				Calendar c_tiemstamp = Calendar.getInstance();
				
				for (String vm : vmids) {
					countEvents++;
					double power  = energyService.getMeasureInIntervalFromVM(Unit.POWER, applicationid, vm, (c_tiemstamp.getTimeInMillis()-(86400*1000)), c_tiemstamp.getTimeInMillis());
					LOGGER.info("This vm power since one day :  "+power);
					totalPower = totalPower + power;
				}
				if (countEvents<=0)return 0;
				return totalPower;
			}
		}else{
			double tot_power=0;
			double countevents=0;
			if (eventid==null){
				for (String vm : vmids) {
					double power  = energyService.getMeasureInIntervalFromVM(Unit.POWER, applicationid, vm,  start.getTime(), end.getTime());
					LOGGER.info("Avg power for VM "+ vm + " was " + String.format( "%.2f", power ));
					if (power>0)tot_power = tot_power + power;
				}
				return tot_power;
			}else{
				for (String vm : vmids) {
					List<DataEvent> events = eventService.getEvents(applicationid, applicationid, vm, eventid,start,end);
					for (DataEvent de: events){
						
						double power  = energyService.getMeasureInIntervalFromVM(Unit.POWER, applicationid, vm,  de.getBegintime(), de.getEndtime());
						if (power > 0){
							LOGGER.info("This event power :  "+power);
						}
						if (power>0){
							tot_power = tot_power + power;
							countevents++;
						}
					}
				}
				if (countevents>0){
					LOGGER.info("I got sum of events power :  "+tot_power+" for total "+countevents+" events");
					return (tot_power/countevents);
				} else {
					return 0;
				}
			}
		}
	}
	

	
	/**
	 * 
	 * PRIVATE INTERNAL METHOD
	 * 
	 */

	private double energyForVM(String providerid, String applicationid, String vmid, String eventid, Timestamp start, Timestamp end) {
		List<String> vmids = new Vector<String>();
		vmids.add(vmid);
		if((start==null)&&(end==null)){
			if (eventid==null){
				LOGGER.info("Application energy estimation for " + applicationid );
				return energyService.getEnergyFromVM(applicationid, vmid, eventid);
			} else {
				LOGGER.info("Energy estimation for " + applicationid + " and its event " + eventid);
				int eventcount =0;
				double energyAverage = 0;
				List<DataEvent> events = eventService.getEvents(applicationid, applicationid, vmid, eventid, null, null);
				for (DataEvent de: events){
					double energy = energyService.getMeasureInIntervalFromVM(Unit.ENERGY, de.getApplicationid(),de.getVmid(),de.getBegintime(),de.getEndtime());
					LOGGER.debug("This event : "+ de.getBegintime() + " and " +de.getEndtime() + " energy "+energy);
					if (energy >0){
						eventcount++;
						energyAverage = energyAverage + energy;
					}
				}
				if (eventcount==0)return -1;
				LOGGER.info("Wh : "+ energyAverage + " over "+eventcount+" events");
				
				return (energyAverage/eventcount );
			}
		} else {
			if (eventid!=null){
				double energyAverage = 0;
				int eventcount =0;
				List<DataEvent> events = eventService.getEvents(applicationid, applicationid, vmid, eventid,start,end);
				for (DataEvent de: events){
					double energy = energyService.getMeasureInIntervalFromVM(Unit.ENERGY, de.getApplicationid(),de.getVmid(),de.getBegintime(),de.getEndtime());
					if (energy >0){
						eventcount++;
						energyAverage = energyAverage + energy;
					}
				}
				if (eventcount==0)return -1;
				LOGGER.info("Wh : "+ energyAverage + " over "+eventcount+" events in the specified interval");
				return (energyAverage/eventcount );			
				
			}else {
				return energyService.getMeasureInIntervalFromVM(Unit.ENERGY, applicationid, vmid, start.getTime(), end.getTime());
			}
				
		}	
	}
		

	
	private List<EventSample> eventsSamplesInInterval(String providerid,String applicationid, String vmid, String eventid,Timestamp start, Timestamp endtime) {
		
		List<EventSample> eSamples = new Vector<EventSample>();
		
		EventSample es = new EventSample();
		List<DataEvent> events = eventService.getEvents(applicationid, applicationid, vmid, eventid,start,endtime);
		for (DataEvent de: events){
			
			es = new EventSample();
			double power  = energyService.getMeasureInIntervalFromVM(Unit.ENERGY,applicationid, vmid, start.getTime(), endtime.getTime());
			double energy = energyService.getMeasureInIntervalFromVM(Unit.POWER, de.getApplicationid(),vmid,de.getBegintime(),de.getEndtime());
			if (energy > 0){
				LOGGER.info("This event :  "+energy);
				es.setTimestampBeging(de.getBegintime());
				es.setTimestampEnd(de.getEndtime());
				es.setVmid(vmid);
				es.setAppid(applicationid);
				es.setEventid(eventid);
				es.setEvalue(energy);
				es.setPvalue(power);
				eSamples.add(es);
			}
			
		}
		
		LOGGER.info("Total events collected : "+ eSamples.size());
		return eSamples;
	
	}
	
	/**
	 * 
	 * Queue message generation
	 *  
	 */
	
	
	private void sendToQueue(String queue,String providerid,String applicationid, List<String> vms, String eventid, GenericEnergyMessage.Unit unit, String referenceTime,double value){
		
		if (queueEnabled) queueManager.sendToQueue(queue, providerid, applicationid, vms, eventid, unit, referenceTime, value);
		
	}
		
	
	/**
	 * Constructor, settings loader 
	 */
	
	public EnergyModellerService(String propertyFile) {
		this.propertyFile=propertyFile;
		LOGGER.info("EM Initialization ongoing");
		configureProperty();
		// Not yet implemented this workflow
		//initializeLoadInjector();
		initializeDataConnectors();
		initializePrediction();
		initializeQueueServiceManager();
		LOGGER.info("EM Initialization complete");
	}

	
	/**
	 * private methods for initialization and configuration of this component
	 */
	
	private void configureProperty(){
		LOGGER.debug("Configuring settings and EM PaaS Database");
		InputStream inputStream;
		try {
			inputStream = new FileInputStream(propertyFile);
			Properties props = new Properties();
			props.load(inputStream);
			emsettings = new EMSettings(props);
			LOGGER.info("Properties loaded");
			dbmanager = new PaaSEMDatabaseManager();
			dbmanager.setup(emsettings);
			LOGGER.debug("Configured");
		} catch (IOException e) {
			LOGGER.error("Properties not loaded due to a failure");
			LOGGER.error("Properties not loaded, file not found!");
			e.printStackTrace();
		}
	}
	
	private void initializeLoadInjector(){
		LOGGER.debug("Configuring load injector");
		loadInjector = new LoadInjectorService();
		loadInjector.configureLoadInjector(emsettings.getServerPath(), emsettings.getServerurl(), emsettings.getPropertyFile(), emsettings.getJmxFilePath());
		LOGGER.debug("Configured");
	}
	
	private void initializeDataConnectors(){
		LOGGER.debug("Setting connection to data sources for events and energy ");
		energyService = new EnergyDataAggregatorServiceQueue();
		eventService = new EventDataAggregatorService();
		eventService.setDaoEvent(dbmanager.getDataEventDAOImpl());
		monitoringDataService = new MonitoringDataService();
		monitoringDataService.setDataDAO(dbmanager.getMonitoringData());
		LOGGER.debug("Configured ");
	}
		
	private void initializeQueueServiceManager(){
		LOGGER.info("Loading Queue service manager "+emsettings.getEnableQueue() +emsettings.getAmanagertopic());
		if (emsettings.getEnableQueue().equals("true")) {
			
			try {
				queueclient = new AmqpClient();
				queueclient.setup(emsettings.getAmqpUrl(), emsettings.getAmqpUser(), emsettings.getAmqpPassword(), emsettings.getMonitoringQueueTopic());
				appRegistry = ApplicationRegistry.getRegistry(emsettings.getPaasdriver(),emsettings.getPaasurl(),emsettings.getPaasdbuser(),emsettings.getPaasdbpassword());
				dataCollectorHandler = DataConsumptionHandler.getHandler(emsettings.getPaasdriver(),emsettings.getPaasurl(),emsettings.getPaasdbuser(),emsettings.getPaasdbpassword());
				energyService.setDataMapper(dataCollectorHandler.getMapper());
				LOGGER.info("Enabled");
				queueManager = new EnergyModellerQueueServiceManager(queueclient,appRegistry,dataCollectorHandler);
				LOGGER.debug("Enabled"+queueclient);
				LOGGER.debug("Enabled"+appRegistry);
				queueManager.createConsumers(emsettings.getAmanagertopic(),emsettings.getPowertopic());
				
			} catch (Exception e) {
				LOGGER.error("ERROR initializing queue disabling the component..");
				emsettings.setEnableQueue("false");
				e.printStackTrace();
			}
		}
		LOGGER.info("Loaded");
	}
	
	private void initializePrediction(){
		LOGGER.info("EM predictor module loaded");
		predictor= PredictorBuilder.getPredictor();
	}
	
	/**
	 * private methods for collecting data
	 */
		

	@Override
	public void manageComponent(String token, String command) {
		
	}

	@Override
	public boolean subscribeMonitoring(String applicationid, String deploymentid, String eventid, long timewindow, Unit unit) {
		monitoringDataService.startMonitoring(applicationid, deploymentid, eventid);
		return true;
	}

	@Override
	public boolean unsubscribeMonitoring(String applicationid,String deploymentid, String eventid, long timewindow, Unit unit) {
		monitoringDataService.stopMonitoring(applicationid, deploymentid);
		return false;
	}	
	
	/**
	 * @return the emsettings of the Energy Modeller. It allows to get the current settings loaded from file
	 */
	public EMSettings getEmsettings() {
		return emsettings;
	}

	/**
	 * @param emsettings allows to inject settings in to the PaaS Energy Modeller
	 */
	public void setEmsettings(EMSettings emsettings) {
		this.emsettings = emsettings;
	}
		
}
