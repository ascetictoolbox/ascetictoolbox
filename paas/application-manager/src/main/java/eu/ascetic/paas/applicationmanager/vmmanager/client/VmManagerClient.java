package eu.ascetic.paas.applicationmanager.vmmanager.client;

import eu.ascetic.paas.applicationmanager.vmmanager.datamodel.ImageUploaded;
import eu.ascetic.paas.applicationmanager.vmmanager.datamodel.ListImagesUploaded;
import eu.ascetic.paas.applicationmanager.vmmanager.datamodel.ListVmEstimates;
import eu.ascetic.paas.applicationmanager.vmmanager.datamodel.ListVmsDeployed;
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
	
	/***
	 * pending methods:
	 * 		- post: deployVMs(ListVMs)
	 * 		- put: deployVMs(ListVms) solo una
	 * 		- delete: destroyVM
	 * 		- deleteVmsOfApp
	 * 		- ¿add image?
	 * 		- deleteImage
	 */
	
	
}
