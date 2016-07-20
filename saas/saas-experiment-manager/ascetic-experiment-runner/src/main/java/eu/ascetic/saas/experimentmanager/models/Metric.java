package eu.ascetic.saas.experimentmanager.models;

import eu.ascetic.saas.experimentmanager.exception.MetricDefinitionIncorrectException;
import eu.ascetic.saas.experimentmanager.exception.NoMeasureException;

public abstract class Metric {

	private String name;
	private String description;
	private String type;
	
	public String get(Scope scope) throws MetricDefinitionIncorrectException, NoMeasureException {
		return null;
	}


	public String getName() {
		return name;
	}


	public void setName(String name) {
		this.name = name;
	}


	public String getDescription() {
		return description;
	}


	public void setDescription(String description) {
		this.description = description;
	}


	public String getType() {
		return type;
	}


	public void setType(String type) {
		this.type = type;
	}



}