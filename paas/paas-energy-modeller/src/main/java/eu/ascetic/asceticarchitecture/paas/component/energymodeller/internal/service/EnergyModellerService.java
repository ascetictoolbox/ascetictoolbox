/**
* Copyright 2014 Hewlett-Packard Development Company, L.P.                                         
 */
package eu.ascetic.asceticarchitecture.paas.component.energymodeller.internal.service;


import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.sql.Timestamp;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Properties;
import java.util.Vector;

import org.apache.log4j.Logger;

import eu.ascetic.asceticarchitecture.iaas.zabbixApi.datamodel.HistoryItem;
import eu.ascetic.asceticarchitecture.paas.component.energymodeller.datatype.ApplicationSample;
import eu.ascetic.asceticarchitecture.paas.component.energymodeller.datatype.EventSample;
import eu.ascetic.asceticarchitecture.paas.component.energymodeller.datatype.Unit;
import eu.ascetic.asceticarchitecture.paas.component.energymodeller.datatype.messages.GenericEnergyMessage;
import eu.ascetic.asceticarchitecture.paas.component.energymodeller.interfaces.PaaSEnergyModeller;
import eu.ascetic.asceticarchitecture.paas.component.energymodeller.internal.common.data.database.PaaSEMDatabaseManager;
import eu.ascetic.asceticarchitecture.paas.component.energymodeller.internal.common.data.database.dao.EMSettings;
import eu.ascetic.asceticarchitecture.paas.component.energymodeller.internal.common.data.database.table.DataEvent;
import eu.ascetic.asceticarchitecture.paas.component.energymodeller.internal.common.data.queue.MessageParserUtility;
import eu.ascetic.asceticarchitecture.paas.component.energymodeller.internal.common.dataservice.EnergyDataAggregatorService;
import eu.ascetic.asceticarchitecture.paas.component.energymodeller.internal.common.dataservice.EventDataAggregatorService;
import eu.ascetic.asceticarchitecture.paas.component.energymodeller.internal.common.dataservice.MonitoringDataService;
import eu.ascetic.asceticarchitecture.paas.component.energymodeller.internal.common.dataservice.ZabbixDataCollectorService;
import eu.ascetic.asceticarchitecture.paas.component.energymodeller.internal.loadinjector.LoadInjectorService;
import eu.ascetic.asceticarchitecture.paas.component.energymodeller.internal.model.interpolator.old.GenericValuesInterpolator;
import eu.ascetic.asceticarchitecture.paas.component.energymodeller.internal.queue.QueueManager;

/**
 * @author davide sommacampagna
 */

public class EnergyModellerService implements PaaSEnergyModeller {

	private final static Logger LOGGER = Logger.getLogger(EnergyModellerService.class.getName());
	
	private String propertyFile = "config.properties";
	private EMSettings emsettings;
	
	private PaaSEMDatabaseManager dbmanager;
	private LoadInjectorService loadInjector;	
	private ZabbixDataCollectorService datacollector;
	private MonitoringDataService monitoringDataService;
	
	private QueueManager queueManager;
	private String monitoringTopic="monitoring";
	private String predictionTopic="prediction";
	private Boolean queueEnabled = false;
	private EnergyDataAggregatorService energyService;
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
		
