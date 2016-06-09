package eu.ascetic.asceticarchitecture.paas.type;

import java.util.Calendar;



public class Energy{
	Calendar time;
	double energy;
	
	public Energy (double energy){
		time = Calendar.getInstance();
		this.energy = energy;
	}
	
	public void setEnergy(double energy){
		this.energy = energy;
	}
	
	public double getEnergy(){
		return energy;
	}
	
	public void setTime(Calendar time){
		this.time = time;
	}
	
	public Calendar getTime(){
		return time;
	}
	
}