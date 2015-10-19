package eu.ascetic.paas.applicationmanager.rest.util;

import static org.junit.Assert.assertEquals;

import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;

import javax.ws.rs.core.MediaType;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import javax.xml.transform.stream.StreamSource;

import org.eclipse.persistence.jaxb.UnmarshallerProperties;
import org.junit.Test;

import eu.ascetic.paas.applicationmanager.model.Agreement;
import eu.ascetic.paas.applicationmanager.model.Application;
import eu.ascetic.paas.applicationmanager.model.Collection;
import eu.ascetic.paas.applicationmanager.model.Cost;
import eu.ascetic.paas.applicationmanager.model.Deployment;
import eu.ascetic.paas.applicationmanager.model.EnergyMeasurement;
import eu.ascetic.paas.applicationmanager.model.EventSample;
import eu.ascetic.paas.applicationmanager.model.Image;
import eu.ascetic.paas.applicationmanager.model.PowerMeasurement;
import eu.ascetic.paas.applicationmanager.model.VM;
import eu.ascetic.paas.applicationmanager.model.converter.ModelConverter;

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

public class XMLBuilderTest {

	@Test
	public void addApplicationXMLInfoTest() {
		Application application = new Application();
		application.setId(1);
		application.setName("name");
		
		application = XMLBuilder.addApplicationXMLInfo(application);
		
		assertEquals(1, application.getId());
		assertEquals("/applications/name", application.getHref());
		assertEquals(4, application.getLinks().size());
		assertEquals("/applications", application.getLinks().get(0).getHref());
		assertEquals("parent", application.getLinks().get(0).getRel());
		assertEquals(MediaType.APPLICATION_XML, application.getLinks().get(0).getType());
		assertEquals("/applications/name", application.getLinks().get(1).getHref());
		assertEquals("self",application.getLinks().get(1).getRel());
		assertEquals(MediaType.APPLICATION_XML, application.getLinks().get(1).getType());
		assertEquals("/applications/name/deployments", application.getLinks().get(2).getHref());
		assertEquals("deployments",application.getLinks().get(2).getRel());
		assertEquals(MediaType.APPLICATION_XML, application.getLinks().get(2).getType());
	}
	
	@Test
	public void addEnergyMeasuremenForDeploymentXMLInfoTest() {
		EnergyMeasurement energyMeasurement = new EnergyMeasurement();
		energyMeasurement.setValue(22.0);
		
		energyMeasurement = XMLBuilder.addEnergyMeasurementForDeploymentXMLInfo(energyMeasurement, "111", "333", "energy-consumption");
		
		assertEquals("/applications/111/deployments/333/energy-consumption", energyMeasurement.getHref());
		assertEquals(22.0, energyMeasurement.getValue(), 0.00001);
		assertEquals("Aggregated energy consumption in Wh for this aplication deployment", energyMeasurement.getDescription());
		assertEquals(2, energyMeasurement.getLinks().size());
		assertEquals("/applications/111/deployments/333", energyMeasurement.getLinks().get(0).getHref());
		assertEquals("parent", energyMeasurement.getLinks().get(0).getRel());
		assertEquals(MediaType.APPLICATION_XML, energyMeasurement.getLinks().get(0).getType());
		assertEquals("/applications/111/deployments/333/energy-consumption", energyMeasurement.getLinks().get(1).getHref());
		assertEquals("self",energyMeasurement.getLinks().get(1).getRel());
		assertEquals(MediaType.APPLICATION_XML, energyMeasurement.getLinks().get(1).getType());
	}
	
	@Test
	public void getEnergyMeasuremenForDeploymentXMLInfoTest() throws Exception {
		EnergyMeasurement energyMeasurement = new EnergyMeasurement();
		energyMeasurement.setValue(22.0);
		
		String xml = XMLBuilder.getEnergyMeasurementForDeploymentXMLInfo(energyMeasurement, "111", "333");
		
		JAXBContext jaxbContext = JAXBContext.newInstance(EnergyMeasurement.class);
		Unmarshaller jaxbUnmarshaller = jaxbContext.createUnmarshaller();
		energyMeasurement = (EnergyMeasurement) jaxbUnmarshaller.unmarshal(new StringReader(xml));
		
		assertEquals("/applications/111/deployments/333/energy-consumption", energyMeasurement.getHref());
		assertEquals(22.0, energyMeasurement.getValue(), 0.00001);
		assertEquals("Aggregated energy consumption in Wh for this aplication deployment", energyMeasurement.getDescription());
		assertEquals(2, energyMeasurement.getLinks().size());
		assertEquals("/applications/111/deployments/333", energyMeasurement.getLinks().get(0).getHref());
		assertEquals("parent", energyMeasurement.getLinks().get(0).getRel());
		assertEquals(MediaType.APPLICATION_XML, energyMeasurement.getLinks().get(0).getType());
		assertEquals("/applications/111/deployments/333/energy-consumption", energyMeasurement.getLinks().get(1).getHref());
		assertEquals("self",energyMeasurement.getLinks().get(1).getRel());
		assertEquals(MediaType.APPLICATION_XML, energyMeasurement.getLinks().get(1).getType());
	}
	
	@Test
	public void addApplicationXMLInfoWithDeploymentsAndVMTest() throws JAXBException {
		Application applicationBeforeXML = new Application();
		applicationBeforeXML.setId(22);
		applicationBeforeXML.setName("name");
		
		Deployment deploymentBeforeXML = new Deployment();
		deploymentBeforeXML.setId(1);
		deploymentBeforeXML.setStatus("RUNNIG");
		deploymentBeforeXML.setPrice("expensive");
		applicationBeforeXML.addDeployment(deploymentBeforeXML);
		
		VM vmBeforeXML = new VM();
		vmBeforeXML.setId(44);
		vmBeforeXML.setIp("127.0.0.1");
		vmBeforeXML.setOvfId("ovf-id");
		vmBeforeXML.setProviderId("provider-id");
		vmBeforeXML.setProviderVmId("provider-vm-id");
		vmBeforeXML.setSlaAgreement("sla-agreement");
		vmBeforeXML.setStatus("RUNNING");
		deploymentBeforeXML.addVM(vmBeforeXML);
		
		String xml = XMLBuilder.getApplicationXML(applicationBeforeXML);
		
		JAXBContext jaxbContext = JAXBContext.newInstance(Application.class);
		Unmarshaller jaxbUnmarshaller = jaxbContext.createUnmarshaller();
		Application application = (Application) jaxbUnmarshaller.unmarshal(new StringReader(xml));
		
		assertEquals(22, application.getId());
		assertEquals("/applications/name", application.getHref());
		assertEquals(4, application.getLinks().size());
		assertEquals("/applications", application.getLinks().get(0).getHref());
		assertEquals("parent", application.getLinks().get(0).getRel());
		assertEquals(MediaType.APPLICATION_XML, application.getLinks().get(0).getType());
		assertEquals("/applications/name", application.getLinks().get(1).getHref());
		assertEquals("self",application.getLinks().get(1).getRel());
		assertEquals(MediaType.APPLICATION_XML, application.getLinks().get(1).getType());
		assertEquals("/applications/name/deployments", application.getLinks().get(2).getHref());
		assertEquals("deployments",application.getLinks().get(2).getRel());
		assertEquals(MediaType.APPLICATION_XML, application.getLinks().get(2).getType());
		
		Deployment deployment = application.getDeployments().get(0);
		assertEquals(1, deployment.getId());
		assertEquals("/applications/name/deployments/1", deployment.getHref());
		assertEquals(6, deployment.getLinks().size());
		assertEquals("/applications/name/deployments", deployment.getLinks().get(0).getHref());
		assertEquals("parent", deployment.getLinks().get(0).getRel());
		assertEquals(MediaType.APPLICATION_XML, deployment.getLinks().get(0).getType());
		assertEquals("/applications/name/deployments/1", deployment.getLinks().get(1).getHref());
		assertEquals("self",deployment.getLinks().get(1).getRel());
		assertEquals(MediaType.APPLICATION_XML, deployment.getLinks().get(1).getType());
		assertEquals("/applications/name/deployments/1/ovf", deployment.getLinks().get(2).getHref());
		assertEquals("ovf",deployment.getLinks().get(2).getRel());
		assertEquals(MediaType.APPLICATION_XML, deployment.getLinks().get(2).getType());
		assertEquals("/applications/name/deployments/1/vms", deployment.getLinks().get(3).getHref());
		assertEquals("vms",deployment.getLinks().get(3).getRel());
		assertEquals(MediaType.APPLICATION_XML, deployment.getLinks().get(3).getType());
		assertEquals("/applications/name/deployments/1/energy-consumption", deployment.getLinks().get(4).getHref());
		assertEquals("energy-consumption",deployment.getLinks().get(4).getRel());
		assertEquals(MediaType.APPLICATION_XML, deployment.getLinks().get(4).getType());
		assertEquals("/applications/name/deployments/1/agreements", deployment.getLinks().get(5).getHref());
		assertEquals("agreements",deployment.getLinks().get(5).getRel());
		assertEquals(MediaType.APPLICATION_XML, deployment.getLinks().get(5).getType());
		
		VM vm = deployment.getVms().get(0);
		assertEquals(44, vm.getId());
		assertEquals("/applications/name/deployments/1/vms/44", vm.getHref());
		assertEquals(2, vm.getLinks().size());
		assertEquals("/applications/name/deployments/1/vms", vm.getLinks().get(0).getHref());
		assertEquals("parent", vm.getLinks().get(0).getRel());
		assertEquals(MediaType.APPLICATION_XML, vm.getLinks().get(0).getType());
		assertEquals("/applications/name/deployments/1/vms/44", vm.getLinks().get(1).getHref());
		assertEquals("self",vm.getLinks().get(1).getRel());
		
		//We verify that we don't parse deployments if speficied... 
		applicationBeforeXML = new Application();
		applicationBeforeXML.setId(22);
		applicationBeforeXML.setName("name");
		
		deploymentBeforeXML = new Deployment();
		deploymentBeforeXML.setId(1);
		deploymentBeforeXML.setStatus("RUNNIG");
		deploymentBeforeXML.setPrice("expensive");
		applicationBeforeXML.addDeployment(deploymentBeforeXML);
		
		vmBeforeXML = new VM();
		vmBeforeXML.setId(44);
		vmBeforeXML.setIp("127.0.0.1");
		vmBeforeXML.setOvfId("ovf-id");
		vmBeforeXML.setProviderId("provider-id");
		vmBeforeXML.setProviderVmId("provider-vm-id");
		vmBeforeXML.setSlaAgreement("sla-agreement");
		vmBeforeXML.setStatus("RUNNING");
		deploymentBeforeXML.addVM(vmBeforeXML);
	}
	
