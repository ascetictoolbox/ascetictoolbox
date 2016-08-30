package eu.ascetic.saas.experimentmanager.models;


public class Event {
	
	private String name;
	/**
	 * @param name the name to set
	 */
	public void setName(String name) {
		this.name = name;
	}

	private String launchCmd;
	
	public Event(){
		
	}
	
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
