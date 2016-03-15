/**
 Copyright (C) 2013-2014  Barcelona Supercomputing Center

 This library is free software; you can redistribute it and/or
 modify it under the terms of the GNU Lesser General Public
 License as published by the Free Software Foundation; either
 version 2.1 of the License, or (at your option) any later version.

 This library is distributed in the hope that it will be useful,
 but WITHOUT ANY WARRANTY; without even the implied warranty of
 MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 Lesser General Public License for more details.

 You should have received a copy of the GNU Lesser General Public
 License along with this library; if not, write to the Free Software
 Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301  USA
 */

package es.bsc.vmm.ascetic.rest.rest;

import es.bsc.demiurge.ws.rest.RestInputValidator;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import javax.ws.rs.WebApplicationException;
import java.util.ArrayList;
import java.util.Arrays;

public class RestInputValidatorTest {

    private RestInputValidator inputValidator = new RestInputValidator();

    @Rule
    public ExpectedException exception = ExpectedException.none();

    @Test
    public void checkImageShouldRaiseExceptionIfItDoesNotExist() {
        String[] imagesIds = {"imageId1", "imageId2"};
        exception.expect(WebApplicationException.class);
        inputValidator.checkImageExists("imageId3", new ArrayList<>(Arrays.asList(imagesIds)));
    }

    @Test
    public void checkImageShouldNotRaiseExceptionIfItExists() {
        String[] imagesIds = {"imageId1", "imageId2"};
        inputValidator.checkImageExists("imageId1", new ArrayList<>(Arrays.asList(imagesIds)));
    }

}