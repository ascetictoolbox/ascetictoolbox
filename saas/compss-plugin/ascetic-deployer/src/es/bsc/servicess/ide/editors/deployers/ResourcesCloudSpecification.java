package es.bsc.servicess.ide.editors.deployers;

import integratedtoolkit.types.resources.ResourcesFile;
import integratedtoolkit.types.resources.jaxb.EndpointType;
import integratedtoolkit.types.resources.jaxb.ImageType;
import integratedtoolkit.types.resources.jaxb.InstanceTypeType;
import integratedtoolkit.types.resources.jaxb.OSTypeType;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.eclipse.core.resources.IFolder;
import org.eclipse.jdt.core.IJavaProject;

import es.bsc.servicess.ide.ProjectMetadata;
import es.bsc.servicess.ide.editors.RuntimeConfigurationSection;
import eu.ascetic.utils.ovf.api.VirtualSystem;
import static es.bsc.servicess.ide.Constants.*;
import static es.bsc.servicess.ide.editors.deployers.ImageCreation.*;

public class ResourcesCloudSpecification {
	/**
	 * Add the cloud provider description to the resources file
	 * 
	 * @param serviceName Name of the implemented service
	 * @param packs Names of the service packages
	 * @param packageFolder 
	 * @param project 
	 * @param prMeta 
	 * @param manifest 
	 * @throws Exception 
	 */
	public static void addCloudProviderToResources(IJavaProject project,String[] packs, IFolder packageFolder, 
			ProjectMetadata prMeta, Manifest manifest, AsceticProperties properties)
			throws Exception {
		String resourcesXSD = prMeta.getRuntimeLocation()+ COMPSS_RT_XMLS_PATH
				+ COMPSS_RESOURCES_PATH + COMPSS_RESOURCE_SCHEMA_NAME;
		File resFile = new File(project.getProject().getLocation()
				+ File.separator + prMeta.getMainPackageFolder() + File.separator+ RESOURCES_FILENAME);
		org.apache.logging.log4j.Logger logger = LogManager.getLogger(ResourcesFile.class);
		ResourcesFile res;
		if (resFile.exists()){
			res = new ResourcesFile(resFile, resourcesXSD, logger);
		}else{
			res = new ResourcesFile( resourcesXSD, logger);
		}
		
		addAsceticCloudToResources(project, res, packs, manifest, properties);
		File file = packageFolder.getFile(RESOURCES_FILENAME).getRawLocation().toFile();
		if (file.exists()) {
			file.delete();
		}
		res.toFile(file);
	}
	
	private static void addAsceticCloudToResources(IJavaProject project, ResourcesFile resources, String[] packs, 
			Manifest manifest,AsceticProperties properties) throws Exception {
		String adaptorName = RuntimeConfigurationSection.getProjectITConfigManager(project).getCommAdaptor();
		List<ImageType> images = new ArrayList<ImageType>();
		List<InstanceTypeType> instances = new ArrayList<InstanceTypeType>();
		for (String p : packs) {
			VirtualSystem vs = manifest.getComponent(Manifest.generateManifestName(p));
			ImageType image;
			if (adaptorName.equalsIgnoreCase(RuntimeConfigurationSection.GAT)){
				image = ResourcesFile.createImage(Manifest.generateManifestImageName(p), adaptorName, RuntimeConfigurationSection.DEFAULT_SUB_SYS_BATCH,
						RuntimeConfigurationSection.DEFAULT_SUB_SYS_QUEUES,	RuntimeConfigurationSection.DEFAULT_SUB_SYS_INTERACTIVE, 
						RuntimeConfigurationSection.DEFAULT_BROKER_ADAPTOR, ASCETIC_USER, OSTypeType.LINUX.value(),null, null);
			}else{
				image = ResourcesFile.createImage(Manifest.generateManifestImageName(p), adaptorName, RuntimeConfigurationSection.DEFAULT_MAX_PORT, 
					RuntimeConfigurationSection.DEFAULT_MIN_PORT, RuntimeConfigurationSection.DEFAULT_EXECUTOR, ASCETIC_USER,OSTypeType.LINUX.value(),null, null);
			}
			images.add(image);
			InstanceTypeType instance = ResourcesFile.createInstance(vs.getName(), vs.getName()+"_P1",vs.getVirtualHardwareSection().getNumberOfVirtualCPUs(),"x86_64", (float) 1000, null,(float)vs.getVirtualHardwareSection().getMemorySize(),null, 10.0f, null);
			instances.add(instance);
		}
		
		EndpointType ep = ResourcesFile.createEndpoint(properties.getDSLocation(),ASCETIC_CONNECTOR, null);
		resources.addCloudProvider("Ascetic", ep, images, instances);
	}

	

	
}
