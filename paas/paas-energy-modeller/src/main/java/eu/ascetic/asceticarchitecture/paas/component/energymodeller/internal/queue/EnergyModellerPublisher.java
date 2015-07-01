package eu.ascetic.asceticarchitecture.paas.component.energymodeller.internal.queue;

import java.util.TimerTask;

import eu.ascetic.asceticarchitecture.paas.component.energymodeller.interfaces.PaaSEnergyModeller;

public class EnergyModellerPublisher extends TimerTask{

	private PaaSEnergyModeller energyModeller;
	
	public EnergyModellerPublisher(PaaSEnergyModeller energyModeller){
		
	}
	
	@Override
	public void run() {
		// get monitoring infor
		
		// get data and publish it to the queue (already by default done by the emodeller)
		
		
		
	}

}
