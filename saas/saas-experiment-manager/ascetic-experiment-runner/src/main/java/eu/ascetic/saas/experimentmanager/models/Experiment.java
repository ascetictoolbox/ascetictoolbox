package eu.ascetic.saas.experimentmanager.models;

import java.util.ArrayList;
import java.util.List;

public class Experiment {
	
	private String name;
	private String applicationId;
	
	private List<Event> event;
	private List<Deployment> deployments;
	private List<KPI> kpis;
	
	
	public Experiment(String name, String applicationId, List<Event> event, List<Deployment> deployments, List<KPI> kpis) {
		this.name = name;
		this.applicationId = applicationId;
		this.event = event;
		this.deployments = deployments;
		this.kpis = kpis;
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

	public Deployment getDeployment(String deplId) {
		for(Deployment depl:getDeployments()){
			if(depl.getId().equals(deplId)){
				return depl;
			}
		}
		return null;
	}

	
	
	

}
