/**
 *
 *   Copyright 2015-2015 Barcelona Supercomputing Center (www.bsc.es) All rights reserved.
 *
 *   Licensed under the Apache License, Version 2.0 (the "License");
 *   you may not use this file except in compliance with the License.
 *   You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *   Unless required by applicable law or agreed to in writing, software
 *   distributed under the License is distributed on an "AS IS" BASIS,
 *   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *   See the License for the specific language governing permissions and
 *   limitations under the License.
 */

package integratedtoolkit.api.impl;

import java.io.File;
import java.io.InputStream;
import java.util.Properties;
import integratedtoolkit.ITConstants;
import integratedtoolkit.api.ITExecution;
import integratedtoolkit.api.IntegratedToolkit;
import integratedtoolkit.comm.Comm;
import integratedtoolkit.types.data.location.DataLocation;
import integratedtoolkit.components.impl.AccessProcessor;
import integratedtoolkit.components.impl.RuntimeMonitor;
import integratedtoolkit.components.impl.TaskDispatcher;
import integratedtoolkit.loader.LoaderAPI;
import integratedtoolkit.loader.total.ObjectRegistry;
import integratedtoolkit.log.Loggers;
import integratedtoolkit.types.parameter.Parameter;
import integratedtoolkit.types.parameter.Parameter.*;
import integratedtoolkit.types.parameter.Parameter.DependencyParameter.*;
import integratedtoolkit.types.data.AccessParams.AccessMode;
import integratedtoolkit.types.data.AccessParams.FileAccessParams;
import integratedtoolkit.types.data.DataAccessId.*;
import integratedtoolkit.types.data.location.URI;
import integratedtoolkit.types.parameter.FileParameter;
import integratedtoolkit.types.parameter.ObjectParameter;
import integratedtoolkit.util.RuntimeConfigManager;
import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;

public class IntegratedToolkitImpl implements IntegratedToolkit, ITExecution, LoaderAPI {

    //According to runcompss script default value
    //private static final String DEFAULT_ADAPTOR = "integratedtoolkit.gat.master.GATAdaptor";
    private static final String DEFAULT_ADAPTOR = "integratedtoolkit.nio.master.NIOAdaptor";

    // Exception constants definition
    protected static final String WARN_IT_FILE_NOT_READ = "WARNING: IT Properties file could not be read";
    protected static final String WARN_FILE_EMPTY_DEFAULT = "WARNING: IT Properties file is null. Setting default values";
    protected static final String WARN_VERSION_PROPERTIES = "WARNING: COMPSs Runtime VERSION-BUILD properties file could not be read";
    protected static final String ERROR_FILE_NAME = "ERROR: Cannot parse file name";
    protected static final String ERROR_OBJECT_SERIALIZE = "ERROR: Cannot serialize object to file";
    protected static final String ERROR_OBJECT_DESERIALIZE = "ERROR: Cannot deserialize object from file";
    protected static final String WARN_WRONG_DIRECTION = "ERROR: Invalid parameter direction: ";

    // Constants
    protected static final String FILE_URI = "file:";
    protected static final String SHARED_URI = "shared:";

    // COMPSs Version and buildnumber attributes
    protected static String COMPSs_VERSION = null;
    protected static String COMPSs_BUILDNUMBER = null;

    // Components
    protected static AccessProcessor ap;
    protected static TaskDispatcher td;

    // Application attributes and directories
    public static String appName;

    public static boolean initialized = false;

    // Object registry
    protected static ObjectRegistry oReg;

    //Monitor 
    protected static RuntimeMonitor monitor;

    // Logger
    protected static Logger logger = null;

