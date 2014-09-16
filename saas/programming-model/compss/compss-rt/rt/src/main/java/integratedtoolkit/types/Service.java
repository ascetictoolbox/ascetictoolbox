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

public class Service extends Implementation {

    private final String namespace;
    private final String serviceName;
    private final String portName;
    private final String operation;

    public Service(int coreId, String namespace, String service, String port, String operation) {
        super(coreId, 0, null, null);
        this.namespace = namespace;
        this.serviceName = service;
        this.portName = port;
        this.operation = operation;
    }

    public String getNamespace() {
        return namespace;
    }

    public String getServiceName() {
        return serviceName;
    }

    public String getPortName() {
        return portName;
    }

    public String getOperation() {
        return operation;
    }

    public static String getSignature(String namespace, String serviceName, String portName, String operation, boolean hasTarget, boolean hasReturn, Parameter[] parameters) {
        StringBuilder buffer = new StringBuilder();

        buffer.append(operation).append("(");
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
        buffer.append(")")
                .append(namespace).append(',')
                .append(serviceName).append(',')
                .append(portName);

        return buffer.toString();
    }

    @Override
    public Type getType() {
        return Type.SERVICE;
    }

    public String toString() {
        return super.toString()
                + " Service in namespace " + namespace
                + " with name " + serviceName
                + " on port " + portName
                + "and operation " + operation;
    }

}
