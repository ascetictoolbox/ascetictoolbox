/**
* Copyright 2014 Hewlett-Packard Development Company, L.P.                                         
 */
package eu.ascetic.asceticarchitecture.paas.component.energymodeller.service;


import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.sql.Timestamp;
import java.util.List;
import java.util.Properties;
import java.util.Vector;

import org.apache.log4j.Logger;

import eu.ascetic.asceticarchitecture.iaas.zabbixApi.datamodel.HistoryItem;
import eu.ascetic.asceticarchitecture.paas.component.common.database.PaaSEMDatabaseManager;
import eu.ascetic.asceticarchitecture.paas.component.common.model.DataEvent;
import eu.ascetic.asceticarchitecture.paas.component.common.model.TimeEnergyInterpolator;
import eu.ascetic.asceticarchitecture.paas.component.energymodeller.datatype.EMSettings;
import eu.ascetic.asceticarchitecture.paas.component.energymodeller.datatype.EnergySample;
import eu.ascetic.asceticarchitecture.paas.component.energymodeller.datatype.Sample;
import eu.ascetic.asceticarchitecture.paas.component.energymodeller.interfaces.PaaSEnergyModeller;
import eu.ascetic.asceticarchitecture.paas.component.energymodeller.service.task.DataCollector;
import eu.ascetic.asceticarchitecture.paas.component.energymodeller.service.task.EnergyDataAggregatorService;
import eu.ascetic.asceticarchitecture.paas.component.energymodeller.service.task.EventDataAggregatorService;
import eu.ascetic.asceticarchitecture.paas.component.loadinjector.interfaces.LoadInjectorInterface;
import eu.ascetic.asceticarchitecture.paas.component.loadinjector.service.LoadInjectorService;

/**
 * @author davide sommacampagna
 */

public class EnergyModellerSimple implements PaaSEnergyModeller {

	private String propertyFile = "config.properties";
	
	private final static Logger LOGGER = Logger.getLogger(EnergyModellerSimple.class.getName());
	
	private LoadInjectorInterface loadInjector;	
	private EMSettings emsettings;
	private PaaSEMDatabaseManager dbmanager;
	private DataCollector datacollector;
	private EnergyDataAggregatorService energyService;
	private EventDataAggregatorService eventService;


	/**
	 * 
	 * GET ENERGY FOR WHOLE APPLICATION
	 * 
	 */
	
	@Override
	public double energyApplicationConsumption(String providerid,String applicationid, List<String> vmids, String eventid) {
		double total_energy=0;
		// Ensure all vm energy data is loaded in to PaaS Database
		//this.loadEnergyData(applicationid, vmids);
		// check if the request is for a specific event or for the whole application/vm
		if (eventid==null){
			// Sum each consumption in every VM
			for (String vm : vmids) {
				double energy = energyEstimationForVM(providerid, applicationid, vm, eventid);
				LOGGER.info("This VM "+ vm + " consumed " + String.format( "%.2f", energy ));
				total_energy = total_energy +energy;
			}			
			LOGGER.info("Application consumed " + String.format( "%.2f", total_energy ));
		} else {
			//  also ensure event data is loaded
			this.loadEnergyData(applicationid, vmids);
			this.loadEventData(applicationid, vmids,eventid);
			LOGGER.debug("## Loading events data ##");
			// if more vm are there, then is the sum of the average consumption over each vm
			for (String vm : vmids) {
				LOGGER.debug("This events consumed on VM "+vm); 
				return energyEstimationForEventInVM(providerid, applicationid, vm, eventid);
				
			}
		}
		return total_energy;
	}
	
	/**
	 * 
	 * ESTIMATE ENERGY  
	 * 
	 */

	@Override
	public double energyEstimation(String providerid, String applicationid,	List<String> vmids, String eventid) {
		double energy = energyApplicationConsumption(providerid,applicationid,vmids,eventid);
		if (eventid==null){
			LOGGER.info("Application consumed " +String.format( "%.2f", energy ));
		} else {
			LOGGER.info("Event consumed " +String.format( "%.2f", energy ));
		}
		return energy;
	}	


	/**
	 * 
	 * GET ENERGY WITHIN TIME 
	 * 
	 */
	
	@Override
	public double energyApplicationConsumptionTimeInterval(String providerid,String applicationid, String vmids, String eventid,Timestamp start, Timestamp end) {
		
		List<String> vmidsv = new Vector<String>();
		vmidsv.add(vmids);
		if (eventid!=null)loadEventData(applicationid, vmidsv,eventid);
		loadEnergyData(applicationid, vmidsv);
		if (eventid!=null){
			return energyEstimationForEventInInterval(providerid,applicationid, vmids,  eventid,start, end);
		} else {
			double res =  energyService.getAverageInInterval(applicationid, vmids, eventid, start.getTime(), end.getTime());
			LOGGER.info(" RES "+res);
			return res;
		}
		
		
		
		

	}