    static {
        // Load Runtime configuration parameters
        String properties_loc = System.getProperty(ITConstants.IT_CONFIG_LOCATION);
        if (properties_loc == null) {
            InputStream stream = findPropertiesConfigFile();
            if (stream != null) {
                try {
                    setPropertiesFromRuntime(new RuntimeConfigManager(stream));
                } catch (Exception e) {
                    System.err.println(WARN_IT_FILE_NOT_READ);
                    e.printStackTrace();
                }
            } else {
                setDefaultProperties();
            }
        } else {
            try {
                setPropertiesFromRuntime(new RuntimeConfigManager(properties_loc));
            } catch (Exception e) {
                System.err.println(WARN_IT_FILE_NOT_READ);
                e.printStackTrace();
            }
        }

        Comm.init();
        /*
         * Configures log4j for the JVM where the application and the IT API belong  
         */
        logger = Logger.getLogger(Loggers.API);
        PropertyConfigurator.configure(System.getProperty(ITConstants.LOG4J));

        /* Create a monitor dir where to store:
         * - Monitor files
         */
    }

    //Code Added to support configuration files
    private static void setPropertiesFromRuntime(RuntimeConfigManager manager) {
        try {
            if (manager != null) {
                if (manager.getAppName() != null && System.getProperty(ITConstants.IT_APP_NAME) == null) {
                    System.setProperty(ITConstants.IT_APP_NAME, manager.getAppName());
                }
                if (manager.getLog4jConfiguration() != null && System.getProperty(ITConstants.LOG4J) == null) {
                    System.setProperty(ITConstants.LOG4J, manager.getLog4jConfiguration());
                }
                if (manager.getResourcesFile() != null && System.getProperty(ITConstants.IT_RES_FILE) == null) {
                    System.setProperty(ITConstants.IT_RES_FILE, manager.getResourcesFile());
                }
                if (manager.getResourcesSchema() != null && System.getProperty(ITConstants.IT_RES_SCHEMA) == null) {
                    System.setProperty(ITConstants.IT_RES_SCHEMA, manager.getResourcesSchema());
                }
                if (manager.getProjectFile() != null && System.getProperty(ITConstants.IT_PROJ_FILE) == null) {
                    System.setProperty(ITConstants.IT_PROJ_FILE, manager.getProjectFile());
                }
                if (manager.getProjectSchema() != null && System.getProperty(ITConstants.IT_PROJ_SCHEMA) == null) {
                    System.setProperty(ITConstants.IT_PROJ_SCHEMA, manager.getProjectSchema());
                }

                if (manager.getScheduler() != null && System.getProperty(ITConstants.IT_SCHEDULER) == null) {
                    System.setProperty(ITConstants.IT_SCHEDULER, manager.getScheduler());
                }
                if (manager.getMonitorInterval() > 0 && System.getProperty(ITConstants.IT_MONITOR) == null) {
                    System.setProperty(ITConstants.IT_MONITOR, Long.toString(manager.getMonitorInterval()));
                }
                if (manager.getGATAdaptor() != null && System.getProperty(ITConstants.GAT_ADAPTOR) == null) {
                    System.setProperty(ITConstants.GAT_ADAPTOR, manager.getGATAdaptor());
                }
                if (manager.getGATBrokerAdaptor() != null && System.getProperty(ITConstants.GAT_BROKER_ADAPTOR) == null) {
                    System.setProperty(ITConstants.GAT_BROKER_ADAPTOR, manager.getGATBrokerAdaptor());
                }
                if (manager.getGATFileAdaptor() != null && System.getProperty(ITConstants.GAT_FILE_ADAPTOR) == null) {
                    System.setProperty(ITConstants.GAT_FILE_ADAPTOR, manager.getGATFileAdaptor());
                }
                if (manager.getWorkerCP() != null && System.getProperty(ITConstants.IT_WORKER_CP) == null) {
                    System.setProperty(ITConstants.IT_WORKER_CP, manager.getWorkerCP());
                }
                if (manager.getServiceName() != null && System.getProperty(ITConstants.IT_SERVICE_NAME) == null) {
                    System.setProperty(ITConstants.IT_SERVICE_NAME, manager.getServiceName());
                }
                if (System.getProperty(ITConstants.COMM_ADAPTOR) == null) {
                    if (manager.getCommAdaptor() != null) {
                        System.setProperty(ITConstants.COMM_ADAPTOR, manager.getCommAdaptor());
                    } else {
                        System.setProperty(ITConstants.COMM_ADAPTOR, DEFAULT_ADAPTOR);
                    }
                }
                if (System.getProperty(ITConstants.GAT_DEBUG) == null) {
                    System.setProperty(ITConstants.GAT_DEBUG, Boolean.toString(manager.isGATDebug()));
                }
                if (System.getProperty(ITConstants.IT_LANG) == null) {
                    System.setProperty(ITConstants.IT_LANG, manager.getLang());
                }
                if (System.getProperty(ITConstants.IT_GRAPH) == null) {
                    System.setProperty(ITConstants.IT_GRAPH, Boolean.toString(manager.isGraph()));
                }
                if (System.getProperty(ITConstants.IT_TRACING) == null) {
                    System.setProperty(ITConstants.IT_TRACING, Boolean.toString(manager.isTracing()));
                }
                if (System.getProperty(ITConstants.IT_PRESCHED) == null) {
                    System.setProperty(ITConstants.IT_PRESCHED, Boolean.toString(manager.isPresched()));
                }

                if (manager.getContext() != null) {
                    System.setProperty(ITConstants.IT_CONTEXT, manager.getContext());
                }
                System.setProperty(ITConstants.IT_TO_FILE, Boolean.toString(manager.isToFile()));
            } else {
                setDefaultProperties();
            }
        } catch (Exception e) {
            System.err.println(WARN_IT_FILE_NOT_READ);
            e.printStackTrace();
        }
    }

