package eu.ascetic.paas.applicationmanager.slam;

import static eu.ascetic.paas.applicationmanager.slam.OVFToSLANames.APP_ENERGY_CONSUMPTION_OVF;
import static eu.ascetic.paas.applicationmanager.slam.OVFToSLANames.APP_ENERGY_CONSUMPTION_SLA;
import static eu.ascetic.paas.applicationmanager.slam.OVFToSLANames.APP_ENERGY_CONSUMPTION_SLA_OPERATOR;
import static eu.ascetic.paas.applicationmanager.slam.OVFToSLANames.POWER_USAGE_PER_VM_OVF;
import static eu.ascetic.paas.applicationmanager.slam.OVFToSLANames.POWER_USAGE_PER_VM_SLA;
import static eu.ascetic.paas.applicationmanager.slam.OVFToSLANames.POWER_USAGE_PER_VM_SLA_OPERATOR;
import static eu.ascetic.paas.applicationmanager.slam.OVFToSLANames.ENERGY_USAGE_PER_VM_OVF;
import static eu.ascetic.paas.applicationmanager.slam.OVFToSLANames.ENERGY_USAGE_PER_VM_SLA;
import static eu.ascetic.paas.applicationmanager.slam.OVFToSLANames.ENERGY_USAGE_PER_VM_SLA_OPERATOR;
import static eu.ascetic.paas.applicationmanager.slam.OVFToSLANames.COMPARATORS;
import static eu.ascetic.paas.applicationmanager.slam.OVFToSLANames.METRIC_UNITS;
import static eu.ascetic.paas.applicationmanager.slam.OVFToSLANames.OVF_ITEM;
import static eu.ascetic.paas.applicationmanager.slam.OVFToSLANames.SUBSET_OF;
import static eu.ascetic.paas.applicationmanager.slam.OVFToSLANames.VM_TYPE;
import static eu.ascetic.paas.applicationmanager.slam.OVFToSLANames.VM_GUARANTEES;
import static eu.ascetic.paas.applicationmanager.slam.OVFToSLANames.AGGREGATED_METRIC_SLA;
import static eu.ascetic.paas.applicationmanager.slam.OVFToSLANames.AGGREGATED_METRIC_SLA_OPERATOR;
import static eu.ascetic.paas.applicationmanager.slam.OVFToSLANames.DATATYPE_DECIMAL;
import static eu.ascetic.paas.applicationmanager.slam.OVFToSLANames.DATATYPE_INTEGER;
import static eu.ascetic.paas.applicationmanager.slam.OVFToSLANames.DATATYPE_STRING;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.log4j.Logger;
import org.slasoi.slamodel.core.FunctionalExpr;
import org.slasoi.slamodel.core.SimpleDomainExpr;
import org.slasoi.slamodel.core.TypeConstraintExpr;
import org.slasoi.slamodel.primitives.CONST;
import org.slasoi.slamodel.primitives.ID;
import org.slasoi.slamodel.primitives.STND;
import org.slasoi.slamodel.primitives.UUID;
import org.slasoi.slamodel.primitives.ValueExpr;
import org.slasoi.slamodel.service.ResourceType;
import org.slasoi.slamodel.sla.AgreementTerm;
import org.slasoi.slamodel.sla.Endpoint;
import org.slasoi.slamodel.sla.Guaranteed;
import org.slasoi.slamodel.sla.InterfaceDeclr;
import org.slasoi.slamodel.sla.Party;
import org.slasoi.slamodel.sla.SLATemplate;
import org.slasoi.slamodel.sla.VariableDeclr;

import eu.ascetic.paas.applicationmanager.conf.Configuration;
import eu.ascetic.paas.applicationmanager.ovf.AsceticSLAInfo;
import eu.ascetic.paas.applicationmanager.ovf.AsceticTermMeasurement;
import eu.ascetic.paas.applicationmanager.ovf.OVFUtils;
import eu.ascetic.paas.applicationmanager.providerregistry.PRClient;
import eu.ascetic.providerregistry.model.Provider;
import eu.ascetic.utils.ovf.api.OvfDefinition;
import eu.ascetic.utils.ovf.api.VirtualSystem;

/**
 * 
 * Copyright 2014 ATOS SPAIN S.A. 
 * 
 * Licensed under the Apache License, Version 2.0 (the License);
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * 
 * @author David Garcia Perez. Atos Research and Innovation, Atos SPAIN SA
 * e-mail david.garciaperez@atos.net 
 * 
 * It creates an SLA Template form an OVF document
 *
 */

