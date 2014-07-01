package eu.ascetic.paas.applicationmanager.model;

import static eu.ascetic.paas.applicationmanager.model.Dictionary.APPLICATION_MANAGER_NAMESPACE;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * Defines the deployment of an Application at PaaS level
 * @author David Garcia Perez - Atos
 */
//XML annotations:
@XmlAccessorType(XmlAccessType.FIELD)
@XmlRootElement(name = "deployment", namespace = APPLICATION_MANAGER_NAMESPACE)
public class Deployment {
	@XmlAttribute
	private String href;
	@XmlElement(name = "id", namespace = APPLICATION_MANAGER_NAMESPACE)
	private int id;
	@XmlElement(name = "status", namespace = APPLICATION_MANAGER_NAMESPACE)
	private String status;
	@XmlElement(name = "deployment-plan-id", namespace = APPLICATION_MANAGER_NAMESPACE)
	private String deploymentPlanId;
	
	public String getDeploymentPlanId() {
		return deploymentPlanId;
	}
	public void setDeploymentPlanId(String deploymentPlanId) {
		this.deploymentPlanId = deploymentPlanId;
	}
	
	public String getStatus() {
		return status;
	}
	public void setStatus(String status) {
		this.status = status;
	}

}
