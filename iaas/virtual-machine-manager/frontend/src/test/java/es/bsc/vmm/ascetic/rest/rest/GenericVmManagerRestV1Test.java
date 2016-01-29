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

import com.google.gson.*;
import com.jayway.restassured.RestAssured;

import es.bsc.demiurge.core.configuration.Config;
import es.bsc.demiurge.core.models.images.ImageToUpload;
import es.bsc.demiurge.core.models.images.ImageUploaded;
import es.bsc.demiurge.core.models.images.ListImagesUploaded;
import es.bsc.demiurge.core.models.vms.ListVmsDeployed;
import es.bsc.demiurge.core.models.vms.Vm;
import es.bsc.demiurge.core.models.vms.VmDeployed;
import org.apache.commons.configuration.Configuration;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.*;

@Ignore
public class GenericVmManagerRestV1Test {

    // Testing configuration variables
    private static String testImageUrl;
    private static String testImageId;
    private static String testImageName;
    private static String testDeploymentBaseUrl;

    // VM descriptions used in the tests
    private static Vm vmDescription1;
    private static Vm vmDescription2;

    private static List<String> idsVmsDeployedBeforeTests = new ArrayList<>();

    private static Gson gson = new Gson();
    private static JsonParser parser = new JsonParser();

    @BeforeClass
    public static void setUpBeforeClass() {
        initializeAttributesFromConfigFile();
        createVmDescriptionsToUseInTests();
        idsVmsDeployedBeforeTests = getIdsExistingVms();
    }

    @AfterClass
    public static void tearDownAfterClass() {
        // Make sure that all the VMs that existed before the tests are still there
        List<String> idsVmsDeployedAfterTests = getIdsExistingVms();
        for (String idVmDeployedBeforeTests: idsVmsDeployedBeforeTests) {
            assertTrue(idsVmsDeployedAfterTests.contains(idVmDeployedBeforeTests));
        }

        // Make sure that all the VMs created during the tests have been deleted
        assertTrue(idsVmsDeployedAfterTests.size() == idsVmsDeployedBeforeTests.size());
    }


    //================================================================================
    // VM Calls
    //================================================================================

    @Test
    public void getVms() {
        // Deploy 2 VMs
        RestAssured.given()
            .contentType("application/json")
            .body(getValidJsonWithTwoVmsToDeploy())
        .post(testDeploymentBaseUrl + "vms/");

        // Check that the response for the get operation contains 2 IDs + the ones that existed before the tests
        assertEquals(2 + idsVmsDeployedBeforeTests.size(), getIdsExistingVms().size());

        // Destroy the VMs deployed in this test
        deleteVms(getIdsExistingVms());
    }
    
    @Test
    public void deployVmsWithValidJson() {
        // Deploy 2 VMs
        List<String> idsVmsDeployed = deployTestVms();

        // Make sure that we get two IDs in the response
        assertEquals(2, idsVmsDeployed.size());

        // Destroy the VMs deployed in this test
        deleteVms(idsVmsDeployed);
    }
    
    @Test
    public void deployVmsWithInvalidJson() {
        RestAssured.expect()
            .statusCode(400)
        .given()
            .contentType("application/json")
            .body(getInvalidJsonWithVmsToDeploy())
        .when()
            .post(testDeploymentBaseUrl + "vms/");
    }
    
    @Test
    public void getExistingVm() {
        // Deploy 2 VMs
        List<String> idsVmsDeployed = deployTestVms();

        // Get the first VM deployed and check that all the information is correct
        String getVmDeployedResponse = RestAssured.get(testDeploymentBaseUrl + "vms/" + idsVmsDeployed.get(0)).asString();
        VmDeployed vmDeployed = gson.fromJson(getVmDeployedResponse, VmDeployed.class);
        assertEquals(vmDescription1.getName(), vmDeployed.getName());
        assertEquals(vmDescription1.getImage(), vmDeployed.getImage());
        assertTrue(vmDescription1.getCpus() == vmDeployed.getCpus());
        assertTrue(vmDescription1.getRamMb() == vmDeployed.getRamMb());
        assertTrue(vmDescription1.getDiskGb() == vmDeployed.getDiskGb());
        assertEquals(vmDescription1.getApplicationId(), vmDeployed.getApplicationId());

        // Delete the 2 VMs deployed
        deleteVms(idsVmsDeployed);
    }
    
