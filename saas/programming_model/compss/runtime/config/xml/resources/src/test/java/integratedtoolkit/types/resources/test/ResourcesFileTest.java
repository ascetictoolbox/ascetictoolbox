package integratedtoolkit.types.resources.test;

import static org.junit.Assert.*;
import integratedtoolkit.types.resources.ResourcesFile;
import integratedtoolkit.types.resources.exceptions.ResourcesFileValidationException;
import integratedtoolkit.types.resources.jaxb.AdaptorType;
import integratedtoolkit.types.resources.jaxb.CloudProviderType;
import integratedtoolkit.types.resources.jaxb.ComputeNodeType;
import integratedtoolkit.types.resources.jaxb.EndpointType;
import integratedtoolkit.types.resources.jaxb.ImageType;
import integratedtoolkit.types.resources.jaxb.InstanceTypeType;
import integratedtoolkit.types.resources.jaxb.NIOAdaptorProperties;
import integratedtoolkit.types.resources.jaxb.OSTypeType;
import integratedtoolkit.types.resources.jaxb.ObjectFactory;
import integratedtoolkit.types.resources.jaxb.ProcessorPropertyType;
import integratedtoolkit.types.resources.jaxb.ProcessorType;

import java.io.File;
import java.io.IOException;
import java.io.StringWriter;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import javax.xml.XMLConstants;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;

import org.apache.commons.io.FileUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.xml.sax.SAXException;


public class ResourcesFileTest {
	
	// Schema
	private static final String SCHEMA_PATH 			= "resources_schema.xsd";
	
	// Default XMLs
	private static final String DEFAULT_XML_PATH 		= "default_resources.xml";
	private static final String FULL_XML_PATH 			= "examples/full/resources.xml";
	private static final String GRID_XML_PATH 			= "examples/grid/resources.xml";
	private static final String SERVICES_XML_PATH 		= "examples/services/resources.xml";
	private static final String ROCCI_XML_PATH 			= "examples/cloud/rocci/resources.xml";
	private static final String JCLOUDS_GCE_XML_PATH 	= "examples/cloud/jclouds/resources_gce.xml";
	
	// TMP XML file
	private static final String XML_TMP_PATH			= "tmp_resources.xml";
	
	// Error Messages
	private static final String ERROR_SCHEMA	 		= "XSD Schema doesn't exist";
	private static final String ERROR_DEFAULT_XML 		= "Default XML doesn't exist";
	private static final String ERROR_FULL_XML 			= "Full XML doesn't exist";
	private static final String ERROR_GRID_XML 			= "Grid XML doesn't exist";
	private static final String ERROR_SERVICES_XML 		= "Services XML doesn't exist";
	private static final String ERROR_ROCCI_XML 		= "Rocci XML doesn't exist";
	private static final String ERROR_JCLOUDS_XML 		= "JClouds XML doesn't exist";
	
	// Test Logger
	private static final Logger logger = LogManager.getLogger("Console");
	
	@BeforeClass
	public static void beforeClass() throws Exception {
		// Initialize logger
		// No longer needed since log4j2 automatically sets it up on error level
		//BasicConfigurator.configure(); 
		
		// Check existance of all files
		if ( !(new File(SCHEMA_PATH)).exists() ) {
			throw new Exception(ERROR_SCHEMA);
		}
		if ( !(new File(DEFAULT_XML_PATH)).exists() ) {
			throw new Exception(ERROR_DEFAULT_XML);
		}
		if ( !(new File(FULL_XML_PATH)).exists() ) {
			throw new Exception(ERROR_FULL_XML);
		}
		if ( !(new File(GRID_XML_PATH)).exists() ) {
			throw new Exception(ERROR_GRID_XML);
		}
		if ( !(new File(SERVICES_XML_PATH)).exists() ) {
			throw new Exception(ERROR_SERVICES_XML);
		}
		if ( !(new File(ROCCI_XML_PATH)).exists() ) {
			throw new Exception(ERROR_ROCCI_XML);
		}
		if ( !(new File(JCLOUDS_GCE_XML_PATH)).exists() ) {
			throw new Exception(ERROR_JCLOUDS_XML);
		}
		
		// Reset the TMP file if needed
		if ( (new File(XML_TMP_PATH)).exists() ) {
			(new File(XML_TMP_PATH)).delete();
		}
	}
	
