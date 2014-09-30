/*
 *  Copyright 2002-2014 Barcelona Supercomputing Center (www.bsc.es)
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package integratedtoolkit.util;

import java.util.LinkedList;
import java.util.List;

import integratedtoolkit.ITConstants;
import integratedtoolkit.types.ProjectWorker;

import java.util.HashMap;
import javax.xml.XMLConstants;
import javax.xml.transform.Source;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamSource;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;
import javax.xml.validation.Validator;

import org.apache.xpath.domapi.XPathEvaluatorImpl;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.xpath.XPathEvaluator;
import org.w3c.dom.xpath.XPathResult;

/**
 * The ProjectManager class is an utility to manage the configuration of all 
 * the workers 
 */
public class ProjectManager {

    private static Document projectDoc;
    private static XPathEvaluator evaluator;
    /** Relation name --> Worker parameters */
    private static HashMap<String, ProjectWorker> workers;
    /** Preferences for the JavaGAT File Adaptor*/
    private static HashMap<String, String> fileAdaptorPreferences;
    /** Preferences for the JavaGAT Job submission Adaptor*/
    private static HashMap<String, String> jobAdaptorPreferences;

    /**
     * Initializes the ProjectManager with the values on the project file
     * @throws Exception Error on the project file parsing
     */
    public static void init() throws Exception {

        String projectFile = System.getProperty(ITConstants.IT_PROJ_FILE);

        // Parse the XML document which contains resource information
        DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
        docFactory.setNamespaceAware(true);
        projectDoc = docFactory.newDocumentBuilder().parse(projectFile);

        // Validate the document against an XML Schema
        SchemaFactory schemaFactory = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
        Source schemaFile = new StreamSource(System.getProperty(ITConstants.IT_PROJ_SCHEMA));
        Schema schema = schemaFactory.newSchema(schemaFile);
        Validator validator = schema.newValidator();
        validator.validate(new DOMSource(projectDoc));

        // Create an XPath evaluator to solve queries
        evaluator = new XPathEvaluatorImpl(projectDoc);




        //LOADING WORKERS
        workers = new HashMap<String, ProjectWorker>();

        // Find all the workers defined in the project file
        String xPathToWorkers = "/Project/Worker";
        XPathResult workerRes = (XPathResult) evaluator.evaluate(xPathToWorkers,
                projectDoc,
                /*resolver*/ null,
                XPathResult.UNORDERED_NODE_ITERATOR_TYPE,
                null);

        Element e;
        ProjectWorker pw;
        Node n;
        while ((n = workerRes.iterateNext()) != null) {

            e = (Element) n;
            int limitOfTasks = 1;
            String iDir = getResourcePropertyInit(e.getAttribute("Name"), ITConstants.INSTALL_DIR);
            String wDir = getResourcePropertyInit(e.getAttribute("Name"), ITConstants.WORKING_DIR);
            String user = getResourcePropertyInit(e.getAttribute("Name"), ITConstants.USER);
            String value = getResourcePropertyInit(e.getAttribute("Name"), ITConstants.LIMIT_OF_TASKS);
            String aDir = getResourcePropertyInit(e.getAttribute("Name"), ITConstants.APP_DIR);
            String lPath = getResourcePropertyInit(e.getAttribute("Name"), ITConstants.LIB_PATH);
            if (value != null)
                limitOfTasks = Integer.parseInt(value);
            else
            	limitOfTasks = Integer.MAX_VALUE;
            pw = new ProjectWorker(e.getAttribute("Name"), null, user, limitOfTasks, iDir, wDir, aDir, lPath);
            workers.put(pw.getName(), pw);
        }

        fileAdaptorPreferences = new HashMap<String, String>();
        jobAdaptorPreferences = new HashMap<String, String>();
    }