    @Test
    public void getVmWithInvalidId() {
        RestAssured.expect()
            .statusCode(404)
        .when()
            .get(testDeploymentBaseUrl + "/vms/fakeId");
    }
    
    @Test
    public void changeStateValid() {
        // Deploy 2 VMs
        List<String> idsVmsDeployed = deployTestVms();

        // Change the state of the VMs performing a valid action.
        // Also, make sure that the status code of the response is 204.
        for (String idVmDeployed: idsVmsDeployed) {
            RestAssured.expect()
                .statusCode(204)
            .given()
                .contentType("application/json")
                .body(getValidActionJson())
            .when()
                .put(testDeploymentBaseUrl + "vms/" + idVmDeployed);
        }

        // Delete the 2 VMs deployed
        deleteVms(idsVmsDeployed);
    }
    
    @Test
    public void changeStateInvalidOption() {
        // Deploy 2 VMs
        List<String> idsVmsDeployed = deployTestVms();

        RestAssured.expect()
            .statusCode(400)
        .given()
            .contentType("application/json")
            .body(getInvalidActionJson())
        .when()
            .put(testDeploymentBaseUrl + "vms/" + idsVmsDeployed.get(0));

        // Delete the 2 VMs deployed
        deleteVms(idsVmsDeployed);

    }
    
    @Test
    public void changeStateNonExistingVm() {
        RestAssured.expect()
            .statusCode(404)
        .when()
            .put(testDeploymentBaseUrl + "vms/fakeId");
    }
    
    @Test
    public void destroyVm() {
        // Deploy 2 VMs
        List<String> idsVmsDeployed = deployTestVms();

        // Delete the VMs and check status code 204
        for (String idVmDeployed: idsVmsDeployed) {
            RestAssured.expect()
                .statusCode(204)
            .when()
                .delete(testDeploymentBaseUrl + "vms/" + idVmDeployed);
        }
    }
    
    @Test
    public void destroyNonExistingVm() {
        RestAssured.expect()
            .statusCode(404)
        .when()
            .delete(testDeploymentBaseUrl + "vms/fakeId");
    }
    
    @Test
    public void getAllVmsOfAnApplication() {
        // Deploy 2 VMs (one of them is part of "myApplication1", and the other is part of "myApplication2"
        List<String> idsVmsDeployed = deployTestVms();

        // Get the IDs of the VMs that are part of the application "myApplication1"
        String vmsOfApplicationJson = RestAssured.get(testDeploymentBaseUrl + "vmsapp/myApplication1").asString();
        List<String> idsVmsOfApp = new ArrayList<>();
        for (VmDeployed vmDeployed: gson.fromJson(vmsOfApplicationJson, ListVmsDeployed.class).getVms()) {
            idsVmsOfApp.add(vmDeployed.getId());
        }

        // Check that only one of the VMs is part of "myApplication1"
        boolean possibility1 = idsVmsOfApp.contains(idsVmsDeployed.get(0)) &&
                !idsVmsOfApp.contains(idsVmsDeployed.get(1));
        boolean possibility2 = idsVmsOfApp.contains(idsVmsDeployed.get(1)) &&
                !idsVmsOfApp.contains(idsVmsDeployed.get(0));
        assertTrue(possibility1 || possibility2);

        // Delete the 2 VMs deployed
        deleteVms(idsVmsDeployed);
    }

    @Test
    public void getAllVmsOfNonExistingAppReturnsEmptyArray() {
        String vmsOfApplicationJson = RestAssured.get(testDeploymentBaseUrl + "vmsapp/nonExistingApp").asString();
        assertEquals(0, gson.fromJson(vmsOfApplicationJson, ListVmsDeployed.class).getVms().size());
    }

