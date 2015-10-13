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

package integratedtoolkit.types;

import integratedtoolkit.types.parameter.Parameter;
import integratedtoolkit.types.resources.ServiceResourceDescription;

public class ServiceImplementation extends Implementation<ServiceResourceDescription> {

    private final String operation;

    public ServiceImplementation(int coreId, String namespace, String service, String port, String operation) {
        super(coreId, 0, null);
        this.requirements = new ServiceResourceDescription(service, namespace, port);
        this.operation = operation;
    }

    @Override
    public Type getType() {
        return Type.SERVICE;
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

    public String toString() {
        ServiceResourceDescription description = this.requirements;
        return super.toString()
                + " Service in namespace " + description.getNamespace()
                + " with name " + description.getPort()
                + " on port " + description.getPort()
                + "and operation " + operation;
    }

}
