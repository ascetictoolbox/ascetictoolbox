package eu.ascetic.iaas.slamanager.poc.slatemplate;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import org.slasoi.slamodel.core.CompoundDomainExpr;
import org.slasoi.slamodel.core.EventExpr;
import org.slasoi.slamodel.core.FunctionalExpr;
import org.slasoi.slamodel.core.SimpleDomainExpr;
import org.slasoi.slamodel.core.TypeConstraintExpr;
import org.slasoi.slamodel.primitives.CONST;
import org.slasoi.slamodel.primitives.Expr;
import org.slasoi.slamodel.primitives.ID;
import org.slasoi.slamodel.primitives.STND;
import org.slasoi.slamodel.primitives.TIME;
import org.slasoi.slamodel.sla.AgreementTerm;
import org.slasoi.slamodel.sla.Guaranteed;
import org.slasoi.slamodel.sla.Guaranteed.Action;
import org.slasoi.slamodel.sla.Guaranteed.State;
import org.slasoi.slamodel.sla.InterfaceDeclr;
import org.slasoi.slamodel.sla.SLATemplate;
import org.slasoi.slamodel.sla.VariableDeclr;
import org.slasoi.slamodel.sla.business.ComponentProductOfferingPrice;
import org.slasoi.slamodel.sla.business.ProductOfferingPrice;
import org.slasoi.slamodel.vocab.core;

import eu.ascetic.iaas.slamanager.poc.exceptions.NotSupportedUnitException;
import eu.ascetic.iaas.slamanager.poc.slatemplate.request.AsceticGenericRequest;
import eu.ascetic.iaas.slamanager.poc.slatemplate.request.AsceticRequest;
import eu.ascetic.iaas.slamanager.poc.slatemplate.request.AsceticResourceRequest;
import eu.ascetic.iaas.slamanager.poc.slatemplate.request.VirtualSystem;
import eu.ascetic.iaas.slamanager.poc.slatemplate.request.guarantee.GenericGuarantee;
import eu.ascetic.iaas.slamanager.poc.slatemplate.request.guarantee.GenericGuarantee.Value;
import eu.ascetic.iaas.slamanager.poc.slatemplate.request.guarantee.Guarantee;
import eu.ascetic.iaas.slamanager.poc.slatemplate.request.guarantee.ResourceGuarantee;
import eu.ascetic.iaas.slamanager.poc.utils.AgreementUtil;
import eu.ascetic.iaas.slamanager.poc.utils.AsceticUnits;

public class SlaTemplateBuilder {

	private AsceticSlaTemplate asceticSlaTemplate = null;

	private SLATemplate newSlaTemplate = null;

	private SLATemplate oldSlaTemplate = null;

	private ArrayList<AgreementTerm> aTerms = null;

	public static final String $STND_business = "http://www.slaatsoi.org/business#";
	public static final String $STND_units = "http://www.slaatsoi.org/coremodel/units#";
	public static final String $STND_coremodel = "http://www.slaatsoi.org/coremodel#";
	public static final String $STND_slamodel = "http://www.slaatsoi.org/slamodel#";

	public SlaTemplateBuilder() {

	}

	public SlaTemplateBuilder setAsceticSlatemplate(AsceticSlaTemplate ast) {
		asceticSlaTemplate = ast;
		oldSlaTemplate = asceticSlaTemplate.getSlaTemplate();
		return this;
	}

	public SLATemplate build() {
		newSlaTemplate = new SLATemplate();
		aTerms = new ArrayList<AgreementTerm>();
		newSlaTemplate.setUuid(oldSlaTemplate.getUuid());
		addProperties();
		addInterfaceDecl();
		addAgreementTerms();
		addPrices();
		AgreementTerm[] terms = new AgreementTerm[aTerms.size()];
		int i = 0;
		for (AgreementTerm at : aTerms) {
			terms[i] = at;
			i++;
		}
		newSlaTemplate.setAgreementTerms(terms);
		return newSlaTemplate;
	}

	private void addPrices() {
		for (VirtualSystem vs : asceticSlaTemplate.getVirtualSystems()) {
			addPriceVS(vs);
		}
	}

