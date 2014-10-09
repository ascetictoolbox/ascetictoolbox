package eu.ascetic.asceticarchitecture.iaas.zabbixApi.datamodel;


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
 * Java representation of host retrieved from Zabbix
 * 
 */

public class Host {

	/** The hostid. */
	private String hostid;
	
	/** The host. */
	private String host;		//hostname
	
	/** The available. */
	private String available;
	
	/** The name. */
	private String name;
	
	/**
	 * Instantiates a new host.
	 */
	public Host(){
		
	}
	
	/**
	 * Gets the hostid.
	 *
	 * @return the hostid
	 */
	public String getHostid() {
		return hostid;
	}
	
	/**
	 * Sets the hostid.
	 *
	 * @param hostid the new hostid
	 */
	public void setHostid(String hostid) {
		this.hostid = hostid;
	}
	
	/**
	 * Gets the host.
	 *
	 * @return the host
	 */
	public String getHost() {
		return host;
	}
	
	/**
	 * Sets the host.
	 *
	 * @param host the new host
	 */
	public void setHost(String host) {
		this.host = host;
	}
	
	/**
	 * Gets the available.
	 *
	 * @return the available
	 */
	public String getAvailable() {
		return available;
	}
	
	/**
	 * Sets the available.
	 *
	 * @param available the new available
	 */
	public void setAvailable(String available) {
		this.available = available;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}
	
}
