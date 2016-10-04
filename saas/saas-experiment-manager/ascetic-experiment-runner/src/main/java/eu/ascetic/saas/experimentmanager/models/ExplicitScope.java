package eu.ascetic.saas.experimentmanager.models;

import java.util.ArrayList;
import java.util.List;

public class ExplicitScope implements ScopeFilter {
	
	private List<Scope> scopes;
	
	/**
	 * @param scopes the scopes to set
	 */
	public void setScopes(List<Scope> scopes) {
		this.scopes = scopes;
	}

	public ExplicitScope(){
		this.scopes=new ArrayList<Scope>();
	}
	
	public ExplicitScope(List<Scope> scopes){
		this.scopes=scopes;
	}

	@Override
	public List<Scope> list(KPI kpi, String applicationId, Deployment deployments, String runId, List<Event> events) {
		return scopes;
	}

	
	
}
