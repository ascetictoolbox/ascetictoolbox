package eu.ascetic.saas.experimentmanager.factories;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

import eu.ascetic.saas.experimentmanager.models.ScopableItem;
import eu.ascetic.saas.experimentmanager.models.Scope;
import eu.ascetic.saas.experimentmanager.util.Generator;

public class ScopeFactory {
	
	private static List<String> itemLabels = new ArrayList<String>(){{
		add("appId");
		add("deplId");
		add("event");
		add("vmId");
	}};

	public static List<Scope> generateScopes(String scopeCategory, List<String> staticPart, 
			List<String> pivots, LinkedList<LinkedList<String>> variations,List<String> itemLabels){
		Logger.getLogger("ScopeFactory").info("generating scope");
		Generator<String> gen = new Generator<>();
		List<Generator<String>.Pivot> tuples = gen.smartGenerate(staticPart, pivots, variations);
		
		List<Scope> scopes = new ArrayList<>();
		for(Generator<String>.Pivot tuple:tuples){
			List<ScopableItem> items = new ArrayList<>();
			for(List<String> suffixe:tuple.suffixes){
				Map<String,Object> value = new HashMap<>();
				
				for(int i=0;i<tuple.prefix.size();++i){
					value.put(itemLabels.get(i), tuple.prefix.get(i));
				}
				for(int i=0;i<suffixe.size();++i){
					value.put(itemLabels.get(i+tuple.prefix.size()), suffixe.get(i));
				}
				items.add(new ScopableItem(value));
			}
			scopes.add(new Scope("",items,scopeCategory));
		}
		
		return scopes;
	}
	
	
	public static List<Scope> getFullDeploymentScope(String appId, 
			String deplId, List<String> events, List<String> vms){
		LinkedList<LinkedList<String>> axes = new LinkedList<>();
		axes.add(new LinkedList<String>(events));
		axes.add(new LinkedList<String>(vms));
		
		List<String> staticPart=new ArrayList<String>(){{
			add(appId);
		}};
		
		List<String> pivots=new ArrayList<String>(){{
			add(deplId);
		}};
		
		return generateScopes("Deployment", staticPart, 
				pivots,axes,itemLabels);
	}
	
	public static List<Scope> getScopeByEvent(String appId, 
			String deplId, List<String> events, List<String> vms){
		LinkedList<LinkedList<String>> axes = new LinkedList<>();
		axes.add(new LinkedList<String>(vms));
		
		List<String> staticPart=new ArrayList<String>(){{
			add(appId);
			add(deplId);
		}};
		
		List<String> pivots=events;
		
		return generateScopes("Events", staticPart, 
				pivots,axes,itemLabels);
	}
	
	
	
	
	
	
	
}