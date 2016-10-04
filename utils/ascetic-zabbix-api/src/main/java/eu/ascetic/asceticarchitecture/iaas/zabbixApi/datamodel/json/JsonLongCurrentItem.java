package eu.ascetic.asceticarchitecture.iaas.zabbixApi.datamodel.json;

// TODO: Auto-generated Javadoc
/**
 * The Class JsonCurrentItem.
 */
public class JsonLongCurrentItem {

	/** The name. */
	private String name;
	
	/** The value. */
	private long value;
	
	/** The units. */
	private String units;
	
	/** The timestamp. */
	private long timestamp;
	
	/**
	 * Instantiates a new json current item.
	 *
	 * @param hostname the hostname
	 */
	public JsonLongCurrentItem(String hostname){
		
	}

	/**
	 * Gets the value.
	 *
	 * @return the value
	 */
	public long getValue() {
		return value;
	}

	/**
	 * Sets the value.
	 *
	 * @param value the new value
	 */
	public void setValue(long value) {
		this.value = value;
	}

	/**
	 * Gets the units.
	 *
	 * @return the units
	 */
	public String getUnits() {
		return units;
	}

	/**
	 * Sets the units.
	 *
	 * @param units the new units
	 */
	public void setUnits(String units) {
		this.units = units;
	}

	/**
	 * Gets the timestamp.
	 *
	 * @return the timestamp
	 */
	public long getTimestamp() {
		return timestamp;
	}

	/**
	 * Sets the timestamp.
	 *
	 * @param timestamp the new timestamp
	 */
	public void setTimestamp(long timestamp) {
		this.timestamp = timestamp;
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
	
}
