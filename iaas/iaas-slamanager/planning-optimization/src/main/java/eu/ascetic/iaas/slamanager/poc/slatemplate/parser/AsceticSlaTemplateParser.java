/**
 * Copyright 2012 Hewlett-Packard Development Company, L.P.                
 *                                                                          
 * Licensed under the Apache License, Version 2.0 (the "License");         
 * you may not use this file except in compliance with the License.        
 * You may obtain a copy of the License at                                 
 *                                                                          
 *     http://www.apache.org/licenses/LICENSE-2.0                          
 *                                                                          
 * Unless required by applicable law or agreed to in writing, software     
 * distributed under the License is distributed on an "AS IS" BASIS,       
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and     
 * limitations under the License.                                          
 */

package eu.ascetic.iaas.slamanager.poc.slatemplate.parser;

import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;

import org.apache.commons.io.FileUtils;
import org.apache.log4j.Logger;
import org.slasoi.slamodel.core.CompoundDomainExpr;
import org.slasoi.slamodel.core.DomainExpr;
import org.slasoi.slamodel.core.FunctionalExpr;
import org.slasoi.slamodel.core.SimpleDomainExpr;
import org.slasoi.slamodel.core.TypeConstraintExpr;
import org.slasoi.slamodel.primitives.CONST;
import org.slasoi.slamodel.primitives.Expr;
import org.slasoi.slamodel.primitives.ID;
import org.slasoi.slamodel.primitives.STND;
import org.slasoi.slamodel.primitives.ValueExpr;
import org.slasoi.slamodel.service.ResourceType;
import org.slasoi.slamodel.sla.AgreementTerm;
import org.slasoi.slamodel.sla.Endpoint;
import org.slasoi.slamodel.sla.Guaranteed;
import org.slasoi.slamodel.sla.InterfaceDeclr;
import org.slasoi.slamodel.sla.SLATemplate;
import org.slasoi.slamodel.sla.VariableDeclr;
import org.slasoi.slamodel.vocab.core;

import eu.ascetic.iaas.slamanager.poc.enums.AsceticAgreementTerm;
import eu.ascetic.iaas.slamanager.poc.enums.OperatorType;
import eu.ascetic.iaas.slamanager.poc.exceptions.NotSupportedUnitException;
import eu.ascetic.iaas.slamanager.poc.manager.resource.OVFRetriever;
import eu.ascetic.iaas.slamanager.poc.manager.resource.OvfResourceParser;
import eu.ascetic.iaas.slamanager.poc.slatemplate.AsceticSlaTemplate;
import eu.ascetic.iaas.slamanager.poc.slatemplate.AsceticSlaTemplate.Builder;
import eu.ascetic.iaas.slamanager.poc.slatemplate.request.AsceticGenericRequest;
import eu.ascetic.iaas.slamanager.poc.slatemplate.request.AsceticRequest;
import eu.ascetic.iaas.slamanager.poc.slatemplate.request.AsceticResourceRequest;
import eu.ascetic.iaas.slamanager.poc.slatemplate.request.SharedDisk;
import eu.ascetic.iaas.slamanager.poc.slatemplate.request.VirtualSystem;
import eu.ascetic.iaas.slamanager.poc.slatemplate.request.guarantee.ActionGuarantee;
import eu.ascetic.iaas.slamanager.poc.slatemplate.request.guarantee.GenericGuarantee;
import eu.ascetic.iaas.slamanager.poc.slatemplate.request.guarantee.Guarantee;
import eu.ascetic.iaas.slamanager.poc.slatemplate.request.guarantee.OvfResourceGuarantee;
import eu.ascetic.iaas.slamanager.poc.slatemplate.request.guarantee.ResourceGuarantee;
import eu.ascetic.iaas.slamanager.poc.utils.AgreementUtil;
import eu.ascetic.iaas.slamanager.poc.utils.AsceticUnits;

public class AsceticSlaTemplateParser {

	private static final Logger logger = Logger.getLogger(AsceticSlaTemplateParser.class.getName());

	private eu.ascetic.iaas.slamanager.poc.slatemplate.AsceticSlaTemplate AsceticSlaTemplate = null;

	private Collection<AsceticRequest> resources = new HashSet<AsceticRequest>();

	HashMap<String, String> mapSlaOvfId = new HashMap<String, String>();

	private OVFRetriever ovfRetriever;
	
	private OvfResourceParser ovfParser;

	private String ovfFile = null;

	

	public AsceticSlaTemplateParser() {
		ovfRetriever = new OVFRetriever();
	}
	
