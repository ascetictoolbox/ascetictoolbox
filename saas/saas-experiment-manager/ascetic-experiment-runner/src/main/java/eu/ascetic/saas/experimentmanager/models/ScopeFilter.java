package eu.ascetic.saas.experimentmanager.models;

import java.util.List;

public interface ScopeFilter {
	
	public List<Scope> list(KPI kpi, String applicationId, Deployment deployments, String runId, List<Event> events);

}
