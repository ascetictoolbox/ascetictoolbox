package eu.ascetic.paas.applicationmanager.slam;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import java.io.File;
import java.io.IOException;
import java.io.StringReader;
import java.net.URISyntaxException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;

import org.junit.Before;
import org.junit.Test;
import org.slasoi.gslam.syntaxconverter.SLASOITemplateRenderer;
import org.slasoi.slamodel.primitives.STND;
import org.slasoi.slamodel.sla.SLATemplate;

import eu.ascetic.paas.applicationmanager.conf.Configuration;
import eu.ascetic.paas.applicationmanager.dao.testUtil.MockWebServer;
import eu.ascetic.paas.applicationmanager.model.SLAApplicationTerms;
import eu.ascetic.paas.applicationmanager.model.SLAInfoTerm;
import eu.ascetic.paas.applicationmanager.ovf.OVFUtils;
import eu.ascetic.paas.applicationmanager.slam.sla.model.AgreementTerm;
import eu.ascetic.paas.applicationmanager.slam.sla.model.FuncExpr;
import eu.ascetic.paas.applicationmanager.slam.sla.model.Parameter;
import eu.ascetic.paas.applicationmanager.slam.sla.model.SimpleDomainExpr;
import eu.ascetic.utils.ovf.api.OvfDefinition;
import eu.slaatsoi.slamodel.SLATemplateDocument;

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
 */

public class SLATemplateCreatorTest {
	private String threeTierWebAppOvfFile = "3tier-webapp.ovf.xml";
	private String threeTierWebAppOvfString;
	private String aAOvfFile = "na-ovf.xml";
	private String aAfString;
	private String ovfSelfAdaptationFile = "output-file-ovf-appPackager.ovf";
	private String ovfSelfAdaptationString;
	private String na1300OvfFile = "na-1300.ovf.xml";
	private String na1300OvfString;
	private MockWebServer mServer;
	private String mBaseURL = "http://localhost:";
	private String value =  "{\"ProvidersList\": [ \n " +
				"{\"provider-uuid\":\"1\", \"p-slam-url\":\"http://10.0.9.149:8080/services/asceticNegotiation?wsdl\"}\n" +
				"{\"provider-uuid\":\"2\", \"p-slam-url\":\"http://10.0.10.149:8080/services/asceticNegotiation?wsdl\"}\n" +
			"]}";
	
	/**
	 * We just read an ovf example... 
	 * @throws IOException
	 * @throws URISyntaxException
	 */
	@Before
	public void setup() throws IOException, URISyntaxException {
		File file = new File(this.getClass().getResource( "/" + threeTierWebAppOvfFile ).toURI());		
		threeTierWebAppOvfString = readFile(file.getAbsolutePath(), StandardCharsets.UTF_8);
		file = new File(this.getClass().getResource( "/" + ovfSelfAdaptationFile ).toURI());		
		ovfSelfAdaptationString = readFile(file.getAbsolutePath(), StandardCharsets.UTF_8);
		file = new File(this.getClass().getResource( "/" + aAOvfFile ).toURI());		
		aAfString = readFile(file.getAbsolutePath(), StandardCharsets.UTF_8);
		mServer = new MockWebServer();
		mServer.start();
		mBaseURL = "http://localhost:";
		mBaseURL = mBaseURL + mServer.getPort();
		System.out.println(ovfSelfAdaptationString);
		file = new File(this.getClass().getResource( "/" + na1300OvfFile ).toURI());		
		na1300OvfString = readFile(file.getAbsolutePath(), StandardCharsets.UTF_8);
	}
	
	@Test
	public void checkIfAppTermsApplyTest() {
		// If the object is null we ignore this for the SLA Template Creation Proccess
		assertNull(SLATemplateCreator.checkIfAppTermsApply("Whatever", null));
		
		// If the list object is null, also return null
		SLAApplicationTerms applicationTerms = new SLAApplicationTerms();
		assertNull(SLATemplateCreator.checkIfAppTermsApply("Whatever", applicationTerms));
		
		// If the list does not contain what we are looking for return null
		SLAInfoTerm infoTerm1 = new SLAInfoTerm();
		infoTerm1.setSlaTerm("Term1");
		SLAInfoTerm infoTerm2 = new SLAInfoTerm();
		infoTerm2.setSlaTerm("Term2");
		
		List<SLAInfoTerm> infoTerms = new ArrayList<SLAInfoTerm>();
		infoTerms.add(infoTerm1);
		infoTerms.add(infoTerm2);
		applicationTerms.setSlaInfoTerms(infoTerms);
		
		assertNull(SLATemplateCreator.checkIfAppTermsApply("Whatever", applicationTerms));
		
		// We get the info term if we input the correct one
		SLAInfoTerm infoTerm3 = new SLAInfoTerm();
		infoTerm3.setSlaTerm("Whatever");
		infoTerms.add(infoTerm3);
		
		
		assertEquals(infoTerm3, SLATemplateCreator.checkIfAppTermsApply("Whatever", applicationTerms));
	}
	
