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

import java.sql.Timestamp;
import java.util.List;
import java.util.Vector;

import org.junit.BeforeClass;
import org.junit.Test;

import eu.ascetic.asceticarchitecture.paas.component.energymodeller.builder.EnergyModellerFactory;
import eu.ascetic.asceticarchitecture.paas.component.energymodeller.datatype.Unit;
import eu.ascetic.asceticarchitecture.paas.component.energymodeller.internal.service.EnergyModellerService;


public class EnergyModellerData {

	// M. Fontanella - 05 Feb 2016 - begin
	private static EnergyModellerService serviceEM;
	
	/* Test 1 
	private static String HOST = "5699";
	private static String PROVIDER = "00000";
	private static String EVENT = "1";	
	private static String APP = "maximTestApp";
	private static String DEP = "938";
	*/
	
	/* Test 2
	private static String HOST = "6000";
	private static String PROVIDER = "00000";
	private static String EVENT = "1";	
	private static String APP = "maximTestApp";
	private static String DEP = "600";
	*/
	
	/* Test 3 
	private static String HOST = "2430";
	private static String PROVIDER = "00000";
	private static String EVENT = "1";	
	private static String APP = "davidgpTestApp";
	private static String DEP = "877";
	*/
	
	/* Test 4
	private static String HOST = "2444";
	private static String PROVIDER = "1";
	private static String EVENT = "event_dstest";	
	private static String APP = "davidgpTestApp";
	private static String DEP = "939";
	// long beginlong = 1444147613055L;
	// long endlong   = 1444147925771L;
	long beginlong = 1444147613000L;
	long endlong   = 1444147925000L;
	*/
	
	/* Test 5 */
	private static String HOST = "2456";
	private static String PROVIDER = "1";
	private static String EVENT = "ciao";	
	private static String APP = "JEPlus";
	private static String DEP = "942";
	/**/
	
	// long beginlong = 1410449721L;
	// long endlong = 1443705931826L;
	
	//	private static String HOST = "1869";
	//	private static String HOST1 = "1870";
	
	//	private static String HOST = "1843";
	//	private static String HOST1 = "1844";

	//  private static String HOST1 = "1765";
	//	private static String HOST2 = "1766";
	//	private static String HOST3 = "1767";
	//	private static String HOST3 = "b52da74d-585c-404d-8f29-4de0d93cfe5e";
	//	private static String HOST4 = "1768";
	
	//	private static String DEP = "584";
	//	private static String APP = "davidgpTestApp";
	//	private static String EVENT = "Create-Object-Light-Load";
		
	//	private static String DEP = "490";
	//	private static String EVENT = "core0impl1";
	//	private static String APP = "newsAsset";
	
	//	private static String DEP = "540";

	// private static String TEST= "11";
	
	// M. Fontanella - 05 Feb 2016 - end
	
