/**
* Copyright 2014 Hewlett-Packard Development Company, L.P.                                         
 */
package eu.ascetic.asceticarchitecture.paas.component.energymodeller.internal.common.dataservice;

import java.util.List;
import java.util.Vector;

import org.apache.log4j.Logger;

import eu.ascetic.asceticarchitecture.paas.component.energymodeller.datatype.Unit;
import eu.ascetic.asceticarchitecture.paas.component.energymodeller.internal.common.data.database.table.DataConsumption;
import eu.ascetic.asceticarchitecture.paas.component.energymodeller.internal.common.data.ibatis.mapper.AppRegistryMapper;
import eu.ascetic.asceticarchitecture.paas.component.energymodeller.internal.common.data.ibatis.mapper.DataConsumptionMapper;

public class EnergyDataAggregatorServiceQueue {

	private DataConsumptionMapper dataConsumptionMapper;
	private AppRegistryMapper registryMapper;
	
	private static final Logger logger = Logger.getLogger(EnergyDataAggregatorServiceQueue.class);
	
	public void setRegistryMapper( AppRegistryMapper mapper){
		this.registryMapper = mapper;
	}
	
	public void setDataMapper(DataConsumptionMapper dataConsumptionMapper) {
		this.dataConsumptionMapper = dataConsumptionMapper;
	}

	public double getEnergyFromVM(String applicationid, String deployment, String vmid, String event) {
		vmid = translatePaaSFromIaasID(deployment,vmid);
		if (vmid == null ){
			logger.info("No PaaS ID found from IaaS ID");
			return -1;
		}
		logger.info("Start computing for"+deployment+" "+ vmid);
		return integrateSamples(deployment, vmid, -1,-1);
	}

	public double getMeasureInIntervalFromVM(Unit unit,String applictionid, String deployment, String vmid, long start, long end) {
		
		vmid = translatePaaSFromIaasID(deployment,vmid);
		
		if (vmid == null ){
			logger.info("No PaaS ID found from IaaS ID" +vmid);
			return 0;
		}
		int samples = dataConsumptionMapper.getSamplesBetweenTime( deployment, vmid, start/1000, end/1000);
		logger.info("Samples used for calculating "+vmid+" on this "+deployment + " value: "+samples+ " between "+start +" and "+end);
		if (samples == 0){
			logger.info("No samples available for the given interval "+start+ " to "+end+" estimating consumption from closest samples");
			long previoussampletime = dataConsumptionMapper.getSampleTimeBefore(deployment, vmid, start/1000);
			
			long aftersampletime = dataConsumptionMapper.getSampleTimeAfter(deployment, vmid, end/1000);
			if (previoussampletime == 0){
				logger.info("Not enough samples - before the event interval" +start/1000);
				return 0;
			}
			if (aftersampletime == 0){
				logger.info("Not enough samples - after the event interval"+end/1000);
				return 0;
			}
			logger.info(previoussampletime);
			logger.info(aftersampletime);
			DataConsumption esfirst = dataConsumptionMapper.getSampleAtTime(deployment,vmid,previoussampletime);
			DataConsumption eslast = dataConsumptionMapper.getSampleAtTime(deployment, vmid, aftersampletime);
			
			logger.info("The lower bound at "+esfirst.getTime()+" value "+esfirst.getVmpower());
			logger.info("The upper bound at "+eslast.getTime()+" value "+eslast.getVmpower());
			
			if (unit == Unit.ENERGY){
				double avgpower = (esfirst.getVmpower()+eslast.getVmpower())/2;
				double energy = avgpower * ((end-start))/3600000;
				// TODO refine better this value 
				logger.info("This interval has consumed energy (Wh) "+energy);
				return energy;
			}else {
				double avgpower = (esfirst.getVmpower()+eslast.getVmpower())/2;
				logger.info("In this interval the average power (W) is "+avgpower);
				return avgpower;
			}
		} else{
			logger.info("Samples available for the given interval " + samples);
			if (samples ==1){
				logger.debug("Only one sample available for the given interval "+start+ " to "+end+" estimating consumption from available samples");
				double avgpower = dataConsumptionMapper.getPowerInIntervalForVM(deployment, vmid, start,  end);
				logger.debug("Power  "+avgpower);
				if (unit == Unit.ENERGY){
					double energy = avgpower * ((end-start))/3600000;
					logger.info("This interval has consumed energy Wh "+energy);
					return energy;
				}else {
					return avgpower;
				}
			}
			
		}
		if (unit == Unit.ENERGY){

			return integrateSamples(deployment, vmid, start, end);
			
		} else {
			double result = dataConsumptionMapper.getPowerInIntervalForVM(deployment, vmid, start/1000, end/1000);
			logger.info("######### Avg power is "+result);
			return result;
		}
		
		
	}
	
