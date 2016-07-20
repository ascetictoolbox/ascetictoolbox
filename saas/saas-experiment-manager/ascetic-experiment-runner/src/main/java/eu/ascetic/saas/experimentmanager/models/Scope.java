package eu.ascetic.saas.experimentmanager.models;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;


public class Scope {
	
	private String description;
	private String name;
	
	private List<ScopableItem> scopableItems;
	private String category;
	
	public Scope(){
		
	}

	public Scope(String name, String description, List<ScopableItem> scopableItems, String category) {
		super();
		this.name = name;
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
	
	
	public List<Scope> subScopes(){
		List<Scope> subscopes = new ArrayList<>(); 
		for (ScopableItem spi:this.getScopableItems()){
			subscopes.add(new Scope(
					this.getName(),
					getDescription()+" (subscope)",
					new ArrayList<ScopableItem>(){{add(spi);}},
					this.getCategory()));
		}
		return subscopes;
	}

	public void setName(String name) {
		this.name=name;
	}
	public String getName() {
		return name;
	}

}
