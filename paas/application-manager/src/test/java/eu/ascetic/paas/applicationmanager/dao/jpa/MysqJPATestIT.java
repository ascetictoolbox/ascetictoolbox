package eu.ascetic.paas.applicationmanager.dao.jpa;

import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import eu.ascetic.paas.applicationmanager.dao.ApplicationDAO;
//import eu.ascetic.paas.applicationmanager.dao.DeploymentDAO;
import eu.ascetic.paas.applicationmanager.dao.ImageDAO;
//import eu.ascetic.paas.applicationmanager.dao.VMDAO;
import eu.ascetic.paas.applicationmanager.model.Application;
import eu.ascetic.paas.applicationmanager.model.Image;


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
 */

public class MysqJPATestIT {
	
//	public static void main(String args[]) throws InterruptedException {
//		// Load Spring configuration
//		@SuppressWarnings("resource")
//		ApplicationContext context = new ClassPathXmlApplicationContext("/mysql-jpa-test-configuration.xml");
//		ApplicationDAO applicationDAO = (ApplicationDAO) context.getBean("ApplicationService");
//		DeploymentDAO deploymentDAO = (DeploymentDAO) context.getBean("DeploymentService");
//		VMDAO vmDAO = (VMDAO) context.getBean("VMService");
//		
//		int size = applicationDAO.getAll().size();
//		
//		System.out.println("Number of Applications in DB at Start: " + size);
//		
//		// We create an application
//		Application application = new Application();
//		application.setName("name");
//		
//		// We store the application
//		boolean saved = applicationDAO.save(application);
//		Application applicationFromDatabae = applicationDAO.getById(size+1);
//		size = applicationDAO.getAll().size();
//	
//		
//		System.out.println("Number of Applications in DB after storing an application: " + size);
//		
//		if(saved == false) {
//			System.out.println("IMPOSIBLE TO SAVE EXPERIMENT!!!!");
//			Thread.sleep(600000l);
//		}
//		
//		size = deploymentDAO.getAll().size();
//		
//		System.out.println("Number of Deployments in DB at Start: " + size);
//		
//		// We store a deployment
//		Deployment deployment = new Deployment();
//		deployment.setStatus("RUNNIG");
//		deployment.setPrice("expensive");
//		
//		saved = deploymentDAO.save(deployment);
//		
//		if(saved == false) {
//			System.out.println("IMPOSIBLE TO SAVE DEPLOYMENT!!!!");
//			Thread.sleep(600000l);
//		}
//		
//		Deployment deploymentFrDeployment = deploymentDAO.getById(size+1);
//		size = deploymentDAO.getAll().size();
//		
//		System.out.println("Number of Deployments in DB after storing one: " + size);
//		
//		size = vmDAO.getAll().size();
//		
//		System.out.println("Number of VMs in DB at Start: " + size);
//		
//		VM vm = new VM();
//		vm.setIp("127.0.0.1");
//		vm.setOvfId("ovf-id");
//		vm.setProviderId("provider-id");
//		vm.setProviderVmId("provider-vm-id");
//		vm.setSlaAgreement("sla-agreement");
//		vm.setStatus("RUNNING");
//		
//		Image image = new Image();
//		image.setProviderImageId("provider-image-id");
//		image.setOvfId("ovf-id");
//		
//		vm.addImage(image);
//		
//		saved = vmDAO.save(vm);
//		
//		if(saved == false) {
//			System.out.println("IMPOSIBLE TO SAVE VM!!!!");
//			Thread.sleep(600000l);
//		}
//		
//		VM vmFromDatabase = vmDAO.getById(size+1);
//		
//		size = vmDAO.getAll().size();
//		
//		System.out.println("###### Number of VMs in DB after storing one: " + size);
//		System.out.println("###### New Image stored: id -> " + vmFromDatabase.getImages().get(0).getId()
//										 + ", provider-id -> " + vmFromDatabase.getImages().get(0).getProviderImageId()
//										 + ", ovf-id -> " + vmFromDatabase.getImages().get(0).getOvfId());
//		
//		vmFromDatabase = vmDAO.getById(vmFromDatabase.getId());
//		Image imageFromDatabase = vmFromDatabase.getImages().get(0);
//		imageFromDatabase.setOvfId("ooo");
//		vmDAO.update(vmFromDatabase);
//		
//		vmFromDatabase = vmDAO.getById(vmFromDatabase.getId());
//		System.out.println("###### New Image stored: id -> " + vmFromDatabase.getImages().get(0).getId()
//				 + ", provider-id -> " + vmFromDatabase.getImages().get(0).getProviderImageId()
//				 + ", ovf-id -> " + vmFromDatabase.getImages().get(0).getOvfId());
//		
//		applicationFromDatabae = applicationDAO.getById(applicationFromDatabae.getId());
//		deploymentFrDeployment = deploymentDAO.getById(deploymentFrDeployment.getId());
//		vmFromDatabase = vmDAO.getById(vmFromDatabase.getId());
//		
//		applicationFromDatabae.addDeployment(deploymentFrDeployment);
//		deploymentFrDeployment.addVM(vmFromDatabase);
//		
//		boolean updated = applicationDAO.update(applicationFromDatabae);
//		
//		if(updated == false) {
//			System.out.println("IMPOSIBLE TO UPDATE APPLICATION!!!!");
//			Thread.sleep(600000l);
//		}
//		
//		boolean deleted = applicationDAO.delete(applicationFromDatabae);
//		
//		if(deleted == false) {
//			System.out.println("IMPOSIBLE TO DELETE APPLICATION!!!!");
//			Thread.sleep(600000l);
//		}
//	}
	
