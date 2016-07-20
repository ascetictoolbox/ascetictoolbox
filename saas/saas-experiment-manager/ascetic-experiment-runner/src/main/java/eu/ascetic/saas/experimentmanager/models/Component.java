package eu.ascetic.saas.experimentmanager.models;

public abstract class Component {
	
	private Deployment depl;
	private String name;
	private String description;
	
	public Component(){
		
	}

	public Component(String name){
		this.name = name;
	}
	
	public String getType(){
		return "";
	}


	public Deployment getDepl() {
		return depl;
	}


	public void setDepl(Deployment depl) {
		this.depl = depl;
	}


	
	public String getName() {
		return name;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}
	
	
	
}