	@Test
	public void getXMLApplicationTest() {
		Application applicationBeforeXML = new Application();
		applicationBeforeXML.setId(1);
		applicationBeforeXML.setName("name");
		String xml = XMLBuilder.getApplicationXML(applicationBeforeXML);
		
		Application application = ModelConverter.xmlApplicationToObject(xml);
		
		verifyGetApplication(application);
	}
	
	private void verifyGetApplication(Application application) {
		assertEquals(1, application.getId());
		assertEquals("/applications/name", application.getHref());
		assertEquals(4, application.getLinks().size());
		assertEquals("/applications", application.getLinks().get(0).getHref());
		assertEquals("parent", application.getLinks().get(0).getRel());
		assertEquals(MediaType.APPLICATION_XML, application.getLinks().get(0).getType());
		assertEquals("/applications/name", application.getLinks().get(1).getHref());
		assertEquals("self",application.getLinks().get(1).getRel());
		assertEquals(MediaType.APPLICATION_XML, application.getLinks().get(1).getType());
		assertEquals("/applications/name/deployments", application.getLinks().get(2).getHref());
		assertEquals("deployments",application.getLinks().get(2).getRel());
		assertEquals(MediaType.APPLICATION_XML, application.getLinks().get(2).getType());
		assertEquals("/applications/name/cache-images", application.getLinks().get(3).getHref());
		assertEquals("cache-image",application.getLinks().get(3).getRel());
		assertEquals(MediaType.APPLICATION_XML, application.getLinks().get(3).getType());
	}
	
	@Test
	public void getJSONLApplicationTest() {
		Application applicationBefore = new Application();
		applicationBefore.setId(1);
		applicationBefore.setName("name");
		String json = XMLBuilder.getApplicationJSON(applicationBefore);
		
		Application application = ModelConverter.jsonApplicationToObject(json);
		
		verifyGetApplication(application);
	}
	
	@Test
	public void addDeploymentXMLInfoTest() {
		Deployment deployment = new Deployment();
		deployment.setId(1);
		deployment.setStatus("RUNNIG");
		deployment.setPrice("expensive");
		
		deployment = XMLBuilder.addDeploymentXMLInfo(deployment, "22");
		
		assertEquals(1, deployment.getId());
		assertEquals("/applications/22/deployments/1", deployment.getHref());
		assertEquals(6, deployment.getLinks().size());
		assertEquals("/applications/22/deployments", deployment.getLinks().get(0).getHref());
		assertEquals("parent", deployment.getLinks().get(0).getRel());
		assertEquals(MediaType.APPLICATION_XML, deployment.getLinks().get(0).getType());
		assertEquals("/applications/22/deployments/1", deployment.getLinks().get(1).getHref());
		assertEquals("self",deployment.getLinks().get(1).getRel());
		assertEquals(MediaType.APPLICATION_XML, deployment.getLinks().get(1).getType());
		assertEquals("/applications/22/deployments/1/ovf", deployment.getLinks().get(2).getHref());
		assertEquals("ovf",deployment.getLinks().get(2).getRel());
		assertEquals(MediaType.APPLICATION_XML, deployment.getLinks().get(2).getType());
		assertEquals("/applications/22/deployments/1/vms", deployment.getLinks().get(3).getHref());
		assertEquals("vms",deployment.getLinks().get(3).getRel());
		assertEquals(MediaType.APPLICATION_XML, deployment.getLinks().get(3).getType());
		assertEquals("/applications/22/deployments/1/energy-consumption", deployment.getLinks().get(4).getHref());
		assertEquals("energy-consumption",deployment.getLinks().get(4).getRel());
		assertEquals(MediaType.APPLICATION_XML, deployment.getLinks().get(4).getType());
		assertEquals("/applications/22/deployments/1/agreements", deployment.getLinks().get(5).getHref());
		assertEquals("agreements",deployment.getLinks().get(5).getRel());
		assertEquals(MediaType.APPLICATION_XML, deployment.getLinks().get(5).getType());
	}
	
	@Test
	public void getXMLDeploymentTest() throws JAXBException {
		Deployment deploymentBeforeXML = new Deployment();
		deploymentBeforeXML.setId(1);
		deploymentBeforeXML.setStatus("RUNNIG");
		deploymentBeforeXML.setPrice("expensive");
		
		String xml = XMLBuilder.getDeploymentXML(deploymentBeforeXML, "22");
		
		JAXBContext jaxbContext = JAXBContext.newInstance(Deployment.class);
		Unmarshaller jaxbUnmarshaller = jaxbContext.createUnmarshaller();
		Deployment deployment = (Deployment) jaxbUnmarshaller.unmarshal(new StringReader(xml));
		
		assertEquals(1, deployment.getId());
		assertEquals("/applications/22/deployments/1", deployment.getHref());
		assertEquals(6, deployment.getLinks().size());
		assertEquals("/applications/22/deployments", deployment.getLinks().get(0).getHref());
		assertEquals("parent", deployment.getLinks().get(0).getRel());
		assertEquals(MediaType.APPLICATION_XML, deployment.getLinks().get(0).getType());
		assertEquals("/applications/22/deployments/1", deployment.getLinks().get(1).getHref());
		assertEquals("self",deployment.getLinks().get(1).getRel());
		assertEquals(MediaType.APPLICATION_XML, deployment.getLinks().get(1).getType());
		assertEquals("/applications/22/deployments/1/ovf", deployment.getLinks().get(2).getHref());
		assertEquals("ovf",deployment.getLinks().get(2).getRel());
		assertEquals(MediaType.APPLICATION_XML, deployment.getLinks().get(2).getType());
		assertEquals("/applications/22/deployments/1/vms", deployment.getLinks().get(3).getHref());
		assertEquals("vms",deployment.getLinks().get(3).getRel());
		assertEquals(MediaType.APPLICATION_XML, deployment.getLinks().get(3).getType());
		assertEquals("/applications/22/deployments/1/energy-consumption", deployment.getLinks().get(4).getHref());
		assertEquals("energy-consumption",deployment.getLinks().get(4).getRel());
		assertEquals(MediaType.APPLICATION_XML, deployment.getLinks().get(4).getType());
		assertEquals("/applications/22/deployments/1/agreements", deployment.getLinks().get(5).getHref());
		assertEquals("agreements",deployment.getLinks().get(5).getRel());
		assertEquals(MediaType.APPLICATION_XML, deployment.getLinks().get(5).getType());
	}
	
	@Test
	public void getJSONDeploymentTest() throws JAXBException {
		Deployment deploymentBeforeXML = new Deployment();
		deploymentBeforeXML.setId(1);
		deploymentBeforeXML.setStatus("RUNNIG");
		deploymentBeforeXML.setPrice("expensive");
		
		String json = XMLBuilder.getDeploymentJSON(deploymentBeforeXML, "22");
		
		Deployment deployment = ModelConverter.jsonDeploymentToObject(json);
		
		assertEquals(1, deployment.getId());
		assertEquals("/applications/22/deployments/1", deployment.getHref());
		assertEquals(6, deployment.getLinks().size());
		assertEquals("/applications/22/deployments", deployment.getLinks().get(0).getHref());
		assertEquals("parent", deployment.getLinks().get(0).getRel());
		assertEquals(MediaType.APPLICATION_XML, deployment.getLinks().get(0).getType());
		assertEquals("/applications/22/deployments/1", deployment.getLinks().get(1).getHref());
		assertEquals("self",deployment.getLinks().get(1).getRel());
		assertEquals(MediaType.APPLICATION_XML, deployment.getLinks().get(1).getType());
		assertEquals("/applications/22/deployments/1/ovf", deployment.getLinks().get(2).getHref());
		assertEquals("ovf",deployment.getLinks().get(2).getRel());
		assertEquals(MediaType.APPLICATION_XML, deployment.getLinks().get(2).getType());
		assertEquals("/applications/22/deployments/1/vms", deployment.getLinks().get(3).getHref());
		assertEquals("vms",deployment.getLinks().get(3).getRel());
		assertEquals(MediaType.APPLICATION_XML, deployment.getLinks().get(3).getType());
		assertEquals("/applications/22/deployments/1/energy-consumption", deployment.getLinks().get(4).getHref());
		assertEquals("energy-consumption",deployment.getLinks().get(4).getRel());
		assertEquals(MediaType.APPLICATION_XML, deployment.getLinks().get(4).getType());
		assertEquals("/applications/22/deployments/1/agreements", deployment.getLinks().get(5).getHref());
		assertEquals("agreements",deployment.getLinks().get(5).getRel());
		assertEquals(MediaType.APPLICATION_XML, deployment.getLinks().get(5).getType());
	}
	
	@Test
	public void addVMXMLInfoTest() {
		VM vm = new VM();
		vm.setId(44);
		vm.setIp("127.0.0.1");
		vm.setOvfId("ovf-id");
		vm.setProviderId("provider-id");
		vm.setProviderVmId("provider-vm-id");
		vm.setSlaAgreement("sla-agreement");
		vm.setStatus("RUNNING");
		
		vm = XMLBuilder.addVMXMLInfo(vm, "22", 33);
		
		assertEquals(44, vm.getId());
		assertEquals("/applications/22/deployments/33/vms/44", vm.getHref());
		assertEquals(2, vm.getLinks().size());
		assertEquals("/applications/22/deployments/33/vms", vm.getLinks().get(0).getHref());
		assertEquals("parent", vm.getLinks().get(0).getRel());
		assertEquals(MediaType.APPLICATION_XML, vm.getLinks().get(0).getType());
		assertEquals("/applications/22/deployments/33/vms/44", vm.getLinks().get(1).getHref());
		assertEquals("self",vm.getLinks().get(1).getRel());
	}
	
