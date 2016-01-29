/**
 Copyright (C) 2013-2014  Barcelona Supercomputing Center

 This library is free software; you can redistribute it and/or
 modify it under the terms of the GNU Lesser General Public
 License as published by the Free Software Foundation; either
 version 2.1 of the License, or (at your option) any later version.

 This library is distributed in the hope that it will be useful,
 but WITHOUT ANY WARRANTY; without even the implied warranty of
 MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 Lesser General Public License for more details.

 You should have received a copy of the GNU Lesser General Public
 License along with this library; if not, write to the Free Software
 Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301  USA
 */

package es.bsc.demiurge.openstackjclouds;

import es.bsc.demiurge.core.configuration.Config;
import es.bsc.demiurge.core.db.VmManagerDb;
import es.bsc.demiurge.core.db.VmManagerDbFactory;
import es.bsc.demiurge.core.models.vms.Vm;
import org.jclouds.openstack.nova.v2_0.domain.Flavor;
import org.jclouds.openstack.nova.v2_0.domain.Server;
import org.jclouds.openstack.nova.v2_0.features.FlavorApi;
import org.jclouds.openstack.nova.v2_0.features.ServerApi;
import org.junit.*;
import org.junit.rules.ExpectedException;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.*;

/**
 * Tests for OpenStackJclouds.
 *
 * @author Mario Macias (github.com/mariomac), David Ortiz Lopez (david.ortiz@bsc.es)
 */
@Ignore
public class OpenStackJcloudsTest {
	
	@Rule
	public ExpectedException exception = ExpectedException.none();
	
	private static OpenStackJclouds openStackJclouds;
	
	private static String testingImageId; // ID of the image used by the VMs created in this test
	
	//IDs of the instances that exist before running the tests
	private static List<String> vmsIdsBeforeTests;
	
	//IDs of the flavors that exist before running the tests
	private static List<String> flavorIdsBeforeTests;
	
	//needed by JClouds
    private static ServerApi serverApi;
    private static FlavorApi flavorApi;
	
	private static void saveIdsOfInstancesThatExistBeforeTheTest() {
	    vmsIdsBeforeTests = new ArrayList<>();
		for (Server server: serverApi.listInDetail().concat()) {
			vmsIdsBeforeTests.add(server.getId());
		}
	}
	
	private static void saveIdsOfFlavorsThatExistBeforeTheTest() {
		flavorIdsBeforeTests = new ArrayList<>();
		for (Flavor flavor: flavorApi.listInDetail().concat()) {
			flavorIdsBeforeTests.add(flavor.getId());
		}
	}
	
	@BeforeClass
	public static void setUpBeforeClass() {
        Config conf = Config.INSTANCE;
		testingImageId = conf.getConfiguration().getString("testingImageId");
		VmManagerDb db = VmManagerDbFactory.getDb("testDb");
		db.deleteAllVms();

        // Initialize JClouds variables
		openStackJclouds = new OpenStackJclouds(
                new String[]{ "host1", "host2" }, // I am ignoring the sec. groups and hosts
				new String[]{ "" });
        serverApi = openStackJclouds.getNovaApi().getServerApiForZone(openStackJclouds.getZone());
        flavorApi = openStackJclouds.getNovaApi().getFlavorApiForZone(openStackJclouds.getZone());
		
		saveIdsOfInstancesThatExistBeforeTheTest();
		saveIdsOfFlavorsThatExistBeforeTheTest();
	}
	
	@AfterClass
    public static void tearDownAfterClass() {
        //make sure that the VMs deployed before beginning these tests are still there
        List<String> vmsIdsAfterTests = new ArrayList<>();
        for (Server server: serverApi.listInDetail().concat()) {
            vmsIdsAfterTests.add(server.getId());
        }
        assertTrue(vmsIdsAfterTests.containsAll(vmsIdsBeforeTests));

        //make sure that the flavors that existed before the tests are still there
        List<String> flavorIdsAfterTests = new ArrayList<>();
        for (Flavor flavor: flavorApi.listInDetail().concat()) {
            flavorIdsAfterTests.add(flavor.getId());
        }
        assertTrue(flavorIdsAfterTests.containsAll(flavorIdsBeforeTests));
	}

