package eu.ascetic.asceticarchitecture.iaas.zabbixApi.utils;


/**
*
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
* Java representation of Dictionary
* 
*/
public class Dictionary {
		
	/** The json rpc version. */
	public static String JSON_RPC_VERSION = "2.0";
	
	//HOST
	public static String HOST_HOSTID = "hostid"; 
	public static String HOST_AVAILABLE = "available";
	public static String HOST_HOST = "host";
	public static String HOST_NAME = "name";
	
	//USER
	public static String USER_LOGIN = "user";
	public static String USER_PASSWORD = "password";
	
	//ITEM
	public static String ITEM_ITEMID = "itemid";
	public static String ITEM_HOSTID = "hostid";
	public static String ITEM_NAME = "name";
	public static String ITEM_KEY = "key_";
	public static String ITEM_DELAY = "delay";
	public static String ITEM_HISTORY = "history";
	public static String ITEM_TRENDS = "trends";
	public static String ITEM_LASTVALUE = "lastvalue";
	public static String ITEM_LASTCLOCK = "lastclock";
	
	//HISTORY ITEM
	public static String HISTORY_ITEM_ITEMID = "itemid";
	public static String HISTORY_ITEM_HOSTID = "hostid";
	public static String HISTORY_ITEM_HOSTS = "hosts";
	public static String HISTORY_ITEM_CLOCK = "clock";
	public static String HISTORY_ITEM_VALUE = "value";
	public static String HISTORY_ITEM_NANOSECONDS = "ns";
	
	//HISTORY ITEM FORMATS
	public static String HISTORY_ITEM_FORMAT_FLOAT = "0";
	public static String HISTORY_ITEM_FORMAT_STRING = "1";
	public static String HISTORY_ITEM_FORMAT_LOG = "2";
	public static String HISTORY_ITEM_FORMAT_INTEGER = "3";
	public static String HISTORY_ITEM_FORMAT_TEXT = "4";
	
	//HOST GROUP
	public static String HOSTGROUP_GROUPID = "groupid";
	public static String HOSTGROUP_NAME = "name";
	
	//TEMPLATE
	public static String TEMPLATE_TEMPLATEID = "templateid";	
	public static String TEMPLATE_HOST = "host";
	public static String TEMPLATE_NAME = "host";

}
