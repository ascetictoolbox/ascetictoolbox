package eu.ascetic.paas.applicationmanager.vmmanager.datamodel;

import java.util.Date;

/**
 * 
 * Copyright 2014 ATOS SPAIN S.A. 
 * 
 * Licensed under the Apache License, Version 2.0 (the License);
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * 
 * @author David Rojo. Atos Research and Innovation, Atos SPAIN SA
 * e-mail david.rojoa@atos.net 
 *
 * VM that has been deployed in the cloud middleware.
 *
 * @author David Ortiz Lopez (david.ortiz@bsc.es)
 *
 */
public class VmDeployed extends Vm {

    private String id;
    private String ipAddress;
    private String state;
    private Date created;
    private String hostName;

    public VmDeployed(String name, String image, int cpus, int ramMb,
            int diskGb, String initScript, String applicationId, String id,
            String ipAddress, String state, Date created, String hostName) {
        super(name, image, cpus, ramMb, diskGb, initScript, applicationId);
        this.id = id;
        this.ipAddress = ipAddress;
        this.state = state;
        this.created = created;
        this.hostName = hostName;
    }

    public String getId() {
        return id;
    }

    public String getIpAddress() {
        return ipAddress;
    }

    public String getState() {
        return state;
    }

    public Date getCreated() {
        return created;
    }

    public String getHostName() {
        return hostName;
    }

}
