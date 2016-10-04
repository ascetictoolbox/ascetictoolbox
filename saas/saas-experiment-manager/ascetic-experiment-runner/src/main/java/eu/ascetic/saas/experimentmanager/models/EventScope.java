package eu.ascetic.saas.experimentmanager.models;

import java.util.List;

import eu.ascetic.saas.experimentmanager.factories.ScopeFactory;

public class EventScope implements ScopeFilter {
	

	@Override
	public List<Scope> list(KPI kpi, String applicationId, Deployment deployment, String runId, List<Event> events) {
		return ScopeFactory.getScopeByEventNoVM(applicationId, deployment, runId, events);
	}
	
	
	

}
