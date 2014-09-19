package eu.ascetic.paas.applicationmanager.slam;

import org.slasoi.slamodel.sla.SLA;
import org.slasoi.slamodel.sla.SLATemplate;

public interface NegotiationClient {
	
	public SLATemplate[] negotiate(String endpoint, SLATemplate slaTemplate, String negotiationId);
	
	public String initiateNegotiation(String endpoint, SLATemplate slaTemplate);
	
	public SLA createAgreement(String endpoint, SLATemplate slaTemplate, String negotiationId);
	
}
