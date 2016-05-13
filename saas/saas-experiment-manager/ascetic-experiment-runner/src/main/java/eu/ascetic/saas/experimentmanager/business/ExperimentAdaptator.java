package eu.ascetic.saas.experimentmanager.business;

import java.util.List;
import java.util.stream.Collectors;

import eu.ascetic.saas.experimentmanager.models.Deployment;
import eu.ascetic.saas.experimentmanager.models.Experiment;
import eu.ascetic.saas.experimentmanager.models.Metric;
import eu.ascetic.saas.experimentmanager.models.PhysicalComponent;

public class ExperimentAdaptator {
	
	public static eu.ascetic.saas.experimentmanager.saasKnowledgeBaseClient.model.Experiment getExperiment(Experiment exp){
		// TODO
		return null;
	}
	
	public static List<eu.ascetic.saas.experimentmanager.saasKnowledgeBaseClient.model.VM> getVMs(Deployment depl){
		
		return depl.getComponents().stream().filter(comp->comp instanceof PhysicalComponent).map(pcomp -> {
			eu.ascetic.saas.experimentmanager.saasKnowledgeBaseClient.model.VM vm = new eu.ascetic.saas.experimentmanager.saasKnowledgeBaseClient.model.VM();
			vm.setDescription(pcomp.getDescription());
			vm.setEvents(null); //TODO See if necessary
			vm.setVmId(pcomp.getName());
			return vm;
		}).collect(Collectors.toList());
	}

	
	public static eu.ascetic.saas.experimentmanager.saasKnowledgeBaseClient.model.Metric getMetric(Metric metric){
		eu.ascetic.saas.experimentmanager.saasKnowledgeBaseClient.model.Metric m = new 
				eu.ascetic.saas.experimentmanager.saasKnowledgeBaseClient.model.Metric();
		
		m.setDescription(metric.getDescription());
		m.setName(metric.getName());
		
		return m;
	}
}
