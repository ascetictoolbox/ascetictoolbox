package eu.ascetic.asceticarchitecture.paas.component.energymodeller.internal.monitor;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.sql.Timestamp;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import org.apache.ibatis.session.SqlSession;
import org.apache.log4j.Logger;

import com.google.gson.FieldNamingPolicy;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;

import eu.ascetic.asceticarchitecture.paas.component.energymodeller.internal.common.data.database.table.EnergyModellerMonitoring;
import eu.ascetic.asceticarchitecture.paas.component.energymodeller.internal.common.data.ibatis.ApplicationRegistry;
import eu.ascetic.asceticarchitecture.paas.component.energymodeller.internal.common.data.ibatis.MonitoringRegistry;
import eu.ascetic.asceticarchitecture.paas.component.energymodeller.internal.common.data.ibatis.mapper.AppRegistryMapper;
import eu.ascetic.asceticarchitecture.paas.component.energymodeller.internal.common.data.ibatis.mapper.MonitoringMapper;
import eu.ascetic.asceticarchitecture.paas.component.energymodeller.internal.common.dataservice.EnergyDataAggregatorServiceQueue;

public class EnergyModellerMonitor implements Runnable {

	private static final Logger logger = Logger.getLogger(EnergyModellerMonitor.class);
	
	private EnergyDataAggregatorServiceQueue energyService;
	private ApplicationRegistry appRegistry;
	private MonitoringRegistry monitoringRegistry;
	// M. Fontanella - 26 Apr 2016 - begin
	private boolean enablePowerFromIaas = true;
	// M. Fontanella - 26 Apr 2016 - end
	
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
	

