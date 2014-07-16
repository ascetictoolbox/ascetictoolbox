package es.bsc.vmmanagercore.rest;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import javax.ws.rs.WebApplicationException;
import java.util.ArrayList;
import java.util.Arrays;

public class VmmRestInputValidatorTest {

    @Rule
    public ExpectedException exception = ExpectedException.none();

    @Test
    public void checkImageShouldRaiseExceptionIfItDoesNoExist() {
        String[] imagesIds = {"imageId1", "imageId2"};
        exception.expect(WebApplicationException.class);
        VmmRestInputValidator.checkImageExists("imageId3", new ArrayList<>(Arrays.asList(imagesIds)));
    }

    @Test
    public void checkImageShouldNotRaiseExceptionIfItExists() {
        String[] imagesIds = {"imageId1", "imageId2"};
        VmmRestInputValidator.checkImageExists("imageId1", new ArrayList<>(Arrays.asList(imagesIds)));
    }

}