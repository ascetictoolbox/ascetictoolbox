package eu.ascetic.saas.experimentmanager.business;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import eu.ascetic.saas.experimentmanager.models.Deployment;
import eu.ascetic.saas.experimentmanager.models.Event;
import eu.ascetic.saas.experimentmanager.models.Experiment;
import eu.ascetic.saas.experimentmanager.models.KPI;
import eu.ascetic.saas.experimentmanager.models.Metric;
import eu.ascetic.saas.experimentmanager.models.PhysicalComponent;

public class ExperimentAdaptator {
	
	public static eu.ascetic.saas.experimentmanager.saasKnowledgeBaseClient.model.Experiment getExperiment(Experiment exp){
		eu.ascetic.saas.experimentmanager.saasKnowledgeBaseClient.model.Experiment pexp = new eu.ascetic.saas.experimentmanager.saasKnowledgeBaseClient.model.Experiment();
		
		pexp.setAppId(exp.getApplicationId());
		pexp.setName(exp.getName());
		pexp.setDescription(exp.getDescription());
		pexp.setEvents(getEvents(exp.getEvent()));
		pexp.setKpis(getKPIs(exp.getKpis()));
		return pexp;
	}
	
	public static List<eu.ascetic.saas.experimentmanager.saasKnowledgeBaseClient.model.Event> getEvents(List<Event> events){
		List<eu.ascetic.saas.experimentmanager.saasKnowledgeBaseClient.model.Event> pevents = new ArrayList<>();
		
		for (Event e : events){
			eu.ascetic.saas.experimentmanager.saasKnowledgeBaseClient.model.Event pe = 
					new eu.ascetic.saas.experimentmanager.saasKnowledgeBaseClient.model.Event();
		
			String eventName = e.getName();
			eventName = eventName.substring(0, eventName.lastIndexOf("-"));
			eventName = eventName.substring(0, eventName.lastIndexOf("-"));
			
			pe.setName(eventName);
			pe.setDescription("Launched by : "+e.getLaunchCmd());
			pevents.add(pe);
		}
		
		return pevents;
	}
	
	public static List<eu.ascetic.saas.experimentmanager.saasKnowledgeBaseClient.model.KPI> getKPIs(List<KPI> kpis){
		List<eu.ascetic.saas.experimentmanager.saasKnowledgeBaseClient.model.KPI> pkpis=new ArrayList<>();
		
		for (KPI kpi:kpis){
			eu.ascetic.saas.experimentmanager.saasKnowledgeBaseClient.model.KPI pkpi = new 
					eu.ascetic.saas.experimentmanager.saasKnowledgeBaseClient.model.KPI();
			pkpi.setName(kpi.getName());
			pkpi.setLevel(kpi.getLevel());
			List<eu.ascetic.saas.experimentmanager.saasKnowledgeBaseClient.model.Metric> metrics = new ArrayList<>();
			metrics.add(getMetric(kpi.getMetric()));
			pkpi.setMetrics(metrics);
			pkpi.setDescription(kpi.getDescription());
			pkpi.setUnit(kpi.getUnit());
			pkpis.add(pkpi);
		}
		return pkpis;
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
		m.setType(metric.getType());
		m.setUnit(metric.getUnit());
		return m;
	}
}