    @Test
    public void deleteAllVmsOfAnApplication() {
        // Deploy 2 VMs (one of them is part of "myApplication1", and the other is part of "myApplication2"
        deployTestVms();

        // Get the IDs of the 2 VMs deployed
        String idVmApp1, idVmApp2;
        idVmApp1 = idVmApp2 = "";
        String vmsOfApplicationJson = RestAssured.get(testDeploymentBaseUrl + "vmsapp/myApplication1").asString();
        JsonObject jsonObject = gson.fromJson(vmsOfApplicationJson, JsonObject.class);
        JsonArray jsonVmsArray = jsonObject.get("vms").getAsJsonArray();
        for (JsonElement vmJson: jsonVmsArray) {
            idVmApp1 = gson.fromJson(vmJson, VmDeployed.class).getId();
        }
        vmsOfApplicationJson = RestAssured.get(testDeploymentBaseUrl + "vmsapp/myApplication2").asString();
        jsonObject = gson.fromJson(vmsOfApplicationJson, JsonObject.class);
        jsonVmsArray = jsonObject.get("vms").getAsJsonArray();
        for (JsonElement vmJson: jsonVmsArray) {
            idVmApp2 = gson.fromJson(vmJson, VmDeployed.class).getId();
        }

        // Delete VMs of "myApplication1"
        RestAssured.delete(testDeploymentBaseUrl + "vmsapp/myApplication1");

        // Get the list of existing VMs and make sure that it does not contain the ID of the VM of
        // "myApplication1" and also, that it does contain the ID of the VM of "myApplication2"
        String json = RestAssured.get(testDeploymentBaseUrl + "vms/").asString();
        List<String> idsVms = new ArrayList<>();
        jsonObject = gson.fromJson(json, JsonObject.class);
        jsonVmsArray = jsonObject.get("vms").getAsJsonArray();
        for (JsonElement vmJson: jsonVmsArray) {
            idsVms.add(gson.fromJson(vmJson, VmDeployed.class).getId());
        }
        assertTrue(idsVms.contains(idVmApp2));
        assertFalse(idsVms.contains(idVmApp1));

        // Delete the other VM deployed
        List<String> idsToDelete = new ArrayList<>();
        idsToDelete.add(idVmApp2);
        deleteVms(idsToDelete);
    }

    @Test
    public void deleteAllVmsOfNonExistingAppDoesNotReturnError() {
        RestAssured.expect()
            .statusCode(204)
        .when()
            .delete(testDeploymentBaseUrl + "vmsapp/nonExistingApp");
    }
    
    
    //================================================================================
    // VM Images Calls
    //================================================================================
    
    @Test
    public void getImages() {
        // Upload image
        String idUploadedImage = uploadTestImage();

        // Check that the response for the get operation contains the image uploaded
        String imagesJson = RestAssured.get(testDeploymentBaseUrl + "images/").asString();

        // Make sure that the image uploaded exists and that it was created with the right name
        boolean imageFound = false;
        for (ImageUploaded imageUploaded: gson.fromJson(imagesJson, ListImagesUploaded.class).getImages()) {
            if (idUploadedImage.equals(imageUploaded.getId())) {
                imageFound = true;
                assertEquals(testImageName, imageUploaded.getName());
            }
        }
        assertTrue(imageFound);

        // Delete the image created in this test
        RestAssured.delete(testDeploymentBaseUrl + "images/" + idUploadedImage);
    }
    
    @Test
    public void uploadImageWithValidJson() {
        // Upload image
        String idUploadedImage = uploadTestImage();
        assertNotNull(idUploadedImage);

        // Delete the image created in this test
        RestAssured.delete(testDeploymentBaseUrl + "images/" + idUploadedImage);
    }
    
