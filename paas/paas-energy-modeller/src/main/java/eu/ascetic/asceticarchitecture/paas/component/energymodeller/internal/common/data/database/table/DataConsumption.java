/**
* Copyright 2014 Hewlett-Packard Development Company, L.P.                                         
 */
package eu.ascetic.asceticarchitecture.paas.component.energymodeller.internal.common.data.database.table;


//application id | deployment id | start time | end time | event load (cpu/ram usage) | total energy consumed | min | max | avg

public class DataConsumption {

	private String applicationid;
	private String deploymentid;
	private String vmid;
	private String eventid;
	private String metrictype;
	private long time;
	private double vmcpu;
	private double vmenergy;
	private double vmpower;
	private double vmmemory;
	
		
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

	public long getTime() {
		return time;
	}
	public void setTime(long time) {
		this.time = time;
	}
	public double getVmcpu() {
		return vmcpu;
	}
	public void setVmcpu(double vmcpu) {
		this.vmcpu = vmcpu;
	}

	public double getVmenergy() {
		return vmenergy;
	}
	public void setVmenergy(double vmenergy) {
		this.vmenergy = vmenergy;
	}

	public double getVmpower() {
		return vmpower;
	}
	public void setVmpower(double vmpower) {
		this.vmpower = vmpower;
	}
	public double getVmmemory() {
		return vmmemory;
	}
	public void setVmmemory(double vmmemory) {
		this.vmmemory = vmmemory;
	}
	public String getMetrictype() {
		return metrictype;
	}
	public void setMetrictype(String metrictype) {
		this.metrictype = metrictype;
	}

}


