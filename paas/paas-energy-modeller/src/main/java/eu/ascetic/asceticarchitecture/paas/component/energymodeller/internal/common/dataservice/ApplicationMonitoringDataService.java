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
package eu.ascetic.asceticarchitecture.paas.component.energymodeller.internal.common.dataservice;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;
import java.util.Vector;

import org.apache.ibatis.session.SqlSession;
import org.apache.log4j.Logger;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import eu.ascetic.asceticarchitecture.paas.component.energymodeller.internal.common.data.database.table.DataEvent;
import eu.ascetic.asceticarchitecture.paas.component.energymodeller.internal.common.data.ibatis.ApplicationRegistry;
import eu.ascetic.asceticarchitecture.paas.component.energymodeller.internal.common.data.ibatis.mapper.AppRegistryMapper;
/* M. Fontanella - 04/10/2016 - BEGIN */
import eu.ascetic.asceticarchitecture.paas.component.energymodeller.internal.common.dataservice.EnergyDataAggregatorServiceQueue;
/* M. Fontanella - 04/10/2016 - END */
 

public class ApplicationMonitoringDataService  {
	private static int MILLIS_IN_A_DAY = 86400000;
	private static int SECS_IN_A_DAY = 86400;
	private static final Logger logger = Logger.getLogger(ApplicationMonitoringDataService.class);
	//private DataEventDAOImpl dataevent;
	private EventDataService eventDataManager;
	
	/* M. Fontanella - 04/10/2016 - BEGIN */
	private EnergyDataAggregatorServiceQueue energyServiceForIaasId;
	/* M. Fontanella - 04/10/2016 - END */
	
	
	private String AMPath = "http://localhost:9000/query";
	private URL url;
	
	private JsonArray checkEvents(String request){
		logger.info("This query " + request);
		HttpURLConnection connection;
		try {
			connection = (HttpURLConnection) url.openConnection();
			connection.setDoOutput(true);
			connection.setDoInput(true);
			connection.setRequestMethod("POST");
			connection.setRequestProperty("Content-Type", "text/plain");
			OutputStream response = connection.getOutputStream();
			response.write(request.getBytes());
			response.flush();
			logger.debug("Connected !");
			if (connection.getResponseCode() >= 400) {
				logger.info("App monitor connection error");
				logger.info("code "+connection.getResponseCode());
			}
			BufferedReader reader = new BufferedReader(new InputStreamReader((connection.getInputStream())));
			String jsonres="";
			String line;
			while ((line = reader.readLine()) != null) {
				jsonres=jsonres+line;
	        }
			logger.info("getting result");
			connection.disconnect();
			JsonArray entries = (JsonArray) new JsonParser().parse(jsonres);
			return entries;
		} catch (IOException e) {
			 logger.error("#problem occurred");
			e.printStackTrace();
			return null;
		}
		
	}
	
