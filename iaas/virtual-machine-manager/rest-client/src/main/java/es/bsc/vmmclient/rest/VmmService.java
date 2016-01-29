/**
 * Copyright (C) 2013-2014  Barcelona Supercomputing Center
 * <p/>
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 * <p/>
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 * <p/>
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301  USA
 */

package es.bsc.vmmclient.rest;

import es.bsc.vmmclient.models.*;
import retrofit.client.Response;
import retrofit.http.*;

import java.util.List;

public interface VmmService {

    /*
    I should get rid of the classes whose only purpose is to wrap other classes.
    For example, VmsDeployedResponse contains just one field with a list of VmDeployed.
    It has been programmed this way, because of the current interface of the VMM. It should probably
    be improved, but that would break compatibility with clients that are sending requests directly to the
    REST service.
     */

    @GET("/vms")
    VmsDeployedResponse getVms();

    @POST("/vms")
    DeployVmsResponse deployVms(@Body VmsList vms);

    @GET("/vms/{id}")
    VmDeployed getVm(@Path("id") String id);

    @PUT("/vms/{id}")
    Response performActionOnVm(@Path("id") String id, @Body VmActionQuery action);

    @DELETE("/vms/{id}")
    Response destroyVm(@Path("id") String id);

    @GET("/vmsapp/{appId}")
    VmsDeployedResponse getAppVms(@Path("appId") String id);

    @DELETE("/vmsapp/{appId}")
    Response destroyAppVms(@Path("appId") String id);

    @GET("/images")
    ImagesUploadedResponse getImages();

    @POST("/images")
    UploadImageResponse uploadImage(@Body ImageToUpload image);

    @GET("/images/{id}")
    ImageUploaded getImage(@Path("id") String id);

    @DELETE("/images/{id}")
    Response destroyImage(@Path("id") String id);

    @GET("/nodes")
    NodesResponse getNodes();

    @POST("/estimates")
    EstimatesResponse getEstimates(@Body VmsToBeEstimatedList vms);

	@POST("/cost")
	List<VmCost> getCosts(@Body List<String> vmIds);

	@PUT("/migrate/{vmId}/{destinationHostName}")
	Response migrate(@Path("vmId") String vmId, @Path("destinationHostName") String destinationHostName);
}
