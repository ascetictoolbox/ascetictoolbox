package eu.ascetic.saas.experimentmanager.business;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;
import java.util.stream.Collectors;

import eu.ascetic.saas.experimentmanager.exception.MetricDefinitionIncorrectException;
import eu.ascetic.saas.experimentmanager.exception.NoMeasureException;
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
	
	public Snapshot takeSnapshot(String experimentId, Experiment experiment, String label, String description, String deplName, Map<String,List<Scope>> scope)  throws MetricDefinitionIncorrectException, NoMeasureException{
		Deployment deployment = experiment.getDeployment(deplName);
		
		Snapshot s= new Snapshot();
		s.setDate(new Date());
		s.setDeplId(saaSProvider.getDeploymentId(deployment.getName()));
		s.setDeplName(deployment.getName());
		s.setDescription(description);
		s.setExperimentId(experimentId);
		s.setName(label);
		
		s.setVms(ExperimentAdaptator.getVMs(deployment));
		
		s.setMeasures(computeMeasure(experiment,scope));
		
		return s;
	}
	
	private List<Measure> computeMeasure(Experiment exp, Map<String,List<Scope>> scopes)  throws MetricDefinitionIncorrectException, NoMeasureException{
		List <Measure> measures = new ArrayList<>();
		
		for (KPI kpi: exp.getKpis()){
			if (scopes.containsKey(kpi.getName())){
				for (Scope scope: scopes.get(kpi.getName())){
					measures.add(measure(kpi.getMetric(),scope));
				}
			}
			else{
				Logger.getLogger("Experiment Runner").warning("Missing scope definition for kpi " + kpi.getName());
			}
		}
		
		return measures;
	}
	
	public Measure measure(Metric metric, Scope s) throws MetricDefinitionIncorrectException, NoMeasureException {
		Logger.getLogger("ExperimentHandler").info("computing measure for metric "
				+metric.getName() +  " on scope on size "+s.getScopableItems().size());
		Measure m = new Measure();
		m.setMetric(metric.getName());
		m.setRefersTo(s.getRefersTo().stream().map(str->{
			Item i = new Item();
			i.setCategory(s.getCategory());
			i.setReference(str);
			i.setName(s.getName());
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
