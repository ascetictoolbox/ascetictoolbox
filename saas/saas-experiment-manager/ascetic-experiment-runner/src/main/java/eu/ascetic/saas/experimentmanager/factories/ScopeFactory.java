package eu.ascetic.saas.experimentmanager.factories;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;
import java.util.stream.Collectors;

import eu.ascetic.saas.experimentmanager.models.Component;
import eu.ascetic.saas.experimentmanager.models.Deployment;
import eu.ascetic.saas.experimentmanager.models.Event;
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

	public static List<Scope> generateScopes(String appId, List<String> tags, String scopeCategory, List<String> staticPart, 
			List<String> pivots, LinkedList<LinkedList<String>> variations,List<String> itemLabels){
		Logger.getLogger("ScopeFactory").info("generating scope");
		Generator<String> gen = new Generator<>();
		List<Generator<String>.Pivot> tuples = gen.smartGenerate(staticPart, pivots, variations);
		
		List<Scope> scopes = new ArrayList<>();
		for(Generator<String>.Pivot tuple:tuples){
			List<ScopableItem> items = new ArrayList<>();
			if(!tuple.suffixes.isEmpty()){
				for(List<String> suffixe:tuple.suffixes){
					Map<String,Object> value = new HashMap<>();
					
					for(int i=0;i<tuple.prefix.size();++i){
						value.put(itemLabels.get(i), tuple.prefix.get(i));
					}
					for(int i=0;i<suffixe.size();++i){
						value.put(itemLabels.get(i+tuple.prefix.size()), suffixe.get(i));
					}
					items.add(new ScopableItem(tags,value));
				}
			}
			else{
				Map<String,Object> value = new HashMap<>();
				for(int i=0;i<tuple.prefix.size();++i){
					value.put(itemLabels.get(i), tuple.prefix.get(i));
				}
				items.add(new ScopableItem(tags,value));
			}
			
			scopes.add(new Scope(appId,"",items,scopeCategory));
		}
		
		return scopes;
	}
	
	
	public static List<Scope> getFullDeploymentScope(String appId, 
			Deployment deployment, List<Event> events){
		LinkedList<String> componentNames = new LinkedList<String>();
		for (Component component:deployment.getComponents()){
			componentNames.add(component.getName());
		}
		
		LinkedList<String> eventNames = new LinkedList<String>();
		for (Event event:events){
			eventNames.add(event.getName());
		}
		
		LinkedList<LinkedList<String>> axes = new LinkedList<>();
		axes.add(eventNames);
		axes.add(componentNames);
		
		List<String> staticPart=new ArrayList<String>(){{
			add(appId);
		}};
		
		List<String> pivots=new ArrayList<String>(){{
			add(deployment.getId());
		}};
		

		List<String> tags=new ArrayList<String>(){{
			add("deployment");
		}};
		
		return generateScopes(appId,tags,"Deployment", staticPart, 
				pivots,axes,itemLabels);
	}
	
	public static List<Scope> getScopeByEvent(String appId, 
			Deployment deployment, List<Event> events
			){
		LinkedList<LinkedList<String>> axes = new LinkedList<>();
		axes.add(new LinkedList<String>(
				deployment.getComponents().stream().map(c -> c.getName()).collect(Collectors.toList())));
		
		List<String> staticPart=new ArrayList<String>(){{
			add(appId);
			add(deployment.getId());
		}};
		
		List<String> pivots=events.stream()
				.map(e -> e.getName()).collect(Collectors.toList());
		
		List<String> tags=new ArrayList<String>(){{
			add("event");
		}};
		return generateScopes(appId,tags,"Events", staticPart, 
				pivots,axes,itemLabels);
	}
	
	public static List<Scope> getScopeByEventNoVM(String appId, 
			Deployment deployment, List<Event> events
			){
		LinkedList<LinkedList<String>> axes = new LinkedList<>();
		
		List<String> staticPart=new ArrayList<String>(){{
			add(appId);
			add(deployment.getId());
		}};
		
		List<String> pivots=events.stream()
				.map(e -> e.getName()).collect(Collectors.toList());
		
		List<String> tags=new ArrayList<String>(){{
			add("event");
		}};
		return generateScopes(appId,tags,"Events", staticPart, 
				pivots,axes,itemLabels);
	}
	
	
	
}
