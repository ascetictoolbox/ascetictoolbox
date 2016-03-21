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
package eu.ascetic.asceticarchitecture.paas.component.energymodeller.test;

import java.util.Date;

import org.junit.BeforeClass;
import org.junit.Test;

import eu.ascetic.asceticarchitecture.paas.component.energymodeller.internal.common.data.database.table.DataConsumption;
import eu.ascetic.asceticarchitecture.paas.component.energymodeller.internal.common.data.ibatis.DataConsumptionHandler;
import eu.ascetic.asceticarchitecture.paas.component.energymodeller.internal.common.data.ibatis.mapper.DataConsumptionMapper;

public class DataConsumptionData {

	// M. Fontanella - 08 Feb 2016 - begin
	private static int MILLISEC=1000;
	// M. Fontanella - 08 Feb 2016 - end
	private static DataConsumptionHandler manager;
	private static DataConsumptionMapper mapper;
	
	@BeforeClass
	public static void setup() {
		// M. Fontanella - 05 Feb 2016 - begin
		manager = DataConsumptionHandler.getHandler("com.mysql.jdbc.Driver","jdbc:mysql://192.168.0.8:3306/ascetic_paas_em","root","root");
		// M. Fontanella - 05 Feb 2016 - end
		mapper = manager.getSession().getMapper(DataConsumptionMapper.class);
	}
	
	
	@Test
	public void testCreate() {
		
		DataConsumption dc = new DataConsumption();
		// M. Fontanella - 20 Jan 2016 - begin
		dc.setProviderid("00000");
		// M. Fontanella - 20 Jan 2016 - end
		// M. Fontanella - 05 Feb 2016 - begin
		dc.setApplicationid("app2");
		dc.setVmcpu(0.6);
		dc.setDeploymentid("2");
		dc.setVmenergy(50);
		dc.setVmpower(5);
		dc.setVmid("iaas2");
		dc.setMetrictype("power");
		// M. Fontanella - 05 Feb 2016 - end
		// M. Fontanella - 08 Feb 2016 - begin
		long init = new Date().getTime() / MILLISEC;
		dc.setTime(new Date().getTime() / MILLISEC);
		// M. Fontanella - 08 Feb 2016 - end
		mapper.createMeasurement(dc);
		
		dc = new DataConsumption();
		// M. Fontanella - 20 Jan 2016 - begin
		dc.setProviderid("00000");
		// M. Fontanella - 20 Jan 2016 - end
		// M. Fontanella - 05 Feb 2016 - begin
		dc.setApplicationid("app2");
		dc.setVmcpu(0.6);
		dc.setDeploymentid("2");
		dc.setVmenergy(55);
		dc.setVmpower(5);
		dc.setVmid("iaas2");
		dc.setMetrictype("power");
		// M. Fontanella - 05 Feb 2016 - end
		// M. Fontanella - 08 Feb 2016 - begin
		init = new Date().getTime() / MILLISEC;
		dc.setTime(new Date().getTime() / MILLISEC);
		// M. Fontanella - 08 Feb 2016 - end
		mapper.createMeasurement(dc);		
		
		
		dc = new DataConsumption();
		// M. Fontanella - 20 Jan 2016 - begin
		dc.setProviderid("00000");
		// M. Fontanella - 20 Jan 2016 - end
		// M. Fontanella - 05 Feb 2016 - begin
		dc.setApplicationid("app2");
		dc.setVmcpu(0.6);
		dc.setDeploymentid("2");
		dc.setVmenergy(60);
		dc.setVmpower(15);
		dc.setVmid("iaas2");
		dc.setMetrictype("power");
		// M. Fontanella - 05 Feb 2016 - end
		// M. Fontanella - 08 Feb 2016 - begin
		init = new Date().getTime() / MILLISEC;
		dc.setTime(new Date().getTime() / MILLISEC);
		// M. Fontanella - 08 Feb 2016 - end
		mapper.createMeasurement(dc);		
		
		// M. Fontanella - 05 Feb 2016 - begin
		System.out.println(mapper.getLastConsumptionForVM("2", "iaas2"));
		
		System.out.println(mapper.getTotalEnergyForVM("2", "iaas2"));
		
		//	System.out.println(mapper.getPowerInIntervalForVM("2", "iaas2", init, end));
		
		System.out.println(mapper.getSampleTimeAfter("2", "iaas2", init));
		
		//	System.out.println(mapper.getSampleTimeBefore("2", "iaas2",end));
		
		//	System.out.println(mapper.getSamplesBetweenTime("2", "iaas2", init, end));
		
		//	System.out.println(mapper.getDataSamplesVM("2", "iaas2", init, end));
		
		System.out.println(mapper.selectByApp("app2"));
		
		System.out.println(mapper.selectByDeploy("2"));
		
		System.out.println(mapper.selectByVm("2", "iaas2"));
		
		System.out.println(mapper.getSampleAtTime("2", "iaas2", init));
		// M. Fontanella - 05 Feb 2016 - end
	}
	
	
	
}
