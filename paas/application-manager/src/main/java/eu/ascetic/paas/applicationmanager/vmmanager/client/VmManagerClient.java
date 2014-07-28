package eu.ascetic.paas.applicationmanager.vmmanager.client;

import java.util.List;

import eu.ascetic.paas.applicationmanager.vmmanager.datamodel.ImageToUpload;
import eu.ascetic.paas.applicationmanager.vmmanager.datamodel.ImageUploaded;
import eu.ascetic.paas.applicationmanager.vmmanager.datamodel.ListImagesUploaded;
import eu.ascetic.paas.applicationmanager.vmmanager.datamodel.ListVmsDeployed;
import eu.ascetic.paas.applicationmanager.vmmanager.datamodel.SchedulingAlgorithm;
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
	 * Deploy v ms.
	 *
	 * @param vms the vms
	 * @return the list
	 */
	public List<String> deployVMs(List<Vm> vms);
	
	/**
	 * Change state vm.
	 *
	 * @param vmId the vm id
	 * @param action the action
	 * @return true, if successful
	 */
	public boolean changeStateVm(String vmId, String action);
	
	/**
	 * Destroy vm.
	 *
	 * @param vmId the vm id
	 * @return true, if successful
	 */
	public boolean destroyVM(String vmId);
	
	/**
	 * Delete vms of app.
	 *
	 * @param appId the app id
	 * @return true, if successful
	 */
	public boolean deleteVmsOfApp(String appId);
 
	/**
	 * Upload image.
	 *
	 * @param imageInfo the image info
	 * @return the new VM created ID
	 */
	public String uploadImage(ImageToUpload imageInfo);

}