	private void addPriceVS(VirtualSystem vs) {
		AgreementTerm aTerm = null;
		ArrayList<ComponentProductOfferingPrice> cpops = new ArrayList<ComponentProductOfferingPrice>();
		ProductOfferingPrice po = null;
		String id = "Price_OF_" + vs.getOvfId();
		String billFreq = null;
		Date from = null;
		Date until = null;
		String priceType = null;
		String currency = null;
		ComponentProductOfferingPrice cpo = null;
		double totalPrice = 0, resourcesPrice = 0, guaranteePrice = 0, reservationPrice = 0;
		totalPrice = vs.getPrice();
		cpo = new ComponentProductOfferingPrice(new ID(id), new STND($STND_business + priceType), new CONST(new Double(totalPrice).toString(), new STND($STND_units + currency)), new CONST("1",
				new STND($STND_units + "vm")));
		cpops.add(cpo);
		ComponentProductOfferingPrice[] cpopsArray = new ComponentProductOfferingPrice[cpops.size()];
		int i = 0;
		for (ComponentProductOfferingPrice c : cpops) {
			cpopsArray[i] = c;
			i++;
		}
		po = new ProductOfferingPrice(new ID("Product_Offering_Price_Of_" + vs.getOvfId()), "", new TIME(dateToCalendar(from)), new TIME(dateToCalendar(until)), new STND($STND_business + billFreq),
				cpopsArray);
		ID ida = new ID(vs.getOvfId());
		Expr[] param = { ida };
		EventExpr ee = new EventExpr(new STND($STND_coremodel + "invocation"), param);
		Guaranteed.Action priceAction = new Action(new ID("Price_Of_VirtualSystem_" + vs.getOvfId()), new ID("http://www.slaatsoi.org/slamodel#provider"), new STND(
				"http://www.slaatsoi.org/slamodel#mandatory"), ee, po);
		Guaranteed[] guarantees = { priceAction };
		aTerm = new AgreementTerm(new ID("Infrastructure_Price_Of_" + vs.getOvfId()), null, null, guarantees);
		aTerms.add(aTerm);
	}

	private void addAgreementTerms() {
		for (AsceticRequest ar : asceticSlaTemplate.getAsceticRequests()) {
			if (ar instanceof AsceticResourceRequest) {
				addAgreementTermResource((AsceticResourceRequest) ar);
			}
			if (ar instanceof AsceticGenericRequest) {
				addAgreementTermGeneric((AsceticGenericRequest) ar);
			}
		}
	}

	private void addAgreementTermGeneric(AsceticGenericRequest cr) {
		// add Agreement Term non negotiable
		for (AgreementTerm at : oldSlaTemplate.getAgreementTerms()) {
			if (at.getId().getValue().equals(cr.getId())) {
				aTerms.add(at);
			}
		}
	}

	private void addAgreementTermResource(AsceticResourceRequest cr) {
		AgreementTerm aTerm = null;
		String id = cr.getId();

		HashMap<String, Expr> variables = cr.getVariables();
		VariableDeclr[] var = buildVariableDecl(variables);

		Collection<Guarantee> guarantees = cr.getGuarantees();
		Guaranteed[] guaranteesArray = buildGuarantees(guarantees);

		aTerm = new AgreementTerm(new ID(id), null, var, guaranteesArray);
		aTerms.add(aTerm);
	}

	private Guaranteed[] buildGuarantees(Collection<Guarantee> guarantees) {
		Guaranteed[] guaranteesArray = new Guaranteed[guarantees.size()];
		int i = 0;
		for (Guarantee g : guarantees) {
			Guaranteed guar = buildGuarantee(g);
			guaranteesArray[i] = guar;
			i++;
		}
		return guaranteesArray;
	}

	private Guaranteed buildGuarantee(Guarantee g) {
		ID[] params = new ID[1];
		Guaranteed.State state = null;
		try {
			params[0] = new ID(g.getDomain());
			FunctionalExpr value = new FunctionalExpr(AgreementUtil.agreementToResource(g.getAgreementTerm()), params);
			if (g instanceof ResourceGuarantee) {
				SimpleDomainExpr domain;
				domain = new SimpleDomainExpr(new CONST(new Double(((ResourceGuarantee) g).getDefault()).toString(), AsceticUnits.convertToSTND(((ResourceGuarantee) g).getUnit().toString())),
						core.equals);
				TypeConstraintExpr tce = new TypeConstraintExpr(value, domain);
				state = new State(new ID(g.getId()), tce);
				return state;
			}
			if (g instanceof GenericGuarantee) {
				List<Value> values = ((GenericGuarantee) g).getValues();
				if (values.size() > 1) {
					SimpleDomainExpr[] sdeArray = new SimpleDomainExpr[values.size()];
					int i = 0;
					for (Value v : values) {
						SimpleDomainExpr sde = new SimpleDomainExpr(new CONST(v.getValue(), new STND(v.getType())), AgreementUtil.convertToSTND(v.getOperator()));
						sdeArray[i] = sde;
						i++;
					}
					CompoundDomainExpr cde = new CompoundDomainExpr(core.or, sdeArray);
					TypeConstraintExpr tce = new TypeConstraintExpr(value, cde);
					state = new State(new ID(g.getId()), tce);
					return state;
				} else {
					Value v = values.get(0); // only one simple expression
					SimpleDomainExpr domain;
					domain = new SimpleDomainExpr(new CONST(v.getValue(), AsceticUnits.convertToSTND(v.getType())), AgreementUtil.convertToSTND(v.getOperator()));
					TypeConstraintExpr tce = new TypeConstraintExpr(value, domain);
					state = new State(new ID(g.getId()), tce);
					return state;
				}
			}
		} catch (NotSupportedUnitException e) {
			e.printStackTrace();
		}
		return state;
	}

