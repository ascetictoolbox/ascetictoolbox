package eu.ascetic.asceticarchitecture.iaas.zabbixApi.datamodel;


/**
 * The Class Item.
 * 
 * @author David Rojo Antona - ATOS
 */
public class Item {

	/** The itemid. */
	private String itemid;
	
	/** The hostid. */
	private String hostid;
	
	/** The name. */
	private String name;
	
	/** The key. */
	private String key;
	
	/** The delay. */
	private String delay;
	
	/** The history. */
	private String history;
	
	/** The trends. */
	private String trends;
	
	/** The last value. */
	private String lastValue;
	
	/** The last clock. */
	private long lastClock;
	
	/**
	 * Instantiates a new item.
	 *
	 * @param name the name
	 */
	public Item(String name){
		setName(name);
	}

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
	 * Gets the name.
	 *
	 * @return the name
	 */
	public String getName() {
		return name;
	}

	/**
	 * Sets the name.
	 *
	 * @param name the new name
	 */
	public void setName(String name) {
		this.name = name;
	}

	/**
	 * Gets the key.
	 *
	 * @return the key
	 */
	public String getKey() {
		return key;
	}

	/**
	 * Sets the key.
	 *
	 * @param key the new key
	 */
	public void setKey(String key) {
		this.key = key;
	}

	/**
	 * Gets the delay.
	 *
	 * @return the delay
	 */
	public String getDelay() {
		return delay;
	}

	/**
	 * Sets the delay.
	 *
	 * @param delay the new delay
	 */
	public void setDelay(String delay) {
		this.delay = delay;
	}

	/**
	 * Gets the history.
	 *
	 * @return the history
	 */
	public String getHistory() {
		return history;
	}

	/**
	 * Sets the history.
	 *
	 * @param history the new history
	 */
	public void setHistory(String history) {
		this.history = history;
	}

	/**
	 * Gets the trends.
	 *
	 * @return the trends
	 */
	public String getTrends() {
		return trends;
	}

	/**
	 * Sets the trends.
	 *
	 * @param trends the new trends
	 */
	public void setTrends(String trends) {
		this.trends = trends;
	}

	/**
	 * Gets the last value.
	 *
	 * @return the last value
	 */
	public String getLastValue() {
		return lastValue;
	}

	/**
	 * Sets the last value.
	 *
	 * @param lastValue the new last value
	 */
	public void setLastValue(String lastValue) {
		this.lastValue = lastValue;
	}

	/**
	 * Gets the last clock.
	 *
	 * @return the last clock
	 */
	public long getLastClock() {
		return lastClock;
	}

	/**
	 * Sets the last clock.
	 *
	 * @param lastClock the new last clock
	 */
	public void setLastClock(long lastClock) {
		this.lastClock = lastClock;
	}
	
	
}
