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
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import org.apache.log4j.Logger;

import eu.ascetic.asceticarchitecture.paas.component.energymodeller.datatype.ApplicationSample;
import eu.ascetic.asceticarchitecture.paas.component.energymodeller.datatype.EventSample;
import eu.ascetic.asceticarchitecture.paas.component.energymodeller.datatype.Unit;
import eu.ascetic.asceticarchitecture.paas.component.energymodeller.datatype.messages.GenericEnergyMessage;
import eu.ascetic.asceticarchitecture.paas.component.energymodeller.interfaces.PaaSEnergyModeller;
import eu.ascetic.asceticarchitecture.paas.component.energymodeller.internal.common.config.EMSettings;
import eu.ascetic.asceticarchitecture.paas.component.energymodeller.internal.common.data.database.table.DataConsumption;
import eu.ascetic.asceticarchitecture.paas.component.energymodeller.internal.common.data.database.table.DataEvent;
import eu.ascetic.asceticarchitecture.paas.component.energymodeller.internal.common.data.ibatis.ApplicationRegistry;
import eu.ascetic.asceticarchitecture.paas.component.energymodeller.internal.common.data.ibatis.DataConsumptionHandler;
import eu.ascetic.asceticarchitecture.paas.component.energymodeller.internal.common.data.ibatis.MonitoringRegistry;
import eu.ascetic.asceticarchitecture.paas.component.energymodeller.internal.common.dataservice.EnergyDataAggregatorServiceQueue;
import eu.ascetic.asceticarchitecture.paas.component.energymodeller.internal.common.dataservice.EventDataAggregatorService;
import eu.ascetic.asceticarchitecture.paas.component.energymodeller.internal.model.PredictorBuilder;
import eu.ascetic.asceticarchitecture.paas.component.energymodeller.internal.model.predictor.PredictorInterface;
import eu.ascetic.asceticarchitecture.paas.component.energymodeller.internal.monitor.EnergyModellerMonitor;
import eu.ascetic.asceticarchitecture.paas.component.energymodeller.internal.queue.EnergyModellerQueueServiceManager;
import eu.ascetic.asceticarchitecture.paas.component.energymodeller.internal.queue.client.AmqpClient;

/**
 * @author davide sommacampagna
 */

public class EnergyModellerService implements PaaSEnergyModeller {

	private final static Logger LOGGER = Logger.getLogger(EnergyModellerService.class.getName());
	private final static int SECONDS_IN_HOUR = 3600;
	private final static int SECONDS_IN_DAY = 86400000;
	private final static int TO_MILLISEC = 1000;
	
	private String propertyFile = "config.properties";
	private EMSettings emsettings;
	
	private DataConsumptionHandler dataCollectorHandler;
	private PredictorInterface predictor;
	private ApplicationRegistry appRegistry;
	private MonitoringRegistry monitoringRegistry;
	
	private EnergyModellerMonitor monitorThread;
	
	private EnergyModellerQueueServiceManager queueManager;
	private AmqpClient paasQueueclient;
	private AmqpClient iaasQueueclient;
	private String monitoringTopic="monitoring";
	private String predictionTopic="prediction";
	private Boolean queueEnabled = false;
	private EnergyDataAggregatorServiceQueue energyService;
	private EventDataAggregatorService eventService;
	private ScheduledExecutorService service;

	
	/**
	 * 
	 * 
	 * MEASURE: get measurements from available samples, 
	 * 			Unit tells if the method should compute value of average instant power or accumulated energy consumption
	 * 			a null eventid means to perform computation over VMs not considering specific application events
	 * 			if timestamps are not provided all available data will be considered regardless the time  
	 * 
	 */	
	
