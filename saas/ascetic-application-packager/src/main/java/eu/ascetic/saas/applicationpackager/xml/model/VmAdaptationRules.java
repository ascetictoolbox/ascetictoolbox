package eu.ascetic.saas.applicationpackager.xml.model;

import java.util.ArrayList;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

// TODO: Auto-generated Javadoc
/**
 * The Class VmAdaptationRules.
 */
@XmlRootElement(name="vmAdaptationRules")
@XmlAccessorType(XmlAccessType.FIELD)
public class VmAdaptationRules {

	/** The adaptation rules. */
	@XmlElement(name="adaptation-rule")
    private ArrayList<AdaptationRule> adaptationRules;

	/**
	 * Gets the adaptation rules.
	 *
	 * @return the adaptation rules
	 */
	public ArrayList<AdaptationRule> getAdaptationRules() {
		return adaptationRules;
	}

	/**
	 * Sets the adaptation rules.
	 *
	 * @param adaptationRules the new adaptation rules
	 */
	public void setAdaptationRules(ArrayList<AdaptationRule> adaptationRules) {
		this.adaptationRules = adaptationRules;
	}
	
}
