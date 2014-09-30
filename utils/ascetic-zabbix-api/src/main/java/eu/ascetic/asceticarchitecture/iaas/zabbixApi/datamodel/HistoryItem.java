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
 * Java representation of history item retrieved from Zabbix
 * 
 */
public class HistoryItem {
	
	/** The hostid. */
	private String hostid;
	
	/** The itemid. */
	private String itemid;
    
    /** The clock: Time when that value was received. */
    private long clock;
    
    /** The value. */
    private String value;
    
    /** The ns: Nanoseconds when the value was received. */
    private String nanoseconds;
    
    
	/**
	 * Gets the itemid.
	 *
	 * @return the itemid
	 */
	public String getItemid() {
		return itemid;
	}
	
	/**
	 * Sets the itemid.
	 *
	 * @param itemid the new itemid
	 */
	public void setItemid(String itemid) {
		this.itemid = itemid;
	}
	
	/**
	 * Gets the clock.
	 *
	 * @return the clock
	 */
	public long getClock() {
		return clock;
	}
	
	/**
	 * Sets the clock.
	 *
	 * @param clock the new clock
	 */
	public void setClock(long clock) {
		this.clock = clock;
	}
	
	/**
	 * Gets the value.
	 *
	 * @return the value
	 */
	public String getValue() {
		return value;
	}
	
	/**
	 * Sets the value.
	 *
	 * @param value the new value
	 */
	public void setValue(String value) {
		this.value = value;
	}
	
	/**
	 * Gets the nanoseconds.
	 *
	 * @return the ns
	 */
	public String getNanoseconds() {
		return nanoseconds;
	}
	
	/**
	 * Sets the nanoseconds.
	 *
	 * @param ns the new ns
	 */
	public void setNanoseconds(String ns) {
		this.nanoseconds = ns;
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
	
	
}