	@Test
	public void totalConversionWithNewAppAgreement() throws Exception {
		// We start fake provider registry
		setupFakeProviderRegistry();
				
		// We read the OVF definition from file
		OvfDefinition ovfDefinition = OVFUtils.getOvfDefinition(ovfSelfAdaptationString);
		
		// We create the new SLAAppTerm
		SLAApplicationTerms appTerms = new SLAApplicationTerms();
		
		SLAInfoTerm infoTerm = new SLAInfoTerm();
		infoTerm.setComparator("LTE");
		infoTerm.setMetricUnit("WattHour");
		infoTerm.setSlaTerm(OVFToSLANames.APP_ENERGY_CONSUMPTION_OVF);
		infoTerm.setSlaType("violation");
		infoTerm.setValue("3000");
		
		List<SLAInfoTerm> infoTerms = new ArrayList<SLAInfoTerm>();
		infoTerms.add(infoTerm);
		
		appTerms.setSlaInfoTerms(infoTerms);
		
		// We setup the propierties section for the template... /
		SLATemplate slaTemplate = SLATemplateCreator.generateSLATemplate(ovfDefinition,  "http://localhost/application-manager/appid/deployments/111/ovf", appTerms);
		
		SLASOITemplateRenderer slasoiTemplateRenderer = new SLASOITemplateRenderer();
		SLATemplateDocument slaTemplateRendered = SLATemplateDocument.Factory.parse(slasoiTemplateRenderer.renderSLATemplate(slaTemplate));
		
		String slaTemplateString = slaTemplateRendered.toString();
		
		System.out.println("SLA rendered as XML: ############################## ");
		System.out.println(slaTemplateString);
		
		// TEST
		eu.ascetic.paas.applicationmanager.slam.sla.model.SLATemplate slat = null;
		
		try {
			JAXBContext jaxbContext = JAXBContext.newInstance(eu.ascetic.paas.applicationmanager.slam.sla.model.SLATemplate.class);
			Unmarshaller jaxbUnmarshaller = jaxbContext.createUnmarshaller();
			slat = (eu.ascetic.paas.applicationmanager.slam.sla.model.SLATemplate) jaxbUnmarshaller.unmarshal(new StringReader(slaTemplateString));
		} catch(JAXBException jaxbExpcetion) {
			jaxbExpcetion.printStackTrace();
		}
		
		// We verify the application guarantees
		assertEquals("App Guarantees", slat.getAgreemenTerms().get(0).getId());
		// Energy App Guarantees
		assertEquals(OVFToSLANames.APP_ENERGY_CONSUMPTION_SLA, slat.getAgreemenTerms().get(0).getGuaranteed().getState().getId());
		assertEquals(OVFToSLANames.APP_ENERGY_CONSUMPTION_SLA_OPERATOR, 
				slat.getAgreemenTerms().get(0).getGuaranteed().getState().getConstraint().getTypeConstraintExpr().getValue().getFuncExpr().getOperator());
		assertEquals(OVFToSLANames.COMPARATORS.get("LTE"),
				slat.getAgreemenTerms().get(0).getGuaranteed().getState().getConstraint().getTypeConstraintExpr().getDomain().getSimpleDomainExpr().getComparisonOp());
		assertEquals("3000",
				slat.getAgreemenTerms().get(0).getGuaranteed().getState().getConstraint().getTypeConstraintExpr().getDomain().getSimpleDomainExpr().getValue().getConstVariable().getValue());
		assertEquals(OVFToSLANames.METRIC_UNITS.get("WattHour"),
				slat.getAgreemenTerms().get(0).getGuaranteed().getState().getConstraint().getTypeConstraintExpr().getDomain().getSimpleDomainExpr().getValue().getConstVariable().getDatatype());

	}
	
