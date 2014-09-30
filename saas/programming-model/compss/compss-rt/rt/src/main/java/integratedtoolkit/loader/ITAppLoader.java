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



package integratedtoolkit.loader;

import java.lang.reflect.Method;
import java.net.URL;

import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;

import integratedtoolkit.ITConstants;
import integratedtoolkit.log.Loggers;

public class ITAppLoader {

    private static final Logger logger = Logger.getLogger(Loggers.LOADER);

    /**
     * Factored out loading function so that subclasses
     * of ITAppLoader can re-use this code
     *
     * @param chosenLoader
     * @param appName
     *
     */
    protected static void load(String chosenLoader, String appName, String[] appArgs)
            throws Exception {

        /* We will have two class loaders:
         * - Custom loader: to load our javassist version classes
         * and the classes that use them.
         * - System loader: parent of the custom loader, it will
         * load the rest of the classes (including the one of the
         * application, once it has been modified).
         */
        CustomLoader myLoader = new CustomLoader(new URL[]{});

        // Add the jars that the custom class loader needs
        //String itLib = System.getProperty(ITConstants.IT_LIB);
        String itHome = System.getenv("IT_HOME");
        //myLoader.addFile(itLib + "/javassist/javassist.jar");
        myLoader.addFile(itHome + "/rt/compss-rt.jar");

        /* The custom class loader must load the class that will modify the application and
         * invoke the modify method on an instance of this class
         */
        String loaderName = "integratedtoolkit.loader." + chosenLoader + ".ITAppModifier";
        Class<?> modifierClass = myLoader.loadClass(loaderName);
        Object modifier = modifierClass.newInstance();
        // logger.info("Modifying application " + appName + " with loader " + chosenLoader);

        Method method = modifierClass.getMethod("modify", new Class[]{String.class});
        Class<?> modAppClass = (Class<?>) method.invoke(modifier, new Object[]{appName});


        if (modAppClass != null) { // if null, the modified app has been written to a file, and thus we're done
            //logger.info("Application " + appName + " instrumented, executing...");
            Method main = modAppClass.getDeclaredMethod("main", new Class[]{String[].class});
            main.invoke(null, new Object[]{appArgs});
        }

    }

    public static void main(String[] args) throws Exception {
        // Configure log4j for the JVM where the main program runs
        PropertyConfigurator.configure(System.getProperty(ITConstants.LOG4J));

        if (args.length < 2) {
            logger.fatal("Error: missing arguments for loader");
            System.exit(1);
        }

        // Prepare the arguments
        String[] appArgs = new String[args.length - 2];
        System.arraycopy(args, 2, appArgs, 0, appArgs.length);
        
        // Load the application
        load(args[0], args[1], appArgs);

    
    }
}
