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

import integratedtoolkit.ITConstants;
import integratedtoolkit.log.Loggers;
import integratedtoolkit.types.Implementation;
import integratedtoolkit.types.Method;
import integratedtoolkit.types.ResourceDescription;
import integratedtoolkit.types.Service;
import integratedtoolkit.types.annotations.Parameter;
import java.io.BufferedReader;
import java.io.FileReader;
import java.lang.annotation.Annotation;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import org.apache.log4j.Logger;

public class CoreManager {

    // Constants definition
    public static final String NO_CONSTR = "/ResourceList/Resource";
    private static final String CONSTR_LOAD_ERR = "Error loading constraints";
    private static final String LANG_UNSUPPORTED_ERR = "Error loading constraints: Language not supported";

    private static final Logger logger = Logger.getLogger(Loggers.TS_COMP);
    private static ITConstants.Lang lang = ITConstants.Lang.JAVA;
    public static final Map<String, Integer> signatureToId = new HashMap<String, Integer>();
    private static final Map<String, Implementation> signatureToImpl = new HashMap<String, Implementation>();
    public static int coreCount = 0;
    private static int nextId = 0;
    private static Implementation[][] implementations;
    private static Constraints[][] annot;
    private static ResourceDescription[][] resources;

    public static Integer getCoreId(String declaringClass, String methodName, boolean hasTarget, boolean hasReturn, integratedtoolkit.types.Parameter[] parameters) {
        Integer methodId = null;
        String signature = Method.getSignature(declaringClass, methodName, hasTarget, hasReturn, parameters);
        methodId = signatureToId.get(signature);
        if (methodId == null) {
            //coreCount++;
            methodId = nextId++;
            signatureToId.put(signature, methodId);
            if (lang == ITConstants.Lang.PYTHON) {
                implementations[methodId][0] = new Method(declaringClass, methodId, 0, null, new ResourceDescription());
            }
        }
        return methodId;
    }

    public static Integer getCoreId(String namespace, String serviceName, String portName, String operation, boolean hasTarget, boolean hasReturn, integratedtoolkit.types.Parameter[] parameters) {
        Integer methodId = null;
        String signature = Service.getSignature(namespace, serviceName, portName, operation, hasTarget, hasReturn, parameters);
        methodId = signatureToId.get(signature);
        if (methodId == null) {
            //coreCount++;
            methodId = nextId++;
            signatureToId.put(signature, methodId);
        }
        return methodId;
    }

    public static Integer getCoreId(String[] signatures) {
        Integer methodId = null;
        for (int i = 0; i < signatures.length && methodId == null; i++) {
            methodId = signatureToId.get(signatures[i]);
        }
        if (methodId == null) {
            //coreCount++;
            methodId = nextId++;

        }
        for (String signature : signatures) {
            signatureToId.put(signature, methodId);
        }
        return methodId;
    }

