package eu.ascetic.iaas.slamanager.poc.manager.negotiation.translator;

import org.slasoi.slamodel.sla.SLA;
import org.slasoi.slamodel.sla.SLATemplate;

public interface SlaTranslator {

	SLATemplate parseSlaTemplate(String xmlSlat) throws Exception;
	
	String renderSlaTemplate(SLATemplate slat) throws Exception;
	
	SLA parseSla(String xmlSla) throws Exception;
	
	String renderSla(SLA sla) throws Exception;

}
