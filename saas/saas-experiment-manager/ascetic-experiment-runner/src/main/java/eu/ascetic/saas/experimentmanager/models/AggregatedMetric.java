package eu.ascetic.saas.experimentmanager.models;

import eu.ascetic.saas.experimentmanager.exception.MetricDefinitionIncorrectException;
import eu.ascetic.saas.experimentmanager.exception.NoMeasureException;


public class AggregatedMetric extends Metric {
	
	private String name;
	private Metric metric;
	
	public AggregatedMetric(){
		
	}
	
	public AggregatedMetric(String id, Metric m){
		this.name = id;
		this.metric = m;
		this.setType("aggregated");
	}
	
	@Override
	public String get(Scope scope) throws MetricDefinitionIncorrectException, NoMeasureException {
		Double res=0.;
		for (Scope subscope:scope.subScopes()){
			res += Double.parseDouble(metric.get(subscope));
		}
		
		return res.toString();
	}

	public String getName() {
		return name;
	}
	
}
