package eu.ascetic.paas.applicationmanager.event.deployment;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;

import reactor.event.Event;
import es.bsc.vmmclient.models.Vm;
import es.bsc.vmmclient.models.VmDeployed;
import eu.ascetic.amqp.client.AmqpMessageReceiver;
import eu.ascetic.paas.applicationmanager.amqp.AbstractTest;
import eu.ascetic.paas.applicationmanager.amqp.AmqpListListener;
import eu.ascetic.paas.applicationmanager.conf.Configuration;
import eu.ascetic.paas.applicationmanager.dao.ApplicationDAO;
import eu.ascetic.paas.applicationmanager.dao.DeploymentDAO;
import eu.ascetic.paas.applicationmanager.dao.ImageDAO;
import eu.ascetic.paas.applicationmanager.dao.VMDAO;
import eu.ascetic.paas.applicationmanager.em.EnergyModellerBean;
import eu.ascetic.paas.applicationmanager.event.DeploymentEvent;
import eu.ascetic.paas.applicationmanager.event.deployment.matchers.ImageToUploadWithEquals;
import eu.ascetic.paas.applicationmanager.event.deployment.matchers.VmWithEquals;
import eu.ascetic.paas.applicationmanager.model.Application;
import eu.ascetic.paas.applicationmanager.model.Deployment;
import eu.ascetic.paas.applicationmanager.model.Dictionary;
import eu.ascetic.paas.applicationmanager.model.Image;
import eu.ascetic.paas.applicationmanager.model.VM;
import eu.ascetic.paas.applicationmanager.model.converter.ModelConverter;
import eu.ascetic.paas.applicationmanager.providerregistry.PRClient;
import eu.ascetic.paas.applicationmanager.vmmanager.client.VmManagerClient;

/**
 * 
 * Copyright 2015 ATOS SPAIN S.A. 
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
 * Test that verifies the Application Manager reacts well to the event that a new 
 * deployment is ready to be deployed into the infrastructure.
 */

// TODO test for the possible exceptions at the hour of creating deployments...
public class DeployEventHandlerTest extends AbstractTest {
	private String threeTierWebAppWithMaxMinCPUFile = "3tier-webapp-max-min-cpu.xml";
	private String threeTierWebAppWithMaxMinCPU;
	private String threeTierWebAppOvfFile = "3tier-webapp.ovf.xml";
	private String threeTierWebAppOvfString;
	private String threeTierWebAppPublicIPsOVFFile = "3tier-webapp-public-ips.ovf.xml";
	private String threeTierWebAppPublicIPsOVFString;
	private String threeTierWebAppDEMOOvfFile = "3tier-webapp.ovf.vmc.xml";
	private String threeTierWebAppDEMOOvfString;

	
	/**
	 * We just read an ovf example... 
	 * @throws IOException
	 * @throws URISyntaxException
	 */
	@Before
	public void setup() throws IOException, URISyntaxException {
		File file = new File(this.getClass().getResource( "/" + threeTierWebAppOvfFile ).toURI());		
		threeTierWebAppOvfString = readFile(file.getAbsolutePath(), StandardCharsets.UTF_8);
		
		// Reading the OVF file with DEMO tags...
		file = new File(this.getClass().getResource( "/" + threeTierWebAppDEMOOvfFile ).toURI());		
		threeTierWebAppDEMOOvfString = readFile(file.getAbsolutePath(), StandardCharsets.UTF_8);
		
		// Reading the OVF file with Public IPs/Floating IPS
		file = new File(this.getClass().getResource( "/" + threeTierWebAppPublicIPsOVFFile ).toURI());		
		threeTierWebAppPublicIPsOVFString = readFile(file.getAbsolutePath(), StandardCharsets.UTF_8);
		
		// Reading ovf file with max and min CPU
		file = new File(this.getClass().getResource( "/" + threeTierWebAppWithMaxMinCPUFile ).toURI());		
		threeTierWebAppWithMaxMinCPU = readFile(file.getAbsolutePath(), StandardCharsets.UTF_8);
	}

	@Test
	public void testWrongStateDoesNothing() {
		DeploymentDAO deploymentDAO = mock(DeploymentDAO.class);
		DeploymentEventService deploymentEventService = mock(DeploymentEventService.class);
		
		DeployEventHandler deploymentEventHandler = new DeployEventHandler();
		deploymentEventHandler.deploymentDAO = deploymentDAO;
		deploymentEventHandler.deploymentEventService = deploymentEventService;
		
		DeploymentEvent deploymentEvent = new DeploymentEvent();
		deploymentEvent.setDeploymentId(22);
		deploymentEvent.setDeploymentStatus("1111");
		
		deploymentEventHandler.deployDeployment(Event.wrap(deploymentEvent));
		
		verify(deploymentDAO, never()).getById(deploymentEvent.getDeploymentId());
		verify(deploymentDAO, never()).update(any(Deployment.class));
		verify(deploymentEventService, never()).fireDeploymentEvent(any(DeploymentEvent.class));
	}
	
	@Test
	public void invalidVMManager() throws Exception {

		Deployment deployment = new Deployment();
		deployment.setId(22);
		deployment.setOvf(threeTierWebAppOvfString);
		
		// We set a listener to get the sent message from the MessageQueue
		AmqpMessageReceiver receiver = new AmqpMessageReceiver(Configuration.amqpAddress, Configuration.amqpUsername, Configuration.amqpPassword,  "APPLICATION.>", true);
		AmqpListListener listener = new AmqpListListener();
		receiver.setMessageConsumer(listener);
		
		// We mock the calls to the PRClient
		Configuration.providerRegistryEndpoint = "http://provider-registry.com";
		PRClient prClient = mock(PRClient.class);
		when(prClient.getVMMClient(-1)).thenReturn(null);
		
		DeploymentDAO deploymentDAO = mock(DeploymentDAO.class);
		
		when(deploymentDAO.getById(22)).thenReturn(deployment);
		
		// The test starts here
		DeploymentEventService deploymentEventService = mock(DeploymentEventService.class);
		
		DeploymentEvent deploymentEvent = new DeploymentEvent();
		deploymentEvent.setDeploymentId(22);
		deploymentEvent.setApplicationName("pepito");
		deploymentEvent.setDeploymentStatus(Dictionary.APPLICATION_STATUS_CONTEXTUALIZED);
		
		// We configure de DeployEventHandler
		DeployEventHandler deploymentEventHandler = new DeployEventHandler();
		deploymentEventHandler.deploymentDAO = deploymentDAO;
		deploymentEventHandler.deploymentEventService = deploymentEventService;
		deploymentEventHandler.prClient = prClient;
				
		deploymentEventHandler.deployDeployment(Event.wrap(deploymentEvent));
				
		// We give time to the new thread to run... 
		Thread.sleep(4000l);
				
		// We verify that at the end we store in the database the right object
		ArgumentCaptor<Deployment> deploymentCaptor = ArgumentCaptor.forClass(Deployment.class);
		verify(deploymentDAO, times(1)).update(deploymentCaptor.capture());
		assertEquals(Dictionary.APPLICATION_STATUS_ERROR, deploymentCaptor.getValue().getStatus());
		
		// We verify that the right messages were sent to the AMQP
		assertEquals(1, listener.getTextMessages().size());
				
		assertEquals("APPLICATION.pepito.DEPLOYMENT.22.ERROR", listener.getTextMessages().get(0).getJMSDestination().toString());
		assertEquals("pepito", ModelConverter.jsonToApplicationManagerMessage(listener.getTextMessages().get(0).getText()).getApplicationId());
		assertEquals("22", ModelConverter.jsonToApplicationManagerMessage(listener.getTextMessages().get(0).getText()).getDeploymentId());
		assertEquals("ERROR", ModelConverter.jsonToApplicationManagerMessage(listener.getTextMessages().get(0).getText()).getStatus());
				
						
		receiver.close();
	}
	
	@Test
	public void testWithMaxAndMinCPU() throws Exception {
		Deployment deployment = new Deployment();
		deployment.setId(22);
		deployment.setOvf(threeTierWebAppWithMaxMinCPU);
		
		VmManagerClient vmMaClient = mock(VmManagerClient.class);

		setupVMMClientMock2(vmMaClient);
		

		verifyDeployVMsWithMaxAndMin(deployment, vmMaClient);
	}
	
