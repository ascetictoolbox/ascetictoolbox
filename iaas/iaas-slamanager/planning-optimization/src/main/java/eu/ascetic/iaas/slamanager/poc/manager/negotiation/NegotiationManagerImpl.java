/**
 *  Copyright 2014 Hewlett-Packard Development Company, L.P.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package eu.ascetic.iaas.slamanager.poc.manager.negotiation;

import java.io.FileReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import org.apache.log4j.Logger;
import org.codehaus.jettison.json.JSONArray;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;
import org.slasoi.slamodel.core.ConstraintExpr;
import org.slasoi.slamodel.core.FunctionalExpr;
import org.slasoi.slamodel.core.SimpleDomainExpr;
import org.slasoi.slamodel.core.TypeConstraintExpr;
import org.slasoi.slamodel.primitives.CONST;
import org.slasoi.slamodel.primitives.ID;
import org.slasoi.slamodel.primitives.STND;
import org.slasoi.slamodel.primitives.ValueExpr;
import org.slasoi.slamodel.sla.AgreementTerm;
import org.slasoi.slamodel.sla.Guaranteed;
import org.slasoi.slamodel.sla.Guaranteed.State;
import org.slasoi.slamodel.sla.InterfaceDeclr;
import org.slasoi.slamodel.sla.SLATemplate;
import org.slasoi.slamodel.sla.VariableDeclr;
import org.slasoi.slamodel.vocab.xsd;

import com.sun.jersey.api.client.ClientResponse;

import eu.ascetic.iaas.slamanager.poc.enums.AsceticAgreementTerm;
import eu.ascetic.iaas.slamanager.poc.enums.OperatorType;
import eu.ascetic.iaas.slamanager.poc.exceptions.NotSupportedVEPOperationException;
import eu.ascetic.iaas.slamanager.poc.manager.resource.OVFRetriever;
import eu.ascetic.iaas.slamanager.poc.manager.resource.OvfResourceParser;
import eu.ascetic.iaas.slamanager.poc.manager.resource.VMResourceManager;
import eu.ascetic.iaas.slamanager.poc.slatemplate.AsceticSlaTemplate;
import eu.ascetic.iaas.slamanager.poc.slatemplate.SlaTemplateBuilder;
import eu.ascetic.iaas.slamanager.poc.slatemplate.parser.AsceticSlaTemplateParser;
import eu.ascetic.iaas.slamanager.poc.slatemplate.request.VMManagerEstimatesRequestObject;
import eu.ascetic.iaas.slamanager.poc.slatemplate.request.VirtualSystem;
import eu.ascetic.iaas.slamanager.poc.slatemplate.request.guarantee.GenericGuarantee;
import eu.ascetic.iaas.slamanager.poc.slatemplate.request.guarantee.GenericGuarantee.Value;

public class NegotiationManagerImpl implements NegotiationManager {

	AsceticSlaTemplateParser asceticSlaTemplateParser;

	private static final Logger logger = Logger.getLogger(NegotiationManagerImpl.class.getName());

	VMResourceManager vmResourceManager;

	private static final String sepr = System.getProperty("file.separator");

	private static final String confPath = System.getenv("SLASOI_HOME");

	private static final String configFile = confPath + sepr + "ascetic-iaas-slamanager" + sepr +

			"planning-optimization" + sepr + "planning_optimization.properties";

	private static Properties configProp;

	private static  List<AgreementTerm> paaSAgreementTerms = new ArrayList<AgreementTerm>();


	public NegotiationManagerImpl() {
		logger.info("Initilalize Negotiation Manager");
		init();
	}

	public void init() {
		vmResourceManager = VMResourceManager.getInstance();
		asceticSlaTemplateParser=new AsceticSlaTemplateParser();
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
		System.out.println("VM Manager address " + url);
		System.out.println("VM Manager connection configured");
	}

	@Override
	public SLATemplate negotiate(SLATemplate templateInitial, String negotiationID) throws NotSupportedVEPOperationException {
		logger.debug("########## NEGOTIATE #########");
		logger.debug("Sla Template to Negotiate:");
		logger.debug(templateInitial);

		//PaaS-IaaS translation
		templateInitial = translatePaasTerms(templateInitial, true);


		AsceticSlaTemplate asceticSlaTemplate = asceticSlaTemplateParser.getAsceticSlat(templateInitial);

		SLATemplate slaReturn = templateInitial;
		
		/*
		 * TODO
		 * Chiamare il VMM solo se esistono termini da negoziare lato IaaS
		 * ANCHE NEL CREATE AGREEMENT!!!
		 */
		if (true) {
		
		VMManagerEstimatesRequestObject.Builder builder = new VMManagerEstimatesRequestObject.Builder();
		builder.setAsceticSlatemplate(asceticSlaTemplate);
		VMManagerEstimatesRequestObject vmEstimatesRequestObject = builder.build();

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
						String vmId = vs.optString("id");
						String vmPower = vs.optString("powerEstimate");
						String vmPrice = vs.optString("priceEstimate");
						VirtualSystem virtSys = asceticSlaTemplate.getVirtualSystem(vmId);
						for (GenericGuarantee gg : virtSys.getGenericGuarantees()) {
							if (gg.getAgreementTerm().equals(AsceticAgreementTerm.power_usage_per_vm)) {
								List<Value> values = new ArrayList<Value>();
								Value v = gg.new Value(xsd.watt.toString(), vmPower, OperatorType.EQUALS);
								values.add(v);
								gg.setValues(values);
								break;
							}
						}
						virtSys.setPrice(new Double(vmPrice).doubleValue());
					}
				}
			} else {
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
		SlaTemplateBuilder slaTemplateBuilder = new SlaTemplateBuilder();
		slaTemplateBuilder.setAsceticSlatemplate(asceticSlaTemplate);
		slaReturn = slaTemplateBuilder.build();

		}
		
		
		logger.debug("SLA to return:");
		logger.debug(slaReturn);

		/*
		 * riaggiungere i termini PaaS precedentemente tolti
		 */
		if (paaSAgreementTerms!=null && paaSAgreementTerms.size()>0) {
			AgreementTerm[] slaTerms = slaReturn.getAgreementTerms();
			List<AgreementTerm> finalTerms = new ArrayList<AgreementTerm>();
			if (slaTerms!=null) {
				for (int i = 0; i < slaTerms.length; i++) {
					finalTerms.add(slaTerms[i]);
				}		
			}


			finalTerms.addAll(paaSAgreementTerms);


			AgreementTerm[] finalArr = new AgreementTerm[finalTerms.size()];
			finalArr = finalTerms.toArray(finalArr);
			slaReturn.setAgreementTerms(finalArr);
			logger.debug("SLA to return after PaaS-IaaS terms translation:");
			logger.debug(slaReturn);
		}
		//empty list
		paaSAgreementTerms = new ArrayList<AgreementTerm>();
		/*
		 * fine
		 */

		return slaReturn;
	}

	@Override
	public SLATemplate createAgreement(SLATemplate templateAccepted, String negotiationID) {
		logger.debug("########## CREATE AGREEMENT #########");
		logger.debug("Sla Template to Negotiate:");
		logger.debug(templateAccepted);

		//PaaS-IaaS terms translation
		templateAccepted = translatePaasTerms(templateAccepted, false);
		
		AsceticSlaTemplate asceticSlaTemplate = asceticSlaTemplateParser.getAsceticSlat(templateAccepted);

		VMManagerEstimatesRequestObject.Builder builder = new VMManagerEstimatesRequestObject.Builder();
		builder.setAsceticSlatemplate(asceticSlaTemplate);
		VMManagerEstimatesRequestObject vmEstimatesRequestObject = builder.build();

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
						String vmId = vs.optString("id");
						String vmPower = vs.optString("powerEstimate");
						String vmPrice = vs.optString("priceEstimate");
						VirtualSystem virtSys = asceticSlaTemplate.getVirtualSystem(vmId);
						for (GenericGuarantee gg : virtSys.getGenericGuarantees()) {
							if (gg.getAgreementTerm().equals(AsceticAgreementTerm.power_usage_per_vm)) {
								List<Value> values = new ArrayList<Value>();
								Value v = gg.new Value(xsd.watt.toString(), vmPower, OperatorType.EQUALS);
								values.add(v);
								gg.setValues(values);
								break;
							}
						}
						virtSys.setPrice(new Double(vmPrice).doubleValue());
					}
				}
			} else {
				jsonResp = new JSONObject(res);
				String errMsg = jsonResp.optString("error");
				throw new NotSupportedVEPOperationException(errMsg);
			}
		} catch (JSONException e) {
			logger.debug(e.getMessage());
		} catch (NotSupportedVEPOperationException e) {
			logger.debug(e.getMessage());
		}
		SlaTemplateBuilder slaTemplateBuilder = new SlaTemplateBuilder();
		slaTemplateBuilder.setAsceticSlatemplate(asceticSlaTemplate);
		SLATemplate slaReturn = slaTemplateBuilder.build();

		logger.debug("SLA to return:");
		logger.debug(slaReturn);
		
		/*
		 * riaggiungere i termini PaaS precedentemente tolti
		 */
		if (paaSAgreementTerms!=null && paaSAgreementTerms.size()>0) {
			AgreementTerm[] slaTerms = slaReturn.getAgreementTerms();
			List<AgreementTerm> finalTerms = new ArrayList<AgreementTerm>();
			if (slaTerms!=null) {
				for (int i = 0; i < slaTerms.length; i++) {
					finalTerms.add(slaTerms[i]);
				}		
			}


			finalTerms.addAll(paaSAgreementTerms);


			AgreementTerm[] finalArr = new AgreementTerm[finalTerms.size()];
			finalArr = finalTerms.toArray(finalArr);
			slaReturn.setAgreementTerms(finalArr);
			logger.debug("SLA to return after PaaS-IaaS terms translation:");
			logger.debug(slaReturn);
		}
		//empty list
		paaSAgreementTerms = new ArrayList<AgreementTerm>();
		/*
		 * fine
		 */
		
		return slaReturn;
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

	private SLATemplate translatePaasTerms(SLATemplate templateInitial, boolean isNegotiation) {
		/*
		 * rimuovere term power_usage_per_app, aggiungere tanti terms power_usage_per_vm quante sono le vm sull'ovf
		 */
		AgreementTerm[] terms = templateInitial.getAgreementTerms();
		List<AgreementTerm> newTerms = new ArrayList<AgreementTerm>();
		if (terms!=null) {
			for (int i = 0; i < terms.length; i++) {
				logger.debug("Term "+i+": "+terms[i]);
				
				//termini da nascondere (no translation)
				if (terms[i].toString().contains("power_usage_per_event")
				|| terms[i].toString().contains("energy_usage_per_event")
				|| terms[i].toString().contains("app_price_for_next_hour")
				|| terms[i].toString().contains("aggregated_event_metric_over_period")) 
				{
					logger.debug("Found a PaaS layer term. Hiding...");
					paaSAgreementTerms.add(terms[i]);
					continue;
				}
					
				//termini da tradurre
				if (terms[i].toString().contains("power_usage_per_app")
				|| terms[i].toString().contains("energy_usage_per_app")) 
				{
					if (isNegotiation) {
						logger.debug("Found a PaaS layer term. Translating it into an IaaS layer term...");
						paaSAgreementTerms.add(terms[i]);
						String type = "";
						if (terms[i].toString().contains("power_usage_per_app"))  type = "power";
						else if (terms[i].toString().contains("energy_usage_per_app"))  type = "energy";

						InterfaceDeclr[] ids = templateInitial.getInterfaceDeclrs();

						String ovfFile = "";
						for (int j = 0; j < ids.length; j++) {
							InterfaceDeclr iD = ids[j];
							STND[] propKey = iD.getPropertyKeys();
							if (propKey != null && propKey.length != 0) {
								for (int l = 0; l < propKey.length; l++) {
									if (propKey[l].equals("OVF_URL")) {
										String ovfURL = iD.getPropertyValue(propKey[l]);
										OVFRetriever ovfRetriever = new OVFRetriever();
										ovfRetriever.retrieveOvf(ovfURL);
										ovfFile = ovfRetriever.getFilename();
										logger.debug("Looking at ovfFile "+ovfFile);
										break;
									}
								}
							}
						}
						OvfResourceParser ovfParser = new OvfResourceParser(ovfFile);
						for (eu.ascetic.utils.ovf.api.VirtualSystem virtualSystem:ovfParser.getVirtualSystems()) {
							logger.debug("Found Virtual System "+virtualSystem.getId()+ " in the OVF.");


							//ID for our terms
							ID id = new ID(virtualSystem.getId() + "_Guarantees");

							// Variable Declarations		
							VariableDeclr variableDeclr = new VariableDeclr(
									new ID("VM_of_type_" + virtualSystem.getId()),
									new FunctionalExpr(
											new STND("http://www.slaatsoi.org/coremodel#subset_of"), 
											new ValueExpr[]{new ID("OVF-Item-" + virtualSystem.getId())})					
									);

							// Energy requirements
							FunctionalExpr functionalExprePowerUsage = new FunctionalExpr(
									new STND("http://www.slaatsoi.org/resources#"+type+"_usage_per_vm"), 
									new ValueExpr[]{new ID("VM_of_type_" + virtualSystem.getId())});


							Guaranteed g = terms[i].getGuarantees()[0];
							TypeConstraintExpr tce = (TypeConstraintExpr)(((Guaranteed.State)g).getState());
							SimpleDomainExpr sde = (SimpleDomainExpr)tce.getDomain();

							CONST originalValue = (CONST) sde.getValue();
							Double mathValue = Double.parseDouble(originalValue.getValue());

							mathValue = mathValue / ovfParser.getVirtualSystems().length;

							SimpleDomainExpr simpleDomainExprePowerUsage = new SimpleDomainExpr(
									new CONST(""+mathValue, new STND("http://www.w3.org/2001/XMLSchema#watt")), 
									sde.getComparisonOp());

							TypeConstraintExpr typeConstraintExprVMCores = new TypeConstraintExpr(functionalExprePowerUsage, simpleDomainExprePowerUsage);

							Guaranteed.State powerUsageState = new Guaranteed.State(new ID("Power_Usage_for_"  + virtualSystem.getId()), typeConstraintExprVMCores);

							// Number of VM Cores
							//						FunctionalExpr functionalExpreVMCores = new FunctionalExpr(
							//								new STND("http://www.slaatsoi.org/resources#vm_cores"), 
							//								new ValueExpr[]{new ID("VM_of_type_" + virtualSystem.getId())});
							//						SimpleDomainExpr simpleDomainExpreVMCores = new SimpleDomainExpr(
							//																	new CONST("" + virtualSystem.getVirtualHardwareSection().getNumberOfVirtualCPUs(), 
							//																			  new STND("http://www.slaatsoi.org/coremodel/units#integer")), 
							//																	new STND("http://www.slaatsoi.org/coremodel#equals"));
							//						
							//						TypeConstraintExpr typeConstraintExprVMCores = new TypeConstraintExpr(functionalExpreVMCores, simpleDomainExpreVMCores);
							//						
							//						Guaranteed.State cpuCoresState = new Guaranteed.State(new ID("CPU_CORES_for_"  + virtualSystem.getId()), typeConstraintExprVMCores);
							//						
							//						// TODO Careful with the memory units... assuming everything is MB
							//						// Number of VM Memory
							//						FunctionalExpr functionalExpreVMMemory = new FunctionalExpr(
							//								new STND("http://www.slaatsoi.org/resources#memory"), 
							//								new ValueExpr[]{new ID("VM_of_type_" + virtualSystem.getId())});
							//						SimpleDomainExpr simpleDomainExpreVMMemory = new SimpleDomainExpr(
							//																	new CONST("" + virtualSystem.getVirtualHardwareSection().getMemorySize(), 
							//																			  new STND("http://www.slaatsoi.org/coremodel/units#MB")), 
							//																	new STND("http://www.slaatsoi.org/coremodel#equals"));
							//						
							//						TypeConstraintExpr typeConstraintExprVMMemory = new TypeConstraintExpr(functionalExpreVMMemory, simpleDomainExpreVMMemory);
							//						
							//						Guaranteed.State memoryState = new Guaranteed.State(new ID("MEMORY_for_"  + virtualSystem.getId()), typeConstraintExprVMMemory);

							Guaranteed[] guarantees = new Guaranteed[1];
							guarantees[0] = powerUsageState;
							//guarantees[1] = memoryState;

							VariableDeclr[] vars = new VariableDeclr[1];
							vars[0] = variableDeclr;

							AgreementTerm aTerm = new AgreementTerm(id, null, vars, guarantees);

							newTerms.add(aTerm);
						}
					}
					//create Agreement
					else {
						logger.debug("Found a PaaS layer term. Hiding...");
						paaSAgreementTerms.add(terms[i]);
					}
				}
				else newTerms.add(terms[i]);
			}    
		}
		AgreementTerm[] initialArr = new AgreementTerm[newTerms.size()];
		initialArr = newTerms.toArray(initialArr);
		templateInitial.setAgreementTerms(initialArr);

		logger.debug("Initial template after translation:\n"+templateInitial);

		return templateInitial;
	}

}
