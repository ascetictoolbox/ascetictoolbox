package eu.ascetic.saas.experimentmanager;

import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.junit.Ignore;
import org.junit.Test;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import eu.ascetic.saas.experimentmanager.business.ExperimentHandler;
import eu.ascetic.saas.experimentmanager.factories.ScopeFactory;
import eu.ascetic.saas.experimentmanager.models.Deployment;
import eu.ascetic.saas.experimentmanager.models.Event;
import eu.ascetic.saas.experimentmanager.models.Experiment;
import eu.ascetic.saas.experimentmanager.models.KPI;
import eu.ascetic.saas.experimentmanager.models.PhysicalComponent;
import eu.ascetic.saas.experimentmanager.models.Scope;
import eu.ascetic.saas.experimentmanager.paasAPI.InformationProvider;
import eu.ascetic.saas.experimentmanager.saasKnowledgeBaseClient.api.ApiException;
import eu.ascetic.saas.experimentmanager.saasKnowledgeBaseClient.client.DefaultApi;
import eu.ascetic.saas.experimentmanager.saasKnowledgeBaseClient.model.Snapshot;

/**
 * Caution ! Test needs the whole environnement to be deployed 
 * 
 * @author ddu
 *
 */
@Ignore
public class FunctionalTest {
	
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
		ApplicationContext context = new ClassPathXmlApplicationContext("deployments.xml");
		List<Deployment> deployments = (List<Deployment>) context.getBean("Deployments");
		return deployments;
	}
		
	public static Experiment getExperiment(String label, String appId, String appName, InformationProvider sp){
		Experiment exp = new Experiment(label,appId, appName, getEvents(),getDeployment(),getKPIs());
		return exp;
	}

	@Test
	public void test() throws Exception {
		ApplicationContext context = new ClassPathXmlApplicationContext("beans.xml");
		ExperimentHandler mi = (ExperimentHandler) context.getBean("MeasureInterceptor");
		
		DefaultApi api = new DefaultApi();
		api.getApiClient().setBasePath("http://localhost:8080");
		
		Experiment exp = getExperiment("News Asset Experiment","newsAsset", "News Asset",mi.getSaaSProvider());
		
		try {
			System.out.println("start");
			List<String> vms = exp.getDeployment("490").getComponents().stream().map(comp -> comp.getName()).collect(Collectors.toList());
			List<Scope> eventScopes = ScopeFactory.getScopeByEvent("newsAsset", "490", exp.getEvent(), 
					exp.getDeployment("490").getComponents());
			System.out.println("number of scopes : "+ eventScopes.size());
			Map<String,List<Scope>> scopes = new HashMap<>();
			for(KPI kpi:exp.getKpis()){
				scopes.put(kpi.getName(),eventScopes);
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
