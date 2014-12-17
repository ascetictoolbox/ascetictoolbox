/**
* Copyright 2014 Hewlett-Packard Development Company, L.P.                                         
 */
package eu.ascetic.asceticarchitecture.paas.component.energymodeller.service.dconnector;

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
import eu.ascetic.asceticarchitecture.paas.component.common.dao.impl.DataConsumptionDAOImpl;
import eu.ascetic.asceticarchitecture.paas.component.common.dao.impl.DataEventDAOImpl;
import eu.ascetic.asceticarchitecture.paas.component.common.database.table.DataConsumption;
import eu.ascetic.asceticarchitecture.paas.component.common.database.table.DataEvent;

public class DataCollector  {

	private DataConsumptionDAOImpl dataconsumption;
	private DataEventDAOImpl dataevent;

	private String AMPath = "http://localhost:9000/query";
	private URL url;
	

	private static final Logger logger = Logger.getLogger(DataCollector.class);
	
	private ZabbixClient zCli ;
	

	
	public void handleConsumptionDataInterval(String applicationid, List<String> vm, String deploymentid, Timestamp start, Timestamp end) {
		for (String virtmac : vm){
			loadVMData(applicationid, deploymentid,virtmac,start,end);
		}
	}

	
	public void handleConsumptionData(String applicationid, List<String> vm, String deploymentid) {
		for (String vmid : vm){
			loadVMData(applicationid, deploymentid,vmid,null,null);
		}
		
	}


	private void loadVMData(String applicationid, String deploymentid,String vmid,Timestamp start, Timestamp end){
		logger.info("Connection to Zabbix for data retrieval");
		
		if (zCli==null){
			logger.info("Connection zabbix unavailable");
			return;
		}
		Timestamp ts = dataconsumption.getLastConsumptionForVM(applicationid, vmid);
		if (ts!=null){
			logger.info ("Data from this vm already loaded untill "+ts.toString());
			logger.info("Retrieving only newer data from Zabbix");
			// retrieving only most recent data
			long datalong = ts.getTime()+1;
			this.getHistoryForItemFrom(applicationid, deploymentid, "Power", searchFullHostsname(vmid),datalong );
			logger.info("Data loaded from "+datalong);
			
		}else{
			logger.info ("Missing energy data");
			logger.debug("Retrieving Data");
			this.getHistoryForItem(applicationid, deploymentid, "Power", searchFullHostsname(vmid), 10);
			logger.info("Imported all data");
		}
	}
	
	
	
	public void setup() {
		try {
			 url = new URL(AMPath);
			 zCli = new ZabbixClient();
		} catch (MalformedURLException e) {
			 logger.error("Problem Services initialization");
		}
	    logger.debug("##Configured data task");
	    
	}
	