	private double integrateSamples(String deployment, String vmid, long start, long end){
		List<DataConsumption> consumptionList;
		if (start>0){
			logger.info("Start computing Wh over a period of time");
			consumptionList = dataConsumptionMapper.getDataSamplesVM(deployment, vmid, start/1000, end/1000);
			
		}else {
			logger.info("Start computing Wh overall samples");
			consumptionList = dataConsumptionMapper.selectByVm(deployment, vmid);
		}
		DataConsumption previousSample=null;
		double accumulatedEnergy = 0;
		logger.info("Start computing Wh "+consumptionList.size());
		double partial = 0;
		for (DataConsumption sample: consumptionList){
			if (previousSample!=null){
				partial = integrate(previousSample.getVmpower(),sample.getVmpower(),previousSample.getTime(),sample.getTime());
				accumulatedEnergy = accumulatedEnergy + partial;
				
			} 
			previousSample = sample;
			
		}
		logger.info("total Wh is "+accumulatedEnergy);
		return accumulatedEnergy;
			
	}
	
	
	public List<DataConsumption> sampleMeasurements(String applicationid, String deployment, String vmid, long start,long end,long interval){
		vmid = translatePaaSFromIaasID(deployment,vmid);
		if (vmid == null ){
			logger.info("No PaaS ID found from IaaS ID");
			return null;
		}
		List<DataConsumption>  result = dataConsumptionMapper.getDataSamplesVM(deployment, vmid, start, end);
		List<DataConsumption> resampledresult = new Vector<DataConsumption>(); 
		if (result==null)return resampledresult;
		if (result.size()==0)return resampledresult;
		long currenttime = 0;
		int iteration = 0;
		int pointer=0;
		double last_powerval=0;
		double last_ts=start;
		currenttime = start;
		double aggr_energy = 0;
		while (currenttime < end){
			if (iteration == 0){
				DataConsumption as = new DataConsumption();
				as.setVmcpu(result.get(0).getVmcpu());
				as.setVmpower(result.get(0).getVmpower());
				as.setVmenergy(0);
				as.setTime(start);
				as.setVmid(vmid);
				last_ts=currenttime;
				last_powerval=result.get(0).getVmpower();
				iteration++;
				resampledresult.add(as);
			}else { 
				DataConsumption as = new DataConsumption();
				while((result.get(pointer).getTime()<currenttime)){
					pointer++;	
					if (pointer==result.size())break;
				}
				if (pointer<result.size()){
					as.setVmcpu(result.get(pointer).getVmcpu());
					as.setVmpower(result.get(pointer).getVmpower());
					as.setTime(currenttime);
					as.setVmid(vmid);
					as.setApplicationid(applicationid);
					double part_energy = ((last_powerval+result.get(pointer).getVmpower())/2)*(currenttime-last_ts)/3600000;
					aggr_energy = aggr_energy +part_energy;
					as.setVmenergy(aggr_energy);
					last_ts=currenttime;
					last_powerval=result.get(pointer).getVmpower();
					iteration++;
					resampledresult.add(as);
				}
			}
			currenttime = currenttime + interval*1000;
			
		}
		return resampledresult;
	}
	
	// TODO to be removed
	public String translatePaaSFromIaasID(String deployid, String paasvmid){
		logger.info(" I translated the iaas id " + paasvmid);
		String iaasVmId = registryMapper.selectFromIaaSID(deployid,paasvmid);
		logger.info(" I translated to " + paasvmid + " the iaas id " + iaasVmId);
		return iaasVmId;
	}
	
	private double integrate(double powera,double powerb, long timea,long timeb){
		
		double integral = Math.abs(timeb-timea)*(powera+powerb)*(0.5/3600);
		if 	(Math.abs(timeb-timea)>3600) {
			logger.info("Delta "+powera+" "+powerb);
			logger.info("Delta between samples greater than one hour! "+integral + " "+(powera+powerb)+ " "+Math.abs(timeb-timea));
		}
		return 	integral;
		
		
	}
	

}
