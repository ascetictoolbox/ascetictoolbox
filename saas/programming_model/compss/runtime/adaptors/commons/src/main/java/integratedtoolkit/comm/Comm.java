package integratedtoolkit.comm;

import integratedtoolkit.types.data.location.URI;
import integratedtoolkit.types.data.LogicalData;
import integratedtoolkit.types.data.location.DataLocation;
import integratedtoolkit.ITConstants;
import java.lang.reflect.Constructor;

import org.apache.log4j.Logger;

import integratedtoolkit.log.Loggers;
import integratedtoolkit.types.COMPSsWorker;
import integratedtoolkit.types.resources.MasterResource;
import integratedtoolkit.types.resources.Resource;
import integratedtoolkit.util.SharedDiskManager;
import java.io.File;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;

import java.util.Map;
import java.util.TreeMap;

public class Comm {

    private static final String defaultAdaptor = System.getProperty(ITConstants.COMM_ADAPTOR);

    // Log and debug
    protected static Logger logger = Logger.getLogger(Loggers.COMM);
    public static boolean debug = logger.isDebugEnabled();

    private static final HashMap< String, CommAdaptor> adaptors = new HashMap< String, CommAdaptor>();

    public static Map<String, LogicalData> data = Collections.synchronizedMap(new TreeMap<String, LogicalData>());

    public static MasterResource appHost;

    // Communications initializer
    public static void init() {
        appHost = new MasterResource();
        try {
            URLClassLoader sysloader = (URLClassLoader) ClassLoader.getSystemClassLoader();
            Class sysclass = URLClassLoader.class;
            String itHome = System.getenv("IT_HOME");
            
            Method method = sysclass.getDeclaredMethod("addURL", new Class[]{URL.class});
            method.setAccessible(true);
            File directory = new File(itHome + File.separator + "adaptors");
            File[] fList = directory.listFiles();
            for (File f : fList) {
                File adaptorMasterDir = new File(f.getAbsolutePath() + File.separator + "master");
                File[] jarList = adaptorMasterDir.listFiles();
                for (File jar : jarList) {
                    try {
                        method.invoke(sysloader, new Object[]{(new File(jar.getAbsolutePath())).toURI().toURL()});
                    } catch (Exception e) {
                        if (debug) {
                            logger.error("COULD NOT LOAD ADAPTOR JAR " + jar.getAbsolutePath(), e);
                        } else {
                            logger.error("COULD NOT LOAD ADAPTOR JAR " + jar.getAbsolutePath());
                        }
                    }
                }
            }
        } catch (Exception e) {
            logger.error("CAN NOT LOAD ANY ADAPTOR ", e);
        }
    }

    public static void addSharedDiskToMaster(String diskName, String mountPoint) {
        SharedDiskManager.addSharedToMachine(diskName, mountPoint, appHost);
    }

    public static COMPSsWorker initWorker(String adaptorName, String name, HashMap<String, String> properties) throws Exception {
        if (adaptorName == null) {
            adaptorName = defaultAdaptor;
        }
        CommAdaptor adaptor;
        synchronized (adaptors) {
            adaptor = adaptors.get(adaptorName);
            if (adaptor == null) {
                try {
                    Constructor constrAdaptor = Class.forName(adaptorName).getConstructor();
                    adaptor = (CommAdaptor) constrAdaptor.newInstance();
                } catch (ClassNotFoundException e) {
                    logger.error("Can not find adaptor class " + adaptorName + ".", e);
                } catch (NoSuchMethodException e) {
                    logger.error("Class " + adaptorName + " has no valid constructor.", e);
                } catch (InstantiationException e) {
                    logger.error("Can not instantiate adaptor " + adaptorName + ".", e);
                } catch (IllegalAccessException e) {
                    logger.error("Illegal access on adaptor " + adaptorName + " creation", e);
                } catch (IllegalArgumentException e) {
                    logger.error("Illegal argument on adaptor " + adaptorName + " creation", e);
                } catch (InvocationTargetException e) {
                    logger.error("Wrong target for " + adaptorName + " invocation", e);
                }
                adaptor.init();
                adaptors.put(adaptorName, adaptor);
            }
        }
        COMPSsWorker worker = adaptor.initWorker(name, properties);
        return worker;
    }

    // Clean FTM, Job, {GATJob, NIOJob} and WSJob
    public static void stop() {
        for (CommAdaptor adaptor : adaptors.values()) {
            adaptor.stop();
        }
    }

    public static LogicalData registerData(String dataId) {
        LogicalData logicalData = new LogicalData(dataId);
        data.put(dataId, logicalData);
        return logicalData;
    }

    public static LogicalData registerLocation(String dataId, DataLocation location) {
        logger.debug("Registering new Location for data " + dataId + ":");
        logger.debug("  * Location: " + location);
        LogicalData logicalData = data.get(dataId);
        logicalData.addLocation(location);
        return logicalData;
    }

    public static LogicalData registerValue(String dataId, Object value) {
        DataLocation location = DataLocation.getLocation(appHost, dataId);
        LogicalData logicalData = registerLocation(dataId, location);
        logicalData.setValue(value);
        return logicalData;
    }

    public static Object clearValue(String dataId) {
        LogicalData logicalData = data.get(dataId);
        return logicalData.removeValue();

    }

    public static boolean existsData(String renaming) {
        return (data.get(renaming) != null);
    }

    public static LogicalData getData(String dataId) {
        return data.get(dataId);
    }

    public static String dataDump() {
        StringBuilder sb = new StringBuilder("DATA DUMP\n");
        for (Map.Entry<String, LogicalData> lde : data.entrySet()) {
            sb.append("\t *").append(lde.getKey()).append(":\n");
            LogicalData ld = lde.getValue();
            for (URI u : ld.getURIs()) {
                sb.append("\t\t + ").append(u.toString()).append("\n");
                for (String adaptor : adaptors.keySet()) {
                    Object internal = u.getInternalURI(adaptor);
                    if (internal != null) {
                        sb.append("\t\t\t - ").append(internal.toString()).append("\n");
                    }
                }
            }
        }
        return sb.toString();
    }

    public static HashSet<LogicalData> getAllData(Resource host) {
        return LogicalData.getAllDataFromHost(host);
    }

    public static void removeData(String renaming) {
        LogicalData ld = data.remove(renaming);
        ld.isObsolete();
    }

    public static HashMap< String, CommAdaptor> getAdaptors() {
        return adaptors;
    }

    public static void stopSubmittedjobs() {
        for (CommAdaptor adaptor : adaptors.values()) {
            adaptor.stopSubmittedJobs();
        }
    }

}
