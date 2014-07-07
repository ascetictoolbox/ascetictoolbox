package eu.ascetic.asceticarchitecture.paas.component.common.database;

import java.sql.Timestamp;
import java.util.List;

import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import eu.ascetic.asceticarchitecture.paas.component.common.dao.impl.DataConsumptionDAOImpl;
import eu.ascetic.asceticarchitecture.paas.component.common.dao.impl.DataEventDAOImpl;
import eu.ascetic.asceticarchitecture.paas.component.common.dao.impl.EnergyModellerMonitoringDAOImpl;
import eu.ascetic.asceticarchitecture.paas.component.common.dao.impl.EnergyModellerTrainingDAOImpl;

public class PaaSEMDatabaseManager {

	//application id | deployment id | start time | end time | event load (cpu/ram usage) | total energy consumed | min | max | avg
	
	private DataConsumptionDAOImpl dataconsumptiondao;
	private DataEventDAOImpl dataeeventdao;
	private EnergyModellerMonitoringDAOImpl monitoringdata;
	private EnergyModellerTrainingDAOImpl trainingadata;
	
	public boolean setup(){
		ApplicationContext context = new ClassPathXmlApplicationContext("spring.xml");	
		dataconsumptiondao = (DataConsumptionDAOImpl)context.getBean("dataConsumptionDAO");
		dataeeventdao = (DataEventDAOImpl)context.getBean("dataEventDAO");
		monitoringdata = (EnergyModellerMonitoringDAOImpl)context.getBean("emModelDAO");
		trainingadata = (EnergyModellerTrainingDAOImpl)context.getBean("emTrainingDAO");
		dataconsumptiondao.initialize();
		dataeeventdao.initialize();
		monitoringdata.initialize();
		trainingadata.initialize();
		return true;
	}
	
	public boolean setup(String contextfile){
		ApplicationContext context = new ClassPathXmlApplicationContext(contextfile);
		dataconsumptiondao = (DataConsumptionDAOImpl)context.getBean("dataConsumptionDAO");
		dataeeventdao = (DataEventDAOImpl)context.getBean("dataEventDAO");
		monitoringdata = (EnergyModellerMonitoringDAOImpl)context.getBean("emModelDAO");
		trainingadata = (EnergyModellerTrainingDAOImpl)context.getBean("emTrainingDAO");
		dataconsumptiondao.initialize();
		dataeeventdao.initialize();
		monitoringdata.initialize();
		trainingadata.initialize();
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
	
	public EnergyModellerTrainingDAOImpl getTrainingData(){
		return trainingadata;
	}
	
	public void getMeasurementQuery(String applicationid,String deployment, List<String> vms, Timestamp start, Timestamp end){
		
	}

	
}
