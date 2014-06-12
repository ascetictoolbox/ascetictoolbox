package net.atos.ari.seip.CloudManager.json;

import org.codehaus.jackson.annotate.JsonProperty;

public class ServiceDeployData {

	@JsonProperty
	String ovf;
	
	@JsonProperty
	String networkID;
	
	public ServiceDeployData(){
		super();
	}
	
	public ServiceDeployData(String ovf, String networkID) {
		super();
		this.ovf = ovf;
		this.networkID = networkID;
	}

	public String getOvf() {
		return ovf;
	}

	public void setOvf(String ovf) {
		this.ovf = ovf;
	}

	public String getNetworkID() {
		return networkID;
	}

	public void setNetworkID(String networkID) {
		this.networkID = networkID;
	}
	
	
}
