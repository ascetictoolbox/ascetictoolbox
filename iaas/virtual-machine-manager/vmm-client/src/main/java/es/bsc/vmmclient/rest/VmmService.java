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

public interface VmmService {

    @GET("/vms")
    VmsDeployedResponse getVms();

    // TODO - Necesito un objeto que detro tenga una lista de objetos que dentro solo tengan un string...
    @POST("/vms")
    DeployVmsResponse deployVms(@Body VmsList vms);

    @GET("/vms/{id}")
    VmDeployed getVm(@Path("id") String id);

    //@PUT("/vms/{id}")
    //TODO

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

}
