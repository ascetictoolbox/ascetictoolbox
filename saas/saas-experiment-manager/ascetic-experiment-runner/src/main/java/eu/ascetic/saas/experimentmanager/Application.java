package eu.ascetic.saas.experimentmanager;


import java.util.ArrayList;
import java.util.HashMap;
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
import eu.ascetic.saas.experimentmanager.factories.ScopeFactory;
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

	
	public static void main(String[] args) throws Exception{
		String basePath = "//Users/ddu/Documents/CETIC/PROJECTS/ASCETIC/CODE/WHOLE2/saas/saas-experiment-manager/ascetic-experiment-runner/experiment_configuration_sample/";
		
		Experiment exp = API.createExperiment("News Asset Experiment","newsAsset","News Asset",
				basePath+"events.xml", basePath+"deployments.xml", basePath+"kpis.xml");
		Snapshot s = API.run(exp,basePath+"scopes.xml");
		API.persist("http://localhost:8080",s);
	}

}
