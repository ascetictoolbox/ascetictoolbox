/**
   Copyright 2014-2015 Hewlett-Packard Development Company, L.P.  
   Licensed under the Apache License, Version 2.0 (the "License");
   you may not use this file except in compliance with the License.
   You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

   Unless required by applicable law or agreed to in writing, software
   distributed under the License is distributed on an "AS IS" BASIS,
   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   See the License for the specific language governing permissions and
   limitations under the License.

 */
package eu.ascetic.asceticarchitecture.paas.component.energymodeller.internal.common.data.queue;

import java.io.IOException;
import java.io.StringWriter;
import java.io.Writer;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import eu.ascetic.asceticarchitecture.paas.component.energymodeller.datatype.messages.GenericEnergyMessage;
/**
 * 
 * @author sommacam
 * utility to parse a class of type GenericEnergyMessage and convert it to a JSON format to be sent to queue as byte payload
 */
public class MessageParserUtility {

	
	private static ObjectMapper mapper = new ObjectMapper(); 
	
	public static String buildStringMessage(GenericEnergyMessage message){
		try {
			Writer strWriter = new StringWriter();
			mapper.writeValue(strWriter, message);
			return strWriter.toString();

		} catch (Exception e) {
			e.printStackTrace();
		}
		
		return null;
	}
	
	
	public static GenericEnergyMessage buildEnergyMessage(String jsonMessage){
		
		GenericEnergyMessage message;
		try {
			message = mapper.readValue(jsonMessage, GenericEnergyMessage.class);
			return message;
		} catch (JsonParseException e) {
			e.printStackTrace();
		} catch (JsonMappingException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return null;
	}
	
	
}
