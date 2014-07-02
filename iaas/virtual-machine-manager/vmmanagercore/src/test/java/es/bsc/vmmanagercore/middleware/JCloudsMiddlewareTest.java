package es.bsc.vmmanagercore.middleware;

import es.bsc.vmmanagercore.cloudmiddleware.JCloudsMiddleware;
import es.bsc.vmmanagercore.db.VmManagerDbHsql;
import es.bsc.vmmanagercore.manager.VmManagerConfiguration;
import es.bsc.vmmanagercore.model.Vm;
import org.jclouds.openstack.nova.v2_0.NovaApi;
import org.jclouds.openstack.nova.v2_0.domain.Flavor;
import org.jclouds.openstack.nova.v2_0.domain.Server;
import org.jclouds.openstack.nova.v2_0.features.FlavorApi;
import org.jclouds.openstack.nova.v2_0.features.ServerApi;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Set;

import static org.junit.Assert.*;


/**
 * Tests for JCloudsMiddleware.
 *
 * @author David Ortiz Lopez (david.ortiz@bsc.es)
 */
public class JCloudsMiddlewareTest {
	
	@Rule
	public ExpectedException exception = ExpectedException.none();
	
	private static JCloudsMiddleware jCloudsMiddleware;
	
	private static String testingImageId; // ID of the image used by the VMs created in this test
	
	//IDs of the instances that exist before running the tests
	private static ArrayList<String> vmsIdsBeforeTests;
	
	//IDs of the flavors that exist before running the tests
	private static ArrayList<String> flavorIdsBeforeTests;
	
	//needed by JClouds
	private static NovaApi novaApi;
	private static Set<String> zones;
	
	private static void saveIdsOfInstancesThatExistBeforeTheTest() {
		for (String zone: zones) {
			ServerApi serverApi = novaApi.getServerApiForZone(zone);
			vmsIdsBeforeTests = new ArrayList<>();
			for (Server server: serverApi.listInDetail().concat()) {
				vmsIdsBeforeTests.add(server.getId());
			}
		}
	}
	
	private static void saveIdsOfFlavorsThatExistBeforeTheTest() {
		for (String zone: zones) {
			FlavorApi flavorApi = novaApi.getFlavorApiForZone(zone);
			flavorIdsBeforeTests = new ArrayList<>();
			for (Flavor flavor: flavorApi.listInDetail().concat()) {
				flavorIdsBeforeTests.add(flavor.getId());
			}
		}
	}
	
	@BeforeClass
	public static void setUpBeforeClass() {
		//get the test image ID from the configuration file
		testingImageId = VmManagerConfiguration.getInstance().testingImageId;
		
		//initialize JClouds variables
		VmManagerDbHsql db = null;
		try {
			db = new VmManagerDbHsql("testDb");
			db.deleteAllVms();
		} catch (Exception e) {
			e.printStackTrace();
		}
		jCloudsMiddleware = new JCloudsMiddleware(db);
		novaApi = jCloudsMiddleware.getNovaApi();
		zones = jCloudsMiddleware.getZones();
		
		saveIdsOfInstancesThatExistBeforeTheTest();
		saveIdsOfFlavorsThatExistBeforeTheTest();
	}
	
	@AfterClass
	public static void tearDownAfterClass() {
		//make sure that the VMs deployed before beginning these tests are still there
		ArrayList<String> vmsIdsAfterTests = new ArrayList<>();
		for (String zone: zones) {
			ServerApi serverApi = novaApi.getServerApiForZone(zone);
			vmsIdsAfterTests = new ArrayList<>();
			for (Server server: serverApi.listInDetail().concat()) {
				vmsIdsAfterTests.add(server.getId());
			}
		}
		assertTrue(vmsIdsAfterTests.containsAll(vmsIdsBeforeTests));
		
		//make sure that the flavors that existed before the tests are still there
		ArrayList<String> flavorIdsAfterTests = new ArrayList<>();
		for (String zone: zones) {
			FlavorApi flavorApi = novaApi.getFlavorApiForZone(zone);
			flavorIdsAfterTests = new ArrayList<>();
			for (Flavor flavor: flavorApi.listInDetail().concat()) {
				flavorIdsAfterTests.add(flavor.getId());
			}
		}
		assertTrue(flavorIdsAfterTests.containsAll(flavorIdsBeforeTests));
	}
	
	@Test
	public void deployVmWithNonExistingFlavor() {
		//deploy a VM
		Vm vmDescription = new Vm("TestVM", testingImageId, 1, 1024, 2, null, "app1");
		String instanceId = jCloudsMiddleware.deploy(vmDescription, null);
		
		//check that the information of the VM is correct
		Server server = null;
		for (String zone: zones) {
			ServerApi serverApi = novaApi.getServerApiForZone(zone);
			if (serverApi.get(instanceId) != null) {
				server = serverApi.get(instanceId);
			}
		}
		assertEquals(instanceId, server.getId());
		assertEquals("TestVM", server.getName());
		assertEquals(testingImageId, server.getImage().getId());
		
		//check that the information of the flavor of the VM is correct
		Flavor flavor = null;
		for (String zone: zones) {
			FlavorApi flavorApi = novaApi.getFlavorApiForZone(zone);
			if (flavorApi.get(server.getFlavor().getId()) != null) {
				flavor = flavorApi.get(server.getFlavor().getId());
			}
		}
		assertTrue(flavor.getVcpus() == 1 && flavor.getRam() == 1024 && flavor.getDisk() == 2);
		
		//destroy the VM 
		jCloudsMiddleware.destroy(instanceId);
	}
	