public class SLATemplateCreator {
	private static Logger logger = Logger.getLogger(SLATemplateCreator.class);
	
	/**
	 * Generates and slaTemplate from an OVF file
	 * @param ovf from which to generate the SLA Template
	 * @return the template
	 */
	public static SLATemplate generateSLATemplate(OvfDefinition ovf, String ovfURL) {
		SLATemplate slaTemplate = new SLATemplate();
		UUID uuid = new UUID("ASCETiC-SLaTemplate-Example-01");
		slaTemplate.setUuid(uuid);
		
		// We add the properties section
		addProperties(slaTemplate);
		
		// We add the parties section
		addProviderEndPointToTemplate(slaTemplate);
		addUserEndPointToTemplate(slaTemplate);
		
		// We add the InterfaceDclr section
		addInterfaceDclr(slaTemplate, ovf, ovfURL);
		
		//Add the App Energy Consumption Agreement Term
		addAppEnergyConsumptionAgreementTerm(slaTemplate, ovf);
		
		addVMSpecificAgreementTerms(slaTemplate, ovf);
		
		//addAgreementTerms(slaTemplate, ovf);
		
		//We verify that the values are the right ones parsing the SLATemplate with different libs...
		
		
		return slaTemplate;
	}
	
	private static void addVMSpecificAgreementTerms(SLATemplate slaTemplate, OvfDefinition ovf) {
		// We need to go over all VMs
		VirtualSystem[] virtualSystemArray = ovf.getVirtualSystemCollection().getVirtualSystemArray();
		
		for(int i = 0; i < virtualSystemArray.length; i++) {
			VirtualSystem virtualSystem = virtualSystemArray[i];
			String ovfId = virtualSystem.getId();
			
			// We get the specific guarantees we are interested
			addPowerVMConsumptionAgreementTerm(slaTemplate, ovf, ovfId);
			addEnergyVMConsumptionAgreementTerm(slaTemplate, ovf, ovfId);
			
			// We add aggregated guarantees if available
			addAggregatedGuarantees(slaTemplate, ovf, ovfId);
		}
	}
	
	private static void addAggregatedGuarantees(SLATemplate slaTemplate, OvfDefinition ovf, String ovfId) {
		List<AsceticTermMeasurement> measurementTerms = OVFUtils.getVMTermMeasurement(ovf, ovfId);
		
		for(AsceticTermMeasurement measurementTerm : measurementTerms) {
			// ID for the agreement term
			ID id = new ID(AGGREGATED_METRIC_SLA);
			// No Variable declarations for this type
			VariableDeclr[] vars = new VariableDeclr[0];
			
			// We add the TypeConstraintExpre
			ValueExpr[] valueExpreArray = new ValueExpr[5];
			//Event Type
			valueExpreArray[0] = new CONST(measurementTerm.getEvent(), new STND(DATATYPE_STRING));
			//Metric
			valueExpreArray[1] = new CONST(measurementTerm.getMetric(), new STND(DATATYPE_STRING));
			//Period
			if(measurementTerm.getPeriod() != null) {
				valueExpreArray[2] = new CONST("" + measurementTerm.getPeriod().intValue(), new STND(DATATYPE_INTEGER));
			} else {
				valueExpreArray[2] = new CONST("NaN", new STND(DATATYPE_INTEGER));
			}
			//Aggregated Function
			valueExpreArray[3] = new CONST(measurementTerm.getAggregator(), new STND(DATATYPE_STRING));
			// Function Parameter
			if(measurementTerm.getParams() != null) {
				valueExpreArray[4] = new CONST("" + measurementTerm.getParams().intValue(), new STND(DATATYPE_INTEGER));
			} else {
				valueExpreArray[4] = new CONST("NaN", new STND(DATATYPE_INTEGER));
			}
			
			FunctionalExpr aggregatedFuncExpr = new FunctionalExpr(new STND(AGGREGATED_METRIC_SLA_OPERATOR), valueExpreArray);
			
			SimpleDomainExpr simpleDomainExprePowerUsage = new SimpleDomainExpr(
								new CONST("" +measurementTerm.getBoundary().doubleValue(), new STND(DATATYPE_DECIMAL)), 
								new STND(COMPARATORS.get("GT")));
			
			TypeConstraintExpr typeConstraintExprVMCores = new TypeConstraintExpr(aggregatedFuncExpr, simpleDomainExprePowerUsage);
			
			// We create the guarantee
			Guaranteed.State aggregatedGuarantee = new Guaranteed.State(new ID(measurementTerm.getEvent() + "_for_" + ovfId), typeConstraintExprVMCores);
			
			// We create the agreement term
			Guaranteed[] guarantees = new Guaranteed[1];
			guarantees[0] = aggregatedGuarantee;
			//guarantees[1] = memoryState;
			
			AgreementTerm agreementTerm = new AgreementTerm(id, null, vars, guarantees);
			// Now we add this agreement terms to the others ones that there are in the SLATemplate
			addAgreementTerm(slaTemplate, agreementTerm); 
		}
	}
	
