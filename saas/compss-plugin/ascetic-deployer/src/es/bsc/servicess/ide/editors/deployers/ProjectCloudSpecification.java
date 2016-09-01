package es.bsc.servicess.ide.editors.deployers;

import integratedtoolkit.types.project.ProjectFile;
import integratedtoolkit.types.project.exceptions.InvalidElementException;
import integratedtoolkit.types.project.exceptions.ProjectFileValidationException;
import integratedtoolkit.types.project.jaxb.CloudProviderType;
import integratedtoolkit.types.project.jaxb.CloudType;
import integratedtoolkit.types.project.jaxb.ImageType;
import integratedtoolkit.types.project.jaxb.InstanceTypeType;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.JAXBException;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;

import org.apache.logging.log4j.LogManager;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.runtime.CoreException;
import org.xml.sax.SAXException;

import es.bsc.servicess.ide.ProjectMetadata;
import static es.bsc.servicess.ide.Constants.*;
import static es.bsc.servicess.ide.editors.deployers.ImageCreation.*;

public class ProjectCloudSpecification {
	
	/**
	 * Add cloud provider description in the project configuration file
	 * 
	 * @param packs Names of the service packages
	 * @param manifest 
	 * @param properties 
	 * @throws ParserConfigurationException
	 * @throws SAXException
	 * @throws IOException
	 * @throws CoreException
	 * @throws TransformerException
	 * @throws JAXBException 
	 * @throws ProjectFileValidationException 
	 */
	public static void addCloudProviderToProject(String[] packs, IFolder packageFolder, 
			ProjectMetadata prMeta, Manifest manifest, AsceticProperties properties)
			throws Exception {
		File prFile = new File(packageFolder.getProject().getLocation()
				+ File.separator + prMeta.getMainPackageFolder() + File.separator+PROJECT_FILENAME);
		String projectXSD = prMeta.getRuntimeLocation()+ COMPSS_RT_XMLS_PATH
				+ COMPSS_PROJECTS_PATH + COMPSS_PROJECT_SCHEMA_NAME;
		org.apache.logging.log4j.Logger logger = LogManager.getLogger(ProjectFile.class);
		ProjectFile project;
		if (prFile.exists()){
			project = new ProjectFile(prFile, projectXSD, logger);
		}else{
			project = new ProjectFile(projectXSD, logger);
		}
		
		addAsceticCloudToProject(project, packs, manifest);
		
		File file = packageFolder.getFile(PROJECT_FILENAME).getRawLocation().toFile();
		if (file.exists()) {
			file.delete();
		}
		project.toFile(file);

	}

	private static void addAsceticCloudToProject(ProjectFile project, String[] packs, Manifest manifest) throws InvalidElementException {
		List<ImageType> images = new ArrayList<ImageType>();
		List<InstanceTypeType> instances = new ArrayList<InstanceTypeType>();
		for (String p : packs) {
			
			ImageType image = ProjectFile.createImage(Manifest.generateManifestImageName(p), IMAGE_DEPLOYMENT_FOLDER, IMAGE_WORKING_FOLDER, ASCETIC_USER, 
					IMAGE_DEPLOYMENT_FOLDER,IMAGE_DEPLOYMENT_FOLDER,IMAGE_DEPLOYMENT_FOLDER, IMAGE_DEPLOYMENT_FOLDER,0,null, null);
			images.add(image);
			InstanceTypeType instance =ProjectFile.createInstance(Manifest.generateManifestName(p));
			instances.add(instance);
		}
		project.addCloudProvider("Ascetic", images, instances, manifest.getVMsToDeploy(true), null);
		
	}
	
	
	


}