	@Test
	public void totalConversionWithNA1300() throws Exception {
		// We start fake provider registry
		setupFakeProviderRegistry();
				
		// We read the OVF definition from file
		OvfDefinition ovfDefinition = OVFUtils.getOvfDefinition(na1300OvfString);
		
		// We setup the propierties section for the template... /
		SLATemplate slaTemplate = SLATemplateCreator.generateSLATemplate(ovfDefinition,  "http://localhost/application-manager/appid/deployments/111/ovf", null);
		
		SLASOITemplateRenderer slasoiTemplateRenderer = new SLASOITemplateRenderer();
		SLATemplateDocument slaTemplateRendered = SLATemplateDocument.Factory.parse(slasoiTemplateRenderer.renderSLATemplate(slaTemplate));
		
		String slaTemplateString = slaTemplateRendered.toString();
		
		System.out.println("SLA rendered as XML: ############################## ");
		System.out.println("SLA rendered as XML: ############################## ");
		System.out.println("SLA rendered as XML: ############################## ");
		System.out.println("SLA rendered as XML: ############################## ");
		System.out.println(slaTemplateString);
		
		// TEST
		eu.ascetic.paas.applicationmanager.slam.sla.model.SLATemplate slat = null;
		
		try {
			JAXBContext jaxbContext = JAXBContext.newInstance(eu.ascetic.paas.applicationmanager.slam.sla.model.SLATemplate.class);
			Unmarshaller jaxbUnmarshaller = jaxbContext.createUnmarshaller();
			slat = (eu.ascetic.paas.applicationmanager.slam.sla.model.SLATemplate) jaxbUnmarshaller.unmarshal(new StringReader(slaTemplateString));
		} catch(JAXBException jaxbExpcetion) {
			jaxbExpcetion.printStackTrace();
		}
		
		// We verify the application guarantees
		assertEquals("Aggregated Guarantees", slat.getAgreemenTerms().get(2).getId());
		// Energy App Guarantees
		assertEquals("anticipatedWorkload_for_NA-Server", slat.getAgreemenTerms().get(2).getGuaranteed().getState().getId());
		assertEquals("http://www.slaatsoi.org/resources#aggregated_event_metric_over_period", 
				slat.getAgreemenTerms().get(2).getGuaranteed().getState().getConstraint().getTypeConstraintExpr().getValue().getFuncExpr().getOperator());
		assertEquals(OVFToSLANames.COMPARATORS.get("LTE"),
				slat.getAgreemenTerms().get(2).getGuaranteed().getState().getConstraint().getTypeConstraintExpr().getDomain().getSimpleDomainExpr().getComparisonOp());
		assertEquals("0.0",
				slat.getAgreemenTerms().get(2).getGuaranteed().getState().getConstraint().getTypeConstraintExpr().getDomain().getSimpleDomainExpr().getValue().getConstVariable().getValue());
		assertEquals(OVFToSLANames.METRIC_UNITS.get("decimal"),
				slat.getAgreemenTerms().get(2).getGuaranteed().getState().getConstraint().getTypeConstraintExpr().getDomain().getSimpleDomainExpr().getValue().getConstVariable().getDatatype());

	}
	
