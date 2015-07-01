/**
* Copyright 2014 Hewlett-Packard Development Company, L.P.                                         
 */
package eu.ascetic.asceticarchitecture.paas.component.energymodeller.internal.common.dataservice;

import java.sql.Timestamp;
import java.util.List;
import java.util.Vector;

import org.apache.log4j.Logger;

import eu.ascetic.asceticarchitecture.paas.component.energymodeller.datatype.ApplicationSample;
import eu.ascetic.asceticarchitecture.paas.component.energymodeller.datatype.Unit;
import eu.ascetic.asceticarchitecture.paas.component.energymodeller.internal.common.data.database.dao.impl.DataConsumptionDAOImpl;

public class EnergyDataAggregatorService {

	private DataConsumptionDAOImpl dataDAO;
	private static final Logger logger = Logger.getLogger(EnergyDataAggregatorService.class);
	
	public void setDataDAO(DataConsumptionDAOImpl dataDAO) {
		this.dataDAO = dataDAO;
	}
	

	public double getEnergyFromVM(String app, String depl, String vmid, String event) {
		return dataDAO.getTotalEnergyForVM(app, depl, vmid);
	}

	public double getMeasureInIntervalFromVM(Unit unit,String app, String vmid, long start, long end) {
		
		int samples = dataDAO.getSamplesBetweenTime( app, vmid, start, end);
		
		if (samples ==0){
			logger.info("No samples available for the given interval "+start+ " to "+end+" estimating consumption from closest samples");
			long previoussampletime = dataDAO.getSampleTimeBefore(app, vmid, start);
			long aftersampletime = dataDAO.getSampleTimeAfter(app, vmid, end);
			if (previoussampletime == 0){
				logger.info("Not enough samples - before the event interval");
				return 0;
			}
			if (aftersampletime == 0){
				logger.info("Not enough samples - after the event interval");
				return 0;
			}
			ApplicationSample esfirst = dataDAO.getSampleAtTime(app, vmid, previoussampletime);
			ApplicationSample eslast = dataDAO.getSampleAtTime(app, vmid, aftersampletime);
			
			logger.info("The lower bound at "+esfirst.getTime()+" value "+esfirst.getP_value());
			logger.info("The upper bound at "+eslast.getTime()+" value "+eslast.getP_value());
			
			if (unit == Unit.ENERGY){
				double avgpower = (esfirst.getP_value()+eslast.getP_value())/2;
				double energy = avgpower * ((end-start))/3600000;
				// TODO refine better this value 
				logger.info("This interval has consumed energy (Wh) "+energy);
				return energy;
			}else {
				double avgpower = (esfirst.getP_value()+eslast.getP_value())/2;
				logger.info("In this interval the average power (W) is "+avgpower);
				return avgpower;
			}
		} else{
			logger.info("Samples available for the given interval " + samples);
			if (samples ==1){
				logger.debug("Only one sample available for the given interval "+start+ " to "+end+" estimating consumption from available samples");
				double avgpower = dataDAO.getPowerInIntervalForVM(app, vmid, new Timestamp(start),  new Timestamp(end));
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
			double result = dataDAO.getTotalEnergyForVMTime(app, vmid,new Timestamp(start),new Timestamp(end));
			
			logger.debug("Whole energy (Wh) is "+result);
			double diff = end - start;
			logger.debug("from "+start + " to "+end);
			diff = diff / 3600000;
			if (result>0)logger.info("Total is "+result + " over "+diff);
			return result;
			
		} else {
			double result = dataDAO.getPowerInIntervalForVM(app, vmid, new Timestamp(start),  new Timestamp(end));
			logger.info("######### Avg power is "+result);
			return result;
		}
		
		
	}
	
	public List<ApplicationSample> getSamplesInInterval(String app, String depl, String vmid, Timestamp start, Timestamp end) {
		return dataDAO.getDataSamplesVM(app, depl, vmid, start.getTime(), end.getTime());
	}
	
	public List<ApplicationSample> sampleMeasurements(String applicationid, String vmid, long start,long end,long interval){

		List<ApplicationSample>  result = dataDAO.getDataSamplesVM(applicationid, "", vmid, start, end);
		List<ApplicationSample> resampledresult = new Vector<ApplicationSample>(); 
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
				ApplicationSample as = new ApplicationSample();
				as.setC_value(result.get(0).getC_value());
				as.setP_value(result.get(0).getP_value());
				as.setE_value(0);
				as.setTime(start);
				as.setVmid(vmid);
				as.setAppid(applicationid);
				last_ts=currenttime;
				last_powerval=result.get(0).getP_value();
				iteration++;
				resampledresult.add(as);
			}else { 
				ApplicationSample as = new ApplicationSample();
				
				while((result.get(pointer).getTime()<currenttime)){
					pointer++;	
					if (pointer==result.size())break;
				}
				if (pointer<result.size()){
					as.setC_value(result.get(pointer).getC_value());
					as.setP_value(result.get(pointer).getP_value());
					
					as.setTime(currenttime);
					as.setVmid(vmid);
					as.setAppid(applicationid);
					double part_energy = ((last_powerval+result.get(pointer).getP_value())/2)*(currenttime-last_ts)/3600000;
					aggr_energy = aggr_energy +part_energy;
					as.setE_value(aggr_energy);
					last_ts=currenttime;
					last_powerval=result.get(pointer).getP_value();
					iteration++;
					resampledresult.add(as);
				}
			}
			currenttime = currenttime + interval*1000;
			
		}
		return resampledresult;
	}
	

}
