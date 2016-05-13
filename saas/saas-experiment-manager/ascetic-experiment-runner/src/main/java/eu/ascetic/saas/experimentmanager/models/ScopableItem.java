package eu.ascetic.saas.experimentmanager.models;


public class ScopableItem {

	public String getReference(){
		return applicationId+"/"+deplId+"/"+vmId+"/"+eventId;
	}
	
	private String applicationId;
	private String deplId;
	private String vmId;
	private String eventId;
	
	public ScopableItem(String applicationId, String deplId, String vmId, String eventId) {
		super();
		this.applicationId = applicationId;
		this.deplId = deplId;
		this.vmId = vmId;
		this.eventId = eventId;
	}

	public String getApplicationId() {
		return applicationId;
	}

	public String getDeplId() {
		return deplId;
	}

	public String getVmId() {
		return vmId;
	}

	public String getEventId() {
		return eventId;
	}
	
	
	

}