		// call measurement, this updates values and also ensure data is loaded
		double currentval = this.measure(providerid, applicationid, vmids, eventid, unit, null, null);
		
		
		
		
		// not yet estimating
		
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
		this.loadEventData(applicationid, vmids,eventid);
		this.loadEnergyData(applicationid, vmids);
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
		List<ApplicationSample> results = new Vector<ApplicationSample>();
		for(String vm : vmids){
			List<ApplicationSample> estim_sample = energyService.sampleMeasurements(applicationid, vm, start.getTime(),end.getTime(),samplingperiod);
			if (estim_sample!=null)results.addAll(estim_sample);
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
				this.loadEventData(applicationid, vmids,eventid);
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
		this.loadEnergyData(applicationid, vmids);
		if((start==null)&&(end==null)){
			if (eventid!=null){
				LOGGER.info("Measuring event average instant power (W)"); 
				this.loadEventData(applicationid, vmids,eventid);
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
				loadEventData(applicationid, vmids,eventid);
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
		this.loadEnergyData(applicationid, vmids);
		if((start==null)&&(end==null)){
			if (eventid==null){
				LOGGER.info("Application energy estimation for " + applicationid );
				return energyService.getEnergyFromVM(applicationid, "deployment1", vmid, eventid);
			} else {
				this.loadEventData(applicationid, vmids,eventid);
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
		initializeProperty();
		initializeLoadInjector();
		initializeDataConnectors();
		initializeQueueService();
		LOGGER.info("EM Initialization complete");
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
	
	/**
	 * private methods for initialization and configuration of this component
	 */
	
	private void initializeProperty(){
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
		datacollector = new ZabbixDataCollectorService();
		datacollector.setAMPath(emsettings.getAppmonitor());
		datacollector.setup();
		datacollector.setDataconumption(dbmanager.getDataConsumptionDAOImpl());
		datacollector.setDataevent(dbmanager.getDataEventDAOImpl());
		energyService = new EnergyDataAggregatorService();
		energyService.setDataDAO(dbmanager.getDataConsumptionDAOImpl());
		eventService = new EventDataAggregatorService();
		eventService.setDaoEvent(dbmanager.getDataEventDAOImpl());
		monitoringDataService = new MonitoringDataService();
		monitoringDataService.setDataDAO(dbmanager.getMonitoringData());
		LOGGER.debug("Configured ");
	}
	
	private void initializeQueueService(){
		if (emsettings.getEnableQueue()=="true"){
			queueManager = new QueueManager();
			try {
				queueManager.setup(emsettings);
				LOGGER.info("Initialized queue manager ");
			} catch (Exception e) {
				LOGGER.error("Issue while configuring the queue manager ");
				e.printStackTrace();
			}
		}
	}
	
	/**
	 * private methods for collecting data
	 */

	
	private void loadEventData(String appid,List<String> vms,String eventid){
		LOGGER.debug("Loading event data");
		datacollector.handleEventData(appid, "deployment1", vms, eventid);
		LOGGER.debug("Loaded event data");
	}
	
	
	private void loadEnergyData(String appid, List<String> vm){
		LOGGER.debug("Loading power data");
		datacollector.handleConsumptionData(appid, vm);
		LOGGER.debug("Loaded power data");
	}
	
	/**
	 * 
	 * 
	 * Math internal functions
	 * 
	 */
	
//	private double integrate(double powera,double powerb, long timea,long timeb){
//		return 	Math.abs((timeb-timea)*(powera+powerb)*0.5)/3600;
//	}

	/**
	 * those methods are legacy or for future implementation and here only temporarly 
	 */
	
//	private List<HistoryItem> buildenergyHistory (List<HistoryItem> power){
//		if (power==null)return null;
//		List<HistoryItem> results = new Vector<HistoryItem>();
//		HistoryItem previous=null;
//		for (int i=0;i<power.size();i++){
//			HistoryItem item = power.get(i);
//			HistoryItem energyitem = new HistoryItem();
//			if (previous!=null){
//				Double energy = integrate(new Double(previous.getValue()).doubleValue(),new Double(item.getValue()).doubleValue(),previous.getClock(),item.getClock());
//				energyitem.setValue(energy.toString());
//				energyitem.setClock(item.getClock());
//				results.add(energyitem);
//			} else {
//				Double energy = integrate(0,new Double(item.getValue()).doubleValue(),0,0);
//				energyitem.setValue(energy.toString());
//				energyitem.setClock(item.getClock());
//				results.add(energyitem);
//			}
//			
//			
//			previous = item;
//		}
//		return results;
//		
//	}
	
//	private GenericValuesInterpolator getInterpolator (List<HistoryItem> items){
//		if (items==null){
//			LOGGER.info(" Samples not available");
//			return null;
//		}
//		if (items.size()<=0){
//			LOGGER.info(" Samples empty");
//			return null;
//		}
//		GenericValuesInterpolator genInt = new GenericValuesInterpolator();
//		double[] timeseries = new double[items.size()];
//		double[] dataseries = new double[items.size()];
//		HistoryItem item;
//		genInt.setLasttime(items.get(0).getClock());
//		genInt.setStarttime(items.get(items.size()-1).getClock());
//		int count=0;
//		for (int i=items.size()-1;i>=0;i--){
//			item = items.get(i);
//			timeseries[count]=(item.getClock()-genInt.getStarttime());
//			dataseries[count]=Double.parseDouble(item.getValue());
//			//LOGGER.info(" Sample: "+count+";"+timeseries[count]+";"+dataseries[count]);
//			count++;
//		}
//		
//		genInt.buildmodel(timeseries, dataseries); 
//		return genInt;
//		
//	}	
//
//	private double energsyEstimation(String providerid, String applicationid,	List<String> vmids, String eventid) {
//		double energy = energyConsumption(providerid,applicationid,vmids,eventid,null,null);
//		if (eventid==null){
//			LOGGER.info("Application consumed " +String.format( "%.2f", energy ));
//		} else {
//			LOGGER.info("Event consumed " +String.format( "%.2f", energy ));
//		}
//		return energy;
//	}


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
		
}