	@Override
	public List<EnergySample> energyApplicationConsumptionData(String providerid,	String applicationid, String vmid, String eventid,Timestamp start, Timestamp end) {
		List<String> vmidsv = new Vector<String>();
		vmidsv.add(vmid);
		if (eventid==null){
			this.loadEnergyData(applicationid, vmidsv);
			return this.energyVMDataInInterval(providerid, applicationid, vmid, start, end);
		}else {
			
			this.loadEventData(applicationid, vmidsv,eventid);
			this.loadEnergyData(applicationid, vmidsv);
			return energyEventDataForEventInInterval( providerid, applicationid,  vmid,  eventid,start, end);
		}
		
		
	}
	
	
	@Override
	public List<Sample> applicationData(String providerid,String applicationid, String vmids, String eventid,long samplingperiod, Timestamp start, Timestamp end) {
		
		
		//List<HistoryItem> hcpu = this.datacollector.getSeriesHistoryForItemInterval("apptest","deptest","CPU user time", datacollector.searchFullHostsname(vmids), start.getTime(), end.getTime());
		List<HistoryItem> hpower = this.datacollector.getSeriesHistoryForItemInterval("apptest","deptest","Power", datacollector.searchFullHostsname(vmids), start.getTime(), end.getTime());
		//List<HistoryItem> hmemory = this.datacollector.getSeriesHistoryForItemInterval("apptest","deptest","Memory", datacollector.searchFullHostsname(vmids), start.getTime(), end.getTime());
		
		
		
		return null;
	}
	
	@Override
	public boolean startModellingApplicationEnergy(String providerid,String applicationid, String deploymentid) {
		LOGGER.info("Registering application " + applicationid + " with deployment " + deploymentid + " for monitoring");
		dbmanager.getMonitoringData().createMonitoring(applicationid, deploymentid, "");
		return true;
	}

	@Override
	public boolean stopModellingApplicationEnergy(String providerid,String applicationid, String deploymentid) {
		// update db
		LOGGER.info("Application " + applicationid + " with deployment " + deploymentid + " removed from monitoring");
		dbmanager.getMonitoringData().terminateMonitoring(applicationid, deploymentid);
		return true;
	}	
	
	/**
	 * 
	 * 
	 * TRAINING
	 * 
	 */
	
	@Override
	public boolean trainApplication(String providerid, String applicationid, String deploymentid, String eventid) {
		LOGGER.info("Starting to train application " + applicationid + " for this deployment " + deploymentid);
		LOGGER.info("Registering training");
		dbmanager.getMonitoringData().createTraining(applicationid, deploymentid, eventid);
		if (eventid==null){
			LOGGER.info("Application training");
			loadInjector.deployTrainingForApplication(applicationid, deploymentid);
		} else {
			LOGGER.info("Event training");
			loadInjector.deployTrainingForApplicationEvent(applicationid, deploymentid, eventid);
		}
		LOGGER.info("Training terminated");
		dbmanager.getMonitoringData().terminateTraining(applicationid, deploymentid);
		return true;
	}
	
	/**
	 * 
	 * PRIVATE INTERNAL METHOD
	 * 
	 */
	

	
	//@Override
	private double energyEstimationForVM(String providerid, String applicationid, String vmid, String eventid) {
		List<String> vmids = new Vector<String>();
		vmids.add(vmid);
		this.loadEnergyData(applicationid, vmids);
		if (eventid==null){
			LOGGER.info("Energy estimation for " + applicationid );

			//datacollector.handleConsumptionData(applicationid, vm, "deployment1");
			double energy =  energyService.getAverage(applicationid, "deployment1", vmid, eventid);
			
			return energy;
		} else {
			this.loadEnergyData(applicationid, vmids);
			LOGGER.info("Energy estimation for " + applicationid + " and event " + eventid);
			LOGGER.info("############################Loading events data "); 

			int eventnum =0;
			double generalAvg = 0;
			List<DataEvent> events = eventService.getEvents(applicationid, applicationid, vmid, eventid);
			for (DataEvent de: events){
				
				LOGGER.info(" got event "+ de.getBegintime() + " untill" +de.getEndtime() + " on "+de.getDeploymentid()+ " vm "+de.getVmid());
				double energy = averageEventConsumption(de.getApplicationid(),de.getVmid(),de.getBegintime(),de.getEndtime());
				LOGGER.info("This event : "+ de.getBegintime() + " and " +de.getEndtime() + " energy "+energy);
				LOGGER.info("This event : "+ de.getBegintime() + " and " +de.getEndtime() + " energy "+energy);
				if (energy >0){
					eventnum++;
					generalAvg = generalAvg + energy;
				}
			}
			if (eventnum==0)return -1;
			LOGGER.info("Wh : "+ generalAvg + " over "+eventnum);
			
			
			return (generalAvg/eventnum );

		}
		
	}
	
	
	//@Override
	private double energyEstimationForEventInVM(String providerid,String applicationid, String vmid, String eventid) {
				
		double generalAvg = 0;
		int eventnum =0;
		List<DataEvent> events = eventService.getEvents(applicationid, applicationid, vmid, eventid);
		for (DataEvent de: events){
			
			double energy = averageEventConsumption(de.getApplicationid(),de.getVmid(),de.getBegintime(),de.getEndtime());
			LOGGER.debug("This event : "+ de.getBegintime() + " and " +de.getEndtime() + " energy "+energy);
			if (energy >0){
				eventnum++;
				generalAvg = generalAvg + energy;
			}
				
		}
		if (eventnum==0)return -1;
		LOGGER.info("Wh : "+ generalAvg + " over "+eventnum);
		
		
		return (generalAvg/eventnum );		
	
	}
	
