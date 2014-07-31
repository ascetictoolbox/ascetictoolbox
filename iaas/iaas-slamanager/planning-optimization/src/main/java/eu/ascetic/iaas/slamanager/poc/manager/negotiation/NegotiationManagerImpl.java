package eu.ascetic.iaas.slamanager.poc.manager.negotiation;

import java.io.FileReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import org.apache.log4j.Logger;
import org.codehaus.jettison.json.JSONArray;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;
import org.slasoi.infrastructure.monitoring.jpa.managers.VmManager;
import org.slasoi.slamodel.sla.SLATemplate;
import org.slasoi.slamodel.vocab.units;
import org.slasoi.slamodel.vocab.xsd;
import org.springframework.beans.factory.annotation.Autowired;

import com.sun.jersey.api.client.ClientResponse;

import eu.ascetic.iaas.slamanager.poc.enums.AsceticAgreementTerm;
import eu.ascetic.iaas.slamanager.poc.enums.OperatorType;
import eu.ascetic.iaas.slamanager.poc.exceptions.NotSupportedVEPOperationException;
import eu.ascetic.iaas.slamanager.poc.manager.resource.VMResourceManager;
import eu.ascetic.iaas.slamanager.poc.slatemplate.AsceticSlaTemplate;
import eu.ascetic.iaas.slamanager.poc.slatemplate.SlaTemplateBuilder;
import eu.ascetic.iaas.slamanager.poc.slatemplate.parser.AsceticSlaTemplateParser;
import eu.ascetic.iaas.slamanager.poc.slatemplate.request.VMManagerEstimatesRequestObject;
import eu.ascetic.iaas.slamanager.poc.slatemplate.request.VirtualSystem;
import eu.ascetic.iaas.slamanager.poc.slatemplate.request.guarantee.GenericGuarantee;
import eu.ascetic.iaas.slamanager.poc.slatemplate.request.guarantee.GenericGuarantee.Value;

public class NegotiationManagerImpl implements NegotiationManager {
	
	@Autowired
	AsceticSlaTemplateParser asceticSlaTemplateParser;
	
	private static final Logger logger = Logger.getLogger(NegotiationManagerImpl.class.getName());
	
	VMResourceManager vmResourceManager;
	
	private static final String sepr = System.getProperty("file.separator");

	private static final String confPath = System.getenv("SLASOI_HOME");

	private static final String configFile = confPath + sepr + "ascetic-iaas-slamanager" + sepr +

	"planning-optimization" + sepr + "planning_optimization.properties";

	private static Properties configProp;
	
	public NegotiationManagerImpl(){
		logger.info("Initilalize Negotiation Manager");
		init();
	}
	
	public void init(){
		vmResourceManager = VMResourceManager.getInstance();
		config(configFile);
		String url = null;
		if (getProperty("trustStore") != null) {
			url = "https://" + getProperty("vm-manager-address") + ":" + getProperty("vm-manager-port") + "/" + getProperty("vm-manager-base-path");
			vmResourceManager.configManager(true, getProperty("trustStore"), getProperty("trustStorePassword"));
			vmResourceManager.setBasePath(url);
		} else {
			url = "http://" + getProperty("vm-manager-address") + ":" + getProperty("vm-manager-port") + "/" + getProperty("vm-manager-base-path");
			vmResourceManager.configManager(false, null, null);
			vmResourceManager.setBasePath(url);
		}
		System.out.println("vep address " + url);
		System.out.println("vep connection configured");
	}