	private static void addEnergyVMConsumptionAgreementTerm(SLATemplate slaTemplate, OvfDefinition ovf, String ovfId) {
		// We estract the ASCETiC SLA Power VM info from OVF
		AsceticSLAInfo info = OVFUtils.getVMSlaInfo(ovf, ENERGY_USAGE_PER_VM_OVF, ovfId);
		
		if(info != null) {
			// ID for the agreement term
			ID id = new ID(ovfId + VM_GUARANTEES);
			
			// Variable Declarations		
			VariableDeclr variableDeclr = new VariableDeclr(
					new ID(VM_TYPE + ovfId),
					new FunctionalExpr(
							new STND(SUBSET_OF), 
							new ValueExpr[]{new ID(OVF_ITEM + ovfId)})					
					);
			
			// Power requirements
			FunctionalExpr functionalExprePowerUsage = new FunctionalExpr(
					new STND(ENERGY_USAGE_PER_VM_SLA_OPERATOR), 
					new ValueExpr[]{new ID(VM_TYPE + ovfId)});
			SimpleDomainExpr simpleDomainExprePowerUsage = new SimpleDomainExpr(
														new CONST(info.getBoundaryValue(), new STND(METRIC_UNITS.get(info.getMetricUnit()))), 
														new STND(COMPARATORS.get(info.getComparator())));
			
			TypeConstraintExpr typeConstraintExprVMCores = new TypeConstraintExpr(functionalExprePowerUsage, simpleDomainExprePowerUsage);
			
			Guaranteed.State powerUsageState = new Guaranteed.State(new ID(ENERGY_USAGE_PER_VM_SLA  + ovfId), typeConstraintExprVMCores);
			
			// We create the agreement term
			Guaranteed[] guarantees = new Guaranteed[1];
			guarantees[0] = powerUsageState;
			//guarantees[1] = memoryState;
			
			VariableDeclr[] vars = new VariableDeclr[1];
			vars[0] = variableDeclr;
 			
			AgreementTerm agreementTerm = new AgreementTerm(id, null, vars, guarantees);
			// Now we add this agreement terms to the others ones that there are in the SLATemplate
			addAgreementTerm(slaTemplate, agreementTerm); 
		}
	}

	private static void addPowerVMConsumptionAgreementTerm(SLATemplate slaTemplate, OvfDefinition ovf, String ovfId) {
		// We estract the ASCETiC SLA Power VM info from OVF
		AsceticSLAInfo info = OVFUtils.getVMSlaInfo(ovf, POWER_USAGE_PER_VM_OVF, ovfId);
		
		if(info != null) {
			// ID for the agreement term
			ID id = new ID(ovfId + VM_GUARANTEES);
			
			// Variable Declarations		
			VariableDeclr variableDeclr = new VariableDeclr(
					new ID(VM_TYPE + ovfId),
					new FunctionalExpr(
							new STND(SUBSET_OF), 
							new ValueExpr[]{new ID(OVF_ITEM + ovfId)})					
					);
			
			// Power requirements
			FunctionalExpr functionalExprePowerUsage = new FunctionalExpr(
					new STND(POWER_USAGE_PER_VM_SLA_OPERATOR), 
					new ValueExpr[]{new ID(VM_TYPE + ovfId)});
			SimpleDomainExpr simpleDomainExprePowerUsage = new SimpleDomainExpr(
														new CONST(info.getBoundaryValue(), new STND(METRIC_UNITS.get(info.getMetricUnit()))), 
														new STND(COMPARATORS.get(info.getComparator())));
			
			TypeConstraintExpr typeConstraintExprVMCores = new TypeConstraintExpr(functionalExprePowerUsage, simpleDomainExprePowerUsage);
			
			Guaranteed.State powerUsageState = new Guaranteed.State(new ID(POWER_USAGE_PER_VM_SLA  + ovfId), typeConstraintExprVMCores);
			
			// We create the agreement term
			Guaranteed[] guarantees = new Guaranteed[1];
			guarantees[0] = powerUsageState;
			//guarantees[1] = memoryState;
			
			VariableDeclr[] vars = new VariableDeclr[1];
			vars[0] = variableDeclr;
 			
			AgreementTerm agreementTerm = new AgreementTerm(id, null, vars, guarantees);
			// Now we add this agreement terms to the others ones that there are in the SLATemplate
			addAgreementTerm(slaTemplate, agreementTerm); 
		}
	}

