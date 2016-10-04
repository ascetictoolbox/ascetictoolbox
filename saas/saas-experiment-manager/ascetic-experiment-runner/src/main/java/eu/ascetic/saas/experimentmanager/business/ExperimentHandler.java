package eu.ascetic.saas.experimentmanager.business;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

import eu.ascetic.saas.experimentmanager.exception.MetricDefinitionIncorrectException;
import eu.ascetic.saas.experimentmanager.exception.NoMeasureException;
import eu.ascetic.saas.experimentmanager.models.Deployment;
import eu.ascetic.saas.experimentmanager.models.Experiment;
import eu.ascetic.saas.experimentmanager.models.KPI;
import eu.ascetic.saas.experimentmanager.models.Metric;
import eu.ascetic.saas.experimentmanager.models.ScopableItem;
import eu.ascetic.saas.experimentmanager.models.Scope;
import eu.ascetic.saas.experimentmanager.models.ScopeFilter;
import eu.ascetic.saas.experimentmanager.paasAPI.InformationProvider;
import eu.ascetic.saas.experimentmanager.saasKnowledgeBaseClient.model.Item;
import eu.ascetic.saas.experimentmanager.saasKnowledgeBaseClient.model.Measure;
import eu.ascetic.saas.experimentmanager.saasKnowledgeBaseClient.model.Snapshot;

public class ExperimentHandler {

	private InformationProvider saaSProvider;

	private Logger log = Logger.getLogger(ExperimentHandler.class.getName());
	
	public ExperimentHandler(InformationProvider provider){
		this.saaSProvider = provider;
	}
	
	public Snapshot takeSnapshot(String experimentId, Experiment experiment, String label, 
			String description, String deplName, String runId, Map<String,ScopeFilter> scopeFilters)  throws MetricDefinitionIncorrectException{
		Deployment deployment = experiment.getDeployment(deplName);
		
		Snapshot s= new Snapshot();
		s.setDate(new Date());
		s.setDeplId(deployment.getId());
		s.setDeplName(deployment.getName());
		s.setDescription(description);
		s.setExperimentId(experimentId);
		s.setName(label);
		
		s.setVms(ExperimentAdaptator.getVMs(deployment));
		
		s.setMeasures(computeMeasure(experiment,deployment, runId, scopeFilters));
		
		return s;
	}
	
	private List<Measure> computeMeasure(Experiment exp, Deployment deployment, String runId, Map<String,ScopeFilter> scopeFilters)  throws MetricDefinitionIncorrectException{
		List <Measure> measures = new ArrayList<>();
		List<String> computed = new ArrayList<>();
		List<String> notFound = new ArrayList<>();
		for (KPI kpi: exp.getKpis()){
			if (scopeFilters.containsKey(kpi.getName())){
				List<Scope> scopes = scopeFilters.get(kpi.getName()).list(kpi, exp.getApplicationId(), deployment, runId, exp.getEvent());
				
				String log = "Perfoming computeMeasure on KPI " + kpi.getName()+ "\n";
				for (Scope scope: scopes){
					log+=" - scope : "+scope.getRefersTo().toString() + "\n";
				}
				Logger.getLogger("Experiment Runner").info(log);
				for (Scope scope: scopes){
					Measure measure = measure(kpi.getMetric(),scope);
					if(measure != null){
						computed.add("kpi="+kpi.getName()+",metric="+kpi.getMetric().getName()+",scope="+scope.getRefersTo().toString()+",measure="+measure.getValue());
						measures.add(measure);
					}
					else{
						notFound.add("kpi="+kpi.getName()+",metric="+kpi.getMetric().getName()+",scope="+scope.getRefersTo().toString());
						Logger.getLogger("Experiment Runner").warning("Can't properly compute measure for kpi " + kpi.getName() + " on scope "+ scope);
					}
				}
			}
			else{
				Logger.getLogger("Experiment Runner").warning("Missing scope definition for kpi " + kpi.getName());
			}
		}
		
		String report = "********RESULT REPORT:********\n\n**SUCCEED**\n";
		for(String e:computed){
			report+=" - "+e+"\n";
		}
		report+= "\n**FAILED**\n";
		for(String e:notFound){
			report+=" - "+e+"\n";
		}
		Logger.getLogger("Experiment Runner").info(report);
		
		return measures;
	}
	
	public Measure measure(Metric metric, Scope s) throws MetricDefinitionIncorrectException {
		Logger.getLogger("ExperimentHandler").info("computing measure for metric "
				+metric.getName() +  " on scope on size "+s.getScopableItems().size());
		Logger.getLogger("ExperimentHandler").info("List of scopable items :");
		for(ScopableItem si:s.getScopableItems()){
			Logger.getLogger("ExperimentHandler").info(si.getReference());	
		}
		
		Measure m = new Measure();
		m.setMetric(metric.getName());
		m.setRefersTo(s.getRefersTo().stream().map(str->{
			Item i = new Item();
			i.setCategory(s.getCategory());
			i.setReference(str);
			i.setName(s.getName());
			return i;
		}).collect(Collectors.toList()));
		m.setDescription(metric.getDescription());
		
		try {
			m.setValue(metric.get(s));
		} catch (NoMeasureException e) {
			Logger.getLogger("Experiment Runner").log(Level.WARNING,"Can't properly compute measure on scope " + s + " no measure found",e);
			return null;
		}
		
		
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
