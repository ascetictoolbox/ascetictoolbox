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

package es.bsc.demiurge.fake;

import es.bsc.demiurge.core.models.images.ImageUploaded;
import es.bsc.demiurge.core.models.vms.Vm;
import es.bsc.demiurge.core.cloudmiddleware.CloudMiddleware;
import es.bsc.demiurge.core.models.hosts.HardwareInfo;
import es.bsc.demiurge.core.models.images.ImageToUpload;
import es.bsc.demiurge.core.models.vms.VmDeployed;
import es.bsc.demiurge.core.models.vms.VmRequirements;

import java.util.*;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * This class simulates a fake middleware. It stores all the information about hosts and VMs in memory, without
 * using a middleware such as OpenStack or OpenNebula. This 'fake' middleware can be useful to perform some
 * tests or simulations.
 *
 * @author Mario Macias (github.com/mariomac), David Ortiz Lopez (david.ortiz@bsc.es)
 */
public class FakeCloudMiddleware implements CloudMiddleware {

    // Note: The methods that perform operations on VMs are not needed.
    // They would be needed if knowing the current state of a Vm ("active", "deleting", etc.) was required.

    // Several threads can access the following attributes at the same time.
    // We need to make sure that we will not fall into an inconsistent state (for example, trying to assign
    // twice the same IDs. Therefore, we need to use the java.util.concurrent classes.
    private final List<HostFake> hosts = new CopyOnWriteArrayList<>();
    private final List<VmDeployed> deployedVms = new CopyOnWriteArrayList<>();
    private final List<ImageUploaded> images = new CopyOnWriteArrayList<>();

    // For assigning IDs and IPs manually
    private final AtomicInteger nextVmId = new AtomicInteger(0);
    private final AtomicInteger nextVmIp = new AtomicInteger(0); // Invalid IPs but it does not matter
    private final AtomicInteger nextImageId = new AtomicInteger(0);

    public FakeCloudMiddleware(List<HostFake> hosts) {
        for (HostFake host: hosts) {
            this.hosts.add(host);
        }
    }

    @Override
    public String deploy(Vm vm, String hostname) {
        HostFake host = getHost(hostname);
        if (host == null) {
            throw new IllegalArgumentException(
                    "Error during VM deployment. There is not a host with the given hostname.");
        }

        if (getVmImage(vm.getImage()) == null) {
            throw new IllegalArgumentException(
                    "Error during VM deployment. There is not an image with the given ID.");
        }

        VmDeployed newVm = new VmDeployed(
                vm.getName(),
                vm.getImage(),
                vm.getCpus(),
                vm.getRamMb(),
                vm.getDiskGb(),
                vm.getSwapMb(),
                vm.getInitScript(),
                vm.getApplicationId(),
                nextVmId.toString(),
                nextVmIp.toString(),
                "active",
                new Date(),
                hostname
            );

        deployedVms.add(newVm);
        host.updateAssignedResourcesAfterVmDeployed(vm);
        nextVmId.getAndIncrement();
        nextVmIp.getAndIncrement();
        return newVm.getId();
    }

    @Override
    public String deployWithVolume(Vm vm, String hostname, String isoPath) {
        // Not used in the fake cloud. Simply do a standard deployment
        return deploy(vm, hostname);
    }

    @Override
    public void destroy(String vmId) {
        for (int i = 0; i < deployedVms.size(); ++i) {
            if (vmId.equals(deployedVms.get(i).getId())) {
                HostFake host = getHost(deployedVms.get(i).getHostName());
                host.updateAssignedResourcesAfterVmDeleted(deployedVms.get(i));
                deployedVms.remove(i);
                return;
            }
        }
    }

    @Override
    public void migrate(String vmId, String destinationNodeHostName) {
        VmDeployed vmToMigrate = getVmById(vmId);
        if (vmToMigrate != null) {
            HostFake oldHost = getHost(vmToMigrate.getHostName());
            HostFake newHost = getHost(destinationNodeHostName);

            if (newHost != null) {
                vmToMigrate.setHostName(destinationNodeHostName);
                oldHost.updateAssignedResourcesAfterVmDeleted(vmToMigrate);
                newHost.updateAssignedResourcesAfterVmDeployed(vmToMigrate);
            }
        }
    }

    @Override
    public List<String> getAllVMsIds() {
        List<String> result = new ArrayList<>();
        for (VmDeployed vmDeployed: deployedVms) {
            result.add(vmDeployed.getId());
        }
        return result;
    }

    @Override
    public List<String> getScheduledNonDeployedVmsIds() {
        // In this simulated cloud middleware, we assume that deployments do not have a delay.
        // Therefore, there cannot be VMs that are scheduled but not deployed.
        return new ArrayList<>();
    }

    @Override
    public VmDeployed getVM(String vmId) {
        return getVmById(vmId);
    }

    @Override
    public boolean existsVm(String vmId) {
        return getVmById(vmId) != null;
    }

    @Override
    public void rebootHardVm(String vmId) {
        // Does not apply
    }

    @Override
    public void rebootSoftVm(String vmId) {
        // Does not apply
    }

    @Override
    public void startVm(String vmId) {
        // Does not apply
    }

    @Override
    public void stopVm(String vmId) {
        // Does not apply
    }

    @Override
    public void suspendVm(String vmId) {
        // Does not apply
    }

    @Override
    public void resumeVm(String vmId) {
        // Does not apply
    }

    @Override
    public List<ImageUploaded> getVmImages() {
        return Collections.unmodifiableList(images);
    }

    @Override
    public String createVmImage(ImageToUpload imageToUpload) {
        ImageUploaded newImage = new ImageUploaded(
                nextImageId.toString(),
                imageToUpload.getName(),
                "active");
        images.add(newImage);
        nextImageId.getAndIncrement();
        return newImage.getId();
    }

    @Override
    public ImageUploaded getVmImage(String imageId) {
        for (ImageUploaded image: images) {
            if (imageId.equals(image.getId())) {
                return image;
            }
        }
        return null;
    }

    @Override
    public void deleteVmImage(String id) {
        for (int i = 0; i < images.size(); ++i) {
            if (id.equals(images.get(i).getId())) {
                images.remove(i);
                return;
            }
        }
    }

    @Override
    public void assignFloatingIp(String vmId) {
        // Does not apply
    }

    public void deleteAllVmImages() {
        images.clear();
    }

    public HostFake getHost(String hostname) {
        for (HostFake host: hosts) {
            if (hostname.equals(host.getHostname())) {
                return host;
            }
        }
        return null;
    }

    public void addHost(HostFake host) {
        hosts.add(host);
    }

    private VmDeployed getVmById(String id) {
        for (VmDeployed vmDeployed: deployedVms) {
            if (id.equals(vmDeployed.getId())) {
                return vmDeployed;
            }
        }
        return null;
    }

	@Override
	public Map<String, String> getFlavours() {
		return null;
	}
    
    @Override
	public void resize(String vmId, VmRequirements vm) {

	}
    
    @Override
	public void confirmResize(String vmId) {

	}

    @Override
    public Map<String, HardwareInfo> getHypervisors(String region) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
}
