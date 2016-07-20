package eu.ascetic.saas.experimentmanager.models;

public class KPI {
	
	private String name;
	private String description;
	private String level;
	private Metric metric;

	public KPI(){
		
	}
	
	public KPI(String name, String level, String description, Metric metric) {
		super();
		this.name = name;
		this.description = description;
		this.level = level;
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

	public String getLevel() {
		return level;
	}

	public void setLevel(String level) {
		this.level = level;
	}

	

}
