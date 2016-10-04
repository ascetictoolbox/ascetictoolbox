package eu.ascetic.saas.experimentmanager.models;



import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import eu.ascetic.saas.experimentmanager.paasAPI.InformationProvider;


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
	
	public void populateComponents(InformationProvider ip, String appId) throws Exception{
		if(components==null||components.isEmpty()){
			components = new ArrayList<>();
			Map<String,String> vms = ip.listOfVM(appId, this.getId());
			for(Map.Entry<String,String> vm:vms.entrySet()){
				PhysicalComponent pc = new PhysicalComponent();
				pc.setDepl(this);
				pc.setName(vm.getKey());
				pc.setDescription(vm.getValue());
				components.add(pc);
			}
		}
	}

}
