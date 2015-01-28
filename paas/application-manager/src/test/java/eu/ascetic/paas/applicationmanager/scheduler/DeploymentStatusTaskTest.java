package eu.ascetic.paas.applicationmanager.scheduler;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.eq;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.times;

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

import eu.ascetic.paas.applicationmanager.dao.ApplicationDAO;
import eu.ascetic.paas.applicationmanager.dao.DeploymentDAO;
import eu.ascetic.paas.applicationmanager.dao.ImageDAO;
import eu.ascetic.paas.applicationmanager.dao.VMDAO;
import eu.ascetic.paas.applicationmanager.model.Application;
import eu.ascetic.paas.applicationmanager.model.Deployment;
import eu.ascetic.paas.applicationmanager.model.Dictionary;
import eu.ascetic.paas.applicationmanager.model.Image;
import eu.ascetic.paas.applicationmanager.model.VM;
import eu.ascetic.paas.applicationmanager.vmmanager.client.VmManagerClient;
import eu.ascetic.paas.applicationmanager.vmmanager.datamodel.ImageToUpload;
import eu.ascetic.paas.applicationmanager.vmmanager.datamodel.Vm;
import eu.ascetic.paas.applicationmanager.vmmanager.datamodel.VmDeployed;

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
 * @author: David Garcia Perez. Atos Research and Innovation, Atos SPAIN SA
 * e-mail: david.garciaperez@atos.net 
 */

