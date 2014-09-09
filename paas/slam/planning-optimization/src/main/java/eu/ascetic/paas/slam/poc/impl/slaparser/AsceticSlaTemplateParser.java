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

package eu.ascetic.paas.slam.poc.impl.slaparser;

import java.util.HashMap;
import java.util.LinkedHashMap;

import org.slasoi.slamodel.core.CompoundDomainExpr;
import org.slasoi.slamodel.core.ConstraintExpr;
import org.slasoi.slamodel.core.DomainExpr;
import org.slasoi.slamodel.core.EventExpr;
import org.slasoi.slamodel.core.FunctionalExpr;
import org.slasoi.slamodel.core.SimpleDomainExpr;
import org.slasoi.slamodel.core.TypeConstraintExpr;
import org.slasoi.slamodel.primitives.CONST;
import org.slasoi.slamodel.primitives.Expr;
import org.slasoi.slamodel.primitives.ID;
import org.slasoi.slamodel.primitives.STND;
import org.slasoi.slamodel.primitives.ValueExpr;
import org.slasoi.slamodel.sla.AgreementTerm;
import org.slasoi.slamodel.sla.Customisable;
import org.slasoi.slamodel.sla.Endpoint;
import org.slasoi.slamodel.sla.Guaranteed;
import org.slasoi.slamodel.sla.Guaranteed.Action;
import org.slasoi.slamodel.sla.Guaranteed.Action.Defn;
import org.slasoi.slamodel.sla.Guaranteed.State;
import org.slasoi.slamodel.sla.InterfaceDeclr;
import org.slasoi.slamodel.sla.SLATemplate;
import org.slasoi.slamodel.sla.VariableDeclr;
import org.slasoi.slamodel.sla.business.ComponentProductOfferingPrice;
import org.slasoi.slamodel.sla.business.ProductOfferingPrice;



public class AsceticSlaTemplateParser {


	private LinkedHashMap<String, DomainExpr> variablesAt;
	private HashMap<String, String> variablesVs;

	private AsceticSlaTemplate asceticSlat;
	

	private AsceticSlaTemplateParser() {
		variablesAt = new LinkedHashMap<String, DomainExpr>();
		variablesVs = new HashMap<String, String>();
		asceticSlat = new AsceticSlaTemplate();
	}


