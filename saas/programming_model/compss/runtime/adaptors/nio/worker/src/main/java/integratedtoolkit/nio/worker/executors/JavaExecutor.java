/*
 *  Copyright 2002-2012 Barcelona Supercomputing Center (www.bsc.es)
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
package integratedtoolkit.nio.worker.executors;

import integratedtoolkit.nio.NIOParam;
import integratedtoolkit.nio.NIOTask;
import integratedtoolkit.nio.worker.NIOWorker;
import java.lang.reflect.Method;
import java.util.Iterator;

public class JavaExecutor extends Executor {

    @Override
    String createSandBox() {
        //SandBox not supported
        return null;
    }

    @Override
    void executeTask(String sandBox, NIOTask nt, NIOWorker nw) throws Exception {
        boolean debug = NIOWorker.debug;
        String className = nt.getClassName();
        String methodName = nt.getMethodName();
        boolean hasTarget = nt.isHasTarget();
        int numParams = nt.getNumParams();
        Class<?> types[];
        Object values[];
        if (hasTarget) {
            // The target object of the last parameter before the return value (if any)
            types = new Class[numParams - 1];
            values = new Object[numParams - 1];
        } else {
            types = new Class[numParams];
            values = new Object[numParams];
        }
        boolean mustWrite[] = new boolean[numParams];
        String renamings[] = new String[numParams];

        // Parse the parameter types and values
        Object target = null;

        Iterator<NIOParam> params = nt.getParams().iterator();
        for (int i = 0; i < numParams; i++) {
            NIOParam np = params.next();
            // We need to use wrapper classes for basic types, reflection will unwrap automatically
            switch (np.getType()) {
                case FILE_T:
                    types[i] = String.class;
                    values[i] = np.getValue();
                    break;
                case OBJECT_T:
                    String renaming = renamings[i] = np.getValue().toString();
                    String name = renaming;
                    mustWrite[i] = np.isWrite();
                    Object o = nw.getObject(name);
                    if (hasTarget && i == numParams - 1) { // last parameter is the target object
                        if (o == null) {
                            throw new JobExecutionException("Target object with renaming " + name + ", method " + methodName + ", class " + className + " is null!" + "\n");
                        }
                        target = o;
                    } else {
                        if (o == null) {
                            throw new JobExecutionException("Object parameter " + i + " with renaming " + name + ", method " + methodName + ", class " + className + " is null!" + "\n");
                        }
                        types[i] = o.getClass();
                        values[i] = o;
                    }
                    break;
                case BOOLEAN_T:
                    types[i] = boolean.class;
                    values[i] = np.getValue();
                    break;
                case CHAR_T:
                    types[i] = char.class;
                    values[i] = np.getValue();
                    break;
                case STRING_T:
                    types[i] = String.class;
                    values[i] = np.getValue();
                    break;
                case BYTE_T:
                    types[i] = byte.class;
                    values[i] = np.getValue();
                    break;
                case SHORT_T:
                    types[i] = short.class;
                    values[i] = np.getValue();
                    break;
                case INT_T:
                    types[i] = int.class;
                    values[i] = np.getValue();
                    break;
                case LONG_T:
                    types[i] = long.class;
                    values[i] = np.getValue();
                    break;
                case FLOAT_T:
                    types[i] = float.class;
                    values[i] = np.getValue();
                    break;
                case DOUBLE_T:
                    types[i] = double.class;
                    values[i] = np.getValue();
                    break;
            }
        }
        if (debug) {
            // Print request information
            System.out.println("WORKER - Parameters of execution:");
            System.out.println("  * Method class: " + className);
            System.out.println("  * Method name: " + methodName);
            System.out.print("  * Parameter types:");
            for (int i = 0; i < types.length; i++) {
                System.out.print(" " + types[i].getName());

            }
            System.out.println();
            System.out.print("  * Parameter values:");
            for (Object v : values) {
                System.out.print(" " + v);
            }
            System.out.println();
        }

        // Use reflection to get the requested method
        Class<?> methodClass = null;
        Method method = null;
        try {
            methodClass = Class.forName(className);
        } catch (Exception e) {
            throw new JobExecutionException("Can not get class by reflection", e);
        }
        try {
            method = methodClass.getMethod(methodName, types);
        } catch (Exception e) {
            throw new JobExecutionException("Can not get method by reflection", e);
        }

        // Invoke the requested method
        Object retValue = null;
        retValue = method.invoke(target, values);

        // Write to disk the updated object parameters, if any (including the target)
        for (int i = 0; i < numParams; i++) {
            if (mustWrite[i]) {
                if (hasTarget && i == numParams - 1) {
                    nw.storeInCache(renamings[i], target);
                } else {
                    nw.storeInCache(renamings[i], values[i]);
                }
            }
        }

        // Serialize the return value if existing
        if (retValue != null) {
            String renaming = (String) nt.getParams().getLast().getValue();
            nw.storeInCache(renaming.substring(renaming.lastIndexOf('/') + 1), retValue);
        }
    }

    @Override
    void removeSandBox(String sandBox) {
        //SandBox not supported
    }

}
