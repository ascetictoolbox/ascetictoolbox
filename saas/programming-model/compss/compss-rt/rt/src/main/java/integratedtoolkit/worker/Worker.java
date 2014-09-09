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




package integratedtoolkit.worker;

import integratedtoolkit.util.Serializer;

import java.lang.reflect.Method;
/**
 * The worker class is executed on the remote resources in order to execute the 
 * tasks.
 */
public class Worker {

    // Parameter type constants
    private static final int FILE_PAR = 0;
    private static final int BOOL_PAR = 1;
    private static final int CHAR_PAR = 2;
    private static final int STRING_PAR = 3;
    private static final int BYTE_PAR = 4;
    private static final int SHORT_PAR = 5;
    private static final int INT_PAR = 6;
    private static final int LONG_PAR = 7;
    private static final int FLOAT_PAR = 8;
    private static final int DOUBLE_PAR = 9;
    private static final int OBJECT_PAR = 10;
    private static final int NUM_HEADER_PARS = 5;

    /**
     * Executes a method taking into acount the parameters. First it parses the
     * parameters assigning values and deserializing Read/creating empty ones 
     * for Write. Invokes the desired method by reflection and serilizes all the
     * objects that has been modified and the result.
     * 
     * @param args args for the execution:
     *  arg[0]: boolean enable debug
     *  arg[1]: String implementing core class name
     *  arg[2]: String core method name
     *  arg[3]: boolean is the method executed on a certain instance
     *  arg[4]: integer amount of parameters of the method
     *  arg[5+]: parameters of the method
     *  For each parameter:
     *      type: 0-10 (file, boolean, char, string, byte, short, int, long, float, double, object)
     *      [substrings: amount of substrings (only used when the type is string)]
     *      value: value for the parameter or the file where it is contained (for objects and files)
     *      [Direction: R/W (only used when the type is object)]
     */
    public static void main(String args[]) {
        boolean debug = Boolean.parseBoolean(args[0]);
        String className = args[1];
        String methodName = args[2];
        boolean hasTarget = Boolean.parseBoolean(args[3]);
        int numParams = Integer.parseInt(args[4]);

        if (args.length < 2 * numParams + NUM_HEADER_PARS) {
            System.err.println("Insufficient number of arguments");
            System.exit(1);
        }

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
        int pos = NUM_HEADER_PARS;
        Object target = null;
        for (int i = 0; i < numParams; i++) {
            // We need to use wrapper classes for basic types, reflection will unwrap automatically
            switch (Integer.parseInt(args[pos])) {
                case FILE_PAR:
                    types[i] = String.class;
                    values[i] = args[pos + 1];
                    break;
                case OBJECT_PAR:
                    String renaming = renamings[i] = (String) args[pos + 1];
                    mustWrite[i] = ((String) args[pos + 2]).equals("W");
                    Object o = null;
                    try {
                        o = Serializer.deserialize(renaming);
                    } catch (Exception e) {
                        System.err.println("Error deserializing object parameter " + i + " with renaming " + renaming + ", method " + methodName + ", class " + className);
                        System.exit(1);
                    }
                    if (hasTarget && i == numParams - 1) // last parameter is the target object
                    {
                        if (o == null) {
                            System.err.println("Target object with renaming " + renaming + ", method " + methodName + ", class " + className + " is null!");
                            System.exit(1);
                        }
                        target = o;
                    } else {
                        if (o == null) {
                            System.err.println("Object parameter " + i + " with renaming " + renaming + ", method " + methodName + ", class " + className + " is null!");
                            System.exit(1);
                        }
                        types[i] = o.getClass();
                        values[i] = o;
                    }
                    pos++;
                    break;
                case BOOL_PAR:
                    types[i] = boolean.class;
                    values[i] = new Boolean(args[pos + 1]);
                    break;
                case CHAR_PAR:
                    types[i] = char.class;
                    values[i] = new Character(args[pos + 1].charAt(0));
                    break;
                case STRING_PAR:
                    types[i] = String.class;
                    int numSubStrings = Integer.parseInt(args[pos + 1]);
                    String aux = "";
                    for (int j = 2; j <= numSubStrings + 1; j++) {
                        aux += args[pos + j];
                        if (j < numSubStrings + 1) {
                            aux += " ";
                        }
                    }
                    values[i] = aux;
                    pos += numSubStrings;
                    break;
                case BYTE_PAR:
                    types[i] = byte.class;
                    values[i] = new Byte(args[pos + 1]);
                    break;
                case SHORT_PAR:
                    types[i] = short.class;
                    values[i] = new Short(args[pos + 1]);
                    break;
                case INT_PAR:
                    types[i] = int.class;
                    values[i] = new Integer(args[pos + 1]);
                    break;
                case LONG_PAR:
                    types[i] = long.class;
                    values[i] = new Long(args[pos + 1]);
                    break;
                case FLOAT_PAR:
                    types[i] = float.class;
                    values[i] = new Float(args[pos + 1]);
                    break;
                case DOUBLE_PAR:
                    types[i] = double.class;
                    values[i] = new Double(args[pos + 1]);
                    break;
            }
            pos += 2;
        }

        if (debug) {
            // Print request information
            System.out.println("WORKER - Parameters of execution:");
            System.out.println("  * Method class: " + className);
            System.out.println("  * Method name: " + methodName);
            System.out.print("  * Parameter types:");
            for (Class<?> c : types) {
                System.out.print(" " + c.getName());
            }
            System.out.println("");
            System.out.print("  * Parameter values:");
            for (Object v : values) {
                System.out.print(" " + v);
            }
            System.out.println("");
        }

        // Use reflection to get the requested method
        Method method = null;
        try {
            Class<?> methodClass = Class.forName(className);
            method = methodClass.getMethod(methodName, types);
        } catch (ClassNotFoundException e) {
            System.err.println("Application class not found");
            System.exit(1);
        } catch (SecurityException e) {
            System.err.println("Security exception");
            e.printStackTrace();
            System.exit(1);
        } catch (NoSuchMethodException e) {
            System.err.println("Requested method not found");
            System.exit(1);
        }

        // Invoke the requested method
        Object retValue = null;
        try {
            retValue = method.invoke(target, values);
        } catch (Exception e) {
            System.err.println("Error invoking requested method");
            e.printStackTrace();
            System.exit(1);
        }

        // Write to disk the updated object parameters, if any (including the target)
        for (int i = 0; i < numParams; i++) {
            if (mustWrite[i]) {
                try {
                    if (hasTarget && i == numParams - 1) {
                        Serializer.serialize(target, renamings[i]);
                    } else {
                        Serializer.serialize(values[i], renamings[i]);
                    }
                } catch (Exception e) {
                    System.err.println("Error serializing object parameter " + i + " with renaming " + renamings[i] + ", method " + methodName + ", class " + className);
                    System.exit(1);
                }
            }
        }

        // Serialize the return value if existing
        if (retValue != null) {
            String renaming = (String) args[pos + 1];
            try {
                Serializer.serialize(retValue, renaming);
            } catch (Exception e) {
                System.err.println("Error serializing object return value with renaming " + renaming + ", method " + methodName + ", class " + className);
                System.exit(1);
            }
        }
    }
}
