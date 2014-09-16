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

import java.io.Serializable;
import integratedtoolkit.api.ITExecution.*;
import integratedtoolkit.util.CoreManager;

public class TaskParams implements Serializable {

    public enum Type {

        SERVICE,
        METHOD
    }
    private final Integer coreId;
    private final String methodName;
    private final Parameter[] parameters;
    private final boolean priority;
    private final boolean hasTarget;
    private final boolean hasReturn;
    private final Type type;

    public TaskParams(String methodClass, String methodName, boolean priority, boolean hasTarget, Parameter[] parameters) {
        this.methodName = methodName;
        this.priority = priority;
        this.hasTarget = hasTarget;
        this.parameters = parameters;
        if (parameters.length == 0) {
            this.hasReturn = false;
        } else {
            Parameter lastParam = parameters[parameters.length - 1];
            this.hasReturn = (lastParam.getDirection() == ParamDirection.OUT && lastParam.getType() == ParamType.OBJECT_T);
        }
        this.coreId = CoreManager.getCoreId(methodClass, methodName, hasTarget, hasReturn, parameters);
        type = Type.METHOD;
    }

    public TaskParams(String namespace, String service, String port, String operation, boolean priority, boolean hasTarget, Parameter[] parameters) {
        this.methodName = operation;
        this.priority = priority;
        this.hasTarget = hasTarget;
        this.parameters = parameters;
        if (parameters.length == 0) {
            this.hasReturn = false;
        } else {
            Parameter lastParam = parameters[parameters.length - 1];
            this.hasReturn = (lastParam.getDirection() == ParamDirection.OUT && lastParam.getType() == ParamType.OBJECT_T);
        }
        this.coreId = CoreManager.getCoreId(namespace, service, port, operation, hasTarget, hasReturn, parameters);
        this.type = Type.SERVICE;
    }

    public Integer getId() {
        return coreId;
    }

    public String getName() {
        return methodName;
    }

    public Parameter[] getParameters() {
        return parameters;
    }

    public boolean hasPriority() {
        return priority;
    }

    public boolean hasTargetObject() {
        return hasTarget;
    }

    public boolean hasReturnValue() {
        return hasReturn;
    }

    public Type getType() {
        return this.type;
    }

    public String toString() {
        StringBuilder buffer = new StringBuilder();

        buffer.append("[Core id: ").append(getId()).append("]");
        buffer.append(", [Priority: ").append(hasPriority()).append("]");
        buffer.append(", [").append(getName()).append("(");
        int numParams = parameters.length;
        if (hasTargetObject()) {
            numParams--;
        }
        if (hasReturnValue()) {
            numParams--;
        }
        if (numParams > 0) {
            buffer.append(parameters[0].getType());
            for (int i = 1; i < numParams; i++) {
                buffer.append(", ").append(parameters[i].getType());
            }
        }
        buffer.append(")]");
        return buffer.toString();
    }

}
