package eu.ascetic.saas.experimentmanager;


import eu.ascetic.saas.experimentmanager.models.Experiment;
import eu.ascetic.saas.experimentmanager.saasKnowledgeBaseClient.model.Snapshot;

public class Application {

	
	public static void main(String[] args) throws Exception{
		String basePath = "//Users/ddu/Documents/CETIC/PROJECTS/ASCETIC/CODE/WHOLE2/saas/saas-experiment-manager/ascetic-experiment-runner/experiment_configuration_sample/";
		
		String urlToApplicationManager="http://192.168.3.222/application-manager/applications";
		String urlToApplicationMonitor="http://192.168.3.222:9000/query";
		
		Experiment exp = API.createExperiment("News Asset Experiment","newsAsset","News Asset","Major experiment",
				basePath+"events.xml", basePath+"deployments.xml", basePath+"kpis.xml");
		Snapshot s = API.run("dzdqzde55",exp,"490", "This is a test snapshot", basePath+"scopes.xml","14", urlToApplicationManager, urlToApplicationMonitor);
		API.persist("http://localhost:8080",s);
	}

}
