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

package es.bsc.demiurge.monitoring.ganglia;

import java.util.List;

/**
 *
 * Configuration element for the Ganglia cluster mapping. Contains hosts' status information
 * of a particular cluster.
 *
 *  <CLUSTER NAME="" LOCALTIME="" OWNER="" LATLONG="" URL="">
 *      <HOST NAME=....
 *      ...
 *      <HOST NAME=..
 *  </CLUSTER>
 * 
 * @author Mauro Canuto <mauro.canuto@bsc.es>
 * 
 */
class Cluster {

    private String name;
    private String localtime;
    private String owner;
    private String latLong;
    private String url;
    private List<Host> hosts;

    public Cluster(String name, String localtime, String owner, String latLong, String url) {
        this.name = name;
        this.localtime = localtime;
        this.owner = owner;
        this.latLong = latLong;
        this.url = url;
    }

    public String getName() {
        return name;
    }

    public String getOwner() {
        return owner;
    }

    public String getLocaltime() {
        return localtime;
    }

    public String getLatLong() {
        return latLong;
    }

    public String getUrl() {
        return url;
    }

    public List<Host> getHosts() {
        return hosts;
    }

    public void setHosts(List<Host> hosts) {
        this.hosts = hosts;
    }
}