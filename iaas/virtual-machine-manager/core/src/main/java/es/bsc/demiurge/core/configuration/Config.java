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

package es.bsc.demiurge.core.configuration;

import es.bsc.demiurge.core.VmmGlobalListener;
import es.bsc.demiurge.core.cloudmiddleware.CloudMiddleware;
import es.bsc.demiurge.core.drivers.Estimator;
import es.bsc.demiurge.core.drivers.Monitoring;
import es.bsc.demiurge.core.drivers.VmmListener;
import es.bsc.demiurge.core.manager.VmManager;
import es.bsc.demiurge.core.monitoring.hosts.HostFactory;
import es.bsc.demiurge.core.vmplacement.CloplaConversor;
import org.apache.commons.configuration.Configuration;
import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.configuration.PropertiesConfiguration;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.optaplanner.core.impl.score.director.simple.SimpleScoreCalculator;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import java.io.File;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Singleton class that contains all the configuration parameters.
 *
 * @author Mario Macias (github.com/mariomac), David Ortiz Lopez (david.ortiz@bsc.es), Mario Macias (http://github.com/mariomac)
 *
 */
public enum Config {
    INSTANCE;

	private String configurationFileName = null;

	// Configuration file
	private static final String PROPNAME_CONF_FILE = "config";

	private static final String DEFAULT_CONF_FILE_LOCATION = "/etc/demiurge/config.properties";
	private static final String OLD_ASCETIC_DEFAULT_CONF_FILE_LOCATION = "/etc/ascetic/vmm/vmmconfig.properties";
    private static final String DEFAULT_DB_NAME = "VmManagerDb";
    private static final String DEFAULT_BEANS_LOCATION = "/Beans.xml";

    // TODO: remove public ATTRIBUTES and access only through apache configuration
    public String dbName;

    // OpenStack configuration
    public boolean deployVmWithVolume;


    // Servers host names
    public String[] hosts;

    // Deploy
    public int connectionPort;
    public String deployPackage;

    private Monitoring monitoring;
    private CloudMiddleware cloudMiddleware;
    private Set<Estimator> estimators;

    // Zabbix config
    public String zabbixDbIp;
    public String zabbixDbUser;
    public String zabbixDbPassword;

	private Map<String,Class<? extends SimpleScoreCalculator>> placementPolicies;
    private Configuration configuration;
	private List<VmmListener> vmmListeners;
	private HostFactory hostFactory;
	private VmManager vmManager;
    private CloplaConversor cloplaConversor;
	private List<VmmGlobalListener> vmmGlobalListeners;

	Config() {
        configuration = getPropertiesObjectFromConfigFile();
		initializeClassAttributes();
    }

    public Configuration getConfiguration() {
        return configuration;
    }

    /**
     * Returns a properties file that contains the configuration parameters for the VM Manager.
     *
     * @return the properties file
     */
    private Configuration getPropertiesObjectFromConfigFile() {
		Logger log = LogManager.getLogger(Config.class);
        try {
			// TO ALLOW COMPATIBILITY WITH OLDER VERSIONS OF VMM (Ascetic_exclusive)
			// If there is a config file in the newest default location, looks for it
			// if not, it looks in the old Ascetic default location
			String defaultFileName = OLD_ASCETIC_DEFAULT_CONF_FILE_LOCATION;
			if(new File(DEFAULT_CONF_FILE_LOCATION).exists()) {
				defaultFileName = DEFAULT_CONF_FILE_LOCATION;
			}
			configurationFileName = System.getProperty(PROPNAME_CONF_FILE, defaultFileName);

            log.debug("Loading configuration file: " + configurationFileName);
            return new PropertiesConfiguration(configurationFileName);
        } catch (ConfigurationException e) {
			log.error("Error loading properties file", e);
            e.printStackTrace();
        }
        return null;
    }

	public String getConfigurationFileName() {
		return configurationFileName;
	}

	/**
     * Initializes all the configuration parameters.
     *
     */
    private void initializeClassAttributes() {
        Logger logger = LogManager.getLogger(Config.class);
        dbName = configuration.getString("dbName", DEFAULT_DB_NAME);

        connectionPort = configuration.getInt("connectionPort",80);
        deployPackage = configuration.getString("deployPackage");
        hosts = configuration.getStringArray("hosts");
        zabbixDbIp = configuration.getString("zabbixDbIp");
        zabbixDbUser = configuration.getString("zabbixDbUser");
        zabbixDbPassword = configuration.getString("zabbixDbPassword");
        deployVmWithVolume = configuration.getBoolean("deployVmWithVolume", false);

		logger.debug("Loading configuration: " + toString());
    }

    public void loadBeansConfig() {
        ApplicationContext springContext = new ClassPathXmlApplicationContext(DEFAULT_BEANS_LOCATION);
		placementPolicies = springContext.getBean("placementPolicies",Map.class);

		vmManager = springContext.getBean("vmManager",VmManager.class);


        cloudMiddleware = springContext.getBean("cloudMiddleware",CloudMiddleware.class);
        monitoring = springContext.getBean("monitoring",Monitoring.class);

        estimators = springContext.getBean("estimators", Set.class);

		vmmListeners = springContext.getBean("vmmListeners", List.class);
		vmmGlobalListeners = springContext.getBean("vmmGlobalListeners", List.class);

        // by the moment, don't need to put these two into beans (to simplify)
		hostFactory = new HostFactory(cloudMiddleware, monitoring);
        cloplaConversor = new CloplaConversor();

        /*
         * Extra initialization actions for managers
         */
        vmManager.doInitActions();


    }

	public Map<String,Class<? extends SimpleScoreCalculator>> getPlacementPolicies() {
		return placementPolicies;
	}

	public Monitoring getMonitoring() {
        return monitoring;
    }

    public CloudMiddleware getCloudMiddleware() {
        return cloudMiddleware;
    }

	public Set<Estimator> getEstimators() {
		return estimators;
	}

	public List<VmmListener> getVmmListeners() {
		return vmmListeners;
	}

    public CloplaConversor getCloplaConversor() {
        return cloplaConversor;
    }

	public List<VmmGlobalListener> getVmmGlobalListeners() { return vmmGlobalListeners; }

    @Override
	public String toString() {
		return "Config{" +
                "\n\tdbName='" + dbName + '\'' +
				"\n\thosts=" + Arrays.toString(hosts) +
				"\n\tconnectionPort='" + connectionPort + '\'' +
				"\n\tdeployPackage='" + deployPackage + '\'' +
				"\n\tzabbixDbIp='" + zabbixDbIp + '\'' +
				"\n\tzabbixDbUser='" + zabbixDbUser + '\'' +
				"\n\tzabbixDbPassword='" + zabbixDbPassword + '\'' +
                "\n\tdeployVmWithVolume='" + deployVmWithVolume + '\'' +
				"\n}";
	}

	public HostFactory getHostFactory() {
		return hostFactory;
	}

	public VmManager getVmManager() {
		return vmManager;
	}
}