	public static void main(String args[]) throws InterruptedException {
		// Load Spring configuration
		@SuppressWarnings("resource")
		ApplicationContext context = new ClassPathXmlApplicationContext("/mysql-jpa-test-configuration.xml");
		ApplicationDAO applicationDAO = (ApplicationDAO) context.getBean("ApplicationService");
//		DeploymentDAO deploymentDAO = (DeploymentDAO) context.getBean("DeploymentService");
//		VMDAO vmDAO = (VMDAO) context.getBean("VMService");
		ImageDAO imageDAO = (ImageDAO) context.getBean("ImageService");
		
		int size = imageDAO.getAll().size();
		Application application = new Application();
		application.setName("pepito");
		applicationDAO.save(application);
		application = applicationDAO.getById(size+1);
		
		
		Image image = new Image();
		image.setProviderImageId("provider-image-id");
		image.setOvfId("ovf-id");
		
		imageDAO.save(image);
		application.addImage(image);
		
		applicationDAO.update(application);
		
		application = applicationDAO.getById(application.getId());
		
		System.out.println("### of images " + application.getImages().size());		
		
		Image image2 = new Image();
		image2.setProviderImageId("provider-image-id");
		image2.setOvfId("ovf-id");
		
		imageDAO.save(image2);
		application.addImage(image2);
		
		applicationDAO.update(application);
		
		application = applicationDAO.getById(application.getId());
		
		System.out.println("### of images " + application.getImages().size());	
	}
	
//	public static void main(String args[]) throws InterruptedException {
//		// Load Spring configuration
//		@SuppressWarnings("resource")
//		ApplicationContext context = new ClassPathXmlApplicationContext("/mysql-jpa-test-configuration.xml");
//		ApplicationDAO applicationDAO = (ApplicationDAO) context.getBean("ApplicationService");
//		DeploymentDAO deploymentDAO = (DeploymentDAO) context.getBean("DeploymentService");
//		VMDAO vmDAO = (VMDAO) context.getBean("VMService");
//		ImageDAO imageDAO = (ImageDAO) context.getBean("ImageService");
//		
//		int size = imageDAO.getAll().size();
//		Image image = new Image();
//		image.setProviderImageId("provider-image-id");
//		image.setOvfId("ovf-id");
//		
//		imageDAO.save(image);
//		image = imageDAO.getById(size+1);
//		
//		size = applicationDAO.getAll().size();
//		
//		System.out.println("Number of Applications in DB at Start: " + size);
//		
//		// We create an application
//		Application application = new Application();
//		application.setName("name");
//		
//		// We store the application
//		boolean saved = applicationDAO.save(application);
//		Application applicationFromDatabae = applicationDAO.getById(size+1);
//		size = applicationDAO.getAll().size();
//	
//		
//		System.out.println("Number of Applications in DB after storing an application: " + size);
//		
//		if(saved == false) {
//			System.out.println("IMPOSIBLE TO SAVE EXPERIMENT!!!!");
//			Thread.sleep(600000l);
//		}
//		
//		size = deploymentDAO.getAll().size();
//		
//		System.out.println("Number of Deployments in DB at Start: " + size);
//		
//		// We store a deployment
//		Deployment deployment = new Deployment();
//		deployment.setStatus("RUNNIG");
//		deployment.setPrice("expensive");
//		
//		saved = deploymentDAO.save(deployment);
//		
//		if(saved == false) {
//			System.out.println("IMPOSIBLE TO SAVE DEPLOYMENT!!!!");
//			Thread.sleep(600000l);
//		}
//		
//		Deployment deploymentFrDeployment = deploymentDAO.getById(size+1);
//		size = deploymentDAO.getAll().size();
//		
//		System.out.println("Number of Deployments in DB after storing one: " + size);
//		
//		size = vmDAO.getAll().size();
//		
//		System.out.println("Number of VMs in DB at Start: " + size);
//		
//		VM vm = new VM();
//		vm.setIp("127.0.0.1");
//		vm.setOvfId("ovf-id");
//		vm.setProviderId("provider-id");
//		vm.setProviderVmId("provider-vm-id");
//		vm.setSlaAgreement("sla-agreement");
//		vm.setStatus("RUNNING");
//		vmDAO.save(vm);
//		vm.addImage(image);
//		vmDAO.update(vm);
//		
//		
//		
//		System.out.println(" ID deployment: " + deploymentFrDeployment);
//		
//		deploymentFrDeployment.addVM(vm);
//		deploymentDAO.update(deploymentFrDeployment);
//		Deployment deploymentFrDeployment2 = deploymentDAO.getById(deploymentFrDeployment.getId());
//		System.out.println("Number of VMs in deployment: " + deploymentFrDeployment.getVms().size());
//		System.out.println("### Number of VMs in deployment: " + deploymentFrDeployment2.getVms().size());
//		
//		System.out.println("We create second VM");
//		
//		//deploymentFrDeployment = deploymentFrDeployment2;
//		
//		VM vm2 = new VM();
//		vm2.setIp("127.0.0.1");
//		vm2.setOvfId("ovf-id");
//		vm2.setProviderId("provider-id");
//		vm2.setProviderVmId("provider-vm-id");
//		vm2.setSlaAgreement("sla-agreement");
//		vm2.setStatus("RUNNING");
//		vmDAO.save(vm2);
//		vm2.addImage(image);
//		vmDAO.update(vm2);
//		
//		
//		
//		deploymentFrDeployment.addVM(vm2);
//		deploymentDAO.update(deploymentFrDeployment);
//		System.out.println("Number of VMs in deployment: " + deploymentFrDeployment.getVms().size());
//		deploymentFrDeployment2 = deploymentDAO.getById(deploymentFrDeployment.getId());
//		System.out.println("Number of VMs in deployment: " + deploymentFrDeployment.getVms().size());
//		System.out.println("### Number of VMs in deployment: " + deploymentFrDeployment2.getVms().size());
//		
//		//deploymentFrDeployment = deploymentFrDeployment2;
//		
//		VM vm3 = new VM();
//		vm3.setIp("127.0.0.1");
//		vm3.setOvfId("ovf-id");
//		vm3.setProviderId("provider-id");
//		vm3.setProviderVmId("provider-vm-id");
//		vm3.setSlaAgreement("sla-agreement");
//		
//		vm3.setStatus("RUNNING");
//		vmDAO.save(vm3);
//		vm3.addImage(image);
//		vmDAO.update(vm3);
//		
//		
//		deploymentFrDeployment.addVM(vm3);
//		deploymentDAO.update(deploymentFrDeployment);
//		System.out.println("Number of VMs in deployment: " + deploymentFrDeployment.getVms().size());
//	    deploymentFrDeployment2 = deploymentDAO.getById(deploymentFrDeployment.getId());
//		System.out.println("Number of VMs in deployment: " + deploymentFrDeployment.getVms().size());
//		System.out.println("### Number of VMs in deployment: " + deploymentFrDeployment2.getVms().size());
//		
//	}
}

