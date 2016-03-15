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

package es.bsc.demiurge.ws.rest;

import es.bsc.demiurge.core.db.VmManagerDb;
import es.bsc.demiurge.core.cloudmiddleware.CloudMiddlewareException;
import es.bsc.demiurge.core.configuration.Config;
import es.bsc.demiurge.core.db.VmManagerDbFactory;
import es.bsc.demiurge.core.manager.VmManager;
import es.bsc.demiurge.core.models.vms.VmRequirements;

import es.bsc.demiurge.ws.rest.error.ErrorHandler;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.codehaus.jackson.map.ObjectMapper;

import javax.inject.Singleton;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.List;

/**
 * REST interface for the VM Manager.
 *
 * @author Mario Macias (github.com/mariomac), David Ortiz Lopez (david.ortiz@bsc.es)
 */

/*It is important to indicate that it is a singleton. This way, the VMM is created only once instead of being created
at every request. This increases performance greatly.*/
@Singleton
@Path("/v1")
public class DemiurgeRestV1 {

    private VmManager vmManager;

    private VmCallsManager vmCallsManager;
    private ImageCallsManager imageCallsManager;
    private SchedulingAlgorithmCallsManager schedAlgCallsManager;
    private NodeCallsManager nodeCallsManager;
    private LogCallsManager logCallsManager;
    private EstimatesCallsManager estimatesCallsManager;
    private VmPlacementCallsManager vmPlacementCallsManager;
    private SelfAdaptationCallsManager selfAdaptationCallsManager;

    private Logger log = LogManager.getLogger(DemiurgeRestV1.class);

    public DemiurgeRestV1() {
        vmManager = Config.INSTANCE.getVmManager();
        if(vmManager == null) throw new AssertionError("VM Manager must not be null");

        vmCallsManager = new VmCallsManager(vmManager);
        imageCallsManager = new ImageCallsManager(vmManager);
        schedAlgCallsManager = new SchedulingAlgorithmCallsManager(vmManager);
        nodeCallsManager = new NodeCallsManager(vmManager);
        logCallsManager = new LogCallsManager();
        estimatesCallsManager = new EstimatesCallsManager();
        vmPlacementCallsManager = new VmPlacementCallsManager(vmManager);
        selfAdaptationCallsManager = new SelfAdaptationCallsManager(vmManager);
    }

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
	@Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.TEXT_PLAIN)
    public String getEstimates(String vms) {
		try {
        	return estimatesCallsManager.getEstimates(vms);
		} catch(Exception e) {
			throw new ErrorHandler(e, Response.Status.INTERNAL_SERVER_ERROR);
		}
    }

	@POST
	@Path("/cost")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	public String getVmCost(List<String> vmIds) {
		try {
			return vmManager.getVmsEstimates(vmIds);
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
        VmManagerDb db = VmManagerDbFactory.getDb(Config.INSTANCE.dbName);
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
        sb.append("Current").append(db.getCurrentSchedulingAlg());
        sb.append("\nSelf-adaptation options:\n").append(db.getSelfAdaptationOptions());
        return sb.toString();
    }

    @GET
    @Path("/hello")
    @Produces(MediaType.TEXT_PLAIN)
    public String hello() {
        return "hello!\n";
    }


	@PUT
	@Path("/migrate/{vmId}/{hostName}")
	public void migrate(@PathParam("vmId") String vmId, @PathParam("hostName") String hostName) throws CloudMiddlewareException {
		vmManager.migrateVm(vmId,hostName);
	}

	@GET
	@Path("/flavours")
	@Produces(MediaType.APPLICATION_JSON)
	public String getFlavours() {
		ByteArrayOutputStream bos;
		ObjectMapper mapper = new ObjectMapper();
		bos = new ByteArrayOutputStream();
		try {
			mapper.writeValue(bos, vmManager.getFlavours());
			return bos.toString();
		} catch(Exception ex) {
			log.error(ex.getMessage(),ex);
			throw new WebApplicationException(ex.getMessage(), ex.getCause());
		} finally {
			try {
				bos.close();
			} catch (IOException e) {
				log.warn(e.getMessage(),e);
			}
		}
	}

	@POST
	@Path("/vms/{id}/resize")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.TEXT_PLAIN)
	public void resize(@PathParam("id") String vmId, String vm) {
        VmRequirements vmRequirements = vmCallsManager.getVmRequirements(vm);
		vmManager.resize(vmId, vmRequirements);
	}
    
    @POST
	@Path("/vms/{id}/confirmResize")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.TEXT_PLAIN)
	public void confirmResize(@PathParam("id") String vmId) {
		vmManager.confirmResize(vmId);
	}
}
