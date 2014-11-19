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

package es.bsc.vmmanagercore.cloudmiddleware.fake;

import es.bsc.vmmanagercore.model.images.ImageToUpload;
import es.bsc.vmmanagercore.model.images.ImageUploaded;
import es.bsc.vmmanagercore.model.vms.Vm;
import es.bsc.vmmanagercore.model.vms.VmDeployed;
import es.bsc.vmmanagercore.monitoring.HostFake;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.*;

/**
 * Tests for FakeCloudMiddleware.
 *
 * @author David Ortiz Lopez (david.ortiz@bsc.es)
 */
public class FakeCloudMiddlewareTest {

    private static FakeCloudMiddleware fakeCloudMiddleware;

    @Rule
    public ExpectedException exception = ExpectedException.none();

    @BeforeClass
    public static void setUpBeforeClass() {
        // Initialize the hosts that are going to be used for the tests
        List<HostFake> hosts = new ArrayList<>();
        hosts.add(new HostFake("host1", 2, 2048, 2, 0, 0, 0));
        hosts.add(new HostFake("host2", 4, 4096, 4, 0, 0, 0));
        hosts.add(new HostFake("host3", 8, 8192, 8, 0, 0, 0));

        fakeCloudMiddleware = new FakeCloudMiddleware(hosts);
    }

    @Before
    public void initialize() {
        // Delete all the VMs deployed and the images uploaded so each test can start from a fresh state
        List<String> deployedVmsIds = fakeCloudMiddleware.getAllVMsIds();
        for (String id: deployedVmsIds) {
            fakeCloudMiddleware.destroy(id);
        }
        fakeCloudMiddleware.deleteAllVmImages();
    }

    @Test
    public void deployVm() {
        String idDeployedVm = fakeCloudMiddleware.deploy(new Vm("testVm", "image", 1, 1024, 1, null, null), "host1");

        // Check that the VM was created correctly
        VmDeployed vmDeployed = fakeCloudMiddleware.getVM(idDeployedVm);
        assertEquals(idDeployedVm, vmDeployed.getId());
        assertEquals("testVm", vmDeployed.getName());
        assertEquals("image", vmDeployed.getImage());
        assertTrue(vmDeployed.getCpus() == 1);
        assertTrue(vmDeployed.getRamMb() == 1024);
        assertTrue(vmDeployed.getDiskGb() == 1);
        assertEquals("host1", vmDeployed.getHostName());
        assertNull(vmDeployed.getInitScript());
        assertNull(vmDeployed.getApplicationId());
        assertNotNull(vmDeployed.getIpAddress());
        assertNotNull(vmDeployed.getCreated());
        assertNotNull(vmDeployed.getState());

        // Check that the assigned resources of the host were updated successfully
        HostFake host = fakeCloudMiddleware.getHost(vmDeployed.getHostName());
        assertTrue(host.getAssignedCpus() == 1 && host.getAssignedMemoryMb() == 1024 && host.getAssignedDiskGb() == 1);
    }

    @Test
    public void deployVmInNonExistingHostRaisesException() {
        exception.expect(IllegalArgumentException.class);
        fakeCloudMiddleware.deploy(new Vm("testVm", "image", 1, 1024, 1, null, null), "nonExistingId");
    }

    @Test
    public void destroyVm() {
        String idDeployedVm = fakeCloudMiddleware.deploy(new Vm("testVm", "image", 1, 1024, 1, null, null), "host1");
        fakeCloudMiddleware.destroy(idDeployedVm);

        // Check that the VM no longer exists
        assertNull(fakeCloudMiddleware.getVM(idDeployedVm));

        // Check that the assigned resources of the host were updated successfully
        HostFake host = fakeCloudMiddleware.getHost("host1");
        assertTrue(host.getAssignedCpus() == 0 && host.getAssignedMemoryMb() == 0 && host.getAssignedDiskGb() == 0);
    }

    @Test
    public void destroyNonExistingVmDoesNotRaiseException() {
        fakeCloudMiddleware.destroy("thisIdDoesNotExist");
    }