    private static void setDefaultProperties() {
        System.err.println(WARN_FILE_EMPTY_DEFAULT);
        if (System.getProperty(ITConstants.IT_RES_SCHEMA) == null || System.getProperty(ITConstants.IT_RES_SCHEMA).equals("")) {
            System.setProperty(ITConstants.IT_RES_SCHEMA, System.getenv("IT_HOME") + "/xml/resources/resource_schema.xsd");
        }
        if (System.getProperty(ITConstants.IT_PROJ_SCHEMA) == null || System.getProperty(ITConstants.IT_PROJ_SCHEMA).equals("")) {
            System.setProperty(ITConstants.IT_PROJ_SCHEMA, System.getenv("IT_HOME") + "/xml/projects/project_schema.xsd");
        }
        if (System.getProperty(ITConstants.GAT_ADAPTOR) == null || System.getProperty(ITConstants.GAT_ADAPTOR).equals("")) {
            System.setProperty(ITConstants.GAT_ADAPTOR, System.getenv("GAT_LOCATION") + "/lib/adaptors");
        }
        if (System.getProperty(ITConstants.COMM_ADAPTOR) == null || System.getProperty(ITConstants.COMM_ADAPTOR).equals("")) {
            System.setProperty(ITConstants.COMM_ADAPTOR, DEFAULT_ADAPTOR);
        }
        if (System.getProperty(ITConstants.IT_TRACING) == null || System.getProperty(ITConstants.IT_TRACING).equals("")) {
            System.setProperty(ITConstants.IT_TRACING, "false");
        }
    }

