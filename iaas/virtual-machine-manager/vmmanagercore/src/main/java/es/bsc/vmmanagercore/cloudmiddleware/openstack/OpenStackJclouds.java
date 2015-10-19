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
import es.bsc.vmmanagercore.cloudmiddleware.CloudMiddlewareException;
import es.bsc.vmmanagercore.models.images.ImageToUpload;
import es.bsc.vmmanagercore.models.images.ImageUploaded;
import es.bsc.vmmanagercore.models.vms.Vm;
import es.bsc.vmmanagercore.models.vms.VmDeployed;
import org.apache.commons.io.IOUtils;
import org.apache.commons.validator.UrlValidator;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.jclouds.openstack.neutron.v2.NeutronApi;
import org.jclouds.openstack.nova.v2_0.NovaApi;
import org.jclouds.openstack.nova.v2_0.domain.*;
import org.jclouds.openstack.nova.v2_0.extensions.FloatingIPApi;
import org.jclouds.openstack.nova.v2_0.options.CreateServerOptions;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.*;

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

	private Set<String> hostNames = new TreeSet<>();

	private Logger logger = LogManager.getLogger(OpenStackJclouds.class);


    /**
     * Class constructor. It performs the connection to the infrastructure and initializes
     * JClouds attributes.
     *
     * @param openStackCredentials OpenStack credentials
     * @param securityGroups the security groups to which the VM will be part of
     */
    public OpenStackJclouds(OpenStackCredentials openStackCredentials, String[] securityGroups, String[] hostNames) {
        openStackJcloudsApis = new OpenStackJcloudsApis(openStackCredentials);
        zone = openStackJcloudsApis.getNovaApi().getConfiguredZones().toArray()[0].toString();
        this.securityGroups = securityGroups;
        glanceConnector = new OpenStackGlance(openStackCredentials);
		this.hostNames.addAll(Arrays.asList(hostNames));
    }

	private void assertHostName(String hostname) throws CloudMiddlewareException {
		if(hostname != null && !hostNames.contains(hostname)) throw new CloudMiddlewareException("Host " + hostname + " is not registered in this VMM");
	}
	private void assertVmId(String vmId) throws CloudMiddlewareException {
		Server server = openStackJcloudsApis.getServerApi().get(vmId);
		if (server == null ) {
			throw new CloudMiddlewareException("Unexistent VM ID: " + vmId);
		} else {
			String vmHost = server.getExtendedAttributes().get().getHostName();
			if(vmHost != null && !hostNames.contains(vmHost)) {
				throw new CloudMiddlewareException("The VM " + vmId + " exists in the middleware but its node ("+vmHost+") is not registered within this VMM");
			}
		}
	}

    @Override
    // This function is blocking. The thread execution blocks until the VM is deployed.
    // I would prefer to see this blocking in the VmManager class. I need to block the execution because
    // I need to return the ID of the deployed VM to the Application Manager.
    public String deploy(Vm vm, String hostname) throws CloudMiddlewareException {
        // Deploy the VM
		assertHostName(hostname);
        try {
            ServerCreated server = openStackJcloudsApis.getServerApi().create(
                    vm.getName(),
                    getImageIdForDeployment(vm),
                    getFlavorIdForDeployment(vm),
                    getDeploymentOptionsForVm(vm, hostname, securityGroups, false, null));

            blockUntilVmIsDeployed(server.getId());
            return server.getId();
        } catch(Exception e) {
            throw new CloudMiddlewareException(e.getMessage(),e);
        }
    }

    @Override
    public String deployWithVolume(Vm vm, String hostname, String isoPath)  throws CloudMiddlewareException {
        // Deploy the VM
		assertHostName(hostname);
		try {
            ServerCreated server = openStackJcloudsApis.getServerApi().create(
                    vm.getName(),
                    getImageIdForDeployment(vm),
                    getFlavorIdForDeployment(vm),
                    getDeploymentOptionsForVm(vm, hostname, securityGroups, true, isoPath));

            blockUntilVmIsDeployed(server.getId());
            return server.getId();
        } catch (Exception e) {
            throw new CloudMiddlewareException(e.getMessage(),e);
        }
    }

    @Override
    // This function is also blocking. The reason is the same as in the deploy function.
    public void destroy(String vmId) throws CloudMiddlewareException {
		assertVmId(vmId);
        Server server = openStackJcloudsApis.getServerApi().get(vmId);
        if (server != null) { // If the VM is in the zone
            openStackJcloudsApis.getServerApi().delete(vmId);
            blockUntilVmIsDeleted(vmId);
        }
    }

    @Override
    public void migrate(String vmId, String destinationNodeHostName) throws CloudMiddlewareException {
		assertHostName(destinationNodeHostName);
		assertVmId(vmId);
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

			// if the host is null, proceed
			boolean containsHost = true;
			try {
				containsHost = hostNames.contains(server.getExtendedAttributes().get().getHostName());
			} catch(Exception e) {
				logger.warn("Error checking if vm is in host: " + e.getMessage(), e);
			}
            if (vmIsActive && !vmIsBeingDeleted && containsHost) {
                vmIds.add(server.getId());
            }
        }
        return vmIds;
    }

    @Override
    public List<String> getScheduledNonDeployedVmsIds() {
        List<String> result = new ArrayList<>();
        for (Server server: openStackJcloudsApis.getServerApi().listInDetail().concat()) {
            ServerExtendedStatus vmStatus = server.getExtendedStatus().get();

            // Here, the state is "building", but using a different API is "BUILD".
            // Explore why and add the appropriate constant.
            boolean vmIsBuilding = vmStatus.getVmState().equals("building");
            boolean vmIsBeingDeleted = DELETING.equals(vmStatus.getTaskState());
			// if the host is null, proceed
			boolean containsHost = true;
			try {
				containsHost = hostNames.contains(server.getExtendedAttributes().get().getHostName());
			} catch(Exception e) {
				logger.warn("Error checking if vm is in host: " + e.getMessage(), e);
			}
			if (vmIsBuilding && !vmIsBeingDeleted && containsHost) {
				result.add(server.getId());
            }
        }
        return result;
    }

    @Override
    public VmDeployed getVM(String vmId) throws CloudMiddlewareException {
        VmDeployed vm = null;
		assertVmId(vmId);

        Server server = openStackJcloudsApis.getServerApi().get(vmId);
        // If the VM is in the zone
        if (server != null ) {
            // Get the information of the VM if it is active and it is not being deleted
            ServerExtendedStatus vmStatus = server.getExtendedStatus().get();
            boolean vmIsBeingDeleted = DELETING.equals(vmStatus.getTaskState());
            if (!vmIsBeingDeleted) {
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
		try {
			assertVmId(vmId);
		} catch(CloudMiddlewareException ex) {
			return false;
		}
		return true;
    }

    @Override
    public void rebootHardVm(String vmId) throws CloudMiddlewareException {
		assertVmId(vmId);
        openStackJcloudsApis.getServerApi().reboot(vmId, RebootType.HARD);
    }
    
    @Override
    public void rebootSoftVm(String vmId) throws CloudMiddlewareException {
		assertVmId(vmId);
        openStackJcloudsApis.getServerApi().reboot(vmId, RebootType.SOFT);
    }
    
    @Override
    public void startVm(String vmId) throws CloudMiddlewareException {
		assertVmId(vmId);
        openStackJcloudsApis.getServerApi().start(vmId);
    }

    @Override
    public void stopVm(String vmId) throws CloudMiddlewareException {
		assertVmId(vmId);
        openStackJcloudsApis.getServerApi().stop(vmId);
    }

    @Override
    public void suspendVm(String vmId) throws CloudMiddlewareException {
		assertVmId(vmId);
        openStackJcloudsApis.getServerAdminApi().suspend(vmId);
    }

    @Override
    public void resumeVm(String vmId) throws CloudMiddlewareException {
		assertVmId(vmId);
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
    public String createVmImage(ImageToUpload imageToUpload) throws CloudMiddlewareException {
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

    @Override
    public void assignFloatingIp(String vmId) throws CloudMiddlewareException {
		assertVmId(vmId);
        FloatingIPApi floatingIPApi = openStackJcloudsApis.getFloatingIpApi();
        if (floatingIPApi != null) {
            String unassignedFloatingIp = selectUnassignedFloatingIp();
            if (unassignedFloatingIp != null) {
                floatingIPApi.addToServer(unassignedFloatingIp, vmId);
            }
        }
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
    private String getImageIdForDeployment(Vm vm) throws CloudMiddlewareException {
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
    private CreateServerOptions getDeploymentOptionsForVm(Vm vm, String hostname, String[] securityGroups,
                                                          boolean useVolume, String isoPath) throws CloudMiddlewareException {
		assertHostName(hostname);
        CreateServerOptions options = new CreateServerOptions();
        includeDstNodeInDeploymentOption(hostname, options);
        includeInitScriptInDeploymentOptions(vm, options);
        includeSecurityGroupInDeploymentOption(options, securityGroups);
        includeNetworkInDeploymentOptions(options, hostname);
        if (useVolume) {
            includeVolumeInDeploymentOptions(vm, options, isoPath);
        }
        return options;
    }

    /**
     * Specifies in the VM deployment options the server on which to deploy the VM.
     *
     * @param dstNode the node where to deploy the VM
     * @param options VM deployment options
     */
    private void includeDstNodeInDeploymentOption(String dstNode, CreateServerOptions options) throws CloudMiddlewareException {
		assertHostName(dstNode);
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

    // This is a quick hack for the Ascetic project. It'll be refactored in the future :)
    // In our new testbeds (wally) we need to select the network that we want the VM to use.
    // Now we are using the VMM specific network. This is not required in the other clusters that we use.
    private void includeNetworkInDeploymentOptions(CreateServerOptions options, String hostname) {
        if (hostname != null && hostname.startsWith("wally")) {
            List<String> networks = new ArrayList<>();
            NeutronApi neutronApi = openStackJcloudsApis.getNeutronApi();

            for (org.jclouds.openstack.neutron.v2.domain.Network network :
                    neutronApi.getNetworkApi(zone).list().concat().toList()) {
                if (network.getName().equals("VMManager_network")) {
                    networks.add(network.getId());
                }
            }

            options.networks(networks);
        }
    }

    private void includeVolumeInDeploymentOptions(Vm vm, CreateServerOptions options, String isoPath) throws CloudMiddlewareException {
        Set<BlockDeviceMapping> blockDeviceMappingSet = new HashSet<>();
        BlockDeviceMapping blockDeviceMapping = BlockDeviceMapping.builder()
                .sourceType("image")
                .uuid(vm.getImage())
                .destinationType("volume")
                .bootIndex(0)
                .deviceName("vda")
                .volumeSize(vm.getDiskGb())
                .deleteOnTermination(true)
                .build();
        blockDeviceMappingSet.add(blockDeviceMapping);

        // If the VM needs to mount an ISO, first we need to upload that ISO to glance, and then add it
        // to the set of block device mappings
        if (isoPath != null && (isoPath.contains(".iso_") || isoPath.endsWith(".iso"))) {
            String imageId = createVmImage(new ImageToUpload(
                    isoPath.split("/")[isoPath.split("/").length - 1],
                    isoPath));
            blockDeviceMappingSet.add(getBlockDeviceMappingIso(imageId));

            // Block until the image is uploaded
            while (!glanceConnector.imageIsActive(imageId)) {
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }

        options.blockDeviceMappings(blockDeviceMappingSet);
    }

    private BlockDeviceMapping getBlockDeviceMappingIso(String imageId) {
        return BlockDeviceMapping.builder()
                .sourceType("image")
                .uuid(imageId)
                .destinationType("volume")
                .bootIndex(1)
                .deviceName("vdc")
                .volumeSize(1) // 1GB. Not sure if this will always be enough.
                .deleteOnTermination(true)
                .build();
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
    private String getVmIp(Server server) throws CloudMiddlewareException {
		assertHostName(server.getExtendedAttributes().get().getHostName());
        // IMPORTANT: this returns only 1 IP, but VMs can have more than 1.
        // For now, I return just 1 to avoid breaking VMM clients.
        // Also, when a VM is scheduled but not deployed, it might not have an IP.ç
        List<Address> addresses = new ArrayList<>(server.getAddresses().values());
        return addresses.isEmpty() ? "" : addresses.get(addresses.size() - 1).getAddr();
    }

    /**
     * Blocks the thread execution until a specific VM is deployed.
     *
     * @param vmId the ID of the VM
     */
    private void blockUntilVmIsDeployed(String vmId) throws CloudMiddlewareException {
		assertVmId(vmId);
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
    private void blockUntilVmIsDeleted(String vmId) throws CloudMiddlewareException {
		assertVmId(vmId);
        while (openStackJcloudsApis.getServerApi().get(vmId).getStatus().toString().equals(DELETING)) {
            try {
                Thread.sleep(BLOCKING_TIME_SEC_DEPLOY_AND_DESTROY);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    private String selectUnassignedFloatingIp() {
        FloatingIPApi floatingIPApi = openStackJcloudsApis.getFloatingIpApi();
        if (floatingIPApi != null) {
            for (FloatingIP floatingIp : floatingIPApi.list()) {
                if (floatingIp.getInstanceId() == null) {
                    return floatingIp.getIp();
                }
            }
        }
        return null;
    }

}