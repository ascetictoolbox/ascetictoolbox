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

import es.bsc.vmmanagercore.cloudmiddleware.CloudMiddleware;
import es.bsc.vmmanagercore.model.images.ImageToUpload;
import es.bsc.vmmanagercore.model.images.ImageUploaded;
import es.bsc.vmmanagercore.model.vms.Vm;
import es.bsc.vmmanagercore.model.vms.VmDeployed;
import es.bsc.vmmanagercore.monitoring.HostFake;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;

/**
 * This class simulates a fake middleware. It stores all the information about hosts and VMs in memory, without
 * using a middleware such as OpenStack or OpenNebula.
 *
 * @author David Ortiz Lopez (david.ortiz@bsc.es)
 */
public class FakeCloudMiddleware implements CloudMiddleware {

    // Note: The methods that perform operations on VMs are not needed.
    // They would be needed if knowing the current state of a Vm ("active", "deleting", etc.) was required.

    private List<HostFake> hosts = new ArrayList<>();
    private List<VmDeployed> deployedVms = new ArrayList<>();
    private List<ImageUploaded> images = new ArrayList<>();

    // For assigning IDs and IPs manually
    private int nextVmId = 0;
    private int nextVmIp = 0; // Invalid IPs but it does not matter
    private int nextImageId = 0;

    public FakeCloudMiddleware(List<HostFake> hosts) {
        for (HostFake host: hosts) {
            this.hosts.add(host);
        }
    }

    @Override
    public String deploy(Vm vm, String hostname) {
        HostFake host = getHost(hostname);
        if (host == null) {
            throw new IllegalArgumentException("There is not a host with the given hostname.");
        }

        VmDeployed newVm = new VmDeployed(
                vm.getName(),
                vm.getImage(),
                vm.getCpus(),
                vm.getRamMb(),
                vm.getDiskGb(),
                vm.getInitScript(),
                vm.getApplicationId(),
                Integer.toString(nextVmId),
                Integer.toString(nextVmIp),
                "active",
                new Date(),
                hostname
            );

        deployedVms.add(newVm);
        host.updateAssignedResourcesAfterVmDeployed(vm);
        ++nextVmId;
        ++nextVmIp;
        return newVm.getId();
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
                Integer.toString(nextImageId),
                imageToUpload.getName(),
                "active");
        images.add(newImage);
        ++nextImageId;
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

    private VmDeployed getVmById(String id) {
        for (VmDeployed vmDeployed: deployedVms) {
            if (id.equals(vmDeployed.getId())) {
                return vmDeployed;
            }
        }
        return null;
    }

}
