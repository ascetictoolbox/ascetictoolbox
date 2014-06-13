package es.bsc.vmmanagercore.rest;

import static com.jayway.restassured.RestAssured.delete;
import static com.jayway.restassured.RestAssured.expect;
import static com.jayway.restassured.RestAssured.get;
import static com.jayway.restassured.RestAssured.given;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import es.bsc.vmmanagercore.manager.VmManagerConfiguration;
import es.bsc.vmmanagercore.model.ImageToUpload;
import es.bsc.vmmanagercore.model.ImageUploaded;
import es.bsc.vmmanagercore.model.Vm;
import es.bsc.vmmanagercore.model.VmDeployed;

public class VmManagerRestTest {

    // Testing configuration variables
    private static String testImageUrl;
    private static String testImageId;
    private static String testImageName;
    private static String testDeploymentBaseUrl;

    // VM descriptions used in the tests
    private static Vm vmDescription1;
    private static Vm vmDescription2;

    private static ArrayList<String> idsVmsDeployedBeforeTests = new ArrayList<>();

    private static Gson gson = new Gson();
    private static JsonParser parser = new JsonParser();

    private static void initializeAttributesFromConfigFile() {
        VmManagerConfiguration conf = VmManagerConfiguration.getInstance();
        testImageUrl = conf.testingImageUrl;
        testImageId = conf.testingImageId;
        testImageName = conf.testingImageName;
        testDeploymentBaseUrl = conf.testingDeploymentBaseUrl;
    }

    @BeforeClass
    public static void setUpBeforeClass() {
        initializeAttributesFromConfigFile();

        // Create 2 VM descriptions to use in the tests
        vmDescription1 = new Vm("vm1", testImageId, 2, 1024, 1, null, "myApplication1");
        vmDescription2 = new Vm("vm2", testImageId, 4, 2048, 2, null, "myApplication2");

        // Save IDs of existing VMs before running the tests
        String json = get(testDeploymentBaseUrl + "vms/").asString();
        JsonObject jsonObject = gson.fromJson(json, JsonObject.class);
        JsonArray jsonVmsArray = jsonObject.get("vms").getAsJsonArray();
        for (JsonElement vmJson: jsonVmsArray) {
            VmDeployed vm = gson.fromJson(vmJson, VmDeployed.class);
            idsVmsDeployedBeforeTests.add(vm.getId());
        }

        for (String id: idsVmsDeployedBeforeTests) {
            System.out.println(id);
        }
    }

    @AfterClass
    public static void tearDownAfterClass() {
        // Make sure that all the VMs that existed before the tests are still there
        String json = get(testDeploymentBaseUrl + "vms/").asString();
        ArrayList<String> idsVmsDeployedAfterTests = new ArrayList<>();
        JsonObject jsonObject = gson.fromJson(json, JsonObject.class);
        JsonArray jsonVmsArray = jsonObject.get("vms").getAsJsonArray();
        for (JsonElement vmJson: jsonVmsArray) {
            VmDeployed vm = gson.fromJson(vmJson, VmDeployed.class);
            idsVmsDeployedAfterTests.add(vm.getId());
        }
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
        given()
            .contentType("application/json")
            .body(getValidJsonWithTwoVmsToDeploy())
        .post(testDeploymentBaseUrl + "vms/");

        // Check that the response for the get operation contains 2 IDs
        String json = get(testDeploymentBaseUrl + "vms/").asString();
        ArrayList<String> idsVmsDeployed = new ArrayList<>();
        JsonObject jsonObject = gson.fromJson(json, JsonObject.class);
        JsonArray jsonVmsArray = jsonObject.get("vms").getAsJsonArray();
        for (JsonElement vmJson: jsonVmsArray) {
            VmDeployed vmDeployed = gson.fromJson(vmJson, VmDeployed.class);
            idsVmsDeployed.add(vmDeployed.getId());
        }
        assertEquals(2 + idsVmsDeployedBeforeTests.size(), idsVmsDeployed.size());

        // Destroy the VMs deployed in this test
        deleteVms(idsVmsDeployed);
    }
    
    @Test
    public void deployVmsWithValidJson() {
        // Deploy 2 VMs
        ArrayList<String> idsVmsDeployed = deployTestVms();

        // Make sure that we get two IDs in the response
        assertEquals(2, idsVmsDeployed.size());

        // Destroy the VMs deployed in this test
        deleteVms(idsVmsDeployed);
    }
    
    @Test
    public void deployVmsWithInvalidJson() {
        expect()
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
        ArrayList<String> idsVmsDeployed = deployTestVms();

        // Get the first VM deployed and check that all the information is correct
        String getVmDeployedResponse = get(testDeploymentBaseUrl + "vms/" + idsVmsDeployed.get(0)).asString();
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
        expect()
            .statusCode(404)
        .when()
            .get(testDeploymentBaseUrl + "/vms/fakeId");
    }
    
