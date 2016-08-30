package eu.ascetic.saas.experimentmanager.models;

import eu.ascetic.saas.experimentmanager.exception.MetricDefinitionIncorrectException;
import eu.ascetic.saas.experimentmanager.exception.NoMeasureException;


public class AggregatedMetric extends Metric {
	
	private Metric metric;
	
	/**
	 * @return the metric
	 */
	public Metric getMetric() {
		return metric;
	}

	/**
	 * @param metric the metric to set
	 */
	public void setMetric(Metric metric) {
		this.metric = metric;
	}

	public AggregatedMetric(){
		
	}
	
	public AggregatedMetric(String id, Metric m){
		this.setName(id);
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

}
