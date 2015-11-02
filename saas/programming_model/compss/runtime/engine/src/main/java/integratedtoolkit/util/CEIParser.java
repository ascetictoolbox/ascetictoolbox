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

package integratedtoolkit.util;

import integratedtoolkit.ITConstants;
import integratedtoolkit.log.Loggers;
import integratedtoolkit.types.Implementation;
import integratedtoolkit.types.MethodImplementation;
import integratedtoolkit.types.ServiceImplementation;
import integratedtoolkit.types.annotations.Parameter;
import integratedtoolkit.types.resources.MethodResourceDescription;
import java.io.BufferedReader;
import java.io.FileReader;
import java.util.HashMap;
import java.util.LinkedList;
import org.apache.log4j.Logger;

public class CEIParser {

    private static ITConstants.Lang lang = ITConstants.Lang.JAVA;

    private static final Logger logger = Logger.getLogger(Loggers.TS_COMP);
    private static final boolean debug = logger.isDebugEnabled();

    private static final String CONSTR_LOAD_ERR = "Error loading constraints";

    static {
        String l = System.getProperty(ITConstants.IT_LANG);
        lang = ITConstants.Lang.JAVA;
        if (l != null) {
            if (l.equalsIgnoreCase("c")) {
                lang = ITConstants.Lang.C;
            } else if (l.equalsIgnoreCase("python")) {
                lang = ITConstants.Lang.PYTHON;
            }
        }
    }

    public static LinkedList<Integer> parse() {
        LinkedList<Integer> updatedCores = new LinkedList<Integer>();
        switch (lang) {
            case JAVA:
                String appName = System.getProperty(ITConstants.IT_APP_NAME);
                try {
                    updatedCores = loadJava(Class.forName(appName + "Itf"));
                } catch (ClassNotFoundException ex) {
                    throw new CoreManager.UndefinedConstraintsSourceException(appName + "Itf class cannot be found.");
                }
                break;
            default:
                throw new CoreManager.LangNotDefinedException();
        }
        return updatedCores;
    }

    /**
     * Loads the annotated class and initializes the data structures that
     * contain the constraints. For each method found in the annotated interface
     * creates its signature and adds the constraints to the structures.
     *
     * @param annotItfClass package and name of the Annotated Interface class
     * @return
     */
    public static LinkedList<Integer> loadJava(Class<?> annotItfClass) {
        LinkedList<Integer> updatedMethods = new LinkedList<Integer>();
        int coreCount = annotItfClass.getDeclaredMethods().length;
        if (debug) {
            logger.debug("Detected methods " + coreCount);
        }
        if (CoreManager.getCoreCount() == 0) {
            CoreManager.resizeStructures(coreCount);
        } else {
            CoreManager.resizeStructures(CoreManager.getCoreCount() + coreCount);
        }

        for (java.lang.reflect.Method m : annotItfClass.getDeclaredMethods()) {
            //Computes the method's signature
            if (debug) {
                logger.debug("Evaluating method " + m.getName());
            }
            StringBuilder buffer = new StringBuilder();
            buffer.append(m.getName()).append("(");
            int numPars = m.getParameterAnnotations().length;
            String type;
            if (numPars > 0) {
                type = inferType(m.getParameterTypes()[0], ((Parameter) m.getParameterAnnotations()[0][0]).type());
                buffer.append(type);
                for (int i = 1; i < numPars; i++) {
                    type = inferType(m.getParameterTypes()[i], ((Parameter) m.getParameterAnnotations()[i][0]).type());
                    buffer.append(",").append(type);
                }
            }
            buffer.append(")");
            if (m.isAnnotationPresent(integratedtoolkit.types.annotations.Method.class)) {
                String methodSignature = buffer.toString();
                integratedtoolkit.types.annotations.Method methodAnnot = m.getAnnotation(integratedtoolkit.types.annotations.Method.class);
                String[] declaringClasses = methodAnnot.declaringClass();
                int implementationCount = declaringClasses.length;
                String[] signatures = new String[implementationCount];
                for (int i = 0; i < signatures.length; i++) {
                    signatures[i] = methodSignature + declaringClasses[i];
                }
                Integer methodId = CoreManager.getCoreId(signatures);
                updatedMethods.add(methodId);
                if (methodId == CoreManager.getCoreCount()) {
                    CoreManager.increaseCoreCount();
                }
                MethodResourceDescription defaultConstraints = new MethodResourceDescription();
                MethodResourceDescription[] implConstraints = new MethodResourceDescription[implementationCount];
                if (m.isAnnotationPresent(integratedtoolkit.types.annotations.Constraints.class)) {
                    defaultConstraints = new MethodResourceDescription(m.getAnnotation(integratedtoolkit.types.annotations.Constraints.class));
                }
                if (m.isAnnotationPresent(integratedtoolkit.types.annotations.MultiConstraints.class)) {
                    integratedtoolkit.types.annotations.MultiConstraints mc = m.getAnnotation(integratedtoolkit.types.annotations.MultiConstraints.class);
                    mc.value();
                    for (int i = 0; i < implementationCount; i++) {
                        MethodResourceDescription specificConstraints = new MethodResourceDescription(mc.value()[i]);
                        specificConstraints.join(defaultConstraints);
                        if (specificConstraints.getProcessorCoreCount() == 0) {
                            specificConstraints.setProcessorCoreCount(1);
                        }
                        implConstraints[i] = specificConstraints;
                    }
                } else {
                    for (int i = 0; i < implementationCount; i++) {
                        implConstraints[i] = defaultConstraints;
                        if (defaultConstraints.getProcessorCoreCount() == 0) {
                            defaultConstraints.setProcessorCoreCount(1);
                        }
                    }
                }
                for (int i = 0; i < implementationCount; i++) {
                    loadMethodConstraints(methodId, implementationCount, declaringClasses, implConstraints, signatures);
                }
            } else { // Service
                integratedtoolkit.types.annotations.Service serviceAnnot = m.getAnnotation(integratedtoolkit.types.annotations.Service.class);
                buffer.append(serviceAnnot.namespace()).append(',').append(serviceAnnot.name()).append(',').append(serviceAnnot.port());
                String signature = buffer.toString();
                Integer methodId = CoreManager.getCoreId(new String[]{signature});
                if (methodId == CoreManager.getCoreCount()) {
                    CoreManager.increaseCoreCount();
                    updatedMethods.add(methodId);
                }
                loadServiceConstraints(methodId, serviceAnnot, new String[]{signature});
            }

        }
        return updatedMethods;
    }

