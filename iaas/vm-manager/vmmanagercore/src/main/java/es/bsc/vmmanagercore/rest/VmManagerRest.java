package es.bsc.vmmanagercore.rest;

import com.google.gson.*;
import es.bsc.vmmanagercore.manager.VmManager;
import es.bsc.vmmanagercore.model.*;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import java.util.ArrayList;
import java.util.Collection;

/**
 *
 *
 * @author David Ortiz Lopez (david.ortiz@bsc.es)
 *
 */

@Path("/vmmanager")
public class VmManagerRest {

    private static String DB_NAME = "VmManagerDb";
    private Gson gson = new Gson();
    private static JsonParser parser = new JsonParser();

    private VmManager vmManager = new VmManager(DB_NAME);

    //================================================================================
    // VM Methods
    //================================================================================

    @GET
    @Path("/vms")
    @Produces(MediaType.APPLICATION_JSON)
    public String getAllVms() {
        ArrayList<VmDeployed> vmsDeployed = vmManager.getAllVms();
        JsonArray jsonVmsArray = new JsonArray();
        for (VmDeployed vmDeployed: vmsDeployed) {
            JsonObject vmJson =
                    (JsonObject) parser.parse(gson.toJson(vmDeployed, VmDeployed.class));
            jsonVmsArray.add(vmJson);
        }
        JsonObject result = new JsonObject();
        result.add("vms", jsonVmsArray);
        return result.toString();
    }

    @POST
    @Path("/vms")
    @Consumes("application/json")
    @Produces(MediaType.APPLICATION_JSON)
    public String deployVMs(String vmDescriptions) {
        // Get the JSON object that contains the VMs that need to be deployed
        JsonObject vmsJson = gson.fromJson(vmDescriptions, JsonObject.class);

        // Check if the JSON that contains the VMs is specified correctly
        if (!VmManagerRestInputValidator.checkVmDescriptions(vmsJson)) {
            throw new WebApplicationException(400);
        }

        // Get the information of the VMs to deploy
        ArrayList<Vm> vmsToDeploy = new ArrayList<>();
        JsonArray vmsArrayJson = vmsJson.getAsJsonArray("vms");
        for (JsonElement vmJson: vmsArrayJson) {
            Vm vm = gson.fromJson(vmJson, Vm.class);
            vmsToDeploy.add(vm);
        }

        // Deploy the VMs
        ArrayList<String> idsVmsDeployed = vmManager.deployVms(vmsToDeploy);

        // Return the JSON with the IDs of the VMs deployed
        JsonArray idsArrayJson = new JsonArray();
        for (String idVmDeployed: idsVmsDeployed) {
            JsonObject idJson = new JsonObject();
            idJson.addProperty("id", idVmDeployed);
            idsArrayJson.add(idJson);
        }
        JsonObject result = new JsonObject();
        result.add("ids", idsArrayJson);
        return result.toString();
    }

    @GET
    @Path("/vms/{id}")
    @Produces(MediaType.APPLICATION_JSON)
    public String getVm(@PathParam("id") String vmId) {
        VmDeployed vmDescription = vmManager.getVm(vmId);
        if (vmDescription == null) { // If the VM does not exist, return 404 error
            throw new WebApplicationException(404);
        }
        return gson.toJson(vmDescription);
    }

    @PUT
    @Path("/vms/{id}")
    @Consumes("application/json")
    public void changeStateVm(@PathParam("id") String vmId, String actionJson) {
        if (!vmManager.existsVm(vmId)) { // If the VM does not exists, return a 404 error
            throw new WebApplicationException(404);
        }

        // Get the action to perform (resume, reboot, etc.)
        JsonObject jsonObject = gson.fromJson(actionJson, JsonObject.class);
        if (jsonObject.get("action") == null) {
            throw new WebApplicationException(400);
        }
        String action = jsonObject.get("action").getAsString();

        // Perform the action or return a 400 error if the user did not specify a valid action
        if (!VmManagerRestInputValidator.isValidAction(action)) {
            throw new WebApplicationException(400);
        }
        vmManager.performActionOnVm(vmId, action);
    }

    @DELETE
    @Path("/vms/{id}")
    public void destroyVm(@PathParam("id") String vmId) {
        if (!vmManager.existsVm(vmId)) { // If the VM does not exists, return a 404 error
            throw new WebApplicationException(404);
        }
        vmManager.deleteVm(vmId);
    }

    @GET
    @Path("/vmsapp/{appId}")
    @Produces(MediaType.APPLICATION_JSON)
    public String getVmsOfApp(@PathParam("appId") String appId) {
        JsonArray vmsDeployedJsonArray = new JsonArray();
        ArrayList<VmDeployed> vmsDeployed = vmManager.getVmsOfApp(appId);
        for (VmDeployed vmDeployed: vmsDeployed) {
            JsonElement vmJsonElement = gson.toJsonTree(vmDeployed, VmDeployed.class);
            vmsDeployedJsonArray.add(vmJsonElement);
        }
        JsonObject result = new JsonObject();
        result.add("vms", vmsDeployedJsonArray);
        return result.toString();
    }


    //================================================================================
    // VM Images Methods
    //================================================================================