	private void verifyDeployVMsWithMaxAndMin(Deployment deployment, VmManagerClient vmMaClient) throws Exception {
		
		// We set a listener to get the sent message from the MessageQueue
		AmqpMessageReceiver receiver = new AmqpMessageReceiver(Configuration.amqpAddress, Configuration.amqpUsername, Configuration.amqpPassword,  "APPLICATION.>", true);
		AmqpListListener listener = new AmqpListListener();
		receiver.setMessageConsumer(listener);
		
		DeploymentDAO deploymentDAO = mock(DeploymentDAO.class);
		
		when(deploymentDAO.getById(22)).thenReturn(deployment);
		
		// Application DAO mock
		ApplicationDAO applicationDAO = mock(ApplicationDAO.class);
		
		Application application = new Application();
		application.setName("threeTierWebApp");
		
		when(applicationDAO.getByName("threeTierWebApp")).thenReturn(application);
		when(applicationDAO.update(application)).thenReturn(true);

		// The object will be updated in the database
		when(deploymentDAO.update(deployment)).thenReturn(true);
		
		//We mock the VM DAO
		VMDAO vmDAO = mock(VMDAO.class);
		when(vmDAO.save((VM) any())).thenReturn(true);
		
		// We mock the class to the VmManagerClient
		when(vmMaClient.uploadImage(eq(new ImageToUploadWithEquals("haproxy.img","/DFS/ascetic/vm-images/threeTierWebApp/haproxy.img")))).thenReturn("haproxy-uuid");
		when(vmMaClient.uploadImage(eq(new ImageToUploadWithEquals("jboss.img","/DFS/ascetic/vm-images/threeTierWebApp/jboss.img")))).thenReturn("jboss-uuid");
		when(vmMaClient.uploadImage(eq(new ImageToUploadWithEquals("mysql.img","/DFS/ascetic/vm-images/threeTierWebApp/mysql.img")))).thenReturn("mysql-uuid");
		when(vmMaClient.uploadImage(eq(new ImageToUploadWithEquals("jmeter.img","/DFS/ascetic/vm-images/threeTierWebApp/jmeter.img")))).thenReturn("jmeter-uuid");
		
		//We mock the image DAO
		ImageDAO imageDAO = mock(ImageDAO.class);
		Image image1 = new Image();
		image1.setOvfId("haproxy-img");
		image1.setOvfHref("/DFS/ascetic/vm-images/threeTierWebApp/haproxy.img");
		image1.setProviderImageId("haproxy-uuid");
		when(imageDAO.save(eq(image1))).thenReturn(true);
		when(imageDAO.getById(0)).thenReturn(image1, image1, image1, image1);
		
		// We mock the calls to get VMs
		when(vmMaClient.getVM("haproxy-vm1")).thenReturn(new VmDeployed("haproxyVM", "haproxy-img", 1, 2, 3, 0, "", "", "", "10.0.0.1", "ACTIVE", new Date(), ""));
		when(vmMaClient.getVM("jboss-vm1")).thenReturn(new VmDeployed("jbossVM", "jboss-img", 1, 2, 3,  0, "", "", "", "10.0.0.2", "ACTIVE", new Date(), ""));
		when(vmMaClient.getVM("mysql-vm1")).thenReturn(new VmDeployed("mysqlVM", "mysql-img", 1, 2, 3,  0, "", "", "", "10.0.0.3", "ACTIVE", new Date(), ""));
		when(vmMaClient.getVM("jmeter-vm1")).thenReturn(new VmDeployed("jmeterVM", "jmeter-img", 1, 2, 3,  0, "", "", "", "10.0.0.4", "ACTIVE", new Date(), ""));
		
		// We mock the calls to the PRClient
		Configuration.providerRegistryEndpoint = "http://provider-registry.com";
		PRClient prClient = mock(PRClient.class);
		when(prClient.getVMMClient(-1)).thenReturn(vmMaClient);
		
		// EnergyModeller Mock
		EnergyModellerBean em = mock(EnergyModellerBean.class);
		
		// The test starts here
		DeploymentEventService deploymentEventService = mock(DeploymentEventService.class);
		
		DeploymentEvent deploymentEvent = new DeploymentEvent();
		deploymentEvent.setDeploymentId(22);
		deploymentEvent.setApplicationName(application.getName());
		deploymentEvent.setDeploymentStatus(Dictionary.APPLICATION_STATUS_CONTEXTUALIZED);
		
		
		// We configure de DeployEventHandler
		DeployEventHandler deploymentEventHandler = new DeployEventHandler();
		deploymentEventHandler.deploymentDAO = deploymentDAO;
		deploymentEventHandler.deploymentEventService = deploymentEventService;
		deploymentEventHandler.applicationDAO = applicationDAO;
		deploymentEventHandler.imageDAO = imageDAO;
		deploymentEventHandler.prClient = prClient;
		deploymentEventHandler.vmDAO = vmDAO;
		deploymentEventHandler.em = em;
		
		//We start the task
		deploymentEventHandler.deployDeployment(Event.wrap(deploymentEvent));
		
		// We give time to the new thread to run... 
		Thread.sleep(4000l);
		
		// We verify the mock calls
		//verify(vmMaClient, times(1)).uploadImage(imageToUploadWithArguments("haproxy.img","/DFS/ascetic/vm-images/threeTierWebApp/haproxy.img"));
		verify(imageDAO, times(1)).save(eq(image1));
		verify(vmMaClient, times(1)).getVM("haproxy-vm1");
		verify(vmMaClient, times(1)).getVM("jboss-vm1");
		verify(vmMaClient, times(1)).getVM("mysql-vm1");
		verify(vmMaClient, times(1)).getVM("jmeter-vm1");
		
		verify(vmDAO, times(4)).save((VM) any());
		
		//Verify EM
		//verifyEm(application.getName(), em);
		
		// We verify that at the end we store in the database the right object
		ArgumentCaptor<Deployment> deploymentCaptor = ArgumentCaptor.forClass(Deployment.class);
		verify(deploymentDAO, times(6)).update(deploymentCaptor.capture());
		assertEquals(4, deploymentCaptor.getValue().getVms().size());
		assertEquals("haproxy", deploymentCaptor.getValue().getVms().get(0).getOvfId());
		assertEquals("10.0.0.1", deploymentCaptor.getValue().getVms().get(0).getIp());
		assertEquals("haproxy-vm1", deploymentCaptor.getValue().getVms().get(0).getProviderVmId());
		assertEquals(4, deploymentCaptor.getValue().getVms().get(0).getCpuMax());
		assertEquals(1, deploymentCaptor.getValue().getVms().get(0).getCpuMin());
		assertEquals("ACTIVE", deploymentCaptor.getValue().getVms().get(0).getStatus());
		assertEquals(1, deploymentCaptor.getValue().getVms().get(0).getImages().size());
		assertEquals("/DFS/ascetic/vm-images/threeTierWebApp/haproxy.img", deploymentCaptor.getValue().getVms().get(0).getImages().get(0).getOvfHref());
		assertEquals("haproxy-img", deploymentCaptor.getValue().getVms().get(0).getImages().get(0).getOvfId());
		assertEquals("haproxy-uuid", deploymentCaptor.getValue().getVms().get(0).getImages().get(0).getProviderImageId());
		
		assertEquals("jboss", deploymentCaptor.getValue().getVms().get(1).getOvfId());
		assertEquals("10.0.0.2", deploymentCaptor.getValue().getVms().get(1).getIp());
		assertEquals("jboss-vm1", deploymentCaptor.getValue().getVms().get(1).getProviderVmId());
		assertEquals("ACTIVE", deploymentCaptor.getValue().getVms().get(1).getStatus());
		assertEquals(1, deploymentCaptor.getValue().getVms().get(1).getCpuMax());
		assertEquals(1, deploymentCaptor.getValue().getVms().get(1).getCpuMin());
		assertEquals(1, deploymentCaptor.getValue().getVms().get(1).getImages().size());
		assertEquals("/DFS/ascetic/vm-images/threeTierWebApp/haproxy.img", deploymentCaptor.getValue().getVms().get(1).getImages().get(0).getOvfHref());
		assertEquals("haproxy-img", deploymentCaptor.getValue().getVms().get(1).getImages().get(0).getOvfId());
		assertEquals("haproxy-uuid", deploymentCaptor.getValue().getVms().get(1).getImages().get(0).getProviderImageId());
		
		assertEquals("mysql", deploymentCaptor.getValue().getVms().get(2).getOvfId());
		assertEquals("10.0.0.3", deploymentCaptor.getValue().getVms().get(2).getIp());
		assertEquals("mysql-vm1", deploymentCaptor.getValue().getVms().get(2).getProviderVmId());
		assertEquals("ACTIVE", deploymentCaptor.getValue().getVms().get(2).getStatus());
		assertEquals(1, deploymentCaptor.getValue().getVms().get(2).getCpuMax());
		assertEquals(1, deploymentCaptor.getValue().getVms().get(2).getCpuMin());
		assertEquals(1, deploymentCaptor.getValue().getVms().get(2).getImages().size());
		assertEquals(1, deploymentCaptor.getValue().getVms().get(0).getImages().size());
		assertEquals("/DFS/ascetic/vm-images/threeTierWebApp/haproxy.img", deploymentCaptor.getValue().getVms().get(2).getImages().get(0).getOvfHref());
		assertEquals("haproxy-img", deploymentCaptor.getValue().getVms().get(2).getImages().get(0).getOvfId());
		assertEquals("haproxy-uuid", deploymentCaptor.getValue().getVms().get(2).getImages().get(0).getProviderImageId());
		
		assertEquals("jmeter", deploymentCaptor.getValue().getVms().get(3).getOvfId());
		assertEquals("10.0.0.4", deploymentCaptor.getValue().getVms().get(3).getIp());
		assertEquals("jmeter-vm1", deploymentCaptor.getValue().getVms().get(3).getProviderVmId());
		assertEquals("ACTIVE", deploymentCaptor.getValue().getVms().get(3).getStatus());
		assertEquals(1, deploymentCaptor.getValue().getVms().get(3).getImages().size());
		assertEquals(1, deploymentCaptor.getValue().getVms().get(0).getImages().size());
		assertEquals("/DFS/ascetic/vm-images/threeTierWebApp/haproxy.img", deploymentCaptor.getValue().getVms().get(3).getImages().get(0).getOvfHref());
		assertEquals("haproxy-img", deploymentCaptor.getValue().getVms().get(3).getImages().get(0).getOvfId());
		assertEquals("haproxy-uuid", deploymentCaptor.getValue().getVms().get(3).getImages().get(0).getProviderImageId());
		
		verify(applicationDAO, times(4)).getByName("threeTierWebApp");
		//verify(applicationDAO, times(4)).update(application);
		
		// We verify that we send the right internal message at the end
		ArgumentCaptor<DeploymentEvent> argument = ArgumentCaptor.forClass(DeploymentEvent.class);
		verify(deploymentEventService).fireDeploymentEvent(argument.capture());
		
		assertEquals(22, argument.getValue().getDeploymentId());
		assertEquals(Dictionary.APPLICATION_STATUS_DEPLOYED, argument.getValue().getDeploymentStatus());
		
		// We verify that the right messages were sent to the AMQP
		assertEquals(6, listener.getTextMessages().size());
		
		assertEquals("APPLICATION.threeTierWebApp.DEPLOYMENT.22.DEPLOYING", listener.getTextMessages().get(0).getJMSDestination().toString());
		assertEquals("threeTierWebApp", ModelConverter.jsonToApplicationManagerMessage(listener.getTextMessages().get(0).getText()).getApplicationId());
		assertEquals("22", ModelConverter.jsonToApplicationManagerMessage(listener.getTextMessages().get(0).getText()).getDeploymentId());
		assertEquals("DEPLOYING", ModelConverter.jsonToApplicationManagerMessage(listener.getTextMessages().get(0).getText()).getStatus());
		
		assertEquals("APPLICATION.threeTierWebApp.DEPLOYMENT.22.VM.0.DEPLOYED", listener.getTextMessages().get(1).getJMSDestination().toString());
		assertEquals("threeTierWebApp", ModelConverter.jsonToApplicationManagerMessage(listener.getTextMessages().get(1).getText()).getApplicationId());
		assertEquals("22", ModelConverter.jsonToApplicationManagerMessage(listener.getTextMessages().get(1).getText()).getDeploymentId());
		assertEquals("DEPLOYING", ModelConverter.jsonToApplicationManagerMessage(listener.getTextMessages().get(1).getText()).getStatus());
		assertEquals("haproxy", ModelConverter.jsonToApplicationManagerMessage(listener.getTextMessages().get(1).getText()).getVms().get(0).getOvfId());
		
		assertEquals("APPLICATION.threeTierWebApp.DEPLOYMENT.22.VM.0.DEPLOYED", listener.getTextMessages().get(2).getJMSDestination().toString());
		assertEquals("threeTierWebApp", ModelConverter.jsonToApplicationManagerMessage(listener.getTextMessages().get(2).getText()).getApplicationId());
		assertEquals("22", ModelConverter.jsonToApplicationManagerMessage(listener.getTextMessages().get(2).getText()).getDeploymentId());
		assertEquals("DEPLOYING", ModelConverter.jsonToApplicationManagerMessage(listener.getTextMessages().get(1).getText()).getStatus());
		assertEquals("jboss", ModelConverter.jsonToApplicationManagerMessage(listener.getTextMessages().get(2).getText()).getVms().get(0).getOvfId());
		
		assertEquals("APPLICATION.threeTierWebApp.DEPLOYMENT.22.VM.0.DEPLOYED", listener.getTextMessages().get(3).getJMSDestination().toString());
		assertEquals("threeTierWebApp", ModelConverter.jsonToApplicationManagerMessage(listener.getTextMessages().get(3).getText()).getApplicationId());
		assertEquals("22", ModelConverter.jsonToApplicationManagerMessage(listener.getTextMessages().get(3).getText()).getDeploymentId());
		assertEquals("DEPLOYING", ModelConverter.jsonToApplicationManagerMessage(listener.getTextMessages().get(3).getText()).getStatus());
		assertEquals("mysql", ModelConverter.jsonToApplicationManagerMessage(listener.getTextMessages().get(3).getText()).getVms().get(0).getOvfId());
		
		assertEquals("APPLICATION.threeTierWebApp.DEPLOYMENT.22.VM.0.DEPLOYED", listener.getTextMessages().get(4).getJMSDestination().toString());
		assertEquals("threeTierWebApp", ModelConverter.jsonToApplicationManagerMessage(listener.getTextMessages().get(4).getText()).getApplicationId());
		assertEquals("22", ModelConverter.jsonToApplicationManagerMessage(listener.getTextMessages().get(4).getText()).getDeploymentId());
		assertEquals("DEPLOYING", ModelConverter.jsonToApplicationManagerMessage(listener.getTextMessages().get(4).getText()).getStatus());
		assertEquals("jmeter", ModelConverter.jsonToApplicationManagerMessage(listener.getTextMessages().get(4).getText()).getVms().get(0).getOvfId());
		
		assertEquals("APPLICATION.threeTierWebApp.DEPLOYMENT.22.DEPLOYED", listener.getTextMessages().get(5).getJMSDestination().toString());
		assertEquals("threeTierWebApp", ModelConverter.jsonToApplicationManagerMessage(listener.getTextMessages().get(5).getText()).getApplicationId());
		assertEquals("22", ModelConverter.jsonToApplicationManagerMessage(listener.getTextMessages().get(5).getText()).getDeploymentId());
		assertEquals("DEPLOYED", ModelConverter.jsonToApplicationManagerMessage(listener.getTextMessages().get(5).getText()).getStatus());
		
		receiver.close();
	}
	
