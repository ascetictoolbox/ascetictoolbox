package eu.ascetic.saas.experimentmanager.models;

public class KPI {
	
	private String name;
	private String description;
	
	private Metric metric;

	public KPI(String name, String description, Metric metric) {
		super();
		this.name = name;
		this.description = description;
		this.metric = metric;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public Metric getMetric() {
		return metric;
	}

	public void setMetric(Metric metric) {
		this.metric = metric;
	}

	public String getName() {
		return name;
	}

	

}
