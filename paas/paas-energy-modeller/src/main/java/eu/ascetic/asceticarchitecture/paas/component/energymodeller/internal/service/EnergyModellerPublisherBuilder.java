package eu.ascetic.asceticarchitecture.paas.component.energymodeller.internal.service;

import java.util.Timer;

import eu.ascetic.asceticarchitecture.paas.component.energymodeller.interfaces.PaaSEnergyModeller;
import eu.ascetic.asceticarchitecture.paas.component.energymodeller.internal.queue.EnergyModellerPublisher;
import eu.ascetic.asceticarchitecture.paas.component.energymodeller.internal.queue.QueueManager;

public class EnergyModellerPublisherBuilder {

	// default 60 seconds
	private long timer = 60000;
	private QueueManager queueManager;
	private EnergyModellerPublisher publisher;
	private Timer time;
	
	public EnergyModellerPublisherBuilder(PaaSEnergyModeller energyModeller){
		publisher  = new EnergyModellerPublisher(energyModeller); 
	}
	
	public void start (){
		time = new Timer(); 
		time.schedule(publisher, 0, timer); 
 
	}
	
	public void stop () {
		time.cancel();
	}
	
	
	public long getTimer() {
		return timer;
	}

	public void setTimer(long timer) {
		this.timer = timer;
	}

	public QueueManager getQueueManager() {
		return queueManager;
	}

	public void setQueueManager(QueueManager queueManager) {
		this.queueManager = queueManager;
	}

	
	
}
