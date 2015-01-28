package eu.ascetic.paas.applicationmanager.vmmanager.datamodel.converter;

import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import eu.ascetic.paas.applicationmanager.vmmanager.datamodel.ImageToUpload;
import eu.ascetic.paas.applicationmanager.vmmanager.datamodel.ImageUploaded;
import eu.ascetic.paas.applicationmanager.vmmanager.datamodel.ListImagesUploaded;
import eu.ascetic.paas.applicationmanager.vmmanager.datamodel.ListVmEstimates;
import eu.ascetic.paas.applicationmanager.vmmanager.datamodel.ListVms;
import eu.ascetic.paas.applicationmanager.vmmanager.datamodel.ListVmsDeployed;
import eu.ascetic.paas.applicationmanager.vmmanager.datamodel.ListVmsToBeEstimated;
import eu.ascetic.paas.applicationmanager.vmmanager.datamodel.VmDeployed;

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
 * @author: David Rojo. Atos Research and Innovation, Atos SPAIN SA
 * e-mail david.rojoa@atos.net 
 *
 * Converts JSON representations and viceversa.
 *
 */
public class ModelConverter {
	
	/** The logger. */
	private static Logger logger = Logger.getLogger(ModelConverter.class);
	
	/**
	 * To json.
	 *
	 * @param <T> the generic type
	 * @param clazz the clazz
	 * @param t the t
	 * @return the string
	 */
//	private static <T> String toJSON(T t) {
//	    try { //TODO 
//	    	
//	    	Gson gson = new GsonBuilder().create();
//	    	return gson.toJson(t);
//	    	
////	    	 return gson.toJson(vmManager.getVm(vmId));
////	    	 return gson.toJson(new ListVmsDeployed(vmManager.getAllVms()));
//	    	
//		} catch(Exception exception) {
//			logger.info("Error converting object to JSON: " + exception.getMessage());
//			return null;
//		}      
//	}
	
	/**
	 * To object.
	 *
	 * @param <T> the generic type
	 * @param clazz the clazz
	 * @param json the json
	 * @return the t
	 */
	private static <T> T toObject(Class<T> clazz, String json) {
		try {
			
			Gson gson = new GsonBuilder().create();
			Object obj = gson.fromJson(json, clazz);
			return clazz.cast(obj);
			
		} catch(Exception exception) {
			logger.info("Error parsing JSON of object: " + exception.getMessage());
			return null;
		}    
	}
	

	/**
	 * Json list images uploaded to object.
	 *
	 * @param json the json
	 * @return the list images uploaded
	 */
	public static ListImagesUploaded jsonListImagesUploadedToObject(String json) {
		return toObject(ListImagesUploaded.class, json);
	}
	
	/**
	 * Json list vm estimates to object.
	 *
	 * @param json the json
	 * @return the list vm estimates
	 */
	public static ListVmEstimates jsonListVmEstimatesToObject(String json) {
		return toObject(ListVmEstimates.class, json);
	}
	
	public static List<String> jsonListStringToObject(String json){
//		Gson gson = new GsonBuilder().create();
//		Object obj = gson.fromJson(json, new TypeToken<List<String>>(){}.getType());
//		return (List<String>) obj;
		
//		Gson gson = new GsonBuilder().create();
//		JsonObject jsonObj = gson.fromJson(json, JsonObject.class);
		
		JsonElement jelement = new JsonParser().parse(json);
		JsonObject jsonObject = jelement.getAsJsonObject();
		JsonArray jsonIdsArray = jsonObject.get("ids").getAsJsonArray();
		List<String> idsList = new ArrayList<String>();//new String[jsonAuthorsArray.size()];
		JsonObject  jobject = null;
		String newId = null;
		for (int i = 0; i < jsonIdsArray.size(); i++) {
			jobject = jsonIdsArray.get(i).getAsJsonObject();
			newId = jobject.get("id").toString();
			//remove " at the begin and end position
			if (newId.startsWith("\"") && newId.endsWith("\"")){
				newId = newId.substring(1, newId.length()-1);
			}
			idsList.add(newId);
	    }
		
		return idsList;
//	    
//	    .getAsJsonObject()
//		
//	    JsonObject  jobject = jelement.getAsJsonObject();
//	    jobject = jobject.getAsJsonObject("ids");
//	    JsonArray jarray = jobject.getAsJsonArray("translations");
//	    jobject = jarray.get(0).getAsJsonObject();
//	    String result = jobject.get("translatedText").toString();
//	    return result;
        
	}
	
