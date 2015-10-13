/**
 *
 *   Copyright 2014-2015 Barcelona Supercomputing Center (www.bsc.es) All rights reserved.
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

package integratedtoolkit.loader.total;

import java.lang.reflect.Method;

import org.apache.log4j.Logger;

import javassist.CannotCompileException;
import javassist.ClassPool;
import javassist.CodeConverter;
import javassist.CtClass;
import javassist.CtConstructor;
import javassist.CtField;
import javassist.CtMethod;
import javassist.Modifier;
import javassist.NotFoundException;

import integratedtoolkit.ITConstants;
import integratedtoolkit.loader.LoaderUtils;
import integratedtoolkit.log.Loggers;

public class ITAppModifier {

    private static final ClassPool cp = ClassPool.getDefault();
    private static final boolean writeToFile = System.getProperty(ITConstants.IT_TO_FILE) != null
            && System.getProperty(ITConstants.IT_TO_FILE).equals("true")
            ? true : false;
    // Flag to indicate in class is WS
    private static final boolean isWSClass = System.getProperty(ITConstants.IT_IS_WS) != null
            && System.getProperty(ITConstants.IT_IS_WS).equals("true")
            ? true : false;
    /* Flag to instrument main method.
     * if IT_IS_MAINCLASS mainclass not defined (Default case) isMain gets true;*/
    private static final boolean isMainClass = System.getProperty(ITConstants.IT_IS_MAINCLASS) != null
            && System.getProperty(ITConstants.IT_IS_MAINCLASS).equals("false")
            ? false : true;
    
    private static final Logger logger = Logger.getLogger(Loggers.LOADER);
    private static final boolean debug = logger.isDebugEnabled();

    
    public Class<?> modify(String appName)
            throws NotFoundException, CannotCompileException, ClassNotFoundException {

        Class<?> annotItf = Class.forName(appName + "Itf");
        // Methods declared in the annotated interface
        Method[] remoteMethods = annotItf.getMethods();

        // Use the application editor to include the IT API calls on the application code
        cp.importPackage("integratedtoolkit");
        cp.importPackage("integratedtoolkit.api");
        cp.importPackage("integratedtoolkit.api.impl");
        cp.importPackage("integratedtoolkit.loader");
        cp.importPackage("integratedtoolkit.loader.total");

        CtClass appClass = cp.get(appName);
        CtClass itApiClass = cp.get("integratedtoolkit.api.IntegratedToolkit");
        CtClass itExecClass = cp.get("integratedtoolkit.api.ITExecution");
        CtClass itSRClass = cp.get("integratedtoolkit.loader.total.StreamRegistry");
        CtClass itORClass = cp.get("integratedtoolkit.loader.total.ObjectRegistry");
        CtClass appIdClass = cp.get("java.lang.Long");

        String varName = LoaderUtils.randomName(5, "it");
        String itApiVar = varName + "Api";
        String itExeVar = varName + "Exe";
        String itSRVar = varName + "SR";
        String itORVar = varName + "OR";
        String itAppIdVar = varName + "AppId";
        CtField itApiField = new CtField(itApiClass, itApiVar, appClass);
        CtField itExeField = new CtField(itExecClass, itExeVar, appClass);
        CtField itSRField = new CtField(itSRClass, itSRVar, appClass);
        CtField itORField = new CtField(itORClass, itORVar, appClass);
        CtField appIdField = new CtField(appIdClass, itAppIdVar, appClass);
        itApiField.setModifiers(Modifier.PRIVATE | Modifier.STATIC);
        itExeField.setModifiers(Modifier.PRIVATE | Modifier.STATIC);
        itSRField.setModifiers(Modifier.PRIVATE | Modifier.STATIC);
        itORField.setModifiers(Modifier.PRIVATE | Modifier.STATIC);
        appIdField.setModifiers(Modifier.PRIVATE | Modifier.STATIC);
        appClass.addField(itApiField);
        appClass.addField(itExeField);
        appClass.addField(itSRField);
        appClass.addField(itORField);
        appClass.addField(appIdField);
        /*CtMethod runner = CtNewMethod.make(
         "public static Object runMethodOnObjectIT(Object o, Class methodClass, String methodName, Object[] values, Class[] types) {" +
         "java.lang.reflect.Method method = null;" +
         "try { method = methodClass.getMethod(methodName, types); }" +
         "catch (SecurityException e) { System.err.println(\"Security exception\"); e.printStackTrace(); System.exit(1); }" +
         "catch (NoSuchMethodException e) { System.err.println(\"Requested method \" + methodName + \" of \" + methodClass + \" not found\"); System.exit(1); }" +
         "Object retValue = null;" +
         "try { retValue = method.invoke(o, values); }" +
         "catch (Exception e) { System.err.println(\"Error invoking requested method\"); e.printStackTrace(); System.exit(1); }" +
         "return retValue; }",
         appClass);
         appClass.addMethod(runner);*/

        /* Create a static constructor to initialize the runtime
         * Create a shutdown hook to stop the runtime before the JVM ends
         */
        manageStartAndStop(appClass, itApiVar, itExeVar, itSRVar, itORVar);

        // Create IT App Editor
        CtMethod[] instrCandidates = appClass.getDeclaredMethods(); // Candidates to be instrumented if they are not remote
        ITAppEditor itAppEditor = new ITAppEditor(remoteMethods, instrCandidates, itApiVar, itExeVar, itSRVar, itORVar);
        itAppEditor.setAppId(itAppIdVar);

        // Create Code Converter
        CodeConverter converter = new CodeConverter();
        CtClass arrayWatcher = cp.get("integratedtoolkit.loader.total.ArrayAccessWatcher");
        CodeConverter.DefaultArrayAccessReplacementMethodNames names = new CodeConverter.DefaultArrayAccessReplacementMethodNames();
        converter.replaceArrayAccess(arrayWatcher, (CodeConverter.ArrayAccessReplacementMethodNames) names);

        /* Find the methods declared in the application class that will be instrumented
         * - Main
         * - Constructors
         * - Methods that are not in the remote list
         */
        if (debug) {
        	logger.debug("Flags: ToFile: "+ writeToFile+ " isWS: "+isWSClass+ " isMainClass: "+ isMainClass);
        }
        for (CtMethod m : instrCandidates) {
            if (LoaderUtils.checkRemote(m, remoteMethods) == null) {
                // Not a remote method, we must instrument it
                if (debug) {
                    logger.debug("Instrumenting method " + m.getName());
                }
                StringBuilder toInsertBefore = new StringBuilder(),
                        toInsertAfter = new StringBuilder();

                /* Add local variable to method representing the execution id, which will be the current thread id.
                 * Used for Services, to handle multiple service executions simultaneously with a single runtime
                 * For normal applications, there will be only one execution id. 
                 */
                m.addLocalVariable(itAppIdVar, appIdClass);
                toInsertBefore.append(itAppIdVar).append(" = new Long(Thread.currentThread().getId());");

                //TODO remove old code: boolean isMainProgram = writeToFile ? LoaderUtils.isOrchestration(m) : LoaderUtils.isMainMethod(m);
                boolean isMainProgram = LoaderUtils.isMainMethod(m);
                boolean isOrchestration = LoaderUtils.isOrchestration(m);
               
                if (isMainProgram && isMainClass) {
                    logger.debug("Inserting calls at the beginning and at the end of main");
                    
                    if (isWSClass) { //
                    	logger.debug("Inserting calls noMoreTasks at the end of main");
                    	toInsertAfter.insert(0, itExeVar + ".noMoreTasks(" + itAppIdVar + ", true);");
                        m.insertBefore(toInsertBefore.toString());
                        m.insertAfter(toInsertAfter.toString()); // executed only if Orchestration finishes properly
                    } else { // Main program
                    	logger.debug("Inserting calls noMoreTasks and stopIT at the end of main");
                    	// Set global variable for main as well, will be used in code inserted after to be run no matter what
                        toInsertBefore.append(appName).append('.').append(itAppIdVar).append(" = new Long(Thread.currentThread().getId());");
                        //toInsertAfter.append("System.exit(0);");
                        toInsertAfter.insert(0, itApiVar + ".stopIT(true);");
                        toInsertAfter.insert(0, itExeVar + ".noMoreTasks(" + appName + '.' + itAppIdVar + ", true);");
                        m.insertBefore(toInsertBefore.toString());
                        m.insertAfter(toInsertAfter.toString(), true); // executed no matter what
                    }

                    /* Instrumenting first the array accesses makes each array access become a call to a black box method
                     * of class ArrayAccessWatcher, whose parameters include the array.
                     * For the second round of instrumentation, the synchronization by transition to black box automatically
                     * synchronizes the arrays accessed.
                     * TODO: Change the order of instrumentation, so that we have more control about the synchronization, and
                     * we can distinguish between a write access and a read access (now it's read/write access by default,
                     * because it goes into the black box). 
                     */
                    m.instrument(converter);
                    m.instrument(itAppEditor);
                }else if (isOrchestration){
                    if (isWSClass) { //
                    	logger.debug("Inserting calls noMoreTasks and stopIT at the end of orchestration");
                    	toInsertAfter.insert(0, itExeVar + ".noMoreTasks(" + itAppIdVar + ", true);");
                        m.insertBefore(toInsertBefore.toString());
                        m.insertAfter(toInsertAfter.toString()); // executed only if Orchestration finishes properly
                    } else {
                    	logger.debug("Inserting only before at the beginning of an orchestration");
                    	 m.insertBefore(toInsertBefore.toString());
                         //TODO remove old code m.insertAfter(toInsertAfter.toString()); // executed only if Orchestration finishes properly
                    }
                    m.instrument(converter);
                    m.instrument(itAppEditor);
                } else {
                	logger.debug("Inserting only before");
                    m.insertBefore(toInsertBefore.toString());
                    if (isWSClass) {
                        // If we're instrumenting a service class, only instrument private methods, public might be non-OE operations
                        if (Modifier.isPrivate(m.getModifiers())) {
                            m.instrument(converter);
                            m.instrument(itAppEditor);
                        }
                    } else {
                        // For an application class, instrument all non-remote methods
                        m.instrument(converter);
                        m.instrument(itAppEditor);
                    }
                }
            }
        }
        // Instrument constructors
        for (CtConstructor c : appClass.getDeclaredConstructors()) {
        	if (debug) {
        		logger.debug("Instrumenting constructor " + c.getLongName());
        	}
            c.instrument(converter);
            c.instrument(itAppEditor);
        }

        if (writeToFile) {
            // Write the modified class to disk 
            try {
                appClass.writeFile();
            } catch (Exception e) {
                logger.fatal("Error writing the instrumented class file");
                System.exit(1);
            }
            return null;
        } else {
            /* Load the modified class into memory and return it.
             * Generally, once a class is loaded into memory no further modifications
             * can be performed on it.
             */
            return appClass.toClass();
        }
    }

    private void manageStartAndStop(CtClass appClass, String itApiVar, String itExeVar, String itSRVar, String itORVar)
            throws CannotCompileException, NotFoundException {
    	if (debug) {
    		logger.debug("Previous class initializer is " + appClass.getClassInitializer());
    	}

        CtConstructor initializer = appClass.makeClassInitializer();

        /* - Creation of the Integrated Toolkit
         * - Creation of the stream registry to keep track of streams (with error handling)
         * - Setting of the ITExecution interface variable
         * - Start of the Integrated Toolkit
         */
        StringBuilder toInsertBefore = new StringBuilder();
        if (isMainClass || isWSClass){
        	toInsertBefore.append("System.setProperty(ITConstants.IT_APP_NAME, \"" + appClass.getName() + "\");");
        }
        toInsertBefore.append(itApiVar + " = new IntegratedToolkitImpl();")
                .append(itExeVar + " = (ITExecution)" + itApiVar + ";")
                .append(itSRVar + " = new StreamRegistry((LoaderAPI) " + itApiVar + " );")
                .append(itORVar + " = new ObjectRegistry((LoaderAPI) " + itApiVar + " );")
                //.append("ArrayAccessWatcher.setObjectRegistry(" + itORVar + ");")
                .append(itApiVar + ".startIT();");

        initializer.insertBefore(toInsertBefore.toString());
    }

}