	@Test
	public void testDeployVmsCreatingImages() throws Exception {

		Deployment deployment = new Deployment();
		deployment.setId(22);
		deployment.setOvf(threeTierWebAppOvfString);
		
		VmManagerClient vmMaClient = mock(VmManagerClient.class);

		setupVMMClientMock(vmMaClient);

		
		verifyDeployVMsWithoutDemoImages(deployment, vmMaClient);
	}
	
	private void setupVMMClientMock2(VmManagerClient vmMaClient) {
		List<Vm> vms1 = new ArrayList<Vm>();
		VmWithEquals vm1 = new VmWithEquals("HAProxy_1","haproxy-uuid",1,512,20, 0,"/DFS/ascetic/vm-images/threeTierWebApp/haproxy.iso_1","threeTierWebApp", "haproxy", "sla-id", false);
		vms1.add(vm1);
		List<String> ids1 = new ArrayList<String>();
		ids1.add("haproxy-vm1");
		
		when(vmMaClient.deployVMs(eq(vms1))).thenReturn(ids1);
		
		List<Vm> vms2 = new ArrayList<Vm>();
		VmWithEquals vm2 = new VmWithEquals("Jboss_1","haproxy-uuid",1,2048,20, 0, "/DFS/ascetic/vm-images/threeTierWebApp/jboss.iso_1","threeTierWebApp", "jboss", "sla-id", false);
		vms2.add(vm2);
		List<String> ids2 = new ArrayList<String>();
		ids2.add("jboss-vm1");
		
		when(vmMaClient.deployVMs(eq(vms2))).thenReturn(ids2);
		
		List<Vm> vms3 = new ArrayList<Vm>();
		VmWithEquals vm3 = new VmWithEquals("MySQL_1","haproxy-uuid",1,1024,20, 0, "/DFS/ascetic/vm-images/threeTierWebApp/mysql.iso_1","threeTierWebApp", "mysql", "", false);
		vms3.add(vm3);
		List<String> ids3 = new ArrayList<String>();
		ids3.add("mysql-vm1");
		
		when(vmMaClient.deployVMs(eq(vms3))).thenReturn(ids3);
		
		List<Vm> vms4 = new ArrayList<Vm>();
		VmWithEquals vm4 = new VmWithEquals("JMeter_1","haproxy-uuid",1,1024,20, 0, "/DFS/ascetic/vm-images/threeTierWebApp/jmeter.iso_1","threeTierWebApp", "jmeter", "sla-id", false);
		vms4.add(vm4);
		List<String> ids4 = new ArrayList<String>();
		ids4.add("jmeter-vm1");
		
		when(vmMaClient.deployVMs(eq(vms4))).thenReturn(ids4);	
	}
	
