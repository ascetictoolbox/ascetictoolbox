package eu.ascetic.paas.applicationmanager.rest.util;

import static org.junit.Assert.assertEquals;

import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;

import javax.ws.rs.core.MediaType;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;

import org.junit.Test;

import eu.ascetic.paas.applicationmanager.model.Application;
import eu.ascetic.paas.applicationmanager.model.Collection;
import eu.ascetic.paas.applicationmanager.model.Deployment;
import eu.ascetic.paas.applicationmanager.model.VM;

public class XMLBuilderTest {

	@Test
	public void addApplicationXMLInfoTest() {
		Application application = new Application();
		application.setId(1);
		application.setName("name");
		
		application = XMLBuilder.addApplicationXMLInfo(application);
		
		assertEquals(1, application.getId());
		assertEquals("/applications/1", application.getHref());
		assertEquals(3, application.getLinks().size());
		assertEquals("/applications", application.getLinks().get(0).getHref());
		assertEquals("parent", application.getLinks().get(0).getRel());
		assertEquals(MediaType.APPLICATION_XML, application.getLinks().get(0).getType());
		assertEquals("/applications/1", application.getLinks().get(1).getHref());
		assertEquals("self",application.getLinks().get(1).getRel());
		assertEquals(MediaType.APPLICATION_XML, application.getLinks().get(1).getType());
		assertEquals("/applications/1/deployments", application.getLinks().get(2).getHref());
		assertEquals("deployments",application.getLinks().get(2).getRel());
		assertEquals(MediaType.APPLICATION_XML, application.getLinks().get(2).getType());
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
		assertEquals("/applications/22", application.getHref());
		assertEquals(3, application.getLinks().size());
		assertEquals("/applications", application.getLinks().get(0).getHref());
		assertEquals("parent", application.getLinks().get(0).getRel());
		assertEquals(MediaType.APPLICATION_XML, application.getLinks().get(0).getType());
		assertEquals("/applications/22", application.getLinks().get(1).getHref());
		assertEquals("self",application.getLinks().get(1).getRel());
		assertEquals(MediaType.APPLICATION_XML, application.getLinks().get(1).getType());
		assertEquals("/applications/22/deployments", application.getLinks().get(2).getHref());
		assertEquals("deployments",application.getLinks().get(2).getRel());
		assertEquals(MediaType.APPLICATION_XML, application.getLinks().get(2).getType());
		
		Deployment deployment = application.getDeployments().get(0);
		assertEquals(1, deployment.getId());
		assertEquals("/applications/22/deployments/1", deployment.getHref());
		assertEquals(4, deployment.getLinks().size());
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
		
		VM vm = deployment.getVms().get(0);
		assertEquals(44, vm.getId());
		assertEquals("/applications/22/deployments/1/vms/44", vm.getHref());
		assertEquals(2, vm.getLinks().size());
		assertEquals("/applications/22/deployments/1/vms", vm.getLinks().get(0).getHref());
		assertEquals("parent", vm.getLinks().get(0).getRel());
		assertEquals(MediaType.APPLICATION_XML, vm.getLinks().get(0).getType());
		assertEquals("/applications/22/deployments/1/vms/44", vm.getLinks().get(1).getHref());
		assertEquals("self",vm.getLinks().get(1).getRel());
	}
	
	@Test
	public void getXMLApplicationTest() throws JAXBException {
		Application applicationBeforeXML = new Application();
		applicationBeforeXML.setId(1);
		applicationBeforeXML.setName("name");
		String xml = XMLBuilder.getApplicationXML(applicationBeforeXML);
		
		JAXBContext jaxbContext = JAXBContext.newInstance(Application.class);
		Unmarshaller jaxbUnmarshaller = jaxbContext.createUnmarshaller();
		Application application = (Application) jaxbUnmarshaller.unmarshal(new StringReader(xml));
		
		assertEquals(1, application.getId());
		assertEquals("/applications/1", application.getHref());
		assertEquals(3, application.getLinks().size());
		assertEquals("/applications", application.getLinks().get(0).getHref());
		assertEquals("parent", application.getLinks().get(0).getRel());
		assertEquals(MediaType.APPLICATION_XML, application.getLinks().get(0).getType());
		assertEquals("/applications/1", application.getLinks().get(1).getHref());
		assertEquals("self",application.getLinks().get(1).getRel());
		assertEquals(MediaType.APPLICATION_XML, application.getLinks().get(1).getType());
		assertEquals("/applications/1/deployments", application.getLinks().get(2).getHref());
		assertEquals("deployments",application.getLinks().get(2).getRel());
		assertEquals(MediaType.APPLICATION_XML, application.getLinks().get(2).getType());
	}
	
	@Test
	public void addDeploymentXMLInfoTest() {
		Deployment deployment = new Deployment();
		deployment.setId(1);
		deployment.setStatus("RUNNIG");
		deployment.setPrice("expensive");
		
		deployment = XMLBuilder.addDeploymentXMLInfo(deployment, 22);
		
		assertEquals(1, deployment.getId());
		assertEquals("/applications/22/deployments/1", deployment.getHref());
		assertEquals(4, deployment.getLinks().size());
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
	}
	
	@Test
	public void getXMLDeploymentTest() throws JAXBException {
		Deployment deploymentBeforeXML = new Deployment();
		deploymentBeforeXML.setId(1);
		deploymentBeforeXML.setStatus("RUNNIG");
		deploymentBeforeXML.setPrice("expensive");
		
		String xml = XMLBuilder.getDeploymentXML(deploymentBeforeXML, 22);
		
		JAXBContext jaxbContext = JAXBContext.newInstance(Deployment.class);
		Unmarshaller jaxbUnmarshaller = jaxbContext.createUnmarshaller();
		Deployment deployment = (Deployment) jaxbUnmarshaller.unmarshal(new StringReader(xml));
		
		assertEquals(1, deployment.getId());
		assertEquals("/applications/22/deployments/1", deployment.getHref());
		assertEquals(4, deployment.getLinks().size());
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
		
		vm = XMLBuilder.addVMXMLInfo(vm, 22, 33);
		
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
		
		String xml = XMLBuilder.getVMXML(vmBeforeXML, 22, 33);
		
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
		application1.setName("name 1");

		Application application2 = new Application();
		application2.setId(2);
		application2.setName("name 2");
		
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
		assertEquals("/applications/1", collection.getItems().getApplications().get(0).getHref());
		assertEquals(3, collection.getItems().getApplications().get(0).getLinks().size());
		assertEquals("/applications/2", collection.getItems().getApplications().get(1).getHref());
		assertEquals(3, collection.getItems().getApplications().get(1).getLinks().size());
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
}
