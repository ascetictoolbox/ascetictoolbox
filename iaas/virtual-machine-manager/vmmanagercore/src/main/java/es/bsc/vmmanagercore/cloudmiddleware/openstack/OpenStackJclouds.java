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

package es.bsc.vmmanagercore.cloudmiddleware.openstack;

import es.bsc.vmmanagercore.cloudmiddleware.CloudMiddleware;
import es.bsc.vmmanagercore.model.images.ImageToUpload;
import es.bsc.vmmanagercore.model.images.ImageUploaded;
import es.bsc.vmmanagercore.model.vms.Vm;
import es.bsc.vmmanagercore.model.vms.VmDeployed;
import org.apache.commons.io.IOUtils;
import org.apache.commons.validator.UrlValidator;
import org.jclouds.openstack.nova.v2_0.NovaApi;
import org.jclouds.openstack.nova.v2_0.domain.*;
import org.jclouds.openstack.nova.v2_0.options.CreateServerOptions;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

/**
 * Class that performs requests to OpenStack using the JClouds library.
 *
 * @author David Ortiz Lopez (david.ortiz@bsc.es)
 */
public class OpenStackJclouds implements CloudMiddleware {

    /* NOTE: in the deployment function, I am assuming that there is only one OpenStack zone configured.
    The rest of the functions should work fine even with several zones configured, but this scenario has
    not been tested. */

    // OpenStack default flavors
    public static final String[] DEFAULT_FLAVORS = new String[] {"1", "2", "3", "4", "5"};

    // OpenStack VMs state
    private static final String ACTIVE = "active";
    private static final String BUILD = "BUILD";
    private static final String DELETING = "deleting";

    private final String zone; // This could be important/problematic in the future because I am assuming that
                         // the cluster only has one zone configured for deployments.
    private final String[] securityGroups;

    private final OpenStackJcloudsApis openStackJcloudsApis;
    private final OpenStackGlance glanceConnector; // Connector for OS Glance

    private final int BLOCKING_TIME_SEC_DEPLOY_AND_DESTROY = 1000;

    /**
     * Class constructor. It performs the connection to the infrastructure and initializes
     * JClouds attributes.
     *
     * @param openStackCredentials OpenStack credentials
     * @param securityGroups the security groups to which the VM will be part of
     */
    public OpenStackJclouds(OpenStackCredentials openStackCredentials, String[] securityGroups) {
        openStackJcloudsApis = new OpenStackJcloudsApis(openStackCredentials);
        zone = openStackJcloudsApis.getNovaApi().getConfiguredZones().toArray()[0].toString();
        this.securityGroups = securityGroups;
        glanceConnector = new OpenStackGlance(openStackCredentials);
    }

    @Override
    // This function is blocking. The thread execution blocks until the VM is deployed.
    // I would prefer to see this blocking in the VmManager class. I need to block the execution because
    // I need to return the ID of the deployed VM to the Application Manager.
    public String deploy(Vm vm, String hostname) {
        // Deploy the VM
        ServerCreated server = openStackJcloudsApis.getServerApi().create(
                vm.getName(),
                getImageIdForDeployment(vm),
                getFlavorIdForDeployment(vm),
                getDeploymentOptionsForVm(vm, hostname, securityGroups));

        blockUntilVmIsDeployed(server.getId());
        return server.getId();
    }

    @Override
    // This function is also blocking. The reason is the same as in the deploy function.
    public void destroy(String vmId) {
        Server server = openStackJcloudsApis.getServerApi().get(vmId);
        if (server != null) { // If the VM is in the zone
            openStackJcloudsApis.getServerApi().delete(vmId);
            blockUntilVmIsDeleted(vmId);
        }
    }

    @Override
    public void migrate(String vmId, String destinationNodeHostName) {
        if (openStackJcloudsApis.getServerApi().get(vmId) != null) {
            openStackJcloudsApis.getServerAdminApi().liveMigrate(vmId, destinationNodeHostName, false, false);
        }
    }

    @Override
    public List<String> getAllVMsIds() {
        List<String> vmIds = new ArrayList<>();
        for (Server server: openStackJcloudsApis.getServerApi().listInDetail().concat()) {
            ServerExtendedStatus vmStatus = server.getExtendedStatus().get();

            // Add the VM to the result if it is active and it is not being deleted
            boolean vmIsActive = ACTIVE.equals(vmStatus.getVmState());
            boolean vmIsBeingDeleted = DELETING.equals(vmStatus.getTaskState());
            if (vmIsActive && !vmIsBeingDeleted) {
                vmIds.add(server.getId());
            }
        }
        return vmIds;
    }

