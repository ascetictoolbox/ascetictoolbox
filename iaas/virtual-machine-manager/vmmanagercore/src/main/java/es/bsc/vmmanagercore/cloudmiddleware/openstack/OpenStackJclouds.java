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
import es.bsc.vmmanagercore.db.VmManagerDb;
import es.bsc.vmmanagercore.model.images.ImageToUpload;
import es.bsc.vmmanagercore.model.images.ImageUploaded;
import es.bsc.vmmanagercore.model.vms.Vm;
import es.bsc.vmmanagercore.model.vms.VmDeployed;
import org.apache.commons.io.IOUtils;
import org.apache.commons.validator.UrlValidator;
import org.jclouds.ContextBuilder;
import org.jclouds.openstack.nova.v2_0.NovaApi;
import org.jclouds.openstack.nova.v2_0.NovaApiMetadata;
import org.jclouds.openstack.nova.v2_0.domain.*;
import org.jclouds.openstack.nova.v2_0.extensions.ServerAdminApi;
import org.jclouds.openstack.nova.v2_0.features.FlavorApi;
import org.jclouds.openstack.nova.v2_0.features.ImageApi;
import org.jclouds.openstack.nova.v2_0.features.ServerApi;
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
 *
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

    // OpenStack APIs defined by JClouds
    private NovaApi novaApi;
    private ServerApi serverApi;
    private ImageApi imageApi;
    private FlavorApi flavorApi;
    private ServerAdminApi serverAdminApi;

    private String zone; // This could be important/problematic in the future because I am assuming that
                         // the cluster only has one zone configured for deployments.
    private String[] securityGroups = {};

    private OpenStackGlance glanceConnector; // Connector for OS Glance
    private VmManagerDb db; // DB that contains the relationship VM-application, the scheduling algorithms, etc.

    /**
     * Class constructor. It performs the connection to the infrastructure and initializes
     * JClouds attributes.
     *
     * @param openStackIP IP of the OpenStack installation
     * @param keyStonePort port where the Keystone service is running
     * @param keyStoneTenant tenant of the Keystone service
     * @param keyStoneUser user of the Keystone service
     * @param keyStonePassword password of the Keystone service
     * @param db database used by the VM Manager
     * @param securityGroups the security groups to which the VM will be part of
     */
    public OpenStackJclouds(String openStackIP, int keyStonePort, String keyStoneTenant, String keyStoneUser,
                            String keyStonePassword, int glancePort, String keyStoneTenantId, VmManagerDb db,
                            String[] securityGroups) {
        getOpenStackApis(openStackIP, keyStonePort, keyStoneTenant, keyStoneUser, keyStonePassword);
        this.securityGroups = securityGroups;
        this.db = db;
        glanceConnector = new OpenStackGlance(openStackIP, glancePort, keyStonePort,
                keyStoneUser, keyStonePassword, keyStoneTenantId);
    }

    @Override
    public String deploy(Vm vm, String hostname) {
        // Deploy the VM
        ServerCreated server = serverApi.create(
                vm.getName(),
                getImageIdForDeployment(vm),
                getFlavorIdForDeployment(vm),
                getDeploymentOptionsForVm(vm, hostname, securityGroups));

        // Wait until the VM is deployed
        while (serverApi.get(server.getId()).getStatus().toString().equals(BUILD)) {
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        // Return the VM id
        return server.getId();
    }

    @Override
    public void destroy(String vmId) {
        Server server = serverApi.get(vmId);
        if (server != null) { // If the VM is in the zone
            serverApi.delete(vmId);
            // Wait until the VM is destroyed
            while (server.getStatus().toString().equals(DELETING)) {
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    @Override
    public void migrate(String vmId, String destinationNodeHostName) {
        if (serverApi.get(vmId) != null) {
            serverAdminApi.liveMigrate(vmId, destinationNodeHostName, false, false);
        }
    }

    @Override
    public List<String> getAllVMsIds() {
        List<String> vmIds = new ArrayList<>();
        for (Server server: serverApi.listInDetail().concat()) {
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
        Server server = serverApi.get(vmId);

        // If the VM is in the zone
        if (server != null ) {
            // Get the information of the VM if it is active and it is not being deleted
            ServerExtendedStatus vmStatus = server.getExtendedStatus().get();
            boolean vmIsActive = ACTIVE.equals(vmStatus.getVmState());
            boolean vmIsBeingDeleted = DELETING.equals(vmStatus.getTaskState());
            if (vmIsActive && !vmIsBeingDeleted) {
                Flavor flavor = flavorApi.get(server.getFlavor().getId());
                String vmIp = getVmIp(server);
                vm = new VmDeployed(server.getName(),
                        server.getImage().getId(), flavor.getVcpus(), flavor.getRam(),
                        flavor.getDisk(), null, db.getAppIdOfVm(vmId), vmId,
                        vmIp, server.getStatus().toString(), server.getCreated(),
                        server.getExtendedAttributes().get().getHostName());
            }
        }

        return vm;
    }

    @Override
    public boolean existsVm(String vmId) {
        return serverApi.get(vmId) != null;
    }

    @Override
    public void rebootHardVm(String vmId) {
        serverApi.reboot(vmId, RebootType.HARD);
    }
    
    @Override
    public void rebootSoftVm(String vmId) {
        serverApi.reboot(vmId, RebootType.SOFT);
    }
    
    @Override
    public void startVm(String vmId) {
        serverApi.start(vmId);
    }

    @Override
    public void stopVm(String vmId) {
        serverApi.stop(vmId);
    }

    @Override
    public void suspendVm(String vmId) {
        novaApi.getServerAdminExtensionForZone(zone).get().suspend(vmId);
    }

    @Override
    public void resumeVm(String vmId) {
        novaApi.getServerAdminExtensionForZone(zone).get().resume(vmId);
    }

    @Override
    public List<ImageUploaded> getVmImages() {
        List<ImageUploaded> vmImages = new ArrayList<>();
        for (Image image: imageApi.listInDetail().concat()) {
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
        Image image = imageApi.get(imageId);
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
        return novaApi;
    }

    /**
     * Gets the OpenStack APIs (nova, server, image, flavor) as defined by the JClouds library.
     *
     * @param openStackIP IP of the OpenStack installation
     * @param keystonePort port where the Keystone service is running
     * @param keystoneTenant tenant of the Keystone service
     * @param keystoneUser user of the Keystone service
     * @param keystonePassword password of the Keystone service
     */
    private void getOpenStackApis(String openStackIP, int keystonePort, String keystoneTenant,
                                  String keystoneUser, String keystonePassword) {
        novaApi = ContextBuilder.newBuilder(new NovaApiMetadata())
                .endpoint("http://" + openStackIP + ":" + keystonePort + "/v2.0")
                .credentials(keystoneTenant + ":" + keystoneUser, keystonePassword)
                .buildApi(NovaApi.class);
        zone = novaApi.getConfiguredZones().toArray()[0].toString(); // Assuming that there is only 1 zone configured
        serverApi = novaApi.getServerApiForZone(zone);
        imageApi = novaApi.getImageApiForZone(zone);
        flavorApi = novaApi.getFlavorApiForZone(zone);
        serverAdminApi = novaApi.getServerAdminExtensionForZone(zone).get();
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
        String flavorId = getFlavorId(vm.getCpus(), vm.getRamMb(), vm.getDiskGb());
        if (flavorId != null) {
            return flavorId;
        }

        //If the flavor does not exist, create it and return the ID
        String id, name;
        id = name = vm.getCpus() + "-" + vm.getDiskGb() + "-" + vm.getRamMb();
        return createFlavor(id, name, vm.getCpus(), vm.getRamMb(), vm.getDiskGb());
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
                // Do not include anything in the VM deployment options
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
     * @return The ID of the flavor. Null if a flavor with the specified characteristics does not exist.
     */
    private String getFlavorId(int cpus, int memoryMb, int diskGb) {
        for (Flavor flavor: flavorApi.listInDetail().concat()) {
            if (flavor.getVcpus() == cpus && flavor.getRam() == memoryMb && flavor.getDisk() == diskGb) {
                return flavor.getId();
            }
        }
        return null;
    }

    /**
     * Creates a flavor with the specified characteristics.
     * @param cpus the number of CPUs of the flavor
     * @param ramMb the amount of RAM of the flavor in MB
     * @param diskGb the amount of disk space of the flavor in GB
     * @return The ID of the created flavor.
     */
    private String createFlavor(String id, String name, int cpus, int ramMb, int diskGb) {
        flavorApi.create(Flavor.builder().id(id).name(name).vcpus(cpus).ram(ramMb).disk(diskGb).build());
        return id;
    }

    /**
     * Checks whether an image with the given ID has been uploaded to the infrastructure.
     *
     * @param id the ID of the image
     * @return true if an image exists with the given Id, false otherwise
     */
    private boolean existsImageWithId(String id) {
        return imageApi.get(id) != null;
    }

    /**
     * Returns the IP of a VM.
     *
     * @param server the VM
     * @return the IP of the VM
     */
    // TODO Redo this. The name of the network should be obtained automatically. This might only work in TUB and BSC.
    private String getVmIp(Server server) {
        if (server.getAddresses().get("vmnet").toArray().length != 0) { // VM network
            return ((Address) server.getAddresses().get("vmnet").toArray()[0]).getAddr();
        }
        return (((Address) server.getAddresses().get("NattedNetwork").toArray()[0]).getAddr()); // Nat network
    }

}