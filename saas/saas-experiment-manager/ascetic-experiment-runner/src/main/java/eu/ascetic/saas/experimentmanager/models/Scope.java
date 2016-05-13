package eu.ascetic.saas.experimentmanager.models;

import java.util.List;
import java.util.stream.Collectors;


public class Scope {
	
	private String description;
	
	private List<ScopableItem> scopableItems;
	private String category;
	

	public Scope(String description, List<ScopableItem> scopableItems, String category) {
		super();
		this.description = description;
		this.scopableItems = scopableItems;
		this.category = category;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public List<ScopableItem> getScopableItems() {
		return scopableItems;
	}

	public void setScopableItems(List<ScopableItem> scopableItems) {
		this.scopableItems = scopableItems;
	}
	
	
	public List<String> getRefersTo(){
		return scopableItems.stream().map(spi -> spi.getReference()).collect(Collectors.toList());
	}

	public String getCategory() {
		return category;
	}

	public void setCategory(String category) {
		this.category = category;
	}

}
