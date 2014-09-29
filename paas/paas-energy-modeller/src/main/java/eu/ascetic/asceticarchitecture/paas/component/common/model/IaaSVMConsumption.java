/**
* Copyright 2014 Hewlett-Packard Development Company, L.P.                                         
 */
package eu.ascetic.asceticarchitecture.paas.component.common.model;

public class IaaSVMConsumption {


	private String vmid;
	
	private String hostid;
	
	private String energy;
	
	private String clock;
	
	private String cpu;

	public String getVmid() {
		return vmid;
	}

	public void setVmid(String vmid) {
		this.vmid = vmid;
	}

	public String getHostid() {
		return hostid;
	}

	public void setHostid(String hostid) {
		this.hostid = hostid;
	}

	public String getEnergy() {
		return energy;
	}

	public void setEnergy(String energy) {
		this.energy = energy;
	}

	public String getClock() {
		return clock;
	}

	public void setClock(String clock) {
		this.clock = clock;
	}
	
	public String getCpu() {
		return cpu;
	}

	public void setCpu(String cpu) {
		this.cpu = cpu;
	}	
	
	
}