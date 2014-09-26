package eu.ascetic.asceticarchitecture.paas.component.energymodeller.service;


import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.sql.Timestamp;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Properties;
import java.util.Vector;

import org.apache.log4j.Logger;

import eu.ascetic.asceticarchitecture.paas.component.common.database.PaaSEMDatabaseManager;
import eu.ascetic.asceticarchitecture.paas.component.common.model.DataEvent;
import eu.ascetic.asceticarchitecture.paas.component.common.model.EnergyInterpolator;
import eu.ascetic.asceticarchitecture.paas.component.common.model.TimeEnergyInterpolator;
import eu.ascetic.asceticarchitecture.paas.component.common.model.WorkLoadEnergyInterpolator;
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


	@Override
	public double energyApplicationConsumption(String providerid,String applicationid, List<String> vmids, String eventid) {
		double total_energy=0;
		// Ensure vm data is loaded in to PaaS Database
		this.loadEnergyData(applicationid, vmids);
		if (eventid==null){
			// For each vm estimate the energy
			for (String vm : vmids) {
				double energy = energyEstimationForVM(providerid, applicationid, vm, null);
				LOGGER.info("This VM "+ vm + " consumed " + String.format( "%.2f", energy ));
				total_energy = total_energy +energy;
			}			
			LOGGER.info("Application consumed " + String.format( "%.2f", total_energy ));
		} else {
			// ensure also event data is loaded
			this.loadEventData(applicationid, vmids,eventid);
			for (String vm : vmids) {
				LOGGER.info("############################Loading events data "); 
				//double energy = energyService.getTotal(applicationid, "nodeployment", vm, "");
				//double energyevent = energyService.getTotal(applicationid, "nodeployment", vm, eventid);
				
				// build estimator of consumption based on time
				this.loadEventData(applicationid, vmids, eventid);
				
				TimeEnergyInterpolator timeestimator = new TimeEnergyInterpolator();
				timeestimator.providedata(dbmanager.getDataConsumptionDAOImpl());
				timeestimator.buildmodel(applicationid, vm);
				
				double generalAvg = 0;
				List<DataEvent> events = eventService.getEvents(applicationid, applicationid, vm, eventid);
				for (DataEvent de: events){
					
					LOGGER.info(" got event "+ de.getBegintime() + " untill" +de.getEndtime() + " on "+de.getDeploymentid()+ " vm "+de.getVmid());
					
					double begin = timeestimator.estimate( de.getBegintime().getTime()/1000);
					double end = timeestimator.estimate( de.getEndtime().getTime()/1000);
					double avg = (end + begin) /2;
					
					LOGGER.info("This event : "+ begin + " and " +end + " avg "+avg);
					
					generalAvg = generalAvg + avg;
					
				}
				if (events.size()==0)return -1;
				LOGGER.info("Total avg : "+ generalAvg + " over "+events.size());
				LOGGER.info("Consumption is : "+ generalAvg /events.size());
				return (generalAvg /events.size());
				// estimator has been build
				
				
				
				
				
				//LOGGER.info("VM "+ vm + " consumed " + String.format( "%.2f", energy ));
				//LOGGER.info("VM "+ vm + " event consumed " + String.format( "%.2f", energyevent ));
				//total_energy = total_energy +energyevent;
			}
			
			
		}
		return total_energy;
	}
	
	@Override
	public double energyEstimationForVM(String providerid, String applicationid, String vmid, String eventid) {
		List<String> vmids = new Vector();
		vmids.add(vmid);
		this.loadEnergyData(applicationid, vmids);
		if (eventid==null){
			LOGGER.info("Energy estimation for " + applicationid );
			List<String> vm	= new Vector<String>();		
			vm.add(vmid);
			//datacollector.handleConsumptionData(applicationid, vm, "deployment1");
			double energy =  energyService.getAverage(applicationid, "deployment1", vmid, "");

			
			
//			LOGGER.info("VM consumed " +energy);
//			WorkLoadEnergyInterpolator wei = new WorkLoadEnergyInterpolator();
//			wei.providedata(dbmanager.getDataConsumptionDAOImpl());
//			wei.buildmodel(applicationid, vmid);
//			LOGGER.info("VM will consume at 10% " +wei.estimate(0.10));
//			LOGGER.info("VM will consume at 20% " +wei.estimate(0.20));
//			LOGGER.info("VM will consume at 30% " +wei.estimate(0.30));
//			LOGGER.info("VM will consume at 40% " +wei.estimate(0.40));
//			LOGGER.info("VM will consume at 50% " +wei.estimate(0.50));
//			LOGGER.info("VM will consume at 60% " +wei.estimate(0.60));
//			LOGGER.info("VM will consume at 70% " +wei.estimate(0.70));
//			LOGGER.info("VM will consume at 80% " +wei.estimate(0.80));
//			LOGGER.info("VM will consume at 90% " +wei.estimate(0.90));
//			LOGGER.info("VM will consume at 100% " +wei.estimate(1.0));
	
			EnergyInterpolator ei = new EnergyInterpolator();
			ei.providedata(dbmanager.getDataConsumptionDAOImpl());
			LOGGER.info("Building estimator");
			ei.buildmodel(applicationid, vmid);
			
			LOGGER.info("Using estimator " );
			DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

			  Date date;
			try {
				date = dateFormat.parse("2014-09-02 20:12:00");
				//LOGGER.info("Time  is " +  dateFormat.format(date));
				//LOGGER.info("Time  is " +  date.getTime());
				//LOGGER.info("VM consumed " +ei.estimate(date.getTime()/1000));
				TimeEnergyInterpolator tei = new TimeEnergyInterpolator();
				tei.providedata(dbmanager.getDataConsumptionDAOImpl());
				tei.buildmodel(applicationid, vmid);
				energy = tei.estimate(date.getTime()/1000);

			} catch (ParseException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

			
			return energy;
		} else {
			LOGGER.info("Energy estimation for " + applicationid + " and event " + eventid);
			//datacollector.handleEventData(applicationid,deploymentid,eventid);
			//double total_event = eventService.getTotal(applicationid, "deployment1", eventid);
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
		this.loadEnergyData(applicationid, vmids);
		if (eventid==null){
			double energy = energyApplicationConsumption(providerid,applicationid,vmids,null);
			LOGGER.info("Application consumed " +String.format( "%.2f", energy ));
			return energy;
			 
		} else {
			LOGGER.info("Energy estimation for " + applicationid + " and event " + eventid);
			this.loadEventData(applicationid,vmids,eventid);
			double total_event = eventService.getTotal(applicationid, "nodeployment", eventid);
			double total_energy = energyService.getTotal(applicationid, "nodeployment", eventid);
			LOGGER.info("Event total "+total_event+" consumed " + String.format( "%.2f", total_energy ));
			if (total_event == 0) return 0;
			return total_energy/total_event;
		}
	}	
	
	/**
	 * 
	 * 
	 *  Estimation method for future usage
	 * 
	 * 
	 */

	@Override
	public double energyConsumptionAtWorkload(String providerid,String applicationid, List<String> vmids, String eventid,double workload) {
		this.loadEnergyData(applicationid, vmids);
		double total=0;
		for (String vm : vmids) {
			WorkLoadEnergyInterpolator wei = new WorkLoadEnergyInterpolator();
			wei.providedata(dbmanager.getDataConsumptionDAOImpl());
			wei.buildmodel(applicationid, vm);
			total = total+wei.estimate(workload);
			LOGGER.info("VM will consume at workload " +wei.estimate(workload));
			
		}
		
		LOGGER.info("VM will consume at workload " +total);
		
		return total;
	}

	@Override
	public double energyConsumptionAtTime(String providerid,String applicationid, List<String> vmids, String eventid,Timestamp time) {
		this.loadEnergyData(applicationid, vmids);
		double total=0;
		DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		Date date;
		for (String vm : vmids) {
			EnergyInterpolator ei = new EnergyInterpolator();
			ei.providedata(dbmanager.getDataConsumptionDAOImpl());
			LOGGER.info("Building estimator for "+vm);
			ei.buildmodel(applicationid, vm);
			
			LOGGER.info("Using estimator " );
			

			LOGGER.info("Time  is " +  dateFormat.format(time));
			LOGGER.info("VM consumed " +ei.estimate(time.getTime()/1000));

		}

		return total;
	}	
	
	@Override
	public double energyEstimationForTime(String providerid,String applicationid, List<String> vmids, String eventid,Timestamp time) {
		this.loadEnergyData(applicationid, vmids);
		double total=0;
		DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		Date date;
		for (String vm : vmids) {
			
			
			TimeEnergyInterpolator te = new TimeEnergyInterpolator();
			te.providedata(dbmanager.getDataConsumptionDAOImpl());
			LOGGER.info("Building estimator for "+vm);
			te.buildmodel(applicationid, vm);
			
			LOGGER.info("Using estimator " );
			
			LOGGER.info("Time  is " +  dateFormat.format(time));
			double cons = te.estimate(time.getTime()/1000);
			total =  total + cons;
			LOGGER.info("VM consumed " +cons);

		}

		return total;
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

	
	@Override
	public double energyApplicationConsumptionTimeInterval(String providerid,String applicationid, List<String> vmids, String eventid,Timestamp start, Timestamp end) {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public double[] energyApplicationConsumptionData(String providerid,	String applicationid, List<String> vmids, String eventid,Timestamp start, Timestamp end) {
		// TODO Auto-generated method stub
		return null;
	}

	
	
	@Deprecated
	@Override
	public double energyApplicationConsumption(String providerid,String applicationid, String deploymentid) {
		//TODO check from db if data has been collected in past or training occurred
		datacollector.handleConsumptionData(applicationid,deploymentid);
		double energy = energyService.getTotal(applicationid, deploymentid, "");
		LOGGER.info("Application consumed " + String.format( "%.2f", energy ));
		return energy;
	}
	
	@Deprecated
	@Override
	public double energyEstimation(String providerid, String applicationid,	String deploymentid,  String eventid) {
		//TODO integrate prediction model
		this.loadEnergyData(applicationid,deploymentid);
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
	
	private void loadEventDataTime(String appid,String deploymentid, List<String> vms, String eventid,Timestamp start, Timestamp end){
		datacollector.handleEventDataInterval(appid, deploymentid, vms, eventid, start, end);
	}
	
	private void loadEventData(String appid,List<String> vms,String eventid){
		datacollector.handleEventData(appid, "deployment1", vms, eventid);
	}
	
	private void loadEventDataTime(String appid,List<String> vms,Timestamp start, Timestamp end){
		datacollector.handleConsumptionDataInterval(appid, vms, "deployment1", start, end);
	}
	
	private void loadEnergyData(String appid, List<String> vm){
		datacollector.handleConsumptionData(appid, vm, "deployment1");
	}

	@Deprecated
	private void loadEnergyData(String appid, String deployment){
		datacollector.handleConsumptionData(appid,  deployment);
	}





	
}
