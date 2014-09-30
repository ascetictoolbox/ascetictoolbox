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
package integratedtoolkit.types;

import integratedtoolkit.util.CoreManager;
import integratedtoolkit.ITConstants;

public class Method extends Implementation {

    private static ITConstants.Lang lang;

    static {
        lang = ITConstants.Lang.JAVA;
        String l = System.getProperty(ITConstants.IT_LANG);
        if (l != null) {
            if (l.equalsIgnoreCase("c")) {
                lang = ITConstants.Lang.C;
            } else if (l.equalsIgnoreCase("python")) {
                lang = ITConstants.Lang.PYTHON;
            }
        }
    }
    private final String declaringClass;

    public Method(String methodClass, int coreId, int implementationId, CoreManager.Constraints annot, ResourceDescription resource) {
        super(coreId, implementationId, annot, resource);
        this.declaringClass = methodClass;
    }

    public String getDeclaringClass() {
        return declaringClass;
    }

    public static String getSignature(String declaringClass, String methodName, boolean hasTarget, boolean hasReturn, Parameter[] parameters) {
        StringBuilder buffer = new StringBuilder();

        buffer.append(methodName).append("(");

        if (lang != ITConstants.Lang.PYTHON) { // there is no function overloading in Python
            int numPars = parameters.length;
            if (hasTarget) {
                numPars--;
            }
            if (hasReturn) {
                numPars--;
            }
            if (numPars > 0) {
                buffer.append(parameters[0].getType());
                for (int i = 1; i < numPars; i++) {
                    buffer.append(",").append(parameters[i].getType());
                }
            }
        }
        buffer.append(")").append(declaringClass);
        return buffer.toString();
    }

    @Override
    public Type getType() {
        return Type.METHOD;
    }

    public String toString() {
        return super.toString() + " Method declared in class " + declaringClass;
    }

}