	private void setupVMMClientMock(VmManagerClient vmMaClient) {
		List<Vm> vms1 = new ArrayList<Vm>();
		VmWithEquals vm1 = new VmWithEquals("HAProxy_1","haproxy-uuid",1,512,20, 0,"","threeTierWebApp", "haproxy", "sla-id", false);
		vms1.add(vm1);
		List<String> ids1 = new ArrayList<String>();
		ids1.add("haproxy-vm1");
		
		when(vmMaClient.deployVMs(eq(vms1))).thenReturn(ids1);
		
		List<Vm> vms2 = new ArrayList<Vm>();
		VmWithEquals vm2 = new VmWithEquals("Jboss_1","jboss-uuid",1,2048,20, 0, "","threeTierWebApp", "jboss", "sla-id", false);
		vms2.add(vm2);
		List<String> ids2 = new ArrayList<String>();
		ids2.add("jboss-vm1");
		
		when(vmMaClient.deployVMs(eq(vms2))).thenReturn(ids2);
		
		List<Vm> vms3 = new ArrayList<Vm>();
		VmWithEquals vm3 = new VmWithEquals("MySQL_1","mysql-uuid",1,512,20, 0, "","threeTierWebApp", "mysql", "", false);
		vms3.add(vm3);
		List<String> ids3 = new ArrayList<String>();
		ids3.add("mysql-vm1");
		
		when(vmMaClient.deployVMs(eq(vms3))).thenReturn(ids3);
		
		List<Vm> vms4 = new ArrayList<Vm>();
		VmWithEquals vm4 = new VmWithEquals("JMeter_1","jmeter-uuid",1,512,20, 0, "","threeTierWebApp", "jmeter", "sla-id", false);
		vms4.add(vm4);
		List<String> ids4 = new ArrayList<String>();
		ids4.add("jmeter-vm1");
		
		when(vmMaClient.deployVMs(eq(vms4))).thenReturn(ids4);	
	}
	