    @Test
    public void deployVmWithNonExistingFlavor() throws Exception {
        //deploy a VM
        Vm vmDescription = new Vm("TestVM", testingImageId, 1, 1024, 2, null, "app1");
        String instanceId = openStackJclouds.deploy(vmDescription, null);

        //check that the information of the VM is correct
        Server server = serverApi.get(instanceId);
        assertEquals(instanceId, server.getId());
        assertEquals("TestVM", server.getName());
        assertEquals(testingImageId, server.getImage().getId());

        //check that the information of the flavor of the VM is correct
        Flavor flavor = flavorApi.get(server.getFlavor().getId());
        assertTrue(flavor.getVcpus() == 1 && flavor.getRam() == 1024 && flavor.getDisk() == 2);

        //destroy the VM
        openStackJclouds.destroy(instanceId);
	}
	
	@Test
    public void deployVmWithExistingFlavor() throws Exception {
        //This test can only be performed if there is at least one flavor registered in OpenStack
        if (OpenStackJclouds.DEFAULT_FLAVORS.length > 0) {
            //get the ID of one of the default flavors
            String flavorId = OpenStackJclouds.DEFAULT_FLAVORS[0];

            //get the flavor with that ID
            Flavor flavor = flavorApi.get(flavorId);

            //deploy a VM with the specs described in the flavor
            Vm vmDescription = new Vm("TestVM", testingImageId,
                    flavor.getVcpus(), flavor.getRam(), flavor.getDisk(), null, "app1");
            String instanceId = openStackJclouds.deploy(vmDescription, null);

            //check that the information of the VM is correct
            Server server = serverApi.get(instanceId);
            assertEquals(instanceId, server.getId());
            assertEquals("TestVM", server.getName());
            assertEquals(testingImageId, server.getImage().getId());

            //check that the information of the flavor of the VM is correct
            assertEquals(flavorId, flavor.getId());

            //destroy the VM
            openStackJclouds.destroy(instanceId);
		}
	}
	
	@Test
	public void cannotDeployVmWithoutExistingImageId() throws Exception {
		Vm vmDescription = new Vm("TestVM", "nonExistingImageId", 1, 1024, 2, null, "app1");
		exception.expect(IllegalArgumentException.class);
		openStackJclouds.deploy(vmDescription, null);
	}
	
	@Test
	public void destroy() throws Exception {
		//deploy a VM
		Vm vmDescription = new Vm("TestVM", testingImageId, 1, 1024, 2, null, "app1");
		String instanceId = openStackJclouds.deploy(vmDescription, null);
		
		//destroy the VM
		openStackJclouds.destroy(instanceId);
		
		//check that the instance no longer exists
		List<String> instancesIds = openStackJclouds.getAllVMsIds();
		assertFalse(instancesIds.contains(instanceId));
	}
	
	@Test
	public void getAllVMs() throws Exception {
		//deploy two VMs
		Vm vmDescription1 = new Vm("TestVM1", testingImageId, 1, 1024, 1, null, "app1");
		String instanceId1 = openStackJclouds.deploy(vmDescription1, null);
		Vm vmDescription2 = new Vm("TestVM2", testingImageId, 1, 1024, 2, null, "app1");
		String instanceId2 = openStackJclouds.deploy(vmDescription2, null);
		
		//get the list of IDs
		List<String> ids = openStackJclouds.getAllVMsIds();
		
		//check that the two VMs exist
		assertTrue(ids.contains(instanceId1) && ids.contains(instanceId2));
		
		//delete the two VMs
		openStackJclouds.destroy(instanceId1);
		openStackJclouds.destroy(instanceId2);
	}
	
	@Test
	public void getVMInfo() throws Exception {
		//deploy a VM
		String instanceId = openStackJclouds.deploy(new Vm("TestVM1", testingImageId, 1, 1024, 2, null, "app1"), null);
		
		//check that the information of the VM is correct
		Vm resultVmDescription = openStackJclouds.getVM(instanceId);
		Assert.assertEquals("TestVM1", resultVmDescription.getName());
		Assert.assertEquals(testingImageId, resultVmDescription.getImage());
		assertTrue(resultVmDescription.getCpus() == 1);
		assertTrue(resultVmDescription.getRamMb() == 1024);
		assertTrue(resultVmDescription.getDiskGb() == 2);
		
		//destroy the VM 
		openStackJclouds.destroy(instanceId);
		
		//check that the function returns null when there is not a VM with the id specified
		assertNull(openStackJclouds.getVM(instanceId));
	}

    @Test
    public void getNonExistingImageReturnsNull() {
        assertNull(openStackJclouds.getVmImage("fakeImage"));
    }

	@Test
	public void migrateTest() {
		// TODO perform this test only if there are 2 or more nodes in the infrastructure
	}
}
