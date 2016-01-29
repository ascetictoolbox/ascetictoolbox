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

import es.bsc.demiurge.core.configuration.Config;
import es.bsc.demiurge.core.monitoring.exceptions.MonitoringException;
import org.apache.commons.configuration.Configuration;

import java.io.IOException;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Class that returns metrics from Ganglia
 *
 * @author Mario Macías (http://github.com/mariomac), Mauro Canuto <mauro.canuto@bsc.es>
 */
class Ganglia {


    private String gangliaCollectorIP;
    private int gangliaPort;
    private int gangliaPortQuery;
    private String gangliaFrontEndPath;

	private static final String GANGLIA_COLLECTOR_IP = "ganglia.collectorIP";
	private static final String GANGLIA_PORT = "ganglia.port";
	private static final String GANGLIA_PORT_QUERY = "ganglia.portQuery";
	private static final String GANGLIA_FRONT_END_PATH = "ganglia.frontEndPath";

    // Class constructor
    public Ganglia() {
        //read the configuration variables to create the testing VM from the config.properties file

		Configuration config = Config.INSTANCE.getConfiguration();

		if(!config.containsKey(GANGLIA_COLLECTOR_IP)
				&& !config.containsKey(GANGLIA_FRONT_END_PATH)
				&& !config.containsKey(GANGLIA_PORT)
				&& !config.containsKey(GANGLIA_PORT_QUERY)) {
			throw new RuntimeException("The configuration file " + Config.INSTANCE.getConfigurationFileName()
					+ " must contain the next properties: " + GANGLIA_COLLECTOR_IP + ", " + GANGLIA_FRONT_END_PATH
					+ ", " + GANGLIA_PORT+ ", " + GANGLIA_PORT_QUERY);
		}
        this.gangliaCollectorIP = config.getString(GANGLIA_COLLECTOR_IP);
        this.gangliaPort = config.getInt(GANGLIA_PORT);
        this.gangliaPortQuery = config.getInt(GANGLIA_PORT_QUERY);
        this.gangliaFrontEndPath = config.getString(GANGLIA_FRONT_END_PATH);

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
            String xml = p.queryGanglia("/" + clusterName + GangliaMetKeys.QUERY_SUMMARY);
            grid = p.parseGangliaSummaryXml(xml);

        } catch (MonitoringException ex) {
            Logger.getLogger(Ganglia.class.getName()).log(Level.SEVERE, null, ex);
        }

        return grid;
    }


    public void fetchData(String clusterName, String hostName, String metric, Integer startTime) throws MalformedURLException, IOException{

    }
}
