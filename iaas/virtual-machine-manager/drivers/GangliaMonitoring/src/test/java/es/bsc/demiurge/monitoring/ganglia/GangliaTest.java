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

import es.bsc.demiurge.monitoring.ganglia.Ganglia;
import es.bsc.demiurge.monitoring.ganglia.Cluster;
import es.bsc.demiurge.monitoring.ganglia.ClusterSummary;
import es.bsc.demiurge.monitoring.ganglia.Host;
import junit.framework.Assert;
import org.junit.Ignore;
import org.junit.Test;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author Mauro Canuto (mauro.canuto@bsc.es)
 */
@Ignore
public class GangliaTest {

    String clusterName = "testing-cloud";
    String hostName = "bscgrid29";

    @Test
    public void gridTest() {

        System.out.println("----- GridTest -----");

        Ganglia g = new Ganglia();
        ArrayList<Cluster> cluster_list;
        cluster_list = g.getGridInfo();

        for (Cluster cluster : cluster_list) {

            System.out.println("Cluster Name: " + cluster.getName());
            List<Host> hosts = cluster.getHosts();

            for (Host host : hosts) {
               /* System.out.println("------- Host Name: " + host.getName() + "---------");

                Float load_one = host.getLoadOne();
                Assert.assertTrue(load_one >= 0);
                System.out.println("Load_one: " + load_one);

                Float mem_total = host.getMemTotal();
                Assert.assertTrue(mem_total >= 0);
                System.out.println("Mem_total: " + mem_total);

                Float mem_free = host.getMemFree();
                Assert.assertTrue(mem_free >= 0);
                System.out.println("Mem_Free: " + mem_free);

                Float mem_used = host.getMemUsed();
                Assert.assertTrue(mem_used >= 0);
                System.out.println("Mem_Used: " + mem_used);
                */
                Assert.assertTrue(host.getLoadFive() >= 0);
                Assert.assertTrue(host.getLoadFifteen() >= 0);
                Assert.assertTrue(host.getCPUUser() >= 0);
                Assert.assertTrue(host.getCPUNice() >= 0);
                Assert.assertTrue(host.getCPUSystem() >= 0);
                Assert.assertTrue(host.getCPUIdle() >= 0);
                Assert.assertTrue(host.getCPUWio() >= 0);

                Assert.assertTrue(host.getDiskTotal() >= 0);
                Assert.assertTrue(host.getDiskFree() >= 0);
                Assert.assertTrue(host.getDiskUsed() >= 0);

            }
        }
        System.out.println("----------");

    }

	@Ignore
    @Test
    public void clusterTest() {

        System.out.println("----- ClusterTest -----");

        Ganglia g = new Ganglia();
        Cluster c = g.getClusterInfo(clusterName);

        if (c == null) {
            System.out.println("No cluster '" + clusterName + "' found");
        } else {

            System.out.println("Number of hosts in cluster " + clusterName + ": " + c.getHosts().size());
            System.out.println("Name: " + c.getName());

            for (Host host : c.getHosts()) {
               /* System.out.println("------- Host Name: " + host.getName() + "---------");

                Float load_one = host.getLoadOne();
                Assert.assertTrue(load_one >= 0);
                System.out.println("Load_one: " + load_one);

                Float mem_total = host.getMemTotal();
                Assert.assertTrue(mem_total >= 0);
                System.out.println("Mem_total: " + mem_total);

                Float mem_free = host.getMemFree();
                Assert.assertTrue(mem_free >= 0);
                System.out.println("Mem_Free: " + mem_free);

                Float mem_used = host.getMemUsed();
                Assert.assertTrue(mem_used >= 0);
                System.out.println("Mem_Used: " + mem_used);*/

                Assert.assertTrue(host.getLoadFive() >= 0);
                Assert.assertTrue(host.getLoadFifteen() >= 0);
                Assert.assertTrue(host.getCPUUser() >= 0);
                Assert.assertTrue(host.getCPUNice() >= 0);
                Assert.assertTrue(host.getCPUSystem() >= 0);
                Assert.assertTrue(host.getCPUIdle() >= 0);
                Assert.assertTrue(host.getCPUWio() >= 0);

                Assert.assertTrue(host.getDiskTotal() >= 0);
                Assert.assertTrue(host.getDiskFree() >= 0);
                Assert.assertTrue(host.getDiskUsed() >= 0);
            }
        }

        System.out.println("----------");
    }

    @Test
    public void hostTest() {

        System.out.println("----- HostTest -----");

        Ganglia g = new Ganglia();
        Host host = g.getHostInfo(clusterName, hostName);

        if (host == null) {
            System.out.println("No host '" + hostName + "' found in cluster '" + clusterName);
        } else {
            System.out.println("Host '" + hostName + "'");

            /*
            Float load_one = host.getLoadOne();
            Assert.assertTrue(load_one >= 0);
            System.out.println("Load_one: " + load_one);

            Float mem_total = host.getMemTotal();
            Assert.assertTrue(mem_total >= 0);
            System.out.println("Mem_total: " + mem_total);

            Float mem_free = host.getMemFree();
            Assert.assertTrue(mem_free >= 0);
            System.out.println("Mem_Free: " + mem_free);

            Float mem_used = host.getMemUsed();
            Assert.assertTrue(mem_used >= 0);
            System.out.println("Mem_Used: " + mem_used); */

            Assert.assertTrue(host.getLoadFive() >= 0);
            Assert.assertTrue(host.getLoadFifteen() >= 0);
            Assert.assertTrue(host.getCPUUser() >= 0);
            Assert.assertTrue(host.getCPUNice() >= 0);
            Assert.assertTrue(host.getCPUSystem() >= 0);
            Assert.assertTrue(host.getCPUIdle() >= 0);
            Assert.assertTrue(host.getCPUWio() >= 0);

            Assert.assertTrue(host.getDiskTotal() >= 0);
            Assert.assertTrue(host.getDiskFree() >= 0);
            Assert.assertTrue(host.getDiskUsed() >= 0);

        }
        System.out.println("----------");
    }

    @Test
    public void clusterSummaryTest() {
        System.out.println("----- clusterSummaryTest -----");

        Ganglia g = new Ganglia();
        ClusterSummary summary = g.getClusterSummaryInfo(clusterName);

        if (summary == null) {
            System.out.println("It has not been possible to retrieve summary information");
        } else {
            System.out.println("Number of aggregated metrics : " + summary.getMetrics().size());
        }

        System.out.println("----------");
    }




    //@Test
    public void fetchDataTest() throws IOException{


    }
}
