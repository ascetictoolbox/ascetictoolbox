package eu.ascetic.paas.applicationmanager.slam;

import java.util.ArrayList;
import java.util.Arrays;

import org.slasoi.slamodel.core.ConstraintExpr;
import org.slasoi.slamodel.core.FunctionalExpr;
import org.slasoi.slamodel.core.SimpleDomainExpr;
import org.slasoi.slamodel.core.TypeConstraintExpr;
import org.slasoi.slamodel.primitives.CONST;
import org.slasoi.slamodel.primitives.Expr;
import org.slasoi.slamodel.primitives.ID;
import org.slasoi.slamodel.primitives.STND;
import org.slasoi.slamodel.primitives.UUID;
import org.slasoi.slamodel.primitives.ValueExpr;
import org.slasoi.slamodel.service.Interface;
import org.slasoi.slamodel.sla.AgreementTerm;
import org.slasoi.slamodel.sla.Endpoint;
import org.slasoi.slamodel.sla.Guaranteed;
import org.slasoi.slamodel.sla.InterfaceDeclr;
import org.slasoi.slamodel.sla.Party;
import org.slasoi.slamodel.sla.SLATemplate;
import org.slasoi.slamodel.sla.VariableDeclr;
import org.slasoi.slamodel.vocab.ext.Expression;

import eu.ascetic.paas.applicationmanager.conf.Configuration;
import eu.ascetic.utils.ovf.api.OvfDefinition;
import eu.ascetic.utils.ovf.api.VirtualSystem;
import eu.slaatsoi.slamodel.InterfaceDeclrType;
import eu.slaatsoi.slamodel.InterfaceResourceTypeType;
import eu.slaatsoi.slamodel.impl.ConstraintExprDocumentImpl;
import eu.slaatsoi.slamodel.impl.InterfaceDeclrDocumentImpl;
import eu.slaatsoi.slamodel.impl.InterfaceDeclrTypeImpl;
import eu.slaatsoi.slamodel.impl.InterfaceResourceTypeTypeImpl;
import eu.slaatsoi.slamodel.impl.SLATemplateDocumentImpl;

public class SLATemplateCreator {
	
	/**
	 * Generates and slaTemplate from an OVF file
	 * @param ovf from which to generate the SLA Template
	 * @return the template
	 */
	public static SLATemplate generateSLATemplate(OvfDefinition ovf, String ovfURL) {
		SLATemplate slaTemplate = new SLATemplate();
		UUID uuid = new UUID("ASCETiC-SLaTemplate-Example-01");
		slaTemplate.setUuid(uuid);
		
		// We add the parties section
		addProviderEndPointToTemplate(slaTemplate);
		addUserEndPointToTemplate(slaTemplate);
		
		// We add the InterfaceDclr section
		addInterfaceDclr(slaTemplate, ovf, ovfURL);
		addAgreementTerms(slaTemplate, ovf);
		
		return slaTemplate;
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
			
			// Number of VM Cores
			FunctionalExpr functionalExpreVMCores = new FunctionalExpr(
					new STND("http://www.slaatsoi.org/resources#vm_cores"), 
					new ValueExpr[]{new ID("VM_of_type_" + virtualSystem.getId())});
			SimpleDomainExpr simpleDomainExpreVMCores = new SimpleDomainExpr(
														new CONST("" + virtualSystem.getVirtualHardwareSection().getNumberOfVirtualCPUs(), 
																  new STND("http://www.slaatsoi.org/coremodel/units#integer")), 
														new STND("http://www.slaatsoi.org/coremodel#equals"));
			
			TypeConstraintExpr typeConstraintExprVMCores = new TypeConstraintExpr(functionalExpreVMCores, simpleDomainExpreVMCores);
			
			Guaranteed.State cpuCoresState = new Guaranteed.State(new ID("CPU_CORES_for_"  + virtualSystem.getId()), typeConstraintExprVMCores);
			
			// TODO Careful with the memory units... assuming everything is MB
			// Number of VM Memory
			FunctionalExpr functionalExpreVMMemory = new FunctionalExpr(
					new STND("http://www.slaatsoi.org/resources#memory"), 
					new ValueExpr[]{new ID("VM_of_type_" + virtualSystem.getId())});
			SimpleDomainExpr simpleDomainExpreVMMemory = new SimpleDomainExpr(
														new CONST("" + virtualSystem.getVirtualHardwareSection().getMemorySize(), 
																  new STND("http://www.slaatsoi.org/coremodel/units#MB")), 
														new STND("http://www.slaatsoi.org/coremodel#equals"));
			
			TypeConstraintExpr typeConstraintExprVMMemory = new TypeConstraintExpr(functionalExpreVMMemory, simpleDomainExpreVMMemory);
			
			Guaranteed.State memoryState = new Guaranteed.State(new ID("MEMORY_for_"  + virtualSystem.getId()), typeConstraintExprVMMemory);
			
			Guaranteed[] guarantees = new Guaranteed[2];
			guarantees[0] = cpuCoresState;
			guarantees[1] = memoryState;
			
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
			ID id = new ID(virtualSystemArray[i].getId());
			// Provider ID
			ID idProvider = new ID("AsceticProvider");

//		    interface_resource_type{
//	            name = OVFAppliance
//	        }

			
			
			
			// TODO
			//		// It is necessary to create an interface
//					InterfaceResourceTypeType interfaceResourceType = new InterfaceResourceTypeTypeImpl(null);
//					interfaceResourceType.setName("OVFAppliance");
			Interface.Specification ispec = new Interface.Specification("OVFAppliance");
			//		iface.set
			//		
			//		// By the example looks like we only have one unique InterfaceDclr.
//			InterfaceDeclrType interfaceDeclaration2 = new InterfaceDeclrTypeImpl(null);
//			interfaceDeclaration2.getInterface();
			InterfaceDeclr interfaceDeclaration = new InterfaceDeclr(id, idProvider, ispec);



			// We set the properties
			interfaceDeclaration.setPropertyValue(new STND("OVF_URL"), ovfURL);

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
		ID id = new ID("ASCETiCUser");
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
