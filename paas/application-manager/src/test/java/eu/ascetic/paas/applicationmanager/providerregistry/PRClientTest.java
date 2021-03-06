package eu.ascetic.paas.applicationmanager.providerregistry;

import static org.junit.Assert.assertEquals;

import org.junit.Before;
import org.junit.Test;

import eu.ascetic.paas.applicationmanager.conf.Configuration;
import eu.ascetic.paas.applicationmanager.dao.testUtil.MockWebServer;

/**
 * 
 * Copyright 2015 ATOS SPAIN S.A. 
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
 * Unit test to verify the correct work of the Provider Registry Client.
 */
public class PRClientTest {
	private MockWebServer mServer;
	private String mBaseURL = "http://localhost:";
	
	@Before
	public void before() {
		mServer = new MockWebServer();
		mServer.start();
		mBaseURL = mBaseURL + mServer.getPort();
	}
	
	@Test
	public void getProvidersTest() {
		Configuration.providerRegistryEndpoint = mBaseURL;
		
		String collection = "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>" +
							"<collection xmlns=\"http://provider-registry.ascetic.eu/doc/schemas/xml\" href=\"/\">" +
								"<items offset=\"0\" total=\"1\">" +
									"<provider href=\"/1\">" +
										"<id>1</id>" +
										"<name>default</name>" +
										"<vmm-url>http://iaas-vm-dev:34372/vmmanager</vmm-url>" +
										"<slam-url>http://10.0.9.149:8080/services/asceticNegotiation?wsdl</slam-url>" +
										"<amqp-url>amqp://guest:guest@iaas-vm-dev:5673</amqp-url>" +
										"<link rel=\"parent\" href=\"/\" type=\"application/xml\"/>" +
										"<link rel=\"self\" href=\"/1\" type=\"application/xml\"/>" +
									"</provider>" +
								"</items>" +
								"<link rel=\"self\" href=\"/\" type=\"application/xml\"/>" +
							"</collection>";
		
		mServer.addPath("/", collection);
		
		PRClient client = new PRClient();
		assertEquals(1, client.getProviders().size());
		assertEquals("default", client.getProviders().get(0).getName());
	}
	
	@Test
	public void getProvidersNoItemsTest() {
		Configuration.providerRegistryEndpoint = mBaseURL;
		
		String collection = "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>" +
							"<collection xmlns=\"http://provider-registry.ascetic.eu/doc/schemas/xml\" href=\"/\">" +
								"<link rel=\"self\" href=\"/\" type=\"application/xml\"/>" +
							"</collection>";
		
		mServer.addPath("/", collection);
		
		PRClient client = new PRClient();
		assertEquals(0, client.getProviders().size());
	}
	
	@Test
	public void getProvidersParsingErrorTest() {
		Configuration.providerRegistryEndpoint = mBaseURL;
		
		String collection = "";
		
		mServer.addPath("/", collection);
		
		PRClient client = new PRClient();
		assertEquals(0, client.getProviders().size());
	}
	
	@Test
	public void getProvidersWrongResponseCodeTest() {
		Configuration.providerRegistryEndpoint = mBaseURL;
		
		String collection =  "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>" +
				"<collection xmlns=\"http://provider-registry.ascetic.eu/doc/schemas/xml\" href=\"/\">" +
				"<items offset=\"0\" total=\"1\">" +
					"<provider href=\"/1\">" +
						"<id>1</id>" +
						"<name>default</name>" +
						"<vmm-url>http://iaas-vm-dev:34372/vmmanager</vmm-url>" +
						"<slam-url>http://10.0.9.149:8080/services/asceticNegotiation?wsdl</slam-url>" +
						"<amqp-url>amqp://guest:guest@iaas-vm-dev:5673</amqp-url>" +
						"<link rel=\"parent\" href=\"/\" type=\"application/xml\"/>" +
						"<link rel=\"self\" href=\"/1\" type=\"application/xml\"/>" +
					"</provider>" +
				"</items>" +
				"<link rel=\"self\" href=\"/\" type=\"application/xml\"/>" +
			"</collection>";
		
		mServer.addPath("/", collection, 500);
		
		PRClient client = new PRClient();
		assertEquals(0, client.getProviders().size());
	}
	
	@Test
	public void getVmmClientTest() {

		 
		Configuration.providerRegistryEndpoint = mBaseURL;
		
		String collection = "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>" +
							"<collection xmlns=\"http://provider-registry.ascetic.eu/doc/schemas/xml\" href=\"/\">" +
								"<items offset=\"0\" total=\"2\">" +
									"<provider href=\"/1\">" +
										"<id>1</id>" +
										"<name>default</name>" +
										"<vmm-url>http://iaas-vm-dev:34372/vmmanager</vmm-url>" +
										"<slam-url>http://10.0.9.149:8080/services/asceticNegotiation?wsdl</slam-url>" +
										"<amqp-url>amqp://guest:guest@iaas-vm-dev:5673</amqp-url>" +
										"<link rel=\"parent\" href=\"/\" type=\"application/xml\"/>" +
										"<link rel=\"self\" href=\"/1\" type=\"application/xml\"/>" +
									"</provider>" +
									"<provider href=\"/2\">" +
										"<id>2</id>" +
										"<name>default</name>" +
										"<vmm-url>http://iaas-vm-dev2:34372/vmmanager</vmm-url>" +
										"<slam-url>http://10.0.9.149:8080/services/asceticNegotiation?wsdl</slam-url>" +
										"<amqp-url>amqp://guest:guest@iaas-vm-dev:5673</amqp-url>" +
										"<link rel=\"parent\" href=\"/\" type=\"application/xml\"/>" +
										"<link rel=\"self\" href=\"/1\" type=\"application/xml\"/>" +
									"</provider>" +
								"</items>" +
								"<link rel=\"self\" href=\"/\" type=\"application/xml\"/>" +
							"</collection>";
		
		mServer.addPath("/", collection);
		
		PRClient prClient = new PRClient();
		
		// We test first that if the ID is -1 it gets the first provider...
		assertEquals("http://iaas-vm-dev:34372/vmmanager", prClient.getVMMClient(-1).getURL());
		
		//We test that we are able to get spcific details for an specific provider:
		assertEquals("http://iaas-vm-dev:34372/vmmanager", prClient.getVMMClient(1).getURL());
		assertEquals("http://iaas-vm-dev2:34372/vmmanager", prClient.getVMMClient(2).getURL());
		
		//We test that we obtain null VMMClient if we don't find the provider...
		assertEquals(null, prClient.getVMMClient(3));
	}
}