	private void parseVariables(AgreementTerm term) {
		VariableDeclr[] vdecs = term.getVariableDeclrs();
		String ovfId = null;
		if (null == vdecs)
			return;
		for (VariableDeclr v : vdecs) {
			if (v instanceof Customisable) {
				Customisable c = (Customisable) v;
				ID var = c.getVar();
				DomainExpr expr = (DomainExpr) c.getExpr();
				variablesAt.put(var.getValue(), expr);
			} 
			else {
				Expr expr = (Expr) v.getExpr();
				if (expr instanceof FunctionalExpr) {
					FunctionalExpr fu = (FunctionalExpr) expr;
					ValueExpr ve = fu.getParameters()[0];
					ovfId = ve.toString();
					variablesVs.put(v.getVar().getValue(), ovfId);
				}
			}
		}
	}

	
	private void parseSimpleDomainExpr(AsceticVirtualSystem cvs, String termName, SimpleDomainExpr sde) {
		String operator = sde.getComparisonOp().toString();
		ValueExpr ve = sde.getValue();
		if (ve instanceof CONST) {
			String value = ((CONST)ve).getValue();
			String unit = ((CONST)ve).getDatatype().toString();
			cvs.addAgreementTerm(new MeasurableAgreementTerm(termName, unit, value, operator));
		} else if (ve instanceof ID) {
			DomainExpr cde = variablesAt.get(((ID)ve).getValue());
			if (cde instanceof SimpleDomainExpr) {
				parseSimpleDomainExpr(cvs, termName, (SimpleDomainExpr)cde);
			}
			if (cde instanceof CompoundDomainExpr) {
				parseCompoundDomainExpr(cvs, termName, (CompoundDomainExpr)cde);
			}
		}
	}

	
	private void parseCompoundDomainExpr(AsceticVirtualSystem cvs, String termName, CompoundDomainExpr cde) {
		if (null == cde)
			return;
		for (DomainExpr de : cde.getSubExpressions()) {
			if (de instanceof SimpleDomainExpr)
				parseSimpleDomainExpr(cvs, termName, (SimpleDomainExpr) de);
		}
	}

	
	private void addPriceTerm(AsceticVirtualSystem cvs, ProductOfferingPrice pop) {
		if (null == cvs)
			return;
        for (ComponentProductOfferingPrice cpop : pop.getComponentProductOfferingPrices()) {
//        	if (cpop.getId().getValue().startsWith("Price_OF")) {
        	if (cpop.getPriceType().getValue().endsWith("#per_hour")) {
        		String price = cpop.getPrice().getValue();
                AsceticAgreementTerm priceTerm = new MeasurableAgreementTerm("price", "EUR", price, "=");
                cvs.addAgreementTerm(priceTerm);
        	}
        }
	}

	
	private void parseSlaTemplate(SLATemplate slaTemplate) {
		InterfaceDeclr[] interfaceDeclrs = slaTemplate.getInterfaceDeclrs();
		HashMap<String, String> ovfVirtualSystems = new HashMap<String, String>();
		@SuppressWarnings("deprecation")
		STND propOvfVsId = new STND();
		propOvfVsId.setValue("OVF_VirtualSystem_ID");
		
		for (InterfaceDeclr idecl : interfaceDeclrs) {
			Endpoint[] endpoints = idecl.getEndpoints(); 
			if (endpoints == null || endpoints.length == 0)
				continue;
			String vsId = endpoints[0].getPropertyValue(propOvfVsId);
			ovfVirtualSystems.put(idecl.getId().toString(), vsId);
			AsceticVirtualSystem cvs = new AsceticVirtualSystem();
			cvs.setId(vsId);
			asceticSlat.addVirtualSystem(cvs);
		}
		
		AgreementTerm[] agreementTerms = slaTemplate.getAgreementTerms();

		for (AgreementTerm term : agreementTerms) {

			parseVariables(term);
			
			Guaranteed[] guaranteeds = term.getGuarantees();

			for (Guaranteed guarantee : guaranteeds) {

				if (guarantee instanceof Guaranteed.State) {
					
					ConstraintExpr ce = ((State) guarantee).getState();
					if (!(ce instanceof TypeConstraintExpr))
						continue;

					TypeConstraintExpr tce = (TypeConstraintExpr) ce;
					ValueExpr expr = tce.getValue();

					String ssTermName = null;
					String vsName = null;
					if (expr instanceof FunctionalExpr) {
						ssTermName = ((FunctionalExpr)expr).getOperator().getValue();
						if (ssTermName == null)
							continue;
						ValueExpr[] parameters = ((FunctionalExpr)expr).getParameters();
						if (parameters == null || parameters.length == 0)
							continue;
						vsName = parameters[0].toString();
					}

					String termName = ssTermName.substring(ssTermName.indexOf('#') + 1);
				

					String ovfId = variablesVs.get(vsName);
					String virtualSystemId = ovfVirtualSystems.get(ovfId);
					AsceticVirtualSystem cvs = asceticSlat.getVirtualSystem(virtualSystemId);
					
					DomainExpr de = tce.getDomain();
					if (de instanceof SimpleDomainExpr)
						parseSimpleDomainExpr(cvs, termName, (SimpleDomainExpr)de);
					if (de instanceof CompoundDomainExpr)
						parseCompoundDomainExpr(cvs, termName, (CompoundDomainExpr)de);
					
				} else if (guarantee instanceof Guaranteed.Action) {
					EventExpr ee = ((Action) guarantee).getPrecondition();
					String value = ee.getParameters()[0].toString();
					
					Defn action = ((Action) guarantee).getPostcondition();
					if (action instanceof ProductOfferingPrice) {
                        ProductOfferingPrice pop = (ProductOfferingPrice) action;
                        AsceticVirtualSystem cvs = asceticSlat.getVirtualSystem(value);
                        addPriceTerm(cvs, pop);
					}
				}
			}
		}
	}

	
	
	private AsceticSlaTemplate getAsceticSlat() {
		return asceticSlat;
	}

	
	public static AsceticSlaTemplate getAsceticSlat(SLATemplate slat) {
		AsceticSlaTemplateParser parser= new AsceticSlaTemplateParser();
		parser.parseSlaTemplate(slat);
		return parser.getAsceticSlat();
	}


}
