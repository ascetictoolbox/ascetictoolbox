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

package integratedtoolkit.types.resources;

import integratedtoolkit.types.Implementation;
import integratedtoolkit.types.Implementation.Type;
import org.w3c.dom.Node;

public class ServiceResourceDescription extends WorkerResourceDescription {

    private final String serviceName;
    private final String namespace;
    private final String port;

    public ServiceResourceDescription(String serviceName, String namespace, String port) {
        this.serviceName = serviceName;
        this.namespace = namespace;
        this.port = port;
    }

    public ServiceResourceDescription(Node n) {
        super(n);
        this.serviceName = "";
        this.namespace = "";
        this.port = "";
    }

    public String getPort() {
        return port;
    }

    public String getNamespace() {
        return namespace;
    }

    public String getServiceName() {
        return serviceName;
    }

    @Override
    public boolean canHost(Implementation impl) {
        if (impl.getType() == Type.SERVICE) {
            ServiceResourceDescription s = (ServiceResourceDescription) impl.getRequirements();
            return s.getServiceName().compareTo(serviceName) == 0
                    && s.getNamespace().compareTo(namespace) == 0
                    && s.getPort().compareTo(port) == 0;
        }
        return false;
    }

    public String toString() {
        return "[SERVICE "
                + "NAMESPACE=" + this.namespace + " "
                + "SERVICE_NAME=" + this.getServiceName() + " "
                + "PORT=" + this.port
                + "]";
    }
}