	@Test
	public void totalConversion() throws Exception {
		// We start fake provider registry
		setupFakeProviderRegistry();
		
		// We read the OVF definition from file
		OvfDefinition ovfDefinition = OVFUtils.getOvfDefinition(ovfSelfAdaptationString);
		
		// We setup the propierties section for the template... /
		SLATemplate slaTemplate = SLATemplateCreator.generateSLATemplate(ovfDefinition,  "http://localhost/application-manager/appid/deployments/111/ovf", null);
		
		SLASOITemplateRenderer slasoiTemplateRenderer = new SLASOITemplateRenderer();
		SLATemplateDocument slaTemplateRendered = SLATemplateDocument.Factory.parse(slasoiTemplateRenderer.renderSLATemplate(slaTemplate));
		
		String slaTemplateString = slaTemplateRendered.toString();
		
		System.out.println("SLA rendered as XML: ############################## ");
		System.out.println(slaTemplateString);
		
		// TEST
		eu.ascetic.paas.applicationmanager.slam.sla.model.SLATemplate slat = null;
		
		try {
			JAXBContext jaxbContext = JAXBContext.newInstance(eu.ascetic.paas.applicationmanager.slam.sla.model.SLATemplate.class);
			Unmarshaller jaxbUnmarshaller = jaxbContext.createUnmarshaller();
			slat = (eu.ascetic.paas.applicationmanager.slam.sla.model.SLATemplate) jaxbUnmarshaller.unmarshal(new StringReader(slaTemplateString));
		} catch(JAXBException jaxbExpcetion) {
			jaxbExpcetion.printStackTrace();
		}
		
		// We verify that the UUID is correctly set
		assertEquals("ASCETiC-SLaTemplate-Example-01", slat.getUUID());
		assertEquals("sla_at_soi_sla_model_v1.0", slat.getModelVersion());
		
		// We verify that we set correctly the Provider list
		assertEquals(1, slat.getProperties().getEntries().size());
		assertEquals(value, slat.getProperties().getEntries().get(0).getValue());
		
		// We verify the we set correctly the Party list
		assertEquals(2, slat.getParties().size());
		assertEquals("AsceticProvider", slat.getParties().get(0).getId());
		assertEquals("http://www.slaatsoi.org/slamodel#provider", slat.getParties().get(0).getRole());
		assertEquals("http://www.slaatsoi.org/slamodel#gslam_epr", slat.getParties().get(0).getProperties().getEntries().get(0).getKey());
		assertEquals("http://111.222.333.444:8080/services/asceticNegotiation?wsdl", slat.getParties().get(0).getProperties().getEntries().get(0).getValue());
		assertEquals("333", slat.getParties().get(1).getId());
		assertEquals("http://www.slaatsoi.org/slamodel#customer", slat.getParties().get(1).getRole());
		assertEquals("http://www.slaatsoi.org/slamodel#gslam_epr", slat.getParties().get(1).getProperties().getEntries().get(0).getKey());
		assertEquals("http://111.222.333.444:8080/services/asceticNegotiation?wsdl", slat.getParties().get(1).getProperties().getEntries().get(0).getValue());
		
		// We verify the interface declarition is correct
		assertEquals("OVF_URL", slat.getInterfaceDeclrs().get(0).getProperties().getEntries().get(0).getKey());
		assertEquals("http://localhost/application-manager/appid/deployments/111/ovf", slat.getInterfaceDeclrs().get(0).getProperties().getEntries().get(0).getValue());
		assertEquals("OVF-Item-NA-HAProxy", slat.getInterfaceDeclrs().get(0).getId());
		assertEquals("AsceticProvider", slat.getInterfaceDeclrs().get(0).getProviderRef());
		assertEquals("OVF_VirtualSystem_ID", slat.getInterfaceDeclrs().get(0).getEndPoint().getProperties().getEntries().get(0).getKey());
		assertEquals("NA-HAProxy", slat.getInterfaceDeclrs().get(0).getEndPoint().getProperties().getEntries().get(0).getValue());
		assertEquals("NA-HAProxy-VM-Type", slat.getInterfaceDeclrs().get(0).getEndPoint().getId());
		assertEquals("VM-Manager ID", slat.getInterfaceDeclrs().get(0).getEndPoint().getLocation());
		assertEquals("http://www.slaatsoi.org/slamodel#HTTP", slat.getInterfaceDeclrs().get(0).getEndPoint().getProtocol());
		assertEquals("OVFAppliance", slat.getInterfaceDeclrs().get(0).getIntf().getInterfaceResourceType().getName());
		
		// We have to have 4 Agreement Terms
		assertEquals(7, slat.getAgreemenTerms().size());
		
		// We verify the application guarantees
		assertEquals("App Guarantees", slat.getAgreemenTerms().get(0).getId());
		// Energy App Guarantees
		assertEquals(OVFToSLANames.APP_ENERGY_CONSUMPTION_SLA, slat.getAgreemenTerms().get(0).getGuaranteed().getState().getId());
		assertEquals(OVFToSLANames.APP_ENERGY_CONSUMPTION_SLA_OPERATOR, 
				     slat.getAgreemenTerms().get(0).getGuaranteed().getState().getConstraint().getTypeConstraintExpr().getValue().getFuncExpr().getOperator());
		assertEquals(OVFToSLANames.COMPARATORS.get("LT"),
				     slat.getAgreemenTerms().get(0).getGuaranteed().getState().getConstraint().getTypeConstraintExpr().getDomain().getSimpleDomainExpr().getComparisonOp());
		assertEquals("2000",
				     slat.getAgreemenTerms().get(0).getGuaranteed().getState().getConstraint().getTypeConstraintExpr().getDomain().getSimpleDomainExpr().getValue().getConstVariable().getValue());
		assertEquals(OVFToSLANames.METRIC_UNITS.get("WattHour"),
			         slat.getAgreemenTerms().get(0).getGuaranteed().getState().getConstraint().getTypeConstraintExpr().getDomain().getSimpleDomainExpr().getValue().getConstVariable().getDatatype());
		// Power App Guarantees
		assertEquals(OVFToSLANames.APP_POWER_CONSUMPTION_SLA, slat.getAgreemenTerms().get(1).getGuaranteed().getState().getId());
		assertEquals(OVFToSLANames.APP_POWER_CONSUMPTION_SLA_OPERATOR, 
				     slat.getAgreemenTerms().get(1).getGuaranteed().getState().getConstraint().getTypeConstraintExpr().getValue().getFuncExpr().getOperator());
		assertEquals(OVFToSLANames.COMPARATORS.get("LTE"),
				     slat.getAgreemenTerms().get(1).getGuaranteed().getState().getConstraint().getTypeConstraintExpr().getDomain().getSimpleDomainExpr().getComparisonOp());
		assertEquals("2000",
				     slat.getAgreemenTerms().get(1).getGuaranteed().getState().getConstraint().getTypeConstraintExpr().getDomain().getSimpleDomainExpr().getValue().getConstVariable().getValue());
		assertEquals(OVFToSLANames.METRIC_UNITS.get("Watt"),
			         slat.getAgreemenTerms().get(1).getGuaranteed().getState().getConstraint().getTypeConstraintExpr().getDomain().getSimpleDomainExpr().getValue().getConstVariable().getDatatype());
		// Price Per Hour App Guarantees
		assertEquals(OVFToSLANames.APP_PRICE_PER_HOUR_SLA, slat.getAgreemenTerms().get(2).getGuaranteed().getState().getId());
		assertEquals(OVFToSLANames.APP_PRICE_PER_HOUR_SLA_OPERATOR, 
				     slat.getAgreemenTerms().get(2).getGuaranteed().getState().getConstraint().getTypeConstraintExpr().getValue().getFuncExpr().getOperator());
		assertEquals(OVFToSLANames.COMPARATORS.get("LTE"),
				     slat.getAgreemenTerms().get(2).getGuaranteed().getState().getConstraint().getTypeConstraintExpr().getDomain().getSimpleDomainExpr().getComparisonOp());
		assertEquals("2000",
				     slat.getAgreemenTerms().get(2).getGuaranteed().getState().getConstraint().getTypeConstraintExpr().getDomain().getSimpleDomainExpr().getValue().getConstVariable().getValue());
		assertEquals(OVFToSLANames.METRIC_UNITS.get("EUR"),
			         slat.getAgreemenTerms().get(2).getGuaranteed().getState().getConstraint().getTypeConstraintExpr().getDomain().getSimpleDomainExpr().getValue().getConstVariable().getDatatype());
		
		// We verify that we add the Agreement Terms per VM
		assertEquals("NA-HAProxy_Guarantees", slat.getAgreemenTerms().get(3).getId());
		assertEquals("VM_of_type_NA-HAProxy", slat.getAgreemenTerms().get(3).getVariableDeclr().getVar());
		assertEquals("http://www.slaatsoi.org/coremodel#subset_of", slat.getAgreemenTerms().get(3).getVariableDeclr().getExpr().getValueExpr().getFuncExpr().getOperator());
		assertEquals("OVF-Item-NA-HAProxy", slat.getAgreemenTerms().get(3).getVariableDeclr().getExpr().getValueExpr().getFuncExpr().getParameters().get(0).getId());
		assertEquals("Power_Usage_for_NA-HAProxy", slat.getAgreemenTerms().get(3).getGuaranteed().getState().getId());
		assertEquals(OVFToSLANames.POWER_USAGE_PER_VM_SLA_OPERATOR, slat.getAgreemenTerms().get(3).getGuaranteed().getState().getConstraint().getTypeConstraintExpr().getValue().getFuncExpr().getOperator());
		assertEquals("VM_of_type_NA-HAProxy", slat.getAgreemenTerms().get(3).getGuaranteed().getState().getConstraint().getTypeConstraintExpr().getValue().getFuncExpr().getParameters().get(0).getId());
		assertEquals(OVFToSLANames.COMPARATORS.get("LT"),
			     slat.getAgreemenTerms().get(3).getGuaranteed().getState().getConstraint().getTypeConstraintExpr().getDomain().getSimpleDomainExpr().getComparisonOp());
		assertEquals("50",
			     slat.getAgreemenTerms().get(3).getGuaranteed().getState().getConstraint().getTypeConstraintExpr().getDomain().getSimpleDomainExpr().getValue().getConstVariable().getValue());
		assertEquals(OVFToSLANames.METRIC_UNITS.get("Watt"),
		         slat.getAgreemenTerms().get(3).getGuaranteed().getState().getConstraint().getTypeConstraintExpr().getDomain().getSimpleDomainExpr().getValue().getConstVariable().getDatatype());
		
		
		// We verify that we add the Agreement Terms per VM
		assertEquals("NA-HAProxy_Guarantees", slat.getAgreemenTerms().get(4).getId());
		assertEquals("VM_of_type_NA-HAProxy", slat.getAgreemenTerms().get(4).getVariableDeclr().getVar());
		assertEquals("http://www.slaatsoi.org/coremodel#subset_of", slat.getAgreemenTerms().get(4).getVariableDeclr().getExpr().getValueExpr().getFuncExpr().getOperator());
		assertEquals("OVF-Item-NA-HAProxy", slat.getAgreemenTerms().get(4).getVariableDeclr().getExpr().getValueExpr().getFuncExpr().getParameters().get(0).getId());
		assertEquals("Energy_Usage_for_NA-HAProxy", slat.getAgreemenTerms().get(4).getGuaranteed().getState().getId());
		assertEquals(OVFToSLANames.ENERGY_USAGE_PER_VM_SLA_OPERATOR, slat.getAgreemenTerms().get(4).getGuaranteed().getState().getConstraint().getTypeConstraintExpr().getValue().getFuncExpr().getOperator());
		assertEquals("VM_of_type_NA-HAProxy", slat.getAgreemenTerms().get(4).getGuaranteed().getState().getConstraint().getTypeConstraintExpr().getValue().getFuncExpr().getParameters().get(0).getId());
		assertEquals(OVFToSLANames.COMPARATORS.get("LT"),
			     slat.getAgreemenTerms().get(4).getGuaranteed().getState().getConstraint().getTypeConstraintExpr().getDomain().getSimpleDomainExpr().getComparisonOp());
		assertEquals("50",
			     slat.getAgreemenTerms().get(4).getGuaranteed().getState().getConstraint().getTypeConstraintExpr().getDomain().getSimpleDomainExpr().getValue().getConstVariable().getValue());
		assertEquals(OVFToSLANames.METRIC_UNITS.get("WattHour"),
		         slat.getAgreemenTerms().get(4).getGuaranteed().getState().getConstraint().getTypeConstraintExpr().getDomain().getSimpleDomainExpr().getValue().getConstVariable().getDatatype());
		
		
		// We verify the Aggregated Guarantees
		AgreementTerm agreementTerm =  slat.getAgreemenTerms().get(5);
		assertEquals("Aggregated Guarantees", agreementTerm.getId());
		//assertEquals("violation_type", agreementTerm.getGuaranteed().getEntry().getKey());
		//assertEquals("information", agreementTerm.getGuaranteed().getEntry().getValue());
		assertEquals("searchForNewsItems_for_NA-HAProxy", agreementTerm.getGuaranteed().getState().getId());
		FuncExpr funcExpr = agreementTerm.getGuaranteed().getState().getConstraint().getTypeConstraintExpr().getValue().getFuncExpr();
		assertEquals(OVFToSLANames.AGGREGATED_METRIC_SLA_OPERATOR, funcExpr.getOperator());
		List<Parameter> parameters = funcExpr.getParameters();
		assertEquals(5, parameters.size());
		// Event Type
		Parameter parameter = parameters.get(0);
		assertEquals("searchForNewsItems", parameter.getcONST().getValue());
		assertEquals(OVFToSLANames.DATATYPE_STRING, parameter.getcONST().getDatatype());
		// Metric
		parameter = parameters.get(1);
		assertEquals("duration", parameter.getcONST().getValue());
		assertEquals(OVFToSLANames.DATATYPE_STRING, parameter.getcONST().getDatatype());
		// Period
		parameter = parameters.get(2);
		assertEquals("15", parameter.getcONST().getValue());
		assertEquals(OVFToSLANames.DATATYPE_INTEGER, parameter.getcONST().getDatatype());
		// Aggregated Function
		parameter = parameters.get(3);
		assertEquals("percentile", parameter.getcONST().getValue());
		assertEquals(OVFToSLANames.DATATYPE_STRING, parameter.getcONST().getDatatype());
		// Function Parameter
		parameter = parameters.get(4);
		assertEquals("90", parameter.getcONST().getValue());
		assertEquals(OVFToSLANames.DATATYPE_INTEGER, parameter.getcONST().getDatatype());
		// Comparison
		SimpleDomainExpr simpleDomainExpr = agreementTerm.getGuaranteed().getState().getConstraint().getTypeConstraintExpr().getDomain().getSimpleDomainExpr();
		assertEquals(OVFToSLANames.COMPARATORS.get("LTE"), simpleDomainExpr.getComparisonOp());
		assertEquals(OVFToSLANames.DATATYPE_DECIMAL, simpleDomainExpr.getValue().getConstVariable().getDatatype());
		assertEquals("0.7", simpleDomainExpr.getValue().getConstVariable().getValue());
		
		
		agreementTerm =  slat.getAgreemenTerms().get(6);
		assertEquals("Aggregated Guarantees", agreementTerm.getId());
		//assertEquals("violation_type", agreementTerm.getGuaranteed().getEntry().getKey());
		//assertEquals("information", agreementTerm.getGuaranteed().getEntry().getValue());
		assertEquals("anticipatedWorkload_for_NA-HAProxy", agreementTerm.getGuaranteed().getState().getId());
		funcExpr = agreementTerm.getGuaranteed().getState().getConstraint().getTypeConstraintExpr().getValue().getFuncExpr();
		assertEquals(OVFToSLANames.AGGREGATED_METRIC_SLA_OPERATOR, funcExpr.getOperator());
		parameters = funcExpr.getParameters();
		assertEquals(5, parameters.size());
		// Event Type
		parameter = parameters.get(0);
		assertEquals("anticipatedWorkload", parameter.getcONST().getValue());
		assertEquals(OVFToSLANames.DATATYPE_STRING, parameter.getcONST().getDatatype());
		// Metric
		parameter = parameters.get(1);
		assertEquals("degree", parameter.getcONST().getValue());
		assertEquals(OVFToSLANames.DATATYPE_STRING, parameter.getcONST().getDatatype());
		// Period
		parameter = parameters.get(2);
		assertEquals("NaN", parameter.getcONST().getValue());
		assertEquals(OVFToSLANames.DATATYPE_INTEGER, parameter.getcONST().getDatatype());
		// Aggregated Function
		parameter = parameters.get(3);
		assertEquals("last", parameter.getcONST().getValue());
		assertEquals(OVFToSLANames.DATATYPE_STRING, parameter.getcONST().getDatatype());
		// Function Parameter
		parameter = parameters.get(4);
		assertEquals("NaN", parameter.getcONST().getValue());
		assertEquals(OVFToSLANames.DATATYPE_INTEGER, parameter.getcONST().getDatatype());
		// Comparison
		simpleDomainExpr = agreementTerm.getGuaranteed().getState().getConstraint().getTypeConstraintExpr().getDomain().getSimpleDomainExpr();
		assertEquals(OVFToSLANames.COMPARATORS.get("LTE"), simpleDomainExpr.getComparisonOp());
		assertEquals(OVFToSLANames.DATATYPE_DECIMAL, simpleDomainExpr.getValue().getConstVariable().getDatatype());
		assertEquals("0.0", simpleDomainExpr.getValue().getConstVariable().getValue());
	}
	
