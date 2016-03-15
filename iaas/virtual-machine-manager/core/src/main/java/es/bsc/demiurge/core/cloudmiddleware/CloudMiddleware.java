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

package es.bsc.demiurge.core.cloudmiddleware;

import es.bsc.demiurge.core.models.images.ImageToUpload;
import es.bsc.demiurge.core.models.images.ImageUploaded;
import es.bsc.demiurge.core.models.vms.Vm;
import es.bsc.demiurge.core.models.vms.VmDeployed;
import es.bsc.demiurge.core.models.vms.VmRequirements;

import java.util.List;
import java.util.Map;

/**
 * Interface for the Cloud Middleware infrastructure (e.g. OpenNebula, OpenStack,
 * EMOTIVE...). It provides a common interface with the most common operations
 * of a cloud Middleware: deploy VM, destroy VM, migrate VM, get VM, etc.
 * 
 * @author Mario Macias (github.com/mariomac), David Ortiz Lopez (david.ortiz@bsc.es)
 * 
 */
public interface CloudMiddleware {
    /**
     * Deploys a virtual machine in a specific host.
     *
     * @param vm the vm
     * @param hostname the hostname
     * @return the ID of the deployed VM
     */
    String deploy(Vm vm, String hostname) throws CloudMiddlewareException;

    /**
     * Deploys a virtual machine in a specific host and using a volume
     *
     * @param vm the vm
     * @param hostname the hostname
     * @param isoPath path of the ISO that the VM needs to mount. Can be null.
     * @return the ID of the deployed VM
     */
    String deployWithVolume(Vm vm, String hostname, String isoPath) throws CloudMiddlewareException;

    /**
     * Destroys a Virtual Machine.
     *
     * @param vmId ID of the VM to be destroyed
     */
    void destroy(String vmId) throws CloudMiddlewareException;
    
    /**
     * Migrates a VM from its current node to another.
     *
     * @param vmId ID of the VM to be migrated
     * @param destinationNodeHostName hostname of the node where the VM will be migrated
     */
    void migrate(String vmId, String destinationNodeHostName) throws CloudMiddlewareException;
    
    /**
     * Retrieves the IDs of all the VMs that are running.
     *
     * @return a list containing the IDs of all the VMs that are running
     */
    List<String> getAllVMsIds();

    /**
     * Retrieves the IDs of the VMs that have been scheduled but have not been deployed yet.
     *
     * @return a list containing the IDs of the VMs that are scheduled but not deployed
     */
    List<String> getScheduledNonDeployedVmsIds();

    /**
     * Retrieves a VM by its ID.
     *
     * @param vmId the ID of the VM
     * @return The VM or NULL if there is not a VM with the given ID
     */
    VmDeployed getVM(String vmId) throws CloudMiddlewareException;

    /**
     * Checks whether a VM with the given ID exists in the system.
     *
     * @param vmId the ID of the VM
     * @return true if the VM exists, false otherwise
     */
    boolean existsVm(String vmId);

    /**
     * Performs a hard reboot on a VM.
     *
     * @param vmId the ID of the VM to be rebooted
     */
    void rebootHardVm(String vmId) throws CloudMiddlewareException;

    /**
     * Performs a soft reboot on a VM.
     *
     * @param vmId the ID of the VM to reboot
     */
    void rebootSoftVm(String vmId) throws CloudMiddlewareException;

    /**
     * Starts a VM.
     *
     * @param vmId the ID of the VM to be started
     */
    void startVm(String vmId) throws CloudMiddlewareException;

    /**
     * Stops a VM.
     *
     * @param vmId the ID of the VM to be stopped
     */
    void stopVm(String vmId) throws CloudMiddlewareException;

    /**
     * Suspends a VM.
     *
     * @param vmId the ID of the VM to be suspended
     */
    void suspendVm(String vmId) throws CloudMiddlewareException;

    /**
     * Resumes a VM.
     *
     * @param vmId the ID of the VM to be resumed
     */
    void resumeVm(String vmId) throws CloudMiddlewareException;

    /**
     * Gets the images uploaded to an infrastructure. This images are the ones used to deploy VMs.
     *
     * @return the images in the infrastructure
     */
    List<ImageUploaded> getVmImages();

    /**
     * Creates a new image in the infrastructure.
     *
     * @param imageToUpload the image to be created
     * @return the ID of the image created
     */
    String createVmImage(ImageToUpload imageToUpload) throws CloudMiddlewareException;

    /**
     * Retrieves an image from the infrastructure.
     *
     * @param imageId the ID of the image to retrieve
     * @return the image
     */
    ImageUploaded getVmImage(String imageId);

    /**
     * Deletes an image from the infrastructure.
     *
     * @param id the ID of the image to be deleted
     */
    void deleteVmImage(String id);

    /**
     * Assigns a floating IP to the VM with the given ID.
     *
     * @param vmId the ID of the VM
     */
    void assignFloatingIp(String vmId) throws CloudMiddlewareException;

    /**
     * Returns a list of flavors.
     * @return 
     */
	Map<String, String> getFlavours();
    
    /**
     * Resizes an existing VM into a different flavor.
     * @param vmId
     * @param flavourId 
     */
	void resize(String vmId, String flavourId);
    
    /**
     * Resizes an existing VM into different hardware settings.
     * @param vmId
     * @param vm
     */
	void resize(String vmId, VmRequirements vm);
    
    /**
     * Confirms a resize.
     * @param vmId
     */
	void confirmResize(String vmId);
}
