/**
* Copyright 2014 Hewlett-Packard Development Company, L.P.                                         
 */
package eu.ascetic.asceticarchitecture.paas.component.energymodeller.service.task;

import java.sql.Timestamp;
import java.util.List;

import org.apache.log4j.Logger;

import eu.ascetic.asceticarchitecture.paas.component.common.dao.impl.DataConsumptionDAOImpl;
import eu.ascetic.asceticarchitecture.paas.component.energymodeller.datatype.EnergySample;

public class EnergyDataAggregatorService {

	private DataConsumptionDAOImpl dataDAO;
	private static final Logger logger = Logger.getLogger(EnergyDataAggregatorService.class);
	
	public void setDataDAO(DataConsumptionDAOImpl dataDAO) {
		this.dataDAO = dataDAO;
	}
	

	public double getTotal(String app, String depl, String vmid,String event) {
		double result = dataDAO.getTotalEnergyForVM(app, depl, vmid);
		return result;
	}


	public double getAverage(String app, String depl, String vmid, String event) {
		double result = dataDAO.getTotalEnergyForVM(app, depl, vmid);
		return result;
	}

	public double getAverageInInterval(String app, String vmid,String event, long start, long end) {
		
		int samples = dataDAO.getSamplesBetweenTime( app,vmid,start, end);
		
		if (samples ==0){
			logger.info("No samples available for the given interval "+start+ " to "+end+" estimating consumption from available samples");
			long previoussampletime = dataDAO.getSampleTimeBefore(app, vmid, start);
			long aftersampletime = dataDAO.getSampleTimeAfter(app, vmid, end);
			if (previoussampletime == 0){
				logger.info("No samples before this interval");
				return 0;
			}
			if (aftersampletime == 0){
				logger.info("No samples after this interval");
				return 0;
			}
			EnergySample esfirst = dataDAO.getSampleAtTime(app, vmid, previoussampletime);
			EnergySample eslast = dataDAO.getSampleAtTime(app, vmid, previoussampletime);
			
			logger.info("The lower bound at "+esfirst.getTimestampBeging()+" value "+esfirst.getP_value());
			logger.info("The upper bound at "+eslast.getTimestampBeging()+" value "+eslast.getP_value());
			double avgpower = (esfirst.getP_value()+eslast.getP_value())/2;
			double energy = avgpower * ((end-start))/3600000;
			
			logger.info("This interval has consumed energy Wh "+energy);
			return energy;
			
		} else{
			logger.info("Samples available for the given interval " + samples);
			if (samples ==1){
				logger.info("Only one sample available for the given interval "+start+ " to "+end+" estimating consumption from available samples");
				double avgpower = dataDAO.getPowerInIntervalForVM(app, vmid, new Timestamp(start),  new Timestamp(end));
				logger.info("Power is "+avgpower);
				
				
				double energy = avgpower * ((end-start))/3600000;
				
				logger.info("This interval has consumed energy Wh "+energy);
				return energy;
				
				
			}
			
			
		}
		
		double result = dataDAO.getTotalEnergyForVMTime(app, vmid,new Timestamp(start),new Timestamp(end));
		

		
		logger.debug("Total is "+result);
		double diff = end -start;
		logger.debug("from "+start + " to "+end);
		diff = diff / 3600000;
		if (result>0)logger.info("Total is "+result + " over "+diff);
		return result;
		
	}
	public double getAvgPower(String app, String vmid,String event, long start, long end) {
		
		logger.info("from "+start + " to "+end);
		double result = dataDAO.getPowerInIntervalForVM(app, vmid, new Timestamp(start),  new Timestamp(end));
		logger.info("######### Avg power is "+result);
		return result;
	}
	

	
	public List<EnergySample> getSamplesInInterval(String app, String depl, String vmid, Timestamp start, Timestamp end) {
		return dataDAO.getDataSamplesVM(app, depl, vmid, start.getTime(), end.getTime());
	}



}
