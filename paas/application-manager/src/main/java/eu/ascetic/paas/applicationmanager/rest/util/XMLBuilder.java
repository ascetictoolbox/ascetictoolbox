package eu.ascetic.paas.applicationmanager.rest.util;

import java.util.List;

import javax.ws.rs.core.MediaType;

import eu.ascetic.paas.applicationmanager.model.Application;
import eu.ascetic.paas.applicationmanager.model.Deployment;
import eu.ascetic.paas.applicationmanager.model.Link;
import eu.ascetic.paas.applicationmanager.model.VM;
import eu.ascetic.paas.applicationmanager.model.converter.ModelConverter;

/**
 * This class prepares objects that are going to be converted to XML to be sent from the REST interface
 * @author David Garcia Perez - Atos
 *
 */
public class XMLBuilder {

	/**
	 * Adds the necessary fields to an Application object to be able to send it as a reply by XML
	 * @param application object that needs the extra fiels
	 * @return the updated object
	 */
	protected static Application addApplicationXMLInfo(Application application) {
		application.setHref("/applications/" + application.getId());
		
		Link linkParent = new Link();
		linkParent.setHref("/applications");
		linkParent.setRel("parent");
		linkParent.setType(MediaType.APPLICATION_XML);
		application.addLink(linkParent);
		
		Link linkSelf = new Link();
		linkSelf.setHref(application.getHref());
		linkSelf.setRel("self");
		linkSelf.setType(MediaType.APPLICATION_XML);
		application.addLink(linkSelf);
		
		Link linkDeployments = new Link();
		linkDeployments.setHref(application.getHref() + "/deployments");
		linkDeployments.setRel("deployments");
		linkDeployments.setType(MediaType.APPLICATION_XML);
		application.addLink(linkDeployments);
		
		// If the Application has Deployments we add the necessary information
		List<Deployment> deployments = application.getDeployments();
		if(deployments != null) {
			for(Deployment deployment : deployments) {
				deployment = addDeploymentXMLInfo(deployment, application.getId());
			}
		}
		
		return application;
	}
	
	/**
	 * Adds the necessary fields to an Application object to be able to send it as a reply by XML
	 * @param application object that needs the extra fiels
	 * @return the XML representation
	 */
	public static String getApplicationXML(Application application) {
		application = addApplicationXMLInfo(application);
		return ModelConverter.objectApplicationToXML(application);
	}

	/**
	 * Adds the necessary fields to a Deployment object to get a fully XML object...
	 * @param deployment object to be converted to XML
	 * @param applicationId Application ID of the associated Deployment Object
	 * @return the deployment object with the extra fields
	 */
	protected static Deployment addDeploymentXMLInfo(Deployment deployment, int applicationId) {
		deployment.setHref("/applications/" + applicationId + "/deployments/" + deployment.getId());
		
		Link linkParent = new Link();
		linkParent.setHref("/applications/" + applicationId + "/deployments");
		linkParent.setRel("parent");
		linkParent.setType(MediaType.APPLICATION_XML);
		deployment.addLink(linkParent);
		
		Link linkSelf = new Link();
		linkSelf.setHref(deployment.getHref());
		linkSelf.setRel("self");
		linkSelf.setType(MediaType.APPLICATION_XML);
		deployment.addLink(linkSelf);
		
		Link linkOVF = new Link();
		linkOVF.setHref(deployment.getHref() + "/ovf");
		linkOVF.setRel("ovf");
		linkOVF.setType(MediaType.APPLICATION_XML);
		deployment.addLink(linkOVF);
		
		Link linkVMs = new Link();
		linkVMs.setHref(deployment.getHref() + "/vms");
		linkVMs.setRel("vms");
		linkVMs.setType(MediaType.APPLICATION_XML);
		deployment.addLink(linkVMs);
		
		List<VM> vms = deployment.getVms();
		if(vms != null) {
			for(VM vm : vms) {
				vm = addVMXMLInfo(vm, applicationId, deployment.getId());
			}
		}
		
		return deployment;
	}

	/**
	 * Adds the necessary fields to a Deployment object to get a fully XML object...
	 * @param deployment object to be converted to XML
	 * @param applicationId Application ID of the associated Deployment Object
	 * @return the deployment XML representation
	 */
	public static String getDeploymentXML(Deployment deployment, int applicationId) {
		deployment = addDeploymentXMLInfo(deployment, applicationId);
		return ModelConverter.objectDeploymentToXML(deployment);
	}

	/**
	 * Adds the necessary fields to a VM object to get a fully XML object representation
	 * @param vm ojbect to be converted to XML
	 * @param applicationId Indentifier of the application to which the VM belongs
	 * @param deploymentId Deployment Id to which the VM belongs
	 * @return the object with the necessary fields
	 */
	protected static VM addVMXMLInfo(VM vm, int applicationId, int deploymentId) {
		vm.setHref("/applications/" + applicationId + "/deployments/" + deploymentId + "/vms/" + vm.getId());
		
		Link linkParent = new Link();
		linkParent.setHref("/applications/" + applicationId + "/deployments/" + deploymentId + "/vms");
		linkParent.setRel("parent");
		linkParent.setType(MediaType.APPLICATION_XML);
		vm.addLink(linkParent);
		
		Link linkSelf = new Link();
		linkSelf.setHref(vm.getHref());
		linkSelf.setRel("self");
		linkSelf.setType(MediaType.APPLICATION_XML);
		vm.addLink(linkSelf);
		
		return vm;
	}

	/**
	 * Adds the necessary fields to a VM object to get a fully XML object representation
	 * @param vm ojbect to be converted to XML
	 * @param applicationId Indentifier of the application to which the VM belongs
	 * @param deploymentId Deployment Id to which the VM belongs
	 * @return vm XML representation
	 */
	public static String getVMXML(VM vm, int applicationId, int deploymentId) {
		vm = addVMXMLInfo(vm, applicationId, deploymentId);
		return ModelConverter.objectVMToXML(vm);
	}
}
