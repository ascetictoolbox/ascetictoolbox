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

import integratedtoolkit.types.Implementation;
import integratedtoolkit.types.Service;

public class ServiceInstance extends Resource {

    private String wsdl;
    private String serviceName;
    private String namespace;
    private String port;

    public ServiceInstance() {
        super();
    }

    public ServiceInstance(String wsdl, String name, String namespace, String port, int maxTaskCount) {
        super(maxTaskCount);
        this.wsdl = wsdl;
        this.serviceName = name;
        this.namespace = namespace;
        this.port = port;
    }

    public void setWsdl(String wsdl) {
        this.wsdl = wsdl;
    }

    public String getWsdl() {
        return wsdl;
    }

    public void setServiceName(String serviceName) {
        this.serviceName = serviceName;
    }

    public String getServiceName() {
        return serviceName;
    }

    public void setNamespace(String namespace) {
        this.namespace = namespace;
    }

    public String getNamespace() {
        return namespace;
    }

    public void setPort(String port) {
        this.port = port;
    }

    public String getPort() {
        return this.port;
    }

    @Override
    public void setName(String name) {
        wsdl = name;
    }

    @Override
    public String getName() {
        return wsdl;
    }

    @Override
    public Integer fitCount(Implementation impl) {
        return Integer.MAX_VALUE;
    }

    @Override
    boolean checkResource(ResourceDescription consumption) {
        return true;
    }

    @Override
    void reserveResource(ResourceDescription consumption) {
    }

    @Override
    void releaseResource(ResourceDescription consumption) {
    }

    @Override
    public Type getType() {
        return Type.SERVICE;
    }

    @Override
    public String getMonitoringData(String prefix) {
        return "";
    }

    @Override
    public int compareTo(Resource t) {
        if (t == null) {
            throw new NullPointerException();
        }
        switch (t.getType()) {
            case SERVICE:
                return getName().compareTo(t.getName());
            case WORKER:
                return -1;
            default:
                return getName().compareTo(t.getName());
        }
    }

    @Override
    public boolean canRun(Implementation implementation) {
        switch (implementation.getType()) {
            case SERVICE:
                Service s = (Service) implementation;
                return (this.namespace.compareTo(s.getNamespace()) == 0
                        && this.serviceName.compareTo(s.getServiceName()) == 0
                        && this.port.compareTo(s.getPortName()) == 0);
            default:
                return false;
        }
    }

    @Override
    public void update(ResourceDescription resDesc) {
        //Do nothing
    }

    @Override
    public boolean markToRemove(ResourceDescription rd) {
        //Do nothing
        return true;
    }

    @Override
    public void confirmRemoval(ResourceDescription modification) {
        //Do nothing
    }

    @Override
    public boolean isAvailable(ResourceDescription rd) {
        //Do nothing
        return true;
    }

}