	@BeforeClass
	public static void setup() {
		// M. Fontanella - 05 Feb 2016 - begin
		serviceEM = (EnergyModellerService) EnergyModellerFactory.getEnergyModeller("c:/mfontanella/new/lavoro/ascetic/config/config.properties");
		// M. Fontanella - 05 Feb 2016 - end
	}

// M. Fontanella - 09 Jun 2016 - begin
	@Test
	public void eventPowerInterface() {
		List<String> vmids = new Vector<String>();
		vmids.add(HOST);		
		EVENT=null;
		// double result = serviceEM.measure(PROVIDER, APP, DEP, vmids, EVENT, Unit.ENERGY,null,null);
		// double result = serviceEM.measure(PROVIDER, APP, DEP, vmids, EVENT, Unit.ENERGY,new Timestamp(beginlong),new Timestamp(endlong));
		double result = serviceEM.estimate(PROVIDER, APP, DEP, vmids, EVENT, Unit.POWER,3600);
		System.out.println("################################ HOST "+HOST+" Average Power "+EVENT+" estimated is:  "+result);
	}
// M. Fontanella - 09 Jun 2016 - end	
	
// M. Fontanella - 05 Feb 2016 - begin
//	@Test
//	public void eventPowerInterface() {
//		List<String> vmids = new Vector<String>();
//		vmids.add(HOST);		
//		EVENT=null;
//		System.out.println("MAXIMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMM");
//		double result = serviceEM.estimate(PROVIDER, APP, DEP, vmids, EVENT, Unit.ENERGY,60);
//		System.out.println("################################ HOST "+HOST+"Average Power "+EVENT+" estimated is:  "+result);
//	}
// M. Fontanella - 05 Feb 2016 - end	


// M. Fontanella - 05 Feb 2016 - begin
//	@Test
//	public void eventPowerInterface() {
//		List<String> vmids = new Vector<String>();
//		vmids.add(HOST);		
		// PROVIDER=null;
//		EVENT=null;
//		double result = serviceEM.measure(PROVIDER, APP, DEP, vmids, EVENT, Unit.POWER,null,null);
//		System.out.println("################################ HOST "+HOST+"Average Power "+EVENT+" estimated is:  "+result);
//	}
// M. Fontanella - 05 Feb 2016 - end	
	
	
//	@Test
//	public void eventEnergyInterface() {
//		List<String> vmids = new Vector<String>();
//		vmids.add(HOST);
//
//		double result = serviceEM.measure(PROVIDER, APP, DEP, vmids, EVENT, Unit.POWER,null,null);
//		System.out.println("################################ TEST "+HOST+"Average Power "+EVENT+" estimated is:  "+result);
//	}
	
//	@Test
//	public void eventPowerInterface() {
//		System.out.println("Testing power measurement over a time range");
//		List<String> vmids = new Vector<String>();
//
//		vmids.add(HOST1);	
//		PROVIDER=null;
//		double result = serviceEM.measure(PROVIDER, APP, DEP, vmids, EVENT, Unit.POWER,null,null);
//		System.out.println("################################ TEST Average Energy estimated is:  "+result);
//	}
//
//	@Test
//	public void eventEnergy2Interface() {
//		List<String> vmids = new Vector<String>();
//		vmids.add(HOST);
//		EVENT = "core0impl1";
//		double result = serviceEM.measure(PROVIDER, APP, DEP, vmids, EVENT, Unit.POWER,null,null);
//		System.out.println("################################ TEST Average Power estimated is:  "+result);
//	}
//	
//	@Test
//	public void eventPower2Interface() {
//		System.out.println("Testing power measurement over a time range");
//		List<String> vmids = new Vector<String>();
//		EVENT = "core0impl1";
//		vmids.add(HOST1);	
//		PROVIDER=null;
//		double result = serviceEM.measure(PROVIDER, APP, DEP, vmids, EVENT, Unit.POWER,null,null);
//		System.out.println("################################ TEST Average Energy estimated is:  "+result);
//	}
	
	
//	@Test
//	public void eventMEnergyInterface() {
//		System.out.println("Testing power measurement over a time range");
//		List<String> vmids = new Vector<String>();
//		vmids.add(HOST);
//		vmids.add(HOST1);
//
//		//EVENT = null;
//		PROVIDER=null;
//		double result = serviceEM.measure(PROVIDER, APP, DEP, vmids, EVENT, Unit.ENERGY, null,null);
//		System.out.println("################################ TEST Average Energy estimated is:  "+result);
//	}
//	
//	@Test
//	public void eventMEnergyInterface() {
//		System.out.println("Testing power measurement over a time range");
//		List<String> vmids = new Vector<String>();
//		vmids.add(HOST);
//		vmids.add(HOST1);
//		vmids.add(HOST2);
//		vmids.add(HOST3);
//		vmids.add(HOST4);	
//		PROVIDER=null;
//		double result = serviceEM.measure(PROVIDER, APP, DEP, vmids, EVENT, Unit.POWER, null,null);
//		System.out.println("################################ TEST Average Energy estimated is:  "+result);
//	}

//	@Test
//	public void estMPowerInterface() {
//		System.out.println("Testing power measurement over a time range");
//		List<String> vmids = new Vector<String>();
//		vmids.add(HOST);
//		vmids.add(HOST1);		
//		//vmids.add(TEST);		
//		PROVIDER=null;
//		double result = serviceEM.estimate(PROVIDER, APP, DEP, vmids, EVENT, Unit.ENERGY,3600);
//		System.out.println("################################ TEST Average Power estimated is:  "+result);
//	}
//	
//	@Test
//	public void estMEnergyInterface() {
//		System.out.println("Testing power measurement over a time range");
//		List<String> vmids = new Vector<String>();
//		vmids.add(TEST);		
//		PROVIDER=null;
//		double result = serviceEM.estimate(PROVIDER, APP, DEP, vmids, EVENT, Unit.ENERGY,3600);
//		System.out.println("################################ TEST Average Energy estimated is:  "+result);
//	}
	