	private void verifyDeployVMsWithoutDemoImages(Deployment deployment, VmManagerClient vmMaClient) throws Exception {
		
		// We set a listener to get the sent message from the MessageQueue
		AmqpMessageReceiver receiver = new AmqpMessageReceiver(Configuration.amqpAddress, Configuration.amqpUsername, Configuration.amqpPassword,  "APPLICATION.>", true);
		AmqpListListener listener = new AmqpListListener();
		receiver.setMessageConsumer(listener);
		
		DeploymentDAO deploymentDAO = mock(DeploymentDAO.class);
		
		when(deploymentDAO.getById(22)).thenReturn(deployment);
		
		// Application DAO mock
		ApplicationDAO applicationDAO = mock(ApplicationDAO.class);
		
		Application application = new Application();
		application.setName("threeTierWebApp");
		
		when(applicationDAO.getByName("threeTierWebApp")).thenReturn(application);
		when(applicationDAO.update(application)).thenReturn(true);

		// The object will be updated in the database
		when(deploymentDAO.update(deployment)).thenReturn(true);
		
		//We mock the VM DAO
		VMDAO vmDAO = mock(VMDAO.class);
		when(vmDAO.save((VM) any())).thenReturn(true);
		
		// We mock the class to the VmManagerClient
		when(vmMaClient.uploadImage(eq(new ImageToUploadWithEquals("haproxy.img","/DFS/ascetic/vm-images/threeTierWebApp/haproxy.img")))).thenReturn("haproxy-uuid");
		when(vmMaClient.uploadImage(eq(new ImageToUploadWithEquals("jboss.img","/DFS/ascetic/vm-images/threeTierWebApp/jboss.img")))).thenReturn("jboss-uuid");
		when(vmMaClient.uploadImage(eq(new ImageToUploadWithEquals("mysql.img","/DFS/ascetic/vm-images/threeTierWebApp/mysql.img")))).thenReturn("mysql-uuid");
		when(vmMaClient.uploadImage(eq(new ImageToUploadWithEquals("jmeter.img","/DFS/ascetic/vm-images/threeTierWebApp/jmeter.img")))).thenReturn("jmeter-uuid");
		
		//We mock the image DAO
		ImageDAO imageDAO = mock(ImageDAO.class);
		Image image1 = new Image();
		image1.setOvfId("haproxy-img");
		image1.setOvfHref("/DFS/ascetic/vm-images/threeTierWebApp/haproxy.img");
		image1.setProviderImageId("haproxy-uuid");
		when(imageDAO.save(eq(image1))).thenReturn(true);
		Image image2 = new Image();
		image2.setOvfId("jboss-img");
		image2.setOvfHref("/DFS/ascetic/vm-images/threeTierWebApp/jboss.img");
		image2.setProviderImageId("jboss-uuid");
		when(imageDAO.save(eq(image2))).thenReturn(true);
		Image image3 = new Image();
		image3.setOvfId("mysql-img");
		image3.setOvfHref("/DFS/ascetic/vm-images/threeTierWebApp/mysql.img");
		image3.setProviderImageId("mysql-uuid");
		when(imageDAO.save(eq(image3))).thenReturn(true);
		Image image4 = new Image();
		image4.setOvfId("jmeter-img");
		image4.setOvfHref("/DFS/ascetic/vm-images/threeTierWebApp/jmeter.img");
		image4.setProviderImageId("jmeter-uuid");
		when(imageDAO.save(eq(image4))).thenReturn(true);
		when(imageDAO.getById(0)).thenReturn(image1, image2, image3, image4);
		
		// We mock the calls to get VMs
		when(vmMaClient.getVM("haproxy-vm1")).thenReturn(new VmDeployed("haproxyVM", "haproxy-img", 1, 2, 3, 0, "", "", "", "10.0.0.1", "ACTIVE", new Date(), ""));
		when(vmMaClient.getVM("jboss-vm1")).thenReturn(new VmDeployed("jbossVM", "jboss-img", 1, 2, 3,  0, "", "", "", "10.0.0.2", "ACTIVE", new Date(), ""));
		when(vmMaClient.getVM("mysql-vm1")).thenReturn(new VmDeployed("mysqlVM", "mysql-img", 1, 2, 3,  0, "", "", "", "10.0.0.3", "ACTIVE", new Date(), ""));
		when(vmMaClient.getVM("jmeter-vm1")).thenReturn(new VmDeployed("jmeterVM", "jmeter-img", 1, 2, 3,  0, "", "", "", "10.0.0.4", "ACTIVE", new Date(), ""));
		
		// We mock the calls to the PRClient
		Configuration.providerRegistryEndpoint = "http://provider-registry.com";
		PRClient prClient = mock(PRClient.class);
		when(prClient.getVMMClient(-1)).thenReturn(vmMaClient);
		
		// Energy modeller mock
		EnergyModellerBean em = mock(EnergyModellerBean.class);
		
		// The test starts here
		DeploymentEventService deploymentEventService = mock(DeploymentEventService.class);
		
		DeploymentEvent deploymentEvent = new DeploymentEvent();
		deploymentEvent.setDeploymentId(22);
		deploymentEvent.setApplicationName(application.getName());
		deploymentEvent.setDeploymentStatus(Dictionary.APPLICATION_STATUS_CONTEXTUALIZED);
		
		// We configure de DeployEventHandler
		DeployEventHandler deploymentEventHandler = new DeployEventHandler();
		deploymentEventHandler.deploymentDAO = deploymentDAO;
		deploymentEventHandler.deploymentEventService = deploymentEventService;
		deploymentEventHandler.applicationDAO = applicationDAO;
		deploymentEventHandler.imageDAO = imageDAO;
		deploymentEventHandler.prClient = prClient;
		deploymentEventHandler.vmDAO = vmDAO;
		deploymentEventHandler.em = em;
		
		//We start the task
		deploymentEventHandler.deployDeployment(Event.wrap(deploymentEvent));
		
		// We give time to the new thread to run... 
		Thread.sleep(4000l);
		
		// We verify the mock calls
		//verify(vmMaClient, times(1)).uploadImage(imageToUploadWithArguments("haproxy.img","/DFS/ascetic/vm-images/threeTierWebApp/haproxy.img"));
		verify(imageDAO, times(1)).save(eq(image1));
		verify(imageDAO, times(1)).save(eq(image2));
		verify(imageDAO, times(1)).save(eq(image3));
		verify(imageDAO, times(1)).save(eq(image4));
		verify(vmMaClient, times(1)).getVM("haproxy-vm1");
		verify(vmMaClient, times(1)).getVM("jboss-vm1");
		verify(vmMaClient, times(1)).getVM("mysql-vm1");
		verify(vmMaClient, times(1)).getVM("jmeter-vm1");
		
		verify(vmDAO, times(4)).save((VM) any());
		
		// We verify the mock calss to em
		//verifyEm(application.getName(), em);
		
		// We verify that at the end we store in the database the right object
		ArgumentCaptor<Deployment> deploymentCaptor = ArgumentCaptor.forClass(Deployment.class);
		verify(deploymentDAO, times(6)).update(deploymentCaptor.capture());
		assertEquals(4, deploymentCaptor.getValue().getVms().size());
		assertEquals("haproxy", deploymentCaptor.getValue().getVms().get(0).getOvfId());
		assertEquals("10.0.0.1", deploymentCaptor.getValue().getVms().get(0).getIp());
		assertEquals("haproxy-vm1", deploymentCaptor.getValue().getVms().get(0).getProviderVmId());
		assertEquals("ACTIVE", deploymentCaptor.getValue().getVms().get(0).getStatus());
		assertEquals(1, deploymentCaptor.getValue().getVms().get(0).getImages().size());
		assertEquals("/DFS/ascetic/vm-images/threeTierWebApp/haproxy.img", deploymentCaptor.getValue().getVms().get(0).getImages().get(0).getOvfHref());
		assertEquals("haproxy-img", deploymentCaptor.getValue().getVms().get(0).getImages().get(0).getOvfId());
		assertEquals("haproxy-uuid", deploymentCaptor.getValue().getVms().get(0).getImages().get(0).getProviderImageId());
		
		assertEquals("jboss", deploymentCaptor.getValue().getVms().get(1).getOvfId());
		assertEquals("10.0.0.2", deploymentCaptor.getValue().getVms().get(1).getIp());
		assertEquals("jboss-vm1", deploymentCaptor.getValue().getVms().get(1).getProviderVmId());
		assertEquals("ACTIVE", deploymentCaptor.getValue().getVms().get(1).getStatus());
		assertEquals(1, deploymentCaptor.getValue().getVms().get(1).getImages().size());
		assertEquals("/DFS/ascetic/vm-images/threeTierWebApp/jboss.img", deploymentCaptor.getValue().getVms().get(1).getImages().get(0).getOvfHref());
		assertEquals("jboss-img", deploymentCaptor.getValue().getVms().get(1).getImages().get(0).getOvfId());
		assertEquals("jboss-uuid", deploymentCaptor.getValue().getVms().get(1).getImages().get(0).getProviderImageId());
		
		assertEquals("mysql", deploymentCaptor.getValue().getVms().get(2).getOvfId());
		assertEquals("10.0.0.3", deploymentCaptor.getValue().getVms().get(2).getIp());
		assertEquals("mysql-vm1", deploymentCaptor.getValue().getVms().get(2).getProviderVmId());
		assertEquals("ACTIVE", deploymentCaptor.getValue().getVms().get(2).getStatus());
		assertEquals(1, deploymentCaptor.getValue().getVms().get(2).getImages().size());
		assertEquals(1, deploymentCaptor.getValue().getVms().get(0).getImages().size());
		assertEquals("/DFS/ascetic/vm-images/threeTierWebApp/mysql.img", deploymentCaptor.getValue().getVms().get(2).getImages().get(0).getOvfHref());
		assertEquals("mysql-img", deploymentCaptor.getValue().getVms().get(2).getImages().get(0).getOvfId());
		assertEquals("mysql-uuid", deploymentCaptor.getValue().getVms().get(2).getImages().get(0).getProviderImageId());
		
		assertEquals("jmeter", deploymentCaptor.getValue().getVms().get(3).getOvfId());
		assertEquals("10.0.0.4", deploymentCaptor.getValue().getVms().get(3).getIp());
		assertEquals("jmeter-vm1", deploymentCaptor.getValue().getVms().get(3).getProviderVmId());
		assertEquals("ACTIVE", deploymentCaptor.getValue().getVms().get(3).getStatus());
		assertEquals(1, deploymentCaptor.getValue().getVms().get(3).getImages().size());
		assertEquals(1, deploymentCaptor.getValue().getVms().get(0).getImages().size());
		assertEquals("/DFS/ascetic/vm-images/threeTierWebApp/jmeter.img", deploymentCaptor.getValue().getVms().get(3).getImages().get(0).getOvfHref());
		assertEquals("jmeter-img", deploymentCaptor.getValue().getVms().get(3).getImages().get(0).getOvfId());
		assertEquals("jmeter-uuid", deploymentCaptor.getValue().getVms().get(3).getImages().get(0).getProviderImageId());
		
		verify(applicationDAO, times(4)).getByName("threeTierWebApp");
		//verify(applicationDAO, times(4)).update(application);
		
		// We verify that we send the right internal message at the end
		ArgumentCaptor<DeploymentEvent> argument = ArgumentCaptor.forClass(DeploymentEvent.class);
		verify(deploymentEventService).fireDeploymentEvent(argument.capture());
		
		assertEquals(22, argument.getValue().getDeploymentId());
		assertEquals(Dictionary.APPLICATION_STATUS_DEPLOYED, argument.getValue().getDeploymentStatus());
		
		// We verify that the right messages were sent to the AMQP
		assertEquals(6, listener.getTextMessages().size());
		
		assertEquals("APPLICATION.threeTierWebApp.DEPLOYMENT.22.DEPLOYING", listener.getTextMessages().get(0).getJMSDestination().toString());
		assertEquals("threeTierWebApp", ModelConverter.jsonToApplicationManagerMessage(listener.getTextMessages().get(0).getText()).getApplicationId());
		assertEquals("22", ModelConverter.jsonToApplicationManagerMessage(listener.getTextMessages().get(0).getText()).getDeploymentId());
		assertEquals("DEPLOYING", ModelConverter.jsonToApplicationManagerMessage(listener.getTextMessages().get(0).getText()).getStatus());
		
		assertEquals("APPLICATION.threeTierWebApp.DEPLOYMENT.22.VM.0.DEPLOYED", listener.getTextMessages().get(1).getJMSDestination().toString());
		assertEquals("threeTierWebApp", ModelConverter.jsonToApplicationManagerMessage(listener.getTextMessages().get(1).getText()).getApplicationId());
		assertEquals("22", ModelConverter.jsonToApplicationManagerMessage(listener.getTextMessages().get(1).getText()).getDeploymentId());
		assertEquals("DEPLOYING", ModelConverter.jsonToApplicationManagerMessage(listener.getTextMessages().get(1).getText()).getStatus());
		assertEquals("haproxy", ModelConverter.jsonToApplicationManagerMessage(listener.getTextMessages().get(1).getText()).getVms().get(0).getOvfId());
		
		assertEquals("APPLICATION.threeTierWebApp.DEPLOYMENT.22.VM.0.DEPLOYED", listener.getTextMessages().get(2).getJMSDestination().toString());
		assertEquals("threeTierWebApp", ModelConverter.jsonToApplicationManagerMessage(listener.getTextMessages().get(2).getText()).getApplicationId());
		assertEquals("22", ModelConverter.jsonToApplicationManagerMessage(listener.getTextMessages().get(2).getText()).getDeploymentId());
		assertEquals("DEPLOYING", ModelConverter.jsonToApplicationManagerMessage(listener.getTextMessages().get(1).getText()).getStatus());
		assertEquals("jboss", ModelConverter.jsonToApplicationManagerMessage(listener.getTextMessages().get(2).getText()).getVms().get(0).getOvfId());
		
		assertEquals("APPLICATION.threeTierWebApp.DEPLOYMENT.22.VM.0.DEPLOYED", listener.getTextMessages().get(3).getJMSDestination().toString());
		assertEquals("threeTierWebApp", ModelConverter.jsonToApplicationManagerMessage(listener.getTextMessages().get(3).getText()).getApplicationId());
		assertEquals("22", ModelConverter.jsonToApplicationManagerMessage(listener.getTextMessages().get(3).getText()).getDeploymentId());
		assertEquals("DEPLOYING", ModelConverter.jsonToApplicationManagerMessage(listener.getTextMessages().get(3).getText()).getStatus());
		assertEquals("mysql", ModelConverter.jsonToApplicationManagerMessage(listener.getTextMessages().get(3).getText()).getVms().get(0).getOvfId());
		
		assertEquals("APPLICATION.threeTierWebApp.DEPLOYMENT.22.VM.0.DEPLOYED", listener.getTextMessages().get(4).getJMSDestination().toString());
		assertEquals("threeTierWebApp", ModelConverter.jsonToApplicationManagerMessage(listener.getTextMessages().get(4).getText()).getApplicationId());
		assertEquals("22", ModelConverter.jsonToApplicationManagerMessage(listener.getTextMessages().get(4).getText()).getDeploymentId());
		assertEquals("DEPLOYING", ModelConverter.jsonToApplicationManagerMessage(listener.getTextMessages().get(4).getText()).getStatus());
		assertEquals("jmeter", ModelConverter.jsonToApplicationManagerMessage(listener.getTextMessages().get(4).getText()).getVms().get(0).getOvfId());
		
		assertEquals("APPLICATION.threeTierWebApp.DEPLOYMENT.22.DEPLOYED", listener.getTextMessages().get(5).getJMSDestination().toString());
		assertEquals("threeTierWebApp", ModelConverter.jsonToApplicationManagerMessage(listener.getTextMessages().get(5).getText()).getApplicationId());
		assertEquals("22", ModelConverter.jsonToApplicationManagerMessage(listener.getTextMessages().get(5).getText()).getDeploymentId());
		assertEquals("DEPLOYED", ModelConverter.jsonToApplicationManagerMessage(listener.getTextMessages().get(5).getText()).getStatus());
		
		receiver.close();
	}
	
//	private void verifyEm(String appName, EnergyModellerBean em) {
//		verify(em, times(1)).notifyVMChangeInStatus("-1", appName, "22", "0", "haproxy-vm1", Dictionary.APPLICATION_STATUS_DEPLOYED);
//		verify(em, times(1)).notifyVMChangeInStatus("-1", appName, "22", "0", "jboss-vm1", Dictionary.APPLICATION_STATUS_DEPLOYED);
//		verify(em, times(1)).notifyVMChangeInStatus("-1", appName, "22", "0", "mysql-vm1", Dictionary.APPLICATION_STATUS_DEPLOYED);
//		verify(em, times(1)).notifyVMChangeInStatus("-1", appName, "22", "0", "jmeter-vm1", Dictionary.APPLICATION_STATUS_DEPLOYED);
//	}
	