    @Test
    public void changeStateValid() {
        // Deploy 2 VMs
        ArrayList<String> idsVmsDeployed = deployTestVms();

        // Change the state of the VMs performing a valid action.
        // Also, make sure that the status code of the response is 204.
        for (String idVmDeployed: idsVmsDeployed) {
            expect()
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
        ArrayList<String> idsVmsDeployed = deployTestVms();

        expect()
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
        expect()
            .statusCode(404)
        .when()
            .put(testDeploymentBaseUrl + "vms/fakeId");
    }
    
    @Test
    public void destroyVm() {
        // Deploy 2 VMs
        ArrayList<String> idsVmsDeployed = deployTestVms();

        // Delete the VMs and check status code 204
        for (String idVmDeployed: idsVmsDeployed) {
            expect()
                .statusCode(204)
            .when()
                .delete(testDeploymentBaseUrl + "vms/" + idVmDeployed);
        }
    }
    
    @Test
    public void destroyNonExistingVm() {
        expect()
            .statusCode(404)
        .when()
            .delete(testDeploymentBaseUrl + "vms/fakeId");
    }
    
    @Test
    public void getAllVmsOfAnApplication() {
        // Deploy 2 VMs (one of them is part of "myApplication1", and the other is part of
        ArrayList<String> idsVmsDeployed = deployTestVms();

        // Get the IDs of the VMs that are part of the application "myApplication1"
        String vmsOfApplicationJson =
                get(testDeploymentBaseUrl + "vmsapp/myApplication1").asString();
        ArrayList<String> idsVmsOfApp = new ArrayList<>();
        JsonObject jsonObject = gson.fromJson(vmsOfApplicationJson, JsonObject.class);
        JsonArray jsonVmsArray = jsonObject.get("vms").getAsJsonArray();
        for (JsonElement vmJson: jsonVmsArray) {
            VmDeployed vm = gson.fromJson(vmJson, VmDeployed.class);
            idsVmsOfApp.add(vm.getId());
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
    
    
    //================================================================================
    // VM Images Calls
    //================================================================================
    
    @Test
    public void getImages() {
        // Upload image
        String idUploadedImage = uploadTestImage();

        // Check that the response for the get operation contains the image uploaded
        String imagesJson = get(testDeploymentBaseUrl + "images/").asString();
        JsonObject jsonObject = gson.fromJson(imagesJson, JsonObject.class);
        JsonArray jsonImagesArray = jsonObject.get("images").getAsJsonArray();
        for (JsonElement imageJson: jsonImagesArray) {
            ImageUploaded image = gson.fromJson(imageJson, ImageUploaded.class);
            if (idUploadedImage.equals(image.getId())) {
                assertEquals(testImageName, image.getName());
            }
        }

        // Delete the image created in this test
        delete(testDeploymentBaseUrl + "images/" + idUploadedImage);
    }
    
    @Test
    public void uploadImageWithValidJson() {
        // Upload image
        String idUploadedImage = uploadTestImage();
        assertNotNull(idUploadedImage);

        // Delete the image created in this test
        delete(testDeploymentBaseUrl + "images/" + idUploadedImage);
    }
    
    @Test
    public void uploadImageWithInvalidJson() {
        expect()
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
        String imageJson = get(testDeploymentBaseUrl + "images/" + idUploadedImage).asString();
        ImageUploaded image = gson.fromJson(imageJson, ImageUploaded.class);
        assertEquals(testImageName, image.getName());

        // Delete the image created in this test
        delete(testDeploymentBaseUrl + "images/" + idUploadedImage);

    }
    
    @Test
    public void getNonExistingImage() {
        expect()
            .statusCode(404)
        .when()
            .get(testDeploymentBaseUrl + "images/fakeId");
    }
    
    @Test
    public void destroyImage() {
        // Upload image
        String idUploadedImage = uploadTestImage();

        // Delete image created. The call should return a 204 code.
        expect()
            .statusCode(204)
        .when()
            .delete(testDeploymentBaseUrl + "images/" + idUploadedImage);
    }
    
    @Test
    public void destroyNonExistingImage() {
        expect()
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
        String availableSchedAlgs = get(testDeploymentBaseUrl + "scheduling_algorithms/").asString();
        JsonArray availableSchedAlgsJson = (JsonArray) gson.fromJson(availableSchedAlgs,
                JsonObject.class).get("scheduling_algorithms");
        ArrayList<String> availableSchedAlgNames = new ArrayList<>();
        for (JsonElement availableSchedAlgJson: availableSchedAlgsJson) {
            availableSchedAlgNames.add(availableSchedAlgJson.getAsJsonObject()
                    .get("name").getAsString());
        }

        // Get the scheduling algorithm being used now
        String currentSchedAlg = get(testDeploymentBaseUrl + "scheduling_algorithms/current").asString();
        String currentSchedAlgName = gson.fromJson(currentSchedAlg, JsonObject.class).get("name").getAsString();

        // Make sure that the algorithm used now is one of the available ones
        assertTrue(availableSchedAlgNames.contains(currentSchedAlgName));
    }
    
    @Test
    public void setSchedulingAlgUsingBadFormattedJson() {
        expect()
            .statusCode(400)
        .given()
            .contentType("application/json")
            .body(getInvalidSchedulingAlgJson())
        .when()
            .put(testDeploymentBaseUrl + "scheduling_algorithms/current");
    }
    
    @Test
    public void setSchedulingAlgUsingJsonWithNonExistingAlg() {
        expect()
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
        invalidVm.addProperty("name", "vm1");
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
        jsonObject.addProperty("action", "stop");
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

    private void deleteVms(ArrayList<String> ids) {
        for (String id: ids) {
            if (!idsVmsDeployedBeforeTests.contains(id)) {
                delete(testDeploymentBaseUrl + "vms/" + id);
            }
        }
    }

    private ArrayList<String> deployTestVms() {
        // Deploy 2 VMs
        String jsonString =
            given()
                .contentType("application/json")
                .body(getValidJsonWithTwoVmsToDeploy())
            .post(testDeploymentBaseUrl + "vms/").asString();

        // Return the IDs of the VMs deployed
        ArrayList<String> idsVmsDeployed = new ArrayList<>();
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
            given()
                .contentType("application/json")
                .body(getValidImageJson())
            .post(testDeploymentBaseUrl + "images/").asString();

        // Return the ID of the image uploaded
        return gson.fromJson(jsonString, JsonObject.class).get("id").getAsString();
    }

}
