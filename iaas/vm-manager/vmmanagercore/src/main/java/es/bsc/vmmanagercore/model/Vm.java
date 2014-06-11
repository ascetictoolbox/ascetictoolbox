package es.bsc.vmmanagercore.model;

import java.io.File;

import org.codehaus.jackson.JsonNode;
import org.codehaus.jackson.node.JsonNodeFactory;
import org.codehaus.jackson.node.ObjectNode;

/**
 * 
 *
 * @author David Ortiz Lopez (david.ortiz@bsc.es)
 *
 */
public class Vm {
	private String name;
	private String image; // It can be an ID or a URL
	private int cpus;
	private int ramMb;
	private int diskGb;
	private String initScript;
	private String applicationId;
	
	/**
	 * Class constructor.
	 * @param instanceName The name of the instance.
	 * @param image The ID of the image or a URL containing it.
	 * @param cpus The number of CPUs.
	 * @param ramMb The amount of RAM in MB.
	 * @param diskGb The size of the disk in GB.
	 * @param initScript Script that will be executed when the VM is deployed.
	 */
	public Vm(String name, String image, int cpus, int ramMb, int diskGb,
			String initScript, String applicationId) {
		this.name = name;
		this.image = image;
		setCpus(cpus);
		setRamMb(ramMb);
		setDiskGb(diskGb);
		setInitScript(initScript);
		this.applicationId = applicationId;
	}

	public Vm(JsonNode json) {
		this.name = json.get("name").asText();
		this.image = json.get("image").asText();
		setCpus(json.get("cpus").asInt());
		setRamMb(json.get("ramMb").asInt());
		setDiskGb(json.get("diskGb").asInt());
		this.initScript = null;
		if (json.has("initScript")) {
			if (!json.get("initScript").asText().equals("")) {
				System.out.println(json.get("initScript").asText());
				setInitScript(json.get("initScript").asText());
			}
		}
		this.applicationId = json.get("applicationId").asText();
	}
	
	public String getName() {
		return name;
	}

	public void setInstanceName(String name) {
		this.name = name;
	}

	public String getImage() {
		return image;
	}

	public void setImage(String image) {
		this.image = image;
	}
	
	public int getCpus() {
		return cpus;
	}

	public void setCpus(int cpus) {
		if (cpus <= 0) {
			throw new IllegalArgumentException("The number of cpus has to be greater than 0");
		}
		this.cpus = cpus;
	}

	public int getRamMb() {
		return ramMb;
	}

	public void setRamMb(int ramMb) {
		if (ramMb <= 0) {
			throw new IllegalArgumentException("The amount of memory has to be greater than 0");
		}
		this.ramMb = ramMb;
	}

	public int getDiskGb() {
		return diskGb;
	}

	public void setDiskGb(int diskGb) {
		if (diskGb <= 0) {
			throw new IllegalArgumentException("The amount of disk size has to be greater than 0");
		}
		this.diskGb = diskGb;
	}
	
	public String getInitScript() {
		return initScript;
	}
	
	public void setInitScript(String initScript) {
		// If a path for an init script was specified
		if (initScript != null) {
			// Check that the path is valid and the file can be read
			File f = new File(initScript);
			if (!f.isFile() || !f.canRead()) {
				throw new IllegalArgumentException("The path specified for the init script"
						+ " is not valid");
			}
		}
		this.initScript = initScript;
	}
	
	public String getApplicationId() {
		return applicationId;
	}
	
	public ObjectNode getJson() {
		ObjectNode vm = JsonNodeFactory.instance.objectNode();
		vm.put("name", name);
		vm.put("image", image);
		vm.put("cpus", cpus);
		vm.put("ramMb", ramMb);
		vm.put("diskGb", diskGb);
		if (initScript != null) {
			vm.put("initScript", initScript);
		}
		vm.put("applicationId", applicationId);
		return vm;
	}
	
}
