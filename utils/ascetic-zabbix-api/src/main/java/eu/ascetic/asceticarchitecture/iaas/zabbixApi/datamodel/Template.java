package eu.ascetic.asceticarchitecture.iaas.zabbixApi.datamodel;

/**
 * The Class Template.
 * 
 * @author David Rojo Antona - ATOS
 */

public class Template {

	/** The template id. */
	private String templateId;
	
	/** The host. */
	private String host;
	
	/** The name. */
	private String name;
	
	
	/**
	 * Instantiates a new template.
	 */
	public Template(){
		
	}
	
	/**
	 * Gets the template id.
	 *
	 * @return the template id
	 */
	public String getTemplateId() {
		return templateId;
	}
	
	/**
	 * Sets the template id.
	 *
	 * @param templateId the new template id
	 */
	public void setTemplateId(String templateId) {
		this.templateId = templateId;
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
