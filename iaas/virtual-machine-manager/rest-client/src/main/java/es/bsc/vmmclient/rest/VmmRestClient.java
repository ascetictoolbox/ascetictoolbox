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

import com.squareup.okhttp.*;
import retrofit.RestAdapter;
import retrofit.client.OkClient;

import javax.net.ssl.*;
import java.io.IOException;
import java.net.Proxy;
import java.security.cert.CertificateException;
import java.util.concurrent.TimeUnit;

public class VmmRestClient {

    private final VmmService service;
    private static final int DEFAULT_TIMEOUT_SECONDS = 9000; // Our testbed can be SLOW sometimes
	private OkHttpClient okHttpClient;;

	private String username, password;

	public VmmRestClient(String url) {
		this(url,DEFAULT_TIMEOUT_SECONDS, null, null);
	}

	public VmmRestClient(String url, long timeout) {
		this(url, timeout, null, null);
	}
	public VmmRestClient(String url, String username, String password) {
		this(url, DEFAULT_TIMEOUT_SECONDS, username, password);
	}

	public VmmRestClient(String url, long timeout, final String username, final String password) {
            if(url.startsWith("https://")) {
                    // TODO: add the option to accept only Trusted HTTPS connections
                    okHttpClient = getUnsafeOkHttpClient();
            } else {
                    okHttpClient = new OkHttpClient();
            }
            
            // Define our own okHttpClient to increase the timeout
            okHttpClient.setReadTimeout(timeout, TimeUnit.SECONDS);

            if(username != null && password != null) {
                okHttpClient.setAuthenticator(new Authenticator() {
                    @Override
                    public Request authenticate(Proxy proxy, Response response) throws IOException {
                        String credential = Credentials.basic(username, password);
                        return response.request().newBuilder().header("Authorization", credential).build();
                    }

                    @Override
                    public Request authenticateProxy(Proxy proxy, Response response) throws IOException {
                        return null;
                    }
                });
            }

        RestAdapter restAdapter = new RestAdapter.Builder()
            .setEndpoint(url)
            .setClient(new OkClient(okHttpClient))
            .build();
        service = restAdapter.create(VmmService.class);
    }

    public VmmService getVmmService() {
        return service;
    }
    
    private static OkHttpClient getUnsafeOkHttpClient() {
        try {
            // Create a trust manager that does not validate certificate chains
            final TrustManager[] trustAllCerts = new TrustManager[] {
                new X509TrustManager() {
                    @Override
                    public void checkClientTrusted(java.security.cert.X509Certificate[] chain, String authType) throws CertificateException {
                    }

                    @Override
                    public void checkServerTrusted(java.security.cert.X509Certificate[] chain, String authType) throws CertificateException {
                    }

                    @Override
                    public java.security.cert.X509Certificate[] getAcceptedIssuers() {
                            return new java.security.cert.X509Certificate[]{};
                    }
                }
            };
            
            // Install the all-trusting trust manager
            final SSLContext sslContext = SSLContext.getInstance("SSL");
            sslContext.init(null, trustAllCerts, new java.security.SecureRandom());
            // Create an ssl socket factory with our all-trusting manager
            final SSLSocketFactory sslSocketFactory = sslContext.getSocketFactory();
            
            OkHttpClient okHttpClient = new OkHttpClient();
            okHttpClient.setSslSocketFactory(sslSocketFactory);
            okHttpClient.setHostnameVerifier(new HostnameVerifier() {
                @Override
                public boolean verify(String hostname, SSLSession session) {
                    return true;
                }
            });
            return okHttpClient;
        } catch (Exception e) {
                throw new RuntimeException(e);
        }
    }
}