    private static InputStream findPropertiesConfigFile() {
        InputStream stream = IntegratedToolkitImpl.class.getResourceAsStream(ITConstants.IT_CONFIG);
        if (stream != null) {
            return stream;
        } else {
            stream = IntegratedToolkitImpl.class.getResourceAsStream(File.separator + ITConstants.IT_CONFIG);
            if (stream != null) {
                return stream;
            } else {
                //System.err.println("IT properties file not defined. Looking at classLoader ...");
                stream = IntegratedToolkitImpl.class.getClassLoader().getResourceAsStream(ITConstants.IT_CONFIG);
                if (stream != null) {
                    return stream;
                } else {
                    stream = IntegratedToolkitImpl.class.getClassLoader().getResourceAsStream(File.separator + ITConstants.IT_CONFIG);
                    if (stream != null) {
                        return stream;
                    } else {
                        //System.err.println("IT properties file not found in classloader. Looking at system resource ...");
                        stream = ClassLoader.getSystemResourceAsStream(ITConstants.IT_CONFIG);
                        if (stream != null) {
                            return stream;
                        } else {
                            stream = ClassLoader.getSystemResourceAsStream(File.separator + ITConstants.IT_CONFIG);
                            if (stream != null) {
                                return stream;
                            } else {
                                //System.err.println("IT properties file not found. Looking at parent ClassLoader");
                                stream = IntegratedToolkitImpl.class.getClassLoader().getParent().getResourceAsStream(ITConstants.IT_CONFIG);
                                if (stream != null) {
                                    return stream;
                                } else {
                                    stream = IntegratedToolkitImpl.class.getClassLoader().getParent().getResourceAsStream(File.separator + ITConstants.IT_CONFIG);
                                    if (stream != null) {
                                        return stream;
                                    } else {
                                        //System.err.println("IT properties file not found");
                                        return null;
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    public IntegratedToolkitImpl() {

        // Load COMPSs version and buildnumber 
        try {
            Properties props = new Properties();
            props.load(this.getClass().getResourceAsStream("/version.properties"));
            COMPSs_VERSION = props.getProperty("compss.version");
            COMPSs_BUILDNUMBER = props.getProperty("compss.build");
        } catch (Exception e) {
            logger.warn(WARN_VERSION_PROPERTIES);
        }

        if (COMPSs_VERSION == null) {
            logger.debug("Deploying COMPSs Runtime");
        } else {
            logger.debug("Deploying COMPSs Runtime v" + COMPSs_VERSION + " (build " + COMPSs_BUILDNUMBER + ")");
        }
    }

    public String getApplicationDirectory() {
        return Comm.appHost.getAppLogDirPath();
    }

    // Integrated Toolkit user interface implementation
    public synchronized void startIT() {
        Thread.currentThread().setName("APPLICATION");
        if (COMPSs_VERSION == null) {
            logger.info("Starting COMPSs Runtime");
        } else {
            logger.info("Starting COMPSs Runtime v" + COMPSs_VERSION + " (build " + COMPSs_BUILDNUMBER + ")");
        }

        if (!initialized) {
            logger.debug("Initializing components");
            td = new TaskDispatcher();
            ap = new AccessProcessor();
            if (RuntimeMonitor.isEnabled()) {
                monitor = new RuntimeMonitor(ap, td, Long.parseLong(System.getProperty(ITConstants.IT_MONITOR)));
            }

            ap.setTD(td);
            td.setTP(ap);
            initialized = true;
            logger.debug("Ready to process tasks");
        } else {
            String className = Thread.currentThread().getStackTrace()[2].getClassName();
            logger.debug("Initializing " + className + "Itf");
            try {
                td.addInterface(Class.forName(className + "Itf"));
            } catch (Exception e) {
                logger.fatal("Error adding interface " + className + "Itf");
                System.exit(1);
            }
        }
    }

    public void stopIT(boolean terminate) {
        System.out.println("Stop IT reached");
        if (RuntimeMonitor.isEnabled()) {
            monitor.shutdown();
        }
        System.out.println("Stopping AP");
        ap.shutdown();
        System.out.println("Stopping TD");
        td.shutdown();
        System.out.println("Stopping Comm");
        Comm.stop();
        logger.info("Execution Finished");

    }

    public int executeTask(Long appId,
            String methodClass,
            String methodName,
            boolean priority,
            boolean hasTarget,
            int parameterCount,
            Object... parameters) {
        if (logger.isDebugEnabled()) {
            logger.debug("Creating task from method " + methodName + " in " + methodClass);
            logger.debug("There " + (parameterCount > 1 ? "are " : "is ") + parameterCount + " parameter" + (parameterCount > 1 ? "s" : ""));
        }

        Parameter[] pars = processParameters(parameterCount, parameters);

        return ap.newTask(appId, methodClass, methodName, priority, hasTarget, pars);
    }

    public int executeTask(Long appId,
            String namespace,
            String service,
            String port,
            String operation,
            boolean priority,
            boolean hasTarget,
            int parameterCount,
            Object... parameters) {
        if (logger.isDebugEnabled()) {
            logger.debug("Creating task from service " + service + ", namespace " + namespace + ", port " + port + ", operation " + operation);
            logger.debug("There " + (parameterCount > 1 ? "are " : "is ") + parameterCount + " parameter" + (parameterCount > 1 ? "s" : ""));
        }

        Parameter[] pars = processParameters(parameterCount, parameters);

        return ap.newTask(appId, namespace, service, port, operation, priority, hasTarget, pars);
    }

    // IT_Execution interface implementation
    private Parameter[] processParameters(int parameterCount, Object[] parameters) {
        Parameter[] pars = new Parameter[parameterCount];
        // Parameter parsing needed, object is not serializable
        int i = 0;
        for (int npar = 0; npar < parameterCount; npar++) {
            ParamType type = (ParamType) parameters[i + 1];
            ParamDirection direction = (ParamDirection) parameters[i + 2];

            if (logger.isDebugEnabled()) {
                logger.debug("  Parameter " + (npar + 1) + " has type " + type.name());
            }
            switch (type) {
                case FILE_T:
                    DataLocation location = null;
                    try {
                        location = getDataLocation((String) parameters[i]);
                    } catch (Exception e) {
                        logger.fatal(ERROR_FILE_NAME, e);
                        System.exit(1);
                    }
                    pars[npar] = new FileParameter(direction, location);
                    break;

                case OBJECT_T:
                    pars[npar] = new ObjectParameter(direction,
                            parameters[i],
                            oReg.newObjectParameter(parameters[i])); // hashCode
                    break;

                default:
                    /* Basic types (including String).
                     * The only possible direction is IN, warn otherwise
                     */
                    if (direction != ParamDirection.IN) {
                        logger.warn(WARN_WRONG_DIRECTION
                                + "Parameter " + npar
                                + " has a basic type, therefore it must have INPUT direction");
                    }
                    pars[npar] = new BasicTypeParameter(type, ParamDirection.IN, parameters[i]);
                    break;
            }
            i += 3;
        }

        return pars;
    }

    public void noMoreTasks(Long appId, boolean terminate) {
        logger.info("No more tasks for app " + appId);
        // Wait until all tasks have finished
        ap.noMoreTasks(appId);
        ap.getResultFiles(appId);
    }

    public String openFile(String fileName, OpenMode m) {
        if (logger.isDebugEnabled()) {
            logger.debug("Opening file " + fileName + " in mode " + m);
        }

        DataLocation loc = null;
        try {
            loc = getDataLocation(fileName);
        } catch (Exception e) {
            logger.fatal(ERROR_FILE_NAME, e);
            System.exit(1);
        }
        AccessMode am = null;
        switch (m) {
            case READ:
                am = AccessMode.R;
                break;
            case WRITE:
                am = AccessMode.W;
                break;
            case APPEND:
                am = AccessMode.RW;
                break;
        }
        // Tell the DM that the application wants to access a file.
        FileAccessParams fap = new FileAccessParams(am, loc);
        DataLocation targetLocation = ap.mainAccessToFile(loc, fap, null);
        String path;
        if (targetLocation == null) {
            URI u = loc.getURIInHost(Comm.appHost);
            if (u != null) {
                path = u.getPath();
            } else {
                path = fileName;
            }
        } else {
            /* Return the path that the application must use to access the (renamed) file
             * The file won't recover its origin)al name until stopIT is called
             */
            path = targetLocation.getPath();
        }
        return path;
    }

    // LoaderAPI interface implementation
    public String getFile(String fileName, String destDir) {
        if (!destDir.endsWith(File.separator)) {
            destDir += File.separator;
        }
        // Parse the file name
        DataLocation sourceLocation = null;
        try {
            sourceLocation = DataLocation.getLocation(Comm.appHost, fileName);
        } catch (Exception e) {
            logger.fatal(ERROR_FILE_NAME, e);
            System.exit(1);
        }
        FileAccessParams fap = new FileAccessParams(AccessMode.R, sourceLocation);
        DataLocation targetLocation = ap.mainAccessToFile(sourceLocation, fap, destDir);
        String path;
        if (targetLocation == null) {
            URI u = sourceLocation.getURIInHost(Comm.appHost);
            if (u != null) {
                path = u.getPath();
            } else {
                path = fileName;
            }
        } else {
            // Return the name of the file (a renaming) on which the stream will be opened
            path = targetLocation.getPath();
        }
        return path;
    }

    public Object getObject(Object o, int hashCode, String destDir) {
        /* We know that the object has been accessed before by a task, otherwise
         * the ObjectRegistry would have discarded it and this method
         * would not have been called.
         */
        if (logger.isDebugEnabled()) {
            logger.debug("Getting object with hash code " + hashCode);
        }
        boolean validValue = ap.isCurrentRegisterValueValid(hashCode);
        if (validValue) {
            // Main code is still performing the same modification. No need to 
            // register it as a new version.
            return null;
        }

        Object oUpdated = ap.mainAcessToObject(o, hashCode, destDir);
        if (logger.isDebugEnabled()) {
            logger.debug("Object obtained " + oUpdated);
        }
        return oUpdated;

    }

    public void serializeObject(Object o, int hashCode, String destDir) {
        /*System.out.println("IT: Serializing object");
         String rename = TP.getLastRenaming(hashCode);

         try {
         DataLocation loc = DataLocation.getLocation(Comm.appHost, destDir + rename);
         Serializer.serialize(o, destDir + rename);
         Comm.registerLocation(rename, loc);
         } catch (Exception e) {
         logger.fatal(ERROR_OBJECT_SERIALIZE + ": " + destDir + rename, e);
         System.exit(1);
         }*/
    }

    public void setObjectRegistry(ObjectRegistry oReg) {
        IntegratedToolkitImpl.oReg = oReg;
    }

    public String getTempDir() {
        return Comm.appHost.getTempDirPath();
    }

    // Private method for file name parsing. TODO: Logical file names?
    protected DataLocation getDataLocation(String fullName) throws Exception {
        DataLocation loc;
        if (fullName.startsWith(FILE_URI)) {
            /* URI syntax with host name and absolute path, e.g. "file://bscgrid01.bsc.es/home/etejedor/file.txt"
             * Only used in grid-aware applications, using IT API and partial loader,
             * since total loader targets sequential applications that use local files.
             */
            String name, path, host;
            java.net.URI u = new java.net.URI(fullName);
            host = u.getHost();
            String fullPath = u.getPath();
            int pathEnd = fullPath.lastIndexOf("/");
            path = fullPath.substring(0, pathEnd + 1);
            name = fullPath.substring(pathEnd + 1);
            throw new UnsupportedOperationException("Referencing files from remote hosts by URI is not supported yet."); //To change body of generated methods, choose Tools | Templates.
        } else if (fullName.startsWith(SHARED_URI)) {
            java.net.URI u = new java.net.URI(fullName);
            String sharedDisk = u.getHost();
            String fullPath = u.getPath();
            loc = DataLocation.getSharedLocation(sharedDisk, fullPath);
        } else {
            // Local file, format will depend on OS
            File f = new File(fullName);
            String canonicalPath = f.getCanonicalPath();
            loc = DataLocation.getLocation(Comm.appHost, canonicalPath);
        }
        return loc;
    }

    public boolean deleteFile(String fileName) {

        // Parse the file name and translate the access mode
        DataLocation loc = null;
        try {
            loc = getDataLocation(fileName);
        } catch (Exception e) {
            logger.fatal(ERROR_FILE_NAME, e);
            System.exit(1);
        }
        ap.markForDeletion(loc);
        return true;
    }
}
