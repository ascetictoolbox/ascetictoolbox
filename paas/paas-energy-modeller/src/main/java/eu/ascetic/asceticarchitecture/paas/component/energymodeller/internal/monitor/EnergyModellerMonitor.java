package eu.ascetic.asceticarchitecture.paas.component.energymodeller.internal.monitor;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;
import java.util.Vector;

import org.apache.log4j.Logger;

import com.google.gson.FieldNamingPolicy;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;

import eu.ascetic.asceticarchitecture.paas.component.energymodeller.internal.common.data.database.PaaSEMDatabaseManager;
import eu.ascetic.asceticarchitecture.paas.component.energymodeller.internal.common.data.database.dao.impl.EnergyModellerMonitoringDAOImpl;
import eu.ascetic.asceticarchitecture.paas.component.energymodeller.internal.common.data.database.table.EnergyModellerMonitoring;
import eu.ascetic.asceticarchitecture.paas.component.energymodeller.internal.common.data.ibatis.ApplicationRegistry;
import eu.ascetic.asceticarchitecture.paas.component.energymodeller.internal.common.data.ibatis.mapper.AppRegistryMapper;
import eu.ascetic.asceticarchitecture.paas.component.energymodeller.internal.common.dataservice.EnergyDataAggregatorServiceQueue;
import eu.ascetic.asceticarchitecture.paas.component.energymodeller.internal.common.dataservice.EventDataAggregatorService;
import eu.ascetic.asceticarchitecture.paas.component.energymodeller.internal.queue.client.AmqpClient;

public class EnergyModellerMonitor implements Runnable {

	private static final Logger logger = Logger.getLogger(EnergyModellerMonitor.class);
	
	private EnergyDataAggregatorServiceQueue energyService;
	private ApplicationRegistry appRegistry;
	private EnergyModellerMonitoringDAOImpl dbmanager;
//	private AmqpClient destinationQueueclient;
	
	// SLA MESSAGES
	private static String VM_CONSUMPTION_POWER="power_usage_per_vm";
	private static String APP_CONSUMPTION_POWER="power_usage_per_app";
	private static String VM_CONSUMPTION_ENERGY="energy_usage_per_vm";
	private static String APP_CONSUMPTION_ENERGY="energy_usage_per_app";
	
	private String AMPath = "http://localhost:9000/event";
	private URL url;
	
	public void setup(String path){
		try {
			url = new URL(AMPath);
		} catch (MalformedURLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}
	
	
	public EnergyModellerMonitoringDAOImpl getDbmanager() {
		return dbmanager;
	}


	public void setDbmanager(PaaSEMDatabaseManager dbmanager) {
		this.dbmanager = dbmanager.getMonitoringData();
	}


	public void setEnergyService(EnergyDataAggregatorServiceQueue energyService){
		this.energyService=energyService;
	}
	
	public ApplicationRegistry getAppRegistry() {
		return appRegistry;
	}

	public void setAppRegistry(ApplicationRegistry appRegistry) {
		this.appRegistry = appRegistry;
	}

//	public AmqpClient getDestinationQueueclient() {
//		return destinationQueueclient;
//	}
//
//	public void setDestinationQueueclient(AmqpClient destinationQueueclient) {
//		this.destinationQueueclient = destinationQueueclient;
//	}


	public EnergyDataAggregatorServiceQueue getEnergyService() {
		return energyService;
	}

	@Override
	public void run() {
		AppRegistryMapper registryMapper = appRegistry.getSession().getMapper(AppRegistryMapper.class);
		logger.info("monitoring data ..");
		List<EnergyModellerMonitoring> monitoredInstances = dbmanager.getMonitoringActive();
		logger.info("monitoring data for "+monitoredInstances.size());
		for(EnergyModellerMonitoring monitoring : monitoredInstances){
			String deployment = monitoring.getDeploymentid();
			logger.info("monitoring deployment "+deployment);
			double total_deployment_energy=0;
			double deployment_power=0;
			double deployment_machines=0;
			List<String> vmsactive = registryMapper.selectVMActiveperDeployment(deployment);
			List<String> vmsterminated = registryMapper.selectVMTerminatedperDeployment(deployment);
			logger.info("monitoring deployment size"+ (vmsactive.size()+ vmsterminated.size()));
			String appid = registryMapper.selectAppByDeploy(deployment);
			logger.info("monitoring deployment "+deployment + " with active VMs "+vmsactive.size()+" on app "+appid);
			logger.info("monitoring deployment "+deployment + " with terminated VMs "+vmsterminated.size()+" on app "+appid);
			for(String vmid : vmsactive){
				// active so contribute to consumption and to power
				logger.info("monitoring this active vm "+vmid);
				double partial_energy = energyService.getEnergyFromVM(appid, deployment, vmid, null);
				logger.info("monitoring energy "+partial_energy);
				double partial_power = energyService.getPowerPerVM(deployment, vmid);
				logger.info("monitoring power "+partial_power);
				if (partial_energy>0)sentToApplicationManager(buildJSONDATA(appid,vmid,VM_CONSUMPTION_ENERGY, partial_energy));
				if (partial_power>0)sentToApplicationManager(buildJSONDATA(appid,vmid,VM_CONSUMPTION_POWER, partial_power));
				logger.info("monitoring has sent data ");
				total_deployment_energy = total_deployment_energy + partial_energy;
				deployment_power=deployment_power+partial_power;
			}
			for(String vmid : vmsterminated){
				// terminated so contribute to consumption but not avg power
				logger.info("monitoring this terminated  vm "+vmid);
				double partial_energy = energyService.getEnergyFromVM(appid, deployment, vmid, null);
				logger.info("monitoring energy "+partial_energy);
				if (partial_energy>0)sentToApplicationManager(buildJSONDATA(appid,vmid,VM_CONSUMPTION_ENERGY, partial_energy));
				total_deployment_energy = total_deployment_energy + partial_energy;
			}
			logger.info("total energy "+total_deployment_energy);
			logger.info("total power pre split "+deployment_power);
			if (total_deployment_energy>0)sentToApplicationManager(buildJSONDATA(appid,"",APP_CONSUMPTION_ENERGY, total_deployment_energy));
			if (deployment_machines>0) sentToApplicationManager(buildJSONDATA(appid,"",APP_CONSUMPTION_POWER, deployment_power/deployment_machines));
			
		}
		
	}
	
	private void sentToApplicationManager(String message){
		
		
		try {
			
			logger.debug("This query " + message);
			HttpURLConnection connection;
			
			logger.debug("App monitor connection on "+url.getHost() + url.getPort() + url.getPath() + url.getProtocol());
			connection = (HttpURLConnection) url.openConnection();
			connection.setDoOutput(true);
			connection.setDoInput(true);
			connection.setRequestMethod("POST");
			connection.setRequestProperty("Content-Type", "application/json");
			OutputStream response = connection.getOutputStream();
			response.write(message.getBytes());
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
			
			
		}catch (Exception e){
			
		}
		
	}
	
	private String buildJSONDATA(String app,String node,String message,double value){
		// create the albums object
		logger.info("monitoring building data ");
		JsonObject payload = new JsonObject();
	
		payload.addProperty("appId", app);
		payload.addProperty("nodeId", node);
		JsonObject data = new JsonObject();
		data.addProperty(message, value);
		payload.add("data", data);
		Gson gson = new GsonBuilder().setPrettyPrinting().serializeNulls().setFieldNamingPolicy(FieldNamingPolicy.UPPER_CAMEL_CASE).create();
		logger.info("monitoring playload "+gson.toJson(payload).toString());
		return gson.toJson(payload).toString();
	}

	
	
}
