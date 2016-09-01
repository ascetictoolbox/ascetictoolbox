/*
 *  Copyright 2013-2014 Barcelona Supercomputing Center (www.bsc.es)
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package es.bsc.servicess.ide.editors.deployers;

import java.io.File;
import java.util.Properties;

import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.configuration.PropertiesConfiguration;

public class AsceticProperties {
	
	/** Service manifest file name (it will be configurable at Y3) */
	public static final String SERVICE_MANIFEST = "service_manifest.xml";
	
	
	private static final String ICS_LOC = "ics.location";
	private static final String DS_LOC = "ds.location";
	private static final String SM_LOC = "sm.location";
	private static final String LS_LOC = "ls.location";
	private static final String QUOTA_FACTOR = "quota.factor";
	private static final String TOLERANCE_FACTOR = "tolerance.factor";
	private static final String LS_CLI_PROP_LOCATION = "ls.client.properties";
	private static final String ICS_REPO_PATH = "ics.repository.path";
	private static final String ICS_RSYNC_PATH = "ics.rsync.path";
	private static final String ICS_RSH_PATH = "ics.rsh.path";


	private static final String ICS_RSH_USER_KEY_PATH = "ics.rsh.user.key.path";


	private static final String ICS_RSH_USERNAME = "ics.rsh.user.name.path";


	private static final String APP_SSH_PUBKEY = "app.ssh.public.key.path";
	private static final String APP_SSH_PRIVKEY = "app.ssh.private.key.path";
	private static final String APP_MON_LOC = "app.mon.location";
	
	private static final String POWER_BOUNDARY = "power.boundary";
	private static final String PRICE_BOUNDARY = "price.boundary";
	private static final String OPTIMIZATION_PARAMETER = "optimization.parameter";
	private static final String MAX_DURATION = "max.app.duration";
	private static final String MAX_ENERGY = "max.app.energy";
	private static final String MAX_COST = "max.app.cost";
	
	private PropertiesConfiguration config;

	public AsceticProperties(String pathToConfigFile)
			throws ConfigurationException {
		config = new PropertiesConfiguration(pathToConfigFile);
	}

	public AsceticProperties(File file) throws ConfigurationException {
		config = new PropertiesConfiguration(file);
	}

	public String getICSLocation() {
		return config.getString(ICS_LOC, "");
	}

	public void setICSLocation(String location) {
		config.setProperty(ICS_LOC, location);
	}

	public String getDSLocation() {
		return config.getString(DS_LOC, "");
	}

	public void setDSLocation(String location) {
		config.setProperty(DS_LOC, location);
	}

	public String getSMLocation() {
		return config.getString(SM_LOC, null);
	}

	public void setSMLocation(String location) {
		config.setProperty(SM_LOC, location);
	}

	public float getToleranceFactor() {
		return config.getFloat(TOLERANCE_FACTOR, 0.9f);
	}

	public float getQuotaFactor() {
		return config.getFloat(QUOTA_FACTOR, 1.3f);
	}

	public void save() throws ConfigurationException {
		config.save();
	}
	
	public String getLSLocation() {
		return config.getString(LS_LOC, "");
	}
	
	public void setLSLocation(String location) {
		config.setProperty(LS_LOC, location);		
	}

	public String getLSClientProperties() {
		return config.getString(LS_CLI_PROP_LOCATION, "");
	}
	
	public void setLSClientProperties(String location) {
		config.setProperty(LS_CLI_PROP_LOCATION, location);
		
	}

	public void setVMICRepoPath(String repoPath) {
		config.setProperty(ICS_REPO_PATH, repoPath);
		
	}
	
	public String getVMICRepoPath(){
		return config.getString(ICS_REPO_PATH, "");
		
	}
	
	public void setVMICRsyncPath(String rsyncPath) {
		config.setProperty(ICS_RSYNC_PATH, rsyncPath);
		
	}
	
	public String getVMICRsyncPath() {
		return config.getString(ICS_RSYNC_PATH, "/usr/bin/rsync");
		
	}
	
	public void setVMICRshPath(String rsyncPath) {
		config.setProperty(ICS_RSH_PATH, rsyncPath);
		
	}
	
	public String getVMICRshPath() {
		return config.getString(ICS_RSH_PATH, "/usr/bin/ssh");
		
	}
	
	public void setVMICRshUserKeyPath(String rsyncPath) {
		config.setProperty(ICS_RSH_USER_KEY_PATH, rsyncPath);
		
	}
	
	public String getVMICRshUserKeyPath() {
		return config.getString(ICS_RSH_USER_KEY_PATH, System.getProperty("user.home")+"/.ssh/id_dsa");
		
	}
	
	public void setVMICRshUsername(String rsyncPath) {
		config.setProperty(ICS_RSH_USERNAME, rsyncPath);
		
	}
	
	public String getVMICRshUsername() {
		return config.getString(ICS_RSH_USERNAME, System.getProperty("user.name"));
		
	}

	public String getApplicationSSHPublicKeyPath() {
		return config.getString(APP_SSH_PUBKEY, "");
	}
	
	public String getApplicationSSHPrivateKeyPath() {
		return config.getString(APP_SSH_PRIVKEY, "");
	}

	public void setApplicationSSHPublicKeyPath(String path) {
		config.setProperty(APP_SSH_PUBKEY, path);
	}
	
	public void setApplicationSSHPrivateKeyPath(String path) {
		config.setProperty(APP_SSH_PRIVKEY, path);
		
	}

	public String getMonitorLocation() {
		return config.getString(APP_MON_LOC, "");
	}

	public void setMonitorLocation(String location) {
		config.setProperty(APP_MON_LOC, location);
	}

	public Double getPowerBoundary() {
		return config.getDouble(POWER_BOUNDARY);
	}
	
	public void setPowerBoundary(double pow) {
		config.setProperty(POWER_BOUNDARY, pow);
	}
	
	public Double getPriceBoundary() {
		return config.getDouble(PRICE_BOUNDARY);
	}
	
	public void setPriceBoundary(double price) {
		config.setProperty(PRICE_BOUNDARY, price);
	}
	
	public String getOptimizationParameter() {
		return config.getString(OPTIMIZATION_PARAMETER, "");
	}
	
	public void setOptimizationParameter(String param) {
		config.setProperty(OPTIMIZATION_PARAMETER, param);
	}
	
	public long getMaxDuration() {
		return config.getLong(MAX_DURATION);
	}
	
	public void setMaxDuration(long duration) {
		config.setProperty(MAX_DURATION, duration);
	}
	
	public Double getMaxEnergy() {
		return config.getDouble(MAX_ENERGY);
	}
	
	public void setMaxEnergy(double energy) {
		config.setProperty(MAX_ENERGY, energy);
	}
	
	public Double getMaxCost() {
		return config.getDouble(MAX_COST);
	}
	
	public void setMaxCost(double cost) {
		config.setProperty(MAX_COST, cost);
	}
}
