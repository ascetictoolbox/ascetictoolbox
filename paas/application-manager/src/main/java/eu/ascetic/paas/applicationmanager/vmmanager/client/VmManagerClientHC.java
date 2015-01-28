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
 * 
 * Copyright 2014 ATOS SPAIN S.A. 
 * 
 * Licensed under the Apache License, Version 2.0 (the License);
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * 
 * @author: David Rojo, David Garcia Perez Atos Research and Innovation, Atos SPAIN SA
 * e-mail david.rojoa@atos.net , david.garciaperez@atos.net
 * 
 * The Apache HTTP Client implementation of the VM Manager ASCETiC Client
 */
public class VmManagerClientHC implements VmManagerClient {
	private static Logger logger = Logger.getLogger(VmManagerClientHC.class);
	private String url;
	
	/**
	 * Creates a new VM Manager Client using the URL
	 * to the VM Manager the one in the Application Manager
	 * Configuration file.
	 */
	public VmManagerClientHC() {
		this.url = Configuration.vmManagerServiceUrl;
	}

	@Override
	public String getURL() {
		return url;
	}

	@Override
	public void setURL(String url) {
		this.url = url;
	}

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

	@Override
	public List<String> deployVMs(List<Vm> vms) {
		List<String> listIDs = null;
		Boolean exception = false;
		String vmDeployUrl = url + "/vms";
		logger.debug("URL build: " + vmDeployUrl);
		
		try {
			ListVms listVms = new ListVms(vms);
			String payload = ModelConverter.objectListVmsToJSON(listVms);
			
			String response = Client.postMethod(vmDeployUrl, payload, Dictionary.CONTENT_TYPE_JSON, Dictionary.CONTENT_TYPE_JSON, exception);
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

	@Override
	public boolean changeStateVm(String vmId, String action) {
		// TODO Auto-generated method stub
		return false;
	}

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
	public boolean deleteVmsOfApp(String appId) {
		// TODO Auto-generated method stub
		return false;
	}

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

	@Override
	public boolean deleteImage(String id) {
		Boolean exception = false;
		String imageUrl = url + "/images/" + id;
		
		try {
			Client.deleteMethod(imageUrl, Dictionary.CONTENT_TYPE_JSON, exception);			
		} catch(Exception e) {
			logger.warn("Error trying to delete the image with ID = " + id + ": " + imageUrl + " Exception: " + e.getMessage());
			exception = true;
		}
		
		return !exception;  
	}

}
