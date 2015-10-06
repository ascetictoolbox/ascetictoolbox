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
import java.util.TimeZone;
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
import eu.ascetic.asceticarchitecture.paas.component.energymodeller.internal.common.dataservice.EventDataAggregatorService;
import eu.ascetic.asceticarchitecture.paas.component.energymodeller.internal.common.dataservice.MonitoringDataService;
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
	private MonitoringDataService monitoringDataService;
	private DataConsumptionHandler dataCollectorHandler;
	private PredictorInterface predictor;
	private ApplicationRegistry appRegistry;
	private EnergyModellerQueueServiceManager queueManager;
	private AmqpClient paasQueueclient;
	private AmqpClient iaasQueueclient;
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
	public double measure(String providerid, String applicationid, String deploymentid,List<String> vmids, String eventid, Unit unit,Timestamp start, Timestamp end) {
		
		
		if((start==null)&&(end==null)){
			LOGGER.info("Measuring from all available data since both timestamps are not specified");
			if (unit==Unit.ENERGY){
				LOGGER.info("Measuring all energy consumption");
				double result = aggregateVMenergyConsumption(providerid,applicationid,deploymentid, vmids,eventid,null,null);
				if (result>=0)sendToQueue(monitoringTopic, providerid, applicationid, deploymentid, vmids, eventid, GenericEnergyMessage.Unit.WATTHOUR, null, result);
				return result;
			} else {
				LOGGER.info("Measuring average instant power"); 
				double result = averagePower(providerid,applicationid, deploymentid, vmids,  eventid,null,null);
				if (result>=0)sendToQueue(monitoringTopic, providerid, applicationid, deploymentid, vmids, eventid, GenericEnergyMessage.Unit.WATT, null, result);
				LOGGER.info("Sending to queue power "+result); 
				return result;
			}
		}else {
			LOGGER.info("Checking the timestamps"); 
			LOGGER.info(start); 
			LOGGER.info(end); 
			if (start==null){
				Calendar calendar = Calendar.getInstance();
				long now = calendar.getTime().getTime();
				now = now - (86400000);
				if (now>end.getTime()){
					now = end.getTime()-(86400000);
				}
				Timestamp currentTimestamp = new Timestamp(now);
				start = currentTimestamp;
				LOGGER.info("Going to get data from the last 24h :  "+now);
			}
			if (end==null){
				Calendar calendar = Calendar.getInstance();
				Timestamp currentTimestamp = new Timestamp(calendar.getTime().getTime());
				end = currentTimestamp;
				LOGGER.info("Going to get data untill now :  "+end.getTime());
			}
			LOGGER.info("Checking the timestamps"); 
			LOGGER.info(start); 
			LOGGER.info(end);
			if (unit==Unit.ENERGY){
				LOGGER.info("Measuring energy consumption");
				double result =  aggregateVMenergyConsumption( providerid, applicationid, deploymentid, vmids,  eventid,  start,  end);
				LOGGER.info("Sending to queue");
				if (result>=0)sendToQueue(monitoringTopic, providerid, applicationid, deploymentid, vmids, eventid, GenericEnergyMessage.Unit.WATTHOUR, null, result);
				return result;
			} else {
				LOGGER.info("Measuring average instant power"); 
				double result =  averagePower(providerid,applicationid, deploymentid, vmids,  eventid,start,end);
				if (result>=0)sendToQueue(monitoringTopic, providerid, applicationid, deploymentid, vmids, eventid, GenericEnergyMessage.Unit.WATT, null, result);
				return result;
			}
			
		}

	}

	/**
	 * 
	 * 
	 * ESTIMATE: estimate a future value from available samples, 
	 * 			Unit tells if the method should compute future value of instant power or the future accumulated energy consumption
	 * 
	 */	
	
	@Override
	public double estimate(String providerid, String applicationid,  String deploymentid,	List<String> vmids, String eventid, Unit unit, long window) {
		LOGGER.info("Forecasting requested"); 
		double currentval = 0;
		if (eventid==null){
			// instant power estimation
			double total_power_estim=0;
			double total_energy_estim=0;
			double count_vm=0;
			for(String vm : vmids){
				count_vm++;
				LOGGER.info("Forecasting this vm power "+vm);
				currentval = predictor.estimate(providerid,applicationid, deploymentid, vm, eventid, unit, window);	
				List<String> thisvm = new Vector<String>();
				thisvm.add(vm);
				LOGGER.info("is  "+currentval);
				total_power_estim = total_power_estim + currentval;
				if (unit==Unit.ENERGY){
					double current_consumption = energyService.getEnergyFromVM(applicationid, deploymentid, vm, eventid);
					LOGGER.info("############ Current vm consumption "+current_consumption);
					DataConsumption dc = energyService.getLastPowerSampleFromVM(applicationid, deploymentid, vm);
					if (dc!=null){
						LOGGER.info("######## ENERGY ESTIMATION SUMMARY");
						
						LOGGER.info("############ Last vm power "+dc.getVmpower()+" at time "+dc.getTime());
						LOGGER.info("############ Estimated vm power "+currentval+" at time ");
						double predicted_consumption = (0.5)*((currentval + dc.getVmpower()) / (window/3600));
						LOGGER.info("############ Estimated increased consumption for this vm "+predicted_consumption);
						total_energy_estim = total_energy_estim + predicted_consumption + current_consumption;
						LOGGER.info("############ New  consumption for this vm "+predicted_consumption);
						double partial = predicted_consumption + current_consumption;
						LOGGER.info("############ Added to the previously counsumption on this vm "+partial);
						if (predicted_consumption>0)sendToQueue(predictionTopic, providerid, applicationid, deploymentid, thisvm, eventid, GenericEnergyMessage.Unit.WATTHOUR, null, partial);
					}
					
				}else{
					LOGGER.info("############ Predicted power "+currentval );
					if (currentval>0)sendToQueue(predictionTopic, providerid, applicationid, deploymentid, thisvm, eventid, GenericEnergyMessage.Unit.WATT, null, currentval);
				}
			}	
			if (unit==Unit.ENERGY){
				LOGGER.info("############ Forecast of consumption for the whole app "+total_energy_estim);
				if (total_energy_estim>0)sendToQueue(predictionTopic, providerid, applicationid, deploymentid, vmids, eventid, GenericEnergyMessage.Unit.WATTHOUR, null, total_energy_estim);
				return total_energy_estim;
			} else if (count_vm>0) {

				LOGGER.info("############ Forecast of power for the whole app "+total_power_estim);
				if (total_power_estim>0)sendToQueue(predictionTopic, providerid, applicationid, deploymentid, vmids, eventid, GenericEnergyMessage.Unit.WATT, null, total_power_estim);
				return total_power_estim;
			}
			
			
				
		} else {
			// average event power
			currentval = averagePower(providerid,applicationid, deploymentid, vmids,  eventid,null,null);
			LOGGER.info("############ EVENT Forecasted instant power, now set to queue " + currentval); 
			if (currentval>0)sendToQueue(predictionTopic, providerid, applicationid, deploymentid, vmids, eventid, GenericEnergyMessage.Unit.WATT, null, currentval);
			long duration = averageDuration(providerid,applicationid, deploymentid, vmids,  eventid);
			
			LOGGER.info("############ EVENT Forecasting duration (sec) " + duration); 
			if (unit==Unit.ENERGY){
				
				if (duration>0){
					double douration_in_hour = duration/3600;
					currentval = currentval*douration_in_hour;
					LOGGER.info("############ EVENT Forecasted consumption " + currentval); 
				}else{
					LOGGER.warn("############ EVENT Something wrong with this values, check calculation");
				}
				if (currentval>0)sendToQueue(predictionTopic, providerid, applicationid, deploymentid, vmids, eventid, GenericEnergyMessage.Unit.WATTHOUR, null, currentval);
				return currentval;
			}
		}
		//predictor
		
		
		return currentval;
	}

	/**
	 * 
	 * GET ENERGY CONSUMED BY AN APPLICATION IF EVENT ID IS NULL  , OR THE TOTAL ENERGY CONSUMED BY THE SAME EVENT TYPE IF ITs ID IS PROVIDED, AGGREGATE FOR ALL VMS SPECCIFIED
	 * 
	 */
	private double aggregateVMenergyConsumption(String providerid,String applicationid, String deploymentid, List<String> vmids, String eventid,Timestamp start,Timestamp end) {
			
			double total_energy=0;
			if (eventid==null){
				LOGGER.debug("Analyzing application");
				for (String vm : vmids) {
					double energy = energyForVM(providerid, applicationid, deploymentid, vm, eventid,start,end);
					LOGGER.info("This VM "+ vm + " consumed " + String.format( "%.2f", energy ));
					total_energy = total_energy +energy;
				}			
				LOGGER.info("Application consumed " + String.format( "%.2f", total_energy ));
			} else {
				LOGGER.debug("Analyzing event");
				for (String vm : vmids) {
					LOGGER.debug("Analyzing events on VM "+vm); 
					total_energy = total_energy + energyForVM(providerid, applicationid, deploymentid, vm, eventid,start,end);
				}
				LOGGER.info("Event energy Wh " +String.format( "%.2f", total_energy ));
			}
			return total_energy;
			
	}
	
	/**
	 * 
	 * 
	 * Returns an EventSample for each event in the time interval, providing average instant power and energy consumption of each
	 * 
	 */	
	
	@Override
	public List<EventSample> eventsData(String providerid,String applicationid,  String deploymentid, List<String> vmids, String eventid,Timestamp start, Timestamp end) {
		List<EventSample> results = new Vector<EventSample>();
		for (String vm : vmids){
			results.addAll(eventsSamplesInInterval( providerid, applicationid, deploymentid, vm,  eventid,start, end));
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
	public List<ApplicationSample> applicationData( String providerid, String applicationid, String deploymentid, List<String> vmids, long samplingperiod,Timestamp start, Timestamp end){
		List<DataConsumption> resultssamples = new Vector<DataConsumption>();
		List<ApplicationSample> results = new Vector<ApplicationSample>();
		for(String vm : vmids){
			List<DataConsumption> estim_sample = energyService.sampleMeasurements(applicationid, deploymentid, vm, start.getTime(),end.getTime(),samplingperiod);
			if (estim_sample!=null)resultssamples.addAll(estim_sample);
		}
		for(DataConsumption dc : resultssamples){
			ApplicationSample as = new ApplicationSample();
			as.setAppid(dc.getApplicationid());
			as.setVmid(dc.getVmid());
			as.setP_value(dc.getVmpower());
			as.setTime(dc.getTime());
			as.setC_value(dc.getVmcpu());
			as.setE_value(dc.getVmpower());
			results.add(as);
		}
		return results;
	}
	
	/**
	 * PRIVATE METHOD SECTION
	 */	
	
	/**
	 * 
	 * 
	 * Returns an Application Sample for each sample in the give interval, sampled at the samplingperiod time
	 * 
	 */	
	
	private long averageDuration(String providerid, String applicationid, String deploymentid, List<String> vmids, String eventid){
		long duration=0;
		long vmwithevent=0;
		
		for (String vm : vmids) {
			LOGGER.info("############ Measuring event average instant power (W) for vm "+vm); 
			// TODO workaround 
			String translated = energyService.translatePaaSFromIaasID(deploymentid, vm);
			List<DataEvent> events = eventService.getEvents(applicationid, deploymentid, translated, eventid,null,null);
			if (events.size()>0){
				vmwithevent++;
			}
			long vmavg =0;
			int totalevent=0;
			for (DataEvent de: events){
				long delta = (de.getEndtime() - de.getBegintime()); 
				totalevent++;
				LOGGER.info("############ Event elapsed time "+delta/1000); 
				vmavg = vmavg +delta/1000;
			}
			
			if (totalevent>0){
				vmavg = vmavg/totalevent;
				duration = duration+vmavg;
			} 
			LOGGER.info("############ This VM " + vm + " has events  " + totalevent + " avg duration " + vmavg  );
			List<String> vms = new Vector<String>();
			vms.add(vm);
			LOGGER.info("############ Sending to queue event statistics for this VM ");
			if (vmavg>=0)sendToQueue(predictionTopic, providerid, applicationid, deploymentid, vms, eventid, GenericEnergyMessage.Unit.SEC, null, vmavg);
			if (totalevent>=0)sendToQueue(predictionTopic, providerid, applicationid, deploymentid, vms, eventid, GenericEnergyMessage.Unit.COUNT, null, totalevent);
			
		}
		if (vmwithevent>0){
			duration = duration/vmwithevent;
		}
		LOGGER.info("This event has been reported by vms  " + vmwithevent + " with an avg " + duration  );
		LOGGER.info("Sending to queue event statistics for the whole application ");
		if (vmwithevent>=0)sendToQueue(predictionTopic, providerid, applicationid, deploymentid, vmids, eventid, GenericEnergyMessage.Unit.APP_DURATION, null, duration);
		if (vmwithevent>=0)sendToQueue(predictionTopic, providerid, applicationid, deploymentid, vmids, eventid, GenericEnergyMessage.Unit.APP_COUNT, null, vmwithevent);
		
		return duration;
	}
	
	/**
	 * 
	 * GET THE AVERAGE INSTANT POWER MEASURED FOR AN APPLICATION IF EVENT ID IS NULL, OR EVENT IF IT IS PROVIDED
	 * 
	 */	
	
	private double averagePower(String providerid, String applicationid, String deploymentid, List<String> vmids, String eventid, Timestamp start, Timestamp end) {
		
		if((start==null)&&(end==null)){
			if (eventid!=null){
				LOGGER.info("Measuring event average instant power (W)"); 
				double totalPower = 0;
				double countEvents = 0;
				double countVM = 0;
				boolean isGlobaEvent = false;
				for (String vm : vmids) {
					LOGGER.info("Measuring event average instant power (W) for vm "+vm); 
					// TODO workaround 
					String translated = energyService.translatePaaSFromIaasID(deploymentid, vm);
					List<DataEvent> events = eventService.getEvents(applicationid, deploymentid, translated, eventid,null,null);
					LOGGER.info("Got events: "+events.size());
					double accumulatedpowerpervm = 0;
					for (DataEvent de: events){
						
						LOGGER.info("Event start "+de.getBegintime()+" and terminates  "+ de.getEndtime()); 
						countEvents++;
						if (de.getData()!=null){
							LOGGER.info("Event global"+de.getData());
							isGlobaEvent = true;
						} else {
							LOGGER.info("Event local");
							
						}
						double power  = energyService.getMeasureInIntervalFromVM(Unit.POWER, applicationid, deploymentid, vm, de.getBegintime(), de.getEndtime());
						LOGGER.info("##### This event power :  "+power);
						if (power>0)countEvents++;
						accumulatedpowerpervm = accumulatedpowerpervm + power;
					}
	
					LOGGER.info("##### average for this vm power was :  "+totalPower);
					totalPower = totalPower + accumulatedpowerpervm;
					if (accumulatedpowerpervm>0){
						LOGGER.info("##### This has meaningfull event data and now is:  "+totalPower);
						LOGGER.info("##### power:  "+accumulatedpowerpervm);
						countVM++;
					} else {
						LOGGER.info("##### This has no event data or power for this event:  "+accumulatedpowerpervm);
					}
					
					//if (events.size()>0)averageDuration(providerid,applicationid, deploymentid, vmids,  eventid);
				}
				LOGGER.info("##### This VM counts? :  "+totalPower + " now the count is "+countVM);
				LOGGER.info("##### This event is global ? " + isGlobaEvent);
				
				if (countEvents<=0) return 0;
				if (isGlobaEvent) return totalPower;
				return totalPower/countEvents;
			}else{
				LOGGER.info("Measuring application average instant power (W) in the last 24 hours"); 
				double totalPower = 0;
				double countEvents = 0;
				Calendar c_tiemstamp = Calendar.getInstance();
				
				for (String vm : vmids) {
					countEvents++;
					double power  = energyService.getMeasureInIntervalFromVM(Unit.POWER, applicationid, deploymentid, vm, (c_tiemstamp.getTimeInMillis()-(86400*1000)), c_tiemstamp.getTimeInMillis());
					LOGGER.info("This vm power since one day :  "+power);
					totalPower = totalPower + power;
				}
				if (countEvents<=0)return 0;
				return totalPower;
			}
		}else{
			double tot_power=0;
			double countevents=0;
			if (end==null){
				Calendar calendar = Calendar.getInstance();
				Timestamp currentTimestamp = new Timestamp(calendar.getTime().getTime());
				end = currentTimestamp;
				LOGGER.info("Going to get data untill now :  "+end.getTime());
			}
			if (start==null){
				Calendar calendar = Calendar.getInstance();
				//TimeZone tz = TimeZone.getTimeZone("GMT");
				calendar.setTimeZone(TimeZone.getTimeZone("GMT"));
				long now = calendar.getTime().getTime();
				now = now - (86400000);
				start = new Timestamp(now);
				LOGGER.info("Going to get data from the last 24h :  "+start);
			}
			
			if (eventid==null){
				for (String vm : vmids) {
					LOGGER.info("Going to use start  :  "+start.getTime());
					LOGGER.info("Going to use end :  "+end.getTime());
					double power  = energyService.getMeasureInIntervalFromVM(Unit.POWER, applicationid, deploymentid, vm,  start.getTime(), end.getTime());
					LOGGER.info("Avg power for VM "+ vm + " was " + String.format( "%.2f", power ));
					if (power>0)tot_power = tot_power + power;
				}
				return tot_power;
			}else{
				for (String vm : vmids) {
					// TODO workaround
					String translated = energyService.translatePaaSFromIaasID(deploymentid, vm);
					List<DataEvent> events = eventService.getEvents( applicationid, deploymentid, translated, eventid,start,end);
					for (DataEvent de: events){
						
						double power  = energyService.getMeasureInIntervalFromVM(Unit.POWER, applicationid, deploymentid, vm,  de.getBegintime(), de.getEndtime());
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
	 * PRIVATE INTERNAL METHOD to get energy consumption per vm
	 * 
	 */

	private double energyForVM(String providerid, String applicationid, String deploymentid, String vmid, String eventid, Timestamp start, Timestamp end) {
		if((start==null)&&(end==null)){
			if (eventid==null){
				LOGGER.info("Application energy estimation for " + deploymentid );
				return energyService.getEnergyFromVM(applicationid, deploymentid, vmid, eventid);
			} else {
				LOGGER.info("Energy estimation for " + applicationid + " and its event " + eventid);
				int eventcount =0;
				double energyAverage = 0;
				String translated = energyService.translatePaaSFromIaasID(deploymentid, vmid);
				List<DataEvent> events = eventService.getEvents(applicationid, deploymentid, translated, eventid, null, null);
				LOGGER.info("events "+events.size());
				for (DataEvent de: events){
					double energy = energyService.getMeasureInIntervalFromVM(Unit.ENERGY, de.getApplicationid(), de.getDeploymentid(),vmid,de.getBegintime(),de.getEndtime());
					LOGGER.info("This event : "+ de.getBegintime() + " and " +de.getEndtime() + " energy "+energy+de.getVmid());
					if (energy >0){
						eventcount++;
						energyAverage = energyAverage + energy;
					}
				}
				if (eventcount==0)return 0;
				LOGGER.info("Wh : "+ energyAverage + " over "+eventcount+" events");
				
				return (energyAverage/eventcount );
			}
		} else {
			if (eventid!=null){
				double energyAverage = 0;
				int eventcount =0;
				String translated = energyService.translatePaaSFromIaasID(deploymentid, vmid);
				List<DataEvent> events = eventService.getEvents( applicationid, deploymentid , translated, eventid,start,end);
				for (DataEvent de: events){
					double energy = energyService.getMeasureInIntervalFromVM(Unit.ENERGY, de.getApplicationid(),de.getVmid() , de.getVmid(),de.getBegintime(),de.getEndtime());
					if (energy >0){
						eventcount++;
						energyAverage = energyAverage + energy;
					}
				}
				if (eventcount==0)return 0;
				LOGGER.info("Wh : "+ energyAverage + " over "+eventcount+" events in the specified interval");
				return (energyAverage/eventcount );			
				
			}else {
				return energyService.getMeasureInIntervalFromVM(Unit.ENERGY, applicationid, deploymentid, vmid, start.getTime(), end.getTime());
			}
				
		}	
	}
		
	/**
	 * 
	 * Extracts events from the database of the EM
	 *  
	 */
	
	private List<EventSample> eventsSamplesInInterval(String providerid,String applicationid, String deploymentid, String vmid, String eventid,Timestamp start, Timestamp endtime) {
		
		List<EventSample> eSamples = new Vector<EventSample>();
		
		EventSample es = new EventSample();
		String translated = energyService.translatePaaSFromIaasID(deploymentid, vmid);
		List<DataEvent> events = eventService.getEvents(applicationid, deploymentid, translated, eventid,start,endtime);
		for (DataEvent de: events){
			
			es = new EventSample();
			double power  = energyService.getMeasureInIntervalFromVM(Unit.ENERGY,applicationid, deploymentid, vmid, start.getTime(), endtime.getTime());
			double energy = energyService.getMeasureInIntervalFromVM(Unit.POWER, de.getApplicationid(), de.getDeploymentid(), vmid,de.getBegintime(),de.getEndtime());
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
	
	private void sendToQueue(String queue,String providerid,String applicationid, String deploymentid, List<String> vms, String eventid, GenericEnergyMessage.Unit unit, String referenceTime,double value){
		LOGGER.info("EM queue enabled is "+queueEnabled);
		if (queueEnabled){
			LOGGER.info("EM publish message of energy");
			queueManager.sendToQueue(queue, providerid, applicationid, deploymentid, vms, eventid, unit, referenceTime, value);
			LOGGER.info("EM Publication complete");
		}
	}
	
	/**
	 * Constructor, settings loader 
	 */
	
	public EnergyModellerService(String propertyFile) {
		this.propertyFile=propertyFile;
		LOGGER.info("EM Initialization ongoing" + propertyFile);
		configureProperty();
		initializeDataConnectors();
		initializeQueueServiceManager();
		initializePrediction();
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
	

	private void initializeDataConnectors(){
		LOGGER.debug("Setting connection to data sources for events and energy ");
		energyService = new EnergyDataAggregatorServiceQueue();
		eventService = new EventDataAggregatorService();
		eventService.setDaoEvent(dbmanager.getDataEventDAOImpl());
		eventService.setupApplicationMonitor(emsettings.getAppmonitor());
		monitoringDataService = new MonitoringDataService();
		monitoringDataService.setDataDAO(dbmanager.getMonitoringData());
		LOGGER.debug("Configured ");
	}
		
	private void initializeQueueServiceManager(){
		LOGGER.info("Loading Queue service manager "+emsettings.getEnableQueue() +emsettings.getAmanagertopic()+emsettings.getIaasAmqpUrl());
		
		if (emsettings.getEnableQueue().equals("true")) {
			this.queueEnabled = true;
			try {
				paasQueueclient = new AmqpClient();
				paasQueueclient.setup(emsettings.getAmqpUrl(), emsettings.getAmqpUser(), emsettings.getAmqpPassword(), emsettings.getMonitoringQueueTopic());
				
				// TODO Hack to IaaS queue to be removed
				LOGGER.info("Enabling IaaS QUEUE TMP Workaround");
				iaasQueueclient = new AmqpClient();
				iaasQueueclient.setup(emsettings.getIaasAmqpUrl(), emsettings.getIaasAmqpUser(), emsettings.getIaasAmqpPassword());
				
				appRegistry = ApplicationRegistry.getRegistry(emsettings.getPaasdriver(),emsettings.getPaasurl(),emsettings.getPaasdbuser(),emsettings.getPaasdbpassword());
				dataCollectorHandler = DataConsumptionHandler.getHandler(emsettings.getPaasdriver(),emsettings.getPaasurl(),emsettings.getPaasdbuser(),emsettings.getPaasdbpassword());
				energyService.setDataMapper(dataCollectorHandler.getMapper());
				energyService.setRegistryMapper(appRegistry.getMapper());
				LOGGER.info("Enabling queue service");
				// TODO remove iaas queue when data will be sent directly to paas
				queueManager = new EnergyModellerQueueServiceManager(iaasQueueclient,paasQueueclient,appRegistry,dataCollectorHandler);
				LOGGER.debug("Enabled");
				
				// TODO remove iaas queue when data will be sent directly to paas
				queueManager.createTwoLayersConsumers(emsettings.getAmanagertopic(),emsettings.getPowertopic());
				LOGGER.debug("PaaS EM activemq connections are now Ready");
				
			} catch (Exception e) {
				LOGGER.error("ERROR initializing queues, now disabling the component..");
				emsettings.setEnableQueue("false");
				e.printStackTrace();
			}
		}
		LOGGER.info("Loaded");
	}
	
	private void initializePrediction(){
		LOGGER.info("EM predictor module loaded");
		predictor= PredictorBuilder.getPredictor("basic");
		predictor.setEnergyService(energyService);
	}

	@Override
	public boolean subscribeMonitoring(String providerid, String applicationid, String deploymentid, String eventid, long timewindow, Unit unit) {
		monitoringDataService.startMonitoring(applicationid, deploymentid, eventid);
		return true;
	}

	@Override
	public boolean unsubscribeMonitoring(String providerid,String applicationid,String deploymentid, String eventid, long timewindow, Unit unit) {
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
	

	/**
	 * 
	 * 
	 * TRAINING function not planned for Y2
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
	
	/*
	 * 
	 * Interface Y3
	 * 
	 */
	
	@Override
	public void manageComponent(String token, String command) {
		
	}
	
	// TODO: partially implemented during Y1
	private void initializeLoadInjector(){
		LOGGER.debug("Configuring load injector");
		loadInjector = new LoadInjectorService();
		loadInjector.configureLoadInjector(emsettings.getServerPath(), emsettings.getServerurl(), emsettings.getPropertyFile(), emsettings.getJmxFilePath());
		LOGGER.debug("Configured");
	}
	
// 
//	@Override
//	public double measure(String providerid, String applicationid,
//			List<String> vmids, String eventid, Unit unit, Timestamp start,
//			Timestamp end) {
//		
//		LOGGER.info("Requested Provider"+providerid+ " now application ");
//		LOGGER.info("Requested Application "+applicationid+" now deployment");
//		LOGGER.info("This is just a workadount of a deprecated IF");
//		return this.measure(null, providerid, applicationid,vmids, eventid, unit, start,  end);
//			
//	}



		
}
