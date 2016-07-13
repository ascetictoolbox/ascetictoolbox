package eu.ascetic.saas.applicationpackager.xml.model;

import java.util.ArrayList;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

// TODO: Auto-generated Javadoc
/**
 * The Class ApplicationSlaInfo.
 */
@XmlRootElement(name="applicationSLAInfo")
@XmlAccessorType(XmlAccessType.FIELD)
public class ApplicationSlaInfo {
	
	/** The application sla target. */
	@XmlElement(name="SLATarget")
    private ArrayList<ApplicationSlaTarget> applicationSlaTargets;

	/**
	 * Gets the application sla target.
	 *
	 * @return the application sla target
	 */
	public ArrayList<ApplicationSlaTarget> getApplicationSlaTarget() {
		return applicationSlaTargets;
	}

	/**
	 * Sets the application sla target.
	 *
	 * @param applicationSlaTarget the new application sla target
	 */
	public void setApplicationSlaTarget(
			ArrayList<ApplicationSlaTarget> applicationSlaTargets) {
		this.applicationSlaTargets = applicationSlaTargets;
	}

	
}