	@Override
	public SLATemplate negotiate(SLATemplate templateInitial, String negotiationID) throws NotSupportedVEPOperationException {
		logger.debug("########## NEGOTIATE #########");
		logger.debug("Sla Template to Negotiate:");
		logger.debug(templateInitial);
		
		AsceticSlaTemplate asceticSlaTemplate = asceticSlaTemplateParser.getAsceticSlat(templateInitial);
		
		VMManagerEstimatesRequestObject.Builder builder=new VMManagerEstimatesRequestObject.Builder();
		builder.setAsceticSlatemplate(asceticSlaTemplate);
		VMManagerEstimatesRequestObject vmEstimatesRequestObject=builder.build();
		
		ClientResponse vmReply;
		try {
			vmReply = vmResourceManager.estimates(vmEstimatesRequestObject.getRequestJson());
			JSONObject jsonResp = null;
			String res = vmReply.getEntity(String.class);
			logger.debug("Response status code: " + vmReply.getStatus());
			logger.debug("Reply from VM Manager: " + res);
			if (vmReply.getStatus() >= 200 && vmReply.getStatus() < 300) {
				jsonResp = new JSONObject(res);
				JSONArray estimatesArray = jsonResp.optJSONArray("estimates");
				if (estimatesArray != null) {
					for (int i = 0; i < estimatesArray.length(); i++) {
						JSONObject vs = estimatesArray.getJSONObject(i);
						String vmId=vs.optString("id");
						String vmPower=vs.optString("powerEstimate");
						String vmPrice=vs.optString("priceEstimate");
						VirtualSystem virtSys=asceticSlaTemplate.getVirtualSystem(vmId);
						for(GenericGuarantee gg : virtSys.getGenericGuarantees()){
							if(gg.getAgreementTerm().equals(AsceticAgreementTerm.power_usage_per_vm)){
								List<Value> values=new ArrayList<Value>();
								Value v=gg.new Value(units.$W, vmPower, OperatorType.EQUALS);
								values.add(v);
								gg.setValues(values);
								break;
							}
						}
						virtSys.setPrice(new Double(vmPrice).doubleValue());
					}
				}
			} 
			else {
				jsonResp = new JSONObject(res);
				String errMsg = jsonResp.optString("error");
				throw new NotSupportedVEPOperationException(errMsg);
			}
		} catch (JSONException e) {
			logger.debug(e.getMessage());
		} catch (NotSupportedVEPOperationException e) {
			logger.debug(e.getMessage());
			throw new NotSupportedVEPOperationException(e.getMessage());
		}
		SlaTemplateBuilder slaTemplateBuilder=new SlaTemplateBuilder();
		slaTemplateBuilder.setAsceticSlatemplate(asceticSlaTemplate);
		
		return slaTemplateBuilder.build();
	}

	@Override
	public SLATemplate createAgreement(SLATemplate templateAccepted, String negotiationID) {
		logger.debug("########## CREATE AGREEMENT #########");
		logger.debug("Sla Template to Negotiate:");
		logger.debug(templateAccepted);
		
		AsceticSlaTemplate asceticSlaTemplate = asceticSlaTemplateParser.getAsceticSlat(templateAccepted);
		
		VMManagerEstimatesRequestObject.Builder builder=new VMManagerEstimatesRequestObject.Builder();
		builder.setAsceticSlatemplate(asceticSlaTemplate);
		VMManagerEstimatesRequestObject vmEstimatesRequestObject=builder.build();
		
		ClientResponse vmReply;
		try {
			vmReply = vmResourceManager.estimates(vmEstimatesRequestObject.getRequestJson());
			JSONObject jsonResp = null;
			String res = vmReply.getEntity(String.class);
			logger.debug("Response status code: " + vmReply.getStatus());
			logger.debug("Reply from VM Manager: " + res);
			if (vmReply.getStatus() >= 200 && vmReply.getStatus() < 300) {
				jsonResp = new JSONObject(res);
				JSONArray estimatesArray = jsonResp.optJSONArray("estimates");
				if (estimatesArray != null) {
					for (int i = 0; i < estimatesArray.length(); i++) {
						JSONObject vs = estimatesArray.getJSONObject(i);
						String vmId=vs.optString("id");
						String vmPower=vs.optString("powerEstimate");
						String vmPrice=vs.optString("priceEstimate");
						VirtualSystem virtSys=asceticSlaTemplate.getVirtualSystem(vmId);
						for(GenericGuarantee gg : virtSys.getGenericGuarantees()){
							if(gg.getAgreementTerm().equals(AsceticAgreementTerm.power_usage_per_vm)){
								List<Value> values=new ArrayList<Value>();
								Value v=gg.new Value(units.$W, vmPower, OperatorType.EQUALS);
								values.add(v);
								gg.setValues(values);
								break;
							}
						}
						virtSys.setPrice(new Double(vmPrice).doubleValue());
					}
				}
			} 
			else {
				jsonResp = new JSONObject(res);
				String errMsg = jsonResp.optString("error");
				throw new NotSupportedVEPOperationException(errMsg);
			}
		} catch (JSONException e) {
			logger.debug(e.getMessage());
		} catch (NotSupportedVEPOperationException e) {
			logger.debug(e.getMessage());
		}
		SlaTemplateBuilder slaTemplateBuilder=new SlaTemplateBuilder();
		slaTemplateBuilder.setAsceticSlatemplate(asceticSlaTemplate);
		
		return slaTemplateBuilder.build();
	}
	
	
	
	private void config(String filename) {
		configProp = new java.util.Properties();
		try {
			configProp.load(new FileReader(filename));
		} catch (Exception eta) {
			logger.debug(eta.getMessage());
		}
	}

	private static String getProperty(String key) {
		String value = configProp.getProperty(key);
		return value;
	}

}
