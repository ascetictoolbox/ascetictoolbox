package eu.ascetic.asceticarchitecture.paas.component.energymodeller.service.task;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.sql.Timestamp;
import java.text.DecimalFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.TimerTask;

import org.apache.log4j.Logger;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import eu.ascetic.asceticarchitecture.paas.component.common.dao.impl.DataConsumptionDAOImpl;
import eu.ascetic.asceticarchitecture.paas.component.common.dao.impl.DataEventDAOImpl;
import eu.ascetic.asceticarchitecture.paas.component.common.dao.impl.IaaSDataDAOImpl;
import eu.ascetic.asceticarchitecture.paas.component.common.model.DataConsumption;
import eu.ascetic.asceticarchitecture.paas.component.common.model.DataEvent;
import eu.ascetic.asceticarchitecture.paas.component.common.model.IaaSVMConsumption;
import eu.ascetic.asceticarchitecture.paas.component.common.model.VMConsumptionPerHour;
import eu.ascetic.asceticarchitecture.paas.component.energymodeller.interfaces.DataCollectorTaskInterface;

public class DataCollector extends TimerTask implements DataCollectorTaskInterface {

	private IaaSDataDAOImpl iaasdatadriver;
	private DataConsumptionDAOImpl dataconsumption;
	private DataEventDAOImpl dataevent;

	private String AMPath = "http://localhost:9000/query";
	private URL url;
	
	private static double H_MAX_POWER = 170;
	private static double H_CPU_CORE = 4;
	private static double H_CPU = 4;
	private static final Logger logger = Logger.getLogger(DataCollector.class);
	

	

	
	@Override
	public void handleEventDataInterval(String applicationid,String deploymentid, List<String> vm, String eventid, Timestamp start, Timestamp end) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void handleConsumptionDataInterval(String applicationid, List<String> vm, String deploymentid, Timestamp start, Timestamp end) {
		for (String vmid : vm){
			if (iaasdatadriver==null){
				logger.info("Connection to IaaS DB unavailable");
				return;
			}
			Timestamp ts = dataconsumption.getLastConsumptionForVM(applicationid, vmid);
			if (ts!=null){
				logger.info ("Data available untill "+ts.toString());
				if (end.after(ts)){
					logger.info ("Data available for this request, not loading new data ");
				} else {
					logger.info ("Importing up to the end timestamp ");
					
				}
			}else{
				logger.info ("Data not available for this request, Importing up to the end timestamp ");
				
			}
		}
		

		
	}

	@Override
	public void handleConsumptionData(String applicationid, List<String> vm, String deploymentid) {
		for (String vmid : vm){
			loadVMData(applicationid, deploymentid,vmid,null,null);
		}
		
	}
	@Override
	public void handleConsumptionData(String applicationid, String vm,	String deploymentid) {
		loadVMData(applicationid, deploymentid,vm,null,null);
		
	}

