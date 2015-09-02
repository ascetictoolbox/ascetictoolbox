package eu.ascetic.asceticarchitecture.paas.component.energymodeller.test;

import java.util.Date;

import org.junit.BeforeClass;
import org.junit.Test;

import eu.ascetic.asceticarchitecture.paas.component.energymodeller.internal.common.data.database.table.DataConsumption;
import eu.ascetic.asceticarchitecture.paas.component.energymodeller.internal.common.data.ibatis.DataConsumptionHandler;
import eu.ascetic.asceticarchitecture.paas.component.energymodeller.internal.common.data.ibatis.mapper.DataConsumptionMapper;

public class DataConsumptionData {

	private static DataConsumptionHandler manager;
	private static DataConsumptionMapper mapper;
	
	@BeforeClass
	public static void setup() {
		manager = DataConsumptionHandler.getHandler("com.mysql.jdbc.Driver","jdbc:mysql://10.15.5.55:3306/ascetic_paas_em","root","root");
		mapper = manager.getMapper();
	}
	
	
	@Test
	public void testCreate() {
		DataConsumption dc = new DataConsumption();
		dc.setApplicationid("123");
		dc.setCpu(0.6);
		dc.setDeploymentid("345");
		dc.setVmenergy(50);
		dc.setVmpower(5);
		dc.setVmid("789");
		long init = new Date().getTime();
		dc.setTime(new Date().getTime());
		mapper.createMeasurement(dc);
		
		dc = new DataConsumption();
		dc.setApplicationid("123");
		dc.setCpu(15);
		dc.setDeploymentid("345");
		dc.setVmenergy(55);
		dc.setVmpower(5);
		dc.setVmid("789");
		dc.setTime(new Date().getTime());
		mapper.createMeasurement(dc);
		
		dc = new DataConsumption();
		dc.setApplicationid("123");
		dc.setCpu(50);
		dc.setDeploymentid("345");
		dc.setVmenergy(60);
		dc.setVmpower(15);
		dc.setVmid("789");
		dc.setTime(new Date().getTime());
		long end = new Date().getTime();
		mapper.createMeasurement(dc);
		
		System.out.println(mapper.getLastConsumptionForVM("345", "789"));
		
		System.out.println(mapper.getTotalEnergyForVM("345", "789"));
		
		System.out.println(mapper.getPowerInIntervalForVM("345", "789", init, end));
		
		System.out.println(mapper.getSampleTimeAfter("345", "789", init));
		
		System.out.println(mapper.getSampleTimeBefore("345", "789",end));
		
		System.out.println(mapper.getSamplesBetweenTime("345", "789", init, end));
		
		System.out.println(mapper.getDataSamplesVM("345", "789", init, end));
		
		System.out.println(mapper.selectByApp("123"));
		
		System.out.println(mapper.selectByDeploy("345"));
		
		System.out.println(mapper.selectByVm("345","789"));
		
		System.out.println(mapper.getSampleAtTime("345", "789", init));
	}
	
	
	
}
