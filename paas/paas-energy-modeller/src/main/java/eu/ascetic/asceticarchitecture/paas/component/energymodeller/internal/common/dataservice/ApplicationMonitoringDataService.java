/**
* Copyright 2014 Hewlett-Packard Development Company, L.P.                                         
 */
package eu.ascetic.asceticarchitecture.paas.component.energymodeller.internal.common.dataservice;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.sql.Timestamp;
import java.util.List;
import java.util.Vector;

import org.apache.log4j.Logger;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import eu.ascetic.asceticarchitecture.iaas.zabbixApi.client.ZabbixClient;
import eu.ascetic.asceticarchitecture.iaas.zabbixApi.datamodel.HistoryItem;
import eu.ascetic.asceticarchitecture.iaas.zabbixApi.datamodel.Host;
import eu.ascetic.asceticarchitecture.iaas.zabbixApi.datamodel.Item;
import eu.ascetic.asceticarchitecture.paas.component.energymodeller.internal.common.data.database.dao.impl.DataConsumptionDAOImpl;
import eu.ascetic.asceticarchitecture.paas.component.energymodeller.internal.common.data.database.dao.impl.DataEventDAOImpl;
import eu.ascetic.asceticarchitecture.paas.component.energymodeller.internal.common.data.database.table.DataConsumption;
import eu.ascetic.asceticarchitecture.paas.component.energymodeller.internal.common.data.database.table.DataEvent;
import eu.ascetic.asceticarchitecture.paas.component.energymodeller.internal.common.dataservice.legacy.ZabbixDataCollectorService;

public class ApplicationMonitoringDataService  {
	private static int MILLIS_IN_A_DAY = 86400000;
	private static int SECS_IN_A_DAY = 86400;
	private static final Logger logger = Logger.getLogger(ApplicationMonitoringDataService.class);
	private DataEventDAOImpl dataevent;

	private String AMPath = "http://localhost:9000/query";
	private URL url;
	
	public void handleEventData(String applicationid, String deploymentid,List<String> vms, String eventid) {
		for (String vmid : vms){
			handleEventData(applicationid, deploymentid, vmid, eventid);
		}
	}

	public void handleEventData(String applicationid, String deploymentid,String vmid, String eventid) {
		// TODO deployment id shoudl be used and in case to filter only some events. 
			logger.info("Now getting events for app " + applicationid);
			logger.info("Now getting events for dep " + deploymentid);
			logger.info("Now getting events for vm " + vmid);
			String requestEntity;
			requestEntity = "FROM events MATCH appId=\""+applicationid+"\" AND data.eventType=\""+eventid+"\" AND nodeId=\""+vmid+"\"";
			
			requestEntity = "FROM events MATCH appId=\""+applicationid+"\" AND data.eventType=\""+eventid+"\" ";
			if (eventid==null){
				logger.warn("No event id supplied. I will load all events for the application");
				requestEntity = "FROM events MATCH appId=\""+applicationid+"\"";
			}
			// TODO workaround use eventType even if it is risky for id issues
			//requestEntity = "FROM events MATCH data.eventType=\""+eventid+"\" ";
			
			logger.info("This query " + requestEntity);
			HttpURLConnection connection;
			try {
				logger.info("App monitor connection on "+url.getHost() + url.getPort() + url.getPath() + url.getProtocol());
				connection = (HttpURLConnection) url.openConnection();
				connection.setDoOutput(true);
				connection.setDoInput(true);
				connection.setRequestMethod("POST");
				connection.setRequestProperty("Content-Type", "text/plain");
				OutputStream response = connection.getOutputStream();
				response.write(requestEntity.getBytes());
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
				logger.info("received "+jsonres);
				logger.info("getting result");
				connection.disconnect();
				
			    JsonArray entries = (JsonArray) new JsonParser().parse(jsonres);
			    long time;
			    // TODO workaround purgeWorkarounddata
			    //dataevent.purgedata(applicationid,vmid,eventid);
			    dataevent.purgeWorkarounddata(eventid);
			    for (JsonElement el : entries){
			    	JsonObject jo = (JsonObject) el;
			    	logger.info("id" + jo.getAsJsonObject("_id"));
			    	logger.debug("appId" + jo.getAsJsonPrimitive("appId"));
			    	logger.info("nodeId" + jo.getAsJsonPrimitive("nodeId"));
			    	logger.debug("data" + jo.getAsJsonObject("data"));
			    	logger.debug("timestamp" + jo.getAsJsonPrimitive("timestamp"));
			    	logger.debug("endtime" + jo.getAsJsonPrimitive("endtime"));
			    	DataEvent data = new DataEvent();
			    	
			    	boolean valid_data = true;
			    	
			    	data.setApplicationid(applicationid);
			    	data.setDeploymentid(deploymentid);
			    	if (jo.getAsJsonPrimitive("eventType")!=null){
			    		data.setEventid(jo.getAsJsonPrimitive("eventType").getAsString());
			    	} else {
			    		valid_data = false;
			    		logger.warn("Event id is null looking into non standard location within the data");
			    		if (jo.getAsJsonObject("data")!=null){
			    			if (jo.getAsJsonObject("data").getAsJsonPrimitive("eventType")!=null){
			    				logger.warn("Document Event id was in legacy location"+jo.getAsJsonObject("_id"));
			    				valid_data = true;
			    				data.setEventid(jo.getAsJsonObject("data").getAsJsonPrimitive("eventType").getAsString());
			    			}
			    			if (jo.getAsJsonObject("data").getAsJsonPrimitive("eventtype")!=null){
			    				logger.warn("Document Event id was in legacy location"+jo.getAsJsonObject("_id"));
			    				data.setEventid(jo.getAsJsonObject("data").getAsJsonPrimitive("eventtype").getAsString());	
			    				valid_data = true;
			    			}
			    		}
			    		
			    	}
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
			    			logger.info("This event has no nodeid, but has correct data, will be calculated for the whole application");
			    			
			    		}
			    		
			    		
			    	} else {
			    		String eventNodeId = jo.getAsJsonPrimitive("nodeId").getAsString();
			    		logger.info("This event has a nodeid "+eventNodeId+", checking if it match "+vmid);
			    		if (eventNodeId.equals(vmid)){
			    			logger.info("This event is from this vm");
			    			
			    		}else {
			    			logger.info("This event refers to another vm");
			    			valid_data = false;
			    		}
			    		
			    	}
			    	if (valid_data){
			    		if (jo.getAsJsonPrimitive("nodeId")!=null){
			    			data.setVmid(jo.getAsJsonPrimitive("nodeId").getAsString());
			    		} else {
			    			logger.info("This eventi is globally assigned also to this vm");
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
				    			if (valid_data)dataevent.save(data);
				    			if (valid_data)logger.info("saving "+data.getEventid()+data.getApplicationid()+data.getBegintime()+data.getEndtime());
				    		}
			    		}
			    }
			} catch (IOException e) {
				 logger.error("#problem occurred");
				e.printStackTrace();
			}
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

	public void setDataevent(DataEventDAOImpl dataevent) {
		this.dataevent = dataevent;
	}	

	
}