	@Test
	public void testDeployVmsCreatingImagesWithPublicIPs() throws Exception {
		Deployment deployment = new Deployment();
		deployment.setId(22);
		deployment.setOvf(threeTierWebAppPublicIPsOVFString);
		
		VmManagerClient vmMaClient = mock(VmManagerClient.class);

		
		List<Vm> vms1 = new ArrayList<Vm>();
		VmWithEquals vm1 = new VmWithEquals("HAProxy_1","haproxy-uuid",1,512,20, 0,"","threeTierWebApp", "haproxy", "sla-id", true);
		vms1.add(vm1);
		List<String> ids1 = new ArrayList<String>();
		ids1.add("haproxy-vm1");
		
		when(vmMaClient.deployVMs(eq(vms1))).thenReturn(ids1);
		
		List<Vm> vms2 = new ArrayList<Vm>();
		VmWithEquals vm2 = new VmWithEquals("Jboss_1","jboss-uuid",1,2048,20, 0, "","threeTierWebApp", "jboss", "sla-id", false);
		vms2.add(vm2);
		List<String> ids2 = new ArrayList<String>();
		ids2.add("jboss-vm1");
		
		when(vmMaClient.deployVMs(eq(vms2))).thenReturn(ids2);
		
		List<Vm> vms3 = new ArrayList<Vm>();
		VmWithEquals vm3 = new VmWithEquals("MySQL_1","mysql-uuid",1,512,20, 0, "","threeTierWebApp", "mysql", "", false);
		vms3.add(vm3);
		List<String> ids3 = new ArrayList<String>();
		ids3.add("mysql-vm1");
		
		when(vmMaClient.deployVMs(eq(vms3))).thenReturn(ids3);
		
		List<Vm> vms4 = new ArrayList<Vm>();
		VmWithEquals vm4 = new VmWithEquals("JMeter_1","jmeter-uuid",1,512,20, 0, "","threeTierWebApp", "jmeter", "sla-id", false);
		vms4.add(vm4);
		List<String> ids4 = new ArrayList<String>();
		ids4.add("jmeter-vm1");
		
		when(vmMaClient.deployVMs(eq(vms4))).thenReturn(ids4);	
		
		verifyDeployVMsWithoutDemoImages(deployment, vmMaClient);
	}
	