	private void parseSlaTemplate(SLATemplate slaTemplate) {
		eu.ascetic.iaas.slamanager.poc.slatemplate.AsceticSlaTemplate.Builder builder = new Builder();
		builder.setSlaTemplate(slaTemplate);
		builder.setUUID(slaTemplate.getUuid().getValue());
		InterfaceDeclr[] ids = slaTemplate.getInterfaceDeclrs();
		initOvfResourceParser(ids);
		builder.setOvfFile(ovfFile);
		for (AgreementTerm aTerm : slaTemplate.getAgreementTerms()) {
			VariableDeclr[] varList = aTerm.getVariableDeclrs();
			parseAgreementTerm(aTerm, varList, ids);
		}
		builder.setAsceticRequests(resources);
		AsceticSlaTemplate = builder.build();
	}

	private AsceticResourceRequest getAsceticResourceRequest(String aTermId, String interfDeclId, InterfaceDeclr[] ids) {
		String ovfId = null;
		AsceticResourceRequest AsceticRequest = null;
		for (InterfaceDeclr id : ids) {
			if (id.getId().getValue().equals(interfDeclId)) {
				Endpoint[] e = id.getEndpoints();
				if (e != null && e.length != 0) {
					if (id.getInterface() instanceof ResourceType) { // is
																		// Virtual
																		// System
						ovfId = e[0].getPropertyValue(new STND("OVF_VirtualSystem_ID"));
						AsceticRequest = new VirtualSystem(aTermId);
						AsceticRequest.setOvfId(ovfId);
						break;
					}
				} else if (id.getInterface().getPropertyValue(new STND("Shared_Disk_ID")) != null) { // is
																										// Shared
																										// Disk
					ovfId = id.getInterface().getPropertyValue(new STND("Shared_Disk_ID"));
					AsceticRequest = new SharedDisk(aTermId);
					AsceticRequest.setOvfId(ovfId);
					break;
				}
			}
		}
		return AsceticRequest;
	}

	private void parseAgreementTerm(AgreementTerm aTerm, VariableDeclr[] varList, InterfaceDeclr[] ids) {
		HashMap<String, Expr> variables = new HashMap<String, Expr>();
		AsceticRequest AsceticRequest = null;
		String interfDeclId = null;
		for (VariableDeclr vd : varList) {
			String key = vd.getVar().getValue();
			Expr value = vd.getExpr();
			variables.put(key, value);
			if (value instanceof FunctionalExpr) {
				ValueExpr[] p = ((FunctionalExpr) value).getParameters();
				STND op = ((FunctionalExpr) value).getOperator();
				if (op.equals(core.subset_of)) {
					ID intDeclId = (ID) p[0];
					interfDeclId = intDeclId.getValue();
					if (interfDeclId != null) { // is AsceticResourceRequest
						AsceticRequest = getAsceticResourceRequest(aTerm.getId().getValue(), interfDeclId, ids);
					}
				}
			}
		}
		if (AsceticRequest != null) { // is AsceticResourceRequest
			AsceticRequest.setVariables(variables);
			for (Guaranteed g : aTerm.getGuarantees()) {
				AsceticRequest.addGuarantee(parseGuarantee(g, variables));
			}
			if (AsceticRequest instanceof VirtualSystem) { // add ovf file
															// guarantee for
															// Virtual System
				eu.ascetic.utils.ovf.api.VirtualSystem[] a = ovfParser.getVirtualSystems();
				for (eu.ascetic.utils.ovf.api.VirtualSystem vs : a) {
					if (vs.getId().equals(AsceticRequest.getOvfId())) {
						AsceticRequest = parseOvfVSGuarantee(AsceticRequest, vs);
						break;
					}
				}
			}
		} else {
			if (!aTerm.getId().getValue().startsWith("Infrastructure_Price")) // skip
																				// price
																				// agreement
																				// term
				AsceticRequest = new AsceticGenericRequest(aTerm.getId().getValue());
		}
		if (AsceticRequest != null)
			resources.add(AsceticRequest);
	}

