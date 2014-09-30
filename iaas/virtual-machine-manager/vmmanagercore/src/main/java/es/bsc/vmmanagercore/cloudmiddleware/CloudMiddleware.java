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

import es.bsc.vmmanagercore.model.images.ImageToUpload;
import es.bsc.vmmanagercore.model.images.ImageUploaded;
import es.bsc.vmmanagercore.model.vms.Vm;
import es.bsc.vmmanagercore.model.vms.VmDeployed;

import java.util.List;

/**
 * Interface for the Cloud Middleware infrastructure (e.g. OpenNebula, OpenStack,
 * EMOTIVE...). It provides a common interface with the most common operations
 * of a cloud Middleware: deploy, cancel, migrate, get information, etc.
 * 
 * @author David Ortiz Lopez (david.ortiz@bsc.es)
 * 
 */
public interface CloudMiddleware {
    /**
     * Deploys a virtual machine.
     *
     * @param vmDescription the description of the VM
     * @param dstNode the identifier of the destination node
     * @return A string that identifies the new VM
     */
    String deploy(Vm vmDescription, String dstNode);
    
    /**
     * Destroys a Virtual Machine.
     *
     * @param vmId identifier of the VM to be destroyed
     */
    void destroy(String vmId);
    
    /**
     * Migrates a Virtual Machine from its current node to another.
     *
     * @param vmId identifier of the VM to be migrated
     * @param destinationNodeHostName hostname of the node where the VM will be migrated
     */
    void migrate(String vmId, String destinationNodeHostName);
    
    /**
     * Gets the identifiers of the VMs that a user is running. If the user is
     * not running any VM, returns null.
     *
     * @return A Collection containing the identifiers of the VMs that a user
     * is running. If the user is not running any VM, returns null
     */
    List<String> getAllVMsId();
    
    /**
     * Gets the complete information of a virtual machine given its identifier.
     *
     * @param vmId Identifier of the VM to which the information is retrieved
     * @return The description of a virtual machine. NULL if there is not
     * a virtual machine with the given id
     */
    VmDeployed getVMInfo(String vmId);

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