	@Deprecated
	@Override
	public void handleConsumptionData(String applicationid, String deploymentid) {
		logger.info("Connection to IaaS DB for data retrieval");
		// TODO get vm for deployment???? also replicate for each vm
		String vmid="10111";
		
		
		long vmcpu_total=1;
		if (iaasdatadriver==null){
			logger.info("Connection to IaaS DB unavailable");
			return;
		}
		Timestamp ts = dataconsumption.getLastConsumptionForVM(applicationid, vmid);
		if (ts!=null){
			logger.info ("Data already loaded "+ts.toString());
		}else{
			logger.info ("Missing IaaS energy data");
			logger.debug("Retrieving Host for the given VM");
			String hostid = iaasdatadriver.getHostIdForVM(vmid);
			//logger.debug("Retrieving total cpu for the given VM");
			//String CPU_HOST = iaasdatadriver.getHostTotalCpu(hostid);
			//logger.debug("Calculating nominal ratio between VM and its Phys. Host");
			//double ratio = vmcpu_total/(H_CPU_CORE*H_CPU);
			//double max_vm_energy = ratio * H_MAX_POWER;
			logger.info("Retrieving data information from IaaS Layer");
			// TODO only if data has not been already loaded
			List<IaaSVMConsumption> data = iaasdatadriver.getEnergyForVM(hostid, vmid);
			
			
			logger.debug("This VM "+vmid + " has CPU "+vmcpu_total);
			logger.debug("This VM is on host "+hostid + " with CPU "+H_CPU_CORE);
			double load;
			
			for (IaaSVMConsumption element : data){
				load = Double.parseDouble(element.getCpu())/100*Double.parseDouble(element.getEnergy());
				
				logger.debug("Got load "+load + " from process load " +element.getCpu() + " and energy "+element.getEnergy());
				DataConsumption datacons = new DataConsumption();
				datacons.setApplicationid(applicationid);
				datacons.setDeploymentid(deploymentid);
				datacons.setVmenergy(load);
				datacons.setVmid(vmid);
				dataconsumption.save(datacons);
			}
		}
		
		
		
	}


	
	private void loadVMData(String applicationid, String deploymentid,String vmid,Timestamp start, Timestamp end){
		logger.info("Connection to IaaS DB for data retrieval");
		// TODO get vm for deployment???? also replicate for each vm
		
		long vmcpu_total=1;
		if (iaasdatadriver==null){
			logger.info("Connection to IaaS DB unavailable");
			return;
		}
		Timestamp ts = dataconsumption.getLastConsumptionForVM(applicationid, vmid);
		if (ts!=null){
			logger.info ("Data already loaded "+ts.toString());
		}else{
			logger.info ("Missing IaaS energy data");
			logger.debug("Retrieving Host for the given VM");
			String vmiaasid = iaasdatadriver.getVMIdForOSID(vmid);
			String hostid = iaasdatadriver.getHostIdForVM(vmiaasid);
			logger.info("Retrieving data information from IaaS Layer");
//			List<IaaSVMConsumption> data = iaasdatadriver.getEnergyForVM(hostid, vmid);
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
//				datacons.setTime(new Timestamp(Long.parseLong(element.getClock())));
//				datacons.setVmenergy(load);
//				datacons.setHostenergy(Double.parseDouble(element.getEnergy()));
//				datacons.setCpu(Double.parseDouble(element.getCpu()));
//				datacons.setVmid(vmid);
//				dataconumption.save(datacons);
//			}
			double energyvm,powervm;
			List<VMConsumptionPerHour> data = iaasdatadriver.getEnergyForVMHourly(hostid, vmiaasid, null);
			logger.info("Importing data");
			for (VMConsumptionPerHour element : data){
				energyvm = Double.parseDouble(element.getLoad())*Double.parseDouble(element.getEnergy());
				powervm = Double.parseDouble(element.getLoad())*Double.parseDouble(element.getPower());
				logger.info("Got energy  "+energyvm + " and power "+powervm);
				DataConsumption datacons = new DataConsumption();
				datacons.setApplicationid(applicationid);
				datacons.setDeploymentid(deploymentid);
				SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
			    Date parsedDate;
				try {
					DecimalFormat mFormat= new DecimalFormat("00");
					
					parsedDate = dateFormat.parse(element.getYear().toString()+"-"+ mFormat.format(Double.valueOf(element.getMonth().toString()))+"-"+mFormat.format(Double.valueOf(element.getDay().toString()))+ " "+mFormat.format(Double.valueOf(element.getHour().toString()))+":00:00");
					Timestamp timestamp = new java.sql.Timestamp(parsedDate.getTime());
					datacons.setTime(timestamp);
				} catch (ParseException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			    
				datacons.setVmenergy(energyvm);
				datacons.setHostenergy(Double.parseDouble(element.getEnergy()));
				datacons.setCpu(Double.parseDouble(element.getLoad()));
				datacons.setVmid(vmid);
				dataconsumption.save(datacons);
		}
		}
	}
	
	@Override
	public void setup() {
		try {
			 url = new URL(AMPath);
		} catch (MalformedURLException e) {
			 logger.error("Problem with AM url");
		}
	    logger.debug("##Configured data task");
	    
	}

	@Override
	public void handleEventData(String applicationid, String deploymentid,String eventid) {
		logger.info("getting data task");
		// TODO temporarly using a test id
		
		
		
		String requestEntity = "{\"$match\":{\"appId\":\""+applicationid+"\"}}";
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
		    	logger.debug("id" + jo.getAsJsonObject("_id"));
		    	logger.debug("appId" + jo.getAsJsonPrimitive("appId"));
		    	logger.debug("nodeId" + jo.getAsJsonPrimitive("nodeId"));
		    	logger.debug("data" + jo.getAsJsonObject("data"));
		    	logger.debug("timestamp" + jo.getAsJsonPrimitive("timestamp"));
		    	logger.debug("endtime" + jo.getAsJsonPrimitive("endtime"));
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

		
	}
	
	
	public String getAMPath() {
		return AMPath;
	}

	public void setAMPath(String aMPath) {
		AMPath = aMPath;
	}
	public void setIaasdatadriver(IaaSDataDAOImpl iaasdatadriver) {
		this.iaasdatadriver = iaasdatadriver;
	}

	public void setDataconumption(DataConsumptionDAOImpl dataconumption) {
		this.dataconsumption = dataconumption;
	}

	public void setDataevent(DataEventDAOImpl dataevent) {
		this.dataevent = dataevent;
	}
	
	
	@Override
	public void run() {
		
	}	


	
	
}