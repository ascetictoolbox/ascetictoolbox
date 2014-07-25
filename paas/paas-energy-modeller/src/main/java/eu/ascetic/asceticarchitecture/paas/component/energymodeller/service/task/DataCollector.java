package eu.ascetic.asceticarchitecture.paas.component.energymodeller.service.task;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;
import java.util.TimerTask;

import org.apache.log4j.Logger;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import eu.ascetic.asceticarchitecture.paas.component.common.dao.impl.IaaSDataDAOImpl;
import eu.ascetic.asceticarchitecture.paas.component.common.model.IaaSVMConsumption;
import eu.ascetic.asceticarchitecture.paas.component.energymodeller.interfaces.DataCollectorTaskInterface;

public class DataCollector extends TimerTask implements DataCollectorTaskInterface {

	private IaaSDataDAOImpl iaasdatadriver;


	private String AMPath = "http://localhost:9000/event";
	private URL url;
	private static final Logger logger = Logger.getLogger(DataCollector.class);
	
	@Override
	public void setup() {
		try {
			 url = new URL(AMPath);
		} catch (MalformedURLException e) {
			
			 logger.error("Problem with AM url");
		}
	    logger.info("##Configured data task");
	    
	}
	
	@Override
	public void handleConsumptionData(String applicationid, String deploymentid) {
		logger.info("Connection to IaaS DB for data retrieval");
		// TODO get vm for deployment???? also replicate for each wm
		String vmid="";
		long vmcpu_total=10;
		
		if (iaasdatadriver==null){
			logger.info("Connection to IaaS DB unavailable");
			return;
		}
		
		String hostid = iaasdatadriver.getHostIdForVM(vmid);
		String CPU_HOST = iaasdatadriver.getHostTotalCpu(hostid);
		double ratio = vmcpu_total/Double.parseDouble(CPU_HOST);
		
		List<IaaSVMConsumption> data = iaasdatadriver.getEnergyForVM(hostid, vmid);
		logger.info("vm id "+vmid + " has cpu "+vmcpu_total);
		logger.info("vm is on host "+hostid + " with cpu "+CPU_HOST);
		long load;
		double utilization;
		for (IaaSVMConsumption element : data){
			utilization = Long.parseLong(element.getCpu())/vmcpu_total;
			logger.info("Connection to IaaS DB ");
			load = (long) ( Long.parseLong(element.getEnergy())*utilization*ratio);
			logger.info("Got load "+load + " from ratio" +ratio + " and utilization "+utilization);
		}
		
	}

	@Override
	public void handleEventData(String applicationid, String deploymentid,String eventid) {
		logger.info("getting data task");
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
			}
	 
			BufferedReader reader = new BufferedReader(new InputStreamReader((connection.getInputStream())));
			String jsonres="";
			String line;
			while ((line = reader.readLine()) != null) {
				jsonres=jsonres+line;
	        }
			
			System.out.println("Received "+jsonres);
			logger.debug("getting result");
			connection.disconnect();
		
		    JsonArray entries = (JsonArray) new JsonParser().parse(jsonres);
		    
		    for (JsonElement el : entries){
		    	JsonObject jo = (JsonObject) el;
		    	logger.debug("id" + jo.getAsJsonObject("_id"));
		    	logger.debug("appId" + jo.getAsJsonPrimitive("appId"));
		    	logger.debug("nodeId" + jo.getAsJsonPrimitive("nodeId"));
		    	logger.debug("data" + jo.getAsJsonObject("data"));
		    	logger.debug("timestamp" + jo.getAsJsonPrimitive("timestamp"));
		    	logger.debug("endtime" + jo.getAsJsonPrimitive("endtime"));
		    }
		} catch (IOException e) {
			// TODO Auto-generated catch block
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

	
}