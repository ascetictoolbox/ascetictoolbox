/**
* Copyright 2014 Hewlett-Packard Development Company, L.P.                                         
 */
package eu.ascetic.asceticarchitecture.paas.component.common.database;

import java.sql.Timestamp;
import java.util.List;

import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.jdbc.datasource.DriverManagerDataSource;

import eu.ascetic.asceticarchitecture.paas.component.common.dao.impl.DataConsumptionDAOImpl;
import eu.ascetic.asceticarchitecture.paas.component.common.dao.impl.DataEventDAOImpl;
import eu.ascetic.asceticarchitecture.paas.component.common.dao.impl.EnergyModellerMonitoringDAOImpl;
import eu.ascetic.asceticarchitecture.paas.component.common.dao.impl.IaaSDataDAOImpl;
import eu.ascetic.asceticarchitecture.paas.component.energymodeller.datatype.EMSettings;

public class PaaSEMDatabaseManager {

	//application id | deployment id | start time | end time | event load (cpu/ram usage) | total energy consumed | min | max | avg
	
	private DataConsumptionDAOImpl dataconsumptiondao;
	private DataEventDAOImpl dataeeventdao;
	private EnergyModellerMonitoringDAOImpl monitoringdata;
	private IaaSDataDAOImpl iaasdatadao;
	

	public boolean setup(EMSettings emSettings){
		dataconsumptiondao = new DataConsumptionDAOImpl();
		dataeeventdao = new DataEventDAOImpl();
		monitoringdata = new EnergyModellerMonitoringDAOImpl();
		iaasdatadao = new IaaSDataDAOImpl();
		DriverManagerDataSource dataSource = new DriverManagerDataSource();
   		dataSource.setDriverClassName(emSettings.getPaasdriver());
   		dataSource.setUrl(emSettings.getPaasurl());
		dataSource.setUsername(emSettings.getPaasdbuser());
		dataSource.setPassword(emSettings.getPaasdbpassword());
		DriverManagerDataSource iaasdataSource = new DriverManagerDataSource();
		iaasdataSource.setDriverClassName(emSettings.getIaasdriver());
		iaasdataSource.setUrl(emSettings.getIaasurl());
		iaasdataSource.setUsername(emSettings.getIaasdbuser());
		iaasdataSource.setPassword(emSettings.getIaasdbpassword());
		dataconsumptiondao.setDataSource(dataSource);
		dataeeventdao.setDataSource(dataSource);
		monitoringdata.setDataSource(dataSource);
		iaasdatadao.setDataSource(iaasdataSource);
		dataconsumptiondao.initialize();
		dataeeventdao.initialize();
		monitoringdata.initialize();
		iaasdatadao.initialize();
		return true;
	}
	
	public boolean setup(String contextfile){
		ApplicationContext context = new ClassPathXmlApplicationContext(contextfile);
		dataconsumptiondao = (DataConsumptionDAOImpl)context.getBean("dataConsumptionDAO");
		dataeeventdao = (DataEventDAOImpl)context.getBean("dataEventDAO");
		monitoringdata = (EnergyModellerMonitoringDAOImpl)context.getBean("emModelDAO");
		iaasdatadao = (IaaSDataDAOImpl) context.getBean("dataSourceIaaSDAO");
		
		dataconsumptiondao.initialize();
		dataeeventdao.initialize();
		monitoringdata.initialize();
		iaasdatadao.initialize();
		return true;
	}
	
	public DataConsumptionDAOImpl getDataConsumptionDAOImpl(){
		return dataconsumptiondao;
	}
	
	public DataEventDAOImpl getDataEventDAOImpl(){
		return dataeeventdao;
	}
	
	public EnergyModellerMonitoringDAOImpl getMonitoringData(){
		return monitoringdata;
	}
	
	public IaaSDataDAOImpl getIaasdatadao() {
		return iaasdatadao;
	}
	
	public void getMeasurementQuery(String applicationid,String deployment, List<String> vms, Timestamp start, Timestamp end){
		
	}

	
}
