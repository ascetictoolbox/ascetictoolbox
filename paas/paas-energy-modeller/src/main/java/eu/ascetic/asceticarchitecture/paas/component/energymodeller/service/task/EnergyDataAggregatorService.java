package eu.ascetic.asceticarchitecture.paas.component.energymodeller.service.task;

import java.sql.Timestamp;

import org.apache.log4j.Logger;

import eu.ascetic.asceticarchitecture.paas.component.common.dao.impl.DataConsumptionDAOImpl;
import eu.ascetic.asceticarchitecture.paas.component.energymodeller.interfaces.DataAggregatorTaskInterface;

public class EnergyDataAggregatorService implements DataAggregatorTaskInterface {

	private DataConsumptionDAOImpl dataDAO;
	private static final Logger logger = Logger.getLogger(EnergyDataAggregatorService.class);
	
	@Override
	public double getTotal(String app, String depl, String event) {
		double result = dataDAO.getTotalEnergyForDeployment(app, depl);
		logger.info("Total is "+result);
		return result;
	}

	@Override
	public double getTotal(String app, String depl, String vmid,String event) {
		double result = dataDAO.getTotalEnergyForVM(app, depl, vmid);
		logger.info("Total is "+result);
		return result;
	}

	@Override
	public double getTotalAtTime(String app, String depl, String event,	Timestamp time) {
		// TODO Auto-generated method stub
		return 1;
	}

	@Override
	public double getTotalAtTime(String app, String depl, String vmid, String event, Timestamp time) {
		// TODO Auto-generated method stub
		return 1;
	}

	public void setDataDAO(DataConsumptionDAOImpl dataDAO) {
		this.dataDAO = dataDAO;
	}



}