    /**
     * Checks if there is a service with identifier wsdl inside the project file
     * @param wsdl identifier
     * @return true if there is a configuration on the project file for a 
     * resource with identifier wsdl
     */
    public static boolean containsServiceInstance(String wsdl) {
        String xPathToService = "/Project/Worker[@Name='" + wsdl + "']";
        XPathResult res = (XPathResult) evaluator.evaluate(xPathToService,
                projectDoc,
                null,
                XPathResult.FIRST_ORDERED_NODE_TYPE,
                null);
        Node n = res.getSingleNodeValue();
        return n != null;
    }

    /**
     * Returns the value of a resource property set in the project file
     * @param workerName name of the resource
     * @param property name of the property 
     * @return value of that resource property on the XML project file
     */
    private static String getResourcePropertyInit(String workerName, String property) {
        String xPathToProp = "/Project/Worker[@Name='" + workerName + "']/" + property;
        XPathResult res = (XPathResult) evaluator.evaluate(xPathToProp,
                projectDoc,
                null,
                XPathResult.FIRST_ORDERED_NODE_TYPE,
                null);
        Node n = res.getSingleNodeValue();
        if (n == null) {
            return null;
        } else {
            return n.getTextContent();
        }
    }

    /**
     * Checks if a any cloud Provider with that name appears on the project file
     * @param cloudName Name of the cloud provider
     * @return true if the provider exists
     */
    public static boolean existsCloudProvider(String cloudName) {
        String xPathToProp = "/Project/Cloud/Provider[@name='" + cloudName + "']";

        XPathResult res = (XPathResult) evaluator.evaluate(xPathToProp,
                projectDoc,
                null,
                XPathResult.FIRST_ORDERED_NODE_TYPE,
                null);
        Node n = res.getSingleNodeValue();
        return n != null;
    }

    /**
     * Returns the value of a property for the whole cloud
     * @param property Name of the property
     * @return value of that cloud property
     */
    public static String getCloudProperty(String property) {
        String xPathToProp = "/Project/Cloud/" + property;

        XPathResult res = (XPathResult) evaluator.evaluate(xPathToProp,
                projectDoc,
                null,
                XPathResult.FIRST_ORDERED_NODE_TYPE,
                null);
        Node n = res.getSingleNodeValue();
        if (n == null) {
            return null;
        } else {
            return n.getTextContent();
        }
    }

    /**
     * Reads from the project file the max amount of VMs that can be created on 
     * a cloud provider at the same time
     * @param cloudName name of the cloud
     * @return limit of VMs that can be running at the same time
     */
    public static Integer getCloudProviderLimitOfVMs(String cloudName) {

        String xPathToProp = "/Project/Cloud/Provider[@name='" + cloudName + "']/LimitOfVMs";
        XPathResult res = (XPathResult) evaluator.evaluate(xPathToProp,
                projectDoc,
                null,
                XPathResult.UNORDERED_NODE_ITERATOR_TYPE,
                null);
        Node n = res.iterateNext();
        while (n != null) {
            return Integer.parseInt(n.getTextContent());
        }

        return null;
    }

    /**
     * Reads from the project file the value of all properties of a resource
     * @param cloudName name of the cloud
     * @return HashMap with the pairs name-value for each property of the cloud
     * with that name
     */
    public static HashMap<String, String> getCloudProviderProperties(String cloudName) {
        String xPathToProp = "/Project/Cloud/Provider[@name='" + cloudName + "']/Property";

        XPathResult res = (XPathResult) evaluator.evaluate(xPathToProp,
                projectDoc,
                null,
                XPathResult.UNORDERED_NODE_ITERATOR_TYPE,
                null);
        HashMap<String, String> properties = new HashMap();
        Node n = res.iterateNext();
        while (n != null) {
            String name = "";
            String value = "";
            for (int i = 0; i < n.getChildNodes().getLength(); i++) {
                Node child = n.getChildNodes().item(i);
                if (child.getNodeName().compareTo("Name") == 0) {
                    name = child.getTextContent();
                } else if (child.getNodeName().compareTo("Value") == 0) {
                    value = child.getTextContent();
                }
            }
            properties.put(name, value);
            if (n.getAttributes().getNamedItem("context") != null) {
                String context = n.getAttributes().getNamedItem("context").getTextContent();
                if (context.compareTo("file") == 0) {
                    fileAdaptorPreferences.put(name, value);
                } else if (context.compareTo("job") == 0) {
                    jobAdaptorPreferences.put(name, value);
                } else {
                    fileAdaptorPreferences.put(name, value);
                    jobAdaptorPreferences.put(name, value);
                }
            }
            n = res.iterateNext();
        }
        return properties;
    }

