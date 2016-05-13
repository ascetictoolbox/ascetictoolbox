package eu.ascetic.saas.experimentmanager;


import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import eu.ascetic.saas.experimentmanager.business.ExperimentHandler;
import eu.ascetic.saas.experimentmanager.models.Component;
import eu.ascetic.saas.experimentmanager.models.Deployment;
import eu.ascetic.saas.experimentmanager.models.Event;
import eu.ascetic.saas.experimentmanager.models.Experiment;
import eu.ascetic.saas.experimentmanager.models.KPI;
import eu.ascetic.saas.experimentmanager.models.PhysicalComponent;
import eu.ascetic.saas.experimentmanager.models.ScopableItem;
import eu.ascetic.saas.experimentmanager.models.Scope;
import eu.ascetic.saas.experimentmanager.paasAPI.InformationProvider;
import eu.ascetic.saas.experimentmanager.saasKnowledgeBaseClient.api.ApiException;
import eu.ascetic.saas.experimentmanager.saasKnowledgeBaseClient.client.DefaultApi;
import eu.ascetic.saas.experimentmanager.saasKnowledgeBaseClient.model.Snapshot;

public class Application {
	
	public static List<String> vms = new ArrayList<String>(){{
		add("1764");
		add("1765");
		add("1766");
		add("1767");
		add("1768");
	}};
	
	public static List<String> eventNames(){
		ApplicationContext context = new ClassPathXmlApplicationContext("events.xml");
		return (List<String>) context.getBean("Events"); 
	}
	
	
	public static List<Event> getEvents(){
		return eventNames().stream().map(n -> new Event(n,"")).collect(Collectors.toList());
	}
	
	public static List<KPI> getKPIs(){
		ApplicationContext context = new ClassPathXmlApplicationContext("kpis.xml");
		List<KPI> kpis = (List<KPI>) context.getBean("KPIs");
		return kpis;
	}
	
	public static List<Deployment> getDeployment(){
		Deployment depl = new Deployment();
		depl.setId("490");
		depl.setComponents(vms.stream().map(name->new PhysicalComponent(name)).collect(Collectors.toList()));
		return new ArrayList<Deployment>(){{add(depl);}};
	}
		
	public static Experiment getExperiment(String label, String appId, InformationProvider sp){
		Experiment exp = new Experiment(label,appId, getEvents(),getDeployment(),getKPIs());
		return exp;
	}
	
	public static List<Scope> getFullDeploymentScope(String appId, String deplId, List<String> events, List<String> vms){
		List<Scope> scopes = new ArrayList<>();
		List<ScopableItem> items = new ArrayList<ScopableItem>();
		
		for (String event:events){
			for(String vm: vms){
				items.add(new ScopableItem(appId, deplId, vm, event));
			}
		}
		
		scopes.add(new Scope("Scope covering the full deployment item (event and components)",items,"Deployment"));
		return scopes;
	}
	
	public static List<Scope> getScopeByEvent(String appId, String deplId, List<String> events, List<String> vms){
		List<Scope> scopes = new ArrayList<>();
		
		for (String event:events){
			List<ScopableItem> items = new ArrayList<ScopableItem>();
			for(String vm: vms){
				items.add(new ScopableItem(appId, deplId, vm, event));
			}
			
			Scope s = new Scope("Scope covering the full deployment item (event and components)",items,"Deployment");
			scopes.add(s);
		}
		
		
		return scopes;
	}

	public static void main(String[] args) throws Exception{
		ApplicationContext context = new ClassPathXmlApplicationContext("beans.xml");
		ExperimentHandler mi = (ExperimentHandler) context.getBean("MeasureInterceptor");
		
		DefaultApi api = new DefaultApi();
		api.getApiClient().setBasePath("http://localhost:8080");
		
		Experiment exp = getExperiment("News Asset Experiment","newsAsset",mi.getSaaSProvider());
		
		try {
			System.out.println("start");
			List<String> vms = exp.getDeployment("490").getComponents().stream().map(comp -> comp.getName()).collect(Collectors.toList());
			List<Scope> eventScopes = getScopeByEvent("newsAsset", "490", eventNames(), vms);
			Map<KPI,List<Scope>> scopes = new HashMap<>();
			for(KPI kpi:exp.getKpis()){
				scopes.put(kpi,eventScopes);
			}
			Snapshot s = mi.takeSnapshot(exp, "A snapshot", "This is a snapshot", "490",scopes);
			System.out.println("computed");
			api.snapshotsPost(s);
			System.out.println("saved and end");
		} catch (ApiException e) {
			e.printStackTrace();
		}
		
	}

}
