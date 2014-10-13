/**
* Copyright 2014 Hewlett-Packard Development Company, L.P.                                         
 */
package eu.ascetic.asceticarchitecture.paas.component.energymodeller.service.task;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.sql.Timestamp;
import java.util.List;
import java.util.TimerTask;
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
import eu.ascetic.asceticarchitecture.paas.component.common.model.DataConsumption;
import eu.ascetic.asceticarchitecture.paas.component.common.model.DataEvent;
import eu.ascetic.asceticarchitecture.paas.component.energymodeller.interfaces.DataCollectorTaskInterface;

public class DataCollector extends TimerTask implements DataCollectorTaskInterface {

	//private IaaSDataDAOImpl iaasdatadriver;
	private DataConsumptionDAOImpl dataconsumption;
	private DataEventDAOImpl dataevent;

	private String AMPath = "http://localhost:9000/query";
	private URL url;
	

	private static final Logger logger = Logger.getLogger(DataCollector.class);
	
	private ZabbixClient zCli ;
	

	@Override
	public void handleConsumptionDataInterval(String applicationid, List<String> vm, String deploymentid, Timestamp start, Timestamp end) {
		for (String virtmac : vm){
			loadVMData(applicationid, deploymentid,virtmac,start,end);
		}
	}

	@Override
	public void handleConsumptionData(String applicationid, List<String> vm, String deploymentid) {
		for (String vmid : vm){
			loadVMData(applicationid, deploymentid,vmid,null,null);
		}
		
	}
//	@Override
//	public void handleConsumptionData(String applicationid, String vm,	String deploymentid) {
//		loadVMData(applicationid, deploymentid,vm,null,null);
//		
//	}
// OLD VERSION WITH IAAS DB
//	private void loadVMData(String applicationid, String deploymentid,String vmid,Timestamp start, Timestamp end){
//		logger.info("Connection to IaaS DB for data retrieval");
//		
//		if (iaasdatadriver==null){
//			logger.info("Connection to IaaS DB unavailable");
//			return;
//		}
//		Timestamp ts = dataconsumption.getLastConsumptionForVM(applicationid, vmid);
//		if (ts!=null){
//			logger.info ("Data already loaded "+ts.toString());
//			String vmiaasid = iaasdatadriver.getVMIdForOSID(vmid);
//			String hostid = iaasdatadriver.getHostIdForVM(vmiaasid);
//			logger.info("Retrieving only newer data from IaaS Layer");
//			List<VMConsumptionPerHour> data = iaasdatadriver.getEnergyForVMHourlyTime(hostid, vmiaasid, ts);
//			double energyvm,powervm;
//			logger.info("Importing data");
//			for (VMConsumptionPerHour element : data){
//				energyvm = Double.parseDouble(element.getLoad())*(Double.parseDouble(element.getEnergy())/100);
//				powervm = Double.parseDouble(element.getLoad())*(Double.parseDouble(element.getPower())/100);
//				logger.info("Got energy  "+energyvm + " and power "+powervm);
//				DataConsumption datacons = new DataConsumption();
//				datacons.setApplicationid(applicationid);
//				datacons.setDeploymentid(deploymentid);
//				SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
//			    Date parsedDate;
//				try {
//					DecimalFormat mFormat= new DecimalFormat("00");
//					
//					parsedDate = dateFormat.parse(element.getYear().toString()+"-"+ mFormat.format(Double.valueOf(element.getMonth().toString()))+"-"+mFormat.format(Double.valueOf(element.getDay().toString()))+ " "+mFormat.format(Double.valueOf(element.getHour().toString()))+":00:00");
//					Timestamp timestamp = new java.sql.Timestamp(parsedDate.getTime());
//					datacons.setTime(timestamp);
//				} catch (ParseException e) {
//					// TODO Auto-generated catch block
//					e.printStackTrace();
//				}
//			    
//				datacons.setVmenergy(energyvm);
//				datacons.setHostenergy(Double.parseDouble(element.getEnergy())/100);
//				datacons.setCpu(Double.parseDouble(element.getLoad()));
//				datacons.setVmid(vmid);
//				dataconsumption.save(datacons);
//			}
//			
//		}else{
//			logger.info ("Missing IaaS energy data");
//			logger.debug("Retrieving Host for the given VM");
//			String vmiaasid = iaasdatadriver.getVMIdForOSID(vmid);
//			String hostid = iaasdatadriver.getHostIdForVM(vmiaasid);
//			logger.info("Retrieving data information from IaaS Layer");
////			List<IaaSVMConsumption> data = iaasdatadriver.getEnergyForVM(hostid, vmid);
////			logger.debug("This VM "+vmid + " has CPU "+vmcpu_total);
////			logger.debug("This VM is on host "+hostid + " with CPU "+H_CPU_CORE);
////			double load;
////			
////			for (IaaSVMConsumption element : data){
////				load = Double.parseDouble(element.getCpu())/100*Double.parseDouble(element.getEnergy());
////				
////				logger.debug("Got load "+load + " from process load " +element.getCpu() + " and energy "+element.getEnergy());
////				DataConsumption datacons = new DataConsumption();
////				datacons.setApplicationid(applicationid);
////				datacons.setDeploymentid(deploymentid);
////				datacons.setTime(new Timestamp(Long.parseLong(element.getClock())));
////				datacons.setVmenergy(load);
////				datacons.setHostenergy(Double.parseDouble(element.getEnergy()));
////				datacons.setCpu(Double.parseDouble(element.getCpu()));
////				datacons.setVmid(vmid);
////				dataconumption.save(datacons);
////			}
//			double energyvm,powervm;
//			List<VMConsumptionPerHour> data = iaasdatadriver.getEnergyForVMHourly(hostid, vmiaasid, null);
//			logger.info("Importing data");
//			for (VMConsumptionPerHour element : data){
//				energyvm = Double.parseDouble(element.getLoad())*(Double.parseDouble(element.getEnergy())/100);
//				powervm = Double.parseDouble(element.getLoad())*(Double.parseDouble(element.getPower())/100);
//				logger.info("Got energy  "+energyvm + " and power "+powervm);
//				DataConsumption datacons = new DataConsumption();
//				datacons.setApplicationid(applicationid);
//				datacons.setDeploymentid(deploymentid);
//				SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
//			    Date parsedDate;
//				try {
//					DecimalFormat mFormat= new DecimalFormat("00");
//					
//					parsedDate = dateFormat.parse(element.getYear().toString()+"-"+ mFormat.format(Double.valueOf(element.getMonth().toString()))+"-"+mFormat.format(Double.valueOf(element.getDay().toString()))+ " "+mFormat.format(Double.valueOf(element.getHour().toString()))+":00:00");
//					Timestamp timestamp = new java.sql.Timestamp(parsedDate.getTime());
//					datacons.setTime(timestamp);
//				} catch (ParseException e) {
//					// TODO Auto-generated catch block
//					e.printStackTrace();
//				}
//			    
//				datacons.setVmenergy(energyvm);
//				datacons.setHostenergy(Double.parseDouble(element.getEnergy())/100);
//				datacons.setCpu(Double.parseDouble(element.getLoad()));
//				datacons.setVmid(vmid);
//				dataconsumption.save(datacons);
//		}
//		}
//	}


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
			this.getHistoryForItemFrom(applicationid, deploymentid, "Power", searchFullHostsname(vmid), ts.getTime());
			logger.info("Data loaded");
			
		}else{
			logger.info ("Missing energy data");
			logger.debug("Retrieving Data");
			this.getHistoryForItem(applicationid, deploymentid, "Power", searchFullHostsname(vmid), 10);
			logger.info("Imported data");
		}
	}
	
	
	@Override
	public void setup() {
		try {
			 url = new URL(AMPath);
			 zCli = new ZabbixClient();
		} catch (MalformedURLException e) {
			 logger.error("Problem Services initialization");
		}
	    logger.debug("##Configured data task");
	    
	}
	

	

	@Override
	public void handleEventData(String applicationid, String deploymentid,String eventid) {
		logger.info("select * from DATAEVENT where applicationid = ? getting data task");

		
		Timestamp lastts = dataevent.getLastEventForVM(applicationid, null, eventid);
		
		
		
		
		String requestEntity;
		if (lastts!=null){
			logger.info("Data has been loaded in the past, checking for newer events only");
			requestEntity = "{\"$match\":{\"appId\":\""+applicationid+"\",\"timestamp\":{\"$gt\":"+lastts.getTime()+"}}}";
		}else{
			requestEntity = "{\"$match\":{\"appId\":\""+applicationid+"\"}}";
		}
		
		
		
		HttpURLConnection connection;
		try {
			logger.debug("App monitor connection on "+url.getHost() + url.getPort() + url.getPath() + url.getProtocol());
			connection = (HttpURLConnection) url.openConnection();
			connection.setDoOutput(true);
			connection.setDoInput(true);
			connection.setRequestMethod("POST");
			connection.setRequestProperty("Content-Type", "application/json");
			OutputStream response = connection.getOutputStream();
			response.write(requestEntity.getBytes());
			response.flush();
			logger.debug("Connected !");
			if (connection.getResponseCode() != HttpURLConnection.HTTP_CREATED) {
				logger.info("App monitor connection error");
				logger.info("code "+connection.getResponseCode());
			}
			BufferedReader reader = new BufferedReader(new InputStreamReader((connection.getInputStream())));
			String jsonres="";
			String line;
			while ((line = reader.readLine()) != null) {
				jsonres=jsonres+line;
	        }
			// TODO get last value to filter only most recent data
			logger.debug("received "+jsonres);
			logger.debug("getting result");
			connection.disconnect();
		    JsonArray entries = (JsonArray) new JsonParser().parse(jsonres);
		    Timestamp ts;
		    long time;
		    for (JsonElement el : entries){
		    	JsonObject jo = (JsonObject) el;
		    	logger.info("id" + jo.getAsJsonObject("_id"));
		    	logger.info("appId" + jo.getAsJsonPrimitive("appId"));
		    	logger.info("nodeId" + jo.getAsJsonPrimitive("nodeId"));
		    	logger.info("data" + jo.getAsJsonObject("data"));
		    	logger.info("timestamp" + jo.getAsJsonPrimitive("timestamp"));
		    	logger.info("endtime" + jo.getAsJsonPrimitive("endtime"));
		    	DataEvent data = new DataEvent();
		    	data.setApplicationid(applicationid);
		    	data.setEventid(eventid);
		    	data.setVmid(jo.getAsJsonPrimitive("nodeId").getAsString());
		    	time=jo.getAsJsonPrimitive("timestamp").getAsLong();
		    	ts = new Timestamp(time);
		    	data.setBegintime(ts);
		    	time=jo.getAsJsonPrimitive("endtime").getAsLong();
		    	ts = new Timestamp(time);
		    	data.setEndtime(ts);
		    	dataevent.save(data);
		    }
		} catch (IOException e) {
			 logger.error("#problem occurred");
			e.printStackTrace();
		}
		
	}


	@Override
	public void handleEventData(String applicationid, String deploymentid,List<String> vm, String eventid) {
		handleEventData( applicationid,  deploymentid, eventid);
		
	}
	
	
	public String getHostForVM(String vmid){
		return zCli.getHostByName(vmid).getHostid();
		
	}

	public String getHostData(String itemkey,String hostname){
		return zCli.getItemByKeyFromHost(itemkey, hostname).getItemid();
	}

	public void getHistoryForItemSamples(String appid, String depid,String itemkey,String hostname, int samples){
		
		List<HistoryItem> items = zCli.getHistoryDataFromItem(itemkey, hostname, "text", samples);
		storeEnergyFromData(appid,depid,hostname,items);	
	}
	
	public void getHistoryForItem(String appid, String depid,String itemkey,String hostname, int daysbefore){
		
		Item item = zCli.getItemByNameFromHost(itemkey, hostname);
		if (item==null)return;
		long time = item.getLastClock() - (86400*daysbefore*1000);
		logger.info("going back "+time);
		List<HistoryItem> items = zCli.getHistoryDataFromItem(itemkey, hostname, "text", time ,item.getLastClock()*1000);
		storeEnergyFromData(appid,depid,hostname,items);
				
	}
	
	public void getHistoryForItemFrom(String appid, String depid,String itemkey,String hostname,long begin){
		
		Item item = zCli.getItemByNameFromHost(itemkey, hostname);
		if (item==null)return;
		List<HistoryItem> items = zCli.getHistoryDataFromItem(itemkey, hostname, "text", begin,item.getLastClock()*1000);
		storeEnergyFromData(appid,depid,hostname,items);
	}
	
	public void getHistoryForItemInterval(String appid, String depid,String itemkey,String hostname,long since, long to){
		List<HistoryItem> items = zCli.getHistoryDataFromItem(itemkey, hostname, "text", since , to);
		storeEnergyFromData(appid,depid,hostname,items);
	}
	
	private void storeEnergyFromData(String appid, String depid, String vmid, List<HistoryItem> items){
		if (items==null)return;
		List<DataConsumption> result=new Vector<DataConsumption>();
		logger.info("I got total items: "+items.size());
		int count = 0;
		HistoryItem previous=null;
		DataConsumption dc = null;
		for (int i=items.size()-1;i>=0;i--){

			HistoryItem item = items.get(i);
			
			dc = new DataConsumption();
			if (previous!=null){
				double energy = integrate(new Double(previous.getValue()).doubleValue(),new Double(item.getValue()).doubleValue(),previous.getClock(),item.getClock());
				//logger.info("Energy,Power,time: ," + energy + " , "+item.getValue()+ " , "+item.getClock());
				dc.setApplicationid(appid);
				dc.setDeploymentid(depid);
				dc.setVmid(vmid);
				dc.setCpu(0);
				dc.setVmenergy(energy);
				dc.setEventid("na");
				dc.setVmpower(Double.parseDouble(item.getValue()));
				dc.setTime(new Timestamp(item.getClock()*1000));
				result.add(dc);
				count++;
				
			} else {
				double energy = integrate(0,new Double(item.getValue()).doubleValue(),0,0);
				//logger.info("Energy is: " + energy +	"Power is : "+item.getValue()+ "Clock is : "+item.getClock());
				dc.setApplicationid(appid);
				dc.setDeploymentid(depid);
				dc.setVmid(vmid);
				dc.setCpu(0);
				dc.setVmenergy(energy);
				dc.setEventid("na");
				dc.setVmpower(Double.parseDouble(item.getValue()));
				dc.setTime(new Timestamp(item.getClock()*1000));
				result.add(dc);
				count++;
			}
			
			
			previous = item;
		}
		logger.info("Sample built for "+count);
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
//	public void setIaasdatadriver(IaaSDataDAOImpl iaasdatadriver) {
//		this.iaasdatadriver = iaasdatadriver;
//	}

	public void setDataconumption(DataConsumptionDAOImpl dataconumption) {
		this.dataconsumption = dataconumption;
	}

	public void setDataevent(DataEventDAOImpl dataevent) {
		this.dataevent = dataevent;
	}
	
	
	@Override
	public void run() {
		
	}	

//	@Deprecated
//	@Override
//	public void handleConsumptionData(String applicationid, String deploymentid) {
//		logger.info("Connection to IaaS DB for data retrieval");
//		// TODO get vm for deployment???? also replicate for each vm
//		String vmid="10111";
//		
//		
//		long vmcpu_total=1;
//		if (iaasdatadriver==null){
//			logger.info("Connection to IaaS DB unavailable");
//			return;
//		}
//		Timestamp ts = dataconsumption.getLastConsumptionForVM(applicationid, vmid);
//		if (ts!=null){
//			logger.info ("Data already loaded "+ts.toString());
//		}else{
//			logger.info ("Missing IaaS energy data");
//			logger.debug("Retrieving Host for the given VM");
//			String hostid = iaasdatadriver.getHostIdForVM(vmid);
//			//logger.debug("Retrieving total cpu for the given VM");
//			//String CPU_HOST = iaasdatadriver.getHostTotalCpu(hostid);
//			//logger.debug("Calculating nominal ratio between VM and its Phys. Host");
//			//double ratio = vmcpu_total/(H_CPU_CORE*H_CPU);
//			//double max_vm_energy = ratio * H_MAX_POWER;
//			logger.info("Retrieving data information from IaaS Layer");
//			// TODO only if data has not been already loaded
//			List<IaaSVMConsumption> data = iaasdatadriver.getEnergyForVM(hostid, vmid);
//			
//			
//			logger.debug("This VM "+vmid + " has CPU "+vmcpu_total);
//			logger.debug("This VM is on host "+hostid + " with CPU "+H_CPU_CORE);
//			double load;
//			
//			for (IaaSVMConsumption element : data){
//				load = Double.parseDouble(element.getCpu())/100*Double.parseDouble(element.getEnergy());
//				
//				logger.debug("Got load "+load + " from process load " +element.getCpu() + " and energy "+element.getEnergy());
//				DataConsumption datacons = new DataConsumption();
//				datacons.setApplicationid(applicationid);
//				datacons.setDeploymentid(deploymentid);
//				datacons.setVmenergy(load);
//				datacons.setVmid(vmid);
//				dataconsumption.save(datacons);
//			}
//		}
//		
//		
//		
//	}
//	
	
}