    @Test
    public void uploadImageWithInvalidJson() {
        RestAssured.expect()
            .statusCode(400)
        .given()
            .contentType("application/json")
            .body(getInvalidImageJson())
        .when()
            .post(testDeploymentBaseUrl + "images/");
    }
    
    @Test
    public void getImage() {
        // Upload image
        String idUploadedImage = uploadTestImage();

        // Get the image by ID and check that it was created correctly
        String imageJson = RestAssured.get(testDeploymentBaseUrl + "images/" + idUploadedImage).asString();
        assertEquals(testImageName, gson.fromJson(imageJson, ImageUploaded.class).getName());

        // Delete the image created in this test
        RestAssured.delete(testDeploymentBaseUrl + "images/" + idUploadedImage);

    }
    
    @Test
    public void getNonExistingImage() {
        RestAssured.expect()
            .statusCode(404)
        .when()
            .get(testDeploymentBaseUrl + "images/fakeId");
    }
    
    @Test
    public void destroyImage() {
        // Upload image
        String idUploadedImage = uploadTestImage();

        // Delete image created. The call should return a 204 code.
        RestAssured.expect()
            .statusCode(204)
        .when()
            .delete(testDeploymentBaseUrl + "images/" + idUploadedImage);
    }
    
    @Test
    public void destroyNonExistingImage() {
        RestAssured.expect()
            .statusCode(404)
        .when()
            .delete(testDeploymentBaseUrl + "images/fakeId");
    }
    
    
    //================================================================================
    // Scheduling Algorithms methods
    //================================================================================
    
    @Test
    public void currentSchedulingAlgIsOneOfTheAvailableOnes() {
        // Get the available scheduling algorithms
        String availableSchedAlgs = RestAssured.get(testDeploymentBaseUrl + "scheduling_algorithms/").asString();
        JsonArray availableSchedAlgsJson = (JsonArray) gson.fromJson(availableSchedAlgs,
                JsonObject.class).get("scheduling_algorithms");
        List<String> availableSchedAlgNames = new ArrayList<>();
        for (JsonElement availableSchedAlgJson: availableSchedAlgsJson) {
            availableSchedAlgNames.add(availableSchedAlgJson.getAsJsonObject()
                    .get("name").getAsString());
        }

        // Get the scheduling algorithm being used now
        String currentSchedAlg = RestAssured.get(testDeploymentBaseUrl + "scheduling_algorithms/current").asString();
        String currentSchedAlgName = gson.fromJson(currentSchedAlg, JsonObject.class).get("name").getAsString();

        // Make sure that the algorithm used now is one of the available ones
        assertTrue(availableSchedAlgNames.contains(currentSchedAlgName));
    }
    
    @Test
    public void setSchedulingAlgUsingBadFormattedJson() {
        RestAssured.expect()
            .statusCode(400)
        .given()
            .contentType("application/json")
            .body(getInvalidSchedulingAlgJson())
        .when()
            .put(testDeploymentBaseUrl + "scheduling_algorithms/current");
    }
    
    @Test
    public void setSchedulingAlgUsingJsonWithNonExistingAlg() {
        RestAssured.expect()
            .statusCode(400)
        .given()
            .contentType("application/json")
            .body(getSchedulingAlgJsonNonExistingAlg())
        .when()
            .put(testDeploymentBaseUrl + "scheduling_algorithms/current");
    }
    
    
    //================================================================================
    // Private methods
    //================================================================================

    private static void initializeAttributesFromConfigFile() {
        Configuration conf = Config.INSTANCE.getConfiguration();
        testImageUrl = conf.getString("testingImageUrl");
        testImageId = conf.getString("testingImageId");
        testImageName = conf.getString("testingImageName");
        testDeploymentBaseUrl = conf.getString("testingDeploymentBaseUrl");
    }

    private static void createVmDescriptionsToUseInTests() {
        vmDescription1 = new Vm("vm1", testImageId, 2, 1024, 1, null, "myApplication1");
        vmDescription2 = new Vm("vm2", testImageId, 4, 2048, 2, null, "myApplication2");
    }

