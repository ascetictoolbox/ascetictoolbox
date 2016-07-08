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

	private static int MILLISEC=1000;
	private static DataConsumptionHandler manager;
	private static DataConsumptionMapper mapper;
	
	@BeforeClass
	public static void setup() {

		manager = DataConsumptionHandler.getHandler("com.mysql.jdbc.Driver","jdbc:mysql://192.168.0.8:3306/ascetic_paas_em","root","root");
		mapper = manager.getSession().getMapper(DataConsumptionMapper.class);
	}
	
	
	@Test
	public void testCreate() {
		
		DataConsumption dc = new DataConsumption();
		dc.setProviderid("00000");
		dc.setApplicationid("app2");
		dc.setVmcpu(0.6);
		dc.setDeploymentid("2");
		dc.setVmenergy(50);
		dc.setVmpower(5);
		dc.setVmid("iaas2");
		dc.setMetrictype("power");
		long init = new Date().getTime() / MILLISEC;
		dc.setTime(new Date().getTime() / MILLISEC);
		mapper.createMeasurement(dc);
		
		dc = new DataConsumption();
		dc.setProviderid("00000");
		dc.setApplicationid("app2");
		dc.setVmcpu(0.6);
		dc.setDeploymentid("2");
		dc.setVmenergy(55);
		dc.setVmpower(5);
		dc.setVmid("iaas2");
		dc.setMetrictype("power");
		init = new Date().getTime() / MILLISEC;
		dc.setTime(new Date().getTime() / MILLISEC);
		mapper.createMeasurement(dc);		
		
		
		dc = new DataConsumption();
		dc.setProviderid("00000");
		dc.setApplicationid("app2");
		dc.setVmcpu(0.6);
		dc.setDeploymentid("2");
		dc.setVmenergy(60);
		dc.setVmpower(15);
		dc.setVmid("iaas2");
		dc.setMetrictype("power");
		init = new Date().getTime() / MILLISEC;
		dc.setTime(new Date().getTime() / MILLISEC);
		mapper.createMeasurement(dc);		
		
		// if (in EMSettings) enablePowerFromIass="true" use "getTotalEnergyForVM"
		// else use "getLastConsumptionForVMVirtualPower"
		System.out.println(mapper.getLastConsumptionForVM("00000","2", "iaas2"));
		// System.out.println(mapper.getLastConsumptionForVMVirtualPower("00000","2", "iaas2"));
		
		// if (in EMSettings) enablePowerFromIass="true" use "getTotalEnergyForVM"
		// else use "getTotalEnergyForVMVirtualPower"
		System.out.println(mapper.getTotalEnergyForVM("00000","2", "iaas2"));
		// System.out.println(mapper.getTotalEnergyForVMVirtualPower("00000","2", "iaas2"));
		
		//	System.out.println(mapper.getPowerInIntervalForVM("2", "iaas2", init, end));
		
		// if (in EMSettings) enablePowerFromIass="true" use "getSampleTimeAfter"
		// else use "getSampleTimeAfterVirtualPower"
		System.out.println(mapper.getSampleTimeAfter("00000","2", "iaas2", init));
		// System.out.println(mapper.getSampleTimeAfterVirtualPower("00000","2", "iaas2", init));
		
		//	System.out.println(mapper.getSampleTimeBefore("2", "iaas2",end));
		
		//	System.out.println(mapper.getSamplesBetweenTime("2", "iaas2", init, end));
		
		//	System.out.println(mapper.getDataSamplesVM("2", "iaas2", init, end));
		
		System.out.println(mapper.selectByApp("app2"));
		
		System.out.println(mapper.selectByDeploy("2"));
		
		// if (in EMSettings) enablePowerFromIass="true" use "selectByVm"
		// else use "selectByVmVirtualPower"
		System.out.println(mapper.selectByVm("00000", "2", "iaas2"));		
		// System.out.println(mapper.selectByVmVirtualPower("00000", "2", "iaas2"));
				
		// if (in EMSettings) enablePowerFromIass="true" use "getSampleAtTime"
		// else use "getSampleAtTimeVirtualPower"
		System.out.println(mapper.getSampleAtTime("00000", "2", "iaas2", init));
		// System.out.println(mapper.getSampleAtTimeVirtualPower("00000", "2", "iaas2", init));
	}	
	
}
