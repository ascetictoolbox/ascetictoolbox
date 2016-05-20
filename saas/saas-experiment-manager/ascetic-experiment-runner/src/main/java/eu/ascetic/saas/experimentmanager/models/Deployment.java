package eu.ascetic.saas.experimentmanager.models;



import java.util.List;
import java.util.stream.Collectors;


public class Deployment {
	
	public Deployment(){
		
	}
	
	public Deployment(String id, List<Component> components){
		this.id = id;
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

	private String id;
	private List<Component> components;
	
	

}