	@Test
	public void getXMLVMTest() throws JAXBException {
		VM vmBeforeXML = new VM();
		vmBeforeXML.setId(44);
		vmBeforeXML.setIp("127.0.0.1");
		vmBeforeXML.setOvfId("ovf-id");
		vmBeforeXML.setProviderId("provider-id");
		vmBeforeXML.setProviderVmId("provider-vm-id");
		vmBeforeXML.setSlaAgreement("sla-agreement");
		vmBeforeXML.setStatus("RUNNING");
		
		String xml = XMLBuilder.getVMXML(vmBeforeXML, "22", 33);
		
		JAXBContext jaxbContext = JAXBContext.newInstance(VM.class);
		Unmarshaller jaxbUnmarshaller = jaxbContext.createUnmarshaller();
		VM vm = (VM) jaxbUnmarshaller.unmarshal(new StringReader(xml));
		
		assertEquals(44, vm.getId());
		assertEquals("/applications/22/deployments/33/vms/44", vm.getHref());
		assertEquals(2, vm.getLinks().size());
		assertEquals("/applications/22/deployments/33/vms", vm.getLinks().get(0).getHref());
		assertEquals("parent", vm.getLinks().get(0).getRel());
		assertEquals(MediaType.APPLICATION_XML, vm.getLinks().get(0).getType());
		assertEquals("/applications/22/deployments/33/vms/44", vm.getLinks().get(1).getHref());
		assertEquals("self",vm.getLinks().get(1).getRel());
	}
	
	@Test
	public void getCollectionApplicationsXMLTest() throws JAXBException  {
		Application application1 = new Application();
		application1.setId(1);
		application1.setName("name-1");

		Application application2 = new Application();
		application2.setId(2);
		application2.setName("name-2");
		
		List<Application> applications = new ArrayList<Application>();
		applications.add(application1);
		applications.add(application2);
		
		String xml = XMLBuilder.getCollectionApplicationsXML(applications);
		
		JAXBContext jaxbContext = JAXBContext.newInstance(Collection.class);
		Unmarshaller jaxbUnmarshaller = jaxbContext.createUnmarshaller();
		Collection collection = (Collection) jaxbUnmarshaller.unmarshal(new StringReader(xml));
		
		assertEquals("/applications", collection.getHref());
		assertEquals(0, collection.getItems().getOffset());
		assertEquals(2, collection.getItems().getTotal());
		assertEquals("/applications/name-1", collection.getItems().getApplications().get(0).getHref());
		assertEquals(4, collection.getItems().getApplications().get(0).getLinks().size());
		assertEquals("/applications/name-2", collection.getItems().getApplications().get(1).getHref());
		assertEquals(4, collection.getItems().getApplications().get(1).getLinks().size());
		assertEquals(2, collection.getLinks().size());
		assertEquals("/", collection.getLinks().get(0).getHref());
		assertEquals("parent", collection.getLinks().get(0).getRel());
		assertEquals(MediaType.APPLICATION_XML, collection.getLinks().get(0).getType());
		assertEquals("/applications", collection.getLinks().get(1).getHref());
		assertEquals("self",collection.getLinks().get(1).getRel());
	}
	
	@Test
	public void getCollectionApplicationsJSONTest() throws JAXBException  {
		Application application1 = new Application();
		application1.setId(1);
		application1.setName("name-1");

		Application application2 = new Application();
		application2.setId(2);
		application2.setName("name-2");
		
		List<Application> applications = new ArrayList<Application>();
		applications.add(application1);
		applications.add(application2);
		
		String json = XMLBuilder.getCollectionApplicationsJSON(applications);
		
        // Create a JaxBContext
		JAXBContext jc = org.eclipse.persistence.jaxb.JAXBContextFactory.createContext(new Class[] {Collection.class}, null);
        
        // Create the Unmarshaller Object using the JaxB Context
        Unmarshaller unmarshaller = jc.createUnmarshaller();
        unmarshaller.setProperty(UnmarshallerProperties.MEDIA_TYPE, "application/json");
        unmarshaller.setProperty(UnmarshallerProperties.JSON_INCLUDE_ROOT, false);
        
        StreamSource jsonSource = new StreamSource(new StringReader(json));  
        Collection collection = unmarshaller.unmarshal(jsonSource, Collection.class).getValue();
		
		assertEquals("/applications", collection.getHref());
		assertEquals(0, collection.getItems().getOffset());
		assertEquals(2, collection.getItems().getTotal());
		assertEquals("/applications/name-1", collection.getItems().getApplications().get(0).getHref());
		assertEquals(4, collection.getItems().getApplications().get(0).getLinks().size());
		assertEquals("/applications/name-2", collection.getItems().getApplications().get(1).getHref());
		assertEquals(4, collection.getItems().getApplications().get(1).getLinks().size());
		assertEquals(2, collection.getLinks().size());
		assertEquals("/", collection.getLinks().get(0).getHref());
		assertEquals("parent", collection.getLinks().get(0).getRel());
		assertEquals(MediaType.APPLICATION_XML, collection.getLinks().get(0).getType());
		assertEquals("/applications", collection.getLinks().get(1).getHref());
		assertEquals("self",collection.getLinks().get(1).getRel());
	}
	
	@Test
	public void getCollectionNullCollectionOfApplicationsXMLTest() throws JAXBException {	
		String xml = XMLBuilder.getCollectionApplicationsXML(null);
		
		JAXBContext jaxbContext = JAXBContext.newInstance(Collection.class);
		Unmarshaller jaxbUnmarshaller = jaxbContext.createUnmarshaller();
		Collection collection = (Collection) jaxbUnmarshaller.unmarshal(new StringReader(xml));
		
		assertEquals("/applications", collection.getHref());
		assertEquals(0, collection.getItems().getOffset());
		assertEquals(0, collection.getItems().getTotal());
		assertEquals(null, collection.getItems().getApplications());
	}
	
	@Test
	public void getCollectionOfCacheImagesTest() throws JAXBException {
		Image image1 = new Image();
		image1.setDemo(true);
		image1.setId(1);
		image1.setOvfHref("ovf-href-1");
		image1.setOvfId("ovf-id1");
		image1.setProviderImageId("uuid1");
		Image image2 = new Image();
		image2.setDemo(true);
		image2.setId(2);
		image2.setOvfHref("ovf-href-2");
		image2.setOvfId("ovf-id2");
		image2.setProviderImageId("uuid2");
		List<Image> images = new ArrayList<Image>();
		images.add(image1);
		images.add(image2);
		
		String xml = XMLBuilder.getCollectionOfCacheImagesXML(images, "name");
		
		JAXBContext jaxbContext = JAXBContext.newInstance(Collection.class);
		Unmarshaller jaxbUnmarshaller = jaxbContext.createUnmarshaller();
		Collection collection = (Collection) jaxbUnmarshaller.unmarshal(new StringReader(xml));
		
		assertEquals("/applications/name/cache-images", collection.getHref());
		assertEquals(0, collection.getItems().getOffset());
		assertEquals(2, collection.getItems().getTotal());
		// Links
		assertEquals(2, collection.getLinks().size());
		assertEquals("/applications/name", collection.getLinks().get(0).getHref());
		assertEquals("parent", collection.getLinks().get(0).getRel());
		assertEquals(MediaType.APPLICATION_XML, collection.getLinks().get(0).getType());
		assertEquals("/applications/name/cache-images", collection.getLinks().get(1).getHref());
		assertEquals("self", collection.getLinks().get(1).getRel());
		assertEquals(MediaType.APPLICATION_XML, collection.getLinks().get(1).getType());
		// Images
		assertEquals(2, collection.getItems().getImages().size());
		//     Image 1
		image1 = collection.getItems().getImages().get(0);
		assertEquals("/applications/name/cache-images/1", image1.getHref());
		assertEquals(1, image1.getId());
		assertEquals("ovf-href-1", image1.getOvfHref());
		assertEquals("ovf-id1", image1.getOvfId());
		assertEquals("uuid1", image1.getProviderImageId());
		//     Image 2
		image2 = collection.getItems().getImages().get(1);
		assertEquals("/applications/name/cache-images/2", image2.getHref());
		assertEquals(2, image2.getId());
		assertEquals("ovf-href-2", image2.getOvfHref());
		assertEquals("ovf-id2", image2.getOvfId());
		assertEquals("uuid2", image2.getProviderImageId());
	}
	
	@Test
	public void getCollectionOfDeploymentsXMLTest() throws JAXBException {
		
		Deployment deployment1 = new Deployment();
		deployment1.setId(1);
		deployment1.setOvf("ovf1");
		deployment1.setPrice("price1");
		deployment1.setStatus("Status1");
		
		Deployment deployment2 = new Deployment();
		deployment2.setId(2);
		deployment2.setOvf("ovf2");
		deployment2.setPrice("price2");
		deployment2.setStatus("Status2");
		
		List<Deployment> deployments = new ArrayList<Deployment>();
		deployments.add(deployment1);
		deployments.add(deployment2);
		
		String xml = XMLBuilder.getCollectionOfDeploymentsXML(deployments, "1");
		
		JAXBContext jaxbContext = JAXBContext.newInstance(Collection.class);
		Unmarshaller jaxbUnmarshaller = jaxbContext.createUnmarshaller();
		Collection collection = (Collection) jaxbUnmarshaller.unmarshal(new StringReader(xml));
		
		assertEquals("/applications/1/deployments", collection.getHref());
		assertEquals(0, collection.getItems().getOffset());
		assertEquals(2, collection.getItems().getTotal());
		//Links
		assertEquals(2, collection.getLinks().size());
		assertEquals("/applications/1", collection.getLinks().get(0).getHref());
		assertEquals("parent", collection.getLinks().get(0).getRel());
		assertEquals(MediaType.APPLICATION_XML, collection.getLinks().get(0).getType());
		assertEquals("/applications/1/deployments", collection.getLinks().get(1).getHref());
		assertEquals("self", collection.getLinks().get(1).getRel());
		assertEquals(MediaType.APPLICATION_XML, collection.getLinks().get(1).getType());
		// Deployments
		assertEquals(2, collection.getItems().getDeployments().size());
		//     Deployment 1
		deployment1 = collection.getItems().getDeployments().get(0);
		assertEquals("/applications/1/deployments/1", deployment1.getLinks().get(1).getHref());
		assertEquals("self", deployment1.getLinks().get(1).getRel());
		assertEquals(MediaType.APPLICATION_XML, deployment1.getLinks().get(1).getType());
		assertEquals("/applications/1/deployments", deployment1.getLinks().get(0).getHref());
		assertEquals("parent", deployment1.getLinks().get(0).getRel());
		assertEquals(MediaType.APPLICATION_XML, deployment1.getLinks().get(0).getType());
		assertEquals("/applications/1/deployments/1/ovf", deployment1.getLinks().get(2).getHref());
		assertEquals("ovf", deployment1.getLinks().get(2).getRel());
		assertEquals(MediaType.APPLICATION_XML, deployment1.getLinks().get(2).getType());
		assertEquals("/applications/1/deployments/1/vms", deployment1.getLinks().get(3).getHref());
		assertEquals("vms", deployment1.getLinks().get(3).getRel());
		assertEquals(MediaType.APPLICATION_XML, deployment1.getLinks().get(3).getType());
		assertEquals("ovf1", deployment1.getOvf());
		assertEquals("price1", deployment1.getPrice());
		assertEquals("Status1", deployment1.getStatus());
		//      Deployment 2
		deployment2 = collection.getItems().getDeployments().get(1);
		assertEquals("/applications/1/deployments/2", deployment2.getLinks().get(1).getHref());
		assertEquals("self", deployment2.getLinks().get(1).getRel());
		assertEquals(MediaType.APPLICATION_XML, deployment1.getLinks().get(1).getType());
		assertEquals("/applications/1/deployments", deployment2.getLinks().get(0).getHref());
		assertEquals("parent", deployment1.getLinks().get(0).getRel());
		assertEquals(MediaType.APPLICATION_XML, deployment2.getLinks().get(0).getType());
		assertEquals("/applications/1/deployments/2/ovf", deployment2.getLinks().get(2).getHref());
		assertEquals("ovf", deployment2.getLinks().get(2).getRel());
		assertEquals(MediaType.APPLICATION_XML, deployment2.getLinks().get(2).getType());
		assertEquals("/applications/1/deployments/2/vms", deployment2.getLinks().get(3).getHref());
		assertEquals("vms", deployment2.getLinks().get(3).getRel());
		assertEquals(MediaType.APPLICATION_XML, deployment1.getLinks().get(3).getType());
		assertEquals("ovf2", deployment2.getOvf());
		assertEquals("price2", deployment2.getPrice());
		assertEquals("Status2", deployment2.getStatus());
	}
	