    @Override
    public VmDeployed getVM(String vmId) {
        VmDeployed vm = null;
        Server server = openStackJcloudsApis.getServerApi().get(vmId);

        // If the VM is in the zone
        if (server != null ) {
            // Get the information of the VM if it is active and it is not being deleted
            ServerExtendedStatus vmStatus = server.getExtendedStatus().get();
            boolean vmIsActive = ACTIVE.equals(vmStatus.getVmState());
            boolean vmIsBeingDeleted = DELETING.equals(vmStatus.getTaskState());
            if (vmIsActive && !vmIsBeingDeleted) {
                Flavor flavor = openStackJcloudsApis.getFlavorApi().get(server.getFlavor().getId());
                String vmIp = getVmIp(server);
                int swapMb = 0;
                if (flavor.getSwap().isPresent() && !flavor.getSwap().get().equals("")) {
                    swapMb = Integer.parseInt(flavor.getSwap().get());
                }
                vm = new VmDeployed(server.getName(),
                        server.getImage().getId(), flavor.getVcpus(), flavor.getRam(),
                        flavor.getDisk(), swapMb, null, null, vmId,
                        vmIp, server.getStatus().toString(), server.getCreated(),
                        server.getExtendedAttributes().get().getHostName());
            }
        }

        return vm;
    }

    @Override
    public boolean existsVm(String vmId) {
        return openStackJcloudsApis.getServerApi().get(vmId) != null;
    }

    @Override
    public void rebootHardVm(String vmId) {
        openStackJcloudsApis.getServerApi().reboot(vmId, RebootType.HARD);
    }
    
    @Override
    public void rebootSoftVm(String vmId) {
        openStackJcloudsApis.getServerApi().reboot(vmId, RebootType.SOFT);
    }
    
    @Override
    public void startVm(String vmId) {
        openStackJcloudsApis.getServerApi().start(vmId);
    }

    @Override
    public void stopVm(String vmId) {
        openStackJcloudsApis.getServerApi().stop(vmId);
    }

    @Override
    public void suspendVm(String vmId) {
        openStackJcloudsApis.getServerAdminApi().suspend(vmId);
    }

    @Override
    public void resumeVm(String vmId) {
        openStackJcloudsApis.getServerAdminApi().resume(vmId);
    }

    @Override
    public List<ImageUploaded> getVmImages() {
        List<ImageUploaded> vmImages = new ArrayList<>();
        for (Image image: openStackJcloudsApis.getImageApi().listInDetail().concat()) {
            vmImages.add(new ImageUploaded(image.getId(), image.getName(), image.getStatus().toString()));
        }
        return vmImages;
    }

    @Override
    public String createVmImage(ImageToUpload imageToUpload) {
        return glanceConnector.createImageFromUrl(imageToUpload);
    }

    @Override
    public ImageUploaded getVmImage(String imageId) {
        Image image = openStackJcloudsApis.getImageApi().get(imageId);
        return image != null ? new ImageUploaded(image.getId(), image.getName(), image.getStatus().toString()) : null;
    }

    @Override
    public void deleteVmImage(String id) {
        glanceConnector.deleteImage(id);
    }

    /**
     * @return the zone
     */
    public String getZone() {
        return zone;
    }

    /**
     * @return NovaApi object associated with the OpenStackJclouds class
     */
    public NovaApi getNovaApi() {
        return openStackJcloudsApis.getNovaApi();
    }

    /**
     * Returns the ID of the flavor that a VM should use when it is deployed.
     * If a flavor with the specs of the VM already exists, the function returns the ID of that flavor.
     * Otherwise, the function creates a new flavor and returns its ID.
     *
     * @param vm the VM to be deployed
     * @return the flavor ID that the VM should use
     */
    private String getFlavorIdForDeployment(Vm vm) {
        // If there is a flavor with the same specs as the ones we need to use return it
        String flavorId = getFlavorId(vm.getCpus(), vm.getRamMb(), vm.getDiskGb(), vm.getSwapMb());
        if (flavorId != null) {
            return flavorId;
        }

        //If the flavor does not exist, create it and return the ID
        String id, name;
        id = name = vm.getCpus() + "-" + vm.getDiskGb() + "-" + vm.getRamMb() + "-" + vm.getSwapMb();
        return createFlavor(id, name, vm.getCpus(), vm.getRamMb(), vm.getDiskGb(), vm.getSwapMb());
    }

    /**
     * Returns the ID of the image that a VM should use when it is deployed.
     *
     * @param vm the VM to be deployed
     * @return the ID of the image
     */
    private String getImageIdForDeployment(Vm vm) {
        // If the vm description contains a URL, create the image using Glance and return its ID
        if (new UrlValidator().isValid(vm.getImage())) {
            return glanceConnector.createImageFromUrl(new ImageToUpload(vm.getImage(), vm.getImage()));
        }
        else { // If the VM description contains the ID of an image, return it unless there is an error
            String imageId = vm.getImage();
            if (!existsImageWithId(imageId)) {
                throw new IllegalArgumentException("There is not an image with the specified ID");
            }
            if (!glanceConnector.imageIsActive(imageId)) {
                throw new IllegalArgumentException("The image specified is not active");
            }
            return imageId;
        }
    }