public class DeploymentStatusTaskTest {	
	private String threeTierWebAppOvfFile = "3tier-webapp.ovf.xml";
	private String threeTierWebAppOvfString;
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
	}
	
	/* // TODO this test should be updated as soon as new functionality it is added... 
	@Test
	public void checkDeploymentStatusTest() {
		DeploymentDAO deploymentDAO = mock(DeploymentDAO.class);
		
		Deployment deployment1 = new Deployment();
		deployment1.setId(1);
		deployment1.setStatus(Dictionary.APPLICATION_STATUS_SUBMITTED);
		Deployment deployment2 = new Deployment();
		deployment2.setId(2);
		deployment2.setStatus(Dictionary.APPLICATION_STATUS_NEGOTIATION);
		Deployment deployment3 = new Deployment();
		deployment3.setId(3);
		deployment3.setStatus(Dictionary.APPLICATION_STATUS_NEGOTIATIED);
		Deployment deployment4 = new Deployment();
		deployment4.setId(4);
		deployment4.setStatus(Dictionary.APPLICATION_STATUS_CONTEXTUALIZATION);
		
		
		List<Deployment> deployments = new ArrayList<Deployment>();
		deployments.add(deployment1);
		deployments.add(deployment2);
		deployments.add(deployment3);
		deployments.add(deployment4);
		
		// The object will be updated in the database
		when(deploymentDAO.getAll()).thenReturn(deployments);
		when(deploymentDAO.update(deployment1)).thenReturn(true);
		when(deploymentDAO.update(deployment3)).thenReturn(true);
		when(deploymentDAO.update(deployment4)).thenReturn(true);
		
		DeploymentsStatusTask task = new DeploymentsStatusTask();
		task.deploymentDAO = deploymentDAO;
		
		// We start to iterate over the list of deployments
		task.checkDeploymentStatus();
		
		assertEquals(Dictionary.APPLICATION_STATUS_NEGOTIATIED, deployment1.getStatus());
		assertEquals(Dictionary.APPLICATION_STATUS_NEGOTIATION, deployment2.getStatus());
		assertEquals(Dictionary.APPLICATION_STATUS_CONTEXTUALIZATION, deployment3.getStatus());
		assertEquals(Dictionary.APPLICATION_STATUS_CONTEXTUALIZED, deployment4.getStatus());
		
		// We verify the calls to the DAO
		verify(deploymentDAO, times(1)).getAll();
		verify(deploymentDAO, times(1)).update(deployment1);
		verify(deploymentDAO, times(1)).update(deployment3);
		verify(deploymentDAO, times(1)).update(deployment4);
	} */
	
	// TODO this test should be updated as soon as new functionality it is added... 
	@Test
	public void testDeploymentAcceptAgreementActions() {
		DeploymentDAO deploymentDAO = mock(DeploymentDAO.class);
		
		Deployment deployment = new Deployment();
		
		// The object will be updated in the database
		when(deploymentDAO.update(deployment)).thenReturn(true);
		
		DeploymentsStatusTask task = new DeploymentsStatusTask();
		task.deploymentDAO = deploymentDAO;
		task.deploymentAcceptAgreementActions(deployment);
		
		assertEquals(Dictionary.APPLICATION_STATUS_CONTEXTUALIZATION, deployment.getStatus());
		// We check that the DAO was called
		verify(deploymentDAO, times(1)).update(deployment);
	}
	
	/* // TODO this test should be updated as soon as new functionality it is added... 
	@Test
	public void testDeploymentStartContextualizationActions() {
		DeploymentDAO deploymentDAO = mock(DeploymentDAO.class);
		
		Deployment deployment = new Deployment();
		
		// The object will be updated in the database
		when(deploymentDAO.update(deployment)).thenReturn(true);
		
		DeploymentsStatusTask task = new DeploymentsStatusTask();
		task.deploymentDAO = deploymentDAO;
		task.deploymentStartContextualizationActions(deployment);
		
		assertEquals(Dictionary.APPLICATION_STATUS_CONTEXTUALIZED, deployment.getStatus());
		// We check that the DAO was called
		verify(deploymentDAO, times(1)).update(deployment);
	} */
	
	@Test
	public void deploymentDeployApplicationActionsCreatingDEMOImagesTest() {
		DeploymentDAO deploymentDAO = mock(DeploymentDAO.class);
		
		Deployment deployment = new Deployment();
		deployment.setOvf(threeTierWebAppDEMOOvfString);
		
		// Application DAO mock
		ApplicationDAO applicationDAO = mock(ApplicationDAO.class);
		
		Application application = new Application();
		when(applicationDAO.getByName("threeTierWebApp")).thenReturn(application);
		when(applicationDAO.update(application)).thenReturn(true);
		
		// The object will be updated in the database
		when(deploymentDAO.update(deployment)).thenReturn(true);
		
		VmManagerClient vmMaClient = mock(VmManagerClient.class);
		// We mock the class to the VmManagerClient
		when(vmMaClient.uploadImage(eq(new ImageToUpload("haproxy.img","/DFS/ascetic/vm-images/threeTierWebApp/haproxy.img")))).thenReturn("haproxy-uuid");
		//when(vmMaClient.uploadImage(eq(new ImageToUpload("jboss.img","/DFS/ascetic/vm-images/threeTierWebApp/jboss.img")))).thenReturn("jboss-uuid");
		when(vmMaClient.uploadImage(eq(new ImageToUpload("mysql.img","/DFS/ascetic/vm-images/threeTierWebApp/mysql.img")))).thenReturn("mysql-uuid");
		when(vmMaClient.uploadImage(eq(new ImageToUpload("jmeter.img","/DFS/ascetic/vm-images/threeTierWebApp/jmeter.img")))).thenReturn("jmeter-uuid");
		
		List<Vm> vms1 = new ArrayList<Vm>();
		Vm vm1 = new Vm("HAProxy_1","haproxy-uuid",1,1024,20,"/DFS/ascetic/vm-images/threeTierWebApp/haproxy.iso_1","threeTierWebApp");
		vm1.setOvfId("haproxy");
		vms1.add(vm1);
		List<String> ids1 = new ArrayList<String>();
		ids1.add("haproxy-vm1");
		
		when(vmMaClient.deployVMs(eq(vms1))).thenReturn(ids1);
		
		List<Vm> vms2 = new ArrayList<Vm>();
		Vm vm2 = new Vm("Jboss_1","jboss-uuid",1,2048,20,"/DFS/ascetic/vm-images/threeTierWebApp/jboss.iso_1","threeTierWebApp");
		vm2.setOvfId("jboss");
		vms2.add(vm2);
		List<String> ids2 = new ArrayList<String>();
		ids2.add("jboss-vm1");
		
		when(vmMaClient.deployVMs(eq(vms2))).thenReturn(ids2);
		
		List<Vm> vms5 = new ArrayList<Vm>();
		Vm vm5 = new Vm("Jboss_2","jboss-uuid",1,2048,20,"/DFS/ascetic/vm-images/threeTierWebApp/jboss.iso_2","threeTierWebApp");
		vm5.setOvfId("jboss");
		vms5.add(vm5);
		List<String> ids5 = new ArrayList<String>();
		ids5.add("jboss-vm2");
		
		when(vmMaClient.deployVMs(eq(vms5))).thenReturn(ids5);
		
		List<Vm> vms3 = new ArrayList<Vm>();
		Vm vm3 = new Vm("MySQL_1","mysql-uuid",1,1024,20,"/DFS/ascetic/vm-images/threeTierWebApp/mysql.iso_1","threeTierWebApp");
		vm3.setOvfId("mysql");
		vms3.add(vm3);
		List<String> ids3 = new ArrayList<String>();
		ids3.add("mysql-vm1");
		
		when(vmMaClient.deployVMs(eq(vms3))).thenReturn(ids3);
		
		List<Vm> vms4 = new ArrayList<Vm>();
		Vm vm4 = new Vm("JMeter_1","jmeter-uuid",1,1024,20,"/DFS/ascetic/vm-images/threeTierWebApp/jmeter.iso_1","threeTierWebApp");
		vm4.setOvfId("jmeter");
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
		when(vmMaClient.getVM("haproxy-vm1")).thenReturn(new VmDeployed("haproxyVM", "haproxy-img", 1, 2, 3, "", "", "", "10.0.0.1", "ACTIVE", new Date(), ""));
		when(vmMaClient.getVM("jboss-vm1")).thenReturn(new VmDeployed("jbossVM", "jboss-img", 1, 2, 3, "", "", "", "10.0.0.2", "ACTIVE", new Date(), ""));
		when(vmMaClient.getVM("jboss-vm2")).thenReturn(new VmDeployed("jbossVM", "jboss-img", 1, 2, 3, "", "", "", "10.0.0.2", "ACTIVE", new Date(), ""));
		when(vmMaClient.getVM("mysql-vm1")).thenReturn(new VmDeployed("mysqlVM", "mysql-img", 1, 2, 3, "", "", "", "10.0.0.3", "ACTIVE", new Date(), ""));
		when(vmMaClient.getVM("jmeter-vm1")).thenReturn(new VmDeployed("jmeterVM", "jmeter-img", 1, 2, 3, "", "", "", "10.0.0.4", "ACTIVE", new Date(), ""));
		
		// The test starts here
		DeploymentsStatusTask task = new DeploymentsStatusTask();
		//We setup the mocks
		task.deploymentDAO = deploymentDAO;
		task.vmManagerClient = vmMaClient;
		task.imageDAO = imageDAO;
		task.vmDAO = vmDAO;
		task.applicationDAO = applicationDAO;
		
		//We start the task
		task.deploymentDeployApplicationActions(deployment);
		
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
		
		// We verify that at the end we store in the database the right object
		ArgumentCaptor<Deployment> deploymentCaptor = ArgumentCaptor.forClass(Deployment.class);
		verify(deploymentDAO, times(7)).update(deploymentCaptor.capture());
		assertEquals(5, deploymentCaptor.getValue().getVms().size());
		assertEquals("haproxy", deploymentCaptor.getValue().getVms().get(0).getOvfId());
		assertEquals("10.0.0.1", deploymentCaptor.getValue().getVms().get(0).getIp());
		assertEquals("haproxy-vm1", deploymentCaptor.getValue().getVms().get(0).getProviderVmId());
		assertEquals("ACTIVE", deploymentCaptor.getValue().getVms().get(0).getStatus());
		assertEquals(1, deploymentCaptor.getValue().getVms().get(0).getImages().size());
		assertEquals("/DFS/ascetic/vm-images/threeTierWebApp/haproxy.img", deploymentCaptor.getValue().getVms().get(0).getImages().get(0).getOvfHref());
		assertEquals("haproxy-img", deploymentCaptor.getValue().getVms().get(0).getImages().get(0).getOvfId());
		assertEquals("haproxy-uuid", deploymentCaptor.getValue().getVms().get(0).getImages().get(0).getProviderImageId());
		assertFalse(deploymentCaptor.getValue().getVms().get(0).getImages().get(0).isDemo());
		
		assertEquals("jboss", deploymentCaptor.getValue().getVms().get(1).getOvfId());
		assertEquals("10.0.0.2", deploymentCaptor.getValue().getVms().get(1).getIp());
		assertEquals("jboss-vm1", deploymentCaptor.getValue().getVms().get(1).getProviderVmId());
		assertEquals("ACTIVE", deploymentCaptor.getValue().getVms().get(1).getStatus());
		assertEquals(1, deploymentCaptor.getValue().getVms().get(1).getImages().size());
		assertEquals("/DFS/ascetic/vm-images/threeTierWebApp/jboss.img", deploymentCaptor.getValue().getVms().get(1).getImages().get(0).getOvfHref());
		assertEquals("jboss-img", deploymentCaptor.getValue().getVms().get(1).getImages().get(0).getOvfId());
		assertEquals("jboss-uuid", deploymentCaptor.getValue().getVms().get(1).getImages().get(0).getProviderImageId());
		assertTrue(deploymentCaptor.getValue().getVms().get(1).getImages().get(0).isDemo());
		
		assertEquals("jboss", deploymentCaptor.getValue().getVms().get(2).getOvfId());
		assertEquals("10.0.0.2", deploymentCaptor.getValue().getVms().get(2).getIp());
		assertEquals("jboss-vm2", deploymentCaptor.getValue().getVms().get(2).getProviderVmId());
		assertEquals("ACTIVE", deploymentCaptor.getValue().getVms().get(2).getStatus());
		assertEquals(1, deploymentCaptor.getValue().getVms().get(2).getImages().size());
		assertEquals("/DFS/ascetic/vm-images/threeTierWebApp/jboss.img", deploymentCaptor.getValue().getVms().get(2).getImages().get(0).getOvfHref());
		assertEquals("jboss-img", deploymentCaptor.getValue().getVms().get(2).getImages().get(0).getOvfId());
		assertEquals("jboss-uuid", deploymentCaptor.getValue().getVms().get(2).getImages().get(0).getProviderImageId());
		
		assertEquals("mysql", deploymentCaptor.getValue().getVms().get(3).getOvfId());
		assertEquals("10.0.0.3", deploymentCaptor.getValue().getVms().get(3).getIp());
		assertEquals("mysql-vm1", deploymentCaptor.getValue().getVms().get(3).getProviderVmId());
		assertEquals("ACTIVE", deploymentCaptor.getValue().getVms().get(3).getStatus());
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
		assertEquals("/DFS/ascetic/vm-images/threeTierWebApp/jmeter.img", deploymentCaptor.getValue().getVms().get(4).getImages().get(0).getOvfHref());
		assertEquals("jmeter-img", deploymentCaptor.getValue().getVms().get(4).getImages().get(0).getOvfId());
		assertEquals("jmeter-uuid", deploymentCaptor.getValue().getVms().get(4).getImages().get(0).getProviderImageId());
		assertTrue(deploymentCaptor.getValue().getVms().get(4).getImages().get(0).isDemo());
		
		verify(applicationDAO, times(3)).getByName("threeTierWebApp");
		verify(applicationDAO, times(3)).update(application);
	}
	
	@Test
	public void deploymentDeployApplicationActionsCreatingImagesTest() {
		DeploymentDAO deploymentDAO = mock(DeploymentDAO.class);
		
		Deployment deployment = new Deployment();
		deployment.setOvf(threeTierWebAppOvfString);
		
		// Application DAO mock
		ApplicationDAO applicationDAO = mock(ApplicationDAO.class);
		
		Application application = new Application();
		when(applicationDAO.getByName("threeTierWebApp")).thenReturn(application);
		when(applicationDAO.update(application)).thenReturn(true);
		
		// The object will be updated in the database
		when(deploymentDAO.update(deployment)).thenReturn(true);
		
		//We mock the VM DAO
		VMDAO vmDAO = mock(VMDAO.class);
		when(vmDAO.save((VM) any())).thenReturn(true);
		
		VmManagerClient vmMaClient = mock(VmManagerClient.class);
		// We mock the class to the VmManagerClient
		when(vmMaClient.uploadImage(eq(new ImageToUpload("haproxy.img","/DFS/ascetic/vm-images/threeTierWebApp/haproxy.img")))).thenReturn("haproxy-uuid");
		when(vmMaClient.uploadImage(eq(new ImageToUpload("jboss.img","/DFS/ascetic/vm-images/threeTierWebApp/jboss.img")))).thenReturn("jboss-uuid");
		when(vmMaClient.uploadImage(eq(new ImageToUpload("mysql.img","/DFS/ascetic/vm-images/threeTierWebApp/mysql.img")))).thenReturn("mysql-uuid");
		when(vmMaClient.uploadImage(eq(new ImageToUpload("jmeter.img","/DFS/ascetic/vm-images/threeTierWebApp/jmeter.img")))).thenReturn("jmeter-uuid");
		
		List<Vm> vms1 = new ArrayList<Vm>();
		Vm vm1 = new Vm("HAProxy_1","haproxy-uuid",1,512,20,"","threeTierWebApp");
		vm1.setOvfId("haproxy");
		vms1.add(vm1);
		List<String> ids1 = new ArrayList<String>();
		ids1.add("haproxy-vm1");
		
		when(vmMaClient.deployVMs(eq(vms1))).thenReturn(ids1);
		
		List<Vm> vms2 = new ArrayList<Vm>();
		Vm vm2 = new Vm("Jboss_1","jboss-uuid",1,2048,20,"","threeTierWebApp");
		vm2.setOvfId("jboss");
		vms2.add(vm2);
		List<String> ids2 = new ArrayList<String>();
		ids2.add("jboss-vm1");
		
		when(vmMaClient.deployVMs(eq(vms2))).thenReturn(ids2);
		
		List<Vm> vms3 = new ArrayList<Vm>();
		Vm vm3 = new Vm("MySQL_1","mysql-uuid",1,512,20,"","threeTierWebApp");
		vm3.setOvfId("mysql");
		vms3.add(vm3);
		List<String> ids3 = new ArrayList<String>();
		ids3.add("mysql-vm1");
		
		when(vmMaClient.deployVMs(eq(vms3))).thenReturn(ids3);
		
		List<Vm> vms4 = new ArrayList<Vm>();
		Vm vm4 = new Vm("JMeter_1","jmeter-uuid",1,512,20,"","threeTierWebApp");
		vm4.setOvfId("jmeter");
		vms4.add(vm4);
		List<String> ids4 = new ArrayList<String>();
		ids4.add("jmeter-vm1");
		
		when(vmMaClient.deployVMs(eq(vms4))).thenReturn(ids4);
		
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
		when(vmMaClient.getVM("haproxy-vm1")).thenReturn(new VmDeployed("haproxyVM", "haproxy-img", 1, 2, 3, "", "", "", "10.0.0.1", "ACTIVE", new Date(), ""));
		when(vmMaClient.getVM("jboss-vm1")).thenReturn(new VmDeployed("jbossVM", "jboss-img", 1, 2, 3, "", "", "", "10.0.0.2", "ACTIVE", new Date(), ""));
		when(vmMaClient.getVM("mysql-vm1")).thenReturn(new VmDeployed("mysqlVM", "mysql-img", 1, 2, 3, "", "", "", "10.0.0.3", "ACTIVE", new Date(), ""));
		when(vmMaClient.getVM("jmeter-vm1")).thenReturn(new VmDeployed("jmeterVM", "jmeter-img", 1, 2, 3, "", "", "", "10.0.0.4", "ACTIVE", new Date(), ""));
		
		// The test starts here
		DeploymentsStatusTask task = new DeploymentsStatusTask();
		//We setup the mocks
		task.deploymentDAO = deploymentDAO;
		task.vmManagerClient = vmMaClient;
		task.imageDAO = imageDAO;
		task.vmDAO = vmDAO;
		task.applicationDAO = applicationDAO;
		
		//We start the task
		task.deploymentDeployApplicationActions(deployment);
		
		// We verify the mock calls
		verify(imageDAO, times(1)).save(eq(image1));
		verify(imageDAO, times(1)).save(eq(image2));
		verify(imageDAO, times(1)).save(eq(image3));
		verify(imageDAO, times(1)).save(eq(image4));
		verify(vmMaClient, times(1)).getVM("haproxy-vm1");
		verify(vmMaClient, times(1)).getVM("jboss-vm1");
		verify(vmMaClient, times(1)).getVM("mysql-vm1");
		verify(vmMaClient, times(1)).getVM("jmeter-vm1");
		
		verify(vmDAO, times(4)).save((VM) any());
		
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
		verify(applicationDAO, times(4)).update(application);
	}
	
	//@Test
	public void deploymentDeployApplicationActionsExceptionsTest() {
		DeploymentDAO deploymentDAO = mock(DeploymentDAO.class);
		
		Deployment deployment = new Deployment();
		
		// The object will be updated in the database
		when(deploymentDAO.update(deployment)).thenReturn(true);
		
		DeploymentsStatusTask task = new DeploymentsStatusTask();
		task.deploymentDAO = deploymentDAO;
		task.deploymentDeployApplicationActions(deployment);
		
		assertEquals(Dictionary.APPLICATION_STATUS_ERROR, deployment.getStatus());
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
