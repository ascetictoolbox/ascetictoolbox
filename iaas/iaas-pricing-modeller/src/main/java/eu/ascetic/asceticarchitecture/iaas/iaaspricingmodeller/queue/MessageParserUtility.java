package eu.ascetic.asceticarchitecture.iaas.iaaspricingmodeller.queue;
import java.io.StringWriter;
import java.io.Writer;
/*
import org.codehaus.jackson.JsonParseException;
import org.codehaus.jackson.map.JsonMappingException;
import org.codehaus.jackson.map.ObjectMapper;



public class MessageParserUtility {

	
	private static ObjectMapper mapper = new ObjectMapper(); 
	
	public static String buildStringMessage(GenericPricingMessage message){
		try {
			
			
			Writer strWriter = new StringWriter();
			mapper.writeValue(strWriter, message);
			return strWriter.toString();

		} catch (Exception e) {
			e.printStackTrace();
		}
		
		return null;
	}
	
	
	public static GenericPricingMessage buildPricingMessage(String jsonMessage){
		
		GenericPricingMessage message;
		try {
			message = mapper.readValue(jsonMessage, GenericPricingMessage.class);
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
	
	
}*/
