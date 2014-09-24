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
//	public static String zabbixUser = "Admin";
	public static String zabbixUser = "admin";
//	public static String zabbixPassword = "zabbix";
	public static String zabbixPassword = "73046447cce977b10167";
//	public static String zabbixUrl = "http://172.24.76.124/zabbix/api_jsonrpc.php";
	public static String zabbixUrl = "https://10.4.0.15/zabbix/api_jsonrpc.php";
	public static String virtualMachinesGroupName = "Virtual Machines";
	public static String osLinuxTemplateName = "Template OS Linux";
	public static Integer zabbixAutoLogoutTime = 990;
	
	private static final String zabbixConfigurationFile = "ascetic-zabbix-api.properties";
	
	static {
        try {
        	String propertiesFile = "ascetic-zabbix-api.properties";
        	
        	File f = new File(zabbixConfigurationFile);
        	if(f.exists()) { 
        		propertiesFile = zabbixConfigurationFile; 
        	}
        	
        	org.apache.commons.configuration.Configuration config = new PropertiesConfiguration(propertiesFile);
        	zabbixUrl = config.getString("zabbix.server.url");
        	zabbixPassword = config.getString("zabbix.password");
        	zabbixUser = config.getString("zabbix.user");
        	zabbixAutoLogoutTime = config.getInt("zabbix.user.auto.logout.time");
        	virtualMachinesGroupName = config.getString("zabbix.group.vm");
        	osLinuxTemplateName = config.getString("zabbix.template.linux");
        	}
        catch (Exception e) {
            logger.info("Error loading the configuration of the Zabbix server");
            logger.info("Exception " + e);
        }  
    }

}
