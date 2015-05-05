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

package es.bsc.vmmclient.rest;

import es.bsc.vmmclient.models.ListVmsDeployed;
import es.bsc.vmmclient.models.Vm;
import es.bsc.vmmclient.models.VmDeployed;
import retrofit.http.DELETE;
import retrofit.http.GET;
import retrofit.http.POST;
import retrofit.http.Path;

import java.util.List;

public interface VmmService {

    @GET("/vms")
    ListVmsDeployed getVms();

    @POST("/vms")
    List<String> deployVms(List<Vm> vms);

    @GET("/vms/{id}")
    VmDeployed getVM(@Path("id") String id);

    // PUT VM actions

    @DELETE("/vms/{id}")
    void destroyVm(@Path("id") String id);

    @GET("/vmsapp/{appId}")
    List<VmDeployed> getAppVms(@Path("appId") String id);

    @DELETE("/vmsapp/{appId}")
    void destroyAppVms(@Path("appId") String id);

}
