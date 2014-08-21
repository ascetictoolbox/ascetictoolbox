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
import eu.ascetic.asceticarchitecture.paas.component.energymodeller.interfaces.DataCollectorTaskInterface;

public class DataCollector extends TimerTask implements DataCollectorTaskInterface {

	private IaaSDataDAOImpl iaasdatadriver;
	private DataConsumptionDAOImpl dataconumption;
	private DataEventDAOImpl dataevent;

	private String AMPath = "http://localhost:9000/query";
	private URL url;
	private static final Logger logger = Logger.getLogger(DataCollector.class);
	
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
	public void handleConsumptionData(String applicationid, String deploymentid) {
		logger.info("Connection to IaaS DB for data retrieval");
		// TODO get vm for deployment???? also replicate for each vm
		String vmid="10111";
		long vmcpu_total=1;
		if (iaasdatadriver==null){
			logger.info("Connection to IaaS DB unavailable");
			return;
		}
		logger.debug("Retrieving Host for the given VM");
		String hostid = iaasdatadriver.getHostIdForVM(vmid);
		logger.debug("Retrieving total cpu for the given VM");
		String CPU_HOST = iaasdatadriver.getHostTotalCpu(hostid);
		if (CPU_HOST.equals("0"))CPU_HOST="1.0";
		logger.debug("Calculating nominal ratio between VM and its Phys. Host");
		double ratio = vmcpu_total/Double.parseDouble(CPU_HOST);
		logger.info("Retrieving data information from IaaS Layer");
		// TODO only if data has not been already loaded
		List<IaaSVMConsumption> data = iaasdatadriver.getEnergyForVM(hostid, vmid);
		logger.debug("This VM "+vmid + " has CPU "+vmcpu_total);
		logger.debug("This VM is on host "+hostid + " with CPU "+CPU_HOST);
		double load;
		double utilization;
		
		for (IaaSVMConsumption element : data){
			utilization = Double.parseDouble(element.getCpu())/vmcpu_total;
			logger.debug("Connection to IaaS DB ");
			load = (double) ( Double.parseDouble(element.getEnergy())*utilization*ratio);
			logger.debug("Got load "+load + " from ratio " +ratio + " and utilization "+utilization+" energy "+Double.parseDouble(element.getEnergy()));
			DataConsumption datacons = new DataConsumption();
			datacons.setApplicationid(applicationid);
			datacons.setDeploymentid(deploymentid);
			datacons.setVmenergy(load);
			datacons.setVmid(vmid);
			dataconumption.save(datacons);
		}
		
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
	public void run() {
		
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
		this.dataconumption = dataconumption;
	}

	public void setDataevent(DataEventDAOImpl dataevent) {
		this.dataevent = dataevent;
	}

	
	
}