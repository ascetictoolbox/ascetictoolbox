package eu.ascetic.asceticarchitecture.paas.component.energymodeller.test;

import java.sql.Timestamp;
import java.util.List;

import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import eu.ascetic.asceticarchitecture.paas.component.common.dao.impl.DataConsumptionDAOImpl;
import eu.ascetic.asceticarchitecture.paas.component.common.dao.impl.DataEventDAOImpl;
import eu.ascetic.asceticarchitecture.paas.component.common.dao.impl.EnergyModellerMonitoringDAOImpl;
import eu.ascetic.asceticarchitecture.paas.component.common.dao.impl.EnergyModellerTrainingDAOImpl;
import eu.ascetic.asceticarchitecture.paas.component.common.database.PaaSEMDatabaseManager;
import eu.ascetic.asceticarchitecture.paas.component.common.model.DataConsumption;
import eu.ascetic.asceticarchitecture.paas.component.common.model.DataEvent;
import eu.ascetic.asceticarchitecture.paas.component.common.model.EnergyModellerMonitoring;
import eu.ascetic.asceticarchitecture.paas.component.common.model.EnergyModellerTraining;

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
		EnergyModellerMonitoring emmonitoring = new EnergyModellerMonitoring();
		emmonitoring.setApplicationid("test1");
		emmonitoring.setDeploymentid("deployment1");
		emmonitoring.setStatus(true);
		Timestamp ts = Timestamp.valueOf("2014-09-27 03:23:34.234");
		emmonitoring.setStarted(ts);
		energymonitoringDao.save(emmonitoring);
		List<EnergyModellerMonitoring> result = energymonitoringDao.getByDeploymentId("test1", "deployment1");
		Assert.assertEquals(result.size(),1);
		energymonitoringDao.terminateModel("test1", "deployment1");
		result = energymonitoringDao.getByDeploymentId("test1", "deployment1");
		Assert.assertEquals(result.size(),1);
		
	}
	
	@Test
	public void testTrainingData() {
		EnergyModellerTrainingDAOImpl energytraining= dbmanager.getTrainingData();
		EnergyModellerTraining emtraining = new EnergyModellerTraining();
		emtraining.setApplicationid("test1");
		emtraining.setDeploymentid("deployment1");
		emtraining.setEvents("*");
		Timestamp ts = Timestamp.valueOf("2014-09-27 03:23:34.234");
		emtraining.setStarted(ts);
		emtraining.setStatus(true);
		energytraining.save(emtraining);
		
		List<EnergyModellerTraining> result = energytraining.getByDeploymentId("test1","deployment1");
		Assert.assertEquals(result.size(),1);
		result = energytraining.getByStatus();
		Assert.assertEquals(result.size(),1);
		
		energytraining.terminateTraining("test1", "deployment1");
		result = energytraining.getByStatus();
		Assert.assertEquals(result.size(),0);
	}
	
	
	
}