	private void setupFakeProviderRegistry() {
		// We configure the mocked Provider Registry
		Configuration.providerRegistryEndpoint = mBaseURL + "/";

		String collection = "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>" +
				"<collection xmlns=\"http://provider-registry.ascetic.eu/doc/schemas/xml\" href=\"/\">" +
				"<items offset=\"0\" total=\"2\">" +
				"<provider href=\"/1\">" +
				"<id>1</id>" +
				"<name>default</name>" +
				"<vmm-url>http://iaas-vm-dev:34372/vmmanager</vmm-url>" +
				"<slam-url>http://10.0.9.149:8080/services/asceticNegotiation?wsdl</slam-url>" +
				"<amqp-url>amqp://guest:guest@iaas-vm-dev:5673</amqp-url>" +
				"<link rel=\"parent\" href=\"/\" type=\"application/xml\"/>" +
				"<link rel=\"self\" href=\"/1\" type=\"application/xml\"/>" +
				"</provider>" +
				"<provider href=\"/1\">" +
				"<id>2</id>" +
				"<name>default</name>" +
				"<vmm-url>http://iaas-vm-dev:34372/vmmanager</vmm-url>" +
				"<slam-url>http://10.0.10.149:8080/services/asceticNegotiation?wsdl</slam-url>" +
				"<amqp-url>amqp://guest:guest@iaas-vm-dev:5673</amqp-url>" +
				"<link rel=\"parent\" href=\"/\" type=\"application/xml\"/>" +
				"<link rel=\"self\" href=\"/1\" type=\"application/xml\"/>" +
				"</provider>" +
				"</items>" +
				"<link rel=\"self\" href=\"/\" type=\"application/xml\"/>" +
				"</collection>";

		mServer.addPath("/", collection);
	}
	