	@Test
	public void testDeployVMsUsingDEMOImages() throws InterruptedException {		
		DeploymentDAO deploymentDAO = mock(DeploymentDAO.class);
		
		Deployment deployment = new Deployment();
		deployment.setId(22);
		deployment.setOvf(threeTierWebAppDEMOOvfString);
		
		when(deploymentDAO.getById(22)).thenReturn(deployment);
		
		// Application DAO mock
		ApplicationDAO applicationDAO = mock(ApplicationDAO.class);
		
		Application application = new Application();
		application.setName("threeTierWebApp");
		when(applicationDAO.getByName("threeTierWebApp")).thenReturn(application);
		when(applicationDAO.update(application)).thenReturn(true);
		
		// The object will be updated in the database
		when(deploymentDAO.update(deployment)).thenReturn(true);
		
		VmManagerClient vmMaClient = mock(VmManagerClient.class);
		// We mock the class to the VmManagerClient
		when(vmMaClient.uploadImage(eq(new ImageToUploadWithEquals("haproxy.img","/DFS/ascetic/vm-images/threeTierWebApp/haproxy.img")))).thenReturn("haproxy-uuid");
		when(vmMaClient.uploadImage(eq(new ImageToUploadWithEquals("mysql.img","/DFS/ascetic/vm-images/threeTierWebApp/mysql.img")))).thenReturn("mysql-uuid");
		when(vmMaClient.uploadImage(eq(new ImageToUploadWithEquals("jmeter.img","/DFS/ascetic/vm-images/threeTierWebApp/jmeter.img")))).thenReturn("jmeter-uuid");
		
		List<Vm> vms1 = new ArrayList<Vm>();
		VmWithEquals vm1 = new VmWithEquals("HAProxy_1","haproxy-uuid",1,1024,20, 0, "/DFS/ascetic/vm-images/threeTierWebApp/haproxy.iso_1","threeTierWebApp", "haproxy", "sla-id", false);
		vms1.add(vm1);
		List<String> ids1 = new ArrayList<String>();
		ids1.add("haproxy-vm1");
		
		when(vmMaClient.deployVMs(eq(vms1))).thenReturn(ids1);
		
		List<Vm> vms2 = new ArrayList<Vm>();
		VmWithEquals vm2 = new VmWithEquals("Jboss_1","jboss-uuid",1,2048,20, 0, "/DFS/ascetic/vm-images/threeTierWebApp/jboss.iso_1","threeTierWebApp", "jboss", "sla-id", false);
		vms2.add(vm2);
		List<String> ids2 = new ArrayList<String>();
		ids2.add("jboss-vm1");
		
		when(vmMaClient.deployVMs(eq(vms2))).thenReturn(ids2);
		
		List<Vm> vms5 = new ArrayList<Vm>();
		VmWithEquals vm5 = new VmWithEquals("Jboss_2","jboss-uuid",1,2048,20, 0, "/DFS/ascetic/vm-images/threeTierWebApp/jboss.iso_2","threeTierWebApp", "jboss", "sla-id", false);
		vms5.add(vm5);
		List<String> ids5 = new ArrayList<String>();
		ids5.add("jboss-vm2");
		
		when(vmMaClient.deployVMs(eq(vms5))).thenReturn(ids5);
		
		List<Vm> vms3 = new ArrayList<Vm>();
		VmWithEquals vm3 = new VmWithEquals("MySQL_1","mysql-uuid",1,1024,20, 0, "/DFS/ascetic/vm-images/threeTierWebApp/mysql.iso_1","threeTierWebApp", "mysql", "sla-id", false);
		vms3.add(vm3);
		List<String> ids3 = new ArrayList<String>();
		ids3.add("mysql-vm1");
		
		when(vmMaClient.deployVMs(eq(vms3))).thenReturn(ids3);
		
		List<Vm> vms4 = new ArrayList<Vm>();
		VmWithEquals vm4 = new VmWithEquals("JMeter_1","jmeter-uuid",1,1024,20, 0, "/DFS/ascetic/vm-images/threeTierWebApp/jmeter.iso_1","threeTierWebApp", "jmeter", "sla-id", false);
		vms4.add(vm4);
		List<String> ids4 = new ArrayList<String>();
		ids4.add("jmeter-vm1");
		
		when(vmMaClient.deployVMs(eq(vms4))).thenReturn(ids4);
		
		//We mock the VM DAO
		VMDAO vmDAO = mock(VMDAO.class);
		when(vmDAO.save((VM) any())).thenReturn(true);
		
		//We mock the image DAO
		ImageDAO imageDAO = mock(ImageDAO.class);
		Image image1 = new Image();
		image1.setOvfId("haproxy-img");
		image1.setOvfHref("/DFS/ascetic/vm-images/threeTierWebApp/haproxy.img");
		image1.setProviderImageId("haproxy-uuid");
		image1.setDemo(false);
		when(imageDAO.save(eq(image1))).thenReturn(true);
		Image image2 = new Image();
		image2.setOvfId("jboss-img");
		image2.setOvfHref("/DFS/ascetic/vm-images/threeTierWebApp/jboss.img");
		image2.setProviderImageId("jboss-uuid");
		image2.setDemo(true);
//		when(imageDAO.save(eq(image2))).thenReturn(true);
		// We read it from the DB
		when(imageDAO.getDemoCacheImage("jboss-img", "/DFS/ascetic/vm-images/threeTierWebApp/jboss.img")).thenReturn(image2);
		Image image3 = new Image();
		image3.setOvfId("mysql-img");
		image3.setOvfHref("/DFS/ascetic/vm-images/threeTierWebApp/mysql.img");
		image3.setProviderImageId("mysql-uuid");
		image3.setDemo(false);
		when(imageDAO.save(eq(image3))).thenReturn(true);
		Image image4 = new Image();
		image4.setOvfId("jmeter-img");
		image4.setOvfHref("/DFS/ascetic/vm-images/threeTierWebApp/jmeter.img");
		image4.setProviderImageId("jmeter-uuid");
		image4.setDemo(true);
		//We try to find the image first in the db
		when(imageDAO.getDemoCacheImage("jmeter-img", "/DFS/ascetic/vm-images/threeTierWebApp/jmeter.img")).thenReturn(null);
		when(imageDAO.save(eq(image4))).thenReturn(true);
		
		when(imageDAO.getById(0)).thenReturn(image1, image2, image3, image4);
		
		// We mock the calls to get VMs
		when(vmMaClient.getVM("haproxy-vm1")).thenReturn(new VmDeployed("haproxyVM", "haproxy-img", 1, 2, 3, 0, "", "",  "", "10.0.0.1", "ACTIVE", new Date(), ""));
		when(vmMaClient.getVM("jboss-vm1")).thenReturn(new VmDeployed("jbossVM", "jboss-img", 1, 2, 3,  0, "", "", "",  "10.0.0.2", "ACTIVE", new Date(), ""));
		when(vmMaClient.getVM("jboss-vm2")).thenReturn(new VmDeployed("jbossVM", "jboss-img", 1, 2, 3,  0, "", "", "", "10.0.0.2", "ACTIVE", new Date(), ""));
		when(vmMaClient.getVM("mysql-vm1")).thenReturn(new VmDeployed("mysqlVM", "mysql-img", 1, 2, 3,  0, "", "", "", "10.0.0.3", "ACTIVE", new Date(), ""));
		when(vmMaClient.getVM("jmeter-vm1")).thenReturn(new VmDeployed("jmeterVM", "jmeter-img", 1, 2, 3,  0, "", "", "", "10.0.0.4", "ACTIVE", new Date(), ""));
		
		// We mock the calls to the PRClient
		Configuration.providerRegistryEndpoint = "http://provider-registry.com";
		PRClient prClient = mock(PRClient.class);
		when(prClient.getVMMClient(-1)).thenReturn(vmMaClient);
		
		// EM bean
		EnergyModellerBean em = mock(EnergyModellerBean.class);
		
		// The test starts here
		DeploymentEventService deploymentEventService = mock(DeploymentEventService.class);
		
		DeploymentEvent deploymentEvent = new DeploymentEvent();
		deploymentEvent.setDeploymentId(22);
		deploymentEvent.setDeploymentStatus(Dictionary.APPLICATION_STATUS_CONTEXTUALIZED);
		
		// We configure de DeployEventHandler
		DeployEventHandler deploymentEventHandler = new DeployEventHandler();
		deploymentEventHandler.deploymentDAO = deploymentDAO;
		deploymentEventHandler.deploymentEventService = deploymentEventService;
		deploymentEventHandler.applicationDAO = applicationDAO;
		deploymentEventHandler.imageDAO = imageDAO;
		deploymentEventHandler.prClient = prClient;
		deploymentEventHandler.vmDAO = vmDAO;
		deploymentEventHandler.em = em;
		
		//We start the task
		deploymentEventHandler.deployDeployment(Event.wrap(deploymentEvent));
		//We give time to the thread to do its business... ugly code... 
		Thread.sleep(4000l);

		// We verify the mock calls
		verify(imageDAO, times(1)).save(eq(image1));
		verify(imageDAO, times(1)).getDemoCacheImage("jboss-img", "/DFS/ascetic/vm-images/threeTierWebApp/jboss.img");
		//verify(imageDAO, times(1)).save(eq(image2));
		verify(imageDAO, times(1)).save(eq(image3));
		verify(imageDAO, times(1)).getDemoCacheImage("jmeter-img", "/DFS/ascetic/vm-images/threeTierWebApp/jmeter.img");
		verify(imageDAO, times(1)).save(eq(image4));
		verify(vmMaClient, times(1)).getVM("haproxy-vm1");
		verify(vmMaClient, times(1)).getVM("jboss-vm1");
		verify(vmMaClient, times(1)).getVM("jboss-vm2");
		verify(vmMaClient, times(1)).getVM("mysql-vm1");
		verify(vmMaClient, times(1)).getVM("jmeter-vm1");
		
		verify(vmDAO, times(5)).save((VM) any());
		
		// Verify Em
//		verify(em, times(1)).notifyVMChangeInStatus("-1", application.getName(), "22", "0", "haproxy-vm1", Dictionary.APPLICATION_STATUS_DEPLOYED);
//		verify(em, times(1)).notifyVMChangeInStatus("-1", application.getName(), "22", "0", "jboss-vm1", Dictionary.APPLICATION_STATUS_DEPLOYED);
//		verify(em, times(1)).notifyVMChangeInStatus("-1", application.getName(), "22", "0", "jboss-vm2", Dictionary.APPLICATION_STATUS_DEPLOYED);
//		verify(em, times(1)).notifyVMChangeInStatus("-1", application.getName(), "22", "0", "mysql-vm1", Dictionary.APPLICATION_STATUS_DEPLOYED);
//		verify(em, times(1)).notifyVMChangeInStatus("-1", application.getName(), "22", "0", "jmeter-vm1", Dictionary.APPLICATION_STATUS_DEPLOYED);
		
		// We verify that at the end we store in the database the right object
		ArgumentCaptor<Deployment> deploymentCaptor = ArgumentCaptor.forClass(Deployment.class);
		verify(deploymentDAO, times(7)).update(deploymentCaptor.capture());
		assertEquals(5, deploymentCaptor.getValue().getVms().size());
		assertEquals("haproxy", deploymentCaptor.getValue().getVms().get(0).getOvfId());
		assertEquals("10.0.0.1", deploymentCaptor.getValue().getVms().get(0).getIp());
		assertEquals("haproxy-vm1", deploymentCaptor.getValue().getVms().get(0).getProviderVmId());
		assertEquals(2, deploymentCaptor.getValue().getVms().get(0).getNumberVMsMax());
		assertEquals(1, deploymentCaptor.getValue().getVms().get(0).getNumberVMsMin());
		assertEquals("ACTIVE", deploymentCaptor.getValue().getVms().get(0).getStatus());
		assertEquals(1, deploymentCaptor.getValue().getVms().get(0).getImages().size());
		assertEquals("/DFS/ascetic/vm-images/threeTierWebApp/haproxy.img", deploymentCaptor.getValue().getVms().get(0).getImages().get(0).getOvfHref());
		assertEquals("haproxy-img", deploymentCaptor.getValue().getVms().get(0).getImages().get(0).getOvfId());
		assertEquals("haproxy-uuid", deploymentCaptor.getValue().getVms().get(0).getImages().get(0).getProviderImageId());
		assertEquals("haproxy-uuid", deploymentCaptor.getValue().getVms().get(0).getImages().get(0).getProviderImageId());
		assertFalse(deploymentCaptor.getValue().getVms().get(0).getImages().get(0).isDemo());
		
		assertEquals("jboss", deploymentCaptor.getValue().getVms().get(1).getOvfId());
		assertEquals("10.0.0.2", deploymentCaptor.getValue().getVms().get(1).getIp());
		assertEquals("jboss-vm1", deploymentCaptor.getValue().getVms().get(1).getProviderVmId());
		assertEquals("ACTIVE", deploymentCaptor.getValue().getVms().get(1).getStatus());
		assertEquals(2, deploymentCaptor.getValue().getVms().get(1).getNumberVMsMax());
		assertEquals(2, deploymentCaptor.getValue().getVms().get(1).getNumberVMsMin());
		assertEquals(1, deploymentCaptor.getValue().getVms().get(1).getImages().size());
		assertEquals("/DFS/ascetic/vm-images/threeTierWebApp/jboss.img", deploymentCaptor.getValue().getVms().get(1).getImages().get(0).getOvfHref());
		assertEquals("jboss-img", deploymentCaptor.getValue().getVms().get(1).getImages().get(0).getOvfId());
		assertEquals("jboss-uuid", deploymentCaptor.getValue().getVms().get(1).getImages().get(0).getProviderImageId());
		assertTrue(deploymentCaptor.getValue().getVms().get(1).getImages().get(0).isDemo());
		
		assertEquals("jboss", deploymentCaptor.getValue().getVms().get(2).getOvfId());
		assertEquals("10.0.0.2", deploymentCaptor.getValue().getVms().get(2).getIp());
		assertEquals("jboss-vm2", deploymentCaptor.getValue().getVms().get(2).getProviderVmId());
		assertEquals("ACTIVE", deploymentCaptor.getValue().getVms().get(2).getStatus());
		assertEquals(2, deploymentCaptor.getValue().getVms().get(2).getNumberVMsMax());
		assertEquals(2, deploymentCaptor.getValue().getVms().get(2).getNumberVMsMin());
		assertEquals(1, deploymentCaptor.getValue().getVms().get(2).getImages().size());
		assertEquals("/DFS/ascetic/vm-images/threeTierWebApp/jboss.img", deploymentCaptor.getValue().getVms().get(2).getImages().get(0).getOvfHref());
		assertEquals("jboss-img", deploymentCaptor.getValue().getVms().get(2).getImages().get(0).getOvfId());
		assertEquals("jboss-uuid", deploymentCaptor.getValue().getVms().get(2).getImages().get(0).getProviderImageId());
		
		assertEquals("mysql", deploymentCaptor.getValue().getVms().get(3).getOvfId());
		assertEquals("10.0.0.3", deploymentCaptor.getValue().getVms().get(3).getIp());
		assertEquals("mysql-vm1", deploymentCaptor.getValue().getVms().get(3).getProviderVmId());
		assertEquals("ACTIVE", deploymentCaptor.getValue().getVms().get(3).getStatus());
		assertEquals(1, deploymentCaptor.getValue().getVms().get(3).getNumberVMsMax());
		assertEquals(1, deploymentCaptor.getValue().getVms().get(3).getNumberVMsMin());
		assertEquals(1, deploymentCaptor.getValue().getVms().get(3).getImages().size());
		assertEquals(1, deploymentCaptor.getValue().getVms().get(3).getImages().size());
		assertEquals("/DFS/ascetic/vm-images/threeTierWebApp/mysql.img", deploymentCaptor.getValue().getVms().get(3).getImages().get(0).getOvfHref());
		assertEquals("mysql-img", deploymentCaptor.getValue().getVms().get(3).getImages().get(0).getOvfId());
		assertEquals("mysql-uuid", deploymentCaptor.getValue().getVms().get(3).getImages().get(0).getProviderImageId());
		assertFalse(deploymentCaptor.getValue().getVms().get(3).getImages().get(0).isDemo());
		
		
		assertEquals("jmeter", deploymentCaptor.getValue().getVms().get(4).getOvfId());
		assertEquals("10.0.0.4", deploymentCaptor.getValue().getVms().get(4).getIp());
		assertEquals("jmeter-vm1", deploymentCaptor.getValue().getVms().get(4).getProviderVmId());
		assertEquals("ACTIVE", deploymentCaptor.getValue().getVms().get(4).getStatus());
		assertEquals(1, deploymentCaptor.getValue().getVms().get(4).getImages().size());
		assertEquals(1, deploymentCaptor.getValue().getVms().get(4).getImages().size());
		assertEquals(1, deploymentCaptor.getValue().getVms().get(4).getNumberVMsMax());
		assertEquals(1, deploymentCaptor.getValue().getVms().get(4).getNumberVMsMin());
		assertEquals("/DFS/ascetic/vm-images/threeTierWebApp/jmeter.img", deploymentCaptor.getValue().getVms().get(4).getImages().get(0).getOvfHref());
		assertEquals("jmeter-img", deploymentCaptor.getValue().getVms().get(4).getImages().get(0).getOvfId());
		assertEquals("jmeter-uuid", deploymentCaptor.getValue().getVms().get(4).getImages().get(0).getProviderImageId());
		assertTrue(deploymentCaptor.getValue().getVms().get(4).getImages().get(0).isDemo());
		
		verify(applicationDAO, times(3)).getByName("threeTierWebApp");
		//verify(applicationDAO, times(3)).update(application); //TODO add the new call to imageDAO update... to verify it
		
		// We verify that we send the right internal message at the end
		ArgumentCaptor<DeploymentEvent> argument = ArgumentCaptor.forClass(DeploymentEvent.class);
		verify(deploymentEventService).fireDeploymentEvent(argument.capture());
		
		assertEquals(22, argument.getValue().getDeploymentId());
		assertEquals(Dictionary.APPLICATION_STATUS_DEPLOYED, argument.getValue().getDeploymentStatus());
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
