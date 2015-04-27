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

package es.bsc.vmmanagercore.cloudmiddleware;

import es.bsc.vmmanagercore.models.images.ImageToUpload;
import es.bsc.vmmanagercore.models.images.ImageUploaded;
import es.bsc.vmmanagercore.models.vms.Vm;
import es.bsc.vmmanagercore.models.vms.VmDeployed;

import java.util.List;

/**
 * Interface for the Cloud Middleware infrastructure (e.g. OpenNebula, OpenStack,
 * EMOTIVE...). It provides a common interface with the most common operations
 * of a cloud Middleware: deploy VM, destroy VM, migrate VM, get VM, etc.
 * 
 * @author David Ortiz Lopez (david.ortiz@bsc.es)
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
    String deploy(Vm vm, String hostname);
    
    /**
     * Destroys a Virtual Machine.
     *
     * @param vmId ID of the VM to be destroyed
     */
    void destroy(String vmId);
    
    /**
     * Migrates a VM from its current node to another.
     *
     * @param vmId ID of the VM to be migrated
     * @param destinationNodeHostName hostname of the node where the VM will be migrated
     */
    void migrate(String vmId, String destinationNodeHostName);
    
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
    VmDeployed getVM(String vmId);

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
    void rebootHardVm(String vmId);

    /**
     * Performs a soft reboot on a VM.
     *
     * @param vmId the ID of the VM to reboot
     */
    void rebootSoftVm(String vmId);

    /**
     * Starts a VM.
     *
     * @param vmId the ID of the VM to be started
     */
    void startVm(String vmId);

    /**
     * Stops a VM.
     *
     * @param vmId the ID of the VM to be stopped
     */
    void stopVm(String vmId);

    /**
     * Suspends a VM.
     *
     * @param vmId the ID of the VM to be suspended
     */
    void suspendVm(String vmId);

    /**
     * Resumes a VM.
     *
     * @param vmId the ID of the VM to be resumed
     */
    void resumeVm(String vmId);

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
    String createVmImage(ImageToUpload imageToUpload);

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
}
