import monitor.EnergyMonitor;
import monitor.ResoucesMonitor;

import org.junit.Test;


public class Script{

	private EnergyMonitor em;
	private ResoucesMonitor rm;
	
	@Test
	public void testEnergyForApp() {
		em = new EnergyMonitor();
		em.runMonitor();
	}
	
	@Test
	public void testResoucesMonitor(){
		rm = new ResoucesMonitor();
		rm.monitorResources();
	}
}