	private static void addAppEnergyConsumptionAgreementTerm(SLATemplate slaTemplate, OvfDefinition ovf) {
		// We extract the ASCETiC Sla Info
		AsceticSLAInfo info = OVFUtils.getAppSlaInfo(ovf, APP_ENERGY_CONSUMPTION_OVF);
		
		if(info != null) {
			
			// ID for the agreement term
			ID id = new ID("App Guarantees");
			
			// We add the TypeConstraintExpre
			ValueExpr[] valueExpre = new ValueExpr[0];
			FunctionalExpr energyUsagePerAppFuncExpr = new FunctionalExpr(new STND(APP_ENERGY_CONSUMPTION_SLA_OPERATOR), valueExpre);
			
			
			SimpleDomainExpr simpleDomainExprAppEnergy = new SimpleDomainExpr(
																new CONST(
																		info.getBoundaryValue(), 
																		new STND(METRIC_UNITS.get(info.getMetricUnit()))),
																new STND(COMPARATORS.get(info.getComparator())));
			
			TypeConstraintExpr typeConstraintExprEnergyApp = new TypeConstraintExpr(energyUsagePerAppFuncExpr, simpleDomainExprAppEnergy);
			
			Guaranteed.State energyUsagePerAppGuarantee = new Guaranteed.State(new ID(APP_ENERGY_CONSUMPTION_SLA), typeConstraintExprEnergyApp);
						
			Guaranteed[] guarantees = new Guaranteed[1];
			guarantees[0] = energyUsagePerAppGuarantee;
			
			VariableDeclr[] vars = new VariableDeclr[0];
			
			AgreementTerm agreementTerm = new AgreementTerm(id, null, vars, guarantees);
			
			// Now we add this agreement terms to the others ones that there are in teh SLATemplate
			addAgreementTerm(slaTemplate, agreementTerm); 
		}
	}
	
	private static void addAgreementTerm(SLATemplate slaTemplate, AgreementTerm agreementTerm) {
		// Now we add this agreement terms to the others ones that there are in teh SLATemplate
		AgreementTerm[] agreementTerms = slaTemplate.getAgreementTerms();
		if(agreementTerms != null) {
			AgreementTerm[] newAgreementTerms = new AgreementTerm[agreementTerms.length + 1];

			for(int i = 0; i < agreementTerms.length ; i++) {
				newAgreementTerms[i] = agreementTerms[i];
			}

			newAgreementTerms[agreementTerms.length] = agreementTerm;
			slaTemplate.setAgreementTerms(newAgreementTerms);

		} else {
			AgreementTerm[] newAgreementTerms = new AgreementTerm[1];
			newAgreementTerms[0] = agreementTerm;
			slaTemplate.setAgreementTerms(newAgreementTerms);
		}
	}

	/**
	 * Adds the different IaaS SLAM information to the SLA Template
	 * @param slaTemplate
	 */
	protected static void addProperties(SLATemplate slaTemplate) {
		PRClient client = new PRClient();
		List<Provider> providers = client.getProviders();
		
		STND stndProperties = new STND("ProvidersList");
		
		// TODO this needs to be by configuration
		String value = "{\"ProvidersList\": [ \n ";
		
		for(Provider provider : providers) {
			value = value + "{\"provider-uuid\":\"" + provider.getId() + "\", \"p-slam-url\":\"" + provider.getSlamUrl() + "\"}\n";
		}
		
		value = value + "]}";
		
		slaTemplate.setPropertyValue(stndProperties, value);
	}
	