	public void setMonitoring(MonitoringRegistry monitoringRegistry) {
		this.monitoringRegistry = monitoringRegistry;
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


	public EnergyDataAggregatorServiceQueue getEnergyService() {
		return energyService;
	}

	// M. Fontanella - 26 Apr 2016 - begin
	public boolean getEnablePowerFromIaas() {
		return enablePowerFromIaas;
	}

	public void setEnablePowerFromIaas(boolean enablePowerFromIaas) {
		this.enablePowerFromIaas = enablePowerFromIaas;
	}
	// M. Fontanella - 26 Apr 2016 - end
		
	public boolean subscribeMonitoring(String providerid, String applicationid,	String deploymentid) {
		SqlSession session = monitoringRegistry.getSession();
		MonitoringMapper monitoringMapper = session.getMapper(MonitoringMapper.class);
		EnergyModellerMonitoring energyModellerMonitoring = new EnergyModellerMonitoring();
		// M. Fontanella - 20 Jan 2016 - begin
		energyModellerMonitoring.setProviderid(providerid);
		// M. Fontanella - 20 Jan 2016 - end
		energyModellerMonitoring.setApplicationid(applicationid);
		energyModellerMonitoring.setDeploymentid(deploymentid);
		// M. Fontanella - 10 Feb 2016 - begin
		// DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		// Calendar cal = Calendar.getInstance();
		// Timestamp ts = Timestamp.valueOf(dateFormat.format(cal.getTime()));
		// energyModellerMonitoring.setStarted(ts);
		Date date = new Date();
		energyModellerMonitoring.setStart(date.getTime());
		// M. Fontanella - 10 Feb 2016 - end
		monitoringMapper.createMonitoring(energyModellerMonitoring);
	
		return false;
	}
	
	public boolean unsubscribeMonitoring(String providerid,	String applicationid, String deploymentid) {
		SqlSession session = monitoringRegistry.getSession();
		MonitoringMapper monitoringMapper = session.getMapper(MonitoringMapper.class);
		// M. Fontanella - 20 Jan 2016 - begin
		monitoringMapper.terminateMonitoring(providerid, applicationid, deploymentid);
		// M. Fontanella - 20 Jan 2016 - end
		return true;
	}

	/**
	 * starts a thread that at regular intervals check via database DAO dbmanager the application, if any, that are reigstered to monitoring, then it calculates the consumption or the prediction and pushes it to the queue
	 * in thi case the monitoring information are sent to the application monitor via rest interface becuase it will then inform other components of the available monitoring information
	 */
	@Override
	public void run() {
		AppRegistryMapper registryMapper = appRegistry.getSession().getMapper(AppRegistryMapper.class);
		logger.info("monitoring data ..");
		SqlSession session = monitoringRegistry.getSession();
		MonitoringMapper monitoringMapper = session.getMapper(MonitoringMapper.class);
		List<EnergyModellerMonitoring> monitoredInstances = monitoringMapper.getMonitoringActive();
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
			// M. Fontanella - 08 Feb 2016 - begin
			String provid = registryMapper.selectProvByDeploy(deployment);
			// M. Fontanella - 08 Feb 2016 - end
			String appid = registryMapper.selectAppByDeploy(deployment);
			// M. Fontanella - 20 Jan 2016 - begin
			logger.info("monitoring deployment "+deployment + " with active VMs "+vmsactive.size()+" on prov "+provid+" on app "+appid);
			logger.info("monitoring deployment "+deployment + " with terminated VMs "+vmsterminated.size()+" on prov "+provid+" on app "+appid);
			// M. Fontanella - 20 Jan 2016 - end
			for(String vmid : vmsactive){
				// active so contribute to consumption and to power
				logger.info("monitoring this active vm "+vmid);
				// M. Fontanella - 11 Jan 2016 - begin
				// M. Fontanella - 26 Apr 2016 - begin
				double partial_energy = energyService.getEnergyFromVM(provid, appid, deployment, vmid, null, enablePowerFromIaas);
				// M. Fontanella - 26 Apr 2016 - end
				// M. Fontanella - 11 Jan 2016 - end
				logger.info("monitoring energy "+partial_energy);
				// M. Fontanella - 26 Apr 2016 - begin
				// M. Fontanella - 16 Jun 2016 - begin
				double partial_power = energyService.getPowerPerVM(provid, deployment, vmid, enablePowerFromIaas);
				// M. Fontanella - 16 Jun 2016 - end
				// M. Fontanella - 26 Apr 2016 - end
				logger.info("monitoring power "+partial_power);
				// M. Fontanella - 20 Jan 2016 - begin
				if (partial_energy>0)sentToApplicationManager(buildJSONDATA(provid,appid,vmid,VM_CONSUMPTION_ENERGY, partial_energy));
				if (partial_power>0)sentToApplicationManager(buildJSONDATA(provid,appid,vmid,VM_CONSUMPTION_POWER, partial_power));
				// M. Fontanella - 20 Jan 2016 - end
				logger.info("monitoring has sent data ");
				total_deployment_energy = total_deployment_energy + partial_energy;
				deployment_power=deployment_power+partial_power;
			}
			for(String vmid : vmsterminated){
				// terminated so contribute to consumption but not avg power
				logger.info("monitoring this terminated  vm "+vmid);
				// M. Fontanella - 11 Jan 2016 - begin
				// M. Fontanella - 26 Apr 2016 - begin
				double partial_energy = energyService.getEnergyFromVM(provid, appid, deployment, vmid, null, enablePowerFromIaas);
				// M. Fontanella - 26 Apr 2016 - end
				// M. Fontanella - 11 Jan 2016 - end
				logger.info("monitoring energy "+partial_energy);
				// M. Fontanella - 20 Jan 2016 - begin
				if (partial_energy>0)sentToApplicationManager(buildJSONDATA(provid,appid,vmid,VM_CONSUMPTION_ENERGY, partial_energy));
				// M. Fontanella - 20 Jan 2016 - end
				total_deployment_energy = total_deployment_energy + partial_energy;
			}
			logger.info("total energy "+total_deployment_energy);
			logger.info("total power pre split "+deployment_power);
			// M. Fontanella - 20 Jan 2016 - begin
			if (total_deployment_energy>0)sentToApplicationManager(buildJSONDATA(provid,appid,"",APP_CONSUMPTION_ENERGY, total_deployment_energy));
			if (deployment_machines>0) sentToApplicationManager(buildJSONDATA(provid,appid,"",APP_CONSUMPTION_POWER, deployment_power/deployment_machines));
			// M. Fontanella - 20 Jan 2016 - end
			
		}
		
	}
	
	/**
	 * 
	 * send a metric to the application manager 
	 */
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
	
	/**
	 * 
	 * build a json representation of the monitoring data around a JSON formatted text
	 */
		// M. Fontanella - 20 Jan 2016 - begin
		private String buildJSONDATA(String prov,String app,String node,String message,double value){
		// M. Fontanella - 20 Jan 2016 - end
			// create the albums object
		logger.info("monitoring building data ");
		JsonObject payload = new JsonObject();
	
		// M. Fontanella - 20 Jan 2016 - begin
		payload.addProperty("provId", prov);
		// M. Fontanella - 20 Jan 2016 - end
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
