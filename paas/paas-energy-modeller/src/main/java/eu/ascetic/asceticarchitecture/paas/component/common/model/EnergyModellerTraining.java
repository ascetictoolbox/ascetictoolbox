package eu.ascetic.asceticarchitecture.paas.component.common.model;

import java.sql.Timestamp;

public class EnergyModellerTraining {

	private String trainingid;
	private String applicationid;
	private String deploymentid;
	private Timestamp started;
	private Timestamp ended;
	private boolean status;
	private String events;
	
	public String getTrainingid() {
		return trainingid;
	}
	public void setTrainingid(String trainingid) {
		this.trainingid = trainingid;
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
	public boolean getStatus() {
		return status;
	}
	public void setStatus(boolean status) {
		this.status = status;
	}
	public String getEvents() {
		return events;
	}
	public void setEvents(String events) {
		this.events = events;
	}

	
	
}
