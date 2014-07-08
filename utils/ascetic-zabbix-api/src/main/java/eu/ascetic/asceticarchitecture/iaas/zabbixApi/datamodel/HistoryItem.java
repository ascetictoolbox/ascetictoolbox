package eu.ascetic.asceticarchitecture.iaas.zabbixApi.datamodel;

// TODO: Auto-generated Javadoc
/**
 * The Class HistoryItem.
 *  
 * @author David Rojo Antona - ATOS
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
