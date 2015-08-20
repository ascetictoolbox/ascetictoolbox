package eu.ascetic.paas.applicationmanager.rest.util;

import java.util.List;

import javax.ws.rs.core.MediaType;

import eu.ascetic.paas.applicationmanager.model.Agreement;
import eu.ascetic.paas.applicationmanager.model.Application;
import eu.ascetic.paas.applicationmanager.model.Collection;
import eu.ascetic.paas.applicationmanager.model.Deployment;
import eu.ascetic.paas.applicationmanager.model.EnergyMeasurement;
import eu.ascetic.paas.applicationmanager.model.EventSample;
import eu.ascetic.paas.applicationmanager.model.Image;
import eu.ascetic.paas.applicationmanager.model.Items;
import eu.ascetic.paas.applicationmanager.model.Link;
import eu.ascetic.paas.applicationmanager.model.PowerMeasurement;
import eu.ascetic.paas.applicationmanager.model.VM;
import eu.ascetic.paas.applicationmanager.model.converter.ModelConverter;

/**
 * 
 * Copyright 2014 ATOS SPAIN S.A. 
 * 
 * Licensed under the Apache License, Version 2.0 (the License);
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * 
 * @author David Garcia Perez. Atos Research and Innovation, Atos SPAIN SA
 * e-mail david.garciaperez@atos.net 
 * 
 * This class prepares objects that are going to be converted to XML to be sent from the REST interface
 *
 */

public class XMLBuilder {