	public void handleEventData(String applicationid, String deploymentid,String vmid,String eventid) {
		//logger.info("select * from DATAEVENT where applicationid = ? getting data task");
//		Timestamp lastts=null;
//		lastts = dataevent.getLastByApplicationId(applicationid);	
		
		
		
		
		String requestEntity;
//		if (lastts!=null){
//			logger.info("Data has been loaded in the past, checking for newer events only");
//			requestEntity = "{\"$match\":{\"appId\":\""+applicationid+"\",\"timestamp\":{\"$gt\":"+lastts.getTime()+"}}}";
//		}else{
			//requestEntity = "{\"$match\":{\"appId\":\""+applicationid+"\"}}";
			
//		}
		
		requestEntity = "FROM events MATCH appId=\""+applicationid+"\" AND eventType=\""+eventid+"\" AND nodeId=\""+vmid+"\"";
		
		if (eventid==null){
			logger.warn("No event id supplied. I will load all events for the application");
			requestEntity = "FROM events MATCH appId=\""+applicationid+"\"";
			
		}
		
		
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
			logger.debug("received "+jsonres);
			logger.debug("getting result");
			connection.disconnect();
			
		    JsonArray entries = (JsonArray) new JsonParser().parse(jsonres);
		    long time;
		    dataevent.purgedata(applicationid,vmid,eventid);
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
		    	if (jo.getAsJsonPrimitive("nodeId")==null) 	{
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
		    	
		    	if (valid_data){
		    		data.setVmid(jo.getAsJsonPrimitive("nodeId").getAsString());
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


	public void handleEventData(String applicationid, String deploymentid,List<String> vm, String eventid) {
		if (vm!=null)for (String vmid : vm)handleEventData( applicationid,  deploymentid, vmid , eventid);
		if (vm==null)handleEventData( applicationid,  deploymentid, "" , eventid);
		
	}
	
	
	public String getHostForVM(String vmid){
		return zCli.getHostByName(vmid).getHostid();
		
	}

	public String getHostData(String itemkey,String hostname){
		return zCli.getItemByKeyFromHost(itemkey, hostname).getItemid();
	}

	public void getHistoryForItemSamples(String appid, String depid,String itemkey,String hostname,int samples){
		
		List<HistoryItem> items = zCli.getHistoryDataFromItem(itemkey, hostname, "text", samples);
		storeEnergyFromData(appid,depid,hostname, items);	
	}
	
	public void getHistoryForItem(String appid, String depid,String itemkey,String hostname, int daysbefore){
		
		Item item = zCli.getItemByNameFromHost(itemkey, hostname);
		if (item==null)return;
		logger.info(""+item.getLastClock());
		long time = item.getLastClock() - (86400*2);
		logger.info("going back "+time);
		if (item.getLastClock()==0){
			logger.warn("no data available");
			return;
		}
		List<HistoryItem> items = zCli.getHistoryDataFromItem(itemkey, hostname, "text", time * 1000 ,item.getLastClock()*1000);
		storeEnergyFromData(appid,depid,hostname,items);
				
	}
	
	public void getHistoryForItemFrom(String appid, String depid,String itemkey,String hostname, long begin){
		
		Item item = zCli.getItemByNameFromHost(itemkey, hostname);
		if (item==null)return;
		if (item.getLastClock()==0){
			logger.warn("no data available");
			return;
		}else {
			logger.info("From (need to add 000) "+item.getLastClock()+" begin "+begin);
		}
		if (begin>(item.getLastClock()*1000)){
			logger.info("No need to load data");
			return;
		}
		long delta = (item.getLastClock()*1000) - begin;
		if (delta > 86400000){
			// more that 2 days time range, need to import gradually all data
			long finalts = 0;
			finalts = begin;
			// load 2 days
			while (delta>86400000){
				
				finalts = finalts + 86400000;
				this.getHistoryForItemInterval(appid, depid, itemkey, hostname, "", begin, finalts);
				Item reloadeditem = zCli.getItemByNameFromHost(itemkey, hostname);
				reloadeditem.getLastClock();
				delta = (item.getLastClock()*1000) - finalts;
				begin=finalts;
			}
			
			this.getHistoryForItemInterval(appid, depid, itemkey, hostname, "", finalts, item.getLastClock()*1000);
			
		} else {
			List<HistoryItem> items = zCli.getHistoryDataFromItem(itemkey, hostname, "text", begin,item.getLastClock()*1000);
			if (items.size()==0){
				logger.warn("no data available");
				return;
			}
			
			storeEnergyFromData(appid,depid,hostname,items);
		}
		
	}
	
	
	
	public void getHistoryForItemInterval(String appid, String depid,String itemkey,String hostname,String eventid, long since, long to){
		List<HistoryItem> items = zCli.getHistoryDataFromItem(itemkey, hostname, "text", since , to);
		
		if (items.size()==0){
			logger.warn("no data available");
			return;
		}
		logger.info("Size of sample "+items.size());
		for (HistoryItem item : items){
			logger.info("This Item "+item.getClock()+" host "+item.getHostid()+ " item val "+item.getValue());
		}
		
		storeEnergyFromData(appid,depid,hostname,items);
	}
	
	
	public List<HistoryItem> getSeriesHistoryForItemInterval(String appid, String depid,String itemkey,String hostname,long since, long to){
		List<HistoryItem> items = zCli.getHistoryDataFromItem(itemkey, hostname, "text", since , to);
		return items;
	}
	
	public List<HistoryItem> splitSeriesHistoryForItemInterval(String appid, String depid,String itemkey,String hostname,long since, long to){
		
		List<HistoryItem> items = zCli.getHistoryDataFromItem(itemkey, hostname, "text", since , to);
		return items;
	}
	
	
	private void storeEnergyFromData(String appid, String depid, String vmid, List<HistoryItem> items){
		if (items==null)return;
		String eventid="vm";
		List<DataConsumption> result=new Vector<DataConsumption>();
		logger.info("I got total items: "+items.size());
		HistoryItem previous=null;
		DataConsumption dc = null;
		for (int i=items.size()-1;i>=0;i--){
			
			HistoryItem item = items.get(i);
			if (i==items.size()-1){
				logger.info("START TS AT : "+item.getClock());
			}
			if (i==0){
				logger.info("END TS AT : "+item.getClock());
			}
			dc = new DataConsumption();
			if (previous!=null){
				
				double energy = integrate(new Double(previous.getValue()).doubleValue(),new Double(item.getValue()).doubleValue(),previous.getClock(),item.getClock());
				if (energy > 10) logger.info(" HighEnergy is: " + energy +	"Power is : "+item.getValue()+ "Clock is : "+item.getClock());

				dc.setApplicationid(appid);
				dc.setDeploymentid(depid);
				dc.setVmid(vmid);
				dc.setCpu(0);
				dc.setVmenergy(energy);
				if ((item.getClock()-previous.getClock())>1800){
					logger.warn("Machine has been shutted down");
					dc.setVmenergy(0);
				}
				
				
				dc.setEventid(eventid);
				dc.setVmpower(Double.parseDouble(item.getValue()));
				dc.setTime((item.getClock()*1000));
				result.add(dc);
				
			} else {
				double energy = integrate(0,new Double(item.getValue()).doubleValue(),0,0);
				if (energy > 10) logger.info(" HighEnergy is: " + energy +	"Power is : "+item.getValue()+ "Clock is : "+item.getClock());
				dc.setApplicationid(appid);
				dc.setDeploymentid(depid);
				dc.setVmid(vmid);
				dc.setCpu(0);
				dc.setVmenergy(energy);
				dc.setEventid(eventid);
				dc.setVmpower(Double.parseDouble(item.getValue()));
				dc.setTime((item.getClock()*1000));
				result.add(dc);
			}
			
			
			previous = item;
		}
		
		logger.info("Sample built for "+items.size());
		dataconsumption.insertBatch(result);
		logger.info("Inserted");

	}
	
	
	private double integrate(double powera,double powerb, long timea,long timeb){
		return 	Math.abs((timeb-timea)*(powera+powerb)*0.5)/3600;
	}
	
	public List<String> getHosts(){
		List<String> hosts = new Vector<String>() ;
		List<Host> result = zCli.getAllHosts();
		
		for(Host host : result){
			hosts.add(host.getHostid());
		}
		return hosts;		
	}
	public List<String> getHostsnames(){
		List<String> hosts = new Vector<String>() ;
		List<Host> result = zCli.getAllHosts();
		
		for(Host host : result){
			hosts.add(host.getHost());
		}
		return hosts;		
	}
	
	public String searchFullHostsname(String hosttbs){
		List<String> hosts = getHostsnames() ;
		for(String host : hosts){
			if(host.contains(hosttbs))return host;
		}
		logger.info("Not found this host");
		return null;		
	}
	
	
	public List<String> getHostsItems(String hostname){
		List<String> items = new Vector<String>() ;
		List<Item> result = zCli.getItemsFromHost(hostname);
		
		for(Item item : result){
			logger.info("I got key "+item.getKey());
			logger.info("I got id "+item.getItemid());
			items.add(item.getItemid());
		}
		return items;		
	}	
	
	
	public String getAMPath() {
		return AMPath;
	}

	public void setAMPath(String aMPath) {
		AMPath = aMPath;
	}

	public void setDataconumption(DataConsumptionDAOImpl dataconumption) {
		this.dataconsumption = dataconumption;
	}

	public void setDataevent(DataEventDAOImpl dataevent) {
		this.dataevent = dataevent;
	}
	

	
}