    /**
     * Checks on the project file if a cloud provider can create machines using
     * a certain image
     * @param cloudProvider name of the cloud provider
     * @param imageName name of the image
     * @return true if the provider can create VMs with that image
     */
    public static Node existsImageOnProvider(String cloudProvider, String imageName) {
        String xPathToProp = "/Project/Cloud/Provider[@name='" + cloudProvider + "']/ImageList/Image[@name='" + imageName + "']";

        XPathResult res = (XPathResult) evaluator.evaluate(xPathToProp,
                projectDoc,
                null,
                XPathResult.FIRST_ORDERED_NODE_TYPE,
                null);
        Node n = res.getSingleNodeValue();
        return n;
    }
    
    /**
     * Checks on the project file if a cloud provider can create certain kind of 
     * instance
     * @param cloudProvider name of the cloud provider
     * @param instanceName name of the image
     * @return true if the provider can create VMs with that image
     */
    public static Node existsInstanceTypeOnProvider(String cloudProvider, String instanceName) {
        String xPathToProp = "/Project/Cloud/Provider[@name='" + cloudProvider + "']/InstanceTypes/Resource[@name='" + instanceName + "']";

        XPathResult res = (XPathResult) evaluator.evaluate(xPathToProp,
                projectDoc,
                null,
                XPathResult.FIRST_ORDERED_NODE_TYPE,
                null);
        Node n = res.getSingleNodeValue();
        return n;
    }
    
    /**
     * Checks if the ProjectManager has already been initialized
     * @return true if it is initalized
     */
    public static boolean isInit() {
        return projectDoc != null;
    }

    /**
     * Returns the preferences to initialize the JavaGat file Adaptor
     * @return pairs preference name - value required to initilize the file 
     * adaptor
     */
    public static HashMap<String, String> getFileAdaptorPreferences() {
        return fileAdaptorPreferences;
    }

    /**
     * Returns the preferences to initialize the JavaGat Job Adaptor
     * @return pairs preference name - value required to initilize the job
     * adaptor
     */
    public static HashMap<String, String> getJobAdaptorPreferences() {
        return jobAdaptorPreferences;
    }

    /**
     * Returns the name of all the workers that can be used at that moment
     * @return list of the worker names
     */
    public static List<String> getWorkers() {
        List<String> workerList = new LinkedList<String>();
        synchronized (workers) {
            for (String key : workers.keySet()) {
                workerList.add(key);
            }
        }
        return workerList;
    }

    /**
     * Stores more workers configuration
     * @param cloudWorkers list of workers descriptions
     */
    public static void addProjectWorkers(List<ProjectWorker> cloudWorkers) {
        synchronized (workers) {
            for (int cloudIndex = 0; cloudIndex < cloudWorkers.size(); cloudIndex++) {
                workers.put(cloudWorkers.get(cloudIndex).getName(), cloudWorkers.get(cloudIndex));
            }
        }
    }

    /**
     * Stores the configuration of a new worker
     * @param cloudWorker Worker configuration
     */
    public static void addProjectWorker(ProjectWorker cloudWorker) {
        synchronized (workers) {
            workers.put(cloudWorker.getName(), cloudWorker);
        }
    }

    /**
     * removes the information related to a set of workers
     * @param cloudWorkers list of worker names to remove
     */
    public static void removeProjectWorkers(List<String> cloudWorkers) {
        synchronized (workers) {
            for (int cloudIndex = 0; cloudIndex < cloudWorkers.size(); cloudIndex++) {
                workers.remove(cloudWorkers.get(cloudIndex));
            }
        }
    }

