package eu.ascetic.asceticarchitecture.paas.component.common.model;

import java.sql.Timestamp;

public class EnergyModellerMonitoring {

	private String monitoringid;
	private String applicationid;
	private String deploymentid;
	private Timestamp started;
	private Timestamp ended;
	private boolean status;
	// TODO will store data for future reference as: energy estimation ecc..
	public String getMonitoringid() {
		return monitoringid;
	}
	public void setMonitoringid(String monitoringid) {
		this.monitoringid = monitoringid;
	}
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
	public Timestamp getStarted() {
		return started;
	}
	public void setStarted(Timestamp started) {
		this.started = started;
	}
	public Timestamp getEnded() {
		return ended;
	}
	public void setEnded(Timestamp ended) {
		this.ended = ended;
	}
	public boolean isStatus() {
		return status;
	}
	public void setStatus(boolean status) {
		this.status = status;
	}
	
	
	
}
