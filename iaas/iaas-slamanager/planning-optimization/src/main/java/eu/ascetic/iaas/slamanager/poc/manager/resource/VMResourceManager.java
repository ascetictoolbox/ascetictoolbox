/**
 *  Copyright 2014 Hewlett-Packard Development Company, L.P.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package eu.ascetic.iaas.slamanager.poc.manager.resource;

import java.io.File;
import java.io.FileInputStream;
import java.net.Socket;
import java.security.KeyStore;
import java.security.Principal;
import java.security.PrivateKey;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.KeyManager;
import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.TrustManager;
import javax.net.ssl.TrustManagerFactory;
import javax.net.ssl.X509KeyManager;
import javax.net.ssl.X509TrustManager;
import javax.ws.rs.core.MediaType;

import org.apache.log4j.Logger;
import org.codehaus.jettison.json.JSONObject;

import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.WebResource;
import com.sun.jersey.api.client.config.ClientConfig;
import com.sun.jersey.api.client.config.DefaultClientConfig;
import com.sun.jersey.client.urlconnection.HTTPSProperties;

public class VMResourceManager implements ResourceManager {

	private static final Logger logger = Logger.getLogger(VMResourceManager.class.getName());

	private static VMResourceManager resourceManager = null;
	private Client client;
	private String basePath;
	private WebResource resourceRootWeb;

	public static VMResourceManager getInstance() {
		if (resourceManager == null) {
			resourceManager = new VMResourceManager();
		}
		return resourceManager;
	}

	public void configManager(boolean secureConnection, String trustStore, String trustStorePwd) {
		ClientConfig cc = new DefaultClientConfig();
		if (secureConnection) {
			try {
				// setting client truststore
				TrustManager trustmanagers[] = null;
				KeyManager keymanagers[] = null;

				if (trustStorePwd != null && !trustStorePwd.isEmpty()) {
					trustmanagers = new TrustManager[] { new MyX509TrustManager(trustStore, trustStorePwd.toCharArray()) };
					keymanagers = new KeyManager[] { new MyX509KeyManager(trustStore, trustStorePwd.toCharArray()) };
				} else {
					trustmanagers = new TrustManager[] { new MyX509TrustManager(trustStore, null) };
					keymanagers = new KeyManager[] { new MyX509KeyManager(trustStore, null) };
				}
				// trust all hostname NOT SAFE!!!
				HostnameVerifier allHostsValid = new HostnameVerifier() {
					@Override
					public boolean verify(String urlHostName, SSLSession session) {
						return true;
					}
				};
				SSLContext ctx = null;
				// setting SSL context and client
				ctx = SSLContext.getInstance("SSL");
				// init contex with custom truststore
				ctx.init(keymanagers, trustmanagers, null);
				cc.getProperties().put(HTTPSProperties.PROPERTY_HTTPS_PROPERTIES, new HTTPSProperties(allHostsValid, ctx));
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		client = Client.create(cc);
	}

	public void setBasePath(String urlBasePath) {
		basePath = urlBasePath;
		initialServiceInitializer();
	}

	private void initialServiceInitializer() {
		resourceRootWeb = client.resource(basePath);
		logger.info("Initialized client connection");
	}

	@Override
	public ClientResponse estimates(JSONObject request) {
		ClientResponse response = null;
		logger.debug("Create Estimates Request: ");
		logger.debug("Request to VM Manager: " + request.toString());
		response = resourceRootWeb.path("estimates").type(MediaType.APPLICATION_JSON_TYPE).post(ClientResponse.class, request.toString());
		return response;
	}

	@Override
	public JSONObject verifyCommitResources(JSONObject request) {
		// TODO Auto-generated method stub
		return null;
	}

	class MyX509TrustManager implements X509TrustManager {

		/*
		 * The default PKIX X509TrustManager9. We'll delegate decisions to it,
		 * and fall back to the logic in this class if the default
		 * X509TrustManager doesn't trust it.
		 */
		X509TrustManager pkixTrustManager;

		MyX509TrustManager(String keyStore, char[] password) throws Exception {
			this(new File(keyStore), password);
		}

		MyX509TrustManager(File keyStore, char[] password) throws Exception {
			// create a "default" JSSE X509TrustManager.

			KeyStore ks = KeyStore.getInstance("PKCS12");
			ks.load(new FileInputStream(keyStore), password);

			TrustManagerFactory tmf = TrustManagerFactory.getInstance("SunX509", "SunJSSE");
			tmf.init(ks);

			TrustManager tms[] = tmf.getTrustManagers();

			/*
			 * Iterate over the returned trustmanagers, look for an instance of
			 * X509TrustManager. If found, use that as our "default" trust
			 * manager.
			 */
			for (int i = 0; i < tms.length; i++) {
				if (tms[i] instanceof X509TrustManager) {
					pkixTrustManager = (X509TrustManager) tms[i];
					return;
				}
			}

			/*
			 * Find some other way to initialize, or else we have to fail the
			 * constructor.
			 */
			throw new Exception("Couldn't initialize");
		}

		/*
		 * Delegate to the default trust manager.
		 */
		public void checkClientTrusted(X509Certificate[] chain, String authType) throws CertificateException {
			try {
				pkixTrustManager.checkClientTrusted(chain, authType);
			} catch (CertificateException excep) {
				// do any special handling here, or rethrow exception.
			}
		}

		/*
		 * Delegate to the default trust manager.
		 */
		public void checkServerTrusted(X509Certificate[] chain, String authType) throws CertificateException {
			try {
				pkixTrustManager.checkServerTrusted(chain, authType);
			} catch (CertificateException excep) {
				/*
				 * Possibly pop up a dialog box asking whether to trust the cert
				 * chain.
				 */
			}
		}

		/*
		 * Merely pass this through.
		 */
		public X509Certificate[] getAcceptedIssuers() {
			return pkixTrustManager.getAcceptedIssuers();
		}

	}

	static class MyX509KeyManager implements X509KeyManager {

		/*
		 * The default PKIX X509KeyManager. We'll delegate decisions to it, and
		 * fall back to the logic in this class if the default X509KeyManager
		 * doesn't trust it.
		 */
		X509KeyManager pkixKeyManager;

		MyX509KeyManager(String keyStore, char[] password) throws Exception {
			this(new File(keyStore), password);
		}

		MyX509KeyManager(File keyStore, char[] password) throws Exception {
			// create a "default" JSSE X509KeyManager.

			KeyStore ks = KeyStore.getInstance("PKCS12");
			ks.load(new FileInputStream(keyStore), password);

			KeyManagerFactory kmf = KeyManagerFactory.getInstance("SunX509", "SunJSSE");
			kmf.init(ks, password);

			KeyManager kms[] = kmf.getKeyManagers();

			/*
			 * Iterate over the returned keymanagers, look for an instance of
			 * X509KeyManager. If found, use that as our "default" key manager.
			 */
			for (int i = 0; i < kms.length; i++) {
				if (kms[i] instanceof X509KeyManager) {
					pkixKeyManager = (X509KeyManager) kms[i];
					return;
				}
			}
			/*
			 * Find some other way to initialize, or else we have to fail the
			 * constructor.
			 */
			throw new Exception("Couldn't initialize KeyManager");
		}

		public PrivateKey getPrivateKey(String arg0) {
			return pkixKeyManager.getPrivateKey(arg0);
		}

		public X509Certificate[] getCertificateChain(String arg0) {
			return pkixKeyManager.getCertificateChain(arg0);
		}

		public String[] getClientAliases(String arg0, Principal[] arg1) {
			return pkixKeyManager.getClientAliases(arg0, arg1);
		}

		public String chooseClientAlias(String[] arg0, Principal[] arg1, Socket arg2) {
			return pkixKeyManager.chooseClientAlias(arg0, arg1, arg2);
		}

		public String[] getServerAliases(String arg0, Principal[] arg1) {
			return pkixKeyManager.getServerAliases(arg0, arg1);
		}

		public String chooseServerAlias(String arg0, Principal[] arg1, Socket arg2) {
			return pkixKeyManager.chooseServerAlias(arg0, arg1, arg2);
		}
	}

}
