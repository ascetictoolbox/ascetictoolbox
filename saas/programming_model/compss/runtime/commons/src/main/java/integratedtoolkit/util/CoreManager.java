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

    public static final Map<String, Implementation> SIGNATURE_TO_IMPL = new HashMap<String, Implementation>();
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

    public static Implementation getImplementation(String signature) {
        return SIGNATURE_TO_IMPL.get(signature);
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

    public static void registerImplementations(int coreId, Implementation[] impls, String[] signatures) {
        implementations[coreId] = impls;
        for (int i = 0; i < signatures.length; i++) {
            SIGNATURE_TO_IMPL.put(signatures[i], impls[i]);
        }
    }

    public static Integer getCoreId(String declaringClass, String methodName, boolean hasTarget, boolean hasReturn, integratedtoolkit.types.parameter.Parameter[] parameters) {
        Implementation impl = null;
        String signature = MethodImplementation.getSignature(declaringClass, methodName, hasTarget, hasReturn, parameters);
        impl = SIGNATURE_TO_IMPL.get(signature);
        Integer methodId = null;
        if (impl == null) {
            methodId = nextId++;
            if (lang == ITConstants.Lang.PYTHON) {
                MethodResourceDescription rd = new MethodResourceDescription();
                rd.setProcessorCoreCount(1);
                ((MethodImplementation) implementations[methodId][0]).setDeclaringClass(declaringClass);
            }
        } else {
            methodId = impl.getCoreId();
        }
        return methodId;
    }

    public static Integer getCoreId(String namespace, String serviceName, String portName, String operation, boolean hasTarget, boolean hasReturn, integratedtoolkit.types.parameter.Parameter[] parameters) {
        Implementation impl = null;
        String signature = ServiceImplementation.getSignature(namespace, serviceName, portName, operation, hasTarget, hasReturn, parameters);
        impl = SIGNATURE_TO_IMPL.get(signature);
        Integer methodId = null;
        if (impl == null) {
            methodId = nextId++;
        } else {
            methodId = impl.getCoreId();
        }
        return methodId;
    }

    public static Integer getCoreId(String[] signatures) {
        Implementation impl = null;
        Integer methodId = null;
        for (int i = 0; i < signatures.length && methodId == null; i++) {
            impl = SIGNATURE_TO_IMPL.get(signatures[i]);
        }
        if (impl == null) {
            methodId = nextId++;
        } else {
            methodId = impl.getCoreId();
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