	@Test
	public void addPropertiesTest() {
		setupFakeProviderRegistry();
		
		// Test
		SLATemplate slaTemplate = new SLATemplate();
		
		SLATemplateCreator.addProperties(slaTemplate);
		
		STND[] propertiesKeys = slaTemplate.getPropertyKeys();
		assertEquals(1, propertiesKeys.length);
		assertEquals(value, slaTemplate.getPropertyValue(propertiesKeys[0])); 
	}
	
	@Test
	public void verifyPartyIsConfiguredCorrectly() throws Exception {
		SLATemplate slaTemplate = new SLATemplate();
		
		SLATemplateCreator.addProviderEndPointToTemplate(slaTemplate);
		
		assertEquals("AsceticProvider", slaTemplate.getParties()[0].getId().getValue());
		assertEquals("http://www.slaatsoi.org/slamodel#gslam_epr", slaTemplate.getParties()[0].getPropertyKeys()[0].getValue());
		assertEquals(Configuration.slamURL, slaTemplate.getParties()[0].getPropertyValue(slaTemplate.getParties()[0].getPropertyKeys()[0]));
		
		System.out.println(slaTemplate);
		
		// Check it does not overwrite all the parties:
		
		SLATemplateCreator.addProviderEndPointToTemplate(slaTemplate);
		assertEquals(2, slaTemplate.getParties().length);
	}
	
