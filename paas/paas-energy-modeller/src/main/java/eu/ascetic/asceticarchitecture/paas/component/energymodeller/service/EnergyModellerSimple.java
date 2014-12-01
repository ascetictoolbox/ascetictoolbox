/**
* Copyright 2014 Hewlett-Packard Development Company, L.P.                                         
 */
package eu.ascetic.asceticarchitecture.paas.component.energymodeller.service;


import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.sql.Timestamp;
import java.util.Calendar;
import java.util.List;
import java.util.Properties;
import java.util.Vector;

import org.apache.log4j.Logger;

import eu.ascetic.asceticarchitecture.iaas.zabbixApi.datamodel.HistoryItem;
import eu.ascetic.asceticarchitecture.paas.component.common.database.PaaSEMDatabaseManager;
import eu.ascetic.asceticarchitecture.paas.component.common.database.table.DataEvent;
import eu.ascetic.asceticarchitecture.paas.component.common.model.interpolator.GenericValuesInterpolator;
import eu.ascetic.asceticarchitecture.paas.component.energymodeller.datatype.ApplicationSample;
import eu.ascetic.asceticarchitecture.paas.component.energymodeller.datatype.EMSettings;
import eu.ascetic.asceticarchitecture.paas.component.energymodeller.datatype.EventSample;
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
		this.loadEnergyData(applicationid, vmids);
		if (eventid==null){
			for (String vm : vmids) {
				double energy = energyEstimationForVM(providerid, applicationid, vm, eventid);
				LOGGER.info("This VM "+ vm + " consumed " + String.format( "%.2f", energy ));
				total_energy = total_energy +energy;
			}			
			LOGGER.info("Application consumed " + String.format( "%.2f", total_energy ));
		} else {
			this.loadEventData(applicationid, vmids,eventid);
			LOGGER.debug("## Loaded events data ##");
			for (String vm : vmids) {
				LOGGER.debug("Analyzing events on VM "+vm); 
				total_energy = total_energy + energyEstimationForEventInVM(providerid, applicationid, vm, eventid);
				
			}
		}
		return total_energy;
	}
	
	/**
	 * 
	 * ESTIMATE ENERGY  
	 * 
	 */
	@Deprecated
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

	@Override
	public double estimation(String providerid, String applicationid,List<String> vmids, String eventid, String unit) {
		if (unit.equals("energy")){
			double energy = energyApplicationConsumption(providerid,applicationid,vmids,eventid);
			if (eventid==null){
				LOGGER.info("Application consumed Wh " +String.format( "%.2f", energy ));
			} else {
				LOGGER.info("Event consumed Wh " +String.format( "%.2f", energy ));
			}
			return energy;
		} else {
			this.loadEnergyData(applicationid, vmids);
			
			if (eventid!=null){
				this.loadEventData(applicationid, vmids,eventid);
				double totalp = 0;
				double cevents = 0;
				for (String vm : vmids) {
					List<DataEvent> events = eventService.getEvents(applicationid, applicationid, vm, eventid);
					for (DataEvent de: events){
						cevents= cevents +1;
						double power  = energyService.getAvgPower(applicationid, vm, eventid, de.getBegintime(), de.getEndtime());
						if (power > 0){
							LOGGER.info("This event power :  "+power);
							
						}
						totalp = totalp + power;
						
						
					}
				}
					
					
				if (cevents<=0) return 0;
				
				return totalp/cevents;
			}else{
				double totalp = 0;
				double cevents = 0;
				Calendar c_tiemstamp = Calendar.getInstance();
				
				for (String vm : vmids) {
					cevents= cevents +1;
					double power  = energyService.getAvgPower(applicationid, vm, eventid, (c_tiemstamp.getTimeInMillis()-(86400*1000)), c_tiemstamp.getTimeInMillis());
					if (power > 0){
						LOGGER.info("This vm power since one day :  "+power);
						
					}
					totalp = totalp + power;
				}
				if (cevents<=0)return 0;
				return totalp;
			}
		}
	}
	
	
	/**
	 * 
	 * GET ENERGY WITHIN TIME 
	 * 
	 */
	
	@Override
	public double applicationConsumptionInInterval(String providerid,String applicationid, String vmids, String eventid,String unit , Timestamp start, Timestamp end) {
		if (unit.equals("energy")){
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
		}else {
			List<String> vmidsv = new Vector<String>();
			vmidsv.add(vmids);
			loadEnergyData(applicationid, vmidsv);
			if (eventid==null){
				double power  = energyService.getAvgPower(applicationid, vmids, eventid, start.getTime(), end.getTime());
				return power;
			}else{
				loadEventData(applicationid, vmidsv,eventid);
				double totalp = 0;
				double cevents = 0;
				List<DataEvent> events = eventService.getEvents(applicationid, applicationid, vmids, eventid);
				for (DataEvent de: events){
					cevents= cevents +1;
					double power  = energyService.getAvgPower(applicationid, vmids, eventid, de.getBegintime(), de.getEndtime());
					if (power > 0){
						LOGGER.info("This event power :  "+power);
						
					}
					totalp = totalp + power;
					
					
				}
				return totalp;
			}
	
		}
		

	}
	

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
	
	
	@Override
	public List<ApplicationSample> applicationData( String providerid, String applicationid,List<String> vmids, long samplingperiod,Timestamp start, Timestamp end){
		List<ApplicationSample> results = new Vector<ApplicationSample>();
		//LOGGER.info("SAmple period  CPU "+currenttime + " last " +endts);
		for(String vm : vmids){
			results.addAll(applicationVMData( applicationid, vm, samplingperiod,  start,  end));
		}
		return results;
	}
	
	private List<ApplicationSample> applicationVMData(String applicationid, String vmids, long samplingperiod, Timestamp start, Timestamp end) {
		List<HistoryItem> hcpu = this.datacollector.getSeriesHistoryForItemInterval("apptest","deptest","system.cpu.load[percpu,avg1]", datacollector.searchFullHostsname(vmids), start.getTime(), end.getTime());
		List<HistoryItem> hpower = this.datacollector.getSeriesHistoryForItemInterval("apptest","deptest","Power", datacollector.searchFullHostsname(vmids), start.getTime(), end.getTime());
		List<HistoryItem> hmenergy = buildenergyHistory (hpower);
		if ( (hcpu == null)|| (hpower==null)|| (hmenergy==null)){
			LOGGER.warn("Missing data");
			return null;
			
		}else {
			LOGGER.info("Ok I have data");
		}
		
		GenericValuesInterpolator cpu = getInterpolator(hcpu);
		GenericValuesInterpolator power = getInterpolator(hpower);
		GenericValuesInterpolator energy = getInterpolator(hmenergy);
		long startts = start.getTime()/1000;
		long endts = end.getTime()/1000;
		long currenttime = 0;
		int iteration = 0;
		currenttime = startts + (iteration * samplingperiod);
		List<ApplicationSample> result = new Vector<ApplicationSample>();
		//LOGGER.info("SAmple period  CPU "+currenttime + " last " +endts);
		while (currenttime < endts){
			
			//LOGGER.info("SAmple period  CPU "+currenttime);
			ApplicationSample cur_sample = new ApplicationSample();
			cur_sample.setTime(currenttime*1000);
			if ((currenttime>cpu.getStarttime())&&(currenttime<cpu.getLasttime())){
				cur_sample.setC_value(cpu.estimate(currenttime-cpu.getStarttime()));
			} else {
				cur_sample.setC_value(0);
			}
			if ((currenttime>power.getStarttime())&&(currenttime<power.getLasttime())){
				cur_sample.setP_value(power.estimate(currenttime-power.getStarttime()));
			} else {
				cur_sample.setP_value(0);
			}
			if ((currenttime>energy.getStarttime())&&(currenttime<energy.getLasttime())){
				cur_sample.setE_value(energy.estimate(currenttime-energy.getStarttime()));
			} else {
				cur_sample.setE_value(0);
			}
			LOGGER.info("SAmple CPU " + cur_sample.getC_value() + " Energy " + cur_sample.getE_value() + " Power " + cur_sample.getP_value() +" Time " +cur_sample.getP_value());
			result.add(cur_sample);
			iteration++;
			currenttime = startts + (iteration * samplingperiod);
			
		}
		return result;
	}
	
	private List<HistoryItem> buildenergyHistory (List<HistoryItem> power){
		if (power==null)return null;
		List<HistoryItem> results = new Vector<HistoryItem>();

		HistoryItem previous=null;
		for (int i=0;i<power.size();i++){

			HistoryItem item = power.get(i);
			HistoryItem energyitem = new HistoryItem();

			if (previous!=null){
				
				Double energy = integrate(new Double(previous.getValue()).doubleValue(),new Double(item.getValue()).doubleValue(),previous.getClock(),item.getClock());
				energyitem.setValue(energy.toString());
				energyitem.setClock(item.getClock());
				//LOGGER.info("val " + energy+ " clock " + item.getClock());
				results.add(energyitem);
	
				
			} else {
				Double energy = integrate(0,new Double(item.getValue()).doubleValue(),0,0);
				energyitem.setValue(energy.toString());
				energyitem.setClock(item.getClock());
				//LOGGER.info("val " + energy+ " clock " + item.getClock());
				results.add(energyitem);

		
			}
			
			
			previous = item;
		}
		return results;
		
	}
	
	private double integrate(double powera,double powerb, long timea,long timeb){
		return 	Math.abs((timeb-timea)*(powera+powerb)*0.5)/3600;
	}
	
	private GenericValuesInterpolator getInterpolator (List<HistoryItem> items){
		if (items.size()<=0)return null;
		GenericValuesInterpolator genInt = new GenericValuesInterpolator();
		
		double[] timeseries = new double[items.size()];
		double[] dataseries = new double[items.size()];
		HistoryItem item;
		genInt.setLasttime(items.get(0).getClock());
		genInt.setStarttime(items.get(items.size()-1).getClock());
		int count=0;
		for (int i=items.size()-1;i>=0;i--){
			item = items.get(i);
			timeseries[count]=(item.getClock()-genInt.getStarttime());
			dataseries[count]=Double.parseDouble(item.getValue());
			//LOGGER.info(" Sample: "+count+";"+timeseries[count]+";"+dataseries[count]);
			count++;
		}
		
		
		
		
		genInt.buildmodel(timeseries, dataseries); 
		return genInt;
		
	}
	
	

	
	/**
	 * 
	 * PRIVATE INTERNAL METHOD
	 * 
	 */
	

	

	private double energyEstimationForVM(String providerid, String applicationid, String vmid, String eventid) {
		List<String> vmids = new Vector<String>();
		vmids.add(vmid);
		this.loadEnergyData(applicationid, vmids);
		if (eventid==null){
			LOGGER.info("Energy estimation for " + applicationid );
			double energy =  energyService.getAverage(applicationid, "deployment1", vmid, eventid);
			
			return energy;
		} else {
			this.loadEventData(applicationid, vmids,eventid);
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
		if (eventnum==0)return 0;
		LOGGER.info("On the VM "+vmid+" The event " +eventid+"eventid"+"Consumed total Wh : "+ generalAvg + " over "+eventnum+"with an average Wh for event "+(generalAvg/eventnum));
		
		
		return (generalAvg/eventnum );		
	
	}
	
	private double energyEstimationForEventInInterval(String providerid,String applicationid, String vmid, String eventid,Timestamp start, Timestamp endtime) {

		
		double generalAvg = 0;
		int eventnum =0;
		List<DataEvent> events = eventService.getEventsInTime(applicationid, applicationid, vmid, eventid,start,endtime);
		for (DataEvent de: events){
			
			double energy = averageEventConsumption(de.getApplicationid(),de.getVmid(),de.getBegintime(),de.getEndtime());
			//if (energy > 0)LOGGER.info("This event : "+de.getBegintime() + " and " +de.getEndtime() + " energy "+energy);
			
			if (energy >0){
				eventnum++;
				generalAvg = generalAvg + energy;
			}
			
		}
	
		if (eventnum==0)return -1;
		LOGGER.info("Wh : "+ generalAvg + " over "+eventnum);
		
		
		return (generalAvg/eventnum );	
	
	}
	
	
	
	private List<EventSample> eventsSamplesInInterval(String providerid,String applicationid, String vmid, String eventid,Timestamp start, Timestamp endtime) {
		

		List<EventSample> eSamples = new Vector<EventSample>();
		
		EventSample es = new EventSample();
		List<DataEvent> events = eventService.getEventsInTime(applicationid, applicationid, vmid, eventid,start,endtime);
		for (DataEvent de: events){
			
			es = new EventSample();
			double power  = energyService.getAvgPower(applicationid, vmid, eventid, start.getTime(), endtime.getTime());
			double energy = averageEventConsumption(de.getApplicationid(),vmid,de.getBegintime(),de.getEndtime());
			if (energy > 0){
				LOGGER.info("This event :  "+energy);
				es.setTimestampBeging(de.getBegintime());
				es.setTimestampEnd(de.getEndtime());
				es.setVmid(vmid);
				es.setEvalue(energy);
				es.setPvalue(power);
				eSamples.add(es);
			}
			
		}
		
		LOGGER.info("Total events collected : "+ eSamples.size());
		return eSamples;
	
	}
	

	public double averageEventConsumption(String appid, String vmid,long start, long end){
		LOGGER.info("consumption of this event from "+start+" to "+end);
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





//	@Override
//	public boolean startModellingApplicationEnergy(String providerid,String applicationid, String deploymentid) {
//		LOGGER.info("Registering application " + applicationid + " with deployment " + deploymentid + " for monitoring");
//		dbmanager.getMonitoringData().createMonitoring(applicationid, deploymentid, "");
//		return true;
//	}
//
//	@Override
//	public boolean stopModellingApplicationEnergy(String providerid,String applicationid, String deploymentid) {
//		// update db
//		LOGGER.info("Application " + applicationid + " with deployment " + deploymentid + " removed from monitoring");
//		dbmanager.getMonitoringData().terminateMonitoring(applicationid, deploymentid);
//		return true;
//	}	
	
	/**
	 * 
	 * 
	 * TRAINING function to be fully implemented in Y2
	 * 
	 */
	
//	@Override
//	public boolean trainApplication(String providerid, String applicationid, String deploymentid, String eventid) {
//		LOGGER.info("Starting to train application " + applicationid + " for this deployment " + deploymentid);
//		LOGGER.info("Registering training");
//		dbmanager.getMonitoringData().createTraining(applicationid, deploymentid, eventid);
//		if (eventid==null){
//			LOGGER.info("Application training");
//			loadInjector.deployTrainingForApplication(applicationid, deploymentid);
//		} else {
//			LOGGER.info("Event training");
//			loadInjector.deployTrainingForApplicationEvent(applicationid, deploymentid, eventid);
//		}
//		LOGGER.info("Training terminated");
//		dbmanager.getMonitoringData().terminateTraining(applicationid, deploymentid);
//		return true;
//	}
		
}
