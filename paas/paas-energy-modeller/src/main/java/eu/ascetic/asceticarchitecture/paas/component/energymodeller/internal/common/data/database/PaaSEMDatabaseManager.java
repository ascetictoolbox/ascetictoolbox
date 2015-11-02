/**
   Copyright 2014-2015 Hewlett-Packard Development Company, L.P.  
   Licensed under the Apache License, Version 2.0 (the "License");
   you may not use this file except in compliance with the License.
   You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

   Unless required by applicable law or agreed to in writing, software
   distributed under the License is distributed on an "AS IS" BASIS,
   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   See the License for the specific language governing permissions and
   limitations under the License.

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
