/**
* Copyright 2014 Hewlett-Packard Development Company, L.P.                                         
 */
package eu.ascetic.asceticarchitecture.paas.component.energymodeller.internal.common.data.database;

import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.jdbc.datasource.DriverManagerDataSource;

import eu.ascetic.asceticarchitecture.paas.component.energymodeller.internal.common.data.database.dao.EMSettings;
import eu.ascetic.asceticarchitecture.paas.component.energymodeller.internal.common.data.database.dao.impl.DataConsumptionDAOImpl;
import eu.ascetic.asceticarchitecture.paas.component.energymodeller.internal.common.data.database.dao.impl.DataEventDAOImpl;
import eu.ascetic.asceticarchitecture.paas.component.energymodeller.internal.common.data.database.dao.impl.EnergyModellerMonitoringDAOImpl;
import eu.ascetic.asceticarchitecture.paas.component.energymodeller.internal.common.dataservice.EventDataService;

public class PaaSEMDatabaseManager {

	//application id | deployment id | start time | end time | event load (cpu/ram usage) | total energy consumed | min | max | avg
	
	private DataConsumptionDAOImpl dataconsumptiondao;
	private EnergyModellerMonitoringDAOImpl monitoringdata;
	

	public boolean setup(EMSettings emSettings){
		dataconsumptiondao = new DataConsumptionDAOImpl();
		monitoringdata = new EnergyModellerMonitoringDAOImpl();
		DriverManagerDataSource dataSource = new DriverManagerDataSource();
   		dataSource.setDriverClassName(emSettings.getPaasdriver());
   		dataSource.setUrl(emSettings.getPaasurl());
		dataSource.setUsername(emSettings.getPaasdbuser());
		dataSource.setPassword(emSettings.getPaasdbpassword());
		dataconsumptiondao.setDataSource(dataSource);
		monitoringdata.setDataSource(dataSource);
		dataconsumptiondao.initialize();
		monitoringdata.initialize();
		return true;
	}
	
	public boolean setup(String contextfile){
		ApplicationContext context = new ClassPathXmlApplicationContext(contextfile);
		dataconsumptiondao = (DataConsumptionDAOImpl)context.getBean("dataConsumptionDAO");
		monitoringdata = (EnergyModellerMonitoringDAOImpl)context.getBean("emModelDAO");
		dataconsumptiondao.initialize();
		monitoringdata.initialize();
		return true;
	}
	
	public DataConsumptionDAOImpl getDataConsumptionDAOImpl(){
		return dataconsumptiondao;
	}
	
	
	public EnergyModellerMonitoringDAOImpl getMonitoringData(){
		return monitoringdata;
	}
	
	
}