	// test for power consumption
	
//	@Test
//	public void testMeasurePoweryInterface() {
//		System.out.println("Testing power measurement");
//		List<String> vmids = new Vector<String>();
//		vmids.add(HOST);
//		
//		
//		//vmids.add(HOST1);
//		//vmids.add(HOST2);
//		//vmids.add(HOST3);
//		//vmids.add(HOST4);
//		//vmids.add(TEST);
//		PROVIDER=null;
//		//EVENT=null;
//		//double result = serviceEM.measure(PROVIDER, APP, DEP, vmids, EVENT, Unit.POWER, new Timestamp(beginlong),new Timestamp(endlong));
//		double result = serviceEM.measure(PROVIDER, APP, DEP, vmids, EVENT, Unit.ENERGY, null,null);
//		System.out.println("############################# TEST Average Power from all samples is:  "+result);
//	}
//	
//	@Test
//	public void testMeasureEnergyInterface() {
//		System.out.println("Testing power measurement");
//		List<String> vmids = new Vector<String>();
//		vmids.add(HOST);
//		vmids.add(HOST1);
//		vmids.add(HOST2);
//		vmids.add(HOST3);
//		vmids.add(HOST4);
//		//vmids.add(TEST);
//		PROVIDER=null;
//		//EVENT=null;
//		//double result = serviceEM.measure(PROVIDER, APP, DEP, vmids, EVENT, Unit.ENERGY, new Timestamp(beginlong),new Timestamp(endlong));
//		double result = serviceEM.measure(PROVIDER, APP, DEP, vmids, EVENT, Unit.ENERGY, null,null);
//		System.out.println("############################# TEST Average Power from all samples is:  "+result);
//	}
	
//	@Test
//	public void testEstimPowerInterface() {
//		System.out.println("Testing power measurement over a time range");
//		List<String> vmids = new Vector<String>();
//		vmids.add(HOST);
//		vmids.add(HOST1);
//		vmids.add(HOST2);
//		vmids.add(HOST3);
//		vmids.add(HOST4);		
//		PROVIDER=null;
//		double result = serviceEM.estimate(PROVIDER, APP, DEP, vmids, EVENT, Unit.POWER,3600);
//		System.out.println("################################ TEST Average Power estimated is:  "+result);
//	}	
//
//	
//	@Test
//	public void testEnergyMeasureInterface() {
//		System.out.println("Testing power measurement");
//		List<String> vmids = new Vector<String>();
//		vmids.add(HOST);
//		vmids.add(HOST1);
//		vmids.add(HOST2);
//		vmids.add(HOST3);
//		vmids.add(HOST4);
//		//vmids.add(TEST);
//		PROVIDER=null;
//		EVENT=null;
//		double result = serviceEM.measure(PROVIDER, APP, DEP, vmids, EVENT, Unit.ENERGY, null,null);
//		System.out.println("############################# TEST Energy from all samples is:  "+result);
//	}
//	
//	@Test
//	public void testEstimEnergyInterface() {
//		System.out.println("Testing power measurement over a time range");
//		List<String> vmids = new Vector<String>();
//		vmids.add(HOST);
//		vmids.add(HOST1);
//		vmids.add(HOST2);
//		vmids.add(HOST3);
//		vmids.add(HOST4);		
//		PROVIDER=null;
//		double result = serviceEM.estimate(PROVIDER, APP, DEP, vmids, null, Unit.ENERGY,3600);
//		System.out.println("################################ TEST Energy estimated is:  "+result);
//	}
	
//	
//	@Test
//	public void superPlot() {
//		System.out.println("Testing power measurement over a time range");
//		List<String> vmids = new Vector<String>();
//		vmids.add(HOST);
//		vmids.add(HOST1);
//		vmids.add(HOST2);
//		vmids.add(HOST3);
//		vmids.add(HOST4);		
//		PROVIDER=null;
//		int time=0;
//		int delta = 60;
//		for (int i=1;i<2;i++){
//			time =  (delta * i);
//			double result = serviceEM.estimate(PROVIDER, APP, DEP, vmids, null, Unit.ENERGY,time);
//			System.out.println("################################ ");
//			System.out.println("################################ ");
//			System.out.println("################################  ");
//			System.out.println("################################ TEST Energy after "+time+" seconds estimated is:  "+result);
//		}
//		
//	}
//	
//
//	@Test
//	public void testMeasureAllEnergyInterface() {
//		System.out.println("Testing energy estimationfrom all samples");
//		List<String> vmids = new Vector<String>();
////		vmids.add(HOST);
////		vmids.add(HOST1);
////		vmids.add(HOST2);
////		vmids.add(HOST3);
////		vmids.add(HOST4);
//		//vmids.add(TEST);
//		EVENT=null;
//		//vmids.add(HOST1);
//		PROVIDER=null;
//		double result = serviceEM.measure(PROVIDER, APP, DEP, vmids, EVENT, Unit.ENERGY, null,null);
//		System.out.println("Energy Wh from all samples is:  "+result);
//	}
	
//	@Test
//	public void testMeasureRangePoweryInterface() {
//		System.out.println("Testing power measurement over a time range");
//		List<String> vmids = new Vector<String>();
//		vmids.add(HOST);
//		vmids.add(HOST1);
//		PROVIDER=null;
//		double result = serviceEM.measure(PROVIDER, APP, DEP, vmids, null, Unit.POWER,new Timestamp(beginlong),new Timestamp(endlong));
//		System.out.println("Average Power in the time range is:  "+result);
//	}
	
	
//	@Test
//	public void testPredizioneRangePoweryInterface() {
//		System.out.println("Testing power measurement over a time range");
//		List<String> vmids = new Vector<String>();
//		vmids.add(HOST);
//		vmids.add(HOST1);
//		vmids.add(HOST2);
//		vmids.add(HOST3);
//		vmids.add(HOST4);		
//		PROVIDER=null;
//		double result = serviceEM.estimate(PROVIDER, APP, DEP, vmids, null, Unit.POWER,10000);
//		System.out.println("Average Power in the time range is:  "+result);
//	}
//

	
//
//	@Test
//	public void testMeasureRangeEnergyInterface() {
//		System.out.println("Testing energy in a time period");
//		List<String> vmids = new Vector<String>();
//		vmids.add(HOST);
//		vmids.add(HOST1);
//		PROVIDER=null;
//		double result = serviceEM.measure(PROVIDER, APP, DEP, vmids, null, Unit.ENERGY, new Timestamp(beginlong),new Timestamp(endlong));
//		System.out.println("Energy Wh from time range:  "+result);
//	}
	
//	@Test
//	public void testMeasureEventAllPoweryInterface() {
//		System.out.println("Testing power measurement");
//		List<String> vmids = new Vector<String>();
//		vmids.add(HOST);
//		vmids.add(HOST1);
//		PROVIDER=null;
//		double result = serviceEM.measure(PROVIDER, APP, DEP, vmids, EVENT, Unit.POWER, null,null);
//		System.out.println("Average Power from all samples is:  "+result);
//	}
//
//	@Test
//	public void testMeasureEventAllEnergyInterface() {
//		System.out.println("Testing energy estimationfrom all samples");
//		List<String> vmids = new Vector<String>();
//		vmids.add(HOST);
//		vmids.add(HOST1);
//		PROVIDER=null;
//		double result = serviceEM.measure(PROVIDER, APP, DEP, vmids, EVENT, Unit.ENERGY, null,null);
//		System.out.println("Energy Wh from all samples is:  "+result);
//	}
	
//	@Test
//	public void testPredictPowerInterface() {
//		System.out.println("Predict energy estimationfrom all samples");
//		List<String> vmids = new Vector<String>();
//		vmids.add(HOST);
//		vmids.add(HOST1);
//		PROVIDER=null;
//		double result = serviceEM.estimate(PROVIDER, APP, DEP, vmids, null, Unit.POWER, 60);
//		System.out.println("Power estimation in "+60+" is :  "+result);
//	}
		
	
//	@Test
//	public void testMeasurePowerInterface() {
//		System.out.println("Testing power estimation between two defined timestamps");
//		List<String> vmids = new Vector<String>();
//		vmids.add(HOST);
//		vmids.add(HOST1);
//		
//		double result = serviceEM.measure(PROVIDER, APP, DEP, vmids, null, Unit.POWER, new Timestamp(beginlong),new Timestamp(endlong));
//		System.out.println("Average Power is:  "+result);
//	}
	
//	@Test
//	public void testMeasureAllPowerInterface() {
//		System.out.println("Testing power estimation with not time specified");
//		List<String> vmids = new Vector<String>();
//		vmids.add(HOST);
//		vmids.add(HOST1);
//		
//		double result = serviceEM.measure(APP, DEP, vmids, null, Unit.POWER, null,null);
//		System.out.println("Average Power is:  "+result);
//	}
//	
//	@Test
//	public void testMeasureSincePowerInterface() {
//		System.out.println("Testing power estimation with only a limit in time");
//		List<String> vmids = new Vector<String>();
//		vmids.add(HOST);
//		vmids.add(HOST1);
//		double result = serviceEM.measure(APP, DEP, vmids, null, Unit.POWER, null,new Timestamp(endlong));
//		System.out.println("Average Power is:  "+result);
//	}
	
//	@Test
//	public void testMeasureUpToPowerInterface() {
//		System.out.println("Testing power estimation with a start time");
//		List<String> vmids = new Vector<String>();
//		vmids.add(HOST);
//		vmids.add(HOST1);
//		double result = serviceEM.measure(APP, DEP, vmids, null, Unit.POWER, new Timestamp(beginlong),null);
//		System.out.println("Average Power is:  "+result);
//	}		
//	@Test
//	public void testAppSample() {
//		System.out.println("Test Sample");
//		List<String> vmids = new Vector<String>();
//		vmids.add(HOST);
//		vmids.add(HOST1);
//		vmids.add(HOST3);
//		List<ApplicationSample> energy = serviceEM.applicationData(null, APP, vmids, 10 , new Timestamp(beginlong),new Timestamp(endlong) );
//		for (ApplicationSample as : energy){
//			System.out.println(as.export());
//		}
//		Assert.assertNotNull(energy);
//	}
	
