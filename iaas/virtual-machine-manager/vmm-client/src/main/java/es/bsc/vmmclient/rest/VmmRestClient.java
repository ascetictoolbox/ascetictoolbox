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

import es.bsc.vmmclient.config.Configuration;
import retrofit.RestAdapter;

public class VmmRestClient {

    private VmmRestClient() {
        // Suppress default constructor for non-instantiability
        throw new AssertionError();
    }

    private static RestAdapter restAdapter = new RestAdapter.Builder()
            .setEndpoint(Configuration.getInstance().restUrl)
            .build();

    private static VmmService service = restAdapter.create(VmmService.class);

    public static VmmService getVmmService() {
        return service;
    }

}
