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

import com.sun.jersey.spi.resource.Singleton;
import es.bsc.vmmanagercore.cloudmiddleware.CloudMiddlewareException;
import es.bsc.vmmanagercore.configuration.VmManagerConfiguration;
import es.bsc.vmmanagercore.db.VmManagerDb;
import es.bsc.vmmanagercore.db.VmManagerDbFactory;
import es.bsc.vmmanagercore.manager.GenericVmManager;
import es.bsc.vmmanagercore.manager.VmManager;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import es.bsc.vmmanagercore.rest.error.ErrorHandler;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

import java.util.List;

/**
 * REST interface for the VM Manager.
 *
 * @author David Ortiz Lopez (david.ortiz@bsc.es)
 */

/*It is important to indicate that it is a singleton. This way, the VMM is created only once instead of being created
at every request. This increases performance greatly.*/
@Singleton
@Path("/vmmanager")
public class VmManagerRest {

    private VmManager vmManager = new GenericVmManager(VmManagerConfiguration.getInstance().dbName);

    private VmCallsManager vmCallsManager = new VmCallsManager(vmManager);
    private ImageCallsManager imageCallsManager = new ImageCallsManager(vmManager);
    private SchedulingAlgorithmCallsManager schedAlgCallsManager = new SchedulingAlgorithmCallsManager(vmManager);
    private NodeCallsManager nodeCallsManager = new NodeCallsManager(vmManager);
    private LogCallsManager logCallsManager = new LogCallsManager();
    private EstimatesCallsManager estimatesCallsManager = new EstimatesCallsManager(vmManager);
    private VmPlacementCallsManager vmPlacementCallsManager = new VmPlacementCallsManager(vmManager);
    private SelfAdaptationCallsManager selfAdaptationCallsManager = new SelfAdaptationCallsManager(vmManager);

    private Logger log = LogManager.getLogger(VmManagerRest.class);

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
        try {
            return vmCallsManager.deployVMs(vmDescriptions);
        } catch (final CloudMiddlewareException e) {
            log.error("Error deploying VMs: " + e.getMessage(), e);
            throw new ErrorHandler(e, Response.Status.INTERNAL_SERVER_ERROR);
        }
    }

    @GET
    @Path("/vms/{id}")
    @Produces(MediaType.APPLICATION_JSON)
    public String getVm(@PathParam("id") String vmId) {
		try {
			return vmCallsManager.getVm(vmId);
		} catch (CloudMiddlewareException e) {
			log.error("error getting vm info: " + e.getMessage(), e);
			throw new ErrorHandler(e, Response.Status.NOT_FOUND);
		}
	}

    @PUT
    @Path("/vms/{id}")
    @Consumes("application/json")
    public void changeStateVm(@PathParam("id") String vmId, String actionJson) {
		try {
			vmCallsManager.changeStateVm(vmId, actionJson);
		} catch (CloudMiddlewareException e) {
			log.error("changeStateVm: " + e.getMessage(), e);
			throw new ErrorHandler(e, Response.Status.NOT_FOUND);
		}
	}

    @DELETE
    @Path("/vms/{id}")
    public void destroyVm(@PathParam("id") String vmId) {
		try {
			vmCallsManager.destroyVm(vmId);
		} catch (CloudMiddlewareException e) {
			log.error("destroyVm: " + e.getMessage(), e);
			throw new ErrorHandler(e, Response.Status.NOT_FOUND);
		}
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
        try {
            return imageCallsManager.uploadImage(imageInfo);
        } catch (CloudMiddlewareException e) {
            throw new ErrorHandler(e, Response.Status.INTERNAL_SERVER_ERROR);
        }
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
		try {
			return vmPlacementCallsManager.getRecommendedPlan(recommendedPlanRequest);
		} catch (CloudMiddlewareException e) {
			log.error("Error getting deployment plan cost: " + e.getMessage(), e);
			throw new ErrorHandler(e, Response.Status.INTERNAL_SERVER_ERROR);
		}
	}

    @PUT
    @Path("/vm_placement/execute_deployment_plan")
    @Consumes("application/json")
    public void executeDeploymentPlan(String deploymentPlan) {
		try {
			vmPlacementCallsManager.executeDeploymentPlan(deploymentPlan);
		} catch (CloudMiddlewareException e) {
			log.error("Error executing deployment plan: " + e.getMessage(), e);
			throw new ErrorHandler(e, Response.Status.INTERNAL_SERVER_ERROR);
		}
	}


    //================================================================================
    //  Self Adaptation Methods
    //================================================================================

    @GET
    @Path("/self_adaptation/options")
    @Produces(MediaType.APPLICATION_JSON)
    public String getSelfAdaptationOptions() {
        return selfAdaptationCallsManager.getSelfAdaptationOptions();
    }

    @PUT
    @Path("/self_adaptation/options")
    @Consumes("application/json")
    public void saveSelfAdaptationOptions(String options) {
        selfAdaptationCallsManager.saveSelfAdaptationOptions(options);
    }

	@PUT
	@Path("/self_adaptation/ondemand")
	public void executeOnDemandSelfAdaptation() {
		try {
			vmManager.executeOnDemandSelfAdaptation();
		}  catch (Exception e) {
			throw new ErrorHandler(e, Response.Status.INTERNAL_SERVER_ERROR);
		}
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

    @PUT
    @Path("/node/{hostname}/powerButton")
    public void pressHostPowerButton(@PathParam("hostname") String hostname) {
        nodeCallsManager.pressHostPowerButton(hostname);
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

	@POST
	@Path("/cost")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	public String getVmCost(List<String> vmIds) {
		try {
			return vmManager.getVmsCost(vmIds);
		} catch(Exception e) {
			log.warn("Error getting vms cost: " + e.getMessage(), e);
			throw new ErrorHandler(e, Response.Status.NOT_FOUND);
		}
	}

    //================================================================================
    // Debug DB
    //================================================================================
    @GET
    @Path("/db")
    @Produces(MediaType.TEXT_PLAIN)
    public String getDbInfo() {
        StringBuilder sb = new StringBuilder("**********\nDB contents\n********\n");
        VmManagerDb db = VmManagerDbFactory.getDb(VmManagerConfiguration.getInstance().dbName);
        List<String> vmIds = db.getAllVmIds();
        if(vmIds != null && vmIds.size() > 0) {
            sb.append("== Virtual Machines ==\n");
            for (String vm : vmIds) {
                sb.append(vm)
                        .append("\n\tappId: ").append(db.getAppIdOfVm(vm))
                        .append("\n\tslaId: ").append(db.getSlaIdOfVm(vm))
                        .append("\n\tovfId: ").append(db.getOvfIdOfVm(vm)).append('\n');
            }
        }
        sb.append("== Self-adaptation options ==\n");
        sb.append("Current").append(db.getCurrentSchedulingAlg().getName());
        sb.append("\nSelf-adaptation options:\n").append(db.getSelfAdaptationOptions());
        return sb.toString();
    }

}
