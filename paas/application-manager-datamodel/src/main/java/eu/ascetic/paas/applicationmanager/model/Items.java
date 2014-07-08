package eu.ascetic.paas.applicationmanager.model;

import static eu.ascetic.paas.applicationmanager.model.Dictionary.APPLICATION_MANAGER_NAMESPACE;

import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * POJO representation of the items inside a Provider Registry Collection
 * @author David Garcia Perez - AtoS
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlRootElement(name = "items", namespace = APPLICATION_MANAGER_NAMESPACE)
public class Items {
	@XmlAttribute
	private int offset;
	@XmlAttribute
	private int total;
	
	@XmlElement(name="application", namespace = APPLICATION_MANAGER_NAMESPACE)
    private List<Application> applications;
	@XmlElement(name="deployment", namespace = APPLICATION_MANAGER_NAMESPACE)
	private List<Deployment> deployments;
	@XmlElement(name="vm", namespace = APPLICATION_MANAGER_NAMESPACE)
	private List<VM> vms;
	@XmlElement(name="agreement", namespace = APPLICATION_MANAGER_NAMESPACE)
	private List<Agreement> agreements;
	
	public int getOffset() {
		return offset;
	}
	public void setOffset(int offset) {
		this.offset = offset;
	}

	public int getTotal() {
		return total;
	}
	public void setTotal(int total) {
		this.total = total;
	}
	

    public List<Application> getApplications() {
		return applications;
	}
	public void setApplications(List<Application> applications) {
		this.applications = applications;
	}
	public void addApplication(Application application) {
		if(applications == null) applications = new ArrayList<Application>();
		applications.add(application);
	}
	
	public List<Deployment> getDeployments() {
		return deployments;
	}
	public void setDeployments(List<Deployment> deployments) {
		this.deployments = deployments;
	}
	public void addDeployment(Deployment deployment) {
		if(deployments == null) deployments = new ArrayList<Deployment>();
		deployments.add(deployment);
	}
	
	public List<VM> getVms() {
		return vms;
	}
	public void setVms(List<VM> vms) {
		this.vms = vms;
	}
	public void addVm(VM vm) {
		if(vms == null) vms = new ArrayList<VM>();
		vms.add(vm);
	}
	
	public List<Agreement> getAgreements() {
		return agreements;
	}
	public void setAgreements(List<Agreement> agreements) {
		this.agreements = agreements;
	}
	public void addAgreement(Agreement agreement) {
		if(agreements == null) agreements = new ArrayList<Agreement>();
		agreements.add(agreement);
	}
}
