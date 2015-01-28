package eu.ascetic.paas.applicationmanager.dao.testUtil;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.InetSocketAddress;

import com.sun.net.httpserver.Headers;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;

/**
 * 
 * Copyright 2014 ATOS SPAIN S.A. 
 * 
 * Licensed under the Apache License, Version 2.0 (the License);
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * 
 * @author David Garcia Perez. Atos Research and Innovation, Atos SPAIN SA
 * e-mail david.garciaperez@atos.net
 * 
 * Mock Web Server to help with the creatings of unit tests
 */
public class MockWebServer {
	private static final int BACKLOG = 5;
	
	private int mPort;
	private HttpServer mServer;
	private String mRequestBody;
	private Headers headers;
	
	public MockWebServer() {
		mRequestBody = "";
	}

	public int getPort() {
		return mPort;
	}
	
	public void start() {
		configureServer();
		startServer();
	}

	private void startServer() {
		mServer.start();
		
		/* 
		 * Uncomment following to see webserver logs on system.err
		 * 
		Logger l = Logger.getLogger("com.sun.net.httpserver");
		l.setLevel(Level.FINER);
		StreamHandler sh = new StreamHandler(System.err, 
				new java.util.logging.SimpleFormatter());
		sh.setLevel(Level.FINER);
		
		l.addHandler(sh);
		*/
		
	}

	public void addPath(String path, String response) {
		mServer.createContext(path, createMockHandler(response));
	}
	
	public void addPath(String path, String response, int responseCode) {
		mServer.createContext(path, createMockHandler(response, responseCode));
	}
	
	public void addPath(String path, HttpHandler h) {
		mServer.createContext(path, h);
	}
	
	protected void configureServer() {	
		try {
			//Address of 0 means pick a free one
			InetSocketAddress addr = new InetSocketAddress(0);
			mServer = HttpServer.create(addr, BACKLOG);
			mPort = mServer.getAddress().getPort();
		} catch (IOException e) {
			//Just rethrow as unchecked, a little naughty perhaps
			throw new IllegalStateException(e);
		}
	}
	
	/**
	 * Creates a HttpHandler which calls getMockResponseData for its answer.
	 *
	 * @return never <code>null</code>.
	 */
	public HttpHandler createMockHandler(final String response, final int code) {

		HttpHandler handler = new HttpHandler() {

			public void handle(HttpExchange t) throws IOException {
				   // We retrieve the headers
				   setHeaders(t.getRequestHeaders());
				   
				   // We retrieve the body
		           InputStream is = t.getRequestBody();
				   mRequestBody = inputStreamToString(is, "UTF-8");
		           t.sendResponseHeaders(code, response.length());
		           OutputStream os = t.getResponseBody();
		           os.write(response.getBytes());
		           os.close();
			}
		};

		return handler;
	}



	/**
	 * Creates a HttpHandler which calls getMockResponseData for its answer.
	 *
	 * @return never <code>null</code>.
	 */
	public HttpHandler createMockHandler(final String response) {

		return createMockHandler(response, 200);
	}

	public void stop() {
		//stop dead, don't wait for exchanges
		if (mServer != null) {
			mServer.stop(0);
		}
	}

	public static void main(String[] args) {

		MockWebServer server = new MockWebServer();

		try {
			server.start();
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	
	public String getRequestBody() {
		return mRequestBody;
	}
	

	protected HttpServer getServer() {
		return mServer;
	}
	
	public static String inputStreamToString(InputStream is, String encoding) 
			throws IOException {

		BufferedReader br= new BufferedReader(
				new InputStreamReader(is, encoding));
		String read = null;
		StringBuffer sb = new StringBuffer();
		read = br.readLine();
		while(read != null) {
			sb.append(read);
			read=br.readLine();
		}
		return sb.toString();
	}
	
	public void finalize() {
		//Make absolutely sure this has been cleaned up.
		//Should have happened earlier - this is only a failsafe
		if (mServer != null) {
			mServer.stop(0);
		}
	}

	public Headers getHeaders() {
		return headers;
	}

	public void setHeaders(Headers headers) {
		this.headers = headers;
	}
}