package eu.ascetic.paas.applicationmanager.vmmanager.client;

import java.util.List;

import org.apache.log4j.Logger;

import eu.ascetic.paas.applicationmanager.Dictionary;
import eu.ascetic.paas.applicationmanager.conf.Configuration;
import eu.ascetic.paas.applicationmanager.http.Client;
import eu.ascetic.paas.applicationmanager.vmmanager.datamodel.ImageToUpload;
import eu.ascetic.paas.applicationmanager.vmmanager.datamodel.ImageUploaded;
import eu.ascetic.paas.applicationmanager.vmmanager.datamodel.ListImagesUploaded;
import eu.ascetic.paas.applicationmanager.vmmanager.datamodel.ListVms;
import eu.ascetic.paas.applicationmanager.vmmanager.datamodel.ListVmsDeployed;
import eu.ascetic.paas.applicationmanager.vmmanager.datamodel.Vm;
import eu.ascetic.paas.applicationmanager.vmmanager.datamodel.VmDeployed;
import eu.ascetic.paas.applicationmanager.vmmanager.datamodel.converter.ModelConverter;


/**
 * Implements the methods that perform the different Actions against the ASCETiC IaaS VM Manager (VMManager)
 * @author David Rojo - Atos
 */
public class VmManagerClientHC implements VmManagerClient {

	/** The logger. */
	private static Logger logger = Logger.getLogger(VmManagerClientHC.class);
	
	/** The url. */
	protected String url;
	
	/**
	 * Instantiates a new vm manager client hc.
	 */
	public VmManagerClientHC() {
		this.url = Configuration.vmManagerServiceUrl;
	}
	
	/**
	 * Instantiates a new vm manager client hc.
	 *
	 * @param url the url
	 */
	public VmManagerClientHC(String url) {
		this.url = url;
	}

	/* (non-Javadoc)
	 * @see eu.ascetic.paas.applicationmanager.vmmanager.client.VmManagerClient#getURL()
	 */
	@Override
	public String getURL() {
		return url;
	}

	/* (non-Javadoc)
	 * @see eu.ascetic.paas.applicationmanager.vmmanager.client.VmManagerClient#setURL(java.lang.String)
	 */
	@Override
	public void setURL(String url) {
		this.url = url;
	}
	
	/* (non-Javadoc)
	 * @see eu.ascetic.paas.applicationmanager.vmmanager.client.VmManagerClient#getListOfImagesUploaded()
	 */
	@Override
	public ListImagesUploaded getAllImages() {
		Boolean exception = false;
		String testbedsUrl = url + "/images";
		
		logger.debug("CONNECTING TO: " + url);
		
		String response = Client.getMethod(testbedsUrl, Dictionary.CONTENT_TYPE_JSON, exception);
		logger.debug("PAYLOAD: " + response);
		
		ListImagesUploaded imagesUploaded = null;
		
		try {
			imagesUploaded = ModelConverter.jsonListImagesUploadedToObject(response);
		} catch(Exception e) {
			logger.warn("Error trying to parse returned list of images: " + url + "/images" + " Exception: " + e.getMessage());
			exception = true;
		}
		
		if(exception) return null;
		
		return imagesUploaded;
	}
	
	/* (non-Javadoc)
	 * @see eu.ascetic.paas.applicationmanager.vmmanager.client.VmManagerClient#getImage(java.lang.String)
	 */
	@Override
	public ImageUploaded getImage(String id) {
		Boolean exception = false;
		
		String imageUrl = url + "/images/" + id;
		String response = Client.getMethod(imageUrl, Dictionary.CONTENT_TYPE_JSON, exception);
		logger.debug("PAYLOAD: " + response);
		
		ImageUploaded imageUploaded = null;
	
		
		try {
			imageUploaded = ModelConverter.jsonImageUploadedToObject(response);
		} catch(Exception e) {
			logger.warn("Error trying to parse returned imageUploaded: " + imageUrl + " Exception: " + e.getMessage());
			exception = true;
		}
			
		if(exception) return null;
		
		return imageUploaded;
	}

	/* (non-Javadoc)
	 * @see eu.ascetic.paas.applicationmanager.vmmanager.client.VmManagerClient#getAllVMs()
	 */
	@Override
	public ListVmsDeployed getAllVMs() {
		Boolean exception = false;
		String testbedsUrl = url + "/vms";
		
		logger.debug("CONNECTING TO: " + url);
		
		String response = Client.getMethod(testbedsUrl, Dictionary.CONTENT_TYPE_JSON, exception);
		logger.debug("PAYLOAD: " + response);
		
		ListVmsDeployed vms = null;
		
		try {
			vms = ModelConverter.jsonListVmsDeployedToObject(response);
		} catch(Exception e) {
			logger.warn("Error trying to parse returned list of vms deployed: " + testbedsUrl + " Exception: " + e.getMessage());
			exception = true;
		}
		
		if(exception) return null;
		
		return vms;
	}

	@Override
	public VmDeployed getVM(String id) {
		Boolean exception = false;
		
		String imageUrl = url + "/vms/" + id;
		String response = Client.getMethod(imageUrl, Dictionary.CONTENT_TYPE_JSON, exception);
		logger.debug("PAYLOAD: " + response);
		
		VmDeployed vm = null;
	
		
		try {
			vm = ModelConverter.jsonVmDeployedToObject(response);
		} catch(Exception e) {
			logger.warn("Error trying to parse returned vm deployed: " + imageUrl + " Exception: " + e.getMessage());
			exception = true;
		}
			
		if(exception) return null;
		
		return vm;
	}