    private static List<String> getIdsExistingVms() {
        List<String> result = new ArrayList<>();
        String json = RestAssured.get(testDeploymentBaseUrl + "vms/").asString();
        for (VmDeployed vmDeployed: gson.fromJson(json, ListVmsDeployed.class).getVms()) {
            result.add(vmDeployed.getId());
        }
        return result;
    }

    private String getValidJsonWithTwoVmsToDeploy() {
        JsonObject vm1Json = (JsonObject) parser.parse(gson.toJson(vmDescription1, Vm.class));
        JsonObject vm2Json = (JsonObject) parser.parse(gson.toJson(vmDescription2, Vm.class));
        JsonArray jsonVmsArray = new JsonArray();
        jsonVmsArray.add(vm1Json);
        jsonVmsArray.add(vm2Json);
        JsonObject result = new JsonObject();
        result.add("vms", jsonVmsArray);
        return result.toString();
    }

    private String getInvalidJsonWithVmsToDeploy() {
        // Create an invalid VM JSON (it does not have all the required parameters)
        JsonObject invalidVm = new JsonObject();
        invalidVm.addProperty("name", "invalidVm");
        invalidVm.addProperty("image", testImageId);
        JsonArray jsonVmsArray = new JsonArray();
        jsonVmsArray.add(invalidVm);
        JsonObject result = new JsonObject();
        result.add("vms", jsonVmsArray);
        return result.toString();
    }

    private String getValidImageJson() {
        return gson.toJson(new ImageToUpload(testImageName, testImageUrl));
    }

    private String getInvalidImageJson() {
        JsonObject jsonObject = new JsonObject();
        jsonObject.addProperty("name", testImageName);
        return jsonObject.toString();
    }

    private String getValidActionJson() {
        JsonObject jsonObject = new JsonObject();
        jsonObject.addProperty("action", "rebootSoft");
        return jsonObject.toString();
    }

    private String getInvalidActionJson() {
        JsonObject jsonObject = new JsonObject();
        jsonObject.addProperty("action", "stopp"); // "stop" misspelled
        return jsonObject.toString();
    }

    private String getInvalidSchedulingAlgJson() {
        JsonObject jsonObject = new JsonObject();
        jsonObject.addProperty("algorithmm", "distribution"); // "algorithm" misspelled
        return jsonObject.toString();
    }

    private String getSchedulingAlgJsonNonExistingAlg() {
        JsonObject jsonObject = new JsonObject();
        jsonObject.addProperty("algorithm", "algorithmThatDoesNotExist");
        return jsonObject.toString();
    }

    private void deleteVms(List<String> ids) {
        for (String id: ids) {
            if (!idsVmsDeployedBeforeTests.contains(id)) {
                RestAssured.delete(testDeploymentBaseUrl + "vms/" + id);
            }
        }
    }

    private List<String> deployTestVms() {
        // Deploy 2 VMs
        String jsonString =
            RestAssured.given()
                .contentType("application/json")
                .body(getValidJsonWithTwoVmsToDeploy())
            .post(testDeploymentBaseUrl + "vms/").asString();

        // Return the IDs of the VMs deployed
        List<String> idsVmsDeployed = new ArrayList<>();
        JsonObject jsonObject = gson.fromJson(jsonString, JsonObject.class);
        JsonArray jsonIdsArray = jsonObject.get("ids").getAsJsonArray();
        for (JsonElement jsonId: jsonIdsArray) {
            idsVmsDeployed.add(jsonId.getAsJsonObject().get("id").getAsString());
        }
        return idsVmsDeployed;
    }

    private String uploadTestImage() {
        // Upload image
        String jsonString =
            RestAssured.given()
                .contentType("application/json")
                .body(getValidImageJson())
            .post(testDeploymentBaseUrl + "images/").asString();

        // Return the ID of the image uploaded
        return gson.fromJson(jsonString, JsonObject.class).get("id").getAsString();
    }
}
