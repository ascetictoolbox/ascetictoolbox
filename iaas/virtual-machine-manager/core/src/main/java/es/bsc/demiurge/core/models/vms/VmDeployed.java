/**
 Copyright (C) 2013-2014  Barcelona Supercomputing Center

 This library is free software; you can redistribute it and/or
 modify it under the terms of the GNU Lesser General Public
 License as published by the Free Software Foundation; either
 version 2.1 of the License, or (at your option) any later version.

 This library is distributed in the hope that it will be useful,
 but WITHOUT ANY WARRANTY; without even the implied warranty of
 MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 Lesser General Public License for more details.

 You should have received a copy of the GNU Lesser General Public
 License along with this library; if not, write to the Free Software
 Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301  USA
 */

package es.bsc.demiurge.core.models.vms;

import java.util.Date;

/**
 * VM that has been deployed in the cloud middleware.
 *
 * @author Mario Macias (github.com/mariomac), David Ortiz Lopez (david.ortiz@bsc.es)
 *
 */
public class VmDeployed extends Vm {

    private final String id;
    private final String ipAddress;
    private final String state;
    private final Date created;
    private String hostName;

    public VmDeployed(String name, String image, int cpus, int ramMb,
            int diskGb, int swapMb, String initScript, String applicationId, String id,
            String ipAddress, String state, Date created, String hostName) {
        super(name, image, cpus, ramMb, diskGb, swapMb, initScript, applicationId);
        this.id = id;
        this.ipAddress = ipAddress;
        this.state = state;
        this.created = new Date(created.getTime());
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
        return new Date(created.getTime());
    }

    public String getHostName() {
        return hostName;
    }

    public void setHostName(String hostname) {
        this.hostName = hostname;
    }

}
