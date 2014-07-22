package eu.ascetic.asceticarchitecture.iaas.zabbixApi.datamodel;

// TODO: Auto-generated Javadoc
/**
 * The Class HostGroup.
 *  
 * @author David Rojo Antona - ATOS
 */
public class HostGroup {

	/** The group id. */
	private String groupId;
	
	/** The name. */
	private String name;

	
	/**
	 * Instantiates a new host group.
	 */
	public HostGroup(){
		
	}
	
	/**
	 * Gets the group id.
	 *
	 * @return the group id
	 */
	public String getGroupId() {
		return groupId;
	}

	/**
	 * Sets the group id.
	 *
	 * @param groupId the new group id
	 */
	public void setGroupId(String groupId) {
		this.groupId = groupId;
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
