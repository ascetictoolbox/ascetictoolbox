package eu.ascetic.saas.applicationpackager.xml.model;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;

// TODO: Auto-generated Javadoc
/**
 * The Class NodeSlaTarget.
 */
@XmlRootElement(name="SLATarget")
@XmlAccessorType(XmlAccessType.FIELD)
public class NodeSlaTarget {

	/** The sla term. */
	@XmlAttribute(name="SLATerm")
	private String slaTerm;
	
	/** The sla metric unit. */
	@XmlAttribute(name="SLAMetricUnit")
	private String slaMetricUnit;
	
	/** The comparator. */
	@XmlAttribute(name="comparator")
	private String comparator;
	
	/** The boundary value. */
	@XmlAttribute(name="boundaryValue")
	private String boundaryValue;
	
	/** The sla type. */
	@XmlAttribute(name="SLAType")
	private String slaType;

	/**
	 * Gets the sla term.
	 *
	 * @return the sla term
	 */
	public String getSlaTerm() {
		return slaTerm;
	}

	/**
	 * Sets the sla term.
	 *
	 * @param slaTerm the new sla term
	 */
	public void setSlaTerm(String slaTerm) {
		this.slaTerm = slaTerm;
	}

	/**
	 * Gets the sla metric unit.
	 *
	 * @return the sla metric unit
	 */
	public String getSlaMetricUnit() {
		return slaMetricUnit;
	}

	/**
	 * Sets the sla metric unit.
	 *
	 * @param slaMetricUnit the new sla metric unit
	 */
	public void setSlaMetricUnit(String slaMetricUnit) {
		this.slaMetricUnit = slaMetricUnit;
	}

	/**
	 * Gets the comparator.
	 *
	 * @return the comparator
	 */
	public String getComparator() {
		return comparator;
	}

	/**
	 * Sets the comparator.
	 *
	 * @param comparator the new comparator
	 */
	public void setComparator(String comparator) {
		this.comparator = comparator;
	}

	/**
	 * Gets the boundary value.
	 *
	 * @return the boundary value
	 */
	public String getBoundaryValue() {
		return boundaryValue;
	}

	/**
	 * Sets the boundary value.
	 *
	 * @param boundaryValue the new boundary value
	 */
	public void setBoundaryValue(String boundaryValue) {
		this.boundaryValue = boundaryValue;
	}

	/**
	 * Gets the sla type.
	 *
	 * @return the sla type
	 */
	public String getSlaType() {
		return slaType;
	}

	/**
	 * Sets the sla type.
	 *
	 * @param slaType the new sla type
	 */
	public void setSlaType(String slaType) {
		this.slaType = slaType;
	}

}