	@AfterClass
	public static void afterClass() throws Exception {
		if ( (new File(XML_TMP_PATH)).exists() ) {
			(new File(XML_TMP_PATH)).delete();
		}
	}
	
	
	/* ***************************************************************
	 * CONSTRUCTOR TESTS
	 * ***************************************************************/
	@Test
	public void creation_XMLfile_XSDschema() throws URISyntaxException, JAXBException, SAXException, ResourcesFileValidationException {
		// Instantiate XSD Schema
		SchemaFactory sf = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI); 
        Schema xsd = sf.newSchema(new File(SCHEMA_PATH));

        // Get default resources file
		File defaultResources = new File(DEFAULT_XML_PATH);
		
		// Instantiate ResourcesFile
		ResourcesFile resources =  new ResourcesFile(defaultResources, xsd, logger);
		
		// Checkers
		assertNotNull(resources);
		
		int numWorkers_byName = resources.getComputeNodes_names().size();
		assertEquals("Should have a worker", numWorkers_byName, 1);
		
		int numWorkers_byList = resources.getComputeNodes_list().size();
		assertEquals("Should have a worker", numWorkers_byList, 1);
		
		int numWorkers_byHashMap = resources.getComputeNodes_hashMap().size();
		assertEquals("Should have a worker", numWorkers_byHashMap, 1);
	}
	
	@Test
	public void creation_XMLstring_XSDschema() throws URISyntaxException, JAXBException, SAXException, ResourcesFileValidationException {
		// Instantiate XSD Schema
		SchemaFactory sf = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI); 
        Schema xsd = sf.newSchema(new File(SCHEMA_PATH));

        // Get default resources file
		String defaultResources = buildXMLString();
		
		// Instantiate ResourcesFile
		ResourcesFile resources =  new ResourcesFile(defaultResources, xsd, logger);
		
		// Checkers
		assertNotNull(resources);
		
		int numWorkers_byName = resources.getComputeNodes_names().size();
		assertEquals("Should have a worker", numWorkers_byName, 1);
		
		int numWorkers_byList = resources.getComputeNodes_list().size();
		assertEquals("Should have a worker", numWorkers_byList, 1);
		
		int numWorkers_byHashMap = resources.getComputeNodes_hashMap().size();
		assertEquals("Should have a worker", numWorkers_byHashMap, 1);
	}
	
	@Test
	public void creation_XMLfile_XSDpath() throws URISyntaxException, JAXBException, SAXException, ResourcesFileValidationException {
		// Get XSD Schema path
		String xsd_path = new File(SCHEMA_PATH).toURI().getPath();

        // Get default resources file
		File defaultResources = new File(DEFAULT_XML_PATH);
		
		// Instantiate ResourcesFile
		ResourcesFile resources =  new ResourcesFile(defaultResources, xsd_path, logger);
		
		// Checkers
		assertNotNull(resources);
		
		int numWorkers_byName = resources.getComputeNodes_names().size();
		assertEquals("Should have a worker", numWorkers_byName, 1);
		
		int numWorkers_byList = resources.getComputeNodes_list().size();
		assertEquals("Should have a worker", numWorkers_byList, 1);
		
		int numWorkers_byHashMap = resources.getComputeNodes_hashMap().size();
		assertEquals("Should have a worker", numWorkers_byHashMap, 1);
	}
	
	@Test
	public void creation_XMLstring_XSDpath() throws URISyntaxException, JAXBException, SAXException, ResourcesFileValidationException {
		// Get XSD Schema path
		String xsd_path = new File(SCHEMA_PATH).toURI().getPath();

        // Get default resources file
		String defaultResources = buildXMLString();
		
		// Instantiate ResourcesFile
		ResourcesFile resources =  new ResourcesFile(defaultResources, xsd_path, logger);
		
		// Checkers
		assertNotNull(resources);
		
		int numWorkers_byName = resources.getComputeNodes_names().size();
		assertEquals("Should have a worker", numWorkers_byName, 1);
		
		int numWorkers_byList = resources.getComputeNodes_list().size();
		assertEquals("Should have a worker", numWorkers_byList, 1);
		
		int numWorkers_byHashMap = resources.getComputeNodes_hashMap().size();
		assertEquals("Should have a worker", numWorkers_byHashMap, 1);
	}
	
	
	/* ***************************************************************
	 * Dumpers checkers
	 * ***************************************************************/
	@Test
	public void XMLtoFile() throws URISyntaxException, SAXException, JAXBException, ResourcesFileValidationException, IOException {
		// Get XSD Schema path
		String xsd_path = new File(SCHEMA_PATH).toURI().getPath();

        // Get default resources file
		File defaultResources = new File(DEFAULT_XML_PATH);
		
		// Instantiate ResourcesFile
		ResourcesFile resources =  new ResourcesFile(defaultResources, xsd_path, logger);
		
		// Checkers
		assertNotNull(resources);
		
		File xml = new File(XML_TMP_PATH);
		resources.toFile(xml);
		boolean compareFile = FileUtils.contentEquals(defaultResources, xml);
		assertEquals("Dump content not equal", true, compareFile);
	}
	
	@Test 
	public void XMLtoString() throws URISyntaxException, SAXException, JAXBException, ResourcesFileValidationException {
		// Get XSD Schema path
		String xsd_path = new File(SCHEMA_PATH).toURI().getPath();

        // Get default resources file
		File defaultResources = new File(DEFAULT_XML_PATH);
		
		// Instantiate ResourcesFile
		ResourcesFile resources =  new ResourcesFile(defaultResources, xsd_path, logger);
		
		// Checkers
		assertNotNull(resources);
		
		String real_xml = resources.getString();
		String ideal_xml = buildXMLString();
		assertEquals("To string method doesn't produce the correct dump", real_xml, ideal_xml);
	}
	
	
	/* ***************************************************************
	 * Different types of XML checkers
	 * ***************************************************************/
	@Test
	public void fullXML() throws URISyntaxException, JAXBException, SAXException, ResourcesFileValidationException, IOException {
		// Get XSD Schema path
		String xsd_path = new File(SCHEMA_PATH).toURI().getPath();

        // Get default resources file
		File fullResources = new File(FULL_XML_PATH);
		
		// Instantiate ResourcesFile
		ResourcesFile resources =  new ResourcesFile(fullResources, xsd_path, logger);
		
		// Checkers
		assertNotNull(resources);
		
		File xml = new File(XML_TMP_PATH);
		resources.toFile(xml);
		boolean compareFile = FileUtils.contentEquals(fullResources, xml);
		assertEquals("Dump content not equal", true, compareFile);
	}
	
	@Test
	public void gridXML() throws URISyntaxException, JAXBException, SAXException, ResourcesFileValidationException, IOException {
		// Get XSD Schema path
		String xsd_path = new File(SCHEMA_PATH).toURI().getPath();

        // Get default resources file
		File gridResources = new File(GRID_XML_PATH);
		
		// Instantiate ResourcesFile
		ResourcesFile resources =  new ResourcesFile(gridResources, xsd_path, logger);
		
		// Checkers
		assertNotNull(resources);
		
		File xml = new File(XML_TMP_PATH);
		resources.toFile(xml);
		boolean compareFile = FileUtils.contentEquals(gridResources, xml);
		assertEquals("Dump content not equal", true, compareFile);
	}
	
	@Test
	public void servicesXML() throws URISyntaxException, JAXBException, SAXException, ResourcesFileValidationException, IOException {
		// Get XSD Schema path
		String xsd_path = new File(SCHEMA_PATH).toURI().getPath();

        // Get default resources file
		File servicesResources = new File(SERVICES_XML_PATH);
		
		// Instantiate ResourcesFile
		ResourcesFile resources =  new ResourcesFile(servicesResources, xsd_path, logger);
		
		// Checkers
		assertNotNull(resources);
		
		File xml = new File(XML_TMP_PATH);
		resources.toFile(xml);
		boolean compareFile = FileUtils.contentEquals(servicesResources, xml);
		assertEquals("Dump content not equal", true, compareFile);
	}
	
	@Test
	public void rocciXML() throws URISyntaxException, JAXBException, SAXException, ResourcesFileValidationException, IOException {
		// Get XSD Schema path
		String xsd_path = new File(SCHEMA_PATH).toURI().getPath();

        // Get default resources file
		File rocciResources = new File(ROCCI_XML_PATH);
		
		// Instantiate ResourcesFile
		ResourcesFile resources =  new ResourcesFile(rocciResources, xsd_path, logger);
		
		// Checkers
		assertNotNull(resources);
		
		File xml = new File(XML_TMP_PATH);
		resources.toFile(xml);
		boolean compareFile = FileUtils.contentEquals(rocciResources, xml);
		assertEquals("Dump content not equal", true, compareFile);
	}
	
	@Test
	public void jclouds_gceXML() throws URISyntaxException, JAXBException, SAXException, ResourcesFileValidationException, IOException {
		// Get XSD Schema path
		String xsd_path = new File(SCHEMA_PATH).toURI().getPath();

        // Get default resources file
		File gceResources = new File(JCLOUDS_GCE_XML_PATH);
		
		// Instantiate ResourcesFile
		ResourcesFile resources =  new ResourcesFile(gceResources, xsd_path, logger);
		
		// Checkers
		assertNotNull(resources);
		
		File xml = new File(XML_TMP_PATH);
		resources.toFile(xml);
		boolean compareFile = FileUtils.contentEquals(gceResources, xml);
		assertEquals("Dump content not equal", true, compareFile);
	}
	
	
	/* ***************************************************************
	 * XML String builder (PRIVATE)
	 * ***************************************************************/
	private String buildXMLString() {
		StringBuilder sb = new StringBuilder("<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>\n");
        sb.append("<ResourcesList>").append("\n");
        
        sb.append("    ").append("<ComputeNode Name=\"localhost\">").append("\n");
        
        sb.append("    ").append("    ").append("<Processor Name=\"MainProcessor\">").append("\n");
        sb.append("    ").append("    ").append("    ").append("<ComputingUnits>4</ComputingUnits>").append("\n");
        sb.append("    ").append("    ").append("</Processor>").append("\n");
        
        sb.append("    ").append("    ").append("<Adaptors>").append("\n");
        sb.append("    ").append("    ").append("    ").append("<Adaptor Name=\"integratedtoolkit.nio.master.NIOAdaptor\">").append("\n");
        sb.append("    ").append("    ").append("    ").append("    ").append("<SubmissionSystem>").append("\n");
        sb.append("    ").append("    ").append("    ").append("    ").append("    ").append("<Interactive/>").append("\n");
        sb.append("    ").append("    ").append("    ").append("    ").append("</SubmissionSystem>").append("\n");
        sb.append("    ").append("    ").append("    ").append("    ").append("<Ports>").append("\n");
        sb.append("    ").append("    ").append("    ").append("    ").append("    ").append("<MinPort>43001</MinPort>").append("\n");
        sb.append("    ").append("    ").append("    ").append("    ").append("    ").append("<MaxPort>43002</MaxPort>").append("\n");
        sb.append("    ").append("    ").append("    ").append("    ").append("</Ports>").append("\n");
        sb.append("    ").append("    ").append("    ").append("</Adaptor>").append("\n");
        sb.append("    ").append("    ").append("    ").append("<Adaptor Name=\"integratedtoolkit.gat.master.GATAdaptor\">").append("\n");
        sb.append("    ").append("    ").append("    ").append("    ").append("<SubmissionSystem>").append("\n");
        sb.append("    ").append("    ").append("    ").append("    ").append("    ").append("<Batch>").append("\n");
        sb.append("    ").append("    ").append("    ").append("    ").append("    ").append("    ").append("<Queue>sequential</Queue>").append("\n");
        sb.append("    ").append("    ").append("    ").append("    ").append("    ").append("</Batch>").append("\n");
        sb.append("    ").append("    ").append("    ").append("    ").append("    ").append("<Interactive/>").append("\n");
        sb.append("    ").append("    ").append("    ").append("    ").append("</SubmissionSystem>").append("\n");
        sb.append("    ").append("    ").append("    ").append("    ").append("<BrokerAdaptor>sshtrilead</BrokerAdaptor>").append("\n");
        sb.append("    ").append("    ").append("    ").append("</Adaptor>").append("\n");
        sb.append("    ").append("    ").append("</Adaptors>").append("\n");
        sb.append("    ").append("</ComputeNode>").append("\n");

        sb.append("</ResourcesList>").append("\n");
        
        return sb.toString();
	}
	
	@Test
	public void staticProcessorAdaptorComputeNodeCreationAndRead() throws SAXException, JAXBException, ResourcesFileValidationException{
		int cu = 5;
		float speed = 2.6f;
		String nodeName = "blablahost";
		String procName = "Proc1";
		String arch = "amd64";
		String key = "procKey";
		String value = "procValue";
		String adaptorName = "nio";
		boolean batch = false;
		boolean interactive = true;
		String gatprop="gat_prop";
		String user = "user";
		
		ProcessorPropertyType pp = ResourcesFile.createProcessorProperty(key, value);
		ProcessorType proc = ResourcesFile.createProcessor(procName,cu,arch,speed,pp);
		AdaptorType ad = ResourcesFile.createAdaptor(adaptorName,batch,null,interactive,gatprop,user);
		String xsd_path = new File(SCHEMA_PATH).toURI().getPath();
			
			// Instantiate ResourcesFile
			ResourcesFile resources =  new ResourcesFile(xsd_path, logger);
			List<ProcessorType> processors = new LinkedList<ProcessorType>();
			processors.add(proc);
			List<AdaptorType> adaptors = new LinkedList<AdaptorType>();
			adaptors.add(ad);
			resources.addComputeNode(nodeName, processors, adaptors);
		ComputeNodeType cn = resources.getComputeNode(nodeName);
		ProcessorType procExtracted = resources.getProcessors(cn).get(0);
		assertEquals(procName, procExtracted.getName());
		assertEquals(cu, resources.getProcessorComputingUnits(procExtracted));
		assertEquals(arch, resources.getProcessorArchitecture(procExtracted));
		assertEquals(speed, resources.getProcessorSpeed(procExtracted),0);
		assertEquals(key, resources.getProcessorProperty(procExtracted).getKey());
		assertEquals(value, resources.getProcessorProperty(procExtracted).getValue());
	}
	
	@Test
	public void computeNodeCreationWithNIOAndRead() throws SAXException, JAXBException, ResourcesFileValidationException{
		int cu = 5;
		float speed = 2.6f;
		String nodeName = "blablahost";
		String procName = "Proc1";
		String arch = "amd64";
		String key = "procKey";
		String value = "procValue";
		float memSize =32.5f;
		float storageSize = 256.0f;
		String osType = OSTypeType.LINUX.value();
		String adaptorName = "nio";
		int minPort = 20;
		int maxPort = 40;
		String executor ="ssh";
		//boolean batch = true;
		//String queue = "default";
		//boolean interactive = true;
		//String gatprop = "sshtrillead";
		String user = "user";
		
		String xsd_path = new File(SCHEMA_PATH).toURI().getPath();
			
		// Instantiate ResourcesFile
		ResourcesFile resources =  new ResourcesFile(xsd_path, logger);
		resources.addComputeNode(nodeName, procName, cu, arch, speed, ResourcesFile.createProcessorProperty(key,value), adaptorName, maxPort, minPort, executor, user, memSize, null, storageSize, null, osType, null, null);
		ComputeNodeType cn = resources.getComputeNode(nodeName);
		ProcessorType procExtracted = resources.getProcessors(cn).get(0);
		assertEquals(procName, procExtracted.getName());
		assertEquals(cu, resources.getProcessorComputingUnits(procExtracted));
		assertEquals(arch, resources.getProcessorArchitecture(procExtracted));
		assertEquals(speed, resources.getProcessorSpeed(procExtracted),0);
		assertEquals(key, resources.getProcessorProperty(procExtracted).getKey());
		assertEquals(value, resources.getProcessorProperty(procExtracted).getValue());
		assertEquals(memSize, resources.getMemorySize(cn),0);
		assertEquals(storageSize, resources.getStorageSize(cn),0);
		
		assertEquals(osType, resources.getOperatingSystemType(cn));
		assertEquals(NIOAdaptorProperties.class, resources.getAdaptorProperties(cn, adaptorName).getClass());
		assertEquals(minPort, ((NIOAdaptorProperties)resources.getAdaptorProperties(cn, adaptorName)).getMinPort());
		assertEquals(maxPort, ((NIOAdaptorProperties)resources.getAdaptorProperties(cn, adaptorName)).getMaxPort());
		assertEquals(executor, ((NIOAdaptorProperties)resources.getAdaptorProperties(cn, adaptorName)).getRemoteExecutionCommand());
		assertNotNull(resources.getString());
	}
	
	@Test
	public void computeNodeCreationWithGATAndRead() throws SAXException, JAXBException, ResourcesFileValidationException{
		int cu = 5;
		float speed = 2.6f;
		String nodeName = "blablahost";
		String procName = "Proc1";
		String arch = "amd64";
		String key = "procKey";
		String value = "procValue";
		float memSize =32.5f;
		float storageSize = 256.0f;
		String osType = OSTypeType.LINUX.value();
		String adaptorName = "gat";
		//int minPort = 20;
		//int maxPort = 40;
		//String executor ="ssh";
		boolean batch = true;
		String queue = "default";
		List<String> queues = new ArrayList<String>();
		queues.add(queue);
		boolean interactive = true;
		String gatprop = "sshtrillead";
		String user = "user";
		
		String xsd_path = new File(SCHEMA_PATH).toURI().getPath();
			
		// Instantiate ResourcesFile
		ResourcesFile resources =  new ResourcesFile(xsd_path, logger);
		resources.addComputeNode(nodeName, procName, cu, arch, speed, ResourcesFile.createProcessorProperty(key,value), adaptorName, batch, queues, interactive, gatprop, user, memSize, null, storageSize, null, osType, null, null);
		ComputeNodeType cn = resources.getComputeNode(nodeName);
		assertNotNull(cn);
		ProcessorType procExtracted = resources.getProcessors(cn).get(0);
		assertNotNull(procExtracted);
		assertEquals(procName, procExtracted.getName());
		assertEquals(cu, resources.getProcessorComputingUnits(procExtracted));
		assertEquals(arch, resources.getProcessorArchitecture(procExtracted));
		assertEquals(speed, resources.getProcessorSpeed(procExtracted),0);
		assertEquals(key, resources.getProcessorProperty(procExtracted).getKey());
		assertEquals(value, resources.getProcessorProperty(procExtracted).getValue());
		assertEquals(memSize, resources.getMemorySize(cn),0);
		assertEquals(storageSize, resources.getStorageSize(cn),0);
		assertEquals(osType, resources.getOperatingSystemType(cn));
		assertEquals(String.class, resources.getAdaptorProperties(cn, adaptorName).getClass());
		assertEquals(gatprop, resources.getAdaptorProperties(cn, adaptorName));
		assertEquals(queue, resources.getAdaptorQueues(cn, adaptorName).get(0));
		assertNotNull(resources.getString());
	}
	
	@Test
	public void CloudCreationWithGATAndRead() throws SAXException, JAXBException, ResourcesFileValidationException{
		int cu = 5;
		float speed = 2.6f;
		String cloudName = "blablacloud";
		String server = "http://server.com";
		String connector = "integratedtoolkit.connector.DummyConnector";
		String instanceName = "instance";
		String procName = "Proc1";
		String arch = "amd64";
		String key = "procKey";
		String value = "procValue";
		float memSize =32.5f;
		float storageSize = 256.0f;
		String imageName = "image" ;
		String osType = OSTypeType.LINUX.value();
		String adaptorName = "gat";
		//int minPort = 20;
		//int maxPort = 40;
		//String executor ="ssh";
		boolean batch = true;
		String queue = "default";
		List<String> queues = new ArrayList<String>();
		queues.add(queue);
		boolean interactive = true;
		String gatprop = "sshtrillead";
		String user = "user";
		
		String xsd_path = new File(SCHEMA_PATH).toURI().getPath();
			
		// Instantiate ResourcesFile
		ResourcesFile resources =  new ResourcesFile(xsd_path, logger);
		List<ImageType> images = new ArrayList<ImageType>();
		List<InstanceTypeType> instances = new ArrayList<InstanceTypeType>();
		ImageType image = ResourcesFile.createImage(imageName, adaptorName, batch, queues,	interactive,gatprop , user, osType,null, null);
		images.add(image);
		InstanceTypeType instance = ResourcesFile.createInstance(instanceName, procName, cu, arch, speed, ResourcesFile.createProcessorProperty(key,value),memSize, null, storageSize, null);
		instances.add(instance);
	
		EndpointType ep = ResourcesFile.createEndpoint(server,connector, null);
		resources.addCloudProvider(cloudName, ep, images, instances);
		CloudProviderType cp = resources.getCloudProvider(cloudName);
		assertNotNull(cp);
		InstanceTypeType cn = resources.getInstance(cp, instanceName);
		assertNotNull(cn);
		ImageType i = resources.getImage(cp, imageName);
		assertNotNull(i);
		ProcessorType procExtracted = resources.getProcessors(cn).get(0);
		assertNotNull(procExtracted);
		assertEquals(procName, procExtracted.getName());
		assertEquals(cu, resources.getProcessorComputingUnits(procExtracted));
		assertEquals(arch, resources.getProcessorArchitecture(procExtracted));
		assertEquals(speed, resources.getProcessorSpeed(procExtracted),0);
		assertEquals(key, resources.getProcessorProperty(procExtracted).getKey());
		assertEquals(value, resources.getProcessorProperty(procExtracted).getValue());
		assertEquals(memSize, resources.getMemorySize(cn),0);
		assertEquals(storageSize, resources.getStorageSize(cn),0);
		assertEquals(osType, resources.getOperatingSystemType(i));
		assertEquals(String.class, resources.getAdaptorProperties(i, adaptorName).getClass());
		assertEquals(gatprop, resources.getAdaptorProperties(i, adaptorName));
		assertEquals(queue, resources.getAdaptorQueues(i, adaptorName).get(0));
		assertNotNull(resources.getString());
		
	}
	
	@Test
	public void CloudCreationWithNIOAndRead() throws SAXException, JAXBException, ResourcesFileValidationException{
		int cu = 5;
		float speed = 2.6f;
		String cloudName = "blablacloud";
		String server = "http://server.com";
		String connector = "integratedtoolkit.connector.DummyConnector";
		String instanceName = "instance";
		String procName = "Proc1";
		String arch = "amd64";
		String key = "procKey";
		String value = "procValue";
		float memSize =32.5f;
		float storageSize = 256.0f;
		String imageName = "image" ;
		String osType = OSTypeType.LINUX.value();
		String adaptorName = "nio";
		int minPort = 20;
		int maxPort = 40;
		String executor ="ssh";
		//boolean batch = true;
		//String queue = "default";
		//List<String> queues = new ArrayList<String>();
		//queues.add(queue);
		//boolean interactive = true;
		//String gatprop = "sshtrillead";
		String user = "user";
		
		String xsd_path = new File(SCHEMA_PATH).toURI().getPath();
			
		// Instantiate ResourcesFile
		ResourcesFile resources =  new ResourcesFile(xsd_path, logger);
		List<ImageType> images = new ArrayList<ImageType>();
		List<InstanceTypeType> instances = new ArrayList<InstanceTypeType>();
		ImageType image = ResourcesFile.createImage(imageName, adaptorName, maxPort, minPort, executor , user, osType,null, null);
		images.add(image);
		InstanceTypeType instance = ResourcesFile.createInstance(instanceName, procName, cu, arch, speed, ResourcesFile.createProcessorProperty(key,value),memSize, null, storageSize, null);
		instances.add(instance);
	
		EndpointType ep = ResourcesFile.createEndpoint(server,connector, null);
		resources.addCloudProvider(cloudName, ep, images, instances);
		CloudProviderType cp = resources.getCloudProvider(cloudName);
		assertNotNull(cp);
		InstanceTypeType cn = resources.getInstance(cp, instanceName);
		assertNotNull(cn);
		ImageType i = resources.getImage(cp, imageName);
		assertNotNull(i);
		ProcessorType procExtracted = resources.getProcessors(cn).get(0);
		assertNotNull(procExtracted);
		assertEquals(procName, procExtracted.getName());
		assertEquals(cu, resources.getProcessorComputingUnits(procExtracted));
		assertEquals(arch, resources.getProcessorArchitecture(procExtracted));
		assertEquals(speed, resources.getProcessorSpeed(procExtracted),0);
		assertEquals(key, resources.getProcessorProperty(procExtracted).getKey());
		assertEquals(value, resources.getProcessorProperty(procExtracted).getValue());
		assertEquals(memSize, resources.getMemorySize(cn),0);
		assertEquals(storageSize, resources.getStorageSize(cn),0);
		assertEquals(osType, resources.getOperatingSystemType(i));
		assertEquals(NIOAdaptorProperties.class, resources.getAdaptorProperties(i, adaptorName).getClass());
		assertEquals(minPort, ((NIOAdaptorProperties)resources.getAdaptorProperties(i, adaptorName)).getMinPort());
		assertEquals(maxPort, ((NIOAdaptorProperties)resources.getAdaptorProperties(i, adaptorName)).getMaxPort());
		assertEquals(executor, ((NIOAdaptorProperties)resources.getAdaptorProperties(i, adaptorName)).getRemoteExecutionCommand());
		assertNotNull(resources.getString());
		File f = new File("test.xml");
		resources.toFile(f);
		assertTrue(f.exists());
	}
	
}