	private AsceticRequest parseOvfVSGuarantee(AsceticRequest AsceticRequest, eu.ascetic.utils.ovf.api.VirtualSystem vs) {
		HashMap<String, Double> vsNeed = ovfParser.getVirtualSystemNeed(vs);
		if (vsNeed != null) {
			if (vsNeed.get("vm_cores") != null) {
				OvfResourceGuarantee ovfReqCore = new OvfResourceGuarantee(vs.getId() + "-vm_cores", AsceticAgreementTerm.vm_cores);
				ovfReqCore.setMin(vsNeed.get("vm_cores"));
				ovfReqCore.setMax(vsNeed.get("vm_cores"));
				ovfReqCore.setDefault(vsNeed.get("vm_cores"));
				AsceticRequest.addGuarantee(ovfReqCore);
			}
			if (vsNeed.get("cpu_speed") != null) {
				OvfResourceGuarantee ovfReqCpuSpeed = new OvfResourceGuarantee(vs.getId() + "-cpu_speed", AsceticAgreementTerm.cpu_speed);
				ovfReqCpuSpeed.setMin(vsNeed.get("cpu_speed"));
				ovfReqCpuSpeed.setMax(vsNeed.get("cpu_speed"));
				ovfReqCpuSpeed.setDefault(vsNeed.get("cpu_speed"));
				AsceticRequest.addGuarantee(ovfReqCpuSpeed);
			}
			if (vsNeed.get("memory") != null) {
				OvfResourceGuarantee ovfReqMem = new OvfResourceGuarantee(vs.getId() + "-memory", AsceticAgreementTerm.memory);
				ovfReqMem.setMin(vsNeed.get("memory"));
				ovfReqMem.setMax(vsNeed.get("memory"));
				ovfReqMem.setDefault(vsNeed.get("memory"));
				AsceticRequest.addGuarantee(ovfReqMem);
			}
		}
		return AsceticRequest;
	}

	private Guarantee parseGuarantee(Guaranteed g, HashMap<String, Expr> variables) {
		Guarantee guarantee = null;
		if (g instanceof Guaranteed.State) {
			Guaranteed.State gs = (Guaranteed.State) g;
			guarantee = parseTypeConstraintExpr(gs, variables);
		}
		return guarantee;
	}

	private Guarantee parseTypeConstraintExpr(Guaranteed.State gs, HashMap<String, Expr> variables) {
		Guarantee guarantee = null;
		TypeConstraintExpr tce = ((TypeConstraintExpr) gs.getState());
		String agreementTermName = AgreementUtil.getStringTerm(gs);
		String guaranteeId = gs.getId().getValue();
		ValueExpr[] varAppExpr = ((FunctionalExpr) tce.getValue()).getParameters();
		if (varAppExpr != null && varAppExpr.length != 0) {
			ID idVarApp = (ID) varAppExpr[0];
			String guaranteeDomain = idVarApp.getValue();
			if (AgreementUtil.isResourceTerm(agreementTermName)) // add resource
																	// request
				guarantee = new ResourceGuarantee(guaranteeId, AgreementUtil.metricToAgreement(agreementTermName));
			if (AgreementUtil.isGenericTerms(agreementTermName)) // add generic
																	// request
				guarantee = new GenericGuarantee(guaranteeId, AgreementUtil.metricToAgreement(agreementTermName));
			STND[] properties = tce.getPropertyKeys();
			if (properties != null && properties.length != 0) { // is an action
																// request
				guarantee = (ActionGuarantee) guarantee;
				((ActionGuarantee) guarantee).setActionType(tce.getPropertyValue(new STND("type")));
			}
			guarantee.setDomain(guaranteeDomain);
			if (tce.getDomain() instanceof SimpleDomainExpr)
				guarantee = parseSimpleDomainExpr(guarantee, (SimpleDomainExpr) tce.getDomain(), variables);
			if (tce.getDomain() instanceof CompoundDomainExpr) {
				CompoundDomainExpr cde = (CompoundDomainExpr) tce.getDomain();
				DomainExpr[] de = cde.getSubExpressions();
				for (DomainExpr d : de) {
					if (d instanceof SimpleDomainExpr) {
						SimpleDomainExpr sde1 = (SimpleDomainExpr) d;
						guarantee = parseSimpleDomainExpr(guarantee, sde1, variables);
					}
				}
			}
		}
		return guarantee;
	}

	private Guarantee parseSimpleDomainExpr(Guarantee guarantee, SimpleDomainExpr sde, HashMap<String, Expr> variables) {
		ValueExpr cn = sde.getValue();
		if (guarantee instanceof ResourceGuarantee) {
			ResourceGuarantee resource = (ResourceGuarantee) guarantee;
			if (cn instanceof ID) { // is variable
				if (variables.containsKey(((ID) cn).getValue())) {
					SimpleDomainExpr simpleDomain = (SimpleDomainExpr) variables.get(((ID) cn).getValue());
					resource = (ResourceGuarantee) parseSimpleDomainExpr(guarantee, simpleDomain, variables);
				}
			} else { // is a CONST
				resource = parseOperatorResource(resource, sde.getComparisonOp(), (CONST) cn);
			}
			return resource;
		}
		if (guarantee instanceof GenericGuarantee) {
			GenericGuarantee genericRequest = (GenericGuarantee) guarantee;
			if (cn instanceof ID) { // is variable
				if (variables.containsKey(((ID) cn).getValue())) {
					SimpleDomainExpr simpleDomain = (SimpleDomainExpr) variables.get(((ID) cn).getValue());
					genericRequest = (GenericGuarantee) parseSimpleDomainExpr(guarantee, simpleDomain, variables);
				}
			} else { // is a CONST
				genericRequest = parseOperatorGeneric(genericRequest, sde.getComparisonOp(), (CONST) cn);
			}
			return genericRequest;
		}
		return null;
	}

