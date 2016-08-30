package eu.ascetic.saas.experimentmanager.models;

import java.util.List;
import java.util.Map;

/**
 * 
 * @author ddu
 *
 * Be care that location map's values shall support the toString() method
 *
 */
public class ScopableItem {
	
	private Map<String,Object> location;
	
	public ScopableItem(){
		
	}
	
	public ScopableItem(List<String> tags, Map<String,Object> location) {
		super();
		this.location = location;
	}
	
	public Map<String,Object> getLocation(){
		return this.location;
	}

	public void setLocation(Map<String,Object> location){
		this.location=location;
	}
	
	public String getReference(){
		StringBuilder sb = new StringBuilder();
		for(Map.Entry<String,Object> e:location.entrySet()){
			sb.append('/');
			sb.append(e.getKey());
			sb.append(':');
			sb.append(e.getValue().toString());
		}
		
		return sb.toString();
	}

	public String toString(){
		return getReference();
	}
}
