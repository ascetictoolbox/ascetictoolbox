package eu.ascetic.paas.applicationmanager.rest;

import java.util.List;

import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import eu.ascetic.paas.applicationmanager.dao.ImageDAO;
import eu.ascetic.paas.applicationmanager.model.Application;
import eu.ascetic.paas.applicationmanager.model.Image;
import eu.ascetic.paas.applicationmanager.model.VM;
import eu.ascetic.paas.applicationmanager.rest.util.XMLBuilder;
import eu.ascetic.paas.applicationmanager.vmmanager.client.VmManagerClient;
import eu.ascetic.paas.applicationmanager.vmmanager.client.VmManagerClientHC;

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
 * @author: David Garcia Perez. Atos Research and Innovation, Atos SPAIN SA
 * e-mail david.garciaperez@atos.net 
 * 
 * Imagecache rest interface... 
 *
 */
@Path("/applications/{application_name}/cache-images")
@Component
@Scope("request")
public class CacheImageRest extends AbstractRest {
	private static Logger logger = Logger.getLogger(CacheImageRest.class);
	@Autowired
	protected ImageDAO imageDAO;
	protected VmManagerClient vmManagerClient = new VmManagerClientHC();
	
	/**
	 * Returns the cache images associated to an application in the DB
	 * @param applicationName from which we want to find the cache images
	 * @return nulls, if no cache images are available.
	 */
	@GET
	@Produces(MediaType.APPLICATION_XML)
	public Response getCacheImagesForApplication(@PathParam("application_name") String applicationName) {
		logger.info("GET request to paht: /applications/" + applicationName + "/cache-images");
		
		// We read first the application from the DB
		Application application = applicationDAO.getByName(applicationName);
		// We get the list of cache images for that application
		List<Image> images = imageDAO.getCacheImagesForApplication(application);
		
		String xml = XMLBuilder.getCollectionOfCacheImagesXML(images, applicationName);
		
		return buildResponse(Status.OK, xml);
	}

	/**
	 * Deletes a cache image if that image is not being using by a VM at the present moment
	 * @param imageId id of the image
	 * @return 204 if everything went ok, specific error message otherwise
	 */
	@DELETE
	@Path("{image_id}")
	public Response deleteCacheImage(@PathParam("application_name") String applicationName, @PathParam("image_id") String imageId) {
		logger.info("GET request to paht: /applications/" + applicationName + "/cache-images/" + imageId);
		
		int id = 0;
		
		try {
			id = Integer.parseInt(imageId);
		} catch(NumberFormatException ex) {
			return buildResponse(Status.BAD_REQUEST, "Id: " + imageId + " is not a valid image id!!!");
		}
		
		Image image = imageDAO.getById(id);
		
		if(image == null) {
			return buildResponse(Status.NOT_FOUND, "No image with id: " + id + " found in the DB!");
		} else if(!image.isDemo()) {
			return buildResponse(Status.BAD_REQUEST, "Image with ID: " + id + " is not a cache Image, not possible to delete it.");
		}
		
		List<VM> vms = vmDAO.getNotDeletedVMsWithImage(image);
		
		if(vms.size() != 0) {
			return buildResponse(Status.CONFLICT, "Image with ID: " + image.getId() + " is still being used by one or more VMs.");
		}
		
		if(vmManagerClient.deleteImage(image.getProviderImageId())) {
			image.setDemo(false);
			imageDAO.update(image);
			
			return buildResponse(Status.NO_CONTENT, "");
		} else {
			return buildResponse(Status.INTERNAL_SERVER_ERROR, "Image with provider image id = " +  image.getProviderImageId() + " cannot be deleted in VM Manager");
		}
	}
}
