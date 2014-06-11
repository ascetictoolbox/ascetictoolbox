package es.bsc.vmmanagercore.rest;

import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.Arrays;

import org.junit.Test;


public class VmManagerRestInputValidatorTest {

	@Test
	public void imageDoesNotExist() {
		String[] imagesIds = {"imageId1", "imageId2"};
		assertFalse(VmManagerRestInputValidator.checkImageExists("imageId3", 
				new ArrayList<>(Arrays.asList(imagesIds))));
	}

	@Test
	public void imageExists() {
		String[] imagesIds = {"imageId1", "imageId2"};
		assertTrue(VmManagerRestInputValidator.checkImageExists("imageId1", 
				new ArrayList<>(Arrays.asList(imagesIds))));
	}
	
}