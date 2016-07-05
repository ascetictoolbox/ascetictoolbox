package eu.ascetic.paas.applicationmanager.vmmanager.client;

import org.apache.log4j.Logger;

import es.bsc.vmmclient.models.ImageToUpload;
import eu.ascetic.paas.applicationmanager.dao.ApplicationDAO;
import eu.ascetic.paas.applicationmanager.dao.ImageDAO;
import eu.ascetic.paas.applicationmanager.model.Application;
import eu.ascetic.paas.applicationmanager.model.Image;

public class ImageUploader {
	private static Logger logger = Logger.getLogger(ImageUploader.class);

	public static Image uploadImage(String urlImg, String fileId, boolean demo, String applicationName, VmManagerClient vmManagerClient, ApplicationDAO applicationDAO, ImageDAO imageDAO) {
		String name = urlImg.substring(urlImg.lastIndexOf("/")+1, urlImg.length());
		
		ImageToUpload imgToUpload = new ImageToUpload(name, urlImg);
		logger.info("Image to upload name: '" + imgToUpload.getName() + "' url '" + imgToUpload.getUrl() + "'");
		
		String imageProviderId = vmManagerClient.uploadImage(imgToUpload);
		logger.info("Provider image id: " + imageProviderId);
		
		//Saving the new image to the database
		Image image = new Image();
		image.setProviderImageId(imageProviderId);
		image.setOvfHref(urlImg);
		image.setDemo(false);
		image.setOvfId(fileId);
		image.setDemo(demo);
		
		imageDAO.save(image);
		logger.info("Image storaged to the DB: id: " + image.getId() + ", ovf-id: " + image.getOvfId() + ", ovf-href: " + image.getOvfHref() 
				                                 + ", provider-image-id: " + image.getProviderImageId() + ", is demo?: " + image.isDemo());
		
		logger.debug("#### applicationName: " + applicationName);
		
		Application application = applicationDAO.getByName(applicationName);
		logger.debug("#### applicationName: " + application);
		if(application != null) {
			logger.debug("#### applicationName: <-->");
			//application.addImage(image);
			image.setApplication(application);
			imageDAO.update(image);
			//boolean x = applicationDAO.update(application);
			//logger.debug("#### applicationName: " + x);
		}
		
		return image;
	}
}
