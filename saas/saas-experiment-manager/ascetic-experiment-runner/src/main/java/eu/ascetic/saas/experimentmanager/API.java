package eu.ascetic.saas.experimentmanager;


import java.util.List;
import java.util.Map;
import java.util.logging.Logger;
import java.util.stream.Collectors;

import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.context.support.FileSystemXmlApplicationContext;

import eu.ascetic.saas.experimentmanager.business.ExperimentHandler;
import eu.ascetic.saas.experimentmanager.exception.MetricDefinitionIncorrectException;
import eu.ascetic.saas.experimentmanager.exception.NoMeasureException;
import eu.ascetic.saas.experimentmanager.models.Deployment;
import eu.ascetic.saas.experimentmanager.models.Event;
import eu.ascetic.saas.experimentmanager.models.Experiment;
import eu.ascetic.saas.experimentmanager.models.KPI;
import eu.ascetic.saas.experimentmanager.models.Scope;
import eu.ascetic.saas.experimentmanager.saasKnowledgeBaseClient.api.ApiException;
import eu.ascetic.saas.experimentmanager.saasKnowledgeBaseClient.client.DefaultApi;
import eu.ascetic.saas.experimentmanager.saasKnowledgeBaseClient.model.Snapshot;

public class API {
	public static Snapshot run(Experiment exp, String scopeDefinitionPath){
		Logger.getLogger("Experiment Runner").info("begin snapshot computation...");
		Map<String,List<Scope>> scopedefinition = getScope(scopeDefinitionPath);
		
		ApplicationContext context = new ClassPathXmlApplicationContext("beans.xml");
		ExperimentHandler mi = (ExperimentHandler) context.getBean("MeasureInterceptor");
		
		try {
			return mi.takeSnapshot(exp, "A snapshot", "This is a snapshot", "490",scopedefinition);
		} catch (NoMeasureException e) {
			e.printStackTrace();
			return null;
		} catch (MetricDefinitionIncorrectException e) {
			e.printStackTrace();
			return null;
		}
	}
	
	public static List<Event> getEvents(String filepath){
		Logger.getLogger("Experiment Runner").info("Loading events from ..." + filepath);
		ApplicationContext context = new FileSystemXmlApplicationContext(filepath);
		return ((List<String>) context.getBean("Events")).stream()
				.map(n -> new Event(n,"")).collect(Collectors.toList());
	}
	
	public static List<KPI> getKPIs(String filepath){
		Logger.getLogger("Experiment Runner").info("Loading kpis from ..." + filepath);
		ApplicationContext context = new FileSystemXmlApplicationContext(filepath);
		List<KPI> kpis = (List<KPI>) context.getBean("KPIs");
		return kpis;
	}
	
	public static List<Deployment> getDeployment(String filepath){
		Logger.getLogger("Experiment Runner").info("Loading deployments from ..." + filepath);
		ApplicationContext context = new FileSystemXmlApplicationContext(filepath);
		List<Deployment> depls = (List<Deployment>) context.getBean("Deployments");
		return depls;
	}
	
	public static Map<String,List<Scope>> getScope(String filepath){
		Logger.getLogger("Experiment Runner").info("Loading scopes from ..." + filepath);
		ApplicationContext context = new FileSystemXmlApplicationContext(filepath);
		Map<String,List<Scope>> scopes = (Map<String,List<Scope>>) context.getBean("Scopes");
		return scopes;
	}

	public static Experiment createExperiment(String label, String appId, String eventsFile, String deplsFile, String kpisFile){
		List<Event> events = getEvents(eventsFile);
		List<Deployment> depls = getDeployment(deplsFile);
		List<KPI> kpis = getKPIs(kpisFile);
		
		return new Experiment(label,appId, events,depls,kpis);
	}
	
	
	public static void persist(String persistServiceBaseUrl, eu.ascetic.saas.experimentmanager.saasKnowledgeBaseClient.model.Experiment exp) throws ApiException{
		Logger.getLogger("Experiment Runner").info("Persisting experiment " + exp.getName() + "...");
		DefaultApi api = new DefaultApi();
		api.getApiClient().setBasePath(persistServiceBaseUrl);
		api.experimentsPost(exp);
	}
	
	public static void persist(String persistServiceBaseUrl, Snapshot s) throws ApiException{
		Logger.getLogger("Experiment Runner").info("Persisting snapshot " + s.getName()+ "...");
		DefaultApi api = new DefaultApi();
		api.getApiClient().setBasePath(persistServiceBaseUrl);
		api.snapshotsPost(s);
	}

}