	@Test
	public void getCollectionOfDeploymentsJSONTest() throws JAXBException {
		
		Deployment deployment1 = new Deployment();
		deployment1.setId(1);
		deployment1.setOvf("ovf1");
		deployment1.setPrice("price1");
		deployment1.setStatus("Status1");
		
		Deployment deployment2 = new Deployment();
		deployment2.setId(2);
		deployment2.setOvf("ovf2");
		deployment2.setPrice("price2");
		deployment2.setStatus("Status2");
		
		List<Deployment> deployments = new ArrayList<Deployment>();
		deployments.add(deployment1);
		deployments.add(deployment2);
		
		String json = XMLBuilder.getCollectionOfDeploymentsJSON(deployments, "1");
		
		Collection collection = ModelConverter.jsonCollectionToObject(json);

		assertEquals("/applications/1/deployments", collection.getHref());
		assertEquals(0, collection.getItems().getOffset());
		assertEquals(2, collection.getItems().getTotal());
		//Links
		assertEquals(2, collection.getLinks().size());
		assertEquals("/applications/1", collection.getLinks().get(0).getHref());
		assertEquals("parent", collection.getLinks().get(0).getRel());
		assertEquals(MediaType.APPLICATION_XML, collection.getLinks().get(0).getType());
		assertEquals("/applications/1/deployments", collection.getLinks().get(1).getHref());
		assertEquals("self", collection.getLinks().get(1).getRel());
		assertEquals(MediaType.APPLICATION_XML, collection.getLinks().get(1).getType());
		// Deployments
		assertEquals(2, collection.getItems().getDeployments().size());
		//     Deployment 1
		deployment1 = collection.getItems().getDeployments().get(0);
		assertEquals("/applications/1/deployments/1", deployment1.getLinks().get(1).getHref());
		assertEquals("self", deployment1.getLinks().get(1).getRel());
		assertEquals(MediaType.APPLICATION_XML, deployment1.getLinks().get(1).getType());
		assertEquals("/applications/1/deployments", deployment1.getLinks().get(0).getHref());
		assertEquals("parent", deployment1.getLinks().get(0).getRel());
		assertEquals(MediaType.APPLICATION_XML, deployment1.getLinks().get(0).getType());
		assertEquals("/applications/1/deployments/1/ovf", deployment1.getLinks().get(2).getHref());
		assertEquals("ovf", deployment1.getLinks().get(2).getRel());
		assertEquals(MediaType.APPLICATION_XML, deployment1.getLinks().get(2).getType());
		assertEquals("/applications/1/deployments/1/vms", deployment1.getLinks().get(3).getHref());
		assertEquals("vms", deployment1.getLinks().get(3).getRel());
		assertEquals(MediaType.APPLICATION_XML, deployment1.getLinks().get(3).getType());
		assertEquals("ovf1", deployment1.getOvf());
		assertEquals("price1", deployment1.getPrice());
		assertEquals("Status1", deployment1.getStatus());
		//      Deployment 2
		deployment2 = collection.getItems().getDeployments().get(1);
		assertEquals("/applications/1/deployments/2", deployment2.getLinks().get(1).getHref());
		assertEquals("self", deployment2.getLinks().get(1).getRel());
		assertEquals(MediaType.APPLICATION_XML, deployment1.getLinks().get(1).getType());
		assertEquals("/applications/1/deployments", deployment2.getLinks().get(0).getHref());
		assertEquals("parent", deployment1.getLinks().get(0).getRel());
		assertEquals(MediaType.APPLICATION_XML, deployment2.getLinks().get(0).getType());
		assertEquals("/applications/1/deployments/2/ovf", deployment2.getLinks().get(2).getHref());
		assertEquals("ovf", deployment2.getLinks().get(2).getRel());
		assertEquals(MediaType.APPLICATION_XML, deployment2.getLinks().get(2).getType());
		assertEquals("/applications/1/deployments/2/vms", deployment2.getLinks().get(3).getHref());
		assertEquals("vms", deployment2.getLinks().get(3).getRel());
		assertEquals(MediaType.APPLICATION_XML, deployment1.getLinks().get(3).getType());
		assertEquals("ovf2", deployment2.getOvf());
		assertEquals("price2", deployment2.getPrice());
		assertEquals("Status2", deployment2.getStatus());
	}
	
	@Test
	public void getCollectionNullCollectionOfDeploymentsXMLTest() throws JAXBException {	
		String xml = XMLBuilder.getCollectionOfDeploymentsXML(null, "1");
		
		JAXBContext jaxbContext = JAXBContext.newInstance(Collection.class);
		Unmarshaller jaxbUnmarshaller = jaxbContext.createUnmarshaller();
		Collection collection = (Collection) jaxbUnmarshaller.unmarshal(new StringReader(xml));
		
		assertEquals("/applications/1/deployments", collection.getHref());
		assertEquals(0, collection.getItems().getOffset());
		assertEquals(0, collection.getItems().getTotal());
		assertEquals(null, collection.getItems().getDeployments());
		//Links
		assertEquals(2, collection.getLinks().size());
		assertEquals("/applications/1", collection.getLinks().get(0).getHref());
		assertEquals("parent", collection.getLinks().get(0).getRel());
		assertEquals(MediaType.APPLICATION_XML, collection.getLinks().get(0).getType());
		assertEquals("/applications/1/deployments", collection.getLinks().get(1).getHref());
		assertEquals("self", collection.getLinks().get(1).getRel());
		assertEquals(MediaType.APPLICATION_XML, collection.getLinks().get(1).getType());
	}
	
	@Test
	public void addEnergyEstimationForDeploymentXMLInfoTest() {
		EnergyMeasurement energyMeasurement = new EnergyMeasurement();
		energyMeasurement.setValue(22.0);
		
		energyMeasurement = XMLBuilder.addEnergyEstimationForDeploymentXMLInfo(energyMeasurement, "111", "333","eventX");
		
		assertEquals("/applications/111/deployments/333/events/eventX/energy-estimation", energyMeasurement.getHref());
		assertEquals(22.0, energyMeasurement.getValue(), 0.00001);
		assertEquals("Aggregated energy estimation for this aplication deployment and specific event", energyMeasurement.getDescription());
		assertEquals(2, energyMeasurement.getLinks().size());
		assertEquals("/applications/111/deployments/333/events/eventX", energyMeasurement.getLinks().get(0).getHref());
		assertEquals("parent", energyMeasurement.getLinks().get(0).getRel());
		assertEquals(MediaType.APPLICATION_XML, energyMeasurement.getLinks().get(0).getType());
		assertEquals("/applications/111/deployments/333/events/eventX/energy-estimation", energyMeasurement.getLinks().get(1).getHref());
		assertEquals("self",energyMeasurement.getLinks().get(1).getRel());
		assertEquals(MediaType.APPLICATION_XML, energyMeasurement.getLinks().get(1).getType());
	}
	
	@Test
	public void addPowerEstimationForDeploymentXMLInfoTest() {
		PowerMeasurement powerMeasurement = new PowerMeasurement();
		powerMeasurement.setValue(22.0);
		
		powerMeasurement = XMLBuilder.addPowerEstimationForDeploymentXMLInfo(powerMeasurement, "111", "333","eventX");
		
		assertEquals("/applications/111/deployments/333/events/eventX/power-estimation", powerMeasurement.getHref());
		assertEquals(22.0, powerMeasurement.getValue(), 0.00001);
		assertEquals("Aggregated power estimation for this aplication deployment and specific event", powerMeasurement.getDescription());
		assertEquals(2, powerMeasurement.getLinks().size());
		assertEquals("/applications/111/deployments/333/events/eventX", powerMeasurement.getLinks().get(0).getHref());
		assertEquals("parent", powerMeasurement.getLinks().get(0).getRel());
		assertEquals(MediaType.APPLICATION_XML, powerMeasurement.getLinks().get(0).getType());
		assertEquals("/applications/111/deployments/333/events/eventX/power-estimation", powerMeasurement.getLinks().get(1).getHref());
		assertEquals("self",powerMeasurement.getLinks().get(1).getRel());
		assertEquals(MediaType.APPLICATION_XML, powerMeasurement.getLinks().get(1).getType());
	}
	
