package es.bsc.vmmanagercore.model;

import java.util.Date;

import org.codehaus.jackson.node.JsonNodeFactory;
import org.codehaus.jackson.node.ObjectNode;

/**
 * 
 *
 * @author David Ortiz Lopez (david.ortiz@bsc.es)
 *
 */
public class VmDeployed extends Vm {
	
	private String id;
	private String ipAddress;
	private String state;
	private Date created;
	
	public VmDeployed(String name, String image, int cpus, int ramMb, 
			int diskGb, String initScript, String applicationId, String id, 
			String ipAddress, String state, Date created) {
		super(name, image, cpus, ramMb, diskGb, initScript, applicationId);
		this.id = id;
		this.ipAddress = ipAddress;
		this.state = state;
		this.created = created;
	}
	
	public String getId() {
		return id;
	}
	
	public String getIpAddress() {
		return ipAddress;
	}
	
	public String getState() {
		return state;
	}
	
	public Date getCreated() {
		return created;
	}
	
	public ObjectNode getJsonNode() {
		JsonNodeFactory factory = JsonNodeFactory.instance;
		ObjectNode infoVmJson = factory.objectNode();
		infoVmJson.put("id", id);
		infoVmJson.put("name", getName());
		infoVmJson.put("image", getImage());
		infoVmJson.put("cpus", getCpus());
		infoVmJson.put("ramMb", getRamMb());
		infoVmJson.put("diskGb", getDiskGb());
		infoVmJson.put("state", state.toString());
		infoVmJson.put("ipAddress", ipAddress);
		infoVmJson.put("dateCreated", created.toString());
		infoVmJson.put("applicationId", getApplicationId());
		return infoVmJson;
	}
	
}
