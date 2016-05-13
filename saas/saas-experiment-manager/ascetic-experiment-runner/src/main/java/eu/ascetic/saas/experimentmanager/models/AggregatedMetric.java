package eu.ascetic.saas.experimentmanager.models;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import eu.ascetic.saas.experimentmanager.exception.MetricDefinitionIncorrectException;
import eu.ascetic.saas.experimentmanager.exception.NoMeasureException;


public class AggregatedMetric extends Metric {
	
	private String name;
	private Metric metric;
	
	public AggregatedMetric(String id, Metric m){
		this.name = id;
		this.metric = m;
	}
	
	@Override
	public String get(Scope scope) throws MetricDefinitionIncorrectException, NoMeasureException {
		Double res=0.;
		for (ScopableItem spi:scope.getScopableItems()){
			Scope subscope = new Scope(
					"subscope of"+this.name,
					new ArrayList<ScopableItem>(){{add(spi);}},
					scope.getCategory());
			res += Double.parseDouble(metric.get(subscope));
		}
		
		return res.toString();
	}

	public String getName() {
		return name;
	}
	
}
