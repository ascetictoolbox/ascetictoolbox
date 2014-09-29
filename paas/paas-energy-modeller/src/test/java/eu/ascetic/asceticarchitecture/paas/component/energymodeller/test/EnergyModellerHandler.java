/**
* Copyright 2014 Hewlett-Packard Development Company, L.P.                                         
 */
package eu.ascetic.asceticarchitecture.paas.component.energymodeller.test;

import java.sql.Timestamp;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Vector;

import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import eu.ascetic.asceticarchitecture.paas.component.energymodeller.builder.EnergyModellerFactory;
import eu.ascetic.asceticarchitecture.paas.component.energymodeller.interfaces.PaaSEnergyModeller;

public class EnergyModellerHandler {

	private static PaaSEnergyModeller serviceEM;
	
	@BeforeClass
	public static void setup() {
		serviceEM = EnergyModellerFactory.getEnergyModeller("c:/test/testconfig.properties");
		//serviceEM = new EnergyModellerSimple("c:/test/testconfig.properties");
	}
	
	@Test
	public void testEnergyModellerApplicationConsumption() {
		
		double energy = serviceEM.energyEstimationForVM("test", "app1", "4351a79d-75ee-42fd-b9e6-b963a10b1787", null);
		
		Assert.assertNotNull(energy);
	}
	
	@Test
	public void testEventConsumption(){
		
		//"HMMERpfam", "45"
		List<String> vms = new Vector<String>();
		vms.add("4351a79d-75ee-42fd-b9e6-b963a10b1787");
		
		double est = serviceEM.energyApplicationConsumption(null, "HMMERpfam", vms, "allevents");
		System.out.println("Energy estim for event "+est);
	}
	
	@Test
	public void testEnergyEstimatorConsumption() {
		
		DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		Date date;
		
		try {
			date = dateFormat.parse("2014-09-11 00:00:00");

			
			long time = date.getTime();
			
			Timestamp ts = new Timestamp(date.getTime());
			
			
			List<String> vm	= new Vector<String>();
			vm.add("fad45a9a-46bc-4876-a641-e90ac9578cc0");
			
			// on date
			
			
			
			System.out.println("Estimation  is "+serviceEM.energyEstimationForTime("test", "app1", vm, null,ts));
			// +1 h
			
			long base = 3600000;
			
			for (int k=0;k<24;k++){
				ts = new Timestamp(date.getTime()+base);
				
				double est = serviceEM.energyEstimationForTime("test", "app1", vm, null,ts);
				
				System.out.println(ts.toString()+";"+est+";");
				
				base = base + 3600000;
			}
			

			
		} catch (ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		
		
		

	}
	
	
	
	

}
