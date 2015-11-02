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

import com.squareup.okhttp.OkHttpClient;
import retrofit.RestAdapter;
import retrofit.client.OkClient;

import java.util.concurrent.TimeUnit;

public class VmmRestClient {

    private final VmmService service;
    private static final int DEFAULT_TIMEOUT_SECONDS = 9000; // Our testbed can be SLOW sometimes
	private OkHttpClient okHttpClient = new OkHttpClient();

	public VmmRestClient(String url) {
		this(url,DEFAULT_TIMEOUT_SECONDS);
	}
	public VmmRestClient(String url, long timeout) {
        // Define our own okHttpClient to increase the timeout
        okHttpClient.setReadTimeout(timeout, TimeUnit.SECONDS);

        RestAdapter restAdapter = new RestAdapter.Builder()
                .setEndpoint(url)
                .setClient(new OkClient(okHttpClient))
                .build();
        service = restAdapter.create(VmmService.class);
    }

    public VmmService getVmmService() {
        return service;
    }

}
