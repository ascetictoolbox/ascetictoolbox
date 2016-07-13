package integratedtoolkit.loader;

import integratedtoolkit.ITConstants;
import integratedtoolkit.log.Loggers;
import integratedtoolkit.util.ErrorManager;

import java.lang.reflect.Method;
import java.net.URL;

import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;


public class ITAppLoader {
	
    protected static final String ERROR_COMPSs_BASE_DIR = "ERROR: Cannot create .COMPSs base directory";
    private static final Logger logger = Logger.getLogger(Loggers.LOADER);
	
    /**
     * Factored out loading function so that subclasses
     * of ITAppLoader can re-use this code
     *
     * @param chosenLoader
     * @param appName
     *
     */
    protected static void load(String chosenLoader, String appName, String[] appArgs) throws Exception {
        /* We will have two class loaders:
         * - Custom loader: to load our javassist version classes
         * and the classes that use them.
         * - System loader: parent of the custom loader, it will
         * load the rest of the classes (including the one of the
         * application, once it has been modified).
         */
        CustomLoader myLoader = null;
        
        try {
	        myLoader = new CustomLoader(new URL[]{});
	
	        // Add the jars that the custom class loader needs
	        String itHome = System.getenv(ITConstants.IT_HOME);
	        myLoader.addFile(itHome + LoaderConstants.ENGINE_JAR_WITH_REL_PATH);
	
	        /* The custom class loader must load the class that will modify the application and
	         * invoke the modify method on an instance of this class
	         */
	        String loaderName = LoaderConstants.CUSTOM_LOADER_PREFIX + chosenLoader + LoaderConstants.CUSTOM_LOADER_SUFFIX;
	        Class<?> modifierClass = myLoader.loadClass(loaderName);
	        
	        Object modifier = modifierClass.newInstance();
	        logger.debug("Modifying application " + appName + " with loader " + chosenLoader);
	
	        Method method = modifierClass.getMethod("modify", new Class[]{String.class});
	        Class<?> modAppClass = (Class<?>) method.invoke(modifier, new Object[]{appName});
	        if (modAppClass != null) { // if null, the modified app has been written to a file, and thus we're done
	            logger.debug("Application " + appName + " instrumented, executing...");
	            Method main = modAppClass.getDeclaredMethod("main", new Class[]{String[].class});
	            main.invoke(null, new Object[]{appArgs});
	        }
        } catch (Exception e) {
        	throw e;
        } finally {
        	// Close loader if needed
        	if (myLoader != null) {
        		myLoader.close();
        	}
        }
    }

    public static void main(String[] args) throws Exception {    	
        // Configure log4j for the JVM where the main program runs
        PropertyConfigurator.configure(System.getProperty(ITConstants.LOG4J));

        if (args.length < 2) {
        	ErrorManager.fatal("Error: missing arguments for loader");
        }

        // Prepare the arguments
        String[] appArgs = new String[args.length - 2];
        System.arraycopy(args, 2, appArgs, 0, appArgs.length);

        // Load the application
        try {
        	load(args[0], args[1], appArgs);
        } catch(Exception e) {
        	logger.fatal("There was an error when loading or executing your application.", e);
        	System.exit(1);
        }
    }

}