	private GenericGuarantee parseOperatorGeneric(GenericGuarantee genericRequest, STND comparisonOp, CONST cn) {
		String type = cn.getDatatype().toString();
		String value = cn.getValue();
		OperatorType op = null;
		if (comparisonOp.equals(core.less_than))
			op = OperatorType.LESS;
		if (comparisonOp.equals(core.less_than_or_equals))
			op = OperatorType.LESS_EQUAL;
		if (comparisonOp.equals(core.greater_than))
			op = OperatorType.GREATER;
		if (comparisonOp.equals(core.greater_than_or_equals))
			op = OperatorType.GREATER_EQUAL;
		if (comparisonOp.equals(core.equals))
			op = OperatorType.EQUALS;
		if (comparisonOp.equals(core.not_equals))
			op = OperatorType.NOT_EQUALS;
		GenericGuarantee.Value c = genericRequest.new Value(type, value, op);
		genericRequest.addConstraint(c);
		return genericRequest;
	}

	private ResourceGuarantee parseOperatorResource(ResourceGuarantee resource, STND comparisonOp, CONST cn) {
		try {
			if (comparisonOp.equals(core.less_than)) {
				resource.setMax(new Double(AsceticUnits.normalizeUnit(new Double(cn.getValue()).doubleValue(), AsceticUnits.convertToEnum(cn.getDatatype().toString()))) - 1);
			}
			if (comparisonOp.equals(core.less_than_or_equals)) {
				resource.setMax(new Double(AsceticUnits.normalizeUnit(new Double(cn.getValue()).doubleValue(), AsceticUnits.convertToEnum(cn.getDatatype().toString()))));
			}
			if (comparisonOp.equals(core.greater_than)) {
				resource.setMin(new Double(AsceticUnits.normalizeUnit(new Double(cn.getValue()).doubleValue(), AsceticUnits.convertToEnum(cn.getDatatype().toString()))) + 1);
			}
			if (comparisonOp.equals(core.greater_than_or_equals)) {
				resource.setMin(new Double(AsceticUnits.normalizeUnit(new Double(cn.getValue()).doubleValue(), AsceticUnits.convertToEnum(cn.getDatatype().toString()))));
			}
			if (comparisonOp.equals(core.equals)) {
				resource.setMin(new Double(AsceticUnits.normalizeUnit(new Double(cn.getValue()).doubleValue(), AsceticUnits.convertToEnum(cn.getDatatype().toString()))));
				resource.setMax(new Double(AsceticUnits.normalizeUnit(new Double(cn.getValue()).doubleValue(), AsceticUnits.convertToEnum(cn.getDatatype().toString()))));
				resource.setDefault(new Double(AsceticUnits.normalizeUnit(new Double(cn.getValue()).doubleValue(), AsceticUnits.convertToEnum(cn.getDatatype().toString()))));
			}
		} catch (NumberFormatException e) {
			e.printStackTrace();
		} catch (NotSupportedUnitException e) {
			e.printStackTrace();
		}
		return resource;
	}

	private String extractOvfFile(String filePath) {
		String result = null;
		try {
			result = FileUtils.readFileToString(new File(filePath));
		} catch (IOException e) {
			e.printStackTrace();
		}
		return result;
	}

	private void initOvfResourceParser(InterfaceDeclr[] ids) {
		for (int i = 0; i < ids.length; i++) {
			InterfaceDeclr iD = ids[i];
			STND[] propKey = iD.getPropertyKeys();
			if (propKey != null && propKey.length != 0) {
				for (int l = 0; l < propKey.length; l++) {
					if (propKey[l].equals("OVF_URL")) {
						String ovfURL = iD.getPropertyValue(propKey[l]);
						ovfRetriever.retrieveOvf(ovfURL);
						ovfFile = extractOvfFile(ovfRetriever.getFilename());
						break;
					}
				}
			}
		}
		ovfParser = new OvfResourceParser(ovfFile);
	}

	private AsceticSlaTemplate getAsceticSlat() {
		logger.info(AsceticSlaTemplate.toString());
		return AsceticSlaTemplate;
	}

	public static AsceticSlaTemplate getAsceticSlat(SLATemplate slat) {
		AsceticSlaTemplateParser parser = new AsceticSlaTemplateParser();
		parser.parseSlaTemplate(slat);
		return parser.getAsceticSlat();
	}

}
