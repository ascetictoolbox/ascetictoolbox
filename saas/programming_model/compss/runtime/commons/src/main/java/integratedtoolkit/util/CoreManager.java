package integratedtoolkit.util;

import integratedtoolkit.ITConstants;
import integratedtoolkit.types.Implementation;
import integratedtoolkit.types.MethodImplementation;
import integratedtoolkit.types.ServiceImplementation;
import integratedtoolkit.types.exceptions.NonInstantiableException;
import integratedtoolkit.types.resources.ResourceDescription;
import integratedtoolkit.types.resources.MethodResourceDescription;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class CoreManager {

    // Constants definition
    private static final String LANG_UNSUPPORTED_ERR = "Error loading constraints: Language not supported";

    private static ITConstants.Lang lang = ITConstants.Lang.JAVA;

    public static final Map<String, Integer> SIGNATURE_TO_ID = new HashMap<String, Integer>();
    private static int coreCount = 0;
    private static int nextId = 0;

    private static Implementation[][] implementations;

    static {
        String l = System.getProperty(ITConstants.IT_LANG);
        lang = ITConstants.Lang.JAVA;
        if (l != null) {
            if (("c").equalsIgnoreCase(l)) {
                lang = ITConstants.Lang.C;
            } else if (("python").equalsIgnoreCase(l)) {
                lang = ITConstants.Lang.PYTHON;
            }
        }
    }

    private CoreManager() {
        throw new NonInstantiableException("CoreManager");
    }

    public static void increaseCoreCount() {
        coreCount++;
    }

    public static void setCoreCount(int newCoreCount) {
        coreCount = newCoreCount;
    }

    public static int getCoreCount() {
        return coreCount;
    }

    public static void resizeStructures(int newCoreCount) {
        if (implementations != null) {
            Implementation[][] oldImplementations = implementations;
            implementations = new Implementation[newCoreCount][];
            System.arraycopy(oldImplementations, 0, implementations, 0, oldImplementations.length);
        } else {
            implementations = new Implementation[newCoreCount][];
        }
    }

    public static void registerImplementations(int coreId, Implementation[] impls) {
        implementations[coreId] = impls;
    }

    public static Integer getCoreId(String declaringClass, String methodName, boolean hasTarget, boolean hasReturn, integratedtoolkit.types.parameter.Parameter[] parameters) {
        Integer methodId = null;
        String signature = MethodImplementation.getSignature(declaringClass, methodName, hasTarget, hasReturn, parameters);
        methodId = SIGNATURE_TO_ID.get(signature);
        if (methodId == null) {
            methodId = nextId++;
            SIGNATURE_TO_ID.put(signature, methodId);
            if (lang == ITConstants.Lang.PYTHON) {
                MethodResourceDescription rd = new MethodResourceDescription();
                rd.setProcessorCoreCount(1);
                ((MethodImplementation) implementations[methodId][0]).setDeclaringClass(declaringClass);
            }
        }
        return methodId;
    }

    public static Integer getCoreId(String namespace, String serviceName, String portName, String operation, boolean hasTarget, boolean hasReturn, integratedtoolkit.types.parameter.Parameter[] parameters) {
        Integer methodId = null;
        String signature = ServiceImplementation.getSignature(namespace, serviceName, portName, operation, hasTarget, hasReturn, parameters);
        methodId = SIGNATURE_TO_ID.get(signature);
        if (methodId == null) {
            methodId = nextId++;
            SIGNATURE_TO_ID.put(signature, methodId);
        }
        return methodId;
    }

    public static Integer getCoreId(String[] signatures) {
        Integer methodId = null;
        for (int i = 0; i < signatures.length && methodId == null; i++) {
            methodId = SIGNATURE_TO_ID.get(signatures[i]);
        }
        if (methodId == null) {
            methodId = nextId++;
        }
        for (String signature : signatures) {
            SIGNATURE_TO_ID.put(signature, methodId);
        }
        return methodId;
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
     * @param rd ResourceDescription to find cores compatible to
     *
     * @return the list of cores which constraints are fulfilled by th described
     * resource
     */
    public static List<Integer> findExecutableCores(ResourceDescription rd) {
        List<Integer> executableList = new LinkedList<Integer>();
        for (int methodId = 0; methodId < CoreManager.coreCount; methodId++) {
            boolean executable = false;
            for (int implementationId = 0; !executable && implementationId < implementations[methodId].length; implementationId++) {
                if (rd.canHost(implementations[methodId][implementationId])) {
                    executableList.add(methodId);
                }
            }
        }
        return executableList;
    }

    public static String debugString() {
        StringBuilder sb = new StringBuilder();
        sb.append("Core Count: ").append(coreCount).append("\n");
        for (int coreId = 0; coreId < coreCount; coreId++) {
            Implementation[] impls = implementations[coreId];
            sb.append("\tCore ").append(coreId).append(":\n");
            for (Implementation impl : impls) {
                sb.append("\t\t -").append(impl.toString()).append("\n");
            }
        }
        return sb.toString();
    }
}