    @Test
    public void migrate() {
        String idDeployedVm = fakeCloudMiddleware.deploy(new Vm("testVm", "image", 1, 1024, 1, null, null), "host1");
        fakeCloudMiddleware.migrate(idDeployedVm, "host2");

        // Check that the hostname in the Vm changed
        assertEquals("host2", fakeCloudMiddleware.getVM(idDeployedVm).getHostName());

        // Check that the assigned resources of the host were updated successfully on both hosts
        HostFake oldHost = fakeCloudMiddleware.getHost("host1");
        assertTrue(oldHost.getAssignedCpus() == 0);
        assertTrue(oldHost.getAssignedMemoryMb() == 0);
        assertTrue(oldHost.getAssignedDiskGb() == 0);

        HostFake newHost = fakeCloudMiddleware.getHost("host2");
        assertTrue(newHost.getAssignedCpus() == 1);
        assertTrue(newHost.getAssignedMemoryMb() == 1024);
        assertTrue(newHost.getAssignedDiskGb() == 1);
    }

    @Test
    public void migrateNonExistingVmDoesNothingAndDoesNotRaiseException() {
        fakeCloudMiddleware.migrate("NonExistingVm", "host1");
    }

    @Test
    public void migrateToNonExistingHostDoesNothingAndDoesNotRaiseException() {
        String idDeployedVm = fakeCloudMiddleware.deploy(new Vm("testVm", "image", 1, 1024, 1, null, null), "host1");
        fakeCloudMiddleware.migrate(idDeployedVm, "nonExistingHost");
        assertEquals("host1", fakeCloudMiddleware.getVM(idDeployedVm).getHostName());
    }

    @Test
    public void getAllVms() {
        String idDeployedVm1 = fakeCloudMiddleware.deploy(new Vm("testVm1", "image", 1, 1024, 1, null, null), "host1");
        String idDeployedVm2 = fakeCloudMiddleware.deploy(new Vm("testVm2", "image", 1, 1024, 1, null, null), "host2");
        String idDeployedVm3 = fakeCloudMiddleware.deploy(new Vm("testVm3", "image", 1, 1024, 1, null, null), "host3");

        List<String> idsDeployedVms = fakeCloudMiddleware.getAllVMsIds();
        assertTrue(idsDeployedVms.contains(idDeployedVm1));
        assertTrue(idsDeployedVms.contains(idDeployedVm2));
        assertTrue(idsDeployedVms.contains(idDeployedVm3));
    }

    @Test
    public void getAllVmsIdsReturnsEmptyListWhenThereAreNoVms() {
        assertTrue(fakeCloudMiddleware.getAllVMsIds().isEmpty());
    }

    @Test
    public void getVmReturnsNullWhenVmDoesNotExist() {
        assertNull(fakeCloudMiddleware.getVM("fakeId"));
    }

    @Test
    public void getVmImages() {
        String newImageId = fakeCloudMiddleware.createVmImage(new ImageToUpload("newImage", "fakeUrl"));
        ImageUploaded imageUploaded = fakeCloudMiddleware.getVmImage(newImageId);
        assertNotNull(imageUploaded.getId());
        assertEquals("newImage", imageUploaded.getName());
        assertNotNull(imageUploaded.getStatus());
    }

    @Test
    public void getVmImagesReturnsEmptyListWhenThereAreNoImages() {
        assertTrue(fakeCloudMiddleware.getVmImages().isEmpty());
    }

    @Test
    public void deleteVmImage() {
        String newImageId = fakeCloudMiddleware.createVmImage(new ImageToUpload("newImage", "fakeUrl"));
        fakeCloudMiddleware.deleteVmImage(newImageId);
        assertTrue(fakeCloudMiddleware.getVmImages().isEmpty());
    }

    @Test
    public void deleteVmImageDoesNothingAndRaisesNoExceptionsWhenInvalidId() {
        fakeCloudMiddleware.deleteVmImage("invalidId");
    }

    @Test
    public void getHost() {
        HostFake host = fakeCloudMiddleware.getHost("host1");
        assertEquals("host1", host.getHostname());
        assertTrue(host.getTotalCpus() == 2);
        assertTrue(host.getTotalMemoryMb() == 2048);
        assertTrue(host.getTotalDiskGb() == 2);
        assertTrue(host.getAssignedCpus() == 0);
        assertTrue(host.getAssignedMemoryMb() == 0);
        assertTrue(host.getAssignedDiskGb() == 0);
    }

    @Test
    public void getHostReturnsNullWhenDoesNotExist() {
        assertNull(fakeCloudMiddleware.getHost("NonExistingHost"));
    }

}
