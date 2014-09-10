package eu.ascetic.asceticarchitecture.paas.component.energymodeller.service;


import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.text.DateFormat;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Properties;
import java.util.Vector;

import org.apache.log4j.Logger;

import eu.ascetic.asceticarchitecture.paas.component.common.database.PaaSEMDatabaseManager;
import eu.ascetic.asceticarchitecture.paas.component.common.model.EnergyInterpolator;
import eu.ascetic.asceticarchitecture.paas.component.energymodeller.datatype.EMSettings;
import eu.ascetic.asceticarchitecture.paas.component.energymodeller.interfaces.PaaSEnergyModeller;
import eu.ascetic.asceticarchitecture.paas.component.energymodeller.service.task.DataCollector;
import eu.ascetic.asceticarchitecture.paas.component.energymodeller.service.task.EnergyDataAggregatorService;
import eu.ascetic.asceticarchitecture.paas.component.energymodeller.service.task.EventDataAggregatorService;
import eu.ascetic.asceticarchitecture.paas.component.loadinjector.interfaces.LoadInjectorInterface;
import eu.ascetic.asceticarchitecture.paas.component.loadinjector.service.LoadInjectorService;

/**
 * @author davide sommacampagna
 *
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
	 * Constructor, load from the configuration file the settings
	 */
	
	public EnergyModellerSimple(String propertyFile) {
		this.propertyFile=propertyFile;
		LOGGER.info("EM Initialization ongoing");
		initializeProperty();
		initializeLoadInjector();
		initializeDataConnectors();
		LOGGER.info("EM Initialization complete");
	}
	


	@Override
	public double energyApplicationConsumption(String providerid,String applicationid, String deploymentid) {
		//TODO check from db if data has been collected in past or training occurred
		datacollector.handleConsumptionData(applicationid,deploymentid);
		double energy = energyService.getTotal(applicationid, deploymentid, "");
		LOGGER.info("Application consumed " + String.format( "%.2f", energy ));
		return energy;
	}



	@Override
	public double energyApplicationConsumption(String providerid,String applicationid, List<String> vmids, String eventid) {
		double total_energy=0;
		if (eventid==null){
			datacollector.handleConsumptionData(applicationid, vmids, "nodeployment");			
			for (String vm : vmids) {
				double energy = energyService.getTotal(applicationid, "nodeployment", vm, "");
				LOGGER.info("VM "+ vm + " consumed " + String.format( "%.2f", energy ));
				total_energy = total_energy +energy;
			}			
			LOGGER.info("Application consumed " + String.format( "%.2f", total_energy ));
		} else {
			datacollector.handleConsumptionData(applicationid, vmids, "nodeployment");
			datacollector.handleEventData(applicationid, "nodeployment", vmids, eventid);
			for (String vm : vmids) {
				double energy = energyService.getTotal(applicationid, "nodeployment", vm, "");
				double energyevent = energyService.getTotal(applicationid, "nodeployment", vm, eventid);
				LOGGER.info("VM "+ vm + " consumed " + String.format( "%.2f", energy ));
				LOGGER.info("VM "+ vm + " event consumed " + String.format( "%.2f", energyevent ));
				total_energy = total_energy +energy;
			}
		}
		return total_energy;
	}
	
	@Override
	public double energyEstimationForVM(String providerid, String applicationid, String vmid, String eventid) {
		//TODO integrate prediction model
		if (eventid==null){
			LOGGER.info("Energy estimation for " + applicationid );
			List<String> vm	= new Vector<String>();		
			vm.add(vmid);
			datacollector.handleConsumptionData(applicationid, vm, "deployment1");
			double energy =  energyService.getAverage(applicationid, "deployment1", vmid, "");
			LOGGER.info("VM consumed " +energy);
//			EnergyInterpolator ei = new EnergyInterpolator();
//			ei.providedata(dbmanager.getDataConsumptionDAOImpl());
//			LOGGER.info("Building estimator");
//			ei.buildmodel(applicationid, vmid);
//			LOGGER.info("Using estimator " );
//			 DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
//			   //get current date time with Date()
//			  Date date = new Date();
//			  LOGGER.info("Time now is " +  dateFormat.format(date));
//			LOGGER.info("VM will consume " +ei.estimate(date.getTime())+energy);
			
			return energy;
		} else {
			LOGGER.info("Energy estimation for " + applicationid + " and event " + eventid);
			//datacollector.handleEventData(applicationid,deploymentid,eventid);
			//datacollectdouble total_event = eventService.getTotal(applicationid, deploymentid, eventid);
			double total_energy = energyService.getTotal(applicationid, null , vmid, eventid);
			double total_event = eventService.getTotal(applicationid, null , vmid, eventid);
			LOGGER.info("Event total "+total_event+" consumed " +total_energy);
			if (total_event == 0) return 0;
			return total_energy/total_event;

		}
		
	}
	
	// Almost the same as the previous calls untill the prediciton will be implemented

	@Override
	public double energyEstimation(String providerid, String applicationid,	List<String> vmids, String eventid) {
		if (eventid==null){
			double energy = energyApplicationConsumption(providerid,applicationid,vmids,null);
			LOGGER.info("Application consumed " +String.format( "%.2f", energy ));
			return energy;
			 
		} else {
			LOGGER.info("Energy estimation for " + applicationid + " and event " + eventid);
			datacollector.handleEventData(applicationid,"nodeployment", vmids,eventid);
			datacollector.handleConsumptionData(applicationid,vmids,"nodeployment");
			double total_event = eventService.getTotal(applicationid, "nodeployment", eventid);
			double total_energy = energyService.getTotal(applicationid, "nodeployment", eventid);
			LOGGER.info("Event total "+total_event+" consumed " + String.format( "%.2f", total_energy ));
			if (total_event == 0) return 0;
			return total_energy/total_event;
		}
	}	
	
	@Override
	public double energyEstimation(String providerid, String applicationid,	String deploymentid,  String eventid) {
		//TODO integrate prediction model
		if (eventid==null){
			LOGGER.info("Energy estimation for " + applicationid );
			datacollector.handleConsumptionData(applicationid,deploymentid);
			double energy = energyService.getTotal(applicationid, deploymentid, "");
			LOGGER.info("Application consumed " +String.format( "%.2f", energy ));
			return energy;
			 
		} else {
			LOGGER.info("Energy estimation for " + applicationid + " and event " + eventid);
			datacollector.handleEventData(applicationid,deploymentid,eventid);
			datacollector.handleConsumptionData(applicationid,deploymentid);
			double total_event = eventService.getTotal(applicationid, deploymentid, eventid);
			double total_energy = energyService.getTotal(applicationid, deploymentid, eventid);
			LOGGER.info("Event total "+total_event+" consumed " + String.format( "%.2f", total_energy ));
			if (total_event == 0) return 0;
			return total_energy/total_event;
		}
		
	}

	

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
		datacollector.setIaasdatadriver(dbmanager.getIaasdatadao());
		datacollector.setDataconumption(dbmanager.getDataConsumptionDAOImpl());
		datacollector.setDataevent(dbmanager.getDataEventDAOImpl());
		energyService = new EnergyDataAggregatorService();
		energyService.setDataDAO(dbmanager.getDataConsumptionDAOImpl());
		eventService = new EventDataAggregatorService();
		eventService.setDaoEvent(dbmanager.getDataEventDAOImpl());
		LOGGER.debug("Configured ");
	}



	
}