	@Test
	public void addPowerConsumptionForDeploymentXMLInfoTest() {
		PowerMeasurement powerMeasurement = new PowerMeasurement();
		powerMeasurement.setValue(22.0);
		
		powerMeasurement = XMLBuilder.addPowerConsumptionForDeploymentXMLInfo(powerMeasurement, "111", "333","eventX");
		
		assertEquals("/applications/111/deployments/333/events/eventX/power-consumption", powerMeasurement.getHref());
		assertEquals(22.0, powerMeasurement.getValue(), 0.00001);
		assertEquals("Aggregated power consumption for this aplication deployment and specific event", powerMeasurement.getDescription());
		assertEquals(2, powerMeasurement.getLinks().size());
		assertEquals("/applications/111/deployments/333/events/eventX", powerMeasurement.getLinks().get(0).getHref());
		assertEquals("parent", powerMeasurement.getLinks().get(0).getRel());
		assertEquals(MediaType.APPLICATION_XML, powerMeasurement.getLinks().get(0).getType());
		assertEquals("/applications/111/deployments/333/events/eventX/power-consumption", powerMeasurement.getLinks().get(1).getHref());
		assertEquals("self",powerMeasurement.getLinks().get(1).getRel());
		assertEquals(MediaType.APPLICATION_XML, powerMeasurement.getLinks().get(1).getType());
	}
	
	@Test
	public void getEnergyEstimationForDeploymentXMLInfoTest() throws Exception {
		EnergyMeasurement energyMeasurement = new EnergyMeasurement();
		energyMeasurement.setValue(22.0);
		
		String xml = XMLBuilder.getEnergyEstimationForDeploymentXMLInfo(energyMeasurement, "111", "333", "eventX");
		
		JAXBContext jaxbContext = JAXBContext.newInstance(EnergyMeasurement.class);
		Unmarshaller jaxbUnmarshaller = jaxbContext.createUnmarshaller();
		energyMeasurement = (EnergyMeasurement) jaxbUnmarshaller.unmarshal(new StringReader(xml));
		
		assertEquals("/applications/111/deployments/333/events/eventX/energy-estimation", energyMeasurement.getHref());
		assertEquals(22.0, energyMeasurement.getValue(), 0.00001);
		assertEquals("Aggregated energy estimation for this aplication deployment and specific event", energyMeasurement.getDescription());
		assertEquals(2, energyMeasurement.getLinks().size());
		assertEquals("/applications/111/deployments/333/events/eventX", energyMeasurement.getLinks().get(0).getHref());
		assertEquals("parent", energyMeasurement.getLinks().get(0).getRel());
		assertEquals(MediaType.APPLICATION_XML, energyMeasurement.getLinks().get(0).getType());
		assertEquals("/applications/111/deployments/333/events/eventX/energy-estimation", energyMeasurement.getLinks().get(1).getHref());
		assertEquals("self",energyMeasurement.getLinks().get(1).getRel());
		assertEquals(MediaType.APPLICATION_XML, energyMeasurement.getLinks().get(1).getType());
	}
	
	@Test
	public void getEnergyComsumptionForDeploymentXMLInfoTest() throws Exception {
		EnergyMeasurement energyMeasurement = new EnergyMeasurement();
		energyMeasurement.setValue(22.0);
		
		String xml = XMLBuilder.getEnergyConsumptionForDeploymentXMLInfo(energyMeasurement, "111", "333", "eventX");
		
		JAXBContext jaxbContext = JAXBContext.newInstance(EnergyMeasurement.class);
		Unmarshaller jaxbUnmarshaller = jaxbContext.createUnmarshaller();
		energyMeasurement = (EnergyMeasurement) jaxbUnmarshaller.unmarshal(new StringReader(xml));
		
		assertEquals("/applications/111/deployments/333/events/eventX/energy-consumption", energyMeasurement.getHref());
		assertEquals(22.0, energyMeasurement.getValue(), 0.00001);
		assertEquals("Aggregated energy consumption for this aplication deployment and specific event", energyMeasurement.getDescription());
		assertEquals(2, energyMeasurement.getLinks().size());
		assertEquals("/applications/111/deployments/333/events/eventX", energyMeasurement.getLinks().get(0).getHref());
		assertEquals("parent", energyMeasurement.getLinks().get(0).getRel());
		assertEquals(MediaType.APPLICATION_XML, energyMeasurement.getLinks().get(0).getType());
		assertEquals("/applications/111/deployments/333/events/eventX/energy-consumption", energyMeasurement.getLinks().get(1).getHref());
		assertEquals("self",energyMeasurement.getLinks().get(1).getRel());
		assertEquals(MediaType.APPLICATION_XML, energyMeasurement.getLinks().get(1).getType());
	}
	
	@Test
	public void getPowerEstimationForDeploymentXMLInfoTest() throws Exception {
		PowerMeasurement powerMeasurement = new PowerMeasurement();
		powerMeasurement.setValue(22.0);
		
		String xml = XMLBuilder.getPowerEstimationForDeploymentXMLInfo(powerMeasurement, "111", "333", "eventX");
		
		JAXBContext jaxbContext = JAXBContext.newInstance(PowerMeasurement.class);
		Unmarshaller jaxbUnmarshaller = jaxbContext.createUnmarshaller();
		powerMeasurement = (PowerMeasurement) jaxbUnmarshaller.unmarshal(new StringReader(xml));
		
		assertEquals("/applications/111/deployments/333/events/eventX/power-estimation", powerMeasurement.getHref());
		assertEquals(22.0, powerMeasurement.getValue(), 0.00001);
		assertEquals("Aggregated power estimation for this aplication deployment and specific event", powerMeasurement.getDescription());
		assertEquals(2, powerMeasurement.getLinks().size());
		assertEquals("/applications/111/deployments/333/events/eventX", powerMeasurement.getLinks().get(0).getHref());
		assertEquals("parent", powerMeasurement.getLinks().get(0).getRel());
		assertEquals(MediaType.APPLICATION_XML, powerMeasurement.getLinks().get(0).getType());
		assertEquals("/applications/111/deployments/333/events/eventX/power-estimation", powerMeasurement.getLinks().get(1).getHref());
		assertEquals("self",powerMeasurement.getLinks().get(1).getRel());
		assertEquals(MediaType.APPLICATION_XML, powerMeasurement.getLinks().get(1).getType());
	}
	
	@Test
	public void getPowerConsumptionForDeploymentXMLInfoTest() throws Exception {
		PowerMeasurement powerMeasurement = new PowerMeasurement();
		powerMeasurement.setValue(22.0);
		
		String xml = XMLBuilder.getPowerConsumptionForDeploymentXMLInfo(powerMeasurement, "111", "333", "eventX");
		
		JAXBContext jaxbContext = JAXBContext.newInstance(PowerMeasurement.class);
		Unmarshaller jaxbUnmarshaller = jaxbContext.createUnmarshaller();
		powerMeasurement = (PowerMeasurement) jaxbUnmarshaller.unmarshal(new StringReader(xml));
		
		assertEquals("/applications/111/deployments/333/events/eventX/power-consumption", powerMeasurement.getHref());
		assertEquals(22.0, powerMeasurement.getValue(), 0.00001);
		assertEquals("Aggregated power consumption for this aplication deployment and specific event", powerMeasurement.getDescription());
		assertEquals(2, powerMeasurement.getLinks().size());
		assertEquals("/applications/111/deployments/333/events/eventX", powerMeasurement.getLinks().get(0).getHref());
		assertEquals("parent", powerMeasurement.getLinks().get(0).getRel());
		assertEquals(MediaType.APPLICATION_XML, powerMeasurement.getLinks().get(0).getType());
		assertEquals("/applications/111/deployments/333/events/eventX/power-consumption", powerMeasurement.getLinks().get(1).getHref());
		assertEquals("self",powerMeasurement.getLinks().get(1).getRel());
		assertEquals(MediaType.APPLICATION_XML, powerMeasurement.getLinks().get(1).getType());
	}
	
	@Test
	public void getEnergyEstimationForAnVMForAnEventXMLInfoTest() throws Exception {
		EnergyMeasurement energyMeasurement = new EnergyMeasurement();
		energyMeasurement.setValue(22.0);
		
		String xml = XMLBuilder.getEnergyEstimationForAnEventInAVMXMLInfo(energyMeasurement, "111", "333", "444", "eventX");
		
		JAXBContext jaxbContext = JAXBContext.newInstance(EnergyMeasurement.class);
		Unmarshaller jaxbUnmarshaller = jaxbContext.createUnmarshaller();
		energyMeasurement = (EnergyMeasurement) jaxbUnmarshaller.unmarshal(new StringReader(xml));
		
		assertEquals("/applications/111/deployments/333/vms/444/events/eventX/energy-estimation", energyMeasurement.getHref());
		assertEquals(22.0, energyMeasurement.getValue(), 0.00001);
		assertEquals("Aggregated energy estimation in Wh for an event in a specific VM", energyMeasurement.getDescription());
		assertEquals(2, energyMeasurement.getLinks().size());
		assertEquals("/applications/111/deployments/333/vms/444/events/eventX", energyMeasurement.getLinks().get(0).getHref());
		assertEquals("parent", energyMeasurement.getLinks().get(0).getRel());
		assertEquals(MediaType.APPLICATION_XML, energyMeasurement.getLinks().get(0).getType());
		assertEquals("/applications/111/deployments/333/vms/444/events/eventX/energy-estimation", energyMeasurement.getLinks().get(1).getHref());
		assertEquals("self",energyMeasurement.getLinks().get(1).getRel());
		assertEquals(MediaType.APPLICATION_XML, energyMeasurement.getLinks().get(1).getType());
	}
	