	// test energy estimation
	
//	@Test
//	public void testMeasurePowerInterface() {
//		System.out.println("Testing energy estimation between two defined timestamps");
//		List<String> vmids = new Vector<String>();
//		vmids.add(HOST);
//		vmids.add(HOST1);
//		
//		double result = serviceEM.measure(APP, DEP, vmids, null, Unit.ENERGY, new Timestamp(beginlong),new Timestamp(endlong));
//		System.out.println("Energy is:  "+result);
//	}	
//	@Test
//	public void testMeasureAllEnergyInterface() {
//		System.out.println("Testing energy estimation with not time specified");
//		List<String> vmids = new Vector<String>();
//		vmids.add(HOST);
//		vmids.add(HOST1);
//		
//		double result = serviceEM.measure(APP, DEP, vmids, null, Unit.ENERGY, null,null);
//		System.out.println("Energy is:  "+result);
//	}
//	
//	@Test
//	public void testMeasureSincePowerInterface() {
//		System.out.println("Testing energy estimation with only a limit in time");
//		List<String> vmids = new Vector<String>();
//		vmids.add(HOST);
//		vmids.add(HOST1);
//		double result = serviceEM.measure(APP, DEP, vmids, null, Unit.ENERGY, null,new Timestamp(endlong));
//		System.out.println("Energy is:  "+result);
//	}
//	@Test
//	public void testMeasureUpToPowerInterface() {
//		System.out.println("Testing energy estimation with a start time");
//		List<String> vmids = new Vector<String>();
//		vmids.add(HOST);
//		vmids.add(HOST1);
//		double result = serviceEM.measure(APP, DEP, vmids, null, Unit.ENERGY, new Timestamp(beginlong),null);
//		System.out.println("Energy is:  "+result);
//	}	
//	
	
