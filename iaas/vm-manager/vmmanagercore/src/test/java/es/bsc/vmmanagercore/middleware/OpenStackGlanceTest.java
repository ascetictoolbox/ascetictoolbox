package es.bsc.vmmanagercore.middleware;

import org.junit.BeforeClass;
import org.junit.Test;

import es.bsc.vmmanagercore.cloudmiddleware.OpenStackGlance;
import es.bsc.vmmanagercore.manager.VmManagerConfiguration;
import es.bsc.vmmanagercore.model.ImageToUpload;

public class OpenStackGlanceTest {

	private static OpenStackGlance glance;
	
	@BeforeClass
	public static void setUpClass() {
		glance = new OpenStackGlance();
	}
	
	//This test only checks that the create and delete operations do not raise exceptions.
	@Test
	public void canCreateAndDelete() {
		ImageToUpload imageToUpload = new ImageToUpload("testImage", 
				VmManagerConfiguration.getInstance().testingImageUrl);
		String imageId = glance.createImageFromUrl(imageToUpload);
		glance.deleteImage(imageId);
	}

}
