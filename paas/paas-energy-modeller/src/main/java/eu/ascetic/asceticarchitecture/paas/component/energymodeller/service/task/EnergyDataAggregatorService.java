/**
* Copyright 2014 Hewlett-Packard Development Company, L.P.                                         
 */
package eu.ascetic.asceticarchitecture.paas.component.energymodeller.service.task;

import java.sql.Timestamp;
import java.util.List;

import org.apache.log4j.Logger;

import eu.ascetic.asceticarchitecture.paas.component.common.dao.impl.DataConsumptionDAOImpl;
import eu.ascetic.asceticarchitecture.paas.component.energymodeller.datatype.EnergySample;
import eu.ascetic.asceticarchitecture.paas.component.energymodeller.interfaces.DataAggregatorTaskInterface;

public class EnergyDataAggregatorService implements DataAggregatorTaskInterface {

	private DataConsumptionDAOImpl dataDAO;
	private static final Logger logger = Logger.getLogger(EnergyDataAggregatorService.class);
	
	public void setDataDAO(DataConsumptionDAOImpl dataDAO) {
		this.dataDAO = dataDAO;
	}
	

	@Override
	public double getTotal(String app, String depl, String vmid,String event) {
		double result = dataDAO.getTotalEnergyForVM(app, depl, vmid);
		return result;
	}


	@Override
	public double getAverage(String app, String depl, String vmid, String event) {
		double result = dataDAO.getTotalEnergyForVM(app, depl, vmid);
//		Timestamp min = dataDAO.getFirsttConsumptionForVM(app, vmid);
//		Timestamp max= dataDAO.getLastConsumptionForVM(app, vmid);
//		if (min == null){
//			return -1;
//		}
//		if (max == null){
//			return -1;
//		}
		//double diff = max.getTime()-min.getTime();
		//diff = diff / 3600000;
		
		//if (result>0)logger.info("Total is "+result + " over "+diff);
		//logger.info("Average is "+result );
		//if (diff==0)return 0;
		//return result/diff;
		return result;
	}

	@Override
	public double getAverageInInterval(String app, String vmid,String event, long start, long end) {
		double result = dataDAO.getTotalEnergyForVMTime(app, vmid,new Timestamp(start),new Timestamp(end));
		
		logger.debug("Total is "+result);
		//logger.info("Per hour is "+result );
		//double diff = end -start;
		//logger.debug("from "+start + " to "+end);
		//diff = diff / 3600000;
		//if (result>0)logger.info("Total is "+result + " over "+diff);
		//if (diff==0)return 0;
		//return result/diff;
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