	@Test
	public void verifyCostumerIsConfiguredCorrectly() throws Exception {
		SLATemplate slaTemplate = new SLATemplate();
		
		SLATemplateCreator.addUserEndPointToTemplate(slaTemplate);
		//"ASCETiCUser"
		assertEquals("333", slaTemplate.getParties()[0].getId().getValue());
		assertEquals("http://www.slaatsoi.org/slamodel#gslam_epr", slaTemplate.getParties()[0].getPropertyKeys()[0].getValue());
		assertEquals(Configuration.slamURL, slaTemplate.getParties()[0].getPropertyValue(slaTemplate.getParties()[0].getPropertyKeys()[0]));
		
		System.out.println(slaTemplate);
		
		// Check it does not overwrite all the parties:
		SLATemplateCreator.addUserEndPointToTemplate(slaTemplate);
		assertEquals(2, slaTemplate.getParties().length);
	}
	
	@Test
	public void verifyAddInterfaceDclr() {
		SLATemplate slaTemplate = new SLATemplate();
		OvfDefinition ovfDefinition = OVFUtils.getOvfDefinition(threeTierWebAppOvfString);
		
		SLATemplateCreator.addInterfaceDclr(slaTemplate, ovfDefinition, "OVF_URL");
		
		// TODO after I know what I'm doing creating the template
		
		System.out.println(slaTemplate);
		
	}
	
