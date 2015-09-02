/**
* Copyright 2014 Hewlett-Packard Development Company, L.P.                                         
 */
package eu.ascetic.asceticarchitecture.paas.component.energymodeller.internal.common.dataservice;

import java.sql.Timestamp;
import java.util.List;
import java.util.Vector;

import org.apache.log4j.Logger;

import eu.ascetic.asceticarchitecture.paas.component.energymodeller.datatype.Unit;
import eu.ascetic.asceticarchitecture.paas.component.energymodeller.internal.common.data.database.table.DataConsumption;
import eu.ascetic.asceticarchitecture.paas.component.energymodeller.internal.common.data.ibatis.mapper.DataConsumptionMapper;

public class EnergyDataAggregatorServiceQueue {

	private DataConsumptionMapper dataConsumptionMapper;
	private static final Logger logger = Logger.getLogger(EnergyDataAggregatorServiceQueue.class);
	
	public void setDataMapper(DataConsumptionMapper dataConsumptionMapper) {
		this.dataConsumptionMapper = dataConsumptionMapper;
	}

	public double getEnergyFromVM(String deployment, String vmid, String event) {
		return dataConsumptionMapper.getTotalEnergyForVM(deployment, vmid);
	}

	public double getMeasureInIntervalFromVM(Unit unit,String deployment, String vmid, long start, long end) {
		
		int samples = dataConsumptionMapper.getSamplesBetweenTime( deployment, vmid, start, end);
		
		if (samples ==0){
			logger.info("No samples available for the given interval "+start+ " to "+end+" estimating consumption from closest samples");
			long previoussampletime = dataConsumptionMapper.getSampleTimeBefore(deployment, vmid, start);
			long aftersampletime = dataConsumptionMapper.getSampleTimeAfter(deployment, vmid, end);
			if (previoussampletime == 0){
				logger.info("Not enough samples - before the event interval");
				return 0;
			}
			if (aftersampletime == 0){
				logger.info("Not enough samples - after the event interval");
				return 0;
			}
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
			double result = dataConsumptionMapper.getTotalEnergyForVMTime(deployment, vmid,start,end);
			
			logger.debug("Whole energy (Wh) is "+result);
			double diff = end - start;
			logger.debug("from "+start + " to "+end);
			diff = diff / 3600000;
			if (result>0)logger.info("Total is "+result + " over "+diff);
			return result;
			
		} else {
			double result = dataConsumptionMapper.getPowerInIntervalForVM(deployment, vmid, start, end);
			logger.info("######### Avg power is "+result);
			return result;
		}
		
		
	}
	
	public List<DataConsumption> getSamplesInInterval(String depl, String vmid, Timestamp start, Timestamp end) {
		return dataConsumptionMapper.getDataSamplesVM(depl, vmid, start.getTime(), end.getTime());
	}
	
	public List<DataConsumption> sampleMeasurements(String applicationid, String deployment, String vmid, long start,long end,long interval){

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
				as.setCpu(result.get(0).getCpu());
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
					as.setCpu(result.get(pointer).getCpu());
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
	

}
