package eu.ascetic.saas.applicationpackager.xml.model;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;

// TODO: Auto-generated Javadoc
/**
 * The Class Adapt.
 */
@XmlRootElement(name="adapt")
@XmlAccessorType(XmlAccessType.FIELD)
public class Adapt {

	
	/** The trigger breach distance percentage min. */
	@XmlAttribute(name="triggerBreachDistancePercentageMin")
	private String triggerBreachDistancePercentageMin;
	
	/** The trigger breach distance percentage max. */
	@XmlAttribute(name="triggerBreachDistancePercentageMax")
	private String triggerBreachDistancePercentageMax;
	
	/** The type. */
	@XmlAttribute(name="type")
	private String type;
	
	/** The direction. */
	@XmlAttribute(name="direction")
	private String direction;

	/** The reset level. */
	@XmlAttribute(name="resetLevel")
	private String resetLevel;

	/** The minimal num of v ms. */
	@XmlAttribute(name="minimalNumOfVMs")
	private String minimalNumOfVMs;

	

	/**
	 * Gets the trigger breach distance percentage min.
	 *
	 * @return the trigger breach distance percentage min
	 */
	public String getTriggerBreachDistancePercentageMin() {
		return triggerBreachDistancePercentageMin;
	}


	/**
	 * Sets the trigger breach distance percentage min.
	 *
	 * @param triggerBreachDistancePercentageMin the new trigger breach distance percentage min
	 */
	public void setTriggerBreachDistancePercentageMin(String triggerBreachDistancePercentageMin) {
		this.triggerBreachDistancePercentageMin = triggerBreachDistancePercentageMin;
	}

	/**
	 * Gets the trigger breach distance percentage max.
	 *
	 * @return the trigger breach distance percentage max
	 */
	public String getTriggerBreachDistancePercentageMax() {
		return triggerBreachDistancePercentageMax;
	}


	/**
	 * Sets the trigger breach distance percentage max.
	 *
	 * @param triggerBreachDistancePercentageMax the new trigger breach distance percentage max
	 */
	public void setTriggerBreachDistancePercentageMax(String triggerBreachDistancePercentageMax) {
		this.triggerBreachDistancePercentageMax = triggerBreachDistancePercentageMax;
	}

	/**
	 * Gets the type.
	 *
	 * @return the type
	 */
	public String getType() {
		return type;
	}

	/**
	 * Sets the type.
	 *
	 * @param type the new type
	 */
	public void setType(String type) {
		this.type = type;
	}

	/**
	 * Gets the direction.
	 *
	 * @return the direction
	 */
	public String getDirection() {
		return direction;
	}

	/**
	 * Sets the direction.
	 *
	 * @param direction the new direction
	 */
	public void setDirection(String direction) {
		this.direction = direction;
	}

	/**
	 * Gets the reset level.
	 *
	 * @return the reset level
	 */
	public String getResetLevel() {
		return resetLevel;
	}

	/**
	 * Sets the reset level.
	 *
	 * @param resetLevel the new reset level
	 */
	public void setResetLevel(String resetLevel) {
		this.resetLevel = resetLevel;
	}

	/**
	 * Gets the minimal num of v ms.
	 *
	 * @return the minimal num of v ms
	 */
	public String getMinimalNumOfVMs() {
		return minimalNumOfVMs;
	}

	/**
	 * Sets the minimal num of v ms.
	 *
	 * @param minimalNumOfVMs the new minimal num of v ms
	 */
	public void setMinimalNumOfVMs(String minimalNumOfVMs) {
		this.minimalNumOfVMs = minimalNumOfVMs;
	}
	
}