	@Test
	public void verifyGenerateSLATemplate() throws Exception {
		OvfDefinition ovfDefinition = OVFUtils.getOvfDefinition(threeTierWebAppOvfString);

		SLATemplate slaTemplate = SLATemplateCreator.generateSLATemplate(ovfDefinition, "http://10.4.0.16/application-manager/applications/threeTierWebApp/deployments/31/ovf", null);

		assertEquals("ASCETiC-SLaTemplate-Example-01", slaTemplate.getUuid().getValue());
		//assertEquals("1", slaTemplate.getModelVersion());

		System.out.println(slaTemplate);
		System.out.println("##########################################################################################");
		System.out.println("##########################################################################################");
		System.out.println("##########################################################################################");

		SLASOITemplateRenderer slasoiTemplateRenderer = new SLASOITemplateRenderer();
		SLATemplateDocument slaTemplateRendered = SLATemplateDocument.Factory.parse(slasoiTemplateRenderer.renderSLATemplate(slaTemplate));

		System.out.println("SLA rendered as XML:");
		System.out.println(slaTemplateRendered.toString());
		
		
		ovfDefinition = OVFUtils.getOvfDefinition(aAfString);

		slaTemplate = SLATemplateCreator.generateSLATemplate(ovfDefinition, "http://192.168.3.222/application-manager/applications/newsAsset/deployments/490/ovf", null);

		assertEquals("ASCETiC-SLaTemplate-Example-01", slaTemplate.getUuid().getValue());
		//assertEquals("1", slaTemplate.getModelVersion());

		System.out.println(slaTemplate);
		System.out.println("##########################################################################################");
		System.out.println("##########################################################################################");
		System.out.println("##########################################################################################");

		slasoiTemplateRenderer = new SLASOITemplateRenderer();
		slaTemplateRendered = SLATemplateDocument.Factory.parse(slasoiTemplateRenderer.renderSLATemplate(slaTemplate));

		System.out.println("SLA rendered as XML:");
		System.out.println(slaTemplateRendered.toString());
		
		System.out.println("@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@");
		System.out.println("##########################################################################################");
		System.out.println("##########################################################################################");

	}
	
	
	/**
	 * It just reads a file form the disk... 
	 * @param path
	 * @param encoding
	 * @return
	 * @throws IOException
	 */
	protected String readFile(String path, Charset encoding) throws IOException {
		byte[] encoded = Files.readAllBytes(Paths.get(path));
		return new String(encoded, encoding);
	}
}