    @GET
    @Path("/images")
    @Produces(MediaType.APPLICATION_JSON)
    public String getAllImages() {
        JsonArray jsonImagesArray = new JsonArray();
        Collection<ImageUploaded> images = vmManager.getVmImages();
        for (ImageUploaded image: images) {
            JsonObject imageJson =
                    (JsonObject) parser.parse(gson.toJson(image, ImageUploaded.class));
            jsonImagesArray.add(imageJson);
        }
        JsonObject result = new JsonObject();
        result.add("images", jsonImagesArray);
        return result.toString();
    }

    @POST
    @Path("/images")
    @Consumes("application/json")
    @Produces(MediaType.APPLICATION_JSON)
    public String uploadImage(String imageInfo) {
        // Read the input JSON
        ImageToUpload imageToUpload =
            gson.fromJson(imageInfo, ImageToUpload.class);

        // Throw a 400 exception if the format of the JSON is not correct
        if (imageToUpload.getName() == null || imageToUpload.getUrl() == null) {
            throw new WebApplicationException(400);
        }

        // Create the image and return its ID
        String id = vmManager.createVmImage(imageToUpload);
        JsonObject idJson = new JsonObject();
        idJson.addProperty("id", id);
        return idJson.toString();
    }

    @GET
    @Path("/images/{id}")
    @Produces(MediaType.APPLICATION_JSON)
    public String getImage(@PathParam("id") String imageId) {
        // Throw error if the image does not exist
        if (!VmManagerRestInputValidator.checkImageExists(imageId, vmManager.getVmImagesIds())) {
            throw new WebApplicationException(404);
        }

        // Return JSON representation of the image
        return gson.toJson(vmManager.getVmImage(imageId));
    }

    @DELETE
    @Path("/images/{id}")
    public void deleteImage(@PathParam("id") String imageId) {
        // Throw error if the image does not exist
        if (!VmManagerRestInputValidator.checkImageExists(imageId, vmManager.getVmImagesIds())) {
            throw new WebApplicationException(404);
        }

        // Delete the image
        vmManager.deleteVmImage(imageId);
    }


    //================================================================================
    // Scheduling algorithm Methods
    //================================================================================

    @GET
    @Path("/scheduling_algorithms")
    @Produces(MediaType.APPLICATION_JSON)
    public String getSchedulingAlgorithms() {
        ArrayList<SchedulingAlgorithm> schedAlgs = vmManager.getAvailableSchedulingAlgorithms();
        JsonArray schedAlgsArrayJson = new JsonArray();
        for (SchedulingAlgorithm schedAlg: schedAlgs) {
            JsonObject schedAlgJson = new JsonObject();
            schedAlgJson.addProperty("name", schedAlg.getAlgorithm());
            schedAlgsArrayJson.add(schedAlgJson);
        }
        JsonObject result = new JsonObject();
        result.add("scheduling_algorithms", schedAlgsArrayJson);
        return result.toString();
    }

    @GET
    @Path("/scheduling_algorithms/current")
    @Produces(MediaType.APPLICATION_JSON)
    public String getCurrentSchedulingAlgorithm() {
        SchedulingAlgorithm schedAlg = vmManager.getCurrentSchedulingAlgorithm();
        JsonObject result = new JsonObject();
        result.addProperty("name", schedAlg.getAlgorithm());
        return result.toString();
    }

    @PUT
    @Path("/scheduling_algorithms/current")
    @Consumes("application/json")
    public void setSchedulingAlgorithm(String schedAlgToSet) {
        // Get the algorithm
        JsonObject jsonObject = gson.fromJson(schedAlgToSet, JsonObject.class);
        if (jsonObject.get("algorithm") == null) { // Invalid JSON format. Throw error.
            throw new WebApplicationException(400);
        }
        String algorithm = jsonObject.get("algorithm").getAsString();

        SchedulingAlgorithm schedulingAlg;
        if (algorithm.equals(SchedulingAlgorithm.CONSOLIDATION.getAlgorithm())) {
            schedulingAlg = SchedulingAlgorithm.CONSOLIDATION;
        }
        else if (algorithm.equals(SchedulingAlgorithm.DISTRIBUTION.getAlgorithm())) {
            schedulingAlg = SchedulingAlgorithm.DISTRIBUTION;
        }
        else if (algorithm.equals(SchedulingAlgorithm.GROUP_BY_APP.getAlgorithm())) {
            schedulingAlg = SchedulingAlgorithm.GROUP_BY_APP;
        }
        else if (algorithm.equals(SchedulingAlgorithm.RANDOM.getAlgorithm())) {
            schedulingAlg = SchedulingAlgorithm.RANDOM;
        }
        else { // Invalid algorithm. Throw error.
            throw new WebApplicationException(400);
        }
        vmManager.setSchedulingAlgorithm(schedulingAlg);
    }


    //================================================================================
    // Node Methods
    //================================================================================

    @GET
    @Path("/nodes")
    @Produces(MediaType.APPLICATION_JSON)
    public String getNodes() {
        //TODO
        return null;
    }

    @GET
    @Path("/node/{hostname}")
    @Produces(MediaType.APPLICATION_JSON)
    public String getVMsDeployedInNode(@PathParam("hostname") String hostname) {
        //TODO
        return null;
    }

}
