package eu.ascetic.saas.experimentmanager.wslayer;

import java.io.InputStream;
import java.util.List;
import java.util.logging.Logger;

import com.jayway.jsonpath.JsonPath;
import com.jayway.jsonpath.ReadContext;

import net.minidev.json.JSONArray;

public class JSONHandler implements Handler {

	public String getSingle(InputStream json, String query) {
		ReadContext ctx = JsonPath.parse(json);
		try{
			Logger.getLogger("JSONHanlder").info("response : " + ctx.jsonString());
			Logger.getLogger("JSONHanlder").info("query    : " + query.toString());
			JSONArray result = ctx.read(query);
			return (String) result.get(0).toString();
		}
		catch(ClassCastException e){
			Logger.getAnonymousLogger().severe("Bad query response, only single text accepted : " + ctx.read(query).toString());
			throw e;
		}
	}

	public List<String> getList(InputStream result, String query) {
		ReadContext ctx = JsonPath.parse(result);
		return ctx.read(query);
	}
	

	public String getAccepted() {
		return "application/json";
	}
	
}
