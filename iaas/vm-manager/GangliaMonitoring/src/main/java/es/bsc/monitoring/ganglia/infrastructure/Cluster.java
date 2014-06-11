
package es.bsc.monitoring.ganglia.infrastructure;

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
public class Cluster {

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