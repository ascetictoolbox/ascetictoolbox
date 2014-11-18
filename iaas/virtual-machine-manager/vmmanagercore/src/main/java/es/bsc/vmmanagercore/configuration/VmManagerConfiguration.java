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
    private static final String CONF_FILE_LOCATION = "configTUB.properties";

    // OpenStack configuration
    public String openStackIP;
    public int keyStonePort;
    public int glancePort;

    // OpenStack login credentials
    public String keyStoneUser;
    public String keyStoneTenant;
    public String keyStoneTenantId;
    public String keyStonePassword;

    // OpenStack login credentials for testing
    public String keyStoneUserTesting;
    public String keyStoneTenantTesting;
    public String keyStoneTenantIdTesting;
    public String keyStonePasswordTesting;

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
    public enum Monitoring { OPENSTACK, GANGLIA, ZABBIX }
    public enum Middleware { OPENSTACK }
    public Monitoring monitoring;
    public Middleware middleware;

    // Some things need to be adapted depending on the project for which the VMM has been deployed.
    // Therefore, we need an attribute that indicates the current project
    public String project;

    /**
     * Returns a properties file that contains the configuration parameters for the VM Manager.
     *
     * @return the properties file
     */
    private Properties getPropertiesObjectFromConfigFile() {
        Properties prop = new Properties();
        try {
            prop.load(VmManagerConfiguration.class.getClassLoader().getResourceAsStream(CONF_FILE_LOCATION));
        } catch (IOException e) {
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
        keyStoneUserTesting = prop.getProperty("keyStoneUserTesting");
        keyStoneTenantTesting = prop.getProperty("keyStoneTenantTesting");
        keyStoneTenantIdTesting = prop.getProperty("keyStoneTenantIdTesting");
        keyStonePasswordTesting = prop.getProperty("keyStonePasswordTesting");
        testingImageId = prop.getProperty("testingImageId");
        testingImageUrl = prop.getProperty("testingImageUrl");
        testingImageName = prop.getProperty("testingImageName");
        testingDeploymentBaseUrl = prop.getProperty("testingDeploymentBaseUrl");
        deployBaseUrl = prop.getProperty("deployBaseUrl");
        deployPackage = prop.getProperty("deployPackage");
        hosts = prop.getProperty("hosts").split(",");
        deploymentEngine = prop.getProperty("deploymentEngine");
        project = prop.getProperty("project");

        if(prop.getProperty("monitoring").equals("openstack")) {
            monitoring = Monitoring.OPENSTACK;
        }
        else if (prop.getProperty("monitoring").equals("ganglia")) {
            monitoring = Monitoring.GANGLIA;
        }
        else if (prop.getProperty("monitoring").equals("zabbix")) {
            monitoring = Monitoring.ZABBIX;
        }
        else {
            throw new IllegalArgumentException("The monitoring software selected is not supported.");
        }

        if(prop.getProperty("middleware").equals("openstack")) {
            middleware = Middleware.OPENSTACK;
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