    /**
     * Retrieves the deployment options needed for deploying a VM in a specific host.
     *
     * @param vm the VM to be deployed
     * @param hostname the hostname
     * @param securityGroups the security groups to which the VM will be part of
     *
     * @return the deployment options
     */
    private CreateServerOptions getDeploymentOptionsForVm(Vm vm, String hostname, String[] securityGroups) {
        CreateServerOptions options = new CreateServerOptions();
        includeDstNodeInDeploymentOption(hostname, options);
        includeInitScriptInDeploymentOptions(vm, options);
        includeSecurityGroupInDeploymentOption(options, securityGroups);
        return options;
    }

    /**
     * Specifies in the VM deployment options the server on which to deploy the VM.
     *
     * @param dstNode the node where to deploy the VM
     * @param options VM deployment options
     */
    private void includeDstNodeInDeploymentOption(String dstNode, CreateServerOptions options) {
        if (dstNode != null) {
            options.availabilityZone("nova:" + dstNode);
        }
    }

    /**
     * Specifies in the VM deployment options a script that will be executed when the VM is deployed.
     *
     * @param vmDescription VM that will use the init script
     * @param options VM deployment options
     */
    private void includeInitScriptInDeploymentOptions(Vm vmDescription, CreateServerOptions options) {
        String initScript = vmDescription.getInitScript();
        if (initScript != null && !"".equals(initScript)) {
            try {
                InputStream inputStream = new FileInputStream(initScript);
                options.userData(IOUtils.toByteArray(inputStream));
                inputStream.close();
            } catch (IOException e) {
                // If a file path was not received, it means that we received a string containing the
                // script directly.
                options.userData(initScript.getBytes());
            }
        }
    }

    private void includeSecurityGroupInDeploymentOption(CreateServerOptions options, String[] securityGroups) {
        if (securityGroups.length > 0) {
            options.securityGroupNames(securityGroups);
        }
    }

    /**
     * Gets the ID of the flavor with the specified characteristics.
     * @param cpus the number of CPUs of the flavor
     * @param memoryMb the amount of RAM of the flavor in MB
     * @param diskGb the amount of disk space of the flavor in GB
     * @param swapMb the amount of swap in MB
     * @return The ID of the flavor. Null if a flavor with the specified characteristics does not exist.
     */
    private String getFlavorId(int cpus, int memoryMb, int diskGb, int swapMb) {
        for (Flavor flavor: openStackJcloudsApis.getFlavorApi().listInDetail().concat()) {
            if (flavor.getVcpus() == cpus && flavor.getRam() == memoryMb && flavor.getDisk() == diskGb) {
                boolean swapGreaterThanZero = (flavor.getSwap().isPresent() && !flavor.getSwap().get().equals(""));
                if ((!swapGreaterThanZero && swapMb == 0)
                        || (swapGreaterThanZero && (swapMb == Integer.parseInt(flavor.getSwap().get())))) {
                    return flavor.getId();
                }
            }
        }
        return null;
    }

    /**
     * Creates a flavor with the specified characteristics.
     * @param cpus the number of CPUs of the flavor
     * @param ramMb the amount of RAM of the flavor in MB
     * @param diskGb the amount of disk space of the flavor in GB
     * @param swapMb the amount of swap of the flavor in MB
     * @return The ID of the created flavor.
     */
    private String createFlavor(String id, String name, int cpus, int ramMb, int diskGb, int swapMb) {
        openStackJcloudsApis.getFlavorApi().create(
                Flavor.builder().id(id).name(name).vcpus(cpus).ram(ramMb).disk(diskGb)
                        .swap(Integer.toString(swapMb)).build());
        return id;
    }

    /**
     * Checks whether an image with the given ID has been uploaded to the infrastructure.
     *
     * @param id the ID of the image
     * @return true if an image exists with the given Id, false otherwise
     */
    private boolean existsImageWithId(String id) {
        return openStackJcloudsApis.getImageApi().get(id) != null;
    }

    /**
     * Returns the IP of a VM.
     *
     * @param server the VM
     * @return the IP of the VM
     */
    private String getVmIp(Server server) {
        List<Address> addresses = new ArrayList<>(server.getAddresses().values());
        return addresses.get(0).getAddr(); // Important: this returns only 1 IP, but VMs can have more than 1.
    }

    /**
     * Blocks the thread execution until a specific VM is deployed.
     *
     * @param vmId the ID of the VM
     */
    private void blockUntilVmIsDeployed(String vmId) {
        while (openStackJcloudsApis.getServerApi().get(vmId).getStatus().toString().equals(BUILD)) {
            try {
                Thread.sleep(BLOCKING_TIME_SEC_DEPLOY_AND_DESTROY);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * Blocks the thread execution until a specific VM is deleted.
     *
     * @param vmId the ID of the VM
     */
    private void blockUntilVmIsDeleted(String vmId) {
        while (openStackJcloudsApis.getServerApi().get(vmId).getStatus().toString().equals(DELETING)) {
            try {
                Thread.sleep(BLOCKING_TIME_SEC_DEPLOY_AND_DESTROY);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

}