package eu.ascetic.asceticarchitecture.paas.component.energymodeller.service;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;
import java.util.Timer;

import org.apache.log4j.Logger;

import eu.ascetic.asceticarchitecture.paas.component.common.database.PaaSEMDatabaseManager;
import eu.ascetic.asceticarchitecture.paas.component.energymodeller.datatype.EMSettings;
import eu.ascetic.asceticarchitecture.paas.component.energymodeller.interfaces.PaaSEnergyModeller;
import eu.ascetic.asceticarchitecture.paas.component.energymodeller.service.task.DataCollector;
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
	private Timer datatimer;
	private DataCollector taskevents;
	private Timer eventimer;
	
	/**
	 * Constructor, load from the configuration file the settings
	 */
	
	public EnergyModellerSimple(String propertyFile) {
		this.propertyFile=propertyFile;
		initializeProperty();
		initializeLoadInjector();
		startTasks();
		LOGGER.info("EM Initialization complete");
	}
	
	@Override
	public boolean startModellingApplicationEnergy(String providerid,String applicationid, String deploymentid) {
		LOGGER.info("Registering application " + applicationid + " and deployment " + deploymentid + " for monitoring");
		dbmanager.getMonitoringData().createMonitoring(applicationid, deploymentid, "");
		return true;
	}

	@Override
	public boolean stopModellingApplicationEnergy(String providerid,String applicationid, String deploymentid) {
		// update db
		LOGGER.info("Application " + applicationid + " on deployment " + deploymentid + " removed from monitoring");
		dbmanager.getMonitoringData().terminateMonitoring(applicationid, deploymentid);
		// get iaas data
		taskevents.handleConsumptionData(applicationid,deploymentid);
		return true;
	}

	@Override
	public String energyApplicationConsumption(String providerid,String applicationid, String deploymentid) {
		// compute value from collected data
		// get from db if data has been collected in past or training occurred
		taskevents.handleConsumptionData(applicationid,deploymentid);
		return "120";
	}

	@Override
	public String energyEstimation(String providerid, String applicationid,	String deploymentid, String eventid) {
		if (eventid==null){
			LOGGER.info("Energy estimation for " + applicationid );
			taskevents.handleConsumptionData(applicationid,deploymentid);
		} else {
			LOGGER.info("Energy estimation for " + applicationid + " and event " + eventid);
			taskevents.handleEventData(applicationid,deploymentid,eventid);
			taskevents.handleConsumptionData(applicationid,deploymentid);
		}
		
		return "120";
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
		LOGGER.info("Initializing component");
		InputStream inputStream = getClass().getClassLoader().getResourceAsStream(propertyFile);
		if (inputStream == null) {
			LOGGER.warn("Properties not loaded, using default values");
		}
		Properties props = new Properties();
		try {
			props.load(inputStream);
			emsettings = new EMSettings(props);
			LOGGER.info("Properties loaded");
			dbmanager = new PaaSEMDatabaseManager();
			dbmanager.setup(emsettings);
			LOGGER.info("Database Configured");
			
		} catch (IOException e) {
			LOGGER.error("Properties not loaded due to a failure");
			e.printStackTrace();
		}
	}
	
	private void initializeLoadInjector(){
		loadInjector = new LoadInjectorService();
		loadInjector.configureLoadInjector(emsettings.getServerPath(), emsettings.getServerurl(), emsettings.getPropertyFile(), emsettings.getJmxFilePath());
		LOGGER.info("Configured load injector");
	}
	
	private void startTasks(){
		LOGGER.info("Creating the tasks that collect data ");
		taskevents = new DataCollector();
		taskevents.setAMPath(emsettings.getAppmonitor());
		taskevents.setup();
		taskevents.setIaasdatadriver(dbmanager.getIaasdatadao());
		LOGGER.info("Starting the tasks that collect data ");
		eventimer = new Timer();
		eventimer.scheduleAtFixedRate(taskevents, 1000, Long.parseLong(emsettings.getEventsloadinterval()));
		LOGGER.info("Task started ");
	}
	
//	private void stopTasks(){
//		LOGGER.info("Closing the tasks that collect data ");
//		datatimer.cancel();
//		eventimer.cancel();
//		LOGGER.info("Closed the tasks that collect data ");
//	}
	
	
}
