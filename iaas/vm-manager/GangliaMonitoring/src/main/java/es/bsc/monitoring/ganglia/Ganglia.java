package es.bsc.monitoring.ganglia;

import es.bsc.monitoring.exceptions.MonitoringException;
import static es.bsc.monitoring.ganglia.configuration.GangliaMetKeys.QUERY_SUMMARY;
import es.bsc.monitoring.ganglia.infrastructure.Cluster;
import es.bsc.monitoring.ganglia.infrastructure.ClusterSummary;
import es.bsc.monitoring.ganglia.infrastructure.Host;
import es.bsc.monitoring.ganglia.parsing.ParseGanglia;
import java.io.IOException;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Class that returns metrics from Ganglia
 *
 * @author Mauro Canuto <mauro.canuto@bsc.es>
 */
public class Ganglia {

    private String gangliaCollectorIP;
    private int gangliaPort;
    private int gangliaPortQuery;
    private String gangliaFrontEndPath;
    
    // Class constructor
    public Ganglia() {
        //read the configuration variables to create the testing VM from the config.properties file
        Properties prop = new Properties();

        try {
            prop.load(ParseGanglia.class.getClassLoader().getResourceAsStream("ganglia_connection.properties"));
        } catch (IOException e) {
            try {
                throw new MonitoringException("Unable to read the configuration properties for ganglia connection"
                        + e.getMessage());
            } catch (MonitoringException ex) {
                Logger.getLogger(Ganglia.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        
        this.gangliaCollectorIP = prop.getProperty("collectorIP");
        this.gangliaPort = Integer.parseInt(prop.getProperty("gangliaPort"));
        this.gangliaPortQuery = Integer.parseInt(prop.getProperty("gangliaPortQuery"));
        this.gangliaFrontEndPath = prop.getProperty("gangliaFrontEndPath");
    }

    /*
     *
     * Get the whole grid;  grid -> clusters -> hosts
     * Returns the list of Clusters in the Grid
     */
    public ArrayList<Cluster> getGridInfo() {

        String gangliaXml = null;
        ArrayList<Cluster> grid = new ArrayList<Cluster>();
        try {
            // 
            ParseGanglia p = new ParseGanglia(gangliaCollectorIP, gangliaPort, gangliaPortQuery);
            gangliaXml = p.getGangliaXml();

            grid = (ArrayList<Cluster>) p.parseGangliaXml(gangliaXml);

        } catch (MonitoringException ex) {
            Logger.getLogger(Ganglia.class.getName()).log(Level.SEVERE, null, ex);
        }
        return grid;
    }

    /*
     *
     * Get info of a specific cluster;  grid -> clusters
     */
    public Cluster getClusterInfo(String clusterName) {

        String gangliaXml = null;
        ArrayList<Cluster> cluster_list = new ArrayList<Cluster>();

        Cluster ret_cluster = null;
        try {
            // 
            ParseGanglia p = new ParseGanglia(gangliaCollectorIP, gangliaPort, gangliaPortQuery);
            gangliaXml = p.queryGanglia("/" + clusterName);
            cluster_list = (ArrayList<Cluster>) p.parseGangliaXml(gangliaXml);

        } catch (MonitoringException ex) {
            Logger.getLogger(Ganglia.class.getName()).log(Level.SEVERE, null, ex);
        }

        for (Cluster cluster : cluster_list) {

            if (cluster.getName().equals(clusterName)) {
                ret_cluster = cluster;
            }
        }

        return ret_cluster;
    }

    /*
     *
     * Get metrics of a specific host;  grid -> clusters -> host
     *
     */
    public Host getHostInfo(String clusterName, String hostName) {

        String gangliaXml = null;
        ArrayList<Cluster> cluster_list = new ArrayList<Cluster>();
        Host ret_host = null;
        try {

            ParseGanglia p = new ParseGanglia(gangliaCollectorIP, gangliaPort, gangliaPortQuery);
            gangliaXml = p.queryGanglia("/" + clusterName + "/" + hostName);
            cluster_list = (ArrayList<Cluster>) p.parseGangliaXml(gangliaXml);

        } catch (MonitoringException ex) {
            Logger.getLogger(Ganglia.class.getName()).log(Level.SEVERE, null, ex);
        }

        for (Cluster cluster : cluster_list) {
            if (cluster.getName().equals(clusterName)) {
                for (Host host : cluster.getHosts()) {

                    if (host.getName().equals(hostName)) {
                        ret_host = host;
                    }
                }
            }

        }

        return ret_host;
    }

    /*
     *
     * Get aggregated metrics of a specific cluster;  grid -> clusters 
     * 
     */
    public ClusterSummary getClusterSummaryInfo(String clusterName) {
        ClusterSummary grid = null;
        try {
            ParseGanglia p = new ParseGanglia(gangliaCollectorIP, gangliaPort, gangliaPortQuery);
            String xml = p.queryGanglia("/" + clusterName + QUERY_SUMMARY);
            grid = p.parseGangliaSummaryXml(xml);

        } catch (MonitoringException ex) {
            Logger.getLogger(Ganglia.class.getName()).log(Level.SEVERE, null, ex);
        }

        return grid;
    }


    public void fetchData(String clusterName, String hostName, String metric, Integer startTime) throws MalformedURLException, IOException{
        
        
        
        
        
    }
    
    

}