	/**
	 * Adds the necessary fields to an Application object to be able to send it as a reply by XML
	 * @param application object that needs the extra fiels
	 * @return the updated object
	 */
	protected static Application addApplicationXMLInfo(Application application) {
		application.setHref("/applications/" + application.getName());
		
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
		
		Link linkCacheImages = new Link();
		linkCacheImages.setHref(application.getHref() + "/cache-images");
		linkCacheImages.setRel("cache-image");
		linkCacheImages.setType(MediaType.APPLICATION_XML);
		application.addLink(linkCacheImages);
		
		// If the Application has Deployments we add the necessary information
		List<Deployment> deployments = application.getDeployments();
		if(deployments != null) {
			for(Deployment deployment : deployments) {
				deployment = addDeploymentXMLInfo(deployment, application.getName());
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
	 * Adds the necessary fields to an Application object to be able to send it as a reply by JSON
	 * @param application object that needs the extra fiels
	 * @return the XML representation
	 */
	public static String getApplicationJSON(Application application) {
		application = addApplicationXMLInfo(application);
		return ModelConverter.objectApplicationToJSON(application);
	}

	/**
	 * Adds the necessary fields to a Deployment object to get a fully XML object...
	 * @param deployment object to be converted to XML
	 * @param applicationId Application ID of the associated Deployment Object
	 * @return the deployment object with the extra fields
	 */
	protected static Deployment addDeploymentXMLInfo(Deployment deployment, String applicationId) {
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
		
		Link linkEnergyConsumption = new Link();
		linkEnergyConsumption.setHref(deployment.getHref() + "/energy-consumption");
		linkEnergyConsumption.setRel("energy-consumption");
		linkEnergyConsumption.setType(MediaType.APPLICATION_XML);
		deployment.addLink(linkEnergyConsumption);
		
		Link linkAgreements = new Link();
		linkAgreements.setHref(deployment.getHref() + "/agreements");
		linkAgreements.setRel("agreements");
		linkAgreements.setType(MediaType.APPLICATION_XML);
		deployment.addLink(linkAgreements);
		
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
	public static String getDeploymentXML(Deployment deployment, String applicationId) {
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
	protected static VM addVMXMLInfo(VM vm, String applicationId, int deploymentId) {
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
	public static String getVMXML(VM vm, String applicationId, int deploymentId) {
		vm = addVMXMLInfo(vm, applicationId, deploymentId);
		return ModelConverter.objectVMToXML(vm);
	}

	/**
	 * Returns the collection XML representation of a collection of applications
	 * @param applications list of applications to be returned as collection
	 * @return String with the XML representation
	 */
	public static String getCollectionApplicationsXML(List<Application> applications) {
		Collection collection = getCollectionApplications(applications);
		
		return ModelConverter.objectCollectionToXML(collection);
	}
	
	/**
	 * Returns the collection JSON representation of a collection of applications
	 * @param applications list of applications to be returned as collection
	 * @return String with the JSON representation
	 */
	public static String getCollectionApplicationsJSON(List<Application> applications) {
		Collection collection = getCollectionApplications(applications);
		
		return ModelConverter.objectCollectionToJSON(collection);
	}
	
	private static Collection getCollectionApplications(List<Application> applications) {
		Collection collection = new Collection();
		collection.setHref("/applications");
		
		Link linkParent = new Link();
		linkParent.setHref("/");
		linkParent.setRel("parent");
		linkParent.setType(MediaType.APPLICATION_XML);
		collection.addLink(linkParent);
		
		Link linkSelf = new Link();
		linkSelf.setHref(collection.getHref());
		linkSelf.setRel("self");
		linkSelf.setType(MediaType.APPLICATION_XML);
		collection.addLink(linkSelf);
		
		Items items = new Items();
		items.setOffset(0);
		collection.setItems(items);
		
		if(applications != null) {
			items.setTotal(applications.size());
			
			for(Application application : applications) {
				application = addApplicationXMLInfo(application);
				items.addApplication(application);
			}
		} else {
			items.setTotal(0);
		}
		
		return collection;
	}
	
	/**
	 * Adds the necessary XML information to a collection of cache image and generates the XML
	 * @param images List of images to add the information
	 * @param applicationName name of the application to add the information
	 * @return the XML string
	 */
	public static String getCollectionOfCacheImagesXML(List<Image> images, String applicationName) {
		Collection collection = new Collection();
		collection.setHref("/applications/" + applicationName + "/cache-images");
		
		Link linkParent = new Link();
		linkParent.setHref("/applications/" + applicationName);
		linkParent.setRel("parent");
		linkParent.setType(MediaType.APPLICATION_XML);
		collection.addLink(linkParent);
		
		Link linkSelf = new Link();
		linkSelf.setHref(collection.getHref());
		linkSelf.setRel("self");
		linkSelf.setType(MediaType.APPLICATION_XML);
		collection.addLink(linkSelf);
		
		Items items = new Items();
		items.setOffset(0);
		collection.setItems(items);
		
		if(images != null) {
			items.setTotal(images.size());
			
			for(Image image : images) {
				image.setHref(collection.getHref() + "/" + image.getId());
				items.addImage(image);
			}
		}
		
		return ModelConverter.objectCollectionToXML(collection);
	}
	
	/**
	 * Adds all the XML information for creating a collection of Deployments
	 * @param deployments List containing all the deployments for an application
	 * @param applicationId from which the deployments belong
	 * @return the XML representation of tha list of deployments
	 */
	public static String getCollectionOfDeploymentsXML(List<Deployment> deployments, String applicationId) {
		Collection collection = new Collection();
		collection.setHref("/applications/" + applicationId + "/deployments");
		
		Link linkParent = new Link();
		linkParent.setHref("/applications/" + applicationId);
		linkParent.setRel("parent");
		linkParent.setType(MediaType.APPLICATION_XML);
		collection.addLink(linkParent);
		
		Link linkSelf = new Link();
		linkSelf.setHref(collection.getHref());
		linkSelf.setRel("self");
		linkSelf.setType(MediaType.APPLICATION_XML);
		collection.addLink(linkSelf);
		
		Items items = new Items();
		items.setOffset(0);
		collection.setItems(items);
		
		if(deployments != null) {
			items.setTotal(deployments.size());
			
			for(Deployment deployment : deployments) {
				deployment = addDeploymentXMLInfo(deployment, applicationId);
				items.addDeployment(deployment);
			}
		}
		
		return ModelConverter.objectCollectionToXML(collection);
	}

	/**
	 * Adds the necessary fields to build the XML of an Energy Measurement aggregated for all the VMs of an Application
	 * @param energyMeasurement the object to be updated
	 * @param applicationId application id from which the calculation is made
	 * @param deploymentId from which the calculation is made
	 * @return the updated object with all its XML fields
	 */
	public static EnergyMeasurement addEnergyMeasurementForDeploymentXMLInfo(
			EnergyMeasurement energyMeasurement, String applicationId, String deploymentId, String href) {
		
		energyMeasurement.setDescription("Aggregated energy consumption in Wh for this aplication deployment");
		energyMeasurement.setHref("/applications/" + applicationId + "/deployments/" + deploymentId + "/" + href);
		
		Link linkParent = new Link();
		linkParent.setHref("/applications/" + applicationId + "/deployments/" + deploymentId);
		linkParent.setRel("parent");
		linkParent.setType(MediaType.APPLICATION_XML);
		energyMeasurement.addLink(linkParent);
		
		Link linkSelf = new Link();
		linkSelf.setHref(energyMeasurement.getHref());
		linkSelf.setRel("self");
		linkSelf.setType(MediaType.APPLICATION_XML);
		energyMeasurement.addLink(linkSelf);
		
		return energyMeasurement;
	}
	
	public static String getEnergyMeasurementForDeploymentXMLInfo(EnergyMeasurement energyMeasurement, String applicationId, String deploymentId) {
		energyMeasurement = XMLBuilder.addEnergyMeasurementForDeploymentXMLInfo(energyMeasurement, applicationId, deploymentId, "energy-consumption");
		
		return ModelConverter.objectEnergyMeasurementToXML(energyMeasurement);
	}
	
	/**
	 * Adds the necessary fields to build the XML of an Power Measurement aggregated for all the VMs of an Application
	 * @param powerMeasurement the object to be updated
	 * @param applicationId application id from which the calculation is made
	 * @param deploymentId from which the calculation is made
	 * @return the updated object with all its XML fields
	 */
	public static PowerMeasurement addPowerMeasurementForDeploymentXMLInfo(
			PowerMeasurement powerMeasurement, String applicationId, String deploymentId, String href) {
		
		powerMeasurement.setDescription("Aggregated power consumption in W for this aplication deployment");
		powerMeasurement.setHref("/applications/" + applicationId + "/deployments/" + deploymentId + "/" + href);
		
		Link linkParent = new Link();
		linkParent.setHref("/applications/" + applicationId + "/deployments/" + deploymentId);
		linkParent.setRel("parent");
		linkParent.setType(MediaType.APPLICATION_XML);
		powerMeasurement.addLink(linkParent);
		
		Link linkSelf = new Link();
		linkSelf.setHref(powerMeasurement.getHref());
		linkSelf.setRel("self");
		linkSelf.setType(MediaType.APPLICATION_XML);
		powerMeasurement.addLink(linkSelf);
		
		return powerMeasurement;
	}
	
	public static String getPowerMeasurementForDeploymentXMLInfo(PowerMeasurement powerMeasurement, String applicationId, String deploymentId) {
		powerMeasurement = XMLBuilder.addPowerMeasurementForDeploymentXMLInfo(powerMeasurement, applicationId, deploymentId, "energy-consumption");
		
		return ModelConverter.objectPowerMeasurementToXML(powerMeasurement);
	}
	
	public static String getEnergyEstimationForAnEventInAVMXMLInfo(
			EnergyMeasurement energyMeasurement, String applicationId, String deploymentId, String vmId, String eventId) {
		
		energyMeasurement = XMLBuilder.addEnergyEstimationForAnEventInAVMXMLInfo(energyMeasurement, applicationId, deploymentId, vmId, eventId);
		
		return ModelConverter.objectEnergyMeasurementToXML(energyMeasurement);
	}
	
	public static String getEnergyConsumptionForAnEventInAVMXMLInfo(
			EnergyMeasurement energyMeasurement, String applicationId, String deploymentId, String vmId, String eventId) {
		
		energyMeasurement = XMLBuilder.addEnergyConsumptionForAnEventInAVMXMLInfo(energyMeasurement, applicationId, deploymentId, vmId, eventId);
		
		return ModelConverter.objectEnergyMeasurementToXML(energyMeasurement);
	}
	
	/**
	 * Adds the necessary fields to build the XML of an Energy Estimation aggregated for all the VMs of an Application
	 * @param energyMeasurement the object to be updated
	 * @param applicationId application id from which the calculation is made
	 * @param deploymentId from which the calculation is made
	 * @param vmId Id of the vm where we want to measure the VM
	 * @param eventId Type of event for which we want the energy measurement
	 * @return the updated object with all its XML fields
	 */
	public static EnergyMeasurement addEnergyEstimationForAnEventInAVMXMLInfo(
			EnergyMeasurement energyMeasurement, String applicationId, String deploymentId, String vmId, String eventId) {
		
		return addEnergyConsumptionOrEstimationForAnEventInAVMXMLInfo(energyMeasurement, applicationId, deploymentId, vmId, eventId, "estimation");
	}
	
	/**
	 * Adds the necessary fields to build the XML of an Energy Estimation aggregated for all the VMs of an Application
	 * @param energyMeasurement the object to be updated
	 * @param applicationId application id from which the calculation is made
	 * @param deploymentId from which the calculation is made
	 * @param vmId Id of the vm where we want to measure the VM
	 * @param eventId Type of event for which we want the energy measurement
	 * @return the updated object with all its XML fields
	 */
	public static EnergyMeasurement addEnergyConsumptionForAnEventInAVMXMLInfo(
			EnergyMeasurement energyMeasurement, String applicationId, String deploymentId, String vmId, String eventId) {
		
		return addEnergyConsumptionOrEstimationForAnEventInAVMXMLInfo(energyMeasurement, applicationId, deploymentId, vmId, eventId, "consumption");
	}
	
	private static EnergyMeasurement addEnergyConsumptionOrEstimationForAnEventInAVMXMLInfo(
													EnergyMeasurement energyMeasurement, 
													String applicationId, 
													String deploymentId, 
													String vmId, 
													String eventId,
													String endURL) {
		energyMeasurement.setDescription("Aggregated energy " + endURL + " in Wh for an event in a specific VM");
		energyMeasurement.setHref("/applications/" + applicationId + "/deployments/" + deploymentId + "/vms/" + vmId + "/events/" + eventId + "/energy-" + endURL);
		
		Link linkParent = new Link();
		linkParent.setHref("/applications/" + applicationId + "/deployments/" + deploymentId + "/vms/" + vmId + "/events/" + eventId);
		linkParent.setRel("parent");
		linkParent.setType(MediaType.APPLICATION_XML);
		energyMeasurement.addLink(linkParent);
		
		Link linkSelf = new Link();
		linkSelf.setHref(energyMeasurement.getHref());
		linkSelf.setRel("self");
		linkSelf.setType(MediaType.APPLICATION_XML);
		energyMeasurement.addLink(linkSelf);
		
		return energyMeasurement;
	}
	
	/**
	 * Adds the necessary fields to build the XML of an Energy Estimation aggregated for all the VMs of an Application
	 * @param energyMeasurement the object to be updated
	 * @param applicationId application id from which the calculation is made
	 * @param deploymentId from which the calculation is made
	 * @param eventId Type of event for which we want the energy measurement
	 * @return the updated object with all its XML fields
	 */
	public static EnergyMeasurement addEnergyEstimationForDeploymentXMLInfo(
			EnergyMeasurement energyMeasurement, String applicationId, String deploymentId, String eventId) {
		
		energyMeasurement.setDescription("Aggregated energy estimation for this aplication deployment and specific event");
		energyMeasurement.setHref("/applications/" + applicationId + "/deployments/" + deploymentId + "/events/" + eventId + "/energy-estimation");
		
		Link linkParent = new Link();
		linkParent.setHref("/applications/" + applicationId + "/deployments/" + deploymentId + "/events/" + eventId);
		linkParent.setRel("parent");
		linkParent.setType(MediaType.APPLICATION_XML);
		energyMeasurement.addLink(linkParent);
		
		Link linkSelf = new Link();
		linkSelf.setHref(energyMeasurement.getHref());
		linkSelf.setRel("self");
		linkSelf.setType(MediaType.APPLICATION_XML);
		energyMeasurement.addLink(linkSelf);
		
		return energyMeasurement;
	}
	
	public static String getEnergyEstimationForDeploymentXMLInfo(EnergyMeasurement energyMeasurement, String applicationId, String deploymentId, String eventId) {
		energyMeasurement = XMLBuilder.addEnergyEstimationForDeploymentXMLInfo(energyMeasurement, applicationId, deploymentId, eventId);
		
		return ModelConverter.objectEnergyMeasurementToXML(energyMeasurement);
	}
	
	public static String getEventSampleCollectionXMLInfo(List<EventSample> eventSamples, String applicationId, String deploymentId, String vmId, String eventId) {
		Collection collection = new Collection();
		
		String parentHref = "/applications/" + applicationId + "/deployments/" + deploymentId + "/vms/" + vmId + "/events/" + eventId; 
		collection.setHref(parentHref + "/event-samples");
		
		Link linkParent = new Link();
		linkParent.setHref(parentHref);
		linkParent.setRel("parent");
		linkParent.setType(MediaType.APPLICATION_XML);
		collection.addLink(linkParent);
		Link linkSelf = new Link();
		linkSelf.setHref(collection.getHref());
		linkSelf.setRel("self");
		linkSelf.setType(MediaType.APPLICATION_XML);
		collection.addLink(linkSelf);
		
		Items items = new Items();
		items.setOffset(0);
		items.setTotal(eventSamples.size());
		items.setEventSamples(eventSamples);
		collection.setItems(items);
		
		return ModelConverter.objectCollectionToXML(collection);
	}
	
	public static Agreement addAgreementXMLInfo(Agreement agreement, String applicationId, int deploymentId) {
		String parentHref = "/applications/" + applicationId + "/deployments/" + deploymentId + "/agreements"; 
		
		agreement.setHref(parentHref + "/" + agreement.getId());
		
		Link linkParent = new Link();
		linkParent.setHref(parentHref);
		linkParent.setRel("parent");
		linkParent.setType(MediaType.APPLICATION_XML);
		agreement.addLink(linkParent);
		
		Link linkSelf = new Link();
		linkSelf.setHref(agreement.getHref());
		linkSelf.setRel("self");
		linkSelf.setType(MediaType.APPLICATION_XML);
		agreement.addLink(linkSelf);
		
		return agreement;
	}
	
	public static String getAgreementXML(Agreement agreement, String applicationId, int deploymentId) {
		agreement = addAgreementXMLInfo(agreement, applicationId, deploymentId);
		
		return ModelConverter.objectAgreementToXML(agreement);
	}
	
	public static String getCollectionOfAgreements(List<Agreement> agreements, String applicationId, int deploymentId) {
		Collection collection = new Collection();
		
		String parentHref = "/applications/" + applicationId + "/deployments/" + deploymentId; 
		
		collection.setHref(parentHref  + "/agreements");
		
		Link linkParent = new Link();
		linkParent.setHref(parentHref);
		linkParent.setRel("parent");
		linkParent.setType(MediaType.APPLICATION_XML);
		collection.addLink(linkParent);
		
		Link linkSelf = new Link();
		linkSelf.setHref(collection.getHref());
		linkSelf.setRel("self");
		linkSelf.setType(MediaType.APPLICATION_XML);
		collection.addLink(linkSelf);
		
		Items items = new Items();
		collection.setItems(items);
		
		if(agreements != null) {
			
			items.setOffset(0);
			items.setTotal(agreements.size());
			
			for(Agreement agreement : agreements) {
				agreement = addAgreementXMLInfo(agreement, applicationId, deploymentId);
				items.addAgreement(agreement);
			}
		}
		
		return ModelConverter.objectCollectionToXML(collection);
	}

	public static String getCollectionOfVMs(List<VM> vms, String applicationId, int deploymentId) {
		Collection collection = new Collection();
		
		String parentHref = "/applications/" + applicationId + "/deployments/" + deploymentId; 
		
		collection.setHref(parentHref  + "/vms");
		
		Link linkParent = new Link();
		linkParent.setHref(parentHref);
		linkParent.setRel("parent");
		linkParent.setType(MediaType.APPLICATION_XML);
		collection.addLink(linkParent);
		
		Link linkSelf = new Link();
		linkSelf.setHref(collection.getHref());
		linkSelf.setRel("self");
		linkSelf.setType(MediaType.APPLICATION_XML);
		collection.addLink(linkSelf);
		
		Items items = new Items();
		collection.setItems(items);
		
		if(vms != null) {
			
			items.setOffset(0);
			items.setTotal(vms.size());
			
			for(VM vm : vms) {
				vm = addVMXMLInfo(vm, applicationId, deploymentId);
				items.addVm(vm);
			}
		}
		
		return ModelConverter.objectCollectionToXML(collection);
	}
}