	@Test
	public void getPowerEstimationForAnVMForAnEventXMLInfoTest() throws Exception {
		PowerMeasurement powerMeasurement = new PowerMeasurement();
		powerMeasurement.setValue(22.0);
		
		String xml = XMLBuilder.getPowerEstimationForAnEventInAVMXMLInfo(powerMeasurement, "111", "333", "444", "eventX");
		
		JAXBContext jaxbContext = JAXBContext.newInstance(PowerMeasurement.class);
		Unmarshaller jaxbUnmarshaller = jaxbContext.createUnmarshaller();
		powerMeasurement = (PowerMeasurement) jaxbUnmarshaller.unmarshal(new StringReader(xml));
		
		assertEquals("/applications/111/deployments/333/vms/444/events/eventX/power-estimation", powerMeasurement.getHref());
		assertEquals(22.0, powerMeasurement.getValue(), 0.00001);
		assertEquals("Aggregated power estimation in Wh for an event in a specific VM", powerMeasurement.getDescription());
		assertEquals(2, powerMeasurement.getLinks().size());
		assertEquals("/applications/111/deployments/333/vms/444/events/eventX", powerMeasurement.getLinks().get(0).getHref());
		assertEquals("parent", powerMeasurement.getLinks().get(0).getRel());
		assertEquals(MediaType.APPLICATION_XML, powerMeasurement.getLinks().get(0).getType());
		assertEquals("/applications/111/deployments/333/vms/444/events/eventX/power-estimation", powerMeasurement.getLinks().get(1).getHref());
		assertEquals("self",powerMeasurement.getLinks().get(1).getRel());
		assertEquals(MediaType.APPLICATION_XML, powerMeasurement.getLinks().get(1).getType());
	}
	
	@Test
	public void getEnergyConsumptionForAnVMForAnEventXMLInfoTest() throws Exception {
		EnergyMeasurement energyMeasurement = new EnergyMeasurement();
		energyMeasurement.setValue(22.0);
		
		String xml = XMLBuilder.getEnergyConsumptionForAnEventInAVMXMLInfo(energyMeasurement, "111", "333", "444", "eventX");
		
		JAXBContext jaxbContext = JAXBContext.newInstance(EnergyMeasurement.class);
		Unmarshaller jaxbUnmarshaller = jaxbContext.createUnmarshaller();
		energyMeasurement = (EnergyMeasurement) jaxbUnmarshaller.unmarshal(new StringReader(xml));
		
		assertEquals("/applications/111/deployments/333/vms/444/events/eventX/energy-consumption", energyMeasurement.getHref());
		assertEquals(22.0, energyMeasurement.getValue(), 0.00001);
		assertEquals("Aggregated energy consumption in Wh for an event in a specific VM", energyMeasurement.getDescription());
		assertEquals(2, energyMeasurement.getLinks().size());
		assertEquals("/applications/111/deployments/333/vms/444/events/eventX", energyMeasurement.getLinks().get(0).getHref());
		assertEquals("parent", energyMeasurement.getLinks().get(0).getRel());
		assertEquals(MediaType.APPLICATION_XML, energyMeasurement.getLinks().get(0).getType());
		assertEquals("/applications/111/deployments/333/vms/444/events/eventX/energy-consumption", energyMeasurement.getLinks().get(1).getHref());
		assertEquals("self",energyMeasurement.getLinks().get(1).getRel());
		assertEquals(MediaType.APPLICATION_XML, energyMeasurement.getLinks().get(1).getType());
	}
	
	@Test
	public void getPowerConsumptionForAnVMForAnEventXMLInfoTest() throws Exception {
		PowerMeasurement powerMeasurement = new PowerMeasurement();
		powerMeasurement.setValue(22.0);
		
		String xml = XMLBuilder.getPowerConsumptionForAnEventInAVMXMLInfo(powerMeasurement, "111", "333", "444", "eventX");
		
		JAXBContext jaxbContext = JAXBContext.newInstance(PowerMeasurement.class);
		Unmarshaller jaxbUnmarshaller = jaxbContext.createUnmarshaller();
		powerMeasurement = (PowerMeasurement) jaxbUnmarshaller.unmarshal(new StringReader(xml));
		
		assertEquals("/applications/111/deployments/333/vms/444/events/eventX/power-consumption", powerMeasurement.getHref());
		assertEquals(22.0, powerMeasurement.getValue(), 0.00001);
		assertEquals("Aggregated power consumption in Wh for an event in a specific VM", powerMeasurement.getDescription());
		assertEquals(2, powerMeasurement.getLinks().size());
		assertEquals("/applications/111/deployments/333/vms/444/events/eventX", powerMeasurement.getLinks().get(0).getHref());
		assertEquals("parent", powerMeasurement.getLinks().get(0).getRel());
		assertEquals(MediaType.APPLICATION_XML, powerMeasurement.getLinks().get(0).getType());
		assertEquals("/applications/111/deployments/333/vms/444/events/eventX/power-consumption", powerMeasurement.getLinks().get(1).getHref());
		assertEquals("self",powerMeasurement.getLinks().get(1).getRel());
		assertEquals(MediaType.APPLICATION_XML, powerMeasurement.getLinks().get(1).getType());
	}
	
	@Test
	public void addEnergyEstimationForAnVMForAnEventXMLInfoTest() throws Exception {
		EnergyMeasurement energyMeasurement = new EnergyMeasurement();
		energyMeasurement.setValue(22.0);
		
		energyMeasurement = XMLBuilder.addEnergyEstimationForAnEventInAVMXMLInfo(energyMeasurement, "111", "333", "444", "eventX");
		
		assertEquals(2, energyMeasurement.getLinks().size());
		assertEquals(MediaType.APPLICATION_XML, energyMeasurement.getLinks().get(0).getType());
		assertEquals("parent", energyMeasurement.getLinks().get(0).getRel());
		assertEquals("/applications/111/deployments/333/vms/444/events/eventX", energyMeasurement.getLinks().get(0).getHref());
		assertEquals(MediaType.APPLICATION_XML, energyMeasurement.getLinks().get(1).getType());
		assertEquals("self", energyMeasurement.getLinks().get(1).getRel());
		assertEquals("/applications/111/deployments/333/vms/444/events/eventX/energy-estimation", energyMeasurement.getLinks().get(1).getHref());

	}
	
	@Test
	public void addPowerEstimationForAnVMForAnEventXMLInfoTest() throws Exception {
		PowerMeasurement powerMeasurement = new PowerMeasurement();
		powerMeasurement.setValue(22.0);
		
		powerMeasurement = XMLBuilder.addPowerEstimationForAnEventInAVMXMLInfo(powerMeasurement, "111", "333", "444", "eventX");
		
		assertEquals(2, powerMeasurement.getLinks().size());
		assertEquals(MediaType.APPLICATION_XML, powerMeasurement.getLinks().get(0).getType());
		assertEquals("parent", powerMeasurement.getLinks().get(0).getRel());
		assertEquals("/applications/111/deployments/333/vms/444/events/eventX", powerMeasurement.getLinks().get(0).getHref());
		assertEquals(MediaType.APPLICATION_XML, powerMeasurement.getLinks().get(1).getType());
		assertEquals("self", powerMeasurement.getLinks().get(1).getRel());
		assertEquals("/applications/111/deployments/333/vms/444/events/eventX/power-estimation", powerMeasurement.getLinks().get(1).getHref());

	}
	
	@Test
	public void addEnergyConsumptionForAnVMForAnEventXMLInfoTest() throws Exception {
		EnergyMeasurement energyMeasurement = new EnergyMeasurement();
		energyMeasurement.setValue(22.0);
		
		energyMeasurement = XMLBuilder.addEnergyConsumptionForAnEventInAVMXMLInfo(energyMeasurement, "111", "333", "444", "eventX");
		
		assertEquals(2, energyMeasurement.getLinks().size());
		assertEquals(MediaType.APPLICATION_XML, energyMeasurement.getLinks().get(0).getType());
		assertEquals("parent", energyMeasurement.getLinks().get(0).getRel());
		assertEquals("/applications/111/deployments/333/vms/444/events/eventX", energyMeasurement.getLinks().get(0).getHref());
		assertEquals(MediaType.APPLICATION_XML, energyMeasurement.getLinks().get(1).getType());
		assertEquals("self", energyMeasurement.getLinks().get(1).getRel());
		assertEquals("/applications/111/deployments/333/vms/444/events/eventX/energy-consumption", energyMeasurement.getLinks().get(1).getHref());

	}
	
	@Test
	public void addPowerConsumptionForAnVMForAnEventXMLInfoTest() throws Exception {
		PowerMeasurement powerMeasurement = new PowerMeasurement();
		powerMeasurement.setValue(22.0);
		
		powerMeasurement = XMLBuilder.addPowerConsumptionForAnEventInAVMXMLInfo(powerMeasurement, "111", "333", "444", "eventX");
		
		assertEquals(2, powerMeasurement.getLinks().size());
		assertEquals(MediaType.APPLICATION_XML, powerMeasurement.getLinks().get(0).getType());
		assertEquals("parent", powerMeasurement.getLinks().get(0).getRel());
		assertEquals("/applications/111/deployments/333/vms/444/events/eventX", powerMeasurement.getLinks().get(0).getHref());
		assertEquals(MediaType.APPLICATION_XML, powerMeasurement.getLinks().get(1).getType());
		assertEquals("self", powerMeasurement.getLinks().get(1).getRel());
		assertEquals("/applications/111/deployments/333/vms/444/events/eventX/power-consumption", powerMeasurement.getLinks().get(1).getHref());

	}
	
