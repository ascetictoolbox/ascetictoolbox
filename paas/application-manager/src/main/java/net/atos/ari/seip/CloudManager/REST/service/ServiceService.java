package net.atos.ari.seip.CloudManager.REST.service;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.List;

import net.atos.ari.seip.CloudManager.db.DAO.InstanceInfoDAO;
import net.atos.ari.seip.CloudManager.db.DAO.ServiceInfoDAO;
import net.atos.ari.seip.CloudManager.manager.CloudComputingManager;

import org.apache.log4j.Logger;

import eu.optimis.manifest.api.sp.Manifest;
import eu.optimis.manifest.api.sp.VirtualMachineComponent;


public class ServiceService {
	
	Logger log = Logger.getLogger(this.getClass().getName());

	String baseurl;
	String secret;
	CloudComputingManager ccManager = null;

	public ServiceService(String secret, String baseurl) {
		super();
		this.baseurl = baseurl;
		this.secret = secret;
	}
	
	private CloudComputingManager getCloudComputerManager(){
		if (ccManager == null){
			ccManager = new CloudComputingManager(secret, baseurl);
		}
		return ccManager;
	}

	/**
	 * Calls the servicemanifest api to parse a manifest file
	 * 
	 * @param manifestFile
	 *            File containing the manifest
	 */
	public Manifest parseSPServiceManifest(File manifestFile) 
	{
		String currOperation = "Parsing Manifest from File:" + manifestFile;
		try
		{
			log.debug(currOperation);
			/*
			XmlBeanServiceManifestDocument doc = XmlBeanServiceManifestDocument.Factory.parse(manifestFile);
			Manifest manifest = Manifest.Factory.newInstance(doc);
			*/
			InputStream file = new FileInputStream(manifestFile);
			byte[] b = new byte[file.available()];
			file.read(b);
			file.close();
			String manifestXML = new String(b);
			Manifest manifest = Manifest.Factory.newInstance(manifestXML);
			return manifest;
		}
		catch (Exception e)
		{
			log.error(currOperation+" FAILED.");
			e.printStackTrace();
			return null;
		}
	}
	
	/**
	 * Calls the servicemanifest api to parse a SP manifest in string format
	 * 
	 * @param manifestXML
	 *            Manifest contents as an xml string
	 */
	public Manifest parseSPServiceManifest(String manifestXML)
	{
		String currOperation = "Parsing SP Manifest from String."; 
		try
		{
			log.debug(currOperation);
			/*
			XmlBeanServiceManifestDocument doc = XmlBeanServiceManifestDocument.Factory.parse(manifestXML);
			Manifest manifest = Manifest.Factory.newInstance(doc);
			*/
			Manifest manifest = Manifest.Factory.newInstance(manifestXML);
			return manifest;
		}
		catch (Exception e)
		{
			log.error(currOperation+" FAILED.");
			e.printStackTrace();
			return null;
		}
	}
	
	/**
	 * Calls the InstanceActions methods to deploy the manifest
	 * 
	 * @param manifest
	 *            Manifest contents as a Manifest object
	 */	
	public String DeployService(Manifest mani) {
		
		//Read Manifest and create each instances as defined
		String vmID;
		String serviceID;
		String image;
		String name;
		int cpu;
		int vcpu;
		int mem;
		String network;
		
		// Read manifest values for each VirtualMachineComponent
		VirtualMachineComponent[] vcomp = mani.getVirtualMachineDescriptionSection().getVirtualMachineComponentArray();
		// Number
		int imagecount = vcomp.length;
		System.out.println("Count "+ imagecount + "\nmanifestXML" + vcomp[1].toString());
		
		
		serviceID = mani.getVirtualMachineDescriptionSection().getServiceId();
		
		//Insert Service register BBDD
		ServiceInfoDAO serv = new ServiceInfoDAO();
		//FIXME Manifest too long (manifest data structure need to be changed)
		//String serviceManifest = mani.toString();
		String serviceManifest = "manifestContent too long";
		try {
			serv.addService(serviceID, serviceManifest);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
				
		
		// Deploy
		log.debug("\nServiceID: " + serviceID + "\n");
		for (VirtualMachineComponent vmconfig : vcomp)
		{
			// Prepare Template
			image = vmconfig.getOVFDefinition().getReferences().getImageFile().getHref();
			name = vmconfig.getOVFDefinition().getReferences().getImageFile().getId();
			cpu= vmconfig.getOVFDefinition().getVirtualSystem().getVirtualHardwareSection().getNumberOfVirtualCPUs();
			vcpu = vmconfig.getOVFDefinition().getVirtualSystem().getVirtualHardwareSection().getNumberOfVirtualCPUs();
			mem = vmconfig.getOVFDefinition().getVirtualSystem().getVirtualHardwareSection().getMemorySize();
			network = "PSNC";
			
			// Create Template
			String template = getCloudComputerManager().createTemplate(name, image, cpu, vcpu, mem, network);

			// Create each instance as defined
			//log.debug(" \n__O____" + "\nSize" + imagecount + "\n" + "\nImage:\n"+ image + "\nname:\n"+ name + "\nCPU:\n"+ cpu + "\nVCPU:\n"+ vcpu + "\nMem:\n"+ mem + "\nNetwork:\n"+ network);
			vmID = getCloudComputerManager().createInstance(template);
			
			// Insert Instance register BBDD	
			InstanceInfoDAO ins = new InstanceInfoDAO();
						try {
				ins.addInstance(vmID, serviceID);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		
		
		return serviceID;		
	}
	
	/**
	 * Calls the InstanceActions methods to undeploy the manifest
	 * 
	 * @param manifest
	 *            ID of the service
	 */	
	public String UndeployService(String serviceID){
		
	    InstanceInfoDAO dao = new InstanceInfoDAO();
	    ServiceInfoDAO sdao = new ServiceInfoDAO();
	    int idserviceInfo = 0;
	    String idserviceInfoS;
	    
	    // Get idservice_info (id of service_info table)
	    try {
			idserviceInfo = sdao.getService(serviceID).getIdserviceInfo();
		} catch (Exception e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
			log.error("ERROR: Getting service");
		}
	    
	    // Get instances by idservice_info
	    try {
	    	idserviceInfoS = Integer.toString(idserviceInfo);
			List<String> list = dao.getInstancesByServiceID(idserviceInfoS);
			log.debug("ServiceID: " + serviceID + "\n");
			for (String vm : list)
			{
				log.debug("VM: " + vm + "\n");
				dao.deleteInstance(vm);
				getCloudComputerManager().deleteInstance(Integer.parseInt(vm));
			}	
		} catch (Exception e) {
			// TODO Auto-generated catch block
			log.error("ERROR: Getting and deleting Instance");
			e.printStackTrace();
			
		}
	    
	    try {
	    	
			sdao.deleteService(serviceID);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			log.error("ERROR: Getting and deleting Instance");			
			e.printStackTrace();
		}
	    
		return "Service Undeployed";
	}
	
	/**
	 * Calls the ServiceInfoDAO methods to obtain the Service list
	 * 
	 * @param void
	 *            
	 */	
	public List<ServiceService> listService(){
		return null;
		
	}
	
	
}
