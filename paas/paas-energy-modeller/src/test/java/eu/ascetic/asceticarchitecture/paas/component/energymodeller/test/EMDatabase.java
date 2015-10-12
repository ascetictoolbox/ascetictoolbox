/**
* Copyright 2014 Hewlett-Packard Development Company, L.P.                                         
 */
package eu.ascetic.asceticarchitecture.paas.component.energymodeller.test;

import java.sql.Timestamp;
import java.util.List;

import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import eu.ascetic.asceticarchitecture.paas.component.energymodeller.internal.common.data.database.PaaSEMDatabaseManager;
import eu.ascetic.asceticarchitecture.paas.component.energymodeller.internal.common.data.database.dao.impl.DataConsumptionDAOImpl;
import eu.ascetic.asceticarchitecture.paas.component.energymodeller.internal.common.data.database.dao.impl.DataEventDAOImpl;
import eu.ascetic.asceticarchitecture.paas.component.energymodeller.internal.common.data.database.dao.impl.EnergyModellerMonitoringDAOImpl;
import eu.ascetic.asceticarchitecture.paas.component.energymodeller.internal.common.data.database.table.DataConsumption;
import eu.ascetic.asceticarchitecture.paas.component.energymodeller.internal.common.data.database.table.DataEvent;
import eu.ascetic.asceticarchitecture.paas.component.energymodeller.internal.common.data.database.table.EnergyModellerMonitoring;

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
		Timestamp ts = Timestamp.valueOf("2014-09-27 03:23:34");
		data.setTime(ts.getTime());
		data.setVmcpu(50.5);
		data.setVmpower(100);
		data.setVmenergy(15);
		
		dataConsumptionDAO.save(data);
		Timestamp tsres = dataConsumptionDAO.getLastConsumptionForVM("test1", "vm1");
		Assert.assertEquals(ts,tsres);
		
//		List<DataConsumption> result;
//		result = dataConsumptionDAO.getByApplicationId("test1");
//		Assert.assertEquals(result.size(),1);
//		result = dataConsumptionDAO.getByDeploymentId("deployment1");
//		Assert.assertEquals(result.size(),1);
//		result = dataConsumptionDAO.getByEventId("event1");
//		Assert.assertEquals(result.size(),1);
//		result = dataConsumptionDAO.getByVMId("vm1");
//		Assert.assertEquals(result.size(),1);
//		Assert.assertEquals(result.get(0).getVmid(),"vm1");
		
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
	
//	@Test
//	public void testIaaSData() {
//		IaaSDataDAOImpl iaasdao = dbmanager.getIaasdatadao();
//		Assert.assertEquals("10106",iaasdao.getHostIdForVM("10111"));
//		Assert.assertEquals("0",iaasdao.getHostTotalCpu("10106"));
//		List<IaaSVMConsumption> list = iaasdao.getEnergyForVM("10106", "10111");
//		Assert.assertEquals(299,list.size());
//	}
//	

	
}
