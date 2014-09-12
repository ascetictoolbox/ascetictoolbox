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
		
		double energy = serviceEM.energyEstimationForVM("test", "app1", "fad45a9a-46bc-4876-a641-e90ac9578cc0", null);
		
		Assert.assertNotNull(energy);
	}
	
	@Test
	public void testEnergyEstimatorConsumption() {
		
		DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		Date date;
		
		try {
			date = dateFormat.parse("2014-09-02 20:12:00");
			System.out.println("Time  is " +  dateFormat.format(date));
			System.out.println("Time  is " +  date.getTime());
			
			long time = date.getTime();
			
			Timestamp ts = new Timestamp(date.getTime());
			
			
			List<String> vm	= new Vector<String>();
			vm.add("fad45a9a-46bc-4876-a641-e90ac9578cc0");
			
			// on date
			System.out.println("Estimation  is "+serviceEM.energyEstimationForTime("test", "app1", vm, null,ts));
			// +1 h
			ts = new Timestamp(date.getTime()+3600);
			System.out.println("Time  is " +  ts);
			System.out.println("Estimation  is "+serviceEM.energyEstimationForTime("test", "app1", vm, null,ts));
			// +6h
			ts = new Timestamp(date.getTime()+21600);
			System.out.println("Time  is " +  ts);
			System.out.println("Estimation  is "+serviceEM.energyEstimationForTime("test", "app1", vm, null,ts));
			// +12h
			ts = new Timestamp(date.getTime()+43200);
			System.out.println("Time  is " +  ts);
			System.out.println("Estimation  is "+serviceEM.energyEstimationForTime("test", "app1", vm, null,ts));
			//+24h
			ts = new Timestamp(date.getTime()+86400);
			System.out.println("Time  is " +  ts);
			System.out.println("Estimation  is "+serviceEM.energyEstimationForTime("test", "app1", vm, null,ts));
			
			
		} catch (ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		

	}
	
	
	
	

}