	private double energyEstimationForEventInInterval(String providerid,String applicationid, String vmid, String eventid,Timestamp start, Timestamp endtime) {

		
		double generalAvg = 0;
		int eventnum =0;
		List<DataEvent> events = eventService.getEventsInTime(applicationid, applicationid, vmid, eventid,start,endtime);
		for (DataEvent de: events){
			
			double energy = averageEventConsumption(de.getApplicationid(),de.getVmid(),de.getBegintime(),de.getEndtime());
			//if (energy > 0)LOGGER.info("This event : "+ de.getBegintime() + " and " +de.getEndtime() + " energy "+energy);
			if (energy >0){
				eventnum++;
				generalAvg = generalAvg + energy;
			}
			
		}
	
		if (eventnum==0)return -1;
		LOGGER.info("Wh : "+ generalAvg + " over "+eventnum);
		
		
		return (generalAvg/eventnum );	
	
	}
	
	
	
	private List<EnergySample> energyEventDataForEventInInterval(String providerid,String applicationid, String vmid, String eventid,Timestamp start, Timestamp endtime) {
		

		List<EnergySample> eSamples = new Vector<EnergySample>();
		
		EnergySample es = new EnergySample();
		List<DataEvent> events = eventService.getEventsInTime(applicationid, applicationid, vmid, eventid,start,endtime);
		for (DataEvent de: events){
			
			es = new EnergySample();
			double power  = energyService.getAvgPower(applicationid, vmid, eventid, start.getTime(), endtime.getTime());
			double energy = averageEventConsumption(de.getApplicationid(),vmid,de.getBegintime(),de.getEndtime());
			if (energy > 0){
				LOGGER.info("This event :  "+energy);
				es.setTimestampBeging(de.getBegintime());
				es.setTimestampBeging(de.getEndtime());
				es.setVmid(vmid);
				es.setE_value(energy);
				es.setP_value(power);
				eSamples.add(es);
			}
			
			
			
		}
		
		LOGGER.info("Total samples : "+ eSamples.size());
		return eSamples;
	
	}
	
	private List<EnergySample> energyVMDataInInterval(String providerid,String applicationid, String vmid,Timestamp start, Timestamp endtime) {
		List<EnergySample> eSamples = energyService.getSamplesInInterval(applicationid, applicationid, vmid, start,endtime);
		LOGGER.info("Total samples : "+ eSamples.size());
		return eSamples;
	}
	
	public double averageEventConsumption(String appid, String vmid,long start, long end){
		
		double energy = energyService.getAverageInInterval(appid, vmid, null, start, end);
		return energy;
	}
	
	
	/**
	 * Constructor, settings loader 
	 */
	