	@Test
	public void getEventSampleCollectionXMLInfo() throws Exception {
		EventSample eventSample1 = new EventSample();
		eventSample1.setCvalue(1.0);
		eventSample1.setEvalue(2.0);
		eventSample1.setPvalue(3.0);
		eventSample1.setTimestampBeging(1l);
		eventSample1.setTimestampEnd(2l);
		eventSample1.setVmid("vmid");
		eventSample1.setAppid("appid");
		
		EventSample eventSample2 = new EventSample();
		eventSample2.setCvalue(4.0);
		eventSample2.setEvalue(5.0);
		eventSample2.setPvalue(6.0);
		eventSample2.setTimestampBeging(3l);
		eventSample2.setTimestampEnd(4l);
		eventSample2.setVmid("vmid2");
		eventSample2.setAppid("appid2");
		
		List<EventSample> eventSamples = new ArrayList<EventSample>();
		eventSamples.add(eventSample1);
		eventSamples.add(eventSample2);
		
		String xml = XMLBuilder.getEventSampleCollectionXMLInfo(eventSamples, "appId", "deploymentId", "vmId", "eventId");
		
		JAXBContext jaxbContext = JAXBContext.newInstance(Collection.class);
		Unmarshaller jaxbUnmarshaller = jaxbContext.createUnmarshaller();
		Collection collection = (Collection) jaxbUnmarshaller.unmarshal(new StringReader(xml));
		
		assertEquals("/applications/appId/deployments/deploymentId/vms/vmId/events/eventId/event-samples", collection.getHref());
		assertEquals(0, collection.getItems().getOffset());
		assertEquals(2, collection.getItems().getTotal());
		//Links
		assertEquals(2, collection.getLinks().size());
		assertEquals("/applications/appId/deployments/deploymentId/vms/vmId/events/eventId", collection.getLinks().get(0).getHref());
		assertEquals("parent", collection.getLinks().get(0).getRel());
		assertEquals(MediaType.APPLICATION_XML, collection.getLinks().get(0).getType());
		assertEquals("/applications/appId/deployments/deploymentId/vms/vmId/events/eventId/event-samples", collection.getLinks().get(1).getHref());
		assertEquals("self", collection.getLinks().get(1).getRel());
		assertEquals(MediaType.APPLICATION_XML, collection.getLinks().get(1).getType());
		// EnergySamples
		assertEquals(2, collection.getItems().getEventSamples().size());
		// EnergySamples 1
 		assertEquals(1.0, collection.getItems().getEventSamples().get(0).getCvalue(), 0.00001);
		assertEquals(2.0, collection.getItems().getEventSamples().get(0).getEvalue(), 0.00001);
		assertEquals(3.0, collection.getItems().getEventSamples().get(0).getPvalue(), 0.00001);
		assertEquals(1l, collection.getItems().getEventSamples().get(0).getTimestampBeging());
		assertEquals(2l, collection.getItems().getEventSamples().get(0).getTimestampEnd());
		assertEquals("vmid", collection.getItems().getEventSamples().get(0).getVmid());
		assertEquals("appid", collection.getItems().getEventSamples().get(0).getAppid());
		// EnergySamples 2
 		assertEquals(4.0, collection.getItems().getEventSamples().get(1).getCvalue(), 0.00001);
		assertEquals(5.0, collection.getItems().getEventSamples().get(1).getEvalue(), 0.00001);
		assertEquals(6.0, collection.getItems().getEventSamples().get(1).getPvalue(), 0.00001);
		assertEquals(3l, collection.getItems().getEventSamples().get(1).getTimestampBeging());
		assertEquals(4l, collection.getItems().getEventSamples().get(1).getTimestampEnd());
		assertEquals("vmid2", collection.getItems().getEventSamples().get(1).getVmid());
		assertEquals("appid2", collection.getItems().getEventSamples().get(1).getAppid());	
	}
	
	@Test
	public void testAddAgreementXMLInfo() {
		Agreement agreement = new Agreement();
		agreement.setAccepted(true);
		agreement.setId(2);
		agreement.setPrice("222");
		agreement.setProviderId("provider-id");
		agreement.setSlaAgreement("sssas");
		agreement.setSlaAgreementId("sla-agreement-id");
		
		agreement = XMLBuilder.addAgreementXMLInfo(agreement, "app-id", 223);
		
		assertEquals("/applications/app-id/deployments/223/agreements/2", agreement.getHref());
		assertEquals("222", agreement.getPrice());
		assertEquals(2, agreement.getId());
		assertEquals("provider-id", agreement.getProviderId());
		assertEquals("sssas", agreement.getSlaAgreement());
		assertEquals("sla-agreement-id", agreement.getSlaAgreementId());
		assertEquals(2, agreement.getLinks().size());
		assertEquals("/applications/app-id/deployments/223/agreements", agreement.getLinks().get(0).getHref());
		assertEquals("parent", agreement.getLinks().get(0).getRel());
		assertEquals(MediaType.APPLICATION_XML, agreement.getLinks().get(0).getType());
		assertEquals("/applications/app-id/deployments/223/agreements/2", agreement.getLinks().get(1).getHref());
		assertEquals("self", agreement.getLinks().get(1).getRel());
		assertEquals(MediaType.APPLICATION_XML, agreement.getLinks().get(1).getType());
	}
	
	@Test
	public void testGetAgreementXML() throws JAXBException {
		Agreement agreement = new Agreement();
		agreement.setAccepted(true);
		agreement.setId(2);
		agreement.setPrice("222");
		agreement.setProviderId("provider-id");
		agreement.setSlaAgreement("sssas");
		agreement.setSlaAgreementId("sla-agreement-id");
		
		String xml = XMLBuilder.getAgreementXML(agreement, "app-id", 223);
			
		JAXBContext jaxbContext = JAXBContext.newInstance(Agreement.class);
		Unmarshaller jaxbUnmarshaller = jaxbContext.createUnmarshaller();
		agreement = (Agreement) jaxbUnmarshaller.unmarshal(new StringReader(xml));

		assertEquals("/applications/app-id/deployments/223/agreements/2", agreement.getHref());
		assertEquals("222", agreement.getPrice());
		assertEquals(2, agreement.getId());
		assertEquals("provider-id", agreement.getProviderId());
		//assertEquals("sssas", agreement.getSlaAgreement());  // This it is not converted to XML... 
		assertEquals("sla-agreement-id", agreement.getSlaAgreementId());
		assertEquals(2, agreement.getLinks().size());
		assertEquals("/applications/app-id/deployments/223/agreements", agreement.getLinks().get(0).getHref());
		assertEquals("parent", agreement.getLinks().get(0).getRel());
		assertEquals(MediaType.APPLICATION_XML, agreement.getLinks().get(0).getType());
		assertEquals("/applications/app-id/deployments/223/agreements/2", agreement.getLinks().get(1).getHref());
		assertEquals("self", agreement.getLinks().get(1).getRel());
		assertEquals(MediaType.APPLICATION_XML, agreement.getLinks().get(1).getType());
	}
	
	@Test
	public void getCollectionOfAgreementsTest() throws JAXBException {
		Agreement agreement1 = new Agreement();
		agreement1.setAccepted(false);
		agreement1.setId(1);
		agreement1.setPrice("111");
		agreement1.setProviderId("provider-id1");
		agreement1.setSlaAgreement("sssas1");
		agreement1.setSlaAgreementId("sla-agreement-id1");
		
		Agreement agreement2 = new Agreement();
		agreement2.setAccepted(true);
		agreement2.setId(2);
		agreement2.setPrice("222");
		agreement2.setProviderId("provider-id");
		agreement2.setSlaAgreement("sssas");
		agreement2.setSlaAgreementId("sla-agreement-id");
		
		List<Agreement> agreements = new ArrayList<Agreement>();
		agreements.add(agreement1);
		agreements.add(agreement2);
		
		String xml = XMLBuilder.getCollectionOfAgreements(agreements, "app-id", 223);
		
		JAXBContext jaxbContext = JAXBContext.newInstance(Collection.class);
		Unmarshaller jaxbUnmarshaller = jaxbContext.createUnmarshaller();
		Collection collection = (Collection) jaxbUnmarshaller.unmarshal(new StringReader(xml));
		
		assertEquals("/applications/app-id/deployments/223/agreements", collection.getHref());
		assertEquals(0, collection.getItems().getOffset());
		assertEquals(2, collection.getItems().getTotal());
		// Links
		assertEquals(2, collection.getLinks().size());
		assertEquals("/applications/app-id/deployments/223", collection.getLinks().get(0).getHref());
		assertEquals("parent", collection.getLinks().get(0).getRel());
		assertEquals(MediaType.APPLICATION_XML, collection.getLinks().get(0).getType());
		assertEquals("/applications/app-id/deployments/223/agreements", collection.getLinks().get(1).getHref());
		assertEquals("self", collection.getLinks().get(1).getRel());
		assertEquals(MediaType.APPLICATION_XML, collection.getLinks().get(1).getType());
		// # Agreements
		assertEquals(2, collection.getItems().getAgreements().size());
		// Agreement #1
		Agreement agreement = collection.getItems().getAgreements().get(0);
		assertEquals("/applications/app-id/deployments/223/agreements/1", agreement.getHref());
		assertEquals("111", agreement.getPrice());
		assertEquals(1, agreement.getId());
		assertEquals("provider-id1", agreement.getProviderId());
		//assertEquals("sssas", agreement.getSlaAgreement());  // This it is not converted to XML... 
		assertEquals("sla-agreement-id1", agreement.getSlaAgreementId());
		assertEquals(2, agreement.getLinks().size());
		assertEquals("/applications/app-id/deployments/223/agreements", agreement.getLinks().get(0).getHref());
		assertEquals("parent", agreement.getLinks().get(0).getRel());
		assertEquals(MediaType.APPLICATION_XML, agreement.getLinks().get(0).getType());
		assertEquals("/applications/app-id/deployments/223/agreements/1", agreement.getLinks().get(1).getHref());
		assertEquals("self", agreement.getLinks().get(1).getRel());
		assertEquals(MediaType.APPLICATION_XML, agreement.getLinks().get(1).getType());
		// Agreement #2
		agreement = collection.getItems().getAgreements().get(1);
		assertEquals("/applications/app-id/deployments/223/agreements/2", agreement.getHref());
		assertEquals("222", agreement.getPrice());
		assertEquals(2, agreement.getId());
		assertEquals("provider-id", agreement.getProviderId());
		//assertEquals("sssas", agreement.getSlaAgreement());  // This it is not converted to XML... 
		assertEquals("sla-agreement-id", agreement.getSlaAgreementId());
		assertEquals(2, agreement.getLinks().size());
		assertEquals("/applications/app-id/deployments/223/agreements", agreement.getLinks().get(0).getHref());
		assertEquals("parent", agreement.getLinks().get(0).getRel());
		assertEquals(MediaType.APPLICATION_XML, agreement.getLinks().get(0).getType());
		assertEquals("/applications/app-id/deployments/223/agreements/2", agreement.getLinks().get(1).getHref());
		assertEquals("self", agreement.getLinks().get(1).getRel());
		assertEquals(MediaType.APPLICATION_XML, agreement.getLinks().get(1).getType());
	}
	
