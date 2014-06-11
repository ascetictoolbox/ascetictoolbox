
package es.bsc.monitoring.ganglia.infrastructure;

import java.util.List;


/**

 *
 * Configuration element for the Ganglia cluster mapping when the query "/cluster_name?filter=summary" is performed. Contains status information
 * of a particular cluster.
 *
 *  <CLUSTER NAME="" LOCALTIME="" OWNER="" LATLONG="" URL="">
 *   <HOSTS UP="" DOWN="" SOURCE=""/>
 *      <METRIC NAME="" VAL="" TYPE="" UNITS="" TN="" TMAX="" DMAX="" SLOPE="" SOURCE="">
 *      ............
 *      <METRIC NAME=....
 *  </CLUSTER>
 * 
 * @author Mauro Canuto <mauro.canuto@bsc.es>
 */
public class ClusterSummary {

    private String name;
    private String localtime;
    private String owner;
    private String latLong;
    private String url;
    private List<Metric> metrics;
    private HostsSummary hosts;

    public ClusterSummary(String name, String localtime, String owner, String latLong, String url) {
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

    public List<Metric> getMetrics() {
        return metrics;
    }

    public void setMetrics(List<Metric> metrics) {
        this.metrics = metrics;
    }

    public HostsSummary getHosts() {
        return hosts;
    }

    public void setHosts(HostsSummary hosts) {
        this.hosts = hosts;
    }


}
