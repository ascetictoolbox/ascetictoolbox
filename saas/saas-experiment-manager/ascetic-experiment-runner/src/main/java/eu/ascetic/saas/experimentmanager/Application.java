package eu.ascetic.saas.experimentmanager;


import eu.ascetic.saas.experimentmanager.models.Experiment;
import eu.ascetic.saas.experimentmanager.saasKnowledgeBaseClient.model.Snapshot;

public class Application {

	
	public static void main(String[] args) throws Exception{
		String basePath = "//Users/ddu/Documents/CETIC/PROJECTS/ASCETIC/CODE/WHOLE2/saas/saas-experiment-manager/ascetic-experiment-runner/experiment_configuration_sample/";
		
		Experiment exp = API.createExperiment("News Asset Experiment","newsAsset","News Asset","Major experiment",
				basePath+"events.xml", basePath+"deployments.xml", basePath+"kpis.xml");
		Snapshot s = API.run("dzdqzde55",exp,"490", "This is a test snapshot", basePath+"scopes.xml");
		API.persist("http://localhost:8080",s);
	}

}
