package eu.ascetic.asceticarchitecture.paas.component.energymodeller.test;

import java.sql.Timestamp;
import java.util.List;

import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import eu.ascetic.asceticarchitecture.paas.component.common.dao.impl.DataConsumptionDAOImpl;
import eu.ascetic.asceticarchitecture.paas.component.common.dao.impl.DataEventDAOImpl;
import eu.ascetic.asceticarchitecture.paas.component.common.dao.impl.EnergyModellerMonitoringDAOImpl;
import eu.ascetic.asceticarchitecture.paas.component.common.dao.impl.IaaSDataDAOImpl;
import eu.ascetic.asceticarchitecture.paas.component.common.database.PaaSEMDatabaseManager;
import eu.ascetic.asceticarchitecture.paas.component.common.model.DataConsumption;
import eu.ascetic.asceticarchitecture.paas.component.common.model.DataEvent;
import eu.ascetic.asceticarchitecture.paas.component.common.model.EnergyModellerMonitoring;
import eu.ascetic.asceticarchitecture.paas.component.common.model.IaaSVMConsumption;

public class EMDatabase {
	private static PaaSEMDatabaseManager dbmanager;
	
	@BeforeClass
	public static void setup() {
		// TODO in future mock the sub component of EM PaaS
		dbmanager = new PaaSEMDatabaseManager();
		dbmanager.setup("springtest.xml");
	}
	
	
	@Test
	public void testDataConsumption() {
		DataConsumptionDAOImpl dataConsumptionDAO= dbmanager.getDataConsumptionDAOImpl();
		DataConsumption data=new DataConsumption();
		data.setApplicationid("test1");
		data.setEventid("event1");
		data.setDeploymentid("deployment1");
		data.setVmid("vm1");
		Timestamp ts = Timestamp.valueOf("2014-09-27 03:23:34.234");
		data.setStarttime(ts);
		ts = Timestamp.valueOf("2014-09-27 03:23:36.234");
		data.setEndtime(ts);
		data.setCpu(50.5);
		data.setMemory(1024);
		data.setNetwork(100);
		data.setDisk(50);
		dataConsumptionDAO.save(data);
		
		
		List<DataConsumption> result;
		result = dataConsumptionDAO.getByApplicationId("test1");
		Assert.assertEquals(result.size(),1);
		result = dataConsumptionDAO.getByDeploymentId("deployment1");
		Assert.assertEquals(result.size(),1);
		result = dataConsumptionDAO.getByEventId("event1");
		Assert.assertEquals(result.size(),1);
		result = dataConsumptionDAO.getByVMId("vm1");
		Assert.assertEquals(result.size(),1);
		Assert.assertEquals(result.get(0).getVmid(),"vm1");
	}
	
	@Test
	public void testDataEvent() {
		DataEventDAOImpl dataEventDAO = dbmanager.getDataEventDAOImpl();
		DataEvent data = new DataEvent();
		data.setApplicationid("test1");
		data.setDeploymentid("deployment1");
		data.setEnergy(100.5);
		Timestamp ts = Timestamp.valueOf("2014-09-27 03:23:34.234");
		data.setTime(ts);
		data.setVmid("vm1");
		dataEventDAO.save(data);
		List<DataEvent> result = dataEventDAO.getByApplicationId("test1");
		Assert.assertEquals(result.size(),1);
		result = dataEventDAO.getByDeploymentId("deployment1");
		Assert.assertEquals(result.size(),1);
		result = dataEventDAO.getByVMId("vm1");
		Assert.assertEquals(result.size(),1);
		
	}
	
	@Test
	public void testMonitoringData() {
		EnergyModellerMonitoringDAOImpl energymonitoringDao = dbmanager.getMonitoringData();
		
		energymonitoringDao.createMonitoring("test1", "deployment1", "");
		energymonitoringDao.createTraining("test1", "deployment1", "");
		List<EnergyModellerMonitoring> result = energymonitoringDao.getByDeploymentId("test1", "deployment1");
		Assert.assertEquals(2,result.size());
		result = energymonitoringDao.getMonitoringActive();
		Assert.assertEquals(1,result.size());
		result = energymonitoringDao.getTrainingActive();
		Assert.assertEquals(1,result.size());
		energymonitoringDao.terminateMonitoring("test1", "deployment1");
		energymonitoringDao.terminateTraining("test1", "deployment1");
		result = energymonitoringDao.getByDeploymentId("test1", "deployment1");
		Assert.assertEquals(2,result.size());
		result = energymonitoringDao.getMonitoringActive();
		Assert.assertEquals(0,result.size());
		result = energymonitoringDao.getTrainingActive();
		Assert.assertEquals(0,result.size());

		
	}
	
	@Test
	public void testIaaSData() {
		IaaSDataDAOImpl iaasdao = dbmanager.getIaasdatadao();
		Assert.assertEquals("0",iaasdao.getHostIdForVM("1"));
		Assert.assertEquals("100",iaasdao.getHostTotalCpu("0"));
		List<IaaSVMConsumption> list = iaasdao.getEnergyForVM("0", "1");
		Assert.assertEquals(2,list.size());
	}
	

	
}