    /**
     * Loads the Constraints in case that core is a service. Only in Xpath
     * format since there are no resource where its tasks can run
     *
     * @param coreId identifier for that core
     * @param service Servive annotation describing the core
     */
    private static void loadServiceConstraints(int coreId, integratedtoolkit.types.annotations.Service service, String[] signatures) {
        Implementation[] implementations = new Implementation[1];
        implementations[0] = new ServiceImplementation(coreId, service.namespace(), service.name(), service.port(), service.operation());
        CoreManager.registerImplementations(coreId, implementations, signatures);
    }

    /**
     * Loads the Constraints in case that core is a service in XPath format and
     * describing the features of the resources able to run its tasks
     *
     * @param coreId identifier for that core
     * @param service Method annotation describing the core
     */
    private static void loadMethodConstraints(int coreId, int implementationCount, String[] declaringClasses, MethodResourceDescription[] cts, String[] signatures) {
        Implementation[] implementations = new Implementation[implementationCount];
        for (int i = 0; i < implementationCount; i++) {
            if (cts[i].getProcessorCoreCount() == 0) {
                cts[i].setProcessorCoreCount(1);
            }
            implementations[i] = new MethodImplementation(declaringClasses[i], coreId, i, cts[i]);
        }
        CoreManager.registerImplementations(coreId, implementations, signatures);
    }

    /**
     * Infers the type of a parameter. If the parameter is annotated as a FILE
     * or a STRING, the type is taken from the annotation. If the annotation is
     * UNSPECIFIED, the type is taken from the formal type.
     *
     * @param formalType Formal type of the parameter
     * @param annotType Annotation type of the parameter
     * @return A String representing the type of the parameter
     */
    private static String inferType(Class<?> formalType, Parameter.Type annotType) {
        if (annotType.equals(Parameter.Type.UNSPECIFIED)) {
            if (formalType.isPrimitive()) {
                if (formalType.equals(boolean.class)) {
                    return "BOOLEAN_T";
                } else if (formalType.equals(char.class)) {
                    return "CHAR_T";
                } else if (formalType.equals(byte.class)) {
                    return "BYTE_T";
                } else if (formalType.equals(short.class)) {
                    return "SHORT_T";
                } else if (formalType.equals(int.class)) {
                    return "INT_T";
                } else if (formalType.equals(long.class)) {
                    return "LONG_T";
                } else if (formalType.equals(float.class)) {
                    return "FLOAT_T";
                } else //if (formalType.equals(double.class))
                {
                    return "DOUBLE_T";
                }
            } /*else if (formalType.isArray()) { // If necessary
             }*/ else { // Object
                return "OBJECT_T";
            }
        } else {
            return annotType + "_T";
        }
    }
}
