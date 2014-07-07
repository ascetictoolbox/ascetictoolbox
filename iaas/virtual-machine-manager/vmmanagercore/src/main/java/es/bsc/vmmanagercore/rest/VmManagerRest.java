package es.bsc.vmmanagercore.rest;

import com.google.gson.*;
import es.bsc.vmmanagercore.manager.VmManager;
import es.bsc.vmmanagercore.model.*;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import java.io.BufferedReader;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.List;

/**
 * REST interface for the VM Manager.
 *
 * @author David Ortiz Lopez (david.ortiz@bsc.es)
 *
 */

@Path("/vmmanager")
public class VmManagerRest {

    private static final String DB_NAME = "VmManagerDb";
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
        JsonArray jsonVmsArray = new JsonArray();
        for (VmDeployed vmDeployed: vmManager.getAllVms()) {
            JsonObject vmJson = (JsonObject) parser.parse(gson.toJson(vmDeployed, VmDeployed.class));
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
        if (!VmmRestInputValidator.checkVmDescriptions(vmsJson)) {
            throw new WebApplicationException(400);
        }

        // Get the information of the VMs to deploy
        List<Vm> vmsToDeploy = new ArrayList<>();
        JsonArray vmsArrayJson = vmsJson.getAsJsonArray("vms");
        for (JsonElement vmJson: vmsArrayJson) {
            Vm vm = gson.fromJson(vmJson, Vm.class);
            vmsToDeploy.add(vm);
        }

        // Deploy the VMs
        List<String> idsVmsDeployed = vmManager.deployVms(vmsToDeploy);

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
        if (vmManager.getVm(vmId) == null) { // If the VM does not exist, return 404 error
            throw new WebApplicationException(404);
        }
        return gson.toJson(vmManager.getVm(vmId));
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

        // Perform the action or return a 400 error if the user did not specify a valid action
        if (!VmmRestInputValidator.isValidAction(jsonObject.get("action").getAsString())) {
            throw new WebApplicationException(400);
        }
        vmManager.performActionOnVm(vmId, jsonObject.get("action").getAsString());
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
        for (VmDeployed vmDeployed: vmManager.getVmsOfApp(appId)) {
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
        for (ImageUploaded image: vmManager.getVmImages()) {
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
        if (!VmmRestInputValidator.checkImageExists(imageId, vmManager.getVmImagesIds())) {
            throw new WebApplicationException(404);
        }

        // Return JSON representation of the image
        return gson.toJson(vmManager.getVmImage(imageId));
    }

    @DELETE
    @Path("/images/{id}")
    public void deleteImage(@PathParam("id") String imageId) {
        // Throw error if the image does not exist
        if (!VmmRestInputValidator.checkImageExists(imageId, vmManager.getVmImagesIds())) {
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
        JsonArray schedAlgsArrayJson = new JsonArray();
        for (SchedulingAlgorithm schedAlg: vmManager.getAvailableSchedulingAlgorithms()) {
            JsonObject schedAlgJson = new JsonObject();
            schedAlgJson.addProperty("name", schedAlg.getName());
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
        JsonObject result = new JsonObject();
        result.addProperty("name", vmManager.getCurrentSchedulingAlgorithm().getName());
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
        if (algorithm.equals(SchedulingAlgorithm.CONSOLIDATION.getName())) {
            schedulingAlg = SchedulingAlgorithm.CONSOLIDATION;
        }
        else if (algorithm.equals(SchedulingAlgorithm.DISTRIBUTION.getName())) {
            schedulingAlg = SchedulingAlgorithm.DISTRIBUTION;
        }
        else if (algorithm.equals(SchedulingAlgorithm.ENERGY_AWARE.getName())) {
            schedulingAlg = SchedulingAlgorithm.ENERGY_AWARE;
        }
        else if (algorithm.equals(SchedulingAlgorithm.GROUP_BY_APP.getName())) {
            schedulingAlg = SchedulingAlgorithm.GROUP_BY_APP;
        }
        else if (algorithm.equals(SchedulingAlgorithm.RANDOM.getName())) {
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


    //================================================================================
    // Logs Methods
    //================================================================================

    @GET
    @Path("/logs")
    @Produces(MediaType.TEXT_PLAIN)
    public String getLogs() {
        // Read the logs file and return its content.
        // If for some reason the logs cannot be read, return an empty string
        String logs;
        try {
            BufferedReader br = new BufferedReader(new FileReader("log/vmmanager.log"));
            StringBuilder sb = new StringBuilder();
            String line = br.readLine();
            while (line != null) {
                sb.append(line);
                sb.append(System.lineSeparator());
                line = br.readLine();
            }
            logs = sb.toString();
        } catch (Exception e) {
            return "";
        }
        return logs;
    }


    //================================================================================
    // VM pricing and energy estimates
    //================================================================================

    @GET
    @Path("/estimates")
    @Produces(MediaType.TEXT_PLAIN)
    public String getEstimates(String vms) {
        // Get the JSON object that contains the VMs that need to be deployed
        JsonObject vmsToBeEstimatedJson = gson.fromJson(vms, JsonObject.class);

        // Get the information of the VMs to estimate
        List<VmToBeEstimated> vmsToBeEstimated = new ArrayList<>();
        JsonArray vmsToBeEstimatedArrayJson = vmsToBeEstimatedJson.getAsJsonArray("vms");
        for (JsonElement vmToBeEstimatedJson: vmsToBeEstimatedArrayJson) {
            VmToBeEstimated vmToBeEstimated = gson.fromJson(vmToBeEstimatedJson, VmToBeEstimated.class);
            vmsToBeEstimated.add(vmToBeEstimated);
        }

        // Return the JSON with the estimates
        JsonArray vmEstimatesJsonArray = new JsonArray();
        for (VmEstimate vmEstimate: vmManager.getVmEstimates(vmsToBeEstimated)) {
            JsonElement vmJsonElement = gson.toJsonTree(vmEstimate, VmEstimate.class);
            vmEstimatesJsonArray.add(vmJsonElement);
        }
        JsonObject result = new JsonObject();
        result.add("estimates", vmEstimatesJsonArray);
        return result.toString();
    }

}
