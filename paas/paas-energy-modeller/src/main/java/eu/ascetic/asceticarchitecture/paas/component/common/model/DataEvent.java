package eu.ascetic.asceticarchitecture.paas.component.common.model;

import java.sql.Timestamp;

//application id | deployment id | event id |  start time | end time | event load (cpu/ram usage) | energy consumed total | min | max | avg


public class DataEvent {
	
	private String applicationid;
	private String deploymentid;
	private String vmid;
	private String eventid;
	public String getEventid() {
		return eventid;
	}
	public void setEventid(String eventid) {
		this.eventid = eventid;
	}
	private Timestamp time;
	private double energy;
	
	public String getApplicationid() {
		return applicationid;
	}
	public void setApplicationid(String applicationid) {
		this.applicationid = applicationid;
	}
	public String getDeploymentid() {
		return deploymentid;
	}
	public void setDeploymentid(String deploymentid) {
		this.deploymentid = deploymentid;
	}
	public String getVmid() {
		return vmid;
	}
	public void setVmid(String vmid) {
		this.vmid = vmid;
	}
	public Timestamp getTime() {
		return time;
	}
	public void setTime(Timestamp time) {
		this.time = time;
	}
	public double getEnergy() {
		return energy;
	}
	public void setEnergy(double energy) {
		this.energy = energy;
	} 
	
	
	
	
	
	

}


