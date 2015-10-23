/**
 Copyright (C) 2013-2014  Barcelona Supercomputing Center

 This library is free software; you can redistribute it and/or
 modify it under the terms of the GNU Lesser General Public
 License as published by the Free Software Foundation; either
 version 2.1 of the License, or (at your option) any later version.

 This library is distributed in the hope that it will be useful,
 but WITHOUT ANY WARRANTY; without even the implied warranty of
 MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 Lesser General Public License for more details.

 You should have received a copy of the GNU Lesser General Public
 License along with this library; if not, write to the Free Software
 Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301  USA
 */

package es.bsc.vmmanagercore.configuration;

import es.bsc.vmmanagercore.manager.DeploymentEngine;
import org.apache.commons.configuration.Configuration;
import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.configuration.PropertiesConfiguration;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

import java.util.Arrays;

/**
 * Singleton class that contains all the configuration parameters.
 *
 * @author David Ortiz Lopez (david.ortiz@bsc.es)
 *
 */
public class VmManagerConfiguration {

	// Configuration file
	private static final String PROPNAME_CONF_FILE = "config";

    private static final String DEFAULT_CONF_FILE_LOCATION = "/etc/ascetic/vmm/vmmconfig.properties";
    private static final String DEFAULT_DB_NAME = "VmManagerDb";

    public String dbName;

    // OpenStack configuration
    public String openStackIP;
    public int keyStonePort;
    public int glancePort;
    public boolean deployVmWithVolume;
    // OpenStack login credentials
    public String keyStoneUser;
    public String keyStoneTenant;
    public String keyStoneTenantId;
    public String keyStonePassword;

    // Testing configuration
    public String testingImageId;
    public String testingImageUrl;
    public String testingImageName;
    public String testingDeploymentBaseUrl;

    // Servers host names
    public String[] hosts;

    // Deploy
    public String deployBaseUrl;
    public String deployPackage;

    // VM deployments
    public DeploymentEngine deploymentEngine;

    // Software used
    public enum Monitoring { OPENSTACK, GANGLIA, ZABBIX, FAKE }
    public enum Middleware { OPENSTACK, FAKE }
    public Monitoring monitoring;
    public Middleware middleware;

    // Some things need to be adapted depending on the project for which the VMM has been deployed.
    // Therefore, we need an attribute that indicates the current project
    public String project;
    
    // Turn on/off servers
    public int defaultServerTurnOnDelaySeconds;
    public int defaultServerTurnOffDelaySeconds;

    // Zabbix config
    public String zabbixDbIp;
    public String zabbixDbUser;
    public String zabbixDbPassword;

    /**
     * Returns a properties file that contains the configuration parameters for the VM Manager.
     *
     * @return the properties file
     */
    private Configuration getPropertiesObjectFromConfigFile() {
		Configuration config;
		Logger log = LogManager.getLogger(VmManagerConfiguration.class);
        try {
			String customFileLocation = System.getProperty(PROPNAME_CONF_FILE,DEFAULT_CONF_FILE_LOCATION);

            log.info("Loading configuration file: " + customFileLocation);
            return new PropertiesConfiguration(customFileLocation);
        } catch (ConfigurationException e) {
			log.error("Error loading properties file", e);
            e.printStackTrace();
        }
        return null;
    }

