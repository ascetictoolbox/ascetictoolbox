package eu.ascetic.asceticarchitecture.paas.type;

import java.util.Calendar;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.concurrent.TimeUnit;


public class EnergyInfo {
	
	double predictedEnergy;
	Energy VMEnergy;
	//Calendar time;
	
	static LinkedList<Energy> currentEnergyConsumption = new LinkedList<Energy>();
	
	private long getDuration(Calendar startTime, Calendar endTime) {
        long end = endTime.getTime().getTime();
        long start = startTime.getTime().getTime();
        return TimeUnit.MILLISECONDS.toSeconds(end - start);
    }
	
	public void setCurrentTotalConsumption(double energy){
		VMEnergy = new Energy(energy);
		currentEnergyConsumption.add(VMEnergy);
	}
	
	public double getCurrentTotalConsumption(){
		return currentEnergyConsumption.getLast().getEnergy();
	}
	
	public Calendar getTimeOfLastEnergyUpdate(){
		return currentEnergyConsumption.getLast().getTime();
	}
	
	public double getEnergyConsumedAfterUpdate(){
		double lastrecord = currentEnergyConsumption.getLast().getEnergy();
		
		if(currentEnergyConsumption.size()>2){
			double previousrecord = currentEnergyConsumption.get(currentEnergyConsumption.size()-2).getEnergy();
			return lastrecord-previousrecord;
		}
		else {
			return lastrecord;
		}
	}
	
	public double getDurationAfterUpdate(){
		Calendar lastrecord = currentEnergyConsumption.getLast().getTime();
		Calendar previousrecord;
	
		if(currentEnergyConsumption.size()>2){
			previousrecord = currentEnergyConsumption.get(currentEnergyConsumption.size()-2).getTime();
			return getDuration(previousrecord, lastrecord);
		}
		else {
			previousrecord = currentEnergyConsumption.getFirst().getTime();
			return getDuration(previousrecord, lastrecord);
		}
	}
	
	public double getTotalDuration(){
		Calendar lastrecord = currentEnergyConsumption.getLast().getTime();
		Calendar previousrecord = currentEnergyConsumption.getFirst().getTime();
		return getDuration(previousrecord, lastrecord);
	}

	public double getEnergyPredicted(){
		return predictedEnergy;
	}
	
	public void setEnergyPredicted(double energy){
		predictedEnergy = energy;
	}
}