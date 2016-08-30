package eu.ascetic.saas.experimentmanager.models;

import java.util.List;

import eu.ascetic.saas.experimentmanager.factories.ScopeFactory;

public class DeploymentScope implements ScopeFilter {
	

	@Override
	public List<Scope> list(KPI kpi, String applicationId, Deployment deployment, List<Event> events) {
		return ScopeFactory.getScopeByEvent(applicationId, deployment, events);
	}
	
	
	

}
