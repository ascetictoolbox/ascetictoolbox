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

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

import java.io.FileReader;
import java.io.IOException;
import java.util.Properties;

/**
 * Singleton class that contains all the configuration parameters.
 *
 * @author David Ortiz Lopez (david.ortiz@bsc.es)
 *
 */
public class VmManagerConfiguration {
	// Configuration file
	private static final String PROPNAME_CONF_FILE = "config";
    private static final String DEFAULT_CONF_FILE = "vmmconfig.properties";

    // OpenStack configuration
    public String openStackIP;
    public int keyStonePort;
    public int glancePort;

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
    public String deploymentEngine;

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
    private Properties getPropertiesObjectFromConfigFile() {
		Properties prop = new Properties();
		Logger log = LogManager.getLogger(VmManagerConfiguration.class);
        try {
			System.out.println("**********************************************************");
			System.out.println("**********************************************************");
			System.out.println("**********************************************************");
			System.out.println("**********************************************************");
			System.out.println("*************** " + System.getProperty(PROPNAME_CONF_FILE) + " ********");
			System.out.println("**********************************************************");
			System.out.println("**********************************************************");
			System.out.println("**********************************************************");
			System.out.println("**********************************************************");
			String customFile = System.getProperty(PROPNAME_CONF_FILE);
			if(customFile != null) {
				log.info("Found a custom file in system property '"+PROPNAME_CONF_FILE+"': " + customFile);
				prop.load(new FileReader(customFile));
			} else {
				log.info("Loading default properties");
            	prop.load(VmManagerConfiguration.class.getClassLoader().getResourceAsStream(DEFAULT_CONF_FILE));
			}
        } catch (IOException e) {
			log.error("Error loading properties file", e);
            e.printStackTrace();
        }
        return prop;
    }

    /**
     * Initializes all the configuration parameters.
     *
     * @param prop properties file that contains the configuration parameters
     */
    private void initializeClassAttributes(Properties prop) {
        openStackIP = prop.getProperty("openStackIP");
        keyStonePort = Integer.parseInt(prop.getProperty("keyStonePort"));
        glancePort = Integer.parseInt(prop.getProperty("glancePort"));
        keyStoneUser = prop.getProperty("keyStoneUser");
        keyStoneTenant = prop.getProperty("keyStoneTenant");
        keyStoneTenantId = prop.getProperty("keyStoneTenantId");
        keyStonePassword = prop.getProperty("keyStonePassword");

        testingImageId = prop.getProperty("testingImageId");
        testingImageUrl = prop.getProperty("testingImageUrl");
        testingImageName = prop.getProperty("testingImageName");
        testingDeploymentBaseUrl = prop.getProperty("testingDeploymentBaseUrl");
        deployBaseUrl = prop.getProperty("deployBaseUrl");
        deployPackage = prop.getProperty("deployPackage");
        hosts = prop.getProperty("hosts").split(",");
        deploymentEngine = prop.getProperty("deploymentEngine");
        project = prop.getProperty("project");
        defaultServerTurnOnDelaySeconds = Integer.parseInt(prop.getProperty("defaultServerTurnOnDelaySeconds"));
        defaultServerTurnOffDelaySeconds = Integer.parseInt(prop.getProperty("defaultServerTurnOffDelaySeconds"));
        zabbixDbIp = prop.getProperty("zabbixDbIp");
        zabbixDbUser = prop.getProperty("zabbixDbUser");
        zabbixDbPassword = prop.getProperty("zabbixDbPassword");

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

}
