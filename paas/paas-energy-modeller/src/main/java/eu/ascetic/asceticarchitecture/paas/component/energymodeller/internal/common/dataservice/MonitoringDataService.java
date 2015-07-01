package eu.ascetic.asceticarchitecture.paas.component.energymodeller.internal.common.dataservice;

import eu.ascetic.asceticarchitecture.paas.component.energymodeller.internal.common.data.database.dao.impl.EnergyModellerMonitoringDAOImpl;

public class MonitoringDataService {

	private EnergyModellerMonitoringDAOImpl dataDao;
	
	public void setDataDAO(EnergyModellerMonitoringDAOImpl dataDao) {
		this.dataDao = dataDao;
	}
	
	public void startMonitoring(String applicationid,String deploymentid,String eventid){
		dataDao.createMonitoring(applicationid, deploymentid, eventid);
	}
	
	public void stopMonitoring(String applicationid,String deploymentid){
		dataDao.terminateMonitoring(applicationid, deploymentid);
	}
	

	
}
