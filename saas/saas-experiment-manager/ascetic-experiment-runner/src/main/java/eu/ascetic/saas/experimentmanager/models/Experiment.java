package eu.ascetic.saas.experimentmanager.models;

import java.util.List;

public class Experiment {
	
	private String name;
	private String applicationId;
	private String applicationName;
	private String description;
	
	private List<Event> event;
	private List<Deployment> deployments;
	private List<KPI> kpis;
	
	public Experiment(){
		
	}
	
	public Experiment(String name, String applicationId, String applicationName, String description, List<Event> event, List<Deployment> deployments, List<KPI> kpis) {
		this.name = name;
		this.applicationId = applicationId;
		this.applicationName = applicationName;
		this.event = event;
		this.deployments = deployments;
		this.kpis = kpis;
		this.description = description;
	}

	public String getApplicationId() {
		return applicationId;
	}

	public void setApplicationId(String applicationId) {
		this.applicationId = applicationId;
	}

	public List<Event> getEvent() {
		return event;
	}

	public void setEvent(List<Event> event) {
		this.event = event;
	}

	public List<Deployment> getDeployments() {
		return deployments;
	}

	public void setDeployments(List<Deployment> deployments) {
		this.deployments = deployments;
	}

	public List<KPI> getKpis() {
		return kpis;
	}

	public void setKpis(List<KPI> kpis) {
		this.kpis = kpis;
	}

	public String getName() {
		return name;
	}

	public Deployment getDeployment(String deplName) {
		for(Deployment depl:getDeployments()){
			if(depl.getName().equals(deplName)){
				return depl;
			}
		}
		return null;
	}

	public String getApplicationName() {
		return applicationName;
	}

	public void setApplicationName(String applicationName) {
		this.applicationName = applicationName;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description){
		this.description = description;
	}
	
	

}