    /**
     * Initializes all the configuration parameters.
     *
     * @param prop properties file that contains the configuration parameters
     */
    private void initializeClassAttributes(Configuration prop) {
        Logger logger = LogManager.getLogger(VmManagerConfiguration.class);
        dbName = prop.getString("dbName", DEFAULT_DB_NAME);
        openStackIP = prop.getString("openStackIP");
        keyStonePort = prop.getInt("keyStonePort");
        glancePort = prop.getInt("glancePort");
        keyStoneUser = prop.getString("keyStoneUser");
        keyStoneTenant = prop.getString("keyStoneTenant");
        keyStoneTenantId = prop.getString("keyStoneTenantId");
        keyStonePassword = prop.getString("keyStonePassword");

        testingImageId = prop.getString("testingImageId");
        testingImageUrl = prop.getString("testingImageUrl");
        testingImageName = prop.getString("testingImageName");
        testingDeploymentBaseUrl = prop.getString("testingDeploymentBaseUrl");
        deployBaseUrl = prop.getString("deployBaseUrl");
        deployPackage = prop.getString("deployPackage");
        hosts = prop.getStringArray("hosts");
        try {
            deploymentEngine = DeploymentEngine.fromName(prop.getString("deploymentEngine"));
        } catch(Exception e) {
            logger.error("Deployment Engine null or unknown. Assuming LEGACY: " + e.getMessage());
            deploymentEngine = DeploymentEngine.LEGACY;
        }
        project = prop.getString("project");
        defaultServerTurnOnDelaySeconds = prop.getInt("defaultServerTurnOnDelaySeconds");
        defaultServerTurnOffDelaySeconds = prop.getInt("defaultServerTurnOffDelaySeconds");
        zabbixDbIp = prop.getString("zabbixDbIp");
        zabbixDbUser = prop.getString("zabbixDbUser");
        zabbixDbPassword = prop.getString("zabbixDbPassword");
        deployVmWithVolume = prop.getBoolean("deployVmWithVolume", false);

        if (prop.getProperty("monitoring").equals("openstack")) {
            monitoring = Monitoring.OPENSTACK;
        }
        else if (prop.getProperty("monitoring").equals("ganglia")) {
            monitoring = Monitoring.GANGLIA;
        }
        else if (prop.getProperty("monitoring").equals("zabbix")) {
            monitoring = Monitoring.ZABBIX;
        }
        else if (prop.getProperty("monitoring").equals("fake")) {
            monitoring = Monitoring.FAKE;
        }
        else {
            throw new IllegalArgumentException("The monitoring software selected is not supported.");
        }

        if (prop.getProperty("middleware").equals("openstack")) {
            middleware = Middleware.OPENSTACK;
        }
        else if (prop.getProperty("middleware").equals("fake")) {
            middleware = Middleware.FAKE;
        }
        else {
            throw new IllegalArgumentException("The cloud middleware selected is not supported");
        }

		logger.debug("Loading configuration: " + toString());
    }

    /**
     * Private constructor that prevents instantiation from other classes (singleton pattern)
     */
    private VmManagerConfiguration() {
        initializeClassAttributes(getPropertiesObjectFromConfigFile());
    }

    /**
     * Singleton holder.
     */
    private static class SingletonHolder {
        private static final VmManagerConfiguration CONF_INSTANCE = new VmManagerConfiguration();
    }

    /**
     * Returns an instance of the VmManagerConfiguration class. It contains all the configuration parameters
     * that the VM Manager needs.
     *
     * @return the instance of VmManagerConfiguration
     */
    public static VmManagerConfiguration getInstance() {
        return SingletonHolder.CONF_INSTANCE;
    }


	@Override
	public String toString() {
		return "VmManagerConfiguration{" +
                "\n\tdbName='" + dbName + '\'' +
				"\n\topenStackIP='" + openStackIP + '\'' +
				"\n\tkeyStonePort=" + keyStonePort +
				"\n\tglancePort=" + glancePort +
				"\n\tkeyStoneUser='" + keyStoneUser + '\'' +
				"\n\tkeyStoneTenant='" + keyStoneTenant + '\'' +
				"\n\tkeyStoneTenantId='" + keyStoneTenantId + '\'' +
				"\n\tkeyStonePassword='" + keyStonePassword + '\'' +
				"\n\ttestingImageId='" + testingImageId + '\'' +
				"\n\ttestingImageUrl='" + testingImageUrl + '\'' +
				"\n\ttestingImageName='" + testingImageName + '\'' +
				"\n\ttestingDeploymentBaseUrl='" + testingDeploymentBaseUrl + '\'' +
				"\n\thosts=" + Arrays.toString(hosts) +
				"\n\tdeployBaseUrl='" + deployBaseUrl + '\'' +
				"\n\tdeployPackage='" + deployPackage + '\'' +
				"\n\tdeploymentEngine='" + deploymentEngine + '\'' +
				"\n\tmonitoring=" + monitoring +
				"\n\tmiddleware=" + middleware +
				"\n\tproject='" + project + '\'' +
				"\n\tdefaultServerTurnOnDelaySeconds=" + defaultServerTurnOnDelaySeconds +
				"\n\tdefaultServerTurnOffDelaySeconds=" + defaultServerTurnOffDelaySeconds +
				"\n\tzabbixDbIp='" + zabbixDbIp + '\'' +
				"\n\tzabbixDbUser='" + zabbixDbUser + '\'' +
				"\n\tzabbixDbPassword='" + zabbixDbPassword + '\'' +
                "\n\tdeployVmWithVolume='" + deployVmWithVolume + '\'' +
				"\n}";
	}
}