	/**
	 * 
	 * retrieves values from the application manager about an event by using a rest interface, if not event are supplied it retrieves all events of the same application
	 * it is possible that events have missing vmid in that case they are assinged to the current vm as they are global events assigned to all vms belonging to the same application
	 */
	public List<DataEvent> generateEventData(String providerid, String applicationid, String deploymentid,String vmid, String eventid) {

		// TODO deployment id shoudl be used and in case to filter only some events. 
			logger.info("Now getting events for prov " + providerid);
			logger.info("Now getting events for app " + applicationid);
			logger.info("Now getting events for dep " + deploymentid);
			logger.info("Now getting events for vm " + vmid);
			/* M. Fontanella - 04/10/2016 - BEGIN */
			// String iaasid = energyServiceForIaasId.translatePaaSFromIaasID(providerid, deploymentid, vmid);
			/* M. Fontanella - 04/10/2016 - END */			 
			
			String requestEntity;
	    	// Assumption: an application can not run simultaneously on different provider id
			// otherwise this change will be required: 
			// requestEntity = "FROM events MATCH provId=\""+providerid+"\"AND appId=\""+applicationid+"\"AND nodeId=\""+vmid+"\"";

			// TODO this is the original query replaced 
			//requestEntity = "FROM events MATCH appId=\""+applicationid+"\" AND data.eventType=\""+eventid+"\" AND nodeId=\""+vmid+"\"";
			requestEntity = "FROM events MATCH appId=\""+applicationid+"\" AND nodeId=\""+vmid+"\"";

			if (eventid==null){
				logger.warn("No event id supplied. I will load all events for the application");
		    	// Assumption: an application can not run simultaneously on different provider id
				// otherwise this change will be required:  
				//requestEntity = "FROM events MATCH provId=\""+providerid+"\"AND appId=\""+applicationid+"\"";
				requestEntity = "FROM events MATCH appId=\""+applicationid+"\"";
			}

			
			logger.info("This query " + requestEntity);

			    long time;
			    JsonArray entries = checkEvents(requestEntity);
			    if ((entries==null)){
			    }else if (entries.size()==0) {
			    	logger.info("specifi events for only this VM not found, looking global events");
			    	// Assumption: an application can not run simultaneously on different provider id
					// otherwise this change will be required: 
			    	// requestEntity = "FROM events MATCH provId=\""+providerid+"\"AND appId=\""+applicationid+"\" AND data.eventType=\""+eventid+"\" ";
			    	requestEntity = "FROM events MATCH appId=\""+applicationid+"\" AND data.eventType=\""+eventid+"\" ";
			    	// requestEntity = "FROM events MATCH appId=\""+applicationid+"\""; //MAXIM
			    	entries = checkEvents(requestEntity);
			    	
			    }
			    
			    List<DataEvent> resultSet = new Vector<DataEvent>();
			    for (JsonElement el : entries){			    	
			    	JsonObject jo = (JsonObject) el;
			    	logger.debug("id" + jo.getAsJsonObject("_id"));
			    	// Assumption: an application can not run simultaneously on different provider id
					// otherwise this change will be required:
			    	// logger.debug("provId" + jo.getAsJsonPrimitive("provId"));
			    	logger.debug("appId" + jo.getAsJsonPrimitive("appId"));
			    	logger.debug("nodeId" + jo.getAsJsonPrimitive("nodeId"));
			    	logger.debug("data" + jo.getAsJsonObject("data"));
			    	logger.debug("timestamp" + jo.getAsJsonPrimitive("timestamp"));
			    	logger.debug("endtime" + jo.getAsJsonPrimitive("endtime"));
			    	
			    	DataEvent data = new DataEvent();
			    	
			    	boolean valid_data = true;
			    	
			    	data.setProviderid(providerid);
			    	data.setApplicationid(applicationid);
			    	data.setDeploymentid(deploymentid);
			    	if (jo.getAsJsonPrimitive("eventType")!=null){
			    		data.setEventid(jo.getAsJsonPrimitive("eventType").getAsString());
			    	} else {
			    		valid_data = false;
			    		logger.debug("Event id is null looking into non standard location within the data");
			    		if (jo.getAsJsonObject("data")!=null){
			    			if (jo.getAsJsonObject("data").getAsJsonPrimitive("eventType")!=null){
			    				logger.debug("Document Event id was in legacy location"+jo.getAsJsonObject("_id"));
			    				valid_data = true;
			    				data.setEventid(jo.getAsJsonObject("data").getAsJsonPrimitive("eventType").getAsString());
			    			}
			    			if (jo.getAsJsonObject("data").getAsJsonPrimitive("eventtype")!=null){
			    				logger.debug("Document Event id was in legacy location"+jo.getAsJsonObject("_id"));
			    				data.setEventid(jo.getAsJsonObject("data").getAsJsonPrimitive("eventtype").getAsString());	
			    				valid_data = true;
			    			}
			    		}
			    	}

			    	double weight = 1.0;
			    	if (jo.getAsJsonPrimitive("eventWeight")!=null){
			    		weight=jo.getAsJsonPrimitive("eventWeight").getAsDouble();			    		
			    	} else {
			    		if (jo.getAsJsonObject("data")!=null){
			    			if (jo.getAsJsonObject("data").getAsJsonPrimitive("eventWeight")==null){
			    				logger.warn("event weight in legacy location is null"+jo.getAsJsonObject("_id"));
			    			} else {
			    				weight=jo.getAsJsonObject("data").getAsJsonPrimitive("eventWeight").getAsDouble();	
			    			}
			    		} else {
			    			logger.warn("event weight (and legacy location) is null"+jo.getAsJsonObject("_id"));
			    		}
			    	}
			    	data.setWeight(weight);
			    	
			    	// Assumption: an application can not run simultaneously on different provider id
					// otherwise this change will be required:
			    	// if (jo.getAsJsonPrimitive("provId")==null) 	{
			    	// 	valid_data = false;
			    	//	logger.warn("prov id is null"+jo.getAsJsonObject("_id"));
			    	// }
			    	
			    	if (jo.getAsJsonPrimitive("appId")==null) 	{
			    		valid_data = false;
			    		logger.warn("app id is null"+jo.getAsJsonObject("_id"));
			    	}

			    	if (jo.getAsJsonPrimitive("timestamp")==null) 	{
			    		valid_data = false;
			    		logger.warn("begin ts null"+jo.getAsJsonObject("_id"));
			    	}
			    	if (jo.getAsJsonPrimitive("endtime")==null) 	{
			    		valid_data = false;
			    		logger.warn("end ts null"+jo.getAsJsonObject("_id"));
			    	}
			    	if (jo.getAsJsonPrimitive("nodeId")==null) 	{
			    		logger.warn("node id is null"+jo.getAsJsonObject("_id"));
			    		if (valid_data){
			    			logger.debug("This event has no nodeid, but has correct data, will be calculated for the whole application");
			    		}
			    	} else {
			    		String eventNodeId = jo.getAsJsonPrimitive("nodeId").getAsString();
			    		logger.debug("This event has a nodeid "+eventNodeId+", checking if it match "+vmid);
			    		if (eventNodeId.equals(vmid)){
			    			logger.info("This event is from this vm");
			    			
			    		}else {
			    			logger.debug("This event refers to another vm");
			    			valid_data = false;
			    		}
			    	}
			    				    	
			    	if (valid_data){
			    		if (jo.getAsJsonPrimitive("nodeId")!=null){
			    			data.setVmid(jo.getAsJsonPrimitive("nodeId").getAsString());
			    		} else {
			    			logger.debug("This event is globally assigned also to this vm");
			    			data.setData("GLOBAL");
			    			data.setVmid(vmid);
			    		}
			    		time=jo.getAsJsonPrimitive("timestamp").getAsLong();
			    		data.setBegintime(time);
			    		time=jo.getAsJsonPrimitive("endtime").getAsLong();
				    	data.setEndtime(time);
			    		if (jo.getAsJsonPrimitive("endtime").getAsLong()<=0){
			    			valid_data = false;
			    			logger.warn("final timestamp issues skipping this event"+jo.getAsJsonObject("_id"));
			    		} else if (data.getBegintime()>data.getEndtime()) {
			    			valid_data = false;
			    			logger.warn("timestamps issues skipping this event"+jo.getAsJsonObject("_id"));
			    		} else {
			    			   // TODO in future could be worth checking if the event is already in the databse
				    			if (valid_data)resultSet.add(data);
								if (valid_data)logger.info("saving event="+data.getEventid()+", providerid="+data.getProviderid()+", applicationid="+data.getApplicationid()+", nodeid="+data.getVmid()+", begin="+data.getBegintime()+", end="+data.getEndtime()+", weight="+data.getWeight());
				    		}
			    		}
			    }
			    logger.info("Built a result set of "+resultSet.size());
			    return resultSet;

	}
	
	public void setup() {
		try {
			 url = new URL(AMPath);
		} catch (MalformedURLException e) {
			 logger.error("Problem Services initialization");
		}
	    logger.debug("##Configured data task");
	    
	}
	
	public String getAMPath() {
		return AMPath;
	}

	public void setAMPath(String aMPath) {
		AMPath = aMPath;
	}
	
}