	public static String jsonStringIdToObject(String json){
		Gson gson = new GsonBuilder().create();
		return gson.fromJson(json, JsonObject.class).get("id").getAsString();
	}
	
	
	/**
	 * Json list vms to object.
	 *
	 * @param json the json
	 * @return the list vms
	 */
	public static ListVms jsonListVmsToObject(String json) {
		return toObject(ListVms.class, json);
	}
	
	/**
	 * Json list vms to be estimated to object.
	 *
	 * @param json the json
	 * @return the list vms to be estimated
	 */
	public static ListVmsToBeEstimated jsonListVmsToBeEstimatedToObject(String json) {
		return toObject(ListVmsToBeEstimated.class, json);
	}
	
	/**
	 * Json list vms deployed to object.
	 *
	 * @param json the json
	 * @return the list vms deployed
	 */
	public static ListVmsDeployed jsonListVmsDeployedToObject(String json) {
		return toObject(ListVmsDeployed.class, json);
	}
	
	/**
	 * Json image uploaded to object.
	 *
	 * @param json the json
	 * @return the image uploaded
	 */
	public static ImageUploaded jsonImageUploadedToObject(String json){
		return toObject(ImageUploaded.class, json);
	}

	
	/**
	 * Json vm deployed to object.
	 *
	 * @param json the json
	 * @return the vm deployed
	 */
	public static VmDeployed jsonVmDeployedToObject(String json){
		return toObject(VmDeployed.class, json);
	}
	
	
	/**
	 * Object image uploaded to json.
	 *
	 * @param imageUploaded the image uploaded
	 * @return the string
	 */
	public static String objectImageUploadedToJSON(ImageUploaded imageUploaded) {
//		return toJSON(imageUploaded);
		Gson gson = new GsonBuilder().create();
		return gson.toJson(imageUploaded);
	}
	
	
	public static String objectImageToUploadToJSON(ImageToUpload imageToUpload) {
//		return toJSON(imageUploaded);
		Gson gson = new GsonBuilder().create();
		return gson.toJson(imageToUpload);
	}
	
	/**
	 * Object list images uploaded to json.
	 *
	 * @param listImagesUploaded the list images uploaded
	 * @return the string
	 */
	public static String objectListImagesUploadedToJSON(ListImagesUploaded listImagesUploaded) {
//		return toJSON(listImagesUploaded);
		Gson gson = new GsonBuilder().create();
		return gson.toJson(listImagesUploaded);
	}
	
	/**
	 * Object list vm estimates to json.
	 *
	 * @param listVmsEstimates the list vms estimates
	 * @return the string
	 */
	public static String objectListVmEstimatesToJSON(ListVmEstimates listVmsEstimates) {
//		return toJSON(listVmsEstimates);
		Gson gson = new GsonBuilder().create();
		return gson.toJson(listVmsEstimates);
	}
	
	/**
	 * Object list vms deployed to json.
	 *
	 * @param listVmsDeployed the list vms deployed
	 * @return the string
	 */
	public static String objectListVmsDeployedToJSON(ListVmsDeployed listVmsDeployed) {
//		return toJSON(listVmsDeployed);
		Gson gson = new GsonBuilder().create();
		return gson.toJson(listVmsDeployed);
	}
	
	/**
	 * Object list vms to be estimated to json.
	 *
	 * @param listVmsToBeEstimated the list vms to be estimated
	 * @return the string
	 */
	public static String objectListVmsToBeEstimatedToJSON(ListVmsToBeEstimated listVmsToBeEstimated) {
//		return toJSON(listVmsToBeEstimated);
		Gson gson = new GsonBuilder().create();
		return gson.toJson(listVmsToBeEstimated);
	}
	
	/**
	 * Object list vms to json.
	 *
	 * @param listVms the list vms
	 * @return the string
	 */
	public static String objectListVmsToJSON(ListVms listVms) {
//		return toJSON(listVms);
		Gson gson = new GsonBuilder().create();
		return gson.toJson(listVms);
	}
	
	/**
	 * Object vm deployed to json.
	 *
	 * @param vmDeployed the vm deployed
	 * @return the string
	 */
	public static String objectVmDeployedToJSON(VmDeployed vmDeployed) {
//		return toJSON(vmDeployed);
		Gson gson = new GsonBuilder().create();
		return gson.toJson(vmDeployed);
	}
	
	
	/**
	 * Gets the json object action.
	 *
	 * @param action the action
	 * @return the json object action
	 */
	public static String getJsonObjectAction(String action){
        JsonObject jsonObject = new JsonObject();
        jsonObject.addProperty("action", action);
        return jsonObject.toString();
	}
	
}