	@Override
	public double measure(String providerid, String applicationid, String deploymentid,List<String> vmids, String eventid, Unit unit,Timestamp start, Timestamp end) {
		// M. Fontanella - 11 Jan 2016 - begin
		if (providerid==null) providerid=emsettings.getProviderIdDefault();
		// M. Fontanella - 11 Jan 2016 - end
		if((start==null)&&(end==null)){
			LOGGER.debug("Sice not timestamps are provide I am measuring from all available data since both timestamps are not specified");
			if (unit==Unit.ENERGY){
				LOGGER.info("Measuring all energy consumption");
				double result = aggregateVMenergyConsumption(providerid,applicationid,deploymentid, vmids,eventid,null,null);
				if (result>=0)sendToQueue(monitoringTopic, providerid, applicationid, deploymentid, vmids, eventid, GenericEnergyMessage.Unit.WATTHOUR, null, result);
				return result;
			} else {
				LOGGER.info("Measuring average instant power"); 
				double result = averagePower(providerid,applicationid, deploymentid, vmids,  eventid,null,null);
				if (result>=0)sendToQueue(monitoringTopic, providerid, applicationid, deploymentid, vmids, eventid, GenericEnergyMessage.Unit.WATT, null, result);
				return result;
			}
		}else {
			LOGGER.info("Populating the timestamps if necessary"); 
			if (start==null){
				// going back to one day
				Calendar calendar = Calendar.getInstance();
				long now = calendar.getTime().getTime();
				now = now - (SECONDS_IN_DAY);
				if (now>end.getTime()){
					now = end.getTime()-(SECONDS_IN_DAY);
				}
				Timestamp currentTimestamp = new Timestamp(now);
				start = currentTimestamp;
				LOGGER.info("Going to get data from the last 24h :  "+now);
			}
			if (end==null){
				// sets to now the final timestamp to which the samples should be searched
				Calendar calendar = Calendar.getInstance();
				Timestamp currentTimestamp = new Timestamp(calendar.getTime().getTime());
				end = currentTimestamp;
				LOGGER.info("Going to get data untill now :  "+end.getTime());
			}
			LOGGER.info("Checking the Units"); 

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
	 * 			if unit is power it just compute the future value of power based on statistical model, if unit is energy it forecast future power
	 * 			and after calculating the current consumption it add the consumption due to this future estimated power value
	 * 			if event id is provided the computation is related only to events and it is estimated their instant power and average duration based on available information
	 * 			calculated values are pushed to AMQP queue only if major than 0
	 */	
	
	@Override
	public double estimate(String providerid, String applicationid,  String deploymentid,	List<String> vmids, String eventid, Unit unit, long window) {
		LOGGER.info("Forecasting requested"); 
		double currentval = 0;
		// M. Fontanella - 11 Jan 2016 - begin
		if (providerid==null) providerid=emsettings.getProviderIdDefault();
		// M. Fontanella - 11 Jan 2016 - end
		if (eventid==null){
			double total_power_estim=0;
			double total_energy_estim=0;
			double count_vm=0;
			for(String vm : vmids){
				count_vm++;
				LOGGER.info("Forecasting for this vm  "+vm);
				currentval = predictor.estimate(providerid,applicationid, deploymentid, vm, eventid, unit, window);	
				List<String> thisvm = new Vector<String>();
				thisvm.add(vm);
				LOGGER.info("current forecast is  "+currentval);
				total_power_estim = total_power_estim + currentval;
				if (unit==Unit.ENERGY){
					LOGGER.info("Energy forecast is requested adding current power forecast to consumption accumulated  "+currentval);
					// M. Fontanella - 11 Jan 2016 - begin
					double current_consumption = energyService.getEnergyFromVM(providerid, applicationid, deploymentid, vm, eventid);
					// M. Fontanella - 11 Jan 2016 - end
					LOGGER.info("############ Current vm consumption "+current_consumption);
					// M. Fontanella - 11 Jan 2016 - begin
					DataConsumption dc = energyService.getLastPowerSampleFromVM(providerid, applicationid, deploymentid, vm);
					// M. Fontanella - 11 Jan 2016 - end
					if (dc!=null){
						LOGGER.info("######## ENERGY ESTIMATION SUMMARY");
						
						LOGGER.info("############ Last vm power "+dc.getVmpower()+" at time "+dc.getTime());
						LOGGER.info("############ Estimated vm power "+currentval+" at time ");
						double predicted_consumption = (0.5)*((currentval + dc.getVmpower()) / (window/SECONDS_IN_HOUR));
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
			// an event id is provided, basically is the average power of events occurred in the past, if consumption is requested, the values is multiplied for their average duration
			currentval = averagePower(providerid,applicationid, deploymentid, vmids,  eventid,null,null);
			LOGGER.info("############ EVENTs Forecasted instant power, now set to queue " + currentval); 
			if (currentval>0)sendToQueue(predictionTopic, providerid, applicationid, deploymentid, vmids, eventid, GenericEnergyMessage.Unit.WATT, null, currentval);
			double duration = averageDuration(providerid,applicationid, deploymentid, vmids,  eventid);
			LOGGER.info("############ EVENT Forecasting duration (sec) " + duration); 
			LOGGER.info("############ EVENT val is " + currentval); 
			if (unit==Unit.ENERGY){
				LOGGER.info("############ EVENT statistics");
				LOGGER.info("############ EVENT duration (sec) "+duration+ " in hour "+duration/3600);
				LOGGER.info("############ EVENT power "+currentval);
				
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
		return currentval;
	}

	
	/**
	 * Private method, used internally for:
	 * GET ENERGY CONSUMED BY AN APPLICATION IF EVENT ID IS NULL  , OR THE TOTAL ENERGY CONSUMED BY THE SAME EVENT TYPE IF ITs ID IS PROVIDED, AGGREGATE FOR ALL VMS SPECCIFIED
	 * 
	 */
	private double aggregateVMenergyConsumption(String providerid,String applicationid, String deploymentid, List<String> vmids, String eventid,Timestamp start,Timestamp end) {
		
			double total_energy=0;
			// M. Fontanella - 11 Jan 2016 - begin
			if (providerid==null) providerid=emsettings.getProviderIdDefault();
			// M. Fontanella - 11 Jan 2016 - end
			if (eventid==null){
				LOGGER.debug("Analyzing each VM consumption");
				for (String vm : vmids) {
					double energy = energyForVM(providerid, applicationid, deploymentid, vm, eventid,start,end);
					LOGGER.info("This VM "+ vm + " consumed " + String.format( "%.2f", energy ));
					total_energy = total_energy +energy;
				}			
				LOGGER.info("Application consumed " + String.format( "%.2f", total_energy ));
			} else {
				LOGGER.debug("Analyzing event consumption");
				for (String vm : vmids) {
					LOGGER.debug("Analyzing events on this VM "+vm); 
					total_energy = total_energy + energyForVM(providerid, applicationid, deploymentid, vm, eventid,start,end);
					LOGGER.info("energy is "+total_energy); 
				}
				LOGGER.info("Event energy Wh " +String.format( "%.2f", total_energy ));
			}
			return total_energy;
			
	}
	
	/**
	 * 
	 * Private method, used internally for:
	 * Returns an EventSample for each event in the time interval, providing average instant power and energy consumption of each
	 * 
	 */	
	
	@Override
	public List<EventSample> eventsData(String providerid,String applicationid,  String deploymentid, List<String> vmids, String eventid,Timestamp start, Timestamp end) {
		// M. Fontanella - 11 Jan 2016 - begin
		if (providerid==null) providerid=emsettings.getProviderIdDefault();
		// M. Fontanella - 11 Jan 2016 - end
		List<EventSample> results = new Vector<EventSample>();
		for (String vm : vmids){
			results.addAll(eventsSamplesInInterval( providerid, applicationid, deploymentid, vm,  eventid,start, end));
		}
		return results;
	}

	/**
	 * 
	 * Private method, used internally for:
	 * Returns an Application Sample for each sample in the give interval, sampled at the samplingperiod time
	 * 
	 */	
	
	@Override
	public List<ApplicationSample> applicationData( String providerid, String applicationid, String deploymentid, List<String> vmids, long samplingperiod,Timestamp start, Timestamp end){
		// M. Fontanella - 11 Jan 2016 - begin
		if (providerid==null) providerid=emsettings.getProviderIdDefault();
		// M. Fontanella - 11 Jan 2016 - end
		List<DataConsumption> resultssamples = new Vector<DataConsumption>();
		List<ApplicationSample> results = new Vector<ApplicationSample>();
		for(String vm : vmids){
			// M. Fontanella - 11 Jan 2016 - begin
			List<DataConsumption> estim_sample = energyService.sampleMeasurements(providerid, applicationid, deploymentid, vm, start.getTime(),end.getTime(),samplingperiod);
			// M. Fontanella - 11 Jan 2016 - end
			if (estim_sample!=null)resultssamples.addAll(estim_sample);
		}
		for(DataConsumption dc : resultssamples){
			ApplicationSample as = new ApplicationSample();
			// M. Fontanella - 20 Jan 2016 - begin
			as.setProvid(dc.getProviderid());
			// M. Fontanella - 20 Jan 2016 - end
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
	 * subscribe an application for monitoring
	 */
	
	@Override
	public boolean subscribeMonitoring(String providerid, String applicationid,	String deploymentid) {
		// M. Fontanella - 11 Jan 2016 - begin
		if (providerid==null) providerid=emsettings.getProviderIdDefault();
		// M. Fontanella - 11 Jan 2016 - end
		monitorThread.subscribeMonitoring(providerid,applicationid, deploymentid);
		
		return false;
	}
	
	/**
	 * remove an application from monitoring
	 */

	@Override
	public boolean unsubscribeMonitoring(String providerid,	String applicationid, String deploymentid) {
		// M. Fontanella - 11 Jan 2016 - begin
		if (providerid==null) providerid=emsettings.getProviderIdDefault();
		// M. Fontanella - 11 Jan 2016 - end
		monitorThread.unsubscribeMonitoring(providerid,applicationid, deploymentid);	
		return false;
	}
	
	/**
	 * PRIVATE METHOD SECTION
	 */	
	
	/**
	 * 
	 * Private method, used internally for:
	 * Calculates the average duration, in seconds, of an event, if more VMs are provided the average is globally calculated
	 * this method also pushed to the queue the event duration value for each VM and the total number of events for VM, this is used by the 
	 * Pricing manager
	 * 
	 */	
	
	private double averageDuration(String providerid, String applicationid, String deploymentid, List<String> vmids, String eventid){
		double duration=0;
		long vmwithevent=0;
		
		// M. Fontanella - 11 Jan 2016 - begin
		if (providerid==null) providerid=emsettings.getProviderIdDefault();
		// M. Fontanella - 11 Jan 2016 - end
		for (String vm : vmids) {
			LOGGER.info("############ Measuring event average instant power (W) for vm "+vm); 
			// TODO workaround 
			String translated = energyService.translatePaaSFromIaasID(deploymentid, vm);
			// M. Fontanella - 11 Jan 2016 - begin
			List<DataEvent> events = eventService.getEvents(providerid, applicationid, deploymentid, translated, eventid,null,null);
			// M. Fontanella - 11 Jan 2016 - end
			long vmavg =0;
			int totalevent=0;
			for (DataEvent de: events){
				long delta = (de.getEndtime() - de.getBegintime()); 
				totalevent++;
				LOGGER.info("############ Event elapsed time "+delta/TO_MILLISEC); 
				vmavg = vmavg +delta/TO_MILLISEC;
			}
			if (totalevent>0){
				vmavg = vmavg/totalevent;
				duration = duration+vmavg;
				if(vmavg>0)vmwithevent++;
			} 
			LOGGER.info("############ This VM " + vm + " has events  " + totalevent + " avg duration " + vmavg + " vms with events are "+vmwithevent );
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
	 * Private method, used internally for:
	 * GET THE AVERAGE INSTANT POWER MEASURED FOR AN APPLICATION IF EVENT ID IS NULL, OR EVENT IF IT IS PROVIDED
	 * for events there is a concept of global event, an event without reference ot a specific VM (it miss the VMid) such events are computed
	 * as they are occurring on each VM where the application has been deployed, so for this reason they are considered global
	 * if more event of different types occurs also the total power is splitted to the global number of events
	 */	
	
	private double averagePower(String providerid, String applicationid, String deploymentid, List<String> vmids, String eventid, Timestamp start, Timestamp end) {
		
		// M. Fontanella - 11 Jan 2016 - begin
		if (providerid==null) providerid=emsettings.getProviderIdDefault();
		// M. Fontanella - 11 Jan 2016 - end
		if((start==null)&&(end==null)){
			if (eventid!=null){
				LOGGER.info("Measuring event average instant power (W)"); 
				double totalPower = 0;
				double countEvents = 0;
				double countVM = 0;
				boolean isGlobaEvent = false;
				for (String vm : vmids) {
					LOGGER.info("Measuring event average instant power (W) for vm "+vm); 
					// TODO workaround because samples are registered with IaaS id, the Paas layer translates the ID registered during application deployment and
					// translates it to the IaaS ID
					String translated = energyService.translatePaaSFromIaasID(deploymentid, vm);
					if (translated==null){
						LOGGER.warn("Error cannot match this PaaS ID with the IaaS ID not found");
					} 
					// M. Fontanella - 11 Jan 2016 - begin
					List<DataEvent> events = eventService.getEvents(providerid, applicationid, deploymentid, translated, eventid,null,null);
					// M. Fontanella - 11 Jan 2016 - end
					
					if (translated!=null)LOGGER.info("Got events: "+events.size());
					double accumulatedpowerpervm = 0;
					for (DataEvent de: events){
						
						LOGGER.info("This event starts "+de.getBegintime()+" and terminates  "+ de.getEndtime());
						if (de.getData()!=null){
							LOGGER.info("Event global"+de.getData());
							isGlobaEvent = true;
						} else {
							LOGGER.info("Event local");
							
						}
						// M. Fontanella - 11 Jan 2016 - begin
						double power  = energyService.getMeasureInIntervalFromVM(Unit.POWER, providerid, applicationid, deploymentid, vm, de.getBegintime(), de.getEndtime());
						int count = eventService.getAllEventsNumber(providerid, applicationid, translated, eventid, de.getBegintime(), de.getEndtime());
						// M. Fontanella - 11 Jan 2016 - end
						LOGGER.info("it has been executed with other "+count);
						if (count>0){
							double event_delta = de.getEndtime() - de.getBegintime();
							// M. Fontanella - 11 Jan 2016 - begin
							List<Long> split = eventService.getAllDeltas(providerid, applicationid, translated, eventid, de.getBegintime(), de.getEndtime());
							// M. Fontanella - 11 Jan 2016 - end
							double split_factor = 0;
							LOGGER.info("## total "+count);
							for (Long delta : split){
								double coeff = delta/event_delta;
								LOGGER.info("## this event split "+coeff + " from its duration "+ delta +"and the original "+ event_delta);
								split_factor = split_factor + coeff;
							}
							//power = power/split_factor;
							LOGGER.info("accurate split factor "+ (power/split_factor) + " split alone" +split_factor );
							LOGGER.info("generic split factor "+ (power/count)  );
							power = power/split_factor;
						}
						LOGGER.info("##### This event "+countEvents+" power :  "+power);
						if (power>0)countEvents++;
						accumulatedpowerpervm = accumulatedpowerpervm + power;
					}
	
					if (translated!=null) LOGGER.info("##### average for this vm power was :  "+totalPower);
					totalPower = totalPower + accumulatedpowerpervm;
					if (accumulatedpowerpervm>0){
						LOGGER.info("##### This vm has relevant event data and now is:  "+totalPower);
						LOGGER.info("##### Accumulated power from this iteration:  "+accumulatedpowerpervm);
						LOGGER.info("##### Accumulated events:  "+countEvents);
						countVM++;
					} else {
						LOGGER.info("##### This vm has no event data or power measurements for this event:  "+accumulatedpowerpervm);
					}
				}
				LOGGER.info("#####  VM counts before this iteration :  "+totalPower + " now the count is "+countVM);
				LOGGER.info("##### This event is globally computer for all VMs " + isGlobaEvent);
				
				if (countEvents<=0) return 0;
				if (isGlobaEvent) return totalPower;
				LOGGER.info("##### Accumulated events:  "+countEvents);
				return totalPower/countEvents;
			}else{
				LOGGER.info("Measuring application average instant power (W) in the last 24 hours"); 
				double totalPower = 0;
				double countEvents = 0;
				Calendar c_tiemstamp = Calendar.getInstance();
				for (String vm : vmids) {
					countEvents++;
					// M. Fontanella - 11 Jan 2016 - begin
					double power  = energyService.getMeasureInIntervalFromVM(Unit.POWER, providerid, applicationid, deploymentid, vm, (c_tiemstamp.getTimeInMillis()-(86400*1000)), c_tiemstamp.getTimeInMillis());
					// M. Fontanella - 11 Jan 2016 - end
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
					// M. Fontanella - 11 Jan 2016 - begin
					double power  = energyService.getMeasureInIntervalFromVM(Unit.POWER, providerid, applicationid, deploymentid, vm,  start.getTime(), end.getTime());
					// M. Fontanella - 11 Jan 2016 - end
					LOGGER.info("Avg power for VM "+ vm + " was " + String.format( "%.2f", power ));
					if (power>0)tot_power = tot_power + power;
				}
				return tot_power;
			}else{
				for (String vm : vmids) {
					// TODO workaround
					String translated = energyService.translatePaaSFromIaasID(deploymentid, vm);
					// M. Fontanella - 11 Jan 2016 - begin
					List<DataEvent> events = eventService.getEvents( providerid, applicationid, deploymentid, translated, eventid,start,end);
					// M. Fontanella - 11 Jan 2016 - end
					for (DataEvent de: events){
						
						// M. Fontanella - 11 Jan 2016 - begin
						double power  = energyService.getMeasureInIntervalFromVM(Unit.POWER, providerid, applicationid, deploymentid, vm,  de.getBegintime(), de.getEndtime());
						// M. Fontanella - 11 Jan 2016 - end
						if (power>0){
							LOGGER.info("This event power :  "+power);
							// M. Fontanella - 11 Jan 2016 - begin
							int count = eventService.getAllEventsNumber(providerid, applicationid, translated, eventid, de.getBegintime(), de.getEndtime());
							// M. Fontanella - 11 Jan 2016 - end
							LOGGER.info(power+"it has been executed with other "+count);
							if (count>0){
								double event_delta = de.getEndtime() - de.getBegintime();
								// M. Fontanella - 11 Jan 2016 - begin
								List<Long> split = eventService.getAllDeltas(providerid, applicationid, translated, eventid, de.getBegintime(), de.getEndtime());
								// M. Fontanella - 11 Jan 2016 - end
								double split_factor = 0;
								LOGGER.info("## total "+count);
								for (Long delta : split){
									
									double coeff = delta/event_delta;
									LOGGER.info("## this event split "+coeff + " from its duration "+ delta +"and the original "+ event_delta);
									split_factor = split_factor + coeff;
								}
								//power = power/split_factor;
								LOGGER.info("accurate split factor "+ power/split_factor );
								power = power/count;
								//LOGGER.info(power+"the power has been split between "+count + "events" );
								LOGGER.info("uniform split "+power );
							}

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
	 *  when both timestamps are null it means that data taken from all samples without time filtering
	 */ 

	private double energyForVM(String providerid, String applicationid, String deploymentid, String vmid, String eventid, Timestamp start, Timestamp end) {
		// M. Fontanella - 11 Feb 2016 - begin
        if (eventid==null){
            LOGGER.info("Application energy estimation for " + deploymentid );
            if ((start==null)&&(end==null)){
                 // M. Fontanella - 11 Jan 2016 - begin
                 return energyService.getEnergyFromVM(providerid, applicationid, deploymentid, vmid, eventid);
                 // M. Fontanella - 11 Jan 2016 - end
            } else {
                // M. Fontanella - 11 Jan 2016 - begin
                return energyService.getMeasureInIntervalFromVM(Unit.ENERGY, providerid, applicationid, deploymentid, vmid, start.getTime(), end.getTime());
                // M. Fontanella - 11 Jan 2016 - end
            }
        } else {
            // M. Fontanella - 20 Jan 2016 - begin
            LOGGER.info("Energy estimation for provider " + providerid + " application " + applicationid + " and its event " + eventid);
            // M. Fontanella - 20 Jan 2016 - end
            int eventcount =0;
            double energyAverage = 0;
            String translated = energyService.translatePaaSFromIaasID(deploymentid, vmid);
            // M. Fontanella - 11 Jan 2016 - begin
            List<DataEvent> events = eventService.getEvents( providerid, applicationid, deploymentid , translated, eventid,start,end);
            // M. Fontanella - 11 Jan 2016 - end
            LOGGER.info("events "+events.size());
            for (DataEvent de: events){
                    // M. Fontanella - 11 Jan 2016 - begin
                    double energy = energyService.getMeasureInIntervalFromVM(Unit.ENERGY, de.getProviderid(), de.getApplicationid(),de.getDeploymentid(), de.getVmid(),de.getBegintime(),de.getEndtime());
                    // M. Fontanella - 11 Jan 2016 - end
                    if (energy >0){
                            // M. Fontanella - 11 Jan 2016 - begin
                            int count = eventService.getAllEventsNumber(providerid, applicationid, translated, eventid, de.getBegintime(), de.getEndtime());
                            // M. Fontanella - 11 Jan 2016 - end
                            LOGGER.info(energy+"it has been with other "+count);
                            if (count>0){
                                    double event_delta = de.getEndtime() - de.getBegintime();
                                    // M. Fontanella - 08 Feb 2016 - begin
                                    // List<Long> split = eventService.getAllDeltas(providerid, applicationid, vmid, eventid, de.getBegintime(), de.getEndtime());
                                    List<Long> split = eventService.getAllDeltas(providerid, applicationid, translated, eventid, de.getBegintime(), de.getEndtime());
                                    // M. Fontanella - 08 Feb 2016 - end
                                    double split_factor = 0;
                                    for (Long delta : split){
                                            double coeff = delta/event_delta;
                                            split_factor = split_factor + coeff;
                                    }
                                    energy = energy/split_factor;
                                    LOGGER.info("the energy has been split between "+count + "events" );
                            }
                    LOGGER.info("This event : "+ de.getBegintime() + " and " +de.getEndtime() + " energy "+energy+de.getVmid());

                            eventcount++;
                            energyAverage = energyAverage + energy;
                    }
            }
            if (eventcount==0)return 0;
            if ((start==null)&&(end==null))   LOGGER.info("Wh : "+ energyAverage + " over "+eventcount+" events");
            else LOGGER.info("Wh : "+ energyAverage + " over "+eventcount+" events in the specified interval");
            
            return (energyAverage/eventcount );
        }

		// M. Fontanella - 11 Feb 2016 - end
	}
		
	/**
	 * 
	 * Extracts events from the database of the EM and for each event generates an Event sample with consumption and duration infromation 
	 *  
	 */
	
	private List<EventSample> eventsSamplesInInterval(String providerid,String applicationid, String deploymentid, String vmid, String eventid,Timestamp start, Timestamp endtime) {
		
		// M. Fontanella - 11 Jan 2016 - begin
		if (providerid==null) providerid=emsettings.getProviderIdDefault();
		// M. Fontanella - 11 Jan 2016 - end
		List<EventSample> eSamples = new Vector<EventSample>();
		
		EventSample es = new EventSample();
		String translated = energyService.translatePaaSFromIaasID(deploymentid, vmid);
		// M. Fontanella - 11 Jan 2016 - begin
		List<DataEvent> events = eventService.getEvents(providerid, applicationid, deploymentid, translated, eventid,start,endtime);
		// M. Fontanella - 11 Jan 2016 - end
		for (DataEvent de: events){
			es = new EventSample();
			// M. Fontanella - 11 Jan 2016 - begin
			double power  = energyService.getMeasureInIntervalFromVM(Unit.ENERGY, providerid, applicationid, deploymentid, vmid, start.getTime(), endtime.getTime());
			double energy = energyService.getMeasureInIntervalFromVM(Unit.POWER, de.getProviderid(), de.getApplicationid(), de.getDeploymentid(), vmid,de.getBegintime(),de.getEndtime());
			// M. Fontanella - 11 Jan 2016 - end
			if (energy > 0){
				// M. Fontanella - 11 Jan 2016 - begin
				int count = eventService.getAllEventsNumber(providerid, applicationid, translated, eventid, de.getBegintime(), de.getEndtime());
				// M. Fontanella - 11 Jan 2016 - end
				LOGGER.info(energy+"it has been with other "+count);
				if (count>0){
					energy = energy/count;
					power=power/count;
					LOGGER.info("the energy has been split between "+count + "events" );
					LOGGER.info("the power has been split between "+count + "events" );
				}
				LOGGER.info("This event :  "+energy);
				// M. Fontanella - 20 Jan 2016 - begin
				es.setProvid(de.getProviderid());
				// M. Fontanella - 20 Jan 2016 - end
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
	 * Queue message generation, this method is a utility that pushed messages to the queue
	 *  
	 */
	
	private void sendToQueue(String queue,String providerid,String applicationid, String deploymentid, List<String> vms, String eventid, GenericEnergyMessage.Unit unit, String referenceTime,double value){
		// M. Fontanella - 11 Jan 2016 - begin
		if (providerid==null) providerid=emsettings.getProviderIdDefault();
		// M. Fontanella - 11 Jan 2016 - end
		LOGGER.info("EM queue enabled is "+queueEnabled);
		if (queueEnabled){
			LOGGER.info("EM publish message of energy");
			queueManager.sendToQueue(queue, providerid, applicationid, deploymentid, vms, eventid, unit, referenceTime, value);
			LOGGER.info("EM Publication complete");
		}
	}
	
	/**
	 * Constructor, settings loader, in order it load the configurations, it setup database connnections, set the queue connection and load the energy model predictor
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
	 * private methods for initialization and configuration of this component, it expects the property file to be in the classpath
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
			LOGGER.debug("Configured");
		} catch (IOException e) {
			LOGGER.error("Properties not loaded due to a failure");
			LOGGER.error("Properties not loaded, file not found!");
			e.printStackTrace();
		}
	}
	
	/**
	 * 
	 *  it sets the Energy and Event services that allows the class to load data collected about consumption and events, it also set the
	 *  database DAO to handle database storaged and searches
	 * 
	 */
	
	private void initializeDataConnectors(){
		LOGGER.debug("Setting connection to data sources for events and energy ");
		energyService = new EnergyDataAggregatorServiceQueue();
		eventService = new EventDataAggregatorService();
		eventService.setupApplicationMonitor(emsettings.getAppmonitor());
		
		
		LOGGER.debug("Configured ");
	}
		
	/**
	 * initialize the queue component, there are two componens, one paasQueueClient that handle messages pushed to AMQP at paas layer (consumption) also read information about application deployed
	 * and iaasQueueClient that is used to retrieve consumption messages generated by the IaaS layer
	 */
	
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
				monitoringRegistry = MonitoringRegistry.getRegistry(emsettings.getPaasdriver(),emsettings.getPaasurl(),emsettings.getPaasdbuser(),emsettings.getPaasdbpassword());
				appRegistry = ApplicationRegistry.getRegistry(emsettings.getPaasdriver(),emsettings.getPaasurl(),emsettings.getPaasdbuser(),emsettings.getPaasdbpassword());
				dataCollectorHandler = DataConsumptionHandler.getHandler(emsettings.getPaasdriver(),emsettings.getPaasurl(),emsettings.getPaasdbuser(),emsettings.getPaasdbpassword());
				energyService.setDataRegistry(dataCollectorHandler);
				energyService.setApplicationRegistry(appRegistry);
				LOGGER.info("Enabling queue service");
				// TODO remove iaas queue when data will be sent directly to paas
				queueManager = new EnergyModellerQueueServiceManager(iaasQueueclient,paasQueueclient,appRegistry,dataCollectorHandler);
				LOGGER.debug("Enabled");
				
				// TODO remove iaas queue when data will be sent directly to paas
				queueManager.createTwoLayersConsumers(emsettings.getAmanagertopic(),emsettings.getPowertopic());
				LOGGER.debug("PaaS EM activemq connections are now Ready");
				// TODO uncomment
				LOGGER.info("PaaS EM starting monitor thread");
				initializeMonitoring(180,emsettings.getAppmonitor(),energyService,appRegistry);
				LOGGER.info("PaaS EM started monitor thread");
			} catch (Exception e) {
				LOGGER.error("ERROR initializing queues, now disabling the component..");
				emsettings.setEnableQueue("false");
				e.printStackTrace();
			}
		}
		LOGGER.info("Loaded");
	}
	
	/**
	 * it starts a thread that check applicatio registered for monitoring, such applicatio are registered via subscribemonitoring interface
	 */
	private void initializeMonitoring(long delay,String app_man_url,EnergyDataAggregatorServiceQueue energyService, ApplicationRegistry registry){
		monitorThread = new EnergyModellerMonitor();
		monitorThread.setup(app_man_url);
		monitorThread.setEnergyService(energyService);
		monitorThread.setAppRegistry(appRegistry);
		monitorThread.setMonitoring(monitoringRegistry);
		service = Executors.newSingleThreadScheduledExecutor();
		service.scheduleAtFixedRate(monitorThread, 0, delay, TimeUnit.SECONDS);
		LOGGER.info("PaaS EM thread on for monitoring");
		
		
	}
	
	/**
	 * initialize the predictor component by callign a builder, the builder return a generic interface of the predictor service taking as input the type of predictor to be instatiated
	 */
	
	private void initializePrediction(){
		LOGGER.info("EM predictor module loaded");
		// M. Fontanella - 08 Jan 2016 - begin
		// predictor= PredictorBuilder.getPredictor("basic");
		predictor= PredictorBuilder.getPredictor(emsettings.getPredictorType());
		// M. Fontanella - 08 Jan 2016 - end
	
		predictor.setEnergyService(energyService);
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
	 * the following interfaces can be ingnored
	 * 
	 */	
	
	@Override
	public boolean trainApplication(String providerid, String applicationid,String deploymentid, String eventid) {

		return true;
	}
	
	
	@Override
	public void manageComponent(String token, String command) {
		
	}

}
