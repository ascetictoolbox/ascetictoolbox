package eu.ascetic.saas.applicationpackager.xml.model;

import java.util.ArrayList;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

// TODO: Auto-generated Javadoc
/**
 * The Class VmSlaInfo.
 */
@XmlRootElement(name="vmSLAInfo")
@XmlAccessorType(XmlAccessType.FIELD)
public class VmSlaInfo {
	
	/** The application sla target. */
	@XmlElement(name="SLATarget")
    private ArrayList<NodeSlaTarget> nodeSlaTargets;

	/**
	 * Gets the node sla target.
	 *
	 * @return the node sla target
	 */
	public ArrayList<NodeSlaTarget> getNodeSlaTarget() {
		return nodeSlaTargets;
	}

	/**
	 * Sets the node sla target.
	 *
	 * @param nodeSlaTarget the new node sla target
	 */
	public void setNodeSlaTarget(ArrayList<NodeSlaTarget> nodeSlaTargets) {
		this.nodeSlaTargets = nodeSlaTargets;
	}



	
}
