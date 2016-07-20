package eu.ascetic.saas.experimentmanager.models;



import java.util.List;


public class Deployment {
	
	private String name;
	
	
	public Deployment(){
		
	}
	
	public Deployment(String id, String name, List<Component> components){
		this.id = id;
		this.setName(name);
		this.components = components;
		linkComponents();
	}
	
	private void linkComponents(){
		components.stream().forEach(comp ->comp.setDepl(this));
	}
	
	public List<Component> getComponents() {
		return components;
	}
	public void setComponents(List<Component> components) {
		this.components = components;
		linkComponents();
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	private String id;
	private List<Component> components;
	
	

}