    /**
     * removes the information related to a worker
     * @param cloudWorker name of the worker
     */
    public static void removeProjectWorker(String cloudWorker) {
        synchronized (workers) {
            workers.remove(cloudWorker);
        }
    }

    /**
     * Gets all the workers that have not been asked to the cloud
     * @return list of names of physical workers
     */
    public static List<String> getPhysicWorkers() {
        List<String> workerList = new LinkedList<String>();
        synchronized (workers) {
            for (ProjectWorker pw : workers.values()) {
                if ((pw.getType() == null || pw.getType().length() == 0)) {
                    if (pw.getWorkingDir() != null && pw.getWorkingDir().length() != 0) {
                        workerList.add(pw.getName());
                    }
                }
            }
        }
        return workerList;
    }

    /**
     * Returns all the worker configurations that can be used at that moment
     * @return list of the configuration of all the resource that can be used at 
     * that moment
     */
    public static LinkedList<ProjectWorker> getAllRegisteredMachines() {
        LinkedList<ProjectWorker> list = new LinkedList();
        synchronized (workers) {
            for (java.util.Map.Entry<String, ProjectWorker> e : workers.entrySet()) {
                ProjectWorker pw = e.getValue();
                if ((pw.getType() == null || pw.getType().length() == 0)) {
                    if (pw.getWorkingDir() != null && pw.getWorkingDir().length() != 0) {
                        list.add(pw);
                    }
                }
            }
        }
        return list;
    }

    /**
     * Returns the configuration of all the machines obteined from a certain 
     * cloud provider
     * @param provider name of the cloud provider
     * @return 
     */
    public static LinkedList<ProjectWorker> getProviderRegisteredMachines(String provider) {
        LinkedList<ProjectWorker> list = new LinkedList();
        synchronized (workers) {
            for (java.util.Map.Entry<String, ProjectWorker> e : workers.entrySet()) {
                ProjectWorker pw = e.getValue();
                String type = pw.getType();
                if (type != null && type.compareTo(provider) == 0) {
                    list.add(pw);
                }
            }
        }
        return list;
    }

    /**
     * Returns the value of a certain property of a worker
     * @param workerName name of the worker
     * @param property name of the property
     * @return value of that property for that worker
     */
    public static String getResourceProperty(String workerName, String property) {
        ProjectWorker pw;
        synchronized (workers) {
            pw = workers.get(workerName);
        }
        if (pw == null) {
            return null;
        }
        if (property.compareTo(ITConstants.USER) == 0) {
            return pw.getUser();
        } else if (property.compareTo(ITConstants.WORKING_DIR) == 0) {
            return pw.getWorkingDir();
        } else if (property.compareTo(ITConstants.INSTALL_DIR) == 0) {
            return pw.getInstallDir();
        } else if (property.compareTo(ITConstants.LIMIT_OF_TASKS) == 0) {
            return "" + pw.getLimitOfTasks();
        } else if (property.compareTo(ITConstants.APP_DIR) == 0) {
            return "" + pw.getAppDir();
        } else if (property.compareTo(ITConstants.LIB_PATH) == 0) {
            return "" + pw.getLibPath();
        } else {
            return null;
        }

    }

    /**
     * Reloads the project xml file 
     * @throws Exception 
     */
    public static void refresh() throws Exception {
        String projectFile = System.getProperty(ITConstants.IT_PROJ_FILE);

        // Parse the XML document which contains resource information
        DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
        docFactory.setNamespaceAware(true);
        Document tempDoc = docFactory.newDocumentBuilder().parse(projectFile);

        // Validate the document against an XML Schema
        SchemaFactory schemaFactory = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
        Source schemaFile = new StreamSource(System.getProperty(ITConstants.IT_PROJ_SCHEMA));
        Schema schema = schemaFactory.newSchema(schemaFile);
        Validator validator = schema.newValidator();
        validator.validate(new DOMSource(tempDoc));

        // Create an XPath evaluator to solve queries
        evaluator = new XPathEvaluatorImpl(tempDoc);
        projectDoc=tempDoc;
    }
}
