package eu.ascetic.iaas.slamanager.poc.manager.negotiation.translator;

import org.slasoi.gslam.core.negotiation.ISyntaxConverter.SyntaxConverterType;
import org.slasoi.gslam.syntaxconverter.SyntaxConverterDelegator;
import org.slasoi.slamodel.sla.SLA;
import org.slasoi.slamodel.sla.SLATemplate;

public class SlaTranslatorImpl implements SlaTranslator{

	@Override
	public SLATemplate parseSlaTemplate(String xmlSlat) throws Exception {
		return (SLATemplate) syntaxConvDelegator.parseSLATemplate(xmlSlat);
	}

	@Override
	public String renderSlaTemplate(SLATemplate slat) throws Exception {
		return syntaxConvDelegator.renderSLATemplate(slat);
	}

	@Override
	public SLA parseSla(String xmlSla) throws Exception {
		return (SLA) syntaxConvDelegator.parseSLA(xmlSla);
	}

	@Override
	public String renderSla(SLA sla) throws Exception {
		return syntaxConvDelegator.renderSLA(sla);
	}

	public SlaTranslatorImpl() {
		syntaxConvDelegator = new SyntaxConverterDelegator(SyntaxConverterType.SLASOISyntaxConverter);
	}
	
	
	private static SyntaxConverterDelegator syntaxConvDelegator;

}