	/* (non-Javadoc)
	 * @see eu.ascetic.paas.applicationmanager.vmmanager.client.VmManagerClient#getVmsOfApp(java.lang.String)
	 */
	@Override
	public ListVmsDeployed getVmsOfApp(String appId) {
		Boolean exception = false;
		String testbedsUrl = url + "/vmsapp/" + appId;
		
		logger.debug("CONNECTING TO: " + url);
		
		String response = Client.getMethod(testbedsUrl, Dictionary.CONTENT_TYPE_JSON, exception);
		logger.debug("PAYLOAD: " + response);
		
		ListVmsDeployed vmsOfApp = null;
		
		try {
			vmsOfApp = ModelConverter.jsonListVmsDeployedToObject(response);
		} catch(Exception e) {
			logger.warn("Error trying to parse returned list of vms deployed: " + testbedsUrl + " Exception: " + e.getMessage());
			exception = true;
		}
		
		if(exception) return null;
		
		return vmsOfApp;
	}


	/* (non-Javadoc)
	 * @see eu.ascetic.paas.applicationmanager.vmmanager.client.VmManagerClient#deployVMs(java.util.List)
	 */
	@Override
	public List<String> deployVMs(List<Vm> vms) {
		List<String> listIDs = null;
		Boolean exception = false;
		String experimentUrl = url + "/vms";
		logger.debug("URL build: " + experimentUrl);
		
		try {
			ListVms listVms = new ListVms(vms);
			String payload = ModelConverter.objectListVmsToJSON(listVms);
			
			String response = Client.postMethod(experimentUrl, payload, Dictionary.CONTENT_TYPE_JSON, Dictionary.CONTENT_TYPE_JSON, exception);
			logger.debug("PAYLOAD: " + response);
			
			try {
				listIDs = ModelConverter.jsonListStringToObject(response);
			} catch(Exception e) {
				logger.warn("Error trying incoming list of new IDs to object. Exception: " + e.getMessage());
				exception = true;
			}
		} catch(Exception e) {
			logger.warn("Error trying to parse list of VMs: " + url + "/vms" + " Exception: " + e.getMessage());
			exception = true;
		}
		
		if(exception) return null;
		return listIDs;
	}

	/* (non-Javadoc)
	 * @see eu.ascetic.paas.applicationmanager.vmmanager.client.VmManagerClient#uploadImage(eu.ascetic.paas.applicationmanager.vmmanager.datamodel.ImageToUpload)
	 */
	@Override
	public String uploadImage(ImageToUpload imageInfo) {
		String newImageID = null;
		Boolean exception = false;
		String experimentUrl = url + "/images";
		logger.debug("URL build: " + experimentUrl);
		
		try {
			String payload = ModelConverter.objectImageToUploadToJSON(imageInfo);
			
			String response = Client.postMethod(experimentUrl, payload, Dictionary.CONTENT_TYPE_JSON, Dictionary.CONTENT_TYPE_JSON, exception);
			logger.debug("PAYLOAD: " + response);
			
			try {
				newImageID = ModelConverter.jsonStringIdToObject(response);
			} catch(Exception e) {
				logger.warn("Error trying incoming list of new IDs to object. Exception: " + e.getMessage());
				exception = true;
			}
		} catch(Exception e) {
			logger.warn("Error trying to parse new image uploaded ID: " + url + "/images" + " Exception: " + e.getMessage());
			exception = true;
		}
		
		if(exception) return null;
		
		return newImageID;
	}

	/* (non-Javadoc)
	 * @see eu.ascetic.paas.applicationmanager.vmmanager.client.VmManagerClient#deleteVmsOfApp(java.lang.String)
	 */
	@Override
	public boolean deleteVmsOfApp(String appId) {
		
		Boolean exception = false;
		String experimentUrl = url + "/vmsapp/" + appId;
		logger.debug("URL build: " + experimentUrl);
		
		try {
			Client.deleteMethod(experimentUrl, Dictionary.CONTENT_TYPE_JSON, exception);			
		} catch(Exception e) {
			logger.warn("Error trying to delete VMs of App: " + url  + "/vmsapp/" + appId + " Exception: " + e.getMessage());
			exception = true;
		}
		
		return !exception;		
	}

	/* (non-Javadoc)
	 * @see eu.ascetic.paas.applicationmanager.vmmanager.client.VmManagerClient#destroyVM(java.lang.String)
	 */
	@Override
	public boolean deleteVM(String vmId) {

		Boolean exception = false;
		String experimentUrl = url + "/vms/" + vmId;
		logger.debug("URL build: " + experimentUrl);
		
		try {
			Client.deleteMethod(experimentUrl, Dictionary.CONTENT_TYPE_JSON, exception);			
		} catch(Exception e) {
			logger.warn("Error trying to delete the VM with ID = " + vmId + ": " + url + "/vms/" + vmId + " Exception: " + e.getMessage());
			exception = true;
		}
		
		return !exception;  		
	}

	@Override
	public boolean changeStateVm(String vmId, String action) {
		
		Boolean exception = false;
		String experimentUrl = url + "/vms/" + vmId;
		logger.debug("URL build: " + experimentUrl);
		
		try {
			String payload = ModelConverter.getJsonObjectAction(action);			
			String response = Client.putMethod(experimentUrl, payload, Dictionary.CONTENT_TYPE_JSON, Dictionary.CONTENT_TYPE_JSON, exception);
			logger.debug("PAYLOAD: " + response);			
		} catch(Exception e) {
			logger.warn("Error trying to changeStateVm: " + url + "/vms/" + vmId + " Exception: " + e.getMessage());
			exception = true;
		}		
		return !exception;
	}

}
