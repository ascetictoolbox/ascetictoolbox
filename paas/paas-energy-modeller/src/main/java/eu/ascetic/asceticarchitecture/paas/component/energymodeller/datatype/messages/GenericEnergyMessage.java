package eu.ascetic.asceticarchitecture.paas.component.energymodeller.datatype.messages;

import java.util.List;

public class GenericEnergyMessage {

	public enum Unit { WATT, WATTHOUR, COUNT, SEC, APP_DURATION, APP_COUNT };
	
	private String provider;
	private String applicationid;
	private String eventid;
	private String deploymentid;
	private List<String> vms;
	private Unit unit;
	private String generattiontimestamp;
	private String referredtimestamp;
	private double value;
	
	public String getProvider() {
		return provider;
	}
	public void setProvider(String provider) {
		this.provider = provider;
	}
	public String getApplicationid() {
		return applicationid;
	}
	public void setApplicationid(String applicationid) {
		this.applicationid = applicationid;
	}
	public String getEventid() {
		return eventid;
	}
	public void setEventid(String eventid) {
		this.eventid = eventid;
	}
	public List<String> getVms() {
		return vms;
	}
	public void setVms(List<String> vms) {
		this.vms = vms;
	}
	public Unit getUnit() {
		return unit;
	}
	public void setUnit(Unit unit) {
		this.unit = unit;
	}
	public String getGenerattiontimestamp() {
		return generattiontimestamp;
	}
	public void setGenerattiontimestamp(String generattiontimestamp) {
		this.generattiontimestamp = generattiontimestamp;
	}
	public String getReferredtimestamp() {
		return referredtimestamp;
	}
	public void setReferredtimestamp(String referredtimestamp) {
		this.referredtimestamp = referredtimestamp;
	}
	public double getValue() {
		return value;
	}
	public void setValue(double value) {
		this.value = value;
	}
	public String getDeploymentid() {
		return deploymentid;
	}
	public void setDeploymentid(String deploymentid) {
		this.deploymentid = deploymentid;
	}
	
	
	
}
