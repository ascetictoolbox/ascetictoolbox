package eu.ascetic.saas.experimentmanager;


import java.util.List;
import java.util.Map;
import java.util.logging.Logger;
import java.util.stream.Collectors;

import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.context.support.FileSystemXmlApplicationContext;

import eu.ascetic.saas.experimentmanager.business.ExperimentHandler;
import eu.ascetic.saas.experimentmanager.exception.AlreadyExistException;
import eu.ascetic.saas.experimentmanager.exception.MetricDefinitionIncorrectException;
import eu.ascetic.saas.experimentmanager.models.Deployment;
import eu.ascetic.saas.experimentmanager.models.Event;
import eu.ascetic.saas.experimentmanager.models.Experiment;
import eu.ascetic.saas.experimentmanager.models.KPI;
import eu.ascetic.saas.experimentmanager.models.ScopeFilter;
import eu.ascetic.saas.experimentmanager.paasAPI.InformationProvider;
import eu.ascetic.saas.experimentmanager.paasAPI.InformationProviderFactory;
import eu.ascetic.saas.experimentmanager.saasKnowledgeBaseClient.api.ApiException;
import eu.ascetic.saas.experimentmanager.saasKnowledgeBaseClient.client.DefaultApi;
import eu.ascetic.saas.experimentmanager.saasKnowledgeBaseClient.model.Snapshot;

public class API {
	public static Snapshot run(String expId, Experiment exp, String deplName, String description, String scopeDefinitionPath, String runId, String urlToApplicationManager, String urlToApplicationMonitor){
		if ((!scopeDefinitionPath.startsWith("//")) && scopeDefinitionPath.startsWith("/")){
			scopeDefinitionPath = "/"+ scopeDefinitionPath;
		}
		
		Logger.getLogger("Experiment Runner").info("begin snapshot computation...");
		Map<String,ScopeFilter> scopeFilters = getScopeFilters(scopeDefinitionPath);
		
		InformationProvider ip = InformationProviderFactory.getDefaultProvider(urlToApplicationManager, urlToApplicationMonitor);
		ExperimentHandler mi = new ExperimentHandler(ip);
		
		try {
			return mi.takeSnapshot(expId, exp, "A snapshot", description, deplName, runId, scopeFilters);
		} catch (MetricDefinitionIncorrectException e) {
			e.printStackTrace();
			return null;
		}
	}
	
	public static List<Event> getEvents(String filepath){
		if ((!filepath.startsWith("//")) && filepath.startsWith("/")){
			filepath = "/"+ filepath;
		}
		Logger.getLogger("Experiment Runner").info("Loading events from ..." + filepath);
		ApplicationContext context = new FileSystemXmlApplicationContext(filepath);
		return ((List<String>) context.getBean("Events")).stream()
				.map(n -> new Event(n,"")).collect(Collectors.toList());
	}
	
	public static List<KPI> getKPIs(String filepath){
		if ((!filepath.startsWith("//")) && filepath.startsWith("/")){
			filepath = "/"+ filepath;
		}
		Logger.getLogger("Experiment Runner").info("Loading kpis from ..." + filepath);
		ApplicationContext context = new FileSystemXmlApplicationContext(filepath);
		List<KPI> kpis = (List<KPI>) context.getBean("KPIs");
		return kpis;
	}
	
	private static List<Deployment> getDeployment(String filepath){
		if ((!filepath.startsWith("//")) && filepath.startsWith("/")){
			filepath = "/"+ filepath;
		}
		Logger.getLogger("Experiment Runner").info("Loading deployments from ..." + filepath);
		ApplicationContext context = new FileSystemXmlApplicationContext(filepath);
		List<Deployment> depls = (List<Deployment>) context.getBean("Deployments");
		return depls;
	}
	
	public static Map<String,ScopeFilter> getScopeFilters(String filepath){
		if ((!filepath.startsWith("//")) && filepath.startsWith("/")){
			filepath = "/"+ filepath;
		}
		Logger.getLogger("Experiment Runner").info("Loading scopes from ..." + filepath);
		ApplicationContext context = new FileSystemXmlApplicationContext(filepath);
		Map<String,ScopeFilter> scopes = (Map<String,ScopeFilter>) context.getBean("Scopes");
		return scopes;
	}

	public static Experiment createExperiment(String label, String appId, String appName, String description, String eventsFile, String deplsFile, String kpisFile){
		List<Event> events = getEvents(eventsFile);
		List<Deployment> depls = getDeployment(deplsFile);
		List<KPI> kpis = getKPIs(kpisFile);
		
		return new Experiment(label,appId, appName, description, events,depls,kpis);
	}
	
	public static Experiment loadExperiment(String experimentFile, String urlToApplicationManager, String urlToApplicationMonitor) throws Exception{
		if ((!experimentFile.startsWith("//")) && experimentFile.startsWith("/")){
			experimentFile = "/"+ experimentFile;
		}
		ApplicationContext context = new FileSystemXmlApplicationContext(experimentFile);
		
		Experiment exp= (Experiment) context.getBean("Experiment");
		
		InformationProvider ip = InformationProviderFactory.getDefaultProvider(urlToApplicationManager, urlToApplicationMonitor);
		
		for(Deployment depl: exp.getDeployments()){
			depl.populateComponents(ip,exp.getApplicationId());
		}
		
		return exp;
	}
	
	public static String expId(String persistServiceBaseUrl, Experiment exp) throws ApiException{
		
		DefaultApi api = new DefaultApi();
		api.getApiClient().setBasePath(persistServiceBaseUrl);
		List<eu.ascetic.saas.experimentmanager.saasKnowledgeBaseClient.model.Experiment> exps= api.experimentsGet();
		
		for(eu.ascetic.saas.experimentmanager.saasKnowledgeBaseClient.model.Experiment _exp:exps){
			if(_exp.getName().equals(exp.getName())){
				return _exp.getId();
			}
		}
		return null;
	}
	
	private static boolean exists(DefaultApi api, String experimentName) throws ApiException{
		List<eu.ascetic.saas.experimentmanager.saasKnowledgeBaseClient.model.Experiment> exps = api.experimentsGet();
		for (eu.ascetic.saas.experimentmanager.saasKnowledgeBaseClient.model.Experiment exp:exps){
			if(exp.getName().equals(experimentName)){
				return true;
			}
		}
		return false;
	}
	
	public static void persist(String persistServiceBaseUrl, eu.ascetic.saas.experimentmanager.saasKnowledgeBaseClient.model.Experiment exp) throws ApiException, AlreadyExistException{
		Logger.getLogger("Experiment Runner").info("Persisting experiment " + exp.getName() + "...");
		DefaultApi api = new DefaultApi();
		api.getApiClient().setBasePath(persistServiceBaseUrl);
		
		if(exists(api, exp.getName())){
			throw new AlreadyExistException("experiment with name "+exp.getName()+" already exists in the saas knowledge base");
		}
		
		api.experimentsPost(exp);
	}
	
	public static void persist(String persistServiceBaseUrl, Snapshot s) throws ApiException{
		Logger.getLogger("Experiment Runner").info("Persisting snapshot " + s.getName()+ "...");
		DefaultApi api = new DefaultApi();
		api.getApiClient().setBasePath(persistServiceBaseUrl);
		api.snapshotsPost(s);
	}

}