	@Test
	public void deployVmWithExistingFlavor() {
		//This test can only be performed if there is at least one flavor registered in OpenStack
		if (JCloudsMiddleware.DEFAULT_FLAVORS.length > 0) {
			
			//get the ID of one of the default flavors
			String flavorId = JCloudsMiddleware.DEFAULT_FLAVORS[0];
			
			//get the flavor with that ID
			Flavor flavor = null;
			for (String zone: zones) {
				FlavorApi flavorApi = novaApi.getFlavorApiForZone(zone);
				flavor = flavorApi.get(flavorId);
			}
			
			//deploy a VM with the specs described in the flavor
			Vm vmDescription = new Vm("TestVM", testingImageId, 
					flavor.getVcpus(), flavor.getRam(), flavor.getDisk(), null, "app1");
			String instanceId = jCloudsMiddleware.deploy(vmDescription, null);
			
			//check that the information of the VM is correct
			Server server = null;
			for (String zone: zones) {
				ServerApi serverApi = novaApi.getServerApiForZone(zone);
				if (serverApi.get(instanceId) != null) {
					server = serverApi.get(instanceId);
				}
			}
			assertEquals(instanceId, server.getId());
			assertEquals("TestVM", server.getName());
			assertEquals(testingImageId, server.getImage().getId());
			
			//check that the information of the flavor of the VM is correct
			assertEquals(flavorId, flavor.getId());
			
			//destroy the VM 
			jCloudsMiddleware.destroy(instanceId);
			
		}
	}
	
	@Test
	public void cannotDeployVmWithoutExistingImageId() {
		Vm vmDescription = new Vm("TestVM", "nonExistingImageId", 1, 1024, 2, null, "app1");
		exception.expect(IllegalArgumentException.class);
		jCloudsMiddleware.deploy(vmDescription, null);
	}
	
	@Test
	public void destroy() {
		//deploy a VM
		Vm vmDescription = new Vm("TestVM", testingImageId, 1, 1024, 2, null, "app1");
		String instanceId = jCloudsMiddleware.deploy(vmDescription, null);
		
		//destroy the VM
		jCloudsMiddleware.destroy(instanceId);
		
		//check that the instance no longer exists
		ArrayList<String> instancesIds = (ArrayList<String>) jCloudsMiddleware.getAllVMsId();
		assertFalse(instancesIds.contains(instanceId));
	}
	
	@Test
	public void getAllVMs() {
		//deploy two VMs
		Vm vmDescription1 = new Vm("TestVM1", testingImageId, 1, 1024, 1, null, "app1");
		String instanceId1 = jCloudsMiddleware.deploy(vmDescription1, null);
		Vm vmDescription2 = new Vm("TestVM2", testingImageId, 1, 1024, 2, null, "app1");
		String instanceId2 = jCloudsMiddleware.deploy(vmDescription2, null);
		
		//get the list of IDs
		Collection<String> ids = jCloudsMiddleware.getAllVMsId();
		
		//check that the two VMs exist
		assertTrue(ids.contains(instanceId1) && ids.contains(instanceId2));
		
		//delete the two VMs
		jCloudsMiddleware.destroy(instanceId1);
		jCloudsMiddleware.destroy(instanceId2);
	}
	
	@Test
	public void getVMInfo() {
		//deploy a VM
		Vm vmDescription1 = new Vm("TestVM1", testingImageId, 1, 1024, 2, null, "app1");
		String instanceId = jCloudsMiddleware.deploy(vmDescription1, null);
		
		//check that the information of the VM is correct
		Vm resultVmDescription = jCloudsMiddleware.getVMInfo(instanceId);
		assertEquals("TestVM1", resultVmDescription.getName());
		assertEquals(testingImageId, resultVmDescription.getImage());
		assertTrue(resultVmDescription.getCpus() == 1);
		assertTrue(resultVmDescription.getRamMb() == 1024);
		assertTrue(resultVmDescription.getDiskGb() == 2);
		
		//destroy the VM 
		jCloudsMiddleware.destroy(instanceId);
		
		//check that the function returns null when there is not a VM with the id specified
		assertNull(jCloudsMiddleware.getVMInfo(instanceId));
	}

	@Test
	public void migrateTest() {
		String[] hosts = jCloudsMiddleware.getHosts();
		//perform this test only if there are 2 or more nodes in the infrastructure
		if (hosts.length >= 2) {
			//TODO
		}
	}
}