	@Test
	public void getCollectionOfNullAgreementsTest() throws JAXBException {
		
		String xml = XMLBuilder.getCollectionOfAgreements(null, "app-id", 223);
		
		JAXBContext jaxbContext = JAXBContext.newInstance(Collection.class);
		Unmarshaller jaxbUnmarshaller = jaxbContext.createUnmarshaller();
		Collection collection = (Collection) jaxbUnmarshaller.unmarshal(new StringReader(xml));
		
		assertEquals("/applications/app-id/deployments/223/agreements", collection.getHref());
		assertEquals(0, collection.getItems().getOffset());
		assertEquals(0, collection.getItems().getTotal());
		// Links
		assertEquals(2, collection.getLinks().size());
		assertEquals("/applications/app-id/deployments/223", collection.getLinks().get(0).getHref());
		assertEquals("parent", collection.getLinks().get(0).getRel());
		assertEquals(MediaType.APPLICATION_XML, collection.getLinks().get(0).getType());
		assertEquals("/applications/app-id/deployments/223/agreements", collection.getLinks().get(1).getHref());
		assertEquals("self", collection.getLinks().get(1).getRel());
		assertEquals(MediaType.APPLICATION_XML, collection.getLinks().get(1).getType());
	}
	
	@Test
	public void getCollectionOfVMsTest() throws JAXBException {
		VM vm1 = new VM();
		vm1.setId(33);
		vm1.setIp("0.0.0.0");
		vm1.setOvfId("vm-ovf-id");
		vm1.setProviderId("provider-id");
		vm1.setSlaAgreement("sla-agreement-id");
		vm1.setStatus("ACTIVE");
		
		VM vm2 = new VM();
		vm2.setId(44);
		vm2.setIp("1.1.1.1");
		vm2.setOvfId("vm-ovf-id2");
		vm2.setProviderId("provider-id2");
		vm2.setSlaAgreement("sla-agreement-id2");
		vm2.setStatus("DELETED");
		
		List<VM> vms = new ArrayList<VM>();
		vms.add(vm1);
		vms.add(vm2);
		
		String xml = XMLBuilder.getCollectionOfVMs(vms, "app-id", 223);
		
		JAXBContext jaxbContext = JAXBContext.newInstance(Collection.class);
		Unmarshaller jaxbUnmarshaller = jaxbContext.createUnmarshaller();
		Collection collection = (Collection) jaxbUnmarshaller.unmarshal(new StringReader(xml));
		
		assertEquals("/applications/app-id/deployments/223/vms", collection.getHref());
		assertEquals(0, collection.getItems().getOffset());
		assertEquals(2, collection.getItems().getTotal());
		// Links
		assertEquals(2, collection.getLinks().size());
		assertEquals("/applications/app-id/deployments/223", collection.getLinks().get(0).getHref());
		assertEquals("parent", collection.getLinks().get(0).getRel());
		assertEquals(MediaType.APPLICATION_XML, collection.getLinks().get(0).getType());
		assertEquals("/applications/app-id/deployments/223/vms", collection.getLinks().get(1).getHref());
		assertEquals("self", collection.getLinks().get(1).getRel());
		assertEquals(MediaType.APPLICATION_XML, collection.getLinks().get(1).getType());
		// # VMs
		assertEquals(2, collection.getItems().getVms().size());
		// VM #1
		VM vm = collection.getItems().getVms().get(0);
		assertEquals("/applications/app-id/deployments/223/vms/33", vm.getHref());
		assertEquals(33, vm.getId());
		assertEquals("0.0.0.0", vm.getIp());
		assertEquals("ACTIVE", vm.getStatus());
		assertEquals("vm-ovf-id", vm.getOvfId());
		assertEquals("provider-id", vm.getProviderId());
		assertEquals(2, vm.getLinks().size());
		assertEquals("/applications/app-id/deployments/223/vms", vm.getLinks().get(0).getHref());
		assertEquals("parent", vm.getLinks().get(0).getRel());
		assertEquals(MediaType.APPLICATION_XML, vm.getLinks().get(0).getType());
		assertEquals("/applications/app-id/deployments/223/vms/33", vm.getLinks().get(1).getHref());
		assertEquals("self", vm.getLinks().get(1).getRel());
		assertEquals(MediaType.APPLICATION_XML, vm.getLinks().get(1).getType());
		// VM #2
		vm = collection.getItems().getVms().get(1);
		assertEquals("/applications/app-id/deployments/223/vms/44", vm.getHref());
		assertEquals(44, vm.getId());
		assertEquals("1.1.1.1", vm.getIp());
		assertEquals("DELETED", vm.getStatus());
		assertEquals("vm-ovf-id2", vm.getOvfId());
		assertEquals("provider-id2", vm.getProviderId());
		assertEquals(2, vm.getLinks().size());
		assertEquals("/applications/app-id/deployments/223/vms", vm.getLinks().get(0).getHref());
		assertEquals("parent", vm.getLinks().get(0).getRel());
		assertEquals(MediaType.APPLICATION_XML, vm.getLinks().get(0).getType());
		assertEquals("/applications/app-id/deployments/223/vms/44", vm.getLinks().get(1).getHref());
		assertEquals("self", vm.getLinks().get(1).getRel());
		assertEquals(MediaType.APPLICATION_XML, vm.getLinks().get(1).getType());
	}
	
	@Test
	public void getCostEstimationForAnEventInAVMXMLInfoTest() throws JAXBException {
		Cost cost = new Cost();
		cost.setCharges(1.0d);
		cost.setEnergyValue(2.0d);
		cost.setPowerValue(1.0d);
		
		String xml = XMLBuilder.getCostEstimationForAnEventInAVMXMLInfo(cost, "app-name", "1", "3", "loquesea");
		
		JAXBContext jaxbContext = JAXBContext.newInstance(Cost.class);
		Unmarshaller jaxbUnmarshaller = jaxbContext.createUnmarshaller();
		cost = (Cost) jaxbUnmarshaller.unmarshal(new StringReader(xml));
		
		assertEquals("/applications/app-name/deployments/1/vms/3/events/loquesea/cost-estimation", cost.getHref());
		assertEquals(1.0d, cost.getCharges().doubleValue(), 0.0001);
		assertEquals("Energy estimation in WATTHOURS", cost.getEnergyDescription());
		assertEquals("Power estimation in WATTS", cost.getPowerDescription());
		assertEquals(2.0d, cost.getEnergyValue().doubleValue(), 0.0001);
		assertEquals(1.0d, cost.getPowerValue().doubleValue(), 0.0001);
		assertEquals("Charges estimation in EUROS", cost.getChargesDescription());
		assertEquals(2, cost.getLinks().size());
		assertEquals("/applications/app-name/deployments/1/vms/3", cost.getLinks().get(0).getHref());
		assertEquals("parent", cost.getLinks().get(0).getRel());
		assertEquals(MediaType.APPLICATION_XML, cost.getLinks().get(0).getType());
		assertEquals("/applications/app-name/deployments/1/vms/3/events/loquesea/cost-estimation", cost.getLinks().get(1).getHref());
		assertEquals("self", cost.getLinks().get(1).getRel());
		assertEquals(MediaType.APPLICATION_XML, cost.getLinks().get(1).getType());
	}
	
	@Test
	public void getCostEstimationForAnEventInADeploymentXMLInfoTest() throws JAXBException {
		Cost cost = new Cost();
		cost.setCharges(1.0d);
		cost.setEnergyValue(2.0d);
		cost.setPowerValue(1.0d);
		
		String xml = XMLBuilder.getCostEstimationForAnEventInADeploymentXMLInfo(cost, "app-name", "1", "loquesea");
		
		JAXBContext jaxbContext = JAXBContext.newInstance(Cost.class);
		Unmarshaller jaxbUnmarshaller = jaxbContext.createUnmarshaller();
		cost = (Cost) jaxbUnmarshaller.unmarshal(new StringReader(xml));
		
		assertEquals("/applications/app-name/deployments/1/events/loquesea/cost-estimation", cost.getHref());
		assertEquals(1.0d, cost.getCharges().doubleValue(), 0.0001);
		assertEquals("Energy estimation in WATTHOURS", cost.getEnergyDescription());
		assertEquals("Power estimation in WATTS", cost.getPowerDescription());
		assertEquals(2.0d, cost.getEnergyValue().doubleValue(), 0.0001);
		assertEquals(1.0d, cost.getPowerValue().doubleValue(), 0.0001);
		assertEquals("Charges estimation in EUROS", cost.getChargesDescription());
		assertEquals(2, cost.getLinks().size());
		assertEquals("/applications/app-name/deployments/1", cost.getLinks().get(0).getHref());
		assertEquals("parent", cost.getLinks().get(0).getRel());
		assertEquals(MediaType.APPLICATION_XML, cost.getLinks().get(0).getType());
		assertEquals("/applications/app-name/deployments/1/events/loquesea/cost-estimation", cost.getLinks().get(1).getHref());
		assertEquals("self", cost.getLinks().get(1).getRel());
		assertEquals(MediaType.APPLICATION_XML, cost.getLinks().get(1).getType());
	}
	
	@Test
	public void getCostConsumptionForADeploymentTest() throws JAXBException {
		Cost cost = new Cost();
		cost.setCharges(1.0d);
		cost.setEnergyValue(2.0d);
		cost.setPowerValue(1.0d);
		
		String xml = XMLBuilder.getCostConsumptionForADeployment(cost, "app-name", "1");
		
		JAXBContext jaxbContext = JAXBContext.newInstance(Cost.class);
		Unmarshaller jaxbUnmarshaller = jaxbContext.createUnmarshaller();
		cost = (Cost) jaxbUnmarshaller.unmarshal(new StringReader(xml));
		
		assertEquals("/applications/app-name/deployments/1/cost-consumption", cost.getHref());
		assertEquals(1.0d, cost.getCharges().doubleValue(), 0.0001);
		assertEquals("N/A", cost.getEnergyDescription());
		assertEquals("N/A", cost.getPowerDescription());
		assertEquals(2.0d, cost.getEnergyValue().doubleValue(), 0.0001);
		assertEquals(1.0d, cost.getPowerValue().doubleValue(), 0.0001);
		assertEquals("Charges estimation in EUROS", cost.getChargesDescription());
		assertEquals(2, cost.getLinks().size());
		assertEquals("/applications/app-name/deployments/1", cost.getLinks().get(0).getHref());
		assertEquals("parent", cost.getLinks().get(0).getRel());
		assertEquals(MediaType.APPLICATION_XML, cost.getLinks().get(0).getType());
		assertEquals("/applications/app-name/deployments/1/cost-consumption", cost.getLinks().get(1).getHref());
		assertEquals("self", cost.getLinks().get(1).getRel());
		assertEquals(MediaType.APPLICATION_XML, cost.getLinks().get(1).getType());
	}
}
