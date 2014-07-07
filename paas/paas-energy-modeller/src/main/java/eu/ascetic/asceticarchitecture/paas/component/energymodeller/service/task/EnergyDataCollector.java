package eu.ascetic.asceticarchitecture.paas.component.energymodeller.service.task;

import eu.ascetic.asceticarchitecture.paas.component.energymodeller.interfaces.DataCollectorTaskInterface;
import java.util.TimerTask;

public class EnergyDataCollector extends TimerTask implements DataCollectorTaskInterface {

	
	
	@Override
	public void setup() {
		// TODO Auto-generated method stub
		
	}
	
	@Override
	public void handleEventData() {
		// TODO Auto-generated method stub
		
	}
	
	@Override
	public void handleConsumptionData() {
		// TODO Auto-generated method stub
		
	}
	
	@Override
	public void run() {
		
		handleConsumptionData();
		
		
		
	}
}