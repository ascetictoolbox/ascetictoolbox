package eu.ascetic.saas.experimentmanager.models;


public class Event {
	
	private String name;
	private String launchCmd;
	
	public Event(String name, String launchCmd) {
		this.name = name;
		this.launchCmd = launchCmd;
	}

	public String getName() {
		return name;
	}


	public String getLaunchCmd() {
		return launchCmd;
	}

	public void setLaunchCmd(String launchCmd) {
		this.launchCmd = launchCmd;
	}

}
