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

package es.bsc.vmmanagercore.rest;

import es.bsc.vmmanagercore.manager.VmManager;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;

/**
 * REST interface for the VM Manager.
 *
 * @author David Ortiz Lopez (david.ortiz@bsc.es)
 *
 */

@Path("/vmmanager")
public class VmManagerRest {

    private static final String DB_NAME = "VmManagerDb";
    private VmManager vmManager = new VmManager(DB_NAME);

    private VmCallsManager vmCallsManager = new VmCallsManager(vmManager);
    private ImageCallsManager imageCallsManager = new ImageCallsManager(vmManager);
    private SchedulingAlgorithmCallsManager schedAlgCallsManager = new SchedulingAlgorithmCallsManager(vmManager);
    private NodeCallsManager nodeCallsManager = new NodeCallsManager(vmManager);
    private LogCallsManager logCallsManager = new LogCallsManager();
    private EstimatesCallsManager estimatesCallsManager = new EstimatesCallsManager(vmManager);
    private VmPlacementCallsManager vmPlacementCallsManager = new VmPlacementCallsManager(vmManager);


    //================================================================================
    // VM Methods
    //================================================================================

    @GET
    @Path("/vms")
    @Produces(MediaType.APPLICATION_JSON)
    public String getAllVms() {
        return vmCallsManager.getAllVms();
    }

    @POST
    @Path("/vms")
    @Consumes("application/json")
    @Produces(MediaType.APPLICATION_JSON)
    public String deployVMs(String vmDescriptions) {
        return vmCallsManager.deployVMs(vmDescriptions);
    }

    @GET
    @Path("/vms/{id}")
    @Produces(MediaType.APPLICATION_JSON)
    public String getVm(@PathParam("id") String vmId) {
        return vmCallsManager.getVm(vmId);
    }

    @PUT
    @Path("/vms/{id}")
    @Consumes("application/json")
    public void changeStateVm(@PathParam("id") String vmId, String actionJson) {
        vmCallsManager.changeStateVm(vmId, actionJson);
    }

    @DELETE
    @Path("/vms/{id}")
    public void destroyVm(@PathParam("id") String vmId) {
        vmCallsManager.destroyVm(vmId);
    }

    @GET
    @Path("/vmsapp/{appId}")
    @Produces(MediaType.APPLICATION_JSON)
    public String getVmsOfApp(@PathParam("appId") String appId) {
        return vmCallsManager.getVmsOfApp(appId);
    }

    @DELETE
    @Path("/vmsapp/{appId}")
    public void deleteVmsOfApp(@PathParam("appId") String appId) {
        vmCallsManager.deleteVmsOfApp(appId);
    }


    //================================================================================
    // VM Images Methods
    //================================================================================

    @GET
    @Path("/images")
    @Produces(MediaType.APPLICATION_JSON)
    public String getAllImages() {
        return imageCallsManager.getAllImages();
    }

    @POST
    @Path("/images")
    @Consumes("application/json")
    @Produces(MediaType.APPLICATION_JSON)
    public String uploadImage(String imageInfo) {
        return imageCallsManager.uploadImage(imageInfo);
    }

    @GET
    @Path("/images/{id}")
    @Produces(MediaType.APPLICATION_JSON)
    public String getImage(@PathParam("id") String imageId) {
        return imageCallsManager.getImage(imageId);
    }

    @DELETE
    @Path("/images/{id}")
    public void deleteImage(@PathParam("id") String imageId) {
        imageCallsManager.deleteImage(imageId);
    }


    //================================================================================
    // Scheduling algorithm Methods
    //================================================================================

    @GET
    @Path("/scheduling_algorithms")
    @Produces(MediaType.APPLICATION_JSON)
    public String getSchedulingAlgorithms() {
        return schedAlgCallsManager.getSchedulingAlgorithms();
    }

    @GET
    @Path("/scheduling_algorithms/current")
    @Produces(MediaType.APPLICATION_JSON)
    public String getCurrentSchedulingAlgorithm() {
        return schedAlgCallsManager.getCurrentSchedulingAlgorithm();
    }

    @PUT
    @Path("/scheduling_algorithms/current")
    @Consumes("application/json")
    public void setSchedulingAlgorithm(String schedAlgToSet) {
        schedAlgCallsManager.setSchedulingAlgorithm(schedAlgToSet);
    }


    //================================================================================
    // VM placement Methods
    //================================================================================

    @GET
    @Path("/vm_placement/construction_heuristics")
    @Produces(MediaType.APPLICATION_JSON)
    public String getConstructionHeuristics() {
        return vmPlacementCallsManager.getConstructionHeuristics();
    }

    @GET
    @Path("/vm_placement/local_search_algorithms")
    @Produces(MediaType.APPLICATION_JSON)
    public String getLocalSearchAlgorithms() {
        return vmPlacementCallsManager.getLocalSearchAlgorithms();
    }

    @PUT
    @Path("/vm_placement/recommended_plan")
    @Consumes("application/json")
    @Produces(MediaType.APPLICATION_JSON)
    public String getRecommendedPlan(String recommendedPlanRequest) {
        return vmPlacementCallsManager.getRecommendedPlan(recommendedPlanRequest);
    }


    //================================================================================
    // Node Methods
    //================================================================================

    @GET
    @Path("/nodes")
    @Produces(MediaType.APPLICATION_JSON)
    public String getNodes() {
        return nodeCallsManager.getNodes();
    }

    @GET
    @Path("/node/{hostname}")
    @Produces(MediaType.APPLICATION_JSON)
    public String getVMsDeployedInNode(@PathParam("hostname") String hostname) {
        return nodeCallsManager.getVMsDeployedInNode(hostname);
    }


    //================================================================================
    // Logs Methods
    //================================================================================

    @GET
    @Path("/logs")
    @Produces(MediaType.TEXT_PLAIN)
    public String getLogs() {
        return logCallsManager.getLogs();
    }


    //================================================================================
    // VM pricing and energy estimates
    //================================================================================

    @POST
    @Path("/estimates")
    @Produces(MediaType.TEXT_PLAIN)
    public String getEstimates(String vms) {
        return estimatesCallsManager.getEstimates(vms);
    }

}
