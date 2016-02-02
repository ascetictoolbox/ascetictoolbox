package eu.ascetic.paas.applicationmanager.spreader.model;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

/**
 * 
 * Copyright 2016 ATOS SPAIN S.A. 
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
 * Test the conversion from and to JSON the monitoring messages from the IaaS layer
 *
 *
 */
public class ConverterTest {
	private String json = "{\n" 
			+ "   \"name\" : \"name\",\n" 
			+ "   \"value\" : 0.1,\n"
			+ "   \"units\" : \"units\",\n"
			+ "   \"timestamp\" : 22\n"
		+ "}";
	
	@Test
	public void iaaSMessageFromJSONtoObjectTest() {
		IaaSMessage im = Converter.iaasMessageFromJSONToObject(json);
		
		assertEquals("name", im.getName());
		assertEquals("units", im.getUnits());
		assertEquals(22l, im.getTimestamp());
		assertEquals(0.1, im.getValue(), 0.00001);
		
		im = Converter.iaasMessageFromJSONToObject("adadafa");
		assertEquals(im, null);
	}
	
	@Test
	public void iaasMessageToJSONTest() {
		IaaSMessage im = new IaaSMessage();
		im.setName("name");
		im.setTimestamp(22l);
		im.setUnits("units");
		im.setValue(0.1);
		
		String out = Converter.iaasMessageToJSON(im);
		
		assertEquals(json, out);
	}
}