	/**
	 * Extract the necessary requirements comming from the OVF to be putted as agreement terms in the SLAM
	 * @param slaTemplate
	 * @param ovf
	 */
	protected static void addAgreementTerms(SLATemplate slaTemplate, OvfDefinition ovf) {
		
		// We check the number of VirtualSystems in the ovf document
		VirtualSystem[] virtualSystemArray = ovf.getVirtualSystemCollection().getVirtualSystemArray();
		
		// We create agreement terms for each one of the VMs
		AgreementTerm[] agreementTerms = new AgreementTerm[virtualSystemArray.length];
		
		for(int i = 0; i < virtualSystemArray.length; i++) {
			VirtualSystem virtualSystem = virtualSystemArray[i];
			
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
					new STND("http://www.slaatsoi.org/resources#power_usage_per_vm"), 
					new ValueExpr[]{new ID("VM_of_type_" + virtualSystem.getId())});
			SimpleDomainExpr simpleDomainExprePowerUsage = new SimpleDomainExpr(
														new CONST("10", new STND("http://www.w3.org/2001/XMLSchema#watt")), 
														new STND("http://www.slaatsoi.org/coremodel#less_than_or_equals"));
			
			TypeConstraintExpr typeConstraintExprVMCores = new TypeConstraintExpr(functionalExprePowerUsage, simpleDomainExprePowerUsage);
			
			Guaranteed.State powerUsageState = new Guaranteed.State(new ID("Power_Usage_for_"  + virtualSystem.getId()), typeConstraintExprVMCores);
			
			// Number of VM Cores
//			FunctionalExpr functionalExpreVMCores = new FunctionalExpr(
//					new STND("http://www.slaatsoi.org/resources#vm_cores"), 
//					new ValueExpr[]{new ID("VM_of_type_" + virtualSystem.getId())});
//			SimpleDomainExpr simpleDomainExpreVMCores = new SimpleDomainExpr(
//														new CONST("" + virtualSystem.getVirtualHardwareSection().getNumberOfVirtualCPUs(), 
//																  new STND("http://www.slaatsoi.org/coremodel/units#integer")), 
//														new STND("http://www.slaatsoi.org/coremodel#equals"));
//			
//			TypeConstraintExpr typeConstraintExprVMCores = new TypeConstraintExpr(functionalExpreVMCores, simpleDomainExpreVMCores);
//			
//			Guaranteed.State cpuCoresState = new Guaranteed.State(new ID("CPU_CORES_for_"  + virtualSystem.getId()), typeConstraintExprVMCores);
//			
//			// TODO Careful with the memory units... assuming everything is MB
//			// Number of VM Memory
//			FunctionalExpr functionalExpreVMMemory = new FunctionalExpr(
//					new STND("http://www.slaatsoi.org/resources#memory"), 
//					new ValueExpr[]{new ID("VM_of_type_" + virtualSystem.getId())});
//			SimpleDomainExpr simpleDomainExpreVMMemory = new SimpleDomainExpr(
//														new CONST("" + virtualSystem.getVirtualHardwareSection().getMemorySize(), 
//																  new STND("http://www.slaatsoi.org/coremodel/units#MB")), 
//														new STND("http://www.slaatsoi.org/coremodel#equals"));
//			
//			TypeConstraintExpr typeConstraintExprVMMemory = new TypeConstraintExpr(functionalExpreVMMemory, simpleDomainExpreVMMemory);
//			
//			Guaranteed.State memoryState = new Guaranteed.State(new ID("MEMORY_for_"  + virtualSystem.getId()), typeConstraintExprVMMemory);
			
			Guaranteed[] guarantees = new Guaranteed[1];
			guarantees[0] = powerUsageState;
			//guarantees[1] = memoryState;
			
			VariableDeclr[] vars = new VariableDeclr[1];
			vars[0] = variableDeclr;
 			
			AgreementTerm agreementTerm = new AgreementTerm(id, null, vars, guarantees);
			agreementTerms[i] = agreementTerm;
		}
		
