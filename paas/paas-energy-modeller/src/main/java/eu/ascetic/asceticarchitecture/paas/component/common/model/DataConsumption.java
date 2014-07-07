package eu.ascetic.asceticarchitecture.paas.component.common.model;

import java.sql.Timestamp;

//application id | deployment id | start time | end time | event load (cpu/ram usage) | total energy consumed | min | max | avg

public class DataConsumption {

	private String applicationid;
	private String deploymentid;
	private String vmid;
	private String eventid;
	private Timestamp starttime;
	private Timestamp endtime;
	private double cpu;
	private double memory;
	private double disk;
	private double network;
	
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
	public String getEventid() {
		return eventid;
	}
	public void setEventid(String eventid) {
		this.eventid = eventid;
	}
	public Timestamp getStarttime() {
		return starttime;
	}
	public void setStarttime(Timestamp starttime) {
		this.starttime = starttime;
	}
	public Timestamp getEndtime() {
		return endtime;
	}
	public void setEndtime(Timestamp endtime) {
		this.endtime = endtime;
	}
	public double getCpu() {
		return cpu;
	}
	public void setCpu(double cpu) {
		this.cpu = cpu;
	}
	public double getMemory() {
		return memory;
	}
	public void setMemory(double memory) {
		this.memory = memory;
	}
	public double getDisk() {
		return disk;
	}
	public void setDisk(double disk) {
		this.disk = disk;
	}
	public double getNetwork() {
		return network;
	}
	public void setNetwork(double network) {
		this.network = network;
	}
	
	
}


