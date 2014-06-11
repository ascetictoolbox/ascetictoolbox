package es.bsc.vmmanagercore.model;

import static org.junit.Assert.*;

import org.junit.Before;
import org.junit.Test;

/**
*
*
* @author David Ortiz Lopez (david.ortiz@bsc.es)
*
*/
public class ImageDescriptionTest {

	private ImageUploaded imageDesc;
	
	@Before
	public void setUp() {
		imageDesc = new ImageUploaded("testImageId", "testImage", "active");
	}
	
	@Test
	public void setGetId() {
		imageDesc.setId("newImageId");
		assertEquals("newImageId", imageDesc.getId());
	}
	
	@Test
	public void setGetName() {
		imageDesc.setName("newImageName");
		assertEquals("newImageName", imageDesc.getName());
	}
	
	@Test
	public void setGetStatus() {
		imageDesc.setStatus("fakeStatus");
		assertEquals("fakeStatus", imageDesc.getStatus());
	}

}
