package eu.ascetic.saas.applicationpackager.xml.model;

import java.util.ArrayList;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

// TODO: Auto-generated Javadoc
/**
 * The Class AdaptationRule.
 */
@XmlRootElement(name="adaptation-rule")
@XmlAccessorType(XmlAccessType.FIELD)
public class AdaptationRule {

	/** The id. */
	@XmlAttribute(name="id")
	private String id;
	
	/** The description. */
	@XmlAttribute(name="description")
	private String description;
	
	/** The adaptation sla target. */
	@XmlElement(name="SLATarget")
    private AdaptationSlaTarget adaptationSlaTarget;
	
	/** The adapt. */
	@XmlElement(name="adapt")
    private ArrayList<Adapt> adapts;

	/**
	 * Gets the id.
	 *
	 * @return the id
	 */
	public String getId() {
		return id;
	}

	/**
	 * Sets the id.
	 *
	 * @param id the new id
	 */
	public void setId(String id) {
		this.id = id;
	}

	/**
	 * Gets the description.
	 *
	 * @return the description
	 */
	public String getDescription() {
		return description;
	}

	/**
	 * Sets the description.
	 *
	 * @param description the new description
	 */
	public void setDescription(String description) {
		this.description = description;
	}

	/**
	 * Gets the adaptation sla target.
	 *
	 * @return the adaptation sla target
	 */
	public AdaptationSlaTarget getAdaptationSlaTarget() {
		return adaptationSlaTarget;
	}

	/**
	 * Sets the adaptation sla target.
	 *
	 * @param adaptationSlaTarget the new adaptation sla target
	 */
	public void setAdaptationSlaTarget(AdaptationSlaTarget adaptationSlaTarget) {
		this.adaptationSlaTarget = adaptationSlaTarget;
	}

	/**
	 * Gets the adapt.
	 *
	 * @return the adapt
	 */
	public ArrayList<Adapt> getAdapt() {
		return adapts;
	}

	/**
	 * Sets the adapt.
	 *
	 * @param adapt the new adapt
	 */
	public void setAdapt(ArrayList<Adapt> adapts) {
		this.adapts = adapts;
	}
	
	
}