		slaTemplate.setAgreementTerms(agreementTerms);
	}
	
	/**
	 * It adds the InterfaceDclr section to an SLATemplate
	 * @param slaTemplate 
	 */
	protected static void addInterfaceDclr(SLATemplate slaTemplate, OvfDefinition ovf, String ovfURL) {
		VirtualSystem[] virtualSystemArray = ovf.getVirtualSystemCollection().getVirtualSystemArray();
		InterfaceDeclr[] ifaces = new InterfaceDeclr[virtualSystemArray.length];
		
		for(int i = 0; i < virtualSystemArray.length; i++) {
		
			// Creating an ID using the OVF ID for the Virtual System Collection
			ID id = new ID("OVF-Item-" + virtualSystemArray[i].getId());
			// Provider ID
			ID idProvider = new ID("AsceticProvider");
			
			

            ResourceType resType=new ResourceType("OVFAppliance");
//            Endpoint endpoint=new Endpoint(new ID("haproxy-VM-Type"), new UUID("VM-Manager ID"), sla.HTTP);
//            Endpoint[] endpoints={endpoint};
//            InterfaceDeclr intDecl=new InterfaceDeclr(new ID("haproxy"), new ID("AsceticProvider"), endpoints, resType);


//		    interface_resource_type{
//	            name = OVFAppliance
//	        }

			
			
			
			// TODO
			//		// It is necessary to create an interface
//					InterfaceResourceTypeType interfaceResourceType = new InterfaceResourceTypeTypeImpl(null);
//					interfaceResourceType.setName("OVFAppliance");
	//		Interface.Specification ispec = new Interface.Specification("OVFAppliance");
			//		iface.set
			//		
			//		// By the example looks like we only have one unique InterfaceDclr.
//			InterfaceDeclrType interfaceDeclaration2 = new InterfaceDeclrTypeImpl(null);
//			interfaceDeclaration2.getInterface();
			InterfaceDeclr interfaceDeclaration = new InterfaceDeclr(id, idProvider, resType);



			// We set the properties
			interfaceDeclaration.setPropertyValue(new STND("OVF_URL"), ovfURL);
			logger.info("OVF_URL: " + ovfURL);

			// We create the Endpoint:
			STND protocol = new STND("http://www.slaatsoi.org/slamodel#HTTP");
			Endpoint[] endPoints = new Endpoint[1];

			ID idEndPoint = new ID(virtualSystemArray[i].getId() + "-VM-Type");

			Endpoint endPoint = new Endpoint(idEndPoint, protocol);
			endPoint.setLocation(new UUID("VM-Manager ID"));
			endPoint.setPropertyValue(new STND("OVF_VirtualSystem_ID"), virtualSystemArray[i].getId());

			endPoints[0] = endPoint;
			interfaceDeclaration.setEndpoints(endPoints);
			
			ifaces[i] = interfaceDeclaration;
		}
		
		slaTemplate.setInterfaceDeclrs(ifaces);
	}
	
	/**
	 * It creates the party fields for an SLA Template reading it from the configuration
	 * @param slaTemplate to which the party information needs to be added
	 */
	protected static void addProviderEndPointToTemplate(SLATemplate slaTemplate) {		
		ID id = new ID("AsceticProvider");
		STND stnd = new STND("http://www.slaatsoi.org/slamodel#provider");
		
		addParties(slaTemplate,stnd,id); 
	}
	
	/**
	 * It creates the user party fields for an SLA Template reading it from the configuration
	 * @param slaTemplate to which the party information needs to be added
	 */
	protected static void addUserEndPointToTemplate(SLATemplate slaTemplate) {
		// TODO revisit this value in the future...
		ID id = new ID("333");
		STND stnd = new STND("http://www.slaatsoi.org/slamodel#customer");
		
		addParties(slaTemplate,stnd,id); 
	}

	private static void addParties(SLATemplate slaTemplate, STND stnd, ID id) {
		Party party = new Party(id, stnd);
		party.setAgreementRole(stnd);

		STND stndEntry = new STND("http://www.slaatsoi.org/slamodel#gslam_epr");
		party.setPropertyValue(stndEntry, Configuration.slamURL);

		if(slaTemplate.getParties() != null) {
			Party[] parties = appendValue(slaTemplate.getParties(), party);
			slaTemplate.setParties(parties);
		} else {
			Party[] parties = new Party[1];
			parties[0] = party;
			slaTemplate.setParties(parties);
		}
	}

	private static Party[] appendValue(Party[] parties, Party party) {	  
		ArrayList<Party> temp = new ArrayList<Party>(Arrays.asList(parties));
		temp.add(party);
		return temp.toArray(new Party[temp.size()]);
	}
}
