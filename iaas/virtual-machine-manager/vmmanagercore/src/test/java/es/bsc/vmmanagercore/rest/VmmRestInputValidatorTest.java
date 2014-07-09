package es.bsc.vmmanagercore.rest;

import org.junit.Test;

import java.util.ArrayList;
import java.util.Arrays;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;


public class VmmRestInputValidatorTest {

    @Test
    public void imageDoesNotExist() {
        String[] imagesIds = {"imageId1", "imageId2"};
        assertFalse(VmmRestInputValidator.checkImageExists("imageId3", new ArrayList<>(Arrays.asList(imagesIds))));
    }

    @Test
    public void imageExists() {
        String[] imagesIds = {"imageId1", "imageId2"};
        assertTrue(VmmRestInputValidator.checkImageExists("imageId1", new ArrayList<>(Arrays.asList(imagesIds))));
    }

}