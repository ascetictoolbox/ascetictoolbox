package es.bsc.vmmanagercore.cloudmiddleware;

import java.util.Collection;

import es.bsc.vmmanagercore.model.ImageToUpload;
import es.bsc.vmmanagercore.model.ImageUploaded;
import es.bsc.vmmanagercore.model.Vm;
import es.bsc.vmmanagercore.model.VmDeployed;

/**
 * Interface to the Cloud Middleware infrastructure (e.g. OpenNebula, OpenStack,
 * EMOTIVE...). It provides a common interface with the most common operations
 * of a cloud Middleware: deploy, cancel, migrate, get information, etc.
 * 
 * @author David Ortiz Lopez (david.ortiz@bsc.es)
 * 
 */
public interface CloudMiddleware {
    /**
     * Deploys a virtual machine
     * @param vmDescription the description of the VM
     * @param dstNode the identifier of the destination node
     * @return A string that identifies the new VM
     */
    String deploy(Vm vmDescription, String dstNode);
    
    /**
     * Destroys a Virtual Machine
     * @param vmId identifier of the VM to destroy
     */
    void destroy(String vmId);
    
    /**
     * Migrates a Virtual Machine from its current node to another
     * @param vmId identifier of the VM to migrate
     * @param destinationNode Node to migrate the VM
     */
    void migrate(String vmId, String destinationNode);
    
    /**
     * Gets the identifiers of the VMs that a user is running. If the user is
     * not running any VM, returns null
     * @return A Collection containing the identifiers of the VMs that a user
     * is running. If the user is not running any VM, returns null
     */
    Collection<String> getAllVMsId();
    
    /**
     * Gets the complete information of a virtual machine given its identifier
     * @param vmId Identifier of the VM to which the information is retrieved
     * @return The description of a virtual machine. NULL if there is not
     * a virtual machine with the given id
     */
    VmDeployed getVMInfo(String vmId);
    
    boolean existsVm(String vmId);
    
    void rebootHardVm(String vmId);
    
    void rebootSoftVm(String vmId);
    
    void startVm(String vmId);

    void stopVm(String vmId);
    
    void suspendVm(String vmId);
    
    void resumeVm(String vmId);
    
    Collection<ImageUploaded> getVmImages();
    
    // Returns the ID of the image created
    String createVmImage(ImageToUpload imageToUpload);
    
    ImageUploaded getVmImage(String imageId);

	void deleteVmImage(String id);
}