	// test events consumption
	
	// other tests
	
	
//	@Test
//	public void testAppSample() {
//		System.out.println("Test Sample");
//		List<String> vmids = new Vector<String>();
//		vmids.add(HOST);
//		vmids.add(HOST1);
//		vmids.add(HOST3);
//		List<ApplicationSample> energy = serviceEM.applicationData(null, APP, vmids, 10 , new Timestamp(beginlong),new Timestamp(endlong) );
//		for (ApplicationSample as : energy){
//			System.out.println(as.export());
//		}
//		Assert.assertNotNull(energy);
//	}	
	
	
	
	
//	@Test
//	public void testEnergyWithinInterval() {
//		System.out.println("Testing energy estimation");
//		List<String> vmids = new Vector<String>();
//		vmids.add(HOST);
//		vmids.add(HOST1);
//
//		List<EventSample> results = serviceEM.eventsData( null, APP,vmids, EVENT,new Timestamp(beginlong),new Timestamp(endlong));	
//		
//		for (EventSample es : results){
//			System.out.println(es.export());
//		}
//	}

//	@Test
//	public void testMeasureEventEnergyInterface() {
//		System.out.println("Testing energy estimation");
//		List<String> vmids = new Vector<String>();
//		vmids.add(HOST);
//		vmids.add(HOST2);
//		vmids.add(HOST3);	
//		
//		double result = serviceEM.measure(null, APP, vmids, EVENT, Unit.ENERGY, new Timestamp(beginlong),new Timestamp(endlong));
//		System.out.println("Testing energy estimation gave "+result);
//	}
	
//	@Test
//	public void testMeasureEventPowerInterface() {
//		System.out.println("Testing energy estimation");
//		List<String> vmids = new Vector<String>();
//		vmids.add(HOST);
//		vmids.add(HOST2);
//		vmids.add(HOST3);	
//		
//		double result = serviceEM.measure(null, APP, vmids, EVENT, Unit.ENERGY, new Timestamp(beginlong),new Timestamp(endlong));
//		System.out.println("Testing energy estimation gave "+result);
//	}
		
//	@Test
//	public void testMeasureAppEnergyInterface() {
//		System.out.println("Testing energy estimation");
//		List<String> vmids = new Vector<String>();
//		vmids.add(HOST);
//		vmids.add(HOST2);
//		vmids.add(HOST3);	
//		
//		double result = serviceEM.measure(null, APP, vmids, EVENT, Unit.ENERGY, new Timestamp(beginlong),new Timestamp(endlong));
//		System.out.println("Testing energy estimation gave "+result);
//	}	

//	@Test
//	public void testMeasureAppPowerInterface() {
//		System.out.println("Testing energy estimation");
//		List<String> vmids = new Vector<String>();
//		vmids.add(HOST);
//		vmids.add(HOST2);
//		vmids.add(HOST3);	
//		
//		double result = serviceEM.measure(null, APP, vmids, null, Unit.POWER, new Timestamp(beginlong),new Timestamp(endlong));
//		System.out.println("Testing energy estimation gave "+result);
//	}
	
//	@Test
//	public void testMeasureAppEnergyAllInterface() {
//		System.out.println("Testing energy estimation");
//		List<String> vmids = new Vector<String>();
//		vmids.add(HOST);
//		vmids.add(HOST2);
//		vmids.add(HOST3);	
//		
//		double result = serviceEM.measure(null, APP, vmids, null, Unit.ENERGY, null , null);
//		System.out.println("Testing energy estimation gave "+result);
//	}	

}
