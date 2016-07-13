package eu.ascetic.saas.applicationpackager.xml.model;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;

// TODO: Auto-generated Javadoc
/**
 * The Class AdaptationSlaTarget.
 */
@XmlRootElement(name="SLATarget")
@XmlAccessorType(XmlAccessType.FIELD)
public class AdaptationSlaTarget {

	/** The sla term. */
	@XmlAttribute(name="SLATerm")
	private String slaTerm;

	/** The application event. */
	@XmlAttribute(name="applicationEvent")
	private String applicationEvent;
	
	
	/** The application metric. */
	@XmlAttribute(name="applicationMetric")
	private String applicationMetric;
	
	
	/** The period. */
	@XmlAttribute(name="period")
	private String period;
	
	
	/** The aggerator. */
	@XmlAttribute(name="aggregator")
	private String aggregator;
	
	
	/** The aggregator params. */
	@XmlAttribute(name="aggregatorParams")
	private String aggregatorParams;
	
	
	/** The comparator. */
	@XmlAttribute(name="comparator")
	private String comparator;
	
	/** The boundary value. */
	@XmlAttribute(name="boundaryValue")
	private String boundaryValue;
	
	
	/** The SLA type. */
	@XmlAttribute(name="SLAType")
	private String SLAType;


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
	 * Gets the application event.
	 *
	 * @return the application event
	 */
	public String getApplicationEvent() {
		return applicationEvent;
	}


	/**
	 * Sets the application event.
	 *
	 * @param applicationEvent the new application event
	 */
	public void setApplicationEvent(String applicationEvent) {
		this.applicationEvent = applicationEvent;
	}


	/**
	 * Gets the application metric.
	 *
	 * @return the application metric
	 */
	public String getApplicationMetric() {
		return applicationMetric;
	}


	/**
	 * Sets the application metric.
	 *
	 * @param applicationMetric the new application metric
	 */
	public void setApplicationMetric(String applicationMetric) {
		this.applicationMetric = applicationMetric;
	}


	/**
	 * Gets the period.
	 *
	 * @return the period
	 */
	public String getPeriod() {
		return period;
	}


	/**
	 * Sets the period.
	 *
	 * @param period the new period
	 */
	public void setPeriod(String period) {
		this.period = period;
	}



	/**
	 * Gets the aggregator.
	 *
	 * @return the aggregator
	 */
	public String getAggregator() {
		return aggregator;
	}



	/**
	 * Sets the aggregator.
	 *
	 * @param aggregator the new aggregator
	 */
	public void setAggregator(String aggregator) {
		this.aggregator = aggregator;
	}


	/**
	 * Gets the aggregator params.
	 *
	 * @return the aggregator params
	 */
	public String getAggregatorParams() {
		return aggregatorParams;
	}


	/**
	 * Sets the aggregator params.
	 *
	 * @param aggregatorParams the new aggregator params
	 */
	public void setAggregatorParams(String aggregatorParams) {
		this.aggregatorParams = aggregatorParams;
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
	 * Gets the SLA type.
	 *
	 * @return the SLA type
	 */
	public String getSLAType() {
		return SLAType;
	}


	/**
	 * Sets the SLA type.
	 *
	 * @param sLAType the new SLA type
	 */
	public void setSLAType(String sLAType) {
		SLAType = sLAType;
	}
	
	
}
