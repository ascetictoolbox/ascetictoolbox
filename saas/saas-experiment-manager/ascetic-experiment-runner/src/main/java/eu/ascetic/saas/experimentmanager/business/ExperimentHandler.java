package eu.ascetic.saas.experimentmanager.business;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;
import java.util.stream.Collectors;

import eu.ascetic.saas.experimentmanager.models.Deployment;
import eu.ascetic.saas.experimentmanager.models.Event;
import eu.ascetic.saas.experimentmanager.models.Experiment;
import eu.ascetic.saas.experimentmanager.models.KPI;
import eu.ascetic.saas.experimentmanager.models.Metric;
import eu.ascetic.saas.experimentmanager.models.Scope;
import eu.ascetic.saas.experimentmanager.paasAPI.InformationProvider;
import eu.ascetic.saas.experimentmanager.saasKnowledgeBaseClient.model.Item;
import eu.ascetic.saas.experimentmanager.saasKnowledgeBaseClient.model.Measure;
import eu.ascetic.saas.experimentmanager.saasKnowledgeBaseClient.model.Snapshot;
import eu.ascetic.saas.experimentmanager.wslayer.exception.WSException;

public class ExperimentHandler {

	private InformationProvider saaSProvider;

	private Logger log = Logger.getLogger(ExperimentHandler.class.getName());
	
	public ExperimentHandler(InformationProvider provider){
		this.saaSProvider = provider;
	}
	
	public Snapshot takeSnapshot(Experiment experiment, String label, String description, String deplId, Map<KPI,List<Scope>> scope) throws Exception{
		Deployment deployment = experiment.getDeployment(deplId);
		
		Snapshot s= new Snapshot();
		s.setDate(new Date());
		s.setDeplId(deployment.getId());
		s.setDescription(description);
		s.setExperimentId(experiment.getName());
		s.setName(label);
		
		s.setVms(ExperimentAdaptator.getVMs(deployment));
		
		s.setMeasures(computeMeasure(experiment,scope));
		
		return s;
	}
	
	private List<Measure> computeMeasure(Experiment exp, Map<KPI,List<Scope>> scopes) throws Exception{
		List <Measure> measures = new ArrayList<>();
		
		for (KPI kpi: exp.getKpis()){
			for (Scope scope: scopes.get(kpi)){
				measures.add(measure(kpi.getMetric(),scope));
			}
		}
		
		return measures;
	}
	
	public Measure measure(Metric metric, Scope s) throws Exception{
		Measure m = new Measure();
		m.setMetric(metric.getName());
		m.setRefersTo(s.getRefersTo().stream().map(str->{
			Item i = new Item();
			i.setCategory(s.getCategory());
			i.setReference(str);
			return i;
		}).collect(Collectors.toList()));
		m.setValue(metric.get(s));
		m.setDescription(metric.getDescription());
		return m;
	}
	
	
	public List<String> getVirtualMachineIds(String applicationId, String deploymentId) throws Exception{
		return saaSProvider.listVirtualMachine(applicationId, deploymentId);
	}
	
	public List<String> getWorkloadIds(String applicationId, String deploymentId) throws Exception{
		return saaSProvider.listEventIds(applicationId, deploymentId);
	}
	
	public InformationProvider getSaaSProvider() {
		return saaSProvider;
	}

	
}