	private VariableDeclr[] buildVariableDecl(HashMap<String, Expr> variables) {
		VariableDeclr[] var = new VariableDeclr[variables.size()];
		int i = 0;
		for (String s : variables.keySet()) {
			VariableDeclr v = new VariableDeclr(new ID(s), variables.get(s));
			var[i] = v;
			i++;
		}
		return var;
	}

	private void addInterfaceDecl() {
		InterfaceDeclr[] intDecl = oldSlaTemplate.getInterfaceDeclrs();
		ArrayList<InterfaceDeclr> intDeclList = new ArrayList<InterfaceDeclr>();
		String ovfApp = null;
		for (InterfaceDeclr i : intDecl) {
			for (AsceticRequest cr : asceticSlaTemplate.getAsceticRequests()) {
				String ovfId = cr.getOvfId();
				org.slasoi.slamodel.sla.Endpoint[] e = i.getEndpoints();
				if (e != null && e.length != 0) { // is Virtual System OR
													// CustomAction
					ovfApp = e[0].getPropertyValue(new STND("OVF_VirtualSystem_ID"));
					if (ovfApp != null) { // is virtualSystem
						if (ovfApp.equals(ovfId)) {
							intDeclList.add(i);
							break;
						}
					} else { // is Custom Action
						intDeclList.add(i);
						break;
					}
				} else {
					ovfApp = i.getInterface().getPropertyValue(new STND("Shared_Disk_ID"));
					if (ovfId != null && ovfId.equals(ovfApp)) { // is Shared
																	// Disk
						intDeclList.add(i);
						break;
					}
				}
			}
		}
		InterfaceDeclr[] newIntDeclrs = new InterfaceDeclr[intDeclList.size()];
		for (int i = 0; i < intDeclList.size(); i++) {
			newIntDeclrs[i] = intDeclList.get(i);
		}
		newSlaTemplate.setInterfaceDeclrs(newIntDeclrs);
	}

	private void addProperties() {
		// add ProvidersList
		String providersList = oldSlaTemplate.getPropertyValue(new STND("ProvidersList"));
		if (providersList != null)
			newSlaTemplate.setPropertyValue(new STND("ProvidersList"), providersList);

		// add Criteria
		String criteria = oldSlaTemplate.getPropertyValue(new STND("Criteria"));
		if (criteria != null)
			newSlaTemplate.setPropertyValue(new STND("Criteria"), criteria);

		// add mapping to federationSlaId
		String federationSlaId = oldSlaTemplate.getPropertyValue(new STND("FederationSlaId"));
		if (federationSlaId != null)
			newSlaTemplate.setPropertyValue(new STND("FederationSlaId"), federationSlaId);

		// add UserUUID
		String userUUID = oldSlaTemplate.getPropertyValue(new STND("UserUUID"));
		if (userUUID != null)
			newSlaTemplate.setPropertyValue(new STND("UserUUID"), userUUID);

		// add providerUUID
		String providerUUID = oldSlaTemplate.getPropertyValue(new STND("ProviderUUid"));
		if (providerUUID != null)
			newSlaTemplate.setPropertyValue(new STND("ProviderUUid"), providerUUID);

		// add AppUUID
		String appUUID = oldSlaTemplate.getPropertyValue(new STND("AppUUID"));
		if (appUUID != null)
			newSlaTemplate.setPropertyValue(new STND("AppUUID"), appUUID);

		newSlaTemplate.setDescr(oldSlaTemplate.getDescr());

		newSlaTemplate.setParties(oldSlaTemplate.getParties());

		newSlaTemplate.setVariableDeclrs(oldSlaTemplate.getVariableDeclrs());

	}

	private static Calendar dateToCalendar(Date date) {
		Calendar cal = Calendar.getInstance();
		cal.setTime(date);
		return cal;
	}

}
