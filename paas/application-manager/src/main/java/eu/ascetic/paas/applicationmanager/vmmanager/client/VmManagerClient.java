package eu.ascetic.paas.applicationmanager.vmmanager.client;

import java.util.List;

import eu.ascetic.paas.applicationmanager.vmmanager.datamodel.ImageToUpload;
import eu.ascetic.paas.applicationmanager.vmmanager.datamodel.ImageUploaded;
import eu.ascetic.paas.applicationmanager.vmmanager.datamodel.ListImagesUploaded;
import eu.ascetic.paas.applicationmanager.vmmanager.datamodel.ListVmsDeployed;
import eu.ascetic.paas.applicationmanager.vmmanager.datamodel.Vm;
import eu.ascetic.paas.applicationmanager.vmmanager.datamodel.VmDeployed;


/**
 * The Interface VmManagerClient.
 */
public interface VmManagerClient {
	
	/**
	 * Gets the url.
	 *
	 * @return the url
	 */
	public String getURL();

	/**
	 * Sets the url.
	 *
	 * @param url the new url
	 */
	public void setURL(String url);
	
	/**
	 * Gets the all images.
	 *
	 * @return the all images
	 */
	public ListImagesUploaded getAllImages();
	
	/**
	 * Gets the image.
	 *
	 * @param id the id
	 * @return the image
	 */
	public ImageUploaded getImage(String id);
	
	/**
	 * Gets the all v ms.
	 *
	 * @return the all v ms
	 */
	public ListVmsDeployed getAllVMs();
	
	/**
	 * Gets the vm.
	 *
	 * @param id the id
	 * @return the vm
	 */
	public VmDeployed getVM(String id);

	
	/**
	 * Gets the vms of app.
	 *
	 * @param appId the app id
	 * @return the vms of app
	 */
	public ListVmsDeployed getVmsOfApp(String appId);
	
	/**
	 * Gets the logs.
	 *
	 * @return the logs
	 */
	public String getLogs();
	
	
	/**
	 * Deploy v ms.
	 *
	 * @param vms the vms
	 * @return the list
	 */
	public List<String> deployVMs(List<Vm> vms);
	
	 
	
	/*
	 * 		- put: deployVMs(ListVms) solo una 
	@PUT
    @Path("/vms/{id}")
    @Consumes("application/json")
    public void changeStateVm(@PathParam("id") String vmId, String actionJson) {
        vmCallsManager.changeStateVm(vmId, actionJson);
    }	
	 * 		- delete: destroyVM
	@DELETE
    @Path("/vms/{id}")
    public void destroyVm(@PathParam("id") String vmId) {
        vmCallsManager.destroyVm(vmId);
    }
    
	 **/
	public void deleteVmsOfApp(String appId);
	/*@DELETE
    @Path("/vmsapp/{appId}")
    public void deleteVmsOfApp(@PathParam("appId") String appId) {
        vmCallsManager.deleteVmsOfApp(appId);
    }
	 */
 
	public String uploadImage(ImageToUpload imageInfo);

	
	
	/* 		- deleteImage
	 *  @DELETE
    @Path("/vmsapp/{appId}")
    public void deleteVmsOfApp(@PathParam("appId") String appId) {
        vmCallsManager.deleteVmsOfApp(appId);
    }
	 */
	
	
}
