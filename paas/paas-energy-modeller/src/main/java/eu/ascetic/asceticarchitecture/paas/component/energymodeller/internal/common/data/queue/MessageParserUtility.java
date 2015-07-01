package eu.ascetic.asceticarchitecture.paas.component.energymodeller.internal.common.data.queue;

import java.io.IOException;
import java.io.StringWriter;
import java.io.Writer;

import org.codehaus.jackson.JsonParseException;
import org.codehaus.jackson.map.JsonMappingException;
import org.codehaus.jackson.map.ObjectMapper;

import eu.ascetic.asceticarchitecture.paas.component.energymodeller.datatype.messages.GenericEnergyMessage;

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