    /**
     * *********************************************
     * *********************************************
     * **************LOADING OPERATIONS ************
     * *********************************************
     * *********************************************
     */
    public static void load() {
        String l = System.getProperty(ITConstants.IT_LANG);
        lang = ITConstants.Lang.JAVA;
        if (l != null) {
            if (l.equalsIgnoreCase("c")) {
                lang = ITConstants.Lang.C;
            } else if (l.equalsIgnoreCase("python")) {
                lang = ITConstants.Lang.PYTHON;
            }
        }
        switch (lang) {
            case JAVA:
                String appName = System.getProperty(ITConstants.IT_APP_NAME);
                try {
                    loadJava(Class.forName(appName + "Itf"));
                } catch (ClassNotFoundException ex) {
                    throw new UndefinedConstraintsSourceException(appName + "Itf class cannot be found.");
                }

                break;
            case C:
                String constraintsFile = System.getProperty(ITConstants.IT_CONSTR_FILE);
                loadC(constraintsFile);
                break;
            case PYTHON:
                loadPython();
                break;
            default:
                throw new LangNotDefinedException();
        }
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
        LinkedList<Integer> newMethods = new LinkedList<Integer>();
        int coreCount = annotItfClass.getDeclaredMethods().length;
        logger.debug("Detected methods " + coreCount);
        if (CoreManager.coreCount == 0) {
            annot = new Constraints[coreCount][];
            resources = new ResourceDescription[coreCount][];
            implementations = new Implementation[coreCount][];
        } else {
            updateArrays(CoreManager.coreCount + coreCount);
        }

        for (java.lang.reflect.Method m : annotItfClass.getDeclaredMethods()) {
            //Computes the method's signature
            logger.debug("Evaluating method " + m.getName());
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
                Integer method_id = CoreManager.getCoreId(signatures);
                if (method_id == CoreManager.coreCount) {
                    CoreManager.coreCount++;
                    newMethods.add(method_id);
                }
                Constraints defaultConstraints = new Constraints();
                Constraints[] implConstraints = new Constraints[implementationCount];
                if (m.isAnnotationPresent(integratedtoolkit.types.annotations.Constraints.class)) {
                    defaultConstraints = new Constraints(m.getAnnotation(integratedtoolkit.types.annotations.Constraints.class));
                }
                if (m.isAnnotationPresent(integratedtoolkit.types.annotations.MultiConstraints.class)) {
                    integratedtoolkit.types.annotations.MultiConstraints mc = m.getAnnotation(integratedtoolkit.types.annotations.MultiConstraints.class);
                    mc.value();
                    for (int i = 0; i < implementationCount; i++) {
                        Constraints specificConstraints = new Constraints(mc.value()[i]);
                        specificConstraints.join(defaultConstraints);
                        implConstraints[i] = specificConstraints;
                    }
                } else {
                    for (int i = 0; i < implementationCount; i++) {
                        implConstraints[i] = defaultConstraints;
                    }
                }
                for (int i = 0; i < implementationCount; i++) {
                    loadMethodConstraints(method_id, implementationCount, methodSignature, declaringClasses, implConstraints);
                }

            } else { // Service
                integratedtoolkit.types.annotations.Service serviceAnnot = m.getAnnotation(integratedtoolkit.types.annotations.Service.class);
                buffer.append(serviceAnnot.namespace()).append(',').append(serviceAnnot.name()).append(',').append(serviceAnnot.port());
                String signature = buffer.toString();
                Integer method_id = CoreManager.getCoreId(new String[]{signature});
                if (method_id == CoreManager.coreCount) {
                    CoreManager.coreCount++;
                    newMethods.add(method_id);
                }
                loadServiceConstraints(method_id, signature, serviceAnnot);
            }

        }
        StringBuilder sb= new StringBuilder("CoreElementInterface signatures:\n");
        for (java.util.Map.Entry<String, Implementation> entry:signatureToImpl.entrySet()){
            sb.append("\t signature: ").append(entry.getKey()).append("--> ").append(entry.getValue());
        }
        System.out.println(signatureToImpl);
        return newMethods;
    }

    /**
     * Loads the Constraints in case that core is a service. Only in Xpath
     * format since there are no resource where its tasks can run
     *
     * @param coreId identifier for that core
     * @param service Servive annotation describing the core
     */
    private static void loadServiceConstraints(int coreId, String signature, integratedtoolkit.types.annotations.Service service) {
        annot[coreId] = new Constraints[1];
        resources[coreId] = new ResourceDescription[1];
        implementations[coreId] = new Implementation[1];
        implementations[coreId][0] = new Service(coreId, service.namespace(), service.name(), service.port(), service.operation());
        signatureToImpl.put(signature, implementations[coreId][0]);
    }

    /**
     * Loads the Constraints in case that core is a service in XPath format and
     * describing the features of the resources able to run its tasks
     *
     * @param coreId identifier for that core
     * @param service Method annotation describing the core
     */
    private static void loadMethodConstraints(int coreId, int implementationCount, String signature, String[] declaringClasses, Constraints[] cts) {
        annot[coreId] = new Constraints[implementationCount];
        resources[coreId] = new ResourceDescription[implementationCount];
        implementations[coreId] = new Implementation[implementationCount];

        for (int i = 0; i < implementationCount; i++) {
            annot[coreId] = cts;
            ResourceDescription rm = new ResourceDescription();
            if (cts[i] != null) {
                //specifies the Resources needed to execute the task
                rm.addHostQueue(cts[i].hostQueue());
                rm.setProcessorCPUCount(cts[i].processorCPUCount());
                rm.setProcessorCoreCount(cts[i].processorCoreCount());
                rm.setProcessorSpeed(cts[i].processorSpeed());
                rm.setProcessorArchitecture(cts[i].processorArchitecture());
                rm.setOperatingSystemType(cts[i].operatingSystemType());
                rm.setStorageElemSize(cts[i].storageElemSize());
                rm.setStorageElemAccessTime(cts[i].storageElemAccessTime());
                rm.setStorageElemSTR(cts[i].storageElemSTR());
                rm.setMemoryPhysicalSize(cts[i].memoryPhysicalSize());
                rm.setMemoryVirtualSize(cts[i].memoryVirtualSize());
                rm.setMemoryAccessTime(cts[i].memoryAccessTime());
                rm.setMemorySTR(cts[i].memorySTR());
                String software = cts[i].appSoftware();
                if (software.compareTo("[unassigned]") != 0) {
                    int last = 0;
                    while (software.length() > 0) {
                        last = software.lastIndexOf(",");
                        rm.addAppSoftware(software.substring(last + 1, software.length()));
                        if (last == -1) {
                            software = "";
                        } else {
                            software = software.substring(0, last);
                        }
                    }
                }
                resources[coreId][i] = rm;
            }
            implementations[coreId][i] = new Method(declaringClasses[i], coreId, i, cts[i], rm);
            signatureToImpl.put(signature + declaringClasses[i], implementations[coreId][i]);
        }
    }

    // C constructor
    private static void loadC(String constraintsFile) {
        HashMap<Integer, Method> readMethods = new HashMap<Integer, Method>();
        Constraints defaultCtr = new Constraints();
        defaultCtr.processorCPUCount = 1;
        ResourceDescription defaultRes = new ResourceDescription();
        defaultRes.setProcessorCPUCount(1);
        try {
            BufferedReader br = new BufferedReader(new FileReader(constraintsFile));

            String line;
            int coreCount = 0;
            while ((line = br.readLine()) != null) {
                line = line.trim();
                if (line.startsWith("//")) {
                    continue;
                }
                StringBuilder buffer = new StringBuilder();
                if (line.matches(".*[(].*[)].*;")) {
                    line = line.replaceAll("[(|)|,|;]", " ");
                    String[] splits = line.split("\\s+");
                    String returnType = splits[0];
                    String methodName = splits[1];
                    //String methodName = String.valueOf(n);                    
                    //Computes the method's signature                    
                    buffer.append(methodName).append("(");
                    for (int i = 2; i < splits.length; i++) {
                        String paramDirection = splits[i++];
                        String paramType = splits[i++];
                        String type = "OBJECT_T";
                        if (paramDirection.toUpperCase().compareTo("INOUT") == 0) {
                            type = "FILE_T";
                        } else if (paramDirection.toUpperCase().compareTo("OUT") == 0) {
                            type = "FILE_T";
                        } else if (paramType.toUpperCase().compareTo("FILE") == 0) {
                            type = "FILE_T";
                        } else if (paramType.compareTo("boolean") == 0) {
                            type = "BOOLEAN_T";
                        } else if (paramType.compareTo("char") == 0) {
                            type = "CHAR_T";
                        } else if (paramType.compareTo("int") == 0) {
                            type = "INT_T";
                        } else if (paramType.compareTo("float") == 0) {
                            type = "FLOAT_T";
                        } else if (paramType.compareTo("double") == 0) {
                            type = "DOUBLE_T";
                        } else if (paramType.compareTo("byte") == 0) {
                            type = "BYTE_T";
                        } else if (paramType.compareTo("short") == 0) {
                            type = "SHORT_T";
                        } else if (paramType.compareTo("long") == 0) {
                            type = "LONG_T";
                        } else if (paramType.compareTo("string") == 0) {
                            type = "STRING_T";
                        }
                        buffer.append(type).append(",");
                        String paramName = splits[i];
                    }
                    buffer.deleteCharAt(buffer.lastIndexOf(","));
                    buffer.append(")");
                    String declaringClass = "NULL";
                    buffer.append(declaringClass);

                    String signature = buffer.toString();
                    //Adds a new Signature-Id if not exists in the TreeMap
                    Integer methodId = CoreManager.getCoreId(new String[]{signature});
                    Method m = new Method(declaringClass, methodId, 0, defaultCtr, defaultRes);
                    readMethods.put(methodId, m);
                    coreCount++;
                }
            }

            annot = new Constraints[coreCount][1];
            resources = new ResourceDescription[coreCount][1];
            implementations = new Implementation[coreCount][1];
            for (int i = 0; i < coreCount; i++) {
                annot[i][0] = defaultCtr;
                resources[i][0] = defaultRes;
                implementations[i][0] = readMethods.get(i);
            }
            CoreManager.coreCount = coreCount;
        } catch (Exception e) {
            logger.fatal(CONSTR_LOAD_ERR, e);
        }

    }

    // Python constructor
    private static void loadPython() {
        String countProp = System.getProperty(ITConstants.IT_CORE_COUNT);
        Integer coreCount;
        if (countProp == null) {
            coreCount = 50;
            logger.debug("Warning: using " + coreCount + " as default for number of task types");
        } else {
            coreCount = Integer.parseInt(countProp);
            logger.debug("Core count is " + coreCount);
        }
        annot = new Constraints[coreCount][1];
        resources = new ResourceDescription[coreCount][1];
        implementations = new Implementation[coreCount][1];
        Constraints defaultCtr = new Constraints();
        defaultCtr.processorCPUCount = 1;
        ResourceDescription defaultRes = new ResourceDescription();
        defaultRes.setProcessorCPUCount(1);
        for (int i = 0; i < coreCount; i++) {
            annot[i][0] = defaultCtr;
            resources[i][0] = defaultRes;
            implementations[i][0] = new Method("NULL", i, 0, defaultCtr, defaultRes);
        }
        CoreManager.coreCount = coreCount;
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

    private static void updateArrays(int newCoreCount) {
        Implementation[][] oldImplementations = implementations;
        Constraints[][] oldAnnot = annot;
        ResourceDescription[][] oldResources = resources;

        annot = new Constraints[newCoreCount][];
        resources = new ResourceDescription[newCoreCount][];
        implementations = new Implementation[newCoreCount][];

        System.arraycopy(oldAnnot, 0, annot, 0, oldAnnot.length);
        System.arraycopy(oldResources, 0, resources, 0, oldResources.length);
        System.arraycopy(oldImplementations, 0, implementations, 0, oldImplementations.length);
    }

    public static Implementation getImplementation(String signature) {
        return signatureToImpl.get(signature);
    }

    //CUSTOM EXCEPTIONS
    public static class LangNotDefinedException extends RuntimeException {

        public LangNotDefinedException() {
            super(LANG_UNSUPPORTED_ERR);
        }
    }

    public static class UndefinedConstraintsSourceException extends RuntimeException {

        public UndefinedConstraintsSourceException(String message) {
            super(message);
        }
    }

    /**
     * *********************************************
     * *********************************************
     * ************** QUERY OPERATIONS *************
     * *********************************************
     * *********************************************
     */
    /**
     * Returns the description of the resources that are able to run that core
     *
     * @param coreId identifier of the core
     * @return the description of the resources that are able to run that core
     */
    public static ResourceDescription[] getResourceConstraints(int coreId) {
        return resources[coreId];
    }

    /**
     * Returns all the implementations of a core Element
     *
     * @return the implementations for a Core Element
     */
    public static Implementation[] getCoreImplementations(int coreId) {
        return implementations[coreId];
    }

    /**
     * Looks for all the cores from in the annotated Interface which constraint
     * are fullfilled by the resource description passed as a parameter
     *
     * @param rd description of the resource
     * @return the list of cores which constraints are fulfilled by rd
     */
    public static List<Integer> findExecutableCores(ResourceDescription rd) {
        LinkedList<Integer> executableList = new LinkedList<Integer>();
        for (int method_id = 0; method_id < CoreManager.coreCount; method_id++) {
            boolean executable = false;
            for (int implementationId = 0; !executable && implementationId < annot[method_id].length; implementationId++) {
                Constraints mc = annot[method_id][implementationId];
                executable = true;
                if (mc != null) {
                    //Check processor
                    if (executable && mc.processorCPUCount() != 0) {
                        executable = (mc.processorCPUCount() <= rd.getProcessorCoreCount());
                    }
                    if (executable && mc.processorSpeed() != 0.0f) {
                        executable = (mc.processorSpeed() <= rd.getProcessorSpeed());
                    }
                    if (executable && mc.processorArchitecture().compareTo("[unassigned]") != 0) {
                        executable = (rd.getProcessorArchitecture().compareTo(mc.processorArchitecture()) == 0);
                    }

                    //Check Memory
                    if (executable && mc.memoryPhysicalSize() != 0.0f) {
                        executable = ((int) ((Float) mc.memoryPhysicalSize() * (Float) 1024f) <= (int) ((Float) rd.getMemoryPhysicalSize() * (Float) 1024f));
                    }
                    if (executable && mc.memoryVirtualSize() != 0.0f) {
                        executable = ((int) ((Float) mc.memoryVirtualSize() * (Float) 1024f) <= (int) ((Float) rd.getMemoryVirtualSize() * (Float) 1024f));
                    }
                    if (executable && mc.memoryAccessTime() != 0.0f) {
                        executable = (mc.memoryAccessTime() >= rd.getMemoryAccessTime());
                    }
                    if (executable && mc.memorySTR() != 0.0f) {
                        executable = (mc.memorySTR() <= rd.getMemorySTR());
                    }

                    //Check disk
                    if (executable && mc.storageElemSize() != 0.0f) {
                        executable = ((int) ((Float) mc.storageElemSize() * (Float) 1024f) <= (int) ((Float) rd.getStorageElemSize() * (Float) 1024f));
                    }
                    if (executable && mc.storageElemAccessTime() != 0.0f) {
                        executable = (mc.storageElemAccessTime() >= rd.getStorageElemAccessTime());
                    }
                    if (executable && mc.storageElemSTR() != 0.0f) {
                        executable = (mc.storageElemSTR() <= rd.getStorageElemSTR());
                    }

                    //Check OS
                    if (executable && mc.operatingSystemType().compareTo("[unassigned]") != 0) {
                        executable = (rd.getOperatingSystemType().compareTo(mc.operatingSystemType()) == 0);
                    }
                }
                if (executable) {
                    executableList.add(method_id);
                }
            }
        }
        return executableList;
    }

    public static class Constraints implements integratedtoolkit.types.annotations.Constraints {

        String processorArchitecture = "[unassigned]";

        int processorCPUCount = 1;
        int processorCoreCount = 1;
        float processorSpeed = 0;
        float memoryPhysicalSize = 0;       // in GB
        float memoryVirtualSize = 0;        // in GB
        float memoryAccessTime = 0;         // in ns
        float memorySTR = 0;                // in GB/s
        float storageElemSize = 0;          // in GB
        float storageElemAccessTime = 0;    // in ms
        float storageElemSTR = 0;           // in MB/s
        String operatingSystemType = "[unassigned]";
        String hostQueue = "[unassigned]";
        String appSoftware = "[unassigned]";

        private Constraints() {
        }

        private Constraints(integratedtoolkit.types.annotations.Constraints m) {
            if (m == null) {
                // Default constraints
                this.processorCPUCount = 1;
            } else {
                this.processorCPUCount = m.processorCPUCount();
                this.processorCoreCount = m.processorCoreCount();
                this.processorSpeed = m.processorSpeed();
                this.processorArchitecture = m.processorArchitecture();
                this.memoryPhysicalSize = m.memoryPhysicalSize();
                this.memoryVirtualSize = m.memoryVirtualSize();
                this.memoryAccessTime = m.memoryAccessTime();
                this.memorySTR = m.memorySTR();
                this.storageElemSize = m.storageElemSize();
                this.storageElemAccessTime = m.storageElemAccessTime();
                this.storageElemSTR = m.storageElemSTR();
                this.operatingSystemType = m.operatingSystemType();
                this.hostQueue = m.hostQueue();
                this.appSoftware = m.appSoftware();
            }
        }

        public String processorArchitecture() {
            return this.processorArchitecture;
        }

        public int processorCoreCount() {
            return this.processorCoreCount;
        }

        public int processorCPUCount() {
            return this.processorCPUCount;
        }

        public float processorSpeed() {
            return this.processorSpeed;
        }

        public float memoryPhysicalSize() {
            return this.memoryPhysicalSize;
        }

        public float memoryVirtualSize() {
            return this.memoryVirtualSize;
        }

        public float memoryAccessTime() {
            return this.memoryAccessTime;
        }

        public float memorySTR() {
            return this.memorySTR;
        }

        public float storageElemSize() {
            return this.storageElemSize;
        }

        public float storageElemAccessTime() {
            return this.storageElemAccessTime;
        }

        public float storageElemSTR() {
            return this.storageElemSTR;
        }

        public String operatingSystemType() {
            return this.operatingSystemType;
        }

        public String hostQueue() {
            return this.hostQueue;
        }

        public String appSoftware() {
            return this.appSoftware;
        }

        public Class<? extends Annotation> annotationType() {
            return integratedtoolkit.types.annotations.Constraints.class;
        }

        private void join(Constraints defaultConstraints) {

            if (this.processorCPUCount == 1) {
                this.processorCPUCount = defaultConstraints.processorCPUCount;
            }
            if (this.processorCoreCount == 1) {
                this.processorCoreCount = defaultConstraints.processorCoreCount;
            }
            if (this.processorSpeed == 0) {
                this.processorSpeed = defaultConstraints.processorSpeed;
            }
            if (this.processorArchitecture.compareTo("[unassigned]") == 0) {
                this.processorArchitecture = defaultConstraints.processorArchitecture;
            }

            if (this.memoryPhysicalSize == 0) {
                this.memoryPhysicalSize = defaultConstraints.memoryPhysicalSize;
            }
            if (this.memoryVirtualSize == 0) {
                this.memoryVirtualSize = defaultConstraints.memoryVirtualSize;
            }
            if (this.memoryAccessTime == 0) {
                this.memoryAccessTime = defaultConstraints.memoryAccessTime;
            }
            if (this.memorySTR == 0) {
                this.memorySTR = defaultConstraints.memorySTR;
            }

            if (this.storageElemSize == 0) {
                this.storageElemSize = defaultConstraints.storageElemSize;
            }
            if (this.storageElemAccessTime == 0) {
                this.storageElemAccessTime = defaultConstraints.storageElemAccessTime;
            }
            if (this.storageElemSTR == 0) {
                this.storageElemSTR = defaultConstraints.storageElemSTR;
            }

            if (this.operatingSystemType.compareTo("[unassigned]") == 0) {
                this.operatingSystemType = defaultConstraints.operatingSystemType;
            }
            if (this.hostQueue.compareTo("[unassigned]") == 0) {
                this.hostQueue = defaultConstraints.hostQueue;
            }
            if (this.appSoftware.compareTo("[unassigned]") != 0) {
                if (defaultConstraints.appSoftware.compareTo("[unassigned]") != 0) {
                    this.appSoftware = this.appSoftware + "," + defaultConstraints.appSoftware;
                }
            } else {
                this.appSoftware = defaultConstraints.appSoftware;
            }
        }
    }

}