	public EnergyModellerSimple(String propertyFile) {
		this.propertyFile=propertyFile;
		LOGGER.info("EM Initialization ongoing");
		initializeProperty();
		initializeLoadInjector();
		initializeDataConnectors();
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
	 * private methods
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
		datacollector = new DataCollector();
		datacollector.setAMPath(emsettings.getAppmonitor());
		datacollector.setup();
		//datacollector.setIaasdatadriver(dbmanager.getIaasdatadao());
		datacollector.setDataconumption(dbmanager.getDataConsumptionDAOImpl());
		datacollector.setDataevent(dbmanager.getDataEventDAOImpl());
		energyService = new EnergyDataAggregatorService();
		energyService.setDataDAO(dbmanager.getDataConsumptionDAOImpl());
		eventService = new EventDataAggregatorService();
		eventService.setDaoEvent(dbmanager.getDataEventDAOImpl());
		LOGGER.debug("Configured ");
	}
	

	
	private void loadEventData(String appid,List<String> vms,String eventid){
		datacollector.handleEventData(appid, "deployment1", vms, eventid);
	}
	
	
	private void loadEnergyData(String appid, List<String> vm){
		datacollector.handleConsumptionData(appid, vm, "deployment1");
	}

	

//	//@Override
//	private double energyConsumptionAtTime(String providerid,String applicationid, List<String> vmids, String eventid,Timestamp time) {
//		this.loadEnergyData(applicationid, vmids);
//		double total=0;
//		DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
//		for (String vm : vmids) {
//			EnergyInterpolator ei = new EnergyInterpolator();
//			ei.providedata(dbmanager.getDataConsumptionDAOImpl());
//			LOGGER.info("Building estimator for "+vm);
//			ei.buildmodel(applicationid, vm);
//			
//			LOGGER.info("Using estimator " );
//			LOGGER.info("Time  is " +  dateFormat.format(time));
//			LOGGER.info("VM consumed " +ei.estimate(time.getTime()/1000));
//
//		}
//		return total;
//	}	
//
//	//@Override
//	private double energyConsumptionAtWorkload(String providerid,String applicationid, List<String> vmids, String eventid,double workload) {
//		this.loadEnergyData(applicationid, vmids);
//		double total=0;
//		for (String vm : vmids) {
//			WorkLoadEnergyInterpolator wei = new WorkLoadEnergyInterpolator();
//			wei.providedata(dbmanager.getDataConsumptionDAOImpl());
//			wei.buildmodel(applicationid, vm);
//			total = total+wei.estimate(workload);
//			LOGGER.info("VM will consume at workload " +wei.estimate(workload));
//			
//		}
//		
//		LOGGER.info("VM will consume at workload " +total);
//		
//		return total;
//	}
//	
//	//@Override
//	private double energyEstimationForTime(String providerid,String applicationid, List<String> vmids, String eventid,Timestamp time) {
//		this.loadEnergyData(applicationid, vmids);
//		double total=0;
//		DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
//		for (String vm : vmids) {
//			
//			TimeEnergyInterpolator te = new TimeEnergyInterpolator();
//			te.providedata(dbmanager.getDataConsumptionDAOImpl());
//			LOGGER.info("Building estimator for "+vm);
//			te.buildmodel(applicationid, vm);
//			
//			LOGGER.info("Using estimator " );
//			
//			LOGGER.info("Time  is " +  dateFormat.format(time));
//			double cons = te.estimate(time.getTime()/1000);
//			total =  total + cons;
//			LOGGER.info("VM consumed " +cons);
//		}
//		return total;
//	}

	
	/**
	 * 
	 * 
	 * DEPRECATED METHODS
	 * 
	 */
	
//	@Deprecated
//	@Override
//	public double energyApplicationConsumption(String providerid,String applicationid, String deploymentid) {
//		//TODO check from db if data has been collected in past or training occurred
//		datacollector.handleConsumptionData(applicationid,deploymentid);
//		double energy = energyService.getTotal(applicationid, deploymentid, "");
//		LOGGER.info("Application consumed " + String.format( "%.2f", energy ));
//		return energy;
//	}
	
//	@Deprecated
//	@Override
//	public double energyEstimation(String providerid, String applicationid,	String deploymentid,  String eventid) {
//		//TODO integrate prediction model
//		this.loadEnergyData(applicationid,deploymentid);
//		if (eventid==null){
//			LOGGER.info("Energy estimation for " + applicationid );
//			datacollector.handleConsumptionData(applicationid,deploymentid);
//			double energy = energyService.getTotal(applicationid, deploymentid, "");
//			LOGGER.info("Application consumed " +String.format( "%.2f", energy ));
//			return energy;
//			 
//		} else {
//			LOGGER.info("Energy estimation for " + applicationid + " and event " + eventid);
//			datacollector.handleEventData(applicationid,deploymentid,eventid);
//			datacollector.handleConsumptionData(applicationid,deploymentid);
//			double total_event = eventService.getTotal(applicationid, deploymentid, eventid);
//			double total_energy = energyService.getTotal(applicationid, deploymentid, eventid);
//			LOGGER.info("Event total "+total_event+" consumed " + String.format( "%.2f", total_energy ));
//			if (total_event == 0) return 0;
//			return total_energy/total_event;
//		}
//		
//	}
	
}
