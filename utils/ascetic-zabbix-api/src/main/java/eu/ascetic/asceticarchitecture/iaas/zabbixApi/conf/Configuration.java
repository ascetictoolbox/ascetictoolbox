package eu.ascetic.asceticarchitecture.iaas.zabbixApi.conf;

import java.io.File;
import org.apache.commons.configuration.PropertiesConfiguration;
import org.apache.log4j.Logger;

/**
 * /**
 *
 * Copyright 2014 ATOS SPAIN S.A.
 *
 * Licensed under the Apache License, Version 2.0 (the License);
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * @author: David Rojo Antona. Atos Research and Innovation, Atos SPAIN SA
 * @email david.rojoa@atos.net
 *
 * Java representation of a Configuration
 *
 */
public class Configuration {

    private static Logger logger = Logger.getLogger(Configuration.class);

	/** The zabbix user. */

    public static String zabbixUser = "admin";
//	public static String zabbixPassword = "zabbix";
//	public static String zabbixUser = "Admin";	//Y1
//	public static String zabbixPassword = "73046447cce977b10167"; //Y1
// 	public static String zabbixUrl = "https://10.4.0.15/zabbix/api_jsonrpc.php";	//Y1	
//	public static String zabbixUrl = "http://172.24.76.124/zabbix/api_jsonrpc.php";

    public static String zabbixUrl = "http://192.168.3.199/zabbix/api_jsonrpc.php";	//Y2
    public static String zabbixPassword = "Brandmeldeanlage104"; //Y2

	
	
    private static final String zabbixConfigurationFile = "ascetic-zabbix-api.properties";

    /**
     * This reads a value from the config file, if it isn't found a default will
     * be used instead.
     *
     * @param config The config file to use.
     * @param key The key used to get the setting from the file.
     * @param defaultValue The default value to use in the event the value is
     * missing.
     * @return The new value read from file, or the default in the event this
     * value was not found.
     */
    private static String readValue(
            org.apache.commons.configuration.Configuration config,
            String key,
            String defaultValue) {
        if (config.containsKey(key)) {
            return config.getString(key);
        } else {
            config.setProperty("zabbix.server.url", defaultValue);
        }
        return defaultValue;
    }

    /**
     * This reads a value from the config file, if it isn't found a default will
     * be used instead.
     *
     * @param config The config file to use.
     * @param key The key used to get the setting from the file.
     * @param defaultValue The default value to use in the event the value is
     * missing.
     * @return The new value read from file, or the default in the event this
     * value was not found.
     */
    private static Integer readValue(
            org.apache.commons.configuration.Configuration config,
            String key,
            Integer defaultValue) {
        if (config.containsKey(key)) {
            return config.getInt(key);
        } else {
            config.setProperty("zabbix.server.url", defaultValue);
        }
        return defaultValue;
    }

    static {
        try {
            String propertiesFile = "ascetic-zabbix-api.properties";

            File f = new File(zabbixConfigurationFile);
            if (f.exists()) {
                propertiesFile = zabbixConfigurationFile;
            }

            PropertiesConfiguration config = new PropertiesConfiguration(propertiesFile);
            config.setAutoSave(true);            
            zabbixUrl = readValue(config, "zabbix.server.url", zabbixUrl);
            zabbixPassword = readValue(config, "zabbix.password", zabbixPassword);
            zabbixUser = readValue(config, "zabbix.user", zabbixUser);
            zabbixAutoLogoutTime = readValue(config, "zabbix.user.auto.logout.time", zabbixAutoLogoutTime);
            virtualMachinesGroupName = readValue(config, "zabbix.group.vm", virtualMachinesGroupName);
            vmTemplateName = readValue(config, "zabbix.vm.template", vmTemplateName);
        } catch (Exception e) {
            logger.info("Error loading the configuration of the Zabbix server");
            logger.info("Exception " + e);
        }
    }

}
